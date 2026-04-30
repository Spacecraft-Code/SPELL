// ################################################################################
// FILE       : SPELLcodeTreeLineIF.H
// DATE       : Mar 17, 2011
// PROJECT    : SPELL
// DESCRIPTION: Code tree line interface definition
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

#ifndef __SPELL_CODE_TREE_LINE_IF_H__
#define __SPELL_CODE_TREE_LINE_IF_H__

// FILES TO INCLUDE ////////////////////////////////////////////////////////
// System includes ---------------------------------------------------------
// Local includes ----------------------------------------------------------
// Project includes --------------------------------------------------------
#include "SPELL_UTIL/SPELLbase.H"

/** \addtogroup SPELL_EXC */
/*@{*/


// FORWARD REFERENCES //////////////////////////////////////////////////////
class SPELLcodeTreeNodeIF;
// TYPES ///////////////////////////////////////////////////////////////////
// DEFINES /////////////////////////////////////////////////////////////////

//////////////////////////////////////////////////////////////////////////////
/**
 ** \brief Executed line data interface
 **
 ** \par Description and usage:
 **
 ** 	Holds information about executed procedure lines.
 **
**////////////////////////////////////////////////////////////////////////////
class SPELLcodeTreeLineIF
{
public: //--------------------------------------------------------------------

    // EXCEPTIONS ////////////////////////////////////////////////////////////
    // ENUMS /////////////////////////////////////////////////////////////////
    // TYPES /////////////////////////////////////////////////////////////////

		/** List of line information */
		typedef std::vector<SPELLcodeTreeLineIF*> List;
		typedef std::map<unsigned int,SPELLcodeTreeLineIF*> Map;

    // LIFECYCLE /////////////////////////////////////////////////////////////

		//////////////////////////////////////////////////////////////////////
		/** Destructor.
		**////////////////////////////////////////////////////////////////////
		virtual ~SPELLcodeTreeLineIF() {;};

    // STATIC ////////////////////////////////////////////////////////////////
    // METHODS ///////////////////////////////////////////////////////////////

		//////////////////////////////////////////////////////////////////////
		/** Get the line number.
		 *
		 * \return The line number.
		**////////////////////////////////////////////////////////////////////
		virtual unsigned int getLineNo() const = 0;

		//////////////////////////////////////////////////////////////////////
		/** Get parent code.
		 *
		 * \return The parent code.
		**////////////////////////////////////////////////////////////////////
		virtual SPELLcodeTreeNodeIF* getParentCode() const = 0;

		//////////////////////////////////////////////////////////////////////
		/** Add a child code.
		 *
		 * \param child IN: the child code.
		**////////////////////////////////////////////////////////////////////
		virtual void addChildCode( SPELLcodeTreeNodeIF* child ) = 0;

		//////////////////////////////////////////////////////////////////////
		/** Get the child code if any.
		 *
		 * \return the child code.
		**////////////////////////////////////////////////////////////////////
		virtual SPELLcodeTreeNodeIF* getChildCode() const = 0;

		//////////////////////////////////////////////////////////////////////
		/** Reset the line model.
		**////////////////////////////////////////////////////////////////////
		virtual void reset() = 0;
};

/*@}*/
#endif
