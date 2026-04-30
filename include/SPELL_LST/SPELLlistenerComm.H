// ################################################################################
// FILE       : SPELLlistenerComm.H
// DATE       : Jul 05, 2011
// PROJECT    : SPELL
// DESCRIPTION: SPELL listener library header file
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

#ifndef __SPELL_LISTENER_OBJECT_H
#define __SPELL_LISTENER_OBJECT_H

// FILES TO INCLUDE ////////////////////////////////////////////////////////
// Local includes ----------------------------------------------------------
// Project includes --------------------------------------------------------
#include "SPELL_UTIL/SPELLbase.H"
#include "SPELL_IPC/SPELLipcInterfaceListener.H"
// System includes ---------------------------------------------------------
#include <vector>
#include <map>

/** \addtogroup SPELL_LST */
/*@{*/

//
// FORWARD REFERENCES //////////////////////////////////////////////////////
// TYPES ///////////////////////////////////////////////////////////////////
// DEFINES /////////////////////////////////////////////////////////////////

#ifndef DBG_LST
#define DBG_LST 0
#endif

class SPELLlistenerComm
{
public:

	virtual ~SPELLlistenerComm(){};

    virtual SPELLipcMessage displace( const SPELLipcMessage& msg ) = 0;
    virtual void notifyClients( const SPELLipcMessage& msg ) = 0;
};

/*@}*/
#endif

