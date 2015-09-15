// ################################################################################
// FILE       : SPELLwsDataHandlerFactory.C
// DATE       : Mar 17, 2011
// PROJECT    : SPELL
// DESCRIPTION: Implementation of the data handler factory
// --------------------------------------------------------------------------------
//
//  Copyright (C) 2008, 2015 SES ENGINEERING, Luxembourg S.A.R.L.
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
#include "SPELL_WS/SPELLwsDataHandlerFactory.H"
#include "SPELL_WS/SPELLwsClassDataHandler.H"
#include "SPELL_WS/SPELLwsInstanceDataHandler.H"
#include "SPELL_WS/SPELLwsCustomTypeDataHandler.H"
#include "SPELL_WS/SPELLwsTmItemDataHandler.H"
#include "SPELL_WS/SPELLwsDictDataHandler.H"
#include "SPELL_WS/SPELLwsListDataHandler.H"
#include "SPELL_WS/SPELLwsTupleDataHandler.H"
#include "SPELL_WS/SPELLwsDbDataHandler.H"
#include "SPELL_WS/SPELLwsTimeDataHandler.H"
#include "SPELL_WS/SPELLwsObjectDataHandler.H"
#include "SPELL_WS/SPELLwsNoneDataHandler.H"
// Project includes --------------------------------------------------------
#include "SPELL_UTIL/SPELLerror.H"
#include "SPELL_UTIL/SPELLpythonHelper.H"
// System includes ---------------------------------------------------------


//=============================================================================
// STATIC : SPELLwsDataHandlerFactory::createDataHandler()
//=============================================================================
SPELLwsDataHandler* SPELLwsDataHandlerFactory::createDataHandler( PyObject* object )
{
	assert(object != NULL);

	SPELLwsDataHandler* handler = NULL;

	//DEBUG("[DHF] Creating handler for object of type " + PYREPR( PyObject_Type(object) ));

	if (PyDict_Check(object))
	{
		//DEBUG("[DHF] Object is a dictionary");
		handler = new SPELLwsDictDataHandler(object);
	}
	else if (PyList_Check(object))
	{
		//DEBUG("[DHF] Object is a list");
		handler = new SPELLwsListDataHandler(object);
	}
	else if (PyTuple_Check(object))
	{
		//DEBUG("[DHF] Object is a tuple");
		handler = new SPELLwsTupleDataHandler(object);
	}
	else if ( Py_None == object )
	{
		//DEBUG("[DHF] Object is None");
		handler = new SPELLwsNoneDataHandler();
	}
	else if (SPELLpythonHelper::instance().isDatabase(object))
	{
		//DEBUG("[DHF] Object is database");
		handler = new SPELLwsDbDataHandler(object);
	}
	else if (SPELLpythonHelper::instance().isTime(object))
	{
		//DEBUG("[DHF] Object is TIME");
		handler = new SPELLwsTimeDataHandler(object);
	}
	else if (PyClass_Check(object))
	{
		DEBUG("[DHF] Object is a class: " + PYCREPR(object));
		handler = new SPELLwsClassDataHandler(object);
	}
	else if (PyInstance_Check(object))
	{
		//DEBUG("[DHF] Object is a instance: " + PYCREPR(object));
		handler = new SPELLwsInstanceDataHandler(object);
	}
	else if (SPELLpythonHelper::instance().isSubclassInstance(object, "TmItemClass", "spell.lib.adapter.tm_item"))
	{
		//DEBUG("[DHF] Object is a TM item: " + PYCREPR(object));
		handler = new SPELLwsTmItemDataHandler(object);
	}
	// Look discussion at mail.python.org/pipermail/python-dev/2004-July/046074.html
	else if ((object->ob_type->tp_flags & Py_TPFLAGS_HEAPTYPE)>0)
	{
		DEBUG("[DHF] Object is a custom type: " + PYCREPR(object));
		handler = new SPELLwsCustomTypeDataHandler(object);
	}
	else
	{
		//DEBUG("[DHF] Default to object handler: " + PYCREPR(object));
		handler = new SPELLwsObjectDataHandler(object);
	}
	return handler;
}

//=============================================================================
// STATIC : SPELLwsDataHandlerFactory::createDataHandler()
//=============================================================================
SPELLwsDataHandler* SPELLwsDataHandlerFactory::createDataHandler( SPELLwsData::Code code )
{
	SPELLwsDataHandler* handler = NULL;

	DEBUG("[DHF] Creating handler for object of code " + SPELLwsData::codeStr(code) );

	switch(code)
	{
	case SPELLwsData::DATA_BYTECODE:
	case SPELLwsData::DATA_GENERIC:
		handler = new SPELLwsObjectDataHandler(NULL);
		break;
	case SPELLwsData::DATA_CLASS:
		handler = new SPELLwsClassDataHandler(NULL);
		break;
	case SPELLwsData::DATA_INSTANCE:
		handler = new SPELLwsInstanceDataHandler(NULL);
		break;
	case SPELLwsData::DATA_CUSTOM_TYPE:
		handler = new SPELLwsCustomTypeDataHandler(NULL);
		break;
	case SPELLwsData::DATA_DICTIONARY:
		handler = new SPELLwsDictDataHandler(NULL);
		break;
	case SPELLwsData::DATA_LIST:
		handler = new SPELLwsListDataHandler(NULL);
		break;
	case SPELLwsData::DATA_TUPLE:
		handler = new SPELLwsTupleDataHandler(NULL);
		break;
	case SPELLwsData::DATA_SPELL_DICT:
		handler = new SPELLwsDbDataHandler(NULL);
		break;
	case SPELLwsData::DATA_SPELL_TIME:
		handler = new SPELLwsTimeDataHandler(NULL);
		break;
	case SPELLwsData::DATA_TM_ITEM:
		handler = new SPELLwsTmItemDataHandler(NULL);
		break;
	case SPELLwsData::DATA_NONE:
		handler = new SPELLwsNoneDataHandler();
		break;
	default:
		THROW_EXCEPTION("Cannot create handler", "Unknown handler type: " + ISTR(code), SPELL_ERROR_WSTART);
	}

	return handler;
}
