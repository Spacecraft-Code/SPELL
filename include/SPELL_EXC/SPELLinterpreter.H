// ################################################################################
// FILE       : SPELLinterpreter.H
// DATE       : Mar 17, 2011
// PROJECT    : SPELL
// DESCRIPTION: Main entry point of the executor.
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

#ifndef __SPELL_INTERPRETER_H__
#define __SPELL_INTERPRETER_H__

// FILES TO INCLUDE ////////////////////////////////////////////////////////
// Local includes ----------------------------------------------------------
#include "SPELL_EXC/SPELLexecutorIF.H"
// Project includes --------------------------------------------------------
// System includes ---------------------------------------------------------


/** \addtogroup SPELL_EXC */
/*@{*/

// FORWARD REFERENCES //////////////////////////////////////////////////////
// ENUMS ///////////////////////////////////////////////////////////////////
// TYPES ///////////////////////////////////////////////////////////////////

/** Configuration parameters for the interpreter */
typedef struct SPELLinterpreterConfig_
{
	/** If true, WS is enabled and the given persistent file is used */
    bool warmstart;
    /** If true, the given persistent file is used to recover status */
    bool recover;
    /** If true, the given procId corresponds to a script, not a proc id */
    bool script;
    /** Path to main configuration file */
    std::string configFile;
    /** Procedure identifier or script path */
    std::string procId;
    /** SPELL context to be used */
    std::string ctxName;
    /** Context listening port */
    int ctxPort;
    /** Time string to be used in generated files */
    std::string timeId;
    /** If given, holds the path to the file where WS data is stored */
    std::string persis;
}
SPELLinterpreterConfig;

// DEFINES /////////////////////////////////////////////////////////////////

//////////////////////////////////////////////////////////////////////////////
/**
 ** \brief Encapsulates the Python interpreter framework in a single object.
 **
 ** \par Description and usage:
 **
 ** 	The interpreter is the main class of the executor
 **  	process. It creates all the pieces needed to execute a given script
 **  	or procedure. It provides also the ability of recovering a proc
 **  	execution by using a given WS information file.
 **
 ** 	Singleton used in the main program function.
 **
**////////////////////////////////////////////////////////////////////////////
class SPELLinterpreter
{
public: //--------------------------------------------------------------------

    // EXCEPTIONS ////////////////////////////////////////////////////////////
    // ENUMS /////////////////////////////////////////////////////////////////
    // TYPES /////////////////////////////////////////////////////////////////
    // LIFECYCLE /////////////////////////////////////////////////////////////

		//////////////////////////////////////////////////////////////////////
		/** Destructor.
		**////////////////////////////////////////////////////////////////////
		~SPELLinterpreter();

    // STATIC ////////////////////////////////////////////////////////////////

		//////////////////////////////////////////////////////////////////////
		/** Access to the singleton instance
		**////////////////////////////////////////////////////////////////////
		static SPELLinterpreter& instance();

    // METHODS ///////////////////////////////////////////////////////////////

		//////////////////////////////////////////////////////////////////////
		/** Main interpreter program loop. Will first prepare the control
		 * objects (controller, scheduler, callstack, etc), \see prepareObjects
		 * method. Then, execution environment will be set up (\see prepareExecution
		 * method). If both operations are success, the methods recover() or
		 * execute() are invoked depending on the command line arguments of
		 * the executor application. Once these finish, it waits for the signal
		 * to cleanup all things in order to exit the executor process.
		**////////////////////////////////////////////////////////////////////
		void mainLoop();

		//////////////////////////////////////////////////////////////////////
		/** Initialize the interpreter with the given configuration
		 **
		 ** \param config IN: Configuration data
		 ** \param cif IN: SPELL client interface instance to use.
		**////////////////////////////////////////////////////////////////////
		void initialize( const SPELLinterpreterConfig& config, SPELLcif* cif );

    // DATA MEMBERS //////////////////////////////////////////////////////////

protected: //-----------------------------------------------------------------

    // EXCEPTIONS ////////////////////////////////////////////////////////////
    // ENUMS /////////////////////////////////////////////////////////////////
    // TYPES /////////////////////////////////////////////////////////////////
    // LIFECYCLE /////////////////////////////////////////////////////////////

		//////////////////////////////////////////////////////////////////////
		/** Constructor.
		**////////////////////////////////////////////////////////////////////
		SPELLinterpreter();

    // METHODS ///////////////////////////////////////////////////////////////
    // DATA MEMBERS //////////////////////////////////////////////////////////

private: //-------------------------------------------------------------------

    // EXCEPTIONS ////////////////////////////////////////////////////////////
    // ENUMS /////////////////////////////////////////////////////////////////
    // TYPES /////////////////////////////////////////////////////////////////
    // LIFECYCLE /////////////////////////////////////////////////////////////
    // METHODS ///////////////////////////////////////////////////////////////

		//////////////////////////////////////////////////////////////////////
		/** Prepare the CIF component
		**////////////////////////////////////////////////////////////////////
		void prepareCIF( const SPELLcontextConfig& ctxConfig );

		//////////////////////////////////////////////////////////////////////
		/** Recover the CIF component information
		**////////////////////////////////////////////////////////////////////
		void recoverCIF( const SPELLcontextConfig& ctxConfig );

		//////////////////////////////////////////////////////////////////////
		/** Prepare the warm start / error recovery support mechanism. It will
		 * build the warmstart persistence file name, create the SPELLwarmStart
		 * object and initialize the execution frame with their references. MAy
		 * fail if SPELL_DATA environment variable is not set, or the persistent
		 * file cannot be created.
		**////////////////////////////////////////////////////////////////////
		void prepareWarmStart( const SPELLcontextConfig& ctxConfig );

		//////////////////////////////////////////////////////////////////////
		/** If initial preparation succeeds, additional mechanisms are intialized
		 *  configured and/or installed:
		 *
		 * 		1. Install special exceptions on Python side
		 * 		2. Install the executor instance on the SPELL registry
		 * 		3. Install the go-to mechanism on Python side
		 * 		4. Install the CIF instance on the SPELL registry
		 * 		5. Get procedure and User Library search paths
		 * 		6. Compile the procedure code and prepare the execution frame.
		 *
		 * \returns False in case of failure.
		**////////////////////////////////////////////////////////////////////
		const bool prepareExecution();

		//////////////////////////////////////////////////////////////////////
		/** Prepare objects neede for the execution of the procedure/script.
		 * The following steps are performed:
		 *
		 * 		1. Initialize Python API
		 * 		2. Install Log mechanism on Python side
		 * 		3. Import SPELL framework on Python side.
		 * 		4. Load configuration from XML files
		 * 		5. Setup the client interface to connect to the client
		 * 		6. Create control objects (controller, scheduler, frame, etc)
		 * 		7. Prepare the warm start support
		 * 		8. Initialize and configure the executor object
		 *      9. Start the command controller.
		 *
		 * \returns False in case of failure.
		**////////////////////////////////////////////////////////////////////
		const bool prepareObjects();

		//////////////////////////////////////////////////////////////////////
		/** Execute the script/procedure. Basically invoke the execute() method
		 * of the executor object and catch any error taking place.
		**////////////////////////////////////////////////////////////////////
		void execute();

		//////////////////////////////////////////////////////////////////////
		/** Recover script/procedure execution from persistent file. Invoke
		 * the recover() method of the executor object.
		**////////////////////////////////////////////////////////////////////
		void recover();

		//////////////////////////////////////////////////////////////////////
		/** Handle errors raised during the initialization performed by the
		 * prepareExecution method.
		**////////////////////////////////////////////////////////////////////
		void handleStartupError();

    // DATA MEMBERS //////////////////////////////////////////////////////////
		/** Interpreter configuration */
		SPELLinterpreterConfig   m_config;
		/** Executor instance */
		SPELLexecutorIF*         m_executor;
		/** Warm start mechanism */
		SPELLwarmStart*          m_warmStart;
		/** Client interface */
		SPELLcif*                m_cif;
		/** Execution controller */
		SPELLcontrollerIF*       m_controller;
		/** Execution scheduler */
		SPELLschedulerIF*        m_scheduler;
		/** Callstack manager */
		SPELLcallstackIF*        m_callstack;
		/** Execution frame */
		SPELLframeManager*       m_frameManager;
		/** Current procedure or file name */
		std::string              m_procedure;
		/** Current procedures path */
		std::string              m_procPath;
		/** Current library path */
		std::string              m_libPath;
};

/*@}*/
#endif
