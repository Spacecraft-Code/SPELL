// ################################################################################
// FILE       : SPELLstepOverMode.H
// DATE       : Mar 17, 2011
// PROJECT    : SPELL
// DESCRIPTION: Defines the step over modes
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

#ifndef __SPELL_SO_MODE_H__
#define __SPELL_SO_MODE_H__

// FILES TO INCLUDE ////////////////////////////////////////////////////////
// System includes ---------------------------------------------------------
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
 ** \brief Modes of step over during procedure execution
 **
**////////////////////////////////////////////////////////////////////////////
typedef enum SPELLstepOverMode_
{
	/** Step over the next function call only. Used in permanent step into with a step over command */
	SO_ONCE_OVER   = 0,
	/** Step over all subsequent function calls (permanent step over)*/
	SO_ALWAYS_OVER = 1,
	/** Step over always, but not the next call. Used in permanent step over with a step into command. */
	SO_ONCE_INTO   = 2,
	/** Do not step over (permanent step into) */
	SO_ALWAYS_INTO = 3
}
SPELLstepOverMode;

/*@}*/
#endif
