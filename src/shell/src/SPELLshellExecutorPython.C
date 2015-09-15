// ################################################################################
// FILE       : SPELLshellExecutorPython.C
// DATE       : Mar 18, 2011
// PROJECT    : SPELL
// DESCRIPTION: Pything bindings for executor in shell
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
#include "SPELL_EXC/SPELLexecutorStatus.H"
#include "SPELL_EXC/SPELLexecutor.H"
#include "SPELL_EXC/SPELLexecutorUtils.H"
// Project includes --------------------------------------------------------
#include "SPELL_UTIL/SPELLbase.H"
#include "SPELL_UTIL/SPELLerror.H"
#include "SPELL_UTIL/SPELLlog.H"
#include "SPELL_UTIL/SPELLpythonHelper.H"
#include "SPELL_UTIL/SPELLpyArgs.H"
#include "SPELL_UTIL/SPELLutils.H"
#include "SPELL_WRP/SPELLregistry.H"
#include "SPELL_WRP/SPELLconstants.H"
using namespace PythonConstants;
using namespace LanguageModifiers;
// System includes ---------------------------------------------------------
#include "structmember.h"

typedef struct PyShellExecutorObject_
{
    PyObject_HEAD
} PyShellExecutorObject;

//============================================================================
// FUNCTION        : ShellExecutor_Init
// DESCRIPTION    : Initialized of the Executor python object
//============================================================================
static int ShellExecutor_Init( PyShellExecutorObject* self, PyObject* args, PyObject* kwds );
//============================================================================
// FUNCTION        : ShellExecutor_Dealloc
// DESCRIPTION    : Cleanup of the Executor python object
//============================================================================
static void ShellExecutor_Dealloc( PyShellExecutorObject* self );
//============================================================================
// FUNCTION        : ShellExecutor_New
// DESCRIPTION    : Constructor of the Executor python object
//============================================================================
static PyObject* ShellExecutor_New( PyTypeObject* type, PyObject* args, PyObject* kwds );
//============================================================================
// FUNCTION        : ShellExecutor_GetStatus
// DESCRIPTION    : Obtain the executor status code
//============================================================================
static PyObject* ShellExecutor_GetStatus( PyObject* self, PyObject* args);
//============================================================================
// FUNCTION        : ShellExecutor_GetEnvironment
// DESCRIPTION    : Obtain the execution environment globals
//============================================================================
static PyObject* ShellExecutor_GetEnvironment( PyObject* self, PyObject* args);
//============================================================================
// FUNCTION        : ShellExecutor_ProcessLock
// DESCRIPTION    : Set the language lock
//============================================================================
static PyObject* ShellExecutor_ProcessLock( PyObject* self, PyObject* args);
//============================================================================
// FUNCTION        : ShellExecutor_ProcessUnlock
// DESCRIPTION    : Unset the language lock
//============================================================================
static PyObject* ShellExecutor_ProcessUnlock( PyObject* self, PyObject* args);
//============================================================================
// FUNCTION        : ShellExecutor_StartWait
// DESCRIPTION    : Start waiting
//============================================================================
static PyObject* ShellExecutor_StartWait( PyObject* self, PyObject* args );
//============================================================================
// FUNCTION        : ShellExecutor_StartPrompt
// DESCRIPTION    : Start prompt
//============================================================================
static PyObject* ShellExecutor_StartPrompt( PyObject* self, PyObject* args );
//============================================================================
// FUNCTION        : ShellExecutor_Wait
// DESCRIPTION    : Wait for event
//============================================================================
static PyObject* ShellExecutor_Wait( PyObject* self, PyObject* args );
//============================================================================
// FUNCTION        : ShellExecutor_FinishWait
// DESCRIPTION    : Finish waiting
//============================================================================
static PyObject* ShellExecutor_FinishWait( PyObject* self, PyObject* args);
//============================================================================
// FUNCTION        : ShellExecutor_FinishPrompt
// DESCRIPTION    : Finish prompt
//============================================================================
static PyObject* ShellExecutor_FinishPrompt( PyObject* self, PyObject* args);
//============================================================================
// FUNCTION        : ShellExecutor_OpenSubprocedure
// DESCRIPTION    : Open subprocedure
//============================================================================
static PyObject* ShellExecutor_OpenSubprocedure( PyObject* self, PyObject* args, PyObject* kwds);
//============================================================================
// FUNCTION        : ShellExecutor_CloseSubprocedure
// DESCRIPTION    : Close subprocedure
//============================================================================
static PyObject* ShellExecutor_CloseSubprocedure( PyObject* self, PyObject* args);
//============================================================================
// FUNCTION        : ShellExecutor_KillSubprocedure
// DESCRIPTION    : Kill subprocedure
//============================================================================
static PyObject* ShellExecutor_KillSubprocedure( PyObject* self, PyObject* args);
//============================================================================
// FUNCTION        : ShellExecutor_GetChildStatus
// DESCRIPTION    : Get subprocedure status
//============================================================================
static PyObject* ShellExecutor_GetChildStatus( PyObject* self, PyObject* args);
//============================================================================
// FUNCTION        : ShellExecutor_GetChildError
// DESCRIPTION    : Get subprocedure error info
//============================================================================
static PyObject* ShellExecutor_GetChildError( PyObject* self, PyObject* args);
//============================================================================
// FUNCTION        : ShellExecutor_IsChildError
// DESCRIPTION    : Is subprocedure in error
//============================================================================
static PyObject* ShellExecutor_IsChildError( PyObject* self, PyObject* args);
//============================================================================
// FUNCTION        : ShellExecutor_IsChildFinished
// DESCRIPTION    : Is subprocedure in finished state
//============================================================================
static PyObject* ShellExecutor_IsChildFinished( PyObject* self, PyObject* args);
//============================================================================
// FUNCTION        : ShellExecutor_Abort
// DESCRIPTION    : Abort the execution
//============================================================================
static PyObject* ShellExecutor_Abort( PyObject* self, PyObject* args);


//============================================================================
// Executor object member specification
//============================================================================
static PyMemberDef ShellExecutor_Members[] =
{
    {NULL} /* Sentinel */
};

//============================================================================
// Executor object method specification
//============================================================================
static PyMethodDef ShellExecutor_Methods[] =
{
    {"getStatus",             ShellExecutor_GetStatus,          METH_NOARGS, "Obtain executor status"},
    {"getEnvironment",        ShellExecutor_GetEnvironment,     METH_NOARGS, "Obtain execution environment"},
    {"processLock",           ShellExecutor_ProcessLock,        METH_NOARGS, "Set the language lock"},
    {"processUnlock",         ShellExecutor_ProcessUnlock,      METH_NOARGS, "Unset the language lock"},
    {"startWait",             ShellExecutor_StartWait,          METH_VARARGS, "Start waiting process"},
    {"finishWait",            ShellExecutor_FinishWait,         METH_NOARGS, "End waiting process"},
    {"startPrompt",           ShellExecutor_StartPrompt,        METH_NOARGS, "Start prompt"},
    {"finishPrompt",          ShellExecutor_FinishPrompt,       METH_NOARGS, "End prompt"},
    {"wait",                  ShellExecutor_Wait,               METH_NOARGS, "Wait for event"},
    {"openSubProcedure",      (PyCFunction)ShellExecutor_OpenSubprocedure,    METH_VARARGS | METH_KEYWORDS, "Open child procedure"},
    {"closeSubProcedure",     ShellExecutor_CloseSubprocedure,  METH_NOARGS, "Close child procedure"},
    {"killSubProcedure",      ShellExecutor_KillSubprocedure,   METH_NOARGS, "Kill child procedure"},
    {"getChildStatus",        ShellExecutor_GetChildStatus,     METH_NOARGS, "Get child procedure status"},
    {"getChildError",         ShellExecutor_GetChildError,      METH_NOARGS, "Get child procedure error info"},
    {"isChildError",          ShellExecutor_IsChildError,       METH_NOARGS, "Is subprocedure in error"},
    {"isChildFinished",       ShellExecutor_IsChildFinished,    METH_NOARGS, "Is subprocedure finished"},
    {"abort",                 ShellExecutor_Abort,              METH_NOARGS, "Abort execution"},
    {NULL, NULL, 0, NULL} /* Sentinel */
};

//============================================================================
// Python representation of the SPELL executor object type
//============================================================================
static PyTypeObject ShellExecutor_Type =
{
    PyObject_HEAD_INIT(NULL)
    0,                                 //ob_size
    "shell.ShellExecutor",       //tp_name
    sizeof(PyShellExecutorObject),          //tp_basicsize
    0,                                 //tp_itemsize
    (destructor)ShellExecutor_Dealloc,      //tp_dealloc
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
    "shell interactive executor",      // tp_doc
    0,                                 // tp_traverse
    0,                                 // tp_clear
    0,                                 // tp_richcompare
    0,                                 // tp_weaklistoffset
    0,                                 // tp_iter
    0,                                 // tp_iternext
    ShellExecutor_Methods,       // tp_methods
    ShellExecutor_Members,       // tp_members
    0,                                 // tp_getset
    0,                                 // tp_base
    0,                                 // tp_dict
    0,                                 // tp_descr_get
    0,                                 // tp_descr_set
    0,                                 // tp_dictoffset
    (initproc)ShellExecutor_Init,// tp_init
    0,                                 // tp_alloc
    ShellExecutor_New,           // tp_new
};

//============================================================================
// FUNCTION        : ShellExecutor_Init
//============================================================================
static int ShellExecutor_Init( PyShellExecutorObject* self, PyObject* args, PyObject* kwds )
{
    return 0;
}

//============================================================================
// FUNCTION        : ShellExecutor_Dealloc
//============================================================================
static void ShellExecutor_Dealloc( PyShellExecutorObject* self )
{
    self->ob_type->tp_free( (PyObject*) self );
}

//============================================================================
// FUNCTION        : ShellExecutor_New
//============================================================================
static PyObject* ShellExecutor_New( PyTypeObject* type, PyObject* args, PyObject* kwds )
{
    //NOTE:  Py_INCREF is already called!
    PyShellExecutorObject* self;
    self = (PyShellExecutorObject*) type->tp_alloc(type,0);
    return (PyObject*)self;
}

//============================================================================
// FUNCTION        : ShellExecutor_GetStatus
//============================================================================
static PyObject* ShellExecutor_GetStatus( PyObject* self, PyObject* args)
{
    PyObject* statusStr = SPELLexecutorUtils::statusToPyString(STATUS_UNINIT);
    Py_INCREF(statusStr);
    return statusStr;
}

//============================================================================
// FUNCTION        : ShellExecutor_GetStatus
//============================================================================
static PyObject* ShellExecutor_GetEnvironment( PyObject* self, PyObject* args)
{
    PyObject* main_dict = SPELLpythonHelper::instance().getMainDict();
    Py_INCREF(main_dict);
    return main_dict;
}

//============================================================================
// FUNCTION        : ShellExecutor_Install
//============================================================================
void ShellExecutor_Install()
{
    if (PyType_Ready(&ShellExecutor_Type) < 0 )
    {
        THROW_EXCEPTION("Cannot install executor", "Unable to register object type", SPELL_ERROR_PYTHON_API);
    }

    PyObject* executor = ShellExecutor_New( &ShellExecutor_Type, NULL, NULL );
    Py_INCREF(executor);
    SPELLregistry::instance().set( executor, "EXEC" );
}

//============================================================================
// FUNCTION        : ShellExecutor_ProcessLock
//============================================================================
static PyObject* ShellExecutor_ProcessLock( PyObject* self, PyObject* args)
{
    Py_RETURN_NONE;
}

//============================================================================
// FUNCTION        : ShellExecutor_ProcessUnlock
//============================================================================
static PyObject* ShellExecutor_ProcessUnlock( PyObject* self, PyObject* args)
{
    Py_RETURN_NONE;
}

//============================================================================
// FUNCTION        : ShellExecutor_StartWait
//============================================================================
static PyObject* ShellExecutor_StartWait(PyObject* self, PyObject* args)
{
	SPELLscheduleCondition condition;

	//-----------------------------------------------------------
	// 1. ARGUMENT CHECK
	//-----------------------------------------------------------
	if (PyTuple_Size(args) > 2)
	{
		THROW_SYNTAX_EXCEPTION("EXEC::startWait() failed","Malformed arguments");
		return NULL;
	}

	// Get the arguments list and config dictionary
	PyObject* argumentsObj = NULL;
	PyObject* configObj = NULL;

	if (PyTuple_Size(args) >= 1)
		argumentsObj = PyTuple_GetItem(args, 0);
	if (PyTuple_Size(args) == 2)
		configObj = PyTuple_GetItem(args, 1);

	SPELLpyArgs arguments(argumentsObj, configObj);

	try
	{
		// Get condition interval and message if any
		SPELLexecutorUtils::getIntervalData(arguments, condition);

		//-----------------------------------------------------------
		// A) FIXED OR TIME WAIT (Modifiers)
		//-----------------------------------------------------------
		// If no fixed arguments are given (only con
		if (arguments.size() == 0)
		{
			SPELLexecutorUtils::configureConditionModifiers(arguments,
					condition);
		}
		//-----------------------------------------------------------
		// B) TIME OR CONDITION WAIT (Argument)
		//-----------------------------------------------------------
		else
		{
			SPELLexecutorUtils::configureConditionArguments(arguments,
					condition, configObj);
		}
		SPELLexecutor::instance().getScheduler().startWait(condition);
	} catch (SPELLcoreException& ex)
	{
		if (ex.getCode() == SPELL_PYERROR_SYNTAX )
		{
			THROW_SYNTAX_EXCEPTION(ex.getError(), ex.getReason());
			return NULL;
		}
		std::string what = ex.what();
		LOG_ERROR("Unable to start wait: " + what);
		THROW_DRIVER_EXCEPTION("EXEC::startWait() failed", what);
		return NULL;
	}
	Py_RETURN_NONE;
}

//============================================================================
// FUNCTION        : ShellExecutor_Wait
//============================================================================
static PyObject* ShellExecutor_Wait( PyObject* self, PyObject* args)
{
    Py_RETURN_NONE;
}

//============================================================================
// FUNCTION        : ShellExecutor_FinishWait
//============================================================================
static PyObject* ShellExecutor_FinishWait( PyObject* self, PyObject* args)
{
    Py_RETURN_NONE;
}

//============================================================================
// FUNCTION        : ShellExecutor_StartPrompt
//============================================================================
static PyObject* ShellExecutor_StartPrompt( PyObject* self, PyObject* args)
{
    Py_RETURN_NONE;
}

//============================================================================
// FUNCTION        : ShellExecutor_FinishPrompt
//============================================================================
static PyObject* ShellExecutor_FinishPrompt( PyObject* self, PyObject* args)
{
    Py_RETURN_NONE;
}

//============================================================================
// FUNCTION        : ShellExecutor_OpenSubprocedure
//============================================================================
static PyObject* ShellExecutor_OpenSubprocedure( PyObject* self, PyObject* args, PyObject* kwds)
{
    Py_RETURN_TRUE;
}

//============================================================================
// FUNCTION        : ShellExecutor_CloseSubprocedure
//============================================================================
static PyObject* ShellExecutor_CloseSubprocedure( PyObject* self, PyObject* args)
{
    Py_RETURN_TRUE;
}

//============================================================================
// FUNCTION        : ShellExecutor_KillSubprocedure
//============================================================================
static PyObject* ShellExecutor_KillSubprocedure( PyObject* self, PyObject* args)
{
    Py_RETURN_TRUE;
}

//============================================================================
// FUNCTION        : ShellExecutor_GetChildStatus
//============================================================================
static PyObject* ShellExecutor_GetChildStatus( PyObject* self, PyObject* args)
{
    Py_RETURN_NONE;
}

//============================================================================
// FUNCTION        : ShellExecutor_GetChildError
//============================================================================
static PyObject* ShellExecutor_GetChildError( PyObject* self, PyObject* args)
{
    Py_RETURN_NONE;
}

//============================================================================
// FUNCTION        : ShellExecutor_IsChildError
//============================================================================
static PyObject* ShellExecutor_IsChildError( PyObject* self, PyObject* args)
{
    Py_RETURN_FALSE;
}

//============================================================================
// FUNCTION        : ShellExecutor_IsChildFinished
//============================================================================
static PyObject* ShellExecutor_IsChildFinished( PyObject* self, PyObject* args)
{
    Py_RETURN_TRUE;
}

//============================================================================
// FUNCTION        : ShellExecutor_Abort
//============================================================================
static PyObject* ShellExecutor_Abort( PyObject* self, PyObject* args)
{
    Py_RETURN_NONE;
}
