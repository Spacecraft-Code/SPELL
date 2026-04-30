// ################################################################################
// FILE       : SPELLgoto.H
// DATE       : Mar 17, 2011
// PROJECT    : SPELL
// DESCRIPTION: Implementation of the Goto mechanism
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

#ifndef __SPELL_GOTO_H__
#define __SPELL_GOTO_H__

// FILES TO INCLUDE ////////////////////////////////////////////////////////
// System includes ---------------------------------------------------------
// Local includes ----------------------------------------------------------
// Project includes --------------------------------------------------------
#include "SPELL_UTIL/SPELLbase.H"

/** \addtogroup SPELL_EXC */
/*@{*/


// FORWARD REFERENCES //////////////////////////////////////////////////////
// TYPES ///////////////////////////////////////////////////////////////////

/** Python binding for the Goto object */
typedef struct PyGotoObject_
{
    PyObject_HEAD;
}
PyGotoObject;

/** Python binding for the Step object */
typedef struct PyStepObject_
{
    PyObject_HEAD;
}
PyStepObject;

/** Install the Goto binding on the Python layer */
void Goto_Install();
/** Install the Step binding on the Python layer */
void Step_Install();

// DEFINES /////////////////////////////////////////////////////////////////

//////////////////////////////////////////////////////////////////////////////
/**
 ** \brief Goto mechanism
 **
 ** \par Description and usage:
 **
 ** 	This class parses a given piece of bytecode searching
 **  	for Goto and Step function calls. It is used by the executor to
 **  	provide the programmed go-to mechanism.
 **
 ** 	Instantiated once per file in the frame manager.
 **
**////////////////////////////////////////////////////////////////////////////
class SPELLgoto
{
public: //--------------------------------------------------------------------

    // EXCEPTIONS ////////////////////////////////////////////////////////////
    // ENUMS /////////////////////////////////////////////////////////////////
    // TYPES /////////////////////////////////////////////////////////////////
    // LIFECYCLE /////////////////////////////////////////////////////////////

		//////////////////////////////////////////////////////////////////////
		/** Constructor.
		 *
		 * \param code IN: the bytecode of the corresponding script
		**////////////////////////////////////////////////////////////////////
		SPELLgoto( PyCodeObject* code );

		//////////////////////////////////////////////////////////////////////
		/** Destructor.
		**////////////////////////////////////////////////////////////////////
		~SPELLgoto();

    // STATIC ////////////////////////////////////////////////////////////////
    // METHODS ///////////////////////////////////////////////////////////////

		//////////////////////////////////////////////////////////////////////
		/** Obtain the map of labels found in the given bytecode.
		 *
		 * \returns The map of label-line number pairs.
		**////////////////////////////////////////////////////////////////////
		const std::map<std::string, unsigned int> getLabels() const {
			return m_labels;
		};

		//////////////////////////////////////////////////////////////////////
		/** Get the script line target of the goto appearing in the given
		 *  bytecode line.
		 *
		 * \returns The target line of the goto, -1 if no goto is found
		 *  in the given line.
		**////////////////////////////////////////////////////////////////////
		const int getTargetLine( const unsigned int frameLine );

		//////////////////////////////////////////////////////////////////////
		/** Check if there is a Step() call in the given source code line.
		 *
		 * \returns True if there is a label (Step) in the line.
		**////////////////////////////////////////////////////////////////////
		const bool isLabel( const unsigned int frameLine );

		//////////////////////////////////////////////////////////////////////
		/** Check if there is a Step(INIT) call in the given source code line.
		 *
		 * \returns True if there is an INIT Step in the line.
		**////////////////////////////////////////////////////////////////////
		const bool isInitStep( const unsigned int frameLine );

		//////////////////////////////////////////////////////////////////////
		/** Check if there is a Step(INIT) call in the given source code.
		 *
		 * \returns True if there is an INIT Step.
		**////////////////////////////////////////////////////////////////////
		const bool hasInitStep() const {
			return (m_initLine != -1);
		};

		//////////////////////////////////////////////////////////////////////
		/** Check parsing
		 *
		 * \return Parsing error with a missing label list, if any.
		**////////////////////////////////////////////////////////////////////
		std::string validateGotos() const;

    // DATA MEMBERS //////////////////////////////////////////////////////////

protected: //-----------------------------------------------------------------

    // EXCEPTIONS ////////////////////////////////////////////////////////////
    // ENUMS /////////////////////////////////////////////////////////////////
    // TYPES /////////////////////////////////////////////////////////////////
    // LIFECYCLE /////////////////////////////////////////////////////////////
    // METHODS ///////////////////////////////////////////////////////////////
    // DATA MEMBERS //////////////////////////////////////////////////////////

private: //-------------------------------------------------------------------

    // EXCEPTIONS ////////////////////////////////////////////////////////////
    // ENUMS /////////////////////////////////////////////////////////////////
    // TYPES /////////////////////////////////////////////////////////////////

		/** Represents a map of labels and line numbers */
		typedef std::map<std::string,unsigned int> LabelMap;

		/** Represents a map of label identifiers and titles */
		typedef std::map<std::string,std::string> TitleMap;

		/** Represents a map of line numbers and target labels */
		typedef std::map<unsigned int,std::string> GotoMap;

    // LIFECYCLE /////////////////////////////////////////////////////////////
    // METHODS ///////////////////////////////////////////////////////////////

		//////////////////////////////////////////////////////////////////////
		/** Analyze the bytecode and populate all information maps.
		**////////////////////////////////////////////////////////////////////
		void analyze();

    // DATA MEMBERS //////////////////////////////////////////////////////////
		/** Reference to the code object (shared) */
		PyCodeObject*  m_code;
		/** Label lines (speed up label line detection) */
		std::set<int>  m_labelLines;
		/** Label information */
		LabelMap       m_labels;
		/** Label titles */
		TitleMap       m_titles;
		/** Goto calls (origin line to target label) */
		GotoMap        m_gotos;
		/** Contains the lineno if the bytecode contains an INIT step, otherwise -1 */
		int            m_initLine;
};

/*@}*/
#endif
