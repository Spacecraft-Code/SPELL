// ################################################################################
// FILE       : SPELLconditionIF.H
// DATE       : Mar 17, 2011
// PROJECT    : SPELL
// DESCRIPTION: Condition interface definition
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

#ifndef __SPELL_CONDITION_IF_H__
#define __SPELL_CONDITION_IF_H__

// FILES TO INCLUDE ////////////////////////////////////////////////////////
// System includes ---------------------------------------------------------
// Local includes ----------------------------------------------------------
#include "SPELL_SYN/SPELLmutex.H"
// Project includes --------------------------------------------------------

/** \addtogroup SPELL_SYN */
/*@{*/


// FORWARD REFERENCES //////////////////////////////////////////////////////
// TYPES ///////////////////////////////////////////////////////////////////
// DEFINES /////////////////////////////////////////////////////////////////

//////////////////////////////////////////////////////////////////////////////
/**
 ** \brief Interface for condition implementations
 **
**////////////////////////////////////////////////////////////////////////////
class SPELLconditionIF
{
public: //--------------------------------------------------------------------

    // EXCEPTIONS ////////////////////////////////////////////////////////////
    // ENUMS /////////////////////////////////////////////////////////////////
    // TYPES /////////////////////////////////////////////////////////////////
    // LIFECYCLE /////////////////////////////////////////////////////////////

		//////////////////////////////////////////////////////////////////////
		/** Destructor.
		**////////////////////////////////////////////////////////////////////
		virtual ~SPELLconditionIF() {;};

    // STATIC ////////////////////////////////////////////////////////////////
    // METHODS ///////////////////////////////////////////////////////////////

		//////////////////////////////////////////////////////////////////////
		/** Signal any waiting thread
		**////////////////////////////////////////////////////////////////////
		virtual void signal() = 0;

		//////////////////////////////////////////////////////////////////////
		/** Wait for the condition to be signalled
		 *
		 * \param m INOUT: Mutex to be used in condition lock
		**////////////////////////////////////////////////////////////////////
		virtual void wait( SPELLmutex* m ) = 0;

		//////////////////////////////////////////////////////////////////////
		/** Wait for the condition to be signalled with a timeout
		 *
		 * \param m INOUT: Mutex to be used in condition lock
		 * \param timeout IN: timeout in milliseconds
		**////////////////////////////////////////////////////////////////////
		virtual bool wait( SPELLmutex* m, unsigned long timeoutMsec ) = 0;
};

/*@}*/
#endif
