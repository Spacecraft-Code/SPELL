// ################################################################################
// FILE       : SPELLdisplayBuffer.C
// DATE       : Mar 17, 2011
// PROJECT    : SPELL
// DESCRIPTION: Implementation of the display messages buffer
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
#include "SPELL_CIFS/SPELLdisplayBuffer.H"
#include "SPELL_CIFS/SPELLserverCif.H"
// Project includes --------------------------------------------------------
#include "SPELL_IPC/SPELLipc.H"
#include "SPELL_SYN/SPELLmonitor.H"
#include "SPELL_UTIL/SPELLlog.H"
#include "SPELL_WRP/SPELLconstants.H"
// System includes ---------------------------------------------------------


#define FLUSH_PERIOD_USEC 200000

//=============================================================================
// CONSTRUCTOR: SPELLdisplayBuffer::SPELLdisplayBuffer
//=============================================================================
SPELLdisplayBuffer::SPELLdisplayBuffer( const std::string& procId, SPELLserverCif& cif )
: SPELLthread("display-buffer"),
  m_cif(cif),
  m_message( MessageId::MSG_ID_WRITE )
{
    DEBUG("[CIF] Created display buffer");
    m_working = true;
	m_severity = "";
	m_text = "";
	m_scope = 0;
    m_message.setType(MSG_TYPE_WRITE);
    m_message.set( MessageField::FIELD_PROC_ID, procId );
}

//=============================================================================
// DESTRUCTOR: SPELLdisplayBuffer::~SPELLdisplayBuffer
//=============================================================================
SPELLdisplayBuffer::~SPELLdisplayBuffer()
{
    DEBUG("[CIF] Destroyed display buffer");
}

//=============================================================================
// METHOD: SPELLdisplayBuffer::run()
//=============================================================================
void SPELLdisplayBuffer::run()
{
	while(m_working)
	{
		usleep(FLUSH_PERIOD_USEC);
		{
			SPELLmonitor m(m_lock);
			if (m_text != "")
			{
				flush();
			}
		}
	}
}

//=============================================================================
// METHOD: SPELLdisplayBuffer::run()
//=============================================================================
void SPELLdisplayBuffer::stop()
{
	m_working = false;
}

//=============================================================================
// METHOD: SPELLdisplayBuffer::write()
//=============================================================================
void SPELLdisplayBuffer::write( const std::string& msg, unsigned int scope )
{
	SPELLmonitor m(m_lock);
	// If there is already data with a different severity, flush first
	if ((m_severity != "")&&(m_severity != MessageValue::DATA_SEVERITY_INFO))
	{
		flush();
	}
	else if ((m_scope !=0)&&(m_scope != scope))
	{
		flush();
	}
	else if (m_text != "") m_text += "\n";
	m_text += msg;
	m_severity = MessageValue::DATA_SEVERITY_INFO;
	m_scope = scope;
}

//=============================================================================
// METHOD: SPELLdisplayBuffer::warning()
//=============================================================================
void SPELLdisplayBuffer::warning( const std::string& msg, unsigned int scope )
{
	SPELLmonitor m(m_lock);
	// If there is already data with a different severity, flush first
	if ((m_severity != "")&&(m_severity != MessageValue::DATA_SEVERITY_WARN))
	{
		flush();
	}
	else if ((m_scope !=0)&&(m_scope != scope))
	{
		flush();
	}
	else if (m_text != "") m_text += "\n";
	m_text += msg;
	m_severity = MessageValue::DATA_SEVERITY_WARN;
	m_scope = scope;
}

//=============================================================================
// METHOD: SPELLdisplayBuffer::error()
//=============================================================================
void SPELLdisplayBuffer::error( const std::string& msg, unsigned int scope )
{
	SPELLmonitor m(m_lock);
	// If there is already data with a different severity, flush first
	if ((m_severity != "")&&(m_severity != MessageValue::DATA_SEVERITY_ERROR))
	{
		flush();
	}
	else if ((m_scope !=0)&&(m_scope != scope))
	{
		flush();
	}
	else if (m_text != "") m_text += "\n";
	m_text += msg;
	m_severity = MessageValue::DATA_SEVERITY_ERROR;
	m_scope = scope;
}

//=============================================================================
// METHOD: SPELLdisplayBuffer::prompt()
//=============================================================================
void SPELLdisplayBuffer::prompt( const std::string& msg, unsigned int scope )
{
	SPELLmonitor m(m_lock);
	// If there is already data with a different severity, flush first
	if ((m_severity != "")&&(m_severity != "<PROMPT>"))
	{
		flush();
	}
	else if ((m_scope !=0)&&(m_scope != scope))
	{
		flush();
	}
	else if (m_text != "") m_text += "\n";
	m_text += msg;
	m_severity = "<PROMPT>";
	m_scope = scope;
}

//=============================================================================
// METHOD: SPELLdisplayBuffer::flush()
//=============================================================================
void SPELLdisplayBuffer::flush()
{
	if (m_text != "")
	{
	    m_cif.completeMessage( m_message );

	    std::string timeStr = SPELLutils::timestampUsec();

	    m_message.set(MessageField::FIELD_TEXT,    m_text );
	    std::string severity = (m_severity == "<PROMPT>") ? MessageValue::DATA_SEVERITY_INFO : m_severity;
	    m_message.set(MessageField::FIELD_LEVEL,   severity);
	    m_message.set(MessageField::FIELD_MSGTYPE, LanguageConstants::DISPLAY);
	    m_message.set(MessageField::FIELD_TIME,    timeStr);
	    m_message.set(MessageField::FIELD_SCOPE,   ISTR(m_scope));

	    m_cif.sendGUIMessage(&m_message);

	    m_text = "";
	    m_severity = "";
	    m_scope = 0;
	}
}
