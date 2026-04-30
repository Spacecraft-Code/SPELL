// ################################################################################
// FILE       : SPELLbrowsableLibMode.H
// DATE       : Dec 12, 2014
// PROJECT    : SPELL
// DESCRIPTION: Browsable modes
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

#ifndef __SPELL_BROWSABLELIB_MODE_H__
#define __SPELL_BROWSABLELIB_MODE_H__

// FILES TO INCLUDE //////////////////////////////////////////////////////////
// Local includes ------------------------------------------------------------
// Project includes ----------------------------------------------------------
#include "SPELL_UTIL/SPELLbase.H"
#include "SPELL_WRP/SPELLconstants.H"

/** \addtogroup SPELL_CFG */
/*@{*/


// FORWARD REFERENCES ////////////////////////////////////////////////////////
// TYPES /////////////////////////////////////////////////////////////////////
// DEFINES ///////////////////////////////////////////////////////////////////

//////////////////////////////////////////////////////////////////////////////
/**
 ** \brief
 **
**////////////////////////////////////////////////////////////////////////////
typedef enum SPELLbrowsableLibMode_
{
	ON,
	OFF,
	DISABLED
}
SPELLbrowsableLibMode;

SPELLbrowsableLibMode stringToBrowsableLibMode( const std::string& mode );
std::string browsableLibModeToString( const SPELLbrowsableLibMode& mode );


/*@}*/
#endif
