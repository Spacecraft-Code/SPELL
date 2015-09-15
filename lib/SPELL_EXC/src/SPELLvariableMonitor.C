// ################################################################################
// FILE       : SPELLvariableMonitor.C
// DATE       : Mar 17, 2011
// PROJECT    : SPELL
// DESCRIPTION: Implementation of the procedure variable monitor
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
#include "SPELL_EXC/SPELLvariableMonitor.H"
// Project includes --------------------------------------------------------
#include "SPELL_UTIL/SPELLlog.H"
#include "SPELL_UTIL/SPELLutils.H"
#include "SPELL_WRP/SPELLconstants.H"
#include "SPELL_UTIL/SPELLpythonMonitors.H"
#include "SPELL_WRP/SPELLpyHandle.H"

// GLOBALS ////////////////////////////////////////////////////////////////////
#define EMPTY_STRING "<empty>"

bool SPELLvariableMonitor::s_enabled = false;

//=============================================================================
// CONSTRUCTOR : SPELLvariableMonitor::SPELLvariableMonitor
//=============================================================================
SPELLvariableMonitor::SPELLvariableMonitor( SPELLvariableChangeListener* listener,
		                                    PyFrameObject* frame,
		                                    std::set<std::string>& initialVariables)
: m_frame(frame),
  m_listener(listener),
  m_ignoreVariables(initialVariables)
{
	DEBUG("[VM] Created variable monitor for code " + PYSTR(m_frame->f_code->co_name));
	DEBUG("[VM] Ignored: " + ISTR(m_ignoreVariables.size()));
	m_initialized = false;
	m_scopeName = PSTR(m_frame);
}

//=============================================================================
// DESTRUCTOR : SPELLvariableMonitor::~SPELLvariableMonitor
//=============================================================================
SPELLvariableMonitor::~SPELLvariableMonitor()
{
	// Do not delete! borrowed reference
	m_frame = NULL;
	// Do not delete! borrowed reference
	m_listener = NULL;
	m_localVariables.clear();
	m_globalVariables.clear();
	DEBUG("[VM] Destroyed");
}

//=============================================================================
// METHOD: SPELLvariableMonitor::retrieveGlobalVariables()
//=============================================================================
void SPELLvariableMonitor::retrieveGlobalVariables()
{
	DEBUG("[VM] Retrieve Globals");

	m_globalVariables.clear();

	/*
	 * Once we get the bottom stack frame, we have to iterate over all the keys
	 * in the globals dictionary, and filter them agains the m_initialVariables
	 */
	PyObject* dict = m_frame->f_globals;
	SPELLpyHandle itemList = PyDict_Keys(dict);
	unsigned int numItems = PyList_Size(itemList.get());
	for( unsigned int index = 0; index<numItems; index++)
	{
		PyObject* key = PyList_GetItem( itemList.get(), index );
		std::string varName = PYSSTR(key);
		PyObject* object = PyDict_GetItem( dict, key );

		if (shouldDiscard(varName,object)) continue;

		DEBUG("[VM] Processing " + varName);

		std::string type = PYSSTR( PyObject_Type(object) );
		std::string value = PYREPR( object );

		DEBUG("[VM] Type      : " + type);
		DEBUG("[VM] Value     : " + value);
		DEBUG("[VM] Global    : " + BSTR(true));

		// Mark empty values (empty strings) as "<empty>"
		if (value == "") value = EMPTY_STRING;

		m_globalVariables.insert( std::make_pair(varName, SPELLvarInfo(varName, type, value, true)) );
	}
}

//=============================================================================
// METHOD: SPELLvariableMonitor::retrieveLocalVariables()
//=============================================================================
void SPELLvariableMonitor::retrieveLocalVariables()
{
	DEBUG("[VM] Retrieve Locals");
	DEBUG("[VM] Frame: " + PYCREPR(m_frame));

	/*
	 * Bottom stack frame is discarded,
	 * as globals and locals are the same dictionary
	 */
	if (m_frame->f_back == NULL) return;

	m_localVariables.clear();

	/*
	 * Get the names defined in the current code, including arguments
	 */
	std::vector<std::string> varNames = retrieveNames();

	/*
	 * Iterate over the locals dictionary, retrieving the names contained in
	 * varNames
	 */
	PyFrame_FastToLocals(m_frame);
	PyObject* dict = m_frame->f_locals;
	for( unsigned int index = 0; index< varNames.size(); index++)
	{
		std::string varName = varNames[index];
		SPELLpyHandle pyVarName = SSTRPY(varName);
		if (PyDict_Contains( dict, pyVarName.get() ))
		{
			PyObject* object = PyDict_GetItem( dict, pyVarName.get() );

			if (shouldDiscard(varName,object)) continue;

			DEBUG("[VM] Processing " + varName);
			std::string type = PYSSTR( PyObject_Type(object) );
			DEBUG("[VM] Type      : " + type);
			std::string value = PYREPR( object );
			DEBUG("[VM] Value     : " + value);
			DEBUG("[VM] Global    : " + BSTR(false));

			// Mark empty values (empty strings) as "<empty>"
			if (value == "") value = EMPTY_STRING;

			m_localVariables.insert( std::make_pair( varName, SPELLvarInfo(varName, type, value, false)) );
		}
	}
	PyFrame_LocalsToFast(m_frame,0);
}

//=============================================================================
// METHOD: SPELLvariableMonitor::retrieveNames()
//=============================================================================
std::vector<std::string> SPELLvariableMonitor::retrieveNames()
{
	std::vector<std::string> varNames;

	PyObject* varList = m_frame->f_code->co_names;
	DEBUG("[VM] CO_NAMES   : " + PYREPR(varList));
	unsigned int numVars = PyTuple_Size(varList);

	/*
	 * co_varnames contains the names of the local variables
	 * (starting with the argument names)
	 */
	varList = m_frame->f_code->co_varnames;
	DEBUG("[VM] CO_VARNAMES: " + PYREPR(varList));
	numVars = PyTuple_Size(varList);
	for( unsigned int index = 0; index<numVars; index++)
	{
		PyObject* varName = PyTuple_GetItem( varList, index );
		varNames.push_back( PYSSTR( varName ) );
	}

	varList = m_frame->f_code->co_freevars;
	DEBUG("[VM] CO_FREEVARS : " + PYREPR(varList));

	varList = m_frame->f_code->co_cellvars;
	DEBUG("[VM] CO_CELLVARS : " + PYREPR(varList));

	return varNames;
}

//=============================================================================
// METHOD: SPELLvariableMonitor::initialize()
//=============================================================================
void SPELLvariableMonitor::initialize()
{
	if (!m_initialized && SPELLvariableMonitor::s_enabled)
	{
		DEBUG("[VM] Initializing variable monitor");

		SPELLsafePythonOperations ops("SPELLvariableMonitor::initialize()");
		retrieveLocalVariables();
		retrieveGlobalVariables();
		m_initialized = true;
	}
}

//=============================================================================
// METHOD: SPELLvariableMonitor::getGlobalVariables()
//=============================================================================
const SPELLvariableMonitor::VarMap& SPELLvariableMonitor::getGlobalVariables()
{
	if (!m_initialized) initialize();
	return m_globalVariables;
}

//=============================================================================
// METHOD: SPELLvariableMonitor::getLocalVariables()
//=============================================================================
const SPELLvariableMonitor::VarMap& SPELLvariableMonitor::getLocalVariables()
{
	if (!m_initialized) initialize();
	return m_localVariables;
}

//=============================================================================
// METHOD: SPELLvariableMonitor::changeVariable()
//=============================================================================
void SPELLvariableMonitor::changeVariable( SPELLvarInfo& var )
{
	SPELLsafePythonOperations ops("SPELLvariableMonitor::changeVariable()");

	DEBUG("[VM] Request changing variable " + var.varName);

	if (!m_initialized) initialize();

	// Evaluate the value for the variable
	PyObject* value = NULL;
	// If varValue is '<empty>' or empty string, do not try to evaluate it,
	// directly assign Python empty string
	if ((var.varValue == EMPTY_STRING)||(var.varValue == ""))
	{
		value = STRPY("");
	}
	else
	{
		// Build assignment expression. We need to check first, if there
		// are double quotes, convert them to single quotes
		SPELLutils::replace(var.varValue, "\"", "'");
		DEBUG("[VM] Evaluating value expression: " + var.varValue );
		// Check value correctness and evaluate it
		value = SPELLpythonHelper::instance().eval(var.varValue, false);
	}

	if ((m_frame->f_globals)&&(var.isGlobal))
	{
		DEBUG("[VM] Setting " + var.varName + " to " + PYREPR(value) + " in globals");
		PyDict_SetItemString( m_frame->f_globals, var.varName.c_str(), value );
	}
	else if ((m_frame->f_locals)&&(!var.isGlobal))
	{
		DEBUG("[VM] Setting " + var.varName + " to " + PYREPR(value) + " in locals");
		// Update locals from fast locals first
		PyFrame_FastToLocals(m_frame);
		PyDict_SetItemString( m_frame->f_locals, var.varName.c_str(), value );
		PyFrame_LocalsToFast(m_frame,0);
	}
	var.varValue = PYSSTR(value);
	if (var.varValue == "") var.varValue = EMPTY_STRING;

	// Update the variable in the internal model and notify to listeners
	if (var.isGlobal)
	{
		VarMap::iterator it = m_globalVariables.find(var.varName);
		if (it != m_globalVariables.end())
		{
			std::vector<SPELLvarInfo> added, changed, deleted;
			it->second.varValue = var.varValue;
			LOG_INFO("[VM] Global variable changed by user: " + var.varName + ", current value: " + var.varValue );
			changed.push_back(it->second);
			m_listener->variableChanged( added, changed, deleted );
		}
	}
	else
	{
		VarMap::iterator it = m_localVariables.find(var.varName);
		if (it != m_localVariables.end())
		{
			std::vector<SPELLvarInfo> added, changed, deleted;
			it->second.varValue = var.varValue;
			LOG_INFO("[VM] Local variable changed by user: " + var.varName + ", current value: " + var.varValue );
			changed.push_back(it->second);
			m_listener->variableChanged( added, changed, deleted );
		}
	}
}

//=============================================================================
// METHOD: SPELLvariableMonitor::getVariableRef
//=============================================================================
PyObject* SPELLvariableMonitor::getVariableRef( const std::string& name )
{
	SPELLpyHandle pyName = SSTRPY(name);
	if (PyDict_Contains(m_frame->f_globals, pyName.get()))
	{
		return PyDict_GetItemString(m_frame->f_globals, name.c_str());
	}
	return NULL;
}

//=============================================================================
// METHOD: SPELLvariableMonitor::getVariable()
//=============================================================================
void SPELLvariableMonitor::getVariable( SPELLvarInfo& var )
{
	if (!m_initialized) initialize();
	VarMap::iterator it = m_globalVariables.find(var.varName);
	VarMap::iterator lit = m_localVariables.find(var.varName);
	if (it != m_globalVariables.end())
	{
		var.varValue = it->second.varValue;
		if (var.varValue == "") var.varValue = EMPTY_STRING;
		var.varType = it->second.varType;
		var.isGlobal = true;
	}
	else if (lit != m_localVariables.end())
	{
		var.varValue = lit->second.varValue;
		if (var.varValue == "") var.varValue = EMPTY_STRING;
		var.varType = lit->second.varType;
		var.isGlobal = false;
	}
	else
	{
		var.varValue = "???";
		var.varType = "???";
		var.isGlobal = false;
	}
}

//=============================================================================
// METHOD: SPELLvariableMonitor::shouldDiscard()
//=============================================================================
bool SPELLvariableMonitor::shouldDiscard( const std::string& varName, PyObject* value )
{
	// Do the following check just when the considered variables are not internal databases
	if ( (varName == DatabaseConstants::SCDB) ||
		 (varName == DatabaseConstants::GDB)  ||
		 (varName == DatabaseConstants::PROC) ||
		 (varName == DatabaseConstants::ARGS) ||
		 (varName == DatabaseConstants::IVARS))
	{
		return true;
	}

	/* If they key is contained in the initial variables set, then ignore it */
	if (m_ignoreVariables.find(varName) != m_ignoreVariables.end())
	{
		return true;
	}

	// Ignore internal flags
	if (varName == "__USERLIB__") return true;

	// Ignore callables and complex objects
	if (SPELLpythonHelper::instance().isInstance(value, "Database", "spell.lib.adapter.databases.database")) return true;
	if (PyCallable_Check(value)) return true;
	if (PyFunction_Check(value)) return true;
	if (PyClass_Check(value)) return true;
	if (PyModule_Check(value)) return true;
	if (PyInstance_Check(value)) return true;

	return false;
}

//=============================================================================
// METHOD: SPELLvariableMonitor::analyze()
//=============================================================================
void SPELLvariableMonitor::analyze()
{
	if (!SPELLvariableMonitor::s_enabled) return;

	if (!m_initialized) initialize();

	SPELLsafePythonOperations ops("SPELLvariableMonitor::analyze()");

	DEBUG("[VM] Analyze changes in " + PYCREPR(m_frame));

	std::vector<SPELLvarInfo> added;
	std::vector<SPELLvarInfo> changed;
	std::vector<SPELLvarInfo> deleted;

	PyObject* dict = m_frame->f_globals;
	SPELLpyHandle globalsItemList = PyDict_Keys(dict);
	unsigned int numItems = PyList_Size(globalsItemList.get());
	VarMap::iterator it;
	for( unsigned int index = 0; index<numItems; index++)
	{
		PyObject* key = PyList_GetItem( globalsItemList.get(), index );
		PyObject* object = PyDict_GetItem( dict, key );
		std::string varName = PYSTR(key);

		if (shouldDiscard(varName,object)) continue;

		it = m_globalVariables.find(varName);
		std::string value = PYREPR( object );

		// The variable is new
		if (it == m_globalVariables.end())
		{
			LOG_INFO("Add new global variable '" + varName + "'");
			std::string type = PYSSTR( PyObject_Type(object) );
			m_globalVariables.insert( std::make_pair( varName, SPELLvarInfo(varName, type, value, true)) );
			it = m_globalVariables.find(varName);
			added.push_back( it->second );
		}
		// The variable exists, compare values
		else
		{
			if (it->second.varValue != value)
			{
				LOG_INFO("[VM] Global variable change: " + varName + ", current value: " + value + ", previous: " + it->second.varValue );
				it->second.varValue = value;
				changed.push_back(it->second);
			}
		}
	}

	// We need to retrieve the function arguments and other locals, which are only stored in
	// fast locals by default
	PyFrame_FastToLocals(m_frame);
	dict = m_frame->f_locals;
	SPELLpyHandle localsItemList = PyDict_Keys(dict);
	numItems = PyList_Size(localsItemList.get());

	for( unsigned int index = 0; index<numItems; index++)
	{
		PyObject* key = PyList_GetItem( localsItemList.get(), index );
		if (PyDict_Contains(m_frame->f_globals,key)) continue;
		PyObject* object = PyDict_GetItem( dict, key );
		std::string varName = PYSTR(key);

		if (shouldDiscard(varName,object)) continue;

		it = m_localVariables.find(varName);
		std::string value = PYREPR( object );

		// The variable is new
		if (it == m_localVariables.end() )
		{
			LOG_INFO("Add new local variable '" + varName + "'");
			std::string type = PYSSTR( PyObject_Type(object) );
			m_localVariables.insert( std::make_pair( varName, SPELLvarInfo(varName, type, value, false)) );
			it = m_localVariables.find(varName);
			added.push_back( it->second );
		}
		// The variable exists, compare values
		else
		{
			if (it->second.varValue != value)
			{
				LOG_INFO("[VM] Local variable change: " + varName + ", current value: " + value + ", previous: " + it->second.varValue );
				it->second.varValue = value;
				changed.push_back(it->second);
			}
		}
	}

	// Finally, check if variables have been deleted
	std::vector<std::string> globalsToClean;
	std::vector<std::string> localsToClean;

	for( it = m_globalVariables.begin(); it != m_globalVariables.end(); it++ )
	{
		SPELLpyHandle gkey = SSTRPY(it->first);
		if (!PyDict_Contains( m_frame->f_globals, gkey.get() ))
		{
			deleted.push_back(it->second);
			globalsToClean.push_back(it->first);
			LOG_INFO("[VM] Global variable removed: " + it->first );
		}
	}
	for( it = m_localVariables.begin(); it != m_localVariables.end(); it++ )
	{
		SPELLpyHandle lkey = SSTRPY(it->first);
		if (!PyDict_Contains( m_frame->f_locals, lkey.get() ))
		{
			deleted.push_back(it->second);
			localsToClean.push_back(it->first);
			LOG_INFO("[VM] Local variable removed: " + it->first );
		}
	}
	for( std::vector<std::string>::const_iterator sit = globalsToClean.begin(); sit != globalsToClean.end(); sit++)
	{
		it = m_globalVariables.find(*sit);
		m_globalVariables.erase(it);
	}
	for( std::vector<std::string>::const_iterator sit = localsToClean.begin(); sit != localsToClean.end(); sit++)
	{
		it = m_localVariables.find(*sit);
		m_localVariables.erase(it);
	}

	PyFrame_LocalsToFast(m_frame,0);


	if (added.size()>0 || changed.size()>0 || deleted.size()>0 )
	{
		m_listener->variableChanged( added, changed, deleted );
	}
}
