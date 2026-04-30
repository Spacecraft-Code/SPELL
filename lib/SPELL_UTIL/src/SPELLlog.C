// ################################################################################
// FILE       : SPELLlog.C
// DATE       : Mar 17, 2011
// PROJECT    : SPELL
// DESCRIPTION: Implementation of the logger
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
#include "SPELL_UTIL/SPELLlog.H"
#include "SPELL_UTIL/SPELLutils.H"
#include "SPELL_UTIL/SPELLpythonHelper.H"
// Project includes --------------------------------------------------------
#include "SPELL_SYN/SPELLmonitor.H"
// System includes ---------------------------------------------------------
#include "log4cplus/appender.h"
#include "log4cplus/fileappender.h"
#include "log4cplus/layout.h"
#include "log4cplus/ndc.h"
#include "log4cplus/configurator.h"

// GLOBALS /////////////////////////////////////////////////////////////////

// Log singleton instance
static SPELLlog* s_instance = 0;

//#define LOG_PATTERN "[ %d{ %Y/%m/%d %H:%M:%S.%q} ] [%-5p] [ %h ] [ %30.30l ]: %m%n"
#define LOG_PATTERN "[ %-30.30l]\t[ %-6p]\t[%d{%Y/%m/%d %H:%M:%S.%q}]\t%m%n"
//=============================================================================
// CONSTRUCTOR : SPELLlog::SPELLlog()
//=============================================================================
SPELLlog::SPELLlog()
{
	std::string file_log4cplus_properties = SPELLutils::getSPELL_CONFIG() + "/spell/log.properties";

	if (SPELLutils::isFile(file_log4cplus_properties))
	{
		log4cplus::Hierarchy& hierarchy = log4cplus::Logger::getDefaultHierarchy();
		int flags = 0;
		log4cplus::PropertyConfigurator::doConfigure( file_log4cplus_properties, hierarchy, flags );
	}
}

//=============================================================================
// DESTRUCTOR : SPELLlog::~SPELLlog
//=============================================================================
SPELLlog::~SPELLlog()
{
}

//=============================================================================
// METHOD    : SPELLlog::instance()
//=============================================================================
SPELLlog& SPELLlog::instance()
{
    if (s_instance == NULL)
    {
        s_instance = new SPELLlog();
    }
    return *s_instance;
}

//=============================================================================
// METHOD    : SPELLlog::setLogFile
//=============================================================================
void SPELLlog::setLogFile( const std::string& filename, const std::string timestamp )
{
    char* home = getenv("SPELL_LOG");
    std::string logbase = ( home == NULL) ? "." : home;

    std::string logfilename = timestamp  + filename + ".log";
    // Remove possible ".." and ".py" from file
    SPELLutils::replace( logfilename, ".py", "" );
    SPELLutils::replace( logfilename, "..", "" );
    SPELLutils::replace( logfilename, "//", "/" );

    std::string::size_type pos =0;
    while(true)
    {
        pos = logfilename.find_first_of("/",pos);
        if (pos != std::string::npos )
        {
            logfilename.replace( pos, 1, "_" );
        }
        else
        {
            break;
        }
    }
    logfilename = logbase + "/" + logfilename;
    m_logFileName = logfilename;

	log4cplus::SharedFileAppenderPtr appender( new log4cplus::RollingFileAppender(LOG4CPLUS_TEXT(logfilename), 10*1024*1024, 5, true, true));
	appender->setLayout( std::auto_ptr<log4cplus::Layout>(new log4cplus::PatternLayout(LOG_PATTERN)) );
	appender->getloc();
	log4cplus::Logger::getRoot().addAppender( log4cplus::SharedAppenderPtr(appender.get()) );
}


