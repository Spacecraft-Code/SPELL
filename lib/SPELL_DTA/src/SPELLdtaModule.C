// ################################################################################
// FILE       : SPELLdtaModule.C
// DATE       : Mar 17, 2011
// PROJECT    : SPELL
// DESCRIPTION: Implementation of DTA module
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
// Project includes --------------------------------------------------------
// Local includes ----------------------------------------------------------
#include "SPELL_DTA/SPELLdtaContainerObject.H"
#include "SPELL_DTA/SPELLdtaVariableObject.H"
// System includes ---------------------------------------------------------
#include "object.h"

// FORWARD REFERENCES //////////////////////////////////////////////////////
// TYPES ///////////////////////////////////////////////////////////////////
// DEFINES /////////////////////////////////////////////////////////////////
// GLOBALS /////////////////////////////////////////////////////////////////

//============================================================================
// Object member specification
//============================================================================
static PyMemberDef SPELLdtaContainerObject_Members[] =
{
    {NULL} /* Sentinel */
};

//============================================================================
// Object method specification
//============================================================================
static PyMethodDef SPELLdtaContainerObject_Methods[] =
{
    {"set",      	SPELLdtaContainerObject_Set,          METH_VARARGS, "Set a value"},
    {"get",      	SPELLdtaContainerObject_Get,          METH_VARARGS, "Get a value"},
    {"has_key",   	SPELLdtaContainerObject_HasKey,       METH_VARARGS, "Check if the key exists"},
    {"keys",      	SPELLdtaContainerObject_Keys,         METH_NOARGS,  "Obtain the keys"},
    {"values",      SPELLdtaContainerObject_Keys,         METH_NOARGS,  "Obtain the values"},
    {"enableNotifications",    SPELLdtaContainerObject_EnableNotifications,  METH_NOARGS, "Enable value notifications"},
    {"disableNotifications",    SPELLdtaContainerObject_DisableNotifications,  METH_NOARGS, "Disable value notifications"},
    {NULL, NULL, 0, NULL} /* Sentinel */
};

//============================================================================
// Object member specification
//============================================================================
static PyMemberDef SPELLdtaVariableObject_Members[] =
{
    {NULL} /* Sentinel */
};

//============================================================================
// Object method specification
//============================================================================
static PyMethodDef SPELLdtaVariableObject_Methods[] =
{
    {NULL, NULL, 0, NULL} /* Sentinel */
};

//============================================================================
// Object method specification
//============================================================================
static PyMappingMethods SPELLdtaContainerObject_MappingMethods =
{
	(lenfunc)SPELLdtaContainerObject_DictLength, /*mp_length*/
	(binaryfunc)SPELLdtaContainerObject_DictSubscript, /*mp_subscript*/
	(objobjargproc)SPELLdtaContainerObject_DictAssSub, /*mp_ass_subscript*/
};

//============================================================================
// Python type specification
//============================================================================
static PyTypeObject SPELLdtaContainerObject_Type =
{
	PyObject_HEAD_INIT(NULL)					/* basic object head */
	0,               							/* var objects */
	"spell.lib.adapter.data.DataContainer",	    /* type name */
	sizeof(SPELLdtaContainerObject),    		/* type size */
	0,											/* item size */
	(destructor)SPELLdtaContainerObject_Dealloc,/* deallocator */
	0,											/* print function */
	(getattrfunc) SPELLdtaContainerObject_GetAttr, /* getattr */
	(setattrfunc) SPELLdtaContainerObject_SetAttr, /* setattr */
	0,											/* compare */
	SPELLdtaContainerObject_Repr,              	/* repr */
	0,											/* as number */
	0, 											/* as sequence */
	&SPELLdtaContainerObject_MappingMethods,	/* as mapping */
	0, 											/* hash */
	0,											/* call */
	SPELLdtaContainerObject_Str,	    		/* str */
	(getattrofunc) SPELLdtaContainerObject_GetAttrRO, /* getattr ro */
	(setattrofunc) SPELLdtaContainerObject_SetAttrRO, /* setattr ro */
	0, 											/* as buffer */
	Py_TPFLAGS_DEFAULT | Py_TPFLAGS_BASETYPE, 	/* flags */
	"Data container",    						/* documentation */
	SPELLdtaContainerObject_Traverse,	        /* traverse */
	SPELLdtaContainerObject_Clear, 				/* clear */
	0, 											/* rich compare */
	0, 											/* weak list offset */
	(getiterfunc) SPELLdtaContainerObject_Iter, /* iter */
	0,                           				/* iter next */
	SPELLdtaContainerObject_Methods,           	/* methods */
	SPELLdtaContainerObject_Members,           	/* members */
	0,											/* getset */
	0,											/* base */
	0,											/* dict */
	0,											/* descr get */
	0,											/* descr set */
	0,											/* dict offset */
	(initproc)SPELLdtaContainerObject_Init,    	/* init */
	0,											/* alloc */
	SPELLdtaContainerObject_New               	/* new */
//	0, /* tp_free */
//	0, /* tp_is_gc */
//	0, /* tp_bases */
//	0, /* tp_mro */
//	0, /* tp_cache */
//	0, /* tp_subclasses */
//	0, /* tp_weaklist */
//	0  /* tp_del */
};

//============================================================================
// Python type specification
//============================================================================
static PyTypeObject SPELLdtaVariableObject_Type =
{
	PyObject_HEAD_INIT(NULL)					/* basic object head */
	0,               							/* var objects */
	"spell.lib.adapter.data.Var",         	    /* type name */
	sizeof(SPELLdtaVariableObject),    				/* type size */
	0,											/* item size */
	(destructor)SPELLdtaVariableObject_Dealloc, 		/* deallocator */
	0,											/* print function */
	0,                                          /* getattr */
	0,                                          /* setattr */
	0,											/* compare */
	SPELLdtaVariableObject_Repr,            			/* repr */
	0,											/* as number */
	0, 											/* as sequence */
	0,                          	            /* as mapping */
	0, 											/* hash */
	0,                                          /* call */
	SPELLdtaVariableObject_Str,	    				/* str */
	0,                                          /* getattr ro */
	0,                                          /* setattr ro */
	0, 											/* as buffer */
	Py_TPFLAGS_DEFAULT | Py_TPFLAGS_BASETYPE, 	/* flags */
	"Value container",    						/* documentation */
	0,											/* traverse */
	0, 											/* clear */
	0, 											/* rich compare */
	0, 											/* weak list offset */
	0, 											/* iter */
	0,                           				/* iter next */
	SPELLdtaVariableObject_Methods,           		/* methods */
	SPELLdtaVariableObject_Members,           		/* members */
	0,											/* getset */
	0,											/* base */
	0,											/* dict */
	0,											/* descr get */
	0,											/* descr set */
	0,											/* dict offset */
	(initproc)SPELLdtaVariableObject_Init,  			/* init */
	0,											/* alloc */
	SPELLdtaVariableObject_New               	    /* new */
//	0, /* tp_free */
//	0, /* tp_is_gc */
//	0, /* tp_bases */
//	0, /* tp_mro */
//	0, /* tp_cache */
//	0, /* tp_subclasses */
//	0, /* tp_weaklist */
//	0  /* tp_del */
};

//////////////////////////////////////////////////////////////////////////////
// PYTHON MODULE INITIALIZATION
//////////////////////////////////////////////////////////////////////////////
PyMODINIT_FUNC
initlibSPELL_DTA(void)
{
    // Will hold the Python module
    PyObject* module;

    // Allocate the new type for SPELL data containers
	SPELLdtaContainerObject_Type.tp_new = PyType_GenericNew;
	if (PyType_Ready(&SPELLdtaContainerObject_Type) < 0)
	{
		std::string err = "Unable to load type " + std::string(SPELLdtaContainerObject_Type.tp_name);
		PyErr_SetString( PyExc_RuntimeError, err.c_str());
		std::cerr << "ERROR: " << err << std::endl;
		return;
	}

    // Allocate the new type for SPELL variable containers
	SPELLdtaVariableObject_Type.tp_new = PyType_GenericNew;
	if (PyType_Ready(&SPELLdtaVariableObject_Type) < 0)
	{
		std::string err = "Unable to load type " + std::string(SPELLdtaVariableObject_Type.tp_name);
		PyErr_SetString( PyExc_RuntimeError, err.c_str());
		std::cerr << "ERROR: " << err << std::endl;
		return;
	}

    // Initialize the module
    module = Py_InitModule3("libSPELL_DTA", NULL, "Module for SPELL data containers");

    Py_INCREF(&SPELLdtaContainerObject_Type);
	PyModule_AddObject(module, "DataContainer", (PyObject *)&SPELLdtaContainerObject_Type);

    Py_INCREF(&SPELLdtaVariableObject_Type);
	PyModule_AddObject(module, "Var", (PyObject *)&SPELLdtaVariableObject_Type);
}
