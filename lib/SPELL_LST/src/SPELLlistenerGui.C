// ################################################################################
// FILE       : SPELLlistenerGui.C
// DATE       : Jul 05, 2011
// PROJECT    : SPELL
// DESCRIPTION: 
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
// Local includes ----------------------------------------------------------
// Project includes --------------------------------------------------------
#include "SPELL_LST/SPELLlistenerGui.H"
#include "SPELL_UTIL/SPELLlog.H"
#include "SPELL_CFG/SPELLconfiguration.H"
#include "SPELL_PRC/SPELLprocessManager.H"
#include "SPELL_IPC/SPELLipcHelper.H"
#include "SPELL_IPC/SPELLipcError.H"
#include "SPELL_IPC/SPELLipc_Context.H"
#include "SPELL_IPC/SPELLipc_Listener.H"
// System includes ---------------------------------------------------------


//=============================================================================
// CONSTRUCTOR: SPELLlistenerGui::SPELLlistenerGui
//=============================================================================
SPELLlistenerGui::SPELLlistenerGui(const std::string& configFile)
{
    m_configFile = configFile;
    m_port = STRI(SPELLconfiguration::instance().getListenerParameter("ListenerPort"));
    m_peer = NULL;
    m_ctx = NULL;
    m_peer = NULL;
    m_ipc = NULL;
}

//=============================================================================
// DESTRUCTOR: SPELLlistenerGui::~SPELLlistenerGui
//=============================================================================
SPELLlistenerGui::~SPELLlistenerGui()
{
}

//=============================================================================
// METHOD: SPELLlistenerGui::startup
//=============================================================================
int SPELLlistenerGui::startup(SPELLlistenerComm* ctx, SPELLlistenerComm* peer)
{
    m_ctx = ctx;
    m_peer = peer;

    DEBUG("Creating GUI listener");

    try {
        m_ipc = new SPELLipcServerInterface("LST-TO-GUI", 777, m_port);
        m_ipc->initialize(this);
        m_ipc->connect();
        m_ipc->start();
        LOG_INFO("Listening GUI on port " + ISTR(m_port));
    } catch(SPELLipcError& err) {
        LOG_ERROR("Could not create GUI listener socket.");
    }

    return 0;
}

//=============================================================================
// METHOD: SPELLlistenerGui::shutdown
//=============================================================================
int SPELLlistenerGui::shutdown()
{
	DEBUG("Shutdown GUI connections");
	m_ipc->disconnect();
    return 0;
}

//=============================================================================
// METHOD: SPELLlistenerGui::processMessage
//=============================================================================
void SPELLlistenerGui::processMessage( const SPELLipcMessage& msg)
{
    DEBUG("Got message: " + msg.getId());
}

//=============================================================================
// METHOD: SPELLlistenerGui::processRequest
//=============================================================================
SPELLipcMessage SPELLlistenerGui::processRequest( const SPELLipcMessage& msg )
{
    SPELLipcMessage res = VOID_MESSAGE;
    GuiInfo info;

    LOG_INFO("Got request: " + msg.getId() + " from GUI " + ISTR(msg.getKey()));

    if (msg.getId() == ContextMessages::REQ_CLOSE_CTX)
        res = this->stopContext(msg);
    else if (msg.getId() == ContextMessages::REQ_OPEN_CTX)
        res = this->startContext(msg);
    else if (msg.getId() == ContextMessages::REQ_CTX_LIST)
        res = this->contextList(msg);
    else if (msg.getId() == ContextMessages::REQ_CTX_INFO)
        res = this->contextInfo(msg);
    else if (msg.getId() == ContextMessages::REQ_ATTACH_CTX)
        res = this->attachContext(msg);
    else if (msg.getId() == ContextMessages::REQ_DESTROY_CTX)
        res = this->destroyContext(msg);
    else if (msg.getId() == ContextMessages::REQ_GUI_LOGIN)
        res = this->guiLogin(msg);
    else if (msg.getId() == ContextMessages::REQ_GUI_LOGOUT)
        res = this->guiLogout(msg);

    if (res.isVoid())
    {
    	LOG_ERROR("Listener request not handled: " + msg.getId());
    }

    return res;

}

//=============================================================================
// METHOD: SPELLlistenerGui::guiLogout
//=============================================================================
SPELLipcMessage SPELLlistenerGui::guiLogout( const SPELLipcMessage& msg )
{
	// No need to close IPC here. When the GUI receives the response,
	// it will close the channel by sending EOC. There, IPC layer will
	// automatically close the corresponding channel.
    m_openGUIs.erase(msg.getKey());
    DEBUG("Unregistered GUI: " + ISTR(msg.getKey()));
    return SPELLipcHelper::createResponse(ContextMessages::RSP_GUI_LOGOUT, msg);
}

//=============================================================================
// METHOD: SPELLlistenerGui::guiLogin
//=============================================================================
SPELLipcMessage SPELLlistenerGui::guiLogin( const SPELLipcMessage& msg )
{
    GuiInfo info;

    info.m_key = msg.getKey();
    info.m_mode = msg.get(MessageField::FIELD_GUI_MODE);
    m_openGUIs[info.m_key] = info;

    DEBUG("Registered new GUI: " + ISTR(info.m_key));

    return SPELLipcHelper::createResponse(ContextMessages::RSP_GUI_LOGIN, msg);
}

//=============================================================================
// METHOD: SPELLlistenerGui::attachContext
//=============================================================================
SPELLipcMessage SPELLlistenerGui::attachContext( const SPELLipcMessage& msg )
{
    SPELLipcMessage res;

    LOG_INFO("Attaching to context " + msg.get(MessageField::FIELD_CTX_NAME));
    res = m_ctx->displace(msg);

    return res;
}

//=============================================================================
// METHOD: SPELLlistenerGui::contextInfo
//=============================================================================
SPELLipcMessage SPELLlistenerGui::contextInfo( const SPELLipcMessage& msg )
{
    SPELLipcMessage res;

    LOG_INFO("Providing context info for " + msg.get(MessageField::FIELD_CTX_NAME));
    res = m_ctx->displace(msg);

    return res;
}

//=============================================================================
// METHOD: SPELLlistenerGui::contextList
//=============================================================================
SPELLipcMessage SPELLlistenerGui::contextList( const SPELLipcMessage& msg )
{
    std::string s;
    std::vector<std::string> ctxList;
    SPELLipcMessage res;

    ctxList = SPELLconfiguration::instance().getAvailableContexts();

    for(std::vector<std::string>::iterator it = ctxList.begin() ;
        it != ctxList.end() ;
        it++)
    {
        s += (s.size() > 0 ? "," : "");
        s += (*it);
    }

    LOG_INFO("Providing list of contexts: " + s);

    res = SPELLipcHelper::createResponse(ContextMessages::RSP_CTX_LIST, msg);
    res.set(MessageField::FIELD_CTX_LIST, s);

    return res;
}

//=============================================================================
// METHOD: SPELLlistenerGui::startContext
//=============================================================================
SPELLipcMessage SPELLlistenerGui::startContext( const SPELLipcMessage& msg )
{
    return m_ctx->displace(msg);
}

//=============================================================================
// METHOD: SPELLlistenerGui::destroyContext
//=============================================================================
SPELLipcMessage SPELLlistenerGui::destroyContext( const SPELLipcMessage& msg )
{
    return m_ctx->displace(msg);
}

//=============================================================================
// METHOD: SPELLlistenerGui::stopContext
//=============================================================================
SPELLipcMessage SPELLlistenerGui::stopContext( const SPELLipcMessage& msg )
{
    return m_ctx->displace(msg);
}

//=============================================================================
// METHOD: SPELLlistenerGui::processConnectionError
//=============================================================================
void SPELLlistenerGui::processConnectionError(int key, std::string error, std::string reason)
{
    LOG_ERROR("GUI connection error:" + error + ": " + reason );
    std::map<int,GuiInfo>::iterator it = m_openGUIs.find(key);
    if (it != m_openGUIs.end())
    {
    	m_openGUIs.erase(it);
    }
}

//=============================================================================
// METHOD: SPELLlistenerGui::processConnectionClosed
//=============================================================================
void SPELLlistenerGui::processConnectionClosed(int key )
{
    LOG_INFO("GUI " + ISTR(key) + " closed the connection");
    std::map<int,GuiInfo>::iterator it = m_openGUIs.find(key);
    if (it != m_openGUIs.end())
    {
    	m_openGUIs.erase(it);
    }
}

//=============================================================================
// METHOD: SPELLlistenerGui::displace
//=============================================================================
SPELLipcMessage SPELLlistenerGui::displace( const SPELLipcMessage& msg )
{
    DEBUG("Notifying " + ISTR(m_openGUIs.size()) + " clients");
    if(msg.getId() == ListenerMessages::MSG_CONTEXT_OP)
    {
        SPELLipcMessage toSend(msg);
        for(std::map<int, GuiInfo>::iterator it = m_openGUIs.begin() ;
            it != m_openGUIs.end() ;
            it++)
        {
            DEBUG("Notifying " + ISTR(it->first));
            m_ipc->sendMessage(it->second.m_key, toSend);
        }
    }
    return VOID_MESSAGE;
}

//=============================================================================
// METHOD: SPELLlistenerGui::notifyClients
//=============================================================================
void SPELLlistenerGui::notifyClients( const SPELLipcMessage& msg )
{
    SPELLipcMessage toSend(msg);
    for(std::map<int, GuiInfo>::iterator it = m_openGUIs.begin() ;
        it != m_openGUIs.end() ;
        it++)
    {
        m_ipc->sendMessage(it->second.m_key, toSend);
    }
}
