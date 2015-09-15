// ################################################################################
// FILE       : SPELLipcChannel.C
// DATE       : Mar 17, 2011
// PROJECT    : SPELL
// DESCRIPTION: Implementation of a peer ipc channel
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
#include "SPELL_IPC/SPELLipcChannel.H"
// Project includes --------------------------------------------------------
#include "SPELL_UTIL/SPELLlog.H"
#include "SPELL_UTIL/SPELLutils.H"
#include "SPELL_SYN/SPELLmonitor.H"
#include "SPELL_SYN/SPELLsyncError.H"
// System includes ---------------------------------------------------------


// DEFINES /////////////////////////////////////////////////////////////////

#define NAME std::string("[ CHANNEL-KEY(") + ISTR(m_key) + ")-" + m_name + " ] "

//=============================================================================
// CONSTRUCTOR: SPELLipcChannel::SPELLipcChannel
//=============================================================================
SPELLipcChannel::SPELLipcChannel( const std::string& name, SPELLsocket* socket, int key, SPELLipcInterface& ifc )
    : m_name(name),
      m_ifc(ifc),
      m_socket(socket),
      m_key(key),
      m_input(NULL),
      m_output(NULL)
{
    DEBUG( NAME + "Channel created");
    m_connected = false;
};

//=============================================================================
// DESTRUCTOR: SPELLipcChannel::~SPELLipcChannel
//=============================================================================
SPELLipcChannel::~SPELLipcChannel()
{
    DEBUG( NAME + "Destroying channel");
	if (m_socket)
	{
		delete m_socket;
		m_socket = NULL;
	}
	if (m_output)
	{
		m_output->join();
		delete m_output;
		m_output = NULL;
	}
	if (m_input)
	{
		m_input->join();
		delete m_input;
		m_input = NULL;
	}
    DEBUG( NAME + "Channel destroyed");
}

//=============================================================================
// METHOD: SPELLipcChannel::connect
//=============================================================================
void SPELLipcChannel::connect()
{
    DEBUG( NAME + "Connect");
	m_output = createOutput();
	m_input = createInput();
    m_input->start();
    setConnected(true);
    DEBUG( NAME + "Connect done");
}

//=============================================================================
// METHOD: SPELLipcChannel::setConnected()
//=============================================================================
void SPELLipcChannel::setConnected( bool connected )
{
	SPELLmonitor m(m_dataLock);
    m_connected = connected;
}

//=============================================================================
// METHOD: SPELLipcChannel::isConnected()
//=============================================================================
bool SPELLipcChannel::isConnected()
{
	SPELLmonitor m(m_dataLock);
    return m_connected;
}

//=============================================================================
// METHOD: SPELLipcChannel::createOutput
//=============================================================================
SPELLipcOutput* SPELLipcChannel::createOutput()
{
    DEBUG( NAME + "Create output");
	return new SPELLipcOutput(m_name, *m_socket, m_key, *this );
}

//=============================================================================
// METHOD: SPELLipcChannel::createInput
//=============================================================================
SPELLipcInput* SPELLipcChannel::createInput()
{
    DEBUG( NAME + "Create input");
	return new SPELLipcInput(m_name, *m_socket, m_key, *this, m_ifc );
}

//=============================================================================
// METHOD: SPELLipcChannel::disconnect
//=============================================================================
void SPELLipcChannel::disconnect()
{
	if (m_connected)
	{
		DEBUG( NAME + "Disconnect");
		m_connected = false;
		m_output->disconnect(true);
		m_input->disconnect();
		m_socket->close();
		DEBUG( NAME + "Disconnect finished");
	}
}

//=============================================================================
// METHOD: SPELLipcChannel::requestStarted()
//=============================================================================
void SPELLipcChannel::requestStarted( const std::string& reqId )
{
	SPELLmonitor m(m_dataLock);
    m_outgoingRequests.push_back(reqId);
}


//=============================================================================
// METHOD: SPELLipcChannel::requestFinished
//=============================================================================
void SPELLipcChannel::requestFinished( const std::string& reqId )
{
    SPELLmonitor m(m_dataLock);
    std::vector<std::string>::iterator it = std::find(m_outgoingRequests.begin(), m_outgoingRequests.end(), reqId);
    if (it != m_outgoingRequests.end())
    {
    	m_outgoingRequests.erase(it);
    }
}

//=============================================================================
// METHOD: SPELLipcChannel::getOngoingRequests
//=============================================================================
std::vector<std::string> SPELLipcChannel::getOngoingRequests()
{
	return m_outgoingRequests;
}

//=============================================================================
// METHOD: SPELLipcChannel::inputFailed
//=============================================================================
void SPELLipcChannel::inputFailed( int errNo, const std::string& error )
{
	if (isConnected())
	{
		setConnected(false);
		DEBUG( NAME + "Force disconnect, input failed");
		m_output->disconnect(false);
		m_socket->shutdown();
		m_socket->close();
		DEBUG( NAME + "Disconnected, reporting interface");
		m_ifc.connectionLost(m_key, errNo, error);
	}
}

//=============================================================================
// METHOD: SPELLipcChannel::outputFailed
//=============================================================================
void SPELLipcChannel::outputFailed( int errNo, const std::string& error )
{
	if (isConnected())
	{
		setConnected(false);
		DEBUG( NAME + "Force disconnect, output failed");
		m_input->disconnect();
		m_socket->shutdown();
		m_socket->close();
		DEBUG( NAME + "Disconnected, reporting interface");
		m_ifc.connectionLost(m_key, errNo, error);
	}
}

//=============================================================================
// METHOD: SPELLipcChannel::requestedEOC
//=============================================================================
void SPELLipcChannel::requestedEOC()
{
	if (isConnected())
	{
		setConnected(false);
		DEBUG( NAME + "Peer requested end of conversation");
		m_output->disconnect(false);
		m_socket->shutdown();
		DEBUG( NAME + "Disconnected, reporting interface");
		m_ifc.connectionClosed(m_key);
	}
}
