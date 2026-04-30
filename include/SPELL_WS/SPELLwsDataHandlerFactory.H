// ################################################################################
// FILE       : SPELLwsDataHandlerFactory.H
// DATE       : Mar 17, 2011
// PROJECT    : SPELL
// DESCRIPTION: Factory for WS data handlers
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

#ifndef __SPELL_WS_DATAHANDLER_FACTORY_H__
#define __SPELL_WS_DATAHANDLER_FACTORY_H__

// FILES TO INCLUDE ////////////////////////////////////////////////////////
// Local includes ----------------------------------------------------------
#include "SPELL_WS/SPELLwsDataHandler.H"
#include "SPELL_WS/SPELLwsDataCodes.H"
// Project includes --------------------------------------------------------
// System includes ---------------------------------------------------------

/** \addtogroup SPELL_WS */
/*@{*/

// FORWARD REFERENCES //////////////////////////////////////////////////////
// ENUMS ///////////////////////////////////////////////////////////////////
// TYPES ///////////////////////////////////////////////////////////////////
// DEFINES /////////////////////////////////////////////////////////////////

//////////////////////////////////////////////////////////////////////////////
/**
 ** \brief Factory for data handlers
 **
 ** \par Description and usage:
 **
 **		Creates the appropriate handler depending on the type of the
 **		given Python object.
 **
**////////////////////////////////////////////////////////////////////////////
class SPELLwsDataHandlerFactory
{
public: //--------------------------------------------------------------------

    // EXCEPTIONS ////////////////////////////////////////////////////////////
    // ENUMS /////////////////////////////////////////////////////////////////
    // TYPES /////////////////////////////////////////////////////////////////
    // LIFECYCLE /////////////////////////////////////////////////////////////

		//////////////////////////////////////////////////////////////////////
		/** Destructor.
		**////////////////////////////////////////////////////////////////////
		virtual ~SPELLwsDataHandlerFactory() {;};

	// STATIC ////////////////////////////////////////////////////////////////

		//////////////////////////////////////////////////////////////////////
		/** Create the appropriate handler.
		 *
		 * \param object IN: Python object to be used by the handler.
		 *
		 * \throws SPELLcoreException if no suitable handler is found.
		 *
		 * \return Handler for the given object.
		**////////////////////////////////////////////////////////////////////
		static SPELLwsDataHandler* createDataHandler( PyObject* object );

		//////////////////////////////////////////////////////////////////////
		/** Create the appropriate handler.
		 *
		 * \param code IN: data code to be used
		 *
		 * \throws SPELLcoreException if no suitable handler is found.
		 *
		 * \return Handler for the given code.
		**////////////////////////////////////////////////////////////////////
		static SPELLwsDataHandler* createDataHandler( SPELLwsData::Code code );

    // METHODS ///////////////////////////////////////////////////////////////
};

/*@}*/
#endif
