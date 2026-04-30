// ################################################################################
// FILE       : SPELLbreakpoint.H
// DATE       : Mar 17, 2011
// PROJECT    : SPELL
// DESCRIPTION: Management of breakpoints in the current procedure
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

#ifndef __SPELL_BREAKPOINT_H__
#define __SPELL_BREAKPOINT_H__

// FILES TO INCLUDE ////////////////////////////////////////////////////////
// Local includes ----------------------------------------------------------
#include "SPELL_EXC/SPELLbreakpointType.H"
// Project includes --------------------------------------------------------
#include "SPELL_UTIL/SPELLbase.H"
// System includes ---------------------------------------------------------

/** \addtogroup SPELL_EXC */
/*@{*/


// FORWARD REFERENCES //////////////////////////////////////////////////////
// TYPES ///////////////////////////////////////////////////////////////////
// DEFINES /////////////////////////////////////////////////////////////////

//////////////////////////////////////////////////////////////////////////////
/**
 ** \brief Breakpoint manager.
 **
**////////////////////////////////////////////////////////////////////////////
class SPELLbreakpoint
{
public: //--------------------------------------------------------------------

    // EXCEPTIONS ////////////////////////////////////////////////////////////
    // ENUMS /////////////////////////////////////////////////////////////////
    // TYPES /////////////////////////////////////////////////////////////////
    // LIFECYCLE /////////////////////////////////////////////////////////////

		//////////////////////////////////////////////////////////////////////
		/** Constructor.
		**////////////////////////////////////////////////////////////////////
		SPELLbreakpoint();

		//////////////////////////////////////////////////////////////////////
		/** Destructor.
		**////////////////////////////////////////////////////////////////////
		~SPELLbreakpoint();

    // METHODS ///////////////////////////////////////////////////////////////

		//////////////////////////////////////////////////////////////////////
		/** Toggle a breakpoint on the given line.
		 *
		 * \param file IN: file where the breakpoint is to be set/unset.
		 * \param line IN: line where the breakpoint is to be set/unset.
		 * \param type IN: the breakpoint type
		 *
		 * \returns True on success.
		**////////////////////////////////////////////////////////////////////
		bool setBreakpoint( const std::string& file,
				            unsigned int line,
				            const SPELLbreakpointType breakpoint );

		//////////////////////////////////////////////////////////////////////
		/** Breakpoint checker.
		 *
		 * \param file IN: current file name.
		 * \param line IN: the current line in the procedure.
		 *
		 * \returns true if the line contains a breakpoint.
		**////////////////////////////////////////////////////////////////////
		bool checkBreakpoint( const std::string& file, unsigned int line );

		//////////////////////////////////////////////////////////////////////
		/** Remove all breakpoints.
		**////////////////////////////////////////////////////////////////////
		void clearBreakpoints();

protected: //-------------------------------------------------------------------

    // EXCEPTIONS ////////////////////////////////////////////////////////////
    // ENUMS /////////////////////////////////////////////////////////////////
    // TYPES /////////////////////////////////////////////////////////////////
    // LIFECYCLE /////////////////////////////////////////////////////////////
    // METHODS ///////////////////////////////////////////////////////////////

		//////////////////////////////////////////////////////////////////////
		/** Add the given breakpoint.
		 *
		 * \param file IN: current file name.
		 * \param line IN: the current line in the procedure.
		 * \param type IN: the breakpoint type
		 *
		**////////////////////////////////////////////////////////////////////
    	void addBreakpoint( const std::string& file,
    			            unsigned int line,
    			            const SPELLbreakpointType type );

		//////////////////////////////////////////////////////////////////////
		/** Remove the given breakpoint.
		 *
		 * \param file IN: current file name.
		 * \param line IN: the current line in the procedure.
		 *
		**////////////////////////////////////////////////////////////////////
    	void removeBreakpoint( const std::string& file, unsigned int line );

private: //-------------------------------------------------------------------

    // EXCEPTIONS ////////////////////////////////////////////////////////////
    // ENUMS /////////////////////////////////////////////////////////////////
    // TYPES /////////////////////////////////////////////////////////////////
    	/** Represents a list of breakpoints */
    	typedef std::vector<unsigned int>   		  BreakpointList;
    	/** Represents a map of list of breakpoints */
    	typedef std::map<std::string,BreakpointList>  BreakpointMap;
    	/** Represents a map of "run to" lines  */
    // LIFECYCLE /////////////////////////////////////////////////////////////
    // METHODS ///////////////////////////////////////////////////////////////
    // DATA MEMBERS //////////////////////////////////////////////////////////
    	/** Map of defined breakpoints */
    	BreakpointMap       m_breakpoints;
    	BreakpointMap	    m_tempBreakpoints;
};

/*@}*/
#endif
