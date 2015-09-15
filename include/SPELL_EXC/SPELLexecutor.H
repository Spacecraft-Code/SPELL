// ################################################################################
// FILE       : SPELLexecutor.H
// DATE       : Mar 17, 2011
// PROJECT    : SPELL
// DESCRIPTION: Singleton factory for the executor
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

#ifndef __SPELL_EXECUTOR_H__
#define __SPELL_EXECUTOR_H__

// FILES TO INCLUDE ////////////////////////////////////////////////////////
// Local includes ----------------------------------------------------------
#include "SPELL_EXC/SPELLexecutorIF.H"
// Project includes --------------------------------------------------------
// System includes ---------------------------------------------------------


/** \defgroup SPELL_EXC Procedure executor (SPELL_EXC)
 *
 * This is the main module of the SPELL framework. It defines the
 * infrastructure to execute and control the execution of procedures.
 *
 * The SPELLinterpreter class is the main class for the Executor process. It
 * creates and configures all control entities and starts the execution
 * process.
 *
 * Other relevant classes are:
 *
 * - SPELLexecutor (singleton access to the executor main control class)
 * - SPELLcontroller (commmand processor)
 * - SPELLcallstack (call stack manager)
 * - SPELLframeManager (bridge to the Python execution and control API)
 * - SPELLscheduler (scheduler for wait conditions)
 * - SPELLvariableManager (for variable watches)
 *
 * */
/*@{*/


// FORWARD REFERENCES //////////////////////////////////////////////////////
// TYPES ///////////////////////////////////////////////////////////////////

//============================================================================
/** Executor binding object for the Python layer */
typedef struct PyExecutorObject_
{
    PyObject_HEAD;
}
PyExecutorObject;
//============================================================================

// GLOBALS /////////////////////////////////////////////////////////////////


//============================================================================
/** C-API function for installing the executor object on the Python environment */
void Executor_Install();
//============================================================================

// DEFINES /////////////////////////////////////////////////////////////////

//////////////////////////////////////////////////////////////////////////////
/**
 ** \brief Executor singleton access point.
 **
 ** \par Description and usage:
 **
 **		This class provides access to the SPELL executor instance.
 **
**////////////////////////////////////////////////////////////////////////////
class SPELLexecutor
{
public: //--------------------------------------------------------------------

    // EXCEPTIONS ////////////////////////////////////////////////////////////
    // ENUMS /////////////////////////////////////////////////////////////////
    // TYPES /////////////////////////////////////////////////////////////////
    // LIFECYCLE /////////////////////////////////////////////////////////////

		//////////////////////////////////////////////////////////////////////
		/** Destructor.
		**////////////////////////////////////////////////////////////////////
		~SPELLexecutor() {;};

    // STATIC ////////////////////////////////////////////////////////////////

		//////////////////////////////////////////////////////////////////////
		/** Access the executor instance
		 *
		 * \return The executor implementation instance.
		**////////////////////////////////////////////////////////////////////
		static SPELLexecutorIF& instance()
		{
		    assert(s_instance != NULL);
		    return *s_instance;
		}

		//////////////////////////////////////////////////////////////////////
		/** Set the executor instance
		 *
		 * \param executor IN: the instance to be provided.
		**////////////////////////////////////////////////////////////////////
		static void setInstance( SPELLexecutorIF* executor )
		{
			s_instance = executor;
		};

    // METHODS ///////////////////////////////////////////////////////////////

private: //-------------------------------------------------------------------

    // EXCEPTIONS ////////////////////////////////////////////////////////////
    // ENUMS /////////////////////////////////////////////////////////////////
    // TYPES /////////////////////////////////////////////////////////////////
    // LIFECYCLE /////////////////////////////////////////////////////////////

		//////////////////////////////////////////////////////////////////////
		/** Constructor
		**////////////////////////////////////////////////////////////////////
		SPELLexecutor() {;};

    // METHODS ///////////////////////////////////////////////////////////////
    // DATA MEMBERS //////////////////////////////////////////////////////////
		/** Holds the executor reference */
		static SPELLexecutorIF* s_instance;
};

//////////////////////////////////////////////////////////////////////////////
/**
 ** \brief Monitor that prevents command processing while other operations
 **  are being carried out
 **
 **
**////////////////////////////////////////////////////////////////////////////
class SPELLnoCommandProcessing
{
public:
	SPELLnoCommandProcessing()
	{
		SPELLexecutor::instance().processLock();
	}
	~SPELLnoCommandProcessing()
	{
		SPELLexecutor::instance().processUnlock();
	}
};

/*@}*/
#endif
