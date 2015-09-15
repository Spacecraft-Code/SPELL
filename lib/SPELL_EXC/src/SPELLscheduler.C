// ################################################################################
// FILE       : SPELLscheduler.C
// DATE       : Mar 17, 2011
// PROJECT    : SPELL
// DESCRIPTION: Implementation of the executor scheduler
// --------------------------------------------------------------------------------
//
//  Copyright (C) 2008, 2015 SES ENGINEERING, Luxembourg S.A.R.L.
//
//  This file is part of SPELL.
//
// SPELL is free software: you can redistribute it and/or modify
// it under the terms of the GNU General Public License as published by
// the Free Software Foundation, either version 3 of the License, or
// (at your option) any later version.
//
// SPELL is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
// GNU General Public License for more details.
//
// You should have received a copy of the GNU General Public License
// along with SPELL. If not, see <http://www.gnu.org/licenses/>.
//
// ################################################################################

// FILES TO INCLUDE ////////////////////////////////////////////////////////
// System includes ---------------------------------------------------------
// Local includes ----------------------------------------------------------
#include "SPELL_EXC/SPELLscheduler.H"
#include "SPELL_EXC/SPELLexecutor.H"
#include "SPELL_EXC/SPELLexecutorStatus.H"
#include "SPELL_EXC/SPELLexecutorUtils.H"
// Project includes --------------------------------------------------------
#include "SPELL_WRP/SPELLpyHandle.H"
#include "SPELL_SYN/SPELLmonitor.H"
#include "SPELL_UTIL/SPELLlog.H"
#include "SPELL_UTIL/SPELLpythonHelper.H"
#include "SPELL_UTIL/SPELLpythonMonitors.H"
#include "SPELL_UTIL/SPELLutils.H"
#include "SPELL_WRP/SPELLconstants.H"
#include "SPELL_WRP/SPELLregistry.H"
using namespace LanguageModifiers;


//=============================================================================
// CONSTRUCTOR    : SPELLscheduler::SPELLscheduler
//=============================================================================
SPELLscheduler::SPELLscheduler( bool useSafeCalls )
: SPELLschedulerIF()
{
    // Set the event, we do not want to block anything yet
    m_waitingEvent.set();

    m_useSafeCalls = useSafeCalls;

    // Initial values
    m_timer = NULL;
    m_condition.reset();
    m_result.reset();
    m_silentCheck = false;
    m_defaultPromptWarningDelay = 30;

    DEBUG("[SCH] Scheduler created");
}

//=============================================================================
// DESTRUCTOR    : SPELLscheduler::~SPELLscheduler
//=============================================================================
SPELLscheduler::~SPELLscheduler()
{
    m_pyTM = NULL;
    delete m_timer;
}

//=============================================================================
// METHOD    : SPELLscheduler::startWait
//=============================================================================
void SPELLscheduler::startWait( const SPELLscheduleCondition& condition )
{
    DEBUG("[SCH] Starting wait");

	// If there is a condition already ongoing, cancel it
	if (m_condition.type != SPELLscheduleCondition::SCH_NONE)
	{
		finishWait(false,false);
	}

    // Store the condition data
    m_condition = condition;

    DEBUG("		Condition type: " + ISTR(m_condition.type));
    DEBUG("     Target time   : " + m_condition.targetTime.toString());
    DEBUG("		Expression    : " + m_condition.expression);
    DEBUG("		Message       : " + m_condition.message);
    DEBUG("		Period        : " + m_condition.period.toString());
    DEBUG("		Timeout       : " + m_condition.timeout.toString());
    DEBUG("		List          : " + PYREPR(m_condition.getVerification()));
    DEBUG("		Configuration : " + PYREPR(m_condition.getConfig()));
    DEBUG("		Prompt user   : " + BSTR(m_condition.promptUser));


	// Reset filters
	m_lastNotifiedItem.name = "";
	m_lastNotifiedItem.value ="";

	// Reset the condition result
    m_result.reset();

    // Set the language lock so that command reception is on hold
    setLanguageLock(STATUS_WAITING);

    // Store the start time and last notification time
    m_checkStartTime.setCurrent();
    m_lastNotificationTime.setCurrent();

    // Read the first notification period if there is any, otherwise use
    // the default 0
    if (m_condition.interval.size()>0)
    {
        m_notificationPeriodIndex = 0;
        m_notificationPeriod = m_condition.interval[0];
        DEBUG("[SCH] Initial notification period: " + m_notificationPeriod.toString());
    }
    else
    {
        DEBUG("[SCH] No notification period");
        m_notificationPeriod = SPELLtime(0,0,true);
    }

    // Depending on the condition type
    if(m_condition.type == SPELLscheduleCondition::SCH_VERIFICATION)
    {
        DEBUG("[SCH] Using verification condition");
        message("Waiting for telemetry condition");
        // Obtain the TM interface handle
        m_pyTM = SPELLregistry::instance().get("TM");
        Py_INCREF(m_pyTM);
        // Start the checking timer
        startTimer();
    }
    else if (m_condition.type == SPELLscheduleCondition::SCH_EXPRESSION)
    {
        DEBUG("[SCH] Using expression condition");
        message("Waiting for expression condition");
        // Start the checking timer
        startTimer();
    }
    else if (m_condition.type == SPELLscheduleCondition::SCH_CHILD)
    {
        DEBUG("[SCH] Using child condition");
        message("Waiting for child procedure");
        // Start the checking timer
        startTimer();
    }
    else if (m_condition.type == SPELLscheduleCondition::SCH_TIME)
    {
        // Start messages are different for relative and absolute time conditions
        if (m_condition.targetTime.isDelta())
        {
            DEBUG("[SCH] Using relative time condition");
            SPELLtime currentTime;
            m_checkTargetTime = currentTime + m_condition.targetTime;
            std::string remainingStr = (m_checkTargetTime - m_checkStartTime).toString();
            message("Starting countdown. Time left " + remainingStr);
        }
        else
        {
            DEBUG("[SCH] Using absolute time condition");
            m_checkTargetTime = m_condition.targetTime;
            std::string remainingStr = (m_checkTargetTime - m_checkStartTime).toString();
            std::string targetStr = m_checkTargetTime.toString();
            message("Waiting until " + targetStr + ", time left: " + remainingStr );
        }
        startTimer();
    }
    else if (m_condition.type == SPELLscheduleCondition::SCH_FIXED)
    {
    	DEBUG("[SCH] Using fixed condition");
        // Result will be success
        m_result.type = SPELLscheduleResult::SCH_SUCCESS;
    }
    else
    {
    	DEBUG("[SCH] Using NO condition");
    }
}

//=============================================================================
// METHOD    : SPELLscheduler::startPrompt
//=============================================================================
void SPELLscheduler::startPrompt( const SPELLtime& timeout, bool headless )
{
    DEBUG("[SCH] Starting prompt (to: " + timeout.toString() + ")");

	// If there is a condition already ongoing, cancel it
	if (m_condition.type != SPELLscheduleCondition::SCH_NONE)
	{
		finishWait(false,false);
	}

    // Store the condition data
	m_condition.reset();
    m_condition.type = SPELLscheduleCondition::SCH_PROMPT;
    // Set no period. This will ensure a check of 1 second once the prompt starts
    m_condition.period.set(0,0);
    // Timeout of the prompt
    if ( timeout.getSeconds() == 0 )
    {
        DEBUG("[SCH] No timeout given from the adapter");
        DEBUG("[SCH] Will use the default: " + ISTR(m_defaultPromptWarningDelay) + " seconds.");
    	// Set the default delay if not given by the adapter
    	m_condition.timeout.set(m_defaultPromptWarningDelay,0);
    }
    else
    {
        DEBUG("[SCH] Timeout given from the adapter");
    	m_condition.timeout = timeout;
    }
    DEBUG("[SCH] Prompt timeout: " + m_condition.timeout.toString());

	// Reset filters
	m_lastNotifiedItem.name = "";
	m_lastNotifiedItem.value ="";

	// Reset the condition result
    m_result.reset();

    // Set the language lock so that command reception is on hold
    setLanguageLock(STATUS_PROMPT);

    // Store the start time and last notification time
    m_checkStartTime.setCurrent();
    m_lastNotificationTime.setCurrent();
	m_notificationPeriod = SPELLtime(0,0,true);
	m_result.type = SPELLscheduleResult::SCH_SUCCESS;

	// If the procedure is headless raise a warning right away
	if (headless)
	{
		raiseEvent("Background procedure " + SPELLexecutor::instance().getInstanceId() + " awaiting prompt answer");
	}

	// Start the timer to check a prompt timeout
	startTimer();
    DEBUG("[SCH] Prompt started");
}

//=============================================================================
// METHOD    : SPELLscheduler::startTimer
//=============================================================================
void SPELLscheduler::startTimer()
{
	DEBUG("Starting timer");
    if (m_timer != NULL)
    {
    	DEBUG("Deleting previous timer");
    	m_abortTimer = true;
    	{
    		SPELLsafeThreadOperations tops("SPELLscheduler::startTimer()");
    		if (m_timer->isCounting())
    		{
    			m_timer->cancel();
    		}
    		m_timer->join();
    	}
    	delete m_timer;
    	m_timer = NULL;
    	DEBUG("Previous timer deleted");
    }
    m_abortTimer = false;
    // Start a timer to check the condition
    // If a period is given, use it. Otherwise use a default
    if (m_condition.period>0)
    {
    	DEBUG("Using timer period: " + m_condition.period.toString() );
        m_timer = new SPELLtimer( m_condition.period.getSeconds() +
                                  m_condition.period.getMilliseconds()*1000, *this );
    }
    else
    {
    	DEBUG("Using default period of 1 second");
        SPELLtime defaultPeriod(1,0,true);
        m_timer = new SPELLtimer( 200, *this );
    }
    m_timer->start();
}

//=============================================================================
// METHOD    : SPELLscheduler::timerCallback
//=============================================================================
bool SPELLscheduler::timerCallback( unsigned long elapsed )
{
    bool abortCheck = false;
    // If abort has been requested, just return true at the end of the method
    if (m_abortTimer)
    {
        DEBUG("[SCH] Timer callback: abort timer");
        abortCheck = true;
    }
    else
    {
    	SPELLsafePythonOperations ops("SPELLscheduler::timerCallback()");
        // Depending on the condition type, we perform one check or another
        switch(m_condition.type)
        {
        case SPELLscheduleCondition::SCH_TIME:
        case SPELLscheduleCondition::SCH_PROMPT:
        {
            abortCheck = checkTime();
            break;
        }
        case SPELLscheduleCondition::SCH_EXPRESSION:
        {
            abortCheck = checkExpression();
            break;
        }
        case SPELLscheduleCondition::SCH_CHILD:
        {
            abortCheck = checkChildProcedure();
            break;
        }
        case SPELLscheduleCondition::SCH_VERIFICATION:
        {
            abortCheck = checkVerification();
            break;
        }
        default:
            break;
        }

        // Periodic notifications with message and interval. We shall not be
        // aborting, there shall be a defined notification period, a message
        // to show shall be available and the time since the last notification
        // shall be greater than the current period.
        SPELLtime currentTime;
        if (not abortCheck &&
                (m_notificationPeriod>0) &&
                (m_condition.message != "") &&
                (currentTime - m_lastNotificationTime)>=m_notificationPeriod )
        {
            DEBUG("[SCH] Issuing user message");
        	SPELLexecutor::instance().getCIF().write(m_condition.message, LanguageConstants::SCOPE_PROC );

            // This is the last notification time now
            m_lastNotificationTime.setCurrent();

            // Depending on the remaining time, we search for the next
            // notification period (Interval modifier) if any
            SPELLtime remainingTime = m_checkTargetTime - currentTime;
            if (remainingTime<=m_notificationPeriod)
            {
                DEBUG("[SCH] Go to next notification period");
                m_notificationPeriodIndex++;
                if (m_condition.interval.size()>m_notificationPeriodIndex)
                {
                    m_notificationPeriod = m_condition.interval[m_notificationPeriodIndex];
                    DEBUG("[SCH] Next notification period: " + m_notificationPeriod.toString());
                }
            }
        }
    }
    if (abortCheck)
    {
        // Declare the condition as fullfilled
        finishWait(true,false);
    }
    return abortCheck;
}

//=============================================================================
// METHOD    : SPELLscheduler::notifyTime
//=============================================================================
void SPELLscheduler::notifyTime( bool finished, bool success )
{
    if (m_silentCheck) return;

    if (!finished)
    {
        SPELLtime currentTime;
        std::string remaining = (m_checkTargetTime - currentTime).toString();
        ItemNotification item;
        item.type = NOTIFY_TIME;
        item.name = "COUNTDOWN";
        item.value = remaining;
        item.comment = "Target time: " + m_checkTargetTime.toString();
        item.status = "WAITING";
        notify( item );
    }
    else if (success)
    {
        message("Target time reached");
        ItemNotification item;
        item.type = NOTIFY_TIME;
        item.name = "COUNTDOWN";
        item.value = "0";
		item.comment = "";
        item.status = "SUCCESS";
        notify( item );
    }
    else
    {
        message("Time condition not fullfilled");
        ItemNotification item;
        item.type = NOTIFY_TIME;
        item.name = "COUNTDOWN";
        item.value = "0";
        item.comment = "Target time: " + m_checkTargetTime.toString();
        item.status = "FAILED";
        notify( item );
    }
}

//=============================================================================
// METHOD    : SPELLscheduler::checkTime
//=============================================================================
bool SPELLscheduler::checkTime()
{
	SPELLmonitor m(m_checkLock);
    bool abortCheck = false;

	// Get the current time and compare it against the target time
	SPELLtime currentTime;

	if (m_condition.type == SPELLscheduleCondition::SCH_NONE)
    {
		abortCheck = true;
    }
	else if (m_condition.type == SPELLscheduleCondition::SCH_PROMPT)
    {
		SPELLtime diff = currentTime - m_checkStartTime;
		if (diff>m_condition.timeout)
		{
			raiseEvent("Procedure awaiting prompt answer");
			// Reset the start time
			m_checkStartTime = currentTime;
		}
    }
    else
    {
		if ( currentTime >= m_checkTargetTime )
		{
			DEBUG("[SCH] Timer callback: condition fullfilled");
			// Notify that the countdown has finished successfully
			notifyTime(true,true);
			// Cancel the timer loop
			abortCheck = true;
			// Set the result
			m_result.type = SPELLscheduleResult::SCH_SUCCESS;
		}
		else
		{
			// Time evolution notification
			notifyTime(false,false);
		}
    }
    return abortCheck;
}

//=============================================================================
// METHOD    : SPELLscheduler::raiseEvent
//=============================================================================
void SPELLscheduler::raiseEvent( const std::string& message )
{
	// Raise event
	try
	{
		PyObject* evIfc = SPELLregistry::instance().get("EV");
		if (evIfc != NULL)
		{
			SPELLpyHandle evMsg = SSTRPY(message);
			SPELLpyHandle severity = PyLong_FromLong(LanguageConstants::WARNING);
			SPELLpyHandle scope = PyLong_FromLong(LanguageConstants::SCOPE_SYS);
			SPELLpyHandle config = PyDict_New();
		    PyDict_SetItemString( config.get(), LanguageModifiers::Severity.c_str(), severity.get() );
		    PyDict_SetItemString( config.get(), LanguageModifiers::Scope.c_str(), scope.get() );
			SPELLpythonHelper::instance().callMethod(evIfc,"raiseEvent",evMsg.get(),config.get(),NULL);
			SPELLpythonHelper::instance().checkError();
		}
	}
	catch(SPELLcoreException& ex)
	{
		LOG_ERROR("Unable to inject event: " + ex.what());
	}
}

//=============================================================================
// METHOD    : SPELLscheduler::checkExpression
//=============================================================================
bool SPELLscheduler::checkExpression()
{
	SPELLmonitor m(m_checkLock);
    // If there is a timeout defined, check it first
    SPELLtime currentTime;
    if (m_condition.timeout>0)
    {
        SPELLtime checkTime = currentTime - m_checkStartTime;
        if (checkTime>m_condition.timeout)
        {
            DEBUG("[SCH] Checking expression timed out");
			// If PromptUser is false, do not mark it as failed but timeout
			if (m_condition.promptUser == false)
			{
	            DEBUG("[SCH] Set as timeout, no failure");
	            m_result.type = SPELLscheduleResult::SCH_TIMEOUT;
			}
			else
			{
	            DEBUG("[SCH] Set as timeout, failure");
	            m_result.type = SPELLscheduleResult::SCH_FAILED;
			}
            m_result.error = "Condition not fullfilled";
            m_result.reason = "Timed out";
            return true;
        }
    }
    DEBUG("[SCH] Checking expression " + m_condition.expression);
    // Reference counter is incremented inside eval
    PyObject* result = SPELLpythonHelper::instance().eval(m_condition.expression,false);
    SPELLpythonHelper::instance().checkError();
    DEBUG("[SCH] Expression result " + PYREPR(result));
    /** \todo protect errors and check result null */
    return (result == Py_True);
}

//=============================================================================
// METHOD    : SPELLscheduler::checkChildProcedure
//=============================================================================
bool SPELLscheduler::checkChildProcedure()
{
	SPELLmonitor m(m_checkLock);
    // If there is a timeout defined, check it first
    SPELLtime currentTime;
    if (m_condition.timeout>0)
    {
        SPELLtime checkTime = currentTime - m_checkStartTime;
        if (checkTime>m_condition.timeout)
        {
            DEBUG("[SCH] Checking child procedure timed out");
			if (m_condition.promptUser == false)
			{
	            DEBUG("[SCH] Set as timeout, no failure");
				m_result.type = SPELLscheduleResult::SCH_TIMEOUT;
			}
			else
			{
	            DEBUG("[SCH] Set as timeout, failure");
				m_result.type = SPELLscheduleResult::SCH_FAILED;
			}
            m_result.error = "Child procedure did not finish in time";
            m_result.reason = "Timed out";
            return true;
        }
    }
    SPELLexecutorStatus st = SPELLexecutor::instance().getChildManager().getChildStatus();
    switch(st)
    {
    case STATUS_ERROR:
    	{
    		DEBUG("[SCH] Child procedure failed");
    		m_result.type = SPELLscheduleResult::SCH_FAILED;
    		m_result.error = SPELLexecutor::instance().getChildManager().getChildError();
    		m_result.reason = SPELLexecutor::instance().getChildManager().getChildErrorReason();
    		return true;
    	}
    case STATUS_ABORTED:
		{
    		DEBUG("[SCH] Child procedure aborted");
    		m_result.type = SPELLscheduleResult::SCH_FAILED;
    		m_result.error = "Child procedure did not finish";
    		m_result.reason = "Status is aborted";
    		return true;
		}
    case STATUS_FINISHED:
		{
			DEBUG("[SCH] Child procedure finished");
			m_result.type = SPELLscheduleResult::SCH_SUCCESS;
			return true;
		}
    default:
		{
			break;
		}
    }
    return false;
}

//=============================================================================
// METHOD    : SPELLscheduler::checkVerification
//=============================================================================
bool SPELLscheduler::checkVerification()
{
	SPELLmonitor m(m_checkLock);
    // If there is a timeout defined, check it first
    SPELLtime currentTime;
    if (m_condition.timeout>0)
    {
        SPELLtime checkTime = currentTime - m_checkStartTime;
        DEBUG("[SCH] Checking condition timeout (elapsed: " + checkTime.toString() + "), timeout " + m_condition.timeout.toString());
        if (checkTime>m_condition.timeout)
        {
            DEBUG("[SCH] Checking verification timed out");
            // Notify to clients
            if (!m_silentCheck)
            {
                ItemNotification item;
                item.type = NOTIFY_VERIFICATION;
                item.name = "TM Condition";
                item.value = "";
                item.comment = "Condition timed out";
                item.status = "FAILED";
                notify( item );
            }

			if (m_condition.promptUser == false)
			{
	            DEBUG("[SCH] Set as timeout, no failure");
	            m_result.type = SPELLscheduleResult::SCH_TIMEOUT;
			}
			else
			{
	            DEBUG("[SCH] Set as timeout, failure");
	            m_result.type = SPELLscheduleResult::SCH_FAILED;
			}
            m_result.error = "Condition not fullfilled";
            m_result.reason = "Timed out";
            return true;
        }
    }
    bool success = false;
    DEBUG("[SCH] Checking verification");
    DEBUG("[SCH] Using TM list " + PYREPR(m_condition.getVerification()));
    DEBUG("[SCH] Using config " + PYREPR(m_condition.getConfig()));
    // Modify the configuration: we do not want to have a DriverException if the verification fails
    // This reference is stored in the condition
    PyObject* verifyConfig = m_condition.getConfig();
    Py_INCREF(verifyConfig);

    PyObject* pyPromptUser = Py_False;
    Py_XINCREF(pyPromptUser);
    PyObject* pyOnFalse = PyLong_FromLong(LanguageConstants::CANCEL);
    PyObject* pyRetries = PyLong_FromLong(0);

    PyDict_SetItemString( verifyConfig, LanguageModifiers::PromptUser.c_str(), pyPromptUser );
    PyDict_SetItemString( verifyConfig, LanguageModifiers::OnFalse.c_str(), pyOnFalse );
    PyDict_SetItemString( verifyConfig, LanguageModifiers::Retries.c_str(), pyRetries );
    PyDict_SetItemString( verifyConfig, LanguageModifiers::InWaitFor.c_str(), Py_True);

    // Perform the actual TM verification with the driver interface
    DEBUG("[SCH] Calling verify");
    SPELLexecutor::instance().getCIF().setManualMode(true);
    SPELLexecutor::instance().getCIF().setVerbosity(999);
    PyObject* tmResult = NULL;

    try
    {
    	// Call method returns a new reference
        DEBUG("[SCH] Invoke TM::verify");
		tmResult = SPELLpythonHelper::instance().callMethod(m_pyTM,"verify",m_condition.getVerification(),verifyConfig,NULL);
		Py_XINCREF(tmResult);
        DEBUG("[SCH] TM result obtained");
		// Check Python errors
		SPELLpythonHelper::instance().checkError();
	    SPELLexecutor::instance().getCIF().setManualMode(false);
	    SPELLexecutor::instance().getCIF().resetVerbosity();
    }
    catch(SPELLcoreException& ex)
    {
        SPELLexecutor::instance().getCIF().setManualMode(false);
        SPELLexecutor::instance().getCIF().resetVerbosity();
    	tmResult = NULL;
        if (!m_silentCheck)
        {
			// Notify to clients
			ItemNotification item;
			item.type = NOTIFY_VERIFICATION;
			item.name = "TM Condition";
			item.value = "";
			item.comment = "Unable to check condition";
			item.status = "FAILED";
			notify( item );
        }
		m_result.type = SPELLscheduleResult::SCH_FAILED;
		m_result.error = "Unable to check condition";
		m_result.reason = ex.what();
        // To finish the verification
        return true;
    }

    if (tmResult)
    {
        DEBUG("[SCH] Checking result");
        // Call method returns new reference
        PyObject* result = SPELLpythonHelper::instance().callMethod(tmResult,"__nonzero__",NULL);
        Py_XINCREF(result);
        SPELLpythonHelper::instance().checkError();
        DEBUG("[SCH] Evaluation result " + PYREPR(result) + PYREPR(PyObject_Type(result)));
        success = (result==Py_True);
    }
    DEBUG("[SCH] Final result " + BSTR(success));

    if (!m_silentCheck)
    {
        if (success)
        {
            SPELLexecutor::instance().getCIF().setManualMode(false);
            SPELLexecutor::instance().getCIF().resetVerbosity();
            // Notify to clients
            ItemNotification item;
            item.type = NOTIFY_VERIFICATION;
            item.name = "TM Condition";
            item.value = "";
            item.comment = "Condition fullfilled";
            item.status = "SUCCESS";
            notify( item );
            message("TM condition fullfilled");
            m_result.type = SPELLscheduleResult::SCH_SUCCESS;
        }
        else
        {
            // Notify to clients
            ItemNotification item;
            item.type = NOTIFY_VERIFICATION;
            item.name = "TM Condition";
            item.value = "";
            item.comment = "Elapsed time: " + (currentTime-m_checkStartTime).toString();
            item.status = "IN PROGRESS";
            notify( item );
        }
    }
    else
    {
    	if (success)
    	{
    		m_result.type = SPELLscheduleResult::SCH_SUCCESS;
    	}
    }
    return success;
}

//=============================================================================
// METHOD    : SPELLscheduler::abortWait
//=============================================================================
bool SPELLscheduler::abortWait( bool setStatus )
{
    DEBUG("[SCH] Aborting wait process");
	SPELLmonitor m(m_checkLock);
    bool aborted = false;
    bool isWaiting = waiting();
    if (setStatus) resetControllerStatus( isWaiting );
    if (isWaiting)
    {
        DEBUG("[SCH] Aborting while waiting");
        if (m_condition.type != SPELLscheduleCondition::SCH_FIXED && m_condition.type != SPELLscheduleCondition::SCH_PROMPT)
        {
            DEBUG("[SCH] Check aborted, cancelling timer");
            if (m_timer) m_timer->cancel();
            m_abortTimer = true;
            SPELLexecutor::instance().getCIF().resetVerbosity();
            m_result.type = SPELLscheduleResult::SCH_ABORTED;
        }
        aborted = true;
        m_condition.reset();
        // Releasing language lock shall be the last thing done
        releaseLanguageLock();
        DEBUG("[SCH] Wait aborted");
    }
    return aborted;
}

//=============================================================================
// METHOD    : SPELLscheduler::restartWait
//=============================================================================
void SPELLscheduler::restartWait()
{
	SPELLmonitor m(m_checkLock);
    if (waiting())
    {
        DEBUG("[SCH] Restarting wait process");
        SPELLexecutor::instance().getController().setStatus(STATUS_WAITING);
        if (m_condition.type != SPELLscheduleCondition::SCH_FIXED) m_timer->cont();
    	SPELLexecutor::instance().getCIF().write("Wait condition resumed", LanguageConstants::SCOPE_SYS );
        DEBUG("[SCH] Wait restarted");
    }
}

//=============================================================================
// METHOD    : SPELLscheduler::interruptWait
//=============================================================================
bool SPELLscheduler::interruptWait()
{
	SPELLmonitor m(m_checkLock);
    // We may continue condition processing, do not release the lock
    bool interrupted = false;
    if (waiting() || inPrompt())
    {
        DEBUG("[SCH] Interrupting wait process");
		// We do not want to report any interruption if the condition is of type FIXED.
		// Also, if it is not fixed, we want to stop the checker timer.
        if (m_condition.type != SPELLscheduleCondition::SCH_FIXED && !inPrompt())
		{
        	m_timer->stop();
            SPELLexecutor::instance().getController().setStatus(STATUS_INTERRUPTED);
        	SPELLexecutor::instance().getCIF().warning("Wait condition interrupted", LanguageConstants::SCOPE_SYS );
		}
        interrupted = true;
        DEBUG("[SCH] Wait interrupted");
    }
    return interrupted;
}

//=============================================================================
// METHOD    : SPELLscheduler::finishWait
//=============================================================================
void SPELLscheduler::finishWait( bool setStatus, bool keepLock )
{
	SPELLmonitor m(m_checkLock);
    if (waiting())
    {
        DEBUG("[SCH] Finish wait status");
        m_condition.reset();
    	if (m_timer) m_timer->stop();
        SPELLexecutor::instance().getCIF().resetVerbosity();
        if (setStatus) resetControllerStatus( keepLock );
        // Releasing language lock shall be the last thing done
        if (!keepLock) releaseLanguageLock();
    }
}

//=============================================================================
// METHOD    : SPELLscheduler::finishPrompt
//=============================================================================
void SPELLscheduler::finishPrompt()
{
	SPELLmonitor m(m_checkLock);
	DEBUG("[SCH] Finish prompt wait status");
	m_condition.reset();
	// Abort the timer that is checking the prompt timeouts
    m_abortTimer = true;
	SPELLexecutor::instance().getCIF().resetVerbosity();
	resetControllerStatus( false );
	// Releasing language lock shall be the last thing done
	releaseLanguageLock();
}

//=============================================================================
// METHOD    : SPELLscheduler::cancelPrompt
//=============================================================================
void SPELLscheduler::cancelPrompt()
{
	SPELLmonitor m(m_checkLock);
	DEBUG("[SCH] Cancel prompt wait status");
	m_condition.reset();
	// Abort the timer that is checking the prompt timeouts
    m_abortTimer = true;
	SPELLexecutor::instance().abort("Prompt cancelled by user", true);
	// Releasing language lock shall be the last thing done
	releaseLanguageLock();
}

//=============================================================================
// METHOD    : SPELLscheduler::wait
//=============================================================================
void SPELLscheduler::wait()
{
	// This may block the execution for a long time, so we need to allow other Python threads
	// to kick in
	SPELLsafeThreadOperations ops("SPELLscheduler::wait()");
	m_waitingEvent.wait();
};

//=============================================================================
// METHOD    : SPELLscheduler::waiting
//=============================================================================
bool SPELLscheduler::waiting()
{
    SPELLmonitor m(m_lock);
    return (m_condition.type != SPELLscheduleCondition::SCH_PROMPT) &&
    	   (m_condition.type != SPELLscheduleCondition::SCH_NONE);
}

//=============================================================================
// METHOD    : SPELLscheduler::inPrompt
//=============================================================================
bool SPELLscheduler::inPrompt()
{
    SPELLmonitor m(m_lock);
    return (m_condition.type == SPELLscheduleCondition::SCH_PROMPT);
}

//=============================================================================
// METHOD    : SPELLscheduler::setLanguageLock
//=============================================================================
void SPELLscheduler::setLanguageLock( const SPELLexecutorStatus& st )
{
    SPELLmonitor m(m_lock);
    DEBUG("[SCH] Setting language lock with status " + SPELLexecutorUtils::statusToString(st) );
    SPELLexecutor::instance().getController().setStatus(st);
    m_waitingEvent.clear();
}


//=============================================================================
// METHOD    : SPELLscheduler::releaseLanguageLock
//=============================================================================
void SPELLscheduler::releaseLanguageLock()
{
    SPELLmonitor m(m_lock);
    DEBUG("[SCH] Releasing language lock");
    m_waitingEvent.set();
}

//=============================================================================
// METHOD    : SPELLscheduler::resetControllerStatus
//=============================================================================
void SPELLscheduler::resetControllerStatus( bool keepLock )
{
    SPELLmonitor m(m_lock);

    // If status is final, dont reset it
    SPELLexecutorStatus st = SPELLexecutor::instance().getController().getStatus();
    if (st == STATUS_FINISHED || st == STATUS_ERROR || st == STATUS_ABORTED )
    {
        DEBUG("[SCH] Shall not reset final status");
    	return;
    }

    DEBUG("[SCH] Resetting controller status");
    SPELLexecutionMode cmode = SPELLexecutor::instance().getController().getMode();
    if (cmode == MODE_STEP)
    {
        DEBUG("[SCH] Set controller status to paused");
		SPELLexecutor::instance().getController().setStatus(STATUS_PAUSED);
    }
    else
    {
        DEBUG("[SCH] Set controller status to " + ( keepLock ? STR("paused") : STR("running")));
		SPELLexecutor::instance().getController().setStatus( keepLock ? STATUS_PAUSED : STATUS_RUNNING );
    }
}

//=============================================================================
// METHOD    : SPELLscheduler::waitCondition
//=============================================================================
bool SPELLscheduler::waitCondition( std::string condition )
{
    LOG_INFO("[SCH] Evaluating procedure launch condition: " + condition);
    bool success = false;
    m_silentCheck = true;
	SPELLpyHandle pyVerify = STRPY("verify");
	SPELLpyHandle pyUntil  = SSTRPY(Until);
	SPELLpyHandle pyDelay  = SSTRPY(Delay);

    try
    {
    	// New reference is returned
        SPELLpyHandle pycond = SPELLpythonHelper::instance().eval(condition,false);
        if (pycond.get() == NULL)
        {
            SPELLexecutor::instance().getCIF().error("Unable to process launch condition: '" + condition + "'", LanguageConstants::SCOPE_SYS );
            LOG_ERROR("[SCH] Unable to process launch condition: '" + condition + "'");
        }
        else
        {
            if (!PyDict_Check(pycond.get()))
            {
                SPELLexecutor::instance().getCIF().error("Unable to process launch condition, not a dictionary: '" + condition + "'", LanguageConstants::SCOPE_SYS );
                LOG_ERROR("[SCH] Unable to process launch condition, not a dictionary: '" + condition + "'");
            }
            else
            {
                if (PyDict_Contains( pycond.get(), pyVerify.get() ))
                {
                    PyObject* verify = PyDict_GetItemString( pycond.get(), "verify" );

                    if (PyList_Check(verify))
                    {
                        SPELLexecutor::instance().getCIF().write("Execution scheduled using telemetry condition: " + PYREPR(verify), LanguageConstants::SCOPE_SYS );

                        SPELLscheduleCondition condition;
                        condition.type = SPELLscheduleCondition::SCH_VERIFICATION;
                        // This will make a copy
                        condition.setVerification(verify);
                        // This will make a copy
                        condition.setConfig(pycond.get());

                        startWait( condition );
                        wait();
                        success = true;
                    }
                    else
                    {
                        SPELLexecutor::instance().getCIF().error("Invalid verification: " + PYREPR(verify), LanguageConstants::SCOPE_SYS );
                    }

                }
                else if (PyDict_Contains( pycond.get(), pyUntil.get() ))
                {
                    std::string until_time = PYREPR(PyDict_GetItemString( pycond.get(), Until.c_str()));
                    until_time = "TIME(" + until_time + ").abs()";

                    // New reference
                    SPELLpyHandle theTime = SPELLpythonHelper::instance().eval(until_time,false);

                    if (theTime.get() != NULL)
                    {
                        SPELLexecutor::instance().getCIF().write("Execution scheduled until time: " + PYREPR(theTime.get()), LanguageConstants::SCOPE_SYS );

                        SPELLscheduleCondition condition;
                        condition.type = SPELLscheduleCondition::SCH_TIME;
                        condition.targetTime = SPELLtime( PyLong_AsLong(theTime.get()),0,false );
                        condition.period.set(10,0);

                        startWait( condition );
                        wait();
                        success = true;
                    }
                    else
                    {
                        SPELLexecutor::instance().getCIF().error("Invalid time value: " + until_time, LanguageConstants::SCOPE_SYS );
                    }
                }
                else if (PyDict_Contains( pycond.get(), pyDelay.get() ))
                {
                    std::string delay_time = PYREPR(PyDict_GetItemString( pycond.get(), Delay.c_str()));
                    delay_time = "TIME(" + delay_time + ").rel()";
                    std::string abs_time = std::string("NOW + ") + "TIME(" + delay_time + ")";

                    SPELLpyHandle theTime = SPELLpythonHelper::instance().eval(delay_time,false);
                    SPELLpyHandle theAbsTime = SPELLpythonHelper::instance().eval(abs_time,false);

                    if (theTime.get() != NULL)
                    {
                        SPELLexecutor::instance().getCIF().write("Execution scheduled until time: " + PYREPR(theAbsTime.get()), LanguageConstants::SCOPE_SYS );

                        SPELLscheduleCondition condition;
                        condition.type = SPELLscheduleCondition::SCH_TIME;
                        condition.targetTime = SPELLtime( PyLong_AsLong(theTime.get()),0,true );
                        condition.period.set(10,0);

                        startWait( condition );
                        wait();
                        success = true;
                    }
                    else
                    {
                        SPELLexecutor::instance().getCIF().error("Invalid time value: " + delay_time, LanguageConstants::SCOPE_SYS );
                    }
                }
                else
                {
                    SPELLexecutor::instance().getCIF().error("Unable to process launch condition, not a dictionary: '" + condition + "'", LanguageConstants::SCOPE_SYS );
                    LOG_ERROR("[SCH] Unable to process launch condition, not a dictionary: '" + condition + "'");
                }
            }
        }
    }
    catch(SPELLcoreException& ex)
    {
        SPELLexecutor::instance().getCIF().error("Unable to process launch condition (ex): '" + condition + "'", LanguageConstants::SCOPE_SYS );
        LOG_ERROR("[SCH] Unable to process launch condition (ex): '" + condition + "'");
    }
    m_silentCheck = false;
    return success;
}

//=============================================================================
// METHOD    : SPELLscheduler::notify()
//=============================================================================
void SPELLscheduler::notify( const ItemNotification& item )
{
    if (m_silentCheck) return;
    if (m_lastNotifiedItem.name  == item.name &&
    	m_lastNotifiedItem.value == item.value &&
    	m_lastNotifiedItem.status == item.status &&
    	m_lastNotifiedItem.comment == item.comment) return;
    m_lastNotifiedItem = item;
    SPELLexecutor::instance().getCIF().notify(item);
}

//=============================================================================
// METHOD    : SPELLscheduler::message()
//=============================================================================
void SPELLscheduler::message( const std::string& message )
{
    if (m_silentCheck) return;
    SPELLexecutor::instance().getCIF().write(message, LanguageConstants::SCOPE_SYS );
}

//=============================================================================
// METHOD    : SPELLscheduler::setPromptWarningDelay()
//=============================================================================
void SPELLscheduler::setPromptWarningDelay( unsigned int secs )
{
	LOG_INFO("[SCH] Update prompt warning default delay to " + ISTR(secs) + " seconds");
	m_defaultPromptWarningDelay = secs;
}
