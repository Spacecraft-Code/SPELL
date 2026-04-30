// ################################################################################
// FILE       : SPELLwsDataCodes.H
// DATE       : Mar 17, 2011
// PROJECT    : SPELL
// DESCRIPTION: WS data types
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

#ifndef __SPELL_WS_DATA_CODES_H__
#define __SPELL_WS_DATA_CODES_H__

// FILES TO INCLUDE ////////////////////////////////////////////////////////
// Local includes ----------------------------------------------------------
// Project includes --------------------------------------------------------
#include "SPELL_UTIL/SPELLbase.H"
// System includes ---------------------------------------------------------

/** \addtogroup SPELL_WS */
/*@{*/

// FORWARD REFERENCES //////////////////////////////////////////////////////
// ENUMS ///////////////////////////////////////////////////////////////////
// TYPES ///////////////////////////////////////////////////////////////////
// DEFINES /////////////////////////////////////////////////////////////////

//////////////////////////////////////////////////////////////////////////////
/**
 ** \brief Codes identifying different data types
 **
 ** \par Description and usage:
 **
 **		Used by data handlers to identify the marshalled data in the
 **		warmstart data files.
 **
**////////////////////////////////////////////////////////////////////////////

namespace SPELLwsData
{

typedef enum SPELLwsDataCode_
{
	DATA_GENERIC     = 0,
	DATA_LIST,
	DATA_TUPLE,
	DATA_DICTIONARY,
	DATA_CLASS,
	DATA_INSTANCE,
	DATA_BYTECODE,
	DATA_SPELL_DICT,
	DATA_SPELL_TIME,
	DATA_CUSTOM_TYPE,
	DATA_SPELL_BUILTIN,
	DATA_TM_ITEM,
	DATA_TC_ITEM,
	DATA_NONE
}
Code;

std::string codeStr( Code code );
SPELLwsData::Code codeOf( PyObject* object );

};

/*@}*/
#endif
