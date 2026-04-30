// ################################################################################
// FILE       : SPELLclientInterestList.C
// DATE       : Mar 17, 2011
// PROJECT    : SPELL
// DESCRIPTION: Implementation of the client interest list
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
#include "SPELL_CTX/SPELLclientInterestList.H"
// Project includes --------------------------------------------------------
#include "SPELL_SYN/SPELLmonitor.H"
#include "SPELL_UTIL/SPELLlog.H"
#include "SPELL_UTIL/SPELLutils.H"
// System includes ---------------------------------------------------------


// DEFINES /////////////////////////////////////////////////////////////////


//=============================================================================
// CONSTRUCTOR: SPELLclientInterestList::SPELLclientInterestList()
//=============================================================================
SPELLclientInterestList::SPELLclientInterestList()
{
	m_listener = NULL;
}

//=============================================================================
// DESTRUCTOR: SPELLclientInterestList::~SPELLclientInterestList()
//=============================================================================
SPELLclientInterestList::~SPELLclientInterestList()
{
	m_listener = NULL;
}

//=============================================================================
// METHOD: SPELLclientInterestList::
//=============================================================================
void SPELLclientInterestList::setClientListener( SPELLclientListener* entity )
{
	SPELLmonitor monitor(m_lock);
	m_listener = entity;
}

//=============================================================================
// METHOD: SPELLclientInterestList::
//=============================================================================
void SPELLclientInterestList::removeClientListener()
{
	SPELLmonitor monitor(m_lock);
	m_listener = NULL;
}

//=============================================================================
// METHOD: SPELLclientInterestList::
//=============================================================================
void SPELLclientInterestList::distributeMessage( const SPELLipcMessage& msg )
{
	SPELLmonitor monitor(m_lock);
	if (m_listener)
	{
		m_listener->processMessageFromClient(msg);
	}
}

//=============================================================================
// METHOD: SPELLclientInterestList::
//=============================================================================
SPELLipcMessage SPELLclientInterestList::distributeRequest( const SPELLipcMessage& msg )
{
	SPELLipcMessage response = VOID_MESSAGE;
	if (m_listener)
	{
		response = m_listener->processRequestFromClient(msg);
	}
	return response;
}
