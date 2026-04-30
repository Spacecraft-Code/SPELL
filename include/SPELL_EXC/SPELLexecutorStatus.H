// ################################################################################
// FILE       : SPELLexecutorStatus.H
// DATE       : Mar 17, 2011
// PROJECT    : SPELL
// DESCRIPTION: Executor status definition
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

#ifndef __SPELL_EXEC_STATUS_H__
#define __SPELL_EXEC_STATUS_H__

// FILES TO INCLUDE ////////////////////////////////////////////////////////
// System includes ---------------------------------------------------------
// Local includes ----------------------------------------------------------
// Project includes --------------------------------------------------------
#include "SPELL_UTIL/SPELLbase.H"

/** \addtogroup SPELL_EXC */
/*@{*/


// FORWARD REFERENCES //////////////////////////////////////////////////////
// TYPES ///////////////////////////////////////////////////////////////////

/**
 * Executor status codes
 */
typedef enum SPELLexecutorStatus_
{
    STATUS_UNINIT = 0,
    STATUS_LOADED,
    STATUS_PAUSED,
    STATUS_RUNNING,
    STATUS_FINISHED,
    STATUS_ABORTED,
    STATUS_ERROR,
    STATUS_WAITING,
    STATUS_PROMPT,
    STATUS_INTERRUPTED,
    STATUS_RELOADING,
    STATUS_UNKNOWN
}
SPELLexecutorStatus;

/*@}*/
#endif
