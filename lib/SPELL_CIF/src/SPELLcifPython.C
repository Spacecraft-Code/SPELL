// ################################################################################
// FILE       : SPELLcifPython.C
// DATE       : Mar 17, 2011
// PROJECT    : SPELL
// DESCRIPTION: Python bindings for CIF object
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
#include "SPELL_CIF/SPELLcif.H"
#include "SPELL_CIF/SPELLpromptDefinition.H"
#include "SPELL_CIF/SPELLcifHelper.H"
// Project includes --------------------------------------------------------
#include "SPELL_EXC/SPELLexecutor.H"
#include "SPELL_UTIL/SPELLlog.H"
#include "SPELL_UTIL/SPELLpyArgs.H"
#include "SPELL_WRP/SPELLregistry.H"
#include "SPELL_UTIL/SPELLpythonHelper.H"
#include "SPELL_WRP/SPELLconstants.H"
// System includes ---------------------------------------------------------
#include "structmember.h"



//###############################################################################
//# UI notification type values
static const std::string DATA_NOTIF_TYPE_ITEM("ITEM");
static const std::string DATA_NOTIF_TYPE_VAL("VALUE");
static const std::string DATA_NOTIF_TYPE_VERIF("VERIFICATION");
static const std::string DATA_NOTIF_TYPE_EXEC("EXECUTION");
static const std::string DATA_NOTIF_TYPE_SYS("SYSTEM");
static const std::string DATA_NOTIF_TYPE_TIME("TIME");


//============================================================================
// FUNCTION        : StrToBoolean
// DESCRIPTION    : Translate strings with boolean meaning to boolean value
//============================================================================
bool StrToBoolean( std::string str )
{
    if (str == "TRUE" || str == "True" || str == "Y" || str == "O" ) return true;
    return false;
}

//============================================================================
// FUNCTION        : ClientIF_Init
// DESCRIPTION    : Initialized of the ClientIF python object
//============================================================================
static int ClientIF_Init( PyClientIFObject* self, PyObject* args, PyObject* kwds );
//============================================================================
// FUNCTION        : ClientIF_Dealloc
// DESCRIPTION    : Cleanup of the ClientIF python object
//============================================================================
static void ClientIF_Dealloc( PyClientIFObject* self );
//============================================================================
// FUNCTION        : ClientIF_New
// DESCRIPTION    : Constructor of the ClientIF python object
//============================================================================
static PyObject* ClientIF_New( PyTypeObject* type, PyObject* args, PyObject* kwds );
//============================================================================
// FUNCTION        : ClientIF_SetVerbosity
// DESCRIPTION    : Configure the CIF verbosity
//============================================================================
static PyObject* ClientIF_SetVerbosity( PyObject* self, PyObject* args );
//============================================================================
// FUNCTION        : ClientIF_ResetVerbosity
// DESCRIPTION    : Reset the CIF verbosity
//============================================================================
static PyObject* ClientIF_ResetVerbosity( PyObject* self, PyObject* args );
//============================================================================
// FUNCTION        : ClientIF_Write
// DESCRIPTION    : Write a message
//============================================================================
static PyObject* ClientIF_Write( PyObject* self, PyObject* args );
//============================================================================
// FUNCTION        : ClientIF_Notify
// DESCRIPTION    : Send a notification
//============================================================================
static PyObject* ClientIF_Notify( PyObject* self, PyObject* args, PyObject* kwds );
//============================================================================
// FUNCTION        : ClientIF_Prompt
// DESCRIPTION    : Send a prompt
//============================================================================
static PyObject* ClientIF_Prompt( PyObject* self, PyObject* args, PyObject* kwds );
//============================================================================
// FUNCTION        : ClientIF_GetSharedData
// DESCRIPTION     : Get shared variables
//============================================================================
static PyObject* ClientIF_GetSharedData( PyObject* self, PyObject* args, PyObject* kwds  );
//============================================================================
// FUNCTION        : ClientIF_SetSharedData
// DESCRIPTION     : Set shared variables
//============================================================================
static PyObject* ClientIF_SetSharedData( PyObject* self, PyObject* args, PyObject* kwds  );
//============================================================================
// FUNCTION        : ClientIF_GetSharedDataKeys
// DESCRIPTION     : Get shared variables names
//============================================================================
static PyObject* ClientIF_GetSharedDataKeys( PyObject* self, PyObject* args, PyObject* kwds  );
//============================================================================
// FUNCTION        : ClientIF_ClearSharedData
// DESCRIPTION     : Remove shared variables
//============================================================================
static PyObject* ClientIF_ClearSharedData( PyObject* self, PyObject* args, PyObject* kwds  );
//============================================================================
// FUNCTION        : ClientIF_AddSharedDataScope
// DESCRIPTION     : Add shared variable scope
//============================================================================
static PyObject* ClientIF_AddSharedDataScope( PyObject* self, PyObject* args, PyObject* kwds  );
//============================================================================
// FUNCTION        : ClientIF_RemoveSharedDataScope
// DESCRIPTION     : Remove shared variables scope
//============================================================================
static PyObject* ClientIF_RemoveSharedDataScope( PyObject* self, PyObject* args, PyObject* kwds  );
//============================================================================
// FUNCTION        : ClientIF_GetSharedDataScopes
// DESCRIPTION     : Get shared variable scopes
//============================================================================
static PyObject* ClientIF_GetSharedDataScopes( PyObject* self, PyObject* args, PyObject* kwds  );
//============================================================================
// FUNCTION        : ClientIF_RemoveSharedDataScopes
// DESCRIPTION     : Remove ALL shared variables scopes
//============================================================================
static PyObject* ClientIF_RemoveSharedDataScopes( PyObject* self, PyObject* args, PyObject* kwds  );




//============================================================================
// ClientIF object member specification
//============================================================================
static PyMemberDef ClientIF_Members[] =
{
    {NULL} /* Sentinel */
};

//============================================================================
// ClientIF object method specification
//============================================================================
static PyMethodDef ClientIF_Methods[] =
{
    {"setVerbosity",           ClientIF_SetVerbosity,           METH_VARARGS, "Change CIF verbosity"},
    {"resetVerbosity",         ClientIF_ResetVerbosity,         METH_NOARGS,  "Reset CIF verbosity"},
    {"write",                  ClientIF_Write,                  METH_VARARGS, "Write a message"},
    {"notify",                 (PyCFunction)ClientIF_Notify,    METH_VARARGS | METH_KEYWORDS,"Send a notification"},
    {"prompt",                 (PyCFunction)ClientIF_Prompt,    METH_VARARGS | METH_KEYWORDS,"Send a prompt"},
    {"clearSharedData",        (PyCFunction)ClientIF_ClearSharedData,    METH_VARARGS | METH_KEYWORDS, "Remove shared variables"},
    {"setSharedData",          (PyCFunction)ClientIF_SetSharedData,      METH_VARARGS | METH_KEYWORDS, "Set shared variable values"},
    {"getSharedData",          (PyCFunction)ClientIF_GetSharedData,      METH_VARARGS | METH_KEYWORDS, "Get shared variable values"},
    {"getSharedDataKeys",      (PyCFunction)ClientIF_GetSharedDataKeys,  METH_VARARGS | METH_KEYWORDS, "Get shared variable names"},
    {"getSharedDataScopes",    (PyCFunction)ClientIF_GetSharedDataScopes,  METH_VARARGS | METH_KEYWORDS, "Get shared variable scopes"},
    {"addSharedDataScope",     (PyCFunction)ClientIF_AddSharedDataScope,  METH_VARARGS | METH_KEYWORDS, "Add shared variable scope"},
    {"removeSharedDataScope",  (PyCFunction)ClientIF_RemoveSharedDataScope,  METH_VARARGS | METH_KEYWORDS, "Remove shared variable scope"},
    {"removeSharedDataScopes", (PyCFunction)ClientIF_RemoveSharedDataScopes,  METH_VARARGS | METH_KEYWORDS, "Remove ALL shared variable scopes"},
    {NULL, NULL, 0, NULL} /* Sentinel */
};

//============================================================================
// Python representation of the SPELL executor object type
//============================================================================
static PyTypeObject ClientIF_Type =
{
    PyObject_HEAD_INIT(NULL)
    0,                                 //ob_size
    "executor.ClientIF",               //tp_name
    sizeof(PyClientIFObject),          //tp_basicsize
    0,                                 //tp_itemsize
    (destructor)ClientIF_Dealloc,      //tp_dealloc
    0,                                 //tp_print
    0,                                 //tp_getattr
    0,                                 //tp_setattr
    0,                                 //tp_compare
    0,                                 //tp_repr
    0,                                 //tp_as_number
    0,                                 //tp_as_sequence
    0,                                 //tp_as_mapping
    0,                                 //tp_hash
    0,                                 //tp_call
    0,                                 //tp_str
    0,                                 //tp_getattro
    0,                                 //tp_setattro
    0,                                 //tp_as_buffer
    Py_TPFLAGS_DEFAULT | Py_TPFLAGS_BASETYPE, //tp_flags
    "client interface",                // tp_doc
    0,                                 // tp_traverse
    0,                                 // tp_clear
    0,                                 // tp_richcompare
    0,                                 // tp_weaklistoffset
    0,                                 // tp_iter
    0,                                 // tp_iternext
    ClientIF_Methods,                  // tp_methods
    ClientIF_Members,                  // tp_members
    0,                                 // tp_getset
    0,                                 // tp_base
    0,                                 // tp_dict
    0,                                 // tp_descr_get
    0,                                 // tp_descr_set
    0,                                 // tp_dictoffset
    (initproc)ClientIF_Init,           // tp_init
    0,                                 // tp_alloc
    ClientIF_New,                      // tp_new
};


//============================================================================
// FUNCTION        : ClientIF_Init
//============================================================================
static int ClientIF_Init( PyClientIFObject* self, PyObject* args, PyObject* kwds )
{
    return 0;
}

//============================================================================
// FUNCTION        : ClientIF_Dealloc
//============================================================================
static void ClientIF_Dealloc( PyClientIFObject* self )
{
    self->ob_type->tp_free( (PyObject*) self );
}

//============================================================================
// FUNCTION        : ClientIF_New
//============================================================================
static PyObject* ClientIF_New( PyTypeObject* type, PyObject* args, PyObject* kwds )
{
    //NOTE:  Py_INCREF is already called!
    PyClientIFObject* self;
    self = (PyClientIFObject*) type->tp_alloc(type,0);
    return (PyObject*)self;
}

//============================================================================
// FUNCTION        : ClientIF_Install
//============================================================================
void ClientIF_Install()
{
    DEBUG("Installing CIF object")

    if (PyType_Ready(&ClientIF_Type) < 0 )
    {
        THROW_EXCEPTION("Cannot install CIF", "Unable to register object type", SPELL_ERROR_PYTHON_API);
    }

    PyObject* cif = ClientIF_New( &ClientIF_Type, NULL, NULL );
    Py_INCREF(cif);

    SPELLregistry::instance().set( cif, "CIF" );
}

//============================================================================
// FUNCTION        : ClientIF_SetVerbosity
//============================================================================
static PyObject* ClientIF_SetVerbosity( PyObject* self, PyObject* args )
{
	SPELLpyArgs pargs(args);
    int verbosity = 0;

    if (pargs.size()>0)
    {
    	PyObject* pyVerbosity = pargs[0];
    	if (PyLong_Check(pyVerbosity))
    	{
    		verbosity = PyLong_AsLong(pyVerbosity);
    	}
    	else if (PyInt_Check(pyVerbosity))
    	{
    		verbosity = PyInt_AsLong(pyVerbosity);
    	}
    	else if(pyVerbosity == Py_None)
    	{
    		// Nothing to do
    	}
    	else
    	{
            THROW_SYNTAX_EXCEPTION("Syntax error on CIF::setVerbosity()", "Malformed argument: " + PYREPR(args));
    	}
    }
    SPELLexecutor::instance().getCIF().setVerbosity(verbosity);
    Py_RETURN_NONE;
}

//============================================================================
// FUNCTION        : ClientIF_ResetVerbosity
//============================================================================
static PyObject* ClientIF_ResetVerbosity( PyObject* self, PyObject* args )
{
    SPELLexecutor::instance().getCIF().resetVerbosity();
    Py_RETURN_NONE;
}

//============================================================================
// FUNCTION        : ClientIF_Write
//============================================================================
static PyObject* ClientIF_Write( PyObject* self, PyObject* args )
{
    DEBUG("[CIF PY] Write args: " + PYREPR(args))

    SPELLpyArgs arguments(args);
    if ((arguments.size()<1)||(arguments.size()>2))
    {
        THROW_SYNTAX_EXCEPTION("CIF::write() failed", "Malformed arguments");
    }

    std::string message = PYSSTR(arguments[0]);
    PyObject* configObj = arguments[1];

    SPELLpyArgs argumentsC(args,configObj);

    int severity = argumentsC.getModifier_Severity();
    int scope = argumentsC.getModifier_Scope();

    switch(severity)
    {
    case LanguageConstants::INFORMATION:
        SPELLexecutor::instance().getCIF().write( message, scope );
        break;
    case LanguageConstants::WARNING:
        SPELLexecutor::instance().getCIF().warning( message, scope );
        break;
    case LanguageConstants::ERROR:
        SPELLexecutor::instance().getCIF().error( message, scope );
        break;
    default:
        THROW_SYNTAX_EXCEPTION("Syntax error on CIF::write()", "Unknown severity: " + severity);
    }
    DEBUG("[CIF PY] Write finished")
    Py_RETURN_NONE;
}

//============================================================================
// FUNCTION        : ClientIF_Notify
//============================================================================
static PyObject* ClientIF_Notify( PyObject* self, PyObject* args, PyObject* kwds )
{
    ItemNotification notification;

    /** \todo use pyargs */

    DEBUG("[CIF PY] Notify args : " + PYREPR(args))
    DEBUG("[CIF PY] Notify kargs: " + PYREPR(kwds))

    PyObject* o_type = PyTuple_GetItem( args, 0 );
    assert( o_type != NULL );
    if (PYSTR(o_type) == DATA_NOTIF_TYPE_VAL)
    {
        notification.type = NOTIFY_VALUE;
    }
    else if (PYSTR(o_type) == DATA_NOTIF_TYPE_ITEM)
    {
        notification.type = NOTIFY_ITEM;
    }
    else if (PYSTR(o_type) == DATA_NOTIF_TYPE_EXEC)
    {
        notification.type = NOTIFY_EXECUTION;
    }
    else if (PYSTR(o_type) == DATA_NOTIF_TYPE_VERIF)
    {
        notification.type = NOTIFY_VERIFICATION;
    }
    else if (PYSTR(o_type) == DATA_NOTIF_TYPE_TIME)
    {
        notification.type = NOTIFY_TIME;
    }
    else if (PYSTR(o_type) == DATA_NOTIF_TYPE_SYS)
    {
        notification.type = NOTIFY_SYSTEM;
    }

    PyObject* o_name = PyTuple_GetItem( args, 1 );
    assert( o_name != NULL );
    notification.name = PYSTR(o_name);

    PyObject* o_value = PyTuple_GetItem( args, 2 );
    assert( o_value != NULL );
    notification.value = PYSSTR(o_value);

    PyObject* o_status = PyTuple_GetItem( args, 3 );
    assert( o_status != NULL );
    notification.status = PYSTR(o_status);

    notification.comment = "";
    if (PyTuple_Size(args)>4)
    {
        PyObject* o_comment = PyTuple_GetItem( args, 4 );
        assert( o_comment != NULL );
        notification.comment = PYSTR(o_comment);
    }

    /** \todo Review times which shall be different for different items */
    notification.time = "";
    if (PyTuple_Size(args)>5)
    {
        PyObject* o_time = PyTuple_GetItem( args, 5 );
        assert( o_time != NULL );
        notification.time= PYSTR(o_time);
    }

    notification.stack = ""; //This is processed by childen CIFs if applicable

    try
    {
    	SPELLexecutor::instance().getCIF().notify( notification );
    }
    catch(SPELLcoreException& ex)
    {
    	LOG_ERROR("Unable to send notification: " + ex.what());
    	LOG_ERROR("Will abort execution!");
    	SPELLexecutor::instance().abort("Unable to send notification to client", true);
    }

    Py_RETURN_NONE;
}

//============================================================================
// FUNCTION        : ClientIF_Prompt
//============================================================================
static PyObject* ClientIF_Prompt( PyObject* self, PyObject* args, PyObject* kwds )
{
    DEBUG("[CIF PY] Prompt args : " + PYREPR(args))
    DEBUG("[CIF PY] Prompt kargs: " + PYREPR(kwds))

    if (PyTuple_Size(args)==0)
    {
    	THROW_SYNTAX_EXCEPTION("Cannot issue prompt", "No arguments given");
    }

    SPELLpyArgs argumentsA(args);
    if (argumentsA.size()<2)
    {
    	THROW_SYNTAX_EXCEPTION("Prompt failed", "Malformed arguments");
    }

    PyObject* messageObj = argumentsA[0];
    PyObject* configObj = argumentsA[2];

    SPELLpyArgs argumentsC(args,configObj);

    SPELLpromptDefinition def;

    // The prompt message
    def.message = PYSTR(messageObj);
    // The prompt type
    def.typecode = LanguageConstants::PROMPT_OK_CANCEL;
    if (argumentsC.hasModifier(LanguageModifiers::Type))
    {
    	def.typecode = argumentsC.getModifier_Type();
    }
    if (argumentsC.hasModifier(LanguageModifiers::Timeout))
    {
    	def.timeout = argumentsC.getModifier_Timeout();
    }
    // The prompt scope
	def.scope = argumentsC.getModifier_Scope();
	// The prompt options and expected list
    SPELLcifHelper::generatePromptOptions(args, def);

    std::string result = "";
    try
    {
    	result = SPELLexecutor::instance().getCIF().prompt( def );
    }
    catch(SPELLcoreException& ex)
    {
    	LOG_ERROR("Unable to receive prompt answer: " + ex.what());
    	LOG_ERROR("Will abort execution!");
    	SPELLexecutor::instance().abort("Unable to receive prompt answer", true);
    	Py_RETURN_NONE;
    }

    return SPELLcifHelper::getPythonResult( result, def );
}

//============================================================================
// FUNCTION        : ClientIF_SetSharedData
//============================================================================
static PyObject* ClientIF_SetSharedData( PyObject* self, PyObject* args, PyObject* kwds )
{
    DEBUG("[CIF PY] SetSharedData args : " + PYREPR(args))
    DEBUG("[CIF PY] SetSharedData kargs: " + PYREPR(kwds))

    if (PyTuple_Size(args)==0)
    {
    	THROW_SYNTAX_EXCEPTION("Cannot set shared data", "No arguments given");
    }

    SPELLpyArgs argumentsA(args);
    if (argumentsA.size()!=4)
    {
    	THROW_SYNTAX_EXCEPTION("Cannot set shared data", "Malformed arguments: expected a variable name, value, expected value and scope");
    }

    PyObject* nameObj = argumentsA[0];
    PyObject* valueObj = argumentsA[1];
    PyObject* expectedObj = argumentsA[2];
    PyObject* scopeObj = argumentsA[3];
    std::string result = "";
    int numItems = 0;
    try
    {
    	std::string name = "";
    	std::string value = "";
    	std::string expected = "";
    	if (PyList_Check(nameObj))
    	{
    		numItems = PyList_Size(nameObj);
    		for(int idx =0; idx<numItems; idx++)
    		{
    			std::string itemName = PYSTR(PyList_GetItem(nameObj,idx));
    			std::string itemValue = PYREPR(PyList_GetItem(valueObj,idx));
    			std::string itemExpected = "";
    			PyObject* itemExpectedObj = PyList_GetItem(expectedObj,idx);
    			if (PyString_Check(itemExpectedObj))
    			{
    				itemExpected = PYSTR(itemExpectedObj);
					if (itemExpected != "__NONE__")
					{
						itemExpected = PYREPR(itemExpectedObj);
					}
    			}
    			else
    			{
    				itemExpected = PYREPR(itemExpectedObj);
    			}

    			if (name != "") name += LIST_SEPARATOR;
    			if (value != "") value += LIST_SEPARATOR;
    			if (expected != "") expected += LIST_SEPARATOR;
    			name += itemName;
    			value += itemValue;
    			expected += itemExpected;
    		}
    	}
    	else
    	{
        	name = PYSTR(nameObj);
        	value = PYREPR(valueObj);
			if (PyString_Check(expectedObj))
			{
				expected = PYSTR(expectedObj);
				if (expected != "__NONE__")
				{
					expected = PYREPR(expectedObj);
				}
			}
			else
			{
				expected = PYREPR(expectedObj);
			}
    	}
    	std::string scope = PYSTR(scopeObj);

    	result = SPELLexecutor::instance().getCIF().setSharedData(name,value,expected,scope);
    }
    catch(SPELLcoreException& ex)
    {
    	LOG_ERROR("Failed to set shared data: " + ex.what());
    	THROW_DRIVER_EXCEPTION(ex.getError(),ex.getReason());
    }

    if (numItems>0)
    {
    	PyObject* list = PyList_New(numItems);
    	std::vector<std::string> items = SPELLutils::tokenize(result,LIST_SEPARATOR_STR);
    	std::vector<std::string>::iterator it;
    	int idx = 0;
    	for( it = items.begin(); it != items.end(); it++  )
    	{
    		if (*it == "True")
    		{
    			PyList_SetItem(list,idx,Py_True);
    		}
    		else
    		{
    			PyList_SetItem(list,idx,Py_False);
    		}
    		idx++;
    	}
    	Py_INCREF(list);
    	return list;
    }
    else
    {
    	if (result == "True")
    	{
        	Py_RETURN_TRUE;
    	}
    	else
    	{
        	Py_RETURN_FALSE;
    	}
    }
}

//============================================================================
// FUNCTION        : ClientIF_ClearSharedData
//============================================================================
static PyObject* ClientIF_ClearSharedData( PyObject* self, PyObject* args, PyObject* kwds )
{
    DEBUG("[CIF PY] ClearSharedData args : " + PYREPR(args))
    DEBUG("[CIF PY] ClearSharedData kargs: " + PYREPR(kwds))

    if (PyTuple_Size(args)==0)
    {
    	THROW_SYNTAX_EXCEPTION("Cannot clear shared data", "No arguments given");
    }

    SPELLpyArgs argumentsA(args);
    if (argumentsA.size()>2)
    {
    	THROW_SYNTAX_EXCEPTION("Cannot clear shared data", "Malformed arguments");
    }

	PyObject* nameObj = NULL;
	PyObject* scopeObj = NULL;
	int numItems = 0;
	std::string result = "";

    if (argumentsA.size()==2)
    {
    	nameObj = argumentsA[0];
    	scopeObj = argumentsA[1];
    }
    else
    {
    	scopeObj = argumentsA[0];
    }

    try
    {
    	std::string scope = PYSTR(scopeObj);

    	if (nameObj != NULL)
    	{
			std::string name = "";
			if (PyList_Check(nameObj))
			{
				numItems = PyList_Size(nameObj);
				for(int idx =0; idx<numItems; idx++)
				{
					std::string itemName = PYSTR(PyList_GetItem(nameObj,idx));
					if (name != "") name += LIST_SEPARATOR;
					name += itemName;
				}
			}
			else
			{
				name = PYSTR(nameObj);
			}
	    	result = SPELLexecutor::instance().getCIF().clearSharedData(name,scope);
    	}
    	else
    	{
	    	result = SPELLexecutor::instance().getCIF().clearSharedData("",scope);
    	}
    }
    catch(SPELLcoreException& ex)
    {
    	LOG_ERROR("Failed to clear shared data: " + ex.what());
    	THROW_DRIVER_EXCEPTION(ex.getError(),ex.getReason());
    }

    if (numItems>0)
    {
    	PyObject* list = PyList_New(numItems);
    	std::vector<std::string> items = SPELLutils::tokenize(result,LIST_SEPARATOR_STR);
    	std::vector<std::string>::iterator it;
    	int idx = 0;
    	for( it = items.begin(); it != items.end(); it++  )
    	{
    		if (*it == "True")
    		{
    			PyList_SetItem(list,idx,Py_True);
    		}
    		else
    		{
    			PyList_SetItem(list,idx,Py_False);
    		}
    		idx++;
    	}
    	Py_INCREF(list);
    	return list;
    }
    else
    {
    	if (result == "True")
    	{
        	Py_RETURN_TRUE;
    	}
    	else
    	{
        	Py_RETURN_FALSE;
    	}
    }
}

//============================================================================
// FUNCTION        : ClientIF_GetSharedData
//============================================================================
static PyObject* ClientIF_GetSharedData( PyObject* self, PyObject* args, PyObject* kwds )
{
    DEBUG("[CIF PY] GetSharedData args : " + PYREPR(args))
    DEBUG("[CIF PY] GetSharedData kargs: " + PYREPR(kwds))

    if (PyTuple_Size(args)==0)
    {
    	THROW_SYNTAX_EXCEPTION("Cannot get shared data", "No arguments given");
    }

    SPELLpyArgs argumentsA(args);
    if (argumentsA.size()!=2)
    {
    	THROW_SYNTAX_EXCEPTION("Cannot get shared data", "Malformed arguments: expected a variable name and scope");
    }

    PyObject* nameObj = argumentsA[0];
    PyObject* scopeObj = argumentsA[1];

    PyObject* result = NULL;

    try
    {
    	std::string name = "";
    	bool usingList = false;
    	if (PyList_Check(nameObj))
    	{
    		usingList = true;
    		int num = PyList_Size(nameObj);
    		for(int idx =0; idx<num; idx++)
    		{
    			std::string itemName = PYSTR(PyList_GetItem(nameObj,idx));
    			if (name != "") name += LIST_SEPARATOR;
    			name += itemName;
    		}
    	}
    	else
    	{
        	name = PYSTR(nameObj);
    	}
    	std::string scope = PYSTR(scopeObj);

    	std::string value = SPELLexecutor::instance().getCIF().getSharedData(name,scope);

    	if (usingList)
    	{
			std::vector<std::string> values = SPELLutils::tokenize(value, LIST_SEPARATOR_STR);
			std::vector<std::string>::iterator it;
			result = PyList_New(values.size());
			int idx = 0;
			for(it = values.begin(); it != values.end(); it++)
			{
				PyObject* item = SPELLpythonHelper::instance().eval( *it, false );
				PyList_SetItem(result, idx, item);
				idx++;
			}
    	}
    	else
    	{
    		result = SPELLpythonHelper::instance().eval( value, false );
    	}
		Py_INCREF(result);
    }
    catch(SPELLcoreException& ex)
    {
    	LOG_ERROR("Failed to get shared data: " + ex.what());
    	THROW_DRIVER_EXCEPTION(ex.getError(),ex.getReason());
    }

    return result;
}

//============================================================================
// FUNCTION        : ClientIF_GetSharedDataKeys
//============================================================================
static PyObject* ClientIF_GetSharedDataKeys( PyObject* self, PyObject* args, PyObject* kwds )
{
    DEBUG("[CIF PY] GetSharedData args : " + PYREPR(args))
    DEBUG("[CIF PY] GetSharedData kargs: " + PYREPR(kwds))

    SPELLpyArgs argumentsA(args);
    if (argumentsA.size()!=1)
    {
    	THROW_SYNTAX_EXCEPTION("Cannot get shared data", "Malformed arguments: expected scope");
    }

    PyObject* scopeObj = argumentsA[0];

    PyObject* result = NULL;

    try
    {
    	std::string scope = PYSTR(scopeObj);
    	std::string keys = SPELLexecutor::instance().getCIF().getSharedDataKeys(scope);

    	if ( keys.find(LIST_SEPARATOR) != std::string::npos )
    	{
        	std::vector<std::string> items = SPELLutils::tokenize(keys,LIST_SEPARATOR_STR);
        	std::vector<std::string>::iterator it;
        	result = PyList_New(items.size());
        	int index = 0;
        	for( it = items.begin(); it != items.end(); it++ )
        	{
        		std::string key = *it;
        		PyObject* item = SSTRPY(key);
        		Py_INCREF(item);
        		PyList_SetItem(result, index, item);
        		index++;
        	}
    	}
    	else if (keys == "")
    	{
    		result = PyList_New(0);
    	}
    	else
    	{
        	result = PyList_New(1);
    		PyList_SetItem(result, 0, SSTRPY( keys ));
    	}
		Py_INCREF(result);
    }
    catch(SPELLcoreException& ex)
    {
    	LOG_ERROR("Failed to get shared data: " + ex.what());
    	THROW_DRIVER_EXCEPTION(ex.getError(),ex.getReason());
    }

    return result;
}

//============================================================================
// FUNCTION        : ClientIF_GetSharedDataScopes
//============================================================================
static PyObject* ClientIF_GetSharedDataScopes( PyObject* self, PyObject* args, PyObject* kwds )
{
    DEBUG("[CIF PY] GetSharedDataScopes args : " + PYREPR(args))
    DEBUG("[CIF PY] GetSharedDataScopes kargs: " + PYREPR(kwds))

    PyObject* result = NULL;

    try
    {
    	std::string keys = SPELLexecutor::instance().getCIF().getSharedDataScopes();

    	if ( keys.find(LIST_SEPARATOR) != std::string::npos )
    	{
        	std::vector<std::string> items = SPELLutils::tokenize(keys,LIST_SEPARATOR_STR);
        	std::vector<std::string>::iterator it;
        	result = PyList_New(items.size());
        	int index = 0;
        	for( it = items.begin(); it != items.end(); it++ )
        	{
        		std::string key = *it;
        		PyObject* item = SSTRPY(key);
        		Py_INCREF(item);
        		PyList_SetItem(result, index, item);
        		index++;
        	}
    	}
    	else if (keys == "")
    	{
    		result = PyList_New(0);
    	}
    	else
    	{
        	result = PyList_New(1);
    		PyList_SetItem(result, 0, SSTRPY( keys ));
    	}
		Py_INCREF(result);
    }
    catch(SPELLcoreException& ex)
    {
    	LOG_ERROR("Failed to get shared data scopes: " + ex.what());
    	THROW_DRIVER_EXCEPTION(ex.getError(),ex.getReason());
    }

    return result;
}

//============================================================================
// FUNCTION        : ClientIF_AddSharedDataScope
//============================================================================
static PyObject* ClientIF_AddSharedDataScope( PyObject* self, PyObject* args, PyObject* kwds )
{
    DEBUG("[CIF PY] AddSharedDataScope args : " + PYREPR(args))
    DEBUG("[CIF PY] AddSharedDataScope kargs: " + PYREPR(kwds))

    SPELLpyArgs argumentsA(args);
    if (argumentsA.size()!=1)
    {
    	THROW_SYNTAX_EXCEPTION("Cannot add shared data scope", "Malformed arguments: expected scope name");
    }

    PyObject* scopeObj = argumentsA[0];

    PyObject* result = NULL;

    try
    {
    	std::string scope = PYSTR(scopeObj);
    	SPELLexecutor::instance().getCIF().addSharedDataScope(scope);
    	Py_RETURN_TRUE;
    }
    catch(SPELLcoreException& ex)
    {
    	LOG_ERROR("Failed to add shared data scope: " + ex.what());
    	THROW_DRIVER_EXCEPTION(ex.getError(),ex.getReason());
    }
    return result;
}

//============================================================================
// FUNCTION        : ClientIF_RemoveSharedDataScope
//============================================================================
static PyObject* ClientIF_RemoveSharedDataScope( PyObject* self, PyObject* args, PyObject* kwds )
{
    DEBUG("[CIF PY] RemoveSharedDataScope args : " + PYREPR(args))
    DEBUG("[CIF PY] RemoveSharedDataScope kargs: " + PYREPR(kwds))

    SPELLpyArgs argumentsA(args);
    if (argumentsA.size()!=1)
    {
    	THROW_SYNTAX_EXCEPTION("Cannot remove shared data scope", "Malformed arguments: expected scope name");
    }

    PyObject* scopeObj = argumentsA[0];

    PyObject* result = NULL;

    try
    {
    	std::string scope = PYSTR(scopeObj);
    	SPELLexecutor::instance().getCIF().removeSharedDataScope(scope);
    	Py_RETURN_TRUE;
    }
    catch(SPELLcoreException& ex)
    {
    	LOG_ERROR("Failed to remove shared data scope: " + ex.what());
    	THROW_DRIVER_EXCEPTION(ex.getError(),ex.getReason());
    }
    return result;
}

//============================================================================
// FUNCTION        : ClientIF_RemoveSharedDataScopes
//============================================================================
static PyObject* ClientIF_RemoveSharedDataScopes( PyObject* self, PyObject* args, PyObject* kwds )
{
    DEBUG("[CIF PY] RemoveSharedDataScopes args : " + PYREPR(args))
    DEBUG("[CIF PY] RemoveSharedDataScopes kargs: " + PYREPR(kwds))

    SPELLpyArgs argumentsA(args);
    if (argumentsA.size()!=0)
    {
    	THROW_SYNTAX_EXCEPTION("Cannot remove ALL shared data scopes", "Expected no arguments");
    }

    PyObject* result = NULL;

    try
    {
    	SPELLexecutor::instance().getCIF().removeSharedDataScopes();
    	Py_RETURN_TRUE;
    }
    catch(SPELLcoreException& ex)
    {
    	LOG_ERROR("Failed to remove ALL shared data scopes: " + ex.what());
    	THROW_DRIVER_EXCEPTION(ex.getError(),ex.getReason());
    }
    return result;
}
