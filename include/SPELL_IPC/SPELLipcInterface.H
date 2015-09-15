// ################################################################################
// FILE       : SPELLipcInterface.H
// DATE       : Mar 17, 2011
// PROJECT    : SPELL
// DESCRIPTION: Common IPC interface
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

#ifndef __SPELL_IPC_INTERFACE_H__
#define __SPELL_IPC_INTERFACE_H__

// FILES TO INCLUDE ////////////////////////////////////////////////////////
// System includes ---------------------------------------------------------
// Local includes ----------------------------------------------------------
#include "SPELL_IPC/SPELLipc.H"
#include "SPELL_IPC/SPELLipcMessageMailbox.H"
#include "SPELL_IPC/SPELLipcInterfaceListener.H"
#include "SPELL_IPC/SPELLipcTrash.H"
// Project includes --------------------------------------------------------
#include "SPELL_SYN/SPELLmonitor.H"


/** \addtogroup SPELL_IPC */
/*@{*/


// FORWARD REFERENCES //////////////////////////////////////////////////////
class SPELLipcMessage;
class SPELLipcOutput;
class SPELLipcInput;
class SPELLipcIncomingBase;
class SPELLipcIncomingMessage;
class SPELLipcIncomingRequest;
// TYPES ///////////////////////////////////////////////////////////////////
// DEFINES /////////////////////////////////////////////////////////////////

//////////////////////////////////////////////////////////////////////////////
/**
 ** \brief IPC base interface
 **
 ** \par Description and usage:
 **
 ** 	Base class of both client and server interfaces. This class
 ** 	encapsulates the IPC socket-based mechanism used to send messages
 ** 	between SPELL processes.
 **
 ** \see SPELLipcClientInterface and SPELLipcServerInterface
 **
**////////////////////////////////////////////////////////////////////////////
class SPELLipcInterface : public SPELLthread
{
public: //--------------------------------------------------------------------

    // EXCEPTIONS ////////////////////////////////////////////////////////////
    // ENUMS /////////////////////////////////////////////////////////////////
    // TYPES /////////////////////////////////////////////////////////////////
    // LIFECYCLE /////////////////////////////////////////////////////////////

		//////////////////////////////////////////////////////////////////////
		/** Constructor.
		 *
		 * \param name IN: name of the interface.
		**////////////////////////////////////////////////////////////////////
		SPELLipcInterface( const std::string& name )
		: SPELLthread(name)
		{};

		//////////////////////////////////////////////////////////////////////
		/** Destructor.
		**////////////////////////////////////////////////////////////////////
		virtual ~SPELLipcInterface() {};

    // STATIC ////////////////////////////////////////////////////////////////
    // METHODS ///////////////////////////////////////////////////////////////

		//////////////////////////////////////////////////////////////////////
		/** Initialize the interface to use the given IPC listener. This class
		 * will process all incoming messages and requests, as well as errors.
		 *
		 * \param listener IN: the listener to be used.
		**////////////////////////////////////////////////////////////////////
		virtual void initialize( SPELLipcInterfaceListener* listener ) = 0;

		//////////////////////////////////////////////////////////////////////
		/** Remove the IPC listener
		**////////////////////////////////////////////////////////////////////
		virtual void removeListener() = 0;

		//////////////////////////////////////////////////////////////////////
		/** Connect the IPC interface.
		**////////////////////////////////////////////////////////////////////
		virtual void connect( bool reconnect = false ) = 0;

		//////////////////////////////////////////////////////////////////////
		/** Disconnect the IPC interface.
		 *
		 * \param send_eoc IN: if true, send an "End-Of-Conversation" message.
		 * Otherwise, disconnect abruptly.
		**////////////////////////////////////////////////////////////////////
		virtual void disconnect() = 0;

		//////////////////////////////////////////////////////////////////////
		/** Interface working thread method.
		 **///////////////////////////////////////////////////////////////////
		virtual void run() = 0;

		//////////////////////////////////////////////////////////////////////
		/** Check if the interface is connected.
		 *
		 * \return True if the interface is connected.
		 **///////////////////////////////////////////////////////////////////
		virtual bool isConnected() =0;

		//////////////////////////////////////////////////////////////////////
		/** Set the interface as ready to work.
		 **///////////////////////////////////////////////////////////////////
		virtual void setReady() = 0;

		//////////////////////////////////////////////////////////////////////
		/** Wait for the interface to be ready.
		 **///////////////////////////////////////////////////////////////////
		virtual void waitReady() = 0;

		//////////////////////////////////////////////////////////////////////
		/** Connection lost callback. Called when a peer abruptly disconnects
		 * from this interface.
		 *
		 * \param peerKey IN: key of the peer that disconnected.
		**////////////////////////////////////////////////////////////////////
		virtual void connectionLost( int peerKey, int errNo, const std::string& reason ) = 0;

		//////////////////////////////////////////////////////////////////////
		/** Connection closed callback. Called when a peer has requested the
		 * end of conversation.
		 *
		 * \param peerKey IN: key of the peer that disconnected.
		**////////////////////////////////////////////////////////////////////
		virtual void connectionClosed( int peerKey ) = 0;

		//////////////////////////////////////////////////////////////////////
		/** Called when a SPELL request comes in.
		 *
		 * \param msg IN: request to be processed.
		**////////////////////////////////////////////////////////////////////
		virtual void incomingRequest( const SPELLipcMessage& msg ) = 0;

		//////////////////////////////////////////////////////////////////////
		/** Called when a SPELL message comes in.
		 *
		 * \param msg IN: message to be processed.
		**////////////////////////////////////////////////////////////////////
		virtual void incomingMessage( const SPELLipcMessage& msg ) = 0;

		//////////////////////////////////////////////////////////////////////
		/** Called when a response to a SPELL request comes in.
		 *
		 * \param msg IN: response to be processed.
		**////////////////////////////////////////////////////////////////////
		virtual void incomingResponse( const SPELLipcMessage& msg ) = 0;
};

/*@}*/
#endif
