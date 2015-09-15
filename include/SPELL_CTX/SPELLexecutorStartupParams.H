// ################################################################################
// FILE       : SPELLexecutorStartupParams.H
// DATE       : Feb 13, 2014
// PROJECT    : SPELL
// DESCRIPTION: Configuration for the executor process
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

#ifndef __SPELL_EXECUTOR_STARTUP_PARAMS_H__
#define __SPELL_EXECUTOR_STARTUP_PARAMS_H__

// FILES TO INCLUDE //////////////////////////////////////////////////////////
// Local includes ------------------------------------------------------------
#include "SPELL_CTX/SPELLclientMode.H"
#include "SPELL_CTX/SPELLopenMode.H"
// Project includes ----------------------------------------------------------
#include "SPELL_UTIL/SPELLbase.H"
#include "SPELL_UTIL/SPELLutils.H"

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
class SPELLexecutorStartupParams
{
public: //--------------------------------------------------------------------

	// EXCEPTIONS ////////////////////////////////////////////////////////////
	// ENUMS /////////////////////////////////////////////////////////////////
	// TYPES /////////////////////////////////////////////////////////////////
	// LIFECYCLE /////////////////////////////////////////////////////////////

		//////////////////////////////////////////////////////////////////////
		/** Constructor.
		**////////////////////////////////////////////////////////////////////
		SPELLexecutorStartupParams( const std::string& instanceId, const std::string& timeId, bool reconnecting = false )
		{
			int idx = instanceId.find("#");
			m_procId = instanceId.substr(0, idx);
			m_timeId = timeId;
			m_instanceNum = STRI(instanceId.substr(idx+1,instanceId.size()-idx));
			m_instanceId = instanceId;
			m_arguments = "";
			m_condition = "";
			m_clientMode = CLIENT_MODE_UNKNOWN;
			m_openMode = OPEN_MODE_UNKNOWN;
			m_parentInstanceId = "";
			m_groupId = "";
			m_originId = "";
			m_parentCallingLine = 0;
			m_configFile = "";
			m_contextName = "";
			m_recoveryFile = "";
			m_ipcPort = 0;
			m_PID = 0;
			m_reconnecting = reconnecting;
		}

		//////////////////////////////////////////////////////////////////////
		/** Destructor.
		**////////////////////////////////////////////////////////////////////
		~SPELLexecutorStartupParams() {};

	// METHODS ///////////////////////////////////////////////////////////////

		std::string getProcId() const { return m_procId; };
		std::string getTimeId() const { return m_timeId; };
		std::string getInstanceId() const { return m_instanceId; };
		std::string getOriginId() const { return m_originId; };
		std::string getGroupId() const { return m_groupId; };
		unsigned int getInstanceNum() const { return m_instanceNum; };

		pid_t getPID() const { return m_PID; };
		int getIpcPort() const { return m_ipcPort; };
		bool isReconnecting() const { return m_reconnecting; }

		std::string getArguments() const { return m_arguments; };
		std::string getCondition() const { return m_condition; };
		SPELLclientMode getClientMode() const { return m_clientMode; };
		SPELLopenMode getOpenMode() const { return m_openMode; };
		std::string getParentInstanceId() const { return m_parentInstanceId; };
		int getParentCallingLine() const { return m_parentCallingLine; };
		std::string getConfigFile() const { return m_configFile; };
		std::string getContextName() const { return m_contextName; };
		std::string getRecoveryFile() const { return m_recoveryFile; };

		void setPID( pid_t pid ) { m_PID = pid; };
		void setIpcPort( int ipcPort ) { m_ipcPort = ipcPort; };
		void setArguments( const std::string& arguments ) { m_arguments = arguments; };
		void setCondition( const std::string& condition ) { m_condition = condition; };
		void setClientMode( const SPELLclientMode& mode ) { m_clientMode = mode; };
		void setOpenMode( const SPELLopenMode& mode ) { m_openMode = mode; };
		void setParentInstanceId( const std::string& parentInstanceId ) { m_parentInstanceId = parentInstanceId; };
		void setGroupId( const std::string& groupId ) { m_groupId = groupId; };
		void setOriginId( const std::string& originId ) { m_originId = originId; };
		void setParentCallingLine( int lineNo ) { m_parentCallingLine = lineNo; };
		void setConfigFile( const std::string& configFile ) { m_configFile = configFile; };
		void setContextName( const std::string& ctxName ) { m_contextName = ctxName; };
		void setRecoveryFile( const std::string& recoveryFile ) { m_recoveryFile = recoveryFile; };

private: //-------------------------------------------------------------------

	// EXCEPTIONS ////////////////////////////////////////////////////////////
	// ENUMS /////////////////////////////////////////////////////////////////
	// TYPES /////////////////////////////////////////////////////////////////
	// STATIC ////////////////////////////////////////////////////////////////
	// LIFECYCLE /////////////////////////////////////////////////////////////
	// METHODS ///////////////////////////////////////////////////////////////
	// DATA MEMBERS //////////////////////////////////////////////////////////

		std::string     m_procId;
		unsigned int    m_instanceNum;
		int 			m_ipcPort;
		pid_t 			m_PID;
		bool 			m_reconnecting;
		std::string		m_instanceId;
		std::string     m_timeId;
		std::string     m_arguments;
		std::string     m_condition;
		SPELLclientMode m_clientMode;
		SPELLopenMode   m_openMode;
		std::string     m_parentInstanceId;
		std::string     m_groupId;
		std::string     m_originId;
		int 			m_parentCallingLine;
		std::string     m_configFile;
		std::string     m_contextName;
		std::string		m_recoveryFile;
};

/*@}*/
#endif
