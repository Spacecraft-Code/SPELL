// ################################################################################
// FILE       : SPELLSpellServicePython.C
// DATE       : Mar 17, 2011
// PROJECT    : SPELL
// DESCRIPTION: Implementation of the goto Python bindings
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
#include "SPELL_UTIL/SPELLbase.H"
#include "SPELL_PRD/SPELLserviceWatchdogPython.H"
#include "SPELL_UTIL/SPELLlog.H"
#include "SPELL_UTIL/SPELLpythonHelper.H"
#include "SPELL_PRD/SPELLprocedureManager.H"
// System includes ---------------------------------------------------------
#include "structmember.h"



//============================================================================
// FUNCTION        : SpellService_Init
// DESCRIPTION    : Initialized of the SpellService python object
//============================================================================
static int SpellServiceWatchdog_Init( PySpellServiceWatchdogObject* self, PyObject* args, PyObject* kwds );
//============================================================================
// FUNCTION        : SpellService_Dealloc
// DESCRIPTION    : Cleanup of the SpellService python object
//============================================================================
static void SpellServiceWatchdog_Dealloc( PySpellServiceWatchdogObject* self );
//============================================================================
// FUNCTION        : SpellService_New
// DESCRIPTION    : Constructor of the SpellService python object
//============================================================================
static PyObject* SpellServiceWatchdog_New( PyTypeObject* type, PyObject* args, PyObject* kwds );
//============================================================================
// FUNCTION        : SpellService_Call
// DESCRIPTION    : Call a SpellService statement
//============================================================================
static PyObject* SpellServiceWatchdog_Call( PyObject* self, PyObject* args, PyObject* kargs );



//============================================================================
// SpellService object member specification
//============================================================================
static PyMemberDef SpellServiceWatchdog_Members[] =
{
    {"callback",T_OBJECT_EX,offsetof(PySpellServiceWatchdogObject,callback),0,"callback to C code"},
    {NULL} /* Sentinel */
};

//============================================================================
// SpellService object method specification
//============================================================================
static PyMethodDef SpellServiceWatchdog_Methods[] =
{
    {NULL} /* Sentinel */
};

//============================================================================
// Python representation of the SPELL SpellService object type
//============================================================================
static PyTypeObject SpellServiceWatchdog_Type =
{
    PyVarObject_HEAD_INIT(NULL,0)
    "context.WatchdogCallback",                   //tp_name
    sizeof(PySpellServiceWatchdogObject),              //tp_basicsize
    0,                                 //tp_itemsize
    (destructor)SpellServiceWatchdog_Dealloc,          //tp_dealloc
    0,                                 //tp_print
    0,                                 //tp_getattr
    0,                                 //tp_setattr
    0,                                 //tp_compare
    0,                                 //tp_repr
    0,                                 //tp_as_number
    0,                                 //tp_as_sequence
    0,                                 //tp_as_mapping
    0,                                 //tp_hash
    SpellServiceWatchdog_Call,                         //tp_call
    0,                                 //tp_str
    0,                                 //tp_getattro
    0,                                 //tp_setattro
    0,                                 //tp_as_buffer
    Py_TPFLAGS_DEFAULT | Py_TPFLAGS_BASETYPE, //tp_flags
    "WatchdogCallback mechanism",                  // tp_doc
    0,                                 // tp_traverse
    0,                                 // tp_clear
    0,                                 // tp_richcompare
    0,                                 // tp_weaklistoffset
    0,                                 // tp_iter
    0,                                 // tp_iternext
    SpellServiceWatchdog_Methods,                      // tp_methods
    SpellServiceWatchdog_Members,                      // tp_members
    0,                                 // tp_getset
    0,                                 // tp_base
    0,                                 // tp_dict
    0,                                 // tp_descr_get
    0,                                 // tp_descr_set
    0,                                 // tp_dictoffset
    (initproc)SpellServiceWatchdog_Init,               // tp_init
    0,                                 // tp_alloc
    SpellServiceWatchdog_New,                          // tp_new
};


//============================================================================
// FUNCTION        : SpellService_Init
//============================================================================
static int SpellServiceWatchdog_Init( PySpellServiceWatchdogObject* self, PyObject* args, PyObject* kwds )
{
    return 0;
}

//============================================================================
// FUNCTION        : SpellService_Dealloc
//============================================================================
static void SpellServiceWatchdog_Dealloc( PySpellServiceWatchdogObject* self )
{
    Py_TYPE(self)->tp_free( (PyObject*) self );
}

//============================================================================
// FUNCTION        : SpellService_New
//============================================================================
static PyObject* SpellServiceWatchdog_New( PyTypeObject* type, PyObject* args, PyObject* kwds )
{
    //NOTE:  Py_INCREF is already called!
    PySpellServiceWatchdogObject* self;
    self = (PySpellServiceWatchdogObject*) type->tp_alloc(type,0);
    PyObject* callback = PyTuple_GetItem(args,0);
    Py_INCREF(callback);
    PyObject_SetAttrString((PyObject*)self, "callback",callback);

    return (PyObject*)self;
}

//============================================================================
// FUNCTION        : SpellService_Call
//============================================================================
static PyObject* SpellServiceWatchdog_Call( PyObject* self, PyObject* args, PyObject* kargs )
{
	PyObject* target = PyTuple_GetItem(args,0);
	Py_IncRef(target);

	void (*callback)(std::string&);
	PyObject* obj = PyObject_GetAttrString(self,"callback");
	callback = (void (*)(std::string&))PyCapsule_GetPointer(obj, NULL);

	std::string targetStr= PyString_AsString(target);
	callback(targetStr);
	Py_INCREF(Py_None);
    return Py_None;
}

//============================================================================
// FUNCTION        : SpellService_Install
//============================================================================
PyObject* SpellServiceWatchdog_Create(void (*callback)(std::string&) )
{
    LOG_INFO("Installing SpellService object");

    if (PyType_Ready(&SpellServiceWatchdog_Type) < 0 )
    {
        THROW_EXCEPTION("Cannot install SpellService", "Unable to register object type", SPELL_ERROR_PYTHON_API);
    }
    PyObject* callbackObj = PyCapsule_New((void*) callback,NULL,NULL);
    PyObject* args = PyTuple_Pack(1,callbackObj,NULL);
    PyObject* watchdog = SpellServiceWatchdog_New( &SpellServiceWatchdog_Type, args, NULL );
    Py_INCREF(watchdog);

    return watchdog;
}

