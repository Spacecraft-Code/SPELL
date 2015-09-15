// ################################################################################
// FILE       : SPELLipcInterfaceListener.H
// DATE       : Mar 17, 2011
// PROJECT    : SPELL
// DESCRIPTION: Interface IPC message listener
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

#ifndef __SPELL_IPC_INTERFACE_LST_H__
#define __SPELL_IPC_INTERFACE_LST_H__

// FILES TO INCLUDE ////////////////////////////////////////////////////////
// System includes ---------------------------------------------------------
// Local includes ----------------------------------------------------------
#include "SPELL_IPC/SPELLipcMessage.H"
// Project includes --------------------------------------------------------

/** \addtogroup SPELL_IPC */
/*@{*/


// FORWARD REFERENCES //////////////////////////////////////////////////////
// TYPES ///////////////////////////////////////////////////////////////////
// DEFINES /////////////////////////////////////////////////////////////////

//////////////////////////////////////////////////////////////////////////////
/**
 ** \brief IPC interface listener
 **
 ** \par Description and usage:
 **
 ** 	Interface used by classes that will process incoming messages and
 ** 	requests from IPC layer.
 **
 **
**////////////////////////////////////////////////////////////////////////////
class SPELLipcInterfaceListener
{
public:

    //////////////////////////////////////////////////////////////////////
    /** Destructor.
    **////////////////////////////////////////////////////////////////////
    virtual ~SPELLipcInterfaceListener() {};

    //////////////////////////////////////////////////////////////////////
    /** Process an incoming message.
     *
     * \param msg IN: the message to be processed.
    **////////////////////////////////////////////////////////////////////
    virtual void processMessage( const SPELLipcMessage& msg ) = 0;

    //////////////////////////////////////////////////////////////////////
    /** Process an incoming request.
     *
     * \param msg IN: request to be processed.
     *
     * \return The request response.
    **////////////////////////////////////////////////////////////////////
    virtual SPELLipcMessage processRequest( const SPELLipcMessage& msg ) = 0;

    //////////////////////////////////////////////////////////////////////
    /** Process an IPC error.
     *
     * \param error IN: error message
     * \param message IN: error explanation
    **////////////////////////////////////////////////////////////////////
    virtual void processConnectionError( int clientKey, std::string error, std::string message ) = 0;

    //////////////////////////////////////////////////////////////////////
    /** Peer has closed the connection (end of conversation)
    **////////////////////////////////////////////////////////////////////
    virtual void processConnectionClosed( int clientKey ) = 0;
};

/*@}*/
#endif
