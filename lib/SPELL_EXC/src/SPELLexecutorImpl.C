// ################################################################################
// FILE       : SPELLexecutorImpl.C
// DATE       : Mar 17, 2011
// PROJECT    : SPELL
// DESCRIPTION: Implementation of the executor
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
#include "SPELL_EXC/SPELLexecutor.H"
#include "SPELL_EXC/SPELLexecutorImpl.H"
// Project includes --------------------------------------------------------
#include "SPELL_PRD/SPELLprocedureManager.H"
#include "SPELL_SYN/SPELLmonitor.H"
#include "SPELL_CFG/SPELLconfiguration.H"
#include "SPELL_DTA/SPELLdtaContainer.H"
#include "SPELL_UTIL/SPELLlog.H"
#include "SPELL_UTIL/SPELLpythonHelper.H"
#include "SPELL_UTIL/SPELLpythonMonitors.H"
#include "SPELL_WRP/SPELLdriverManager.H"
#include "SPELL_WRP/SPELLdatabaseManager.H"
#include "SPELL_WRP/SPELLpyHandle.H"
#include "SPELL_WRP/SPELLregistry.H"
#include "SPELL_WRP/SPELLconstants.H"
#include "SPELL_CFG/SPELLbrowsableLibMode.H"

using namespace PythonConstants;

// GLOBALS ////////////////////////////////////////////////////////////////////

// Bridge for C-API dispatching callback
static int static_dispatch( PyObject* obj, PyFrameObject* frame, int what, PyObject* args )
{
    return SPELLexecutor::instance().dispatch(obj, frame, what, args);
}

static SPELLexecutorConfig EMPTY_CONFIGURATION;

//=============================================================================
// FUNCTION: convert dispatch data type to string
//=============================================================================
std::string dispatchDataType( int what )
{
    std::string etype = "other";
    switch(what)
    {
    case PyTrace_EXCEPTION:
    	etype = "EXCEPTION";
    	break;
    case PyTrace_LINE:
    	etype = "LINE";
    	break;
    case PyTrace_CALL:
    	etype = "CALL";
    	break;
    case PyTrace_RETURN:
    	etype = "RETURN";
    	break;
    default:
    	break;
    }
    return etype;
}

//=============================================================================
// CONSTRUCTOR : SPELLexecutorImpl::SPELLexecutorImpl
//=============================================================================
SPELLexecutorImpl::SPELLexecutorImpl()
    : SPELLexecutorIF(),
      m_importChecker()
{
    m_initialized        = false;
    m_initStepDone       = false;
    m_initLines.clear();
    m_cif                = NULL;
    m_frameManager       = NULL;
    m_instanceId         = "";
    m_parentId           = "";
    m_childId            = "";
    m_procPath = "";

    m_scheduler          = NULL;
    m_callstack          = NULL;
    m_controller         = NULL;
    m_childMgr           = NULL;
    m_varManager         = NULL;

    m_userAction.reset();
    m_gotoTarget = "";

    m_childMgr = new SPELLchildManager();

    DEBUG("[E] SPELLexecutor created");
}

//=============================================================================
// DESTRUCTOR : SPELLexecutorImpl::~SPELLexecutorImpl
//=============================================================================
SPELLexecutorImpl::~SPELLexecutorImpl()
{
    DEBUG("[E] SPELLexecutor destroyed");
    if (m_childMgr)
    {
        delete m_childMgr;
    }

}

//=============================================================================
// METHOD    : SPELLexecutorImpl::initialize()
//=============================================================================
void SPELLexecutorImpl::initialize( SPELLcif* cif,
                                SPELLcontrollerIF* controller,
                                SPELLschedulerIF* scheduler,
                                SPELLcallstackIF* callstack,
                                SPELLframeManager* frame )
{
    assert( cif != NULL );
    assert( controller != NULL );
    assert( scheduler != NULL );
    assert( callstack != NULL );
    assert( frame != NULL );
    m_cif = cif;
    m_controller = controller;
    m_scheduler = scheduler;
    m_callstack = callstack;
    m_frameManager = frame;
    m_varManager = new SPELLvariableManager(*frame);

    // The order is important here!
    addDispatchListener(m_frameManager);
    addDispatchListener(m_callstack);
    addDispatchListener(m_controller);
}

//=============================================================================
// METHOD    : SPELLexecutorImpl::getConfiguration()
//=============================================================================
SPELLexecutorConfig& SPELLexecutorImpl::getConfiguration() const
{
	if (m_cif != NULL)
	{
		return m_cif->getExecutorConfig();
	}
	return EMPTY_CONFIGURATION;
};

//=============================================================================
// METHOD    : SPELLexecutorImpl::getStatus()
//=============================================================================
const SPELLexecutorStatus SPELLexecutorImpl::getStatus() const
{
    if (m_controller != NULL)
    {
        return m_controller->getStatus();
    }
    return STATUS_UNKNOWN;
}

//=============================================================================
// METHOD    : SPELLexecutorImpl::getInstanceId
//=============================================================================
const std::string SPELLexecutorImpl::getInstanceId() const
{
    return m_instanceId;
}

//=============================================================================
// METHOD    : SPELLexecutorImpl::prepare
//=============================================================================
void SPELLexecutorImpl::prepare( const std::string& instanceId, const SPELLcontextConfig& ctxConfig )
{
	assert( m_cif != NULL );
	assert( m_controller != NULL );
	assert( m_scheduler != NULL );
	assert( m_callstack != NULL );
	assert( m_frameManager != NULL );

    LOG_INFO("[E] Preparing execution");

    // Check for Python errors
    SPELLpythonHelper::instance().checkError();

    // See constants.H
    // We do this on preparation stage because these values shall not be reset
    // on procedure reload. The user may have changed them in the meantime.
    if (!m_initialized)
    {
        m_instanceId = instanceId;

        //Get from CTX Config
        m_inputDir = SPELLutils::resolvePath( ctxConfig.getInputDirectory() );
        m_outputDir = SPELLutils::resolvePath( ctxConfig.getOutputDirectory() );

        // Report the current configuration
        LOG_INFO("EXECUTOR CONFIGURATION (" + getInstanceId() + ")");
        LOG_INFO("[E] Headless         : " + BSTR(getConfiguration().isHeadless()) );
        LOG_INFO("[E] Parent           : " + getConfiguration().getParentProcId() );
        LOG_INFO("[E] Arguments        : " + getConfiguration().getArguments()  );
        LOG_INFO("[E] Condition        : " + getConfiguration().getCondition()  );
        LOG_INFO("[E] Automatic mode   : " + BSTR( getConfiguration().isAutomatic() ));
        LOG_INFO("[E] Visible mode     : " + BSTR( getConfiguration().isVisible()   ));
        LOG_INFO("[E] Blocking mode    : " + BSTR( getConfiguration().isBlocking()  ));
        LOG_INFO("[E] Browsable lib    : " + getConfiguration().getBrowsableLibStr()  );
        LOG_INFO("[E] TC Confirm       : " + BSTR( getConfiguration().isForceTcConfirm()  ));
        LOG_INFO("[E] Save state mode  : " + getConfiguration().getSaveStateMode() );
        LOG_INFO("[E] Watch variables  : " + BSTR(getConfiguration().isWatchEnabled()) );
        LOG_INFO("[E] Input directory  : " + m_inputDir );
        LOG_INFO("[E] Output directory : " + m_outputDir );
        LOG_INFO("[E] Run Into         : " + BSTR(getConfiguration().isRunInto()));
        LOG_INFO("[E] Exec Delay       : " + ISTR(getConfiguration().getExecDelay()));
        LOG_INFO("[E] Prompt Delay     : " + ISTR(getConfiguration().getPromptWarningDelay()));
        LOG_INFO("[E] By Step          : " + BSTR(getConfiguration().isByStep()));
        LOG_INFO("[E] Max Verbosity    : " + ISTR(getConfiguration().getMaxVerbosity()));

        m_scheduler->setPromptWarningDelay(getConfiguration().getPromptWarningDelay());

        m_initialized = true;
    }

    // Check that no previous errors are there
    SPELLpythonHelper::instance().checkError();

    // Setup extra environment stuff
    // 1. Procedure arguments and internal variables.
    // If there are arguments available we need to evaluate
    // them in the Python environment and to install them in the execution environment
    // via the ARGS and IVARS global objects.
    installCallingArguments();
    installInternalVariables();

    // 2. Process condition and open mode
    // If there is a condition available, we set it to the controller. The controller
    // will read and evaluate it and hold the execution until the condition is
    // fullfilled.
    if (getConfiguration().getCondition() != "")
    {
        m_controller->setCondition( getConfiguration().getCondition() );
    }

    // Reset any user action function previously set
    m_userAction.reset();

    // Configure the initial step over mode in the callstack
    if (getConfiguration().isRunInto()==true)
    {
    	m_callstack->stepOver( SO_ALWAYS_INTO );
    }
    else
    {
    	m_callstack->stepOver( SO_ALWAYS_OVER );
    }

    LOG_INFO("[E] Executor prepared");
}

//=============================================================================
// METHOD    : SPELLexecutorImpl::installCallingArguments
//=============================================================================
void SPELLexecutorImpl::installCallingArguments()
{
    DEBUG("[E] Installing calling arguments");

    PyObject* argDict = NULL;
    if (getConfiguration().getArguments() != "")
    {
        try
        {
            // Evaluate the argument string. It is expected to be a Python dictionary.
            // No error check is done for this.
            argDict = SPELLpythonHelper::instance().eval(getConfiguration().getArguments(),false);
        }
        catch(SPELLcoreException& ex)
        {
            m_cif->error( "Unable to install arguments data holder: " + ex.what(), LanguageConstants::SCOPE_SYS );
            argDict = PyDict_New();
        }
    }
    else
    {
        // In case of no arguments given, we install an empty Python dictionary.
        argDict = PyDict_New();
    }


    // Initialize now the data container for Calling Arguments
    Py_INCREF(argDict);
    DEBUG("[E] Argument dictionary: " + PYREPR(argDict));
    PyObject* classObj = SPELLpythonHelper::instance().getObject("libSPELL_DTA", "DataContainer");
    PyObject* argTuple = PyTuple_New(1);
    PyObject* argsName = STRPY("Calling Arguments");
    PyTuple_SetItem(argTuple,0,argsName);
    Py_INCREF(argsName);
    Py_INCREF(argTuple);
    PyObject* args = SPELLpythonHelper::instance().newInstance(classObj, argTuple, NULL);

    DEBUG("[E] Calling arguments instance: " + PYREPR(args) + " type: " + PYCREPR(PyObject_Type(args)));

    // Now assign the passed arguments. Disable notifications for DTA
    SPELLdtaContainer::setGlobalNotificationsEnabled(false);
    SPELLpyHandle argKeys = PyDict_Keys(argDict);
    unsigned int numKeys = PyList_Size(argKeys.get());
    for(unsigned int idx = 0; idx<numKeys; idx++)
    {
    	PyObject* key = PyList_GetItem(argKeys.get(),idx);
    	PyObject* value = PyDict_GetItem(argDict, key);
        DEBUG("[E]    - Argument " + PYSSTR(key) + " = " + PYREPR(value));
        SPELLpyHandle set = STRPY("set");
        PyObject_CallMethodObjArgs( args, set.get(), key, value, NULL);
    }
    SPELLdtaContainer::setGlobalNotificationsEnabled(true);
    // Check for Python errors
    SPELLpythonHelper::instance().checkError();

    // Install the object in the global scope
    SPELLpythonHelper::instance().install(args,"ARGS");
}

//=============================================================================
// METHOD    : SPELLexecutorImpl::installInternalVariables
//=============================================================================
void SPELLexecutorImpl::installInternalVariables()
{
    PyObject* classObj = SPELLpythonHelper::instance().getObject("libSPELL_DTA", "DataContainer");
    PyObject* argTuple = PyTuple_New(1);
    PyObject* argsName = STRPY("Internal Variables");
    PyTuple_SetItem(argTuple,0,argsName);
    Py_INCREF(argsName);
    Py_INCREF(argTuple);
    PyObject* args = SPELLpythonHelper::instance().newInstance(classObj, argTuple, NULL);

    // Install the object in the global scope
    SPELLpythonHelper::instance().install(args,"IVARS");
}

//=============================================================================
// METHOD    : SPELLexecutorImpl::execute
//=============================================================================
void SPELLexecutorImpl::execute()
{
    // Load the SPELL driver
    loadDriver();

    // Load the builtin databases, etc.
    loadExecutionEnvironment();

	executeInternal(true);
}

//=============================================================================
// METHOD    : SPELLexecutorImpl::executeInternal
//=============================================================================
void SPELLexecutorImpl::executeInternal( bool doReset )
{
    DEBUG("[E] Starting execution");

    // This flag allows us to re-execute after recovery or reload
    bool continueExecuting = false;

    // Initial flags when loading (not reloading/recovering)
    m_initStepDone = !doReset;
    // Reset flag: we shall not reset executor entities if we are recovering
    bool resetEntities = doReset;

    do
    {
        DEBUG("[E] Set system trace on " + PSTR(PyThreadState_GET()));
        // Setup the dispatching mechanism
        PyEval_SetTrace( static_dispatch, NULL );

        // Reset execution status
        if (resetEntities)
        {
			m_callstack->reset();
        }
		DEBUG("[E] Reset status");
		m_controller->reset();
		m_importChecker.reset();
        // Reset CIF closure lock always
		m_cif->resetClose();

        // Establish initial run-into value now (after reset)
        m_controller->enableRunInto( getConfiguration().isRunInto() );

        // The client will know that the executor is ready to go
        m_controller->setStatus(STATUS_LOADED);

        // If in automatic and backgorund mode, set the controller in play mode accordingly
        // IMPORTANT If in automatic and foreground mode, it is the GUI who sends the run command
        if (  getConfiguration().isHeadless() || (getConfiguration().isAutomatic() && (!getConfiguration().isVisible()) ) )
		{
    		DEBUG("[E] Set autorun");
        	m_controller->setAutoRun();
		}

        DEBUG("[E] Launching execution");
        // Execute the procedure/script. This triggers the procedure
        // execution under dispatcher control.
        SPELLexecutionResult result = m_frameManager->execute();

        DEBUG("[E] Execution done, checking result (" + ISTR(result) + ")");

        // Check execution result and proceed accordingly.
        // Frame will be reset and environment unloaded if needed, depending on the case.

        switch(result)
        {
        case EXECUTION_ERROR:
			{
		        DEBUG("[E] Result ERROR");
		        m_initStepDone = true;
				continueExecuting = executorFinishedWithErrors();
				if (continueExecuting)
				{
					// We will recover, so do not reset entities
					resetEntities = false;
				}
				break;
			}
        case EXECUTION_SUCCESS:
        case EXECUTION_TERMINATED:
			{
		        DEBUG("[E] Result SUCCESS/TERMINATED");
		        m_initStepDone = false;
        		continueExecuting = executorFinishedRight();
        		break;
        	}
        case EXECUTION_ABORTED:
			{
		        DEBUG("[E] Result ABORTED");
		        m_initStepDone = false;
				continueExecuting = executorAborted();
				break;
			}
        default:
			{
		        DEBUG("[E] Execution result unknown: " + ISTR(result));
		        m_initStepDone = false;
		        unloadDriver(true);
				continueExecuting = false;
				break;
			}
        }
    }
    while(continueExecuting);

    // This may be redundant, but does not harm.
    PyEval_SetTrace( NULL, NULL );

    DEBUG("[E] Execution finished");
}

//=============================================================================
// METHOD    : SPELLexecutorImpl::reloadOrClose
//=============================================================================
bool SPELLexecutorImpl::reloadOrClose()
{
	DEBUG("[E] Waiting for CIF closure");
	// Wait the close or reload command from the user
	m_cif->waitClose();
	// This tells us if user wants to reload, not close
	if (m_controller->shouldReload())
	{
	    DEBUG("[E] Clear asrun");
	    m_cif->clearAsRun();
	    DEBUG("[E] Reload driver");
		// We are reloading, load again the execution environment and reset the frame
		loadDriver();
	    DEBUG("[E] Reset frame");
		m_frameManager->reset();
	    DEBUG("[E] Reload environment");
		loadExecutionEnvironment();
		return true;
	}
	else
	{
		// Completely unload execution environment if we are not reloading
		unloadDriver(true);
		return false;
	}
}

//=============================================================================
// METHOD    : SPELLexecutorImpl::executorAborted
//=============================================================================
bool SPELLexecutorImpl::executorAborted()
{
	DEBUG("[E] Execution finished due to abort");
	// Cleanup the runtime exception that lead to the abort of the interpretation
	SPELLerror::instance().clearErrors();
	// Set the status aborted here, not before. Otherwise, the GUI would be notified
	// before the driver actually unloads, and this may take time.
	getController().setStatus(STATUS_ABORTED);
	// Procedure has aborted, so unload the driver and execution environment right away
	unloadDriver(false);
	// Wait until the user decides to close or to reload
	return reloadOrClose();
}

//=============================================================================
// METHOD    : SPELLexecutorImpl::executorFinishedRight
//=============================================================================
bool SPELLexecutorImpl::executorFinishedRight()
{
	DEBUG("[E] Execution finished successfully");
	// Cleanup the runtime exception that lead to the termination of the interpretation
	SPELLerror::instance().clearErrors();

	// Mark the procedure as finished
    m_controller->setFinished();

	// Procedure has finished, so unload the driver and execution environment right away
	unloadDriver(false);
    // This call may be misleading but the behavior at this point is the same as the aborted state
	return reloadOrClose();
}

//=============================================================================
// METHOD    : SPELLexecutorImpl::executorFinishedWithErrors
//=============================================================================
bool SPELLexecutorImpl::executorFinishedWithErrors()
{
	DEBUG("[E] Execution finished with errors");
    // Will return true if the error was recoverable AND
    // the user chooses to recover.
    AfterError errorHandlingResult = handleExecutionError();
    bool continueExecuting = false;

    // If the user does not want to (either closing or reloading)
    switch(errorHandlingResult)
    {
    case CANCEL_RECOVER:
		{
	        // This tells us if user wants to reload, not close
	        if(m_controller->shouldReload())
	        {
	        	// If we are reloading, just dont unload the execution environment, reset the
	        	// frame and start over
	        	m_frameManager->reset();
	        	continueExecuting = true;
	        }
	        else
	        {
	        	// Unload the environment if we dont recover
	        	unloadDriver(true);
	        }
	        // Else will close and not reload/recover
	        break;
		}
    case RECOVER_SUCCESS:
		{
			// To re-execute. But do not reset the frame and dont unload the environment.
			continueExecuting = true;
			break;
		}
    case RECOVER_FAILED:
    case CANNOT_RECOVER:
		{
			// Unload the environment, could not recover it
			unloadDriver(false);
			// We will need to wait users choice
			DEBUG("[E] Waiting for CIF closure (recovery failed)");
			// Wait the close or reload command from the user
			m_cif->resetClose();
			m_cif->waitClose();
			// This may be misleading, but the behavior at this point is the same as aborted
			continueExecuting = reloadOrClose();
			break;
		}
    default:
    	break;
    }
    return continueExecuting;
}

//=============================================================================
// METHOD    : SPELLexecutorImpl::save
//=============================================================================
void SPELLexecutorImpl::save()
{
    DEBUG("[E] Saving on demand");
    try
    {
        // Restore the state from persistent file
        m_frameManager->saveState();
    }
    catch(SPELLcoreException& ex)
    {
    	std::string msg = "Save state failed: " + std::string(ex.what());
        m_cif->notifyError( msg, ex.what(), true);
    	LOG_ERROR(msg);
    }
    DEBUG("[E] Saving on demand done");
}

//=============================================================================
// METHOD    : SPELLexecutorImpl::recover
//=============================================================================
void SPELLexecutorImpl::recover()
{
    DEBUG("[E] Recovering execution");

    try
    {
        // Load the SPELL driver
        loadDriver();

        // Load the builtin databases, etc.
        loadExecutionEnvironment();

        // Restore the state from persistent file
        m_frameManager->restoreState();

        // Re-create the internal callstack model
        m_frameManager->replayStack( m_callstack );
    }
    catch(SPELLcoreException& ex)
    {
    	std::string msg = "Recovery failed, could not restore state: " + std::string(ex.what());
        m_cif->notifyError( msg, ex.what(), true);
    	LOG_ERROR(msg);
        PyEval_SetTrace( NULL, NULL );
        DEBUG("[E] Waiting for CIF closure");
        m_cif->waitClose();
        return;
    }
    DEBUG("[E] Execution recovery finished");
    executeInternal(false);
}

//=============================================================================
// METHOD    : SPELLexecutorImpl::finalize
//=============================================================================
void SPELLexecutorImpl::finalize()
{
    DEBUG("[E] Finalizing, user request closure");
    // Release the CIF lock, the controller received the close command
    // so we can proceed
    m_cif->canClose();
}

//=============================================================================
// METHOD    : SPELLexecutorImpl::loadExecutionEnvironment
//=============================================================================
void SPELLexecutorImpl::loadExecutionEnvironment()
{
    DEBUG("[E] Loading execution environment");

    // Check for Python errors
    SPELLpythonHelper::instance().checkError();

	// Load SCDB and GDB
	SPELLdatabaseManager::instance().loadBuiltinDatabases();

	// Create proc dictionary object and install it
	PyObject* procObj = PyDict_New();
	PyObject* pname = SSTRPY(m_instanceId);
	PyObject* arguments = SSTRPY(getConfiguration().getArguments());
	PyObject* outputDataDir = SSTRPY( m_outputDir );
	PyObject* inputDataDir = SSTRPY( m_inputDir );
	PyObject* parentId = SSTRPY( getConfiguration().getParentProcId() );

	PyDict_SetItemString( procObj, DatabaseConstants::NAME.c_str(), pname);
	PyDict_SetItemString( procObj, DatabaseConstants::ARGS.c_str(), arguments);
	PyDict_SetItemString( procObj, DatabaseConstants::STEP.c_str(), Py_None);
	PyDict_SetItemString( procObj, DatabaseConstants::PREV_STEP.c_str(), Py_None);
	PyDict_SetItemString( procObj, DatabaseConstants::OUTPUT_DATA.c_str(), outputDataDir);
	PyDict_SetItemString( procObj, DatabaseConstants::INPUT_DATA.c_str(), inputDataDir);
	PyDict_SetItemString( procObj, DatabaseConstants::PARENT.c_str(), parentId);

	SPELLpythonHelper::instance().install( procObj,  DatabaseConstants::PROC );

	SPELLpythonHelper::instance().importUserLibraries( m_libPath );

	SPELLpythonHelper::instance().checkError();

	// Tell the execution frame that the environment has been updated
	m_frameManager->filterDictUpdated();

    DEBUG("[E] Loading execution environment done");
}

//=============================================================================
// METHOD    : SPELLexecutorImpl::loadDriver
//=============================================================================
void SPELLexecutorImpl::loadDriver()
{
	DEBUG("#############################################################");
	DEBUG("[E] Load driver - start");

    // Check for Python errors
    SPELLpythonHelper::instance().checkError();

	SPELLnoCommandProcessing nc;

	LOG_INFO("Loading driver on context " + getConfiguration().getContextName());
	// Prepare and load the SPELL driver
	SPELLdriverManager::instance().setup( getConfiguration().getContextName() );

	// Enable TC confirmation if so said the config
    if (getConfiguration().isForceTcConfirm())
    {
    	setForceTcConfirmInternal(true);
    }

	// Load the driver language specifics
	SPELLcontextConfig& ctxConfig = SPELLconfiguration::instance().getContext( getConfiguration().getContextName() );
	std::string driverName = ctxConfig.getDriverName();
	SPELLdriverConfig& drvConfig = SPELLconfiguration::instance().getDriver( driverName );

	std::string path = drvConfig.getPath() + PATH_SEPARATOR + drvConfig.getIdentifier();

	path = SPELLutils::resolvePath(path);

	DEBUG("[E] Driver files loaded in " + path);

	// Import modifiers
	std::string package = "";
	std::string moduleFile = path + PATH_SEPARATOR + "modifiers.py";
	DEBUG("[E] Checking for module " + moduleFile);
	if (SPELLutils::isFile( moduleFile ))
	{
		package = drvConfig.getIdentifier() + ".modifiers";
		DEBUG("[E] Importing driver package " + package);
		SPELLpythonHelper::instance().importAllFrom( package );
	}

	// Import constants
	moduleFile = path + PATH_SEPARATOR + "constants.py";
	DEBUG("[E] Checking for module " + moduleFile);
	if (SPELLutils::isFile( moduleFile ))
	{
		package = drvConfig.getIdentifier() + ".constants";
		DEBUG("[E] Importing driver package " + package);
		SPELLpythonHelper::instance().importAllFrom( package );
	}

	// Import functions
	moduleFile = path + PATH_SEPARATOR + "functions.py";
	DEBUG("[E] Checking for module " + moduleFile);
	if (SPELLutils::isFile( path + PATH_SEPARATOR + "functions.py" ))
	{
		package = drvConfig.getIdentifier() + ".functions";
		DEBUG("[E] Importing driver package " + package);
		SPELLpythonHelper::instance().importAllFrom( package );
	}
	SPELLpythonHelper::instance().checkError();

	DEBUG("[E] Load driver - end");
	DEBUG("#############################################################");
}

//=============================================================================
// METHOD    : SPELLexecutorImpl::unloadDriver
//=============================================================================
void SPELLexecutorImpl::unloadDriver( bool shutdown )
{
    try
    {
    	DEBUG("[E] Unload driver - start");

    	SPELLnoCommandProcessing nc;

        // Just cleanup driver if reload/recover will be done
        SPELLdriverManager::instance().cleanup(shutdown);

    	DEBUG("[E] Unload driver - end");
    }
    catch(SPELLcoreException& ex)
    {
        throw ex;
    }
}

//=============================================================================
// METHOD    : SPELLexecutorImpl::handleExecutionError
//=============================================================================
const SPELLexecutorImpl::AfterError SPELLexecutorImpl::handleExecutionError()
{
    AfterError result = CANNOT_RECOVER;
    SPELLcoreException* exc = SPELLerror::instance().getError();
    bool canRestore = m_frameManager->haveWarmStart();
    bool fatalError = exc->isFatal() || (!canRestore);
    LOG_ERROR("[E] Execution failed: " + exc->what() + " can restore: " + BSTR(canRestore) + ", fatal: " + BSTR(exc->isFatal()));

    // If the error is inside user library, we do not allow recovery
    // to browsable lib ON to show where the error was.
    std::string location = SPELLerror::instance().getErrorLocation();
    DEBUG("[E] Error location is " + location);
    if ((location != "")&& (m_libPath != ""))
    {
    	if (location.find(m_libPath) == 0)
    	{
    	    m_controller->setError( "Error happened in user library: " + exc->getError(), exc->getReason(), true );
    		return CANNOT_RECOVER;
    	}
    }

    DEBUG("[E] Notify error to controller");
    // Otherwise notify the error normally
    m_controller->setError( "Execution aborted: " + exc->getError(), exc->getReason(), fatalError  );

    // If the execution frame has warmstart information
    if ( canRestore )
    {
		// Wait user request to reload or abort
		LOG_INFO("[E] Waiting for recover command");
		m_cif->waitClose();
		// This will tell us if the user wants recovery or not
		bool doRecover = m_controller->shouldRecover();
		LOG_INFO("[E] Recovery flag: " + ( doRecover ? STR("enabled") : STR("disabled")));
    	DEBUG("Do recover flag: " + BSTR(doRecover));
        if (doRecover)
        {
            LOG_INFO("[E] Recovering execution");
            m_cif->warning("Recovering execution from failure", LanguageConstants::SCOPE_SYS );
            try
            {
                try
                {
                    // No commands processed in the meantime
                	SPELLnoCommandProcessing nc;
                    // Recover the state in the frame
                    m_frameManager->fixState();
                    // Set the callstack in recovery mode so that
                    // the next module call event is not processed, and
                    // therefore add a spurious element to the stack
                    m_callstack->setRecoveryMode();
                }
                catch(SPELLcoreException& ex)
                {
                    throw ex;
                }

                // Now re-invoke execution
                DEBUG("[E] Re-executing");
                PyErr_Clear();
                result = RECOVER_SUCCESS;
            }
            catch(SPELLcoreException& ex)
            {
            	std::string msg = "[E] Recovery failed: " + ex.what();
                LOG_ERROR(msg);
                DEBUG(msg);
                m_cif->notifyError("Recovery failed", ex.what(), true);
                result = RECOVER_FAILED;
            }
        }
        else
        {
            LOG_WARN("[E] Recovery cancelled by user");
            m_cif->warning("Recovery cancelled, procedure will be aborted", LanguageConstants::SCOPE_SYS );
            result = CANCEL_RECOVER;
        }
    }
    else
    {
        LOG_ERROR("Recovery failed, warm start mechanism disabled");
        m_cif->notifyError("Recovery failed", "Warm start mechanism disabled", true);
        // result is already CANNOT_RECOVER
    }
    return result;
}

//=============================================================================
// METHOD    : SPELLexecutorImpl::dispatch
//=============================================================================
int SPELLexecutorImpl::dispatch( PyObject* obj, PyFrameObject* frame, int what, PyObject* args )
{
    //SPELLmonitor m(m_dspLock);

    // Do not dispatch if the notified code is not a procedure
    std::string file = PYSTR(frame->f_code->co_filename);

    //DEBUG("[DISPATCH] Processing file '" + file + "'")
    //DEBUG("[DISPATCH] Proc path: '" + m_procPath + "'")
    //DEBUG("[DISPATCH] Lib path: '" + m_libPath + "'")

    bool notProc = (file.find(m_procPath) == std::string::npos);
    bool notUserLib = true;
    if (notProc)
    {
		notUserLib = (m_libPath == "") || (file.find(m_libPath) == std::string::npos);
		if (notUserLib) return 0;
    }

    // Filter importing state
    std::string name = PYSTR(frame->f_code->co_name);
    // Do not dispatch if the interpreter is importing a file
    if (m_importChecker.isImporting(what,file,frame->f_lineno,name)) return 0;

    // Extract the rest of information
    std::string path = PYSTR(frame->f_code->co_filename);

    // Will provide procedure or user library id
    std::string procId = SPELLprocedureManager::instance().getProcId(path);

    int lineno = frame->f_lineno;
    std::string etype = dispatchDataType(what);

    //DEBUG("[DISPATCH] START Dispatch event in " + procId + ":" + ISTR(lineno));
    //DEBUG("     (" + name + ") at " + PYCREPR(frame));
    //DEBUG("     event type " + etype);
//    std::cerr << "################################################################################" << std::endl;
//    std::cerr << "[DISPATCH] Dispatch event in " + procId + ":" + ISTR(lineno) + " (" + name + ") at " + PYCREPR(frame) << std::endl;
//    std::cerr << "################################################################################" << std::endl;

    // Notify the frame manager to update the frame. This way, the internal models are
    // created/updated at the proper time following the procedure execution. This call
    // must be done before any other call to the frame manager that makes use of the
    // 'procedure execution model object', because it is first created at this point.
	m_frameManager->updateCurrentFrame(frame, what );

	// INIT step feature
	// IMPORTANT for this first if, there is no frame model until the first CALL dispatch
	// has been processed. So we cannot use getModel() until then. That is why we have
	// first the check to be PyTrace_LINE.
	if (!m_initStepDone &&
		what == PyTrace_LINE &&
		m_frameManager->isAtInitialFrame() &&
		m_frameManager->getModel().hasInitStep())
	{
        getCIF().disableNotifications();

		if (checkInitStep(lineno))
		{
			m_initLines.push_back(lineno);
			return 0;
		}
		else
		{
			LOG_INFO("INIT step mode finished");
			// Use the callstack to notify the executed lines during INIT step, all together,
			// to the controlling client. We do not care about dispatch and control during
			// init mode, but we want to mark the appropriate lines as executed.
			std::list<unsigned int>::const_iterator it;
			for( it = m_initLines.begin(); it != m_initLines.end(); it++ )
			{
				getCallstack().callbackEventLine(frame, procId, (*it), name);
			}
			m_initLines.clear();

         	// Re-enable notifications once INIT step is reached
            getCIF().enableNotifications();
		}
	}

    // Browsable lib feature: if the flag is enabled, allow browsing inside the user library.
    // this means that dispatch shall continue when notUserLib is false --> we make it true.
    notUserLib = notUserLib | (getConfiguration().getBrowsableLib()==ON);

    // By-step feature: if enabled, pause on each Step statement
    // Note that checkByStep cannot be used on the very first CALL notification
    // as the current SPELLexecutionModel is not created yet, therefore there would
    // be a segmentation fault.
    if ( (what == PyTrace_LINE) && getConfiguration().isByStep()) checkByStep(lineno);

    // Breakpoint feature: pause on the breakpoints
    checkBreakpoint(procId,lineno);

    // Hold the dispatching mechanism if there are commmands to be processed,
    // and until the command finishes
    {
    	SPELLsafeThreadOperations ops("SPELLexecutorImpl::dispatch()");
    	m_controller->waitCommand();
    }

    // Repeat flag is used for certain cases of skip mechanism.
    bool repeat = true;
    // Will be false if the execution has been aborted (the controller knows this)
    // if the execution has been aborted, dispatching wont be done in control objects below.
    bool statusOk = true;
    while(repeat)
    {
        // Check aborted state beforehand
        statusOk = m_controller->checkAborted();
        repeat = false;

        // Here is the in-language goto mechanism implementation. Whenever the current line is
        // a line with a programmed goto (there is a target line in the Goto model) the frame
        // lineno will be changed accordingly.
        if (m_gotoTarget != "")
        {
        	try
        	{
				repeat = repeat || m_frameManager->goLabel(m_gotoTarget, false, true);
				lineno = frame->f_lineno;
				m_gotoTarget = "";
        	}
        	catch(SPELLcoreException& ex)
        	{
				m_gotoTarget = "";
				abort(ex.what(),true);
				statusOk = false;
        	}
        }

        // If not aborted and not in User library, perform the data dispatch to each control object
        if (statusOk && notUserLib)
        {
            //DEBUG("[DISPATCH] Will do dispatch");
            switch(what)
            {
            case PyTrace_EXCEPTION:
            {
                DEBUG("[DISPATCH] Exception " + procId + ":" + ISTR(lineno) );
                DEBUG("[DISPATCH] Args: " + PYREPR(args) );
				DEBUG("[DISPATCH] Going to error state");

				notifyErrorEvent( frame, procId, lineno, name );

				if ( processException( args, lineno ) )
				{
					DEBUG("[DISPATCH] Exception is SPELL");
					return 0;
				}

				DEBUG("[DISPATCH] Python exception");
                statusOk = false;
                break;
            }
            case PyTrace_LINE:
            {
                notifyLineEvent( frame, procId, lineno, name );
                break;
            }
            case PyTrace_CALL:
            {
                notifyCallEvent( frame, procId, lineno, name );
                break;
            }
            case PyTrace_RETURN:
            {
                notifyReturnEvent( frame, procId, lineno, name );
                break;
            }
            default:
                LOG_ERROR("[DISPATCH] Uncontrolled event " + procId + ":" + ISTR(lineno) );
                break;
            }
        }
		lineno = frame->f_lineno;
		repeat = m_controller->shallRepeat();
    }

    // If we are terminating/aborting the execution, tell the frame to move to the end of
    // the bytecode. This way we guarantee nothing will be executed.
    if(!m_controller->checkAborted())
    {
        m_frameManager->terminate();
        return -1;
    }

    return 0;
//    DEBUG("[DISPATCH] EXIT dispatch event in " + procId + ":" + ISTR(frame->f_lineno));
}

//=============================================================================
// METHOD    : SPELLexecutorImpl::processException
//=============================================================================
bool SPELLexecutorImpl::processException( PyObject* data, int lineno )
{
	// Check if it is a SPELL exception (syntax, driver, etc). If so,
	// we want to KEEP the dispatching mechanism on because we still are
	// executing a procedure normally.
	// Handle name errors
	PyObject* errValue = PyTuple_GetItem(data,1);
	bool isSpell = SPELLpythonHelper::instance().isInstance( errValue, "SpellException", "spell.lib.exception" );

	// If we are in a try block there will be no error report,
	// but inform the user about the catched exception
	if (m_frameManager->getModel().isInTryBlock( lineno ))
	{
		PyObject* errType = PyTuple_GetItem(data,0);
		PyObject* errTb = PyTuple_GetItem(data,2);
		SPELLcoreException* ex = SPELLerror::instance().errorToException( errType, errType, errValue, errTb );
		if (isSpell)
		{
			m_cif->write( "SPELL exception: " + PYREPR(errValue), LanguageConstants::SCOPE_SYS);
		}
		else if (ex)
		{
			m_cif->write( ex->what(), LanguageConstants::SCOPE_SYS );
		}
	}

	return isSpell;
}

//=============================================================================
// METHOD    : SPELLexecutorImpl::setRunInto
//=============================================================================
void SPELLexecutorImpl::setRunInto( const bool enabled )
{
    if (getConfiguration().isRunInto() != enabled)
    {
        LOG_INFO("[EXEC] Run into flag set to " + (enabled ? STR("ENABLED") : STR("DISABLED")));
        getConfiguration().setRunInto(enabled);
        m_controller->enableRunInto(enabled);
    }
}

//=============================================================================
// METHOD    : SPELLexecutorImpl::setByStep
//=============================================================================
void SPELLexecutorImpl::setByStep( const bool enabled )
{
    if (getConfiguration().isByStep() != enabled)
    {
        LOG_INFO("[EXEC] By step flag set to " + (enabled ? STR("ENABLED") : STR("DISABLED")));
        getConfiguration().setByStep(enabled);
        /** \todo configure for dispatch */
    }
}

//=============================================================================
// METHOD    : SPELLexecutorImpl::setBrowsableLib
//=============================================================================
void SPELLexecutorImpl::setBrowsableLib( const SPELLbrowsableLibMode& browsable )
{
    if (getConfiguration().getBrowsableLib() != browsable)
    {
    	getConfiguration().setBrowsableLib(browsable);
        LOG_INFO("[EXEC] Browsable lib flag set to " + browsableLibModeToString(browsable) );
        /** \todo configure for dispatch */
    }
}


//=============================================================================
// METHOD    : SPELLexecutorImpl::setBrowsableLib
//=============================================================================
void SPELLexecutorImpl::setBrowsableLibStr( const std::string& browsableStr )
{
    if (getConfiguration().getBrowsableLibStr() != browsableStr)
    {
    	getConfiguration().setBrowsableLibStr(browsableStr);
    	LOG_INFO("[EXEC] Browsable lib flag set to " + browsableStr);
        /** \todo configure for dispatch */
    }
}

//=============================================================================
// METHOD    : SPELLexecutorImpl::setExecDelay
//=============================================================================
void SPELLexecutorImpl::setExecDelay( const int delay )
{
    if (getConfiguration().getExecDelay() != delay)
    {
        LOG_INFO("[EXEC] Execution delay set to " + ISTR(delay) + " milliseconds");
        getConfiguration().setExecDelay(delay);
    }
}

//=============================================================================
// METHOD    : SPELLexecutorImpl::setPromptWarningDelay
//=============================================================================
void SPELLexecutorImpl::setPromptWarningDelay( const int delay )
{
    if (getConfiguration().getPromptWarningDelay() != delay)
    {
        LOG_INFO("[EXEC] Prompt warning delay set to " + ISTR(delay));
        getConfiguration().setPromptWarningDelay(delay);
        m_scheduler->setPromptWarningDelay(delay);
    }
}

//=============================================================================
// METHOD    : SPELLexecutorImpl::setForceTcConfirm
//=============================================================================
void SPELLexecutorImpl::setForceTcConfirm( const bool force )
{
	if (getConfiguration().isForceTcConfirm() != force )
	{
		LOG_INFO("[EXEC] Set force TC confirmation to " + BSTR(force));
		getConfiguration().setForceTcConfirm(force);
		setForceTcConfirmInternal(force);
	}
}

//=============================================================================
// METHOD    : SPELLexecutorImpl::command
//=============================================================================
void SPELLexecutorImpl::setForceTcConfirmInternal( bool force )
{
	PyObject* tc = SPELLregistry::instance().get("TC");
	if (tc != NULL)
	{
		PyObject* arg = force ? Py_True : Py_False;
		SPELLpythonHelper::instance().callMethod( tc, "forceTcConfirm", arg, NULL );
	}
}

//=============================================================================
// METHOD    : SPELLexecutorImpl::command
//=============================================================================
void SPELLexecutorImpl::command( const ExecutorCommand& cmd, const bool high_priority )
{
    SPELLmonitor m(m_cmdLock);
    DEBUG("[E] Issuing command " + cmd.id);
	m_controller->command( cmd, (cmd.id != CMD_ABORT), high_priority );
}

//=============================================================================
// METHOD    : SPELLexecutorImpl::abort
//=============================================================================
void SPELLexecutorImpl::abort( const std::string& message, bool systemAborted )
{
    SPELLmonitor m(m_cmdLock);

    // Send the finish message if any
    if (message != "")
    {
    	m_cif->warning(message, systemAborted ? LanguageConstants::SCOPE_SYS : LanguageConstants::SCOPE_PROC );
    }

    // Abort command is a special case which needs this method to process
    // abort requests coming from the SPELL framework, not the user (in the
    // former case abort shall be immediate, whereas in the latter the abort
    // command shall wait till the language execution lock is released)
	ExecutorCommand cmd_abort;
	cmd_abort.id = CMD_ABORT;
	m_controller->command( cmd_abort, false, true );
}

//=============================================================================
// METHOD    : SPELLexecutorImpl::pause
//=============================================================================
void SPELLexecutorImpl::pause()
{
    SPELLmonitor m(m_cmdLock);

    // Pause command is a special case which needs this method to process
    // pause requests coming from the SPELL framework, not the user.
	ExecutorCommand cmd_pause;
	cmd_pause.id = CMD_PAUSE;
	m_controller->command( cmd_pause, false, true );
}

//=============================================================================
// METHOD    : SPELLexecutorImpl::interrupt
//=============================================================================
void SPELLexecutorImpl::interrupt()
{
    SPELLmonitor m(m_cmdLock);
	ExecutorCommand cmd_int;
	cmd_int.id = CMD_INTERRUPT;
	m_controller->command( cmd_int, false, true );
}

//=============================================================================
// METHOD    : SPELLexecutorImpl::finish
//=============================================================================
void SPELLexecutorImpl::finish( const std::string& message )
{
    SPELLmonitor m(m_cmdLock);

    // Send the finish message if any
    if (message != "")
    {
    	m_cif->warning(message, LanguageConstants::SCOPE_PROC );
    }
    // Finish command is a special case which needs this method to process
    // finish requests coming from the SPELL framework, not the user.
	ExecutorCommand cmd_finish;
	cmd_finish.id = CMD_FINISH;
	m_controller->command( cmd_finish, false, true );
}

//=============================================================================
// METHOD    : SPELLexecutorImpl::processLock
//=============================================================================
void SPELLexecutorImpl::processLock()
{
    SPELLmonitor m(m_cmdLock);
    m_controller->executionLock();
}

//=============================================================================
// METHOD    : SPELLexecutorImpl::processUnlock
//=============================================================================
void SPELLexecutorImpl::processUnlock()
{
    SPELLmonitor m(m_cmdLock);
    m_controller->executionUnlock();
}

//=============================================================================
// METHOD    : SPELLexecutorImpl::canSkip
//=============================================================================
const bool SPELLexecutorImpl::canSkip()
{
	// Allow skipping if in wait or interrupted state
	SPELLexecutorStatus status = m_controller->getStatus();
	if ((status == STATUS_WAITING)||(status == STATUS_INTERRUPTED)||(status == STATUS_PROMPT)) return true;
	return m_frameManager->canSkip();
}

//=============================================================================
// METHOD    : SPELLexecutorImpl::goNextLine
//=============================================================================
const bool SPELLexecutorImpl::goNextLine()
{
	unsigned int currentLine = m_frameManager->getCurrentLine();
	if (m_frameManager->getAST().isBlockStart(currentLine))
	{
		DEBUG("[E] Skipping entire code block");
		unsigned int nextLine = m_frameManager->getAST().getBlockEnd(currentLine);
		return goLine(nextLine);
	}
	else
	{
		DEBUG("[E] Skipping single line");
		// When we skip the line, we dont want the callstack and trace model to register the current line
		m_callstack->skipCurrentLine();
		return m_frameManager->goNextLine();
	}
}

//=============================================================================
// METHOD    : SPELLexecutorImpl::goLabel
//=============================================================================
const bool SPELLexecutorImpl::goLabel( const std::string& label, bool programmed )
{
	if (programmed)
	{
		m_gotoTarget = label;
		return true;
	}
	else
	{
		m_gotoTarget = "";
		// It is a manual goto: we want to report
		bool result = m_frameManager->goLabel(label, true, false);
		if (result)
		{
			// When we skip the line, we dont want the callstack and trace model to register the current line
			m_callstack->skipCurrentLine();
		}
		else
		{
			m_cif->warning("Unable to go to label '" + label + "'", LanguageConstants::SCOPE_SYS );
		}
		return result;
	}
}

//=============================================================================
// METHOD    : SPELLexecutorImpl::goLine
//=============================================================================
const bool SPELLexecutorImpl::goLine( const int new_lineno )
{
	// The frame manager will go to the given line only if possible, checks done inside
    bool result = m_frameManager->goLine( new_lineno, true );
    if (result)
    {
    	// When we skip the line, we dont want the callstack and trace model to register the current line
    	m_callstack->skipCurrentLine();
    }
    return result;
}

//=============================================================================
// METHOD    : SPELLexecutorImpl::setBreakpoint
//=============================================================================
const bool SPELLexecutorImpl::setBreakpoint( const std::string& file,
		                                     const unsigned int line,
		                                     const SPELLbreakpointType type )
{
	return m_frameManager->getBreakpoints().setBreakpoint( file, line, type );
}

//=============================================================================
// METHOD    : SPELLexecutorImpl::checkBreakpoint
//=============================================================================
void SPELLexecutorImpl::checkBreakpoint( const std::string& file, const unsigned int line )
{
	if (m_controller->getStatus() == STATUS_RUNNING)
	{
		if (m_frameManager->getBreakpoints().checkBreakpoint(file,line))
		{
			DEBUG("[BYSTEP] Pausing procedure on breakpoint");
			LOG_INFO("Breakpoint on line " + ISTR(line));
			pause();
		}
	}
}

//=============================================================================
// METHOD    : SPELLexecutorImpl::clearBreakpoints
//=============================================================================
void SPELLexecutorImpl::clearBreakpoints()
{
	m_frameManager->getBreakpoints().clearBreakpoints();
}

//=============================================================================
// METHOD    : SPELLexecutorImpl::checkByStep
//=============================================================================
const bool SPELLexecutorImpl::checkByStep( const int& frameLine )
{
    // Only if we are not in background (headless)
    if (getConfiguration().isHeadless()) return false;

    if (m_frameManager->getModel().isLabel(frameLine))
    {
        DEBUG("[BYSTEP] Pausing procedure");
        pause();
        return true;
    }
   return false;
}

//=============================================================================
// METHOD    : SPELLexecutorImpl::checkInitStep
//=============================================================================
const bool SPELLexecutorImpl::checkInitStep( const int& frameLine )
{
    bool abortDispatching = false;
    // Only if there is an INIT step in the code
    if (m_frameManager->getModel().hasInitStep())
    {
        // Abort the dispatching unless the current line is the INIT line
        abortDispatching = true;
        if(m_frameManager->getModel().isInitStep( frameLine ))
        {
			LOG_INFO("Pausing on INIT step on line " + ISTR(frameLine));
			m_initStepDone = true;
			// Only if we are not in background (headless)
			if (!getConfiguration().isHeadless()) pause();
			// Continue dispatching so that the
			// controller holds the execution in PAUSE
			abortDispatching = false;
        }
    }
    return abortDispatching;
}

//=============================================================================
// METHOD    : SPELLexecutorImpl::runScript
//=============================================================================
const bool SPELLexecutorImpl::runScript( const std::string& script )
{
    m_cif->setManualMode(true);
    bool result = true;
    try
    {
        m_frameManager->runScript( script );
    }
    catch(SPELLcoreException& ex)
    {
        result = false;
        m_cif->warning("Failed to execute script: " + ex.what(), LanguageConstants::SCOPE_SYS );
    }
    m_cif->setManualMode(false);
    return result;
}

//=============================================================================
// METHOD    : SPELLexecutorImpl::executeUserAction()
//=============================================================================
void SPELLexecutorImpl::executeUserAction()
{
    if (m_userAction.isEnabled())
    {
    	LOG_INFO("Executing user action function '" + m_userAction.getAction() + "'");
        try
        {
        	SPELLpythonHelper::instance().checkError();
            m_cif->warning("Running user action '" + m_userAction.getAction() + "'", LanguageConstants::SCOPE_SYS );
            std::string actionScript = m_userAction.getAction() + "()";
            m_frameManager->runScript( actionScript );
        }
        catch(SPELLcoreException& ex)
        {
        	LOG_ERROR("Failed to execute user action: " + ex.what());
            m_cif->error("Failed to execute user action: " + ex.what(), LanguageConstants::SCOPE_SYS );
        }
    }
    else
    {
    	LOG_WARN("Cannot execute user action, not enabled");
    }
}

//=============================================================================
// METHOD    : SPELLexecutorImpl::setUserAction()
//=============================================================================
void SPELLexecutorImpl::setUserAction( const SPELLuserAction& action )
{
    m_userAction = action;
    m_cif->notifyUserActionSet(action.getLabel(),action.getSeverity());
}

//=============================================================================
// METHOD    : SPELLexecutorImpl::enableUserAction()
//=============================================================================
void SPELLexecutorImpl::enableUserAction( bool enable )
{
    m_userAction.enable(enable);
    m_cif->notifyUserActionEnable(enable);
}

//=============================================================================
// METHOD    : SPELLexecutorImpl::dismissUserAction()
//=============================================================================
void SPELLexecutorImpl::dismissUserAction()
{
    m_userAction.reset();
    m_cif->notifyUserActionUnset();
}

//=============================================================================
// METHOD    : SPELLexecutorImpl::stageReached
//=============================================================================
void SPELLexecutorImpl::stageReached( const std::string& id, const std::string& title )
{
	displayStage(id,title);
    // Notify the event to the excecution frame
    m_frameManager->eventStage();
}

//=============================================================================
// METHOD    : SPELLexecutorImpl::
//=============================================================================
void SPELLexecutorImpl::displayStage( const std::string& id, const std::string& title )
{
	if (title != "")
	{
		m_cif->write("Step " + id + ": " + title, LanguageConstants::SCOPE_STEP );
	}
	else
	{
		m_cif->write("Step " + id, LanguageConstants::SCOPE_STEP );
	}
	// Change also in callstack
	m_callstack->setStage(id,title);
	// Update the data in the procedure
	PyObject* proc = getVariableManager().getVariableRef(DatabaseConstants::PROC);
	if (proc != NULL)
	{
		SPELLsafePythonOperations ops("SPELLexecutor::displayStage()");
		PyObject* pyId = SSTRPY(id);
		PyObject* pyDesc = SSTRPY(title);
		PyObject* list = PyList_New(2);
		PyList_SetItem(list,0,pyId);
		PyList_SetItem(list,1,pyDesc);
		Py_INCREF(pyId);
		Py_INCREF(pyDesc);

		PyDict_SetItemString( proc, DatabaseConstants::STEP.c_str(), list);

		PyObject* prev = PyDict_GetItemString(proc, DatabaseConstants::STEP.c_str());
		if (prev != NULL)
		{
			PyDict_SetItemString( proc, DatabaseConstants::PREV_STEP.c_str(), prev);
		}
	}
}

//=============================================================================
// METHOD    : SPELLexecutorImpl::
//=============================================================================
void SPELLexecutorImpl::addDispatchListener( SPELLdispatchListener* listener )
{
	if (std::find(m_listeners.begin(), m_listeners.end(), listener) == m_listeners.end())
	{
		DEBUG( "Added listener " + listener->getId() );
		m_listeners.push_back(listener);
	}
}

//=============================================================================
// METHOD    : SPELLexecutorImpl::
//=============================================================================
void SPELLexecutorImpl::removeDispatchListener( SPELLdispatchListener* listener )
{
	SPELLdispatchListeners::iterator it = std::find(m_listeners.begin(), m_listeners.end(), listener);
	if (it != m_listeners.end())
	{
		DEBUG( "Removed listener " + listener->getId() );
		m_listeners.erase(it);
	}
}

//=============================================================================
// METHOD    : SPELLexecutorImpl::
//=============================================================================
void SPELLexecutorImpl::notifyLineEvent( PyFrameObject* frame, const std::string& file, const int line, const std::string& name )
{
	//DEBUG( "Notify listeners: line event");
	SPELLdispatchListeners::iterator it;
	for( it = m_listeners.begin(); it != m_listeners.end(); it++)
	{
		//DEBUG( "   - notify dispatch listener " + (*it)->getId() );
		(*it)->callbackEventLine( frame, file, line, name );
	}
}

//=============================================================================
// METHOD    : SPELLexecutorImpl::
//=============================================================================
void SPELLexecutorImpl::notifyCallEvent( PyFrameObject* frame, const std::string& file, const int line, const std::string& name )
{
	//DEBUG( "Notify listeners: call event");
	SPELLdispatchListeners::iterator it;
	for( it = m_listeners.begin(); it != m_listeners.end(); it++)
	{
		//DEBUG( "   - notify dispatch listener " + (*it)->getId() );
		(*it)->callbackEventCall( frame, file, line, name );
	}
}

//=============================================================================
// METHOD    : SPELLexecutorImpl::
//=============================================================================
void SPELLexecutorImpl::notifyReturnEvent( PyFrameObject* frame, const std::string& file, const int line, const std::string& name )
{
	//DEBUG( "Notify listeners: return event");
	SPELLdispatchListeners::iterator it;
	for( it = m_listeners.begin(); it != m_listeners.end(); it++)
	{
		//DEBUG( "   - notify dispatch listener " + (*it)->getId() );
		(*it)->callbackEventReturn( frame, file, line, name );
	}
}

//=============================================================================
// METHOD    : SPELLexecutorImpl::
//=============================================================================
void SPELLexecutorImpl::notifyErrorEvent( PyFrameObject* frame, const std::string& file, const int line, const std::string& name )
{
	DEBUG( "Notify listeners: error event");
	SPELLdispatchListeners::iterator it;
	for( it = m_listeners.begin(); it != m_listeners.end(); it++)
	{
		DEBUG( "   - notify dispatch listener " + (*it)->getId() );
		(*it)->callbackEventError( frame, file, line, name );
	}
	DEBUG( "Notify listeners: error event done");
}
