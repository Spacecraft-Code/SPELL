// ################################################################################
// FILE       : SPELLipcDataChunk.H
// DATE       : Mar 17, 2011
// PROJECT    : SPELL
// DESCRIPTION: Mechanism for transferring big data
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

#ifndef __SPELL_IPC_DATA_CHUNK_H__
#define __SPELL_IPC_DATA_CHUNK_H__

// FILES TO INCLUDE ////////////////////////////////////////////////////////
// System includes ---------------------------------------------------------
// Local includes ----------------------------------------------------------
// Project includes --------------------------------------------------------
#include "SPELL_SYN/SPELLmutex.H"

/** \addtogroup SPELL_IPC */
/*@{*/


// FORWARD REFERENCES //////////////////////////////////////////////////////
// TYPES ///////////////////////////////////////////////////////////////////
// DEFINES /////////////////////////////////////////////////////////////////

//////////////////////////////////////////////////////////////////////////////
/**
 ** \brief Used to manage splitted SPELL messages.
 **
 ** \par Description and usage:
 **
 ** 	Used when a SPELL message length exceeds the limit set by the IPC
 ** 	interface. In these cases, the message is split in chunks of
 ** 	same length, which are sent/received sequentially. This class is
 ** 	in charge of controlling the chunk generation sequence for the
 ** 	sender.
 **
**////////////////////////////////////////////////////////////////////////////
class SPELLipcDataChunk
{
public: //--------------------------------------------------------------------

    // EXCEPTIONS ////////////////////////////////////////////////////////////
    // ENUMS /////////////////////////////////////////////////////////////////
    // TYPES /////////////////////////////////////////////////////////////////
		/** Represents a list of data pieces */
    	typedef std::vector<std::string> DataList;
    // LIFECYCLE /////////////////////////////////////////////////////////////

		//////////////////////////////////////////////////////////////////////
		/** Constructor.
		 *
		 * \param chunkSize IN: the side in bytes of the chunks.
		**////////////////////////////////////////////////////////////////////
		SPELLipcDataChunk( unsigned int chunkSize );

		//////////////////////////////////////////////////////////////////////
		/** Destructor.
		**////////////////////////////////////////////////////////////////////
		~SPELLipcDataChunk();

    // METHODS ///////////////////////////////////////////////////////////////

		//////////////////////////////////////////////////////////////////////
		/** Start chunking a message.
		 *
		 * \param id IN: identifier of the chunk set.
		 * \param data IN: total amount of data to split.
		 *
		 * \returns Number of chunks generated.
		**////////////////////////////////////////////////////////////////////
		int startChunks( const std::string& id, const DataList& data );

		//////////////////////////////////////////////////////////////////////
		/** Finish the chunk management for the given identifier.
		 *
		 * \param id IN: identifier of the chunk set.
		**////////////////////////////////////////////////////////////////////
		void endChunks( const std::string& id );

		//////////////////////////////////////////////////////////////////////
		/** Get the size of the given chunk set.
		 *
		 * \param id IN: identifier of the chunk set.
		 *
		 * \return The size of the set (number of chunks)
		**////////////////////////////////////////////////////////////////////
		int getSize( const std::string& id);

		//////////////////////////////////////////////////////////////////////
		/** Get the given chunk from the given set.
		 *
		 * \param id IN: identifier of the chunk set.
		 * \param num IN: index of the chunk.
		 *
		 * \return The chunk data list
		**////////////////////////////////////////////////////////////////////
		DataList getChunk( const std::string& id, unsigned int num );

protected: //-----------------------------------------------------------------

    // EXCEPTIONS ////////////////////////////////////////////////////////////
    // ENUMS /////////////////////////////////////////////////////////////////
    // TYPES /////////////////////////////////////////////////////////////////
    // LIFECYCLE /////////////////////////////////////////////////////////////
    // METHODS ///////////////////////////////////////////////////////////////
    // DATA MEMBERS //////////////////////////////////////////////////////////

private: //-------------------------------------------------------------------

    // EXCEPTIONS ////////////////////////////////////////////////////////////
    // ENUMS /////////////////////////////////////////////////////////////////
    // TYPES /////////////////////////////////////////////////////////////////
		/** Represents a list of data lists (chunks) */
		typedef std::vector<DataList*> ListOfDataList;
		/** Map of chunk-lists indexed by identifier */
		typedef std::map<std::string,ListOfDataList*> DataMap;
    // LIFECYCLE /////////////////////////////////////////////////////////////
    // METHODS ///////////////////////////////////////////////////////////////
    // DATA MEMBERS //////////////////////////////////////////////////////////
		/** Synchronization mutex */
		SPELLmutex        m_lock;
		/** All chunks */
		DataMap           m_chunks;
		/** Programmed chunk size */
		unsigned int      m_chunkSize;
};

/*@}*/
#endif

