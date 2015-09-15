// ################################################################################
// FILE       : SPELLcontext.H
// DATE       : Apr 11, 2011
// PROJECT    : SPELL
// DESCRIPTION: Main class of the context process
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

#ifndef __SPELL_CONTEXT_H__
#define __SPELL_CONTEXT_H__

// FILES TO INCLUDE //////////////////////////////////////////////////////////
// Local includes ------------------------------------------------------------
#include "SPELL_CTX/SPELLcontextConfiguration.H"
#include "SPELL_CTX/SPELLexecutorIPC.H"
#include "SPELL_CTX/SPELLclientIPC.H"
#include "SPELL_CTX/SPELLclient.H"
#include "SPELL_CTX/SPELLclientMode.H"
#include "SPELL_CTX/SPELLexecutorOperation.H"
#include "SPELL_CTX/SPELLsharedDataOperation.H"
#include "SPELL_CTX/SPELLlistenerIPC.H"
#include "SPELL_CTX/SPELLsharedData.H"
// Project includes ----------------------------------------------------------
#include "SPELL_SDB/SPELLdatabase.H"
#include "SPELL_EXC/SPELLexecutorStatus.H"
#include "SPELL_SYN/SPELLevent.H"
#include "SPELL_IPC/SPELLipcDataChunk.H"
#include "SPELL_CFG/SPELLexecutorDefaults.H"
#include "SPELL_CFG/SPELLbrowsableLibMode.H"

/** \addtogroup SPELL_CTX */
/*@{*/


// FORWARD REFERENCES ////////////////////////////////////////////////////////
// TYPES /////////////////////////////////////////////////////////////////////
// DEFINES ///////////////////////////////////////////////////////////////////

//////////////////////////////////////////////////////////////////////////////
/**
 ** \brief
 **
**////////////////////////////////////////////////////////////////////////////
class SPELLcontext
{
public: //--------------------------------------------------------------------

	// EXCEPTIONS ////////////////////////////////////////////////////////////
	// ENUMS /////////////////////////////////////////////////////////////////
	// TYPES /////////////////////////////////////////////////////////////////
	// LIFECYCLE /////////////////////////////////////////////////////////////

		//////////////////////////////////////////////////////////////////////
		/** Constructor.
		**////////////////////////////////////////////////////////////////////
		SPELLcontext();

		//////////////////////////////////////////////////////////////////////
		/** Destructor.
		**////////////////////////////////////////////////////////////////////
		virtual ~SPELLcontext();

	// METHODS ///////////////////////////////////////////////////////////////

		//////////////////////////////////////////////////////////////////////
		/** Start the context
		**////////////////////////////////////////////////////////////////////
		void start( const SPELLcontextConfiguration& config );

		//////////////////////////////////////////////////////////////////////
		/** Stop the context
		**////////////////////////////////////////////////////////////////////
		void stop();

		//////////////////////////////////////////////////////////////////////
		/** Get amount of active procedures
		**////////////////////////////////////////////////////////////////////
		unsigned int getNumActiveProcedures();

		//////////////////////////////////////////////////////////////////////
		/** Stop the context
		**////////////////////////////////////////////////////////////////////
		void waitFinish();

		//////////////////////////////////////////////////////////////////////
		/** Stop the context
		**////////////////////////////////////////////////////////////////////
		void readyToFinish();

		std::string getContextName() { return m_config.contextName; };

		int getListenerPort() { return m_config.listenerPort; };

		std::string getListenerHost() { return m_config.listenerHost; };

		int getClientPort() { return m_clientIPC.getPort(); };

		void notifyExecutorOperation( const SPELLexecutorOperation& op );

		void notifySharedDataOperation( const std::string& procId, const SPELLsharedDataOperation& operation, const std::string& variables, const std::string& values, const std::string& scope );
		void notifySharedDataOperation( int clientKey, const SPELLsharedDataOperation& operation, const std::string& variables, const std::string& values, const std::string& scope );
		void notifySharedDataOperation( const SPELLipcMessage& msg, const SPELLsharedDataOperation& operation, const std::string& variables, const std::string& values, const std::string& scope );

		void fillExecutorDefaults( SPELLipcMessage& msg );


		//====================================================================
		// REQUEST PROCESSING
		//====================================================================

		SPELLipcMessage openExecutor( const SPELLipcMessage& msg, SPELLclient* controllingClient );
		SPELLipcMessage getInstanceId(const SPELLipcMessage& msg);
		SPELLipcMessage getCurrentTime(const SPELLipcMessage& msg);
		SPELLipcMessage closeExecutor(const SPELLipcMessage& msg);
		SPELLipcMessage killExecutor(const SPELLipcMessage& msg);
		SPELLipcMessage recoverExecutor( const SPELLipcMessage& msg, SPELLclient* controllingClient );
		SPELLipcMessage removeControl( const SPELLipcMessage& msg );
		SPELLipcMessage setExecutorInBackground( const SPELLipcMessage& msg );
		SPELLipcMessage getProcedureList( const SPELLipcMessage& msg );
		SPELLipcMessage getExecutorList( const SPELLipcMessage& msg );
		SPELLipcMessage getProcedureProperties( const SPELLipcMessage& msg );
		SPELLipcMessage getProcedureCode( const SPELLipcMessage& msg );
		SPELLipcMessage getExecutorInfo(const SPELLipcMessage& msg);
		SPELLipcMessage attachExecutor(const SPELLipcMessage& msg, SPELLclient* client );
		SPELLipcMessage detachExecutor(const SPELLipcMessage& msg, SPELLclient* client );
		SPELLipcMessage getClientInfo(const SPELLipcMessage& msg);
		SPELLipcMessage getServerFilePath(const SPELLipcMessage& msg);
		SPELLipcMessage getProcedureRecoveryList( const SPELLipcMessage& msg );
		SPELLipcMessage getProcedureAsRunList( const SPELLipcMessage& msg );
		SPELLipcMessage deleteRecoveryFiles( const SPELLipcMessage& msg );
		SPELLipcMessage listFiles( const SPELLipcMessage& msg );
		SPELLipcMessage listDataDirectories( const SPELLipcMessage& msg );
		SPELLipcMessage getInputFile( const SPELLipcMessage& msg );

		SPELLipcMessage getExecutorDefaults( const SPELLipcMessage& msg );
		SPELLipcMessage setExecutorDefaults( const SPELLipcMessage& msg );

		//====================================================================
		// REQUESTS FOR SHARED DATA
		//====================================================================
		SPELLipcMessage setSharedData( const SPELLipcMessage& msg );
		SPELLipcMessage getSharedData( const SPELLipcMessage& msg );
		SPELLipcMessage clearSharedData( const SPELLipcMessage& msg );
		SPELLipcMessage getSharedDataKeys( const SPELLipcMessage& msg );
		SPELLipcMessage getSharedDataScopes( const SPELLipcMessage& msg );
		SPELLipcMessage addSharedDataScope( const SPELLipcMessage& msg );
		SPELLipcMessage removeSharedDataScope( const SPELLipcMessage& msg );

		//====================================================================
		// IPC FAILURE
		//====================================================================
		void clientLost( int clientKey );
		void executorLost( const std::string& procId );

	// STATIC ////////////////////////////////////////////////////////////////

		static SPELLcontext& instance();

	// DATA MEMBERS //////////////////////////////////////////////////////////

protected: //-----------------------------------------------------------------

	// EXCEPTIONS ////////////////////////////////////////////////////////////
	// ENUMS /////////////////////////////////////////////////////////////////
	// TYPES /////////////////////////////////////////////////////////////////
	// STATIC ////////////////////////////////////////////////////////////////
	// LIFECYCLE /////////////////////////////////////////////////////////////
	// METHODS ///////////////////////////////////////////////////////////////

		SPELLipcDataChunk::DataList getServerFileData( const std::string& filename );
		SPELLipcDataChunk::DataList getInputFileData( SPELLdatabase* db );
		typedef std::map<std::string,SPELLipcDataChunk*> ChunkerMap;

		void checkRecoveryFiles( const std::string& filename );

		SPELLipcDataChunk& getChunker( const SPELLipcMessage& msg );

		void clearChunker( const SPELLipcMessage& msg );

private: //-------------------------------------------------------------------

	// EXCEPTIONS ////////////////////////////////////////////////////////////
	// ENUMS /////////////////////////////////////////////////////////////////
	// TYPES /////////////////////////////////////////////////////////////////
	// STATIC ////////////////////////////////////////////////////////////////
	// LIFECYCLE /////////////////////////////////////////////////////////////
	// METHODS ///////////////////////////////////////////////////////////////
	// DATA MEMBERS //////////////////////////////////////////////////////////
		/** Holds the context configuration */
		SPELLcontextConfiguration m_config;
		/** Holds the maximum amount of procedures allowed */
		unsigned int m_maxProcs;
		/** Holds the IPC system for clients */
		SPELLclientIPC			m_clientIPC;
		/** Holds the IPC system for listener */
		SPELLlistenerIPC		m_listenerIPC;
		/** Holds the context execution default parameters */
		SPELLexecutorDefaults m_executorDefaults;
		/** Finish event */
		SPELLevent              m_eventFinish;
		/** Singleton instance */
		static SPELLcontext*  s_instance;
		/** IPC data chunkers */
		ChunkerMap m_dataChunkers;
		/** Data chunking synchronization */
		SPELLmutex m_chunkLock;
		/** Shared data blackboard */
		SPELLsharedData m_sharedData;
};

/*@}*/
#endif
