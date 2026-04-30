// ################################################################################
// FILE       : SPELLwsInstanceDataHandler.C
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
#include "SPELL_WS/SPELLwsInstanceDataHandler.H"
#include "SPELL_WS/SPELLwsClassDataHandler.H"
#include "SPELL_WS/SPELLwsDictDataHandler.H"
// Project includes --------------------------------------------------------
#include "SPELL_UTIL/SPELLlog.H"
#include "SPELL_UTIL/SPELLpythonHelper.H"
// System includes ---------------------------------------------------------

//=============================================================================
// CONSTRUCTOR: SPELLwsInstanceDataHandler::SPELLwsInstanceDataHandler
//=============================================================================
SPELLwsInstanceDataHandler::SPELLwsInstanceDataHandler( PyObject* object )
: SPELLwsObjectDataHandler( object )
{
	/** \todo Review the need of storing additional information */
	setCode( SPELLwsData::DATA_INSTANCE );
}

//=============================================================================
// DESTRUCTOR: SPELLwsInstanceDataHandler::~SPELLwsInstanceDataHandler
//=============================================================================
SPELLwsInstanceDataHandler::~SPELLwsInstanceDataHandler()
{
}

//=============================================================================
// METHOD    : SPELLwsInstanceDataHandler::write()
//=============================================================================
void SPELLwsInstanceDataHandler::write()
{
	assert( PyInstance_Check(getObject()) );

	PyInstanceObject* instanceObject = reinterpret_cast<PyInstanceObject*>(getObject());
	PyObject* classObject = reinterpret_cast<PyObject*>(instanceObject->in_class);


	SPELLwsClassDataHandler classHandler( classObject );
	DEBUG("[IDH] Storing class: " + PYREPR(classObject) );
	classHandler.setStorage(getStorage());
	// Store the instance class. We dont need a data code, we know what is there
	classHandler.write();

	DEBUG("[IDH] Storing dictionary: " + PYREPR(instanceObject->in_dict) );
	SPELLwsDictDataHandler dictHandler( instanceObject->in_dict );
	dictHandler.setStorage(getStorage());
	// Store the instance dictionary
	dictHandler.write();

	DEBUG("[IDH] Storing instance done" );
}

//=============================================================================
// METHOD    : SPELLwsInstanceDataHandler::read()
//=============================================================================
void SPELLwsInstanceDataHandler::read()
{
	// Store the class name
	DEBUG("[IDH] Loading class");
	SPELLwsClassDataHandler classHandler( NULL );
	classHandler.setStorage(getStorage());
	// Read the data
	classHandler.read();
	DEBUG("[IDH] Class loaded " + PYREPR(classHandler.getObject()));

	DEBUG("[IDH] Loading dictionary");
	SPELLwsDictDataHandler dictHandler( NULL );
	dictHandler.setStorage(getStorage());
	// Read the data
	dictHandler.read();
	DEBUG("[IDH] Dictionary loaded " + PYREPR(dictHandler.getObject()));

	// Create the instance object
	PyObject* instanceObject = PyInstance_NewRaw( classHandler.getObject(), dictHandler.getObject() );

	// Set it as associated object
	setObject( instanceObject );
	DEBUG("[IDH] Loading instance done");
}

