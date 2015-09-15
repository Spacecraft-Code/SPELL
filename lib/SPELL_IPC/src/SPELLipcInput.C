// ################################################################################
// FILE       : SPELLipcInput.C
// DATE       : Mar 17, 2011
// PROJECT    : SPELL
// DESCRIPTION: Implementation of the data reader
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
#include "SPELL_IPC/SPELLipcError.H"
#include "SPELL_IPC/SPELLipcInput.H"
#include "SPELL_IPC/SPELLipcChannel.H"
// Project includes --------------------------------------------------------
#include "SPELL_SYN/SPELLmonitor.H"
#include "SPELL_UTIL/SPELLlog.H"
#include "SPELL_UTIL/SPELLcompression.H"
// System includes ---------------------------------------------------------

// DEFINES /////////////////////////////////////////////////////////////////

#define NAME std::string("[ INPUT-KEY(") + ISTR(m_key) + ")-" + m_ifcName + " ] "
#define SIZE sizeof(unsigned int)

// store the diagnostics config
#pragma GCC diagnostic push
//temporarily disable the warning for signed/unsigned mismatch
#pragma GCC diagnostic ignored "-Wsign-compare"

//=============================================================================
// CONSTRUCTOR: SPELLipcInput::SPELLipcInput
//=============================================================================
SPELLipcInput::SPELLipcInput( const std::string& name, SPELLsocket& skt, int peerKey, SPELLipcChannel& chan,  SPELLipcInterface& ifc )
    : SPELLthread(name),
      m_socket(skt),
      m_interface(ifc),
      m_channel(chan),
      m_lock()
{
    m_ifcName = name;
    m_key = peerKey;
    m_connected = true;
    DEBUG( NAME + "Created");
}

//=============================================================================
// DESTRUCTOR: SPELLipcInput:SPELLipcInput
//=============================================================================
SPELLipcInput::~SPELLipcInput()
{
    DEBUG( NAME + "Destroyed");
}


//=============================================================================
// METHOD: SPELLipcInput::readPacketLength
//=============================================================================
int SPELLipcInput::readPacketLength( int numRead )
{
    assert(numRead >= SIZE);
    //DEBUG(NAME << "Reading length");

    // The buffer shall contain the length in the first SIZE bytes

    // We shall take into account endianess
    unsigned int length = 0;
    for( unsigned int c = 0; c<SIZE; c++ )
    {
        length = (length<<8) + (m_buffer[c] < 0 ? m_buffer[c] + 256 : m_buffer[c]);
    }
    m_totalPacketLength = length;

    //DEBUG( NAME + " readPacketLength length: " + ISTR(m_totalPacketLength) + " " + SPELLutils::dumpString(tmp));
    assert(m_totalPacketLength < 9999000);

    // Remove the length bytes from buffer
    for( unsigned int byte=0; byte<(numRead-SIZE); byte++)
    {
        memcpy(m_buffer+byte,m_buffer+SIZE+byte,1);
    }
    // Reset the SIZE latest bytes
    memset(m_buffer+numRead-SIZE, 0, SIZE);
    // Reduce the number of read bytes
    numRead -= SIZE;
    //DEBUG( NAME << "Bytes after reading length: " << numRead);
    return numRead;
}

//=============================================================================
// METHOD: SPELLipcInput::readMoreBytes
//=============================================================================
int SPELLipcInput::readMoreBytes()
{
    int numRead = 0;
    memset ( m_buffer, 0, IPC_BUFFER_SIZE + 1);

    while(1)
    {
        bool dataIn = m_socket.waitData(10000);//10 sec
        if (dataIn)
        {
        	int retries = 0;
        	while(1)
        	{
				numRead = m_socket.receive( m_buffer, IPC_BUFFER_SIZE );
				int errNo = errno;
				if (numRead == 0 && m_connected)
				{
					LOG_WARN( NAME + "WARNING: retrying read...");
					retries++;
					if (retries ==3) throw SPELLipcError(NAME + "Peer has disconnected (errno " + ISTR(errNo) + ")", errNo );
				}
				else break;
        	}
            return numRead;
        }
        if (!isConnected())
        {
            return 0;
        }
    }
    return -1;
}

//=============================================================================
// METHOD: SPELLipcInput::completeLength
//=============================================================================
int SPELLipcInput::completeLength( int numRead )
{
    int more = m_socket.receive( m_buffer+numRead, IPC_BUFFER_SIZE-numRead );
    int errNo = errno;
    assert( more >= 0 );
    if (more == 0 && m_connected)
    {
        DEBUG( NAME + "Read 0, peer has disconnected");
        // When we receive zero it means that the peer has invoked
        // shutdown(). We shall do the same on our side. The IPC
        // interface is in charge of that, we shall not touch the
        // socket file descriptor here in SPELLipcInput. Just finish the
        // loop.
        throw SPELLipcError( NAME + "Peer has disconnected (errno " + ISTR(errNo) + ")", errNo);
    }
    return more;
}

//=============================================================================
// METHOD: SPELLipcInput::addBytesToPacket
//=============================================================================
void SPELLipcInput::addBytesToPacket( int numRead )
{
    assert( m_lastByte != NULL );
    //DEBUG(NAME << "[1] Adding " << numRead << " bytes to packet");
    memcpy(m_lastByte,m_buffer,numRead);
    m_lastByte += numRead;
    //DEBUG(NAME << "[1] Read so far: " << (m_lastByte-m_packet) << " bytes");
    //DEBUG(NAME << "[1] No bytes remaining");
}

//=============================================================================
// METHOD: SPELLipcInput::addBytesToPacketExtra
//=============================================================================
int SPELLipcInput::addBytesToPacketExtra( int numRead, int readLength )
{
    int bytesToRead = (m_totalPacketLength - readLength);
    //DEBUG(NAME << "[2] Second case, there are " << numRead << " bytes and packet is " << m_totalPacketLength << " bytes long");
    //DEBUG(NAME << "[2] So far I read " << readLength << " so I need to read " << bytesToRead << " bytes");
    memcpy(m_lastByte,m_buffer,bytesToRead);

    // Dispatch the completed packet
    //DEBUG( NAME << "[2] Dispatch")
    dispatch();

    // Remove the length bytes from buffer
    for( int byte=0; byte<(numRead-bytesToRead); byte++)
    {
        memcpy(m_buffer+byte,m_buffer+bytesToRead+byte,1);
    }
    // Reset the bytesToRead latest bytes
    memset(m_buffer+numRead-bytesToRead, 0, bytesToRead);
    // Reduce the number of read bytes
    numRead -= bytesToRead;
    //DEBUG(NAME << "[2] Remaining: " << numRead << " bytes: '" << std::string(m_buffer) << "'");

    return numRead;
}

//=============================================================================
// METHOD: SPELLipcInput::addBytesToPacketFit
//=============================================================================
void SPELLipcInput::addBytesToPacketFit( int numRead )
{
    //DEBUG( NAME << "[3] Add packets")
    memcpy(m_lastByte,m_buffer,numRead);
    // Dispatch the completed packet
    //DEBUG( NAME << "[3] Dispatch")
    dispatch();
    //DEBUG(NAME << "[3] No bytes remaining");
}

//=============================================================================
// METHOD: SPELLipcInput::createPacket
//=============================================================================
void SPELLipcInput::createPacket()
{
    assert( m_packet.empty() );
    //DEBUG(NAME << "Creating packet of length " << m_totalPacketLength);
    assert(m_totalPacketLength>0);
    // Create the packet
    // We need one byte more for the final \0 character
    m_packet.resize(m_totalPacketLength);
    m_lastByte = &m_packet[0];
}

//=============================================================================
// METHOD: SPELLipcInput::processReadBytes
//=============================================================================
int SPELLipcInput::processReadBytes( int numRead )
{
    // Copy the bytes to the packet latest position
    // This is the packet length so fat
    int readLength = m_lastByte - &m_packet[0];

    //DEBUG(NAME << "+---------------------------------------------------------+")
    //DEBUG(NAME << "Incoming bytes: " << numRead);
    //DEBUG(NAME << "Read so far: " << readLength);

    // First case: the number of read bytes does not complete a packet yet
    if ( numRead < (m_totalPacketLength - readLength))
    {
        addBytesToPacket( numRead );
        numRead = 0;
    }
    // Second case: the number of read bytes is bigger than the number of bytes
    // required to complete the packet
    else if ( numRead > (m_totalPacketLength - readLength))
    {
        numRead = addBytesToPacketExtra( numRead, readLength );
    }
    // Third case: the bytes comming are exactly the amount needed
    else if (numRead == (m_totalPacketLength - readLength))
    {
        addBytesToPacketFit( numRead );
        numRead = 0;
    }
    //DEBUG(NAME << "+---------------------------------------------------------+")
    return numRead;
}

//=============================================================================
// METHOD: SPELLipcInput::run
//=============================================================================
void SPELLipcInput::run()
{
    DEBUG( NAME + "start");

    int numRead = 0;
    m_packet = "";
    m_lastByte = &m_packet[0];
    m_totalPacketLength = 0;
    m_requestedEOC = false;

    DEBUG( NAME + "Setting interface ready");
    m_interface.setReady();

    DEBUG( NAME + "Entering loop");

    while(isConnected())
    {
        try
        {
            // Read bytes on buffer only if the buffer is empty
            if (numRead==0)
            {
                numRead = readMoreBytes();
                // If we did not read anything, terminate
                if (numRead == 0)
                {
                    break;
                }
            }
            else if ( numRead < SIZE)
            {
                numRead += completeLength( numRead );
            }

//            if (isConnected() && (numRead==-1 || errno != 0))
//            {
//            	int err = errno;
//                LOG_ERROR( NAME + "Error on input, errno " + ISTR(err));
//                throw SPELLipcError( NAME + "Error in socket input, errno="+ ISTR(err), err);
//            }

            if (numRead == -1)
			{
            	int err = errno;
            	LOG_ERROR( NAME + "Error on input, errno " + ISTR(err) );
            	return;
			}

            if (m_totalPacketLength == 0)
            {
                // Once we have bytes, if there is no packet length defined yet
                if ( numRead>=SIZE )
                {
                    numRead = readPacketLength( numRead );
                }
            }
            else
            {
                // If we have bytes to read
                if ( numRead>0 && isConnected() )
                {
                    //DEBUG(NAME << "Reading bytes for packet");
                    // If we still dont have a packet
                    if (m_packet.empty())
                    {
                        createPacket();
                    }
                    numRead = processReadBytes( numRead );
                }
            }
        }
        catch( SPELLipcError& ex )
        {
			switch(ex.getCode())
			{
			case IPC_ERROR_UNKNOWN_MSG_TYPE:
				{
					LOG_ERROR( NAME + "Unable to process packet. " + ex.getError())
					DEBUG("Packet size: " + ISTR(m_packet.size()));
					DEBUG("Packet expected length: " + ISTR(m_totalPacketLength));
					DEBUG(SPELLutils::dumpString(m_packet));
					// Remove all packet data and continue processing
					if (!m_packet.empty())
					{
						m_packet = "";
						m_lastByte = NULL;
						m_totalPacketLength = 0;
					}
					break;
				}
			default:
				{
					if (m_connected)
					{
						LOG_ERROR( NAME + "Fatal error while processing data. " + ex.getError());
						LOG_ERROR( NAME + "ERRNO " + ISTR(ex.getCode()));
						m_connected = false;
						m_channel.inputFailed(ex.getCode(), ex.getError());
						return;
					}
					break;
				}
			}
        }

    }
    DEBUG( NAME + "interface end");
    if (m_requestedEOC) m_channel.requestedEOC();
}

//=============================================================================
// METHOD: SPELLipcInput::getConnected
//=============================================================================
bool SPELLipcInput::isConnected()
{
    return m_connected;
}

//=============================================================================
// METHOD: SPELLipcInput::disconnect
//=============================================================================
void SPELLipcInput::disconnect()
{
    // Just change the connected flag so that the input thread finishes.
    // We shall not disconnect the socket since it is the IPC interface
    // who is in charge of creating and closing the sockets.

    if (!m_connected) return;

    DEBUG( NAME + "Disconnect input");
    m_connected = false;
    usleep(25000);
    DEBUG( NAME + "Disconnected");
}

//=============================================================================
// METHOD: SPELLipcInput::readData
//=============================================================================
void SPELLipcInput::dispatch()
{
	DEBUG( NAME + " Dispatch TRY IN");
    if (!m_connected) return;
    //DEBUG( NAME + "Dispatching: '" + m_packet + "'");
    assert( m_packet.size() == m_totalPacketLength );
    SPELLipcMessage msg;
    //DEBUG( NAME + "Created message, processing data");

    std::string dataForMsg = "";

    //DEBUG("Raw data: " + SPELLutils::dumpString(m_packet))

    bool compression = false;

    if (m_packet[0] == COMPRESSION_FLAG_TRUE)
    {
    	compression = true;
    	// Without the leading character
    	SPELLcompression decompressor( m_packet.substr(1,m_packet.size()-1) );
    	dataForMsg = decompressor.uncompress();
        //DEBUG("Uncompressed data: " + SPELLutils::dumpString(dataForMsg) );
    }
    else
    {
    	dataForMsg = m_packet.substr(1,m_packet.size()-1);
    }

	msg.fromData( dataForMsg );
	msg.setCompressed(compression);
    msg.setKey(m_key);

    DEBUG( NAME + "DISPATCH: " + msg.dataStr());

    switch(msg.getType())
    {
    case MSG_TYPE_PING:
    	break;
    case MSG_TYPE_RESPONSE:
    case MSG_TYPE_ERROR:
        m_interface.incomingResponse(msg);
        break;
    case MSG_TYPE_REQUEST:
        m_interface.incomingRequest(msg);
        break;
    case MSG_TYPE_PROMPT:
    case MSG_TYPE_NOTIFY:
    case MSG_TYPE_ONEWAY:
    case MSG_TYPE_NOTIFY_ASYNC:
    case MSG_TYPE_WRITE:
        m_interface.incomingMessage(msg);
        break;
    case MSG_TYPE_EOC:
        DEBUG(NAME + "Received EOC");
        m_connected = false;
        m_requestedEOC = true;
        return;
    default:
    	if ( (msg.getType()) < MessageType::NumTypes)
    	{
            LOG_ERROR( NAME + " Unprocessed message type: " + MessageType::TypeStr[msg.getType()] );
    	}
    	else
    	{
    		std::string msgType = ISTR(msg.getType());
            LOG_ERROR( NAME + "Unknown message type: " + ISTR(msg.getType()) );
    	}
        LOG_ERROR( NAME + " Message: " + dataForMsg );
    	break;
    }
    m_packet = "";
    m_lastByte = NULL;
    m_totalPacketLength = 0;
	DEBUG( NAME + "DISPATCH OUT");
}

#pragma GCC diagnostic pop // restore diagnostics mode
