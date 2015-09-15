// ################################################################################
// FILE       : SPELLgotoPython.C
// DATE       : Mar 17, 2011
// PROJECT    : SPELL
// DESCRIPTION: Implementation of the goto Python bindings
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
#include "SPELL_EXC/SPELLexecutor.H"
#include "SPELL_EXC/SPELLgoto.H"
// Project includes --------------------------------------------------------
#include "SPELL_UTIL/SPELLlog.H"
#include "SPELL_UTIL/SPELLpythonHelper.H"
// System includes ---------------------------------------------------------
#include "structmember.h"



//============================================================================
// FUNCTION        : Goto_Init
// DESCRIPTION    : Initialized of the Goto python object
//============================================================================
static int Goto_Init( PyGotoObject* self, PyObject* args, PyObject* kwds );
//============================================================================
// FUNCTION        : Goto_Dealloc
// DESCRIPTION    : Cleanup of the Goto python object
//============================================================================
static void Goto_Dealloc( PyGotoObject* self );
//============================================================================
// FUNCTION        : Goto_New
// DESCRIPTION    : Constructor of the Goto python object
//============================================================================
static PyObject* Goto_New( PyTypeObject* type, PyObject* args, PyObject* kwds );
//============================================================================
// FUNCTION        : Goto_Call
// DESCRIPTION    : Call a goto statement
//============================================================================
static PyObject* Goto_Call( PyObject* self, PyObject* args, PyObject* kargs );



//============================================================================
// Goto object member specification
//============================================================================
static PyMemberDef Goto_Members[] =
{
    {NULL} /* Sentinel */
};

//============================================================================
// Goto object method specification
//============================================================================
static PyMethodDef Goto_Methods[] =
{
    {NULL} /* Sentinel */
};

//============================================================================
// Python representation of the SPELL Goto object type
//============================================================================
static PyTypeObject Goto_Type =
{
    PyObject_HEAD_INIT(NULL)
    0,                                 //ob_size
    "executor.Goto",                   //tp_name
    sizeof(PyGotoObject),              //tp_basicsize
    0,                                 //tp_itemsize
    (destructor)Goto_Dealloc,          //tp_dealloc
    0,                                 //tp_print
    0,                                 //tp_getattr
    0,                                 //tp_setattr
    0,                                 //tp_compare
    0,                                 //tp_repr
    0,                                 //tp_as_number
    0,                                 //tp_as_sequence
    0,                                 //tp_as_mapping
    0,                                 //tp_hash
    Goto_Call,                         //tp_call
    0,                                 //tp_str
    0,                                 //tp_getattro
    0,                                 //tp_setattro
    0,                                 //tp_as_buffer
    Py_TPFLAGS_DEFAULT | Py_TPFLAGS_BASETYPE, //tp_flags
    "Goto mechanism",                  // tp_doc
    0,                                 // tp_traverse
    0,                                 // tp_clear
    0,                                 // tp_richcompare
    0,                                 // tp_weaklistoffset
    0,                                 // tp_iter
    0,                                 // tp_iternext
    Goto_Methods,                      // tp_methods
    Goto_Members,                      // tp_members
    0,                                 // tp_getset
    0,                                 // tp_base
    0,                                 // tp_dict
    0,                                 // tp_descr_get
    0,                                 // tp_descr_set
    0,                                 // tp_dictoffset
    (initproc)Goto_Init,               // tp_init
    0,                                 // tp_alloc
    Goto_New,                          // tp_new
};


//============================================================================
// FUNCTION        : Goto_Init
//============================================================================
static int Goto_Init( PyGotoObject* self, PyObject* args, PyObject* kwds )
{
    return 0;
}

//============================================================================
// FUNCTION        : Goto_Dealloc
//============================================================================
static void Goto_Dealloc( PyGotoObject* self )
{
    self->ob_type->tp_free( (PyObject*) self );
}

//============================================================================
// FUNCTION        : Goto_New
//============================================================================
static PyObject* Goto_New( PyTypeObject* type, PyObject* args, PyObject* kwds )
{
    //NOTE:  Py_INCREF is already called!
    PyGotoObject* self;
    self = (PyGotoObject*) type->tp_alloc(type,0);
    return (PyObject*)self;
}

//============================================================================
// FUNCTION        : Goto_Call
//============================================================================
static PyObject* Goto_Call( PyObject* self, PyObject* args, PyObject* kargs )
{
	PyObject* target = PyTuple_GetItem(args,0);
	std::string label = PYSTR(target);
	SPELLexecutor::instance().goLabel(label,true);
    Py_RETURN_NONE;
}

//============================================================================
// FUNCTION        : Goto_Install
//============================================================================
void Goto_Install()
{
    DEBUG("Installing Goto object");

    if (PyType_Ready(&Goto_Type) < 0 )
    {
        THROW_EXCEPTION("Cannot install Goto", "Unable to register object type", SPELL_ERROR_PYTHON_API);
    }

    PyObject* Goto = Goto_New( &Goto_Type, NULL, NULL );
    Py_INCREF(Goto);

    SPELLpythonHelper::instance().install(Goto,"Goto");
}

