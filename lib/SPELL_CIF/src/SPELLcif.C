// ################################################################################
// FILE       : SPELLcif.C
// DATE       : Mar 17, 2011
// PROJECT    : SPELL
// DESCRIPTION: Implementation of the CIF
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
#include "SPELL_CIF/SPELLcif.H"
// Project includes --------------------------------------------------------
#include "SPELL_UTIL/SPELLlog.H"
#include "SPELL_WRP/SPELLconstants.H"
#include "SPELL_CFG/SPELLconfiguration.H"
#include "SPELL_EXC/SPELLexecutor.H"
#include "SPELL_EXC/SPELLexecutorUtils.H"

#include "SPELL_SYN/SPELLmonitor.H"

// DEFINES /////////////////////////////////////////////////////////////////
// GLOBALS /////////////////////////////////////////////////////////////////


// STATIC //////////////////////////////////////////////////////////////////

//=============================================================================
// CONSTRUCTOR: SPELLcif::SPELLcif
//=============================================================================
SPELLcif::SPELLcif()
: m_config()
{
    m_asRun = NULL;
    m_verbosity = -1;
    m_manualMode = false;
    m_timeId = "";
    m_ctxName = "";
    m_ctxPort = 0;
    m_notificationsEnabled = true;

    m_finishEvent.clear();
}

//=============================================================================
// DESTRUCTOR: SPELLcif::~SPELLcif
//=============================================================================
SPELLcif::~SPELLcif()
{
    if (m_asRun != NULL)
    {
        delete m_asRun;
        m_asRun = NULL;
    }
}

//=============================================================================
// METHOD    : SPELLcif::setup
//=============================================================================
void SPELLcif::setup( const SPELLcifStartupInfo& info )
{
    // Store the configuration values
    m_procId = info.procId;
    m_timeId = info.timeId;
    m_ctxName = info.contextName;
    m_ctxPort = info.contextPort;

    // Retrieve the verbosity value. For that we need to get the context
    // configuration parameters from the XML config file.
    const SPELLcontextConfig& ctx = SPELLconfiguration::instance().getContext(m_ctxName);

    // Create the AsRUN file manager
	m_asRun = new SPELLasRun( ctx, m_timeId, m_procId );

	specificSetup(info);
}

//=============================================================================
// METHOD    : SPELLcif::cleanup
//=============================================================================
void SPELLcif::cleanup( bool force )
{
    // Nothing to do

	specificCleanup( force );

}

//=============================================================================
// METHOD    : SPELLcif::useAsRun
//=============================================================================
void SPELLcif::useAsRun( const std::string& asrunFile )
{
	m_asRun->substituteFile( asrunFile );
}

//=============================================================================
// METHOD    : SPELLcif::clearAsRun
//=============================================================================
void SPELLcif::clearAsRun()
{
    m_asRun->clear();
}

//=============================================================================
// METHOD    : SPELLcif::setManualMode
//=============================================================================
void SPELLcif::setManualMode( bool manual )
{
    m_manualMode = manual;
}

//=============================================================================
// METHOD    : SPELLcif::setVerbosity
//=============================================================================
void SPELLcif::setVerbosity( int verbosity )
{
    m_verbosity = verbosity;
}
//=============================================================================
// METHOD    : SPELLcif::setVerbosityFilter
//=============================================================================
void SPELLcif::setVerbosityFilter( int verbosity )
{
	m_config.setMaxVerbosity(verbosity);
}

//=============================================================================
// METHOD    : SPELLcif::setMaxVerbosity
//=============================================================================
void SPELLcif::setMaxVerbosity()
{
    m_verbosity = 10;
}

//=============================================================================
// METHOD    : SPELLcif::resetVerbosity
//=============================================================================
void SPELLcif::resetVerbosity()
{
    m_verbosity = -1;
}

//=============================================================================
// METHOD    : SPELLcif::getVerbosity
//=============================================================================
const int SPELLcif::getVerbosity() const
{
    return m_verbosity;
}

//=============================================================================
// METHOD    : SPELLcif::isManual
//=============================================================================
const bool SPELLcif::isManual() const
{
    return m_manualMode;
}

//=============================================================================
// METHOD    : SPELLcif::getVerbosityFilter
//=============================================================================
const int SPELLcif::getVerbosityFilter() const
{
	return m_config.getMaxVerbosity();
}

//=============================================================================
// METHOD    : SPELLcif::getProcId
//=============================================================================
const std::string SPELLcif::getProcId() const
{
    return m_procId;
}

//=============================================================================
// METHOD    : SPELLcif::getStack
//=============================================================================
const std::string SPELLcif::getStack() const
{
    return SPELLexecutor::instance().getCallstack().getStack();
}

//=============================================================================
// METHOD    : SPELLcif::getCodeName
//=============================================================================
const std::string SPELLcif::getCodeName() const
{
    return SPELLexecutor::instance().getCallstack().getCodeName();
}

//=============================================================================
// METHOD    : SPELLcif::getStage
//=============================================================================
const std::string SPELLcif::getStage() const
{
    return SPELLexecutor::instance().getCallstack().getStage();
}

//=============================================================================
// METHOD    : SPELLcif::toAsRun
//=============================================================================
const std::string SPELLcif::getAsRunName() const
{
    if (m_asRun == NULL) return "";
    return m_asRun->getFileName();
}

//=============================================================================
// METHOD    : SPELLcif::getNumExecutions
//=============================================================================
unsigned int SPELLcif::getNumExecutions() const
{
	return SPELLexecutor::instance().getFrameManager().getCurrentTraceModel().getCurrentLineExecutions();
}

//=============================================================================
// METHOD    : SPELLcif::getAvailableStack()
//=============================================================================
std::string SPELLcif::getAvailableStack()
{
	std::string stack = SPELLexecutor::instance().getCallstack().getFullStack();

    if (stack == "")
    {
    	stack = SPELLexecutor::instance().getCallstack().getStack();
    }

    return stack;
} //std::string getAvailableStack()

//=============================================================================
// METHOD    : SPELLcif::notifyLine
//=============================================================================
void  SPELLcif::notifyLine()
{
	// Local variables
	std::string stack = "";

	//Protection Lock
	SPELLmonitor m( m_lineLock );

	//Check errors and exit on error
	if (m_errorState)
	{
		return;
	} //errorState true
	
	//Get stack info
	stack = getAvailableStack();

	//Check repeated stack
	if (m_lastStack == stack)
	{
		return;
	} //lastStack == stack

	//Update last Stack used for line notifications
    m_lastStack = stack;

	//Log in AsRun
    m_asRun->writeLine( stack + "/" + ISTR(getNumExecutions()) );

	//Run Implementation Specifics
	specificNotifyLine();

} //SPELLcif::notifyLine()


//=============================================================================
// METHOD    : SPELLcif::notifyCall
//=============================================================================
void  SPELLcif::notifyCall()
{
	//Debug
	DEBUG("[CIF] Procedure call");

	// Local variable declaration
	std::string stack = "";

	// check error
	if (m_errorState) return;

	//Get stack info
	stack = getAvailableStack() + "/" + ISTR(getNumExecutions());

	// log in AsRun
	m_asRun->writeCall( stack );

	//Run Implementation Specifics
	specificNotifyCall();

} //SPELLcif::notifyCall()


//=============================================================================
// METHOD: SPELLcif::notifyReturn
//=============================================================================
void SPELLcif::notifyReturn()
{
	//Debug
	DEBUG("[CIF] Procedure return");

	// check error
	if (m_errorState) return;

	// log in AsRun
	m_asRun->writeReturn();

	//Run Implementation Specifics
	specificNotifyReturn();

} //void SPELLcif::notifyReturn()


//=============================================================================
// METHOD: SPELLcif::notifyStatus
//=============================================================================
void SPELLcif::notifyStatus( const SPELLstatusInfo& st )
{
	//Debug
	DEBUG("Status notification: " + SPELLexecutorUtils::statusToString(st.status) + " (" + st.condition + ")");

	m_asRun->writeStatus( st.status );

	//Run Implementation Specifics
	specificNotifyStatus( st );

} //void SPELLcif::notifyStatus( const SPELLstatusInfo& st )


//=============================================================================
// METHOD: SPELLcif::notifyStatus
//=============================================================================
void SPELLcif::notify( ItemNotification notification ) {

	//Exit when notifications not enabled
	if (!notificationsEnabled()) return;

	// log in AsRun
    m_asRun->writeItem( getAvailableStack() + "/" + ISTR(getNumExecutions()),
						NOTIF_TYPE_STR[notification.type],
                        notification.name,
                        notification.value,
                        notification.status,
                        notification.getTokenizedComment(),
                        notification.getTokenizedTime() );

	//Run Implementation Specifics
	specificNotify( notification );

} //void notify( ItemNotification notification )


//=============================================================================
// METHOD: SPELLserverCif::notifyError
//=============================================================================
void SPELLcif::notifyError( const std::string& error, const std::string& reason, bool fatal )
{
	// log error
	LOG_ERROR("[CIF] Error notification: " + error + " (" + reason + ")");

	// set error flag
	m_errorState = true;

	// log in AsRun
	m_asRun->writeErrorInfo( error, reason );

	//Run Implementation Specifics
	specificNotifyError( error, reason, fatal );

} //void SPELLcif::notifyError( const std::string& error, const std::string& reason, bool fatal )


//=============================================================================
// METHOD: SPELLcif::write
//=============================================================================
void SPELLcif::write( const std::string& msg, unsigned int scope )
{
	//Control verbosity, errors & scope
	if ( getVerbosity() > getVerbosityFilter() ) return;

	if (m_errorState && scope != LanguageConstants::SCOPE_SYS) return;

	// log in AsRun
	m_asRun->writeInfo( getStack(), msg, scope );

	//Run Implementation Specifics
	specificWrite( msg, scope );

} //void SPELLcif::write( const std::string& msg, unsigned int scope )


//=============================================================================
// METHOD: SPELLcif::warning
//=============================================================================
void SPELLcif::warning( const std::string& msg, unsigned int scope )
{
	//DEBUG("[CIF] Warning message: " + msg);

	//Control verbosity, errors & scope
	if ( getVerbosity() > getVerbosityFilter() ) return;

	// log in AsRun
	m_asRun->writeWarning( getStack(), msg, scope );

	//Run Implementation Specifics
	specificWarning( msg, scope );

} //void SPELLcif::warning( const std::string& msg, unsigned int scope )


//=============================================================================
// METHOD: SPELLcif::error
//=============================================================================
void SPELLcif::error( const std::string& msg, unsigned int scope )
{
	//DEBUG("[CIF] Error message: " + msg);

	//Control verbosity, errors & scope
	if ( getVerbosity() > getVerbosityFilter() ) return;

    // log in AsRun
	m_asRun->writeError( getStack(), msg, scope );

	//Run Implementation Specifics
	specificError( msg, scope );

} //void error( const std::string& msg, unsigned int scope = 2 )


//=============================================================================
// METHOD: SPELLcif::prompt
//=============================================================================
std::string  SPELLcif::prompt( const SPELLpromptDefinition& def )
{
	// Local variables
	std::string answerToShow= "";
	std::string rawAnswer = "";

	// Debug trace
	DEBUG("[CIF] Prompt message");

	// Write the prompt in the asrun
	m_asRun->writePrompt( getStack(), def );

	//Run Implementation Specifics
	specificPrompt( def, rawAnswer, answerToShow );

	// Write the answer in asrun
	m_asRun->writeAnswer( getStack(), rawAnswer, def.scope );

	return answerToShow;
} //std::string  SPELLcif::prompt( const SPELLpromptDefinition& definition )


//=============================================================================
// METHOD: SPELLcif::canClose
//=============================================================================
void SPELLcif::canClose()
{
    m_finishEvent.set();
}

//=============================================================================
// METHOD: SPELLcif::waitClose
//=============================================================================
void SPELLcif::waitClose()
{
    m_finishEvent.wait();
}

//=============================================================================
// METHOD: SPELLcif::resetClose
//=============================================================================
void SPELLcif::resetClose()
{
    // Reset the last notified info. In case of recovery we want
    // the initial line notification.
    m_lastStack = "";
    m_errorState = false;
    // Clear the finish event so that we can wait for final commands next time
    if (!m_finishEvent.isClear())
    {
        m_finishEvent.clear();
    }
}


//=============================================================================
// METHOD: SPELLcif::notifyUserActionSet
//=============================================================================
void SPELLcif::notifyUserActionSet( const std::string& label, const unsigned int severity )
{
	m_asRun->writeUserActionSet( getStack(), label, severity );

	specificNotifyUserActionSet( label, severity );
}


//=============================================================================
// METHOD: SPELLcif::notifyUserActionUnset
//=============================================================================
void SPELLcif::notifyUserActionUnset()
{
	m_asRun->writeUserActionUnset(getStack());

	specificNotifyUserActionUnset();
}


//=============================================================================
// METHOD: SPELLcif::notifyUserActionEnable
//=============================================================================
void SPELLcif::notifyUserActionEnable( bool enable )
{
	m_asRun->writeUserActionEnable( getStack(), enable );

	specificNotifyUserActionEnable( enable );
}


//=============================================================================
// METHOD: SPELLcif::notifyVariableChange()
//=============================================================================
void SPELLcif::notifyVariableChange( const std::vector<SPELLvarInfo>& added,
										   const std::vector<SPELLvarInfo>& changed,
		                                   const std::vector<SPELLvarInfo>& deleted )
{
	specificNotifyVariableChange( added, changed, deleted );
}


//=============================================================================
// METHOD: SPELLcif::notifyVariableScopeChange()
//=============================================================================
void SPELLcif::notifyVariableScopeChange( const std::string& scopeName,
		                                        const std::vector<SPELLvarInfo>& globals,
		                                        const std::vector<SPELLvarInfo>& locals )
{
	specificNotifyVariableScopeChange( scopeName, globals, locals );
}


//=============================================================================
// METHOD: SPELLcif::openSubprocedure
//=============================================================================
std::string SPELLcif::openSubprocedure( const std::string& procId, int callingLine, const std::string& args, bool automatic, bool blocking, bool visible )
{
	return specificOpenSubprocedure( procId, callingLine, args, automatic, blocking, visible );
}

//=============================================================================
// METHOD: SPELLcif::closeSubprocedure
//=============================================================================
void SPELLcif::closeSubprocedure( const std::string& procId )
{
	specificCloseSubprocedure( procId );
}

//=============================================================================
// METHOD: SPELLcif::killSubprocedure
//=============================================================================
void SPELLcif::killSubprocedure( const std::string& procId )
{
	specificKillSubprocedure( procId );
}
