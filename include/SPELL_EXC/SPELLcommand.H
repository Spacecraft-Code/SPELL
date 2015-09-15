// ################################################################################
// FILE       : SPELLcommand.H
// DATE       : Mar 17, 2011
// PROJECT    : SPELL
// DESCRIPTION: Executor command definition
// --------------------------------------------------------------------------------
//
//  Copyright (C) 2008, 2015 SES ENGINEERING, Luxembourg S.A.R.L.
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

#ifndef __SPELL_COMMAND_H__
#define __SPELL_COMMAND_H__

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
 * Executor command structure.
 */
typedef struct ExecutorCommand_
{
	/** Identifier of the command */
    std::string id;
    /** Command argument */
    std::string arg;
    /** Extra argument, if any */
    std::string earg;
}
ExecutorCommand;

/** Identifier for the STOP command */
#define CMD_STOP        "CMD_STOP"
/** Identifier for the ABORT command */
#define CMD_ABORT       "CMD_ABORT"
/** Identifier for the STEP command */
#define CMD_STEP        "CMD_STEP"
/** Identifier for the STEP OVER command */
#define CMD_STEP_OVER   "CMD_STEP_OVER"
/** Identifier for the RUN command */
#define CMD_RUN         "CMD_RUN"
/** Identifier for the SKIP command */
#define CMD_SKIP        "CMD_SKIP"
/** Identifier for the GOTO command */
#define CMD_GOTO        "CMD_GOTO"
/** Identifier for the PAUSE command */
#define CMD_PAUSE       "CMD_PAUSE"
/** Identifier for the INTERRUPT command */
#define CMD_INTERRUPT   "CMD_INTERRUPT"
/** Identifier for the SCRIPT command */
#define CMD_SCRIPT      "CMD_SCRIPT"
/** Identifier for the RELOAD command */
#define CMD_RELOAD      "CMD_RELOAD"
/** Identifier for the RECOVER command */
#define CMD_RECOVER     "CMD_RECOVER"
/** Identifier for the CLOSE command */
#define CMD_CLOSE       "CMD_CLOSE"
/** Identifier for the BLOCK command */
#define CMD_BLOCK       "CMD_BLOCK"
/** Identifier for the ACTION command */
#define CMD_ACTION      "CMD_ACTION"
/** Identifier for the FINISH command */
#define CMD_FINISH      "CMD_FINISH"

/*@}*/
#endif
