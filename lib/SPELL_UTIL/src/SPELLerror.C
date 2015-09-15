// ################################################################################
// FILE       : SPELLerror.C
// DATE       : Mar 17, 2011
// PROJECT    : SPELL
// DESCRIPTION: Implementation of the Python error bindings
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
#include "SPELL_UTIL/SPELLerror.H"
#include "SPELL_UTIL/SPELLpythonHelper.H"
#include "SPELL_UTIL/SPELLpythonMonitors.H"
// Project includes --------------------------------------------------------
#include "SPELL_EXC/SPELLexecutor.H"
#include "SPELL_SYN/SPELLmutex.H"
#include "SPELL_SYN/SPELLmonitor.H"
// System includes ---------------------------------------------------------
#include "structmember.h"


// GLOBALS /////////////////////////////////////////////////////////////////


SPELLerror* SPELLerror::s_instance = NULL;
SPELLmutex SPELLerror::s_instanceLock;


//============================================================================
// FUNCTION        : ExecutionAborted_Init
// DESCRIPTION    : Initialized of the ExecutionAborted python object
//============================================================================
static int ExecutionAborted_Init( PyExecutionAbortedObject* self, PyObject* args, PyObject* kwds );
//============================================================================
// FUNCTION        : ExecutionAborted_Dealloc
// DESCRIPTION    : Cleanup of the ExecutionAborted python object
//============================================================================
static void ExecutionAborted_Dealloc( PyExecutionAbortedObject* self );
//============================================================================
// FUNCTION        : ExecutionAborted_New
// DESCRIPTION    : Constructor of the ExecutionAborted python object
//============================================================================
static PyObject* ExecutionAborted_New( PyTypeObject* type, PyObject* args, PyObject* kwds );



//============================================================================
// FUNCTION        : ExecutionTerminated_Init
// DESCRIPTION    : Initialized of the ExecutionTerminated python object
//============================================================================
static int ExecutionTerminated_Init( PyExecutionTerminatedObject* self, PyObject* args, PyObject* kwds );
//============================================================================
// FUNCTION        : ExecutionTerminated_Dealloc
// DESCRIPTION    : Cleanup of the ExecutionTerminated python object
//============================================================================
static void ExecutionTerminated_Dealloc( PyExecutionTerminatedObject* self );
//============================================================================
// FUNCTION        : ExecutionTerminated_New
// DESCRIPTION    : Constructor of the ExecutionTerminated python object
//============================================================================
static PyObject* ExecutionTerminated_New( PyTypeObject* type, PyObject* args, PyObject* kwds );



//============================================================================
// ExecutionAborted object member specification
//============================================================================
static PyMemberDef ExecutionAborted_Members[] =
{
    {NULL} /* Sentinel */
};

//============================================================================
// ExecutionAborted object method specification
//============================================================================
static PyMethodDef ExecutionAborted_Methods[] =
{
    {NULL} /* Sentinel */
};

//============================================================================
// Python representation of the SPELL ExecutionAborted object type
//============================================================================
static PyTypeObject ExecutionAborted_Type =
{
    PyObject_HEAD_INIT(NULL)
    0,                                 //ob_size
    "executor.ExecutionAborted",       //tp_name
    sizeof(PyExecutionAbortedObject),  //tp_basicsize
    0,                                 //tp_itemsize
    (destructor)ExecutionAborted_Dealloc,     //tp_dealloc
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
    "ExecutionAborted exception",      // tp_doc
    0,                                 // tp_traverse
    0,                                 // tp_clear
    0,                                 // tp_richcompare
    0,                                 // tp_weaklistoffset
    0,                                 // tp_iter
    0,                                 // tp_iternext
    ExecutionAborted_Methods,          // tp_methods
    ExecutionAborted_Members,          // tp_members
    0,                                 // tp_getset
    0,                                 // tp_base
    0,                                 // tp_dict
    0,                                 // tp_descr_get
    0,                                 // tp_descr_set
    0,                                 // tp_dictoffset
    (initproc)ExecutionAborted_Init,   // tp_init
    0,                                 // tp_alloc
    ExecutionAborted_New,              // tp_new
};

//============================================================================
// ExecutionTerminated object member specification
//============================================================================
static PyMemberDef ExecutionTerminated_Members[] =
{
    {NULL} /* Sentinel */
};

//============================================================================
// ExecutionTerminated object method specification
//============================================================================
static PyMethodDef ExecutionTerminated_Methods[] =
{
    {NULL} /* Sentinel */
};

//============================================================================
// Python representation of the SPELL ExecutionTerminated object type
//============================================================================
static PyTypeObject ExecutionTerminated_Type =
{
    PyObject_HEAD_INIT(NULL)
    0,                                 //ob_size
    "executor.ExecutionTerminated",    //tp_name
    sizeof(PyExecutionTerminatedObject),        //tp_basicsize
    0,                                 //tp_itemsize
    (destructor)ExecutionTerminated_Dealloc,     //tp_dealloc
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
    "ExecutionTerminated exception",   // tp_doc
    0,                                 // tp_traverse
    0,                                 // tp_clear
    0,                                 // tp_richcompare
    0,                                 // tp_weaklistoffset
    0,                                 // tp_iter
    0,                                 // tp_iternext
    ExecutionTerminated_Methods,       // tp_methods
    ExecutionTerminated_Members,       // tp_members
    0,                                 // tp_getset
    0,                                 // tp_base
    0,                                 // tp_dict
    0,                                 // tp_descr_get
    0,                                 // tp_descr_set
    0,                                 // tp_dictoffset
    (initproc)ExecutionTerminated_Init,// tp_init
    0,                                 // tp_alloc
    ExecutionTerminated_New,           // tp_new
};


//============================================================================
// FUNCTION        : ExecutionAborted_Init
//============================================================================
static int ExecutionAborted_Init( PyExecutionAbortedObject* self, PyObject* args, PyObject* kwds )
{
    PyObject* message=NULL;

    if (! PyArg_ParseTuple(args, "S", &message))
    {
        return -1;
    }
    if (message)
    {
        PyObject* tmp = self->message;
        Py_INCREF(message);
        self->message = message;
        Py_XDECREF(tmp);
    }
    return 0;
}

//============================================================================
// FUNCTION        : ExecutionAborted_Dealloc
//============================================================================
static void ExecutionAborted_Dealloc( PyExecutionAbortedObject* self )
{
    Py_XDECREF(self->message);
    self->ob_type->tp_free( (PyObject*) self );
}

//============================================================================
// FUNCTION        : ExecutionAborted_New
//============================================================================
static PyObject* ExecutionAborted_New( PyTypeObject* type, PyObject* args, PyObject* kwds )
{
    //NOTE:  Py_INCREF is already called!
    PyExecutionAbortedObject* self;
    self = (PyExecutionAbortedObject*) type->tp_alloc(type,0);
    if (self != NULL)
    {
        self->message = PyString_FromString("");
        if (self->message == NULL)
        {
            Py_DECREF(self);
            return NULL;
        }

    }
    return (PyObject*)self;
}

//============================================================================
// FUNCTION        : ExecutionTerminated_Init
//============================================================================
static int ExecutionTerminated_Init( PyExecutionTerminatedObject* self, PyObject* args, PyObject* kwds )
{
    return 0;
}

//============================================================================
// FUNCTION        : ExecutionTerminated_Dealloc
//============================================================================
static void ExecutionTerminated_Dealloc( PyExecutionTerminatedObject* self )
{
    self->ob_type->tp_free( (PyObject*) self );
}

//============================================================================
// FUNCTION        : ExecutionTerminated_New
//============================================================================
static PyObject* ExecutionTerminated_New( PyTypeObject* type, PyObject* args, PyObject* kwds )
{
    //NOTE:  Py_INCREF is already called!
    PyExecutionTerminatedObject* self;
    self = (PyExecutionTerminatedObject*) type->tp_alloc(type,0);
    return (PyObject*)self;
}


//============================================================================
// CONSTRUCTOR : SPELLerror::SPELLerror()
//============================================================================
SPELLerror::SPELLerror()
{
	m_ctrlExceptionSet = EXC_NONE;
	m_currentError = NULL;
    PyType_Ready(&ExecutionAborted_Type);
    m_excExecutionAborted = (PyObject*) PyObject_New( PyExecutionAbortedObject, &ExecutionAborted_Type );
    PyType_Ready(&ExecutionTerminated_Type);
    m_excExecutionTerminated = (PyObject*) PyObject_New( PyExecutionTerminatedObject, &ExecutionTerminated_Type );
}

//============================================================================
// DESTRUCTOR: SPELLerror::~SPELLerror()
//============================================================================
SPELLerror::~SPELLerror()
{

}

//============================================================================
// STATIC: SPELLerror::instance()
//============================================================================
SPELLerror& SPELLerror::instance()
{
	SPELLmonitor m(s_instanceLock);
	if (s_instance == NULL)
	{
		s_instance = new SPELLerror();
	}
	return *s_instance;
}

//============================================================================
// METHOD: SPELLerror::clearErrors()
//============================================================================
void SPELLerror::clearErrors()
{
	LOG_WARN("Clear errors ========================================================");
	SPELLsafePythonOperations ops("SPELLerror::clearErrors()");
    PyErr_Clear();
    m_ctrlExceptionSet = EXC_NONE;
	if (m_currentError) delete m_currentError;
	m_currentError = NULL;
	m_errorLocation = "";
}

//============================================================================
// METHOD: SPELLerror::clearErrors()
//============================================================================
void SPELLerror::updateErrors()
{
	DEBUG("[ERR] Updating errors");
	// If a control exception is set, do not update
	if (m_ctrlExceptionSet != EXC_NONE)
	{
		DEBUG("[ERR] Control exception set, nothing to update");
		return;
	}

	// Otherwise, ensure that the tracked error is the same as in Python layer
	// provided that there is one
	SPELLcoreException* exc = errorToException();
	if (exc != NULL)
	{
		DEBUG("[ERR] There is an error in Python layer: " + exc->what());
		if (m_currentError == NULL)
		{
			DEBUG("[ERR] Updating current Python error" );
			m_currentError = exc;
			m_errorLocation = exc->at();
		}
		// If the Python error is already tracked do nothing
		else if ((*m_currentError) == (*exc))
		{
			DEBUG("[ERR] Error already tracked");
			return;
		}
		// If the Python error is different from the one tracked, substitute it
		else
		{
			DEBUG("[ERR] Replacing error " + m_currentError->what() + " by new one");
			delete m_currentError;
			m_currentError = NULL;
			m_errorLocation = "";
			m_currentError = exc;
			m_errorLocation = exc->at();
		}
	}
	// If there is no error in the Python layer ensure that the
	// current error is clean here
	else if (m_currentError != NULL)
	{
		DEBUG("[ERR] No error in Python layer, but we need cleanup: " + m_currentError->what() );
		delete m_currentError;
		m_currentError = NULL;
		m_errorLocation = "";
	}
}

//============================================================================
// METHOD: SPELLerror::setExecutionAborted()
//============================================================================
void SPELLerror::setExecutionAborted()
{
	DEBUG("[ERR] Setting execution aborted");
	if ( m_ctrlExceptionSet == EXC_NONE )
	{
		LOG_WARN("Setting EXECUTION ABORTED");
		SPELLsafePythonOperations ops("terminate");
		clearErrors();
		PyErr_SetString( PyExc_RuntimeError, "EXECUTION ABORTED");
		m_ctrlExceptionSet = EXC_ABORTED;
		m_currentError = new SPELLcoreException("Execution aborted", "", SPELL_ERROR_EXECUTION, true);
		m_errorLocation = "(none)";
		LOG_WARN("Setting EXECUTION ABORTED done");
	}
}

//============================================================================
// METHOD: SPELLerror::setExecutionTerminated()
//============================================================================
void SPELLerror::setExecutionTerminated()
{
	DEBUG("[ERR] Setting execution terminated");
	if ( m_ctrlExceptionSet == EXC_NONE )
	{
		LOG_WARN("Setting EXECUTION TERMINATED");
		clearErrors();
		SPELLsafePythonOperations ops("SPELLerror::executionTerminated");
		PyErr_SetString( PyExc_RuntimeError, "EXECUTION TERMINATED");
		m_ctrlExceptionSet = EXC_TERMINATED;
		m_currentError = new SPELLcoreException("Execution terminated", "", SPELL_ERROR_EXECUTION, true);
		m_errorLocation = "(none)";
		LOG_WARN("Setting EXECUTION TERMINATED done");
	}
	else
	{
		DEBUG("[ERR] Cannot set execution terminated, already set to " + ISTR(m_ctrlExceptionSet));
	}
}

//============================================================================
// METHOD: SPELLerror::isExecutionAborted()
//============================================================================
bool SPELLerror::isExecutionAborted()
{
    return (m_ctrlExceptionSet == EXC_ABORTED);
}

//============================================================================
// METHOD: SPELLerror::isExecutionAborted()
//============================================================================
bool SPELLerror::isExecutionTerminated()
{
    return (m_ctrlExceptionSet == EXC_TERMINATED);
}

//=============================================================================
// METHOD    : SPELLerror::errorToException
//=============================================================================
SPELLcoreException* SPELLerror::errorToException()
{
    SPELLcoreException* exception = NULL;
    PyObject* err = PyErr_Occurred();
    if (err != NULL)
    {
        PyObject* ptype;
        PyObject* pvalue;
        PyObject* ptraceback;
        // Fetch the error information
        PyErr_Fetch( &ptype, &pvalue, &ptraceback );

        exception = errorToException( err, ptype, pvalue, ptraceback );
    }
    return exception;
}

//=============================================================================
// METHOD    : SPELLerror::errorToException
//=============================================================================
SPELLcoreException* SPELLerror::errorToException( PyObject* err, PyObject* ptype, PyObject* pvalue, PyObject* ptraceback )
{
    SPELLcoreException* exception = NULL;
    DEBUG("[PYH] Fetching error information");

    // Otherwise gather error information
    std::string value = PyString_AsString( PyObject_Str(pvalue) );
    std::string proc = "???";
    int line = -1;

    std::string message = "Error back trace:\n";
    std::string reason = PYSSTR(pvalue);

    // Parse and show the backtrace if any available
    LOG_ERROR("**************************************");
    LOG_ERROR(" PYTHON ERROR: " + PYREPR(ptype));
    PyTracebackObject* tb = (PyTracebackObject*) ptraceback;

    if (tb != NULL)
    {
		while(tb != NULL)
		{
			proc = PYSTR(tb->tb_frame->f_code->co_filename);
			line = tb->tb_lineno;
			std::string at = proc + ":" + ISTR(line);
			LOG_ERROR( "  - At: " + at);
			tb = tb->tb_next;
			message += " - file '" + proc + "', line " + ISTR(line) + ";\n";
		}
		message += "Reason: ";
    }
    else
    {
    	message = " (no back trace information)\n";
    }
    LOG_ERROR("**************************************");

    // Ensure that proc and line contain the top of the traceback
    tb = (PyTracebackObject*) ptraceback;
    if (tb && tb->tb_frame && tb->tb_frame->f_code && tb->tb_frame->f_code->co_filename)
    {
    	proc = PYSTR(tb->tb_frame->f_code->co_filename);
        line = tb->tb_lineno;
    }
    else
    {
		PyObject* data = PyTuple_GetItem(pvalue, 1);
		if (PyTuple_Size(data)==4)
		{
			PyObject* filename = PyTuple_GetItem( data, 0);
			PyObject* lineno   = PyTuple_GetItem( data, 1);
			proc = PYSSTR(filename);
			line = PyLong_AsLong(lineno);
		}
    }

    // Find out the kind of error
    if (SPELLpythonHelper::instance().isInstance(err, "SpellException", "spell.lib.exception"))
    {
    	exception = NULL;
    }
    else if (PyErr_GivenExceptionMatches(err, PyExc_ImportError))
    {
        message = "Import error\n" + message;
        exception = new SPELLcoreException( message, reason, SPELL_PYERROR_IMPORT, false );
    }
    else if (PyErr_GivenExceptionMatches(err, PyExc_IndexError))
    {
        message = "Index error\n" + message;
        exception = new SPELLcoreException( message, reason, SPELL_PYERROR_INDEX, false );
    }
    else if (PyErr_GivenExceptionMatches(err, PyExc_KeyError))
    {
        message = "Key error\n" + message;
        exception = new SPELLcoreException( message, reason, SPELL_PYERROR_KEY, false );
    }
    else if (PyErr_GivenExceptionMatches(err, PyExc_MemoryError))
    {
        message = "Memory error\n" + message;
        exception = new SPELLcoreException( message, reason, SPELL_PYERROR_MEMORY, false );
    }
    else if (PyErr_GivenExceptionMatches(err, PyExc_NameError))
    {
        message = "Name error\n" + message;
        exception = new SPELLcoreException( message, reason, SPELL_PYERROR_NAME, false );
    }
    else if (PyErr_GivenExceptionMatches(err, PyExc_SyntaxError))
    {
        // It is a tuple like ('invalid syntax', ('file.py', lineno, offset, 'text'))
		message = "Unknown syntax error (could not gather information)\n" + message;
    	if (PyTuple_Size(pvalue)==2)
    	{
    		PyObject* data = PyTuple_GetItem(pvalue, 1);
    		if (PyTuple_Size(data)==4)
    		{
    			PyObject* filename = PyTuple_GetItem( data, 0);
    			PyObject* lineno   = PyTuple_GetItem( data, 1);
    			PyObject* text     = PyTuple_GetItem( data, 3);
    			message = message + "At " + PYREPR(filename) + ", line " + PYSSTR(lineno) + ": \n" + PYREPR(text);
    		}
    	}
        exception = new SPELLcoreException( message, "Invalid syntax", SPELL_PYERROR_SYNTAX, false );
    }
    else if (PyErr_GivenExceptionMatches(err, PyExc_TypeError))
    {
        message = "Type error\n" + message;
        exception = new SPELLcoreException( message, reason, SPELL_PYERROR_TYPE, false );
    }
    else if (PyErr_GivenExceptionMatches(err, PyExc_ValueError))
    {
        message = "Value error\n" + message;
        exception = new SPELLcoreException( message, reason, SPELL_PYERROR_VALUE, false );
    }
    else if (PyErr_GivenExceptionMatches(err, PyExc_BaseException))
    {
        message = "Python error\n" + message;
        exception = new SPELLcoreException( message, reason, SPELL_PYERROR_OTHER, false );
    }
    else
    {
        exception = new SPELLcoreException("Uncontrolled error", reason, SPELL_PYERROR_OTHER, true);
    }
    return exception;
}

//=============================================================================
// METHOD    : SPELLerror::throwSyntaxException
//=============================================================================
void SPELLerror::throwSyntaxException( const std::string& message, const std::string& reason )
{
	LOG_ERROR("SYNTAX EXCEPTION: " + message + ", " + reason);

	PyObject* syntaxException = NULL;
	PyObject* instance = NULL;
	syntaxException = SPELLpythonHelper::instance().getObject( "spell.lib.exception", "SyntaxException" );
    if (syntaxException == NULL)
    {
        PyErr_Print();
        throwRuntimeException("Cannot throw a SyntaxException", "Unable to access class");
        return;
    }
	instance = PyObject_CallFunction( syntaxException, NULL, NULL);
    if (instance == NULL)
    {
        PyErr_Print();
        throwRuntimeException("Cannot throw a SyntaxException", "Unable to instantiate");
        return;
    }
    setPythonError( instance, syntaxException, message, reason );
}

//=============================================================================
// METHOD    : SPELLerror::throwDriverException
//=============================================================================
void SPELLerror::setPythonError( PyObject* instance, PyObject* errClass, const std::string& message, const std::string& reason )
{
	SPELLpythonHelper::instance().acquireGIL();

    if (m_currentError != NULL)
    {
    	if ( m_ctrlExceptionSet != EXC_NONE )
    	{
    		LOG_WARN("Overriden by control exception");
    	}
    	else
    	{
    		LOG_WARN("Overriding current exception: " + m_currentError->what() );
    		clearErrors();
        	m_currentError = new SPELLcoreException(message, reason, SPELL_ERROR_LANGUAGE, false);
            PyObject* msg = SSTRPY(message);
            PyObject* rea = SSTRPY(reason);
            PyObject_SetAttrString( instance, "message", msg );
            PyObject_SetAttrString( instance, "reason", rea );
            PyErr_SetObject( errClass, instance );
    	}
    }
    else
    {
    	m_currentError = new SPELLcoreException(message, reason, SPELL_ERROR_LANGUAGE, false);
        PyObject* msg = SSTRPY(message);
        PyObject* rea = SSTRPY(reason);
        PyObject_SetAttrString( instance, "message", msg );
        PyObject_SetAttrString( instance, "reason", rea );
        PyErr_SetObject( errClass, instance );
    }
	//IMPORTANT!! DO NOT RELEASE GIL HERE, THIS TRICK WILL ALLOW
    //THE INTERPRETER TO PROCESS THE ERROR PROPERLY
}

//=============================================================================
// METHOD    : SPELLerror::throwDriverException
//=============================================================================
void SPELLerror::throwDriverException( const std::string& message, const std::string& reason )
{
	LOG_ERROR("DRIVER EXCEPTION: " + message + ", " + reason);

	PyObject* driverException = NULL;
	PyObject* instance = NULL;
	driverException = SPELLpythonHelper::instance().getObject( "spell.lib.exception", "DriverException" );
    if (driverException == NULL)
    {
        PyErr_Print();
        throwRuntimeException("Cannot throw a DriverException", "Unable to access class");
        return;
    }
	instance = PyObject_CallFunction( driverException, NULL, NULL);
    if (instance == NULL)
    {
        PyErr_Print();
        throwRuntimeException("Cannot throw a DriverException", "Unable to instantiate");
        return;
    }

    setPythonError( instance, driverException, message, reason );
}

//=============================================================================
// METHOD    : SPELLerror::throwRuntimeException
//=============================================================================
void SPELLerror::throwRuntimeException( const std::string& message, const std::string& reason )
{
	SPELLpythonHelper::instance().acquireGIL();

	std::string err = message + ": " + reason;
	PyErr_SetString( PyExc_RuntimeError, err.c_str() );

    if (m_currentError != NULL)
    {
    	LOG_WARN("(Throwing runtime exception) Overriding current exception: " + m_currentError->what() );
    	clearErrors();
    }
    m_currentError = new SPELLcoreException(message, reason, SPELL_ERROR_EXECUTION, true);
	//IMPORTANT!! DO NOT RELEASE GIL HERE, THIS TRICK WILL ALLOW
    //THE INTERPRETER TO PROCESS THE ERROR PROPERLY
}

//=============================================================================
// METHOD    : SPELLerror::getError
//=============================================================================
SPELLcoreException* SPELLerror::getError()
{
	getPythonError();
	return m_currentError;
}

//=============================================================================
// METHOD    : SPELLerror::inError
//=============================================================================
bool SPELLerror::inError()
{
	getPythonError();
	return (m_currentError != NULL);
}

//=============================================================================
// METHOD    : SPELLerror::inError
//=============================================================================
void SPELLerror::getPythonError()
{
	DEBUG("[ERR] Get python error");
	if (m_currentError != NULL) return;

	DEBUG("[ERR] No error tracked yet");

	PyObject* err = PyErr_Occurred();

	if (err != NULL)
	{
		LOG_ERROR("Some error occured");

		PyObject* ptype;
		PyObject* pvalue;
		PyObject* ptraceback;

		// Fetch the error information
		PyErr_Fetch( &ptype, &pvalue, &ptraceback );

		LOG_ERROR("Error is " + PYREPR(pvalue));

		m_currentError = errorToException(err,ptype,pvalue,ptraceback);

		std::string m_errorLocation = "";
		int line = -1;

	    // Parse and show the traceback if any available
	    PyTracebackObject* tb = (PyTracebackObject*) ptraceback;
	    while(tb != NULL)
	    {
	        std::string proc = PYSTR(tb->tb_frame->f_code->co_filename);
	        line = tb->tb_lineno;
	        std::string at = proc + ":" + ISTR(line);
			LOG_ERROR("    at " + at);
	        tb = tb->tb_next;
	        if (m_errorLocation != "") m_errorLocation += "\n";
	        m_errorLocation += at;
	    }
	}
}
