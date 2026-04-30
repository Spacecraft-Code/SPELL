// ################################################################################
// FILE       : SPELLwsDictDataHandler.C
// DATE       : Mar 17, 2011
// PROJECT    : SPELL
// DESCRIPTION: Implementation of the data handler for dictionaries
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
#include "SPELL_WS/SPELLwsDictDataHandler.H"
#include "SPELL_WS/SPELLwsDataHandlerFactory.H"
#include "SPELL_WS/SPELLwsWarmStartImpl.H"
// Project includes --------------------------------------------------------
#include "SPELL_UTIL/SPELLlog.H"
#include "SPELL_EXC/SPELLexecutor.H"
#include "SPELL_WRP/SPELLconstants.H"
#include "SPELL_UTIL/SPELLpythonHelper.H"
#include "SPELL_WRP/SPELLpyHandle.H"
// System includes ---------------------------------------------------------

//=============================================================================
// CONSTRUCTOR: SPELLwsDictDataHandler::SPELLwsDictDataHandler
//=============================================================================
SPELLwsDictDataHandler::SPELLwsDictDataHandler( PyObject* object )
: SPELLwsObjectDataHandler( object )
{
	/** \todo Review the need of storing additional information */
	setCode( SPELLwsData::DATA_DICTIONARY );
}

//=============================================================================
// DESTRUCTOR: SPELLwsDictDataHandler::~SPELLwsDictDataHandler
//=============================================================================
SPELLwsDictDataHandler::~SPELLwsDictDataHandler()
{
}

//=============================================================================
// METHOD    : SPELLwsDictDataHandler::write()
//=============================================================================
void SPELLwsDictDataHandler::write()
{
	if (getObject() == NULL)
	{
		getStorage()->storeLong( -1 );
		return;
	}

	assert( PyDict_Check(getObject()));

	SPELLpyHandle keys = PyDict_Keys( getObject() );
	unsigned int numItems = PyList_Size( keys.get() );

	DEBUG("[DDH] Storing dictionary items (total " + ISTR(numItems) + ") address " + PSTR(getObject()));

	PyObject* key = NULL;
	PyObject* item = NULL;

	long toStore = 0;
	// Calculate the number of items to store
	// Store each list item
	DEBUG("[DDH] Keys of the dictionary:");
	for( unsigned int index = 0; index < numItems; index++)
	{
		key = PyList_GetItem( keys.get(), index );
		item = PyDict_GetItem( getObject(), key );
		if (SPELLwsWarmStartImpl::shouldFilter(key,item))
		{
			continue;
		}
		else
		{
			DEBUG("     - " + PYSSTR(key));
		}
		toStore++;
	}

	DEBUG("[DDH] Will store " + ISTR(toStore) + " keys");
	// Store the number of items
	getStorage()->storeLong( (long) toStore );

	DEBUG("[DDH] Start checking items");
	if (toStore>0)
	{
		// Store each list item
		for( unsigned int index = 0; index < numItems; index++)
		{
			key = PyList_GetItem( keys.get(), index );
			item = PyDict_GetItem( getObject(), key );

			DEBUG("[DDH] Checking item " + PYCREPR(key));

			// Do not consider filtered values
			if (SPELLwsWarmStartImpl::shouldFilter(key,item)) continue;

			SPELLpythonHelper::instance().checkError();

			DEBUG("		[DDH] Key index " + ISTR(index));
			DEBUG("		[DDH] Key to use" + PYREPR(key));
			DEBUG("		[DDH] Item type:" + PYREPR(PyObject_Type(item)));

			// Handler for the key
			SPELLwsObjectDataHandler keyHandler(key);
			keyHandler.setStorage(getStorage());

			DEBUG("		[DDH] Creating handler");

			// Create a handler for the item
			SPELLwsDataHandler* handler = SPELLwsDataHandlerFactory::createDataHandler(item);
			handler->setStorage(getStorage());

			// Store the key
			DEBUG("		[DDH] Storing key: " + PYREPR(key));
			keyHandler.write();

			// Store the item data code in order to recognise it later
			DEBUG("		[DDH] Storing data code: " + SPELLwsData::codeStr(handler->getCode()));
			handler->storeDataCode();

			// IMPORTANT in the case of lists and dictionaries, we want to be able to continue
			// the storage even if there is a problem in the handler processing at this point.
			// If that is the case, a fake empty object will be replaced by the object being
			// processed by the handler, and the dumping of this collection will continue.
			try
			{
				if (handler->getCode() == SPELLwsData::DATA_CUSTOM_TYPE )
				{
					std::string msg = "WARNING! warm start not supported for custom Python types (" + PYREPR(key) + "=" + PYREPR(item) + ")";
					LOG_WARN(msg);
					SPELLexecutor::instance().getCIF().warning(msg);
					storeFakeObject( SPELLwsData::DATA_NONE );
				}
				else
				{
					// Store the value
					DEBUG("		[DDH] Storing value: " + PYREPR(item));
					handler->write();
					DEBUG("		[DDH] Storing value done");
				}
			}
			catch(SPELLcoreException& ex)
			{
				std::string msg = "WARNING! WS storage of element " + ISTR(index) + " failed: " + ex.what();
				LOG_WARN(msg);
				SPELLexecutor::instance().getCIF().warning(msg);
				storeFakeObject( handler->getCode() );
			}
			delete handler;
		}
	}

	DEBUG("[DDH] Storing dictionary done");
}

//=============================================================================
// METHOD    : SPELLwsDictDataHandler::read()
//=============================================================================
void SPELLwsDictDataHandler::read()
{
	DEBUG("[DDH] Loading dictionary items");
	// Load the number of items
	int numItems = getStorage()->loadLong();
	DEBUG("[DDH] Number of items " + ISTR(numItems));

	if (numItems == -1)
	{
		setObject(NULL);
		return;
	}

	// Create a dictionary
	PyObject* dictObject = PyDict_New();

	for( unsigned int index = 0; index < (unsigned) numItems; index++)
	{
		// We know that first an Object comes, as the key. So make the handler directly.
		SPELLwsObjectDataHandler keyHandler(NULL);
		keyHandler.setStorage(getStorage());
		DEBUG("		[DDH] Loading key");
		keyHandler.read();
		PyObject* key = keyHandler.getObject();
		DEBUG("		[DDH] Loaded key " + PYREPR(key));

		// Load the item code
		DEBUG("		[DDH] Loading data code");
		SPELLwsData::Code code = loadDataCode();
		DEBUG("		[DDH] Loaded data code " + SPELLwsData::codeStr(code));

		if (code == SPELLwsData::DATA_CUSTOM_TYPE )
		{
			std::string msg = "WARNING! warm start not supported for custom Python types (" + PYREPR(key) + ")";
			LOG_WARN(msg);
			SPELLexecutor::instance().getCIF().warning(msg);
			getStorage()->loadObject(); // Load none
			PyDict_SetItem(dictObject, key, Py_None);
		}
		else
		{
			// Create an appropriate handler
			SPELLwsDataHandler* handler = SPELLwsDataHandlerFactory::createDataHandler(code);
			handler->setStorage(getStorage());
			try
			{
				// Read the data
				DEBUG("		[DDH] Loading value");
				handler->read();
				DEBUG("		[DDH] Value loaded " + PYREPR(handler->getObject()));
				// Add the item to the dictionary
				PyDict_SetItem(dictObject, key, handler->getObject());
			}
			catch(SPELLcoreException& ex)
			{
				std::string msg = "Failed to recover dictionary item " + PYREPR(key) + ": " + ex.what();
				LOG_WARN(msg);
				SPELLexecutor::instance().getCIF().warning(msg);
				PyDict_SetItem(dictObject, key, Py_None);
			}
			delete handler;
			DEBUG("     [DDH] ");
		}
	}

	DEBUG("[DDH] Dictionary loaded");

	// Set it as associated object
	setObject( dictObject );
}
