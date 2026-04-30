// ################################################################################
// FILE       : SPELLxmlConfigReaderFactory.H
// DATE       : Mar 17, 2011
// PROJECT    : SPELL
// DESCRIPTION: Factory for XML configuration readers
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

#ifndef __SPELL_XMLCONFIG_READER_FACTORY_H__
#define __SPELL_XMLCONFIG_READER_FACTORY_H__

// FILES TO INCLUDE ////////////////////////////////////////////////////////
// System includes ---------------------------------------------------------
// Local includes ----------------------------------------------------------
#include "SPELL_CFG/SPELLxmlConfigReader.H"
// Project includes --------------------------------------------------------

/** \addtogroup SPELL_CFG */
/*@{*/


// FORWARD REFERENCES //////////////////////////////////////////////////////
// TYPES ///////////////////////////////////////////////////////////////////
// DEFINES /////////////////////////////////////////////////////////////////

//////////////////////////////////////////////////////////////////////////////
/**
 * \brief Factory for XML config readers.
 *
 * \par Description and usage:
 *
 * 		The factory is used by the SPELLconfiguration singleton in order
 * 		to create an instance of an XML reader to parse the SPELL configuration
 * 		file.
 *
 * \see SPELLxmlConfigReader
 */
//////////////////////////////////////////////////////////////////////////////
class SPELLxmlConfigReaderFactory
{
public: //--------------------------------------------------------------------

    // EXCEPTIONS ////////////////////////////////////////////////////////////
    // ENUMS /////////////////////////////////////////////////////////////////
    // TYPES /////////////////////////////////////////////////////////////////
    // LIFECYCLE /////////////////////////////////////////////////////////////
    // STATIC ////////////////////////////////////////////////////////////////

		//////////////////////////////////////////////////////////////////////
		/** Create an instance of XML configuration reader.
		 *
		 * \return The instance of the reader.
		**////////////////////////////////////////////////////////////////////
		static SPELLxmlConfigReader* createXMLConfigReader();

    // METHODS ///////////////////////////////////////////////////////////////
    // DATA MEMBERS //////////////////////////////////////////////////////////
};

/*@}*/
#endif
