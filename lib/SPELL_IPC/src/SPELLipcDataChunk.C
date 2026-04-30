// ################################################################################
// FILE       : SPELLipcDataChunk.C
// DATE       : Mar 17, 2011
// PROJECT    : SPELL
// DESCRIPTION: Implementation of the IPC data chunker
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
#include "SPELL_IPC/SPELLipcDataChunk.H"
// Project includes --------------------------------------------------------
#include "SPELL_UTIL/SPELLlog.H"
#include "SPELL_UTIL/SPELLutils.H"
#include "SPELL_SYN/SPELLmonitor.H"
// System includes ---------------------------------------------------------


// DEFINES /////////////////////////////////////////////////////////////////

//=============================================================================
// CONSTRUCTOR: SPELLipcDataChunk::SPELLipcDataChunk
//=============================================================================
SPELLipcDataChunk::SPELLipcDataChunk( unsigned int chunkSize )
{
    m_chunkSize = chunkSize;
};

//=============================================================================
// DESTRUCTOR: SPELLipcDataChunk::~SPELLipcDataChunk
//=============================================================================
SPELLipcDataChunk::~SPELLipcDataChunk()
{
    DataMap::iterator it;
    for( it = m_chunks.begin(); it != m_chunks.end(); it++)
    {
        ListOfDataList* chunks = it->second;
        ListOfDataList::iterator lit;
        for( lit = chunks->begin(); lit != chunks->end(); lit++)
        {
            delete (*lit);
        }
        chunks->clear();
        delete chunks;
    }
    m_chunks.clear();
}

//=============================================================================
// METHOD: SPELLipcDataChunk::
//=============================================================================
int SPELLipcDataChunk::startChunks( const std::string& id, const DataList& data )
{
    SPELLmonitor m(m_lock);

    if (data.size()<=m_chunkSize) return 0;

    ListOfDataList* chunks = new ListOfDataList();

    DataList::const_iterator it;
    DataList::const_iterator end = data.end();
    unsigned int lineCount = 0;

    DataList* chunk = new DataList();

    DEBUG("[IPCDC] Start chunks for " + id);
    for( it = data.begin(); it != end; it++)
    {
        chunk->push_back( (*it) );
        lineCount++;
        if (lineCount==m_chunkSize)
        {
            chunks->push_back( chunk );
            chunk = new DataList();
            lineCount = 0;
        }
    }
    // Add the last partial chunk if any
    if (chunk->size()>0)
    {
        chunks->push_back( chunk );
    }
    m_chunks[id] = chunks;
    int totalChunks = chunks->size();
    DEBUG("[IPCDC] Total chunks: " + ISTR(totalChunks));
    return totalChunks;

}

//=============================================================================
// METHOD: SPELLipcDataChunk::
//=============================================================================
void SPELLipcDataChunk::endChunks( const std::string& id )
{
    DataMap::iterator it = m_chunks.find(id);
    if (it != m_chunks.end())
    {
        ListOfDataList* chunks = it->second;
        ListOfDataList::iterator lit;
        for( lit = chunks->begin(); lit != chunks->end(); lit++)
        {
            delete (*lit);
        }
        chunks->clear();
        delete chunks;
        DEBUG("[IPCDC] Finish chunks for " + id);
        m_chunks.erase(it);
    }
}

//=============================================================================
// METHOD: SPELLipcDataChunk::
//=============================================================================
int SPELLipcDataChunk::getSize( const std::string& id)
{
    int totalChunks = 0;
    DataMap::iterator it = m_chunks.find(id);
    if (it != m_chunks.end())
    {
        totalChunks = it->second->size();
    }
    return totalChunks;
}

//=============================================================================
// METHOD: SPELLipcDataChunk::
//=============================================================================
SPELLipcDataChunk::DataList SPELLipcDataChunk::getChunk( const std::string& id, unsigned int num )
{
    DataList chunk;
    DataMap::iterator it = m_chunks.find(id);
    DEBUG("[IPCDC] Obtain chunk for " + id + " number " + ISTR(num));
    if (it != m_chunks.end())
    {
        ListOfDataList* chunks = it->second;
        if (num<chunks->size())
        {
            DataList* list = (*chunks)[num];
            DataList::iterator lit;
            for( lit = list->begin(); lit != list->end(); lit++)
            {
                chunk.push_back( (*lit) );
            }
        }
    }
    return chunk;
}
