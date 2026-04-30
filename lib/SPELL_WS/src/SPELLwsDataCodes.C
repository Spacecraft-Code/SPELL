// ################################################################################
// FILE       : SPELLwsDataCodes.C
// DATE       : Mar 17, 2011
// PROJECT    : SPELL
// DESCRIPTION: Data code functions
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

// FILES TO INCLUDE ////////////////////////////////////////////////////////
// Local includes ----------------------------------------------------------
#include "SPELL_WS/SPELLwsDataCodes.H"
// Project includes --------------------------------------------------------
#include "SPELL_UTIL/SPELLpythonHelper.H"
// System includes ---------------------------------------------------------


static std::string DATA_CODE_STR[] =
{
		"GENERIC",
		"LIST",
		"TUPLE",
		"DICTIONARY",
		"CLASS",
		"INSTANCE",
		"BYTECODE",
		"SPELL_DICT",
		"TIME",
		"CUSTOM_TYPE",
		"SPELL_BUILTIN",
		"TM_ITEM",
		"TC_ITEM",
		"NONE"
};

static const unsigned short NUM_CODES = sizeof(DATA_CODE_STR)/sizeof(std::string);

std::string SPELLwsData::codeStr( SPELLwsData::Code code )
{
	if (((unsigned short)code)>NUM_CODES-1)
	{
		return ISTR( (unsigned short) code ) + "?";
	}
	return DATA_CODE_STR[ (int) code ];
}

SPELLwsData::Code SPELLwsData::codeOf( PyObject* object )
{
	if (PyDict_Check(object))
	{
		return SPELLwsData::DATA_DICTIONARY;
	}
	else if (PyList_Check(object))
	{
		return SPELLwsData::DATA_LIST;
	}
	else if (PyTuple_Check(object))
	{
		return SPELLwsData::DATA_TUPLE;
	}
	else if ( Py_None == object )
	{
		return SPELLwsData::DATA_NONE;
	}
	else if (SPELLpythonHelper::instance().isDatabase(object))
	{
		return SPELLwsData::DATA_SPELL_DICT;
	}
	else if (SPELLpythonHelper::instance().isTime(object))
	{
		return SPELLwsData::DATA_SPELL_TIME;
	}
	else if ( PyClass_Check(object))
	{
		return SPELLwsData::DATA_CLASS;
	}
	else if ( PyInstance_Check(object))
	{
		return SPELLwsData::DATA_INSTANCE;
	}
	// Look discussion at mail.python.org/pipermail/python-dev/2004-July/046074.html
	else if ((object->ob_type->tp_flags & Py_TPFLAGS_HEAPTYPE)>0)
	{
		return SPELLwsData::DATA_CUSTOM_TYPE;
	}
	else if (SPELLpythonHelper::instance().isInstance(object,"TmItemClass", "spell.lib.adapter.tm_item"))
	{
		return SPELLwsData::DATA_TM_ITEM;
	}
	else if (SPELLpythonHelper::instance().isInstance(object,"TcItemClass", "spell.lib.adapter.tc_item"))
	{
		//TODO
	}

	return SPELLwsData::DATA_GENERIC;
}

