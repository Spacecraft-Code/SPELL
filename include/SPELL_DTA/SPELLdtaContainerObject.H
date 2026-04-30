// ################################################################################
// FILE       : SPELLdtaContainerObject.H
// DATE       : Mar 17, 2011
// PROJECT    : SPELL
// DESCRIPTION: SPELL data dictionary
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

#ifndef __SPELL_DTA_CONTAINER_OBJECT_H__
#define __SPELL_DTA_CONTAINER_OBJECT_H__

// FILES TO INCLUDE ////////////////////////////////////////////////////////
// Project includes --------------------------------------------------------
#include "SPELL_UTIL/SPELLbase.H"
// Local includes ----------------------------------------------------------
#include "SPELL_DTA/SPELLdtaContainer.H"
// System includes ---------------------------------------------------------
#include "structmember.h"

// FORWARD REFERENCES //////////////////////////////////////////////////////
// TYPES ///////////////////////////////////////////////////////////////////
// DEFINES /////////////////////////////////////////////////////////////////
// GLOBALS /////////////////////////////////////////////////////////////////

/** \defgroup SPELL_DTA SPELL data (SPELL_DTA)
 *
 * This module defines the Python objects used for special data containers.
 *
 *
 * */
/*@{*/

//============================================================================
/** Python representation of the SPELL database */
typedef struct SPELLdtaContainerObject_
{
    PyObject_HEAD;
    SPELLdtaContainer* container;
}
SPELLdtaContainerObject;

typedef struct
{
        PyObject_HEAD
        PyDictObject*  di_dict; /* Set to NULL when iterator is exhausted */
        Py_ssize_t di_used;
        Py_ssize_t di_pos;
        PyObject*  di_result; /* reusable result tuple for iteritems */
        Py_ssize_t len;
}
SPELLdtaContainerIteratorObject;

//============================================================================
// FUNCTION       : SPELLdtaContainerObject_Init
// DESCRIPTION    : Initialized of the SPELL database Python object
//============================================================================
int SPELLdtaContainerObject_Init( SPELLdtaContainerObject* self, PyObject* args, PyObject* kwds );
//============================================================================
// FUNCTION       : SPELLdtaContainerObject_Dealloc
// DESCRIPTION    : Cleanup of the SPELL database Python object
//============================================================================
void SPELLdtaContainerObject_Dealloc( SPELLdtaContainerObject* self );
//============================================================================
// FUNCTION       : SPELLdtaContainerObject_New
// DESCRIPTION    : Constructor of the SPELL database Python object
//============================================================================
PyObject* SPELLdtaContainerObject_New( PyTypeObject* type, PyObject* args, PyObject* kwds );
//============================================================================
// FUNCTION       : SPELLdtaContainerObject_Repr
// DESCRIPTION    : Repr method
//============================================================================
PyObject* SPELLdtaContainerObject_Repr( PyObject* self );
//============================================================================
// FUNCTION       : SPELLdtaContainerObject_Str
// DESCRIPTION    : Str method
//============================================================================
PyObject* SPELLdtaContainerObject_Str( PyObject* self );
//============================================================================
// FUNCTION       : SPELLdtaContainerObject_Set
// DESCRIPTION    : Set method
//============================================================================
PyObject* SPELLdtaContainerObject_Set( PyObject* self, PyObject* args );
//============================================================================
// FUNCTION       : SPELLdtaContainerObject_Get
// DESCRIPTION    : Get method
//============================================================================
PyObject* SPELLdtaContainerObject_Get( PyObject* self, PyObject* args );
//============================================================================
// FUNCTION       : SPELLdtaContainerObject_Keys
// DESCRIPTION    : Keys method
//============================================================================
PyObject* SPELLdtaContainerObject_Keys( PyObject* self, PyObject* args );
//============================================================================
// FUNCTION       : SPELLdtaContainerObject_Values
// DESCRIPTION    : Values method
//============================================================================
PyObject* SPELLdtaContainerObject_Values( PyObject* self, PyObject* args );
//============================================================================
// FUNCTION       : SPELLdtaContainerObject_DictLength
// DESCRIPTION    : Length method
//============================================================================
Py_ssize_t SPELLdtaContainerObject_DictLength( PyObject* self );
//============================================================================
// FUNCTION       : SPELLdtaContainerObject_DictSubscript
// DESCRIPTION    : Subscript method
//============================================================================
PyObject* SPELLdtaContainerObject_DictSubscript( PyObject* self, PyObject* key );
//============================================================================
// FUNCTION       : SPELLdtaContainerObject_DictAssSub
// DESCRIPTION    : Subscript method
//============================================================================
int SPELLdtaContainerObject_DictAssSub( PyObject* dict, PyObject* v, PyObject* w );
//============================================================================
// FUNCTION       : SPELLdtaContainerObject_HasKey
// DESCRIPTION    : Check key existence
//============================================================================
PyObject* SPELLdtaContainerObject_HasKey( PyObject* self, PyObject* args );
//============================================================================
// FUNCTION       : SPELLdtaContainerObject_GetAttrRO
// DESCRIPTION    : Get attribute
//============================================================================
PyObject * SPELLdtaContainerObject_GetAttrRO(PyObject*, PyObject *);
//============================================================================
// FUNCTION       : SPELLdtaContainerObject_SetAttrRO
// DESCRIPTION    : Set attribute
//============================================================================
int SPELLdtaContainerObject_SetAttrRO(PyObject*, PyObject*, PyObject* );
//============================================================================
// FUNCTION       : SPELLdtaContainerObject_GetAttr
// DESCRIPTION    : Get attribute
//============================================================================
PyObject * SPELLdtaContainerObject_GetAttr(PyObject*, char* );
//============================================================================
// FUNCTION       : SPELLdtaContainerObject_SetAttr
// DESCRIPTION    : Set attribute
//============================================================================
int SPELLdtaContainerObject_SetAttr(PyObject*, char*, PyObject* );
//============================================================================
// FUNCTION       : SPELLdtaContainerObject_Traverse
// DESCRIPTION    : Traverse container
//============================================================================
int SPELLdtaContainerObject_Traverse(PyObject*, visitproc visit, void* arg);
//============================================================================
// FUNCTION       : SPELLdtaContainerObject_Clear
// DESCRIPTION    : Clear container
//============================================================================
int SPELLdtaContainerObject_Clear(PyObject*);
//============================================================================
// FUNCTION       : SPELLdtaContainerObject_Iter
// DESCRIPTION    : Get iterator
//============================================================================
PyObject* SPELLdtaContainerObject_Iter(SPELLdtaContainerObject* container);
//============================================================================
// FUNCTION       : SPELLdtaContainerObject_NewIterator
// DESCRIPTION    : Create iterator
//============================================================================
PyObject* SPELLdtaContainerObject_NewIterator( SPELLdtaContainerObject* container );
//============================================================================
// FUNCTION       : SPELLdtaContainerObject_DeallocIterator
// DESCRIPTION    : Destroy iterator
//============================================================================
void SPELLdtaContainerObject_DeallocIterator( SPELLdtaContainerIteratorObject* container );
//============================================================================
// FUNCTION       : SPELLdtaContainerObject_NextKey
// DESCRIPTION    : Get next iterator key
//============================================================================
PyObject* SPELLdtaContainerObject_NextKey( SPELLdtaContainerIteratorObject* di );
//============================================================================
// FUNCTION       : SPELLdtaContainerObject_IterLen
// DESCRIPTION    : Get iterator size
//============================================================================
PyObject* SPELLdtaContainerObject_IterLen( SPELLdtaContainerIteratorObject* di );
//============================================================================
// FUNCTION       : SPELLdtaContainerObject_EnableNotifications
// DESCRIPTION    : Enable notifications
//============================================================================
PyObject* SPELLdtaContainerObject_EnableNotifications( PyObject* self, PyObject* args );
//============================================================================
// FUNCTION       : SPELLdtaContainerObject_DisableNotifications
// DESCRIPTION    : Disable notifications
//============================================================================
PyObject* SPELLdtaContainerObject_DisableNotifications( PyObject* self, PyObject* args );

/*@}*/
#endif
