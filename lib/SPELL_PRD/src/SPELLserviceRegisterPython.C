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
#include "SPELL_PRD/SPELLservicePython.H"
#include "SPELL_UTIL/SPELLlog.H"
#include "SPELL_UTIL/SPELLpythonHelper.H"
#include "SPELL_PRD/SPELLprocedureManager.H"
// System includes ---------------------------------------------------------
#include "structmember.h"



//============================================================================
// FUNCTION        : SpellService_Init
// DESCRIPTION    : Initialized of the SpellService python object
//============================================================================
static int SpellService_Init( PySpellServiceObject* self, PyObject* args, PyObject* kwds );
//============================================================================
// FUNCTION        : SpellService_Dealloc
// DESCRIPTION    : Cleanup of the SpellService python object
//============================================================================
static void SpellService_Dealloc( PySpellServiceObject* self );
//============================================================================
// FUNCTION        : SpellService_New
// DESCRIPTION    : Constructor of the SpellService python object
//============================================================================
static PyObject* SpellService_New( PyTypeObject* type, PyObject* args, PyObject* kwds );
//============================================================================
// FUNCTION        : SpellService_Call
// DESCRIPTION    : Call a SpellService statement
//============================================================================
static PyObject* SpellService_Call( PyObject* self, PyObject* args, PyObject* kargs );



//============================================================================
// SpellService object member specification
//============================================================================
static PyMemberDef SpellService_Members[] =
{
    {NULL} /* Sentinel */
};

//============================================================================
// SpellService object method specification
//============================================================================
static PyMethodDef SpellService_Methods[] =
{
    {NULL} /* Sentinel */
};

//============================================================================
// Python representation of the SPELL SpellService object type
//============================================================================
static PyTypeObject SpellService_Type =
{
    PyVarObject_HEAD_INIT(NULL,0)
    "context.RegisterSpellService",                   //tp_name
    sizeof(PySpellServiceObject),              //tp_basicsize
    0,                                 //tp_itemsize
    (destructor)SpellService_Dealloc,          //tp_dealloc
    0,                                 //tp_print
    0,                                 //tp_getattr
    0,                                 //tp_setattr
    0,                                 //tp_compare
    0,                                 //tp_repr
    0,                                 //tp_as_number
    0,                                 //tp_as_sequence
    0,                                 //tp_as_mapping
    0,                                 //tp_hash
    SpellService_Call,                         //tp_call
    0,                                 //tp_str
    0,                                 //tp_getattro
    0,                                 //tp_setattro
    0,                                 //tp_as_buffer
    Py_TPFLAGS_DEFAULT | Py_TPFLAGS_BASETYPE, //tp_flags
    "RegisterSpellService mechanism",                  // tp_doc
    0,                                 // tp_traverse
    0,                                 // tp_clear
    0,                                 // tp_richcompare
    0,                                 // tp_weaklistoffset
    0,                                 // tp_iter
    0,                                 // tp_iternext
    SpellService_Methods,                      // tp_methods
    SpellService_Members,                      // tp_members
    0,                                 // tp_getset
    0,                                 // tp_base
    0,                                 // tp_dict
    0,                                 // tp_descr_get
    0,                                 // tp_descr_set
    0,                                 // tp_dictoffset
    (initproc)SpellService_Init,               // tp_init
    0,                                 // tp_alloc
    SpellService_New,                          // tp_new
};


//============================================================================
// FUNCTION        : SpellService_Init
//============================================================================
static int SpellService_Init( PySpellServiceObject* self, PyObject* args, PyObject* kwds )
{
    return 0;
}

//============================================================================
// FUNCTION        : SpellService_Dealloc
//============================================================================
static void SpellService_Dealloc( PySpellServiceObject* self )
{
    Py_TYPE(self)->tp_free( (PyObject*) self );
}

//============================================================================
// FUNCTION        : SpellService_New
//============================================================================
static PyObject* SpellService_New( PyTypeObject* type, PyObject* args, PyObject* kwds )
{
    //NOTE:  Py_INCREF is already called!
    PySpellServiceObject* self;
    self = (PySpellServiceObject*) type->tp_alloc(type,0);
    return (PyObject*)self;
}

//============================================================================
// FUNCTION        : SpellService_Call
//============================================================================
static PyObject* SpellService_Call( PyObject* self, PyObject* args, PyObject* kargs )
{
	PyObject* target = PyTuple_GetItem(args,0);
	Py_IncRef(target);

	SPELLprocedureManager::instance().registerServiceHandler(target);

    return target;
}

//============================================================================
// FUNCTION        : SpellService_Install
//============================================================================
void SpellService_Install()
{
    LOG_INFO("Installing SpellService object");

    if (PyType_Ready(&SpellService_Type) < 0 )
    {
        THROW_EXCEPTION("Cannot install SpellService", "Unable to register object type", SPELL_ERROR_PYTHON_API);
    }

    PyObject* SpellService = SpellService_New( &SpellService_Type, NULL, NULL );
    Py_INCREF(SpellService);

    SPELLpythonHelper::instance().install(SpellService,"RegisterSpellService");
}

