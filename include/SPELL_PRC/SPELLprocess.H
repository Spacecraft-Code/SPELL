// ################################################################################
// FILE       : SPELLprocess.H
// DATE       : Mar 17, 2011
// PROJECT    : SPELL
// DESCRIPTION: Process model
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

#ifndef __SPELL_PROCESS_H__
#define __SPELL_PROCESS_H__

// FILES TO INCLUDE ////////////////////////////////////////////////////////
// Local includes ----------------------------------------------------------
#include "SPELL_PRC/SPELLprocessStatus.H"
#include "SPELL_PRC/SPELLprocessInfo.H"
// Project includes --------------------------------------------------------
#include "SPELL_SYN/SPELLthread.H"
#include "SPELL_SYN/SPELLevent.H"
// System includes ---------------------------------------------------------

/** \defgroup SPELL_PRC Process management (SPELL_PRC)
 *
 * This module provides mechanisms to start, stop, kill and control subprocesses.
 *
 **/
/*@{*/


// FORWARD REFERENCES //////////////////////////////////////////////////////
class SPELLprocessManager;
// TYPES ///////////////////////////////////////////////////////////////////
// DEFINES /////////////////////////////////////////////////////////////////

//////////////////////////////////////////////////////////////////////////////
/**
 ** \brief Subprocess controller
 **
 ** \par Description and usage:
 **
 ** 	Model of a secondary process launched by the application. Used by the
 **     process manager to monitor the status of child processes.
 **
**////////////////////////////////////////////////////////////////////////////
class SPELLprocess : public SPELLthread
{
public: //--------------------------------------------------------------------

    // EXCEPTIONS ////////////////////////////////////////////////////////////
    // ENUMS /////////////////////////////////////////////////////////////////
    // TYPES /////////////////////////////////////////////////////////////////
    // LIFECYCLE /////////////////////////////////////////////////////////////

		//////////////////////////////////////////////////////////////////////
		/** Constructor
		 *
		 * \param identifier IN: unique identifier for this process.
		 * \param command IN: commmand (with arguments) to launch the process.
		 * \param mgr IN: process manager reference.
		**////////////////////////////////////////////////////////////////////
		SPELLprocess( std::string identifier, std::string command, SPELLprocessManager* mgr );

		//////////////////////////////////////////////////////////////////////
		/** Constructor
		 *
		 * \param identifier IN: unique identifier for this process.
		 * \param pid IN: process identifier
		 * \param mgr IN: process manager reference.
		**////////////////////////////////////////////////////////////////////
		SPELLprocess( std::string identifier, int pid, SPELLprocessManager* mgr );

		//////////////////////////////////////////////////////////////////////
		/** Destructor.
		**////////////////////////////////////////////////////////////////////
		~SPELLprocess();

    // STATIC ////////////////////////////////////////////////////////////////
    // METHODS ///////////////////////////////////////////////////////////////

		//////////////////////////////////////////////////////////////////////
		/** Start the process. (Main thread method)
		**////////////////////////////////////////////////////////////////////
		void run();

		//////////////////////////////////////////////////////////////////////
		/** Kill this process.
		**////////////////////////////////////////////////////////////////////
		void kill();

		//////////////////////////////////////////////////////////////////////
		/** Mark as about to close (meaningful only for not-child processes)
		**////////////////////////////////////////////////////////////////////
		void toClose() { m_aboutToClose = true; };

		//////////////////////////////////////////////////////////////////////
		/** Wait this process to finish.
		 *
		 * \returns The process exit code.
		**////////////////////////////////////////////////////////////////////
		int wait();

		//////////////////////////////////////////////////////////////////////
		/** Obtain the process exit code (shall be finished).
		 *
		 * \return The process exit code.
		**////////////////////////////////////////////////////////////////////
		int getReturnValue()
		{
			return m_retValue;
		};

		//////////////////////////////////////////////////////////////////////
		/** Obtain the process status.
		 *
		 * \return the process status.
		**////////////////////////////////////////////////////////////////////
		SPELLprocessStatus getStatus()
		{
			return m_statusCode;
		};

		//////////////////////////////////////////////////////////////////////
		/** Obtain the process information.
		 *
		 * \return the process information.
		**////////////////////////////////////////////////////////////////////
		SPELLprocessInfo getInfo();

    // DATA MEMBERS //////////////////////////////////////////////////////////

protected: //-----------------------------------------------------------------

    // EXCEPTIONS ////////////////////////////////////////////////////////////
    // ENUMS /////////////////////////////////////////////////////////////////
    // TYPES /////////////////////////////////////////////////////////////////
    // STATIC ////////////////////////////////////////////////////////////////
    // LIFECYCLE /////////////////////////////////////////////////////////////

    // METHODS ///////////////////////////////////////////////////////////////

		//////////////////////////////////////////////////////////////////////
		/** Popen implementation modified to be able to get the child process
		 * PID.
		 *
		 * \param command IN: command to start the process.
		 *
		 * \return The process pid.
		**////////////////////////////////////////////////////////////////////
		pid_t popen( std::string command );

    // DATA MEMBERS //////////////////////////////////////////////////////////

private: //-------------------------------------------------------------------

    // EXCEPTIONS ////////////////////////////////////////////////////////////
    // ENUMS /////////////////////////////////////////////////////////////////
    // TYPES /////////////////////////////////////////////////////////////////
    // LIFECYCLE /////////////////////////////////////////////////////////////
    // METHODS ///////////////////////////////////////////////////////////////
    // DATA MEMBERS //////////////////////////////////////////////////////////

		/** True if attaching instead of starting */
		bool                    m_notChild;
		/** Process manager reference */
		SPELLprocessManager*    m_pManager;
		/** Process PID */
		pid_t                   m_processPID;
		/** Process identifier */
		std::string             m_identifier;
		/** Process start command */
		std::string             m_command;
		/** Process exit code */
		int                     m_retValue;
		/** Process status (internal) */
		int                     m_status;
		/** Process status code */
		SPELLprocessStatus		m_statusCode;
		/** True if the process shall be killed */
		bool                    m_killIt;
		/** True if the process is expected to close */
		bool                    m_aboutToClose;
};

/*@}*/
#endif
