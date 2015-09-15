// ################################################################################
// FILE       : SPELLcontrollerIF.H
// DATE       : Mar 17, 2011
// PROJECT    : SPELL
// DESCRIPTION: Defines the controller interface.
// --------------------------------------------------------------------------------
//
//  Copyright (C) 2008, 2015 SES ENGINEERING, Luxembourg S.A.R.L.
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

#ifndef __SPELL_CONTROLLER_IF_H__
#define __SPELL_CONTROLLER_IF_H__

// FILES TO INCLUDE ////////////////////////////////////////////////////////
// System includes ---------------------------------------------------------
// Local includes ----------------------------------------------------------
#include "SPELL_EXC/SPELLexecutorStatus.H"
#include "SPELL_EXC/SPELLexecutionMode.H"
#include "SPELL_EXC/SPELLcommand.H"
#include "SPELL_EXC/SPELLcontrollerListener.H"
#include "SPELL_EXC/SPELLdispatchListener.H"
// Project includes --------------------------------------------------------


/** \addtogroup SPELL_EXC */
/*@{*/


// FORWARD REFERENCES //////////////////////////////////////////////////////
// TYPES ///////////////////////////////////////////////////////////////////
// DEFINES /////////////////////////////////////////////////////////////////

//////////////////////////////////////////////////////////////////////////////
/**
 ** \brief Controller interface
 **
**////////////////////////////////////////////////////////////////////////////
class SPELLcontrollerIF : public SPELLdispatchListener
{
public: //--------------------------------------------------------------------

    // EXCEPTIONS ////////////////////////////////////////////////////////////
    // ENUMS /////////////////////////////////////////////////////////////////
    // TYPES /////////////////////////////////////////////////////////////////
    // LIFECYCLE /////////////////////////////////////////////////////////////

		//////////////////////////////////////////////////////////////////////
		/** Constructor.
		**////////////////////////////////////////////////////////////////////
		SPELLcontrollerIF() : SPELLdispatchListener() {;};

		//////////////////////////////////////////////////////////////////////
		/** Destructor.
		**////////////////////////////////////////////////////////////////////
		virtual ~SPELLcontrollerIF() {;};

    // STATIC ////////////////////////////////////////////////////////////////

    // METHODS ///////////////////////////////////////////////////////////////

		//////////////////////////////////////////////////////////////////////
		/** Start the command dispatching thread, Used on procedure closure.
		**////////////////////////////////////////////////////////////////////
		virtual void begin() = 0;

		//////////////////////////////////////////////////////////////////////
		/** Stop the command dispatching thread, Used on procedure closure.
		**////////////////////////////////////////////////////////////////////
		virtual void stop() = 0;

		//////////////////////////////////////////////////////////////////////
		/** Reset the command queues and put the controller on the initial
		 * state. Used on procedure reload.
		**////////////////////////////////////////////////////////////////////
		virtual void reset() = 0;

		//////////////////////////////////////////////////////////////////////
		/** Issue a command to the controller.
		**////////////////////////////////////////////////////////////////////
		virtual void command( const ExecutorCommand& cmd, const bool queueIt, const bool high_priority ) = 0;

		//////////////////////////////////////////////////////////////////////
		/** Check if should reload. Used by the interpreter object to know
		 * if the user wants the procedure be reloaded or closed. Reloading
		 * does not necessarily imply procedure recovery (wstart)
		 *
		 * \returns True if the procedure should be reloaded.
		**////////////////////////////////////////////////////////////////////
		virtual const bool shouldReload() const = 0;

		//////////////////////////////////////////////////////////////////////
		/** Wait for condition to reload the procedure and recover the
		 * previous state. Will block the caller until the procedure is ready
		 * to recover (or close)
		 *
		 * \returns True if the procedure recovery can be performed.
		**////////////////////////////////////////////////////////////////////
		virtual const bool shouldRecover() = 0;

		//////////////////////////////////////////////////////////////////////
		/** Obtain the current execution status.
		 *
		 * \returns The executor status code.
		**////////////////////////////////////////////////////////////////////
		virtual const SPELLexecutorStatus getStatus() const = 0;

		//////////////////////////////////////////////////////////////////////
		/** Obtain the current execution mode (play or step)
		 *
		 * \returns The current execution mode.
		**////////////////////////////////////////////////////////////////////
		virtual const SPELLexecutionMode getMode() const = 0;

		//////////////////////////////////////////////////////////////////////
		/** Set the current execution status and notify the client interface.
		 *
		 * \param st IN: current executor status.
		**////////////////////////////////////////////////////////////////////
		virtual void setStatus( const SPELLexecutorStatus& st ) = 0;

		//////////////////////////////////////////////////////////////////////
		/** Set the current execution mode.
		 *
		 * \param mode IN: the execution mode.
		**////////////////////////////////////////////////////////////////////
		virtual void setMode( const SPELLexecutionMode& mode ) = 0;

		//////////////////////////////////////////////////////////////////////
		/** Set the procedure condition for initial schedule. If a condition
		 * is set, the procedure execution is on hold until the condition
		 * is fulfilled. At that moment the controler triggers the automatic
		 * execution of the proc.
		 *
		 * \param condition IN: the condition expression.
		**////////////////////////////////////////////////////////////////////
		virtual void setCondition( const std::string& condition ) = 0;

		//////////////////////////////////////////////////////////////////////
		/** Get the current schedule condition, if any. @see setCondition.
		 *
		 * \returns The current schedule condition, or an empty string if there
		 * is no condition defined.
		**////////////////////////////////////////////////////////////////////
		virtual const std::string& getCondition() const = 0;

		//////////////////////////////////////////////////////////////////////
		/** Check if there is a schedule condition defined.
		 *
		 * \returns True if there is a condition defined.
		**////////////////////////////////////////////////////////////////////
		virtual const bool hasCondition() const = 0;

		//////////////////////////////////////////////////////////////////////
		/** Set the auto-run flag. If this flag is set to true at the moment
		 * of the procedure preparation, the procedure will go automatically
		 * to play mode as soon as it is ready.
		**////////////////////////////////////////////////////////////////////
		virtual void setAutoRun() = 0;

		//////////////////////////////////////////////////////////////////////
		/** Set the run into flag. If this flag is true, the executor goes
		 * into all functions automatically. If the flag is false, the executor
		 * does step over in all functions.
		 *
		 * \param enable IN: the flag value.
		**////////////////////////////////////////////////////////////////////
		virtual void enableRunInto( const bool enable ) = 0;

		//////////////////////////////////////////////////////////////////////
		/** Check the terminated/aborted state of the procedure. If the
		 * controlled has been previously set to aborted/terminated state,
		 * this method:
		 *
		 * 1. Inserts a python exception in the interpreter evaluation chain,
		 *    so that the execution of python code is aborted.
		 * 2. Blocks the dispatching mechanism until (a) the procedure is
		 *    closed or (b) the procedure is reloaded.
		 *
		 * \returns True if the execution can continue.
		**////////////////////////////////////////////////////////////////////
		virtual const bool checkAborted() = 0;

		//////////////////////////////////////////////////////////////////////
		/** Acquire the language lock. Used by SPELL language functions so
		 * that commands are not processed until the SPELL function finishes
		 * its work. This is not applicable to "interruptible" SPELL statements.
		**////////////////////////////////////////////////////////////////////
		virtual void executionLock() = 0;

		//////////////////////////////////////////////////////////////////////
		/** Release the language lock. Used by SPELL language functions so
		 * that commands can be processed, since the SPELL function has finished
		 * its work. This is not applicable to "interruptible" SPELL statements.
		**////////////////////////////////////////////////////////////////////
		virtual void executionUnlock() = 0;

		//////////////////////////////////////////////////////////////////////
		/**
		**////////////////////////////////////////////////////////////////////
		virtual void waitCommand() = 0;

		//////////////////////////////////////////////////////////////////////
		/** True if Python dispatch loop shall be repeated
		**////////////////////////////////////////////////////////////////////
		virtual bool shallRepeat() = 0;

		//////////////////////////////////////////////////////////////////////
		/** Mark the procedure start time
		**////////////////////////////////////////////////////////////////////
		virtual void setStartTime() = 0;

		//////////////////////////////////////////////////////////////////////
		/** Mark the procedure end time
		**////////////////////////////////////////////////////////////////////
		virtual void setEndTime() = 0;

		//////////////////////////////////////////////////////////////////////
		/** Set the procedure finished status
		**////////////////////////////////////////////////////////////////////
		virtual void setFinished() = 0;

		//////////////////////////////////////////////////////////////////////
		/** Set the procedure in error state
		 *
		 * \param error IN: error message
		 * \param reason IN: error reason
		 * \param fatal IN: if true, indicates that the error is unrecoverable.
		**////////////////////////////////////////////////////////////////////
		virtual void setError( const std::string& error, const std::string& reason, const bool fatal ) = 0;

		//////////////////////////////////////////////////////////////////////
		/** Add a controller listener
		**////////////////////////////////////////////////////////////////////
		virtual void addControllerListener( SPELLcontrollerListener* listener ) {;};

		//////////////////////////////////////////////////////////////////////
		/** Remove a controller listener
		**////////////////////////////////////////////////////////////////////
		virtual void removeControllerListener( SPELLcontrollerListener* listener ) {;};
};

/*@}*/
#endif
