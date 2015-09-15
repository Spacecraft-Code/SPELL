// ################################################################################
// FILE       : SPELLexecutorConfiguration.C
// DATE       : Mar 17, 2011
// PROJECT    : SPELL
// DESCRIPTION: Implementation of the executor configuration
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

// FILES TO INCLUDE ////////////////////////////////////////////////////////
// System includes ---------------------------------------------------------
// Local includes ----------------------------------------------------------
#include "SPELL_EXC/SPELLexecutorConfiguration.H"
// Project includes --------------------------------------------------------
#include "SPELL_UTIL/SPELLlog.H"
#include "SPELL_CFG/SPELLbrowsableLibMode.H"

// GLOBALS ////////////////////////////////////////////////////////////////////

//=============================================================================
// CONSTRUCTOR : SPELLexecutorConfig::SPELLexecutorConfig
//=============================================================================
SPELLexecutorConfig::SPELLexecutorConfig()
{
    reset();
    DEBUG("[E] SPELLexecutorConfig created");
}

//=============================================================================
// DESTRUCTOR : SPELLexecutorConfig::~SPELLexecutorConfig
//=============================================================================
SPELLexecutorConfig::~SPELLexecutorConfig()
{
    DEBUG("[E] SPELLexecutorConfig destroyed");
}


//=============================================================================
// CONSTRUCTOR : SPELLexecutorConfig::SPELLexecutorConfig
//=============================================================================
SPELLexecutorConfig::SPELLexecutorConfig( const SPELLexecutorConfig& refConfig )
{
	m_parentProcId = refConfig.getParentProcId();
	m_byStep = refConfig.isByStep();
	m_execDelay = refConfig.getExecDelay();
	m_promptWarningDelay = refConfig.getPromptWarningDelay();
	m_runInto = refConfig.isRunInto();
	m_browsableLib = refConfig.getBrowsableLib();
	m_arguments = refConfig.getArguments();
	m_condition = refConfig.getCondition();
	m_visible = refConfig.isVisible();
	m_automatic = refConfig.isAutomatic();
	m_blocking = refConfig.isBlocking();
	m_saveStateMode = refConfig.getSaveStateMode();
	m_watchVariables = refConfig.isWatchEnabled();
	m_forceTcConfirm = refConfig.isForceTcConfirm();
	m_controlClient = refConfig.getControlClient();
	m_controlHost = refConfig.getControlHost();
	m_headless = refConfig.isHeadless();
	m_maxVerbosity = refConfig.getMaxVerbosity();
	m_contextName = refConfig.getContextName();
} //Constructor SPELLexecutorConfig( SPELLexecutorConfig& other )


//=============================================================================
// Operator overload : SPELLexecutorConfig::operator=
//=============================================================================
SPELLexecutorConfig& SPELLexecutorConfig::operator=( SPELLexecutorConfig& refConfig )
{
	m_parentProcId = refConfig.getParentProcId();
	m_byStep = refConfig.isByStep();
	m_execDelay = refConfig.getExecDelay();
	m_promptWarningDelay = refConfig.getPromptWarningDelay();
	m_runInto = refConfig.isRunInto();
	m_browsableLib = refConfig.getBrowsableLib();
	m_arguments = refConfig.getArguments();
	m_condition = refConfig.getCondition();
	m_visible = refConfig.isVisible();
	m_automatic = refConfig.isAutomatic();
	m_blocking = refConfig.isBlocking();
	m_saveStateMode = refConfig.getSaveStateMode();
	m_watchVariables = refConfig.isWatchEnabled();
	m_forceTcConfirm = refConfig.isForceTcConfirm();
	m_controlClient = refConfig.getControlClient();
	m_controlHost = refConfig.getControlHost();
	m_headless = refConfig.isHeadless();
	m_maxVerbosity = refConfig.getMaxVerbosity();
	m_contextName = refConfig.getContextName();

	return *this;
} //SPELLexecutorConfig& operator=( SPELLexecutorConfig& refConfig )


//=============================================================================
// METHOD    : SPELLexecutorConfig::reset()
//=============================================================================
void SPELLexecutorConfig::reset()
{
	// Login params
    m_visible            = true;
    m_automatic          = false;
    m_blocking           = true;
    m_arguments          = "";
    m_condition          = "";
    m_browsableLib		 = DISABLED;
    m_headless			 = false;

	// Startup params
	m_byStep             = false;
    m_execDelay          = 0;
    m_promptWarningDelay = 30;
    m_runInto            = true;

    m_watchVariables 	 = false;
    m_forceTcConfirm     = false;
	m_controlClient = "";
	m_controlHost = "";
	m_contextName = "";

	m_maxVerbosity = 0;
	m_parentProcId = -1;
    m_saveStateMode = MODE_UNINIT;
}

