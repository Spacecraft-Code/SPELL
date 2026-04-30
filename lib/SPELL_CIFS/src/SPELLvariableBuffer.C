// ################################################################################
// FILE       : SPELLvariableBuffer.C
// DATE       : Mar 17, 2011
// PROJECT    : SPELL
// DESCRIPTION: Implementation of the WOV messages buffer
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
#include "SPELL_CIFS/SPELLvariableBuffer.H"
#include "SPELL_CIFS/SPELLserverCif.H"
// Project includes --------------------------------------------------------
#include "SPELL_IPC/SPELLipc.H"
#include "SPELL_SYN/SPELLmonitor.H"
#include "SPELL_UTIL/SPELLlog.H"
#include "SPELL_WRP/SPELLconstants.H"
// System includes ---------------------------------------------------------


#define FLUSH_PERIOD_USEC 250000 //half a sec

//=============================================================================
// CONSTRUCTOR: SPELLvariableBuffer::SPELLvariableBuffer
//=============================================================================
SPELLvariableBuffer::SPELLvariableBuffer( const std::string& procId, SPELLserverCif& cif )
: SPELLthread("display-buffer"),
  m_cif(cif),
  m_variableMessage( ExecutorMessages::MSG_VARIABLE_CHANGE )
{
    DEBUG("[CIF] Created display buffer");
    m_working = true;
    m_variableMessage.setType(MSG_TYPE_ONEWAY);
    m_variableMessage.set( MessageField::FIELD_PROC_ID, procId );
    m_total = 0;
}

//=============================================================================
// DESTRUCTOR: SPELLvariableBuffer::~SPELLvariableBuffer
//=============================================================================
SPELLvariableBuffer::~SPELLvariableBuffer()
{
    DEBUG("[CIF] Destroyed WOV buffer");
}

//=============================================================================
// METHOD: SPELLvariableBuffer::run()
//=============================================================================
void SPELLvariableBuffer::run()
{
	while(m_working)
	{
		usleep(FLUSH_PERIOD_USEC);
		{
			SPELLmonitor m(m_lock);
			if (hasDataToSend())
			{
				flush();
			}
		}
	}
}

//=============================================================================
// METHOD: SPELLvariableBuffer::run()
//=============================================================================
void SPELLvariableBuffer::stop()
{
	m_working = false;
}

//=============================================================================
// METHOD: SPELLvariableBuffer::variableChange()
//=============================================================================
void SPELLvariableBuffer::variableChange( const std::vector<SPELLvarInfo>& added,
                                          const std::vector<SPELLvarInfo>& changed,
                                          const std::vector<SPELLvarInfo>& deleted )
{
	SPELLmonitor m(m_lock);
	std::vector<SPELLvarInfo>::const_iterator it;
	std::map<std::string,SPELLvarInfo>::iterator mit;

	for( it = added.begin(); it != added.end(); it++)
	{
		std::string name = (*it).varName;
		m_variables[ name ] = (*it);
		m_variables[ name ].isAdded = true;
		m_variables[ name ].isDeleted = false;
	}
	for( it = changed.begin(); it != changed.end(); it++)
	{
		// If the variable was added and not yet notified, just not process
		// this notification and leave the add operation but with the new value.
		std::string name = (*it).varName;
		bool wasAdded = m_variables[ name ].isAdded;
		m_variables[ name ] = (*it);
		m_variables[ name ].isAdded = wasAdded;
		m_variables[ name ].isDeleted = false;
	}
	for( it = deleted.begin(); it != deleted.end(); it++)
	{
		// If the variable was added and not yet notified, just not process
		// this notification and remove the added operation. Nevertheless we
		// need to keep the update operation if any (actually becomes a delete op)
		std::string name = (*it).varName;
		mit = m_variables.find(name);
		if (mit != m_variables.end())
		{
			if (mit->second.isAdded)
			{
				m_variables.erase(mit);
				continue;
			}
		}
		// Otherwise set it to delete
		m_variables[ name ] = (*it);
		m_variables[ name ].isAdded = false;
		m_variables[ name ].isDeleted = true;
	}
	m_total++;
}

//=============================================================================
// METHOD: SPELLvariableBuffer::flush()
//=============================================================================
void SPELLvariableBuffer::flush()
{
	SPELLmonitor m(m_lock);

	std::string names = "";
	std::string types = "";
	std::string values = "";
	std::string globals = "";
	std::string deleteds = "";
	std::string addeds = "";

	std::map<std::string,SPELLvarInfo>::const_iterator it;
	for(it = m_variables.begin(); it != m_variables.end(); it++)
	{
		if (names != "")
		{
			names += VARIABLE_SEPARATOR;
			types += VARIABLE_SEPARATOR;
			values += VARIABLE_SEPARATOR;
			globals += VARIABLE_SEPARATOR;
			deleteds += VARIABLE_SEPARATOR;
			addeds += VARIABLE_SEPARATOR;
		}
		names += it->second.varName;
		types += it->second.varType;
		values += it->second.varValue;
		globals += it->second.isGlobal ? "True" : "False";
		deleteds += it->second.isDeleted ? "True" : "False";
		addeds += it->second.isAdded ? "True" : "False";
	}

	m_variables.clear();

	m_variableMessage.set(MessageField::FIELD_VARIABLE_NAME,   names);
	m_variableMessage.set(MessageField::FIELD_VARIABLE_TYPE,   types);
	m_variableMessage.set(MessageField::FIELD_VARIABLE_VALUE,  values);
	m_variableMessage.set(MessageField::FIELD_VARIABLE_GLOBAL, globals);
	m_variableMessage.set(MessageField::FIELD_VARIABLE_DELETE, deleteds);
	m_variableMessage.set(MessageField::FIELD_VARIABLE_ADDED, addeds);

	std::cerr << "SEND WOV (" << m_total << ")" << std::endl;

	m_cif.sendGUIMessage(m_variableMessage);
	m_total = 0;
}

//=============================================================================
// METHOD: SPELLvariableBuffer::hasDataToSend()
//=============================================================================
bool SPELLvariableBuffer::hasDataToSend()
{
	SPELLmonitor m(m_lock);
	return (m_variables.size()>0);
}

//=============================================================================
// METHOD: SPELLvariableBuffer::scopeChange()
//=============================================================================
void SPELLvariableBuffer::scopeChange()
{
	SPELLmonitor m(m_lock);
	m_variables.clear();
	m_total = 0;
}
