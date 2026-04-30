// ################################################################################
// FILE       : SPELLdataTable.C
// DATE       : Mar 17, 2011
// PROJECT    : SPELL
// DESCRIPTION: Implementation of the shared data tables
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
#include "SPELL_CTX/SPELLdataTable.H"
// Project includes --------------------------------------------------------
#include "SPELL_SYN/SPELLmonitor.H"
#include "SPELL_UTIL/SPELLlog.H"
#include "SPELL_UTIL/SPELLerror.H"
#include "SPELL_UTIL/SPELLutils.H"
// System includes ---------------------------------------------------------


// DEFINES /////////////////////////////////////////////////////////////////

//=============================================================================
// CONSTRUCTOR: SPELLdataTable::SPELLdataTable()
//=============================================================================
SPELLdataTable::SPELLdataTable()
{

}

//=============================================================================
// DESTRUCTOR: SPELLdataTable::~SPELLdataTable()
//=============================================================================
SPELLdataTable::~SPELLdataTable()
{
	clear();
}

//=============================================================================
// METHOD: SPELLdataTable::
//=============================================================================
std::string SPELLdataTable::get( const std::string& name )
{
	SPELLmonitor m(m_lock);
	DataMap::iterator it = m_map.find(name);

	std::string value = "";
	if ( it != m_map.end() )
	{
		value = it->second;
	}
	else
	{
		THROW_EXCEPTION("Cannot obtain shared variable value", "No such variable '" + name + "'", SPELL_ERROR_ENVIRONMENT);
	}
	return value;
}

//=============================================================================
// METHOD: SPELLdataTable::
//=============================================================================
SPELLdataTable::KeyList SPELLdataTable::getVariableNames()
{
	SPELLmonitor m(m_lock);
	KeyList list;
	DataMap::iterator it;
	for( it = m_map.begin(); it != m_map.end(); it++)
	{
		list.push_back(it->first);
	}
	return list;
}

//=============================================================================
// METHOD: SPELLdataTable::
//=============================================================================
void SPELLdataTable::set( const std::string& name, const std::string& value )
{
	SPELLmonitor m(m_lock);
	DataMap::iterator it = m_map.find(name);
	// If the variable exists
	if ( it != m_map.end() )
	{
		it->second = value;
	}
	else
	{
		m_map[name] = value;
	}
}

//=============================================================================
// METHOD: SPELLdataTable::
//=============================================================================
bool SPELLdataTable::clear( const std::string& name )
{
	SPELLmonitor m(m_lock);
	DataMap::iterator it = m_map.find(name);
	bool result = false;
	// If the variable exists
	if ( it != m_map.end() )
	{
		// Remove the variable
		m_map.erase(it);
		result = true;
	}
	return result;
}

//=============================================================================
// METHOD: SPELLdataTable::
//=============================================================================
void SPELLdataTable::clear()
{
	SPELLmonitor m(m_lock);
	m_map.clear();
}
