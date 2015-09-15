// ################################################################################
// FILE       : SPELLipcOutput.C
// DATE       : Mar 17, 2011
// PROJECT    : SPELL
// DESCRIPTION: Implementation of the data writer
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
#include "SPELL_IPC/SPELLipcOutput.H"
#include "SPELL_IPC/SPELLipcError.H"
#include "SPELL_IPC/SPELLipcChannel.H"
// Project includes --------------------------------------------------------
#include "SPELL_SYN/SPELLmonitor.H"
#include "SPELL_UTIL/SPELLlog.H"
#include "SPELL_UTIL/SPELLcompression.H"
// System includes ---------------------------------------------------------


// DEFINES /////////////////////////////////////////////////////////////////

#define NAME std::string("[ OUTPUT-KEY(") + ISTR(m_key) + ")-" + m_ifcName + " ] "
#define SIZE sizeof(unsigned int)
#define MINIMUM_SIZE_FOR_COMPRESSION 512
#define PING_PERIOD_SEC 250

//=============================================================================
// FUNCTION: encodeLength
// DESCRIPTION: encode the message length into bytes
//=============================================================================
char* encodeLength( int length )
{
    char* bytes = new char[4];
    for( int i=3; i>=0; i--)
    {
        bytes[i] = (char) (length & 0xff);
        length >>= 8;
    }
    return bytes;
};


//=============================================================================
// CONSTRUCTOR: SPELLipcOutput::SPELLipcOutput
//=============================================================================
SPELLipcOutput::SPELLipcOutput( const std::string& name, SPELLsocket& skt, int myKey, SPELLipcChannel& chan )
: SPELLthread( std::string("[ IPC-OUT-") + ISTR(myKey) + "-" + name + " ]-PING" ),
  m_ifcName(name),
  m_key(myKey),
  m_socket(skt),
  m_channel(chan),
  m_connected(true)
{
    DEBUG( NAME + "Created");
    m_ping.setId("ping");
    m_ping.setType(MSG_TYPE_PING);
}

//=============================================================================
// DESTRUCTOR: SPELLipcOutput:SPELLipcOutput
//=============================================================================
SPELLipcOutput::~SPELLipcOutput()
{
    DEBUG( NAME + "Destroyed");
}

//=============================================================================
// METHOD: SPELLipcOutput::send
//=============================================================================
void SPELLipcOutput::send( SPELLipcMessage& msg )
{
    msg.setKey( m_key );
    DEBUG( NAME + "SEND " + msg.dataStr() );
    try
    {
    	std::string data = msg.data();
    	bool doCompression = (msg.getCompressed() && (data.size()>= MINIMUM_SIZE_FOR_COMPRESSION));
        if (doCompression)
        {
        	int origSize = data.size();
        	std::cerr << "Compressing message of length " << data.size() << " bytes (" << msg.getId() << ")" << std::endl;
            DEBUG("Raw data: " + SPELLutils::dumpString(data) + " size " + ISTR(data.length()) );
            SPELLcompression compressor(data);
        	data = compressor.compress();
            DEBUG("Compressed data: " + SPELLutils::dumpString(data) + " size " + ISTR(data.length()) );
            double ratio = ((double) origSize - (double) data.size())/((double)origSize) * 100.0;
        	std::cerr << "Compressed to " << data.size() << " bytes (ratio " << std::setprecision(2) << ratio << "%)" << std::endl;
        }
    	// Add compression flag
        data = (doCompression ? COMPRESSION_FLAG_TRUE_STR : COMPRESSION_FLAG_FALSE_STR) + data;

		writeData( data );
    }
    catch(SPELLipcError& err)
    {
        int errNo = errno;
        if (m_connected)
        {
            LOG_ERROR( NAME + " Connection lost at output " + std::string(err.what()) );
            LOG_ERROR( NAME + " Data being sent: " + msg.dataStr());
            m_channel.outputFailed(errNo, err.getError());
        }
        else
        {
            DEBUG( NAME + "Send failed, not connected: " + err.what() + ", errno=" + ISTR(errNo) );
        }
    }
    catch(...)
    {
        int errNo = errno;
        if (m_connected)
        {
            LOG_ERROR( NAME + " Connection lost at output, unknown error" );
            LOG_ERROR( NAME + " Data being sent: " + msg.dataStr());
            m_channel.outputFailed(errNo, "Unknown error");
        }
        else
        {
            DEBUG( NAME + "Send failed, not connected, errno=" + ISTR(errNo) );
        }
    }
    DEBUG( NAME + "SEND DONE" );
}

//=============================================================================
// METHOD: SPELLipcOutput::disconnect
//=============================================================================
void SPELLipcOutput::disconnect( bool send_eoc )
{
    if (!m_connected) return;

    DEBUG( NAME + "SPELLipcOutput disconnect");

    if (send_eoc)
    {
        LOG_INFO( NAME + "Sending EOC");
        SPELLipcMessage eoc("EOC");
        eoc.setType( MSG_TYPE_EOC );
        eoc.setKey(m_key);
        send(eoc);
        DEBUG( NAME + "EOC sent");
    }
    m_connected = false;
}

//=============================================================================
// METHOD: SPELLipcOutput::writeData
//=============================================================================
void SPELLipcOutput::writeData( std::string data )
{
    DEBUG( NAME + "Write data TRY-IN");
    SPELLmonitor m(m_lock);
    DEBUG( NAME + "Write data IN");
    if (m_connected)
    {
        unsigned int sizeData = data.size();
        //DEBUG( NAME + "Packet length " + ISTR(sizeData));

        int success = 0;

        // Take into account endianess
        std::string sbuf;
        sbuf.resize(SIZE, '\0');
        unsigned int lc = sizeData;
        for( int i=SIZE-1; i>=0; i-- )
        {
            sbuf[i] = (unsigned char)(lc & 0xFF);
            lc >>= 8;
        }

        success = m_socket.send(&sbuf[0],SIZE);

        if (success == -1)
        {
            throw SPELLipcError( NAME + "Cannot send length");
        }

        int numBytes, offset;
        numBytes = sizeData;
        success = 0;
        offset = 0;
        //DEBUG("writeData sending message: " + SPELLutils::dumpString(data));
        while( numBytes > 0 )
        {
            success = m_socket.send(&data[offset],numBytes);
            if (success == -1 || errno != 0)
            {
                throw SPELLipcError( NAME + "Error while sending data, errno " + ISTR(errno) );
            }
                
            // If there are remaining bytes, move the pointer to continue;
            numBytes -= success;
            offset += success;
        }

        m_lastSentTime.setCurrent();
    }
    DEBUG( NAME + "Write data OUT");
}

//=============================================================================
// METHOD: SPELLipcOutput::run()
//=============================================================================
void SPELLipcOutput::run()
{
	while(m_connected)
	{
		SPELLtime currentTime;
		SPELLtime lastTime;
		SPELLtime delta;
		{
			SPELLmonitor m(m_lock);
			lastTime = m_lastSentTime;
		}
		delta = currentTime - lastTime;
		if (delta.getSeconds() > PING_PERIOD_SEC)
		{
			std::cerr << "ping [" << NAME << "] " << currentTime.toString() << std::endl;
			try
			{
				send(m_ping);
			}
			catch(...)
			{
		        DEBUG( NAME + "Abort ping loop");
				return;
			}
		}
        DEBUG( NAME + "1");
		usleep(2000000); // 2 seconds
        DEBUG( NAME + "2");
	}
    DEBUG( NAME + "Finish ping loop");
}

