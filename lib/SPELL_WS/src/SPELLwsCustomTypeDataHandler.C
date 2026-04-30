// ################################################################################
// FILE       : SPELLwsCustomTypeDataHandler.C
// DATE       : Mar 17, 2011
// PROJECT    : SPELL
// DESCRIPTION: Implementation of the data handler for instances
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
#include "SPELL_WS/SPELLwsCustomTypeDataHandler.H"
#include "SPELL_WS/SPELLwsClassDataHandler.H"
#include "SPELL_WS/SPELLwsDataHandlerFactory.H"
// Project includes --------------------------------------------------------
#include "SPELL_EXC/SPELLexecutor.H"
#include "SPELL_UTIL/SPELLlog.H"
#include "SPELL_UTIL/SPELLpythonHelper.H"
// System includes ---------------------------------------------------------

//=============================================================================
// CONSTRUCTOR: SPELLwsCustomTypeDataHandler::SPELLwsCustomTypeDataHandler
//=============================================================================
SPELLwsCustomTypeDataHandler::SPELLwsCustomTypeDataHandler( PyObject* object )
: SPELLwsObjectDataHandler( object )
{
	/** \todo Review the need of storing additional information */
	setCode( SPELLwsData::DATA_CUSTOM_TYPE );
}

//=============================================================================
// DESTRUCTOR: SPELLwsCustomTypeDataHandler::~SPELLwsCustomTypeDataHandler
//=============================================================================
SPELLwsCustomTypeDataHandler::~SPELLwsCustomTypeDataHandler()
{
}

//=============================================================================
// METHOD    : SPELLwsCustomTypeDataHandler::write()
//=============================================================================
void SPELLwsCustomTypeDataHandler::write()
{
	// This case applies for all 'new instance' objects in Python.
	// We would:
	// 1. Determine the object class, accessible via the type field of the object structure
	// 2. Inspect all the elements given by the dir() of the type
	// 3. For those elements which are data members, store/load the values
	// Also storing the class object is required for the restoration process.
	//
	// BUT, IMPORTANT NOTE: we need to use actually the weak reference to the type object, not the
	// type object itself. Getting attributes from the type directly will not provide correct
	// values, we need to get attributes from the weak reference. See differences in the following
	// (commented) test code.
	//
	//
	// PENDING PROBLEM: store/recover the type class.
	//
	/*
		PyObject* obj = getObject();
		std::string module = PYSSTR( PyDict_GetItemString(obj->ob_type->tp_dict, "__module__" ) );
		std::string className = module + "." + obj->ob_type->tp_name;

		std::cerr << "@@@@@@@@@ OBJECT TYPE " << PYCREPR( obj->ob_type ) << " at " << PSTR(obj->ob_type) << std::endl;
		std::cerr << "@@@@@@@@@ CLASS       " << className << std::endl;
		std::cerr << "@@@@@@@@@ TYPE BASE   " << PYCREPR( obj->ob_type->tp_base ) << std::endl;
		std::cerr << "@@@@@@@@@ TYPE NAME   " << obj->ob_type->tp_name << std::endl;
		std::cerr << "@@@@@@@@@ WEAK REFS   " << PYCREPR( obj->ob_type->tp_weaklist ) << std::endl;
		std::cerr << "@@@@@@@@@ WEAK REF OF " << obj->ob_type->tp_weaklistoffset << std::endl;
		// To obtain the class:
		PyObject* ref = PyWeakref_GetObject(obj->ob_type->tp_weaklist);
		std::cerr << "@@@@@@@@@ WR RESOLVE  " << PYCREPR( ref ) << ", type " << PYCREPR(PyObject_Type(ref)) << " at " << PSTR(ref) << std::endl;
		std::cerr << "@@@@@@@@@ WR DIR      " << PYCREPR( PyObject_Dir(ref) ) << std::endl;
		std::cerr << "@@@@@@@@@ WR DATA     " << PYCREPR( PyObject_GetAttr(obj, STRPY("member1")) ) << std::endl;
		std::cerr << "@@@@@@@@@ TYPE DIR    " << PYCREPR( PyObject_Dir( (PyObject*) obj->ob_type ) ) << std::endl;
		std::cerr << "@@@@@@@@@ TYPE DATA   " << PYCREPR( PyObject_GetAttr( (PyObject*)obj->ob_type, STRPY("member1")) ) << std::endl;
	*/
}

//=============================================================================
// METHOD    : SPELLwsCustomTypeDataHandler::read()
//=============================================================================
void SPELLwsCustomTypeDataHandler::read()
{
	////////////////////////////////////////////////////////////
	// IMPORTANT NOTE: read comments in the write method()
	////////////////////////////////////////////////////////////
}

