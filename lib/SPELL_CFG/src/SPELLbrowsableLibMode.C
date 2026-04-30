// ################################################################################
// FILE       : SPELLbdriverConfig.C
// DATE       : Dec 15, 2014
// PROJECT    : SPELL
// DESCRIPTION:
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
#include "SPELL_CFG/SPELLbrowsableLibMode.H"
// Project includes --------------------------------------------------------


// Data
std::string s_BROWSABLELIB_MODE_STR[] =
{
	"True",
	"False",
	"Disabled"
};

// Methods


//=============================================================================
// METHOD    : SPELLbrowsableLibMod::toBrowsableLibMode()
//=============================================================================
SPELLbrowsableLibMode stringToBrowsableLibMode( const std::string& mode )
{
	unsigned int index = 0;
	unsigned int s_NUM_MODES = sizeof(s_BROWSABLELIB_MODE_STR) / sizeof(std::string);

	for( index = 0; index < s_NUM_MODES; index++)
	{
		if (mode == s_BROWSABLELIB_MODE_STR[index]) return (SPELLbrowsableLibMode) index;
	}
	return OFF;
} //toBrowsableLibMode


//=============================================================================
// METHOD    : SPELLbrowsableLibMod::toString()
//=============================================================================
std::string browsableLibModeToString( const SPELLbrowsableLibMode& mode )
{
	return s_BROWSABLELIB_MODE_STR[mode];
} //toString


