// ################################################################################
// FILE       : SPELLprocessInfo.H
// DATE       : Jul 18, 2011
// PROJECT    : SPELL
// DESCRIPTION: Process information
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

#ifndef __SPELL_PROCESS_INFO_H__
#define __SPELL_PROCESS_INFO_H__

// FILES TO INCLUDE ////////////////////////////////////////////////////////
// Local includes ----------------------------------------------------------
// Project includes --------------------------------------------------------
#include "SPELL_UTIL/SPELLbase.H"
// System includes ---------------------------------------------------------

/** \defgroup SPELL_PRC Process management (SPELL_PRC)
 *
 * Defines the process information
 *
 **/
/*@{*/


// FORWARD REFERENCES //////////////////////////////////////////////////////
// TYPES ///////////////////////////////////////////////////////////////////
// DEFINES /////////////////////////////////////////////////////////////////

//////////////////////////////////////////////////////////////////////////////
/**
 ** \brief Process information
 **
 ** \par Description and usage:
 **
 ** 	Used by the process control library
 **
**////////////////////////////////////////////////////////////////////////////
class SPELLprocessInfo
{
public:
    /** Process PID */
    pid_t                   m_processPID;
    /** Process identifier */
    std::string             m_identifier;
    /** Process start command */
    std::string             m_command;
    /** Process exit code */
    int                     m_retValue;
    /** Process status (internal) */
    int                     m_status;
    /** Process status code */
    SPELLprocessStatus		m_statusCode;
    /** True if the process shall be killed */
    bool                    m_killIt;
};

/*@}*/
#endif
