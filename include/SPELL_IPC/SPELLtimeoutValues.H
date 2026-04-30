// ################################################################################
// FILE       : SPELLtimeoutValues.H
// DATE       : Mar 17, 2011
// PROJECT    : SPELL
// DESCRIPTION: Inter process communication, timeout values
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

#ifndef __SPELL_IPC_TIMEOUT_H__
#define __SPELL_IPC_TIMEOUT_H__

// FILES TO INCLUDE ////////////////////////////////////////////////////////
// System includes ---------------------------------------------------------
// Local includes ----------------------------------------------------------
// Project includes --------------------------------------------------------


// DEFINES /////////////////////////////////////////////////////////////////

/** Timeout for the login message response */
#define IPC_EXLOGIN_DEFAULT_TIMEOUT_MSEC        10000
/** Timeout for the notification responses */
#define IPC_GUIREQUEST_DEFAULT_TIMEOUT_MSEC 20000
/** Timeout for opening a subprocedure */
#define IPC_OPENPROC_DEFAULT_TIMEOUT_MSEC     30000
/** General timeout for context requests */
#define IPC_CTXREQUEST_DEFAULT_TIMEOUT_MSEC   20000
/** General timeout for retry delay */
#define IPC_REQUEST_RETRY_DEFAULT_DELAY_MSEC  1000

/*@}*/
#endif

