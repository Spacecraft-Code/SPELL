// ################################################################################
// FILE       : SPELLuserAction.H
// DATE       : Mar 17, 2011
// PROJECT    : SPELL
// DESCRIPTION: Defines a user action
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

#ifndef __SPELL_USER_ACTION_H__
#define __SPELL_USER_ACTION_H__

// FILES TO INCLUDE ////////////////////////////////////////////////////////
// System includes ---------------------------------------------------------
// Local includes ----------------------------------------------------------
// Project includes --------------------------------------------------------
#include "SPELL_UTIL/SPELLbase.H"

/** \addtogroup SPELL_EXC */
/*@{*/


// FORWARD REFERENCES //////////////////////////////////////////////////////
// TYPES ///////////////////////////////////////////////////////////////////
// DEFINES /////////////////////////////////////////////////////////////////

//////////////////////////////////////////////////////////////////////////////
/**
 ** \brief User action definition
 **
**////////////////////////////////////////////////////////////////////////////
class SPELLuserAction
{
public:
	
	typedef enum UserActionStatus_
	{
		NOTSET,
		ENABLED,
		DISABLED
	}
	UserActionStatus;

	SPELLuserAction()
	{
		reset();
	};

	void reset()
	{
		m_action = "";
		m_label = "";
		m_status = NOTSET;
		m_severity = 0;
	}
	
	std::string getAction() const { return m_action; };

	std::string getLabel() const { return m_label; };

	bool isEnabled() const { return m_status == ENABLED; };

	bool isSet() const { return m_status == ENABLED || m_status == DISABLED; };

	const unsigned int getSeverity() const { return m_severity; };

	void setAction( const std::string& action ) { m_action = action; };

	void setLabel( const std::string& label ) { m_label = label; };

	void enable( bool enable ) { m_status = enable ? ENABLED : DISABLED; };

	void setSeverity( const unsigned int sev ) { m_severity = sev; };

private:
	
	std::string m_action;
	std::string m_label;
	UserActionStatus m_status;
	unsigned int m_severity;
	
};

/*@}*/
#endif
