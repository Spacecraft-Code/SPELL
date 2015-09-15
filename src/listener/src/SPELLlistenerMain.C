// ################################################################################
// FILE       : SPELLlistenerMain.C
// DATE       : Jul 05, 2011
// PROJECT    : SPELL
// DESCRIPTION: SPELL listener main program
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

// FILES TO INCLUDE //////////////////////////////////////////////////////////
// Project includes ----------------------------------------------------------
#include "SPELL_UTIL/SPELLbase.H"
#include "SPELL_UTIL/SPELLlog.H"
#include "SPELL_UTIL/SPELLutils.H"
#include "SPELL_LST/SPELLlistener.H"
// System includes -----------------------------------------------------------
#include <signal.h>
// Local includes ------------------------------------------------------------

// GLOBALS ///////////////////////////////////////////////////////////////////

// Initialization/configuration variables
static std::string configFile = "";
// Initialize the singleton instance
SPELLlistener* SPELLlistener::s_instance = NULL;
// Finish event
SPELLevent s_finishEvent;

// STATIC ////////////////////////////////////////////////////////////////////

// Signal information
typedef struct
{
  int sig;
  char *name;
} TrappedSignal;

char STR_SIGINT[]  = "SIGINT";
char STR_SIGTERM[] = "SIGTERM";
char STR_SIGABRT[] = "SIGABRT";
char STR_SIGHUP[]  = "SIGHUP";
char STR_SIGQUIT[] = "SIGQUIT";
char STR_SIGILL[]  = "SIGILL";
char STR_SIGNONE[] = "";

// Signals to trap
static const TrappedSignal signalList[] =
  {
    { SIGINT,  STR_SIGINT  },
    { SIGTERM, STR_SIGTERM },
    { SIGABRT, STR_SIGABRT },
    { SIGHUP,  STR_SIGHUP  },
    { SIGQUIT, STR_SIGQUIT },
    { SIGILL,  STR_SIGILL  },
    { 0,       STR_SIGNONE }, // Closure
  };
// Number of signals
static const unsigned int NumSignals = sizeof(signalList) / sizeof(TrappedSignal);

//============================================================================
// Show usage
//============================================================================
void usage( char** argv )
{
    std::cerr << "Syntax:" << std::endl;
    std::cerr << "    " << argv[0] << " -c <config>" << std::endl;
    std::cerr << std::endl;
    std::cerr << "         - c : configuration file" <<  std::endl;
    std::cerr << std::endl;
}


//============================================================================
// Parse program arguments
//============================================================================
int parseArgs( int argc, char** argv )
{
    int code;
    while( ( code = getopt(argc, argv, "c:")) != -1)
    {
        switch(code)
        {
        case 'c':
            configFile = std::string(optarg);
            break;
        }
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
// Signal handler
//============================================================================
void signal_handler(int signum)
{
	if (signum < NumSignals)
	{
		std::cerr << "[***] Captured signal " << signalList[signum].name << std::endl;
	}
	else
	{
		std::cerr << "[***] Captured signal " << signum << std::endl;
	}
	std::cerr << "[***] Stopping listener services" << std::endl;
	SPELLlistener::get().stop();
	s_finishEvent.set();
	std::cerr << "[***] Services stopped" << std::endl;
}

//============================================================================
// Setup signal handler
//============================================================================
void setupSignals()
{
	std::cerr << "[***] Setup signal handlers" << std::endl;

	sigset_t signalsOfInterest;

	// Set the signal set
	sigemptyset(&signalsOfInterest);
	for (int loop = 0; signalList[loop].sig; loop++)
	{
		sigaddset(&signalsOfInterest, signalList[loop].sig);
	}

	// Set the actions
	for (int loop = 0; signalList[loop].sig; loop++)
	{
		struct sigaction whatToDo;
		whatToDo.sa_handler = signal_handler;
		whatToDo.sa_mask = signalsOfInterest;
		whatToDo.sa_flags = 0;
		sigaction(signalList[loop].sig, &whatToDo, 0);
	}
}

//============================================================================
// MAIN PROGRAM
//============================================================================
int main( int argc, char** argv )
{
    std::string timeId = SPELLutils::fileTimestamp();

    SPELLlog::instance().setLogFile("_Listener", timeId);

    if ( parseArgs(argc, argv) != 0 ) return 1;

    SPELLlistener::get().start(configFile);

    setupSignals();

    s_finishEvent.clear();
    s_finishEvent.wait();

    SPELLlistener::get().stop();

    return 0;
}
