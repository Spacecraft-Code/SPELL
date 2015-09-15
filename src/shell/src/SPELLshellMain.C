// ################################################################################
// FILE       : SPELLshellMain.C
// DATE       : Mar 18, 2011
// PROJECT    : SPELL
// DESCRIPTION: Shell main program
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
// Project includes --------------------------------------------------------
#include "SPELL_UTIL/SPELLbase.H"
#include "SPELL_UTIL/SPELLlog.H"
#include "SPELL_UTIL/SPELLutils.H"
#include "SPELL_UTIL/SPELLerror.H"
#include "SPELL_EXC/SPELLexecutor.H"
#include "SPELL_EXC/SPELLscheduler.H"
#include "SPELL_EXC/SPELLschedulerIF.H"
#include "SPELL_EXC/SPELLcontrollerIF.H"
#include "SPELL_EXC/SPELLcallstackIF.H"
#include "SPELL_UTIL/SPELLpythonHelper.H"
#include "SPELL_WRP/SPELLdriverManager.H"
#include "SPELL_WRP/SPELLdatabaseManager.H"
#include "SPELL_WRP/SPELLregistry.H"
#include "SPELL_CFG/SPELLconfiguration.H"
#include "SPELL_CIF/SPELLcif.H"
#include "SPELL_PRD/SPELLprocedureManager.H"
// Local includes ----------------------------------------------------------
#include "SPELLshellCif.H"
#include "SPELLshellExecutor.H"
#include "SPELLshellController.H"
#include "SPELLshellCallstack.H"
// System includes ---------------------------------------------------------
#include <signal.h>

// GLOBALS ///////////////////////////////////////////////////////////////////

// Initialization/configuration variables
// Holds the configuration file
static std::string configFile = "";
// Holds the context name
static std::string contextName = "";
// STATIC ////////////////////////////////////////////////////////////////////

//============================================================================
// Show usage
//============================================================================
void usage( char** argv )
{
    std::cerr << "Syntax:" << std::endl;
    std::cerr << "    " << argv[0] << " -c <config> -n <context>" << std::endl;
    std::cerr << std::endl;
    std::cerr << "         - c : configuration file" <<  std::endl;
    std::cerr << "         - n : context identifier" <<  std::endl;
    std::cerr << std::endl;
    std::cerr << "Options p/s and w/r are mutually exclusive." << std::endl << std::endl;
}

//============================================================================
// Install the shell executor (fake) on Python side
//============================================================================
void ShellExecutor_Install();

//============================================================================
// Install the client interface for the shell
//============================================================================
void ShellClientIF_Install( SPELLcif* cif );

//============================================================================
// Parse program arguments
//============================================================================
int parseArgs( int argc, char** argv )
{
    int code;
    while( ( code = getopt(argc, argv, "n:c:")) != -1)
    {
        switch(code)
        {
        case 'n':
            contextName = std::string(optarg);
            break;
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
    if (contextName == "")
    {
        std::cerr << "Error: no context name provided" << std::endl;
        usage(argv);
        return 1;
    }
    return 0;
}

//============================================================================
// Utility class
//============================================================================
class UsingPython
{
public:
    UsingPython() {
        SPELLpythonHelper::instance().initialize();
    };
    ~UsingPython() {
        SPELLpythonHelper::instance().finalize();
    };
};

//============================================================================
// Utility class
//============================================================================
class UsingDriver
{
public:
    UsingDriver() {
        ;
    };
    ~UsingDriver()
    {
        try
        {
            SPELLdriverManager::instance().cleanup(true);
        }
        catch(SPELLcoreException& ex)
        {
            std::cerr << "ERROR on cleanup: " << ex.what() << std::endl;
        }
    };
};

//============================================================================
// SIGNAL HANDLER
//============================================================================
void signalHandler( int signal )
{
	std::cerr << "Keyboard signal " << signal << std::endl;
	SPELLexecutor::instance().getScheduler().abortWait(false);
}

//============================================================================
// MAIN PROGRAM
//============================================================================
int main( int argc, char** argv )
{
    if ( parseArgs(argc,argv) != 0 ) return 1;

    // Initialize the Python environment. Will clean it up when destroyed.
    UsingPython usingPython;

    // Load the readline module if available
    if (isatty(fileno(stdin)))
    {
        PyObject *v;
        v = PyImport_ImportModule("readline");
        if (v==NULL)
        {
            PyErr_Clear();
        }
        else
        {
            Py_DECREF(v);
        }
    }

    SPELLlog::instance().setLogFile("SHELL","SHELL");

    // Install log support
    Log_Install();

    try
    {
        // Setup the execution environment
    	std::cerr << "Loading SPELL framework" << std::endl;
        SPELLpythonHelper::instance().loadFramework();
        // Load the SPELL configuration (will fail with exception if there is an error)
    	std::cerr << "Loading configuration" << std::endl;
        SPELLconfiguration::instance().loadConfig(configFile);
        // Load the configuration on python side for the language and drivers
        SPELLconfiguration::instance().loadPythonConfig(configFile);
        // Load user libraries
    	std::cerr << "Loading user libraries" << std::endl;
    	SPELLprocedureManager::instance().setup(contextName);
    	std::string libPath = SPELLprocedureManager::instance().getLibPath();
        if (SPELLutils::isDirectory(libPath))
        {
            SPELLpythonHelper::instance().addToPath(libPath);
            std::list<std::string> files = SPELLutils::getFilesInDir(libPath);
            std::list<std::string>::iterator it;
            std::list<std::string>::iterator end = files.end();
            for( it = files.begin(); it != end; it++)
            {
                std::string filename = (*it);
                if (filename == "__init__.py" ) continue;
                std::size_t idx = filename.find(".py");
                if ((idx != std::string::npos) && (idx>0))
                {
                	std::cerr << "    - library " << filename << std::endl;
                    std::string module = filename.substr(0,idx);
                    SPELLpythonHelper::instance().importAllFrom(module);
                }
            }
        }

    }
    catch(SPELLcoreException& ex)
    {
        std::cerr << "FATAL ERROR: cannot initialize: " << ex.what() << std::endl;
        return 1;
    }

    // Will cleanup the driver manager when destroyed
    UsingDriver usingDriver;

    try
    {
        // Load the driver
        SPELLdriverManager::instance().setup(contextName);
    }
    catch(SPELLcoreException& ex)
    {
        std::cerr << "FATAL ERROR: cannot setup driver: " << ex.what() << std::endl;
        return 1;
    }

    // Load predefined databases and interfaces
    PyObject* dbMgr = SPELLregistry::instance().get("DBMGR");
    if (dbMgr == NULL)
    {
        std::cerr << "WARNING: cannot install DB manager" << std::endl;
    }
    else
    {
        // Install the runtime databases
    	SPELLdatabaseManager::instance().loadBuiltinDatabases();
    }


    signal(SIGABRT, signalHandler);
    signal(SIGTERM, signalHandler);

    SPELLexecutorIF* executor = new SPELLshellExecutor();
    SPELLcif* cif = new SPELLshellCif();
    SPELLschedulerIF* scheduler = new SPELLscheduler(true);
    SPELLcontrollerIF* controller = new SPELLshellController();
    SPELLcallstackIF* callstack = new SPELLshellCallstack();
    executor->initialize( cif, controller, scheduler, callstack, NULL );
    SPELLexecutor::setInstance(executor);

    ShellExecutor_Install();
    ShellClientIF_Install( cif );

    // Launch the interactive loop
    int status = 0;
    try
    {
        status = PyRun_AnyFileExFlags( stdin, "<stdin>", 0, NULL );
    }
    catch(SPELLcoreException& ex)
    {
        std::cerr << "FATAL ERROR: " << ex.what() << std::endl;
        status = ex.getCode();
    }

    return status;
}
