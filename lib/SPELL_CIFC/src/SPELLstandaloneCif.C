// ################################################################################
// FILE       : SPELLstandaloneCif.C
// DATE       : Mar 17, 2011
// PROJECT    : SPELL
// DESCRIPTION: Implementation of the CIF for standalone executor
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
// System includes ---------------------------------------------------------
// Project includes --------------------------------------------------------
#include "SPELL_UTIL/SPELLlog.H"
#include "SPELL_UTIL/SPELLpythonHelper.H"
#include "SPELL_WRP/SPELLconstants.H"
#include "SPELL_CFG/SPELLconfiguration.H"
#include "SPELL_EXC/SPELLcommand.H"
#include "SPELL_EXC/SPELLexecutor.H"
#include "SPELL_EXC/SPELLexecutorUtils.H"
// Local includes ----------------------------------------------------------
#include "SPELL_CIFC/SPELLstandaloneCif.H"



// DEFINES /////////////////////////////////////////////////////////////////


//=============================================================================
// CONSTRUCTOR: SPELLstandaloneCif::SPELLstandaloneCif
//=============================================================================
SPELLstandaloneCif::SPELLstandaloneCif()
    : SPELLcif()
{
    m_shell = NULL;
}

//=============================================================================
// DESTRUCTOR: SPELLstandaloneCif::~SPELLstandaloneCif
//=============================================================================
SPELLstandaloneCif::~SPELLstandaloneCif()
{
    if (m_shell != NULL)
    {
        delete m_shell;
        m_shell = NULL;
    }
}

//=============================================================================
// METHOD: SPELLstandaloneCif::setup
//=============================================================================
void SPELLstandaloneCif::setup( const SPELLcifStartupInfo& info )
{
    SPELLcif::setup(info);
    DEBUG("[CIF] Installed standalone CIF")
    m_shell = new SPELLncursesShell();
    DEBUG("[CIF] Launching shell")
    m_shell->start();
}

//=============================================================================
// METHOD: SPELLstandaloneCif::cleanup
//=============================================================================
void SPELLstandaloneCif::cleanup( bool force )
{
    SPELLcif::cleanup(force);
    m_shell->cleanup();
}

//=============================================================================
// METHOD: SPELLstandaloneCif::canClose
//=============================================================================
void SPELLstandaloneCif::canClose() {};

//=============================================================================
// METHOD: SPELLstandaloneCif::resetClose
//=============================================================================
void SPELLstandaloneCif::resetClose() {};

//=============================================================================
// METHOD: SPELLstandaloneCif::waitClose
//=============================================================================
void SPELLstandaloneCif::waitClose()
{
    m_shell->waitClose();
}

//=============================================================================
// METHOD: SPELLstandaloneCif::getArguments
//=============================================================================
std::string SPELLstandaloneCif::getArguments()
{
    return "";
}

//=============================================================================
// METHOD: SPELLstandaloneCif::getCondition
//=============================================================================
std::string SPELLstandaloneCif::getCondition()
{
    return "";
}

//=============================================================================
// METHOD: SPELLstandaloneCif::isAutomatic
//=============================================================================
bool SPELLstandaloneCif::isAutomatic()
{
    return false;
}

//=============================================================================
// METHOD: SPELLstandaloneCif::notifyLine
//=============================================================================
void SPELLstandaloneCif::notifyLine()
{
    m_shell->show_stack( getStack() + " (" + getStage() + ")");
}

//=============================================================================
// METHOD: SPELLstandaloneCif::notifyCall
//=============================================================================
void SPELLstandaloneCif::notifyCall()
{
    m_shell->show_stack( getStack() );
}

//=============================================================================
// METHOD: SPELLstandaloneCif::notifyReturn
//=============================================================================
void SPELLstandaloneCif::notifyReturn()
{
	// Nothing to do
}

//=============================================================================
// METHOD: SPELLstandaloneCif::notifyStatus
//=============================================================================
void SPELLstandaloneCif::notifyStatus( const SPELLstatusInfo& st )
{
    m_shell->show_status( SPELLexecutorUtils::statusToString(st.status) + " (" + st.condition + ")" );
}

//=============================================================================
// METHOD: SPELLstandaloneCif::notifyError
//=============================================================================
void SPELLstandaloneCif::notifyError( const std::string& error, const std::string& reason, bool fatal )
{
    std::string fatalStr = "(Fatal:no)";
    if (fatal)
    {
        fatalStr = "(Fatal:yes)";
    }
    m_shell->show_error( error + ": " + reason + " " + fatalStr);
}

//=============================================================================
// METHOD: SPELLstandaloneCif::write
//=============================================================================
void SPELLstandaloneCif::write( const std::string& msg, unsigned int scope )
{
    m_shell->show_info(msg);
}

//=============================================================================
// METHOD: SPELLstandaloneCif::warning
//=============================================================================
void SPELLstandaloneCif::warning( const std::string& msg, unsigned int scope )
{
    m_shell->show_warning(msg);
}

//=============================================================================
// METHOD: SPELLstandaloneCif::error
//=============================================================================
void SPELLstandaloneCif::error( const std::string& msg, unsigned int scope )
{
    m_shell->show_error(msg);
}

//=============================================================================
// METHOD: SPELLstandaloneCif::log
//=============================================================================
void SPELLstandaloneCif::log( const std::string& msg )
{
    m_shell->log(msg);
}

//=============================================================================
// METHOD: SPELLstandaloneCif::prompt
//=============================================================================
std::string SPELLstandaloneCif::prompt( const SPELLpromptDefinition& def )
{
	/** \todo Implement prompt for Standalone CIF */
    return "";
}

//=============================================================================
// METHOD: SPELLstandaloneCif::specificSetup
//=============================================================================
void SPELLstandaloneCif::specificSetup( const SPELLcifStartupInfo& info )
{
	LOG_INFO("Initializing defaults for configuration");
	//Initialize Executor Config parameters
    getExecutorConfig().setVisible(true);
    getExecutorConfig().setAutomatic(false);
    getExecutorConfig().setBlocking(true);
    getExecutorConfig().setContextName( info.contextName );
}



