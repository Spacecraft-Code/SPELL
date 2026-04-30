// ################################################################################
// FILE       : SPELLbreakpointType.H
// DATE       : Mar 17, 2011
// PROJECT    : SPELL
// DESCRIPTION: Types of breakpoints
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

#ifndef __SPELL_BREAKPOINT_TYPE_H__
#define __SPELL_BREAKPOINT_TYPE_H__

// FILES TO INCLUDE ////////////////////////////////////////////////////////
// Local includes ----------------------------------------------------------
// Project includes --------------------------------------------------------
#include "SPELL_UTIL/SPELLbase.H"
// System includes ---------------------------------------------------------

/** \addtogroup SPELL_EXC */
/*@{*/


// FORWARD REFERENCES //////////////////////////////////////////////////////
// TYPES ///////////////////////////////////////////////////////////////////
// DEFINES /////////////////////////////////////////////////////////////////

//////////////////////////////////////////////////////////////////////////////
/**
 ** \brief Breakpoint type.
 **
**////////////////////////////////////////////////////////////////////////////
typedef enum SPELLbreakpointType_
{
	PERMANENT=0,
	TEMPORARY,
	UNKNOWN
}
SPELLbreakpointType;

//////////////////////////////////////////////////////////////////////////////
/**
 ** Get the Breakpoint type from the given string.
 ** \param stringified IN: a stringified representation of a Breakpoint type
 ** \return the Breakpoint type represented in the given string
 **/
/////////////////////////////////////////////////////////////////////////////
SPELLbreakpointType breakpointTypeFromString(const std::string& stringified);

/*@}*/
#endif
