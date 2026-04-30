// ################################################################################
// FILE       : SPELLwsWorkingMode.H
// DATE       : Mar 17, 2011
// PROJECT    : SPELL
// DESCRIPTION: Definition of WS working modes
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

#ifndef __SPELL_WS_WORKING_MODE_H__
#define __SPELL_WS_WORKING_MODE_H__

// FILES TO INCLUDE ////////////////////////////////////////////////////////
// System includes ---------------------------------------------------------
// Local includes ----------------------------------------------------------
#include "SPELL_UTIL/SPELLbase.H"
// Project includes --------------------------------------------------------

/** \addtogroup SPELL_WS */
/*@{*/

// FORWARD REFERENCES //////////////////////////////////////////////////////
// ENUMS ///////////////////////////////////////////////////////////////////
// TYPES ///////////////////////////////////////////////////////////////////
// DEFINES /////////////////////////////////////////////////////////////////

//////////////////////////////////////////////////////////////////////////////
/**
 ** \brief Different working modes of the warmstart mechanism.
 **
**////////////////////////////////////////////////////////////////////////////
typedef enum SPELLwsWorkingMode_
{
	MODE_ON_LINE = 0,
	MODE_ON_STEP,
	MODE_ON_DEMAND,
	MODE_DISABLED,
	MODE_UNINIT
}
SPELLwsWorkingMode;

SPELLwsWorkingMode StringToWorkingMode( const std::string& mode );
std::string WorkingModeToString( const SPELLwsWorkingMode& mode );

/*@}*/
#endif
