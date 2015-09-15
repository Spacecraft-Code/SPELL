// ################################################################################
// FILE       : SPELLautomaticCif.C
// DATE       : Mar 17, 2011
// PROJECT    : SPELL
// DESCRIPTION: Implementation of the automatic (non-interactive) CIF
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
#include "SPELL_CIFC/SPELLautomaticCif.H"
#include "SPELL_CIF/SPELLcifHelper.H"
// Project includes --------------------------------------------------------
#include "SPELL_UTIL/SPELLlog.H"
#include "SPELL_UTIL/SPELLpythonHelper.H"
#include "SPELL_EXC/SPELLexecutor.H"
#include "SPELL_EXC/SPELLexecutorUtils.H"
#include "SPELL_CFG/SPELLconfiguration.H"
#include "SPELL_WRP/SPELLconstants.H"
#include "SPELL_EXC/SPELLcommand.H"
#include "SPELL_CFG/SPELLbrowsableLibMode.H"

// DEFINES /////////////////////////////////////////////////////////////////


//=============================================================================
// CONSTRUCTOR: SPELLautomaticCif::SPELLautomaticCif
//=============================================================================
SPELLautomaticCif::SPELLautomaticCif( const std::string& promptFile, const std::string& procArguments )
: SPELLcif(),
  m_promptFilename(promptFile)
{
	getExecutorConfig().setArguments(procArguments);
	loadPromptAnswers();
}

//=============================================================================
// DESTRUCTOR: SPELLautomaticCif::~SPELLautomaticCif
//=============================================================================
SPELLautomaticCif::~SPELLautomaticCif()
{
}

//=============================================================================
// METHOD: SPELLautomaticCif::specificSetup
//=============================================================================
void SPELLautomaticCif::specificSetup( const SPELLcifStartupInfo& info )
{
    DEBUG("[CIF] Installed automatic CIF");

	const SPELLcontextConfig& ctx = SPELLconfiguration::instance().getContext(m_ctxName);

	// Obtain the verbosity value
	std::string max = ctx.getExecutorParameter( ExecutorConstants::MaxVerbosity );
	if (max == PythonConstants::None)
	{
		// Default value
		setVerbosityFilter(10);
		getExecutorConfig().setMaxVerbosity(10);
	}
	else
	{
		setVerbosityFilter(STRI(max));
		getExecutorConfig().setMaxVerbosity(STRI(max));
	}

	getExecutorConfig().setContextName(m_ctxName);
	getExecutorConfig().setVisible(true);
	getExecutorConfig().setBlocking(true);
	getExecutorConfig().setAutomatic(true);
	getExecutorConfig().setHeadless(true);
	getExecutorConfig().setBrowsableLib(DISABLED); //default value

	getExecutorConfig().setRunInto( (ctx.getExecutorParameter(ExecutorConstants::RunInto) == PythonConstants::True) );
	getExecutorConfig().setByStep( (ctx.getExecutorParameter(ExecutorConstants::ByStep) == PythonConstants::True) );
	getExecutorConfig().setExecDelay( STRI((ctx.getExecutorParameter(ExecutorConstants::ExecDelay))) );
	getExecutorConfig().setPromptWarningDelay( STRI((ctx.getExecutorParameter(ExecutorConstants::PromptDelay))) );
	getExecutorConfig().setBrowsableLib(  stringToBrowsableLibMode(ctx.getExecutorParameter(ExecutorConstants::BrowsableLib) ) );
	getExecutorConfig().setForceTcConfirm( (ctx.getExecutorParameter(ExecutorConstants::ForceTcConfirm) == PythonConstants::True) );
	std::string saveMode = ctx.getExecutorParameter(ExecutorConstants::SaveStateMode);
	getExecutorConfig().setSaveStateMode( saveMode );

	std::string wvMode = ctx.getExecutorParameter(ExecutorConstants::WatchVariables);
	bool wvEnabled = wvMode == ExecutorConstants::ENABLED;
	getExecutorConfig().setWatchEnabled( wvEnabled );
}

//=============================================================================
// METHOD: SPELLautomaticCif::cleanup
//=============================================================================
void SPELLautomaticCif::specificCleanup( bool force )
{
    //Nothing to do
}


//=============================================================================
// METHOD: SPELLautomaticCif::specificNotifyLine
//=============================================================================
void SPELLautomaticCif::specificNotifyLine()
{
	std::cout << "[  LINE  ] " << getStack() << std::endl;
}

//=============================================================================
// METHOD: SPELLautomaticCif::notifyCall
//=============================================================================
void SPELLautomaticCif::specificNotifyCall()
{
	std::cout << "[  CALL  ] " << getStack() << std::endl;
}

//=============================================================================
// METHOD: SPELLautomaticCif::notifyReturn
//=============================================================================
void SPELLautomaticCif::specificNotifyReturn()
{
	std::cout << "[ RETURN  ] " << getStack() << std::endl;
}

//=============================================================================
// METHOD: SPELLautomaticCif::notifyStatus
//=============================================================================
void SPELLautomaticCif::specificNotifyStatus( const SPELLstatusInfo& st )
{
    std::cout << "[ STATUS  ] " << SPELLexecutorUtils::statusToString(st.status) << std::endl;

    if (st.status == STATUS_PAUSED)
    {
    	ExecutorCommand cmd;
    	cmd.id = CMD_RUN;
    	SPELLexecutor::instance().command(cmd,false);
    }

}

//=============================================================================
// METHOD: SPELLautomaticCif::notifyError
//=============================================================================
void SPELLautomaticCif::specificNotifyError( const std::string& error, const std::string& reason, bool fatal )
{
    std::cout << "[  ERROR  ]" << error << ": " << reason  << " (fatal:" << BSTR(fatal) << ")" << std::endl;
}

//=============================================================================
// METHOD: SPELLautomaticCif::specificWrite
//=============================================================================
void SPELLautomaticCif::specificWrite( const std::string& msg, unsigned int scope  )
{
    std::cout << "[ DISPLAY ] " << msg << " (scope:" << scope << ")" << std::endl;
}

//=============================================================================
// METHOD: SPELLautomaticCif::warning
//=============================================================================
void SPELLautomaticCif::specificWarning( const std::string& msg, unsigned int scope  )
{
    std::cout << "[ WARNING ] " << msg << " (scope:" << scope << ")" << std::endl;
}

//=============================================================================
// METHOD: SPELLautomaticCif::specificError
//=============================================================================
void SPELLautomaticCif::specificError( const std::string& msg, unsigned int scope  )
{
    std::cout << "[  ERROR  ] " << msg << " (scope:" << scope << ")"  << std::endl;
}

//=============================================================================
// METHOD: SPELLautomaticCif::log
//=============================================================================
void SPELLautomaticCif::log( const std::string& msg )
{
}

//=============================================================================
// METHOD: SPELLautomaticCif::specificPrompt
//=============================================================================
void SPELLautomaticCif::specificPrompt( const SPELLpromptDefinition& def, std::string& rawAnswer, std::string& answerToShow )
{
	std::string answer = "";
    //std::cout << "[ PROMPT ] " << def.message << " (type:" << def.typecode << ", scope:" << def.scope << ")" << std::endl;
	std::cout << "[ PROMPT ] ";
    SPELLcifHelper::displayPrompt( def );

	if (m_promptAnswers.size()>0)
	{
		try
		{
			answer = automaticPrompt(def);
		}
		catch( SPELLcoreException& ex )
		{
			error("Unable to answer the prompt automatically: " + std::string(ex.what()), -1);
			answer = SPELLcifHelper::commandLinePrompt(def,true);
		}
	}
	else
	{
		answer = SPELLcifHelper::commandLinePrompt(def,true);
	}

	//rawAnser is the index of the available options. For numeric or text is the answer introduced by the user, answerToShow.
	//answerToShow is the value obtained from the method getResult from the rawAnswer. It is not the same as the answer introduced by the user.
	rawAnswer = SPELLcifHelper::getRawAnswer( answer, def );
	answerToShow = SPELLcifHelper::getResult( rawAnswer, def );;
} //specificPrompt

//=============================================================================
// METHOD: SPELLautomaticCif::loadPromptAnswers()
//=============================================================================
void SPELLautomaticCif::loadPromptAnswers()
{
	if (m_promptFilename == "") return;
	std::ifstream file;
	std::cout << "File: " << m_promptFilename << std::endl;
	if (!SPELLutils::pathExists(m_promptFilename))
	{
		std::cerr << "File not found: " << m_promptFilename << std::endl;
		THROW_EXCEPTION("Unable to load prompt answers", "File not found: '" + m_promptFilename + "'", SPELL_ERROR_FILESYSTEM);
	}
	file.open( m_promptFilename.c_str() );
	if (!file.is_open())
	{
		THROW_EXCEPTION("Unable to load prompt answers", "Cannot open file for read: '" + m_promptFilename + "'", SPELL_ERROR_FILESYSTEM);
	}

    while(!file.eof())
    {
        std::string line = "";
        std::getline(file,line);
        SPELLutils::trim(line);
        if (line == "") continue;
        m_promptAnswers.push_back(line);
    }
	m_promptAnswerIndex = 0;
	file.close();
}

//=============================================================================
// METHOD: SPELLautomaticCif::automaticPrompt()
//=============================================================================
std::string SPELLautomaticCif::automaticPrompt( const SPELLpromptDefinition& def )
{
	if (m_promptAnswers.size()==0)
	{
		THROW_EXCEPTION("Cannot perform automatic prompt", "No answers available", SPELL_ERROR_EXECUTION);
	}
	if (m_promptAnswerIndex==m_promptAnswers.size())
	{
		THROW_EXCEPTION("Cannot perform automatic prompt", "No more answers available", SPELL_ERROR_EXECUTION);
	}
	std::string answer = m_promptAnswers[m_promptAnswerIndex];
    std::cout << "[ ANSWER  ] " << answer << " (item " << m_promptAnswerIndex << ")" << std::endl;
	m_promptAnswerIndex++;
	return answer;
}


//=============================================================================
// METHOD: SPELLautomaticCif::specificNotifyUserActionSet
//=============================================================================
void SPELLautomaticCif::specificNotifyUserActionSet( const std::string& label, const unsigned int severity )
{
	std::cout << "[ USER ACTION SET ] " << label << "(Severity: " << severity << ")" << std::endl;
}


//=============================================================================
// METHOD: SPELLautomaticCif::specificNotifyUserActionUnset
//=============================================================================
void SPELLautomaticCif::specificNotifyUserActionUnset()
{
	std::cout << "[ USER ACTION UNSET ] " << std::endl;
}


//=============================================================================
// METHOD: SPELLautomaticCif::specificNotifyUserActionEnable
//=============================================================================
void SPELLautomaticCif::specificNotifyUserActionEnable( bool enable )
{
	std::cout << "[ USER ACTION ENABLE ] " << "(Enable: " << enable << ")" << std::endl;
}

//=============================================================================
// METHOD: SPELLautomaticCif::specificNotifyVariableChange()
//=============================================================================
void SPELLautomaticCif::specificNotifyVariableChange( const std::vector<SPELLvarInfo>& added,
										   const std::vector<SPELLvarInfo>& changed,
		                                   const std::vector<SPELLvarInfo>& deleted )
{
	std::cout << "[  NOTIFY VARIABLE CHANGE  ] " << "Variable Change" << std::endl;
}

//=============================================================================
// METHOD: SPELLautomaticCif::specificNotifyVariableScopeChange()
//=============================================================================
void SPELLautomaticCif::specificNotifyVariableScopeChange( const std::string& scopeName,
		                                        const std::vector<SPELLvarInfo>& globals,
		                                        const std::vector<SPELLvarInfo>& locals )
{
	std::cout << "[  NOTIFY VARIABLE SCOPE CHANGE  ] " << "Variable Scope Change" << std::endl;
}

//=============================================================================
// METHOD: SPELLautomaticCif::specificNotify
//=============================================================================
void SPELLautomaticCif::specificNotify( ItemNotification notification )
{
	std::cout 	<< "[  ITEM  ] " 	<< notification.name
				<< "<" 				<< NOTIF_TYPE_STR[notification.type]
				<< ">: " 			<< notification.value
				<< " ("				<< notification.status
				<< ") Comment: "	<< notification.getTokenizedComment()
				<< " Time: "		<< notification.getTokenizedTime()
				<< " Count: "		<< notification.getSuccessfulCount()
				<< std::endl;

    // Update message
    /*std::stringstream buffer;
    buffer << notification.getSuccessfulCount();
    m_ntMessage.set(MessageField::FIELD_NOTIF_ITEM_SCOUNT, buffer.str());
    */
}

//=============================================================================
// METHOD: SPELLautomaticCif::specificOpenSubprocedure
//=============================================================================
std::string SPELLautomaticCif::specificOpenSubprocedure( const std::string& procId, int callingLine, const std::string& args, bool automatic, bool blocking, bool visible )
{
	std::cout << "[  SUBPROCEDURE  ] " << "Open" << std::endl;
	return "";
}

//=============================================================================
// METHOD: SPELLautomaticCif::specificCloseSubprocedure
//=============================================================================
void SPELLautomaticCif::specificCloseSubprocedure( const std::string& procId )
{
	std::cout << "[  SUBPROCEDURE  ] " << "Close" << std::endl;
}

//=============================================================================
// METHOD: SPELLautomaticCif::specificKillSubprocedure
//=============================================================================
void SPELLautomaticCif::specificKillSubprocedure( const std::string& procId )
{
	std::cout << "[  SUBPROCEDURE  ] " << "Kill" << std::endl;
}
