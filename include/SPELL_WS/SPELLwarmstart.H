// ################################################################################
// FILE       : SPELLwarmstart.H
// DATE       : Mar 17, 2011
// PROJECT    : SPELL
// DESCRIPTION: SPELL warmstart interface
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

#ifndef __SPELL_WARMSTART_H__
#define __SPELL_WARMSTART_H__

// FILES TO INCLUDE ////////////////////////////////////////////////////////
// Local includes ----------------------------------------------------------
#include "SPELL_WS/SPELLwsStartupInfo.H"
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
 ** \brief Provides support for storing/restoring the Python interpreter state.
 **
 ** \par Description:
 **
 ** \par Usage:
 **
 ** \par Synchronization:
 **      No concurrency issues foreseen.
 **
**////////////////////////////////////////////////////////////////////////////
class SPELLwarmStart
{
public: //--------------------------------------------------------------------

    // EXCEPTIONS ////////////////////////////////////////////////////////////
    // ENUMS /////////////////////////////////////////////////////////////////
    // TYPES /////////////////////////////////////////////////////////////////
    // LIFECYCLE /////////////////////////////////////////////////////////////

		//////////////////////////////////////////////////////////////////////
		/** Destructor.
		**////////////////////////////////////////////////////////////////////
		virtual ~SPELLwarmStart() {;};

    // METHODS ///////////////////////////////////////////////////////////////

		//////////////////////////////////////////////////////////////////////
		/** Initialize the mechanism
		 *
		 * \param identifier IN: the identifier used for data files.
		 * \param configuredMode IN: the working mode in configuration
		 *
		**////////////////////////////////////////////////////////////////////
		virtual void initialize( const SPELLwsStartupInfo& info ) = 0;

		//////////////////////////////////////////////////////////////////////
		/** Reset the mechanism
		**////////////////////////////////////////////////////////////////////
		virtual void reset() = 0;

		//////////////////////////////////////////////////////////////////////
		/** Cleanup and erase all data
		**////////////////////////////////////////////////////////////////////
		virtual void cleanup() = 0;

	//========================================================================
	// METHODS TO KEEP THE SAVED STATE UPDATED
	//========================================================================

		//////////////////////////////////////////////////////////////////////
		/** Notify that a call to a function has been performed. The frame
		 * that has been created is provided, in order to create a new
		 * persistent file for it.
		 *
		 * \param newFrame IN: the new frame.
		**////////////////////////////////////////////////////////////////////
		virtual void notifyCall( const std::string& id, PyFrameObject* newFrame ) = 0;

		//////////////////////////////////////////////////////////////////////
		/** Notify that the interpreter has come back from a function. The frame
		 * that was being used is disposed, and the associated warmstart data
		 * is to be destroyed as well.
		**////////////////////////////////////////////////////////////////////
		virtual void notifyReturn() = 0;

		//////////////////////////////////////////////////////////////////////
		/** Notify that the execution progresses in a block. This is the time
		 * to check for data changes and keep the dynamic data updated in the
		 * warmstart files.
		**////////////////////////////////////////////////////////////////////
		virtual void notifyLine() = 0;

		//////////////////////////////////////////////////////////////////////
		/** Notify that the execution has reached an stage. This notification
		 * is additional and redundant to line notifications, and it is used
		 * in the working mode \c MODE_BY_STEP.
		**////////////////////////////////////////////////////////////////////
		virtual void notifyStage() = 0;

	//========================================================================
	// METHODS TO CONTROL THE STORAGE PROCESS
	//========================================================================

		//////////////////////////////////////////////////////////////////////
		/** Obtain the current working mode.
		 *
		 * \return The working mode.
		**////////////////////////////////////////////////////////////////////
		virtual SPELLwsWorkingMode getWorkingMode() const = 0;

	//========================================================================
	// METHODS TO CONTROL STATE MANAGEMENT
	//========================================================================

		//////////////////////////////////////////////////////////////////////
		/** Restore execution from scratch, using all available frame data.
		 *
		 * \throws SPELLcoreException if process fails.
		 *
		 * \return The top frame object to restart execution, or NULL if
		 * the restoration process fails.
		**////////////////////////////////////////////////////////////////////
		virtual PyFrameObject* restoreState() = 0;

		//////////////////////////////////////////////////////////////////////
		/** Fix execution failure without rebuilding the frame structure
		 *
		 * \throws SPELLcoreException if process fails.
		 *
		 * \return The top frame object to restart execution, or NULL if
		 * the restoration process fails.
		**////////////////////////////////////////////////////////////////////
		virtual PyFrameObject* fixState() = 0;

		//////////////////////////////////////////////////////////////////////
		/** Save the state programmatically.
		**////////////////////////////////////////////////////////////////////
		virtual void saveState() = 0;

};

/*@}*/
#endif
