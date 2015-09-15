// ################################################################################
// FILE       : SPELLlistenerContext.H
// DATE       : Jul 05, 2011
// PROJECT    : SPELL
// DESCRIPTION: SPELL listener library header file
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

#ifndef __SPELL_LISTENER_CONTEXT_H
#define __SPELL_LISTENER_CONTEXT_H

// FILES TO INCLUDE ////////////////////////////////////////////////////////
// Local includes ----------------------------------------------------------
// Project includes --------------------------------------------------------
#include "SPELL_LST/SPELLlistenerComm.H"
#include "SPELL_UTIL/SPELLbase.H"
#include "SPELL_CFG/SPELLconfiguration.H"
#include "SPELL_IPC/SPELLipcServerInterface.H"
#include "SPELL_IPC/SPELLipcInterfaceListener.H"
#include "SPELL_IPC/SPELLipcMessage.H"
#include "SPELL_PRC/SPELLprocessManager.H"
#include "SPELL_SYN/SPELLevent.H"
// System includes ---------------------------------------------------------
#include <vector>
#include <map>

/** \addtogroup SPELL_LST */
/*@{*/

//
// FORWARD REFERENCES //////////////////////////////////////////////////////
// TYPES ///////////////////////////////////////////////////////////////////
// DEFINES /////////////////////////////////////////////////////////////////

class SPELLlistenerContext :
    public SPELLipcInterfaceListener, 
    public SPELLprocessListener,
    public SPELLlistenerComm
{
public:
    class ContextInfo
    {
    public:
        int m_key;
        std::string m_name;
        unsigned short m_port;
        std::string m_status;
        SPELLcontextConfig* m_ctxConfig;
        SPELLdriverConfig* m_drvConfig;
    };

private:
    std::string m_configFile;
    int m_port;
    std::string m_contextStartupCmd;
    std::map<std::string, ContextInfo> m_openContexts;
    SPELLipcServerInterface *m_ipc;
    SPELLlistenerComm *m_gui;
    SPELLlistenerComm *m_peer;
    SPELLevent m_waitForContextStart;
    SPELLevent m_waitForAck;
    SPELLevent m_waitForContextClose;

public:
    SPELLlistenerContext(const std::string&);
    virtual ~SPELLlistenerContext();

    // SPELLipcInterfaceListener methods
    void processMessage( const SPELLipcMessage& msg );
    SPELLipcMessage processRequest( const SPELLipcMessage& msg );
    void processConnectionError(int, std::string, std::string);
    //////////////////////////////////////////////////////////////////////
    /** \see SPELLipcInterfaceListener
    **////////////////////////////////////////////////////////////////////
    void processConnectionClosed( int clientKey );

    // SPELLlistenerComm methods
    SPELLipcMessage displace( const SPELLipcMessage& msg );
    void notifyClients( const SPELLipcMessage& msg );
    
    // SPELLprocessListener methods
    void processStarted( const std::string& identifier );
	void processFinished( const std::string& identifier, const int& retValue );
	void processKilled( const std::string& identifier );
	void processFailed( const std::string& identifier );
    bool respawnContext(const std::string& ctxName);

    // Internals
    int startup(SPELLlistenerComm*, SPELLlistenerComm*);
    int shutdown();
    void fillContextInfo(const std::string, SPELLipcMessage& );

    // Actions
    SPELLipcMessage startContext(const SPELLipcMessage&);
    SPELLipcMessage stopContext(const SPELLipcMessage&);
    SPELLipcMessage contextInfo(const SPELLipcMessage&);
    SPELLipcMessage attachContext(const SPELLipcMessage&);
    SPELLipcMessage destroyContext(const SPELLipcMessage&);
    SPELLipcMessage takeControl(const SPELLipcMessage&);

    // Events
    void onNewContext(const SPELLipcMessage&);
    void onClosedContext(const SPELLipcMessage&);

};

/*@}*/
#endif
