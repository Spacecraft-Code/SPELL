// ################################################################################
// FILE       : SPELLSpellService.H
// DATE       : Mar 17, 2011
// PROJECT    : SPELL
// DESCRIPTION: Implementation of the SpellService mechanism
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

#ifndef __SPELL_SpellPyServiceWatchdog_H__
#define __SPELL_SpellPyServiceWatchdog_H__

// FILES TO INCLUDE ////////////////////////////////////////////////////////
// System includes ---------------------------------------------------------
// Local includes ----------------------------------------------------------
// Project includes --------------------------------------------------------
#include "SPELL_UTIL/SPELLbase.H"

/** \addtogroup SPELL_CTX */
/*@{*/


// FORWARD REFERENCES //////////////////////////////////////////////////////
// TYPES ///////////////////////////////////////////////////////////////////

/** Python binding for the SpellService object */
typedef struct PySpellServiceWatchdogObject_
{
    PyObject_HEAD;
    PyObject *callback;
}
PySpellServiceWatchdogObject;


// DEFINES /////////////////////////////////////////////////////////////////
PyObject* SpellServiceWatchdog_Create(void (*callback)(std::string&));


/*@}*/
#endif
