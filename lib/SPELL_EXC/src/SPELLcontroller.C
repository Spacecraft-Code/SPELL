// ################################################################################
// FILE       : SPELLcontroller.C
// DATE       : Mar 17, 2011
// PROJECT    : SPELL
// DESCRIPTION: Implementation of the executor controller
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
// Project includes --------------------------------------------------------
#include "SPELL_UTIL/SPELLlog.H"
#include "SPELL_UTIL/SPELLerror.H"
#include "SPELL_UTIL/SPELLpythonHelper.H"
#include "SPELL_UTIL/SPELLpythonMonitors.H"
#include "SPELL_WRP/SPELLconstants.H"
#include "SPELL_WRP/SPELLregistry.H"
#include "SPELL_WRP/SPELLdriverManager.H"
// Local includes ----------------------------------------------------------
#include "SPELL_EXC/SPELLexecutor.H"
#include "SPELL_EXC/SPELLcontroller.H"
#include "SPELL_EXC/SPELLexecutorUtils.H"
#include "SPELL_EXC/SPELLcommand.H"



//=============================================================================
// CONSTRUCTOR    : SPELLcontroller::SPELLcontroller
//=============================================================================
SPELLcontroller::SPELLcontroller( const std::string& procId )
    : SPELLthread("controller"),
      SPELLcontrollerIF()
{
    DEBUG("[C] Controller created");
    m_procId = procId;
    m_mode = MODE_STEP;
    m_condition = "";
    reset();
    m_shallRepeat = false;
}

//=============================================================================
// DESTRUCTOR    : SPELLcontroller::~SPELLcontroller
//=============================================================================
SPELLcontroller::~SPELLcontroller()
{
    DEBUG("[C] Controller destroyed");
}

//=============================================================================
// METHOD    : SPELLcontroller::reset
//=============================================================================
void SPELLcontroller::reset()
{
    m_status = STATUS_UNINIT;
    m_mainProc = "";
    m_currentProc = "";
    m_skipping = false;

    m_abort = false;
    m_error = false;
    m_finished = false;

    m_recover = false;
    m_reload = false;
    m_wantPause = false;
    m_wantInterrupt = false;

    m_recoverEvent.set();
    m_controllerLock.set();
    m_execLock.set();

    // No commands pending to be processed
    m_commandPending.set();

    m_mailbox.reset();

    DEBUG("[C] SPELLcontroller reset");
}

//=============================================================================
// METHOD    : SPELLcontroller::command
//=============================================================================
void SPELLcontroller::command( const ExecutorCommand& cmd, const bool queueIt, const bool high_priority )
{
	if (queueIt)
	{
		DEBUG("[C] Pushing command " + cmd.id);
		m_mailbox.push(cmd, high_priority);
	}
	else
	{
		// Direct execution of commands, dont go thru queue
		executeCommand(cmd);
	}
}

//=============================================================================
// METHOD    : SPELLcontroller::setStatus
//=============================================================================
void SPELLcontroller::setStatus( const SPELLexecutorStatus& st )
{
	DEBUG("[C] Attempt to set status " + SPELLexecutorUtils::statusToString(st));
    bool newStatus = (st != m_status);

	if (newStatus) notifyBeforeStatusChange(m_status);

    m_status = st;
    // We don't want to notify status redundantly
    if (newStatus || st == STATUS_WAITING || st == STATUS_PROMPT)
    {
        LOG_INFO("Procedure status: " + SPELLexecutorUtils::statusToString(st));
        SPELLstatusInfo info(st);
        info.condition = getCondition();
        info.actionLabel = SPELLexecutor::instance().getUserAction().getLabel();
        info.actionEnabled = SPELLexecutor::instance().getUserAction().isEnabled();

		SPELLexecutor::instance().getCIF().notifyStatus( info );
    }

    if (newStatus) notifyAfterStatusChange(m_status);

    if (newStatus && st == STATUS_ABORTED)
    {
    	std::string event = "Procedure aborted: " + m_procId;
    	raiseEvent( event );
    }
}

//=============================================================================
// METHOD    : SPELLcontroller::getStatus
//=============================================================================
const SPELLexecutorStatus SPELLcontroller::getStatus() const
{
    return m_status;
}

//=============================================================================
// METHOD    : SPELLcontroller::setMode
//=============================================================================
void SPELLcontroller::setMode( const SPELLexecutionMode& mode )
{
    m_mode = mode;
}

//=============================================================================
// METHOD    : SPELLcontroller::getMode
//=============================================================================
const SPELLexecutionMode SPELLcontroller::getMode() const
{
    return m_mode;
}

//=============================================================================
// METHOD    : SPELLcontroller::setCondition
//=============================================================================
void SPELLcontroller::setCondition( const std::string& condition )
{
    m_condition = condition;
}

//=============================================================================
// METHOD    : SPELLcontroller::getCondition
//=============================================================================
const std::string& SPELLcontroller::getCondition() const
{
    return m_condition;
}

//=============================================================================
// METHOD    : SPELLcontroller::hasCondition
//=============================================================================
const bool SPELLcontroller::hasCondition() const
{
    return (getCondition() != "");
}

//=============================================================================
// METHOD    : SPELLcontroller::setAutoRun
//=============================================================================
void SPELLcontroller::setAutoRun()
{
    DEBUG("[C] Set autorun");
    setMode( MODE_PLAY );
    setStatus(STATUS_RUNNING);
    doContinue();
}

//=============================================================================
// METHOD    : SPELLcontroller::enableRunInto
//=============================================================================
void SPELLcontroller::enableRunInto( const bool enable )
{
    DEBUG("[C] Run into enabled: " + (enable ? STR("yes") : STR("no")));
    // Change the step over mode in the call stack manager
    if(enable)
    {
        SPELLexecutor::instance().getCallstack().stepOver( SO_ALWAYS_INTO );
    }
    else
    {
        SPELLexecutor::instance().getCallstack().stepOver( SO_ALWAYS_OVER );
    }
}

//=============================================================================
// METHOD    : SPELLcontroller::checkAborted
//=============================================================================
const bool SPELLcontroller::checkAborted()
{
    bool executionOk = true;
    if (m_finished)
    {
        SPELLerror::instance().setExecutionTerminated();
        executionOk = false;
    }
    else if (m_abort)
    {
        SPELLerror::instance().setExecutionAborted();
        executionOk = false;
    }
    return executionOk;
}

//=============================================================================
// METHOD    : SPELLcontroller::shouldRecover()
//=============================================================================
const bool SPELLcontroller::shouldRecover()
{
    m_recoverEvent.wait();
    return m_recover;
}

//=============================================================================
// METHOD    : SPELLcontroller::run
//=============================================================================
void SPELLcontroller::run()
{
    while(1)
    {
        DEBUG("[C] Waiting for new commands");
        ExecutorCommand cmd = m_mailbox.pull();
        DEBUG("[C] Got command " + cmd.id);
        if (cmd.id == CMD_STOP)
        {
            m_reload = false;
            m_recoverEvent.set();
            m_commandPending.set();
            DEBUG("[C] Stop done");
            return;
        }
        executeCommand(cmd);
    }
}

//=============================================================================
// METHOD    : SPELLcontroller::notifyCommandToCore
//=============================================================================
void SPELLcontroller::notifyCommandToCore( const std::string& id )
{
	// Notify the adapter and the driver to give the chance
	// to abort ongoing operations if needed
	if (id == CMD_ABORT ||
		id == CMD_PAUSE ||
		id == CMD_INTERRUPT ||
		id == CMD_SKIP  ||
		id == CMD_RUN)
	{
		LOG_INFO("Notify command " + id + " to core");
		SPELLdriverManager::instance().onCommand(id);
		DEBUG("[C] Notify command to core OUT");
	}
}

//=============================================================================
// METHOD    : SPELLcontroller::executeCommand()
//=============================================================================
void SPELLcontroller::executeCommand( const ExecutorCommand& cmd )
{
	// If a (repeatable) command is being executed, discard this one
	if (isCommandPending() &&
	    (cmd.id != CMD_ABORT) &&
	    (cmd.id != CMD_FINISH) &&
	    (cmd.id != CMD_INTERRUPT) &&
	    (cmd.id != CMD_PAUSE) &&
	    (cmd.id != CMD_CLOSE))
	{
		LOG_WARN("Discarding command " + cmd.id);
		return;
	}

	LOG_INFO("Now executing command " + cmd.id);

    startCommandProcessing();

	notifyCommandToCore( cmd.id );

    if (cmd.id == CMD_ABORT)
    {
        doAbort();
    }
    else if (cmd.id == CMD_FINISH)
    {
        doFinish();
    }
    else if (cmd.id == CMD_ACTION)
    {
        doUserAction();
    }
    else if (cmd.id == CMD_STEP)
    {
        doStep( false );
    }
    else if (cmd.id == CMD_STEP_OVER)
    {
        doStep( true );
    }
    else if (cmd.id == CMD_RUN)
    {
        doPlay();
    }
    else if (cmd.id == CMD_SKIP)
    {
        doSkip();
    }
    else if (cmd.id == CMD_GOTO)
    {
        if (cmd.earg == "line")
        {
            DEBUG("[C] Processing go-to-line " + cmd.arg);
            try
            {
                int line = STRI(cmd.arg);
                doGoto( line );
            }
            catch(...) {};
        }
        else if (cmd.earg == "label")
        {
            DEBUG("[C] Processing go-to-label " + cmd.arg);
            doGoto( cmd.arg );
        }
        else
        {
        	SPELLexecutor::instance().getCIF().error("Unable to process Go-To command, no target information", LanguageConstants::SCOPE_SYS );
        }
    }
    else if (cmd.id == CMD_PAUSE)
    {
        doPause();
    }
    else if (cmd.id == CMD_INTERRUPT)
    {
        doInterrupt();
    }
    else if (cmd.id == CMD_SCRIPT)
    {
    	/** \todo determine when to override */
        doScript(cmd.arg,false);
    }
    else if (cmd.id == CMD_CLOSE)
    {
        m_recover = false;
        m_reload = false;
        doClose();
    }
    else if (cmd.id == CMD_RELOAD)
    {
        doReload();
    }
    else if (cmd.id == CMD_RECOVER)
    {
        doRecover();
    }
    else
    {
        LOG_ERROR("[C] UNRECOGNISED COMMAND: " + cmd.id);
    }
	m_mailbox.commandProcessed();

	// The command has finished, release the dispatcher
	setCommandFinished();
	DEBUG("[C] Command execution finished " + cmd.id);
}

//=============================================================================
// METHOD    : SPELLcontroller::run
//=============================================================================
void SPELLcontroller::waitCommand()
{
	// Block the dispatcher is there is a command waiting
	if (isCommandPending())
	{
		// Wait for the command to finish
		DEBUG("[C] Dispatching lets the command execute");
		waitCommandFinished();
		DEBUG("[C] Dispatching proceeds, command finished");
	};
}

//=============================================================================
// METHOD    : SPELLcontroller::run
//=============================================================================
void SPELLcontroller::stop()
{
    ExecutorCommand stop;
    stop.id = CMD_STOP;
    m_mailbox.push( stop, true );
}

//=============================================================================
// METHOD    : SPELLcontroller::executionLock
//=============================================================================
void SPELLcontroller::executionLock()
{
    if (m_abort) doWait();
    DEBUG("[C] Controller process lock");
    notifyBeforeDriverOperation();
    m_controllerLock.clear();
}

//=============================================================================
// METHOD    : SPELLcontroller::executionUnlock
//=============================================================================
void SPELLcontroller::executionUnlock()
{
    DEBUG("[C] Controller process unlock");
    notifyAfterDriverOperation();
    m_controllerLock.set();
}

//=============================================================================
// METHOD    : SPELLcontroller::doStep
//=============================================================================
void SPELLcontroller::doStep( bool stepOver )
{
	switch(getStatus())
	{
	case STATUS_PAUSED: // Allowed status
	case STATUS_INTERRUPTED: // Allowed status
		break;
	default:
		return;
	}
    if (m_error) return; // Do not continue on error

    DEBUG("[C] Do step ( stepping over: " + BSTR(stepOver) + ")" );

    SPELLexecutor::instance().getScheduler().restartWait();

    // A step will disable the step over
    if (stepOver)
    {
        SPELLexecutor::instance().getCallstack().stepOver( SO_ONCE_OVER );
    }
    else
    {
        SPELLexecutor::instance().getCallstack().stepOver( SO_ONCE_INTO );
    }

    setMode( MODE_STEP );

    doContinue();
}

//=============================================================================
// METHOD    : SPELLcontroller::doPlay
//=============================================================================
void SPELLcontroller::doPlay()
{
    DEBUG("[C] Do play (current status " + SPELLexecutorUtils::statusToString(getStatus()) + ")");
	switch(getStatus())
	{
	case STATUS_PAUSED: // Allowed status
	case STATUS_INTERRUPTED: // Allowed status
		break;
	default:
		return;
	}
    if (m_error) return; // Do not continue on error

    DEBUG("[C] Restart wait in scheduler" );
    SPELLexecutor::instance().getScheduler().restartWait();
    DEBUG("[C] Set status running and mode play" );
    setStatus(STATUS_RUNNING);
    setMode(MODE_PLAY);
    DEBUG("[C] Continue" );
    doContinue();
}

//=============================================================================
// METHOD    : SPELLcontroller::doPause
//=============================================================================
void SPELLcontroller::doPause()
{
	SPELLexecutorStatus currentStatus = getStatus();

	switch(currentStatus)
    {
    case STATUS_RUNNING: // Allowed status
    case STATUS_PROMPT: // Allowed status
    case STATUS_WAITING: // Allowed status
        break;
    case STATUS_PAUSED:
    	return;
    default:
        LOG_WARN("[C] Cannot pause in status " + SPELLexecutorUtils::statusToString(getStatus()));
        return;
    }
    if (m_error) return; // Do not continue on error

    DEBUG("[C] Do pause");

    // If there is something ongoing, warn the user
    if (m_controllerLock.isClear())
    {
    	SPELLexecutor::instance().getCIF().warning("Procedure will pause", LanguageConstants::SCOPE_SYS );
    }

    DEBUG("[C] Set step mode");
    setMode( MODE_STEP );
    // Enable the pause flag for the line event to hold the execution
    m_wantPause = true;
	// Enable the stepping mechanism
	m_execLock.clear();

    DEBUG("[C] Do pause done");
}

//=============================================================================
// METHOD    : SPELLcontroller::doInterrupt
//=============================================================================
void SPELLcontroller::doInterrupt()
{
	SPELLexecutorStatus currentStatus = getStatus();

	switch(currentStatus)
    {
    case STATUS_RUNNING: // Allowed status
    case STATUS_PAUSED: // Allowed status
    case STATUS_WAITING: // Allowed status
        break;
    default:
        DEBUG("[C] Discard interrupt command in current status " + SPELLexecutorUtils::statusToString(currentStatus));
        return;
    }
    if (m_error) return; // Do not continue on error

    // Do not process interrupt if there is nothing ongoing
    if (!m_controllerLock.isClear() && !SPELLexecutor::instance().getScheduler().waiting())
    {
        DEBUG("[C] Discard interrupt command as no driver operation is ongoing");
    	SPELLexecutor::instance().getCIF().write("No ongoing operations to be interrupted", LanguageConstants::SCOPE_SYS );
    	return;
    }
    else
    {
    	SPELLexecutor::instance().getCIF().warning("Interrupt current operation", LanguageConstants::SCOPE_SYS );
    }

    DEBUG("[C] Do interrupt");

    DEBUG("[C] Set step mode");
    setMode( MODE_STEP );
    setStatus( STATUS_INTERRUPTED );
    // Enable the pause flag for the line event to hold the execution
    m_wantPause = true;
    // Enable the interrupt flag to report the correct status
    m_wantInterrupt = true;

	// Enable the stepping mechanism
	m_execLock.clear();

    if (hasCondition())
    {
    	SPELLexecutor::instance().getCIF().warning("Execution schedule condition has been cancelled", LanguageConstants::SCOPE_SYS );
    	SPELLexecutor::instance().getScheduler().abortWait(false);
    }
    else
    {
    	SPELLexecutor::instance().getScheduler().interruptWait();
    }

    DEBUG("[C] Do interrupt done");
}

//=============================================================================
// METHOD    : SPELLcontroller::doAbort
//=============================================================================
void SPELLcontroller::doAbort()
{
    switch(getStatus())
    {
    case STATUS_ERROR: // Disallowed status
    case STATUS_ABORTED: // Disallowed status
    case STATUS_FINISHED: // Disallowed status
    case STATUS_RELOADING: // Disallowed status
        return;
    default:
        break;
    }
    if (m_error) return; // Do not continue on error

    DEBUG("[C] Do abort");

    // Setting this flags like this will provoke the execution abort thanks to the
    // checkAborted() call in the dispatching mechanism. We shall not do more
    // here (like setting the status) since the status ABORTED shall be set AFTER
    // doing some extra operations in the executor, like unloading the SPELL driver.
    // See SPELLexecutorImpl::executionAborted() method.
    m_abort = true;
    m_recover = false;
    m_reload = false;
    m_recoverEvent.set();

    SPELLexecutor::instance().getScheduler().abortWait( false );
    doContinue();
}

//=============================================================================
// METHOD    : SPELLcontroller::doFinish
//=============================================================================
void SPELLcontroller::doFinish()
{
    switch(getStatus())
    {
    case STATUS_RUNNING: // Allowed status
    case STATUS_PAUSED: // Allowed status
    case STATUS_WAITING: // Allowed status
    case STATUS_PROMPT: // Allowed status
        break;
    default:
        return;
    }
    if (m_error) return; // Do not continue on error

    DEBUG("[C] Do finish");

    m_finished = true;
    m_recover = false;
    m_reload = false;
    m_recoverEvent.set();

    SPELLexecutor::instance().getScheduler().abortWait( false );
    setStatus(STATUS_FINISHED);
    doContinue();
}

//=============================================================================
// METHOD    : SPELLcontroller::doClose
//=============================================================================
void SPELLcontroller::doClose()
{
    DEBUG("[C] Do close");
    m_finished = true;
    SPELLexecutorStatus st = getStatus();
    if ( st != STATUS_FINISHED && st != STATUS_ABORTED && st != STATUS_ERROR )
    {
        doContinue();
    }
    m_recoverEvent.set();
    DEBUG("[C] Finalize executor");
    SPELLexecutor::instance().finalize();
}

//=============================================================================
// METHOD    : SPELLcontroller::doReload
//=============================================================================
void SPELLcontroller::doReload()
{
    switch(getStatus())
    {
    case STATUS_ABORTED: // Allowed status
    case STATUS_FINISHED: // Allowed status
    case STATUS_ERROR: // Allowed status
        break;
    default:
        return;
    }

    DEBUG("[C] Begin reloading");
    setStatus( STATUS_RELOADING );
    setMode( MODE_STEP );
    m_recover = false;
    m_reload = true;
    doClose();
}

//=============================================================================
// METHOD    : SPELLcontroller::doRecover
//=============================================================================
void SPELLcontroller::doRecover()
{
	std::cerr << "############ TRY TO RECOVER ###############" << std::endl;
    if ((getStatus() != STATUS_ERROR) && !m_error) return;

    setMode( MODE_STEP );
    m_recover = true;
    m_reload = true;
    doClose();
}

//=============================================================================
// METHOD    : SPELLcontroller::doSkip
//=============================================================================
void SPELLcontroller::doSkip()
{
    switch(getStatus())
    {
    case STATUS_PAUSED: // Allowed status
    case STATUS_INTERRUPTED: // Allowed status
        break;
    default:
        return;
    }
    if (m_error) return; // Do not continue on error

    if (!SPELLexecutor::instance().canSkip())
    {
        LOG_WARN("[C] Cannot skip current line");
        return;
    }

    DEBUG("[C] Do skip");

    bool waitAborted = SPELLexecutor::instance().getScheduler().abortWait( false );

    // Either we skip a proc line, or we abort a wait condition (then we dont want to actually skip a line)
    if (waitAborted)
    {
    	SPELLexecutor::instance().getCIF().warning("Wait condition aborted", LanguageConstants::SCOPE_SYS );
    	setStatus(STATUS_PAUSED);
    }
    else
    {
        if (SPELLexecutor::instance().goNextLine())
        {
            m_skipping = not waitAborted;
            doContinue();
        }
        else
        {
        	/** \todo Should try to go to upper frame instead of stepping.
            // This is the issue that obligues us to put a return statement
            // at the end of each function. */
            doStep(false);
        }
    }
}

//=============================================================================
// METHOD    : SPELLcontroller::doGoto
//=============================================================================
void SPELLcontroller::doGoto( const std::string& label )
{
    if (getStatus() != STATUS_PAUSED || m_error ) return;

    DEBUG("[C] Do goto label " + label);

    if (SPELLexecutor::instance().goLabel(label,false))
    {
        m_skipping = true;
        doContinue();
    }
}

//=============================================================================
// METHOD    : SPELLcontroller::doGoto
//=============================================================================
void SPELLcontroller::doGoto( const int line )
{
    if (getStatus() != STATUS_PAUSED || m_error ) return;

    DEBUG("[C] Do goto line " + ISTR(line));

    if (SPELLexecutor::instance().goLine(line))
    {
        m_skipping = true;
        doContinue();
    }
}

//=============================================================================
// METHOD    : SPELLcontroller::doScript
//=============================================================================
void SPELLcontroller::doScript( const std::string& script, const bool override )
{
    switch(getStatus())
    {
    case STATUS_PAUSED: // Allowed status
    case STATUS_WAITING: // Allowed status
    case STATUS_PROMPT: // Allowed status
    case STATUS_INTERRUPTED: // Allowed status
        break;
    default:
        return;
    }
    if (m_error) return; // Do not continue on error

    DEBUG("[C] Do script: " + script);
    SPELLexecutor::instance().runScript( script );
}

//=============================================================================
// METHOD    : SPELLcontroller::doUserAction
//=============================================================================
void SPELLcontroller::doUserAction()
{
    switch(getStatus())
    {
    case STATUS_ABORTED: // Disallowed status
    case STATUS_ERROR: // Disallowed status
    case STATUS_FINISHED: // Disallowed status
    case STATUS_RELOADING: // Dissallowed status
    case STATUS_INTERRUPTED: // Dissallowed status
        return;
    default:
        break;
    }
    if (m_error) return; // Do not continue on error

    DEBUG("[C] Do user action");
	SPELLexecutor::instance().executeUserAction();
}

//=============================================================================
// METHOD    : SPELLcontroller::doWait
//=============================================================================
void SPELLcontroller::doWait()
{
    if (m_abort || m_error) return;
    DEBUG("[C] Wait in");
	m_wantPause = false;

	// Set PAUSED unless entering INTERRUPTED status
	if (!m_wantInterrupt)
	{
		setStatus( STATUS_PAUSED );
	}
	m_wantInterrupt = false;

	// Execution lock
    {
    	notifyBeforeWait();
    	SPELLsafeThreadOperations ops("SPELLcontroller::doWait");
    	m_execLock.clear();
    	m_execLock.wait();
    	notifyAfterWait();
    }
    DEBUG("[C] Wait out");
}


//=============================================================================
// METHOD    : SPELLcontroller::doContinue
//=============================================================================
void SPELLcontroller::doContinue()
{
    DEBUG("[C] Continue");
    m_execLock.set();
}

//=============================================================================
// METHOD    : SPELLcontroller::callbackEventLine
//=============================================================================
void SPELLcontroller::callbackEventLine( PyFrameObject* frame, const std::string& file, const int line, const std::string& name )
{
    DEBUG("[C] Line event: " + file + ":" + ISTR(line) );

    if (hasCondition())
    {
        DEBUG("[E] Waiting for start condition");
        if (!SPELLexecutor::instance().getScheduler().waitCondition( getCondition() ))
        {
            DEBUG("[C] Condition failed, pausing");
            setMode(MODE_STEP);
        }
        else // Scheduling success
        {
            DEBUG("[C] Condition success, go play");
            setMode(MODE_PLAY);
        }
        m_condition = "";
    }

    // Stepping (execution hold) mechanism
    if (m_wantPause || !SPELLexecutor::instance().getCallstack().isSteppingOver())
    {
		if (getMode() == MODE_STEP)
		{
	        DEBUG("[C] Entering stepping");
	    	doWait();
		}
		else
		{
			usleep( SPELLexecutor::instance().getConfiguration().getExecDelay() * 1000 );
		}
    }
    else
    {
        setStatus(STATUS_RUNNING);
    }

	notifyOnLineStep();

    m_shallRepeat = false;

    // Skip-out-function flag
    if (m_skipping)
    {
        m_skipping = false;
        m_shallRepeat = true;
    }
    // Update the current procedure
    m_currentProc = file;
}

//=============================================================================
// METHOD    : SPELLcontroller::callbackEventCall
//=============================================================================
void SPELLcontroller::callbackEventCall( PyFrameObject* frame, const std::string& file, const int line, const std::string& name)
{
    DEBUG("[C] Call event");

    // Detect loaded status
    if (m_mainProc == "")
    {
        DEBUG("[C] Set main procedure");
        m_mainProc = file;
    }

    // Detect the initial event for the procedure load. If autorun is not set
    // and there is no scheduling condition, pause the procedure.
    if ((name == "<module>") &&
            (not hasCondition()) &&
            (getStatus() == STATUS_LOADED))
    {
        DEBUG("[C] Pausing procedure at start");
        // Enable the pause flag for the line event to hold the execution
        m_wantPause = true;
    	// Enable the stepping mechanism
        setMode( MODE_STEP );
    	m_execLock.clear();
        setStartTime();
    }
    /** \todo review: Nominal case when calling functions, importing status?? */
    else if ( name == "<module>" && file != m_currentProc )
    {
        //Nothing to do
    }
}

//=============================================================================
// METHOD    : SPELLcontroller::callbackEventReturn
//=============================================================================
void SPELLcontroller::callbackEventReturn( PyFrameObject* frame, const std::string& file, const int line, const std::string& name)
{
    DEBUG("[C] Return event");
}

//=============================================================================
// METHOD    : SPELLcontroller::setStartTime
//=============================================================================
void SPELLcontroller::setStartTime()
{
	std::string msg = "Procedure started: " + m_procId + " at " + SPELLutils::timestamp();
    LOG_INFO("[C] " + msg);
	notifyOnStart();
	// Raise event
	raiseEvent(msg);
}

//=============================================================================
// METHOD    : SPELLcontroller::setEndTime
//=============================================================================
void SPELLcontroller::setEndTime()
{
	std::string msg = "Procedure finished: " + m_procId + " at " + SPELLutils::timestamp() + " (status " + SPELLexecutorUtils::statusToString(m_status) + ")";
    LOG_INFO("[C] " + msg);
	notifyOnFinish();
	// Raise event
	raiseEvent(msg);
}

//=============================================================================
// METHOD    : SPELLcontroller::raiseEvent
//=============================================================================
void SPELLcontroller::raiseEvent( const std::string& message )
{
	// Raise event
	try
	{
		PyObject* evIfc = SPELLregistry::instance().get("EV");
		if (evIfc != NULL)
		{
			std::string copy = message;
			PyObject* evMsg = SSTRPY(copy);
			if (evMsg==NULL)
			{
				LOG_ERROR("Unable to inject event, cannot generate string");
				return;
			}
			Py_XINCREF(evMsg);
			SPELLpythonHelper::instance().callMethod(evIfc,"raiseEvent",evMsg,NULL);
			SPELLpythonHelper::instance().checkError();
		}
	}
	catch(SPELLcoreException& ex)
	{
		LOG_ERROR("Unable to inject event: " + ex.what());
	}
}

//=============================================================================
// METHOD    : SPELLcontroller::setFinished
//=============================================================================
void SPELLcontroller::setFinished()
{
    if (!m_error && !m_finished)
    {
        SPELLexecutor::instance().getScheduler().abortWait( false );
        setStatus(STATUS_FINISHED);
        // Mark the procedure end time
        setEndTime();
    }
}

//=============================================================================
// METHOD    : SPELLcontroller::setError
//=============================================================================
void SPELLcontroller::setError( const std::string& error, const std::string& reason, const bool fatal )
{
    DEBUG("[C] Notified error");
    // Ensure the procedure will remain paused
    setMode( MODE_STEP );

    // Store status
    m_error = true;
    m_status = STATUS_ERROR;

    // Cannot reload/recover if the error is fatal
    m_recoverEvent.clear();
    if (fatal)
    {
        m_reload = false;
        m_recover = false;
    }

    DEBUG("[C] Aborting wait state");

    // Abort any possible wait condition ongoing
    SPELLexecutor::instance().getScheduler().abortWait( false );

    // Report the error
    std::string event = "Procedure error: " + error + ", " + reason + " ( fatal: " + BSTR(fatal) + ")";
    LOG_ERROR(event);

	// Raise event
    raiseEvent(event);

    DEBUG("[C] Sending error to GUI");
    SPELLexecutor::instance().getCIF().notifyError( error, reason, fatal );

    // Allow execution to continue
    m_execLock.clear();
}

//=============================================================================
// METHOD    : SPELLcontroller::
//=============================================================================
void SPELLcontroller::addControllerListener( SPELLcontrollerListener* listener )
{
	if (std::find(m_listeners.begin(), m_listeners.end(), listener) == m_listeners.end())
	{
		DEBUG( "Added listener " + listener->getId() );
		m_listeners.push_back(listener);
	}
}

//=============================================================================
// METHOD    : SPELLcontroller::
//=============================================================================
void SPELLcontroller::removeControllerListener( SPELLcontrollerListener* listener )
{
	SPELLcontrollerListeners::iterator it = std::find(m_listeners.begin(), m_listeners.end(), listener);
	if (it != m_listeners.end())
	{
		DEBUG( "Removed listener " + listener->getId() );
		m_listeners.erase(it);
	}
}

//=============================================================================
// METHOD    : SPELLcontroller::
//=============================================================================
void SPELLcontroller::notifyBeforeWait()
{
	DEBUG( "Notify listeners: before wait");
	SPELLcontrollerListeners::iterator it;
	for( it = m_listeners.begin(); it != m_listeners.end(); it++)
	{
		DEBUG( "   - notify " + (*it)->getId() );
		(*it)->callbackBeforeWait();
	}
}

//=============================================================================
// METHOD    : SPELLcontroller::
//=============================================================================
void SPELLcontroller::notifyAfterWait()
{
	DEBUG( "Notify listeners: after wait");
	SPELLcontrollerListeners::iterator it;
	for( it = m_listeners.begin(); it != m_listeners.end(); it++)
	{
		DEBUG( "   - notify " + (*it)->getId() );
		(*it)->callbackAfterWait();
	}
}

//=============================================================================
// METHOD    : SPELLcontroller::
//=============================================================================
void SPELLcontroller::notifyBeforeStatusChange( const SPELLexecutorStatus& status )
{
	DEBUG( "Notify listeners: before status change");
	SPELLcontrollerListeners::iterator it;
	for( it = m_listeners.begin(); it != m_listeners.end(); it++)
	{
		DEBUG( "   - notify " + (*it)->getId() );
		(*it)->callbackBeforeStatusChange( status );
	}
}

//=============================================================================
// METHOD    : SPELLcontroller::
//=============================================================================
void SPELLcontroller::notifyAfterStatusChange( const SPELLexecutorStatus& status )
{
	DEBUG( "Notify listeners: after status change");
	SPELLcontrollerListeners::iterator it;
	for( it = m_listeners.begin(); it != m_listeners.end(); it++)
	{
		DEBUG( "   - notify " + (*it)->getId() );
		(*it)->callbackAfterStatusChange( status );
	}
}

//=============================================================================
// METHOD    : SPELLcontroller::
//=============================================================================
void SPELLcontroller::notifyOnStart()
{
	DEBUG( "Notify listeners: on start");
	SPELLcontrollerListeners::iterator it;
	for( it = m_listeners.begin(); it != m_listeners.end(); it++)
	{
		DEBUG( "   - notify " + (*it)->getId() );
		(*it)->callbackOnStart();
	}
}

//=============================================================================
// METHOD    : SPELLcontroller::
//=============================================================================
void SPELLcontroller::notifyOnFinish()
{
	DEBUG( "Notify listeners: on finish");
	SPELLcontrollerListeners::iterator it;
	for( it = m_listeners.begin(); it != m_listeners.end(); it++)
	{
		DEBUG( "   - notify " + (*it)->getId() );
		(*it)->callbackOnFinish();
	}
}

//=============================================================================
// METHOD    : SPELLcontroller::
//=============================================================================
void SPELLcontroller::notifyOnLineStep()
{
	DEBUG( "Notify listeners: on line step");
	SPELLcontrollerListeners::iterator it;
	for( it = m_listeners.begin(); it != m_listeners.end(); it++)
	{
		DEBUG( "   - notify " + (*it)->getId() );
		(*it)->callbackOnLineStep();
	}
}

//=============================================================================
// METHOD    : SPELLcontroller::notifyBeforeDriverOperation
//=============================================================================
void SPELLcontroller::notifyBeforeDriverOperation()
{
	DEBUG( "Notify listeners: before driver operation");
	SPELLcontrollerListeners::iterator it;
	for( it = m_listeners.begin(); it != m_listeners.end(); it++)
	{
		DEBUG( "   - notify " + (*it)->getId() );
		(*it)->callbackBeforeDriverOperation();
	}
	DEBUG( "Notify listeners: before driver operation: done");
}

//=============================================================================
// METHOD    : SPELLcontroller::notifyAfterDriverOperation
//=============================================================================
void SPELLcontroller::notifyAfterDriverOperation()
{
	DEBUG( "Notify listeners: after driver operation");
	SPELLcontrollerListeners::iterator it;
	for( it = m_listeners.begin(); it != m_listeners.end(); it++)
	{
		DEBUG( "   - notify " + (*it)->getId() );
		(*it)->callbackAfterDriverOperation();
	}
}
