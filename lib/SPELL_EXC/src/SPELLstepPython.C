// ################################################################################
// FILE       : SPELLstepPython.C
// DATE       : Mar 17, 2011
// PROJECT    : SPELL
// DESCRIPTION: Implementation of the Step Python bindings
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
// FUNCTION        : Step_Init
// DESCRIPTION    : Initialized of the Step python object
//============================================================================
static int Step_Init( PyStepObject* self, PyObject* args, PyObject* kwds );
//============================================================================
// FUNCTION        : Step_Dealloc
// DESCRIPTION    : Cleanup of the Step python object
//============================================================================
static void Step_Dealloc( PyStepObject* self );
//============================================================================
// FUNCTION        : Step_New
// DESCRIPTION    : Constructor of the Step python object
//============================================================================
static PyObject* Step_New( PyTypeObject* type, PyObject* args, PyObject* kwds );
//============================================================================
// FUNCTION        : Step_Call
// DESCRIPTION    : Call a step instruction
//============================================================================
static PyObject* Step_Call( PyObject* self, PyObject* args, PyObject* kargs );



//============================================================================
// Step object member specification
//============================================================================
static PyMemberDef Step_Members[] =
{
    {NULL} /* Sentinel */
};

//============================================================================
// Step object method specification
//============================================================================
static PyMethodDef Step_Methods[] =
{
    {NULL} /* Sentinel */
};

//============================================================================
// Python representation of the SPELL Step object type
//============================================================================
static PyTypeObject Step_Type =
{
    PyObject_HEAD_INIT(NULL)
    0,                                 //ob_size
    "executor.Step",                   //tp_name
    sizeof(PyStepObject),              //tp_basicsize
    0,                                 //tp_itemsize
    (destructor)Step_Dealloc,          //tp_dealloc
    0,                                 //tp_print
    0,                                 //tp_getattr
    0,                                 //tp_setattr
    0,                                 //tp_compare
    0,                                 //tp_repr
    0,                                 //tp_as_number
    0,                                 //tp_as_sequence
    0,                                 //tp_as_mapping
    0,                                 //tp_hash
    Step_Call,                         //tp_call
    0,                                 //tp_str
    0,                                 //tp_getattro
    0,                                 //tp_setattro
    0,                                 //tp_as_buffer
    Py_TPFLAGS_DEFAULT | Py_TPFLAGS_BASETYPE, //tp_flags
    "Step mechanism",                  // tp_doc
    0,                                 // tp_traverse
    0,                                 // tp_clear
    0,                                 // tp_richcompare
    0,                                 // tp_weaklistoffset
    0,                                 // tp_iter
    0,                                 // tp_iternext
    Step_Methods,                      // tp_methods
    Step_Members,                      // tp_members
    0,                                 // tp_getset
    0,                                 // tp_base
    0,                                 // tp_dict
    0,                                 // tp_descr_get
    0,                                 // tp_descr_set
    0,                                 // tp_dictoffset
    (initproc)Step_Init,               // tp_init
    0,                                 // tp_alloc
    Step_New,                          // tp_new
};


//============================================================================
// FUNCTION        : Step_Init
//============================================================================
static int Step_Init( PyStepObject* self, PyObject* args, PyObject* kwds )
{
    return 0;
}

//============================================================================
// FUNCTION        : Step_Dealloc
//============================================================================
static void Step_Dealloc( PyStepObject* self )
{
    self->ob_type->tp_free( (PyObject*) self );
}

//============================================================================
// FUNCTION        : Step_New
//============================================================================
static PyObject* Step_New( PyTypeObject* type, PyObject* args, PyObject* kwds )
{
    //NOTE:  Py_INCREF is already called!
    PyStepObject* self;
    self = (PyStepObject*) type->tp_alloc(type,0);
    return (PyObject*)self;
}

//============================================================================
// FUNCTION        : Step_Call
//============================================================================
static PyObject* Step_Call( PyObject* self, PyObject* args, PyObject* kargs )
{
	std::string stageId = "<\?\?\?>";
	std::string stageTitle = "<\?\?\?>";

	if ((args != NULL)&&(PyTuple_Size(args)==2))
	{
		PyObject* id = PyTuple_GetItem(args,0);
		PyObject* title = PyTuple_GetItem(args,1);
		stageId = PYSTR(id);
		stageTitle = PYSTR(title);
	}
    SPELLexecutor::instance().stageReached(stageId,stageTitle);
    Py_RETURN_NONE;
}

//============================================================================
// FUNCTION        : Step_Install
//============================================================================
void Step_Install()
{
    DEBUG("Installing Step object");

    if (PyType_Ready(&Step_Type) < 0 )
    {
        THROW_EXCEPTION("Cannot install Step", "Unable to register object type", SPELL_ERROR_PYTHON_API);
    }

    PyObject* Step = Step_New( &Step_Type, NULL, NULL );
    Py_INCREF(Step);

    SPELLpythonHelper::instance().install(Step,"Step");
}
