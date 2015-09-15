// ################################################################################
// FILE       : SPELLcontextMain.C
// DATE       : Mar 18, 2011
// PROJECT    : SPELL
// DESCRIPTION: SPELL context main program
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
// Project includes --------------------------------------------------------
#include "SPELL_UTIL/SPELLbase.H"
#include "SPELL_UTIL/SPELLlog.H"
#include "SPELL_UTIL/SPELLutils.H"
#include "SPELL_UTIL/SPELLerror.H"
// Local includes ----------------------------------------------------------
#include "SPELL_CTX/SPELLcontext.H"

// GLOBALS ///////////////////////////////////////////////////////////////////

// Initialization/configuration variables
static int warm = 0;
static int port = 0;
static std::string configFile = "";
static std::string context = "";

// For POST
// STATIC ////////////////////////////////////////////////////////////////////


//============================================================================
// Show usage
//============================================================================
void usage( char** argv )
{
    std::cerr << "Syntax:" << std::endl;
    std::cerr << "    " << argv[0] << " -n <ctx name> -s <port> -c <config> [-w]" << std::endl;
    std::cerr << std::endl;
    std::cerr << "         - c : configuration file" <<  std::endl;
    std::cerr << "         - n : context identifier" <<  std::endl;
    std::cerr << "         - s : listener port" <<  std::endl;
    std::cerr << "         - w : use warmstart capabilities" <<  std::endl;
    std::cerr << std::endl;
}


//============================================================================
// Parse program arguments
//============================================================================
int parseArgs( int argc, char** argv )
{
    int code;
    while( ( code = getopt(argc, argv, "wn:c:s:")) != -1)
    {
        switch(code)
        {
        case 'w':
            warm = 1;
            std::cout << "* Enable warmstart" << std::endl;
            break;
        case 'n':
            context = std::string(optarg);
            std::cout << "* Name: " << context << std::endl;
            break;
        case 'c':
            configFile = std::string(optarg);
            break;
        case 's':
            port = STRI(optarg);
            std::cout << "* Port: " << port << std::endl;
            break;
        }
    }
    // We need proc id and context at least
    if (context == "")
    {
        std::cerr << "Error: context name not provided" << std::endl;
        usage(argv);
        return 1;
    }
    if (port == 0)
    {
        std::cerr << "Error: no context port provided" << std::endl;
        usage(argv);
        return 1;
    }
    if (configFile == "")
    {
        std::cerr << "Error: configuration file not provided" << std::endl;
        usage(argv);
        return 1;
    }
    return 0;
}

//============================================================================
// MAIN PROGRAM
//============================================================================
int main( int argc, char** argv )
{
    if ( parseArgs(argc,argv) != 0 ) return 1;

    // The time identifier is used for unique log file names
    std::string timeId = SPELLutils::fileTimestamp();

    // Configuration of the context. By default we choose to have the listener
    // process running on the same host as the contexts.
    SPELLcontextConfiguration configParameters;
    configParameters.listenerHost = "localhost";
    configParameters.listenerPort = port;
    configParameters.contextName = context;
    configParameters.timeId = timeId;
    configParameters.useWarmstart = (warm == 1);
    configParameters.configFile = configFile;

    // Setup logging
    SPELLlog::instance().setLogFile("_Context_" + context,timeId);

    LOG_INFO("Context started on " + SPELLutils::timestamp());

    try
    {
    	// Will setup the context and prepare all components
    	SPELLcontext::instance().start( configParameters );
    }
    catch(SPELLcoreException& ex)
    {
    	LOG_ERROR("FATAL: " + std::string(ex.what()) );
    	return 1;
    }

    // Will wait until the context is closed, when a close command is received from listener
    SPELLcontext::instance().waitFinish();

    // Clenanup internal context components
    SPELLcontext::instance().stop();
    return 0;
}
