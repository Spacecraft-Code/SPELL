// ################################################################################
// FILE       : SPELLframeManager.C
// DATE       : Mar 17, 2011
// PROJECT    : SPELL
// DESCRIPTION: Implementation of the Python frame manager
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
// Local includes ----------------------------------------------------------
#include "SPELL_EXC/SPELLframeManager.H"
#include "SPELL_EXC/SPELLexecutor.H"
#include "SPELL_EXC/SPELLdispatchListener.H"
// Project includes --------------------------------------------------------
#include "SPELL_UTIL/SPELLlog.H"
#include "SPELL_UTIL/SPELLutils.H"
#include "SPELL_UTIL/SPELLerror.H"
#include "SPELL_UTIL/SPELLpythonHelper.H"
#include "SPELL_UTIL/SPELLpythonMonitors.H"
#include "SPELL_DTA/SPELLdtaContainerObject.H"
#include "SPELL_WS/SPELLwsWarmStartImpl.H"
// System includes ---------------------------------------------------------

// GLOBALS /////////////////////////////////////////////////////////////////

//=============================================================================
// CONSTRUCTOR    : SPELLframeManager::SPELLframeManager
//=============================================================================
SPELLframeManager::SPELLframeManager()
: 	SPELLdispatchListener(),
	m_modelMap()
{
    m_warmStart = NULL;
    m_status = EXECUTION_UNKNOWN;
    m_code = NULL;
    m_initialFrame = NULL;
    m_currentFrame = NULL;
    m_model = NULL;
    m_filename = "";
    m_definitions = NULL;
}

//=============================================================================
// DESTRUCTOR    : SPELLframeManager::~SPELLframeManager
//=============================================================================
SPELLframeManager::~SPELLframeManager()
{
	reset();
    if (m_warmStart != NULL)
    {
        delete m_warmStart;
    }
    if (m_code != NULL)
    {
        Py_XDECREF(m_code);
    }
    if (m_initialFrame != NULL)
    {
        Py_XDECREF(m_initialFrame);
    }
    m_discardedNames.clear();
}

//=============================================================================
// METHOD     : SPELLframeManager::initialize()
//=============================================================================
void SPELLframeManager::initialize( const std::string& scriptFile )
{
    m_filename = scriptFile;

    // Enable or disable monitoring according to configuration
    SPELLvariableMonitor::s_enabled = SPELLexecutor::instance().getConfiguration().isWatchEnabled();

    // Compile the script and create the Python frame
    // to execute bytecode
    reset();
}

//=============================================================================
// METHOD    : SPELLframeManager::saveState
//=============================================================================
void SPELLframeManager::saveState()
{
    // If we have warmstart support
	if (m_warmStart)
	{
		DEBUG("[EF] Performing on demand state save");
		m_warmStart->saveState();
	}
}

//=============================================================================
// METHOD    : SPELLframeManager::restoreState
//=============================================================================
void SPELLframeManager::restoreState()
{
    // If we have warmstart support
    if (m_warmStart)
    {
        DEBUG( "[EF] Starting recovery");
        // Remove the current frame if any
        if (m_initialFrame != NULL)
        {
            Py_XDECREF(m_initialFrame);
            m_initialFrame = NULL;
        }
        // Recover the frame
        m_initialFrame = m_warmStart->restoreState();

        // Re-create the internal models
        DEBUG( "[EF] Re-create internal models");

        // We need to reverse the frames for the recreation
        std::list<PyFrameObject*> frames;
        PyFrameObject* currentFrame = m_initialFrame;
        while(currentFrame != NULL)
        {
        	frames.push_back(currentFrame);
        	currentFrame = currentFrame->f_back;
        }
        std::list<PyFrameObject*>::iterator it;
        int callLine = 0;
        for( it = frames.begin(); it != frames.end(); it++)
        {
        	currentFrame = *it;
        	createFrameModel( callLine, currentFrame );
        	callLine = currentFrame->f_lineno;
        }
        LOG_INFO( "Recovered state at: " + PYREPR(m_initialFrame->f_code->co_filename) + ":" + ISTR(m_initialFrame->f_lineno));
    }
    else
    {
        THROW_EXCEPTION("Cannot restore state", "Warm start mechanism unavailable", SPELL_ERROR_WSTART);
    }
}

//=============================================================================
// METHOD    : SPELLframeManager::fixState
//=============================================================================
void SPELLframeManager::fixState()
{
    // If we have warmstart support
    if (m_warmStart)
    {
        DEBUG( "Starting state fix");
#ifdef WITH_DEBUG
	DEBUG("*******************************************************");
	DEBUG("[EF] Current keys in list: " );
	for(KeyList::iterator dkit = m_modelKeys.begin(); dkit != m_modelKeys.end(); dkit++)
	{
		DEBUG("   - " + *dkit );
	}

	DEBUG("[EF] Current keys in map:");
	for( ModelMap::iterator dmit = m_modelMap.begin(); dmit != m_modelMap.end(); dmit++ )
	{
		DEBUG("   - Key: " + dmit->first);
	}
	DEBUG("*******************************************************");
#endif
        // Fix the error state
        m_initialFrame = m_warmStart->fixState();
        LOG_INFO( "Fixed state at: " + PYREPR(m_initialFrame->f_code->co_filename) + ":" + ISTR(m_initialFrame->f_lineno));
    }
    else
    {
        THROW_EXCEPTION("Cannot fix state", "Warm start mechanism unavailable", SPELL_ERROR_WSTART);
    }
}

//=============================================================================
// METHOD     : SPELLframeManager::compile
//=============================================================================
void SPELLframeManager::compile()
{
    assert( m_filename != "");
    LOG_INFO("[EF] Compiling procedure " + m_filename);

    // Will hold the bytecode code object
    if (m_code != NULL)
    {
        Py_XDECREF(m_code);
        m_code = NULL;
    }

    m_code = SPELLpythonHelper::instance().compile(m_filename);

	LOG_INFO("[EF] Compilation success");
}

//=============================================================================
// METHOD     : SPELLframeManager::compileScript
//=============================================================================
PyCodeObject* SPELLframeManager::compileScript( const std::string& script )
{
    LOG_INFO("[EF] Compiling user script");

	// Will hold the code object
    PyCodeObject* code = SPELLpythonHelper::instance().compileScript(script);

    LOG_INFO("[EF] Compilation success");
    return code;
}

//=============================================================================
// METHOD     : SPELLframeManager::createInitialFrame
//=============================================================================
void SPELLframeManager::createInitialFrame()
{
    // Obtain the main module, this is already setup by Py_Initialize
    DEBUG("[EF] Creating initial frame");

    if (m_initialFrame != NULL)
    {
        Py_DECREF(m_initialFrame);
        m_initialFrame = NULL;
    }

    m_initialFrame = SPELLpythonHelper::instance().createFrame( m_filename, m_code );

    // Force the first frame update
    if (m_currentFrame != NULL)
    {
        Py_XDECREF(m_currentFrame);
        m_currentFrame = NULL;
    }

    updateCurrentFrame( m_initialFrame, true );

    DEBUG("[EF] Initial frame ready");
}

//=============================================================================
// METHOD     : SPELLframeManager::reset
//=============================================================================
void SPELLframeManager::reset()
{
    DEBUG("[EF] Resetting");
	// Reset warmstart mechanism in case of reload
    if (m_warmStart) m_warmStart->reset();

    DEBUG("[EF] Removing analysis models");
    // Remove execution analysis models
    ModelMap::iterator mit;
    for( mit = m_modelMap.begin(); mit != m_modelMap.end(); mit++)
    {
    	delete mit->second;
    }
    m_modelKeys.clear();
    m_modelMap.clear();
    m_model = NULL;

    DEBUG("[EF] Clearing breakpoints");
    // Clear all defined breakpoints
    m_breakpoints.clearBreakpoints();

    DEBUG("[EF] Clearing execution trace");
    // Remove the execution trace markers
    resetExecutionTrace();

    // Reset error information
    SPELLerror::instance().clearErrors();
    m_terminated = false;

    DEBUG("[EF] Re-compiling");
    // Compile the script.
    compile();

    DEBUG("[EF] Clear error mode");
    m_inError = false;

    // Create the Python frame for execution
    createInitialFrame();

    // Initialize the globals filter
    PyObject* mainDict = SPELLpythonHelper::instance().getMainDict();
    SPELLwsWarmStartImpl::setGlobalsFilter( PyDict_Keys(mainDict) );

    DEBUG("[EF] Reset done.");
}

//=============================================================================
// METHOD     : SPELLframeManager::terminate
//=============================================================================
void SPELLframeManager::terminate()
{
	if (!m_terminated)
	{
		DEBUG("[EF] Terminating execution");
		SPELLpythonHelper::instance().acquireGIL();
		// Note: we substract 3 bytes since the last two instructions are always
		// resembling a "return None" statement. We want the instruction previous
		// to the last one here.
		ModelMap::iterator end = m_modelMap.end();
		for(ModelMap::iterator it = m_modelMap.begin(); it != end; it++)
		{
			PyFrameObject* frame = it->second->getFrame();
			int lasti = it->second->getLastAddress() -3 ;
			int lineno = it->second->getLastLine();
			DEBUG("[EF] Setting lasti " + ISTR(lasti) + " lineno " + ISTR(lineno) + " on " + PYCREPR(frame));
			SPELLpythonHelper::instance().setNewLine(frame,lineno,lasti);
			DEBUG("Frame stack top " + PSTR(frame->f_stacktop));
			DEBUG("Frame value stack " + PSTR(frame->f_valuestack));
		}
		m_terminated = true;
		DEBUG("[EF] Terminating execution finished");
	}
}

//=============================================================================
// METHOD     : SPELLframeManager::execute
//=============================================================================
const SPELLexecutionResult SPELLframeManager::execute()
{
	SPELLerror::instance().clearErrors();
    PyFrameObject* frame = m_initialFrame;
    m_status = EXECUTION_SUCCESS;

    DEBUG("[EF] Starting execution loop, initial frame " + PYCREPR(frame) );
    while(frame!= NULL)
    {
        DEBUG("[EF] Running frame " + PYCREPR(frame));
        PyEval_EvalFrame( frame );
        DEBUG("[EF] Frame finished " + PYCREPR(frame));
        DEBUG("[EF] Next frame " + PYCREPR(frame));
        // Check errors
        checkRuntimeError();
        // If there is any error, stop right away. Status will be
        // updated by the function above.
        if (m_terminated || SPELLerror::instance().inError()) break;
        // Go to next frame, if any
        frame = frame->f_back;
    }
    return m_status;
}

//=============================================================================
// METHOD    : SPELLframeManager::checkRuntimeError
//=============================================================================
void SPELLframeManager::checkRuntimeError()
{
	// Ensure that the error information is cleaned up
	SPELLerror::instance().updateErrors();
	DEBUG("[EF] Checking runtime errors");
	if (SPELLerror::instance().isExecutionAborted())
	{
		DEBUG("[EF] Execution aborted exception");
		m_status = EXECUTION_ABORTED;
	}
    else if (SPELLerror::instance().isExecutionTerminated())
    {
        DEBUG("[EF] Execution terminated exception");
        m_status = EXECUTION_TERMINATED;
    }
    else if (SPELLerror::instance().inError())
    {
		DEBUG("[EF] Execution loop aborted due to an error");
		m_status = EXECUTION_ERROR;
    }
}

//=============================================================================
// METHOD    : SPELLframeManager::retrieveDiscardedNames
//=============================================================================
void SPELLframeManager::retrieveDiscardedNames(PyFrameObject* frame)
{
	PyObject* itemList = PyDict_Keys(frame->f_globals);
	unsigned int numItems = PyList_Size(itemList);
	DEBUG("[F] Names to discard: " + ISTR(numItems) );
	for( unsigned int index = 0; index<numItems; index++)
	{
		PyObject* key = PyList_GetItem( itemList, index );
		m_discardedNames.insert( PYSSTR(key) );
	}
	Py_XDECREF(itemList);
}

//=============================================================================
// METHOD    : SPELLframeManager::getCurrentLine()
//=============================================================================
const unsigned int SPELLframeManager::getCurrentLine()
{
	return m_currentFrame->f_lineno;
}

//=============================================================================
// METHOD    : SPELLframeManager::canSkip
//=============================================================================
const bool SPELLframeManager::canSkip()
{
    SPELLsafePythonOperations ops("SPELLframeManager::canSkip()");
    int new_lineno = -1;
    int currentLine = getCurrentLine();

	DEBUG("[F] Check can skip line " + ISTR( currentLine ));

    bool isSimpleLine = m_ast.isSimpleLine( currentLine );
    bool isBlockStart = m_ast.isBlockStart( currentLine );
    bool isInsideBlock = m_ast.isInsideBlock( currentLine );
    bool lineAfterTryBlock = true;

    // Special treatment for try blocks
    bool isEndTryBlock = (getModel().isInTryBlock(m_currentFrame->f_lineno) && getModel().isTryBlockEnd(m_currentFrame->f_lineno));

    if (isEndTryBlock)
    {
		DEBUG("[F] Line " + ISTR( getCurrentLine() ) + " is the last one in a try-except block");
        // The current lnotab model will find the next available line number in the
        // current code. If there is no such line it returns -1.
        new_lineno = getModel().lineAfter( getModel().tryBlockEndLine( m_currentFrame->f_lineno ) );

        // If there is no line avaiable after, dont do the go next
        if (new_lineno == -1)
		{
    		DEBUG("[F] Cannot find line after the try-except block");
        	lineAfterTryBlock = false;
		}
    }

    // Special condition for try blocks
    if (isEndTryBlock && !lineAfterTryBlock)
	{
    	std::string msg = "Cannot skip line " + ISTR(currentLine) + ", cannot find line after try-except block";
		LOG_WARN("[F] " + msg);
		SPELLexecutor::instance().getCIF().warning(msg, LanguageConstants::SCOPE_SYS );
    	return false;
	}

    if (isInsideBlock)
    {
    	std::string msg = "Cannot skip line " + ISTR(currentLine) + ", it is inside a code block";
		LOG_WARN("[F] " + msg);
		SPELLexecutor::instance().getCIF().warning(msg, LanguageConstants::SCOPE_SYS );
    	return false;
	}

    if (getModel().getLastLine() == currentLine)
    {
		DEBUG("[F] Line " + ISTR( currentLine ) + " is the last one in the code block");
    	if (!getModel().isReturnConstant(currentLine))
    	{
        	std::string msg = "Cannot skip this type of return statement at line " + ISTR(currentLine);
    		LOG_WARN("[F] " + msg);
    		SPELLexecutor::instance().getCIF().warning(msg, LanguageConstants::SCOPE_SYS );
    		return false;
    	}
    }
    else
    {
		DEBUG("[F] Line " + ISTR( currentLine ) + " is NOT the last one in the code block");
    }

    if ( !isSimpleLine && !isBlockStart)
    {
    	std::string msg = "Cannot skip line " + ISTR(currentLine);
		LOG_WARN("[F] " + msg);
		SPELLexecutor::instance().getCIF().warning(msg, LanguageConstants::SCOPE_SYS );
    	return false;
    }
	return true;
}

//=============================================================================
// METHOD    : SPELLframeManager::goNextLine
//=============================================================================
const bool SPELLframeManager::goNextLine()
{
    bool controllerContinue = true;
    SPELLsafePythonOperations ops("SPELLframeManager::goNextLine()");
    int new_lineno = -1;
    unsigned int currentLine = m_currentFrame->f_lineno;

	DEBUG("[F] Go to next line");

    // Special treatment for try blocks
    if (getModel().isInTryBlock(currentLine) && getModel().isTryBlockEnd(currentLine))
    {
		DEBUG("[F] Line " + ISTR(m_currentFrame->f_lineno) + " is the last one in a try-except block");
        // The current lnotab model will find the next available line number in the
        // current code. If there is no such line it returns -1.
        new_lineno = getModel().lineAfter( getModel().tryBlockEndLine( currentLine ) );
    }
    else
    {
        // The current lnotab model will find the next available line number in the
        // current code. If there is no such line it returns -1.
        new_lineno = getModel().lineAfter( currentLine );
    }

    // If there is no next line in this frame
    if (new_lineno == -1)
    {
    	// If we are already in the last line of the code block:
    	// 1. It is a "return <constant>" statement: we can skip it by setting the lasti
    	//    (current instruction) to be the last one in the LNOTAB. That way
    	//    the interpreter will just jump out of the function.
    	// 2. It is a "return <fast>": we can skip but there is the risk of using an undefined var.
    	// 3. It is a "return <call>": we cannot skip as it leads to inconsistent code.
		DEBUG("[F] No next line available in frame at " + PSTR(m_currentFrame));
    	if (((unsigned int)getModel().getLastLine())==currentLine)
    	{
    		// Get the last instruction address
    		int lastAddress = getModel().getLastAddress();
    		// Set the instruction pointer so that we make the return statement
    		lastAddress -= 3;
    		// If we are already in the last instruction set, do nothing
    		if (m_currentFrame->f_lasti != lastAddress)
    		{
        		DEBUG("[F] Set instruction to " + ISTR(lastAddress));
    			setNewLine( currentLine, lastAddress );
    		}
    	}
		controllerContinue = false;
    }
    else
    {
        DEBUG("[F] Next line available in frame is " + ISTR(new_lineno));
        // If there is next line, try to find the corresponding instruction offset
        int new_lasti = getModel().offset(new_lineno);
        if ( new_lasti == -1 )
        {
            DEBUG("[F] No next instruction available in frame at " + PSTR(m_currentFrame));
			controllerContinue = false;
        }
        else
        {
            DEBUG("[F] Go next line " + ISTR(new_lineno) + " at " + ISTR(new_lasti));
			controllerContinue = setNewLine( new_lineno, new_lasti );
        }
    }

    return controllerContinue;
}


//=============================================================================
// METHOD    : SPELLframeManager::goLabel
//=============================================================================
const bool SPELLframeManager::goLabel( const std::string& label, bool report, bool programmed )
{
	SPELLsafePythonOperations ops("SPELLframeManager::goLabel()");
    LOG_INFO("[F] Go-to label '" + label + "'");
    std::map<std::string,unsigned int> labels = m_model->getLabels();
    std::map<std::string,unsigned int>::const_iterator it = labels.find(label);
    if ( it == labels.end() )
    {
        LOG_ERROR("[F] Could not find label '" + label + "'");
        if (programmed)
        {
        	THROW_EXCEPTION("Unable to jump", "Label '" + label + "' not found", SPELL_ERROR_EXECUTION);
        }
        else if (report)
	    {
	    	SPELLexecutor::instance().getCIF().warning("Unable to go to label '" + label + "', not found", LanguageConstants::SCOPE_SYS );
	    }
        return false;
    }
    else
    {
        return goLine( (*it).second, report );
    }
}

//=============================================================================
// METHOD    : SPELLframeManager::goLine
//=============================================================================
const bool SPELLframeManager::goLine( const int& new_lineno, bool report )
{
	SPELLsafePythonOperations ops("SPELLframeManager::goLine()");
    DEBUG("[F] Go-to line " + ISTR(new_lineno) + " on model " + PSTR(m_model));

    // We cannot jump in the middle of a block
	if (m_ast.isInsideBlock(new_lineno))
	{
		LOG_WARN("[F] Cannot go to line " + ISTR(new_lineno) + ", it is inside a block");
	    if (report)
	    {
	    	SPELLexecutor::instance().getCIF().warning("Unable to go to line " + ISTR(new_lineno) + ", it is inside of a code block", LanguageConstants::SCOPE_SYS );
	    }
		return false;
	}

	// We cannot jump if the target line is not executable
	if (!getModel().isExecutable(new_lineno))
	{
		LOG_WARN("[F] Cannot go to line " + ISTR(new_lineno));
	    if (report)
	    {
	    	SPELLexecutor::instance().getCIF().warning("Unable to go to line " + ISTR(new_lineno), LanguageConstants::SCOPE_SYS );
	    }
		return false;
	}

	// We cannot jump inside blocks
	bool inTry = getModel().isInTryBlock(new_lineno);
	bool inExc = getModel().isInExceptBlock(new_lineno);
	bool inFin = getModel().isInFinallyBlock(new_lineno);
	if (inTry | inExc | inFin )
	{
		LOG_WARN("[F] Cannot go to line " + ISTR(new_lineno));
	    if (report)
	    {
	    	SPELLexecutor::instance().getCIF().warning("Unable to go to line " + ISTR(new_lineno), LanguageConstants::SCOPE_SYS );
	    }
		return false;
	}

    int new_lasti = getModel().offset(new_lineno);
    if ( new_lasti == -1 )
    {
		LOG_WARN("[F] Cannot go to line " + ISTR(new_lineno) + " unable to find corresponding instruction");
	    if (report)
	    {
	    	SPELLexecutor::instance().getCIF().warning("Unable to go to line '" + ISTR(new_lineno) + "', cannot find destination", LanguageConstants::SCOPE_SYS );
	    }
        return false;
    }

    bool result = false;
    try
    {
    	result = setNewLine( new_lineno, new_lasti );
        DEBUG("[F] Go new line " + ISTR(new_lineno) + " at " + ISTR(new_lasti));
        if (report)
        {
        	SPELLexecutor::instance().getCIF().write("Jump to line " + ISTR(new_lineno), LanguageConstants::SCOPE_SYS );
        }
    }
    catch(SPELLcoreException& ex)
    {
    	SPELLexecutor::instance().getCIF().warning("Unable to go to line " + ISTR(new_lineno) + ", " + ex.what(), LanguageConstants::SCOPE_SYS );
    }

    return result;
}

//=============================================================================
// METHOD      : SPELLframeManager::programmedGoto
//=============================================================================
bool SPELLframeManager::programmedGoto( const int& frameLine )
{
    int target = m_model->getTargetLine(frameLine);
    bool gotoSuccess = false;
    if (target != -1)
    {
        DEBUG("[GOTO] Programmed jump at line " + ISTR(frameLine) + " to line " + ISTR(target));
        LOG_INFO("Goto " + ISTR(target));
        gotoSuccess = goLine(target, false);
    }
    return gotoSuccess;
}

//=============================================================================
// METHOD    : SPELLframeManager::runScript
//=============================================================================
void SPELLframeManager::runScript( const std::string& script )
{
	DEBUG("[EF] Executing script TRY-IN");
	SPELLsafePythonOperations ops("SPELLframeManager::runScript()");
	DEBUG("[EF] Executing script IN");
	try
    {
    	PyCodeObject* code = compileScript( std::string(script) );
    	DEBUG("[EF] Executing script on frame " + PYCREPR(m_currentFrame));
    	LOG_INFO("Executing script: '" + script + "'");
    	// We need to take into account the fast locals. First the copy the fast locals to normal locals,
    	// and after the execution we UPDATE the fast locals, so that the values get updated in case of
    	// value redefinition in the script command.
    	PyFrame_FastToLocals(m_currentFrame);
    	PyEval_EvalCode(code, m_currentFrame->f_globals, m_currentFrame->f_locals);
    	PyFrame_LocalsToFast(m_currentFrame,0);
        SPELLpythonHelper::instance().checkError();
    	// See model update its information
    	getModel().update();
    	DEBUG("[EF] Executing script OUT");
    }
	catch (SPELLcoreException& ex)
	{
		LOG_ERROR("Error while executing script: " + ex.what());
		// Reset the error data in Python layer. We do not want the interpreter to
		// think that there is an error in the procedure execution.
		PyErr_Print();
		PyErr_Clear();
		throw ex;
	}
}

//=============================================================================
// METHOD    : SPELLframeManager::callbackEventCall
//=============================================================================
void SPELLframeManager::callbackEventCall( PyFrameObject* frame, const std::string& file, const int line, const std::string& name )
{
	DEBUG("[EF] Event CALL on frame " + PYCREPR(frame) + " - " + file + ":" + ISTR(line) + " (" + name + ")");

	DEBUG("[EF] Error mode check: " + BSTR(m_inError));
	if (m_inError)
	{
		DEBUG("[EF] Discard call event meanwhile in error mode");
		return;
	}

    // We use the call line to identify different calls of the same code block (functions)
    // in the same procedure
    int callLine = 0;
    if (m_currentFrame != NULL)
    {
    	callLine = m_currentFrame->f_lineno;
    }

    // Update the current frame, and create a model if needed, or reuse the existing one
	createFrameModel( callLine, frame );

	// Update the AST analyzer to set the proper current file
	m_ast.process( PYSTR(frame->f_code->co_filename) );

	DEBUG("------------------------------------------------------------------------");
	DEBUG("[EF] Using model " + PSTR(m_model) + " -- " + m_model->getIdentifier());
    DEBUG("     Frame model: " + PSTR(m_model->getFrame()));
	DEBUG("------------------------------------------------------------------------");

	// Notify the scope change
	m_model->scopeChanged();

	// Notify the warmstart mechanism
	if (m_warmStart)
	{
		m_warmStart->notifyCall( m_model->getIdentifier(), frame );
	}
}

//=============================================================================
// METHOD    : SPELLframeManager::callbackEventLine
//=============================================================================
void SPELLframeManager::callbackEventLine( PyFrameObject* frame, const std::string& file, const int line, const std::string& name )
{
	DEBUG("[EF] Event LINE on frame " + PYCREPR(frame) + " - " + file + ":" + ISTR(line) + " (" + name + ")");
	DEBUG("[EF] Using model " + PSTR(m_model) + " -- " + m_model->getIdentifier());
    DEBUG("     Frame model: " + PSTR(m_model->getFrame()));

	// Notify the warmstart mechanism
	if (m_warmStart)
	{
		// Warmstart data shall not be built/updated in the middle of a code block
		// Otherwise we may break things when doing the iterator construction on
		// a 'for' loop, for example.
		unsigned int lineno = frame->f_lineno;
		bool doNotify = m_ast.isSimpleLine(lineno) || m_ast.isBlockStart(lineno);
		if (doNotify)
		{
			DEBUG("[EF] Notifying line event to warmstart");
			m_warmStart->notifyLine();
		}
	}
	// See model update its information (including variable monitor if enabled)
	getModel().update();

	DEBUG("[EF] Reset error mode");
	m_inError = false;
}

//=============================================================================
// METHOD    : SPELLframeManager::callbackEventReturn
//=============================================================================
void SPELLframeManager::callbackEventReturn( PyFrameObject* frame, const std::string& file, const int line, const std::string& name )
{
	DEBUG("[EF] Event RETURN on frame " + PYCREPR(frame) + " - " + file + ":" + ISTR(line) + " (" + name + ")");
	DEBUG("[EF] Back frame is " + PYCREPR(frame->f_back) );

	if (!m_model)
	{
		THROW_EXCEPTION("Failed to process return event", "No model available", SPELL_ERROR_EXECUTION);
	}

	DEBUG("[EF] Error mode check: " + BSTR(m_inError));
	if (m_inError)
	{
		DEBUG("[EF] Discard return event meanwhile in error mode");
		return;
	}

	// Notify the warmstart mechanism
	if (m_warmStart)
	{
		m_warmStart->notifyReturn();
	}

	std::string currentModelId = m_model->getIdentifier();
	unsigned int numModels = m_modelKeys.size();

//#ifdef WITH_DEBUG
//	DEBUG("*******************************************************");
//	DEBUG("[EF] Current keys in list: " );
//	for(KeyList::iterator dkit = m_modelKeys.begin(); dkit != m_modelKeys.end(); dkit++)
//	{
//		DEBUG("   - " + *dkit );
//	}
//
//	DEBUG("[EF] Current keys in map:");
//	for( ModelMap::iterator dmit = m_modelMap.begin(); dmit != m_modelMap.end(); dmit++ )
//	{
//		DEBUG("   - Key: " + dmit->first);
//	}
//	DEBUG("*******************************************************");
//#endif

	if (numModels>1)
	{
		KeyList::iterator kit = m_modelKeys.end();
		kit--;
		DEBUG("[EF] Removing top model key " + *kit);
		if (kit != m_modelKeys.begin())
		{
			m_modelKeys.erase(kit);
		}
		ModelMap::iterator it = m_modelMap.find(currentModelId);
		if (it != m_modelMap.end())
		{
			DEBUG("[EF] Removing frame model");
			delete it->second;
			m_modelMap.erase(it);
		}
		TraceMap::iterator tit = m_traceMap.find(currentModelId);
		if (tit != m_traceMap.end())
		{
			DEBUG("[EF] Removing trace model");
			m_traceMap.erase(tit);
		}

		// Set the current model as the back one
		std::string backModelId = m_modelKeys.back();
		DEBUG("[EF] Setting back model " + backModelId);
		it = m_modelMap.find( backModelId );
		if (it != m_modelMap.end())
		{
			m_model = it->second;
			m_currentFrame = m_model->getFrame();
			DEBUG("[EF] Current frame is " + PSTR(m_currentFrame));
		}
		else
		{
			THROW_EXCEPTION("Failed to process return event", "Back model not found: '" + backModelId + "'", SPELL_ERROR_EXECUTION);
		}
	}
	else
	{
		LOG_WARN("Unable to remove model '" + currentModelId + "': we are at root!");
	}

	// Notify the scope change with the new model
	m_model->scopeChanged();

	DEBUG("------------------------------------------------------------------------");
	DEBUG("[EF] After return: using model " + PSTR(m_model) + " -- " + m_model->getIdentifier());
    DEBUG("     Frame model: " + PSTR(m_model->getFrame()));
	DEBUG("------------------------------------------------------------------------");

//#ifdef WITH_DEBUG
//	DEBUG("*******************************************************");
//	DEBUG("[EF] Current keys in list: " );
//	for(KeyList::iterator dkit = m_modelKeys.begin(); dkit != m_modelKeys.end(); dkit++)
//	{
//		DEBUG("   - " + *dkit );
//	}
//
//	DEBUG("[EF] Current keys in map:");
//	for( ModelMap::iterator dmit = m_modelMap.begin(); dmit != m_modelMap.end(); dmit++ )
//	{
//		DEBUG("   - Key: " + dmit->first);
//	}
//	DEBUG("*******************************************************");
//#endif
}

//=============================================================================
// METHOD    : SPELLframeManager::eventStage
//=============================================================================
void SPELLframeManager::callbackEventError( PyFrameObject* frame, const std::string& file, const int line, const std::string& name )
{
	DEBUG("[EF] Go to error mode");
	m_inError = true;
}

//=============================================================================
// METHOD    : SPELLframeManager::eventStage
//=============================================================================
void SPELLframeManager::eventStage()
{
	DEBUG("[EF] Event stage on current frame");
	// Notify the warmstart mechanism
	if (m_warmStart)
	{
		m_warmStart->notifyStage();
	}
}

//=============================================================================
// METHOD    : SPELLframeManager::updateCurrentFrame
//=============================================================================
void SPELLframeManager::updateCurrentFrame( PyFrameObject* frame, int dispatchType )
{
	if (m_currentFrame == frame) return;

	if (!SPELLerror::instance().inError() && (dispatchType == PyTrace_CALL) )
	{
	    DEBUG("[EF] Update frame: current is " + PSTR(m_currentFrame) + ", new is " + PSTR(frame));
		// Update data in the new frame
	    updateVariables( frame );
		updateDefinitions( m_currentFrame, frame );
		updateDatabases( frame );
	}
}

//=============================================================================
// METHOD    : SPELLframeManager::updateVariables()
//=============================================================================
void SPELLframeManager::updateVariables( PyFrameObject* frame )
{
	// Create the set if initial names to be discarded when retrieving variables
	if  (m_discardedNames.size() == 0)
	{
		retrieveDiscardedNames(frame);
	}
}

//=============================================================================
// METHOD    : SPELLframeManager::copyDatabase()
//=============================================================================
void SPELLframeManager::copyDatabase( const std::string& database, PyFrameObject* toFrame )
{
	DEBUG("[EF] Copy database " + database + " into " + PSTR(toFrame));
	if (m_currentFrame == NULL || m_currentFrame->f_globals == NULL)
	{
		LOG_ERROR("Unable to copy database reference: " + database + " no source frame or no globals");
		return;
	}
	// Borrowed
	PyObject* db = PyDict_GetItemString(m_currentFrame->f_globals, database.c_str());

	if (database == DatabaseConstants::IVARS || database == DatabaseConstants::ARGS )
	{
		SPELLdtaContainerObject* dta = reinterpret_cast<SPELLdtaContainerObject*>(db);
		dta->container->incref();
	}

	// If the database is not found in current frame, copy it from __main__, if available
	if (db == NULL)
	{
		// New reference
		db = SPELLpythonHelper::instance().getObject("__main__", database);
	}
	if (db)
	{
		DEBUG("[EF] Copy database now");
		Py_INCREF(db);
		PyDict_SetItemString(toFrame->f_globals, database.c_str(), db);
		PyDict_SetItemString(toFrame->f_locals, database.c_str(), db);
	}
	else
	{
		LOG_ERROR("Unable to copy database reference: " + database + " on " + PYCREPR(toFrame));
	}
}

//=============================================================================
// METHOD    : SPELLframeManager::updateDatabases()
//=============================================================================
void SPELLframeManager::updateDatabases( PyFrameObject* frame )
{
	// Update SCDB and GDB instances in locals so that they are directly available in functions
	// Do this in functions only, not in main frame
	if (m_currentFrame != NULL)
	{
		DEBUG("[EF] Copying databases on " + PYCREPR(frame) );
		PyFrame_FastToLocals(frame);

		copyDatabase( DatabaseConstants::SCDB, frame );
		copyDatabase( DatabaseConstants::GDB, frame );
		copyDatabase( DatabaseConstants::PROC, frame );
		//copyDatabase( DatabaseConstants::ARGS, frame );
		//copyDatabase( DatabaseConstants::IVARS, frame );

		PyFrame_LocalsToFast(frame,1);

		SPELLpythonHelper::instance().checkError();
	}
}

//=============================================================================
// METHOD    : SPELLframeManager::updateDefinitions
//=============================================================================
void SPELLframeManager::updateDefinitions( PyFrameObject* source, PyFrameObject* target )
{
    // Update SPELL language definitions if required
    // Definitions list will be NULL if there is no warmstart available
    if (m_definitions != NULL)
    {
		DEBUG("[EF] Copying SPELL definitions");
		unsigned int numKeys = PyList_Size(m_definitions);
		for( unsigned int count = 0; count < numKeys; count++)
		{
			PyObject* key = PyList_GetItem( m_definitions, count );
			if (PYSTR(key) == "ARGS" || PYSTR(key) == "IVARS" || PYSTR(key) == "__name__") continue;
			if (!PyDict_Contains(target->f_globals, key))
			{
				PyObject* obj = PyDict_GetItem( source->f_globals, key );
				if (obj != NULL)
				{
					//DEBUG("   Copying " + PYREPR(key))
					Py_INCREF(key);
					Py_INCREF(obj);
					PyDict_SetItem( target->f_globals, key, obj );
				}
			}
		}
		DEBUG("Frame " + PSTR(target) + "," + PYSTR(target->f_code->co_filename) + ":" + ISTR(target->f_lineno));
    }
}

//=============================================================================
// METHOD    : SPELLframeManager::createFrameModel()
//=============================================================================
void SPELLframeManager::createFrameModel( int callLine, PyFrameObject* frame )
{
	// Build the code block identifier
	std::string filename = PYSTR(frame->f_code->co_filename);
	std::string codename = PYSTR(frame->f_code->co_name);
    static unsigned int tail = 0;
    // Get time of day
    struct timeval time;
    gettimeofday(&time, NULL);

	std::string code_id = filename + "-" + codename + ":" + ISTR(callLine) + ISTR(time.tv_sec) + "." + ISTR(time.tv_usec) + "-" + ISTR(tail);
    tail++;

	DEBUG("[EF] Create frame model for " + code_id);

    // Make the current frame the new one
	m_currentFrame = frame;

	// Generate or reuse execution model
	ModelMap::iterator mit;

#ifdef WITH_DEBUG
	DEBUG("*******************************************************");
	DEBUG("[EF] Current keys in list: " );
	for(KeyList::iterator dkit = m_modelKeys.begin(); dkit != m_modelKeys.end(); dkit++)
	{
		DEBUG("   - " + *dkit );
	}

	DEBUG("[EF] Current keys in map:");
	for( ModelMap::iterator dmit = m_modelMap.begin(); dmit != m_modelMap.end(); dmit++ )
	{
		DEBUG("   - Key: " + dmit->first);
	}
	DEBUG("*******************************************************");
#endif

	mit = m_modelMap.find(code_id);
	if (mit == m_modelMap.end())
	{
		DEBUG("[EF] Creating model");
		m_model = new SPELLexecutionModel( code_id,
				                           filename,
				                           frame,
										   m_discardedNames);


        // Initialize the variable monitor
        m_model->getVariableMonitor().initialize();

		m_modelMap.insert( std::make_pair( code_id, m_model ));

		// Validate the data analyzed by the model
		std::string errors = m_model->validateGotos();
		if (errors != "")
		{
			DEBUG("[EF] WARNING missing gotos!");
			std::string msg1 = "WARNING: found wrong 'Goto()' sentences in this procedure";
			std::string msg2 = "Please check the code, the following target labels were not found: " + errors;
			LOG_WARN(msg1);
			LOG_WARN(msg2);
			SPELLexecutor::instance().getCIF().warning(msg1, LanguageConstants::SCOPE_SYS );
			SPELLexecutor::instance().getCIF().warning(msg2, LanguageConstants::SCOPE_SYS );
			SPELLexecutor::instance().pause();
		}
		Py_INCREF(m_currentFrame);
	}
	else
	{
		DEBUG("[EF] Reusing model for " + code_id);
		m_model = (*mit).second;
	}

	m_modelKeys.push_back(code_id);

#ifdef WITH_DEBUG
	DEBUG("*******************************************************");
	DEBUG("[EF] Current keys in list: " );
	for(KeyList::iterator dkit = m_modelKeys.begin(); dkit != m_modelKeys.end(); dkit++)
	{
		DEBUG("   - " + *dkit );
	}

	DEBUG("[EF] Current keys in map:");
	for( ModelMap::iterator dmit = m_modelMap.begin(); dmit != m_modelMap.end(); dmit++ )
	{
		DEBUG("   - Key: " + dmit->first);
	}
	DEBUG("*******************************************************");
#endif

	// Create the corresponding trace model
	getTraceModel(code_id);
}

//=============================================================================
// METHOD    : SPELLframeManager::getModel()
//=============================================================================
SPELLexecutionModel& SPELLframeManager::getModel( const std::string& code_id )
{
	ModelMap::iterator mit;
	mit = m_modelMap.find(code_id);
	if (mit != m_modelMap.end())
	{
		return *(mit->second);
	}
	THROW_EXCEPTION("Cannot access model", "No such identifier: " + code_id, SPELL_ERROR_EXECUTION );
}

//=============================================================================
// METHOD    : SPELLframeManager::filterDictUpdated
//=============================================================================
void SPELLframeManager::filterDictUpdated()
{
	DEBUG("[EF] Reading filter definitions");
	PyObject* dict = PyDict_Copy(SPELLpythonHelper::instance().getMainDict());
	m_definitions = PyDict_Keys(dict);
	DEBUG("[EF] " + ISTR(PyList_Size(m_definitions)) + " definitions read");
}

//=============================================================================
// METHOD    : SPELLframeManager::setNewLine
//=============================================================================
const bool SPELLframeManager::setNewLine( const int& new_lineno, const int& new_lasti )
{
	DEBUG("[EF] Set new line " + ISTR(new_lineno) + " on frame " + PSTR(m_currentFrame));
	return SPELLpythonHelper::instance().setNewLine( m_currentFrame, new_lineno, new_lasti );
}

//=============================================================================
// METHOD    : SPELLframeManager::getTraceModel()
//=============================================================================
SPELLexecutionTrace& SPELLframeManager::getTraceModel( const std::string& codeId )
{
	TraceMap::iterator it = m_traceMap.find(codeId);
	m_currentTraceModel = codeId;

//#ifdef WITH_DEBUG
//	DEBUG("[EF] Find trace model for " + codeId );
//	DEBUG("*******************************************************");
//	DEBUG("[EF] Current keys in list: " );
//	for(KeyList::iterator dkit = m_modelKeys.begin(); dkit != m_modelKeys.end(); dkit++)
//	{
//		DEBUG("   - " + *dkit );
//	}
//
//	DEBUG("[EF] Current keys in trace model map:");
//	for( TraceMap::iterator dmit = m_traceMap.begin(); dmit != m_traceMap.end(); dmit++ )
//	{
//		DEBUG("   - Key: " + dmit->first);
//	}
//	DEBUG("*******************************************************");
//#endif

	if ( it == m_traceMap.end() )
	{
		DEBUG("[EF] Create trace model for " + codeId );
		m_traceMap.insert( std::make_pair( std::string(codeId), SPELLexecutionTrace() ) );
		return m_traceMap.find(codeId)->second;
	}
	else
	{
		return it->second;
	}
}

//=============================================================================
// METHOD    : SPELLframeManager::getCurrentTraceModel()
//=============================================================================
SPELLexecutionTrace& SPELLframeManager::getCurrentTraceModel()
{
	return getTraceModel(m_currentTraceModel);
}

//=============================================================================
// METHOD    : SPELLframeManager::resetExecutionTrace()
//=============================================================================
void SPELLframeManager::resetExecutionTrace()
{
	m_currentTraceModel = "";
	m_traceMap.clear();
}

//=============================================================================
// METHOD    : SPELLframeManager::replayStack()
//=============================================================================
void SPELLframeManager::replayStack( SPELLcallstackIF* callstack )
{
	DEBUG("[EF] Re-creating call stack model ");
	KeyList::iterator it;
	std::string file;
	std::string name;
	int lineno;
	callstack->enableNotifications(false);
	for( it = m_modelKeys.begin(); it != m_modelKeys.end(); it++)
	{
		SPELLexecutionModel& model = getModel(*it);
		PyFrameObject* frame = model.getFrame();

		SPELLlnotab lnotab(frame->f_code);
		file = PYSTR(frame->f_code->co_filename);
		lineno = frame->f_lineno;
		name = PYSTR(frame->f_code->co_name);

		DEBUG("[EF] Re-creating call stack model for frame " + name);
		DEBUG("   name     : " + name );
		DEBUG("   call line: " + ISTR(lnotab.getFirstLine()));
		DEBUG("   curr line: " + ISTR(lineno));
		// Sequence to reproduce the stack
		// 1. Event call with the first line of the code
		// 2. Event line with the lineno where the frame remained
		// 3. Next frame
		callstack->callbackEventCall( frame, file, lnotab.getFirstLine(), name );
		callstack->callbackEventLine( frame, file, lineno, name );
	}
	callstack->enableNotifications(true);
	DEBUG("[EF] Re-creating call stack model done");
}
