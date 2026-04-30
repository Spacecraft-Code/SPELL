// ################################################################################
// FILE       : SPELLasRun.C
// DATE       : Mar 17, 2011
// PROJECT    : SPELL
// DESCRIPTION: Implementation of the As-RUN interface
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
// Local includes ----------------------------------------------------------
#include "SPELL_CIF/SPELLasRun.H"
// Project includes --------------------------------------------------------
#include "SPELL_UTIL/SPELLerror.H"
#include "SPELL_UTIL/SPELLlog.H"
#include "SPELL_WRP/SPELLconstants.H"
#include "SPELL_CFG/SPELLconfiguration.H"
#include "SPELL_EXC/SPELLexecutorUtils.H"
#include "SPELL_PRD/SPELLprocedureManager.H"



// DEFINES /////////////////////////////////////////////////////////////////
static const std::string ASRUN_LOCATION = "ar";

static const std::string SEVERITY_STR[] = { "INFO", "WARN" , "ERROR" };

//=============================================================================
// CONSTRUCTOR: SPELLasRun::SPELLasRun
//=============================================================================
SPELLasRun::SPELLasRun( const SPELLcontextConfig& ctxConfig, const std::string& time, const std::string& procId )
{
	m_fileName = getAsRunFilename( ctxConfig, time, procId );
    m_file.open( m_fileName.c_str(), std::ios::out);
	m_sequence = 0;

    // Dump procedure header to the AsRun file
    try
    {
    	SPELLprocedure& proc = SPELLprocedureManager::instance().getProcedure(procId);
    	m_file << proc.getHeader() << std::endl;
    }
    catch(SPELLcoreException& ex)
    {
    	LOG_ERROR("Cannot dump procedure header to ASRUN file: " + ex.what());
    }

    toAsRun( "INIT", true );
}

//=============================================================================
// DESTRUCTOR: SPELLasRun::~SPELLasRun
//=============================================================================
SPELLasRun::~SPELLasRun()
{
	m_file.close();
}

//=============================================================================
// METHOD:    SPELLasRun::substituteFile()
//=============================================================================
void SPELLasRun::substituteFile( const std::string& asrunFile )
{
	m_file.close();
    SPELLutils::copyFile( asrunFile, m_fileName );
    m_file.open( m_fileName.c_str(), std::ios::out | std::ios::app );
}

//=============================================================================
// METHOD:    SPELLasRun::getAsRunFilename()
//=============================================================================
std::string SPELLasRun::getAsRunFilename( const SPELLcontextConfig& ctxConfig, const std::string& timeId, const std::string& procId )
{
    std::string home = SPELLutils::getSPELL_DATA();

    // Character replacements
    std::string theId = procId;
    SPELLutils::replace( theId, ".py", "" );
    SPELLutils::replace( theId, "..", "" );
    SPELLutils::replace( theId, "//", "/" );
    SPELLutils::replace( theId, PATH_SEPARATOR, "__" );

    // Get the location of AsRUN files
    std::string ddir = ctxConfig.getLocationPath( ASRUN_LOCATION );

    // Build the full file path
    std::string fileName = home + PATH_SEPARATOR + ddir + PATH_SEPARATOR;
    fileName = fileName + timeId + "_Executor_" + theId + ".ASRUN";
    LOG_INFO("[ASRUN] asRun file: " + fileName);

    return fileName;
}

//=============================================================================
// METHOD:    SPELLasRun::toAsRun()
//=============================================================================
void SPELLasRun::toAsRun( const std::string& info, bool increaseSequence )
{
    std::string line = SPELLutils::timestamp() + "\t" + ISTR(m_sequence) + "\t" + info;
    m_file << line << std::endl;
    m_file.flush();
    if (increaseSequence) m_sequence++;
}

//=============================================================================
// METHOD:    SPELLasRun::clear()
//=============================================================================
void SPELLasRun::clear()
{
	m_sequence = 0;
    m_file.close();
    m_file.open( m_fileName.c_str(), std::ios::trunc | std::ios::out );
    toAsRun( "INIT", true );
}

//=============================================================================
// METHOD:    SPELLasRun::writeStatus()
//=============================================================================
void SPELLasRun::writeStatus( const SPELLexecutorStatus st )
{
    toAsRun( STR("STATUS") + "\t\t\t" + SPELLexecutorUtils::statusToString(st), true );
}

//=============================================================================
// METHOD:    SPELLasRun::writeInfo()
//=============================================================================
void SPELLasRun::writeInfo( const std::string& stack, const std::string& msg, unsigned int scope )
{
    std::string message = msg;
    SPELLutils::replace( message, "\n", "%C%");
    SPELLutils::replace( message, "\t", "%T%");
    toAsRun( STR("DISPLAY") + "\tINFO\t" + stack + "\t" + message + "\t" + ISTR(scope), true);
}

//=============================================================================
// METHOD:    SPELLasRun::writeWarning
//=============================================================================
void SPELLasRun::writeWarning( const std::string& stack, const std::string& msg, unsigned int scope )
{
    std::string message = msg;
    SPELLutils::replace( message, "\n", "%C%");
    SPELLutils::replace( message, "\t", "%T%");
    toAsRun( STR("DISPLAY") + "\tWARN\t" + stack + "\t" + message + "\t" + ISTR(scope), true);
}

//=============================================================================
// METHOD:    SPELLasRun::writeError
//=============================================================================
void SPELLasRun::writeError( const std::string& stack, const std::string& msg, unsigned int scope )
{
    std::string message = msg;
    SPELLutils::replace( message, "\n", "%C%");
    SPELLutils::replace( message, "\t", "%T%");
    toAsRun( STR("DISPLAY") + "\tERROR\t" + stack + "\t" + message + "\t" + ISTR(scope), true);
}

//=============================================================================
// METHOD:    SPELLasRun::writePrompt
//=============================================================================
void SPELLasRun::writePrompt( const std::string& stack, const SPELLpromptDefinition& def )
{
    std::string message = def.message;
    SPELLutils::replace( message, "\n", "%C%");
    SPELLutils::replace( message, "\t", "%T%");
    toAsRun( STR("PROMPT") + "\t\t" + stack + "\t" + message + "\t" + ISTR(def.scope), true);

    toAsRun( STR("PROMPT_TYPE") + "\t" + ISTR(def.typecode), false);

    switch(def.typecode)
    {
    case LanguageConstants::PROMPT_OK:
        toAsRun( STR("PROMPT_OPTIONS") + "\tOk", false );
        toAsRun( STR("PROMPT_EXPECTED") + "\tO", false);
        break;
    case LanguageConstants::PROMPT_CANCEL:
        toAsRun( STR("PROMPT_OPTIONS") + "\tCancel", false );
        toAsRun( STR("PROMPT_EXPECTED") + "\tC", false);
        break;
    case LanguageConstants::PROMPT_OK_CANCEL:
        toAsRun( STR("PROMPT_OPTIONS") + "\tOk,,Cancel", false );
        toAsRun( STR("PROMPT_EXPECTED") + "\tO,,C", false);
        break;
    case LanguageConstants::PROMPT_YES:
        toAsRun( STR("PROMPT_OPTIONS") + "\tYes", false );
        toAsRun( STR("PROMPT_EXPECTED") + "\tY", false);
        break;
    case LanguageConstants::PROMPT_NO:
        toAsRun( STR("PROMPT_OPTIONS") + "\tNo", false );
        toAsRun( STR("PROMPT_EXPECTED") + "\tN", false);
        break;
    case LanguageConstants::PROMPT_YES_NO:
        toAsRun( STR("PROMPT_OPTIONS") + "\tYes,,No", false );
        toAsRun( STR("PROMPT_EXPECTED") + "\tY,,N", false);
        break;
    default:
        if ((def.typecode & LanguageConstants::PROMPT_LIST)>0)
        {
        	std::vector<std::string>::const_iterator it;

        	std::string options = "";
        	for( it = def.options.begin(); it != def.options.end(); it++)
        	{
        		if (!options.empty()) options += ",,";
        		options += *it;
        	}
            toAsRun( STR("PROMPT_OPTIONS") + "\t" + options, false);

        	std::string expected = "";
        	for( it = def.expected.begin(); it != def.expected.end(); it++)
        	{
        		if (!expected.empty()) expected += ",,";
        		expected += *it;
        	}
            toAsRun( STR("PROMPT_EXPECTED") + "\t" + expected, false);
        }
    	break;
    }
}

//=============================================================================
// METHOD:    SPELLasRun::writeAnswer
//=============================================================================
void SPELLasRun::writeAnswer( const std::string& stack, const std::string& msg, unsigned int scope )
{
    std::string message = msg;
    SPELLutils::replace( message, "\n", "%C%");
    SPELLutils::replace( message, "\t", "%T%");
    toAsRun( STR("ANSWER") + "\t\t" + stack + "\t" + message + "\t" + ISTR(scope), true);
}

//=============================================================================
// METHOD:    SPELLasRun::writeChildProc
//=============================================================================
void SPELLasRun::writeChildProc( const std::string& stack, const std::string& asrun, const std::string& arguments, const std::string& openMode )
{
    toAsRun( STR("CHILD") + "\t" + STR("START") + "\t" + stack + "\t" + asrun + "\t" + arguments + "\t" + openMode, true);
}

//=============================================================================
// METHOD:    SPELLasRun::writeItem
//=============================================================================
void SPELLasRun::writeItem( const std::string& stack,
							const std::string& type, const std::string& name,
                            const std::string& value, const std::string& status,
                            const std::string& comment, const std::string& timestamp )
{
    std::string theComment = " ";
    std::string theTimestamp = " ";
    if (comment != "") theComment = comment;
    if (timestamp != "" ) theTimestamp = timestamp;
    toAsRun( STR("ITEM") + "\t" + type + "\t" + stack + "\t" + name + "\t" + value + "\t" + status + "\t" + theTimestamp + "\t" + theComment, true);
}

//=============================================================================
// METHOD:    SPELLasRun::writeLine
//=============================================================================
void SPELLasRun::writeLine( const std::string& stack )
{
    toAsRun( STR("LINE") + "\t\t" + stack , true);
}

//=============================================================================
// METHOD:    SPELLasRun::writeCall
//=============================================================================
void SPELLasRun::writeCall( const std::string& stack )
{
    toAsRun( STR("CALL") + "\t\t" + stack , true);
}

//=============================================================================
// METHOD:    SPELLasRun::writeReturn
//=============================================================================
void SPELLasRun::writeReturn()
{
    toAsRun( STR("RETURN") , true);
}

//=============================================================================
// METHOD:    SPELLasRun::writeErrorInfo
//=============================================================================
void SPELLasRun::writeErrorInfo( const std::string& error, const std::string& reason )
{
    std::string errMessage = error;
    SPELLutils::replace( errMessage, "\n", "%C%");
    SPELLutils::replace( errMessage, "\t", "%T%");
    std::string rsnMessage = reason;
    SPELLutils::replace( rsnMessage, "\n", "%C%");
    SPELLutils::replace( rsnMessage, "\t", "%T%");
    toAsRun( STR("ERROR") + "\t\t\t" + errMessage + "\t" + rsnMessage, true);
}

//=============================================================================
// METHOD:    SPELLasRun::writeErrorInfo
//=============================================================================
void SPELLasRun::writeUserActionSet( const std::string& stack, const std::string& label, const unsigned int severity )
{
    toAsRun( STR("UACTION") + "\t" + STR("ENABLED") + "\t" + stack + "\t" + label + "\t" + SEVERITY_STR[severity], true);
}

//=============================================================================
// METHOD:    SPELLasRun::writeErrorInfo
//=============================================================================
void SPELLasRun::writeUserActionUnset( const std::string& stack )
{
    toAsRun( STR("UACTION") + "\t" + STR("DISMISSED") + "\t" + stack,true);
}

//=============================================================================
// METHOD:    SPELLasRun::writeErrorInfo
//=============================================================================
void SPELLasRun::writeUserActionEnable( const std::string& stack, bool enable )
{
    toAsRun( STR("UACTION") + "\t" + (enable ? STR("ENABLED") : STR("DISABLED")) + "\t" + stack, true);
}
