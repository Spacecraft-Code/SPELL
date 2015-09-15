// ################################################################################
// FILE       : SPELLipcInput.H
// DATE       : Mar 17, 2011
// PROJECT    : SPELL
// DESCRIPTION: Data reader
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

#ifndef __SPELL_IPC_INPUT_H__
#define __SPELL_IPC_INPUT_H__

// FILES TO INCLUDE ////////////////////////////////////////////////////////
// System includes ---------------------------------------------------------
// Local includes ----------------------------------------------------------
#include "SPELL_IPC/SPELLipcMessage.H"
#include "SPELL_IPC/SPELLipcInterface.H"
#include "SPELL_IPC/SPELLsocket.H"
// Project includes --------------------------------------------------------


/** \addtogroup SPELL_IPC */
/*@{*/


// FORWARD REFERENCES //////////////////////////////////////////////////////
class SPELLipcChannel;
// TYPES ///////////////////////////////////////////////////////////////////
// DEFINES /////////////////////////////////////////////////////////////////

//////////////////////////////////////////////////////////////////////////////
/**
 ** \brief Input device
 **
 ** \par Description: this class is in charge of read incoming data from
 ** the connected socket, building SPELL messages out of it, and pass them
 ** to the associated IPC interface for further processing.
 **
 ** \par Usage: instantiated in IPC interfaces. There is one IPC input
 ** interface per connected peer.
 **
**////////////////////////////////////////////////////////////////////////////
class SPELLipcInput : public SPELLthread
{
public: //--------------------------------------------------------------------

    // EXCEPTIONS ////////////////////////////////////////////////////////////
    // ENUMS /////////////////////////////////////////////////////////////////
    // TYPES /////////////////////////////////////////////////////////////////
    // LIFECYCLE /////////////////////////////////////////////////////////////

		//////////////////////////////////////////////////////////////////////
		/** Constructor.
		 *
		 * \param name IN: name identifier for traces
		 * \param skt IN: socket class for reading data from
		 * \param peerKey IN: associated peer key
		 * \param ifc IN: reference to the IPC interface.
		**////////////////////////////////////////////////////////////////////
		SPELLipcInput( const std::string& name, SPELLsocket& skt , int peerKey, SPELLipcChannel& chan, SPELLipcInterface& ifc );

		//////////////////////////////////////////////////////////////////////
		/** Destructor.
		**////////////////////////////////////////////////////////////////////
		~SPELLipcInput();

		// STATIC ////////////////////////////////////////////////////////////////
		// METHODS ///////////////////////////////////////////////////////////////

		//////////////////////////////////////////////////////////////////////
		/** Disconnect and shutdown this reader.
		**////////////////////////////////////////////////////////////////////
		void disconnect();

		//////////////////////////////////////////////////////////////////////
		/** Check if the input interface is connected.
		 *
		 * \returns True if it is connected.
		**////////////////////////////////////////////////////////////////////
		bool isConnected();

    // DATA MEMBERS //////////////////////////////////////////////////////////

protected: //-----------------------------------------------------------------

    // EXCEPTIONS ////////////////////////////////////////////////////////////
    // ENUMS /////////////////////////////////////////////////////////////////
    // TYPES /////////////////////////////////////////////////////////////////
    // STATIC ////////////////////////////////////////////////////////////////
    // LIFECYCLE /////////////////////////////////////////////////////////////
    // METHODS ///////////////////////////////////////////////////////////////

		//////////////////////////////////////////////////////////////////////
		/** Main thread method.
		**////////////////////////////////////////////////////////////////////
		void run();

    // DATA MEMBERS //////////////////////////////////////////////////////////

private: //-------------------------------------------------------------------

    // EXCEPTIONS ////////////////////////////////////////////////////////////
    // ENUMS /////////////////////////////////////////////////////////////////
    // TYPES /////////////////////////////////////////////////////////////////
    // LIFECYCLE /////////////////////////////////////////////////////////////
    // METHODS ///////////////////////////////////////////////////////////////

		//////////////////////////////////////////////////////////////////////
		/** Create a packet in memory containing a complete message.
		**////////////////////////////////////////////////////////////////////
		void createPacket();

		//////////////////////////////////////////////////////////////////////
		/** Read the length of an incoming packet.
		 *
		 * \param numRead IN: number of bytes read already.
		 *
		 * \returns The packet length.
		**////////////////////////////////////////////////////////////////////
		int readPacketLength( int numRead );

		//////////////////////////////////////////////////////////////////////
		/** Read more bytes from the socket in order to create packets.
		 *
		 * \returns The number of read bytes.
		**////////////////////////////////////////////////////////////////////
		int readMoreBytes();

		//////////////////////////////////////////////////////////////////////
		/** Complete a packet length data. Invoked to find out the expected
		 * length of a given packet.
		 *
		 * \param numRead IN: number of already read bytes
		**////////////////////////////////////////////////////////////////////
		int completeLength( int numRead );

		//////////////////////////////////////////////////////////////////////
		/** Append data to the current packet.
		 *
		 * \param numRead IN: number of already read bytes.
		**////////////////////////////////////////////////////////////////////
		void addBytesToPacket( int numRead );

		//////////////////////////////////////////////////////////////////////
		/** Complete a packet given the expected length, having some bytes
		 * extra which do not fit into this packet but the next one.
		 *
		 * \param numRead IN: number of already read bytes.
		 * \param readLength IN: length of the packet.
		 *
		 * \returns The number of bytes left.
		**////////////////////////////////////////////////////////////////////
		int addBytesToPacketExtra( int numRead, int readLength );

		//////////////////////////////////////////////////////////////////////
		/** Complete a packet given the expected length, It is assumed that
		 * the number of available bytes complete a packet and nothing remains
		 * out.
		 *
		 * \param numRead IN: number of already read bytes.
		**////////////////////////////////////////////////////////////////////
		void addBytesToPacketFit( int numRead );

		//////////////////////////////////////////////////////////////////////
		/** Determines which operation should be done (addBytesToPacket,
		 * addBytesToPacketExtra, addBytesToPacketFit) depending on the amount
		 * of bytes read and the expected length of the packet.
		 *
		 * \param numRead IN: number of already read bytes.
		 *
		 * \returns The number of bytes read after the operation.
		**////////////////////////////////////////////////////////////////////
		int processReadBytes( int numRead );

		//////////////////////////////////////////////////////////////////////
		/** Once a packet is complete, create a SPELL message out of it and
		 * send it to the listener via the IPC interface.
		**////////////////////////////////////////////////////////////////////
		void dispatch();

		//////////////////////////////////////////////////////////////////////
		/** Report ERRNO
		**////////////////////////////////////////////////////////////////////
		void reportError();

    // DATA MEMBERS //////////////////////////////////////////////////////////

		/** Stores the expected packet length */
		unsigned int m_totalPacketLength;
		/** Holds the bytes read from socket */
		char m_buffer[IPC_BUFFER_SIZE];
		/** Holds the packet being created */
		std::string m_packet;
		/** Holds the position of the last byte read */
		char* m_lastByte;
		/** Holds the interface identifier */
		std::string m_ifcName;
		/** Holds the peer key */
		int m_key;
		/** Holds the socket file descriptor */
		SPELLsocket& m_socket;
		/** Holds a reference to the IPC interface */
		SPELLipcInterface&     m_interface;
		/** Holds a reference to the IPC channel */
		SPELLipcChannel& m_channel;
		/** Synchronization mutex */
		SPELLmutex m_lock;
		/** True if connected and reading data */
		bool m_connected;
		/** True if EOC requested */
		bool m_requestedEOC;
};

/*@}*/
#endif
