// ################################################################################
// FILE       : SPELLasRun.H
// DATE       : Mar 17, 2011
// PROJECT    : SPELL
// DESCRIPTION: As-Run file interface
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

#ifndef __SPELL_ASRUN_H__
#define __SPELL_ASRUN_H__

// FILES TO INCLUDE ////////////////////////////////////////////////////////
// Local includes ----------------------------------------------------------
#include "SPELL_CIF/SPELLpromptDefinition.H"
// Project includes --------------------------------------------------------
#include "SPELL_CFG/SPELLcontextConfig.H"
#include "SPELL_EXC/SPELLexecutorStatus.H"

/** \defgroup SPELL_CIF Generic client interface (SPELL_CIF)
 *
 * This module defines the generic client interface that the SPELL executor
 * process uses to communicate with the associated client or the user.
 *
 * It also defines the AsRUN file manager and the format of data notifications.
 *
 * */
/*@{*/


// FORWARD REFERENCES //////////////////////////////////////////////////////
// TYPES ///////////////////////////////////////////////////////////////////
// DEFINES /////////////////////////////////////////////////////////////////

//////////////////////////////////////////////////////////////////////////////
/**
 ** \brief AsRun file interface
 **
 ** \par Description and usage:
 **
 ** 	This class is the wrapper for the AsRUN file generation.
 **  	The client interfaces (\c SPELLcif) will dump any reported operation
 **  	to the AsRUN file for tracking purposes. AsRUN files are used by GUIs
 **  	when performing monitoring/user handover tasks.
 **
 **  	It is initialized by the SPELL client interfaces (\c SPELLcif)
 **
**////////////////////////////////////////////////////////////////////////////
class SPELLasRun
{
public: //--------------------------------------------------------------------

    // EXCEPTIONS ////////////////////////////////////////////////////////////
    // ENUMS /////////////////////////////////////////////////////////////////
    // TYPES /////////////////////////////////////////////////////////////////
    // LIFECYCLE /////////////////////////////////////////////////////////////

		//////////////////////////////////////////////////////////////////////
		/** Constructor.
		 *
		 * \param ctxConfig IN: context configuration object
		 * \param time IN: timestamp identifier for the file name
		 * \param procId IN: identifier of the procedure being run
		**////////////////////////////////////////////////////////////////////
		SPELLasRun( const SPELLcontextConfig& ctxConfig, const std::string& time, const std::string& procId );

		//////////////////////////////////////////////////////////////////////
		/** Destructor.
		**////////////////////////////////////////////////////////////////////
		virtual ~SPELLasRun();

    // METHODS ///////////////////////////////////////////////////////////////

		//////////////////////////////////////////////////////////////////////
		/** Substitute the current file by the given one
		 *
		 * \param asrunFile IN: the new asrun file
		**////////////////////////////////////////////////////////////////////
		void substituteFile( const std::string& asrunFile );

		//////////////////////////////////////////////////////////////////////
		/** Write status data to AsRun file.
		 *
		 * \param st IN: executor status code
		**////////////////////////////////////////////////////////////////////
		void writeStatus( const SPELLexecutorStatus st );

		//////////////////////////////////////////////////////////////////////
		/** Information message displayed.
		 *
		 * \param stack IN: stack information at the moment of the message
		 * \param msg IN: message text
		**////////////////////////////////////////////////////////////////////
		void writeInfo( const std::string& stack, const std::string& msg, unsigned int scope );

		//////////////////////////////////////////////////////////////////////
		/** Warning message displayed.
		 *
		 * \param stack IN: stack information at the moment of the message
		 * \param msg IN: message text
		**////////////////////////////////////////////////////////////////////
		void writeWarning( const std::string& stack, const std::string& msg, unsigned int scope );

		//////////////////////////////////////////////////////////////////////
		/** Error message displayed.
		 *
		 * \param stack IN: stack information at the moment of the message
		 * \param msg IN: message text
		**////////////////////////////////////////////////////////////////////
		void writeError( const std::string& stack, const std::string& msg, unsigned int scope );

		//////////////////////////////////////////////////////////////////////
		/** Prompt message.
		 *
		 * \param stack IN: stack information at the moment of the message
		 * \param msg IN: message text
		**////////////////////////////////////////////////////////////////////
		void writePrompt( const std::string& stack, const SPELLpromptDefinition& def );

		//////////////////////////////////////////////////////////////////////
		/** Answer to prompt.
		 *
		 * \param stack IN: stack information at the moment of the message
		 * \param msg IN: message text
		**////////////////////////////////////////////////////////////////////
		void writeAnswer( const std::string& stack, const std::string& msg, unsigned int scope );

		//////////////////////////////////////////////////////////////////////
		/** Start a child procedure
		 *
		 * \param asrun IN: corresponding asrun file
		**////////////////////////////////////////////////////////////////////
		void writeChildProc( const std::string& stack, const std::string& asrun, const std::string& arguments, const std::string& openMode );

		//////////////////////////////////////////////////////////////////////
		/** Write item status data.
		 *
		 * \param stack IN: stack information
		 * \param type IN: type of item data
		 * \param name IN: name of the item
		 * \param value IN: value of the item
		 * \param status IN: status of the item
		 * \param comment IN: comments regarding the item status
		 * \param timestamp IN: timestamp for the item status update
		**////////////////////////////////////////////////////////////////////
		void writeItem( const std::string& stack,
						const std::string& type, const std::string& name,
						const std::string& value, const std::string& status,
						const std::string& comment, const std::string& timestamp );

		//////////////////////////////////////////////////////////////////////
		/** Line info data.
		 *
		 * \param stack IN: the call stack information
		**////////////////////////////////////////////////////////////////////
		void writeLine( const std::string& stack );

		//////////////////////////////////////////////////////////////////////
		/** Call info data.
		 *
		 * \param stack IN: the stack information at the moment of the notification
		 *
		**////////////////////////////////////////////////////////////////////
		void writeCall( const std::string& stack );

		//////////////////////////////////////////////////////////////////////
		/** Return info data.
		 *
		**////////////////////////////////////////////////////////////////////
		void writeReturn();

		//////////////////////////////////////////////////////////////////////
		/** Error info data.
		 *
		 * \param error IN: error message
		 * \param reason IN: error reason
		**////////////////////////////////////////////////////////////////////
		void writeErrorInfo( const std::string& error, const std::string& reason );

		//////////////////////////////////////////////////////////////////////
		/** User action logging
		**////////////////////////////////////////////////////////////////////
		void writeUserActionSet( const std::string& stack, const std::string& label, const unsigned int severity );
		void writeUserActionUnset(const std::string& stack );
		void writeUserActionEnable( const std::string& stack, bool enable );

		//////////////////////////////////////////////////////////////////////
		/** Get the AsRUN file name.
		 *
		 * \returns The AsRUN file name.
		**////////////////////////////////////////////////////////////////////
		const std::string& getFileName() const {
			return m_fileName;
		};

		//////////////////////////////////////////////////////////////////////
		/** Clear the AsRUN file data.
		**////////////////////////////////////////////////////////////////////
		void clear();

    // DATA MEMBERS //////////////////////////////////////////////////////////

protected: //-----------------------------------------------------------------

    // EXCEPTIONS ////////////////////////////////////////////////////////////
    // ENUMS /////////////////////////////////////////////////////////////////
    // TYPES /////////////////////////////////////////////////////////////////
    // STATIC ////////////////////////////////////////////////////////////////
    // LIFECYCLE /////////////////////////////////////////////////////////////
    // METHODS ///////////////////////////////////////////////////////////////
    // DATA MEMBERS //////////////////////////////////////////////////////////

private: //-------------------------------------------------------------------

    // EXCEPTIONS ////////////////////////////////////////////////////////////
    // ENUMS /////////////////////////////////////////////////////////////////
    // TYPES /////////////////////////////////////////////////////////////////
    // LIFECYCLE /////////////////////////////////////////////////////////////
    // METHODS ///////////////////////////////////////////////////////////////

		//////////////////////////////////////////////////////////////////////
		/** Generate the asrun file name
		 *
		**////////////////////////////////////////////////////////////////////
		std::string getAsRunFilename( const SPELLcontextConfig& ctxConfig, const std::string& timeId, const std::string& procId );

		//////////////////////////////////////////////////////////////////////
		/** Writes the given string to the AsRUN file and flushes it.
		 *
		 * \param info IN: the AsRUN information already in the right format.
		**////////////////////////////////////////////////////////////////////
		void toAsRun( const std::string& info, bool increaseSequence );

    // DATA MEMBERS //////////////////////////////////////////////////////////
		/** Holds the output stream for the AsRUN file. */
		std::ofstream    m_file;
		/** Holds the AsRUN file name */
		std::string      m_fileName;
		/** Sequence counter */
		unsigned long    m_sequence;
};

/*@}*/
#endif
