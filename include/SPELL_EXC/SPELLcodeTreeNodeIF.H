// ################################################################################
// FILE       : SPELLcodeTreeNodeIF.H
// DATE       : Mar 17, 2011
// PROJECT    : SPELL
// DESCRIPTION: Code tree node interface
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

#ifndef __SPELL_CODE_TREE_NODE_IF_H__
#define __SPELL_CODE_TREE_NODE_IF_H__

// FILES TO INCLUDE ////////////////////////////////////////////////////////
// System includes ---------------------------------------------------------
// Local includes ----------------------------------------------------------
#include "SPELL_EXC/SPELLcodeTreeLineIF.H"
// Project includes --------------------------------------------------------
#include "SPELL_UTIL/SPELLbase.H"

/** \addtogroup SPELL_EXC */
/*@{*/


// FORWARD REFERENCES //////////////////////////////////////////////////////
// TYPES ///////////////////////////////////////////////////////////////////
// DEFINES /////////////////////////////////////////////////////////////////

//////////////////////////////////////////////////////////////////////////////
/**
 ** \brief Executed node data interface
 **
 ** \par Description and usage:
 **
 ** 	Holds information about executed subprocedures.
 **
**////////////////////////////////////////////////////////////////////////////
class SPELLcodeTreeNodeIF
{
public: //--------------------------------------------------------------------

    // EXCEPTIONS ////////////////////////////////////////////////////////////
    // ENUMS /////////////////////////////////////////////////////////////////
    // TYPES /////////////////////////////////////////////////////////////////
    // LIFECYCLE /////////////////////////////////////////////////////////////

		//////////////////////////////////////////////////////////////////////
		/** Destructor.
		**////////////////////////////////////////////////////////////////////
		virtual ~SPELLcodeTreeNodeIF() {;};

    // STATIC ////////////////////////////////////////////////////////////////
    // METHODS ///////////////////////////////////////////////////////////////

		//////////////////////////////////////////////////////////////////////
		/** Notify about a call in this code, that will lead to a subprocedure.
		 *
		 * \param codeId IN: the identifier of the child procedure code.
		 * \param lineNo IN: the line number where the call occurred.
		 *
		**////////////////////////////////////////////////////////////////////
		virtual void eventCall( const std::string& codeId, unsigned int lineNo ) = 0;

		//////////////////////////////////////////////////////////////////////
		/** Notify about the visitation of the given line in this code.
		 *
		 * \param lineNo IN: the line number.
		 *
		**////////////////////////////////////////////////////////////////////
		virtual void eventLine( unsigned int lineNo ) = 0;

		//////////////////////////////////////////////////////////////////////
		/** Reset the model.
		**////////////////////////////////////////////////////////////////////
		virtual void reset() = 0;

		//////////////////////////////////////////////////////////////////////
		/** Get depth in the call stack
		 *
		 * \return The depth in the call stack
		**////////////////////////////////////////////////////////////////////
		virtual unsigned int getDepth() = 0;

		//////////////////////////////////////////////////////////////////////
		/** Obtain the parent line if any, or NULL.
		 *
		 * \return The parent line or NULL.
		**////////////////////////////////////////////////////////////////////
		virtual const SPELLcodeTreeLineIF* getParent() const = 0;

		//////////////////////////////////////////////////////////////////////
		/** Obtain a child line.
		 *
		 * \param lineNo IN: index number of the line to obtain.
		 *
		 * \return The required line.
		**////////////////////////////////////////////////////////////////////
		virtual SPELLcodeTreeLineIF* getLine( unsigned int lineNo ) const = 0;

		//////////////////////////////////////////////////////////////////////
		/** Obtain the current child line.
		 *
		 * \return The current line.
		**////////////////////////////////////////////////////////////////////
		virtual SPELLcodeTreeLineIF* getCurrentLine() const = 0;

		//////////////////////////////////////////////////////////////////////
		/** Obtain the code identifier string.
		 *
		 * \return The code identifier.
		**////////////////////////////////////////////////////////////////////
		virtual std::string getCodeIdentifier() const = 0;
};

/*@}*/
#endif
