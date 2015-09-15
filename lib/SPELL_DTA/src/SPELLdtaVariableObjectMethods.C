// ################################################################################
// FILE       : SPELLdtaVariableObjectMethods.C
// DATE       : Mar 17, 2011
// PROJECT    : SPELL
// DESCRIPTION: Implementation of variable container
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
#include "SPELL_UTIL/SPELLpythonHelper.H"
#include "SPELL_WRP/SPELLconstants.H"
// Local includes ----------------------------------------------------------
#include "SPELL_DTA/SPELLdtaVariableObject.H"
// System includes ---------------------------------------------------------

// FORWARD REFERENCES //////////////////////////////////////////////////////
// TYPES ///////////////////////////////////////////////////////////////////
// DEFINES /////////////////////////////////////////////////////////////////
// GLOBALS /////////////////////////////////////////////////////////////////


//============================================================================
// FUNCTION        : SPELLdtaVariableObject_Init
//============================================================================
int SPELLdtaVariableObject_Init( SPELLdtaVariableObject* self, PyObject* args, PyObject* kwds )
{
	if(PyTuple_Size(args) != 0)
	{
		THROW_SYNTAX_EXCEPTION_NR("Cannot create variable container", "Expected no posicional arguments");
		return -1;
	}

	SPELLdtaVariableObject* dself = reinterpret_cast<SPELLdtaVariableObject*>(self);

	try
	{
		dself->var = new SPELLdtaVariable(kwds);
	}
	catch(SPELLcoreException& ex)
	{
		THROW_SYNTAX_EXCEPTION_NR(ex.getError(), ex.getReason());
		return -1;
	}

    return 0;
}

//============================================================================
// FUNCTION        : SPELLdtaVariableObject_Dealloc
//============================================================================
void SPELLdtaVariableObject_Dealloc( SPELLdtaVariableObject* self )
{
	delete self->var;
    self->ob_type->tp_free( (PyObject*) self );
}

//============================================================================
// FUNCTION        : SPELLdtaVariableObject_New
//============================================================================
PyObject* SPELLdtaVariableObject_New( PyTypeObject* type, PyObject* args, PyObject* kwds )
{
    //NOTE:  Py_INCREF is already called!
    SPELLdtaVariableObject* self;
    self = (SPELLdtaVariableObject*) type->tp_alloc(type,0);
    return (PyObject*)self;
}

//============================================================================
// FUNCTION       : SPELLdtaVariableObject_Repr
//============================================================================
PyObject* SPELLdtaVariableObject_Repr( PyObject* self )
{
	SPELLdtaVariableObject* dself = reinterpret_cast<SPELLdtaVariableObject*>(self);
	std::string str = dself->var->repr();
	return SSTRPY(str);
}

//============================================================================
// FUNCTION       : SPELLdtaVariableObject_Str
//============================================================================
PyObject* SPELLdtaVariableObject_Str( PyObject* self )
{
	SPELLdtaVariableObject* dself = reinterpret_cast<SPELLdtaVariableObject*>(self);
	std::string str = dself->var->str();
	return SSTRPY(str);
}

