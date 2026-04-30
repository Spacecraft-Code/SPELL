// ################################################################################
// FILE       : SPELLconfigDict.C
// DATE       : Mar 17, 2011
// PROJECT    : SPELL
// DESCRIPTION: Implementation of the configuration dictionary wrapper
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
// System includes ---------------------------------------------------------
// Local includes ----------------------------------------------------------
#include "SPELL_WRP/SPELLconfigDict.H"
#include "SPELL_WRP/SPELLregistry.H"
#include "SPELL_WRP/SPELLpyHandle.H"
#include "SPELL_UTIL/SPELLpythonHelper.H"
#include "SPELL_UTIL/SPELLpythonMonitors.H"
#include "SPELL_UTIL/SPELLutils.H"
// Project includes --------------------------------------------------------



//=============================================================================
// CONSTRUCTOR : SPELLconfigDict::SPELLconfigDict()
//=============================================================================
SPELLconfigDict::SPELLconfigDict( PyObject* dict )
{
	reset(dict);
}

//=============================================================================
// DESTRUCTOR : SPELLconfigDict::~SPELLconfigDict
//=============================================================================
SPELLconfigDict::~SPELLconfigDict()
{
}

//=============================================================================
// CONSTRUCTOR: SPELLconfigDict::SPELLconfigDict()
//=============================================================================
SPELLconfigDict::SPELLconfigDict( const SPELLconfigDict& other )
{
	m_values = other.m_values;
}

//=============================================================================
// METHOD: SPELLconfigDict::reset()
//=============================================================================
void SPELLconfigDict::reset( PyObject* dict )
{
	m_values.clear();
	if (dict)
	{
		SPELLsafePythonOperations ops ("SPELLconfigDict::reset()");
		SPELLpyHandle keys = PyDict_Keys(dict);
		unsigned int numKeys = PyList_Size(keys.get());
		for( unsigned int idx = 0; idx < numKeys; idx++ )
		{
			PyObject* key = PyList_GetItem( keys.get(), idx );
			PyObject* value = PyDict_GetItem( dict, key );
			m_values.insert( std::make_pair( PYSSTR(key), SPELLpyValue(value) ));
		}
	}
}

//=============================================================================
// METHOD: SPELLconfigDict::
//=============================================================================
SPELLconfigDict& SPELLconfigDict::operator=( const SPELLconfigDict& other )
{
	if (&other != this)
	{
		m_values = other.m_values;
	}
	return *this;
}

//=============================================================================
// METHOD: SPELLconfigDict::
//=============================================================================
SPELLpyValue SPELLconfigDict::get( const std::string& key ) const
{
	std::map<std::string,SPELLpyValue>::const_iterator it = m_values.find(key);
	if (it != m_values.end() )
	{
		return it->second;
	}
	return SPELLpyValue(NULL);
}

//=============================================================================
// METHOD: SPELLconfigDict::
//=============================================================================
bool SPELLconfigDict::hasKey( const std::string& key ) const
{
	std::map<std::string,SPELLpyValue>::const_iterator it = m_values.find(key);
	return (it != m_values.end() );
}

//=============================================================================
// METHOD: SPELLconfigDict::
//=============================================================================
void SPELLconfigDict::update( const SPELLconfigDict& other )
{
	std::map<std::string,SPELLpyValue>::const_iterator it;
	for( it = other.m_values.begin(); it != other.m_values.end(); it++ )
	{
		m_values.insert( std::make_pair( it->first, it->second ));
	}
}

//=============================================================================
// METHOD: SPELLconfigDict::
//=============================================================================
PyObject* SPELLconfigDict::toPython() const
{
	SPELLsafePythonOperations ops("SPELLconfigDict::toPython()");
	PyObject* dict = PyDict_New();
	std::map<std::string,SPELLpyValue>::const_iterator it;
	for( it = m_values.begin(); it != m_values.end(); it++ )
	{
		PyDict_SetItemString( dict, it->first.c_str(), it->second.get() );

	}
	return dict;
}
