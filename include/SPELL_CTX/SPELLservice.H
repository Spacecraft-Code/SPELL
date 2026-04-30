// ################################################################################
// FILE       : SPELLservice.H
// DATE       : Apr 11, 2011
// PROJECT    : SPELL
// DESCRIPTION: Executor manager and controller
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

#ifndef __SPELL_SERVICE_H__
#define __SPELL_SERVICE_H__

// FILES TO INCLUDE //////////////////////////////////////////////////////////
// Local includes ------------------------------------------------------------
#include "SPELL_CTX/SPELLserviceListener.H"
#include "SPELL_CTX/SPELLserviceStartupParams.H"
#include "SPELL_CTX/SPELLclient.H"
#include "SPELL_CTX/SPELLexecutor.H"
// Project includes ----------------------------------------------------------
#include "SPELL_PRC/SPELLprocessManager.H"
#include "SPELL_EXC/SPELLcommand.H"
#include "SPELL_EXC/SPELLexecutorStatus.H"
#include "SPELL_SYN/SPELLevent.H"
#include "SPELL_SYN/SPELLmutex.H"

/** \addtogroup SPELL_CTX */
/*@{*/


// FORWARD REFERENCES ////////////////////////////////////////////////////////

// TYPES /////////////////////////////////////////////////////////////////////
// DEFINES ///////////////////////////////////////////////////////////////////

//////////////////////////////////////////////////////////////////////////////
/**
 ** \brief
 **
**////////////////////////////////////////////////////////////////////////////
class SPELLservice : public SPELLexecutor
{
public: //--------------------------------------------------------------------

	// EXCEPTIONS ////////////////////////////////////////////////////////////
	// ENUMS /////////////////////////////////////////////////////////////////
	// TYPES /////////////////////////////////////////////////////////////////
	// LIFECYCLE /////////////////////////////////////////////////////////////

		//////////////////////////////////////////////////////////////////////
		/** Constructor.
		**////////////////////////////////////////////////////////////////////
		SPELLservice( const SPELLserviceStartupParams& config );

		//////////////////////////////////////////////////////////////////////
		/** Destructor.
		**////////////////////////////////////////////////////////////////////
		virtual ~SPELLservice();

	// METHODS ///////////////////////////////////////////////////////////////

	    //////////////////////////////////////////////////////////////////////
	    /** Start the executor process
	    **////////////////////////////////////////////////////////////////////
	    void start();



};


/*@}*/
#endif
