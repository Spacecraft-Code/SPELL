// ################################################################################
// FILE       : SPELLserverCif.H
// DATE       : Mar 17, 2011
// PROJECT    : SPELL
// DESCRIPTION: Client interface for server environment
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

#ifndef __SPELL_SERVER_CIF_H__
#define __SPELL_SERVER_CIF_H__

// FILES TO INCLUDE ////////////////////////////////////////////////////////
// System includes ---------------------------------------------------------
// Local includes ----------------------------------------------------------
#include "SPELL_CIFS/SPELLrequestProcessor.H"
#include "SPELL_CIFS/SPELLdisplayBuffer.H"
#include "SPELL_CIFS/SPELLvariableBuffer.H"
// Project includes --------------------------------------------------------
#include "SPELL_SYN/SPELLthread.H"
#include "SPELL_SYN/SPELLevent.H"
#include "SPELL_SYN/SPELLtimer.H"
#include "SPELL_UTIL/SPELLtime.H"
#include "SPELL_CIF/SPELLcif.H"
#include "SPELL_IPC/SPELLipc_Executor.H"
#include "SPELL_IPC/SPELLipcClientInterface.H"
#include "SPELL_IPC/SPELLipcInterfaceListener.H"

/** \defgroup SPELL_CIFS Server-IPC client interface (SPELL_CIFS)
 *
 * This module defines the IPC client interface used by the Executor to
 * communicate with its associated GUIs.
 *
 * */
/*@{*/


// FORWARD REFERENCES //////////////////////////////////////////////////////
// TYPES ///////////////////////////////////////////////////////////////////
// DEFINES /////////////////////////////////////////////////////////////////


//////////////////////////////////////////////////////////////////////////////
/**
 ** \brief IPC based client interface
 **
 ** \par Description and usage:
 **
 ** 	This implementation of SPELLcif uses the IPC layer to
 **  	connect to a SPELL context process.
 **
**////////////////////////////////////////////////////////////////////////////
class SPELLserverCif: public SPELLcif, public SPELLipcInterfaceListener, public SPELLtimerListener
{
public: //--------------------------------------------------------------------

    // EXCEPTIONS ////////////////////////////////////////////////////////////
    // ENUMS /////////////////////////////////////////////////////////////////
    // TYPES /////////////////////////////////////////////////////////////////
    // LIFECYCLE /////////////////////////////////////////////////////////////

		//////////////////////////////////////////////////////////////////////
		/** Constructor.
		**////////////////////////////////////////////////////////////////////
		SPELLserverCif();

		//////////////////////////////////////////////////////////////////////
		/** Destructor.
		**////////////////////////////////////////////////////////////////////
		virtual ~SPELLserverCif();

	// STATIC ////////////////////////////////////////////////////////////////
	// METHODS ///////////////////////////////////////////////////////////////


    //====================================================================
    // IPC callbacks
    //====================================================================

		//////////////////////////////////////////////////////////////////////
		/** Process IPC messages.
		 *
		 * \param msg IN: message coming from the controlling GUI via the
		 *  SPELL context.
		**////////////////////////////////////////////////////////////////////
		void processMessage( const SPELLipcMessage& msg );

		//////////////////////////////////////////////////////////////////////
		/** Process IPC requests.
		 *
		 * \param msg IN: request coming from the controlling GUI via the
		 * SPELL context.
		 *
		 * \returns The response message to be sent back to the GUI.
		**////////////////////////////////////////////////////////////////////
		SPELLipcMessage processRequest( const SPELLipcMessage& msg );

	    //////////////////////////////////////////////////////////////////////
	    /** \see SPELLipcInterfaceListener
	    **////////////////////////////////////////////////////////////////////
		void processConnectionError( int clientKey, std::string error, std::string reason );

	    //////////////////////////////////////////////////////////////////////
	    /** \see SPELLipcInterfaceListener
	    **////////////////////////////////////////////////////////////////////
	    void processConnectionClosed( int clientKey );

	//====================================================================
	// Timer callback
	//====================================================================

	    //////////////////////////////////////////////////////////////////////
	    /** \see SPELLtimer
	    **////////////////////////////////////////////////////////////////////
	    bool timerCallback( unsigned long usecs );

    //====================================================================
    // Python bindings start (\see SPELLcif class)
    //====================================================================

		//////////////////////////////////////////////////////////////////////
		/** \see SPELLcif class.
		**////////////////////////////////////////////////////////////////////
		void specificNotifyUserActionSet( const std::string& label, const unsigned int severity );

		//////////////////////////////////////////////////////////////////////
		/** \see SPELLcif class.
		**////////////////////////////////////////////////////////////////////
		void specificNotifyUserActionUnset();

		//////////////////////////////////////////////////////////////////////
		/** \see SPELLcif class.
		**////////////////////////////////////////////////////////////////////
		void specificNotifyUserActionEnable( bool enable );

		//////////////////////////////////////////////////////////////////////
		/** Notify a variable change.
		**////////////////////////////////////////////////////////////////////
		void specificNotifyVariableChange(const std::vector<SPELLvarInfo>& added,
				                  const std::vector<SPELLvarInfo>& changed,
				                  const std::vector<SPELLvarInfo>& deleted);

		//////////////////////////////////////////////////////////////////////
		/** Notify a variable scope change.
		**////////////////////////////////////////////////////////////////////
		void specificNotifyVariableScopeChange( const std::string& scopeName,
				                        const std::vector<SPELLvarInfo>& globals,
				                        const std::vector<SPELLvarInfo>& locals );

		//////////////////////////////////////////////////////////////////////
		/** See openSubprocedure
		**////////////////////////////////////////////////////////////////////
		std::string specificOpenSubprocedure( const std::string& procId,
											  int callingLine,
											  const std::string& args, bool automatic, bool blocking, bool visible );

		//////////////////////////////////////////////////////////////////////
		/** See CloseSubprocedure
		**////////////////////////////////////////////////////////////////////
		void specificCloseSubprocedure( const std::string& procId );

		//////////////////////////////////////////////////////////////////////
		/** See KillSubprocedure
		**////////////////////////////////////////////////////////////////////
		void specificKillSubprocedure( const std::string& procId );

		//////////////////////////////////////////////////////////////////////
		/** Get prompt information if any
		**////////////////////////////////////////////////////////////////////
		SPELLpromptDefinition getPromptInfo() { return m_promptDef; }

    //====================================================================
    // Python bindings end
    //====================================================================

    // DATA MEMBERS //////////////////////////////////////////////////////////

protected: //-----------------------------------------------------------------

		friend class SPELLdisplayBuffer;
		friend class SPELLvariableBuffer;

    // EXCEPTIONS ////////////////////////////////////////////////////////////
    // ENUMS /////////////////////////////////////////////////////////////////
    // TYPES /////////////////////////////////////////////////////////////////
    // STATIC ////////////////////////////////////////////////////////////////
    // LIFECYCLE /////////////////////////////////////////////////////////////
    // METHODS ///////////////////////////////////////////////////////////////

		//////////////////////////////////////////////////////////////////////
		/** Send a request to the controlling GUI.
		 *
		 * \param msg IN: request to be sent.
		 * \param timeoutSec IN: timeout for the request
		 * \returns The request response.
		**////////////////////////////////////////////////////////////////////
		SPELLipcMessage sendGUIRequest( const SPELLipcMessage& msg, unsigned long timeoutSec );

		//////////////////////////////////////////////////////////////////////
		/** Send a message to the controlling GUI.
		 *
		 * \param msg IN: message to be sent.
		**////////////////////////////////////////////////////////////////////
		void sendGUIMessage( const SPELLipcMessage& msg );

		//////////////////////////////////////////////////////////////////////
		/** Send a request to the context.
		 *
		 * \param msg IN: request to be sent.
		 * \param timeoutSec IN: timeout for the request.
		 *
		 * \returns The request response.
		**////////////////////////////////////////////////////////////////////
		SPELLipcMessage sendCTXRequest( const SPELLipcMessage& msg, unsigned long timeoutSec );

		//////////////////////////////////////////////////////////////////////
		/** Send a message to the context.
		 *
		 * \param msg IN: message to be sent.
		**////////////////////////////////////////////////////////////////////
		void sendCTXMessage( const SPELLipcMessage& msg );

		//////////////////////////////////////////////////////////////////////
		/** Complete a message for the GUI.
		 *
		 * \param msg IN: message or request to be sent
		 *
		 *////////////////////////////////////////////////////////////////////
		void completeMessage( SPELLipcMessage& msg );

		//////////////////////////////////////////////////////////////////////
		/** Wait acknowledge from client
		 *////////////////////////////////////////////////////////////////////
		void waitAcknowledge( const SPELLipcMessage& msg );

		//====================================================================
		// Shared data handling
		//====================================================================
		std::string setSharedData( const std::string& name, const std::string& value, const std::string& expected, const std::string& scope );
		std::string clearSharedData( const std::string& name, const std::string& scope );
		std::string getSharedData( const std::string& name, const std::string& scope );
		std::string getSharedDataKeys( const std::string& scope );
		std::string getSharedDataScopes();
		void removeSharedDataScope( const std::string& scope );
		void removeSharedDataScopes();
		void addSharedDataScope( const std::string& scope );

		//////////////////////////////////////////////////////////////////////
		/** Specific code of NotifyLine
		 *////////////////////////////////////////////////////////////////////
		void specificNotifyLine();

		//////////////////////////////////////////////////////////////////////
		/** Specific code of NotifyCall
		 *////////////////////////////////////////////////////////////////////
		void specificNotifyCall();

		//////////////////////////////////////////////////////////////////////
		/** Specific code of NotifyReturn
		 *////////////////////////////////////////////////////////////////////
		void specificNotifyReturn();

		//////////////////////////////////////////////////////////////////////
		/** Specific code of NotifyStatus
		 *////////////////////////////////////////////////////////////////////
		void specificNotifyStatus( const SPELLstatusInfo& st );

		//////////////////////////////////////////////////////////////////////
		/** Specific code of Notify
		 *
		 * \param notification IN: Notification
		 *////////////////////////////////////////////////////////////////////
		void specificNotify( ItemNotification notification );

		//////////////////////////////////////////////////////////////////////
		/** Specific code for NotifyError
		 *
		 * \param error IN: error message
		 * \param reason IN: error cause, if known
		 * \param fatal IN: true if the error cannot be recovered
		**////////////////////////////////////////////////////////////////////
		void specificNotifyError( const std::string& error, const std::string& reason, bool fatal );

		//////////////////////////////////////////////////////////////////////
		/** Specific code of Write
		 *////////////////////////////////////////////////////////////////////
		void specificWrite( const std::string& msg, unsigned int scope = 2 );

		//////////////////////////////////////////////////////////////////////
		/** Specific code of Warning
		 *////////////////////////////////////////////////////////////////////
		//virtual void warning( const std::string& msg, unsigned int scope = 2) = 0;
		void specificWarning( const std::string& msg, unsigned int scope = 2 );

		//////////////////////////////////////////////////////////////////////
		/** Specific code of Error
		 *////////////////////////////////////////////////////////////////////
		void specificError( const std::string& msg, unsigned int scope = 2 );

		//////////////////////////////////////////////////////////////////////
		/** Specific code for Prompt
		 *////////////////////////////////////////////////////////////////////
		void specificPrompt( const SPELLpromptDefinition& def, std::string& rawAnswer, std::string& answerToShow );

		//////////////////////////////////////////////////////////////////////
		/** \see SPELLcif::setup()
		**////////////////////////////////////////////////////////////////////
		void specificSetup( const SPELLcifStartupInfo& info );

		//////////////////////////////////////////////////////////////////////
		/** \see SPELLcif::cleanup()
		**////////////////////////////////////////////////////////////////////
		void specificCleanup( bool force );




		//////////////////////////////////////////////////////////////////////
		/** Prepare message for the GUI.
		 *
		 * \param msg IN: message or request to be sent
		 * \param dataType IN: Specifies the data type of the message
		 * \param msgType IN: specifies the message type
		 *////////////////////////////////////////////////////////////////////
		void prepareMessage( SPELLipcMessage& msg,  const std::string dataType, SPELLipcMessageType_ msgType );


    // DATA MEMBERS //////////////////////////////////////////////////////////

private: //-------------------------------------------------------------------

    // EXCEPTIONS ////////////////////////////////////////////////////////////
    // ENUMS /////////////////////////////////////////////////////////////////
    // TYPES /////////////////////////////////////////////////////////////////
    // LIFECYCLE /////////////////////////////////////////////////////////////
    // METHODS ///////////////////////////////////////////////////////////////

		//////////////////////////////////////////////////////////////////////
		/** Send the login message to the SPELL context.
		 *
		 * \returns The login response from the context.
		**////////////////////////////////////////////////////////////////////
		SPELLipcMessage login();

		//////////////////////////////////////////////////////////////////////
		/** Send the logout message to the SPELL context.
		**////////////////////////////////////////////////////////////////////
		void logout();

		//////////////////////////////////////////////////////////////////////
		/** Process the login response and extract configuration parameters.
		 *
		 * \param loginResp IN: response sent by context after login.
		**////////////////////////////////////////////////////////////////////
		void processLogin( const SPELLipcMessage& loginResp );

		//////////////////////////////////////////////////////////////////////
		/** Process messages regarding GUI commands
		**////////////////////////////////////////////////////////////////////
		void processMessageCommand( const SPELLipcMessage& msg );

		//////////////////////////////////////////////////////////////////////
		/** Start a prompt processing
		**////////////////////////////////////////////////////////////////////
		void startPrompt();

		//////////////////////////////////////////////////////////////////////
		/** Cancel prompt processing
		**////////////////////////////////////////////////////////////////////
		void cancelPrompt();

		//////////////////////////////////////////////////////////////////////
		/** Wait for prompt answer to arrive
		**////////////////////////////////////////////////////////////////////
		void waitPromptAnswer();

		//////////////////////////////////////////////////////////////////////
		/** Set closing flag
		**////////////////////////////////////////////////////////////////////
		void setClosing();

		//////////////////////////////////////////////////////////////////////
		/** Get closing flag
		**////////////////////////////////////////////////////////////////////
		bool isClosing();

    // DATA MEMBERS //////////////////////////////////////////////////////////
		/** Holds the IPC interface to communicate with the SPELL context */
		SPELLipcClientInterface* m_ifc;
		/** Buffer to accumulate display messages */
		SPELLdisplayBuffer* m_buffer;
		/** Buffer to accumulate WOV messages */
		SPELLvariableBuffer* m_wovBuffer;


		/** True when the CIF is ready to work */
		bool m_ready;
		/** Optimization: write message skeleton */
		SPELLipcMessage m_wrMessage;
		/** Optimization: line notification request skeleton */
		SPELLipcMessage m_lnMessage;
		/** Optimization: item notification request skeleton */
		SPELLipcMessage m_ntMessage;
		/** Optimization: status notification request skeleton */
		SPELLipcMessage m_stMessage;
		/** Processes incoming messages and requests */
		SPELLrequestProcessor m_processor;
		/** Sequence counter */
		unsigned long m_sequence;
		/** Stack sequence counter */
		unsigned long m_sequenceStack;
		/** Holds the active prompt configuration */
		SPELLpromptDefinition m_promptDef;
		/** Prompt response arrival event */
		SPELLevent m_promptAnswerEvent;
		/** Holds the active prompt message */
		SPELLipcMessage m_promptMessage;
		/** Holds the prompt answer message */
		SPELLipcMessage m_promptAnswer;

		/** IPC synchronization */
		SPELLmutex m_ipcLock;
		/** Close synchronization */
		SPELLmutex m_closeLock;
		/** Closing flag */
		bool m_closing;
		/** Timeout in msec for executor requests to GUI */
		unsigned int m_ipcTimeoutGuiRequestMsec;
		/** Timeout in msec for executor requests to context */
		unsigned int m_ipcTimeoutCtxRequestMsec;
		/** Timeout in msec for opening a subprocedure */
		unsigned int m_timeoutOpenProcMsec;
		/** Timeout in msec for executor login response */
		unsigned int m_timeoutExecLoginMsec;
		/** Timeout in seconds for acknowledge messages */
		unsigned int m_timeoutAcknowledgeSec;
		/** Number of messages not acknowledged */
		unsigned int m_numNotAcknowledged;
		/** Timer to ensure correct line alignment with GUI */
		SPELLtimer m_lineTimer;


		/** Notification synchronization */
		SPELLmutex m_ackLock;
		std::vector<std::string> m_ackSequences;
		bool m_ipcInterruptionNotified;

};

/*@}*/
#endif
