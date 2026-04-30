// ################################################################################
// FILE       : SPELLwsFrame.H
// DATE       : Mar 17, 2011
// PROJECT    : SPELL
// DESCRIPTION: WS frame manager
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

#ifndef __SPELL_WS_FRAME_H__
#define __SPELL_WS_FRAME_H__

// FILES TO INCLUDE ////////////////////////////////////////////////////////
// System includes ---------------------------------------------------------
// Local includes ----------------------------------------------------------
#include "SPELL_WS/SPELLwsStaticData.H"
#include "SPELL_WS/SPELLwsDynamicData.H"
// Project includes --------------------------------------------------------
#include "SPELL_UTIL/SPELLbase.H"

/** \addtogroup SPELL_WS */
/*@{*/

// FORWARD REFERENCES //////////////////////////////////////////////////////
// ENUMS ///////////////////////////////////////////////////////////////////
// TYPES ///////////////////////////////////////////////////////////////////
// DEFINES /////////////////////////////////////////////////////////////////

//////////////////////////////////////////////////////////////////////////////
/**
 ** \brief In charge of storing the data of a given frame in a dedicated
 **  warmstart file. Also loads the data in case of recovery and provides a
 **  new frame.
 **
 **  \par Description and usage:
 **
 **  It is composed of two main parts:
 **
 **  - The static data (basically, bytecode and builtins) that does not
 **    change during the procedure execution.
 **
 **  - The dynamic data (globals, locals, stack, etc.) that changes during
 **    the execution.
 **
 **  Each part is managed by a dedicated controller (SPELLwsStaticData and
 **  SPELLwsDynamicData), each one having a binary file associated to it
 **  where the data is stored (SPELLwsStorage).
 **
 **  The idea is that the static data file is created only once and never
 **  changes. The user data is registered in the dynamic data file, which
 **  is smaller and easier to handle.
 **
**////////////////////////////////////////////////////////////////////////////
class SPELLwsFrame
{
public: //--------------------------------------------------------------------

    // EXCEPTIONS ////////////////////////////////////////////////////////////
    // ENUMS /////////////////////////////////////////////////////////////////
    // TYPES /////////////////////////////////////////////////////////////////
    // LIFECYCLE /////////////////////////////////////////////////////////////

		//////////////////////////////////////////////////////////////////////
		/** Constructor. Create a frame manager for an already existing frame
		 * during normal procedure execution.
		 *
		 * \param id IN: frame identifier
		 * \param info IN: startup configuration
		 * \param depth IN: recursion depth of this frame.
		 * \param frame IN: the associated frame, or previous/original head
		 *                  frame in recovery mode
		 *
		**////////////////////////////////////////////////////////////////////
		SPELLwsFrame( const std::string& id, const SPELLwsStartupInfo& info, unsigned int depth, PyFrameObject* frame );

		//////////////////////////////////////////////////////////////////////
		/** Destructor.
		**////////////////////////////////////////////////////////////////////
		~SPELLwsFrame();

    // METHODS ///////////////////////////////////////////////////////////////

		//////////////////////////////////////////////////////////////////////
		/** Get the represented frame.
		 *
		 * \return The frame.
		**////////////////////////////////////////////////////////////////////
		PyFrameObject* getFrameObject() const { return m_frame; };

		//////////////////////////////////////////////////////////////////////
		/** Cleanup error state in the associated frame.
		 *
		 * \param newState IN: new Python thread state to use
		 * \param isHead IN: true if the associated frame is the head of the tree.
		 *
		**////////////////////////////////////////////////////////////////////
		void fixState( PyThreadState* newState, bool isHead );

		//////////////////////////////////////////////////////////////////////
		/** Save the frame state in persistent files
		**////////////////////////////////////////////////////////////////////
		void saveState();

		//////////////////////////////////////////////////////////////////////
		/** Notify a line event
		**////////////////////////////////////////////////////////////////////
		void eventLine();

		//////////////////////////////////////////////////////////////////////
		/** Reset the recovery information
		**////////////////////////////////////////////////////////////////////
		void reset();

		//////////////////////////////////////////////////////////////////////
		/** Cleanup data stored in files
		**////////////////////////////////////////////////////////////////////
		void cleanup();

		//////////////////////////////////////////////////////////////////////
		/** Get the frame identifier
		**////////////////////////////////////////////////////////////////////
		std::string getCodeId() { return m_codeId; };

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
	// LIFECYCLE /////////////////////////////////////////////////////////////
    // METHODS ///////////////////////////////////////////////////////////////
    // DATA MEMBERS //////////////////////////////////////////////////////////

		/** Internal identifier of the frame model */
		std::string         m_codeId;
		/** Borrowed reference of the Python frame */
		PyFrameObject*      m_frame;
		/** Static data file manager */
		SPELLwsStaticData*	m_static;
		/** Dynamic data file manager */
		SPELLwsDynamicData*	m_dynamic;
		/** Last instruction pointer, used for recovery */
		unsigned int m_lastInstruction;
		/** Last line pointer, used for recovery */
		unsigned int m_lastLine;
};

/*@}*/
#endif
