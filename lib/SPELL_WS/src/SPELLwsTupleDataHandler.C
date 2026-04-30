// ################################################################################
// FILE       : SPELLwsTupleDataHandler.C
// DATE       : Mar 17, 2011
// PROJECT    : SPELL
// DESCRIPTION: Implementation of the data handler for lists
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
#include "SPELL_WS/SPELLwsTupleDataHandler.H"
#include "SPELL_WS/SPELLwsDataHandlerFactory.H"
// Project includes --------------------------------------------------------
#include "SPELL_UTIL/SPELLlog.H"
#include "SPELL_UTIL/SPELLpythonHelper.H"
// System includes ---------------------------------------------------------

//=============================================================================
// CONSTRUCTOR: SPELLwsTupleDataHandler::SPELLwsTupleDataHandler
//=============================================================================
SPELLwsTupleDataHandler::SPELLwsTupleDataHandler( PyObject* object )
: SPELLwsObjectDataHandler( object )
{
	/** \todo Review the need of storing additional information */
	setCode( SPELLwsData::DATA_TUPLE );
}

//=============================================================================
// DESTRUCTOR: SPELLwsTupleDataHandler::~SPELLwsTupleDataHandler
//=============================================================================
SPELLwsTupleDataHandler::~SPELLwsTupleDataHandler()
{
}

//=============================================================================
// METHOD    : SPELLwsTupleDataHandler::write()
//=============================================================================
void SPELLwsTupleDataHandler::write()
{
	assert( PyTuple_Check(getObject()));

	unsigned int numItems = PyTuple_Size( getObject() );

	// Store the number of items
	getStorage()->storeLong( (long) numItems );

	// Store each tuple item
	for( unsigned int index = 0; index < numItems; index++)
	{
		PyObject* item = PyTuple_GetItem( getObject(), index );

		try
		{
			SPELLwsDataHandler* handler = SPELLwsDataHandlerFactory::createDataHandler(item);
			handler->setStorage(getStorage());
			// Store the item data code. We need it for reading operation, in order to
			// know which type of handler to create.
			handler->storeDataCode();

			// IMPORTANT in the case of lists and dictionaries, we want to be able to continue
			// the storage evenif there is a problem in the handler processing at this point.
			// If that is the case, a fake empty object will be replaced by the object being
			// processed by the handler, and the dumping of this collection will continue.
			try
			{
				// Store the data
				handler->write();
			}
			catch(SPELLcoreException& ex)
			{
				std::string msg = "WARNING! Storage of element " + ISTR(index) + " failed: " + ex.what();
				LOG_WARN(msg);
				std::cerr << msg << std::endl;
				storeFakeObject( handler->getCode() );
			}
			delete handler;
		}
		catch(SPELLcoreException& ex)
		{
			std::string msg = "WARNING! Failed to create data handler: " + ex.what();
			LOG_WARN(msg);
			storeFakeObject( SPELLwsData::DATA_NONE );
		}

	}
}

//=============================================================================
// METHOD    : SPELLwsTupleDataHandler::read()
//=============================================================================
void SPELLwsTupleDataHandler::read()
{
	// Load the number of items
	unsigned int numItems = getStorage()->loadLong();

	// Create a tuple
	PyObject* listObject = PyTuple_New(numItems);

	for( unsigned int index = 0; index < numItems; index++)
	{
		// Load the item code
		SPELLwsData::Code code = loadDataCode();
		// Create an appropriate handler
		SPELLwsDataHandler* handler = SPELLwsDataHandlerFactory::createDataHandler(code);
		handler->setStorage(getStorage());
		// Read the data
		handler->read();
		// Add the item to the list
		PyTuple_SetItem(listObject, index, handler->getObject());
		delete handler;
	}

	// Set it as associated object
	setObject( listObject );
}
