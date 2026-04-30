// ################################################################################
// FILE       : SPELLwsClassDataHandler.C
// DATE       : Mar 17, 2011
// PROJECT    : SPELL
// DESCRIPTION: Implementation of the data handler for classes
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
#include "SPELL_WS/SPELLwsClassDataHandler.H"
#include "SPELL_WS/SPELLwsDictDataHandler.H"
// Project includes --------------------------------------------------------
#include "SPELL_UTIL/SPELLlog.H"
#include "SPELL_UTIL/SPELLpythonHelper.H"
// System includes ---------------------------------------------------------

//=============================================================================
// CONSTRUCTOR: SPELLwsClassDataHandler::SPELLwsClassDataHandler
//=============================================================================
SPELLwsClassDataHandler::SPELLwsClassDataHandler( PyObject* object )
: SPELLwsObjectDataHandler( object )
{
	/** \todo Review the need of storing additional information */
	setCode( SPELLwsData::DATA_CLASS );
}

//=============================================================================
// DESTRUCTOR: SPELLwsClassDataHandler::~SPELLwsClassDataHandler
//=============================================================================
SPELLwsClassDataHandler::~SPELLwsClassDataHandler()
{
}

//=============================================================================
// METHOD    : SPELLwsClassDataHandler::write()
//=============================================================================
void SPELLwsClassDataHandler::write()
{
	assert( PyClass_Check(getObject()) );

	PyClassObject* classObject = reinterpret_cast<PyClassObject*>(getObject());

	DEBUG("[CDH] Storing class name: " + PYREPR(classObject->cl_name) );
	SPELLwsObjectDataHandler nameHandler( classObject->cl_name );
	nameHandler.setStorage(getStorage());

	//DEBUG("[CDH] Storing dictionary: " + PYREPR(classObject->cl_dict));
	SPELLwsDictDataHandler dictHandler( classObject->cl_dict );
	dictHandler.setStorage(getStorage());

	// Store the class name, We dont need a data code, we know what is there
	nameHandler.write();
	// Store the class dictionary
	dictHandler.write();
	//DEBUG("[CDH] Storing class done" );
}

//=============================================================================
// METHOD    : SPELLwsClassDataHandler::read()
//=============================================================================
void SPELLwsClassDataHandler::read()
{
	// Store the class name
	SPELLwsObjectDataHandler nameHandler( NULL );
	nameHandler.setStorage(getStorage());
	SPELLwsDictDataHandler dictHandler( NULL );
	dictHandler.setStorage(getStorage());

	// Read the data
	DEBUG("[CDH] Reading name");
	nameHandler.read();
	DEBUG("[CDH] Name read: " + PYREPR(nameHandler.getObject()));

	PyObject* classObject = NULL;
	if ( PYSTR(nameHandler.getObject()) == "__FAILED_WS_CLASS__" )
	{
		classObject = PyClass_New( NULL, PyDict_New(), nameHandler.getObject() );
		std::string msg = "Detected failed WS class, assuming error during marshal process";
		LOG_ERROR(msg);
		std::cerr << msg << std::endl;
	}
	else
	{
		DEBUG("[CDH] Reading dictionary");
		dictHandler.read();
		DEBUG("[CDH] Dictionary read: " + PYREPR(dictHandler.getObject()));
		// Create the class object
		classObject = PyClass_New( NULL, dictHandler.getObject(), nameHandler.getObject() );
	}
	// Set it as associated object
	setObject( classObject );
}

