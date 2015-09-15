// ################################################################################
// FILE       : SPELLrequestProcessor.C
// DATE       : Mar 17, 2011
// PROJECT    : SPELL
// DESCRIPTION: Implementation of the message request processor
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
// Local includes ----------------------------------------------------------
#include "SPELL_CIFS/SPELLrequestProcessor.H"
#include "SPELL_CIFS/SPELLserverCif.H"
#include "SPELL_CIFS/SPELLcifPromptHelper.H"
// Project includes --------------------------------------------------------
#include "SPELL_EXC/SPELLexecutor.H"
#include "SPELL_EXC/SPELLexecutorUtils.H"
#include "SPELL_EXC/SPELLcommand.H"
#include "SPELL_IPC/SPELLipcError.H"
#include "SPELL_WRP/SPELLconstants.H"
#include "SPELL_UTIL/SPELLlog.H"
#include "SPELL_UTIL/SPELLcompression.H"
#include "SPELL_UTIL/SPELLpythonMonitors.H"
#include "SPELL_IPC/SPELLipc_Executor.H"
#include "SPELL_IPC/SPELLipc_Context.H"
#include "SPELL_IPC/SPELLipcHelper.H"
#include "SPELL_SYN/SPELLmonitor.H"
#include "SPELL_DTA/SPELLdtaContainerObject.H"
#include "SPELL_DTA/SPELLdtaVariableObject.H"
#include "SPELL_CFG/SPELLbrowsableLibMode.H"
#include "SPELL_WRP/SPELLpyHandle.H"
using namespace PythonConstants;
using namespace LanguageConstants;
using namespace LanguageModifiers;
// System includes ---------------------------------------------------------


#define VALUE_CHUNK_SIZE 5000

//=============================================================================
// CONSTRUCTOR: SPELLrequestProcessor::SPELLrequestProcessor
//=============================================================================
SPELLrequestProcessor::SPELLrequestProcessor( SPELLserverCif& cif )
: m_cif(cif),
  m_valueChunks(),
  m_requestLock()
{
    DEBUG("[CIF] Created request processor");
}

//=============================================================================
// DESTRUCTOR: SPELLrequestProcessor::~SPELLrequestProcessor
//=============================================================================
SPELLrequestProcessor::~SPELLrequestProcessor()
{
    DEBUG("[CIF] Destroyed request processor");
}

//=============================================================================
// METHOD: SPELLrequestProcessor::processMessageForChild
//=============================================================================
void SPELLrequestProcessor::processMessageForChild( const SPELLipcMessage& msg )
{
	SPELLmonitor mon(m_requestLock);
    std::string procId = msg.get(MessageField::FIELD_PROC_ID);

    if (SPELLexecutor::instance().getChildManager().hasChild() &&
    	SPELLexecutor::instance().getChildManager().getChildId() == procId)
    {
		if (msg.getId() == ContextMessages::MSG_EXEC_OP)
        {
        	DEBUG("[CIF] Notifying child operation");
            std::string operation = msg.get(MessageField::FIELD_EXOP);
            if (operation == MessageValue::DATA_EXOP_CLOSE)
            {
            	LOG_INFO("[CIF] Child closed");
                SPELLexecutor::instance().getChildManager().notifyChildClosed();
            }
            else if ( operation == MessageValue::DATA_EXOP_KILL || operation == MessageValue::DATA_EXOP_CRASH )
            {
            	LOG_WARN("[CIF] Child killed");
                SPELLexecutor::instance().getChildManager().notifyChildKilled();
            }
            else if ( operation == MessageValue::DATA_EXOP_OPEN )
            {
            	LOG_INFO("[CIF] Child open: " + msg.dataStr());
            }
            else if ( operation == MessageValue::DATA_TYPE_STATUS )
            {
    			std::string status = msg.get(MessageField::FIELD_EXEC_STATUS);
    			SPELLexecutorStatus childStatus = SPELLexecutorUtils::stringToStatus(status);
    			if (childStatus == STATUS_ERROR)
    			{
    				std::string childError = msg.get(MessageField::FIELD_ERROR);
    				std::string childErrorReason = msg.get(MessageField::FIELD_REASON);
    				LOG_INFO("[CIF] Error info: " + childError + ": " + childErrorReason);
    				SPELLexecutor::instance().getChildManager().notifyChildError( childError, childErrorReason );
    			}
    			else if (childStatus == STATUS_ABORTED)
    			{
    				std::string childError = "Child execution did not finish";
    				std::string childErrorReason = "Execution was aborted";
    				SPELLexecutor::instance().getChildManager().notifyChildError( childError, childErrorReason );
    			}
    			else
    			{
    				SPELLexecutor::instance().getChildManager().notifyChildStatus( childStatus );
    			}
    			LOG_INFO("[CIF] Child status: " + status );
            }
            else
            {
            	LOG_INFO("[CIF] Child operation ignored: " + operation);
            }
        }
        else if (msg.getType() == MSG_TYPE_ERROR)
        {
        	DEBUG("[CIF] Notifying child error");
            std::string childError = msg.get(MessageField::FIELD_ERROR);
            std::string childErrorReason = msg.get(MessageField::FIELD_REASON);
            SPELLexecutor::instance().getChildManager().notifyChildError( childError, childErrorReason );
        }
    }
    else
    {
        LOG_ERROR("[E] Unexpected message to a child: " + procId + ": " + msg.getId() );
    }
}

//=============================================================================
// METHOD: SPELLrequestProcessor::processGetConfig
//=============================================================================
void SPELLrequestProcessor::processGetConfig( const SPELLipcMessage& msg, SPELLipcMessage& response )
{
	SPELLmonitor mon(m_requestLock);
    DEBUG("[CIF] Request to get executor config");
    response.setId(ExecutorMessages::RSP_GET_CONFIG);
    SPELLexecutorConfig& config = SPELLexecutor::instance().getConfiguration();
    response.set(ExecutorConstants::RunInto, config.isRunInto() ? True : False);

    std::string execDelay = ISTR(config.getExecDelay());
    std::string promptDelay = ISTR(config.getPromptWarningDelay());
    std::string byStep = config.isByStep() ? True : False;
    std::string browsableLibStr = config.getBrowsableLibStr();
    std::string forceTcConfirm = config.isForceTcConfirm() ? True : False;

    response.set(ExecutorConstants::ExecDelay, execDelay);
    response.set(ExecutorConstants::PromptDelay, promptDelay);
    response.set(ExecutorConstants::ByStep, byStep);
    response.set(ExecutorConstants::BrowsableLib, browsableLibStr );
    response.set(ExecutorConstants::ForceTcConfirm, forceTcConfirm);

    LOG_INFO("Current executor configuration:");
    LOG_INFO("           run into   : " + (config.isRunInto() ? True : False) );
    LOG_INFO("           exec delay : " + execDelay);
    LOG_INFO(" prompt warning delay : " + promptDelay);
    LOG_INFO("              by step : " + byStep);
    LOG_INFO("        browsable lib : " + browsableLibStr );
    LOG_INFO("        force confirm : " + forceTcConfirm);
    DEBUG("[CIF] Request to get executor config done");
}

//=============================================================================
// METHOD: SPELLrequestProcessor::processGetStatus
//=============================================================================
void SPELLrequestProcessor::processGetStatus( const SPELLipcMessage& msg, SPELLipcMessage& response )
{
	SPELLmonitor mon(m_requestLock);
    DEBUG("[CIF] Request to get executor status");
    response.setId(ExecutorMessages::RSP_EXEC_STATUS);
    response.set( MessageField::FIELD_PROC_ID, m_cif.getProcId() );

    SPELLexecutorStatus st = SPELLexecutor::instance().getStatus();
    response.set( MessageField::FIELD_EXEC_STATUS, SPELLexecutorUtils::statusToString( st ));
    if ( st == STATUS_ERROR )
    {
    	SPELLcoreException* ex = SPELLerror::instance().getError();
    	if (ex)
    	{
    		response.set( MessageField::FIELD_ERROR, ex->getError() );
    		response.set( MessageField::FIELD_REASON, ex->getReason() );
    		response.set( MessageField::FIELD_FATAL, ex->isFatal() ? "true" : "false" );
    	}
    	else
    	{
    		response.set( MessageField::FIELD_ERROR, "Unknown error" );
    		response.set( MessageField::FIELD_REASON, " " );
    		response.set( MessageField::FIELD_FATAL, "true" );
    	}
    }
    else if ( st == STATUS_PROMPT )
    {
    	SPELLpromptDefinition def = m_cif.getPromptInfo();
    	std::string timeStr = SPELLutils::timestampUsec();
        response.set(MessageField::FIELD_TEXT, def.message);
        response.set(MessageField::FIELD_DATA_TYPE, ISTR(def.typecode));
        response.set(MessageField::FIELD_TIME, timeStr);
        response.set(MessageField::FIELD_DEFAULT, def.defaultAnswer);
        response.set(MessageField::FIELD_SCOPE, ISTR(def.scope));
        SPELLcifPromptHelper helper;
        helper.completeOptions( def, response, "" );
    }

    response.set( MessageField::FIELD_CSP, SPELLexecutor::instance().getCallstack().getFullStack() );

    DEBUG("[CIF] Request to get executor status done");
}

//=============================================================================
// METHOD: SPELLrequestProcessor::processSetConfig
//=============================================================================
void SPELLrequestProcessor::processSetConfig( const SPELLipcMessage& msg, SPELLipcMessage& response )
{
	SPELLmonitor mon(m_requestLock);
    DEBUG("[CIF] Request to change executor config");
    response.setId(ExecutorMessages::RSP_SET_CONFIG);
    std::string runInto = msg.get(ExecutorConstants::RunInto);
    std::string execDelay = msg.get(ExecutorConstants::ExecDelay);
    std::string promptDelay = msg.get(ExecutorConstants::PromptDelay);
    std::string byStep = msg.get(ExecutorConstants::ByStep);
    std::string browsableLibStr = msg.get(ExecutorConstants::BrowsableLib);
    std::string forceTcConfirm = msg.get(ExecutorConstants::ForceTcConfirm);


    LOG_INFO("New executor configuration ----------------------------------");
    LOG_INFO("   Run into  : " + runInto);
    LOG_INFO("   Delay     : " + execDelay);
    LOG_INFO(" Prompt Delay: " + promptDelay);
    LOG_INFO("   By step   : " + byStep);
    LOG_INFO("   Br. lib   : " + browsableLibStr);
    LOG_INFO("   TC confirm: " + forceTcConfirm);
    LOG_INFO("-------------------------------------------------------------");

    SPELLexecutor::instance().setRunInto( (runInto == True) );
    SPELLexecutor::instance().setExecDelay( STRI( execDelay ));
    SPELLexecutor::instance().setPromptWarningDelay ( STRI( promptDelay ));
    SPELLexecutor::instance().setByStep( (byStep == True) );
    SPELLexecutor::instance().setBrowsableLibStr( browsableLibStr );
    SPELLexecutor::instance().setForceTcConfirm( forceTcConfirm == True );
    DEBUG("[CIF] Request to change executor config done");
}

//=============================================================================
// METHOD: SPELLrequestProcessor::processSetBreakpoint
//=============================================================================
void SPELLrequestProcessor::processSetBreakpoint( const SPELLipcMessage& msg, SPELLipcMessage& response )
{
	SPELLmonitor mon(m_requestLock);
    DEBUG("[CIF] Request to set breakpoint");
    // Update the repsonse message
    response.setId(ExecutorMessages::RSP_SET_BREAKPOINT);
    // Retrieve the information from the message
    std::string codeId = msg.get(MessageField::FIELD_BREAKPOINT_PROC);
    std::string targetLine = msg.get(MessageField::FIELD_BREAKPOINT_LINE);
    std::string bpType = msg.get(MessageField::FIELD_BREAKPOINT_TYPE);
    SPELLbreakpointType bp = breakpointTypeFromString(bpType);
    // Perform the action
    SPELLexecutor::instance().setBreakpoint(codeId, STRI(targetLine), bp);
}

//=============================================================================
// METHOD: SPELLrequestProcessor::processClearBreakpoints
//=============================================================================
void SPELLrequestProcessor::processClearBreakpoints( const SPELLipcMessage& msg, SPELLipcMessage& response )
{
	SPELLmonitor mon(m_requestLock);
    DEBUG("[CIF] Request to clear breakpoints for the given code");
    // Update the response message
    response.setId(ExecutorMessages::RSP_CLEAR_BREAKPOINT);
    // Perform the action in the executor
    SPELLexecutor::instance().clearBreakpoints();
}

//=============================================================================
// METHOD: SPELLrequestProcessor::processCheckVariablesEnabled
//=============================================================================
void SPELLrequestProcessor::processCheckVariablesEnabled( const SPELLipcMessage& msg, SPELLipcMessage& response )
{
	SPELLmonitor mon(m_requestLock);
    // Update the response message
    response.setId(ExecutorMessages::RSP_WVARIABLES_ENABLED);
    bool enabled = SPELLexecutor::instance().getVariableManager().isEnabled();
    response.set( MessageField::FIELD_WVARIABLES_ENABLED, enabled ? "True" : "False");
}

//=============================================================================
// METHOD: SPELLrequestProcessor::processGetVariables
//=============================================================================
void SPELLrequestProcessor::processGetVariables( const SPELLipcMessage& msg, SPELLipcMessage& response )
{
	SPELLmonitor mon(m_requestLock);
    // Update the response message
    response.setId(ExecutorMessages::RSP_GET_VARIABLES);

	if (msg.get( MessageField::FIELD_CHUNK ) != "")
	{
		int chunkNo = STRI( msg.get( MessageField::FIELD_CHUNK ) );
		DEBUG("Get variable value chunk " + ISTR(chunkNo));
		std::string valueChunk = m_valueChunks[chunkNo];
		int totalChunks = m_valueChunks.size();
		response.set( MessageField::FIELD_CHUNK, ISTR(chunkNo) );
		response.set( MessageField::FIELD_TOTAL_CHUNKS, ISTR(totalChunks));
		response.set( MessageField::FIELD_VARIABLE_VALUE, valueChunk );
		DEBUG("Given chunk " +ISTR(chunkNo) + " (size " + ISTR(valueChunk.size())+ ")");
	}
	else
	{
		if (!SPELLexecutor::instance().getVariableManager().isStatusValid())
		{
			LOG_WARN("Variable manager is not in valid status");
			response.setType(MSG_TYPE_ERROR);
			response.set( MessageField::FIELD_ERROR, "Cannot retrieve variables");
			response.set( MessageField::FIELD_REASON, "Variable analysis cannot be done in the current status");
			response.set( MessageField::FIELD_FATAL, PythonConstants::False );
			return;
		}

		std::vector<SPELLvarInfo> localVars;
		std::vector<SPELLvarInfo> globalVars;

		if (!SPELLexecutor::instance().getVariableManager().isEnabled())
		{
			LOG_WARN("Variable manager is disabled: trigger analysis on demand");
			SPELLexecutor::instance().getVariableManager().analyze();
		}

		globalVars = SPELLexecutor::instance().getVariableManager().getGlobalVariables();
		localVars = SPELLexecutor::instance().getVariableManager().getLocalVariables();

		std::string names = "";
		std::string types = "";
		std::string values = "";
		std::string globals = "";

		for(unsigned int index = 0; index<globalVars.size(); index++)
		{
			if (names != "")
			{
				names += VARIABLE_SEPARATOR;
				types += VARIABLE_SEPARATOR;
				values += VARIABLE_SEPARATOR;
				globals += VARIABLE_SEPARATOR;
			}
			names += globalVars[index].varName;
			types += globalVars[index].varType;
			values += globalVars[index].varValue;
			globals += "True";
		}

		for(unsigned int index = 0; index<localVars.size(); index++)
		{
			if (names != "")
			{
				names += VARIABLE_SEPARATOR;
				types += VARIABLE_SEPARATOR;
				values += VARIABLE_SEPARATOR;
				globals += VARIABLE_SEPARATOR;
			}
			names += localVars[index].varName;
			types += localVars[index].varType;
			values += localVars[index].varValue;
			globals += "False";
		}

		// Chunk if needed
		if (values.size()>VALUE_CHUNK_SIZE)
		{
			DEBUG("Variable value needs chunk: " + ISTR(values.size()));
			m_valueChunks.clear();

			int numChunks = values.size()/VALUE_CHUNK_SIZE;
			int startIdx = 0;
			for(int index=0; index<numChunks; index++)
			{
				DEBUG(" - chunk " + ISTR(index) + ": " + ISTR(startIdx) + "->" + ISTR(startIdx+VALUE_CHUNK_SIZE));
				m_valueChunks.push_back(values.substr(startIdx,VALUE_CHUNK_SIZE));
				startIdx += VALUE_CHUNK_SIZE;
			}
			m_valueChunks.push_back(values.substr(startIdx, values.size()-startIdx ));
			DEBUG("Total " + ISTR(m_valueChunks.size()) + " chunks");

			response.set( MessageField::FIELD_CHUNK, "0" );

			if (m_valueChunks.size()==1)
			{
				response.set( MessageField::FIELD_VARIABLE_NAME, names );
				response.set( MessageField::FIELD_VARIABLE_TYPE, types );
				response.set( MessageField::FIELD_VARIABLE_GLOBAL, globals );
				response.set( MessageField::FIELD_TOTAL_CHUNKS, "0");
				response.set( MessageField::FIELD_VARIABLE_VALUE, values );
				DEBUG("No chunks done");
			}
			else
			{
				DEBUG("Start chunks " + ISTR(m_valueChunks.size()));
				response.set( MessageField::FIELD_VARIABLE_NAME, names );
				response.set( MessageField::FIELD_VARIABLE_TYPE, types );
				response.set( MessageField::FIELD_VARIABLE_GLOBAL, globals );
				response.set( MessageField::FIELD_TOTAL_CHUNKS, ISTR(m_valueChunks.size()));
				response.set( MessageField::FIELD_VARIABLE_VALUE, m_valueChunks[0] );
				DEBUG("Given chunk 0 (size " + ISTR(m_valueChunks[0].size())+ ")");
			}
		}
		else
		{
			DEBUG("No need for chunk");
			response.set( MessageField::FIELD_VARIABLE_NAME, names );
			response.set( MessageField::FIELD_VARIABLE_TYPE, types );
			response.set( MessageField::FIELD_VARIABLE_GLOBAL, globals );
			response.set( MessageField::FIELD_TOTAL_CHUNKS, "0");
			response.set( MessageField::FIELD_VARIABLE_VALUE, values );
		}
	}
}

//=============================================================================
// METHOD: SPELLrequestProcessor::processChangeVariable
//=============================================================================
void SPELLrequestProcessor::processChangeVariable( const SPELLipcMessage& msg, SPELLipcMessage& response )
{
	SPELLmonitor mon(m_requestLock);
    // Update the response message
    response.setId(ExecutorMessages::RSP_CHANGE_VARIABLE);

    std::string name = msg.get( MessageField::FIELD_VARIABLE_NAME );
    std::string valueExpression = msg.get( MessageField::FIELD_VARIABLE_VALUE );
    bool global = (msg.get( MessageField::FIELD_VARIABLE_GLOBAL ) == "True");

    // Type and registration flag are not important here
    SPELLvarInfo info(name,"",valueExpression,global);

	SPELLexecutor::instance().getVariableManager().changeVariable( info );
}

//=============================================================================
// METHOD: SPELLrequestProcessor::processGetDictionary
//=============================================================================
void SPELLrequestProcessor::processGetDictionary( const SPELLipcMessage& msg, SPELLipcMessage& response )
{
	SPELLmonitor mon(m_requestLock);
    // Update the response message
    response.setId(ExecutorMessages::RSP_GET_DICTIONARY);

    std::string dictName = msg.get( MessageField::FIELD_DICT_NAME );

	DEBUG("Get dictionary contents for " + dictName);

	if (msg.get( MessageField::FIELD_CHUNK ) != "")
	{
		int chunkNo = STRI( msg.get( MessageField::FIELD_CHUNK ) );
		DEBUG("Get dictionary contents chunk " + ISTR(chunkNo));
		std::string valueChunk = m_valueChunks[chunkNo];
		int totalChunks = m_valueChunks.size();
		response.set( MessageField::FIELD_CHUNK, ISTR(chunkNo) );
		response.set( MessageField::FIELD_TOTAL_CHUNKS, ISTR(totalChunks));
		response.set( MessageField::FIELD_DICT_CONTENTS, valueChunk );
		DEBUG("Given chunk " +ISTR(chunkNo) + " (size " + ISTR(valueChunk.size())+ ")");
	}
	else
	{
		std::string contents = "";

		// These requests run on a different thread than the interpreter's, therefore the
		// thread safe operations need to be requested. AFTER this, the GIL must be acquired
		// in order to perform the evaluation of expressions.
		// RACC 14-MAY SPELLsafeThreadOperations ops;
		SPELLsafePythonOperations ops("SPELLrequestProcessor::processGetDictionary");

		PyObject* dict = SPELLexecutor::instance().getVariableManager().getVariableRef(dictName);

		if (dict == NULL)
		{
			LOG_ERROR("Dictionary " + dictName + " not found");
			//TODO maybe raise an expection/warning
			contents = None;
		}
		else if ( isDataContainer(dict) )
		{
			DEBUG("Getting data container " + dictName);

			SPELLdtaContainerObject* containerObj = reinterpret_cast<SPELLdtaContainerObject*>(dict);
			PyObject* keyList = containerObj->container->getKeys();
			unsigned int numKeys = PyList_Size(keyList);
			DEBUG( ISTR(numKeys) + " keys to read");
			for(unsigned int index = 0; index< numKeys; index++)
			{
				PyObject* key = PyList_GetItem(keyList,index);
				Py_XINCREF(key);
				PyObject* value = PyDict_GetItem(containerObj->container->getDict(),key);
				Py_XINCREF(value);
				SPELLdtaVariableObject* varObj = reinterpret_cast<SPELLdtaVariableObject*>(value);
				SPELLdtaVariable* var = varObj->var;

				std::string confirm = var->getConfirmGet() ? "true" : "false";

				std::string valueStr = PYSSTR(var->getValueEx());

				// Extract range
				std::vector<SPELLpyValue> range = var->getRange();
				std::string rangeStr = "[]";
				if (range.size()==2)
				{
					rangeStr = "[" + range[0].str() + "," + range[1].str() + "]";
				}

				// Extract expected
				std::string expStr = "[]";
				std::vector<SPELLpyValue> expected = var->getExpected();
				if (expected.size()>0)
				{
					expStr = "[";
					for(unsigned int idx2 = 0; idx2 < expected.size(); idx2++)
					{
						if (idx2>0) expStr += ",";
						expStr += expected[idx2].str();
					}
					expStr += "]";
				}

				std::string valueType = var->getType();
				std::string varCode = PYSSTR(key)      + VARIABLE_PROPERTY_SEPARATOR +
						              valueStr         + VARIABLE_PROPERTY_SEPARATOR +
						              var->getFormat() + VARIABLE_PROPERTY_SEPARATOR +
						              valueType        + VARIABLE_PROPERTY_SEPARATOR +
						              rangeStr         + VARIABLE_PROPERTY_SEPARATOR +
						              expStr           + VARIABLE_PROPERTY_SEPARATOR +
						              confirm;

				Py_XDECREF(key);
				Py_XDECREF(value);
				if (contents.size()>0) contents += VARIABLE_SEPARATOR;
				contents+= varCode;
			}
		}
		else if ( SPELLpythonHelper::instance().isDatabase(dict) )
		{
			DEBUG("Getting SPELL database " + dictName);

			PyObject* keyList = SPELLpythonHelper::instance().callMethod(dict, "keys", NULL);
			unsigned int numKeys = PyList_Size(keyList);
			DEBUG( ISTR(numKeys) + " keys to read");
			for(unsigned int index = 0; index< numKeys; index++)
			{
				PyObject* key = PyList_GetItem(keyList,index);
				Py_XINCREF(key);
				PyObject* value = PyObject_GetItem(dict,key);
				Py_XINCREF(value);
				std::string valueStr = PYSSTR(value);
				std::string varCode = PYSSTR(key)      + VARIABLE_PROPERTY_SEPARATOR +  valueStr;
				Py_XDECREF(key);
				Py_XDECREF(value);
				if (contents.size()>0) contents += VARIABLE_SEPARATOR;
				contents+= varCode;
			}
		}
		else if ( PyDict_Check(dict) )
		{
			DEBUG("Getting dictionary " + dictName);

			SPELLpyHandle keyList = PyDict_Keys(dict);
			unsigned int numKeys = PyList_Size(keyList.get());
			DEBUG( ISTR(numKeys) + " keys to read");
			for(unsigned int index = 0; index< numKeys; index++)
			{
				SPELLpyHandle key = PyList_GetItem(keyList.get(),index);
				SPELLpyHandle value = PyDict_GetItem(dict,key.get());
				std::string valueStr = PYSSTR(value.get());
				std::string varCode = PYSSTR(key.get())      + VARIABLE_PROPERTY_SEPARATOR +  valueStr;
				if (contents.size()>0) contents += VARIABLE_SEPARATOR;
				contents+= varCode;
			}
		}
		else
		{
			contents = PYREPR(dict);
		}

		// Chunk if needed
		if (contents.size()>VALUE_CHUNK_SIZE)
		{
			DEBUG("Dictionary contents needs chunk: " + ISTR(contents.size()));
			m_valueChunks.clear();

			int numChunks = contents.size()/VALUE_CHUNK_SIZE;
			int startIdx = 0;
			for(int index=0; index<numChunks; index++)
			{
				DEBUG(" - chunk " + ISTR(index) + ": " + ISTR(startIdx) + "->" + ISTR(startIdx+VALUE_CHUNK_SIZE));
				m_valueChunks.push_back(contents.substr(startIdx,VALUE_CHUNK_SIZE));
				startIdx += VALUE_CHUNK_SIZE;
			}
			m_valueChunks.push_back(contents.substr(startIdx, contents.size()-startIdx ));
			DEBUG("Total " + ISTR(m_valueChunks.size()) + " chunks");

			response.set( MessageField::FIELD_CHUNK, "0" );

			if (m_valueChunks.size()==1)
			{
				response.set( MessageField::FIELD_DICT_CONTENTS, contents );
				response.set( MessageField::FIELD_TOTAL_CHUNKS, "0");
				DEBUG("No chunks done");
			}
			else
			{
				DEBUG("Start chunks " + ISTR(m_valueChunks.size()));
				response.set( MessageField::FIELD_TOTAL_CHUNKS, ISTR(m_valueChunks.size()));
				response.set( MessageField::FIELD_DICT_CONTENTS, m_valueChunks[0] );
				DEBUG("Given chunk 0 (size " + ISTR(m_valueChunks[0].size())+ ")");
			}
		}
		else
		{
			DEBUG("No need for chunk");
			response.set( MessageField::FIELD_TOTAL_CHUNKS, "0");
			response.set( MessageField::FIELD_DICT_CONTENTS, contents);
		}
	}
}

//=============================================================================
// METHOD: SPELLrequestProcessor::isDataContainer
//=============================================================================
bool SPELLrequestProcessor::isDataContainer(PyObject* dict)
{
    PyTypeObject* type = reinterpret_cast<PyTypeObject*>(dict->ob_type);
    return std::string(type->tp_name) == "spell.lib.adapter.data.DataContainer";
}

//=============================================================================
// METHOD: SPELLrequestProcessor::processUpdateDictionary
//=============================================================================
void SPELLrequestProcessor::processUpdateDictionary( const SPELLipcMessage& msg, SPELLipcMessage& response )
{
	SPELLmonitor mon(m_requestLock);
    // Update the response message
    response.setId(ExecutorMessages::RSP_UPD_DICTIONARY);

    std::string dictName = msg.get( MessageField::FIELD_DICT_NAME );

    bool mergeNew = false;

    if (msg.hasField( MessageField::FIELD_DICT_MERGENEW ))
	{
    	std::string mergeNewStr = msg.get(MessageField::FIELD_DICT_MERGENEW);
    	mergeNew = mergeNewStr == "True" || mergeNewStr == "true";
	}

    DEBUG("Update dictionary contents for " + dictName);

	// These requests run on a different thread than the interpreter's, therefore the
	// thread safe operations need to be requested. AFTER this, the GIL must be acquired
	// in order to perform the evaluation of expressions.
	//RACC 15-MAY SPELLsafeThreadOperations ops;
	SPELLsafePythonOperations ops("SPELLrequestProcessor::processUpdateDictionary");

	PyObject* dict = SPELLexecutor::instance().getVariableManager().getVariableRef(dictName);
	Py_XINCREF(dict);

	if (dict == NULL)
	{
		LOG_ERROR("Dictionary " + dictName + " not found");
	}
	else if ( isDataContainer(dict) )
	{
		DEBUG("Updating data container " + dictName);

		SPELLdtaContainerObject* containerObj = reinterpret_cast<SPELLdtaContainerObject*>(dict);
		std::string varData = msg.get( MessageField::FIELD_DICT_CONTENTS );

		bool notificationsEnabled = containerObj->container->areNotificationsEnabled();
		if (notificationsEnabled)
		{
			containerObj->container->setNotificationsEnabled(false);
		}

		std::string error = "";

		std::vector<std::string> fields = SPELLutils::tokenize(varData, VARIABLE_PROPERTY_SEPARATOR_STR);

		std::string varName = fields[0];
		std::string varValue = fields[1];
		std::string varFormat = fields[2];
		std::string varConfirm = fields[3];

		if (varFormat == LanguageConstants::DEC || varFormat == "NONE" ) varFormat = "";

		DEBUG("Updating variable " + varName + "=" + varValue + + ", format " + varFormat + ", confirm " + varConfirm);
		try
		{
			PyObject* value = SPELLpythonHelper::instance().eval(varValue,false);
			Py_INCREF(value);
			// IMPORTANT: we need to bypass the setValue method of the container
			// since we do not want to have prompts to the user at this stage. We
			// rather capture exceptions here and log the errors.

			PyObject* key = SSTRPY(varName);
			Py_INCREF(key);

			// Also, ignore those variable names that do not exist in the dictionary
			if (containerObj->container->hasKey(key))
			{
				PyObject* varObj = PyDict_GetItem(containerObj->container->getDict(), key );
				SPELLdtaVariableObject* dtaVar = reinterpret_cast<SPELLdtaVariableObject*>(varObj);
				dtaVar->var->setValue(value);
				dtaVar->var->setConfirmGet( (varConfirm == "True") );
				dtaVar->var->setFormat( varFormat );
			}
			else if (mergeNew)
			{
				containerObj->container->setValue(key,value);
				PyObject* varObj = PyDict_GetItem(containerObj->container->getDict(), key );
				SPELLdtaVariableObject* dtaVar = reinterpret_cast<SPELLdtaVariableObject*>(varObj);
				dtaVar->var->setConfirmGet( (varConfirm == "True") );
				dtaVar->var->setFormat( varFormat );
			}
			else
			{
				error = "  - variable '" + varName + "': does not exist in the data container";
			}
		}
		catch(SPELLcoreException& ex)
		{
			error = "  - variable '" + varName + "': " + ex.what();
		}

		if (error != "")
		{
			response = SPELLipcHelper::createErrorResponse( ExecutorMessages::RSP_UPD_DICTIONARY, msg);
			response.set( MessageField::FIELD_ERROR, error);
			response.set( MessageField::FIELD_REASON, "");
			response.set( MessageField::FIELD_FATAL, PythonConstants::False);
		}

		if (notificationsEnabled)
		{
			containerObj->container->setNotificationsEnabled(true);
		}

		DEBUG("Updating data container done");
	}
	else if(SPELLpythonHelper::instance().isDatabase(dict))
	{
		DEBUG("Updating SPELL database " + dictName);

		std::string varData = msg.get( MessageField::FIELD_DICT_CONTENTS );
		std::string error = "";

		std::vector<std::string> fields = SPELLutils::tokenize(varData, VARIABLE_PROPERTY_SEPARATOR_STR);

		std::string varName = fields[0];
		std::string varValue = fields[1];

		DEBUG("Updating variable " + varName + "=" + varValue);
		try
		{
			PyObject* value = SPELLpythonHelper::instance().eval(varValue,false);
			Py_INCREF(value);
			PyObject* key = SSTRPY(varName);
			Py_INCREF(key);
			PyObject_SetItem(dict,key,value);
			SPELLpythonHelper::instance().checkError();
		}
		catch(SPELLcoreException& ex)
		{
			error = "  - variable '" + varName + "': " + ex.what();
		}

		if (error != "")
		{
			response = SPELLipcHelper::createErrorResponse( ExecutorMessages::RSP_UPD_DICTIONARY, msg);
			response.set( MessageField::FIELD_ERROR, error);
			response.set( MessageField::FIELD_REASON, "");
			response.set( MessageField::FIELD_FATAL, PythonConstants::False);
		}
		DEBUG("Updating dictionary done");
	}
	// Update of regular dictionaries
	else if (PyDict_Check(dict))
	{
		DEBUG("Updating dictionary " + dictName);

		std::string varData = msg.get( MessageField::FIELD_DICT_CONTENTS );
		std::string error = "";

		std::vector<std::string> fields = SPELLutils::tokenize(varData, VARIABLE_PROPERTY_SEPARATOR_STR);

		std::string varName = fields[0];
		std::string varValue = fields[1];

		DEBUG("Updating variable " + varName + "=" + varValue);
		try
		{
			PyObject* value = SPELLpythonHelper::instance().eval(varValue,false);
			Py_INCREF(value);
			PyObject* key = SSTRPY(varName);
			Py_INCREF(key);
			PyObject_SetItem(dict,key,value);
			SPELLpythonHelper::instance().checkError();
		}
		catch(SPELLcoreException& ex)
		{
			error = "  - variable '" + varName + "': " + ex.what();
		}

		if (error != "")
		{
			response = SPELLipcHelper::createErrorResponse( ExecutorMessages::RSP_UPD_DICTIONARY, msg);
			response.set( MessageField::FIELD_ERROR, error);
			response.set( MessageField::FIELD_REASON, "");
			response.set( MessageField::FIELD_FATAL, PythonConstants::False);
		}
		DEBUG("Updating dictionary done");
	}
	else
	{
		LOG_ERROR("Unsupported update for regular dictionaries");
	}
	//Py_XDECREF(dict);
}
