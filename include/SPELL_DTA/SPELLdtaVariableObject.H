// ################################################################################
// FILE       : SPELLdtaVariableObject.H
// DATE       : Mar 17, 2011
// PROJECT    : SPELL
// DESCRIPTION: SPELL variable
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

#ifndef __SPELL_DTA_VAR_OBJECT_H__
#define __SPELL_DTA_VAR_OBJECT_H__

// FILES TO INCLUDE ////////////////////////////////////////////////////////
// Project includes --------------------------------------------------------
#include "SPELL_UTIL/SPELLbase.H"
// Local includes ----------------------------------------------------------
#include "SPELL_DTA/SPELLdtaVariable.H"
// System includes ---------------------------------------------------------
#include "structmember.h"

// FORWARD REFERENCES //////////////////////////////////////////////////////
// TYPES ///////////////////////////////////////////////////////////////////
// DEFINES /////////////////////////////////////////////////////////////////
// GLOBALS /////////////////////////////////////////////////////////////////

/** \addtogroup SPELL_DTA  */
/*@{*/

//============================================================================
/** Python representation of the SPELL database */
typedef struct SPELLdtaVariableObject_
{
    PyObject_HEAD;
    SPELLdtaVariable* var;
}
SPELLdtaVariableObject;

//============================================================================
// FUNCTION       : SPELLdtaVariableObject_Init
// DESCRIPTION    : Initialized of the SPELL database Python object
//============================================================================
int SPELLdtaVariableObject_Init( SPELLdtaVariableObject* self, PyObject* args, PyObject* kwds );
//============================================================================
// FUNCTION       : SPELLdtaVariableObject_Dealloc
// DESCRIPTION    : Cleanup of the SPELL database Python object
//============================================================================
void SPELLdtaVariableObject_Dealloc( SPELLdtaVariableObject* self );
//============================================================================
// FUNCTION       : SPELLdtaVariableObject_New
// DESCRIPTION    : Constructor of the SPELL database Python object
//============================================================================
PyObject* SPELLdtaVariableObject_New( PyTypeObject* type, PyObject* args, PyObject* kwds );
//============================================================================
// FUNCTION       : SPELLdtaVariableObject_Repr
// DESCRIPTION    : Repr method
//============================================================================
PyObject* SPELLdtaVariableObject_Repr( PyObject* self );
//============================================================================
// FUNCTION       : SPELLdtaVariableObject_Str
// DESCRIPTION    : Str method
//============================================================================
PyObject* SPELLdtaVariableObject_Str( PyObject* self );
/*@}*/
#endif
