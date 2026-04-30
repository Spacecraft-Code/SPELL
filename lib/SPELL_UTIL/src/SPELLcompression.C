// ################################################################################
// FILE       : SPELLcompression.C
// DATE       : Mar 17, 2011
// PROJECT    : SPELL
// DESCRIPTION: Implementation of the compression utility
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
#include "SPELL_UTIL/SPELLbase.H"
#include "SPELL_UTIL/SPELLutils.H"
#include "SPELL_UTIL/SPELLerror.H"
#include "SPELL_UTIL/SPELLcompression.H"
// Project includes --------------------------------------------------------
// System includes ---------------------------------------------------------

// DEFINES /////////////////////////////////////////////////////////////////
#define COMPRESSION_CHUNK_SIZE 512
#define COMPRESSION_BUFFER_SIZE 32768

#define DEBUG_COMPRESSION false

#define CDEBUG(x) if(DEBUG_COMPRESSION) std::cout << x << std::endl


//=============================================================================
// CONSTRUCTOR : SPELLcompression::SPELLcompression
//=============================================================================
SPELLcompression::SPELLcompression( std::string data )
{
	// Initialize the work buffers
	m_processInput = new char[COMPRESSION_CHUNK_SIZE];
	m_processOutput = new char[COMPRESSION_CHUNK_SIZE];

	// Initialize common stream settings
	m_stream.zalloc = Z_NULL;
	m_stream.zfree = Z_NULL;
	m_stream.opaque = Z_NULL;

	// Initialize counters
	m_totalReadBytes = 0;
	m_totalWriteBytes = 0;

	// Store the input data and reset output
	m_inputData = data;
	m_outputData.str("");
	m_totalInputLen = data.size();
}

//=============================================================================
// DESTRUCTOR : SPELLcompression::~SPELLcompression
//=============================================================================
SPELLcompression::~SPELLcompression()
{
	delete[] m_processInput;
	delete[] m_processOutput;
}

//=============================================================================
// METHOD : SPELLcompression::compress
//=============================================================================
std::string SPELLcompression::compress()
{
	int ret, flush;

	//CDEBUG("Original data: " << SPELLutils::dumpString(m_inputData) );
	//CDEBUG("initializing deflate");
	//CDEBUG("total input " << m_totalInputLen);
	//CDEBUG("remaining   " << getRemaining());

	// Initialize the deflate algorithm
	int windowBits = 15 + 16;
	ret = deflateInit2( &m_stream, Z_BEST_SPEED, Z_DEFLATED, windowBits, 8, Z_DEFAULT_STRATEGY );
	if (ret != Z_OK)
	{
		THROW_FATAL_EXCEPTION("Unable to compress", "Cannot initialize deflate algorithm", SPELL_ERROR_UTILS);
	}

	// Compress until the end of the input data
	do
	{
		// Copy the next bytes on the input buffer and returns the number of read bytes
		m_stream.avail_in = readMoreBytes();

		flush = (getRemaining()==0) ? Z_FINISH : Z_NO_FLUSH;
		m_stream.next_in = (Bytef*) m_processInput;

		// Run deflate on input until output buffer not full, finish compression
		// if all of source has been read in
		do
		{
			m_stream.avail_out = COMPRESSION_CHUNK_SIZE;
			m_stream.next_out = (Bytef*) m_processOutput;
        	//CDEBUG("calling deflate");
			ret = deflate( &m_stream, flush );
        	//CDEBUG("deflate result " << ret);
			assert( ret != Z_STREAM_ERROR );
			unsigned int numCompressed = COMPRESSION_CHUNK_SIZE - m_stream.avail_out;
        	//CDEBUG("number of deflated bytes " << numCompressed);
			writeNextBytes(numCompressed);
		}
		while( m_stream.avail_out == 0 );
		assert(m_stream.avail_in == 0);

    	//CDEBUG("deflate cycle done");

	}
	while( flush != Z_FINISH );
	assert( ret == Z_STREAM_END );


	deflateEnd(&m_stream);

	//CDEBUG("Compressed data: " << SPELLutils::dumpString(m_outputData.str()));
	//CDEBUG("Total written " << m_totalWriteBytes);

	return m_outputData.str();
}

//=============================================================================
// METHOD : SPELLcompression::uncompress
//=============================================================================
std::string SPELLcompression::uncompress()
{
	int ret;

	m_stream.avail_in = 0;
	m_stream.next_in = Z_NULL;

	//CDEBUG("Original data: " << SPELLutils::dumpString(m_inputData) );
	//CDEBUG("initializing inflate");
	//CDEBUG("total input " << m_totalInputLen);
	//CDEBUG("remaining   " << getRemaining());

	int windowBits = 15 + 16;
	ret = inflateInit2( &m_stream, windowBits );
	if (ret != Z_OK)
	{
		THROW_FATAL_EXCEPTION("Unable to uncompress", "Cannot initialize inflate algorithm", SPELL_ERROR_UTILS);
	}

    // decompress until deflate stream ends or end of file
    do
    {
        m_stream.avail_in = readMoreBytes();
        if (m_stream.avail_in == 0)
		{
        	//CDEBUG("no more to read, break");
        	break;
		}
        m_stream.next_in = (Bytef*) m_processInput;

        // run inflate() on input until output buffer not full
        do
        {
            m_stream.avail_out = COMPRESSION_CHUNK_SIZE;
            m_stream.next_out = (Bytef*) m_processOutput;
        	//CDEBUG("calling inflate");
            ret = inflate(&m_stream, Z_NO_FLUSH);
        	//CDEBUG("inflate result " << ret);
            assert(ret != Z_STREAM_ERROR);  // state not clobbered
            switch (ret)
            {
            case Z_NEED_DICT:
            case Z_DATA_ERROR:
            case Z_MEM_ERROR:
                inflateEnd(&m_stream);
                THROW_FATAL_EXCEPTION("Unable to uncompress", "Inflate failed", SPELL_ERROR_UTILS);
                break;
            }
            unsigned int numExpanded = COMPRESSION_CHUNK_SIZE - m_stream.avail_out;
        	//CDEBUG("number of inflated bytes " << numExpanded);
            writeNextBytes(numExpanded);
        }
        while (m_stream.avail_out == 0);

    	//CDEBUG("inflate cycle done");

    // done when inflate() says it's done
    }
    while (ret != Z_STREAM_END);

	//CDEBUG("Uncompressed data: " << SPELLutils::dumpString(m_outputData.str()));
	//CDEBUG("Total written " << m_totalWriteBytes);

    // clean up
    inflateEnd(&m_stream);

    return m_outputData.str();
}

//=============================================================================
// METHOD : SPELLcompression::writeNextBytes
//=============================================================================
void SPELLcompression::writeNextBytes( unsigned int len )
{
	//CDEBUG("writing bytes    : " << len);
	for( unsigned int c=0; c<len; c++)
	{
		m_outputData.sputc( m_processOutput[c] );
	}
	m_totalWriteBytes += len;
	//CDEBUG("written in total : " << m_totalWriteBytes);
}

//=============================================================================
// METHOD : SPELLcompression::getRemaining
//=============================================================================
unsigned int SPELLcompression::getRemaining()
{
	return m_totalInputLen - m_totalReadBytes;
}

//=============================================================================
// METHOD : SPELLcompression::readMoreBytes
//=============================================================================
unsigned int SPELLcompression::readMoreBytes()
{
	unsigned int numRead = -1;
	unsigned int remainingData = getRemaining();
	//CDEBUG("read more bytes (remaining: " << remainingData << ")");

	// If the read position is exactly at the end
	if ( remainingData == 0 )
	{
		//CDEBUG("  no remaining data");
		return 0;
	}
	// If the remaining bytes in the input data are less size than the chunk
	else if ( remainingData  <  COMPRESSION_CHUNK_SIZE )
	{
		// Return the remaining
		numRead = remainingData;
	}
	// If the remaining data is greather than a chunk
	else
	{
		// Just read a chunk
		numRead = COMPRESSION_CHUNK_SIZE;
	}
	// Note that start of source is positioned by m_totalReadBytes
	memcpy( m_processInput, m_inputData.c_str() + m_totalReadBytes, numRead );
	// Shift the read position the amount of bytes read
	m_totalReadBytes += numRead;
	//CDEBUG("  bytes read so far   :" << m_totalReadBytes);
	//CDEBUG("  remaining data now  :" << getRemaining());
	//CDEBUG("read bytes            :" << numRead);
	return numRead;
}
