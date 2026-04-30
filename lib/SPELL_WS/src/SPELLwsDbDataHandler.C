// ################################################################################
// FILE       : SPELLwsDbDataHandler.C
// DATE       : Mar 17, 2011
// PROJECT    : SPELL
// DESCRIPTION: Implementation of the data handler for SPELL databases
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
#include "SPELL_WS/SPELLwsDbDataHandler.H"
#include "SPELL_WS/SPELLwsDataHandlerFactory.H"
// Project includes --------------------------------------------------------
#include "SPELL_UTIL/SPELLlog.H"
#include "SPELL_UTIL/SPELLpythonHelper.H"
// System includes ---------------------------------------------------------


const std::string SPELLwsDbDataHandler::DB_TYPE_FILE = "<DB_FILE>";
const std::string SPELLwsDbDataHandler::DB_TYPE_FILE_SPB = "<DB_FILE_SPB>";
const std::string SPELLwsDbDataHandler::DB_TYPE_SVN = "<DB_SVN>";
const std::string SPELLwsDbDataHandler::DB_TYPE_ANY = "<DB_ANY>";


//=============================================================================
// CONSTRUCTOR: SPELLwsDbDataHandler::SPELLwsDbDataHandler
//=============================================================================
SPELLwsDbDataHandler::SPELLwsDbDataHandler( PyObject* object )
: SPELLwsObjectDataHandler( object )
{
	setCode( SPELLwsData::DATA_SPELL_DICT );
}

//=============================================================================
// DESTRUCTOR: SPELLwsDbDataHandler::~SPELLwsDbDataHandler
//=============================================================================
SPELLwsDbDataHandler::~SPELLwsDbDataHandler()
{
}

//=============================================================================
// METHOD    : SPELLwsDbDataHandler::write()
//=============================================================================
void SPELLwsDbDataHandler::write()
{
	if (getObject() == NULL)
	{
		getStorage()->storeLong( -1 );
		return;
	}

	PyObject* keys = SPELLpythonHelper::instance().callMethod( getObject(), "keys", NULL );
	unsigned int numItems = PyList_Size( keys );

	DEBUG("[DBDH] Storing database items (total " + ISTR(numItems) + ")");
	// Store the number of items
	getStorage()->storeLong( numItems );

	//DEBUG("[DBDH] Storing database type and parameters");
	// Store the type of database. TODO fix this with a better implementation of
	// the database object on the python side
	if (SPELLpythonHelper::instance().isInstance( getObject(), "DatabaseFile", "spell.lib.adapter.databases.dbfile" ))
	{
		//DEBUG("[DBDH] It is a file database");
		// Store the database type
		getStorage()->storeObject( SSTRPY(DB_TYPE_FILE) );
		storeDatabaseFile();
	}
	else if (SPELLpythonHelper::instance().isInstance( getObject(), "DatabaseFileSPB", "spell.lib.adapter.databases.dbfilespb" ))
	{
		//DEBUG("[DBDH] It is a SPB file database");
		// Store the database type
		getStorage()->storeObject( SSTRPY(DB_TYPE_FILE_SPB) );
		storeDatabaseFileSPB();
	}
	else if (SPELLpythonHelper::instance().isInstance( getObject(), "DatabaseSubversion", "spell.lib.adapter.databases.dbsvn" ))
	{
		//DEBUG("[DBDH] It is a SVN file database");
		// Store the database type
		getStorage()->storeObject( SSTRPY(DB_TYPE_SVN) );
		storeDatabaseSubversion();
	}
	else
	{
		THROW_EXCEPTION("Cannot store database", "Unsupported database type: " + PYREPR(getObject()), SPELL_ERROR_WSTART);
	}

	//DEBUG("[DBDH] Storing database items");
	// Load the items
	storeItems( keys, numItems );

	DEBUG("[DBDH] Storing database done");
}

//=============================================================================
// METHOD    : SPELLwsDbDataHandler::read()
//=============================================================================
void SPELLwsDbDataHandler::read()
{
	DEBUG("[DBDH] Loading database");

	// Load the number of items
	int numItems = getStorage()->loadLong();
	DEBUG("[DBDH] Number of items " + ISTR(numItems));

	if (numItems == -1)
	{
		setObject(NULL);
		return;
	}

	// Load the database type
	DEBUG("[DBDH] Loading database type");
	PyObject* pyType = getStorage()->loadObject();
	std::string typeStr = PYSTR(pyType);
	DEBUG("[DBDH] Database type " + typeStr );

	// Load database object
	if (typeStr == DB_TYPE_FILE )
	{
		DEBUG("[DBDH] It is a file database");
		loadDatabaseFile();
	}
	else if (typeStr == DB_TYPE_FILE_SPB)
	{
		loadDatabaseFileSPB();
	}
	else if (typeStr == DB_TYPE_SVN )
	{
		loadDatabaseSubversion();
	}
	else
	{
		THROW_EXCEPTION("Cannot load database", "Unsupported database type: " + typeStr, SPELL_ERROR_WSTART);
	}

	DEBUG("[DBDH] Load database items");
	loadItems(numItems);
}

//=============================================================================
// METHOD    : SPELLwsDbDataHandler::
//=============================================================================
void SPELLwsDbDataHandler::storeItems( PyObject* keys, unsigned int numItems )
{

	PyObject* key = NULL;
	PyObject* item = NULL;

	// Store each list item
	for( unsigned int index = 0; index < numItems; index++)
	{
		key = PyList_GetItem( keys, index );
		item = PyObject_GetItem( getObject(), key );

		//DEBUG("		[DBDH] Key index " + ISTR(index));
		//DEBUG("		[DBDH] Key to use" + PYREPR(key));
		//DEBUG("		[DBDH] Item type:" + PYREPR(PyObject_Type(item)));

		// Handler for the key
		SPELLwsObjectDataHandler keyHandler(key);
		keyHandler.setStorage(getStorage());

		try
		{
			// Create a handler for the item
			SPELLwsDataHandler* handler = SPELLwsDataHandlerFactory::createDataHandler(item);
			handler->setStorage(getStorage());

			// Store the key
			//DEBUG("		[DBDH] Storing key: " + PYREPR(key));
			keyHandler.write();

			// Store the item data code in order to recognise it later
			//DEBUG("		[DBDH] Storing data code: " + SPELLwsData::codeStr(handler->getCode()));
			handler->storeDataCode();

			// IMPORTANT in the case of lists and dictionaries, we want to be able to continue
			// the storage evenif there is a problem in the handler processing at this point.
			// If that is the case, a fake empty object will be replaced by the object being
			// processed by the handler, and the dumping of this collection will continue.
			try
			{
				// Store the value
				//DEBUG("		[DBDH] Storing value: " + PYREPR(item));
				handler->write();
				//DEBUG("		[DBDH] Storing value done");
			}
			catch(SPELLcoreException& ex)
			{
				std::string msg = "WARNING! Storage of element " + ISTR(index) + " failed: " + ex.what();
				LOG_WARN(msg);
				storeFakeObject( handler->getCode() );
			}

			delete handler;
		}
		catch(SPELLcoreException& ex)
		{
			std::string msg = "WARNING! Unable to create handler: " + ex.what();
			LOG_WARN(msg);
			storeFakeObject( SPELLwsData::DATA_NONE );
		}
	}
	//DEBUG("[DBDH] Storing database items done");
}

//=============================================================================
// METHOD    : SPELLwsDbDataHandler::
//=============================================================================
void SPELLwsDbDataHandler::loadItems( unsigned int numItems )
{
	DEBUG("[DBDH] Loading database items");

	for( unsigned int index = 0; index < (unsigned) numItems; index++)
	{
		// We know that first an Object comes, as the key. So make the handler directly.
		SPELLwsObjectDataHandler keyHandler(NULL);
		keyHandler.setStorage(getStorage());
		DEBUG("		[DBDH] Loading key");
		keyHandler.read();
		PyObject* key = keyHandler.getObject();
		DEBUG("		[DBDH] Loaded key " + PYREPR(key));

		// Load the item code
		DEBUG("		[DBDH] Loading data code");
		SPELLwsData::Code code = loadDataCode();
		DEBUG("		[DBDH] Loaded data code " + SPELLwsData::codeStr(code));
		// Create an appropriate handler
		SPELLwsDataHandler* handler = SPELLwsDataHandlerFactory::createDataHandler(code);
		handler->setStorage(getStorage());
		// Read the data
		DEBUG("		[DBDH] Loading value");
		handler->read();
		DEBUG("		[DBDH] Value loaded " + PYREPR(handler->getObject()));
		// Add the item to the dictionary
		PyObject_SetItem(getObject(), key, handler->getObject());
		delete handler;
		DEBUG("     [DBDH] ");
	}

	DEBUG("[DBDH] Database items loaded");
}

//=============================================================================
// METHOD    : SPELLwsDbDataHandler::
//=============================================================================
void SPELLwsDbDataHandler::storeDatabaseFile()
{
	DEBUG("[DBDH] Storing FILE database parameters");
	PyObject* dbName = PyObject_GetAttrString( getObject(), "_name" );
	DEBUG("[DBDH] Name: " + PYREPR(dbName) );
	getStorage()->storeObject( dbName );
	PyObject* dbFile = PyObject_GetAttrString( getObject(), "_filename" );
	DEBUG("[DBDH] File: " + PYREPR(dbFile) );
	getStorage()->storeObject( dbFile );
	PyObject* dbExt = PyObject_GetAttrString( getObject(), "_defaultExt" );
	DEBUG("[DBDH] Ext: " + PYREPR(dbExt) );
	getStorage()->storeObjectOrNone( dbExt );
	DEBUG("[DBDH] File database parameters done");
}

//=============================================================================
// METHOD    : SPELLwsDbDataHandler::
//=============================================================================
void SPELLwsDbDataHandler::storeDatabaseFileSPB()
{
	DEBUG("[DBDH] Storing SPB database parameters");
	PyObject* dbName = PyObject_GetAttrString( getObject(), "_name" );
	DEBUG("[DBDH] Name: " + PYREPR(dbName) );
	getStorage()->storeObject( dbName );
	PyObject* dbFile = PyObject_GetAttrString( getObject(), "_filename" );
	DEBUG("[DBDH] File: " + PYREPR(dbFile) );
	getStorage()->storeObject( dbFile );
	PyObject* dbExt = PyObject_GetAttrString( getObject(), "_defaultExt" );
	DEBUG("[DBDH] Ext: " + PYREPR(dbExt) );
	getStorage()->storeObjectOrNone( dbExt );
	DEBUG("[DBDH] File database parameters done");
}

//=============================================================================
// METHOD    : SPELLwsDbDataHandler::
//=============================================================================
void SPELLwsDbDataHandler::storeDatabaseSubversion()
{
	DEBUG("[DBDH] Storing SVN database parameters");
	PyObject* dbName = PyObject_GetAttrString( getObject(), "_name" );
	DEBUG("[DBDH] Name: " + PYREPR(dbName) );
	getStorage()->storeObject( dbName );
	PyObject* dbFile = PyObject_GetAttrString( getObject(), "_filename" );
	DEBUG("[DBDH] File: " + PYREPR(dbFile) );
	getStorage()->storeObject( dbFile );
	PyObject* dbExt = PyObject_GetAttrString( getObject(), "_defaultExt" );
	DEBUG("[DBDH] Ext: " + PYREPR(dbExt) );
	getStorage()->storeObjectOrNone( dbExt );
	DEBUG("[DBDH] File database parameters done");
}

//=============================================================================
// METHOD    : SPELLwsDbDataHandler::
//=============================================================================
void SPELLwsDbDataHandler::loadDatabaseFile()
{
	DEBUG("[DBDH] Loading FILE database parameters" );
	PyObject* classObject = SPELLpythonHelper::instance().getObject("spell.lib.adapter.databases.dbfile", "DatabaseFile");
	DEBUG("[DBDH] Class is " + PYREPR(classObject) );

	PyObject* dbName = getStorage()->loadObject();
	DEBUG("[DBDH] Name: " + PYREPR(dbName) );
	PyObject* dbFile = getStorage()->loadObject();
	DEBUG("[DBDH] File: " + PYREPR(dbFile) );
	PyObject* dbExt  = getStorage()->loadObject();
	DEBUG("[DBDH] Ext: " + PYREPR(dbExt) );

	PyObject* args = PyTuple_New(3);

	PyTuple_SetItem( args, 0, dbName );
	PyTuple_SetItem( args, 1, dbFile );
	PyTuple_SetItem( args, 2, dbExt );
	Py_INCREF(dbName);
	Py_INCREF(dbFile);
	Py_INCREF(dbExt);
	Py_INCREF(args);

	PyObject* database = PyObject_CallObject( classObject, args );
	Py_INCREF(database);

	setObject(database);
	DEBUG("[DBDH] Loading file database parameters done" );
}

//=============================================================================
// METHOD    : SPELLwsDbDataHandler::
//=============================================================================
void SPELLwsDbDataHandler::loadDatabaseFileSPB()
{
	DEBUG("[DBDH] Loading FILE database parameters" );
	PyObject* classObject = SPELLpythonHelper::instance().getObject("spell.lib.adapter.databases.dbfilespb", "DatabaseFileSPB");
	DEBUG("[DBDH] Class is " + PYREPR(classObject) );

	PyObject* dbName = getStorage()->loadObject();
	DEBUG("[DBDH] Name: " + PYREPR(dbName) );
	PyObject* dbFile = getStorage()->loadObject();
	DEBUG("[DBDH] File: " + PYREPR(dbFile) );
	PyObject* dbExt  = getStorage()->loadObject();
	DEBUG("[DBDH] Ext: " + PYREPR(dbExt) );

	PyObject* args = PyTuple_New(3);

	PyTuple_SetItem( args, 0, dbName );
	PyTuple_SetItem( args, 1, dbFile );
	PyTuple_SetItem( args, 2, dbExt );
	Py_INCREF(dbName);
	Py_INCREF(dbFile);
	Py_INCREF(dbExt);
	Py_INCREF(args);

	PyObject* database = PyObject_CallObject( classObject, args );
	Py_INCREF(database);

	setObject(database);
	DEBUG("[DBDH] Loading file database parameters done" );
}

//=============================================================================
// METHOD    : SPELLwsDbDataHandler::
//=============================================================================
void SPELLwsDbDataHandler::loadDatabaseSubversion()
{
	DEBUG("[DBDH] Loading FILE database parameters" );
	PyObject* classObject = SPELLpythonHelper::instance().getObject("spell.lib.adapter.databases.dbsvn", "DatabaseSubversion");
	DEBUG("[DBDH] Class is " + PYREPR(classObject) );

	PyObject* dbName = getStorage()->loadObject();
	DEBUG("[DBDH] Name: " + PYREPR(dbName) );
	PyObject* dbFile = getStorage()->loadObject();
	DEBUG("[DBDH] File: " + PYREPR(dbFile) );
	PyObject* dbExt  = getStorage()->loadObject();
	DEBUG("[DBDH] Ext: " + PYREPR(dbExt) );

	PyObject* args = PyTuple_New(3);

	PyTuple_SetItem( args, 0, dbName );
	PyTuple_SetItem( args, 1, dbFile );
	PyTuple_SetItem( args, 2, dbExt );
	Py_INCREF(dbName);
	Py_INCREF(dbFile);
	Py_INCREF(dbExt);
	Py_INCREF(args);

	PyObject* database = PyObject_CallObject( classObject, args );
	Py_INCREF(database);

	setObject(database);
	DEBUG("[DBDH] Loading file database parameters done" );
}

