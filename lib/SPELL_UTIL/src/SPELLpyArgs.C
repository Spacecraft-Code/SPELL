// ################################################################################
// FILE       : SPELLpyArgs.C
// DATE       : Mar 17, 2011
// PROJECT    : SPELL
// DESCRIPTION: Implementation of the argument helper
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
// System includes ---------------------------------------------------------
// Local includes ----------------------------------------------------------
#include "SPELL_UTIL/SPELLpyArgs.H"
#include "SPELL_UTIL/SPELLpythonHelper.H"
// Project includes --------------------------------------------------------
#include "SPELL_WRP/SPELLconstants.H"
#include "SPELL_WRP/SPELLpyHandle.H"
#include "SPELL_UTIL/SPELLlog.H"

// GLOBALS /////////////////////////////////////////////////////////////////

//=============================================================================
// CONSTRUCTOR: SPELLpyArgs::SPELLpyArgs
//=============================================================================
SPELLpyArgs::SPELLpyArgs( PyObject* args )
{
	m_args = args;
	m_config = Py_None;
	if (args == NULL)
	{
		Py_INCREF(Py_None);
		m_args = Py_None;
	}
}

//=============================================================================
// CONSTRUCTOR: SPELLpyArgs::SPELLpyArgs
//=============================================================================
SPELLpyArgs::SPELLpyArgs( PyObject* args, PyObject* config )
{
    m_args = args;
    m_config = config;
	if (args == NULL)
	{
		Py_INCREF(Py_None);
		m_args = Py_None;
	}
	if (config == NULL)
	{
		Py_INCREF(Py_None);
		m_config = Py_None;
	}
}

//=============================================================================
// DESTRUCTOR: SPELLpyArgs::~SPELLpyArgs
//=============================================================================
SPELLpyArgs::~SPELLpyArgs()
{
    m_args = NULL;
    m_config = NULL;
}

//=============================================================================
// METHOD: SPELLpyArgs::size
//=============================================================================
unsigned int SPELLpyArgs::size() const
{
    if (m_args == Py_None) return 0;
    return PyTuple_Size(m_args);
}

//=============================================================================
// METHOD: SPELLpyArgs::operator[]
//=============================================================================
PyObject* SPELLpyArgs::operator[]( const int index ) const
{
    if (m_args == Py_None) return NULL;
    if (PyTuple_Size(m_args)<=index) return NULL;
    return PyTuple_GetItem(m_args,index);
}

//=============================================================================
// METHOD: SPELLpyArgs::operator[]
//=============================================================================
PyObject* SPELLpyArgs::operator[]( const std::string& modifier ) const
{
    if (m_config == Py_None) return NULL;
    SPELLpyHandle pyMod = SSTRPY(modifier);
    if (PyDict_Contains( m_config, pyMod.get() ))
    {
        return PyDict_GetItemString( m_config, modifier.c_str() );
    }
    else
    {
        return NULL;
    }
}

//=============================================================================
// METHOD: SPELLpyArgs::hasModifier
//=============================================================================
bool SPELLpyArgs::hasModifier( const std::string& modifier ) const
{
    if (m_config == Py_None) return false;
    SPELLpyHandle pyMod = SSTRPY(modifier);
    bool keyInDict = (PyDict_Contains( m_config, pyMod.get())>0);
    PyObject* value = PyDict_GetItemString( m_config, modifier.c_str() );
    bool isNone = (value == Py_None);
    return (keyInDict && (!isNone));
}

//=============================================================================
// METHOD: SPELLpyArgs::getModifier_Wait()
//=============================================================================
bool SPELLpyArgs::getModifier_Wait() const
{
    return getModifier_AsBoolean(LanguageModifiers::Wait);
}

//=============================================================================
// METHOD: SPELLpyArgs::getModifier_PromptUser()
//=============================================================================
bool SPELLpyArgs::getModifier_PromptUser() const
{
    PyObject* valueObj = (*this)[LanguageModifiers::PromptUser];
    DEBUG("[CFGHLP] Configured prompt user: " + PYREPR(valueObj) );
    bool value = true;
    if (valueObj != NULL)
    {
        if (PyBool_Check(valueObj))
        {
            value = (valueObj == Py_True);
        }
        else
        {
            THROW_EXCEPTION("Failed to get boolean value", "Expected a boolean value, obtained: " + PYREPR(valueObj), SPELL_ERROR_LANGUAGE );
        }
    }
    return value;
}

//=============================================================================
// METHOD: SPELLpyArgs::getModifier_LoadOnly()
//=============================================================================
bool SPELLpyArgs::getModifier_LoadOnly() const
{
    return getModifier_AsBoolean(LanguageModifiers::LoadOnly);
}

//=============================================================================
// METHOD: SPELLpyArgs::getModifier_Block()
//=============================================================================
bool SPELLpyArgs::getModifier_Block() const
{
    return getModifier_AsBoolean(LanguageModifiers::Block);
}

//=============================================================================
// METHOD: SPELLpyArgs::getModifier_Sequence()
//=============================================================================
bool SPELLpyArgs::getModifier_Sequence() const
{
    return getModifier_AsBoolean(LanguageModifiers::Sequence);
}

//=============================================================================
// METHOD: SPELLpyArgs::getModifier_AsTime()
//=============================================================================
SPELLtime SPELLpyArgs::getModifier_AsTime( const std::string& modifier ) const
{
    PyObject* timeObj = (*this)[modifier];
    DEBUG("[CFGHLP] Configured modifier: " + PYREPR(timeObj));
    SPELLtime time(0,true);
    if (timeObj != NULL)
    {
        if (PyLong_Check(timeObj))
        {
            time.set( PyLong_AsLong(timeObj), 0);
        }
        else if (PyInt_Check(timeObj))
        {
            time.set(PyInt_AsLong(timeObj),0);
        }
        else if (PyFloat_Check(timeObj))
        {
            double secsd = PyFloat_AsDouble( timeObj );
            unsigned long secs = (unsigned long) secsd;
            unsigned int msecs = (unsigned int) ((secsd-secs)*1000);
            time.set( secs, msecs );
        }
        else
        {
            time = SPELLpythonHelper::instance().evalTime(timeObj);
        }
    }
    return time;
}

//=============================================================================
// METHOD: SPELLpyArgs::getModifier_AsBoolean()
//=============================================================================
bool SPELLpyArgs::getModifier_AsBoolean( const std::string& modifier ) const
{
    PyObject* valueObj = (*this)[modifier];
    DEBUG("[CFGHLP] Configured modifier: " + PYREPR(valueObj))
    bool value = false;
    if (valueObj != NULL)
    {
        if (PyBool_Check(valueObj))
        {
            value = (valueObj == Py_True);
        }
        else
        {
            THROW_EXCEPTION("Failed to get boolean value", "Expected a boolean value, obtained: " + PYREPR(valueObj), SPELL_ERROR_LANGUAGE );
        }
    }
    return value;
}

//=============================================================================
// METHOD: SPELLpyArgs::getModifier_Timeout()
//=============================================================================
SPELLtime SPELLpyArgs::getModifier_Timeout() const
{
    return getModifier_AsTime( LanguageModifiers::Timeout );
}

//=============================================================================
// METHOD: SPELLpyArgs::getModifier_Time()
//=============================================================================
SPELLtime SPELLpyArgs::getModifier_Time() const
{
    return getModifier_AsTime( LanguageModifiers::Time );
}

//=============================================================================
// METHOD: SPELLpyArgs::getModifier_ReleaseTime()
//=============================================================================
SPELLtime SPELLpyArgs::getModifier_ReleaseTime() const
{
    return getModifier_AsTime( LanguageModifiers::ReleaseTime );
}

//=============================================================================
// METHOD: SPELLpyArgs::getModifier_Until()
//=============================================================================
SPELLtime SPELLpyArgs::getModifier_Until() const
{
    return getModifier_AsTime( LanguageModifiers::Until );
}

//=============================================================================
// METHOD: SPELLpyArgs::getModifier_Delay()
//=============================================================================
SPELLtime SPELLpyArgs::getModifier_Delay() const
{
    return getModifier_AsTime( LanguageModifiers::Delay );
}

//=============================================================================
// METHOD: SPELLpyArgs::getModifier_ValueFormat()
//=============================================================================
std::string SPELLpyArgs::getModifier_ValueFormat() const
{
    PyObject* formatObj = (*this)[LanguageModifiers::ValueFormat];
    DEBUG("[CFGHLP] Configured ValueFormat: " + PYREPR(formatObj))

    std::string format = LanguageConstants::ENG;

    if (formatObj != NULL)
    {
        if (PyString_Check(formatObj))
        {
            format = PYSTR(formatObj);
            if ( format != LanguageConstants::ENG  && format != LanguageConstants::RAW )
            {
                THROW_EXCEPTION("Failed to get ValueFormat value", "Expected ENG or RAW constants", SPELL_ERROR_LANGUAGE);
            }
        }
        else
        {
            THROW_EXCEPTION("Failed to get ValueFormat value", "Expected ENG or RAW constants", SPELL_ERROR_LANGUAGE );
        }
    }
    return format;
}

//=============================================================================
// METHOD: SPELLpyArgs::getModifier_Expected()
//=============================================================================
std::string SPELLpyArgs::getModifier_Expected() const
{
    PyObject* expectedObj = (*this)[LanguageModifiers::Expected];
    DEBUG("[CFGHLP] Configured Expected: " + PYREPR(expectedObj))

    std::string expected = "";
    if ( expectedObj != NULL )
    {
        if (PyString_Check( expectedObj ))
        {
            expected = PYSTR(expectedObj);
        }
        else
        {
            THROW_EXCEPTION("Failed to get Expected value", "Expected a string", SPELL_ERROR_LANGUAGE);
        }
    }
	return expected;
}

//=============================================================================
// METHOD: SPELLpyArgs::getModifier_LoRed()
//=============================================================================
std::string SPELLpyArgs::getModifier_LoRed() const
{
    PyObject* valueObj = (*this)[LanguageModifiers::LoRed];
    DEBUG("[CFGHLP] Configured LoRed: " + PYREPR(valueObj))

    std::string value = "";
    if ( valueObj != NULL )
    {
		value = PYSSTR(valueObj);
    }
	return value;
}

//=============================================================================
// METHOD: SPELLpyArgs::getModifier_LoYel()
//=============================================================================
std::string SPELLpyArgs::getModifier_LoYel() const
{
    PyObject* valueObj = (*this)[LanguageModifiers::LoYel];
    DEBUG("[CFGHLP] Configured LoYel: " + PYREPR(valueObj))

    std::string value = "";
    if ( valueObj != NULL )
    {
		value = PYSSTR(valueObj);
    }
	return value;
}

//=============================================================================
// METHOD: SPELLpyArgs::getModifier_HiRed()
//=============================================================================
std::string SPELLpyArgs::getModifier_HiRed() const
{
    PyObject* valueObj = (*this)[LanguageModifiers::HiRed];
    DEBUG("[CFGHLP] Configured HiRed: " + PYREPR(valueObj))

    std::string value = "";
    if ( valueObj != NULL )
    {
		value = PYSSTR(valueObj);
    }
	return value;
}

//=============================================================================
// METHOD: SPELLpyArgs::getModifier_HiYel()
//=============================================================================
std::string SPELLpyArgs::getModifier_HiYel() const
{
    PyObject* valueObj = (*this)[LanguageModifiers::HiYel];
    DEBUG("[CFGHLP] Configured HiYel: " + PYREPR(valueObj))

    std::string value = "";
    if ( valueObj != NULL )
    {
		value = PYSSTR(valueObj);
    }
	return value;
}

//=============================================================================
// METHOD: SPELLpyArgs::getModifier_Message()
//=============================================================================
std::string SPELLpyArgs::getModifier_Message() const
{
    PyObject* valueObj = (*this)[LanguageModifiers::Message];

    std::string value = "";
    if ( valueObj != NULL )
    {
        if (PyString_Check( valueObj ))
        {
            value = PYSTR(valueObj);
        }
        else
        {
            THROW_EXCEPTION("Failed to get Message value", "Expected a string", SPELL_ERROR_LANGUAGE);
        }
    }
	return value;
}

//=============================================================================
// METHOD: SPELLpyArgs::getModifier_Procedure()
//=============================================================================
std::string SPELLpyArgs::getModifier_Procedure() const
{
    PyObject* valueObj = (*this)[LanguageModifiers::Procedure];

    std::string value = "";
    if ( valueObj != NULL )
    {
        if (PyString_Check( valueObj ))
        {
            value = PYSTR(valueObj);
        }
        else
        {
            THROW_EXCEPTION("Failed to get Procedure value", "Expected a string", SPELL_ERROR_LANGUAGE);
        }
    }
	return value;
}

//=============================================================================
// METHOD: SPELLpyArgs::getModifier_Severity()
//=============================================================================
int SPELLpyArgs::getModifier_Severity() const
{
    PyObject* valueObj = (*this)[LanguageModifiers::Severity];

    int value = LanguageConstants::INFORMATION;
    if ( valueObj != NULL )
    {
        if (PyInt_Check( valueObj ) || PyLong_Check( valueObj ))
        {
        	value = PyLong_AsLong(valueObj);
        	switch(value)
        	{
        	case LanguageConstants::INFORMATION:
        	case LanguageConstants::WARNING:
        	case LanguageConstants::ERROR:
        		break;
        	default:
        		THROW_EXCEPTION("Failed to get Severity value", "Wrong severity code: " + ISTR(value), SPELL_ERROR_LANGUAGE);
        		break;
        	}
        }
        else
        {
        	THROW_EXCEPTION("Failed to get Severity value", "Expected a severity code", SPELL_ERROR_LANGUAGE);
        }
    }
	return value;
}

//=============================================================================
// METHOD: SPELLpyArgs::getModifier_Scope()
//=============================================================================
int SPELLpyArgs::getModifier_Scope() const
{
    PyObject* valueObj = (*this)[LanguageModifiers::Scope];

    int value = LanguageConstants::SCOPE_SYS;

    if ( valueObj != NULL )
    {
        if (PyInt_Check( valueObj ) || PyLong_Check( valueObj ))
        {
        	value = PyLong_AsLong(valueObj);
        	switch(value)
        	{
        	case LanguageConstants::SCOPE_PROC:
        	case LanguageConstants::SCOPE_SYS:
        	case LanguageConstants::SCOPE_CFG:
        		break;
        	default:
        		THROW_EXCEPTION("Failed to get Scope value", "Wrong scope code: " + ISTR(value), SPELL_ERROR_LANGUAGE);
        		break;
        	}
        }
        else
        {
        	THROW_EXCEPTION("Failed to get Scope value", "Expected a scope code", SPELL_ERROR_LANGUAGE);
        }
    }
	return value;
}

//=============================================================================
// METHOD: SPELLpyArgs::getModifier_Type()
//=============================================================================
int SPELLpyArgs::getModifier_Type() const
{
    PyObject* valueObj = (*this)[LanguageModifiers::Type];

    int value = 0;
    if ( valueObj != NULL )
    {
        if (PyInt_Check( valueObj ) || PyLong_Check( valueObj ))
        {
        	value = PyLong_AsLong(valueObj);
        }
        else
        {
        	THROW_EXCEPTION("Failed to get type value", "Expected an integer code", SPELL_ERROR_LANGUAGE);
        }
    }
    else
    {
    	THROW_EXCEPTION("Failed to get type value", "Could not get value", SPELL_ERROR_LANGUAGE);
    }
	return value;
}

//=============================================================================
// METHOD: SPELLpyArgs::getModifier_Default()
//=============================================================================
std::string SPELLpyArgs::getModifier_Default() const
{
    PyObject* valueObj = (*this)[LanguageModifiers::Default];

    std::string value = "";
    if ( valueObj != NULL )
    {
        if (PyString_Check( valueObj ))
        {
        	value = PYSTR(valueObj);
        }
        else if (PyInt_Check( valueObj ) || PyLong_Check( valueObj ))
        {
        	value = ISTR(PyLong_AsLong(valueObj));
        }
        else if (PyFloat_Check( valueObj ))
        {
        	char* buffer = new char[512];
        	PyFloat_AsString(buffer, (PyFloatObject*) (valueObj) );
        	value = std::string(buffer);
        	delete buffer;
        }
        else if (PyBool_Check( valueObj ))
        {
        	if (valueObj == Py_True)
        	{
        		value = "True";
        	}
        	else
        	{
        		value = "False";
        	}
        }
        else
        {
        	THROW_EXCEPTION("Failed to get default value", "Failed type detection", SPELL_ERROR_LANGUAGE);
        }
    }
    else
    {
    	THROW_EXCEPTION("Failed to get default value", "Could not get value", SPELL_ERROR_LANGUAGE);
    }
	return value;
}
