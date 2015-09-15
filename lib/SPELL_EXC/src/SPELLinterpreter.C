// ################################################################################
// FILE       : SPELLinterpreter.C
// DATE       : Mar 17, 2011
// PROJECT    : SPELL
// DESCRIPTION: Implementation of the custom interpreter
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
#include "SPELL_EXC/SPELLinterpreter.H"
#include "SPELL_EXC/SPELLgoto.H"
#include "SPELL_EXC/SPELLexecutor.H"
#include "SPELL_EXC/SPELLexecutorStatus.H"
#include "SPELL_EXC/SPELLcontroller.H"
#include "SPELL_EXC/SPELLscheduler.H"
#include "SPELL_EXC/SPELLcallstack.H"
#include "SPELL_EXC/SPELLexecutorImpl.H"
// Project includes --------------------------------------------------------
#include "SPELL_PRD/SPELLprocedureManager.H"
#include "SPELL_UTIL/SPELLerror.H"
#include "SPELL_UTIL/SPELLlog.H"
#include "SPELL_UTIL/SPELLpythonHelper.H"
#include "SPELL_UTIL/SPELLutils.H"
#include "SPELL_CFG/SPELLconfiguration.H"
#include "SPELL_WRP/SPELLregistry.H"
#include "SPELL_WRP/SPELLdriverManager.H"
#include "SPELL_WS/SPELLwsWarmStartImpl.H"
#include "SPELL_WS/SPELLwsStartupInfo.H"
#include "SPELL_CIF/SPELLcif.H"


// GLOBALS /////////////////////////////////////////////////////////////////

// Interpreter singleton instance
static SPELLinterpreter* s_instance = NULL;

// Warmstart location name
static const std::string WS_LOCATION    = "ws";
static const std::string ASRUN_LOCATION = "ar";

//=============================================================================
// CONSTRUCTOR: SPELLinterpreter::SPELLinterpreter
//=============================================================================
SPELLinterpreter::SPELLinterpreter()
{
	m_executor = NULL;
    m_cif = NULL;
    m_controller = NULL;
    m_scheduler = NULL;
    m_callstack = NULL;
    m_frameManager = NULL;
    m_warmStart = NULL;
    m_procPath = "";
    m_procedure = "";
}

//=============================================================================
// DESTRUCTOR: SPELLinterpreter::~SPELLinterpreter
//=============================================================================
SPELLinterpreter::~SPELLinterpreter()
{
    DEBUG("[*] Cleaning up");
    if (m_warmStart != NULL)
    {
        DEBUG("[*] Cleaning warm start mechanism");
        delete m_warmStart;
        m_warmStart = NULL;
    }
    if (m_callstack != NULL)
    {
        delete m_callstack;
        m_callstack = NULL;
    }
    if (m_controller != NULL)
    {
        delete m_controller;
        m_controller = NULL;
    }
    if (m_scheduler != NULL)
    {
        delete m_scheduler;
        m_scheduler = NULL;
    }
    if (m_executor != NULL)
    {
    	delete m_executor;
    	m_executor = NULL;
    }
    SPELLpythonHelper::instance().finalize();
    DEBUG("[*] End");
}

//=============================================================================
// METHOD    : SPELLinterpreter::instance
//=============================================================================
SPELLinterpreter& SPELLinterpreter::instance()
{
    if (s_instance == NULL)
    {
        s_instance = new SPELLinterpreter();
    }
    return *s_instance;
}

//=============================================================================
// METHOD    : SPELLinterpreter::initialize
//=============================================================================
void SPELLinterpreter::initialize( const SPELLinterpreterConfig& config, SPELLcif* cif )
{
    LOG_INFO("[*] Initializing interpreter");

    m_cif = cif;
    m_procedure = config.procId;
    m_config = config;
}

//=============================================================================
// METHOD    : SPELLinterpreter::mainLoop
//=============================================================================
void SPELLinterpreter::mainLoop()
{
    // Load the SPELL configuration (will fail with exception if there is an error)
    SPELLconfiguration::instance().loadConfig(m_config.configFile);

    // Configure the time format if defined in configuration
    std::string format = SPELLconfiguration::instance().getCommonParameter("TdsTimeFormat");
    if (format.length()!=0)
    {
    	if (format.compare ("1") == 0)
    	{
    		SPELLutils::setTimeFormat(TIME_FORMAT_SLASH);
    	}
    	else if (format.compare("0")==0)
    	{
    		SPELLutils::setTimeFormat(TIME_FORMAT_DOT);
    	}
    }

    LOG_INFO("[***] Start main loop");

    // Create and connect all execution control objects
    if (prepareObjects())
    {
        // Now try to initialize (compile) the execution frame.
        if (prepareExecution())
        {
            // If we are in recovery mode:
            if (m_config.recover)
            {
                recover();
            }
            else // Or we are in running mode
            {
                execute();
            }
        }

        // After the execution cycle, wait for the user/server to autorize closure
        DEBUG("[***] Main loop waiting for CIF");
        m_cif->waitClose();

        // Once all is finished, stop controller
        DEBUG("[***] Stopping command controller");
        m_controller->stop();

        DEBUG("[***] Cleaning CIF");
        m_cif->cleanup(false);
    }

    // Cleanup all warm start files
    if (m_warmStart)
    {
    	m_warmStart->cleanup();
    }

    LOG_INFO("[***] End main loop");
}

//=============================================================================
// METHOD    : SPELLinterpreter::prepareWarmStart
//=============================================================================
void SPELLinterpreter::prepareWarmStart( const SPELLcontextConfig& ctxConfig )
{
    // Prepare warmstart file if applicable
    if (m_config.warmstart)
    {
        char* home = getenv("SPELL_DATA");
        if (home == NULL)
        {
            std::string msg = "Unable to setup persistent file, no SPELL_DATA environment variable defined";
            LOG_ERROR("    " + msg);
            m_cif->error(msg, LanguageConstants::SCOPE_SYS );
            m_config.warmstart = false;
            return;
        }

		// Get the location of WS files
		std::string wsdir = ctxConfig.getLocationPath( WS_LOCATION );
		std::string saveMode = ctxConfig.getExecutorParameter(ExecutorConstants::SaveStateMode);

		// Prepare the WS configuration
		SPELLwsStartupInfo startup;

		if (saveMode == "DISABLED")
		{
			LOG_WARN("Warmstart disabled by configuration");
			startup.persistentFile = "";
		}
		else
		{
			startup.persistentFile = STR(home) + PATH_SEPARATOR + wsdir + PATH_SEPARATOR;
			// Check that the directory exists
			if (!SPELLutils::pathExists(startup.persistentFile))
			{
	            std::string msg = "Unable to setup persistent file, warm-start directory not found: " + startup.persistentFile;
	            LOG_ERROR("    " + msg);
	            m_cif->error(msg, LanguageConstants::SCOPE_SYS );
	            m_config.warmstart = false;
	            return;
			}

	        // Character replacements for constructing the file ids
	        std::string theId = m_procedure;
	        SPELLutils::replace( theId, ".py", "" );
	        SPELLutils::replace( theId, "..", "" );
	        SPELLutils::replace( theId, "//", "/" );
	        SPELLutils::replace( theId, PATH_SEPARATOR, "__" );
	        startup.persistentFile += m_config.timeId + "_Executor_" + theId;
			LOG_INFO("Warm start persistent file: " + startup.persistentFile);
		}


		LOG_INFO("Warm start files location: " + wsdir);
		LOG_INFO("Warm start working mode: " + saveMode);
		LOG_INFO("Warm start procedure file: " + m_procedure);


        // Set the configured working mode
        startup.workingMode = StringToWorkingMode(saveMode);

        // Set the procedure file
        startup.procedureFile = m_procedure;

        // Set the recovery file if any
        if (m_config.persis != "")
        {
        	startup.recoveryFile = STR(home) + PATH_SEPARATOR + wsdir + PATH_SEPARATOR + m_config.persis;
        	startup.performRecovery = true;
        }

		try
		{
            // Create the warm start support
            m_warmStart = new SPELLwsWarmStartImpl();
            // Initialize it
            m_warmStart->initialize( startup );
            // Set the warm start support in the execution frame
            m_frameManager->setWarmStart( m_warmStart );
        }
        catch(SPELLcoreException& ex)
        {
            if (m_warmStart != NULL) delete m_warmStart;
            m_warmStart = NULL;
            throw ex;
        }
    }
    else
    {
        m_cif->warning("ATTENTION: no warmstart mechanism is being used", LanguageConstants::SCOPE_SYS );
        LOG_WARN("[***] No warmstart mechanism will be used");
    }
}

//=============================================================================
// METHOD    : SPELLinterpreter::prepareCIF
//=============================================================================
void SPELLinterpreter::prepareCIF( const SPELLcontextConfig& ctxConfig )
{
    DEBUG("   Setup client interface");

    // Create the configuration for the CIF
    SPELLcifStartupInfo cifStartup;
    cifStartup.procId = m_procedure;
    cifStartup.procName = SPELLprocedureManager::instance().getProcName(m_procedure);
    cifStartup.contextName = m_config.ctxName;
    cifStartup.contextPort = m_config.ctxPort;
    cifStartup.timeId = m_config.timeId;

    m_cif->setup( cifStartup );
}

//=============================================================================
// METHOD    : SPELLinterpreter::prepareObjects
//=============================================================================
void SPELLinterpreter::recoverCIF( const SPELLcontextConfig& ctxConfig )
{
    char* home = getenv("SPELL_DATA");
    if (home == NULL)
    {
        std::string msg = "Unable to recover CIF asrun, no SPELL_DATA environment variable defined";
        LOG_ERROR("    " + msg);
        m_cif->error(msg, LanguageConstants::SCOPE_SYS );
        return;
    }

	// Get the location of WS files
	std::string ardir = ctxConfig.getLocationPath( ASRUN_LOCATION );

	std::string originalAsRun = std::string(home) + PATH_SEPARATOR + ardir + PATH_SEPARATOR + m_config.persis + ".ASRUN";

	m_cif->useAsRun( originalAsRun );
}

//=============================================================================
// METHOD    : SPELLinterpreter::prepareObjects
//=============================================================================
const bool SPELLinterpreter::prepareObjects()
{
    LOG_INFO("[***] Preparing objects");
    // Setup the client interface. The standalone one does
    // just a few value initializations, the server one
    // does the login in the context process
    assert(m_cif != NULL);
    bool result = true;
    try
    {
        DEBUG("   Initializing Python interface");
        SPELLpythonHelper::instance().initialize();

        // Install log support asap
        Log_Install();

        DEBUG("   Loading execution framework functions");
        // Setup the execution environment
        SPELLpythonHelper::instance().loadFramework();

        // Load the configuration on python side for the language and drivers
        SPELLconfiguration::instance().loadPythonConfig(m_config.configFile);
        // Get the context configuration
        SPELLcontextConfig& ctxConfig = SPELLconfiguration::instance().getContext(m_config.ctxName);

        // Setup procedure manager
        DEBUG("   Preparing procedure manager");
        SPELLprocedureManager::instance().setup(m_config.ctxName);
        m_procPath = SPELLprocedureManager::instance().getProcPath();
        // User library
        m_libPath = SPELLprocedureManager::instance().getLibPath();

        DEBUG("   Creating and linking");
        // Create the executor instance
        m_executor = new SPELLexecutorImpl();
        // Set the reference for the static accessor (Python layer and others)
        SPELLexecutor::setInstance( m_executor );

        m_controller = new SPELLcontroller( m_config.procId );
        m_scheduler = new SPELLscheduler(false);
        m_callstack = new SPELLcallstack();
        // Create and initialize the execution frame
        m_frameManager = new SPELLframeManager();

        // Setup the CIF now
        try
        {
        	prepareCIF( ctxConfig );
        }
        catch(SPELLcoreException& ex)
        {
        	// If we have no CIF there is no way to communicate anything. Bail out
        	LOG_ERROR("[FATAL] Failed to setup CIF: cannot start executor.");
        	LOG_ERROR("[FATAL] Error was: " + ex.what());
        	::exit(EIO);
        }

        DEBUG("   Initializing executor");
        // Initialize the executor with the objects
        m_executor->initialize(m_cif, m_controller, m_scheduler, m_callstack, m_frameManager);

        // Prepare and configure warm start mechanism now
        DEBUG("   Preparing warm start");
        prepareWarmStart( ctxConfig );

        // Let the executor prepare all the rest it needs for execution. Anything done here
        // shall be independent from reloads/aborts, etc.
        m_executor->prepare( m_procedure, ctxConfig );

        DEBUG("   Starting controller");
        m_controller->begin();

        // If we are in recovery mode, recover also the original asrun file
    	if (m_config.persis != "")
    	{
    		recoverCIF( ctxConfig );
    	}

        LOG_INFO("[***] Objects ready");
    }
    catch(SPELLcoreException& ex)
    {
        std::string msg = "[***] Failed to create objects: " + ex.what();
        LOG_ERROR(msg);
        // Notify error only if we have a way to do it, that is, no CIF failure.
        if (m_controller)
        {
        	m_controller->setError( "Failed to initialize: " + ex.getError(), ex.getReason(), true);
        }
        result = false;
    }
    return result;
}

//=============================================================================
// METHOD    : SPELLinterpreter::prepareExecution
//=============================================================================
const bool SPELLinterpreter::prepareExecution()
{
    LOG_INFO("[***] Preparing execution");
    try
    {
        DEBUG("   Installing executor binding");
        Executor_Install();

        DEBUG("   Installing goto bindings");
        Goto_Install();
        Step_Install();

        // Install the CIF object in the SPELL registry
        DEBUG("   Installing client interface");
        ClientIF_Install();

        if (m_config.script)
        {
            // In this case the proc path is just the script
            m_procPath = m_procedure;
            LOG_INFO("   Compiling script");
            // Compile and create initial execution frame
            m_frameManager->initialize( m_procedure );
        }
        else
        {
            LOG_INFO("   Compiling procedure");
            // Compile and create initial execution frame
            m_frameManager->initialize( SPELLprocedureManager::instance().getProcFile(m_procedure) );
        }

        // Set the procedures path
        m_executor->setProcedurePath( m_procPath );
        // Set the user library path
        m_executor->setLibraryPath( m_libPath );

        LOG_INFO("Executor procedure path: " + m_procPath);
        LOG_INFO("Executor library path: " + m_libPath);

    }
    catch(SPELLcoreException& ex)
    {
        LOG_ERROR("   Catched error during preparation: " + ex.what());
        m_controller->setError( "Error during preparation: " + ex.getError(), ex.getReason(), true);
        return false;
    }
    LOG_INFO("[***] Execution ready");
    return true;
}

//=============================================================================
// METHOD    : SPELLinterpreter::execute
//=============================================================================
void SPELLinterpreter::execute()
{
    // Setup procedure manager
    LOG_INFO("[*] Starting execution");

    try
    {
        m_executor->execute();
    }
    catch(SPELLcoreException& ex)
    {
        LOG_ERROR("[FATAL] Error during execution: " + ex.what());
		m_controller->setError( "Error during execution: " + ex.getError(), ex.getReason(), ex.isFatal());
    }

    LOG_INFO("[*] Execution finished");
}

//=============================================================================
// METHOD    : SPELLinterpreter::recover
//=============================================================================
void SPELLinterpreter::recover()
{
    // Setup procedure manager
    LOG_INFO("[*] Recovering execution from persistent file");
    try
    {
        m_executor->recover();
    }
    catch(SPELLcoreException& ex)
    {
        LOG_ERROR("[FATAL] Error during recovery: " + ex.what());
		/** \todo handle exception: send error */
    }
    LOG_INFO("[*] Execution finished");
}
