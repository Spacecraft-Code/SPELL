// ################################################################################
// FILE       : SPELLwsObjectDataHandler.C
// DATE       : Mar 17, 2011
// PROJECT    : SPELL
// DESCRIPTION: Implementation of the data handler for objects
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
#include "SPELL_WS/SPELLwsObjectDataHandler.H"
// Project includes --------------------------------------------------------
#include "SPELL_UTIL/SPELLlog.H"
#include "SPELL_UTIL/SPELLpythonHelper.H"
// System includes ---------------------------------------------------------

//=============================================================================
// CONSTRUCTOR: SPELLwsObjectDataHandler::SPELLwsObjectDataHandler
//=============================================================================
SPELLwsObjectDataHandler::SPELLwsObjectDataHandler( PyObject* object )
: SPELLwsDataHandler( SPELLwsData::DATA_GENERIC )
{
	setObject(object);
}

//=============================================================================
// DESTRUCTOR: SPELLwsObjectDataHandler::~SPELLwsObjectDataHandler
//=============================================================================
SPELLwsObjectDataHandler::~SPELLwsObjectDataHandler()
{
	// Do not delete nor DECREF, it is a borrowed reference
	m_object = NULL;
}

//=============================================================================
// METHOD    : SPELLwsObjectDataHandler::write()
//=============================================================================
void SPELLwsObjectDataHandler::write()
{
    assert(m_object != NULL);
	// Storing a generic object means storing the code first, then the object itself.
	getStorage()->storeObject(m_object);
}

//=============================================================================
// METHOD    : SPELLwsObjectDataHandler::read()
//=============================================================================
void SPELLwsObjectDataHandler::read()
{
	m_object = getStorage()->loadObject();
}

//=============================================================================
// METHOD    : SPELLwsObjectDataHandler::storeFakeObject()
//=============================================================================
void SPELLwsObjectDataHandler::storeFakeObject( SPELLwsData::Code code )
{
	switch(code)
	{
	case SPELLwsData::DATA_BYTECODE:
	case SPELLwsData::DATA_GENERIC:
	case SPELLwsData::DATA_NONE:
		getStorage()->storeObject(Py_None);
		break;
	case SPELLwsData::DATA_CLASS:
		getStorage()->storeObject( PyString_FromString("__FAILED_WS_CLASS__"));
		break;
	case SPELLwsData::DATA_DICTIONARY:
		getStorage()->storeObject(PyDict_New());
		break;
	case SPELLwsData::DATA_LIST:
		getStorage()->storeObject(PyList_New(0));
		break;
	default:
		THROW_EXCEPTION("Cannot store fake object", "Unknown handler type: " + ISTR(code), SPELL_ERROR_WSTART);
	}
}
