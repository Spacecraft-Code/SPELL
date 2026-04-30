// ################################################################################
// FILE       : SPELLexecutorModel.C
// DATE       : Mar 17, 2011
// PROJECT    : SPELL
// DESCRIPTION: Implementation of the executor model
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

// FILES TO INCLUDE ////////////////////////////////////////////////////////
// Local includes ----------------------------------------------------------
#include "SPELL_CTX/SPELLexecutorModel.H"
#include "SPELL_CTX/SPELLdataHelper.H"
// Project includes --------------------------------------------------------
#include "SPELL_SYN/SPELLmonitor.H"
#include "SPELL_UTIL/SPELLlog.H"
#include "SPELL_UTIL/SPELLutils.H"
// System includes ---------------------------------------------------------


// DEFINES /////////////////////////////////////////////////////////////////


//=============================================================================
// CONSTRUCTOR: SPELLexecutorModel::SPELLexecutorModel()
//=============================================================================
SPELLexecutorModel::SPELLexecutorModel( const SPELLexecutorStartupParams& config )
: m_lock()
{
	m_procId = config.getProcId();
	m_instanceId = config.getInstanceId();
	m_timeId = config.getTimeId();
	m_instanceNum = config.getInstanceNum();
	m_parentProcId = config.getParentInstanceId();
	m_parentCallingLine = config.getParentCallingLine();
	m_groupId = config.getGroupId();
	m_originId = config.getOriginId();
	m_arguments = config.getArguments();
	m_condition = config.getCondition();
	m_openMode = config.getOpenMode();
	m_configFile = config.getConfigFile();
	m_contextName = config.getContextName();
	m_ipcKey = -1;
	m_ipcPort = config.getIpcPort();
	m_PID = config.getPID();
	m_status = STATUS_UNKNOWN;
	m_logFileName = "";
	m_wsFileName = config.getRecoveryFile();
	m_currentStageId = "";
	m_currentStageTitle = "";
}

//=============================================================================
// DESTRUCTOR: SPELLexecutorModel::~SPELLexecutorModel()
//=============================================================================
SPELLexecutorModel::~SPELLexecutorModel()
{

}

//=============================================================================
// METHOD: SPELLexecutorModel::getStatus()
//=============================================================================
SPELLexecutorStatus SPELLexecutorModel::getStatus()
{
	SPELLmonitor m(m_lock);
	return m_status;
}

//=============================================================================
// METHOD: SPELLexecutorModel::setStatus()
//=============================================================================
void SPELLexecutorModel::setStatus( const SPELLexecutorStatus& status )
{
	SPELLmonitor m(m_lock);
	m_status = status;
}

//=============================================================================
// METHOD: SPELLexecutorModel::
//=============================================================================
int SPELLexecutorModel::getIpcKey()
{
	SPELLmonitor m(m_lock);
	return m_ipcKey;
};

//=============================================================================
// METHOD: SPELLexecutorModel::
//=============================================================================
int SPELLexecutorModel::getIpcPort()
{
	SPELLmonitor m(m_lock);
	return m_ipcPort;
};

//=============================================================================
// METHOD: SPELLexecutorModel::
//=============================================================================
pid_t SPELLexecutorModel::getPID()
{
	SPELLmonitor m(m_lock);
	return m_PID;
};

//=============================================================================
// METHOD: SPELLexecutorModel::
//=============================================================================
std::string SPELLexecutorModel::getProcId()
{
	SPELLmonitor m(m_lock);
	return m_procId;
};

//=============================================================================
// METHOD: SPELLexecutorModel::
//=============================================================================
std::string SPELLexecutorModel::getOriginId()
{
	SPELLmonitor m(m_lock);
	return m_originId;
};

//=============================================================================
// METHOD: SPELLexecutorModel::
//=============================================================================
std::string SPELLexecutorModel::getGroupId()
{
	SPELLmonitor m(m_lock);
	return m_groupId;
};

//=============================================================================
// METHOD: SPELLexecutorModel::
//=============================================================================
std::string SPELLexecutorModel::getInstanceId()
{
	SPELLmonitor m(m_lock);
	return m_instanceId;
};

//=============================================================================
// METHOD: SPELLexecutorModel::
//=============================================================================
unsigned int SPELLexecutorModel::getInstanceNum()
{
	SPELLmonitor m(m_lock);
	return m_instanceNum;
};

//=============================================================================
// METHOD: SPELLexecutorModel::
//=============================================================================
std::string SPELLexecutorModel::getParentInstanceId()
{
	SPELLmonitor m(m_lock);
	return m_parentProcId;
};

//=============================================================================
// METHOD: SPELLexecutorModel::
//=============================================================================
int SPELLexecutorModel::getParentCallingLine()
{
	SPELLmonitor m(m_lock);
	return m_parentCallingLine;
};

//=============================================================================
// METHOD: SPELLexecutorModel::
//=============================================================================
std::string SPELLexecutorModel::getArguments()
{
	SPELLmonitor m(m_lock);
	return m_arguments;
};

//=============================================================================
// METHOD: SPELLexecutorModel::
//=============================================================================
std::string SPELLexecutorModel::getCondition()
{
	SPELLmonitor m(m_lock);
	return m_condition;
};

//=============================================================================
// METHOD: SPELLexecutorModel::
//=============================================================================
SPELLopenMode SPELLexecutorModel::getOpenMode()
{
	SPELLmonitor m(m_lock);
	return m_openMode;
};

//=============================================================================
// METHOD: SPELLexecutorModel::
//=============================================================================
std::string SPELLexecutorModel::getAsRunFilename()
{
	SPELLmonitor m(m_lock);
	return m_arFileName;
}

//=============================================================================
// METHOD: SPELLexecutorModel::
//=============================================================================
std::string SPELLexecutorModel::getLogFilename()
{
	SPELLmonitor m(m_lock);
	return m_logFileName;
};

//=============================================================================
// METHOD: SPELLexecutorModel::
//=============================================================================
std::string SPELLexecutorModel::getWsFilename()
{
	SPELLmonitor m(m_lock);
	return m_wsFileName;
};

//=============================================================================
// METHOD: SPELLexecutorModel::
//=============================================================================
std::string SPELLexecutorModel::getConfigFile()
{
	SPELLmonitor m(m_lock);
	return m_configFile;
};

//=============================================================================
// METHOD: SPELLexecutorModel::
//=============================================================================
std::string SPELLexecutorModel::getContextName()
{
	SPELLmonitor m(m_lock);
	return m_contextName;
};

//=============================================================================
// METHOD: SPELLexecutorModel::
//=============================================================================
std::string SPELLexecutorModel::getTimeId()
{
	SPELLmonitor m(m_lock);
	return m_timeId;
};

//=============================================================================
// METHOD: SPELLexecutorModel::
//=============================================================================
void SPELLexecutorModel::setIpcPort( int port )
{
	SPELLmonitor m(m_lock);
	m_ipcPort = port;
};

//=============================================================================
// METHOD: SPELLexecutorModel::
//=============================================================================
void SPELLexecutorModel::setPID( pid_t pid )
{
	SPELLmonitor m(m_lock);
	m_PID = pid;
};

//=============================================================================
// METHOD: SPELLexecutorModel::
//=============================================================================
void SPELLexecutorModel::setAsRunFilename( const std::string& filename )
{
	SPELLmonitor m(m_lock);
	m_arFileName = filename;
};

//=============================================================================
// METHOD: SPELLexecutorModel::
//=============================================================================
void SPELLexecutorModel::setWsFilename( const std::string& filename )
{
	SPELLmonitor m(m_lock);
	m_wsFileName = filename;
};

//=============================================================================
// METHOD: SPELLexecutorModel::
//=============================================================================
void SPELLexecutorModel::setLogFilename( const std::string& filename )
{
	SPELLmonitor m(m_lock);
	m_logFileName = filename;
};

//=============================================================================
// METHOD: SPELLexecutorModel::
//=============================================================================
void SPELLexecutorModel::setError( const std::string& error, const std::string& reason )
{
	SPELLmonitor m(m_lock);
	m_errorMsg = error;
	m_errorReason = reason;
}

//=============================================================================
// METHOD: SPELLexecutorModel::
//=============================================================================
std::string SPELLexecutorModel::getErrorMessage()
{
	SPELLmonitor m(m_lock);
	return m_errorMsg;
};

//=============================================================================
// METHOD: SPELLexecutorModel::
//=============================================================================
std::string SPELLexecutorModel::getErrorReason()
{
	SPELLmonitor m(m_lock);
	return m_errorReason;
};

//=============================================================================
// METHOD: SPELLexecutorModel::
//=============================================================================
std::string SPELLexecutorModel::getStageId()
{
	SPELLmonitor m(m_lock);
	return m_currentStageId;
}

//=============================================================================
// METHOD: SPELLexecutorModel::
//=============================================================================
std::string SPELLexecutorModel::getStageTitle()
{
	SPELLmonitor m(m_lock);
	return m_currentStageTitle;
}

//=============================================================================
// METHOD: SPELLexecutorModel::
//=============================================================================
void SPELLexecutorModel::setStage( const std::string& stageId, const std::string& stageTitle )
{
	SPELLmonitor m(m_lock);
	m_currentStageId = stageId;
	m_currentStageTitle = stageTitle;
}

//=============================================================================
// METHOD: SPELLexecutorModel::
//=============================================================================
void SPELLexecutorModel::setStack( const std::string& csp, const std::string& codeName )
{
	SPELLmonitor m(m_lock);
	m_currentStack = csp;
	m_currentCode  = codeName;
}
//=============================================================================
// METHOD: SPELLexecutorModel::
//=============================================================================
std::string SPELLexecutorModel::getStack()
{
	SPELLmonitor m(m_lock);
	return m_currentStack;
}

//=============================================================================
// METHOD: SPELLexecutorModel::
//=============================================================================
std::string SPELLexecutorModel::getCode()
{
	SPELLmonitor m(m_lock);
	return m_currentCode;
}

//=============================================================================
// METHOD: SPELLexecutorModel::
//=============================================================================
SPELLuserAction& SPELLexecutorModel::getUserAction()
{
	SPELLmonitor m(m_lock);
	return m_userAction;
};
