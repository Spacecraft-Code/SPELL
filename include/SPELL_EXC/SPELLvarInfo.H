// ################################################################################
// FILE       : SPELLvarInfo.H
// DATE       : Mar 17, 2011
// PROJECT    : SPELL
// DESCRIPTION: Procedure variable information
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

#ifndef __SPELL_VAR_INFO_H__
#define __SPELL_VAR_INFO_H__

// FILES TO INCLUDE ////////////////////////////////////////////////////////
// Local includes ----------------------------------------------------------
// Project includes --------------------------------------------------------
#include "SPELL_UTIL/SPELLbase.H"

/** \addtogroup SPELL_EXC */
/*@{*/


// FORWARD REFERENCES //////////////////////////////////////////////////////
// TYPES ///////////////////////////////////////////////////////////////////
// DEFINES /////////////////////////////////////////////////////////////////

//////////////////////////////////////////////////////////////////////////////
/**
 ** \brief Variable information
 **
**////////////////////////////////////////////////////////////////////////////
typedef struct SPELLvarInfo_
{
	SPELLvarInfo_()
	{
		varName = "";
		varType = "";
		varValue = "";
		isGlobal = false;
		isDeleted = false;
		isAdded = false;
	}

	SPELLvarInfo_( const std::string& name,
				   const std::string& type,
				   const std::string& value,
				   bool global )
	{
		varName = name;
		varType = type;
		varValue = value;
		isGlobal = global;
		isDeleted = false;
		isAdded = false;
	}

	SPELLvarInfo_& operator=( const SPELLvarInfo_& other )
	{
		if (this != &other)
		{
			varName = other.varName;
			varType = other.varType;
			varValue = other.varValue;
			isGlobal = other.isGlobal;
			isDeleted = other.isDeleted;
			isAdded = other.isAdded;
		}
		return *this;
	}

	SPELLvarInfo_( const SPELLvarInfo_& other )
	{
		varName = other.varName;
		varType = other.varType;
		varValue = other.varValue;
		isGlobal = other.isGlobal;
		isDeleted = other.isDeleted;
		isAdded = other.isAdded;
	}

	/** Holds the variable name */
	std::string varName;
	/** Holds the variable type string */
	std::string varType;
	/** Holds the variable value */
	std::string varValue;
	/** True if the variable is global */
	bool isGlobal;
	/** Operation flags */
	bool isAdded;
	bool isDeleted;
}
SPELLvarInfo;

/*@}*/
#endif
