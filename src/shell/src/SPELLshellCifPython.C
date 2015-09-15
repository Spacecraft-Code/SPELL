// ################################################################################
// FILE       : SPELLshellCifPython.C
// DATE       : Mar 18, 2011
// PROJECT    : SPELL
// DESCRIPTION: Python bindings for CIF
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
#include "SPELL_CIF/SPELLcifHelper.H"
// Project includes --------------------------------------------------------
#include "SPELL_IPC/SPELLipc.H"
#include "SPELL_IPC/SPELLipc_Executor.H"
#include "SPELL_EXC/SPELLexecutor.H"
#include "SPELL_UTIL/SPELLlog.H"
#include "SPELL_UTIL/SPELLerror.H"
#include "SPELL_WRP/SPELLregistry.H"
#include "SPELL_UTIL/SPELLutils.H"
#include "SPELL_UTIL/SPELLpyArgs.H"
#include "SPELL_UTIL/SPELLpythonHelper.H"
#include "SPELL_WRP/SPELLconstants.H"
using namespace LanguageConstants;
using namespace LanguageModifiers;
// System includes ---------------------------------------------------------
#include "structmember.h"

static SPELLcif* cifObject = NULL;

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
    {NULL, NULL, 0, NULL} /* Sentinel */
};

//============================================================================
// Python representation of the SPELL executor object type
//============================================================================
static PyTypeObject ClientIF_Type =
{
    PyObject_HEAD_INIT(NULL)
    0,                                 //ob_size
    "shell.ClientIF",                  //tp_name
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
void ShellClientIF_Install( SPELLcif* cif )
{
    cifObject = cif;
    if (PyType_Ready(&ClientIF_Type) < 0 )
    {
        THROW_EXCEPTION("Cannot install CIF", "Unable to register object type", SPELL_ERROR_PYTHON_API);
    }
    PyObject* pcif = ClientIF_New( &ClientIF_Type, NULL, NULL );
    Py_INCREF(pcif);
    SPELLregistry::instance().set( pcif, "CIF" );
}

//============================================================================
// FUNCTION        : ClientIF_SetVerbosity
//============================================================================
static PyObject* ClientIF_SetVerbosity( PyObject* self, PyObject* args )
{
    int verbosity = 0;
    if (!PyArg_ParseTuple( args, "i", &verbosity))
    {
        if (PyTuple_GetItem(args, 0) == Py_None)
        {
            PyErr_Clear();
            cifObject->setMaxVerbosity();
        }
        else
        {
            PyErr_Print();
            THROW_SYNTAX_EXCEPTION("Syntax error on CIF::setVerbosity()", "Malformed argument: " + PYREPR(args));
        }
    }
    cifObject->setVerbosity(verbosity);
    Py_RETURN_NONE;
}

//============================================================================
// FUNCTION        : ClientIF_ResetVerbosity
//============================================================================
static PyObject* ClientIF_ResetVerbosity( PyObject* self, PyObject* args )
{
    cifObject->resetVerbosity();
    Py_RETURN_NONE;
}

//============================================================================
// FUNCTION        : ClientIF_Write
//============================================================================
static PyObject* ClientIF_Write( PyObject* self, PyObject* args )
{
    DEBUG("[CIF PY] Write args: " + PYREPR(args))
    char* message;
    PyObject* config = NULL;
    if (!PyArg_ParseTuple( args, "sO", &message, &config ))
    {
        if (!PyArg_ParseTuple( args, "s", &message ))
        {
            PyErr_Print();
            THROW_SYNTAX_EXCEPTION("Syntax error on CIF::setVerbosity()", "Malformed argument: " + PYREPR(args));
        }
        else
        {
            PyErr_Clear();
        }
    }
    DEBUG("[CIF PY] Write args parsed")

    int severity = INFORMATION;
    if ( config != NULL && PyDict_Contains(config, SSTRPY(Severity)))
    {
        PyObject* value = PyDict_GetItemString( config, Severity.c_str() );
        severity = PyLong_AsLong(value);
    }

    switch(severity)
    {
    case INFORMATION:
        cifObject->write( message, 0 );
        break;
    case WARNING:
        cifObject->warning( message, 0 );
        break;
    case ERROR:
        cifObject->error( message, 0 );
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

    DEBUG("[CIF PY] Notify args : " + PYREPR(args))
    DEBUG("[CIF PY] Notify kargs: " + PYREPR(kwds))

    PyObject* o_type = PyTuple_GetItem( args, 0 );
    assert( o_type != NULL );
    if (PYSTR(o_type) == NotificationValue::DATA_NOTIF_TYPE_VAL)
    {
        notification.type = NOTIFY_VALUE;
    }
    else if (PYSTR(o_type) == NotificationValue::DATA_NOTIF_TYPE_ITEM)
    {
        notification.type = NOTIFY_ITEM;
    }
    else if (PYSTR(o_type) == NotificationValue::DATA_NOTIF_TYPE_EXEC)
    {
        notification.type = NOTIFY_EXECUTION;
    }
    else if (PYSTR(o_type) == NotificationValue::DATA_NOTIF_TYPE_VERIF)
    {
        notification.type = NOTIFY_VERIFICATION;
    }
    else if (PYSTR(o_type) == NotificationValue::DATA_NOTIF_TYPE_TIME)
    {
        notification.type = NOTIFY_TIME;
    }
    else if (PYSTR(o_type) == NotificationValue::DATA_NOTIF_TYPE_SYS)
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

    //TODO review times which shall be different for different items
    notification.time = "";
    if (PyTuple_Size(args)>5)
    {
        PyObject* o_time = PyTuple_GetItem( args, 4 );
        assert( o_time != NULL );
        notification.time= PYSTR(o_time);
    }

    notification.stack = ""; //This is processed by childen CIFs if applicable

    cifObject->notify( notification );

    Py_RETURN_NONE;
}

//============================================================================
// FUNCTION        : ClientIF_Prompt
//============================================================================
static PyObject* ClientIF_Prompt( PyObject* self, PyObject* args, PyObject* kwds )
{
    DEBUG("[CIF PY] Prompt args : " + PYREPR(args))
    DEBUG("[CIF PY] Prompt kargs: " + PYREPR(kwds))
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
    // The prompt scope
	def.scope = argumentsC.getModifier_Scope();
	// The prompt options and expected list
    SPELLcifHelper::generatePromptOptions(args, def);

    std::string result = cifObject->prompt( def );

    return SPELLcifHelper::getPythonResult( result, def );
}
