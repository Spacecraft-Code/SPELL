// ################################################################################
// FILE       : SPELLpersistencyTable.C
// DATE       : Mar 17, 2011
// PROJECT    : SPELL
// DESCRIPTION: Implementation of the executor persistency table
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
#include "SPELL_CTX/SPELLpersistencyTable.H"
// Project includes --------------------------------------------------------
#include "SPELL_UTIL/SPELLlog.H"
#include "SPELL_UTIL/SPELLutils.H"
// System includes ---------------------------------------------------------


// DEFINES /////////////////////////////////////////////////////////////////

static SPELLexecutorPersistency EMPTY_PERSISTENCY;

//=============================================================================
// CONSTRUCTOR: SPELLpersistencyTable::SPELLpersistencyTable
//=============================================================================
SPELLpersistencyTable::SPELLpersistencyTable( const std::string& ctxName )
{
	EMPTY_PERSISTENCY.instanceId = "<none>";
	EMPTY_PERSISTENCY.ipcPort = -1;
	m_persistencyFileName = SPELLutils::getSPELL_DATA() + PATH_SEPARATOR + "Runtime" + PATH_SEPARATOR + "ctx_" + ctxName + ".dat";
	m_persistencyFile.open( m_persistencyFileName.c_str(), std::fstream::in | std::fstream::out | std::fstream::app );
	if (!m_persistencyFile.is_open())
	{
		LOG_ERROR("Unable to create context persistency file: " + m_persistencyFileName);
		m_persistencyFileName = "";
	}
	m_persistencyFile.close();
}

//=============================================================================
// DESTRUCTOR: SPELLpersistencyTable::~SPELLpersistencyTable
//=============================================================================
SPELLpersistencyTable::~SPELLpersistencyTable()
{
	// Just remove the persistency file
	if (m_persistencyFileName != "")
	{
		m_persistencyFile.close();
		SPELLutils::deleteFile(m_persistencyFileName);
	}
}

//=============================================================================
// METHOD: SPELLpersistencyTable::
//=============================================================================
bool SPELLpersistencyTable::load()
{
	if (m_persistencyFileName != "")
	{
		m_persistencyFile.open( m_persistencyFileName.c_str(), std::fstream::in );
		std::string line = "";
		while( m_persistencyFile.good() )
		{
			std::getline( m_persistencyFile, line );
			if (line != "")
			{
				std::vector<std::string> tokens = SPELLutils::tokenize(line, ",");
				if (tokens.size() == 5)
				{
					std::string parentId = tokens[2];
					if (parentId == "none") parentId = "";
					SPELLexecutorPersistency pers;
					pers.instanceId = tokens[0];
					pers.ipcPort = STRI(tokens[3]);
					pers.timeId = tokens[1];
					pers.parentId = parentId;
					pers.pid = STRI(tokens[4]);
					m_data.insert( std::make_pair(tokens[0], pers) );
				}
				else
				{
					LOG_ERROR("Invalid executor persistency entry: '" + line + "'");
				}
			}
		}
		m_persistencyFile.close();
		return (m_data.size()>0);
	}
	return false;
}

//=============================================================================
// METHOD: SPELLpersistencyTable::
//=============================================================================
const std::vector<std::string> SPELLpersistencyTable::getRegisteredExecutors() const
{
	std::vector<std::string> ids;
	for(PersistencyMap::const_iterator it = m_data.begin(); it != m_data.end(); it++)
	{
		ids.push_back(it->first);
	}
	return ids;
}

//=============================================================================
// METHOD: SPELLpersistencyTable::
//=============================================================================
const SPELLexecutorPersistency SPELLpersistencyTable::getExecutorPersistency( const std::string& instanceId ) const
{
	PersistencyMap::const_iterator it = m_data.find(instanceId);
	if (it != m_data.end())
	{
		return it->second;
	}
	return EMPTY_PERSISTENCY;
}

//=============================================================================
// METHOD: SPELLpersistencyTable::
//=============================================================================
void SPELLpersistencyTable::registerExecutor( const std::string& instanceId, const std::string& timeId, const std::string& parentId, int ipcPort, pid_t pid )
{
	if (m_persistencyFileName != "")
	{
		SPELLexecutorPersistency pers;
		pers.instanceId = instanceId;
		pers.ipcPort = ipcPort;
		pers.timeId = timeId;
		pers.parentId = parentId;
		pers.pid = pid;
		m_data.insert( std::make_pair(instanceId, pers) );
		dumpToFile();
	}
}

//=============================================================================
// METHOD: SPELLpersistencyTable::
//=============================================================================
void SPELLpersistencyTable::deregisterExecutor( const std::string& instanceId )
{
	PersistencyMap::iterator it = m_data.find(instanceId);
	if (it != m_data.end())
	{
		m_data.erase(it);
		dumpToFile();
	}
}

//=============================================================================
// METHOD: SPELLpersistencyTable::
//=============================================================================
void SPELLpersistencyTable::dumpToFile()
{
	if (m_persistencyFileName != "")
	{
		m_persistencyFile.open( m_persistencyFileName.c_str(), std::fstream::out );
		for(PersistencyMap::const_iterator it = m_data.begin(); it != m_data.end(); it++)
		{
			std::string parentId = it->second.parentId;
			if (parentId == "")
			{
				parentId = "none";
			}
			m_persistencyFile << it->second.instanceId << "," <<
					             it->second.timeId     << "," <<
					             parentId              << "," <<
					             it->second.ipcPort    << "," <<
					             it->second.pid        << std::endl;
		}
		m_persistencyFile.close();
	}
}


