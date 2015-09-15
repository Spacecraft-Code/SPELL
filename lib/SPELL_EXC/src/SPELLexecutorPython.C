// ################################################################################
// FILE       : SPELLexecutorPython.C
// DATE       : Mar 17, 2011
// PROJECT    : SPELL
// DESCRIPTION: Implementation of the executor Python bindings
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
// System includes ---------------------------------------------------------
// Local includes ----------------------------------------------------------
#include "SPELL_EXC/SPELLexecutor.H"
#include "SPELL_EXC/SPELLuserAction.H"
#include "SPELL_EXC/SPELLexecutorUtils.H"
// Project includes --------------------------------------------------------
#include "structmember.h"
#include "SPELL_UTIL/SPELLlog.H"
#include "SPELL_UTIL/SPELLpythonHelper.H"
#include "SPELL_UTIL/SPELLpythonMonitors.H"
#include "SPELL_CIF/SPELLnotifications.H"
#include "SPELL_WRP/SPELLregistry.H"
#include "SPELL_WRP/SPELLpyHandle.H"
#include "SPELL_WRP/SPELLconstants.H"
using namespace PythonConstants;
using namespace LanguageModifiers;



//============================================================================
// FUNCTION        : Executor_Init
// DESCRIPTION    : Initialized of the Executor python object
//============================================================================
static int Executor_Init( PyExecutorObject* self, PyObject* args, PyObject* kwds );
//============================================================================
// FUNCTION        : Executor_Dealloc
// DESCRIPTION    : Cleanup of the Executor python object
//============================================================================
static void Executor_Dealloc( PyExecutorObject* self );
//============================================================================
// FUNCTION        : Executor_New
// DESCRIPTION    : Constructor of the Executor python object
//============================================================================
static PyObject* Executor_New( PyTypeObject* type, PyObject* args, PyObject* kwds );
//============================================================================
// FUNCTION        : Executor_GetStatus
// DESCRIPTION    : Obtain the executor status code
//============================================================================
static PyObject* Executor_GetStatus( PyObject* self, PyObject* args);
//============================================================================
// FUNCTION        : Executor_GetEnvironment
// DESCRIPTION    : Obtain the execution environment globals
//============================================================================
static PyObject* Executor_GetEnvironment( PyObject* self, PyObject* args);
//============================================================================
// FUNCTION        : Executor_ProcessLock
// DESCRIPTION    : Set the language lock
//============================================================================
static PyObject* Executor_ProcessLock( PyObject* self, PyObject* args);
//============================================================================
// FUNCTION        : Executor_ProcessUnlock
// DESCRIPTION    : Unset the language lock
//============================================================================
static PyObject* Executor_ProcessUnlock( PyObject* self, PyObject* args);
//============================================================================
// FUNCTION        : Executor_StartWait
// DESCRIPTION    : Start waiting
//============================================================================
static PyObject* Executor_StartWait( PyObject* self, PyObject* args );
//============================================================================
// FUNCTION        : Executor_StartPrompt
// DESCRIPTION    : Start a prompt
//============================================================================
static PyObject* Executor_StartPrompt( PyObject* self, PyObject* args );
//============================================================================
// FUNCTION        : Executor_Wait
// DESCRIPTION    : Wait for event
//============================================================================
static PyObject* Executor_Wait( PyObject* self, PyObject* args );
//============================================================================
// FUNCTION        : Executor_FinishWait
// DESCRIPTION    : Finish waiting
//============================================================================
static PyObject* Executor_FinishWait( PyObject* self, PyObject* args);
//============================================================================
// FUNCTION        : Executor_FinishPrompt
// DESCRIPTION    : Finish a prompt
//============================================================================
static PyObject* Executor_FinishPrompt( PyObject* self, PyObject* args);
//============================================================================
// FUNCTION        : Executor_OpenSubprocedure
// DESCRIPTION    : Open subprocedure
//============================================================================
static PyObject* Executor_OpenSubprocedure( PyObject* self, PyObject* args, PyObject* kwds);
//============================================================================
// FUNCTION        : Executor_CloseSubprocedure
// DESCRIPTION    : Close subprocedure
//============================================================================
static PyObject* Executor_CloseSubprocedure( PyObject* self, PyObject* args);
//============================================================================
// FUNCTION        : Executor_KillSubprocedure
// DESCRIPTION    : Kill subprocedure
//============================================================================
static PyObject* Executor_KillSubprocedure( PyObject* self, PyObject* args);
//============================================================================
// FUNCTION        : Executor_GetChildStatus
// DESCRIPTION    : Get subprocedure status
//============================================================================
static PyObject* Executor_GetChildStatus( PyObject* self, PyObject* args);
//============================================================================
// FUNCTION        : Executor_GetChildError
// DESCRIPTION    : Get subprocedure error info
//============================================================================
static PyObject* Executor_GetChildError( PyObject* self, PyObject* args);
//============================================================================
// FUNCTION        : Executor_IsChildError
// DESCRIPTION    : Is subprocedure in error
//============================================================================
static PyObject* Executor_IsChildError( PyObject* self, PyObject* args);
//============================================================================
// FUNCTION        : Executor_IsChildFinished
// DESCRIPTION    : Is subprocedure in finished state
//============================================================================
static PyObject* Executor_IsChildFinished( PyObject* self, PyObject* args);
//============================================================================
// FUNCTION        : Executor_Abort
// DESCRIPTION    : Abort the execution
//============================================================================
static PyObject* Executor_Abort( PyObject* self, PyObject* args);
//============================================================================
// FUNCTION        : Executor_Pause
// DESCRIPTION    : Pause the execution
//============================================================================
static PyObject* Executor_Pause( PyObject* self, PyObject* args);
//============================================================================
// FUNCTION        : Executor_Finish
// DESCRIPTION    : Finalize the execution
//============================================================================
static PyObject* Executor_Finish( PyObject* self, PyObject* args );
//============================================================================
// FUNCTION        : Executor_SetUserAction
// DESCRIPTION    : Set the user action function
//============================================================================
static PyObject* Executor_SetUserAction( PyObject* self, PyObject* args );
//============================================================================
// FUNCTION        : Executor_DismissUserAction
// DESCRIPTION    : Unset the user action function
//============================================================================
static PyObject* Executor_DismissUserAction( PyObject* self, PyObject* args );
//============================================================================
// FUNCTION        : Executor_EnableUserAction
// DESCRIPTION    : Enable the user action
//============================================================================
static PyObject* Executor_EnableUserAction( PyObject* self, PyObject* args );
//============================================================================
// FUNCTION        : Executor_DisableUserAction
// DESCRIPTION    : Disable the user action
//============================================================================
static PyObject* Executor_DisableUserAction( PyObject* self, PyObject* args );
//============================================================================
// FUNCTION        : Executor_SetStep
// DESCRIPTION     : set current step id and description
//============================================================================
static PyObject* Executor_SetStep( PyObject* self, PyObject* args );
//============================================================================
// FUNCTION        : Executor_Notify
// DESCRIPTION     : Notify arbitrary data
//============================================================================
static PyObject* Executor_Notify( PyObject* self, PyObject* args );
//============================================================================
// FUNCTION        : Executor_GetControllingHost
// DESCRIPTION     : Get the controlling host
//============================================================================
static PyObject* Executor_GetControllingHost( PyObject* self, PyObject* args );
//============================================================================
// FUNCTION        : Executor_SetExecutionDelay
// DESCRIPTION     : set execution delay
//============================================================================
static PyObject* Executor_SetExecutionDelay( PyObject* self, PyObject* args );


//============================================================================
// Executor object member specification
//============================================================================
static PyMemberDef Executor_Members[] =
{
    {NULL} /* Sentinel */
};

//============================================================================
// Executor object method specification
//============================================================================
static PyMethodDef Executor_Methods[] =
{
    {"getStatus",             Executor_GetStatus,          METH_NOARGS, "Obtain executor status"},
    {"getEnvironment",        Executor_GetEnvironment,     METH_NOARGS, "Obtain execution environment"},
    {"processLock",           Executor_ProcessLock,        METH_NOARGS, "Set the language lock"},
    {"processUnlock",         Executor_ProcessUnlock,      METH_NOARGS, "Unset the language lock"},
    {"startWait",             Executor_StartWait,          METH_VARARGS, "Start waiting process"},
    {"finishWait",            Executor_FinishWait,         METH_NOARGS, "End waiting process"},
    {"startPrompt",           Executor_StartPrompt,        METH_NOARGS, "Start prompt"},
    {"finishPrompt",          Executor_FinishPrompt,       METH_NOARGS, "End prompt"},
    {"wait",                  Executor_Wait,               METH_NOARGS, "Wait for event"},
    {"openSubProcedure",      (PyCFunction)Executor_OpenSubprocedure,    METH_VARARGS | METH_KEYWORDS, "Open child procedure"},
    {"closeSubProcedure",     Executor_CloseSubprocedure,  METH_NOARGS, "Close child procedure"},
    {"killSubProcedure",      Executor_KillSubprocedure,   METH_NOARGS, "Kill child procedure"},
    {"getChildStatus",        Executor_GetChildStatus,     METH_NOARGS, "Get child procedure status"},
    {"getChildError",         Executor_GetChildError,      METH_NOARGS, "Get child procedure error info"},
    {"isChildError",          Executor_IsChildError,       METH_NOARGS, "Is subprocedure in error"},
    {"isChildFinished",       Executor_IsChildFinished,    METH_NOARGS, "Is subprocedure finished"},
    {"abort",                 Executor_Abort,              METH_VARARGS, "Abort execution"},
    {"pause",                 Executor_Pause,              METH_NOARGS, "Pause execution"},
    {"finish",                Executor_Finish,             METH_VARARGS, "Finalize execution"},
    {"setUserAction",         Executor_SetUserAction,      METH_VARARGS, "Set the user action function"},
    {"dismissUserAction",     Executor_DismissUserAction,  METH_NOARGS, "Unset the user action function"},
    {"enableUserAction",      Executor_EnableUserAction,   METH_NOARGS, "Enable the user action function"},
    {"disableUserAction",     Executor_DisableUserAction,  METH_NOARGS, "Disable the user action function"},
    {"setStep" ,              Executor_SetStep,            METH_VARARGS, "Set current step name"},
    {"setExecutionDelay",     Executor_SetExecutionDelay,  METH_VARARGS, "Set execution delay"},
    {"notify",      		  Executor_Notify,   		   METH_VARARGS, "Notify items"},
    {"getControllingHost",    Executor_GetControllingHost, METH_NOARGS, "Get the controlling host"},
    {NULL, NULL, 0, NULL} /* Sentinel */
};

//============================================================================
// Python representation of the SPELL executor object type
//============================================================================
static PyTypeObject Executor_Type =
{
    PyObject_HEAD_INIT(NULL)
    0,                                 //ob_size
    "executor.Executor",               //tp_name
    sizeof(PyExecutorObject),          //tp_basicsize
    0,                                 //tp_itemsize
    (destructor)Executor_Dealloc,      //tp_dealloc
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
    "procedure executor",              // tp_doc
    0,                                 // tp_traverse
    0,                                 // tp_clear
    0,                                 // tp_richcompare
    0,                                 // tp_weaklistoffset
    0,                                 // tp_iter
    0,                                 // tp_iternext
    Executor_Methods,                  // tp_methods
    Executor_Members,                  // tp_members
    0,                                 // tp_getset
    0,                                 // tp_base
    0,                                 // tp_dict
    0,                                 // tp_descr_get
    0,                                 // tp_descr_set
    0,                                 // tp_dictoffset
    (initproc)Executor_Init,           // tp_init
    0,                                 // tp_alloc
    Executor_New,                      // tp_new
};

//============================================================================
// FUNCTION        : Executor_Init
//============================================================================
static int Executor_Init( PyExecutorObject* self, PyObject* args, PyObject* kwds )
{
    return 0;
}

//============================================================================
// FUNCTION        : Executor_Dealloc
//============================================================================
static void Executor_Dealloc( PyExecutorObject* self )
{
    self->ob_type->tp_free( (PyObject*) self );
}

//============================================================================
// FUNCTION        : Executor_New
//============================================================================
static PyObject* Executor_New( PyTypeObject* type, PyObject* args, PyObject* kwds )
{
    //NOTE:  Py_INCREF is already called!
    PyExecutorObject* self;
    self = (PyExecutorObject*) type->tp_alloc(type,0);
    return (PyObject*)self;
}

//============================================================================
// FUNCTION        : Executor_GetStatus
//============================================================================
static PyObject* Executor_GetStatus( PyObject* self, PyObject* args)
{
    SPELLexecutorStatus status = SPELLexecutor::instance().getStatus();
    PyObject* statusStr = SPELLexecutorUtils::statusToPyString(status);
    Py_INCREF(statusStr);
    return statusStr;
}

//============================================================================
// FUNCTION        : Executor_GetStatus
//============================================================================
static PyObject* Executor_GetEnvironment( PyObject* self, PyObject* args)
{
    PyObject* main_dict = SPELLpythonHelper::instance().getMainDict();
    Py_INCREF(main_dict);
    return main_dict;
}

//============================================================================
// FUNCTION        : Executor_Install
//============================================================================
void Executor_Install()
{
    DEBUG("[EXCPY] Installing executor object");

    if (PyType_Ready(&Executor_Type) < 0 )
    {
        THROW_EXCEPTION("Cannot install executor", "Unable to register object type", SPELL_ERROR_PYTHON_API);
    }

    PyObject* executor = Executor_New( &Executor_Type, NULL, NULL );
    Py_INCREF(executor);

    DEBUG("[EXCPY] Executor created, registering");
    SPELLregistry::instance().set( executor, "EXEC" );
    DEBUG("[EXCPY] Executor installed");
}

//============================================================================
// FUNCTION        : Executor_ProcessLock
//============================================================================
static PyObject* Executor_ProcessLock( PyObject* self, PyObject* args)
{
    SPELLexecutor::instance().processLock();
    Py_RETURN_NONE;
}

//============================================================================
// FUNCTION        : Executor_ProcessUnlock
//============================================================================
static PyObject* Executor_ProcessUnlock( PyObject* self, PyObject* args)
{
    SPELLexecutor::instance().processUnlock();
    Py_RETURN_NONE;
}

//============================================================================
// FUNCTION        : Executor_StartPrompt
//============================================================================
static PyObject* Executor_StartPrompt( PyObject* self, PyObject* args )
{
    DEBUG("[EXECPY] Start prompt" );
	try
	{
	    // Get the arguments list and config dictionary
	    PyObject* argumentsObj = NULL;
	    PyObject* configObj = NULL;

	    if (PyTuple_Size( args )>=1) argumentsObj = PyTuple_GetItem( args, 0 );
	    if (PyTuple_Size( args )==2) configObj = PyTuple_GetItem( args, 1 );

	    DEBUG("[EXECPY] Arguments " + PYREPR(argumentsObj) );
	    DEBUG("[EXECPY] Configuration " + PYREPR(configObj) );

	    SPELLpyArgs arguments(argumentsObj, configObj);

	    SPELLtime timeout;
	    if (arguments.hasModifier(LanguageModifiers::Timeout))
	    {
	    	timeout = arguments.getModifier_Timeout();
	    }
	    else
	    {
	    	timeout.set(0,0);
	    }
	    bool headless = SPELLexecutor::instance().getConfiguration().isHeadless();
		SPELLexecutor::instance().getScheduler().startPrompt( timeout, headless );
	}
	catch(SPELLcoreException& ex)
	{
		std::string what = ex.what();
		LOG_ERROR("Unable to start prompt: " + what);
		THROW_DRIVER_EXCEPTION( "EXEC::startPrompt() failed", what );
	}
	Py_RETURN_NONE;
}

//============================================================================
// FUNCTION        : Executor_StartWait
//============================================================================
static PyObject* Executor_StartWait( PyObject* self, PyObject* args )
{
    DEBUG("[EXECPY] Start wait args " + PYREPR(args) );
    SPELLscheduleCondition condition;

    //-----------------------------------------------------------
    // 1. ARGUMENT CHECK
    //-----------------------------------------------------------
    if (PyTuple_Size(args)>2)
    {
        THROW_SYNTAX_EXCEPTION("EXEC::startWait() failed", "Malformed arguments");
    }

    // Get the arguments list and config dictionary
    PyObject* argumentsObj = NULL;
    PyObject* configObj = NULL;

    if (PyTuple_Size( args )>=1) argumentsObj = PyTuple_GetItem( args, 0 );
    if (PyTuple_Size( args )==2) configObj = PyTuple_GetItem( args, 1 );

    DEBUG("[EXECPY] Arguments " + PYREPR(argumentsObj) );
    DEBUG("[EXECPY] Configuration " + PYREPR(configObj) );

    SPELLpyArgs arguments(argumentsObj, configObj);

    try
    {
		// Get condition interval and message if any
		SPELLexecutorUtils::getIntervalData( arguments, condition );

		//-----------------------------------------------------------
		// A) FIXED OR TIME WAIT (Modifiers)
		//-----------------------------------------------------------
		// If no fixed arguments are given
		if (arguments.size()==0)
		{
			SPELLexecutorUtils::configureConditionModifiers( arguments, condition );
		}
		//-----------------------------------------------------------
		// B) TIME OR CONDITION WAIT (Argument)
		//-----------------------------------------------------------
		else
		{
			SPELLexecutorUtils::configureConditionArguments( arguments, condition, configObj );
		}
		SPELLexecutor::instance().getScheduler().startWait( condition );
    }
    catch(SPELLcoreException& ex)
    {
    	if (ex.getCode() == SPELL_PYERROR_SYNTAX)
    	{
    		THROW_SYNTAX_EXCEPTION(ex.getError(), ex.getReason());
    	}
    	std::string what = ex.what();
    	LOG_ERROR("Unable to start wait: " + what);
    	THROW_DRIVER_EXCEPTION( "EXEC::startWait() failed", what );
    }
    Py_RETURN_NONE;
}

//============================================================================
// FUNCTION        : Executor_Wait
//============================================================================
static PyObject* Executor_Wait( PyObject* self, PyObject* args)
{
    DEBUG("[EXECPY] Wait");

	try
    {
		SPELLexecutor::instance().getScheduler().wait();
    }
    catch(SPELLcoreException& ex)
    {
    	std::string what = ex.what();
    	LOG_ERROR("Unable to wait: " + what);
    	THROW_DRIVER_EXCEPTION( "EXEC::wait() failed", what );
    }

    const SPELLscheduleResult& result = SPELLexecutor::instance().getScheduler().result();

    if (result.type == SPELLscheduleResult::SCH_FAILED)
    {
        THROW_DRIVER_EXCEPTION( result.error, result.reason );
    }
    else if (result.type == SPELLscheduleResult::SCH_TIMEOUT)
    {
    	Py_RETURN_FALSE;
    }

    Py_RETURN_TRUE;
}

//============================================================================
// FUNCTION        : Executor_FinishWait
//============================================================================
static PyObject* Executor_FinishWait( PyObject* self, PyObject* args)
{
    DEBUG("[EXECPY] Finish wait");
	try
    {
		SPELLexecutor::instance().getScheduler().finishWait(true,false);
    }
    catch(SPELLcoreException& ex)
    {
    	std::string what = ex.what();
    	LOG_ERROR("Unable to finish wait: " + what);
    	THROW_DRIVER_EXCEPTION( "EXEC::finishWait() failed", what );
    }
    Py_RETURN_NONE;
}

//============================================================================
// FUNCTION        : Executor_FinishPrompt
//============================================================================
static PyObject* Executor_FinishPrompt( PyObject* self, PyObject* args)
{
    DEBUG("[EXECPY] Finish prompt");
	try
    {
		SPELLexecutor::instance().getScheduler().finishPrompt();
    }
    catch(SPELLcoreException& ex)
    {
    	std::string what = ex.what();
    	LOG_ERROR("Unable to finish prompt: " + what);
    	THROW_DRIVER_EXCEPTION( "EXEC::finishPrompt() failed", what );
    }
    Py_RETURN_NONE;
}

//============================================================================
// FUNCTION        : Executor_OpenSubprocedure
//============================================================================
static PyObject* Executor_OpenSubprocedure( PyObject* self, PyObject* args, PyObject* kwds)
{
    DEBUG("[EXECPY] Open subprocedure: " + PYREPR(args) + " : " + PYREPR(kwds));
    std::string procId    = PYSTR(PyTuple_GetItem(args, 0));
    PyObject*   argumentsDict = PyTuple_GetItem(args, 1);
    PyObject*   configDict    = PyDict_GetItemString(kwds, "config");


    std::string autoStr  = PYSSTR(PyDict_GetItemString(configDict,Automatic.c_str()));
    std::string blockStr = PYSSTR(PyDict_GetItemString(configDict,Blocking.c_str()));
    std::string visibStr = PYSSTR(PyDict_GetItemString(configDict,Visible.c_str()));

    std::string arguments = PYSSTR(argumentsDict);
    try
    {
        SPELLexecutor::instance().getChildManager().openChildProcedure(procId, arguments, (autoStr == True), (blockStr == True), (visibStr == True));
        Py_RETURN_TRUE;
    }
    catch(SPELLcoreException& ex)
    {
		std::string what = ex.what();
		LOG_ERROR("Unable to start subprocedure: " + what);
		THROW_DRIVER_EXCEPTION( "Unable to start subprocedure", what );
    }
    return NULL;
}

//============================================================================
// FUNCTION        : Executor_CloseSubprocedure
//============================================================================
static PyObject* Executor_CloseSubprocedure( PyObject* self, PyObject* args)
{
    DEBUG("[EXECPY] Close subprocedure");
    try
    {
    	SPELLexecutor::instance().getChildManager().closeChildProcedure();
    }
    catch(SPELLcoreException& ex)
    {
    	std::string what = ex.what();
    	LOG_ERROR("Unable to close subprocedure: " + what);
    	THROW_DRIVER_EXCEPTION( "Unable to close subprocedure", what );
    }
    Py_RETURN_TRUE;
}

//============================================================================
// FUNCTION        : Executor_KillSubprocedure
//============================================================================
static PyObject* Executor_KillSubprocedure( PyObject* self, PyObject* args)
{
    DEBUG("[EXECPY] Kill subprocedure");
	try
    {
		SPELLexecutor::instance().getChildManager().killChildProcedure();
    }
    catch(SPELLcoreException& ex)
    {
    	std::string what = ex.what();
    	LOG_ERROR("Unable to kill subprocedure: " + what);
    	THROW_DRIVER_EXCEPTION( "Unable to kill subprocedure", what );
    }
    Py_RETURN_TRUE;
}

//============================================================================
// FUNCTION        : Executor_GetChildStatus
//============================================================================
static PyObject* Executor_GetChildStatus( PyObject* self, PyObject* args)
{
    SPELLexecutorStatus st = SPELLexecutor::instance().getChildManager().getChildStatus();
    PyObject* status = SSTRPY(SPELLexecutorUtils::statusToString(st));
    DEBUG("[EXECPY] Get child status: " + SPELLexecutorUtils::statusToString(st));
    return status;
}

//============================================================================
// FUNCTION        : Executor_GetChildError
//============================================================================
static PyObject* Executor_GetChildError( PyObject* self, PyObject* args)
{
    std::string error  = SPELLexecutor::instance().getChildManager().getChildError();
    std::string reason = SPELLexecutor::instance().getChildManager().getChildErrorReason();
    DEBUG("[EXECPY] Get child error: " + error + "(" + reason + ")");
    PyObject* err = PyList_New(2);
    PyList_SetItem( err, 0, SSTRPY(error));
    PyList_SetItem( err, 1, SSTRPY(reason));
    Py_INCREF(err);
    return err;
}

//============================================================================
// FUNCTION        : Executor_IsChildError
//============================================================================
static PyObject* Executor_IsChildError( PyObject* self, PyObject* args)
{
    SPELLexecutorStatus st = SPELLexecutor::instance().getChildManager().getChildStatus();
    if (st == STATUS_ERROR || st == STATUS_ABORTED )
    {
        DEBUG("[EXECPY] Is child error: true");
        Py_RETURN_TRUE;
    }
    else
    {
        DEBUG("[EXECPY] Is child error: false");
        Py_RETURN_FALSE;
    }
}

//============================================================================
// FUNCTION        : Executor_IsChildFinished
//============================================================================
static PyObject* Executor_IsChildFinished( PyObject* self, PyObject* args)
{
    SPELLexecutorStatus st = SPELLexecutor::instance().getChildManager().getChildStatus();
    if (st == STATUS_FINISHED)
    {
        DEBUG("[EXECPY] Is child finished: true");
        Py_RETURN_TRUE;
    }
    else
    {
        DEBUG("[EXECPY] Is child finished: false");
        Py_RETURN_FALSE;
    }
}

//============================================================================
// FUNCTION        : Executor_Abort
//============================================================================
static PyObject* Executor_Abort( PyObject* self, PyObject* args)
{
    DEBUG("[EXECPY] Aborting execution");
    std::string abortMessage = "";
    if (PyTuple_Size(args)>0)
    {
    	PyObject* pyMsg = PyTuple_GetItem(args,0);
    	abortMessage = PYSTR(pyMsg);
    }
    SPELLexecutor::instance().abort(abortMessage, false);
    Py_RETURN_NONE;
}

//============================================================================
// FUNCTION        : Executor_Pause
//============================================================================
static PyObject* Executor_Pause( PyObject* self, PyObject* args)
{
    DEBUG("[EXECPY] Pausing execution");
    SPELLexecutor::instance().pause();
    Py_RETURN_NONE;
}

//============================================================================
// FUNCTION        : Executor_Finish
//============================================================================
static PyObject* Executor_Finish( PyObject* self, PyObject* args )
{
    DEBUG("[EXECPY] Finish execution");
    std::string finishMessage = "";
    if (PyTuple_Size(args)>0)
    {
    	PyObject* pyMsg = PyTuple_GetItem(args,0);
    	finishMessage = PYSTR(pyMsg);
    }
    SPELLexecutor::instance().finish(finishMessage);
    Py_RETURN_NONE;
}

//============================================================================
// FUNCTION        : Executor_SetUserAction
//============================================================================
static PyObject* Executor_SetUserAction( PyObject* self, PyObject* args )
{
    DEBUG("[EXECPY] Set user action");
    PyObject* funcObj = PyTuple_GetItem(args,0);
    std::string label = PYSTR(PyTuple_GetItem(args, 1));
    std::string funcName = PYSTR( PyObject_GetAttrString( funcObj, "func_name" ) );
    PyObject* configObj = PyTuple_GetItem(args,2);
    unsigned int severity = LanguageConstants::INFORMATION;
    SPELLpyHandle pySev = SSTRPY(LanguageModifiers::Severity);
    if (PyDict_Contains(configObj, pySev.get()))
	{
    	PyObject* pySev = PyDict_GetItemString(configObj, LanguageModifiers::Severity.c_str());
    	severity = PyInt_AsLong(pySev);
	}
    SPELLuserAction action;
    action.setAction(funcName);
    action.setLabel(label);
    action.setSeverity(severity);
    action.enable(true);
	SPELLexecutor::instance().setUserAction(action);
    Py_RETURN_NONE;
}

//============================================================================
// FUNCTION        : Executor_DismissUserAction
//============================================================================
static PyObject* Executor_DismissUserAction( PyObject* self, PyObject* args )
{
    DEBUG("[EXECPY] Dismiss user action");
    SPELLexecutor::instance().dismissUserAction();
    Py_RETURN_NONE;
}

//============================================================================
// FUNCTION        : Executor_EnableUserAction
//============================================================================
static PyObject* Executor_EnableUserAction( PyObject* self, PyObject* args )
{
    DEBUG("[EXECPY] Enable user action");
    SPELLexecutor::instance().enableUserAction(true);
    Py_RETURN_NONE;
}

//============================================================================
// FUNCTION        : Executor_DisableUserAction
//============================================================================
static PyObject* Executor_DisableUserAction( PyObject* self, PyObject* args )
{
    DEBUG("[EXECPY] Disable user action");
    SPELLexecutor::instance().enableUserAction(false);
    Py_RETURN_NONE;
}

//============================================================================
// FUNCTION        : Executor_Notify
//============================================================================
static PyObject* Executor_Notify( PyObject* self, PyObject* args )
{
    DEBUG("[EXECPY] Notify items");

    ItemNotification notification;

    PyObject* nameObj = PyTuple_GetItem(args,0);
    PyObject* valueObj = PyTuple_GetItem(args,1);
    PyObject* statusObj = PyTuple_GetItem(args,2);

    notification.name = PYSTR(nameObj);
    notification.value = PYREPR(valueObj);
    notification.comment = "";
    notification.stack = "";
    notification.status = PYSTR(statusObj);
    notification.time = SPELLutils::timestamp();
    notification.type = NOTIFY_ITEM;

    SPELLexecutor::instance().getCIF().notify(notification);

    Py_RETURN_NONE;
}

//============================================================================
// FUNCTION        : Executor_SetStep
//============================================================================
static PyObject* Executor_SetStep( PyObject* self, PyObject* args )
{
    DEBUG("[EXECPY] Set current step");

    PyObject* idObj = PyTuple_GetItem(args,0);
    PyObject* descObj = PyTuple_GetItem(args,1);
    SPELLexecutor::instance().displayStage( PYSSTR(idObj), PYSSTR(descObj) );
    Py_RETURN_NONE;
}

//============================================================================
// FUNCTION        : Executor_SetExecutionDelay
//============================================================================
static PyObject* Executor_SetExecutionDelay( PyObject* self, PyObject* args )
{
    DEBUG("[EXECPY] Set execution delay");

    PyObject* delayObj = PyTuple_GetItem(args,0);
    if (PyLong_Check(delayObj))
    {
    	SPELLexecutor::instance().setExecDelay( PyLong_AsLong(delayObj) );
    }
    else if (PyInt_Check(delayObj))
    {
    	SPELLexecutor::instance().setExecDelay( PyInt_AsLong(delayObj) );
    }
    else if (PyFloat_Check(delayObj))
    {
    	SPELLexecutor::instance().setExecDelay( PyLong_AsLong(PyLong_FromDouble( PyFloat_AsDouble(delayObj)) ) );
    }
    Py_RETURN_NONE;
}

//============================================================================
// FUNCTION        : Executor_GetControllingHost
//============================================================================
static PyObject* Executor_GetControllingHost( PyObject* self, PyObject* args )
{
    DEBUG("[EXECPY] Get controlling host");
    std::string host = SPELLexecutor::instance().getConfiguration().getControlHost();
    if (host.empty())
    {
        Py_RETURN_NONE;
    }
    else
    {
        return Py_BuildValue("s", host.c_str());
    }
}
