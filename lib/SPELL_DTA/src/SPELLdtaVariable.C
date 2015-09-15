// ################################################################################
// FILE       : SPELLdtaVariable.C
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
#include "SPELL_WRP/SPELLconstants.H"
#include "SPELL_UTIL/SPELLutils.H"
#include "SPELL_UTIL/SPELLerror.H"
#include "SPELL_UTIL/SPELLpythonHelper.H"
#include "SPELL_UTIL/SPELLpythonMonitors.H"
#include "SPELL_CIF/SPELLcif.H"
#include "SPELL_EXC/SPELLexecutor.H"
#include "SPELL_CIF/SPELLnotifications.H"
#include "SPELL_WRP/SPELLpyHandle.H"
// Local includes ----------------------------------------------------------
#include "SPELL_DTA/SPELLdtaVariable.H"
#include "SPELL_DTA/SPELLdtaContainer.H"
// System includes ---------------------------------------------------------

// TYPES ///////////////////////////////////////////////////////////////////
// DEFINES /////////////////////////////////////////////////////////////////
// GLOBALS /////////////////////////////////////////////////////////////////


//============================================================================
// CONSTRUCTOR: SPELLdtaVariable::SPELLdtaVariable
//============================================================================
SPELLdtaVariable::SPELLdtaVariable( PyObject* kwds )
: m_value(Py_None)
{
	m_name = "";
	m_container = NULL;
	DEBUG("[DTAV] Initialization keywords " + PYREPR(kwds));

	if (kwds == NULL)
	{
		m_value = Py_None;
		m_type = "";
		m_format = "";
		m_range.clear();
		m_expected.clear();
		m_confirmGet = false;
	}
	else
	{
		ItemNotification notification;
		try
		{
			if (SPELLdtaContainer::areGlobalnotificationsEnabled())
			{
				notification.name = m_name;
				notification.value = "???";
				notification.status = LanguageConstants::ITEM_PROGRESS;
				notification.type = NOTIFY_ITEM;
				SPELLexecutor::instance().getCIF().notify( notification );
			}

			getGivenValue(kwds);
			getGivenRange(kwds);
			getGivenExpected(kwds);
			getGivenConfirm(kwds);

			if (!getGivenType(kwds))
			{
				if (!inferTypeFromValue() && !inferTypeFromRange())
				{
					inferTypeFromExpected();
				}
			}
			else
			{
				checkTypeConsistent();
			}

			if (getGivenFormat(kwds))
			{
				checkFormatConsistent();
			}

			checkRangeConsistent();
			checkExpectedConsistent();

			if (m_value != Py_None)
			{
				checkValueAgainstRange(m_value);
				checkValueAgainstExpected(m_value);
			}

			if (SPELLdtaContainer::areGlobalnotificationsEnabled())
			{
				notification.value = formatValue(m_value);
				notification.status = LanguageConstants::ITEM_SUCCESS;
				SPELLexecutor::instance().getCIF().notify( notification );
			}
		}
		catch(SPELLcoreException& ex)
		{
			if (SPELLdtaContainer::areGlobalnotificationsEnabled())
			{
				notification.status = LanguageConstants::ITEM_FAILED;
				SPELLexecutor::instance().getCIF().notify( notification );
			}
			throw;
		}

		DEBUG("Initialized values:");
		DEBUG("  - value   : " + m_value.str());
		DEBUG("  - type    : " + m_type);
		DEBUG("  - format  : " + m_format);
		DEBUG("  - expected: " + ISTR(m_expected.size()) );
		DEBUG("  - range   : " + ISTR(m_range.size()) );
		DEBUG("  - confirm : " + BSTR(m_confirmGet));
	}
}

//============================================================================
// DESTRUCTOR: SPELLdtaVariable::~SPELLdtaVariable
//============================================================================
SPELLdtaVariable::~SPELLdtaVariable()
{
	DEBUG("[DTAV] ######## Destroy variable " + m_name);
}

//============================================================================
// METHOD: SPELLdtaVariable::formatValue
//============================================================================
std::string SPELLdtaVariable::formatValue( SPELLpyValue& value )
{
	std::string repr = value.str();
	if (m_type == LanguageConstants::LONG && (value != NULL) && (value != Py_None) )
	{
		if (m_format == LanguageConstants::BIN)
		{
			repr = "0b" + SPELLutils::binstr(STRI(repr));
		}
		else if (m_format == LanguageConstants::HEX)
		{
			repr = "0x" + SPELLutils::toUpper(SPELLutils::hexstr(STRI(repr)));
		}
		else if (m_format == LanguageConstants::OCT)
		{
			repr = "0" + SPELLutils::octstr(STRI(repr));
		}
	}
	return repr;
}

//============================================================================
// METHOD: SPELLdtaVariable::setContainer
//============================================================================
void SPELLdtaVariable::setContainer( SPELLdtaContainer* container )
{
	m_container = container;
}

//============================================================================
// METHOD: SPELLdtaVariable::shouldNotify()
//============================================================================
bool SPELLdtaVariable::shouldNotify()
{
	if (m_container)
	{
		if (!SPELLdtaContainer::areGlobalnotificationsEnabled()) return false;
		return m_container->areNotificationsEnabled();
	}
	else
	{
		return SPELLdtaContainer::areGlobalnotificationsEnabled();
	}
}

//============================================================================
// METHOD: SPELLdtaVariable::setValue
//============================================================================
void SPELLdtaVariable::setValue( PyObject* pyValue )
{
	DEBUG("[DTAV] Setting value: " + PYREPR(pyValue));

	SPELLpyValue value = pyValue;

	ItemNotification notification;
	if (shouldNotify())
	{
		notification.name = m_name;
		notification.value = formatValue(value);
		notification.status = LanguageConstants::ITEM_PROGRESS;
		notification.type = NOTIFY_ITEM;
		SPELLexecutor::instance().getCIF().notify( notification );
	}

	try
	{
		// BEFORE any checks:
		// In case of assigning TIME values, we need to evaluate the passed string expression
		// in order to obtain a TIME instance
		if (m_type == LanguageConstants::DATETIME || m_type == LanguageConstants::RELTIME)
		{
			value = SPELLpythonHelper::instance().pythonTime( value.str() );
		}

		// If the value can be checked
		if (!value.isNone())
		{
			// Will do nothing if no type is defined
			checkValueAgainstType(value);
			// Will do nothing if no range is defined
			checkValueAgainstRange(value);
			// Will do nothing if no expected list is defined
			checkValueAgainstExpected(value);
		}

		// We need to readjust the value if this variable is float but we accepted an integer
		if (m_type == LanguageConstants::FLOAT && value.typeStr() == LanguageConstants::LONG )
		{
			int longValue = 0;
			if (PyInt_Check(pyValue))
			{
				longValue = PyInt_AsLong(pyValue);
			}
			else
			{
				longValue = PyLong_AsLongLong(pyValue);
			}
			value = PyFloat_FromDouble( (double) longValue );
		}

		m_value = value;

		DEBUG("[DTAV] Assigned value: " + m_value.str() + ": " + m_value.typeStr() );

		// If no type defined yet, infer it from the given value
		if ( m_type == "" )
		{
			inferTypeFromValue();
		}

		if (shouldNotify())
		{
			notification.status = LanguageConstants::ITEM_SUCCESS;
			SPELLexecutor::instance().getCIF().notify( notification );
		}

		DEBUG("[DTAV] Value set: " + m_value.str());
	}
	catch( SPELLcoreException& ex )
	{
		if (shouldNotify())
		{
			notification.status = LanguageConstants::ITEM_FAILED;
			SPELLexecutor::instance().getCIF().notify( notification );
		}
		throw;
	}
}

//============================================================================
// METHOD: SPELLdtaVariable::getValue
//============================================================================
PyObject* SPELLdtaVariable::getValue()
{
	ItemNotification notification;
	if (shouldNotify())
	{
		notification.name = m_name;
		notification.value = "...";
		notification.status = LanguageConstants::ITEM_PROGRESS;
		notification.type = NOTIFY_ITEM;
		SPELLexecutor::instance().getCIF().notify( notification );
	}

	if (m_value.isNone())
	{
		if (shouldNotify())
		{
			notification.value = "???";
			notification.status = LanguageConstants::ITEM_FAILED;
			SPELLexecutor::instance().getCIF().notify( notification );
		}
		THROW_EXCEPTION("Cannot use variable", "Variable has not been initialized", SPELL_ERROR_LANGUAGE );
	}

	if (shouldNotify())
	{
		notification.value = formatValue(m_value);
		notification.status = LanguageConstants::ITEM_SUCCESS;
		SPELLexecutor::instance().getCIF().notify( notification );
	}

	return m_value.get();
}

//============================================================================
// METHOD: SPELLdtaVariable::getValueEx
//============================================================================
PyObject* SPELLdtaVariable::getValueEx()
{
	return m_value.get();
}

//============================================================================
// METHOD: SPELLdtaVariable::getGivenValue
//============================================================================
void SPELLdtaVariable::getGivenValue( PyObject* kwds )
{
	SPELLpyHandle pyMod = SSTRPY(LanguageModifiers::Default);
	if (PyDict_Contains(kwds, pyMod.get() ))
	{
		// Returns borrowed reference
		m_value = PyDict_GetItemString( kwds, LanguageModifiers::Default.c_str() );
	}
	else
	{
		m_value = Py_None;
	}
	DEBUG("[DTAV] Given default value: " + m_value.str());
}

//============================================================================
// METHOD: SPELLdtaVariable::getGivenExpected
//============================================================================
void SPELLdtaVariable::getGivenExpected( PyObject* kwds )
{
	m_expected.clear();
	SPELLpyHandle pyMod = SSTRPY(LanguageModifiers::Expected);
	if (PyDict_Contains(kwds, pyMod.get() ))
	{
		// Borrowed reference
		PyObject* expected = PyDict_GetItemString( kwds, LanguageModifiers::Expected.c_str() );
		DEBUG("[DTAV] Expected modifier: " + PYREPR(expected));
		if (expected != Py_None)
		{
			if (!PyList_Check(expected) || PyList_Size(expected)==0)
			{
				THROW_EXCEPTION("Cannot create variable container", "List of expected values is not a list or is empty", SPELL_ERROR_LANGUAGE);
			}
		}
		int numExpected = PyList_Size(expected);
		for(int idx = 0; idx < numExpected; idx++)
		{
			SPELLpyValue exp = PyList_GetItem(expected, idx);
			m_expected.push_back(exp);
		}
	}

	if (!m_expected.empty())
	{
		// If we are handling TIME expressions, we need to convert the range elements to TIME instances
		if (m_type == LanguageConstants::DATETIME || m_type == LanguageConstants::RELTIME )
		{
			for( unsigned int index = 0; index < m_expected.size(); index++)
			{
				SPELLpyValue& exp = m_expected[index];
				SPELLpyValue newExp = SPELLpythonHelper::instance().pythonTime( exp.str() );
				m_expected[index] = newExp;
			}
		}
	}
}

//============================================================================
// METHOD: SPELLdtaVariable::getGivenRange
//============================================================================
void SPELLdtaVariable::getGivenRange( PyObject* kwds )
{
	m_range.clear();
	SPELLpyHandle pyMod = SSTRPY(LanguageModifiers::Range);
	if (PyDict_Contains(kwds, pyMod.get() ))
	{
		// Borrowed reference
		PyObject* range = PyDict_GetItemString( kwds, LanguageModifiers::Range.c_str() );
		DEBUG("[DTAV] Range modifier: " + PYREPR(range));
		if (range != Py_None)
		{
			if (!PyList_Check(range) || PyList_Size(range)!=2)
			{
				THROW_EXCEPTION("Cannot create variable container", "Range value is not a list of two values", SPELL_ERROR_LANGUAGE);
			}
		}
		for(int idx = 0; idx < 2; idx++)
		{
			SPELLpyValue rg = PyList_GetItem(range, idx);
			m_range.push_back(rg);
		}
	}

	if (!m_range.empty())
	{
		// If we are handling TIME expressions, we need to convert the range elements to TIME instances
		if (m_type == LanguageConstants::DATETIME || m_type == LanguageConstants::RELTIME )
		{
			SPELLpyValue& lo = m_range[0];
			SPELLpyValue newLo = SPELLpythonHelper::instance().pythonTime( lo.str() );
			m_range[0] = newLo;

			SPELLpyValue& hi = m_range[1];
			SPELLpyValue newHi = SPELLpythonHelper::instance().pythonTime( hi.str() );
			m_range[1] = newHi;
		}
	}
}

//============================================================================
// METHOD: SPELLdtaVariable::getGivenType
//============================================================================
bool SPELLdtaVariable::getGivenType( PyObject* kwds )
{
	SPELLpyHandle pyMod = SSTRPY(LanguageModifiers::Type);
	if (PyDict_Contains(kwds, pyMod.get() ))
	{
		// Borrowed reference
		PyObject* type = PyDict_GetItemString( kwds, LanguageModifiers::Type.c_str() );
		if (type != Py_None)
		{
			if (!PyString_Check(type))
			{
				THROW_EXCEPTION("Cannot create variable container", "Wrong value given to Type modifier (" + PYREPR(type) + ")", SPELL_ERROR_LANGUAGE);
			}
			std::string valueType = PYSTR(type);

			if ( valueType != LanguageConstants::LONG     &&
				 valueType != LanguageConstants::FLOAT    &&
				 valueType != LanguageConstants::BOOLEAN  &&
				 valueType != LanguageConstants::STRING   &&
				 valueType != LanguageConstants::DATETIME &&
				 valueType != LanguageConstants::RELTIME  )
			{
				THROW_EXCEPTION("Cannot create variable container", "Wrong value given to Type modifier (expected LONG, FLOAT, BOOLEAN, STRING, RELTIME or DATETIME), given " + PYREPR(type), SPELL_ERROR_LANGUAGE);
			}

			m_type = valueType;
			return true;
		}
		else
		{
			m_type = "";
			return false;
		}
	}
	m_type = "";
	return false;
}

//============================================================================
// METHOD: SPELLdtaVariable::getGivenFormat
//============================================================================
bool SPELLdtaVariable::getGivenFormat( PyObject* kwds )
{
	SPELLpyHandle pyMod = SSTRPY(LanguageModifiers::ValueFormat);
	if (PyDict_Contains(kwds, pyMod.get() ))
	{
		// Borrowed reference
		PyObject* format = PyDict_GetItemString( kwds, LanguageModifiers::ValueFormat.c_str());
		if (format != Py_None)
		{
			m_format = PYSSTR(format);
			if (m_type != "")
			{
				if (m_format == LanguageConstants::OCT ||
					m_format == LanguageConstants::BIN ||
					m_format == LanguageConstants::HEX)
				{
					if (m_type != LanguageConstants::LONG)
					{
						THROW_EXCEPTION("Cannot create variable container",
								        "Value type and value format are not consistent",
								        SPELL_ERROR_LANGUAGE);
					}
				}
				else if (m_format == LanguageConstants::DEC)
				{
					if (m_type != LanguageConstants::LONG && m_type != LanguageConstants::FLOAT)
					{
						THROW_EXCEPTION("Cannot create variable container",
								        "Value type and value format are not consistent",
								        SPELL_ERROR_LANGUAGE);
					}
				}
			}
			return true;
		}
		else
		{
			m_format = "";
			return false;
		}
	}
	m_format = "";
	return false;
}

//============================================================================
// METHOD: SPELLdtaVariable::getGivenConfirm
//============================================================================
void SPELLdtaVariable::getGivenConfirm( PyObject* kwds )
{
	SPELLpyHandle pyMod = SSTRPY(LanguageModifiers::Confirm);
	if (PyDict_Contains(kwds, pyMod.get() ))
	{
		// Borrowed reference
		PyObject* confirm = PyDict_GetItemString( kwds, LanguageModifiers::Confirm.c_str() );
		if (confirm != Py_None)
		{
			if (!PyBool_Check(confirm))
			{
				THROW_EXCEPTION("Cannot create variable container",
								"Expected True or False as a value for Confirm modifier",
								SPELL_ERROR_LANGUAGE);
			}
		}
		m_confirmGet = (confirm == Py_True);
	}
	else
	{
		m_confirmGet = false;
	}
}

//============================================================================
// METHOD: SPELLdtaVariable::inferTypeFromValue
//============================================================================
bool SPELLdtaVariable::inferTypeFromValue()
{
	if (!m_value.isNone())
	{
		m_type = m_value.typeStr();
		return true;
	}
	else
	{
		m_type = "";
		return false;
	}
}

//============================================================================
// METHOD: SPELLdtaVariable::checkValueAgainstType
//============================================================================
void SPELLdtaVariable::checkValueAgainstType( SPELLpyValue& value )
{
	if (m_type != "")
	{
		DEBUG("[DTAV] Check value " + value.str() + " against type " + m_type);
		if (m_type != value.typeStr())
		{
			if (m_type == LanguageConstants::LONG)
			{
				THROW_EXCEPTION("Cannot accept value",
								"Value and variable type do not match (expected type is LONG, given value is '" + value.str() + "')",
								SPELL_ERROR_LANGUAGE);
			}
			else if (m_type == LanguageConstants::FLOAT)
			{
				// Special case: allow integers passed to float variables
				if (value.typeStr() != LanguageConstants::LONG)
				{
					THROW_EXCEPTION("Cannot accept value",
								"Value and variable type do not match (expected type is FLOAT, given value is '" + value.str() + "')",
								SPELL_ERROR_LANGUAGE);
				}
			}
			else if (m_type == LanguageConstants::BOOLEAN)
			{
				THROW_EXCEPTION("Cannot accept value",
								"Value and variable type do not match (expected type is BOOLEAN, given value is '" + value.str() + "')",
								SPELL_ERROR_LANGUAGE);
			}
			else if (m_type == LanguageConstants::STRING)
			{
				THROW_EXCEPTION("Cannot accept value",
								"Value and variable type do not match (expected type is STRING, given value is '" + value.str() + "')",
								SPELL_ERROR_LANGUAGE);
			}
			else if (m_type == LanguageConstants::RELTIME)
			{
				if (value.typeStr() == LanguageConstants::DATETIME)
				{
					THROW_EXCEPTION("Cannot accept value",
									"Value and variable type do not match (expected type is relative time, value is an absolute time '" + value.str() + "')",
									SPELL_ERROR_LANGUAGE);

				}
				else
				{
					THROW_EXCEPTION("Cannot accept value",
								"Value and variable type do not match (expected type is TIME, value is '" + value.str() + "')",
								SPELL_ERROR_LANGUAGE);
				}
			}
			else if (m_type == LanguageConstants::DATETIME)
			{
				if (value.typeStr() == LanguageConstants::RELTIME )
				{
					THROW_EXCEPTION("Cannot accept value",
									"Value and variable type do not match (expected type is absolute time, value is a relative time '" + value.str() + "')",
									SPELL_ERROR_LANGUAGE);

				}
				else
				{
					THROW_EXCEPTION("Cannot accept value",
								"Value and variable type do not match (expected type is TIME, value is '" + value.str() + "')",
								SPELL_ERROR_LANGUAGE);
				}
			}
		}
	}
}

//============================================================================
// METHOD: SPELLdtaVariable::checkTypeConsistent
//============================================================================
void SPELLdtaVariable::checkTypeConsistent()
{
	if (!m_value.isNone() && m_type != "" && m_type != m_value.typeStr())
	{
		if (m_type == LanguageConstants::LONG)
		{
			THROW_EXCEPTION("Cannot create variable container",
							"Default value and expected type do not match (expected is LONG, value is '" + m_value.str() + "')",
							SPELL_ERROR_LANGUAGE);
		}
		else if (m_type == LanguageConstants::FLOAT)
		{
			THROW_EXCEPTION("Cannot create variable container",
							"Default value and expected type do not match (expected is FLOAT, value is '" + m_value.str() + "')",
							SPELL_ERROR_LANGUAGE);
		}
		else if (m_type == LanguageConstants::BOOLEAN)
		{
			THROW_EXCEPTION("Cannot create variable container",
							"Default value and expected type do not match (expected is BOOLEAN, value is '" + m_value.str() + "')",
							SPELL_ERROR_LANGUAGE);
		}
		else if (m_type == LanguageConstants::STRING)
		{
			THROW_EXCEPTION("Cannot create variable container",
							"Default value and expected type do not match (expected is STRING, value is '" + m_value.str() + "')",
							SPELL_ERROR_LANGUAGE);

			if (SPELLpythonHelper::instance().isTime(m_value.get()))
			{
				THROW_EXCEPTION("Cannot create variable container",
						        "Default value and expected type do not match (expected is STRING, value is TIME)",
						        SPELL_ERROR_LANGUAGE);
			}
		}
		else if (m_type == LanguageConstants::RELTIME)
		{
			if (m_value.typeStr() == LanguageConstants::DATETIME)
			{
				THROW_EXCEPTION("Cannot accept value",
								"Value and variable type do not match (expected type is relative time, value is an absolute time '" + m_value.str() + "')",
								SPELL_ERROR_LANGUAGE);

			}
			else
			{
				THROW_EXCEPTION("Cannot accept value",
							"Value and variable type do not match (expected type is TIME, value is '" + m_value.str() + "')",
							SPELL_ERROR_LANGUAGE);
			}
		}
		else if (m_type == LanguageConstants::DATETIME)
		{
			if (m_value.typeStr() == LanguageConstants::RELTIME )
			{
				THROW_EXCEPTION("Cannot accept value",
								"Value and variable type do not match (expected type is absolute time, value is a relative time '" + m_value.str() + "')",
								SPELL_ERROR_LANGUAGE);

			}
			else
			{
				THROW_EXCEPTION("Cannot accept value",
							"Value and variable type do not match (expected type is TIME, value is '" + m_value.str() + "')",
							SPELL_ERROR_LANGUAGE);
			}
		}
	}
}

//============================================================================
// METHOD: SPELLdtaVariable::checkFormatConsistent
//============================================================================
void SPELLdtaVariable::checkFormatConsistent()
{
	if (m_type != "")
	{
		if (m_type != LanguageConstants::LONG )
		{
			if (m_format != "" )
			{
				THROW_EXCEPTION("Cannot create variable container",
						        "Given format is inconsistent with variable type",
						        SPELL_ERROR_LANGUAGE);
			}

		}
	}
}

//============================================================================
// METHOD: SPELLdtaVariable::checkRangeConsistent
//============================================================================
void SPELLdtaVariable::checkRangeConsistent()
{
	// If there is value given, check that the type of the
	// range elements is consistent with the type. Note
	// that m_type already contains the proper type code
	// since it was either given and checked already,
	// or inferred from value.
	if (m_type != "" && !m_range.empty())
	{
		// We have checked already that the range is
		// a list of two items
		if (m_range[0].typeStr() != m_type || m_range[1].typeStr() != m_type)
		{
			THROW_EXCEPTION("Cannot create variable container",
					        "Given range [" + m_range[0].str() + "," + m_range[1].str() + "] is inconsistent with variable type (" + m_type + ")",
					        SPELL_ERROR_LANGUAGE);
		}
	}
}

//============================================================================
// METHOD: SPELLdtaVariable::checkExpectedConsistent
//============================================================================
void SPELLdtaVariable::checkExpectedConsistent()
{
	// If there is value given, check that the type of the
	// list of expected elements is consistent with the type. Note
	// that m_type already contains the proper type code
	// since it was either given and checked already,
	// or inferred from value.
	if (m_type != "" && !m_expected.empty())
	{
		// We have checked already that the expected is a list

		for(unsigned int idx = 0; idx < m_expected.size(); idx++)
		{
			if (m_expected[idx].typeStr() != m_type)
			{
				std::string expStr = "[";
				for(unsigned int idx2 = 0; idx2 < m_expected.size(); idx2++)
				{
					if (idx2>0) expStr += ",";
					expStr += m_expected[idx2].str();
				}
				expStr += "]";

				THROW_EXCEPTION("Cannot create variable container",
						        "List of expected values " + expStr + " is inconsistent with variable type (" + m_type + ")",
						        SPELL_ERROR_LANGUAGE);
			}
		}
	}
}

//============================================================================
// METHOD: SPELLdtaVariable::checkValueAgainstRange
//============================================================================
void SPELLdtaVariable::checkValueAgainstRange( SPELLpyValue& value )
{
	// Check that the value matches the range limits
	if (!m_range.empty())
	{
		if ( m_range[0] > value )
		{
			std::string rangeStr = "[" + m_range[0].str() + "," + m_range[1].str() + "]";
			THROW_EXCEPTION("Cannot accept value '" + value.str() + "'", "It is below range limits " + rangeStr, SPELL_ERROR_LANGUAGE);
		}
		if ( m_range[1] < value )
		{
			std::string rangeStr = "[" + m_range[0].str() + "," + m_range[1].str() + "]";
			THROW_EXCEPTION("Cannot accept value '" + value.str() + "'", "It is above range limits " + rangeStr, SPELL_ERROR_LANGUAGE);
		}
	}
}

//============================================================================
// METHOD: SPELLdtaVariable::checkValueAgainstExpected
//============================================================================
void SPELLdtaVariable::checkValueAgainstExpected( SPELLpyValue& value )
{
	// Check that the value is in the expected values
	if (!m_expected.empty())
	{
		bool match = false;
		for( unsigned int idx = 0; idx < m_expected.size(); idx++)
		{
			if ( value == m_expected[idx] )
			{
				match = true;
				break;
			}
		}
		if (!match)
		{
			std::string expStr = "[";
			for(unsigned int idx2 = 0; idx2 < m_expected.size(); idx2++)
			{
				if (idx2>0) expStr += ",";
				expStr += m_expected[idx2].str();
			}
			expStr += "]";

			THROW_EXCEPTION("Cannot accept value '" + value.str() + "'", "Value is not in the list of expected values " + expStr, SPELL_ERROR_LANGUAGE);
		}
	}
}

//============================================================================
// METHOD: SPELLdtaVariable::str
//============================================================================
std::string SPELLdtaVariable::str()
{
	return m_value.str();
}

//============================================================================
// METHOD: SPELLdtaVariable::repr
//============================================================================
std::string SPELLdtaVariable::repr()
{
	return "'" + m_value.str() + "'";
}

//============================================================================
// METHOD: SPELLdtaVariable::toString
//============================================================================
std::string SPELLdtaVariable::toString()
{
	std::string str = "( ";
	if (!m_value.isNone())
	{
		str += m_value.str();
	}
	else
	{
		str += "[NOT INITIALIZED]";
	}

	if (m_type != "")
	{
		str += " type: " + m_type;
	}

	if (!m_range.empty())
	{
		std::string rangeStr = "[" + m_range[0].str() + "," + m_range[1].str() + "]";
		str += " range: " + rangeStr;
	}

	if (!m_expected.empty())
	{
		std::string expStr = "[";
		for(unsigned int idx2 = 0; idx2 < m_expected.size(); idx2++)
		{
			if (idx2>0) expStr += ",";
			expStr += m_expected[idx2].str();
		}
		expStr += "]";

		str += " expected: " + expStr;
	}

	if (m_confirmGet)
	{
		str += " <confirm get>";
	}

	str += " )";
	return str;
}

//============================================================================
// METHOD: SPELLdtaVariable::inferTypeFromRange
//============================================================================
bool SPELLdtaVariable::inferTypeFromRange()
{
	if (m_type == "" && !m_range.empty())
	{
		m_type = m_range[0].typeStr();
		return true;
	}
	return false;
}

//============================================================================
// METHOD: SPELLdtaVariable::inferTypeFromExpected
//============================================================================
void SPELLdtaVariable::inferTypeFromExpected()
{
	if (m_type == "" && !m_expected.empty())
	{
		m_type = m_expected[0].typeStr();
	}
}

//============================================================================
// METHOD: SPELLdtaVariable::promptForValue
//============================================================================
bool SPELLdtaVariable::promptForValue( std::string& message )
{
	SPELLpromptDefinition promptDef;
	promptDef.message = message;
	if (m_type != "")
	{
		promptDef.message +="\n   - Data type: " + m_type;
	}
	if (!m_range.empty())
	{
		std::string rangeStr = "[" + m_range[0].str() + "," + m_range[1].str() + "]";
		promptDef.message +="\n   - Range of values: " + rangeStr;
	}
	if (!m_expected.empty())
	{
		std::string expStr = "[";
		for(unsigned int idx2 = 0; idx2 < m_expected.size(); idx2++)
		{
			if (idx2>0) expStr += ",";
			expStr += m_expected[idx2].str();
		}
		expStr += "]";
		promptDef.message +="\n   - Expected values: " + expStr;
	}

	// If a list of expected values is given, build a prompt list out of it
	if (!m_expected.empty())
	{
		promptDef.typecode = LanguageConstants::PROMPT_LIST | LanguageConstants::PROMPT_ALPHA;
		for(unsigned int idx = 0; idx < m_expected.size(); idx++)
		{
			std::string option = "";
			if (m_type == LanguageConstants::STRING || m_type == LanguageConstants::DATETIME || m_type == LanguageConstants::RELTIME )
			{
				option = "'" + m_expected[idx].str() + "'";
			}
			else
			{
				option = m_expected[idx].str();
			}
			promptDef.options.push_back(option);
			promptDef.expected.push_back(option);
		}
	}
	else
	{
		// Adjust the type of the prompt according to the data type
		promptDef.typecode = LanguageConstants::PROMPT_ALPHA;
		if (m_type == LanguageConstants::LONG || m_type == LanguageConstants::FLOAT )
		{
			promptDef.typecode = LanguageConstants::PROMPT_NUM;
		}
		else if (m_type == LanguageConstants::BOOLEAN )
		{
			promptDef.typecode = LanguageConstants::PROMPT_LIST | LanguageConstants::PROMPT_ALPHA;
			promptDef.options.push_back("True");
			promptDef.options.push_back("False");
			promptDef.expected.push_back("True");
			promptDef.expected.push_back("False");
		}
	}

	promptDef.scope = LanguageConstants::SCOPE_PROC;

	//RACC 15-MAY SPELLsafeThreadOperations ops;
	//RACC 15-MAY SPELLsafePythonOperations ops2;
	std::string answer = SPELLexecutor::instance().getCIF().prompt(promptDef);

	if (answer == PROMPT_CANCELLED ||
		answer == PROMPT_ERROR     ||
		answer == PROMPT_TIMEOUT   ||
		answer == "" )
	{
		SPELLexecutor::instance().getCIF().warning("Variable value assignment canceled", LanguageConstants::SCOPE_SYS);
		m_value = Py_None;
		Py_INCREF(Py_None);
		return true;
	}
	else
	{
		try
		{
			if (m_type == LanguageConstants::DATETIME || m_type == LanguageConstants::RELTIME)
			{
				setValue(SPELLpythonHelper::instance().pythonTime(answer));
			}
			else if (m_type == LanguageConstants::STRING )
			{
				SPELLutils::trim(answer);
				if (answer[0] != '\'' && answer[0] != '"')
				{
					answer = "'" + answer + "'";
				}
				setValue(SPELLpythonHelper::instance().eval(answer,false));
			}
			else
			{
				setValue(SPELLpythonHelper::instance().eval(answer,false));
			}
			return true;
		}
		catch(SPELLcoreException& ex)
		{
			DEBUG("[DTAV] Error in evaluation: " + ex.what());
			message = ex.what();
			return false;
		}
	}
	return false;
}

//============================================================================
// METHOD: SPELLdtaVariable::confirmValue
//============================================================================
PyObject* SPELLdtaVariable::confirmValue()
{
	SPELLpromptDefinition promptDef;
	DEBUG("[DTAV] Confirm value of variable " + getName());
	std::string msg = "The current value of the variable '" +  getName() + "'";
	if (m_name != "")
	{
		if (m_container)
		{
			msg += " in set '" + m_container->getContainerName() + "'";
		}
		else
		{
			msg += " (not stored in a set)";
		}
	}
	msg += " is '" + PYSSTR(getValueEx()) + "'.\nIs it correct, or do you want to change the value?";
	promptDef.message = msg;
	promptDef.typecode = LanguageConstants::PROMPT_LIST;
	promptDef.options.push_back("1: It is CORRECT");
	promptDef.options.push_back("2: CHANGE the value");
	promptDef.expected.push_back("1");
	promptDef.expected.push_back("2");
	promptDef.scope = LanguageConstants::SCOPE_SYS;
	std::string answer = SPELLexecutor::instance().getCIF().prompt(promptDef);
	DEBUG("[DTAV] Answer: " + answer);
	if (answer == "1")
	{
		DEBUG("[DTAV] Return direct value");
		return getValueEx();
	}
	DEBUG("[DTAV] Incorrect, prompt for value");
	bool valueOk = false;
	msg = "Please provide a new value for variable '" + getName() + "':";
	do
	{
		valueOk = promptForValue( msg );
	}
	while( valueOk != true );
	return getValueEx();
}
