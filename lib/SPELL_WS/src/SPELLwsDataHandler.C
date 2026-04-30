// ################################################################################
// FILE       : SPELLwsDataHandler.C
// DATE       : Mar 17, 2011
// PROJECT    : SPELL
// DESCRIPTION: Implementation of the base data handler
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
#include "SPELL_WS/SPELLwsDataHandler.H"
// Project includes --------------------------------------------------------
#include "SPELL_UTIL/SPELLlog.H"
// System includes ---------------------------------------------------------

//=============================================================================
// CONSTRUCTOR: SPELLwsDataHandler::SPELLwsDataHandler
//=============================================================================
SPELLwsDataHandler::SPELLwsDataHandler( SPELLwsData::Code code )
: m_storage(NULL),
  m_dataCode(code)
{

}

//=============================================================================
// DESTRUCTOR: SPELLwsDataHandler::~SPELLwsDataHandler
//=============================================================================
SPELLwsDataHandler::~SPELLwsDataHandler()
{
}

//=============================================================================
// METHOD: SPELLwsDataHandler::setStorage()
//=============================================================================
void SPELLwsDataHandler::setStorage( SPELLwsStorage* storage )
{
	assert( storage != NULL );
	m_storage = storage;
};

//=============================================================================
// METHOD: SPELLwsDataHandler::getCode()
//=============================================================================
SPELLwsData::Code SPELLwsDataHandler::getCode() const
{
	return m_dataCode;
};

//=============================================================================
// METHOD: SPELLwsDataHandler::storeDataCode()
//=============================================================================
void SPELLwsDataHandler::storeDataCode()
{
	getStorage()->storeLong( static_cast<long>(getCode()) );
};

//=============================================================================
// METHOD: SPELLwsDataHandler::loadDataCode()
//=============================================================================
SPELLwsData::Code SPELLwsDataHandler::loadDataCode()
{
	return static_cast<SPELLwsData::Code>(getStorage()->loadLong());
}

//=============================================================================
// METHOD: SPELLwsDataHandler::getStorage()
//=============================================================================
SPELLwsStorage* SPELLwsDataHandler::getStorage()
{
	assert(m_storage != NULL);
	return m_storage;
};

//=============================================================================
// METHOD: SPELLwsDataHandler::setCode()
//=============================================================================
void SPELLwsDataHandler::setCode( SPELLwsData::Code code )
{
	m_dataCode = code;
};
