// ################################################################################
// FILE       : SPELLpyDatabaseObject.H
// DATE       : Mar 17, 2011
// PROJECT    : SPELL
// DESCRIPTION: SPELL database Python extension
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

#ifndef __SPELL_PY_DATABASE_H__
#define __SPELL_PY_DATABASE_H__

// FILES TO INCLUDE ////////////////////////////////////////////////////////
// Project includes --------------------------------------------------------
#include "SPELL_UTIL/SPELLbase.H"
// Local includes ----------------------------------------------------------
#include "SPELL_SDB/SPELLdatabase.H"
// System includes ---------------------------------------------------------
#include "structmember.h"

// FORWARD REFERENCES //////////////////////////////////////////////////////
// TYPES ///////////////////////////////////////////////////////////////////
// DEFINES /////////////////////////////////////////////////////////////////
// GLOBALS /////////////////////////////////////////////////////////////////

/** \defgroup SPELL_SDB SPELL databases (SPELL_SDB)
 *
 * This module defines the Python objects used for creating SPELL databases.
 *
 * It also defines the AsRUN file manager and the format of data notifications.
 *
 * */
/*@{*/

//============================================================================
/** Python representation of the SPELL database */
typedef struct PyDatabaseObject_
{
    PyObject_HEAD;
    SPELLdatabase* __pdb;
    std::string    __name;
    std::string    __filename;
    std::string    __defaultEx;
}
PyDatabaseObject;

typedef struct
{
        PyObject_HEAD
        PyDictObject*  di_dict; /* Set to NULL when iterator is exhausted */
        Py_ssize_t     di_used;
        Py_ssize_t     di_pos;
        PyObject*      di_result; /* reusable result tuple for iteritems */
        Py_ssize_t     len;
}
PyDatabaseIteratorObject;


//============================================================================
// FUNCTION       : SPELLpyDatabase_Init
// DESCRIPTION    : Initialized of the SPELL database Python object
//============================================================================
int SPELLpyDatabase_Init( PyDatabaseObject* self, PyObject* args, PyObject* kwds );
//============================================================================
// FUNCTION       : SPELLpyDatabase_Dealloc
// DESCRIPTION    : Cleanup of the SPELL database Python object
//============================================================================
void SPELLpyDatabase_Dealloc( PyDatabaseObject* self );
//============================================================================
// FUNCTION       : SPELLpyDatabase_New
// DESCRIPTION    : Constructor of the SPELL database Python object
//============================================================================
PyObject* SPELLpyDatabase_New( PyTypeObject* type, PyObject* args, PyObject* kwds );
//============================================================================
// FUNCTION       : SPELLpyDatabase_Create
// DESCRIPTION    : Create method
//============================================================================
PyObject* SPELLpyDatabase_Create( PyObject* self, PyObject* args );
//============================================================================
// FUNCTION       : SPELLpyDatabase_Load
// DESCRIPTION    : Load method
//============================================================================
PyObject* SPELLpyDatabase_Load( PyObject* self, PyObject* args );
//============================================================================
// FUNCTION       : SPELLpyDatabase_Reload
// DESCRIPTION    : Reload method
//============================================================================
PyObject* SPELLpyDatabase_Reload( PyObject* self, PyObject* args );
//============================================================================
// FUNCTION       : SPELLpyDatabase_Id
// DESCRIPTION    : Id method
//============================================================================
PyObject* SPELLpyDatabase_Id( PyObject* self, PyObject* args );
//============================================================================
// FUNCTION       : SPELLpyDatabase_Commit
// DESCRIPTION    : Commit method
//============================================================================
PyObject* SPELLpyDatabase_Commit( PyObject* self, PyObject* args );
//============================================================================
// FUNCTION       : SPELLpyDatabase_Set
// DESCRIPTION    : Set method
//============================================================================
PyObject* SPELLpyDatabase_Set( PyObject* self, PyObject* args );
//============================================================================
// FUNCTION       : SPELLpyDatabase_Get
// DESCRIPTION    : Get method
//============================================================================
PyObject* SPELLpyDatabase_Get( PyObject* self, PyObject* args );
//============================================================================
// FUNCTION       : SPELLpyDatabase_Keys
// DESCRIPTION    : Keys method
//============================================================================
PyObject* SPELLpyDatabase_Keys( PyObject* self, PyObject* args );
//============================================================================
// FUNCTION       : SPELLpyDatabase_Repr
// DESCRIPTION    : Repr method
//============================================================================
PyObject* SPELLpyDatabase_Repr( PyObject* self );
//============================================================================
// FUNCTION       : SPELLpyDatabase_Str
// DESCRIPTION    : Str method
//============================================================================
PyObject* SPELLpyDatabase_Str( PyObject* self );
//============================================================================
// FUNCTION       : SPELLpyDatabase_DictLength
// DESCRIPTION    : Length method
//============================================================================
Py_ssize_t SPELLpyDatabase_DictLength( PyObject* self );
//============================================================================
// FUNCTION       : SPELLpyDatabase_DictSubscript
// DESCRIPTION    : Subscript method
//============================================================================
PyObject* SPELLpyDatabase_DictSubscript( PyObject* self, PyObject* key );
//============================================================================
// FUNCTION       : SPELLpyDatabase_DictAssSub
// DESCRIPTION    : Subscript method
//============================================================================
int SPELLpyDatabase_DictAssSub( PyObject* dict, PyObject* v, PyObject* w );
//============================================================================
// FUNCTION       : SPELLpyDatabase_HasKey
// DESCRIPTION    : Check key existence
//============================================================================
PyObject* SPELLpyDatabase_HasKey( PyObject* self, PyObject* args );
//============================================================================
// FUNCTION       : SPELLpyDatabase_GetAttrRO
// DESCRIPTION    : Get attribute
//============================================================================
PyObject * SPELLpyDatabase_GetAttrRO(PyObject*, PyObject *);
//============================================================================
// FUNCTION       : SPELLpyDatabase_SetAttrRO
// DESCRIPTION    : Set attribute
//============================================================================
int SPELLpyDatabase_SetAttrRO(PyObject*, PyObject*, PyObject* );
//============================================================================
// FUNCTION       : SPELLpyDatabase_GetAttr
// DESCRIPTION    : Get attribute
//============================================================================
PyObject * SPELLpyDatabase_GetAttr(PyObject*, char* );
//============================================================================
// FUNCTION       : SPELLpyDatabase_SetAttr
// DESCRIPTION    : Set attribute
//============================================================================
int SPELLpyDatabase_SetAttr(PyObject*, char*, PyObject* );
//============================================================================
// FUNCTION       : SPELLpyDatabase_Traverse
// DESCRIPTION    : Traverse container
//============================================================================
int SPELLpyDatabase_Traverse(PyObject*, visitproc visit, void* arg);
//============================================================================
// FUNCTION       : SPELLpyDatabase_Clear
// DESCRIPTION    : Clear container
//============================================================================
int SPELLpyDatabase_Clear(PyObject*);
//============================================================================
// FUNCTION       : SPELLpyDatabase_Iter
// DESCRIPTION    : Get iterator
//============================================================================
PyObject* SPELLpyDatabase_Iter(PyDatabaseObject* container);
//============================================================================
// FUNCTION       : SPELLpyDatabase_NewIterator
// DESCRIPTION    : Create iterator
//============================================================================
PyObject* SPELLpyDatabase_NewIterator( PyDatabaseObject* container );
//============================================================================
// FUNCTION       : SPELLpyDatabase_DeallocIterator
// DESCRIPTION    : Destroy iterator
//============================================================================
void SPELLpyDatabase_DeallocIterator( PyDatabaseIteratorObject* iterator );
//============================================================================
// FUNCTION       : SPELLpyDatabase_NextKey
// DESCRIPTION    : Get next iterator key
//============================================================================
PyObject* SPELLpyDatabase_NextKey( PyDatabaseIteratorObject* iterator );
//============================================================================
// FUNCTION       : SPELLpyDatabase_IterLen
// DESCRIPTION    : Get iterator size
//============================================================================
PyObject* SPELLpyDatabase_IterLen( PyDatabaseIteratorObject* iterator );

/** Convenience macro for defining a Python object type for databases */
#define DATABASE_DEFINITION(__type__,__name__,__doc__)  \
		static PyTypeObject __type__ =                  \
		{                                               \
		    PyObject_HEAD_INIT(NULL)					/* basic object head */ \
		    0,               							/* var objects */ 		\
		    __name__,									/* type name */  		\
		    sizeof(PyDatabaseObject),    				/* type size */ 		\
		    0,											/* item size */         \
		    (destructor)SPELLpyDatabase_Dealloc, 		/* deallocator */       \
		    0,											/* print function */    \
		    (getattrfunc) SPELLpyDatabase_GetAttr,      /* getattr */           \
			(setattrfunc) SPELLpyDatabase_SetAttr,      /* setattr */           \
		    0,											/* compare */           \
            SPELLpyDatabase_Repr,              			/* repr */              \
		    0,											/* as number */         \
		    0, 											/* as sequence */       \
		    &SPELLpyDatabase_MappingMethods,	        /* as mapping */        \
		    0, 											/* hash */              \
		    0,											/* call */              \
		    SPELLpyDatabase_Str,						/* str */               \
		    (getattrofunc) SPELLpyDatabase_GetAttrRO,	/* getattr ro */        \
		    (setattrofunc) SPELLpyDatabase_SetAttrRO,	/* setattr ro */        \
		    0, 											/* as buffer */         \
		    Py_TPFLAGS_DEFAULT | Py_TPFLAGS_BASETYPE, 	/* flags */             \
		    __doc__, 									/* documentation */     \
     		SPELLpyDatabase_Traverse,	        		/* traverse */          \
	     	SPELLpyDatabase_Clear,  					/* clear */             \
		    0, 											/* rich compare */      \
		    0, 											/* weak list offset */  \
		    (getiterfunc) SPELLpyDatabase_Iter,         /* iter */              \
		    0,                           				/* iter next */         \
		    SPELLpyDatabase_Methods,           			/* methods */           \
		    SPELLpyDatabase_Members,           			/* members */           \
		    0,											/* getset */            \
		    0,											/* base */              \
		    0,											/* dict */              \
		    0,											/* descr get */         \
		    0,											/* descr set */         \
		    0,											/* dict offset */       \
		    (initproc)SPELLpyDatabase_Init,    			/* init */              \
		    0,											/* alloc */             \
		    SPELLpyDatabase_New,               		    /* new */				\
		};
	    /*
	     * tp_free,
	     * tp_is_gc,
	     * tp_bases,
	     * tp_mro,
	     * tp_cache
	     * tp_subclasses,
	     * tp_weaklist,
	     * tp_del
	     * */

/** Convenience macro to allocate the new type for SPELL basic database objects */
#define DATABASE_TYPE_READY(__type__) \
		__type__.tp_new = PyType_GenericNew;			\
		if (PyType_Ready(&__type__) < 0)				\
		{												\
			std::string err = "Unable to load type ";	\
			err += std::string(__type__.tp_name);		\
			PyErr_SetString( PyExc_RuntimeError, err.c_str());	\
			std::cerr << "ERROR: " << err << std::endl; \
			return;                                     \
		}

/** Convenience macro to load a new type into the given module */
#define DATABASE_TYPE_LOAD(__module__,__type__,__name__)        \
	    Py_INCREF(&__type__);									\
		PyModule_AddObject(__module__, __name__, (PyObject *)&__type__);

/*@}*/
#endif
