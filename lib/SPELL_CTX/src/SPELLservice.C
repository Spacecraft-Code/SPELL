// ################################################################################
// FILE       : SPELLexecutor.C
// DATE       : Mar 17, 2011
// PROJECT    : SPELL
// DESCRIPTION: Implementation of the executor manager
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
#include "SPELL_CTX/SPELLservice.H"
#include "SPELL_CTX/SPELLserviceManager.H"
#include "SPELL_CTX/SPELLdataHelper.H"
#include "SPELL_CTX/SPELLcontext.H"
#include "SPELL_PRD/SPELLprocedureManager.H"
// Project includes --------------------------------------------------------
#include "SPELL_WRP/SPELLconstants.H"
#include "SPELL_CFG/SPELLconfiguration.H"
#include "SPELL_SYN/SPELLmonitor.H"
#include "SPELL_SYN/SPELLthread.H"
#include "SPELL_IPC/SPELLipcHelper.H"
#include "SPELL_UTIL/SPELLlog.H"
#include "SPELL_UTIL/SPELLutils.H"
#include "SPELL_UTIL/SPELLerror.H"
#include "SPELL_UTIL/SPELLtime.H"
#include "SPELL_IPC/SPELLipc_Executor.H"
#include "SPELL_IPC/SPELLipc_Context.H"
// System includes ---------------------------------------------------------

// DEFINES /////////////////////////////////////////////////////////////////

#define DEFAULT_START_TIMEOUT 20
#define DEFAULT_LOGIN_TIMEOUT 60


//=============================================================================
// CONSTRUCTOR: SPELLservice::SPELLservice()
//=============================================================================
SPELLservice::SPELLservice( const SPELLserviceStartupParams& config )
: SPELLexecutor(config,NULL)
{

}

//=============================================================================
// DESTRUCTOR: SPELLservice::~SPELLservice()
//=============================================================================
SPELLservice::~SPELLservice()
{

}

//=============================================================================
// METHOD: SPELLservice::start()
//=============================================================================
void SPELLservice::start()
{
	LOG_INFO("Starting service " + m_model.getInstanceId());
	m_ipc.setup();
	m_model.setIpcPort( m_ipc.getPort() );
	m_executorLoggedInEvent.clear();

	if (!m_reconnecting)
	{
		m_processStartedEvent.clear();
		std::string command;
		SPELLpyService& service = SPELLprocedureManager::instance().getService(m_model.getProcId());
        int startTimeout = -1;
        int loginTimeout = -1;
		if (service.useCommandOverride())
        {
            std::string startTimeoutStr = SPELLconfiguration::instance().getContextParameter(SPELLcontextConfig::ExecutorStartTimeout);
            std::string loginTimeoutStr = SPELLconfiguration::instance().getContextParameter(SPELLcontextConfig::ExecutorLoginTimeout);
            if (startTimeoutStr != "")
            {
                startTimeout = STRI(startTimeoutStr);
            }
            if (loginTimeoutStr != "")
            {
                loginTimeout = STRI(loginTimeoutStr);
            }
            if (startTimeout == -1)
                startTimeout = DEFAULT_START_TIMEOUT;
            if (loginTimeout == -1)
                loginTimeout = DEFAULT_LOGIN_TIMEOUT;

            LOG_INFO("Using start timeout: " + ISTR(startTimeout) + " seconds");
            LOG_INFO("Using login timeout: " + ISTR(loginTimeout) + " seconds");

            std::string config = m_model.getConfigFile();
            std::string contextName = m_model.getContextName();
            int ipcPort = m_ipc.getPort();
            std::string instanceId = m_model.getInstanceId();
            std::string warmstartFile = "";
            if (m_model.getWsFilename() != "")
            {
                warmstartFile = m_model.getWsFilename();
            }
            command = service.getCommandOverride(startTimeout,loginTimeout,config,contextName,ipcPort,instanceId,warmstartFile);
        }
		else
		{
		    command = SPELLconfiguration::instance().getContextParameter( SPELLcontextConfig::ServiceProgram );

            if (command == "")
            {
                THROW_EXCEPTION("Cannot launch service", "No service command defined in configuration", SPELL_ERROR_CONFIG);
            }


            std::string startTimeoutStr = SPELLconfiguration::instance().getContextParameter( SPELLcontextConfig::ExecutorStartTimeout );
            std::string loginTimeoutStr = SPELLconfiguration::instance().getContextParameter( SPELLcontextConfig::ExecutorLoginTimeout );
            if (startTimeoutStr != "")
            {
                startTimeout = STRI(startTimeoutStr);
            }
            if (loginTimeoutStr != "")
            {
                loginTimeout = STRI(loginTimeoutStr);
            }
            if (startTimeout == -1) startTimeout = DEFAULT_START_TIMEOUT;
            if (loginTimeout == -1) loginTimeout = DEFAULT_LOGIN_TIMEOUT;

            LOG_INFO("Using start timeout: " + ISTR(startTimeout) + " seconds");
            LOG_INFO("Using login timeout: " + ISTR(loginTimeout) + " seconds");

            command += " -c " + m_model.getConfigFile();
            command += " -n " + m_model.getContextName();
            command += " -s " + ISTR(m_ipc.getPort());
            command += " -p " + m_model.getInstanceId(); // Parent id
            command += " -w ";
            if (m_model.getWsFilename() != "")
            {
                command += " -r " + m_model.getWsFilename();
            }
		}
		if (SPELLprocessManager::instance().checkProcessExist(m_model.getInstanceId() ))
			SPELLprocessManager::instance().killProcess( m_model.getInstanceId() );
		SPELLprocessManager::instance().clearProcess( m_model.getInstanceId() );
		SPELLprocessManager::instance().startProcess( m_model.getInstanceId(), command );

		DEBUG("Waiting executor process to begin");
		bool timedOut = m_processStartedEvent.wait( startTimeout * 1000 ); // milliseconds
		if (timedOut)
		{
			THROW_EXCEPTION("Cannot launch executor", "Executor process did not begin in time", SPELL_ERROR_PROCESS);
		}
		DEBUG("Start event received");

		m_processStatus = SPELLprocessManager::instance().getProcessStatus( m_model.getInstanceId() );
		if (m_processStatus == PSTATUS_RUNNING)
		{
			if (!m_loggedIn)
			{
				DEBUG("Executor process started, waiting for login");
				bool timeout = m_executorLoggedInEvent.wait( loginTimeout * 1000 ); // milliseconds
				if (timeout)
				{
					THROW_EXCEPTION("Cannot launch executor", "Executor did not login in time", SPELL_ERROR_PROCESS);
				}
			}
			DEBUG("Executor process logged in");
			m_model.setPID( SPELLprocessManager::instance().getProcessId( m_model.getInstanceId() ));
			LOG_INFO("Executor started: " + m_model.getInstanceId() + " with pid " + ISTR(m_model.getPID()));
		}
		else if (m_processStatus == PSTATUS_FINISHED )
		{
			THROW_EXCEPTION("Cannot launch executor", "Executor process finished too quickly", SPELL_ERROR_PROCESS);
		}
		else if (m_processStatus == PSTATUS_FAILED )
		{
			THROW_EXCEPTION("Cannot launch executor", "Executor process failed to start", SPELL_ERROR_PROCESS);
		}
		else if (m_processStatus == PSTATUS_KILLED )
		{
			THROW_EXCEPTION("Cannot launch executor", "Executor process crashed or was killed", SPELL_ERROR_PROCESS);
		}
		else
		{
			THROW_EXCEPTION("Cannot launch executor", "Executor process in unexpected state: " + SPELLprocessUtils::processStatusToString(m_processStatus), SPELL_ERROR_PROCESS);
		}
	}
	else
	{
		m_loginMonitor = new LoginMonitor(*this);
		m_loginMonitor->start();
	}
}

