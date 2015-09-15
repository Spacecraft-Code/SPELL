// ################################################################################
// FILE       : SPELLpyValue.C
// DATE       : Mar 17, 2011
// PROJECT    : SPELL
// DESCRIPTION: Data variable implementation
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
#include "SPELL_UTIL/SPELLutils.H"
#include "SPELL_UTIL/SPELLerror.H"
#include "SPELL_UTIL/SPELLpythonHelper.H"
// Local includes ----------------------------------------------------------
#include "SPELL_WRP/SPELLconstants.H"
#include "SPELL_WRP/SPELLpyValue.H"
#include "SPELL_WRP/SPELLpyHandle.H"
// System includes ---------------------------------------------------------

// TYPES ///////////////////////////////////////////////////////////////////
// DEFINES /////////////////////////////////////////////////////////////////
// GLOBALS /////////////////////////////////////////////////////////////////

//============================================================================
// CONSTRUCTOR: SPELLpyValue::SPELLpyValue
//============================================================================
SPELLpyValue::SPELLpyValue( PyObject* pyValue )
{
	set(pyValue);
}

//============================================================================
// DESTRUCTOR: SPELLpyValue::~SPELLpyValue
//============================================================================
SPELLpyValue::~SPELLpyValue()
{
}

//============================================================================
// METHOD: SPELLpyValue::type()
//============================================================================
SPELLpyValue::Type SPELLpyValue::type() const
{
	return m_type;
}

//============================================================================
// METHOD: SPELLpyValue::set
//============================================================================
void SPELLpyValue::set( PyObject* pyValue )
{
	m_type = NONE;
	m_intValue = 0;
	m_boolValue = false;
	m_floatValue = 0.0;
	m_timeValue.set(0,0);
	m_stringValue = "";

	if (pyValue == NULL) return;

	DEBUG("[PYVAL] Set value from " + PYREPR(pyValue));
	if (pyValue != Py_None)
	{
		if (PyBool_Check(pyValue))
		{
			m_type = BOOLEAN;
			m_boolValue = pyValue == Py_True;
		}
		else if (PyLong_Check(pyValue))
		{
			DEBUG("[PYVAL] Long check");
			m_type = LONG;
			m_intValue = PyLong_AsLongLong(pyValue);
		}
		else if (PyInt_Check(pyValue))
		{
			DEBUG("[PYVAL] Int check");
			m_type = LONG;
			m_intValue = PyInt_AsLong(pyValue);
		}
		else if (PyFloat_Check(pyValue))
		{
			m_type = DOUBLE;
			m_floatValue = PyFloat_AsDouble(pyValue);
		}
		else if (SPELLpythonHelper::instance().isTime(pyValue))
		{
			m_timeValue = SPELLpythonHelper::instance().evalTime(PYSSTR(pyValue));
			if (m_timeValue.isDelta())
			{
				m_type = RELTIME;
			}
			else
			{
				m_type = ABSTIME;
			}
		}
		else if (PyString_Check(pyValue))
		{
			m_type = STRING;
			m_stringValue = PYSTR(pyValue);
		}
		else if (PyList_Check(pyValue))
		{
			m_type = LIST;
			m_listValue.clear();
			unsigned int numItems = PyList_Size(pyValue);
			for(unsigned int idx = 0; idx < numItems; idx++)
			{
				m_listValue.push_back( SPELLpyValue( PyList_GetItem( pyValue, idx) ));
			}
		}
		else if (PyDict_Check(pyValue))
		{
			m_type = DICT;
			m_dictValue.clear();
			SPELLpyHandle keys = PyDict_Keys(pyValue);
			unsigned int numItems = PyList_Size(keys.get());
			for(unsigned int idx = 0; idx < numItems; idx++)
			{
				PyObject* key = PyList_GetItem(keys.get(),idx);
				PyObject* value = PyDict_GetItem( pyValue, key );
				m_dictValue.insert( std::make_pair( PYSSTR(key), SPELLpyValue(value) ) );
			}
		}
		else
		{
			THROW_EXCEPTION("Cannot create variable value",
					        "Cannot infer type from value (" + PYREPR(pyValue) + ")",
					        SPELL_ERROR_LANGUAGE);
		}
		SPELLpythonHelper::instance().checkError();
	}
}

//============================================================================
// METHOD: SPELLpyValue::get
//============================================================================
PyObject* SPELLpyValue::get() const
{
	if (m_type == BOOLEAN )
	{
		if (m_boolValue)
		{
			Py_RETURN_TRUE;
		}
		else
		{
			Py_RETURN_FALSE;
		}
	}
	else if (m_type == STRING )
	{
		return SSTRPY(m_stringValue);
	}
	else if (m_type == LONG )
	{
		return PyLong_FromLong(m_intValue);
	}
	else if (m_type == DOUBLE)
	{
		return PyFloat_FromDouble(m_floatValue);
	}
	else if (m_type == RELTIME )
	{
		PyObject* time = SPELLpythonHelper::instance().pythonTime(m_timeValue);
		return time;
	}
	else if (m_type == ABSTIME )
	{
		PyObject* time = SPELLpythonHelper::instance().pythonTime(m_timeValue);
		return time;
	}
	else if (m_type == LIST )
	{
		PyObject* list = PyList_New(m_listValue.size());
		ValueList::const_iterator it = m_listValue.begin();
		unsigned int idx = 0;
		for( it = m_listValue.begin(); it != m_listValue.end(); it++ )
		{
			const SPELLpyValue& value = *it;
			PyObject* item = value.get();
			Py_INCREF(item);
			PyList_SetItem(list,idx,item);
			idx++;
		}
		return list;
	}
	else if (m_type == DICT )
	{
		PyObject* dict = PyDict_New();
		ValueMap::const_iterator it = m_dictValue.begin();
		for( it = m_dictValue.begin(); it != m_dictValue.end(); it++ )
		{
			std::string key = it->first;
			const SPELLpyValue& value = it->second;
			PyObject* item = value.get();
			Py_INCREF(item);
			PyDict_SetItemString(dict,key.c_str(),item);
		}
		return dict;
	}
	Py_RETURN_NONE;
}

//============================================================================
// METHOD: SPELLpyValue::typeStr()
//============================================================================
std::string SPELLpyValue::typeStr() const
{
	switch(m_type)
	{
	case LONG:
		return LanguageConstants::LONG;
	case DOUBLE:
		return LanguageConstants::FLOAT;
	case BOOLEAN:
		return LanguageConstants::BOOLEAN;
	case STRING:
		return LanguageConstants::STRING;
	case ABSTIME:
		return LanguageConstants::DATETIME;
	case RELTIME:
		return LanguageConstants::RELTIME;
	case LIST:
		return "LIST";
	case DICT:
		return "DICT";
	default:
		return "";
	}
}

//============================================================================
// METHOD: SPELLpyValue::str
//============================================================================
std::string SPELLpyValue::str() const
{
	if (m_type == BOOLEAN )
	{
		if (m_boolValue)
		{
			return "True";
		}
		else
		{
			return "False";
		}
	}
	else if (m_type == STRING )
	{
		return m_stringValue;
	}
	else if (m_type == LONG )
	{
		return ISTR(m_intValue);
	}
	else if (m_type == DOUBLE)
	{
		PyObject* pyValue = PyFloat_FromDouble(m_floatValue);
		PyObject* str = PyObject_Str(pyValue);
		std::string val = PYSTR(str);
		Py_DECREF(pyValue);
		Py_DECREF(str);
		return val;
	}
	else if (m_type == RELTIME )
	{
		return m_timeValue.toTIMEString();
	}
	else if (m_type == ABSTIME )
	{
		return m_timeValue.toTIMEString();
	}
	else if (m_type == LIST )
	{
		std::string val = "[";
		ValueList::const_iterator it;
		for( it = m_listValue.begin(); it != m_listValue.end(); it++ )
		{
			if (val.length()>1) val += ", ";
			val += it->str();
		}
		val += "]";
		return val;
	}
	else if (m_type == DICT )
	{
		std::string val = "{";
		ValueMap::const_iterator it;
		for( it = m_dictValue.begin(); it != m_dictValue.end(); it++ )
		{
			if (val.length()>1) val += ", ";
			val += it->first + ":" + it->second.str();
		}
		val += "}";
		return val;
	}
	return "";
}

//============================================================================
// OPERATOR: ==
//============================================================================
bool SPELLpyValue::operator==( const SPELLpyValue& other )
{
	if (m_type != other.m_type) return false;
	if (m_type == NONE) return true;

	if (m_type == BOOLEAN )
	{
		return m_boolValue == other.m_boolValue;
	}
	else if (m_type == STRING )
	{
		return m_stringValue == other.m_stringValue;
	}
	else if (m_type == LONG )
	{
		return m_intValue == other.m_intValue;
	}
	else if (m_type == DOUBLE)
	{
		return m_floatValue == other.m_floatValue;
	}
	else if (m_type == RELTIME )
	{
		return m_timeValue == other.m_timeValue;
	}
	else if (m_type == ABSTIME )
	{
		return m_timeValue == other.m_timeValue;
	}
	else if (m_type == LIST )
	{
		return false; //TODO
	}
	else if (m_type == DICT )
	{
		return false; //TODO
	}
	return false;
}

//============================================================================
// OPERATOR: ==
//============================================================================
bool SPELLpyValue::operator<=( const SPELLpyValue& other )
{
	if (m_type != other.m_type) return false;
	if (m_type == NONE) return true;

	if (m_type == BOOLEAN )
	{
		return m_boolValue <= other.m_boolValue;
	}
	else if (m_type == STRING )
	{
		return m_stringValue <= other.m_stringValue;
	}
	else if (m_type == LONG )
	{
		return m_intValue <= other.m_intValue;
	}
	else if (m_type == DOUBLE)
	{
		return m_floatValue <= other.m_floatValue;
	}
	else if (m_type == RELTIME )
	{
		return m_timeValue <= other.m_timeValue;
	}
	else if (m_type == ABSTIME )
	{
		return m_timeValue <= other.m_timeValue;
	}
	else if (m_type == LIST )
	{
		return false; //TODO
	}
	else if (m_type == DICT )
	{
		return false; //TODO
	}
	return false;
}

//============================================================================
// OPERATOR: ==
//============================================================================
bool SPELLpyValue::operator<( const SPELLpyValue& other )
{
	if (m_type != other.m_type) return false;
	if (m_type == NONE) return false;

	if (m_type == BOOLEAN )
	{
		return m_boolValue < other.m_boolValue;
	}
	else if (m_type == STRING )
	{
		return m_stringValue < other.m_stringValue;
	}
	else if (m_type == LONG )
	{
		return m_intValue < other.m_intValue;
	}
	else if (m_type == DOUBLE)
	{
		return m_floatValue < other.m_floatValue;
	}
	else if (m_type == RELTIME )
	{
		return m_timeValue < other.m_timeValue;
	}
	else if (m_type == ABSTIME )
	{
		return m_timeValue < other.m_timeValue;
	}
	else if (m_type == LIST )
	{
		return false; //TODO
	}
	else if (m_type == DICT )
	{
		return false; //TODO
	}
	return false;

}

//============================================================================
// OPERATOR: ==
//============================================================================
bool SPELLpyValue::operator>=( const SPELLpyValue& other )
{
	if (m_type != other.m_type) return false;
	if (m_type == NONE) return true;

	if (m_type == BOOLEAN )
	{
		return m_boolValue >= other.m_boolValue;
	}
	else if (m_type == STRING )
	{
		return m_stringValue >= other.m_stringValue;
	}
	else if (m_type == LONG )
	{
		return m_intValue >= other.m_intValue;
	}
	else if (m_type == DOUBLE)
	{
		return m_floatValue >= other.m_floatValue;
	}
	else if (m_type == RELTIME )
	{
		return m_timeValue >= other.m_timeValue;
	}
	else if (m_type == ABSTIME )
	{
		return m_timeValue >= other.m_timeValue;
	}
	else if (m_type == LIST )
	{
		return false; //TODO
	}
	else if (m_type == DICT )
	{
		return false; //TODO
	}
	return false;
}

//============================================================================
// OPERATOR: ==
//============================================================================
bool SPELLpyValue::operator>( const SPELLpyValue& other )
{
	if (m_type != other.m_type) return false;
	if (m_type == NONE) return false;

	if (m_type == BOOLEAN )
	{
		return m_boolValue > other.m_boolValue;
	}
	else if (m_type == STRING )
	{
		return m_stringValue > other.m_stringValue;
	}
	else if (m_type == LONG )
	{
		return m_intValue > other.m_intValue;
	}
	else if (m_type == DOUBLE)
	{
		return m_floatValue > other.m_floatValue;
	}
	else if (m_type == RELTIME )
	{
		return m_timeValue > other.m_timeValue;
	}
	else if (m_type == ABSTIME )
	{
		return m_timeValue > other.m_timeValue;
	}
	else if (m_type == LIST )
	{
		return false; //TODO
	}
	else if (m_type == DICT )
	{
		return false; //TODO
	}
	return false;
}

//============================================================================
// OPERATOR: ==
//============================================================================
bool SPELLpyValue::operator!=( const SPELLpyValue& other )
{
	if (m_type != other.m_type) return true;
	if (m_type == NONE) return false;

	if (m_type == BOOLEAN )
	{
		return m_boolValue != other.m_boolValue;
	}
	else if (m_type == STRING )
	{
		return m_stringValue != other.m_stringValue;
	}
	else if (m_type == LONG )
	{
		return m_intValue != other.m_intValue;
	}
	else if (m_type == DOUBLE)
	{
		return m_floatValue != other.m_floatValue;
	}
	else if (m_type == RELTIME )
	{
		return !(m_timeValue == other.m_timeValue);
	}
	else if (m_type == ABSTIME)
	{
		return !(m_timeValue == other.m_timeValue);
	}
	else if (m_type == LIST )
	{
		return false; //TODO
	}
	else if (m_type == DICT )
	{
		return false; //TODO
	}
	return false;
}

//============================================================================
// OPERATOR: ==
//============================================================================
SPELLpyValue& SPELLpyValue::operator=( PyObject* pyValue )
{
	set(pyValue);
	return *this;
}

//============================================================================
// OPERATOR: ==
//============================================================================
SPELLpyValue& SPELLpyValue::operator=( const SPELLpyValue& other )
{
	m_intValue = other.m_intValue;
	m_floatValue = other.m_floatValue;
	m_boolValue = other.m_boolValue;
	m_stringValue = other.m_stringValue;
	m_timeValue = other.m_timeValue;
	m_type = other.m_type;
	ValueList::const_iterator it;
	for( it = other.m_listValue.begin(); it != other.m_listValue.end(); it++ )
	{
		m_listValue.push_back(*it);
	}
	ValueMap::const_iterator dit;
	for( dit = other.m_dictValue.begin(); dit != other.m_dictValue.end(); dit++ )
	{
		m_dictValue.insert( std::make_pair( dit->first, dit->second ) );
	}
	return *this;
}
