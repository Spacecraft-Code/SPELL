// ################################################################################
// FILE       : SPELLserverCif.C
// DATE       : Mar 17, 2011
// PROJECT    : SPELL
// DESCRIPTION: Implementation of the CIF for server environment
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
#include "SPELL_CIFS/SPELLserverCif.H"
#include "SPELL_CIFS/SPELLcifPromptHelper.H"
// Project includes --------------------------------------------------------
#include "SPELL_CIF/SPELLcifHelper.H"
#include "SPELL_EXC/SPELLexecutor.H"
#include "SPELL_EXC/SPELLexecutorUtils.H"
#include "SPELL_EXC/SPELLcommand.H"
#include "SPELL_IPC/SPELLipcError.H"
#include "SPELL_IPC/SPELLipcMessage.H"
#include "SPELL_WRP/SPELLconstants.H"
#include "SPELL_SYN/SPELLmonitor.H"
#include "SPELL_SYN/SPELLsyncError.H"
#include "SPELL_UTIL/SPELLtime.H"
#include "SPELL_UTIL/SPELLlog.H"
#include "SPELL_UTIL/SPELLpythonHelper.H"
#include "SPELL_UTIL/SPELLpythonMonitors.H"
#include "SPELL_IPC/SPELLtimeoutValues.H"
#include "SPELL_CFG/SPELLconfiguration.H"
#include "SPELL_IPC/SPELLipc_Context.H"
#include "SPELL_IPC/SPELLipc_Executor.H"
// System includes ---------------------------------------------------------



// DEFINES /////////////////////////////////////////////////////////////////
#define ACKNOWLEDGE_TIMEOUT_SEC 30
// GLOBALS /////////////////////////////////////////////////////////////////
SPELLserverCif* s_handle = NULL;

//=============================================================================
// CONSTRUCTOR: SPELLserverCif::SPELLserverCif
//=============================================================================
SPELLserverCif::SPELLserverCif()
    : SPELLcif(),
      SPELLipcInterfaceListener(),
      m_wrMessage( MessageId::MSG_ID_WRITE ),
      m_lnMessage( MessageId::MSG_ID_NOTIFICATION ),
      m_ntMessage( MessageId::MSG_ID_NOTIFICATION ),
      m_stMessage( MessageId::MSG_ID_NOTIFICATION ),
      m_processor(*this),
	  m_lineTimer( 500, *this )
{
    DEBUG("[CIF] Created server CIF");
    m_ifc = NULL;
    m_buffer = NULL;
    m_wovBuffer = NULL;
    m_ready = false;

    m_errorState = false;
    m_sequence = 0;
    m_sequenceStack = 0;

    getExecutorConfig().setVisible(true);
    getExecutorConfig().setAutomatic(false);
    getExecutorConfig().setBlocking(true);
    getExecutorConfig().setHeadless(false);
    getExecutorConfig().setArguments("");
    getExecutorConfig().setControlClient("");
    getExecutorConfig().setControlHost("");
    getExecutorConfig().setParentProcId("");

    m_stMessage.setType(MSG_TYPE_NOTIFY);
    m_stMessage.set( MessageField::FIELD_DATA_TYPE, MessageValue::DATA_TYPE_STATUS );

    m_ntMessage.setType(MSG_TYPE_NOTIFY);
    m_ntMessage.set( MessageField::FIELD_DATA_TYPE, MessageValue::DATA_TYPE_ITEM );

    m_wrMessage.setType(MSG_TYPE_WRITE);

    m_promptMessage = VOID_MESSAGE;

    s_handle = this;
    m_closing = false;

    m_lastStack = "";

	m_ipcTimeoutGuiRequestMsec = SPELLconfiguration::instance().commonOrDefault( "GuiRequestTimeout", IPC_GUIREQUEST_DEFAULT_TIMEOUT_MSEC );
	m_ipcTimeoutCtxRequestMsec = SPELLconfiguration::instance().commonOrDefault( "CtxRequestTimeout", IPC_CTXREQUEST_DEFAULT_TIMEOUT_MSEC );
	m_timeoutOpenProcMsec = SPELLconfiguration::instance().commonOrDefault( "OpenProcTimeout", IPC_OPENPROC_DEFAULT_TIMEOUT_MSEC );
	m_timeoutExecLoginMsec = SPELLconfiguration::instance().commonOrDefault( "ExecutorLoginTimeout", IPC_EXLOGIN_DEFAULT_TIMEOUT_MSEC );
	m_timeoutAcknowledgeSec = SPELLconfiguration::instance().commonOrDefault( "AcknowledgeTimeout", ACKNOWLEDGE_TIMEOUT_SEC );
}

//=============================================================================
// DESTRUCTOR: SPELLserverCif::~SPELLserverCif
//=============================================================================
SPELLserverCif::~SPELLserverCif()
{
    if (m_ifc != NULL)
	{
    	delete m_ifc;
    	m_ifc = NULL;
	}
    if (m_buffer != NULL)
    {
    	delete m_buffer;
    	m_buffer = NULL;
	}
    if (m_wovBuffer != NULL)
    {
    	delete m_wovBuffer;
    	m_wovBuffer = NULL;
	}
}

//=============================================================================
// METHOD: SPELLserverCif::specificSetup
//=============================================================================
void SPELLserverCif::specificSetup( const SPELLcifStartupInfo& info )
{
	//Initialize Executor Config parameters
    getExecutorConfig().setVisible(true);
    getExecutorConfig().setAutomatic(false);
    getExecutorConfig().setBlocking(true);
    getExecutorConfig().setContextName( info.contextName );

	//Prepare message
    m_wrMessage.set( MessageField::FIELD_PROC_ID, getProcId() );
    m_stMessage.set( MessageField::FIELD_PROC_ID, getProcId() );
    m_lnMessage.set( MessageField::FIELD_PROC_ID, getProcId() );
    m_ntMessage.set( MessageField::FIELD_PROC_ID, getProcId() );

    DEBUG("[CIF] Setup server CIF");
    m_ifc = new SPELLipcClientInterface( "EXEC-TO-CTX", "0.0.0.0", info.contextPort );

    m_buffer = new SPELLdisplayBuffer( getProcId(), *this );
    m_buffer->start();

    m_wovBuffer = new SPELLvariableBuffer( getProcId(), *this );
    m_wovBuffer->start();

    // Initialize message sequencer
    m_sequence = 0;
    m_sequenceStack = 0;
    m_errorState = false;

    m_ifc->initialize( this );

    DEBUG("[CIF] Connecting to context");
    m_ifc->connect();

    // Perform login
    SPELLipcMessage response = login();
    processLogin(response);

    m_ipcInterruptionNotified = false;
    m_numNotAcknowledged = 0;

    m_ready = true;
    DEBUG("[CIF] Ready to go");
}

//=============================================================================
// METHOD: SPELLserverCif::cleanup
//=============================================================================
void SPELLserverCif::specificCleanup( bool force )
{
    m_ready = false;
    DEBUG("[CIF] Cleaning server CIF");
    cancelPrompt();
    m_buffer->stop();
    m_wovBuffer->stop();
    m_buffer->join();
    m_wovBuffer->join();
    DEBUG("[CIF] Disconnecting server CIF");
    if (!force)
    {
        DEBUG("[CIF] Send logout message to context");
        logout();
    }

    DEBUG("[CIF] Disconnecting IPC");

    m_ifc->disconnect();
    // Release pending requests
    m_ipcLock.unlock();
    DEBUG("[CIF] Cleanup server CIF finished");
}


//=============================================================================
// METHOD: SPELLserverCif::timerCallback
//=============================================================================
bool SPELLserverCif::timerCallback( unsigned long usecs )
{
	return true;
}

//=============================================================================
// METHOD: SPELLserverCif::logout
//=============================================================================
void SPELLserverCif::logout()
{
    DEBUG("[CIF] Sending logout message");

    SPELLipcMessage logoutmsg( ExecutorMessages::MSG_NOTIF_EXEC_CLOSE);
    logoutmsg.setType(MSG_TYPE_ONEWAY);
    logoutmsg.setSender(getProcId());
    logoutmsg.setReceiver("CTX");
    logoutmsg.set( MessageField::FIELD_PROC_ID, getProcId());

    m_ifc->sendMessage(logoutmsg);
}

//=============================================================================
// METHOD: SPELLserverCif::login
//=============================================================================
SPELLipcMessage SPELLserverCif::login()
{
    DEBUG("[CIF] Sending login message");

    // Create the login message
    SPELLipcMessage loginmsg( ExecutorMessages::REQ_NOTIF_EXEC_OPEN);
    loginmsg.setType(MSG_TYPE_REQUEST);
    loginmsg.setSender(getProcId());
    loginmsg.setReceiver("CTX");
    loginmsg.set( MessageField::FIELD_PROC_ID, getProcId());
    loginmsg.set( MessageField::FIELD_CSP, getProcId());

    // status loaded is not correct if the procedure did not compile, but the error will be
    // notified afterwards anyhow.
    loginmsg.set( MessageField::FIELD_EXEC_STATUS, SPELLexecutorUtils::statusToString(STATUS_LOADED));
    loginmsg.set( MessageField::FIELD_ASRUN_NAME, getAsRunName() );
    loginmsg.set( MessageField::FIELD_LOG_NAME, SPELLlog::instance().getLogFile() );

    LOG_INFO("Login information:");
    LOG_INFO("Proc  : " + getProcId() );
    LOG_INFO("Status: LOADED");
    LOG_INFO("AsRun : " + getAsRunName() );
    LOG_INFO("Log   : " + SPELLlog::instance().getLogFile() );

    // Send the login message.
    // ERRORS: if there is a timeout in this request, an SPELLipcError exception will
    // be thrown and will make the executor die
    SPELLipcMessage response = m_ifc->sendRequest(loginmsg, m_timeoutExecLoginMsec);

    DEBUG("[CIF] Login done");
    return response;
}

//=============================================================================
// METHOD: SPELLserverCif::processLogin
//=============================================================================
void SPELLserverCif::processLogin( const SPELLipcMessage& loginResp )
{
    DEBUG("[CIF] Processing login response");

    std::string openMode = loginResp.get(MessageField::FIELD_OPEN_MODE);
    if (openMode != "")
    {
    	openMode = openMode.substr(1, openMode.size()-2);
        std::vector<std::string> pairs = SPELLutils::tokenize( openMode, ",");
        std::vector<std::string>::iterator it;
        for( it = pairs.begin(); it != pairs.end(); it++)
        {
            std::vector<std::string> kv = SPELLutils::tokenize( *it, ":" );
            if (kv.size()!=2) continue;
            std::string modifier = kv[0];
            std::string value = kv[1];
            SPELLutils::trim(modifier);
            SPELLutils::trim(value);
            if (modifier.find(LanguageModifiers::Automatic) != std::string::npos)
            {
                if (value == PythonConstants::True) getExecutorConfig().setAutomatic(true);
            }
            else if (modifier.find(LanguageModifiers::Visible) != std::string::npos)
            {
                if (value == PythonConstants::False) getExecutorConfig().setVisible(false);
            }
            else if (modifier.find(LanguageModifiers::Blocking) != std::string::npos)
            {
                if (value == PythonConstants::False) getExecutorConfig().setBlocking(false);
            }
        }
    }
    else
    {
    	LOG_WARN("Open mode not set, using defaults");
    }

    if (loginResp.hasField(MessageField::FIELD_GUI_CONTROL))
    {
    	std::string ctrlKey = loginResp.get(MessageField::FIELD_GUI_CONTROL);
    	if (ctrlKey == "<BACKGROUND>")
    	{
        	getExecutorConfig().setControlClient("");
        	getExecutorConfig().setControlHost("");
        	getExecutorConfig().setHeadless(true);
        	getExecutorConfig().setAutomatic(true);
    	}
    	else
    	{
            getExecutorConfig().setControlClient( ctrlKey );
            if (loginResp.hasField(MessageField::FIELD_GUI_CONTROL_HOST))
            {
                getExecutorConfig().setControlHost(loginResp.get(MessageField::FIELD_GUI_CONTROL_HOST));
            }
    	}
    }

    if (loginResp.hasField(MessageField::FIELD_CONDITION))
    {
        getExecutorConfig().setCondition( loginResp.get(MessageField::FIELD_CONDITION) );
    }

    if (loginResp.hasField(MessageField::FIELD_ARGS))
    {
        getExecutorConfig().setArguments( loginResp.get(MessageField::FIELD_ARGS) );
    }

    if (loginResp.hasField(MessageField::FIELD_PARENT_PROC))
    {
        getExecutorConfig().setParentProcId( loginResp.get(MessageField::FIELD_PARENT_PROC) );
    }

    if (loginResp.hasField(MessageField::FIELD_GROUP_ID))
    {
        getExecutorConfig().setGroupId( loginResp.get(MessageField::FIELD_GROUP_ID) );
    }

    if (loginResp.hasField(MessageField::FIELD_ORIGIN_ID))
    {
        getExecutorConfig().setOriginId( loginResp.get(MessageField::FIELD_ORIGIN_ID) );
    }

    //Other variables comming in login message
    if (loginResp.hasField(MessageField::FIELD_RUN_INTO))
    {
    	getExecutorConfig().setRunInto( loginResp.get(MessageField::FIELD_RUN_INTO) == ExecutorConstants::TRUE_VALUE );
    }

    if (loginResp.hasField(MessageField::FIELD_EXEC_DELAY))
    {
    	getExecutorConfig().setExecDelay( STRI(loginResp.get(MessageField::FIELD_EXEC_DELAY)) );
    }

    if (loginResp.hasField(MessageField::FIELD_PROMPT_DELAY))
    {
    	getExecutorConfig().setPromptWarningDelay( STRI(loginResp.get(MessageField::FIELD_PROMPT_DELAY)) );
    }

    if (loginResp.hasField(MessageField::FIELD_BY_STEP))
    {
    	getExecutorConfig().setByStep( loginResp.get(MessageField::FIELD_BY_STEP) == ExecutorConstants::TRUE_VALUE );
    }

    if (loginResp.hasField(MessageField::FIELD_FORCE_TC_CONFIRM))
    {
    	getExecutorConfig().setForceTcConfirm( loginResp.get(MessageField::FIELD_FORCE_TC_CONFIRM)  == ExecutorConstants::TRUE_VALUE );
    }

    if (loginResp.hasField(MessageField::FIELD_SAVE_STATE_MODE))
    {
    	getExecutorConfig().setSaveStateMode( loginResp.get(MessageField::FIELD_SAVE_STATE_MODE) );
    }

    if (loginResp.hasField(MessageField::FIELD_WATCH_VARIABLES))
    {
    	getExecutorConfig().setWatchEnabled( loginResp.get(MessageField::FIELD_WATCH_VARIABLES) == ExecutorConstants::TRUE_VALUE );
    }

    if (loginResp.hasField(MessageField::FIELD_MAX_VERBOSITY))
    {
    	getExecutorConfig().setMaxVerbosity( STRI(loginResp.get(MessageField::FIELD_MAX_VERBOSITY)) );
    }

    if (loginResp.hasField(MessageField::FIELD_BROWSABLE_LIB))
    {
    	getExecutorConfig().setBrowsableLibStr( loginResp.get(MessageField::FIELD_BROWSABLE_LIB) );
    }

    LOG_INFO("Parent proc   : " + getExecutorConfig().getParentProcId());
    LOG_INFO("Headless mode : " + (getExecutorConfig().isHeadless() ? STR("yes") : STR("no")));
    LOG_INFO("Automatic mode: " + (getExecutorConfig().isAutomatic() ? STR("yes") : STR("no")));
    LOG_INFO("Blocking mode : " + (getExecutorConfig().isBlocking() ? STR("yes") : STR("no")));
    LOG_INFO("Visible mode  : " + (getExecutorConfig().isVisible() ? STR("yes") : STR("no")));
    LOG_INFO("Arguments     : " + getExecutorConfig().getArguments());
    LOG_INFO("Browsable lib : " + getExecutorConfig().getBrowsableLibStr() );
    LOG_INFO("Condition     : " + getExecutorConfig().getCondition());
    LOG_INFO("Control client: " + getExecutorConfig().getControlClient());
    LOG_INFO("Control host  : " + getExecutorConfig().getControlHost());
    LOG_INFO("Run Into      : " + BSTR(getExecutorConfig().isRunInto()));
    LOG_INFO("Exec Delay    : " + ISTR(getExecutorConfig().getExecDelay()));
    LOG_INFO("Prompt Delay  : " + ISTR(getExecutorConfig().getPromptWarningDelay()));
    LOG_INFO("By Step       : " + BSTR(getExecutorConfig().isByStep()));
    LOG_INFO("Force Tc Conf : " + BSTR(getExecutorConfig().isForceTcConfirm()));
    LOG_INFO("Save State Mod: " + getExecutorConfig().getSaveStateMode());
    LOG_INFO("WatchVariables: " + BSTR(getExecutorConfig().isWatchEnabled()));
    LOG_INFO("Max Verbosity : " + ISTR(getExecutorConfig().getMaxVerbosity()));

}

//=============================================================================
// METHOD: SPELLserverCif::sendGUIRequest
//=============================================================================
SPELLipcMessage SPELLserverCif::sendGUIRequest( const SPELLipcMessage& msg, unsigned long timeoutMsec )
{
    static std::string procId = getProcId();
    SPELLipcMessage response = VOID_MESSAGE;
	SPELLmonitor m(m_ipcLock);
    if (m_ready)
    {
        //DEBUG("[CIF] Sending GUI request: " + msg.dataStr());
        SPELLipcMessage toSend(msg);
        toSend.setSender(procId);
        toSend.setReceiver("GUI");
        try
        {
        	//DEBUG("[CIF] Request timeout is " + ISTR(timeoutMsec) + " ms");
        	//RACC 15-MAY SPELLsafeThreadOperations ops;
        	response = m_ifc->sendRequest(toSend, timeoutMsec);

        	if (!response.isVoid())
        	{
        		if ((response.getType() == MSG_TYPE_ERROR)&&( response.getId() == MessageId::MSG_PEER_LOST ))
        		{
        			LOG_ERROR("Unable to communicate with GUI: " + msg.getId());
        		}
        	}
        }
        catch(SPELLipcError& ex)
        {
        	LOG_ERROR("Unable to communicate with GUI: " + std::string(ex.what()));
        }
    }
    else
    {
    	LOG_ERROR("GUI request not sent: not ready");
    }
    return response;
}

//=============================================================================
// METHOD: SPELLserverCif::sendGUIMessage
//=============================================================================
void SPELLserverCif::sendGUIMessage( const SPELLipcMessage& msg )
{
    static std::string procId = getProcId();
    if (m_ready)
    {
    	try
    	{
			//DEBUG("[CIF] Sending GUI message");
			SPELLipcMessage toSend(msg);
			toSend.setSender(procId);
			toSend.setReceiver("GUI");
			m_ifc->sendMessage(toSend);
    	}
        catch(SPELLipcError& ex)
        {
        	LOG_ERROR("Unable to communicate with Context: " + std::string(ex.what()));
        	SPELLexecutor::instance().pause();
        }
    }
}

//=============================================================================
// METHOD: SPELLserverCif::sendCTXRequest
//=============================================================================
SPELLipcMessage SPELLserverCif::sendCTXRequest( const SPELLipcMessage& msg, unsigned long timeoutMsec )
{
    static std::string procId = getProcId();
    SPELLipcMessage response = VOID_MESSAGE;
	SPELLmonitor m(m_ipcLock);
    if (m_ready)
    {
        try
        {
			DEBUG("[CIF] Sending CTX request: " + msg.dataStr());
			SPELLipcMessage toSend(msg);
			toSend.setSender(procId);
			toSend.setReceiver("CTX");
			response = m_ifc->sendRequest(toSend,timeoutMsec);
			DEBUG("[CIF] Got CTX response: " + response.dataStr());
        	if ((response.getType() == MSG_TYPE_ERROR)&&( response.getId() == MessageId::MSG_PEER_LOST ))
        	{
            	LOG_ERROR("Unable to communicate with Context: " + msg.getId());
        	}
        }
        catch(SPELLipcError& ex)
        {
        	LOG_ERROR("Unable to communicate with Context: " + std::string(ex.what()));
        	if (SPELLexecutor::instance().getStatus() != STATUS_PAUSED)
        	{
        		SPELLexecutor::instance().pause();
        	}
        }
    }
    return response;
}

//=============================================================================
// METHOD: SPELLserverCif::sendCTXMessage
//=============================================================================
void SPELLserverCif::sendCTXMessage( const SPELLipcMessage& msg )
{
    static std::string procId = getProcId();
    if (m_ready)
    {
        //DEBUG("[CIF] Sending CTX message");
        SPELLipcMessage toSend(msg);
        toSend.setSender(procId);
        toSend.setReceiver("CTX");
        m_ifc->sendMessage(toSend);
    }
}

//=============================================================================
// METHOD: SPELLserverCif::completeMessage
//=============================================================================
void SPELLserverCif::completeMessage( SPELLipcMessage& msg )
{
    msg.set(MessageField::FIELD_TIME, SPELLutils::timestamp() );
	msg.set(MessageField::FIELD_MSG_SEQUENCE, ISTR(m_sequence));
	m_sequence++;

	std::string stack = getAvailableStack();

    msg.set(MessageField::FIELD_CSP, stack + "/" + ISTR(getNumExecutions()) );

    if (isManual())
    {
    	msg.set(MessageField::FIELD_EXECUTION_MODE, MessageValue::DATA_EXEC_MODE_MANUAL);
    }
    else
    {
    	msg.set(MessageField::FIELD_EXECUTION_MODE, MessageValue::DATA_EXEC_MODE_PROCEDURE);
    }
}


//=============================================================================
// METHOD: SPELLserverCif::completeMessage
//=============================================================================
void SPELLserverCif::prepareMessage( SPELLipcMessage& msg,  const std::string dataType, SPELLipcMessageType_ msgType )
{
	msg.set( MessageField::FIELD_DATA_TYPE, dataType );
	msg.setType( msgType );
	completeMessage( msg );
	msg.set( MessageField::FIELD_MSG_SEQUENCE, ISTR( m_sequenceStack ));
	m_sequenceStack++;
	msg.set( MessageField::FIELD_CODE_NAME, getCodeName() );
} //void SPELLserverCif::prepareMessage( SPELLipcMessage& msg,  const std::string dataType, SPELLipcMessageType_ msgType )

//=============================================================================
// METHOD: SPELLserverCif::specificNnotifyLine
//=============================================================================
void SPELLserverCif::specificNotifyLine()
{
	// Local variables
	std::string stage = "";

	// prepares the message to be sent to GUI
	prepareMessage( m_lnMessage, MessageValue::DATA_TYPE_LINE, MSG_TYPE_NOTIFY_ASYNC);

	// Update message with stage info
    stage = getStage();

    if (stage.find(":") != std::string::npos)
    {
        std::vector<std::string> stage_title = SPELLutils::tokenize(stage,":");
        if (stage_title.size()==2)
        {
        	m_lnMessage.set(MessageField::FIELD_STAGE_ID,stage_title[0]);
        	m_lnMessage.set(MessageField::FIELD_STAGE_TL,stage_title[1]);
        }
        else
        {
            m_lnMessage.set(MessageField::FIELD_STAGE_ID,"(none)");
            m_lnMessage.set(MessageField::FIELD_STAGE_TL,"(none)");
        }
    }
    else
    {
        m_lnMessage.set(MessageField::FIELD_STAGE_ID,stage);
        m_lnMessage.set(MessageField::FIELD_STAGE_TL,stage);
    }

	sendGUIMessage(m_lnMessage);

} //SPELLserverCif::specificNotifyLine()


//=============================================================================
// METHOD: SPELLserverCif::specificNotifyCall
//=============================================================================
void SPELLserverCif::specificNotifyCall()
{
	// prepares the message to be sent to GUI
	prepareMessage( m_lnMessage, MessageValue::DATA_TYPE_CALL, MSG_TYPE_NOTIFY);

	sendGUIMessage(m_lnMessage);
    waitAcknowledge(m_lnMessage);
} //void SPELLserverCif::specificNotifyCall()


//=============================================================================
// METHOD: SPELLserverCif::notifyReturn
//=============================================================================
void SPELLserverCif::specificNotifyReturn()
{
	// prepares the message to be sent to GUI
	prepareMessage( m_lnMessage, MessageValue::DATA_TYPE_RETURN, MSG_TYPE_NOTIFY);

    sendGUIMessage(m_lnMessage);
    waitAcknowledge(m_lnMessage);
}

//=============================================================================
// METHOD: SPELLserverCif::notifyStatus
//=============================================================================
void SPELLserverCif::specificNotifyStatus( const SPELLstatusInfo& st )
{
 	// Prepare message
	m_stMessage.set(MessageField::FIELD_EXEC_STATUS, SPELLexecutorUtils::statusToString(st.status));
    completeMessage( m_stMessage );

    // Set message Condition information
    if (st.condition.size()>0)
    {
        m_stMessage.set( MessageField::FIELD_CONDITION, st.condition );
    }

    // Set message Action information
    if (st.actionLabel != "")
    {
    	m_stMessage.set( MessageField::FIELD_ACTION_LABEL, st.actionLabel );
    	m_stMessage.set( MessageField::FIELD_ACTION_ENABLED, st.actionEnabled ? MessageValue::DATA_TRUE : MessageValue::DATA_FALSE );
    }

    sendGUIMessage(m_stMessage);
    waitAcknowledge(m_stMessage);
}

//=============================================================================
// METHOD: SPELLserverCif::specificNotifyUserActionSet
//=============================================================================
void SPELLserverCif::specificNotifyUserActionSet( const std::string& label, const unsigned int severity )
{
    SPELLipcMessage actionMessage(MessageId::MSG_ID_SET_UACTION);
    actionMessage.setType(MSG_TYPE_ONEWAY);
    actionMessage.set( MessageField::FIELD_PROC_ID, getProcId() );
    actionMessage.set( MessageField::FIELD_ACTION_LABEL, label );
    switch(severity)
    {
    case LanguageConstants::INFORMATION:
		actionMessage.set( MessageField::FIELD_ACTION_SEVERITY, MessageValue::DATA_SEVERITY_INFO );
		break;
    case LanguageConstants::WARNING:
		actionMessage.set( MessageField::FIELD_ACTION_SEVERITY, MessageValue::DATA_SEVERITY_WARN );
		break;
    case LanguageConstants::ERROR:
		actionMessage.set( MessageField::FIELD_ACTION_SEVERITY, MessageValue::DATA_SEVERITY_ERROR );
		break;
    default:
		actionMessage.set( MessageField::FIELD_ACTION_SEVERITY, MessageValue::DATA_SEVERITY_INFO );
		break;
    }
    sendGUIMessage(actionMessage);
}

//=============================================================================
// METHOD: SPELLserverCif::specificNotifyUserActionUnset
//=============================================================================
void SPELLserverCif::specificNotifyUserActionUnset()
{
    SPELLipcMessage actionMessage(MessageId::MSG_ID_DISMISS_UACTION);
    actionMessage.setType(MSG_TYPE_ONEWAY);
    actionMessage.set( MessageField::FIELD_PROC_ID, getProcId() );
    sendGUIMessage(actionMessage);
}

//=============================================================================
// METHOD: SPELLserverCif::specificNotifyUserActionEnable
//=============================================================================
void SPELLserverCif::specificNotifyUserActionEnable( bool enable )
{
    if (enable)
    {
        SPELLipcMessage actionMessage(MessageId::MSG_ID_ENABLE_UACTION);
        actionMessage.setType(MSG_TYPE_ONEWAY);
        actionMessage.set( MessageField::FIELD_PROC_ID, getProcId() );
        sendGUIMessage(actionMessage);
    }
    else
    {
        SPELLipcMessage actionMessage(MessageId::MSG_ID_DISABLE_UACTION);
        actionMessage.setType(MSG_TYPE_ONEWAY);
        actionMessage.set( MessageField::FIELD_PROC_ID, getProcId() );
        sendGUIMessage(actionMessage);
    }
}

//=============================================================================
// METHOD: SPELLserverCif::specificNotify
//=============================================================================
void SPELLserverCif::specificNotify( ItemNotification notification )
{
	//Prepare message
    completeMessage( m_ntMessage );

    //DEBUG("[CIF] Processing status");

    // Update message
    std::stringstream buffer;
    buffer << notification.getSuccessfulCount();

    m_ntMessage.set(MessageField::FIELD_NOTIF_ITEM_TYPE, NOTIF_TYPE_STR[notification.type]);
    m_ntMessage.set(MessageField::FIELD_NOTIF_ITEM_NAME, notification.name);
    m_ntMessage.set(MessageField::FIELD_NOTIF_ITEM_VALUE, notification.value);
    m_ntMessage.set(MessageField::FIELD_NOTIF_ITEM_STATUS, notification.status);
    m_ntMessage.set(MessageField::FIELD_NOTIF_ITEM_REASON, notification.getTokenizedComment());
    m_ntMessage.set(MessageField::FIELD_NOTIF_ITEM_TIME, notification.getTokenizedTime());
    m_ntMessage.set(MessageField::FIELD_NOTIF_ITEM_SCOUNT, buffer.str());

    //DEBUG("[CIF] Message prepared, sending");

    sendGUIMessage(m_ntMessage);
    waitAcknowledge(m_ntMessage);
    //DEBUG("[CIF] Notification sent");
}

//=============================================================================
// METHOD: SPELLserverCif::waitAcknowledge()
//=============================================================================
void SPELLserverCif::waitAcknowledge( const SPELLipcMessage& msg )
{
    if (m_ready && !m_ipcInterruptionNotified)
    {
    	bool found = false;
    	std::string seq = msg.get(MessageField::FIELD_MSG_SEQUENCE);
    	SPELLtime waitStart;
    	while (!found)
    	{
    		std::vector<std::string>::iterator it;
    		std::map<std::string,SPELLtime>::iterator mit;
    		{
        		SPELLmonitor m(m_ackLock);

        		it = std::find(m_ackSequences.begin(), m_ackSequences.end(), seq);

        		found = it != m_ackSequences.end();
        		if (found)
        		{
        			m_ackSequences.erase(it);
        		}
        		else
        		{
        			SPELLtime now;
					SPELLtime delta = now - waitStart;
					if (delta.getSeconds()>m_timeoutAcknowledgeSec)
					{
						LOG_WARN("#########################");
						LOG_WARN("Acknowledge not received!");
						LOG_WARN(msg.dataStr());
						LOG_WARN("#########################");
						m_numNotAcknowledged++;

						if (m_numNotAcknowledged==5)
						{
							m_numNotAcknowledged = 0;
							if (!m_ipcInterruptionNotified)
							{
								m_ipcInterruptionNotified = true;
								SPELLexecutorStatus st = SPELLexecutor::instance().getStatus();
								if ( st == STATUS_RUNNING )
								{
									ExecutorCommand cmd;
									cmd.id = CMD_PAUSE;
									warning("Communication with controlling GUI interrupted. Procedure paused for safety.", LanguageConstants::SCOPE_SYS);
									warning("You may try to resume execution.", LanguageConstants::SCOPE_SYS);
									SPELLexecutor::instance().command(cmd,false);
								}
								else if ( st == STATUS_WAITING )
								{
									ExecutorCommand cmd;
									cmd.id = CMD_INTERRUPT;
									warning("Communication with controlling GUI interrupted. Procedure interruptedfor safety.", LanguageConstants::SCOPE_SYS);
									warning("You may try to resume execution.", LanguageConstants::SCOPE_SYS);
									SPELLexecutor::instance().command(cmd,false);
								}
							}
						}
						return;
					}
        		}
    		}
    		if (!m_ready) return;
    		usleep(52000);
    	}
    }
}

//=============================================================================
// METHOD: SPELLserverCif::specificNotifyError
//=============================================================================
void SPELLserverCif::specificNotifyError( const std::string& error, const std::string& reason, bool fatal )
{
    //LOG_ERROR("[CIF] Error notification: " + error + " (" + reason + ")");

	// prepare error message
    SPELLipcMessage errorMsg( MessageId::MSG_ID_ERROR);
    errorMsg.setType(MSG_TYPE_ERROR);
    errorMsg.set( MessageField::FIELD_PROC_ID, getProcId() );
    errorMsg.set( MessageField::FIELD_DATA_TYPE, MessageValue::DATA_TYPE_STATUS );
    errorMsg.set( MessageField::FIELD_EXEC_STATUS, SPELLexecutorUtils::statusToString(STATUS_ERROR) );
    errorMsg.set( MessageField::FIELD_ERROR, error );
    errorMsg.set( MessageField::FIELD_REASON, reason );

    if (fatal)
    {
        errorMsg.set( MessageField::FIELD_FATAL, PythonConstants::True );
    }
    else
    {
        errorMsg.set( MessageField::FIELD_FATAL, PythonConstants::False );
    }

    completeMessage( errorMsg );
    sendGUIMessage(errorMsg);

} //void SPELLserverCif::specificNotifyError( const std::string& error, const std::string& reason, bool fatal )

//=============================================================================
// METHOD: SPELLserverCif::specificWrite
//=============================================================================
void SPELLserverCif::specificWrite( const std::string& msg, unsigned int scope )
{
    if (isManual())
    {
        completeMessage( m_wrMessage );
        std::string timeStr = SPELLutils::timestampUsec();
        m_wrMessage.set(MessageField::FIELD_TEXT,msg);
        m_wrMessage.set(MessageField::FIELD_LEVEL,MessageValue::DATA_SEVERITY_INFO);
        m_wrMessage.set(MessageField::FIELD_MSGTYPE,LanguageConstants::DISPLAY);
        m_wrMessage.set(MessageField::FIELD_TIME, timeStr);
        m_wrMessage.set(MessageField::FIELD_SCOPE, ISTR(scope));
        sendGUIMessage(m_wrMessage);
    }
    else
    {
    	m_buffer->write( msg, scope );
    }
}

//=============================================================================
// METHOD: SPELLserverCif::specificWarning
//=============================================================================
void SPELLserverCif::specificWarning( const std::string& msg, unsigned int scope )
{

    // We shall not bufferize in manual mode
    if (isManual())
    {
        completeMessage( m_wrMessage );
        std::string timeStr = SPELLutils::timestampUsec();
        m_wrMessage.set(MessageField::FIELD_TEXT,msg);
        m_wrMessage.set(MessageField::FIELD_LEVEL,MessageValue::DATA_SEVERITY_WARN);
        m_wrMessage.set(MessageField::FIELD_MSGTYPE,LanguageConstants::DISPLAY);
        m_wrMessage.set(MessageField::FIELD_TIME, timeStr);
        m_wrMessage.set(MessageField::FIELD_SCOPE, ISTR(scope));
        sendGUIMessage(m_wrMessage);
    }
    else
    {
    	m_buffer->warning( msg, scope );
    }

}

//=============================================================================
// METHOD: SPELLserverCif::specificError
//=============================================================================
void SPELLserverCif::specificError( const std::string& msg, unsigned int scope )
{

    // We shall not bufferize in manual mode
    if (isManual())
    {
        completeMessage( m_wrMessage );
        std::string timeStr = SPELLutils::timestampUsec();
        m_wrMessage.set(MessageField::FIELD_TEXT,msg);
        m_wrMessage.set(MessageField::FIELD_LEVEL,MessageValue::DATA_SEVERITY_ERROR);
        m_wrMessage.set(MessageField::FIELD_MSGTYPE,LanguageConstants::DISPLAY);
        m_wrMessage.set(MessageField::FIELD_TIME, timeStr);
        m_wrMessage.set(MessageField::FIELD_SCOPE, ISTR(scope));
        sendGUIMessage(m_wrMessage);
    }
    else
    {
    	m_buffer->error( msg, scope );
    }

} //void SPELLserverCif::specificError( const std::string& msg, unsigned int scope )

//=============================================================================
// METHOD: SPELLserverCif::specificNotifyVariableChange()
//=============================================================================
void SPELLserverCif::specificNotifyVariableChange( const std::vector<SPELLvarInfo>& added,
										   const std::vector<SPELLvarInfo>& changed,
		                                   const std::vector<SPELLvarInfo>& deleted )
{
	m_wovBuffer->variableChange(added,changed,deleted);
}

//=============================================================================
// METHOD: SPELLserverCif::specificNotifyVariableScopeChange()
//=============================================================================
void SPELLserverCif::specificNotifyVariableScopeChange( const std::string& scopeName,
		                                        const std::vector<SPELLvarInfo>& globals,
		                                        const std::vector<SPELLvarInfo>& locals )
{
	SPELLipcMessage notifyMsg(ExecutorMessages::MSG_SCOPE_CHANGE);
	notifyMsg.setType(MSG_TYPE_ONEWAY);

	std::string names = "";
	std::string types = "";
	std::string values = "";
	std::string globalFlags = "";

	for( unsigned int index = 0; index<globals.size(); index++)
	{
		if (names != "")
		{
			names += VARIABLE_SEPARATOR;
			types += VARIABLE_SEPARATOR;
			values += VARIABLE_SEPARATOR;
			globalFlags += VARIABLE_SEPARATOR;
		}
		names += globals[index].varName;
		types += globals[index].varType;
		values += globals[index].varValue;
		globalFlags += "True";
	}


	for( unsigned int index = 0; index<locals.size(); index++)
	{
		if (names != "")
		{
			names += VARIABLE_SEPARATOR;
			types += VARIABLE_SEPARATOR;
			values += VARIABLE_SEPARATOR;
			globalFlags += VARIABLE_SEPARATOR;
		}
		names += locals[index].varName;
		types += locals[index].varType;
		values += locals[index].varValue;
		globalFlags += "False";
	}

    notifyMsg.set( MessageField::FIELD_PROC_ID, getProcId() );
	notifyMsg.set( MessageField::FIELD_VARIABLE_SCOPE,  scopeName );
	notifyMsg.set( MessageField::FIELD_VARIABLE_NAME,   names );
	notifyMsg.set( MessageField::FIELD_VARIABLE_TYPE,   types );
	notifyMsg.set( MessageField::FIELD_VARIABLE_VALUE,  values );
	notifyMsg.set( MessageField::FIELD_VARIABLE_GLOBAL, globalFlags );

	m_wovBuffer->scopeChange();

	sendGUIMessage(notifyMsg);
}

//=============================================================================
// METHOD: SPELLserverCif::specificPrompt
//=============================================================================
void SPELLserverCif::specificPrompt( const SPELLpromptDefinition& def, std::string& rawAnswer, std::string& answerToShow )
{
    m_promptDef = def;

    std::string timeStr = SPELLutils::timestampUsec();

    m_promptMessage = SPELLipcMessage(MessageId::MSG_ID_PROMPT);

    completeMessage( m_promptMessage );

    m_promptMessage.setType(MSG_TYPE_PROMPT);
    m_promptMessage.set(MessageField::FIELD_PROC_ID, getProcId() );
    m_promptMessage.set(MessageField::FIELD_TEXT, def.message);
    m_promptMessage.set(MessageField::FIELD_DATA_TYPE, ISTR(def.typecode));
    m_promptMessage.set(MessageField::FIELD_TIME, timeStr);
    m_promptMessage.set(MessageField::FIELD_DEFAULT, def.defaultAnswer);
    m_promptMessage.set(MessageField::FIELD_SCOPE, ISTR(def.scope));

    DEBUG("[CIF] Prompt typecode " + ISTR(def.typecode));

    // the prompt display
    std::string msgToShow = def.message;

    if ( def.options.size() == 0 )
    {
        DEBUG("[CIF] Prompt is simple");
        m_promptMessage.set(MessageField::FIELD_EXPECTED, "");
    }
    else
    {
    	SPELLcifPromptHelper helper;

    	msgToShow = helper.completeOptions( def, m_promptMessage, msgToShow );
    }
    DEBUG("[CIF] Option string: " + m_promptMessage.get(MessageField::FIELD_OPTIONS));
    DEBUG("[CIF] Expected string: " + m_promptMessage.get(MessageField::FIELD_EXPECTED));

    // Send the display message via the buffer to ensure synchronization
    m_buffer->prompt(msgToShow, LanguageConstants::SCOPE_PROMPT);

    // Ensure buffer is flushed
    m_buffer->flush();

    DEBUG("[CIF] Messsage prepared");

    // Start prompt status and send request to client
    startPrompt();
    waitPromptAnswer();

    DEBUG("[CIF] Prompt response received");

    // Process prompt response
    std::string toProcess = "";
    if (m_promptAnswer.getId() == MessageId::MSG_ID_CANCEL)
    {
    	LOG_WARN("Prompt cancelled");
        DEBUG("[CIF] Prompt cancelled");
        // Abort execution in this case
        SPELLexecutor::instance().abort("Prompt cancelled",true);
        answerToShow = PROMPT_CANCELLED;
        return;
    }
    else if (m_promptAnswer.getId() == MessageId::MSG_ID_TIMEOUT)
    {
    	LOG_ERROR("Prompt timed out");
        DEBUG("[CIF] Prompt timed out");
        // Abort execution in this case
        SPELLexecutor::instance().abort("Prompt timed out", true);
        answerToShow = PROMPT_TIMEOUT;
        return;
    }
    else if (m_promptAnswer.getType() == MSG_TYPE_ERROR)
    {
    	std::string errorMsg = m_promptAnswer.get( MessageField::FIELD_ERROR );
        DEBUG("[CIF] Prompt error: " + errorMsg );
    	LOG_ERROR("Prompt error: " + errorMsg);
        // \todo Should fix this and use an error code
        if (errorMsg == "No controlling client")
        {
        	warning("No controlling client to issue prompt!", LanguageConstants::SCOPE_SYS );
        	SPELLexecutor::instance().pause();
        }
        else
        {
        	error( "Prompt error: " + errorMsg, LanguageConstants::SCOPE_SYS  );
        	// Abort execution in this case
        	SPELLexecutor::instance().abort("Prompt error",true);
        }
        answerToShow = PROMPT_ERROR;
        return;
    }
    else
    {
		toProcess = m_promptAnswer.get(MessageField::FIELD_RVALUE);
	}

	DEBUG("[CIF] Prompt response: " + toProcess);

	std::string toShow = SPELLcifHelper::getResult( toProcess, def );

	DEBUG("[CIF] Translated prompt response: " + toProcess);

    // \todo When there is no controlling client we should keep the child procedure in prompt waiting state

    // Send the display message via the buffer to ensure synchronization
    m_buffer->write("Answer: '" + toShow + "'", LanguageConstants::SCOPE_PROMPT);
    m_buffer->flush();

    rawAnswer = toProcess;
    answerToShow = toShow;
}

//=============================================================================
// METHOD: SPELLserverCif::specificOpenSubprocedure
//=============================================================================
std::string SPELLserverCif::specificOpenSubprocedure( const std::string& procId, int callingLine, const std::string& args, bool automatic, bool blocking, bool visible )
{
    std::string openModeStr = "{";
    DEBUG("[CIF] Open subprocedure options: " + BSTR(automatic) + "," + BSTR(blocking) + "," + BSTR(visible));
    openModeStr += (automatic ? (LanguageModifiers::Automatic + ":" + PythonConstants::True) : (LanguageModifiers::Automatic + ":" + PythonConstants::False)) + ",";
    openModeStr += (blocking ? (LanguageModifiers::Blocking + ":" + PythonConstants::True) : (LanguageModifiers::Blocking+ ":" + PythonConstants::False)) + ",";
    openModeStr += (visible ? (LanguageModifiers::Visible + ":" + PythonConstants::True) : (LanguageModifiers::Visible + ":" + PythonConstants::False)) + "}";

    std::string parent = getProcId();

    // Request first an available instance number
    SPELLipcMessage instanceMsg(ContextMessages::REQ_INSTANCE_ID);
    instanceMsg.setType(MSG_TYPE_REQUEST);
    instanceMsg.set(MessageField::FIELD_PROC_ID, procId);

    SPELLipcMessage response = sendCTXRequest( instanceMsg, m_ipcTimeoutCtxRequestMsec );

    std::string subprocId = "";

    subprocId = response.get(MessageField::FIELD_INSTANCE_ID);

    DEBUG("[CIF] Request context to open subprocedure " + subprocId + " in mode " + openModeStr);

    SPELLipcMessage openMsg(ContextMessages::REQ_OPEN_EXEC);
    openMsg.setType(MSG_TYPE_REQUEST);
    openMsg.set(MessageField::FIELD_PROC_ID, parent);
    openMsg.set(MessageField::FIELD_PARENT_PROC_LINE, ISTR(callingLine));
    openMsg.set(MessageField::FIELD_SPROC_ID, subprocId);

    // We need to add a group identifier to the request, so that this same id is associated to the new
    // child procedure. This way all procedures on a StartProc-dependency-tree will be related to
    // each other via the same group id. If the current group ID is empty it is because this is
    // a main procedure and therefore we will take our own procId as the group id.
    std::string groupId = getExecutorConfig().getGroupId();
    openMsg.set(MessageField::FIELD_GROUP_ID, groupId );

    // The origin identifier is optional and just informative. It shall be copied as well from parents to
    // children.
    openMsg.set(MessageField::FIELD_ORIGIN_ID, getExecutorConfig().getOriginId() );
    openMsg.set(MessageField::FIELD_OPEN_MODE, openModeStr);
    openMsg.set(MessageField::FIELD_ARGS, args);

    // We need to set the proper client mode so that a background procedure spawns also background procedures
    if (getExecutorConfig().isHeadless())
    {
    	openMsg.set(MessageField::FIELD_GUI_MODE, MessageValue::DATA_GUI_MODE_B);
    }
    else
    {
    	openMsg.set(MessageField::FIELD_GUI_MODE, MessageValue::DATA_GUI_MODE_C);
    }

    response = sendCTXRequest( openMsg, m_timeoutOpenProcMsec );

    DEBUG("[CIF] Request context to provide child procedure information");

    // Request first an available instance number
    SPELLipcMessage infoMsg(ContextMessages::REQ_EXEC_INFO);
    infoMsg.setType(MSG_TYPE_REQUEST);
    infoMsg.setSender(getProcId());
    infoMsg.setReceiver("CTX");
    infoMsg.set(MessageField::FIELD_PROC_ID, subprocId);

    SPELLipcMessage execInfo = sendCTXRequest( infoMsg, m_ipcTimeoutCtxRequestMsec );

    std::string childAsRun = execInfo.get(MessageField::FIELD_ASRUN_NAME);
    DEBUG("[CIF] Child ASRUN: " + childAsRun);

    m_asRun->writeChildProc( getStack(), childAsRun, args, openModeStr );

    return subprocId;
}

//=============================================================================
// METHOD: SPELLserverCif::specificCloseSubprocedure
//=============================================================================
void SPELLserverCif::specificCloseSubprocedure( const std::string& procId )
{
    SPELLipcMessage closeMsg(ContextMessages::REQ_CLOSE_EXEC);
    closeMsg.setType(MSG_TYPE_REQUEST);
    closeMsg.set(MessageField::FIELD_SPROC_ID, procId);

    sendCTXRequest( closeMsg, m_ipcTimeoutCtxRequestMsec );
}

//=============================================================================
// METHOD: SPELLserverCif::specificKillSubprocedure
//=============================================================================
void SPELLserverCif::specificKillSubprocedure( const std::string& procId )
{
    SPELLipcMessage killMsg(ContextMessages::REQ_KILL_EXEC);
    killMsg.setType(MSG_TYPE_REQUEST);
    killMsg.set(MessageField::FIELD_SPROC_ID, procId);

    sendCTXRequest( killMsg, m_ipcTimeoutCtxRequestMsec );
}

//=============================================================================
// METHOD: SPELLserverCif::processMessage
//=============================================================================
void SPELLserverCif::processMessage( const SPELLipcMessage& msg )
{
    std::string msgId = msg.getId();
    std::string procId = msg.get(MessageField::FIELD_PROC_ID);
    std::string parentProcId = "";

    if (m_ready && msgId == ExecutorMessages::ACKNOWLEDGE)
    {
    	std::string seq = msg.get(MessageField::FIELD_MSG_SEQUENCE);
    	{
    		SPELLmonitor m(m_ackLock);
    		m_ackSequences.push_back(seq);
    	}
    	return;
    }

    // Reset the IPC interruption flag so that normal
    // communication is restored
    m_ipcInterruptionNotified = false;

    // Prompt answers
    if ( msgId == MessageId::MSG_ID_PROMPT_ANSWER )
    {
    	DEBUG("[CIF] Got prompt answer");
    	m_promptAnswer = msg;
    	m_promptAnswerEvent.set();
    	return;
    }
    else if ( msgId == MessageId::MSG_ID_CANCEL )
    {
    	LOG_WARN("Prompt has been cancelled by client");
    	cancelPrompt();
    	return;
    }

    // Other messages...

    if (msg.hasField(MessageField::FIELD_PARENT_PROC))
    {
    	parentProcId = msg.get(MessageField::FIELD_PARENT_PROC);
    }
    SPELLipcMessageType type = msg.getType();

    // If the message is for a child procedure of our own
    if (parentProcId == getProcId())
    {
    	DEBUG("[CIF] Message is for child ( " + procId + " | " + getProcId() + "=" + parentProcId + ")");
        m_processor.processMessageForChild( msg );
    }
    // If it is directed to this procedure
    else if (procId == getProcId() )
    {
        switch(type)
        {
        case MSG_TYPE_ONEWAY:
            if (msgId == MessageId::MSG_ID_ADD_CLIENT)
            {
            	LOG_INFO("Add controlling client");
                if (msg.hasField(MessageField::FIELD_GUI_CONTROL))
                {
    				getExecutorConfig().setHeadless( false );
                    getExecutorConfig().setControlClient( msg.get(MessageField::FIELD_GUI_CONTROL) );
                    getExecutorConfig().setControlHost( msg.get(MessageField::FIELD_GUI_CONTROL_HOST) );
                }
            }
            else if (msgId == MessageId::MSG_ID_REMOVE_CLIENT)
            {
            	LOG_WARN("Controlling client removed");
            	switch(SPELLexecutor::instance().getStatus())
            	{
            	case STATUS_FINISHED:
            	case STATUS_PAUSED:
            	case STATUS_ABORTED:
            	case STATUS_ERROR:
            	case STATUS_PROMPT:
            	case STATUS_INTERRUPTED:
            		break;
            	default:
                    if (msg.hasField(MessageField::FIELD_GUI_CONTROL) && 
                        msg.get(MessageField::FIELD_GUI_CONTROL) == getExecutorConfig().getControlClient())
                    {
                        getExecutorConfig().setControlClient("");
                        getExecutorConfig().setControlHost("");
                    }
                    SPELLexecutor::instance().pause();
            	}
            }
            else if (msgId == MessageId::MSG_ID_BACKGROUND)
            {
            	LOG_WARN("Going to background mode");
                getExecutorConfig().setControlClient("");
                getExecutorConfig().setControlHost("");
				getExecutorConfig().setHeadless( true );
            }
            else if (msgId == MessageId::MSG_ID_NODE_DEPTH)
            {
                DEBUG("[CIF] Move stack to level");
            	unsigned int level = STRI( msg.get(MessageField::FIELD_LEVEL) );
            	SPELLexecutor::instance().getCallstack().moveToLevel(level);
            }
            else if (msgId == MessageId::MSG_ID_WVARIABLES_ENABLE)
            {
            	SPELLexecutor::instance().getVariableManager().setEnabled(true);
            }
            else if (msgId == MessageId::MSG_ID_WVARIABLES_DISABLE)
            {
            	SPELLexecutor::instance().getVariableManager().setEnabled(false);
            }
            else
            {
            	processMessageCommand(msg);
            }
            break;
        default:
            LOG_ERROR("[CIF] MESSAGE UNPROCESSED: " + msgId);
            break;
        }
    }
    else
    {
    	LOG_ERROR("[CIF] MESSAGE UNPROCESSED: " + msgId);
    }
}

//=============================================================================
// METHOD: SPELLserverCif::setClosing
//=============================================================================
void SPELLserverCif::setClosing()
{
	SPELLmonitor m(m_closeLock);
	m_closing = true;
}

//=============================================================================
// METHOD: SPELLserverCif::isClosing
//=============================================================================
bool SPELLserverCif::isClosing()
{
	SPELLmonitor m(m_closeLock);
	return m_closing;
}

//=============================================================================
// METHOD: SPELLserverCif::processMessageCommand
//=============================================================================
void SPELLserverCif::processMessageCommand( const SPELLipcMessage& msg )
{
	std::string msgId = msg.getId();
    ExecutorCommand cmd;
    bool high_priority = false;

    cmd.id = msgId;

    LOG_INFO("[CIF] Command received: " + cmd.id);
    if (msgId == ExecutorMessages::MSG_CMD_ABORT)
    {
        high_priority = true;
    }
    else if (msgId == ExecutorMessages::MSG_CMD_CLOSE)
    {
    	// Retain further outgoing requests
    	m_ipcLock.lock();
    	setClosing();
        high_priority = true;
    }
    else if (msgId == ExecutorMessages::MSG_CMD_BLOCK)
    {
    	// Retain further outgoing requests
    	m_ipcLock.lock();
    	setClosing();
        // Do not forward to executor, this is a command for the CIF
    	return;
    }
    else if (msgId == ExecutorMessages::MSG_CMD_GOTO)
    {
        if (msg.hasField(MessageField::FIELD_GOTO_LINE))
        {
            cmd.earg = "line";
            cmd.arg = msg.get(MessageField::FIELD_GOTO_LINE);
        }
        else if (msg.hasField(MessageField::FIELD_GOTO_LABEL))
        {
            cmd.earg = "label";
            cmd.arg = msg.get(MessageField::FIELD_GOTO_LABEL);
        }
    }
    else if (msgId == ExecutorMessages::MSG_CMD_SCRIPT)
    {
        cmd.arg = msg.get(MessageField::FIELD_SCRIPT);
    }
    SPELLexecutor::instance().command(cmd, high_priority);
}

//=============================================================================
// METHOD: SPELLserverCif::processRequest
//=============================================================================
SPELLipcMessage SPELLserverCif::processRequest( const SPELLipcMessage& msg )
{
    std::string requestId = msg.getId();
    std::string procId = msg.get(MessageField::FIELD_PROC_ID);
    SPELLipcMessage response( msg.getId() );
    response.setType(MSG_TYPE_RESPONSE);
    response.setReceiver( msg.getSender() );
    response.setSender( msg.getReceiver() );

	DEBUG("[CIF] Request: " + msg.getId());

    if (requestId == ExecutorMessages::REQ_GET_CONFIG)
    {
    	m_processor.processGetConfig(msg,response);
    }
    else if (requestId == ExecutorMessages::REQ_SET_CONFIG)
    {
    	m_processor.processSetConfig(msg,response);
    }
    else if (requestId == ExecutorMessages::REQ_EXEC_STATUS)
    {
    	m_processor.processGetStatus( msg,response );
    }
    else if (requestId == ExecutorMessages::REQ_SET_BREAKPOINT)
    {
        m_processor.processSetBreakpoint(msg, response);
    }
    else if (requestId == ExecutorMessages::REQ_CLEAR_BREAKPOINT)
    {
        m_processor.processClearBreakpoints(msg, response);
    }
    else if (requestId == ExecutorMessages::REQ_GET_VARIABLES)
    {
    	m_processor.processGetVariables(msg, response);
    }
    else if (requestId == ExecutorMessages::REQ_CHANGE_VARIABLE)
    {
    	m_processor.processChangeVariable(msg, response);
    }
    else if (requestId == ExecutorMessages::REQ_WVARIABLES_ENABLED)
    {
    	m_processor.processCheckVariablesEnabled(msg, response);
    }
    else if (requestId == ExecutorMessages::REQ_GET_DICTIONARY)
    {
    	m_processor.processGetDictionary(msg, response);
    }
    else if (requestId == ExecutorMessages::REQ_UPD_DICTIONARY)
    {
    	m_processor.processUpdateDictionary(msg, response);
    }
    else
    {
        LOG_ERROR("[CIF] Unprocessed request: " + requestId);
    }
    return response;
}

//=============================================================================
// METHOD: SPELLserverCif::processConnectionError
//=============================================================================
void SPELLserverCif::processConnectionError( int clientKey, std::string error, std::string reason )
{
	if (!isClosing())
	{
	    m_ready = false;
		LOG_ERROR("IPC error: " + error + ": " + reason);
		SPELLexecutor::instance().pause();
		while(!m_ready)
		{
			try
			{
				LOG_WARN("Trying to reconnect on port " + ISTR(m_ifc->getServerPort()));
				m_ifc->connect(true);

				LOG_WARN("Reconnected, login");

				// Perform login
			    SPELLipcMessage response = login();
			    processLogin(response);

			    m_ready = true;

				LOG_INFO("Successfully reconnected to context");
			}
			catch(SPELLipcError& err)
			{
				LOG_ERROR("Failed to reconnect: " + err.what());
				usleep(5000000);
			}
		}
	}
}

//=============================================================================
// METHOD: SPELLserverCif::processConnectionClosed
//=============================================================================
void SPELLserverCif::processConnectionClosed( int clientKey )
{
	if (!isClosing())
	{
		LOG_WARN("Connection closed by context");
		SPELLexecutor::instance().pause();
	}
}

//=============================================================================
// METHOD: SPELLserverCif::startPrompt
//=============================================================================
void SPELLserverCif::startPrompt()
{
    // Send notification of prompt start
    SPELLipcMessage promptStart( m_promptMessage );
    promptStart.setId( MessageId::MSG_ID_PROMPT_START );
    promptStart.setType( MSG_TYPE_ONEWAY );
    sendGUIMessage(&promptStart);

    // If we are issuing a prompt and we are in background mode, raise a warning
    if ( getExecutorConfig().getControlClient() == "" && getExecutorConfig().isHeadless() )
    {
    	warning("Procedure requires user intervention for prompt", LanguageConstants::SCOPE_SYS);
    }

    // Put the scheduler in wait
    SPELLexecutor::instance().getScheduler().startPrompt( m_promptDef.timeout, getExecutorConfig().isHeadless() );

    // Send actual prompt message
	sendGUIMessage(m_promptMessage);

	// Reset the event that will trigger the answer
	m_promptAnswerEvent.clear();
	m_promptAnswer = VOID_MESSAGE;
}

//=============================================================================
// METHOD: SPELLserverCif::cancelPrompt
//=============================================================================
void SPELLserverCif::cancelPrompt()
{
	if (!m_promptMessage.isVoid())
	{
		m_promptAnswer.setId(MessageId::MSG_ID_CANCEL);
		m_promptAnswerEvent.set();

		// Send notification of prompt end
		SPELLipcMessage promptEnd( m_promptMessage );
		promptEnd.setId( MessageId::MSG_ID_PROMPT_END );
		promptEnd.setType( MSG_TYPE_ONEWAY );
		sendGUIMessage(&promptEnd);

		// Finish the prompt state
		SPELLexecutor::instance().getScheduler().cancelPrompt();

		m_promptMessage = VOID_MESSAGE;
	}
}

//=============================================================================
// METHOD: SPELLserverCif::waitPromptAnswer
//=============================================================================
void SPELLserverCif::waitPromptAnswer()
{
	// Since this wait will block for an unknown amount of time, we need to provide
	// access to other Python threads to work in the meantime
	SPELLsafeThreadOperations ops("SPELLserverCif::waitPromptAnswer()");
	m_promptAnswerEvent.wait();

	// Send notification of prompt end
    SPELLipcMessage promptEnd( m_promptMessage );
    promptEnd.setId( MessageId::MSG_ID_PROMPT_END );
    promptEnd.setType( MSG_TYPE_ONEWAY );
    sendGUIMessage(&promptEnd);

    // Finish the prompt state
    SPELLexecutor::instance().getScheduler().finishPrompt();

    m_promptMessage = VOID_MESSAGE;
}

//=============================================================================
// METHOD: SPELLserverCif::
//=============================================================================
std::string SPELLserverCif::setSharedData( const std::string& name, const std::string& value, const std::string& expected, const std::string& scope )
{
	// Send notification of prompt end
    SPELLipcMessage setData;
    setData.setId( ContextMessages::REQ_SET_SHARED_DATA );
    setData.setType( MSG_TYPE_REQUEST );
    setData.setSender( getProcId() );
    setData.setReceiver( "CTX" );
    setData.set( MessageField::FIELD_PROC_ID, getProcId());
    setData.set( MessageField::FIELD_SHARED_VARIABLE, name);
    setData.set( MessageField::FIELD_SHARED_VALUE, value);
    setData.set( MessageField::FIELD_SHARED_EXPECTED, expected);
    setData.set( MessageField::FIELD_SHARED_SCOPE, scope);
    SPELLipcMessage response = sendCTXRequest(&setData, m_ipcTimeoutCtxRequestMsec);

    std::string result = "";

	if (response.isVoid() )
	{
		THROW_EXCEPTION("Failed to set shared data", "No response from context", SPELL_ERROR_IPC);
	}
	else if (response.getType() == MSG_TYPE_ERROR )
	{
		THROW_EXCEPTION(response.get(MessageField::FIELD_ERROR),response.get(MessageField::FIELD_REASON), SPELL_ERROR_ENVIRONMENT);
	}
	else
	{
		result = response.get(MessageField::FIELD_SHARED_SUCCESS);
	}
	return result;
}

//=============================================================================
// METHOD: SPELLserverCif::
//=============================================================================
std::string SPELLserverCif::clearSharedData( const std::string& name, const std::string& scope )
{
	// Send notification of prompt end
    SPELLipcMessage clearData;
    clearData.setId( ContextMessages::REQ_DEL_SHARED_DATA );
    clearData.setType( MSG_TYPE_REQUEST );
    clearData.setSender( getProcId() );
    clearData.setReceiver( "CTX" );
    clearData.set( MessageField::FIELD_PROC_ID, getProcId());
    clearData.set( MessageField::FIELD_SHARED_VARIABLE, name);
    clearData.set( MessageField::FIELD_SHARED_SCOPE, scope);

    std::string result = "";

    SPELLipcMessage response = sendCTXRequest(&clearData, m_ipcTimeoutCtxRequestMsec);
	if (response.isVoid() )
	{
		THROW_EXCEPTION("Failed to clear shared data", "No response from context", SPELL_ERROR_IPC);
	}
	else if (response.getType() == MSG_TYPE_ERROR )
	{
		THROW_EXCEPTION(response.get(MessageField::FIELD_ERROR),response.get(MessageField::FIELD_REASON), SPELL_ERROR_ENVIRONMENT);
	}
	else
	{
		result = response.get(MessageField::FIELD_SHARED_SUCCESS);
	}
	return result;
}

//=============================================================================
// METHOD: SPELLserverCif::
//=============================================================================
std::string SPELLserverCif::getSharedData( const std::string& name, const std::string& scope )
{
	// Send notification of prompt end
    SPELLipcMessage getData;
    getData.setId( ContextMessages::REQ_GET_SHARED_DATA );
    getData.setType( MSG_TYPE_REQUEST );
    getData.setSender( getProcId() );
    getData.setReceiver( "CTX" );
    getData.set( MessageField::FIELD_SHARED_VARIABLE, name);
    getData.set( MessageField::FIELD_SHARED_SCOPE, scope);

    SPELLipcMessage response = sendCTXRequest(&getData, m_ipcTimeoutCtxRequestMsec);
	if (response.isVoid() )
	{
		THROW_EXCEPTION("Failed to get shared data", "No response from context", SPELL_ERROR_IPC);
	}
	else if (response.getType() == MSG_TYPE_ERROR )
	{
		THROW_EXCEPTION(response.get(MessageField::FIELD_ERROR),response.get(MessageField::FIELD_REASON), SPELL_ERROR_ENVIRONMENT);
	}

	return response.get(MessageField::FIELD_SHARED_VALUE);
}

//=============================================================================
// METHOD: SPELLserverCif::
//=============================================================================
std::string SPELLserverCif::getSharedDataKeys( const std::string& scope )
{
	// Send notification of prompt end
    SPELLipcMessage getDataKeys;
    getDataKeys.setId( ContextMessages::REQ_GET_SHARED_DATA_KEYS );
    getDataKeys.setType( MSG_TYPE_REQUEST );
    getDataKeys.setSender( getProcId() );
    getDataKeys.setReceiver( "CTX" );
    getDataKeys.set( MessageField::FIELD_SHARED_SCOPE, scope);

    SPELLipcMessage response = sendCTXRequest(&getDataKeys, m_ipcTimeoutCtxRequestMsec);
	if (response.isVoid() )
	{
		THROW_EXCEPTION("Failed to get shared data variables", "No response from context", SPELL_ERROR_IPC);
	}
	else if (response.getType() == MSG_TYPE_ERROR )
	{
		THROW_EXCEPTION(response.get(MessageField::FIELD_ERROR),response.get(MessageField::FIELD_REASON), SPELL_ERROR_ENVIRONMENT);
	}

	return response.get(MessageField::FIELD_SHARED_VARIABLE);
}

//=============================================================================
// METHOD: SPELLserverCif::
//=============================================================================
std::string SPELLserverCif::getSharedDataScopes()
{
	// Send notification of prompt end
    SPELLipcMessage getDataScopes;
    getDataScopes.setId( ContextMessages::REQ_GET_SHARED_DATA_SCOPES );
    getDataScopes.setType( MSG_TYPE_REQUEST );
    getDataScopes.setSender( getProcId() );
    getDataScopes.setReceiver( "CTX" );

    SPELLipcMessage response = sendCTXRequest(&getDataScopes, m_ipcTimeoutCtxRequestMsec);
	if (response.isVoid() )
	{
		THROW_EXCEPTION("Failed to get shared data scopes", "No response from context", SPELL_ERROR_IPC);
	}
	else if (response.getType() == MSG_TYPE_ERROR )
	{
		THROW_EXCEPTION(response.get(MessageField::FIELD_ERROR),response.get(MessageField::FIELD_REASON), SPELL_ERROR_ENVIRONMENT);
	}

	return response.get(MessageField::FIELD_SHARED_SCOPE);
}

//=============================================================================
// METHOD: SPELLserverCif::
//=============================================================================
void SPELLserverCif::addSharedDataScope( const std::string& scope )
{
	// Send notification of prompt end
    SPELLipcMessage addScope;
    addScope.setId( ContextMessages::REQ_ADD_SHARED_DATA_SCOPE );
    addScope.setType( MSG_TYPE_REQUEST );
    addScope.setSender( getProcId() );
    addScope.setReceiver( "CTX" );
    addScope.set( MessageField::FIELD_PROC_ID, getProcId());
    addScope.set( MessageField::FIELD_SHARED_SCOPE, scope);

    SPELLipcMessage response = sendCTXRequest(&addScope, m_ipcTimeoutCtxRequestMsec);
	if (response.isVoid() )
	{
		THROW_EXCEPTION("Failed to add shared data scope", "No response from context", SPELL_ERROR_IPC);
	}
	else if (response.getType() == MSG_TYPE_ERROR )
	{
		THROW_EXCEPTION(response.get(MessageField::FIELD_ERROR),response.get(MessageField::FIELD_REASON), SPELL_ERROR_ENVIRONMENT);
	}
}

//=============================================================================
// METHOD: SPELLserverCif::
//=============================================================================
void SPELLserverCif::removeSharedDataScope( const std::string& scope )
{
	// Send notification of prompt end
    SPELLipcMessage removeScope;
    removeScope.setId( ContextMessages::REQ_DEL_SHARED_DATA_SCOPE );
    removeScope.setType( MSG_TYPE_REQUEST );
    removeScope.setSender( getProcId() );
    removeScope.setReceiver( "CTX" );
    removeScope.set( MessageField::FIELD_PROC_ID, getProcId());
    removeScope.set( MessageField::FIELD_SHARED_SCOPE, scope);

    SPELLipcMessage response = sendCTXRequest(&removeScope, m_ipcTimeoutCtxRequestMsec);
	if (response.isVoid() )
	{
		THROW_EXCEPTION("Failed to remove shared data scope", "No response from context", SPELL_ERROR_IPC);
	}
	else if (response.getType() == MSG_TYPE_ERROR )
	{
		THROW_EXCEPTION(response.get(MessageField::FIELD_ERROR),response.get(MessageField::FIELD_REASON), SPELL_ERROR_ENVIRONMENT);
	}
}

//=============================================================================
// METHOD: SPELLserverCif::removeSharedDataScopes
//=============================================================================
void SPELLserverCif::removeSharedDataScopes()
{
	// Send notification of prompt end
    SPELLipcMessage removeScopes;
    removeScopes.setId( ContextMessages::REQ_DEL_SHARED_DATA_SCOPE );
    removeScopes.setType( MSG_TYPE_REQUEST );
    removeScopes.setSender( getProcId() );
    removeScopes.setReceiver( "CTX" );
    removeScopes.set( MessageField::FIELD_PROC_ID, getProcId());

    SPELLipcMessage response = sendCTXRequest(&removeScopes, m_ipcTimeoutCtxRequestMsec);
	if (response.isVoid() )
	{
		THROW_EXCEPTION("Failed to remove ALL shared data scopes", "No response from context", SPELL_ERROR_IPC);
	}
	else if (response.getType() == MSG_TYPE_ERROR )
	{
		THROW_EXCEPTION(response.get(MessageField::FIELD_ERROR),response.get(MessageField::FIELD_REASON), SPELL_ERROR_ENVIRONMENT);
	}
}
