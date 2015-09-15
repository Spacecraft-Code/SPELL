// ################################################################################
// FILE       : SPELLlogPython.C
// DATE       : Mar 17, 2011
// PROJECT    : SPELL
// DESCRIPTION: Implementation of the logger bindings
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
#include "SPELL_UTIL/SPELLlog.H"
#include "SPELL_UTIL/SPELLpythonHelper.H"
#include "SPELL_UTIL/SPELLerror.H"
// Project includes --------------------------------------------------------
// System includes ---------------------------------------------------------
#include "structmember.h"



// GLOBALS /////////////////////////////////////////////////////////////////
static PyObject* KEY_SEVERITY = STRPY("severity");
static PyObject* KEY_LEVEL    = STRPY("level");

static std::map<std::string,LogSeverity> str_to_sev;
static std::map<std::string,LogLevel>    str_to_lev;



//============================================================================
// FUNCTION        : Log_Init
// DESCRIPTION    : Initialized of the LOG python object
//============================================================================
static int Log_Init( PyLogObject* self, PyObject* args, PyObject* kwds );
//============================================================================
// FUNCTION        : Log_Dealloc
// DESCRIPTION    : Cleanup of the LOG python object
//============================================================================
static void Log_Dealloc( PyLogObject* self );
//============================================================================
// FUNCTION        : Log_New
// DESCRIPTION    : Constructor of the LOG python object
//============================================================================
static PyObject* Log_New( PyTypeObject* type, PyObject* args, PyObject* kwds );
//============================================================================
// FUNCTION        : Log_Call
// DESCRIPTION    : Implements the __call__ method of the LOG object
//============================================================================
static PyObject* Log_Call( PyObject* self, PyObject* args, PyObject* kargs );



//============================================================================
// LOG object member specification
//============================================================================
static PyMemberDef Log_Members[] =
{
    {NULL} /* Sentinel */
};

//============================================================================
// LOG object method specification
//============================================================================
static PyMethodDef Log_Methods[] =
{
    {NULL} /* Sentinel */
};

//============================================================================
// Python representation of the SPELL executor object type
//============================================================================
static PyTypeObject Log_Type =
{
    PyObject_HEAD_INIT(NULL)
    0,                                 //ob_size
    "executor.LOG",                    //tp_name
    sizeof(PyLogObject),               //tp_basicsize
    0,                                 //tp_itemsize
    (destructor)Log_Dealloc,           //tp_dealloc
    0,                                 //tp_print
    0,                                 //tp_getattr
    0,                                 //tp_setattr
    0,                                 //tp_compare
    0,                                 //tp_repr
    0,                                 //tp_as_number
    0,                                 //tp_as_sequence
    0,                                 //tp_as_mapping
    0,                                 //tp_hash
    Log_Call,                          //tp_call
    0,                                 //tp_str
    0,                                 //tp_getattro
    0,                                 //tp_setattro
    0,                                 //tp_as_buffer
    Py_TPFLAGS_DEFAULT | Py_TPFLAGS_BASETYPE, //tp_flags
    "SPELL logger",                    // tp_doc
    0,                                 // tp_traverse
    0,                                 // tp_clear
    0,                                 // tp_richcompare
    0,                                 // tp_weaklistoffset
    0,                                 // tp_iter
    0,                                 // tp_iternext
    Log_Methods,                       // tp_methods
    Log_Members,                       // tp_members
    0,                                 // tp_getset
    0,                                 // tp_base
    0,                                 // tp_dict
    0,                                 // tp_descr_get
    0,                                 // tp_descr_set
    0,                                 // tp_dictoffset
    (initproc)Log_Init,                // tp_init
    0,                                 // tp_alloc
    Log_New,                           // tp_new
};


//============================================================================
// FUNCTION        : Log_Init
//============================================================================
static int Log_Init( PyLogObject* self, PyObject* args, PyObject* kwds )
{
    return 0;
}

//============================================================================
// FUNCTION        : Log_Dealloc
//============================================================================
static void Log_Dealloc( PyLogObject* self )
{
    self->ob_type->tp_free( (PyObject*) self );
}

//============================================================================
// FUNCTION        : Log_New
//============================================================================
static PyObject* Log_New( PyTypeObject* type, PyObject* args, PyObject* kwds )
{
    //NOTE:  Py_INCREF is already called!
    PyLogObject* self;
    self = (PyLogObject*) type->tp_alloc(type,0);
    return (PyObject*)self;
}

//============================================================================
// FUNCTION        : Log_Call
//============================================================================
static PyObject* Log_Call( PyObject* self, PyObject* args, PyObject* kargs )
{
    char* message     = (char*)"";
    LogSeverity sev   = LOG_INFO;
    LogLevel    level = LOG_PROC;

    int size = PyTuple_Size(args);
    if (size==1)
    {
        if (!PyArg_ParseTuple(args,"s", &message))
        {
            THROW_SYNTAX_EXCEPTION("Cannot log message", "Wrong arguments");
        }
        if (kargs != NULL)
        {
            if (PyDict_Contains( kargs, KEY_SEVERITY ))
            {
                std::string value = PYSTR(PyDict_GetItemString( kargs, "severity" ));
                sev = str_to_sev.find(value)->second;

            }
            if (PyDict_Contains( kargs, KEY_LEVEL ))
            {
                std::string value = PYSTR(PyDict_GetItemString( kargs, "level" ));
                level = str_to_lev.find(value)->second;
            }
        }
    }
    else if (size == 2)
    {
        char* psev = (char*) "";
        if (!PyArg_ParseTuple(args,"ss", &message, &psev))
        {
            THROW_SYNTAX_EXCEPTION("Cannot log message", "Wrong arguments");
        }
        if (kargs != NULL)
        {
            if (PyDict_Contains( kargs, KEY_LEVEL ))
            {
                std::string value = PYSTR(PyDict_GetItemString( kargs, "level" ));
                level = str_to_lev.find(value)->second;
            }
        }
        sev = str_to_sev.find(STR(psev))->second;
    }
    else if (size == 3)
    {
        char* psev = (char*) "";
        char* plev = (char*) "";
        if (!PyArg_ParseTuple(args,"sss", &message, &psev, &plev))
        {
            THROW_SYNTAX_EXCEPTION("Cannot log message", "Wrong arguments");
        }
        sev = str_to_sev.find(STR(psev))->second;
        level = str_to_lev.find(STR(plev))->second;
    }
    else
    {
        THROW_SYNTAX_EXCEPTION("Cannot log message", "Wrong arguments");
    }

    switch(sev)
    {
    case LOG_INFO:
    	LOG_INFO_PY(message);
    	break;
    case LOG_WARN:
    	LOG_WARN_PY(message);
    	break;
    case LOG_ERROR:
    	LOG_ERROR_PY(message);
    	break;
    }

    Py_RETURN_NONE;
}

//============================================================================
// FUNCTION        : Log_Install
//============================================================================
void Log_Install()
{
    if (PyType_Ready(&Log_Type) < 0 )
    {
        THROW_EXCEPTION("Cannot install LOG", "Unable to register object type", SPELL_ERROR_PYTHON_API);
    }
    PyObject* logobj = Log_New( &Log_Type, NULL, NULL );

    SPELLpythonHelper::instance().install(logobj, "LOG", "spell.utils.log");

    int count = 0;
    for( count = 0; count < LOG_LEV_MAX; count ++)
    {
        SPELLpythonHelper::instance().install(SSTRPY(LOG_LEVEL_STR[count]),
                                              LOG_LEVEL_LBL[count],
                                              "spell.utils.log");
        str_to_lev.insert( std::make_pair( LOG_LEVEL_STR[count], (LogLevel)count ));
    }

    for( count = 0; count < LOG_SEV_MAX; count ++)
    {
        SPELLpythonHelper::instance().install(SSTRPY(LOG_SEVERITY_STR[count]),
                                              LOG_SEVERITY_LBL[count],
                                              "spell.utils.log");
        str_to_sev.insert( std::make_pair( LOG_SEVERITY_STR[count], (LogSeverity)count ));
    }
}
