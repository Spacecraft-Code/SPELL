// ################################################################################
// FILE       : SPELLexecutorUtils.C
// DATE       : Mar 17, 2011
// PROJECT    : SPELL
// DESCRIPTION: Implementation of the executor utilities
// --------------------------------------------------------------------------------
//
//  Copyright (C) 2008, 2018 SES ENGINEERING, Luxembourg S.A.R.L.
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
#include "SPELL_EXC/SPELLexecutorUtils.H"
// Project includes --------------------------------------------------------
#include "SPELL_UTIL/SPELLlog.H"
#include "SPELL_UTIL/SPELLpythonHelper.H"
#include "SPELL_IPC/SPELLipc_Executor.H"
#include "SPELL_UTIL/SPELLutils.H"
#include "SPELL_WRP/SPELLregistry.H"
#include "SPELL_WRP/SPELLconstants.H"
using namespace PythonConstants;
using namespace LanguageModifiers;


//============================================================================
// FUNCTION        : getIntervalData
//============================================================================
void SPELLexecutorUtils::getIntervalData( SPELLpyArgs& arguments, SPELLscheduleCondition& condition )
{
	PyObject* intervalObj = arguments[LanguageModifiers::Interval];
	if (intervalObj != NULL)
	{
		if (PyList_Check(intervalObj))
		{
			DEBUG("[EU] Processing interval list");
			int numInts = PyList_Size(intervalObj);
			for(int index=0; index<numInts; index++)
			{
				PyObject* intrv = PyList_GetItem( intervalObj, index );
				if (PyLong_Check(intrv))
				{
					condition.interval.push_back( SPELLtime( PyLong_AsLong(intrv), 0, true ));
				}
				else if (PyInt_Check(intrv))
				{
					condition.interval.push_back( SPELLtime( PyInt_AsLong(intrv), 0, true ));
				}
				else if (PyFloat_Check(intrv))
				{
					double secsd = PyFloat_AsDouble(intrv);
					unsigned long secs = (unsigned long) secsd;
					unsigned int msecs = (unsigned int) ((secsd-secs)*1000);
					condition.interval.push_back( SPELLtime( secs, msecs, true ));
				}
				else
				{
					SPELLtime itime = SPELLpythonHelper::instance().evalTime(intrv);
					condition.interval.push_back( SPELLtime(itime) );
				}
			}
		}
		else if (PyLong_Check(intervalObj))
		{
			DEBUG("[EU] Processing numeric interval");
			condition.interval.push_back( SPELLtime( PyLong_AsLong(intervalObj), 0, true ));
		}
		else if (PyInt_Check(intervalObj))
		{
			DEBUG("[EU] Processing numeric interval");
			condition.interval.push_back( SPELLtime( PyInt_AsLong(intervalObj), 0, true ));
		}
		else if (PyFloat_Check(intervalObj))
		{
			DEBUG("[EU] Processing numeric interval");
			double secsd = PyFloat_AsDouble(intervalObj);
			unsigned long secs = (unsigned long) secsd;
			unsigned int msecs = (unsigned int) ((secsd-secs)*1000);
			condition.interval.push_back( SPELLtime( secs, msecs, true ));
		}
		else
		{
			DEBUG("[EU] Processing TIME interval");
			SPELLtime itime = SPELLpythonHelper::instance().evalTime(intervalObj);
			condition.interval.push_back( SPELLtime(itime) );
		}
		condition.message = arguments.getModifier_Message();
		DEBUG("[EU] Interval message is '" + condition.message +"'");
		if (condition.message == "")
		{
			THROW_EXCEPTION("Cannot accept condition", "Interval message value not given", SPELL_PYERROR_SYNTAX );
		}
	}
}

//============================================================================
// FUNCTION        : configureConditionModifiers
//============================================================================
void SPELLexecutorUtils::configureConditionModifiers( SPELLpyArgs& arguments, SPELLscheduleCondition& condition )
{
	// It may be fixed or time condition. Check modifiers Until and Delay.
	if (arguments.hasModifier( LanguageModifiers::Delay ))
	{
		condition.targetTime = arguments.getModifier_Delay();
		DEBUG("[EU] Configuring delay: " + condition.targetTime.toString());
		condition.type = SPELLscheduleCondition::SCH_TIME;
	}
	else if (arguments.hasModifier( LanguageModifiers::Until ))
	{
		condition.targetTime = arguments.getModifier_Until();
		DEBUG("[EU] Configuring until: " + condition.targetTime.toString());
		condition.type = SPELLscheduleCondition::SCH_TIME;
	}
	else if (arguments.hasModifier( LanguageModifiers::Procedure ))
	{
		condition.type = SPELLscheduleCondition::SCH_CHILD;
		condition.period.set(0,200);
		condition.expression = arguments.getModifier_Procedure();
	}
	else if (arguments.hasModifier( LanguageModifiers::PromptUser ))
	{
		condition.promptUser = arguments.getModifier_PromptUser();
		DEBUG("[EU] Configuring prompt user: " + BSTR(condition.promptUser));
	}
	else
	{
		condition.type = SPELLscheduleCondition::SCH_FIXED;
	}
}

//============================================================================
// FUNCTION        : setTimeCondition
//============================================================================
void SPELLexecutorUtils::setTimeCondition( SPELLpyArgs& arguments, SPELLscheduleCondition& condition )
{
	// We need to differentiate two cases: using modifiers, or using default
	PyObject* conditionObj = NULL;
	condition.type = SPELLscheduleCondition::SCH_TIME;
	DEBUG("[EU] Setting time condition");

	if (arguments.hasModifier(LanguageModifiers::Until))
	{
		//-----------------------------------------------------------
		// TIME CONDITION (Must be absolute)
		//-----------------------------------------------------------
		conditionObj = arguments[LanguageModifiers::Until];

		// The condition may be a string
		if (PyString_Check(conditionObj))
		{
			std::string repr = PYSTR(conditionObj);
			condition.targetTime = SPELLpythonHelper::instance().evalTime(repr);
		}
		// The condition may be a TIME instance
		else if (SPELLpythonHelper::instance().isTime(conditionObj))
		{
			condition.targetTime = SPELLpythonHelper::instance().evalTime(conditionObj);
		}
		// We do not accept numbers here, since Until requires absolute times
		else
		{
			THROW_EXCEPTION("Wait failed", "Must use absolute times for Until modifier", SPELL_PYERROR_SYNTAX );
		}

		// Check if the target time is not delta
		if (condition.targetTime.isDelta())
		{
			THROW_EXCEPTION("Wait failed", "Must use absolute times for Until modifier", SPELL_PYERROR_SYNTAX );
		}
	}
	else if (arguments.hasModifier(LanguageModifiers::Delay))
	{
		//-----------------------------------------------------------
		// TIME CONDITION (Must be relative)
		//-----------------------------------------------------------
		conditionObj = arguments[LanguageModifiers::Delay];

		// The condition may be a string
		if (PyString_Check(conditionObj))
		{
			std::string repr = PYSTR(conditionObj);
			condition.targetTime = SPELLpythonHelper::instance().evalTime(repr);
		}
		// The condition may be a TIME instance
		else if (SPELLpythonHelper::instance().isTime(conditionObj))
		{
			condition.targetTime = SPELLpythonHelper::instance().evalTime(conditionObj);
		}
		// May be a long
		else if (PyLong_Check(conditionObj))
		{
			condition.targetTime = SPELLtime( PyLong_AsLong(conditionObj), true );
		}
		// May be an int
		else if (PyInt_Check(conditionObj))
		{
			condition.targetTime = SPELLtime( PyInt_AsLong(conditionObj), true );
		}
		// May be a float
		else if (PyFloat_Check(conditionObj))
		{
			double secsd = PyFloat_AsDouble(conditionObj);
			unsigned long secs =(unsigned long) secsd;
			unsigned int msecs = (unsigned int)((secsd-secs)*1000);
			condition.targetTime = SPELLtime( secs, msecs, true );
		}
		// Other cases
		else
		{
			THROW_EXCEPTION("Wait failed", "Invalid input for Delay", SPELL_PYERROR_SYNTAX );
		}

		// Check if the target time is not delta
		if (!condition.targetTime.isDelta())
		{
			THROW_EXCEPTION("Wait failed", "Must use relative times for Delay modifier", SPELL_PYERROR_SYNTAX );
		}
	}
	else // No modifier is used but the time is given as positional argument
	{
		if (arguments.size()!=1)
		{
			THROW_EXCEPTION("Wait failed", "Expected Until or Delay modifiers, or valid time as argument", SPELL_PYERROR_SYNTAX );
		}
		conditionObj = arguments[0];

		// If the condition is a string, it wont go thru this section
		// The condition may be a TIME instance
		if (SPELLpythonHelper::instance().isTime(conditionObj))
		{
			condition.targetTime = SPELLpythonHelper::instance().evalTime(conditionObj);
		}
		// May be a long
		else if (PyLong_Check(conditionObj))
		{
			condition.targetTime = SPELLtime( PyLong_AsLong(conditionObj), true );
		}
		// May be an int
		else if (PyInt_Check(conditionObj))
		{
			condition.targetTime = SPELLtime( PyInt_AsLong(conditionObj), true );
		}
		// May be a float
		else if (PyFloat_Check(conditionObj))
		{
			double secsd = PyFloat_AsDouble(conditionObj);
			unsigned long secs =(unsigned long) secsd;
			unsigned int msecs = (unsigned int)((secsd-secs)*1000);
			condition.targetTime = SPELLtime( secs, msecs, true );
		}
		else if (PyString_Check(conditionObj))
		{
			std::string repr = PYSTR(conditionObj);
			condition.targetTime = SPELLpythonHelper::instance().evalTime(repr);
		}
		// Other cases
		else
		{
			condition.type = SPELLscheduleCondition::SCH_NONE;
			THROW_EXCEPTION("Wait failed", "Invalid time input", SPELL_PYERROR_SYNTAX );
		}
	}
}

//============================================================================
// FUNCTION        : setVerificationCondition
//============================================================================
void SPELLexecutorUtils::setVerificationCondition( SPELLpyArgs& arguments, SPELLscheduleCondition& condition, PyObject* configObj )
{
	PyObject* conditionObj = arguments[0];

	condition.type = SPELLscheduleCondition::SCH_VERIFICATION;
	// Reference count is held inside the condition
	// Get the condition
	condition.setVerification(conditionObj);
	// Prepare the configuration
	condition.setConfig(configObj);

	DEBUG("[EU] Setting verification condition");
}

//============================================================================
// FUNCTION        : setEvaluationCondition
//============================================================================
void SPELLexecutorUtils::setEvaluationCondition( SPELLpyArgs& arguments, SPELLscheduleCondition& condition )
{
	PyObject* conditionObj = arguments[0];
	// Try to evaluate a time expression
	try
	{
		if (PyString_Check(conditionObj))
		{
			std::string repr = PYSTR(conditionObj);
			condition.targetTime = SPELLpythonHelper::instance().evalTime(repr);
		}
		else
		{
			condition.targetTime = SPELLpythonHelper::instance().evalTime(conditionObj);
		}
		condition.type = SPELLscheduleCondition::SCH_TIME;
	}
	catch(SPELLcoreException& ex)
	{
		THROW_EXCEPTION("Wait failed", "Expected a TM verification list, time condition or string: " + ex.what() + "\n" + PYREPR(conditionObj), SPELL_PYERROR_SYNTAX );
	}
}

//============================================================================
// FUNCTION        : configureConditionArguments
//============================================================================
void SPELLexecutorUtils::configureConditionArguments( SPELLpyArgs& arguments, SPELLscheduleCondition& condition, PyObject* configObj )
{
	PyObject* conditionObj = arguments[0];

	DEBUG("[EU] Condition object: " + PYREPR(conditionObj));

	if (PyLong_Check(conditionObj) || PyInt_Check(conditionObj) || PyFloat_Check(conditionObj) || PyString_Check(conditionObj))
	{
		setTimeCondition( arguments, condition );
	}
	else if (PyList_Check(conditionObj) || SPELLpythonHelper::instance().isInstance( conditionObj, "Expression", "spell.lib.adapter.expression" ))
	{
		setVerificationCondition( arguments, condition, configObj );
	}
	else
	{
		setEvaluationCondition( arguments, condition );
	}

	// Configure others
	if (arguments.hasModifier( LanguageModifiers::PromptUser ))
	{
		condition.promptUser = arguments.getModifier_PromptUser();
		DEBUG("[EU] Configuring prompt user: " + BSTR(condition.promptUser));
	}

	if (arguments.hasModifier( LanguageModifiers::Delay ))
	{
		condition.timeout = arguments.getModifier_Delay();
	}
}

//============================================================================
// FUNCTION        : statusToString
//============================================================================
std::string SPELLexecutorUtils::statusToString( SPELLexecutorStatus st )
{
    return MessageValue::StatusStr[st];
}

//============================================================================
// FUNCTION        : statusToPyString
//============================================================================
PyObject* SPELLexecutorUtils::statusToPyString( SPELLexecutorStatus st )
{
    return SSTRPY( statusToString(st) );
}

//============================================================================
// FUNCTION        : stringToStatus
//============================================================================
SPELLexecutorStatus SPELLexecutorUtils::stringToStatus( std::string st )
{
    int idx = 0;
    for( idx = 0; idx<MessageValue::NumStatus; idx++)
    {
        if (st == MessageValue::StatusStr[idx]) return (SPELLexecutorStatus) idx;
    }
    return STATUS_UNKNOWN;
}

//============================================================================
// FUNCTION        : pyStringToStatus
//============================================================================
SPELLexecutorStatus SPELLexecutorUtils::pyStringToStatus( PyObject* pyst )
{
    std::string equiv = PyString_AsString( pyst );
    return stringToStatus( equiv );
}

