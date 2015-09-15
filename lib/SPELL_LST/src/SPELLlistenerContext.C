// ################################################################################
// FILE       : SPELLlistenerContext.C
// DATE       : Jul 05, 2011
// PROJECT    : SPELL
// DESCRIPTION: 
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
#include "SPELL_LST/SPELLlistenerContext.H"
// Project includes --------------------------------------------------------
#include "SPELL_UTIL/SPELLlog.H"
#include "SPELL_UTIL/SPELLerror.H"
#include "SPELL_CFG/SPELLconfiguration.H"
#include "SPELL_PRC/SPELLprocessManager.H"
#include "SPELL_WRP/SPELLconstants.H"
#include "SPELL_IPC/SPELLipcHelper.H"
#include "SPELL_IPC/SPELLipcMessage.H"
#include "SPELL_IPC/SPELLipcError.H"
#include "SPELL_IPC/SPELLipc_Context.H"
#include "SPELL_IPC/SPELLipc_Listener.H"
// System includes ---------------------------------------------------------

// DEFINES /////////////////////////////////////////////////////////////////
// GLOBALS /////////////////////////////////////////////////////////////////
// STATIC //////////////////////////////////////////////////////////////////

//=============================================================================
// CONSTRUCTOR: SPELLlistenerContext::SPELLlistenerContext
//=============================================================================
SPELLlistenerContext::SPELLlistenerContext(const std::string& configFile)
{
    SPELLcontextConfig* ctxConfig;
    SPELLdriverConfig* drvConfig;
    ContextInfo ctxInfo;
    std::vector<std::string> ctxList;

    m_gui = NULL;
    m_peer = NULL;

    m_configFile = configFile;
    m_port = STRI(SPELLconfiguration::instance().getListenerParameter("ContextListenerPort"));
    m_contextStartupCmd = SPELLconfiguration::instance().getListenerParameter("ContextScript");

    ctxList = SPELLconfiguration::instance().getAvailableContexts();

    for(std::vector<std::string>::iterator it = ctxList.begin() ;
        it != ctxList.end() ;
        it++)
    {
		DEBUG("Loading context information for " + *it);

		ctxInfo.m_key = 0;
		ctxInfo.m_name = *it;
		ctxInfo.m_port = 0;
		ctxInfo.m_status = MessageValue::DATA_CTX_AVAILABLE;

		try
		{
			ctxConfig = &SPELLconfiguration::instance().getContext(ctxInfo.m_name);
			drvConfig = &SPELLconfiguration::instance().getDriver(ctxConfig->getDriverName());
			ctxInfo.m_ctxConfig = ctxConfig;
			ctxInfo.m_drvConfig = drvConfig;
		}
		catch(SPELLcoreException& ex)
		{
			LOG_ERROR("Failed to read context configuration");
			ctxInfo.m_ctxConfig = NULL;
			ctxInfo.m_drvConfig = NULL;
		}

		m_openContexts[ctxInfo.m_name] = ctxInfo;

		DEBUG("Added context:");
		DEBUG("- Name=" + ctxInfo.m_name);
		DEBUG("- Key=" + ISTR(ctxInfo.m_key));
		DEBUG("- Port=" + ISTR(ctxInfo.m_port));
		DEBUG("- Status=" + ctxInfo.m_status);
    }
}

//=============================================================================
// DESTRUCTOR: SPELLlistenerContext::~SPELLlistenerContext
//=============================================================================
SPELLlistenerContext::~SPELLlistenerContext()
{
}


//=============================================================================
// METHOD: SPELLlistenerContext::startup
//=============================================================================
int SPELLlistenerContext::startup(SPELLlistenerComm* gui, SPELLlistenerComm* peer)
{
	DEBUG("Creating context listener");

    m_gui = gui;
    m_peer = peer;

    try {
        m_ipc = new SPELLipcServerInterface("LST-TO-CTX", 777, m_port);
        m_ipc->initialize(this);
        m_ipc->connect();
        m_ipc->start();

        m_port = m_ipc->getPort();

        LOG_INFO("Listening context on port " + ISTR(m_port));
    } catch(SPELLipcError& err) {
        LOG_ERROR("Could not create context listener socket.");
    }

    return 0;
}

//=============================================================================
// METHOD: SPELLlistenerContext::shutdown
//=============================================================================
int SPELLlistenerContext::shutdown()
{
	DEBUG("Shutting down");
    m_ipc->disconnect();
    // Kill all remaining contexts
    std::map<std::string, ContextInfo>::iterator it;
    for( it = m_openContexts.begin(); it != m_openContexts.end(); it++)
    {
    	ContextInfo& info = it->second;
    	if (info.m_status == MessageValue::DATA_CTX_RUNNING)
    	{
    		LOG_WARN("Killing context " + info.m_name);
    		try
    		{
    			SPELLprocessManager::instance().killProcess( "CTX_" + info.m_name );
    		}
    		catch(SPELLcoreException& ex)
    		{
    			LOG_ERROR("Unable to kill context");
    		}
    	}
    }
    return 0;
}

//=============================================================================
// METHOD: SPELLlistenerContext::startContext
//=============================================================================
SPELLipcMessage SPELLlistenerContext::startContext( const SPELLipcMessage& msg )
{
    std::string cmd;
    std::string name;

    name = msg.get(MessageField::FIELD_CTX_NAME);

    cmd = SPELLutils::getSPELL_HOME() + PATH_SEPARATOR + "bin" + PATH_SEPARATOR 
        + "SPELL-Context -n " + name + " -s " + ISTR(m_port)
        + " -c " + m_configFile;

    DEBUG("Opening context using command: " + cmd);

    std::string identifier = "CTX_" + name;

    // Will have no effect if the listener is there already
    SPELLprocessManager::instance().addListener("CTX_" + name, this);

    // Clear process information, in case it is there already
    SPELLprocessManager::instance().clearProcess(identifier);
    // Register and start the process
    SPELLprocessManager::instance().startProcess("CTX_" + name, cmd);

    // Notify to other clients
    SPELLipcMessage notify( msg );
    notify.setId( ListenerMessages::MSG_CONTEXT_OP );
    notify.setType( MSG_TYPE_ONEWAY );
    notify.set( MessageField::FIELD_CTX_STATUS, MessageValue::DATA_CTX_STARTING );
    notify.setSender("LST");
    notify.setReceiver("GUI");
    m_gui->displace(&notify);

    DEBUG("Wait context " + name + " login");
    m_waitForContextStart.clear();
    bool timeout = m_waitForContextStart.wait(15*1000);
    DEBUG("Wait context " + name + " login done: timeout " + BSTR(timeout));

    if (timeout)
    {
    	SPELLipcMessage resp = SPELLipcHelper::createErrorResponse(ContextMessages::RSP_OPEN_CTX, msg);
    	resp.set( MessageField::FIELD_FATAL, PythonConstants::True);
    	resp.set( MessageField::FIELD_ERROR, "Cannot open context " + name);
    	resp.set( MessageField::FIELD_REASON, "Context did not log in in time");
    	return resp;
    }
    else
    {
    	LOG_INFO("Context running");
    	return SPELLipcHelper::createResponse(ContextMessages::RSP_OPEN_CTX, msg);
    }
}

//=============================================================================
// METHOD: SPELLlistenerContext::destroyContext
//=============================================================================
SPELLipcMessage SPELLlistenerContext::destroyContext( const SPELLipcMessage& msg )
{
    std::string name;

    name = msg.get(MessageField::FIELD_CTX_NAME);

    ContextInfo& ctxInfo = m_openContexts[name];
    ctxInfo.m_status = MessageValue::DATA_CTX_AVAILABLE;

    m_ipc->disconnectClient( ctxInfo.m_key );

    try
    {
    	SPELLprocessManager::instance().killProcess("CTX_" + name);
    	SPELLprocessManager::instance().waitProcess("CTX_" + name);
    	SPELLprocessManager::instance().clearProcess("CTX_" + name);
    }
    catch(SPELLcoreException& ex) {};

    LOG_INFO("Destroyed context");
    
    return SPELLipcHelper::createResponse(ContextMessages::RSP_DESTROY_CTX, msg);
}

//=============================================================================
// METHOD: SPELLlistenerContext::stopContext
//=============================================================================
SPELLipcMessage SPELLlistenerContext::stopContext( const SPELLipcMessage& msg )
{
    SPELLipcMessage closemsg( ContextMessages::MSG_CLOSE_CTX );
    ContextInfo* ctxInfo;
    std::string name;

    name = msg.get(MessageField::FIELD_CTX_NAME);

    this->fillContextInfo(name, closemsg);

    ctxInfo = &m_openContexts[name];

    closemsg.setType( MSG_TYPE_ONEWAY );
    closemsg.setSender("LST");
    closemsg.setReceiver("CTX_" + name);

    m_waitForContextClose.clear();

    DEBUG("Send close message to context");
    m_ipc->sendMessage( ctxInfo->m_key, closemsg );
    
    DEBUG("Wait for context to close");
    bool timeout = m_waitForContextClose.wait( 5000 );

    DEBUG("Disconnect context IPC");
	m_ipc->disconnectClient( ctxInfo->m_key );
    DEBUG("Disconnected context IPC");

	if (timeout)
    {
	    LOG_WARN("Cannot close, killing instead");
		SPELLprocessManager::instance().killProcess("CTX_" + name);
    }
    else
    {
    	try
    	{
    	    DEBUG("Waiting context process to finish");
    		SPELLprocessManager::instance().waitProcess("CTX_" + name);
    		SPELLprocessManager::instance().clearProcess("CTX_" + name);
    	    LOG_INFO("Closed context");

    	    SPELLipcMessage notify(ListenerMessages::MSG_CONTEXT_OP);
    	    notify.setType( MSG_TYPE_ONEWAY );

    	    ctxInfo = &m_openContexts[msg.get(MessageField::FIELD_CTX_NAME)];
    	    ctxInfo->m_status = MessageValue::DATA_CTX_AVAILABLE;

    	    std::string identifier = "CTX_" + ctxInfo->m_name;

    	    DEBUG("Closed context:");
    	    DEBUG("- Name=" + ctxInfo->m_name);
    	    DEBUG("- Key=" + ISTR(ctxInfo->m_key));
    	    DEBUG("- Port=" + ISTR(ctxInfo->m_port));
    	    DEBUG("- Status=" + ctxInfo->m_status);

    	    SPELLprocessManager::instance().removeListener(identifier, this);
    	    SPELLprocessManager::instance().clearProcess(identifier);

    	    this->fillContextInfo(ctxInfo->m_name, notify);

    	    // Notify other clients
    	    notify.setId( ListenerMessages::MSG_CONTEXT_OP );
    	    notify.set( MessageField::FIELD_CTX_STATUS, MessageValue::DATA_CTX_AVAILABLE );
    	    notify.setSender("LST");
    	    notify.setReceiver("GUI");
    	    m_gui->displace(&notify);

    	}
    	catch(SPELLcoreException& ex) {};
    }

    return SPELLipcHelper::createResponse(ContextMessages::RSP_CLOSE_CTX, msg);
}

//=============================================================================
// METHOD: SPELLlistenerContext::contextInfo
//=============================================================================
SPELLipcMessage SPELLlistenerContext::contextInfo( const SPELLipcMessage& msg )
{
    std::string name;

    SPELLipcMessage res = SPELLipcHelper::createResponse(ContextMessages::RSP_CTX_INFO, msg);
    name = msg.get(MessageField::FIELD_CTX_NAME);
    this->fillContextInfo(name, res);

    return res;
}

//=============================================================================
// METHOD: SPELLlistenerContext::attachContext
//=============================================================================
SPELLipcMessage SPELLlistenerContext::attachContext( const SPELLipcMessage& msg )
{
    std::string name;

    SPELLipcMessage res = SPELLipcHelper::createResponse(ContextMessages::RSP_ATTACH_CTX, msg);
    name = msg.get(MessageField::FIELD_CTX_NAME);
    this->fillContextInfo(name, res);

    return res;
}

//=============================================================================
// METHOD: SPELLlistenerContext::processMessage
//=============================================================================
void SPELLlistenerContext::processMessage( const SPELLipcMessage& msg )
{
    DEBUG("Got message: " + msg.getId());

	if (msg.getId() == ListenerMessages::MSG_CONTEXT_OPEN)
	{
		this->onNewContext(msg);
	}
	else if (msg.getId() == ListenerMessages::MSG_CONTEXT_CLOSED)
	{
		this->onClosedContext(msg);
	}
	else
	{
        LOG_ERROR("Unprocessed message: " + msg.getId());
	}
}

//=============================================================================
// METHOD: SPELLlistenerContext::onNewContext
//=============================================================================
void SPELLlistenerContext::onNewContext( const SPELLipcMessage& msg )
{
    ContextInfo* ctxInfo;

    std::string ctxName = msg.get(MessageField::FIELD_CTX_NAME);

    ctxInfo = &m_openContexts[ctxName];
    ctxInfo->m_key = msg.getKey();
    ctxInfo->m_port = STRI(msg.get(MessageField::FIELD_CTX_PORT));
    ctxInfo->m_status = MessageValue::DATA_CTX_RUNNING;

    DEBUG("New context:");
    DEBUG("- Name=" + ctxInfo->m_name);
    DEBUG("- Key=" + ISTR(ctxInfo->m_key));
    DEBUG("- Port=" + ISTR(ctxInfo->m_port));
    DEBUG("- Status=" + ctxInfo->m_status);

    m_waitForContextStart.set();

    // Notify to other clients
    SPELLipcMessage notify( msg );
    notify.setId( ListenerMessages::MSG_CONTEXT_OP );
    notify.setType( MSG_TYPE_ONEWAY );
    notify.set( MessageField::FIELD_CTX_STATUS, MessageValue::DATA_CTX_RUNNING );
    notify.setSender("LST");
    notify.setReceiver("GUI");
    m_gui->displace(&notify);

    // Send notification to peer if any
    if (m_peer) m_peer->displace(msg);
}

//=============================================================================
// METHOD: SPELLlistenerContext::fillContextInfo
//=============================================================================
void SPELLlistenerContext::fillContextInfo(const std::string ctxName, SPELLipcMessage& msg )
{
    std::string driverName;
    SPELLcontextConfig* ctxConfig;
    SPELLdriverConfig* drvConfig;
    ContextInfo* ctxInfo;

	ctxInfo = &m_openContexts[ctxName];
	ctxConfig = ctxInfo->m_ctxConfig;
	drvConfig = ctxInfo->m_drvConfig;

	msg.set(MessageField::FIELD_CTX_NAME, ctxName);
	msg.set( MessageField::FIELD_CTX_PORT, ISTR(ctxInfo->m_port) );

	if (ctxConfig != NULL)
	{
		msg.set(MessageField::FIELD_CTX_DRV, ctxConfig->getDriverName());
		msg.set(MessageField::FIELD_CTX_SC, ctxConfig->getSC());
		msg.set(MessageField::FIELD_CTX_FAM, ctxConfig->getFamily());
		msg.set(MessageField::FIELD_CTX_GCS, ctxConfig->getGCS());
		msg.set(MessageField::FIELD_CTX_DESC, ctxConfig->getDescription());
		msg.set(MessageField::FIELD_CTX_STATUS, ctxInfo->m_status);
	}
	else
	{
		msg.set(MessageField::FIELD_CTX_DRV, "Unknown");
		msg.set(MessageField::FIELD_CTX_SC, "Unknown");
		msg.set(MessageField::FIELD_CTX_FAM, "Unknown");
		msg.set(MessageField::FIELD_CTX_GCS, "Unknown");
		msg.set(MessageField::FIELD_CTX_DESC, "Unknown");
		msg.set(MessageField::FIELD_CTX_STATUS, "ERROR");
	}

	if (drvConfig != NULL)
	{
		msg.set(MessageField::FIELD_CTX_MAXPROC, ISTR(drvConfig->getMaxProcs()));
	}
	else
	{
		msg.set(MessageField::FIELD_CTX_MAXPROC, "0" );
	}
}

//=============================================================================
// METHOD: SPELLlistenerContext::onClosedContext
//=============================================================================
void SPELLlistenerContext::onClosedContext( const SPELLipcMessage& msg )
{
    m_waitForContextClose.set();
}

//=============================================================================
// METHOD: SPELLlistenerContext::processRequest
//=============================================================================
SPELLipcMessage SPELLlistenerContext::processRequest( const SPELLipcMessage& msg )
{
    DEBUG("Got request: " + msg.getId());

    // No request from context yet

    return VOID_MESSAGE;
}

//=============================================================================
// METHOD: SPELLlistenerContext::processConnectionError
//=============================================================================
void SPELLlistenerContext::processConnectionError(int key, std::string error, std::string reason)
{
    LOG_ERROR("Context connection error: " + error + ": " + reason );
}

//=============================================================================
// METHOD: SPELLlistenerContext::processConnectionClosed
//=============================================================================
void SPELLlistenerContext::processConnectionClosed(int key)
{
    LOG_ERROR("Context connection closed");
}

//=============================================================================
// METHOD: SPELLlistenerContext::displace
//=============================================================================
SPELLipcMessage SPELLlistenerContext::displace( const SPELLipcMessage& msg )
{
    SPELLipcMessage res = VOID_MESSAGE;
    std::string name;

    DEBUG("Got IPC object: " + msg.getId());

    if (msg.getId() == ContextMessages::REQ_OPEN_CTX)
        res = this->startContext(msg);
    else if (msg.getId() == ContextMessages::REQ_CLOSE_CTX)
        res = this->stopContext(msg);
    else if (msg.getId() == ContextMessages::REQ_CTX_INFO)
        res = this->contextInfo(msg);
    else if (msg.getId() == ContextMessages::REQ_ATTACH_CTX)
        res = this->attachContext(msg);
    else if (msg.getId() == ContextMessages::REQ_DESTROY_CTX)
        res = this->destroyContext(msg);
    else
        LOG_ERROR("Unprocessed message: " + msg.getId())

	DEBUG("Response IPC object: " + res.dataStr());

    return res;
}

//=============================================================================
// METHOD: SPELLlistenerContext::processStarted
//=============================================================================
void SPELLlistenerContext::processStarted( const std::string& identifier )
{
	LOG_INFO("Context process started: " + identifier);
}

//=============================================================================
// METHOD: SPELLlistenerContext::processFinished
//=============================================================================
void SPELLlistenerContext::processFinished( const std::string& identifier, const int& retValue )
{
	LOG_INFO("Context process finished: " + identifier);
}

//=============================================================================
// METHOD: SPELLlistenerContext::processKilled
//=============================================================================
void SPELLlistenerContext::processKilled( const std::string& identifier )
{
    ContextInfo* ctxInfo;
    std::string ctxName = identifier.substr(4);
    LOG_ERROR("Context " + ctxName + " was killed");
    ctxInfo = &m_openContexts[ctxName];
    ctxInfo->m_status = MessageValue::DATA_CTX_KILLED;

    // Notify to other clients
    DEBUG("Notifying GUIs");
    SPELLipcMessage notify( ListenerMessages::MSG_CONTEXT_OP );
    notify.set( MessageField::FIELD_CTX_NAME, ctxName );
    notify.set( MessageField::FIELD_CTX_STATUS, MessageValue::DATA_CTX_KILLED);
    notify.setType( MSG_TYPE_ONEWAY );
    notify.setSender("LST");
    notify.setReceiver("GUI");
    m_gui->displace(&notify);
    DEBUG("Process killed processing done");
}

//=============================================================================
// METHOD: SPELLlistenerContext::processFailed
//=============================================================================
void SPELLlistenerContext::processFailed( const std::string& identifier )
{
    ContextInfo* ctxInfo;
    LOG_ERROR("Context " + identifier + " failed");
    ctxInfo = &m_openContexts[identifier.substr(4)];
    ctxInfo->m_status = MessageValue::DATA_CTX_ERROR;
    m_ipc->disconnect();

    // Notify to other clients
    SPELLipcMessage notify( ListenerMessages::MSG_CONTEXT_OP );
    notify.set( MessageField::FIELD_CTX_NAME, ctxInfo->m_name );
    notify.set( MessageField::FIELD_CTX_STATUS, MessageValue::DATA_CTX_ERROR);
    notify.setType( MSG_TYPE_ONEWAY );
    notify.setSender("LST");
    notify.setReceiver("GUI");
    m_gui->displace(&notify);
}

//=============================================================================
// METHOD: SPELLlistenerContext::notifyClients
//=============================================================================
void SPELLlistenerContext::notifyClients( const SPELLipcMessage& msg )
{
}

//=============================================================================
// METHOD: SPELLlistenerContext::respawnContext
//=============================================================================
bool SPELLlistenerContext::respawnContext( const std::string& ctxName )
{
	SPELLipcMessage fakeMsg(ContextMessages::REQ_OPEN_CTX);
    fakeMsg.set(MessageField::FIELD_CTX_NAME, ctxName);
    SPELLipcMessage resp = startContext(fakeMsg);
	LOG_WARN("Got response: " + resp.dataStr());
    if (resp.getType() == MSG_TYPE_ERROR) return false;
    return true;
}
