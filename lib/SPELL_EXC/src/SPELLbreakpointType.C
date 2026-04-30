// ################################################################################
// FILE       : SPELLbreakpointType.C
// DATE       : Mar 17, 2011
// PROJECT    : SPELL
// DESCRIPTION: Implementation of the breakpoint types
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
// System includes ---------------------------------------------------------
// Local includes ----------------------------------------------------------
#include "SPELL_EXC/SPELLbreakpointType.H"
// Project includes --------------------------------------------------------

// GLOBALS ////////////////////////////////////////////////////////////////////
static std::string s_STRING_BREAKPOINTS[] = {"PERMANENT","TEMPORARY","UNKNOWN"};
static unsigned int s_STRING_SIZE = sizeof(s_STRING_BREAKPOINTS)/sizeof(std::string);

//=============================================================================
// FUNCTION    : breakpointTypeFromString
//=============================================================================
SPELLbreakpointType breakpointTypeFromString(const std::string& stringified)
{
	for (unsigned int i = 0; i < s_STRING_SIZE; i++)
	{
		if (s_STRING_BREAKPOINTS[i] == stringified)
		{
			return (SPELLbreakpointType)i;
		}
	}
	return UNKNOWN;
}
