// ################################################################################
// FILE       : SPELLexecutorModel.H
// DATE       : Mar 17, 2011
// PROJECT    : SPELL
// DESCRIPTION: Information of a given executor
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

#ifndef __SPELL_EXEC_MODEL_H__
#define __SPELL_EXEC_MODEL_H__

// FILES TO INCLUDE //////////////////////////////////////////////////////////
// System includes -----------------------------------------------------------
// Project includes ----------------------------------------------------------
#include "SPELL_UTIL/SPELLbase.H"
#include "SPELL_EXC/SPELLexecutorStatus.H"
#include "SPELL_EXC/SPELLuserAction.H"
#include "SPELL_SYN/SPELLmutex.H"
// Local includes ------------------------------------------------------------
#include "SPELL_CTX/SPELLexecutorStartupParams.H"
#include "SPELL_CTX/SPELLopenMode.H"

/** \addtogroup SPELL_CTX */
/*@{*/


// FORWARD REFERENCES ////////////////////////////////////////////////////////
class SPELLexecutor;
// TYPES /////////////////////////////////////////////////////////////////////
// DEFINES ///////////////////////////////////////////////////////////////////


//////////////////////////////////////////////////////////////////////////////
/**
 ** \brief Executor information class
 **
 ** \par Description:
 **
 ** \par Usage:
 **
 ** \par Synchronization:
 **
**////////////////////////////////////////////////////////////////////////////
class SPELLexecutorModel
{
public: //--------------------------------------------------------------------

    // EXCEPTIONS ////////////////////////////////////////////////////////////
    // ENUMS /////////////////////////////////////////////////////////////////
    // TYPES /////////////////////////////////////////////////////////////////
    // LIFECYCLE /////////////////////////////////////////////////////////////

		//////////////////////////////////////////////////////////////////////
		/** Constructor.
		**////////////////////////////////////////////////////////////////////
		SPELLexecutorModel( const SPELLexecutorStartupParams& config );

		//////////////////////////////////////////////////////////////////////
		/** Destructor.
		**////////////////////////////////////////////////////////////////////
		~SPELLexecutorModel();

    // STATIC ////////////////////////////////////////////////////////////////
    // METHODS ///////////////////////////////////////////////////////////////

		SPELLexecutorStatus getStatus();

		int getIpcKey();
		int getIpcPort();
		pid_t getPID();

		std::string getProcId();
		std::string getOriginId();
		std::string getGroupId();
		std::string getInstanceId();
		unsigned int getInstanceNum();
		std::string getParentInstanceId();
		int getParentCallingLine();
		std::string getArguments();
		std::string getCondition();
		SPELLopenMode getOpenMode();
		std::string getAsRunFilename();
		std::string getLogFilename();
		std::string getWsFilename();
		std::string getConfigFile();
		std::string getContextName();
		std::string getTimeId();

		void setIpcPort( int port );
		void setPID( pid_t pid );
		void setAsRunFilename( const std::string& filename );
		void setWsFilename( const std::string& filename );
		void setLogFilename( const std::string& filename );
		void setError( const std::string& error, const std::string& reason );
		std::string getErrorMessage();
		std::string getErrorReason();

	    //////////////////////////////////////////////////////////////////////
	    /** Get the current step of the executor
	     *
	     * \returns The current step if defined
	    **////////////////////////////////////////////////////////////////////
	    std::string getStageId();
	    std::string getStageTitle();
	    void setStage( const std::string& stageId, const std::string& stageTitle );
	    void setStack( const std::string& csp, const std::string& codeName );

	    //////////////////////////////////////////////////////////////////////
	    /** Get the current stack of the executor
	     *
	     * \returns The current stack
	    **////////////////////////////////////////////////////////////////////
	    std::string getStack();
	    std::string getCode();
	    SPELLuserAction& getUserAction();

    // DATA MEMBERS //////////////////////////////////////////////////////////

protected: //-----------------------------------------------------------------

		friend class SPELLexecutor;

    // EXCEPTIONS ////////////////////////////////////////////////////////////
    // ENUMS /////////////////////////////////////////////////////////////////
    // TYPES /////////////////////////////////////////////////////////////////
    // LIFECYCLE /////////////////////////////////////////////////////////////
    // METHODS ///////////////////////////////////////////////////////////////

		void setIpcKey( int ipcKey ) { m_ipcKey = ipcKey; };
		void setStatus( const SPELLexecutorStatus& status );

    // DATA MEMBERS //////////////////////////////////////////////////////////

private: //-------------------------------------------------------------------

    // EXCEPTIONS ////////////////////////////////////////////////////////////
    // ENUMS /////////////////////////////////////////////////////////////////
    // TYPES /////////////////////////////////////////////////////////////////
    // LIFECYCLE /////////////////////////////////////////////////////////////
    // METHODS ///////////////////////////////////////////////////////////////
    // DATA MEMBERS //////////////////////////////////////////////////////////

		int					m_ipcKey;
		pid_t               m_PID;
		int                 m_ipcPort;
		std::string         m_procId;
		std::string         m_instanceId;
		std::string         m_timeId;
		std::string         m_groupId;
		std::string         m_originId;
		unsigned int        m_instanceNum;
		SPELLexecutorStatus m_status;
		std::string         m_parentProcId;
		int                 m_parentCallingLine;
		std::string         m_arguments;
		SPELLopenMode		m_openMode;
		std::string         m_condition;
		std::string	        m_arFileName;
		std::string	        m_logFileName;
		std::string			m_wsFileName;
		std::string         m_configFile;
		SPELLmutex			m_lock;
		std::string			m_errorMsg;
		std::string			m_errorReason;
		std::string         m_contextName;
		/** Holds the current executor step if any*/
		std::string         m_currentStageId;
		std::string         m_currentStageTitle;
		/** Holds the current executor stack */
		std::string         m_currentStack;
		std::string         m_currentCode;
		/** User action information */
		SPELLuserAction     m_userAction;
};

/*@}*/
#endif
