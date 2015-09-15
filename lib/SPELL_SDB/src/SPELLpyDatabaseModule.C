// ################################################################################
// FILE       : SPELLpyDatabaseModule.C
// DATE       : Mar 17, 2011
// PROJECT    : SPELL
// DESCRIPTION: Implementation of Python database module
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
// Project includes --------------------------------------------------------
// Local includes ----------------------------------------------------------
#include "SPELL_SDB/SPELLpyDatabaseObject.H"
// System includes ---------------------------------------------------------
#include "object.h"

// FORWARD REFERENCES //////////////////////////////////////////////////////
// TYPES ///////////////////////////////////////////////////////////////////
// DEFINES /////////////////////////////////////////////////////////////////
// GLOBALS /////////////////////////////////////////////////////////////////

//============================================================================
// Object member specification
//============================================================================
static PyMemberDef SPELLpyDatabase_Members[] =
{
    {NULL} /* Sentinel */
};

//============================================================================
// Object method specification
//============================================================================
static PyMethodDef SPELLpyDatabase_Methods[] =
{
    {"create",      SPELLpyDatabase_Create,       METH_NOARGS,  "Create the database"},
    {"load",        SPELLpyDatabase_Load,         METH_NOARGS,  "Load the database data"},
    {"reload",      SPELLpyDatabase_Reload,       METH_NOARGS,  "Reload the database data"},
    {"id",      	SPELLpyDatabase_Id,           METH_NOARGS,  "Get the database identifier"},
    {"commit",      SPELLpyDatabase_Commit,       METH_NOARGS,  "Commit the database changes"},
    {"set",      	SPELLpyDatabase_Set,          METH_VARARGS, "Set a database value"},
    {"get",      	SPELLpyDatabase_Get,          METH_VARARGS, "Get a database value"},
    {"has_key",   	SPELLpyDatabase_HasKey,       METH_VARARGS, "Check if the key exists"},
    {"keys",      	SPELLpyDatabase_Keys,         METH_NOARGS,  "Obtain the database keys"},
    {NULL, NULL, 0, NULL} /* Sentinel */
};

//============================================================================
// Object method specification
//============================================================================
static PyMappingMethods SPELLpyDatabase_MappingMethods =
{
	(lenfunc)SPELLpyDatabase_DictLength, /*mp_length*/
	(binaryfunc)SPELLpyDatabase_DictSubscript, /*mp_subscript*/
	(objobjargproc)SPELLpyDatabase_DictAssSub, /*mp_ass_subscript*/
};

DATABASE_DEFINITION( SPELLpyDatabase_Type, "spell.adapter.databases.Database", "Basic database" );
DATABASE_DEFINITION( SPELLpyDatabaseFile_Type, "spell.adapter.databases.DatabaseFile", "File database" );
DATABASE_DEFINITION( SPELLpyDatabaseFileSPB_Type, "spell.adapter.databases.DatabaseFileSPB", "SPB file database" );

//////////////////////////////////////////////////////////////////////////////
// PYTHON MODULE INITIALIZATION
//////////////////////////////////////////////////////////////////////////////
PyMODINIT_FUNC
initlibSPELL_SDB(void)
{
    // Will hold the Python module
    PyObject* module;

    // Allocate the new type for SPELL basic database objects
    DATABASE_TYPE_READY(SPELLpyDatabase_Type);
    DATABASE_TYPE_READY(SPELLpyDatabaseFile_Type);
    DATABASE_TYPE_READY(SPELLpyDatabaseFileSPB_Type);

    // Initialize the module
    module = Py_InitModule3("libSPELL_SDB", NULL, "Module for SPELL databases");

    // Add the classes to the module dictionary
    DATABASE_TYPE_LOAD(module, SPELLpyDatabase_Type, "Database" );
    DATABASE_TYPE_LOAD(module, SPELLpyDatabaseFile_Type, "DatabaseFile" );
    DATABASE_TYPE_LOAD(module, SPELLpyDatabaseFileSPB_Type, "DatabaseFileSPB" );
}
