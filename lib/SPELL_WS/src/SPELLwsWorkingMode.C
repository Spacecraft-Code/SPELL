// ################################################################################
// FILE       : SPELLwsWorkingMode.C
// DATE       : Mar 17, 2011
// PROJECT    : SPELL
// DESCRIPTION: Implementation of the warmstart working mode
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
#include "SPELL_WS/SPELLwsWorkingMode.H"
// Project includes --------------------------------------------------------
// System includes ---------------------------------------------------------

std::string s_WORKING_MODE_STR[] =
{
	"ON_LINE",
	"ON_STEP",
	"ON_DEMAND",
	"DISABLED",
	"UNINIT"
};
unsigned int s_NUM_MODES = sizeof(s_WORKING_MODE_STR) / sizeof(std::string);

//=============================================================================
// METHOD    : SPELLwsListDataHandler::read()
//=============================================================================
SPELLwsWorkingMode StringToWorkingMode( const std::string& mode )
{
	unsigned int index = 0;
	for( index = 0; index< s_NUM_MODES; index++)
	{
		if (mode == s_WORKING_MODE_STR[index]) return (SPELLwsWorkingMode) index;
	}
	return MODE_UNINIT;
}

std::string WorkingModeToString( const SPELLwsWorkingMode& mode )
{
	return s_WORKING_MODE_STR[mode];
}
