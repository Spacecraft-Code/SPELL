// ################################################################################
// FILE       : SPELLxmlConfigReader.H
// DATE       : Mar 17, 2011
// PROJECT    : SPELL
// DESCRIPTION: Wrapper for the XML configuration reader
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

#ifndef __SPELL_XMLCONFIG_READER_H__
#define __SPELL_XMLCONFIG_READER_H__

// FILES TO INCLUDE ////////////////////////////////////////////////////////
// System includes ---------------------------------------------------------
// Local includes ----------------------------------------------------------
#include "SPELL_CFG/SPELLxmlNode.H"
// Project includes --------------------------------------------------------

/** \addtogroup SPELL_CFG */
/*@{*/


// FORWARD REFERENCES //////////////////////////////////////////////////////
// TYPES ///////////////////////////////////////////////////////////////////
// DEFINES /////////////////////////////////////////////////////////////////

//////////////////////////////////////////////////////////////////////////////
/**
 * Wrapper for the XML config reader.
 *
 * \par Description and usage:
 *
 * 		Interface for all XML configuration readers. Must be used by
 * 		by concrete implementations of XML readers.
 */
//////////////////////////////////////////////////////////////////////////////
class SPELLxmlConfigReader
{
public: //--------------------------------------------------------------------

    // EXCEPTIONS ////////////////////////////////////////////////////////////
    // ENUMS /////////////////////////////////////////////////////////////////
    // TYPES /////////////////////////////////////////////////////////////////
    // LIFECYCLE /////////////////////////////////////////////////////////////

		//////////////////////////////////////////////////////////////////////
		/** Constructor
		**////////////////////////////////////////////////////////////////////
		SPELLxmlConfigReader() {};

		//////////////////////////////////////////////////////////////////////
		/** Destructor
		**////////////////////////////////////////////////////////////////////
		virtual ~SPELLxmlConfigReader() {};

    // STATIC ////////////////////////////////////////////////////////////////
    // METHODS ///////////////////////////////////////////////////////////////

		//////////////////////////////////////////////////////////////////////
		/** Parse the given XML configuration file.
		 *
		 * \returns True if success.
		**////////////////////////////////////////////////////////////////////
		virtual bool parseFile( std::string xmlFile ) = 0;

		//////////////////////////////////////////////////////////////////////
		/** Obtain the root node of the XML configuration document.
		 *
		 * \returns The root node if any.
		**////////////////////////////////////////////////////////////////////
		virtual SPELLxmlNode* getRoot() = 0;

		//////////////////////////////////////////////////////////////////////
		/** Give the list of all XML elements whose name is the given one.
		 *
		 * \param tagName IN: the name of the element
		 *
		 * \returns A list of XML nodes satisfying the name condition.
		**////////////////////////////////////////////////////////////////////
		virtual SPELLxmlNodeList findElementsByName( std::string tagName ) = 0;
};

/*@}*/
#endif
