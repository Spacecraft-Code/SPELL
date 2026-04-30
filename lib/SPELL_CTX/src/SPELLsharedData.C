// ################################################################################
// FILE       : SPELLsharedData.C
// DATE       : Mar 17, 2011
// PROJECT    : SPELL
// DESCRIPTION: Implementation of the shared data tables container
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
#include "SPELL_CTX/SPELLsharedData.H"
// Project includes --------------------------------------------------------
#include "SPELL_SYN/SPELLmonitor.H"
#include "SPELL_UTIL/SPELLlog.H"
#include "SPELL_UTIL/SPELLerror.H"
#include "SPELL_UTIL/SPELLutils.H"
// System includes ---------------------------------------------------------


// DEFINES /////////////////////////////////////////////////////////////////

//=============================================================================
// CONSTRUCTOR: SPELLsharedData::SPELLsharedData()
//=============================================================================
SPELLsharedData::SPELLsharedData()
{
	// Create the global scope
	m_tables[GLOBAL_SCOPE] = new SPELLdataTable();
	m_tables[GLOBAL_SCOPE]->setScope(GLOBAL_SCOPE);
}

//=============================================================================
// DESTRUCTOR: SPELLsharedData::~SPELLsharedData()
//=============================================================================
SPELLsharedData::~SPELLsharedData()
{
	clear();
}

//=============================================================================
// METHOD: SPELLsharedData::
//=============================================================================
void SPELLsharedData::clearScope( const std::string& scope )
{
	SPELLmonitor m(m_lock);
	DataTableMap::iterator it = m_tables.find(scope);
	if ( it != m_tables.end() )
	{
		LOG_INFO("Clearing scope '" + scope + "'");
		it->second->clear();
	}
	else
	{
		THROW_EXCEPTION("Cannot clear shared data scope", "No such scope '" + scope + "'", SPELL_ERROR_ENVIRONMENT);
	}
}

//=============================================================================
// METHOD: SPELLsharedData::
//=============================================================================
void SPELLsharedData::clear()
{
	SPELLmonitor m(m_lock);
	DataTableMap::iterator it;
	for(it = m_tables.begin(); it != m_tables.end(); it++)
	{
		delete it->second;
	}
	m_tables.clear();
}

//=============================================================================
// METHOD: SPELLsharedData::
//=============================================================================
bool SPELLsharedData::clear( const std::string& name, const std::string& scope )
{
	SPELLmonitor m(m_lock);
	DataTableMap::iterator it = m_tables.find(scope);
	LOG_INFO("Clear shared variable '" + name + "' in " + scope);
	bool result = false;
	if ( it != m_tables.end() )
	{
		result = it->second->clear(name);
	}
	else
	{
		THROW_EXCEPTION("Cannot clear shared data variable", "No such scope '" + scope + "'", SPELL_ERROR_ENVIRONMENT);
	}
	LOG_INFO("Variables cleared: " + BSTR(result));
	return result;
}

//=============================================================================
// METHOD: SPELLsharedData::
//=============================================================================
bool SPELLsharedData::set( const std::string& name, const std::string& value, const std::string& expected, const std::string& scope )
{
	SPELLmonitor m(m_lock);
	DataTableMap::iterator it = m_tables.find(scope);
	bool result = false;
	LOG_INFO("Set shared variable '" + name + "' to '" + value + "' in " + scope);
	if (expected != NO_EXPECTED)
	{
		LOG_INFO("Only if current value is '" + expected + "'");
	}
	if ( it != m_tables.end() )
	{
		if ( expected == NO_EXPECTED || it->second->get(name) == expected)
		{
			it->second->set(name,value);
			result = true;
		}
		else
		{
			LOG_INFO("Variable unchanged, value is '" + it->second->get(name) + "'");
		}
	}
	else
	{
		THROW_EXCEPTION("Cannot set shared data variables", "No such scope '" + scope + "'", SPELL_ERROR_ENVIRONMENT);
	}
	LOG_INFO("Variable set: " + BSTR(result));
	return result;
}

//=============================================================================
// METHOD: SPELLsharedData::
//=============================================================================
std::string SPELLsharedData::get( const std::string& name, const std::string& scope )
{
	SPELLmonitor m(m_lock);
	DataTableMap::iterator it = m_tables.find(scope);
	std::string result = "";
	LOG_INFO("Get shared variable '" + name + "' value in scope " + scope);
	if ( it != m_tables.end() )
	{
		result = it->second->get(name);
	}
	else
	{
		THROW_EXCEPTION("Cannot get shared data variable", "No such scope '" + scope + "'", SPELL_ERROR_ENVIRONMENT);
	}
	return result;
}

//=============================================================================
// METHOD: SPELLsharedData::
//=============================================================================
SPELLdataTable::KeyList SPELLsharedData::getVariables( const std::string& scope )
{
	SPELLmonitor m(m_lock);
	DataTableMap::iterator it = m_tables.find(scope);
	SPELLdataTable::KeyList keys;
	LOG_INFO("Get shared variable keys in scope " + scope);
	if ( it != m_tables.end() )
	{
		keys = it->second->getVariableNames();
	}
	else
	{
		THROW_EXCEPTION("Cannot get shared data variables", "No such scope '" + scope + "'", SPELL_ERROR_ENVIRONMENT);
	}
	return keys;
}

//=============================================================================
// METHOD: SPELLsharedData::
//=============================================================================
SPELLdataTable::KeyList SPELLsharedData::getScopes()
{
	SPELLmonitor m(m_lock);
	DataTableMap::iterator it;
	SPELLdataTable::KeyList keys;
	LOG_INFO("Get existing scopes");
	for(it = m_tables.begin(); it != m_tables.end(); it++)
	{
		keys.push_back(it->first);
	}
	return keys;
}

//=============================================================================
// METHOD: SPELLsharedData::
//=============================================================================
void SPELLsharedData::addScope( const std::string& scope )
{
	SPELLmonitor m(m_lock);
	DataTableMap::iterator it;
	SPELLdataTable::KeyList keys;
	it = m_tables.find(scope);
	if (it == m_tables.end())
	{
		LOG_INFO("Creating scope '" + scope + "'");
		// Create the scope
		m_tables[scope] = new SPELLdataTable();
		m_tables[scope]->setScope(scope);
	}
	else
	{
		THROW_EXCEPTION("Cannot add scope '" + scope + "'", "Already exists", SPELL_ERROR_ENVIRONMENT);
	}
}

//=============================================================================
// METHOD: SPELLsharedData::
//=============================================================================
void SPELLsharedData::removeScope( const std::string& scope )
{
	SPELLmonitor m(m_lock);
	DataTableMap::iterator it;
	SPELLdataTable::KeyList keys;
	it = m_tables.find(scope);
	if (it != m_tables.end())
	{
		// Remove any empty scope unless it is the global scope
		if ( (scope != GLOBAL_SCOPE) )
		{
			LOG_INFO("Removing scope '" + scope + "'");
			it->second->clear();
			delete it->second;
			m_tables.erase(it);
			LOG_INFO("Deleted scope " + scope);
		}
	}
	else
	{
		THROW_EXCEPTION("Cannot remove scope '" + scope + "'", "Not found", SPELL_ERROR_ENVIRONMENT);
	}
}

//=============================================================================
// METHOD: SPELLsharedData::
//=============================================================================
bool SPELLsharedData::hasScope( const std::string& scope )
{
	SPELLmonitor m(m_lock);
	DataTableMap::iterator it;
	SPELLdataTable::KeyList keys;
	it = m_tables.find(scope);
	return (it != m_tables.end());
}
