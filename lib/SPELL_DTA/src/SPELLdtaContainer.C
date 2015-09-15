// ################################################################################
// FILE       : SPELLdtaContainer.C
// DATE       : Mar 17, 2011
// PROJECT    : SPELL
// DESCRIPTION: Data variable container implementation
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
#include "SPELL_WRP/SPELLconstants.H"
#include "SPELL_UTIL/SPELLutils.H"
#include "SPELL_UTIL/SPELLerror.H"
#include "SPELL_UTIL/SPELLpythonHelper.H"
#include "SPELL_UTIL/SPELLpythonMonitors.H"
#include "SPELL_WRP/SPELLpyHandle.H"
// Local includes ----------------------------------------------------------
#include "SPELL_DTA/SPELLdtaContainer.H"
#include "SPELL_DTA/SPELLdtaVariableObject.H"
#include "SPELL_DTA/SPELLdtaVariable.H"
// System includes ---------------------------------------------------------

// TYPES ///////////////////////////////////////////////////////////////////
// DEFINES /////////////////////////////////////////////////////////////////
// GLOBALS /////////////////////////////////////////////////////////////////

bool SPELLdtaContainer::s_notificationsEnabled = true;

//============================================================================
// CONSTRUCTOR: SPELLdtaContainer::SPELLdtaContainer
//============================================================================
SPELLdtaContainer::SPELLdtaContainer( const std::string& name )
{
	DEBUG("[DTAC] Container create");
	m_name = name;
	m_dict = PyDict_New();
	m_notificationsEnabled = true;
	Py_XINCREF(m_dict);
	DEBUG("[DTAC] Container created");
}

//============================================================================
// DESTRUCTOR: SPELLdtaContainer::SPELLdtaContainer
//============================================================================
SPELLdtaContainer::~SPELLdtaContainer()
{

}

//============================================================================
// DESTRUCTOR: SPELLdtaContainer::incref
//============================================================================
void SPELLdtaContainer::incref()
{
	DEBUG("[DTAC] INCREF BEGIN (" + ISTR(m_dict->ob_refcnt) + ")");
	Py_INCREF(m_dict);
	SPELLpyHandle keyList = PyDict_Keys(m_dict);
	int numKeys = PyList_Size(keyList.get());
	std::string refc = "(";
	for(int idx = 0; idx < numKeys; idx++)
	{
		PyObject* obj = PyDict_GetItem( m_dict, PyList_GetItem(keyList.get(), idx) );
		Py_INCREF(obj);
		if (refc != "(") refc += ",";
		refc += ISTR(obj->ob_refcnt);
	}
	refc += ")";
	DEBUG("[DTAC] " + refc);
	DEBUG("[DTAC] INCREF END (" + ISTR(m_dict->ob_refcnt) + ")");
}

//============================================================================
// METHOD: SPELLdtaContainer::setValue
//============================================================================
void SPELLdtaContainer::setValue( PyObject* key, PyObject* value )
{
	DEBUG("[DTAC] Set value");
	SPELLsafePythonOperations ops("DTA");

	// If there is no such a key: first check if the given value is a Var.
	// if so, just assign it to the dict. If the given value is not a Var,
	// create one and assign it.

	// If there is a key, assign the value to the Var. This may raise a core
	// exception if the value is not consistent with the Var definition.
	// Note that if the given value is also a var, the value gets directly substituted.

	if (PyDict_Contains(m_dict, key))
	{
		// Borrowed reference
		PyObject* obj = PyDict_GetItem(m_dict, key);
		// Make sure it keeps existing
		Py_INCREF(obj);

		PyTypeObject* type = (PyTypeObject*) PyObject_Type(value);
		Py_XINCREF(type);
		std::string varType = "spell.lib.adapter.data.Var";

		// If the passed value is a Var instance, just substitute
		if (varType == type->tp_name )
		{
			DEBUG("[DTAC] Substitute pre-existing variable");
			// Reference count of key and value need to be increased
			// they are borrowed from the arguments tuple
			// see SPELLdtaContainerObjectMethods.c
			// BUT, PyDict_SetItem does this by itself.
			PyDict_SetItem(m_dict, key, value);
			DEBUG("[DTAC] Substitute done");
		}
		else
		{
			DEBUG("[DTAC] Not a variable, assign the value to the existing one");
			SPELLdtaVariableObject* varObj = reinterpret_cast<SPELLdtaVariableObject*>(obj);
			try
			{
				// May throw exception if value is not consistent with definition
				// Incref is done inside setValue
				varObj->var->setValue(value);
			}
			catch(SPELLcoreException& ex)
			{
				// If setting the initially given value failed, enter a loop to
				// ask the user to correct it
				std::string message = ex.what();
				bool valueOk = false;
				do
				{
					valueOk = varObj->var->promptForValue( message );
				}
				while(valueOk == false);
			}

			DEBUG("[DTAC] Set value done: " + PYREPR(value) + "-->" + PYREPR(varObj->var->getValueEx()) );
		}

		//Py_DECREF(obj);
	}
	else
	{
		DEBUG("[DTAC] Assign a new variable");

		// Register the variable creation order
		m_varOrder.push_back(PYSSTR(key));

		// New reference
		PyTypeObject* typeObj = (PyTypeObject*) PyObject_Type(value);
		Py_XINCREF(typeObj);
		std::string typeStr = "spell.lib.adapter.data.Var";
		if (typeStr == typeObj->tp_name)
		{
			DEBUG("[DTAC] Given variable was already a variable");
			// Reference count of key and value need to be increased
			// they are borrowed from the arguments tuple
			// see SPELLdtaContainerObjectMethods.c
			// BUT, PyDict_SetItem does this by itself.
			PyDict_SetItem(m_dict,key,value);
			SPELLdtaVariableObject* varObj = reinterpret_cast<SPELLdtaVariableObject*>(value);
			varObj->var->setContainer(this);
			varObj->var->setName(PYSSTR(key));
			DEBUG("[DTAC] Set value done: " + PYREPR(value) + "-->" + PYREPR(varObj->var->getValueEx()) );
		}
		else
		{
			DEBUG("[DTAC] Creating a new variable to contain the value");

			PyObject* varClass = SPELLpythonHelper::instance().getObject("libSPELL_DTA", "Var");
			Py_INCREF(varClass);
			assert(varClass != NULL);
			PyObject* dict = PyDict_New();
			Py_XINCREF(dict);
			// Reference count of key and value need to be increased
			// they are borrowed from the arguments tuple
			// see SPELLdtaContainerObjectMethods.c
			// BUT, PyDict_SetItem does this by itself.
			PyDict_SetItemString( dict, LanguageModifiers::Default.c_str(), value );
			PyObject* instance = SPELLpythonHelper::instance().newInstance(varClass, NULL, dict);
			Py_XINCREF(instance);
			assert(instance != NULL);
			// Reference count of key and value need to be increased
			// they are borrowed from the arguments tuple
			// see SPELLdtaContainerObjectMethods.c
			// BUT, PyDict_SetItem does this by itself.
			PyDict_SetItem(m_dict, key, instance);
			SPELLdtaVariableObject* varObj = reinterpret_cast<SPELLdtaVariableObject*>(instance);
			varObj->var->setContainer(this);
			varObj->var->setName(PYSSTR(key));
			DEBUG("[DTAC] Set value done: " + PYREPR(value) + "-->" + PYREPR(varObj->var->getValueEx()) );
		}
		//Py_DECREF(typeObj);
	}
	SPELLpythonHelper::instance().checkError();
}

//============================================================================
// METHOD: SPELLdtaContainer::getValueEx
//============================================================================
PyObject* SPELLdtaContainer::getValueEx( PyObject* key )
{
	SPELLsafePythonOperations ops("DTA");
	if (PyDict_Contains(m_dict, key))
	{
		return PyDict_GetItem(m_dict,key);
	}
	return NULL;
}

//============================================================================
// METHOD: SPELLdtaContainer::getValue
//============================================================================
PyObject* SPELLdtaContainer::getValue( PyObject* key )
{
	DEBUG("[DTAC] Get value");
	SPELLsafePythonOperations ops("DTA");

	if (!PyDict_Contains(m_dict, key))
	{
		DEBUG("[DTAC] Variable is missing, asking for a value");
		PyObject* varClass = SPELLpythonHelper::instance().getObject("libSPELL_DTA", "Var");
		Py_XINCREF(varClass);
		PyObject* instance = SPELLpythonHelper::instance().newInstance(varClass, NULL, NULL);
		Py_XINCREF(instance);

		//Py_DECREF(varClass);
		// PyDict_Setitem increments the referene count
		PyDict_SetItem(m_dict, key, instance);

		std::string message = "Data ";
		if (m_name != "")
		{
			message += "set '" + m_name + "'";
		}
		else
		{
			message += "set";
		}
		message += " does not contain variable '" + PYSTR(key) + "'.\nPlease provide a value for it.";

		SPELLdtaVariableObject* varObj = reinterpret_cast<SPELLdtaVariableObject*>(instance);
		SPELLdtaVariable* var = varObj->var;
		bool valueOk = false;
		do
		{
			valueOk = var->promptForValue( message );
		}
		while(valueOk == false);

		DEBUG("[DTAC] Get value done.");
		return var->getValue();
	}

	// Borrowed reference
	PyObject* value = PyDict_GetItem(m_dict, key);
	Py_XINCREF(value);
	assert(value != NULL);
	PyObject* result = Py_None;

	// New reference
	PyTypeObject* type = (PyTypeObject*) PyObject_Type(value);
	Py_XINCREF(type);
	std::string varType = "spell.lib.adapter.data.Var";
	if (varType == type->tp_name )
	{
		DEBUG("[DTAC] Variable exists and is type Var: " + PYSSTR(value));
		SPELLdtaVariableObject* varObj = reinterpret_cast<SPELLdtaVariableObject*>(value);
		assert(varObj != NULL);
		SPELLdtaVariable* var = varObj->var;
		assert(var != NULL);
		try
		{
			DEBUG("[DTAC] Get variable value from " + PSTR(varObj) + ": " + PYCREPR(varObj));
			result = var->getValue();
			DEBUG("[DTAC] Current result is " + PYREPR(result));
			if (var->getConfirmGet())
			{
				DEBUG("[DTAC] Confirm variable value");
				result = var->confirmValue();
			}

		}
		catch(SPELLcoreException& err)
		{
			DEBUG("[DTAC] Captured uninit variable");

			bool valueOk = false;
			std::string msg = "Variable '" + PYSTR(key) + "'";
			if (m_name != "")
			{
				msg += " in set '" + m_name + "'";
			}
			msg += " is not initialized, please provide a value.";

			do
			{
				valueOk = var->promptForValue( msg );
			}
			while( valueOk != true );
			result = var->getValue();
		}
	}
	else
	{
		DEBUG("[DTAC] Not a variable type");
		result = value;
	}

	Py_XINCREF(result);
	//Py_DECREF(type);

	DEBUG("[DTAC] Get value done: " + PYCREPR(result));
	SPELLpythonHelper::instance().checkError();
	return result;
}

//============================================================================
// METHOD: SPELLdtaContainer::hasKey
//============================================================================
bool SPELLdtaContainer::hasKey( PyObject* key )
{
	SPELLsafePythonOperations ops("DTA");
	return PyDict_Contains(m_dict,key);
}

//============================================================================
// METHOD: SPELLdtaContainer::getKeys()
//============================================================================
PyObject* SPELLdtaContainer::getKeys()
{
	SPELLsafePythonOperations ops("DTA");
	PyObject* keyList = PyList_New( m_varOrder.size() );
	Py_XINCREF(keyList);
	std::vector<std::string>::iterator it;
	unsigned int index = 0;
	for(it = m_varOrder.begin(); it != m_varOrder.end(); it++)
	{
		std::string keyStr = *it;
		PyObject* key = SSTRPY(keyStr);
		Py_XINCREF(key);
		PyList_SetItem( keyList, index, key );
		index++;
	}
	return keyList;
}
