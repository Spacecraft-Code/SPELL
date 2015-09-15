// ################################################################################
// FILE       : SPELLexecutorIF.H
// DATE       : Mar 17, 2011
// PROJECT    : SPELL
// DESCRIPTION: Executor interface definition
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

#ifndef __SPELL_EXECUTOR_IF_H__
#define __SPELL_EXECUTOR_IF_H__

// FILES TO INCLUDE ////////////////////////////////////////////////////////
// Local includes ----------------------------------------------------------
#include "SPELL_EXC/SPELLchildManager.H"
#include "SPELL_EXC/SPELLexecutorConfiguration.H"
#include "SPELL_EXC/SPELLuserAction.H"
#include "SPELL_EXC/SPELLframeManager.H"
#include "SPELL_EXC/SPELLschedulerIF.H"
#include "SPELL_EXC/SPELLcontrollerIF.H"
#include "SPELL_EXC/SPELLcallstackIF.H"
#include "SPELL_EXC/SPELLbreakpointType.H"
#include "SPELL_EXC/SPELLvariableManager.H"
#include "SPELL_EXC/SPELLdispatchListener.H"


// Project includes --------------------------------------------------------
#include "SPELL_CIF/SPELLcif.H"
#include "SPELL_CFG/SPELLcontextConfig.H"
#include "SPELL_CFG/SPELLbrowsableLibMode.H"

// System includes ---------------------------------------------------------


/** \addtogroup SPELL_EXC */
/*@{*/


// FORWARD REFERENCES //////////////////////////////////////////////////////
// TYPES ///////////////////////////////////////////////////////////////////
// DEFINES /////////////////////////////////////////////////////////////////

//////////////////////////////////////////////////////////////////////////////
/**
 ** \brief Main executor interface
 **
 ** \par Description and usage:
 **
 **		Defines the interface for any executor controller object.
 **
**////////////////////////////////////////////////////////////////////////////
class SPELLexecutorIF
{
public: //--------------------------------------------------------------------

    // EXCEPTIONS ////////////////////////////////////////////////////////////
    // ENUMS /////////////////////////////////////////////////////////////////
    // TYPES /////////////////////////////////////////////////////////////////
    // LIFECYCLE /////////////////////////////////////////////////////////////

		//////////////////////////////////////////////////////////////////////
		/** Destructor.
		**////////////////////////////////////////////////////////////////////
		virtual ~SPELLexecutorIF() {;};

    // STATIC ////////////////////////////////////////////////////////////////
    // METHODS ///////////////////////////////////////////////////////////////

    // *******************************************************************
    // INITIALIZATION
    // *******************************************************************

		//////////////////////////////////////////////////////////////////////
		/** Initialize the executor. Store the given references (whose life-
		 *  cycle depends on the Interpreter).
		 *
		 * \param cif INOUT: the reference to the client interface
		 * \param controller INOUT: the reference to the command controller.
		 * \param scheduler INOUT: the reference to the scheduler.
		 * \param callstack INOUT: the reference to the callstack model.
		 * \param frameManager INOUT: the reference to the frame manager.
		**////////////////////////////////////////////////////////////////////
		virtual void initialize( SPELLcif* cif,
						 SPELLcontrollerIF* controller,
						 SPELLschedulerIF* scheduler,
						 SPELLcallstackIF* callstack,
						 SPELLframeManager* frameManager ) = 0;

		//////////////////////////////////////////////////////////////////////
		/** Prepare execution. The first time it is called will load the default
		 * values for configuration parameters like RunInto and ByStep flags.
		 * Will process the procedure arguments and execution condition also,
		 * and prepare the controller for automatic mode if needed.
		**////////////////////////////////////////////////////////////////////
		virtual void prepare( const std::string& instanceId, const SPELLcontextConfig& ctxConfig ) = 0;


    // *******************************************************************
    // COMPONENT ACCESSORS
    // *******************************************************************

		//////////////////////////////////////////////////////////////////////
		/** Obtain the client interface reference
		 *
		 * \returns The client interface reference
		**////////////////////////////////////////////////////////////////////
		virtual SPELLcif& getCIF() const = 0;

		//////////////////////////////////////////////////////////////////////
		/** Obtain the call stack reference
		 *
		 * \returns The call stack reference
		**////////////////////////////////////////////////////////////////////
		virtual SPELLcallstackIF& getCallstack() const = 0;

		//////////////////////////////////////////////////////////////////////
		/** Obtain the controller reference
		 *
		 * \returns The controller reference
		**////////////////////////////////////////////////////////////////////
		virtual SPELLcontrollerIF& getController() const = 0;

		//////////////////////////////////////////////////////////////////////
		/** Obtain the scheduler reference
		 *
		 * \returns The scheduler reference
		**////////////////////////////////////////////////////////////////////
		virtual SPELLschedulerIF& getScheduler() const = 0;

		//////////////////////////////////////////////////////////////////////
		/** Obtain the child manager reference
		 *
		 * \returns The child manager reference
		**////////////////////////////////////////////////////////////////////
		virtual SPELLchildManager& getChildManager() const = 0;

		//////////////////////////////////////////////////////////////////////
		/** Obtain executor configuration.
		 *
		 * \returns The configuration object.
		**////////////////////////////////////////////////////////////////////
		virtual SPELLexecutorConfig& getConfiguration() const = 0;

		//////////////////////////////////////////////////////////////////////
		/** Obtain variable manager.
		 *
		 * \returns The variable manager object.
		**////////////////////////////////////////////////////////////////////
		virtual SPELLvariableManager& getVariableManager() const = 0;

		//////////////////////////////////////////////////////////////////////
		/** Obtain execution frame.
		 *
		 * \returns The execution frame object.
		**////////////////////////////////////////////////////////////////////
		virtual SPELLframeManager& getFrameManager() const = 0;

    // *******************************************************************
    // EXECUTION ENTRY POINTS
    // *******************************************************************

		//////////////////////////////////////////////////////////////////////
		/** Start execution. The following operations are done:
		 *
		 * 		1. Set the Python API dispatch mechanism
		 * 		2. Load the execution environment (driver and data)
		 * 		3. Launch the frame execution
		 *
		 * Following actions depend on the result of the frame execution. In
		 * case of error, the executor will analyze the exception in order to
		 * find out if the execution can be recovered or not. In case of abort,
		 * will wait for a close or reload command. In case of success execution
		 * will set the procedure status as finished.
		 *
		 * After the operations described above, the execution environment
		 * will be unloaded. If the execution is about to be repeated (due to
		 * a recover or reload command) the execution environment is shut down
		 * (which implies more cleanup than the unload operation before), the
		 * execution frame state is reset, and the process is repeated from
		 * step 1 above. If the execution has finished completely, the trace
		 * dispatch mechanism is removed and the method finishes.
		**////////////////////////////////////////////////////////////////////
		virtual void execute() = 0;

		//////////////////////////////////////////////////////////////////////
		/** Recover execution. Invoked when a recoverable error takes place
		 * and the user chooses to recover the procedure state. The execution
		 * frame will try to restore the state. If success, the execute method
		 * is called again.
		**////////////////////////////////////////////////////////////////////
		virtual void recover() = 0;

		//////////////////////////////////////////////////////////////////////
		/** Finalize execution. Basically tell the CIF that nothing is to be
		 * done, in order to properly shutdown and allow the executor process
		 * to exit cleanly.
		**////////////////////////////////////////////////////////////////////
		virtual void finalize() = 0;

		//////////////////////////////////////////////////////////////////////
		/** Save the execution status on demand.
		**////////////////////////////////////////////////////////////////////
		virtual void save() = 0;


    // *******************************************************************
    // MAIN EXECUTION CONTROL
    // *******************************************************************

		//////////////////////////////////////////////////////////////////////
		/** Add a dispatch event listener
		**////////////////////////////////////////////////////////////////////
		virtual void addDispatchListener( SPELLdispatchListener* listener ) {;};

		//////////////////////////////////////////////////////////////////////
		/** Remove a dispatch event listener
		**////////////////////////////////////////////////////////////////////
		virtual void removeDispatchListener( SPELLdispatchListener* listener ) {;};

		//////////////////////////////////////////////////////////////////////
		/** Execution control dispatcher. This method is called via a static
		 * function (bridge). This is the core of the Python dispatching
		 * mechanism, that allows the executor to control the procedure
		 * execution (pausing, stepping, aborting, the goto mechanism, etc).
		 *
		 * \param obj IN: ignored.
		 * \param frame IN: the current Python frame
		 * \param what IN: type of event taking place (line, call, return, etc)
		 * \param args IN: arguments providing more info about the event.
		**////////////////////////////////////////////////////////////////////
		virtual int dispatch( PyObject* obj, PyFrameObject* frame, int what, PyObject* args ) = 0;

		//////////////////////////////////////////////////////////////////////
		/** Send command to executor. Will push the given command into the
		 * command mailbox in order to be processed by the command controller.
		 *
		 * \param cmd IN: command to be processed.
		 * \param high_priority IN: if True, the given command will have priority
		 * over "normal" commands already waiting to be processed.
		**////////////////////////////////////////////////////////////////////
		virtual void command( const ExecutorCommand& cmd, const bool high_priority ) = 0;

		//////////////////////////////////////////////////////////////////////
		/** Abort execution from within the SPELL framework (not user command).
		 * Will terminate the Frame Evaluation process on Python side in order
		 * to cancel the execution immediately. This method is to be called
		 * not by CIF (because it overrides the language interlock) but by
		 * the SPELL framework (adapter, language or drivers).
		 *
		 * \param message IN: message to be shown, or empty string
		 * \param systemAborted IN: true if the abort is being performed by the system, not the proc
		 *
		**////////////////////////////////////////////////////////////////////
		virtual void abort( const std::string& message, bool systemAborted ) = 0;

		//////////////////////////////////////////////////////////////////////
		/** Finalize execution from within the SPELL framework (not user command).
		 * Will terminate the Frame Evaluation process on Python side in order
		 * to end the execution immediately. This method is to be called
		 * not by CIF (because it overrides the language interlock) but by
		 * the SPELL framework (adapter, language or drivers).
		**////////////////////////////////////////////////////////////////////
		virtual void finish( const std::string& message ) = 0;

		//////////////////////////////////////////////////////////////////////
		/** Pause execution from within the SPELL framework (not user command).
		 * Will hold the Frame Evaluation process on Python side. This method
		 * is to be called not by CIF (because it overrides the language
		 * interlock) but by the SPELL framework (adapter, language or drivers).
		**////////////////////////////////////////////////////////////////////
		virtual void pause() = 0;

		//////////////////////////////////////////////////////////////////////
		/** Interrupt execution from within the SPELL framework (not user command).
		 * Will hold the Frame Evaluation process on Python side. This method
		 * is to be called not by CIF (because it overrides the language
		 * interlock) but by the SPELL framework (adapter, language or drivers).
		**////////////////////////////////////////////////////////////////////
		virtual void interrupt() {};

    // *******************************************************************
    // INFORMATION ACCESSORS
    // *******************************************************************

		//////////////////////////////////////////////////////////////////////
		/** Obtain the current executor status
		 *
		 * \returns The current executor status
		**////////////////////////////////////////////////////////////////////
		virtual const SPELLexecutorStatus getStatus() const = 0;

		//////////////////////////////////////////////////////////////////////
		/** Obtain the procedure instance identifier
		 *
		 * \returns The procedure instance identifier
		**////////////////////////////////////////////////////////////////////
		virtual const std::string getInstanceId() const = 0;

    // *******************************************************************
    // EXECUTION CONFIGURATION PARAMETERS
    // *******************************************************************

		//////////////////////////////////////////////////////////////////////
		/** Set the configured procedure path.
		 *
		 * \param procPath IN: the procedure path.
		**////////////////////////////////////////////////////////////////////
		virtual void setProcedurePath( const std::string& procPath ) = 0;

		//////////////////////////////////////////////////////////////////////
		/** Set the configured user library path.
		 *
		 * \param libPath IN: the user library path.
		**////////////////////////////////////////////////////////////////////
		virtual void setLibraryPath( const std::string& libPath ) = 0;

		//////////////////////////////////////////////////////////////////////
		/** Set the run-into flag
		 *
		 * \param enabled IN: the flag value
		**////////////////////////////////////////////////////////////////////
		virtual void setRunInto( const bool enabled ) = 0;

		//////////////////////////////////////////////////////////////////////
		/** Set the by-step flag
		 *
		 * \param enabled IN: the flag value
		**////////////////////////////////////////////////////////////////////
		virtual void setByStep( const bool enabled ) = 0;

		//////////////////////////////////////////////////////////////////////
		/** Set the browsable lib flag
		 *
		 * \param enabled IN: the flag value
		**////////////////////////////////////////////////////////////////////
		virtual void setBrowsableLib( const SPELLbrowsableLibMode& browsableLib ) = 0;

		virtual void setBrowsableLibStr( const std::string& browsableLibStr ) = 0;

		//////////////////////////////////////////////////////////////////////
		/** Set the execution delay
		 *
		 * \param delay IN: the delay to be used
		**////////////////////////////////////////////////////////////////////
		virtual void setExecDelay( const int delay ) = 0;

		//////////////////////////////////////////////////////////////////////
		/** Set the prompt warning delay
		 *
		 * \param delay IN: the delay to be used
		**////////////////////////////////////////////////////////////////////
		virtual void setPromptWarningDelay( const int delay ) = 0;

		//////////////////////////////////////////////////////////////////////
		/** Set the TC confirmation flag
		 *
		 * \param force IN: the flag value
		**////////////////////////////////////////////////////////////////////
		virtual void setForceTcConfirm( const bool force ) = 0;

    // *******************************************************************
    // LANGUAGE INTERLOCK
    // *******************************************************************

		//////////////////////////////////////////////////////////////////////
		/** Set the language lock. When set, any command coming from the
		 * client will be holded. This way, commands are not processed while
		 * important operations take place inside the executor.
		**////////////////////////////////////////////////////////////////////
		virtual void processLock() = 0;

		//////////////////////////////////////////////////////////////////////
		/** Unset the language lock. \see processLock for details.
		**////////////////////////////////////////////////////////////////////
		virtual void processUnlock() = 0;


	// *******************************************************************
    // EXECUTION MANIPULATION
    // *******************************************************************

		//////////////////////////////////////////////////////////////////////
		/** Skip mechanism. Check if the current line is skippable.
		 *
		 * \returns True if the line can be skipped.
		**////////////////////////////////////////////////////////////////////
		virtual const bool canSkip() = 0;

		//////////////////////////////////////////////////////////////////////
		/** Skip mechanism. Make the execution advance to the next line without
		 * executing the current one.
		 *
		 * \returns True if a STEP command shall be added after the skip.
		 *
		 * FIXME: this is a workaround to allow skipping at the very end of a
		 * function. The problem caused by this is that it is obligued that the
		 * last statement in a function shall be a 'return' statement always,
		 * because so far we cannot prevent the executor from executing that
		 * statement when exiting the function. A more clever processing should
		 * be implemented in the SPELLframeManager object when doing this
		 * kind of skip.
		 *
		**////////////////////////////////////////////////////////////////////
		virtual const bool goNextLine() = 0;

		//////////////////////////////////////////////////////////////////////
		/** Go to the given label in the procedure. Will report a WARNING if
		 * the go-to cannot be performed.
		 *
		 * \param label IN: label (step identifier) where to go.
		 * \param programmed IN: if true, the goto is coded in a proc
		 *
		 * \returns True on success.
		**////////////////////////////////////////////////////////////////////
		virtual const bool goLabel( const std::string& label, bool programmed ) = 0;

		//////////////////////////////////////////////////////////////////////
		/** Go to the given line num. in the procedure. Will report a WARNING if
		 * the go-to cannot be performed.
		 *
		 * \param line IN: line where to go.
		 *
		 * \returns True on success.
		**////////////////////////////////////////////////////////////////////
		virtual const bool goLine( const int line ) = 0;

		//////////////////////////////////////////////////////////////////////
		/** Toggle a breakpoint on the given line.
		 *
		 * \param file IN: file where the breakpoint is to be set/unset.
		 * \param line IN: line where the breakpoint is to be set/unset.
		 * \param type IN: the breakpoint type
		 *
		 * \returns True on success.
		**////////////////////////////////////////////////////////////////////
		virtual const bool setBreakpoint( const std::string& file,
				                          const unsigned int line,
				                          const SPELLbreakpointType type ) = 0;

		//////////////////////////////////////////////////////////////////////
		/** Clear all breakpoints.
		**////////////////////////////////////////////////////////////////////
		virtual void clearBreakpoints() = 0;

		//////////////////////////////////////////////////////////////////////
		/** Execute a Python code snippet in the context of the current
		 * execution frame (thus modifying currently available data and vars).
		 * Will report a WARNING if the execution fails.
		 *
		 * \param script IN: code to be executed.
		 *
		 * \returns True on success.
		**////////////////////////////////////////////////////////////////////
		virtual const bool runScript( const std::string& script ) = 0;

		//////////////////////////////////////////////////////////////////////
		/** Execute a user action defined previously by language calls.
		**////////////////////////////////////////////////////////////////////
		virtual void executeUserAction() = 0;

		//////////////////////////////////////////////////////////////////////
		/** Set the user action function, name and label
		**////////////////////////////////////////////////////////////////////
		virtual void setUserAction( const SPELLuserAction& action ) = 0;

		//////////////////////////////////////////////////////////////////////
		/** Enable or disable the user action function
		**////////////////////////////////////////////////////////////////////
		virtual void enableUserAction( const bool enable ) = 0;

		//////////////////////////////////////////////////////////////////////
		/** Remove the user action function trigger
		**////////////////////////////////////////////////////////////////////
		virtual void dismissUserAction() = 0;

		//////////////////////////////////////////////////////////////////////
		/** Get the user action information if any
		**////////////////////////////////////////////////////////////////////
		virtual const SPELLuserAction getUserAction() const = 0;

		//////////////////////////////////////////////////////////////////////
		/** Used by the SPELL language to report that a Step() call has been
		 * reached, so the current stage information is updated.
		 *
		 * \param id IN: step identifier
		 * \param title IN: step title.
		 *
		**////////////////////////////////////////////////////////////////////
		virtual void stageReached( const std::string& id, const std::string& title ) = 0;

		//////////////////////////////////////////////////////////////////////
		/** Used by the SPELL language to report that a Step() call has been
		 * reached, so the current stage information is updated.
		 *
		 * \param id IN: step identifier
		 * \param title IN: step title.
		 *
		**////////////////////////////////////////////////////////////////////
		virtual void displayStage( const std::string& id, const std::string& title ) = 0;

};

/*@}*/
#endif
