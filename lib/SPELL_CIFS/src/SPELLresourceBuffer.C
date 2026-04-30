// ################################################################################
// FILE       : SPELLresourceBuffer.C
// DATE       : Mar 17, 2011
// PROJECT    : SPELL
// DESCRIPTION: Implementation of the RDOTCO messages buffer
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
#include "SPELL_CIFS/SPELLresourceBuffer.H"
#include "SPELL_CIFS/SPELLserverCif.H"
// Project includes --------------------------------------------------------
#include "SPELL_IPC/SPELLipc.H"
#include "SPELL_SYN/SPELLmonitor.H"
#include "SPELL_UTIL/SPELLlog.H"
#include "SPELL_WRP/SPELLconstants.H"
// System includes ---------------------------------------------------------


#define FLUSH_PERIOD_USEC 250000 //half a sec

//=============================================================================
// CONSTRUCTOR: SPELLresourceBuffer::SPELLresourceBuffer
//=============================================================================
SPELLresourceBuffer::SPELLresourceBuffer( const std::string& procId, SPELLserverCif& cif )
: SPELLthread("display-buffer"),
  m_cif(cif),
  m_resourceMessage( ExecutorMessages::MSG_RESOURCE_CHANGE )
{
    DEBUG("[CIF] Created display buffer");
    m_working = true;
    m_resourceMessage.setType(MSG_TYPE_ONEWAY);
    m_resourceMessage.set( MessageField::FIELD_PROC_ID, procId );
    m_total = 0;
	m_bufferHasListResources = false;
}

//=============================================================================
// DESTRUCTOR: SPELLresourceBuffer::~SPELLresourceBuffer
//=============================================================================
SPELLresourceBuffer::~SPELLresourceBuffer()
{
    DEBUG("[CIF] Destroyed RDOTCO buffer");
}

//=============================================================================
// METHOD: SPELLresourceBuffer::run()
//=============================================================================
void SPELLresourceBuffer::run()
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
// METHOD: SPELLresourceBuffer::run()
//=============================================================================
void SPELLresourceBuffer::stop()
{
	m_working = false;
}

//=============================================================================
// METHOD: SPELLresourceBuffer::resourceChange()
//=============================================================================
void SPELLresourceBuffer::resourceChange( const std::vector<SPELLresourceInfo>& changed )
{
	SPELLmonitor m(m_lock);
	std::vector<SPELLresourceInfo>::const_iterator it;
	std::map<std::string,SPELLresourceInfo>::iterator mit;

	for( it = changed.begin(); it != changed.end(); it++)
	{
		// If the resource was added and not yet notified, just not process
		// this notification and leave the add operation but with the new value.
		std::string name = (*it).resourceName;
		m_resources[ name ] = (*it);
	}
	m_total++;
}

//=============================================================================
// METHOD: SPELLresourceBuffer::resourceList()
//=============================================================================
void SPELLresourceBuffer::resourceList( const std::vector<SPELLresourceInfo>& resources )
{
	resourceChange(resources);
	m_bufferHasListResources = true;
}

//=============================================================================
// METHOD: SPELLresourceBuffer::flush()
//=============================================================================
void SPELLresourceBuffer::flush()
{
	SPELLmonitor m(m_lock);

	std::string names = "";
	std::string types = "";
	std::string globalValues = "";
	std::string localValues = "";
	std::string valueRange = "";
	std::string gcsValueRange = "";
	std::string gcsNames = "";

	std::map<std::string,SPELLresourceInfo>::const_iterator it;
	for(it = m_resources.begin(); it != m_resources.end(); it++)
	{
		if (names != "")
		{
			names += VARIABLE_SEPARATOR;
			types += VARIABLE_SEPARATOR;
			globalValues += VARIABLE_SEPARATOR;
			localValues += VARIABLE_SEPARATOR;
			gcsNames += VARIABLE_SEPARATOR;
			if (m_bufferHasListResources)
			{
				valueRange += VARIABLE_SEPARATOR;
				gcsValueRange += VARIABLE_SEPARATOR;
			}
		}
		names += it->second.resourceName;
		types += it->second.resourceType;
		globalValues += it->second.resourceGlobalValue;
		localValues += it->second.resourceLocalValue;
		gcsNames += it->second.resourceGCSName;
		if (m_bufferHasListResources)
		{
			for (unsigned int vri = 0; vri < it->second.resourceValueRange.size(); vri++)
			{
				if (vri > 0)
				{
					valueRange += VARIABLE_PROPERTY_SEPARATOR;
					gcsValueRange += VARIABLE_PROPERTY_SEPARATOR;
				}
				valueRange += it->second.resourceValueRange[vri].first;
				gcsValueRange += it->second.resourceValueRange[vri].second;
			}
		}
	}

	m_resources.clear();

	m_resourceMessage.set(MessageField::FIELD_RESOURCE_NAME, names);
	m_resourceMessage.set(MessageField::FIELD_RESOURCE_TYPE, types);
	m_resourceMessage.set(MessageField::FIELD_RESOURCE_GLOBAL_VALUE, globalValues);
	m_resourceMessage.set(MessageField::FIELD_RESOURCE_LOCAL_VALUE, localValues);
	// An empty string is passed in case of resource change (no need to resend the ranges)
	m_resourceMessage.set(MessageField::FIELD_RESOURCE_VALUE_RANGE, valueRange);
	m_resourceMessage.set(MessageField::FIELD_RESOURCE_GCS_VALUE_RANGE, gcsValueRange);
	m_resourceMessage.set(MessageField::FIELD_RESOURCE_GCS_NAME, gcsNames);

#ifdef WITH_DEBUG
	std::cerr << "SEND RDOTCO (" << m_total << ")" << std::endl;
#endif

	m_cif.sendGUIMessage(m_resourceMessage);
	m_total = 0;
	m_bufferHasListResources = false;
}

//=============================================================================
// METHOD: SPELLresourceBuffer::hasDataToSend()
//=============================================================================
bool SPELLresourceBuffer::hasDataToSend()
{
	SPELLmonitor m(m_lock);
	return (m_resources.size()>0);
}
