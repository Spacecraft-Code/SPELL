// ################################################################################
// FILE       : SPELLlistenerGui.H
// DATE       : Jul 05, 2011
// PROJECT    : SPELL
// DESCRIPTION: SPELL listener library header file
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

#ifndef __SPELL_LISTENER_GUI_H
#define __SPELL_LISTENER_GUI_H

// FILES TO INCLUDE ////////////////////////////////////////////////////////
// Local includes ----------------------------------------------------------
// Project includes --------------------------------------------------------
#include "SPELL_LST/SPELLlistenerComm.H"
#include "SPELL_UTIL/SPELLbase.H"
#include "SPELL_CFG/SPELLconfiguration.H"
#include "SPELL_IPC/SPELLipcServerInterface.H"
#include "SPELL_IPC/SPELLipcInterfaceListener.H"
#include "SPELL_IPC/SPELLipcMessage.H"
// System includes ---------------------------------------------------------
#include <vector>
#include <map>

/** \addtogroup SPELL_LST */
/*@{*/

//
// FORWARD REFERENCES //////////////////////////////////////////////////////
// TYPES ///////////////////////////////////////////////////////////////////
// DEFINES /////////////////////////////////////////////////////////////////

class SPELLlistenerGui : 
    public SPELLipcInterfaceListener,
    public SPELLlistenerComm
{
public:
    class GuiInfo
    {
    public:
        int m_key;
        std::string m_mode;
    };

private:
    std::string m_configFile;
    int m_port;
    std::map<int, GuiInfo> m_openGUIs;
    SPELLipcServerInterface* m_ipc;
    SPELLlistenerComm* m_ctx;
    SPELLlistenerComm* m_peer;

public:
    SPELLlistenerGui(const std::string&);
    virtual ~SPELLlistenerGui();

    // SPELLipcInterfaceListener methods
    void processMessage( const SPELLipcMessage& );
    SPELLipcMessage processRequest( const SPELLipcMessage& );
    void processConnectionError(int, std::string, std::string);
    //////////////////////////////////////////////////////////////////////
    /** \see SPELLipcInterfaceListener
    **////////////////////////////////////////////////////////////////////
    void processConnectionClosed( int clientKey );

    // SPELLlistenerComm methods
    SPELLipcMessage displace( const SPELLipcMessage&);
    void notifyClients( const SPELLipcMessage& msg);

    // Internals
    int startup(SPELLlistenerComm*, SPELLlistenerComm*);
    int shutdown();
    std::vector<SPELLlistenerGui::GuiInfo*> getGuiList();

    // Actions
    SPELLipcMessage startContext( const SPELLipcMessage& );
    SPELLipcMessage stopContext( const SPELLipcMessage& );
    SPELLipcMessage contextList( const SPELLipcMessage& );
    SPELLipcMessage contextInfo( const SPELLipcMessage& );
    SPELLipcMessage attachContext( const SPELLipcMessage& );
    SPELLipcMessage destroyContext( const SPELLipcMessage& );
    SPELLipcMessage guiLogin( const SPELLipcMessage& );
    SPELLipcMessage guiLogout( const SPELLipcMessage& );
};

/*@}*/
#endif
