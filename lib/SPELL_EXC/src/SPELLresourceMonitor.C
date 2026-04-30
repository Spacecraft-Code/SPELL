// ################################################################################
// FILE       : SPELLresourceMonitor.C
// DATE       : Mar 17, 2011
// PROJECT    : SPELL
// DESCRIPTION: Implementation of the procedure resource monitor
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
#include "SPELL_EXC/SPELLresourceMonitor.H"
#include "SPELL_EXC/SPELLexecutor.H"
// Project includes --------------------------------------------------------
#include "SPELL_UTIL/SPELLlog.H"
#include "SPELL_UTIL/SPELLutils.H"
#include "SPELL_WRP/SPELLconstants.H"
#include "SPELL_UTIL/SPELLpythonMonitors.H"
#include "SPELL_WRP/SPELLpyHandle.H"

// GLOBALS ////////////////////////////////////////////////////////////////////
#define EMPTY_STRING "<empty>"

bool SPELLresourceMonitor::s_enabled = false;

//=============================================================================
// CONSTRUCTOR : SPELLresourceMonitor::SPELLresourceMonitor
//=============================================================================
SPELLresourceMonitor::SPELLresourceMonitor( SPELLresourceChangeListener* listener)
: m_listener(listener)
{
	DEBUG("[RD] Created resource monitor");
	m_initialized = false;
}

//=============================================================================
// DESTRUCTOR : SPELLresourceMonitor::~SPELLresourceMonitor
//=============================================================================
SPELLresourceMonitor::~SPELLresourceMonitor()
{
	delete m_listener;
	// Do not delete! borrowed reference
	m_listener = NULL;
	m_resources.clear();
	DEBUG("[RD] Destroyed");
}

//=============================================================================
// METHOD: SPELLresourceMonitor::retrieveResources()
//=============================================================================
void SPELLresourceMonitor::retrieveResources( ResourceMap &resultingMap, bool onlyLocal )
{

	resultingMap.clear();

	SPELLsafePythonOperations ops("SPELLresourceMonitor::retrieveResources()");
	// Retrieve the resources

	for (long isLocal=0; isLocal<=1; ++isLocal)
	{
		if(isLocal)
		{
			if (!SPELLexecutor::instance().getConfiguration().isLocalResourcesSupported())
			{
				continue;
			}
		}
		else
		{
			if (onlyLocal)
			{
				continue;
			}
		}

		DEBUG("[RD] Get resources from procedure, with 'IsLocal' = " + BSTR(bool(isLocal)));

		PyObject* pyIsLocal = PyBool_FromLong(isLocal);
		PyObject* dict = SPELLpythonHelper::instance().callFunction("spell.utils.resources_utils", "getResourcesUtil", pyIsLocal, NULL);
		/*PyObject* args = PyTuple_New(0);
		PyObject* kwargs = PyDict_New();
		PyDict_SetItemString(kwargs, "IsLocal", pyIsLocal);
		PyObject* dict = SPELLpythonHelper::instance().callSpellFunction("GetResources", args, kwargs);
		Py_XDECREF(args);
		Py_XDECREF(kwargs);*/
		SPELLpythonHelper::instance().checkError();
		Py_XDECREF(pyIsLocal);	

		SPELLpyHandle itemList = PyDict_Keys(dict);
		unsigned int numItems = PyList_Size(itemList.get());
		for( unsigned int index = 0; index<numItems; index++)
		{
			PyObject* key = PyList_GetItem( itemList.get(), index );
			std::string resourceName = PYSSTR(key);
			PyObject* object = PyDict_GetItem( dict, key );

			if (object == NULL) continue;

			Py_XINCREF(object);

			if (shouldDiscard(resourceName)) continue;

			DEBUG("[RD] Processing " + resourceName);

			std::string type = PYSSTR( PyObject_Type(object) );
			std::string value = PYSSTR( object ); //PYCREPR( object );

			DEBUG("[RD] Type      : " + type);
			DEBUG("[RD] Value     : " + value);
			DEBUG("[RD] isLocal   : " + BSTR(bool(isLocal)));

			// Mark empty values (empty strings) as "<empty>"
			//if (value == "") value = EMPTY_STRING;

			if (isLocal)
			{
				// override previously set value. If not set (when onlyLocal is true), other fields are 
				// initialized by the default ResourceInfo initializer (called from analyser)
				resultingMap[resourceName].resourceLocalValue = value;
			}
			else
			{
				std::string gcsName;
				std::map<std::string, std::string>::iterator itNM = m_resourceNameMapping.find(resourceName);
				if (itNM != m_resourceNameMapping.end())
				{
					gcsName = itNM->second;
				}
				else
				{ 
					gcsName = resourceName;
				}

				// get the accepted values list
				std::map<std::string, std::vector<std::pair<std::string, std::string> > >::iterator it = m_resourceAcceptedValues.find(resourceName);
				// first set the local value to the empty, wil be updated in the second iteration
				if (it != m_resourceAcceptedValues.end())
				{
					resultingMap.insert( std::make_pair(resourceName, SPELLresourceInfo(resourceName, type, value, "", it->second, gcsName)) );
				}
				else
				{
					resultingMap.insert( std::make_pair(resourceName, SPELLresourceInfo(resourceName, type, value, "", std::vector<std::pair<std::string, std::string> >(), gcsName)) );
				}
			}

			Py_XDECREF(object);
		}
	}
}

//=============================================================================
// METHOD: SPELLresourceMonitor::retrieveResourceValueRangeAndNameMap()
//=============================================================================
void SPELLresourceMonitor::retrieveResourceValueRangeAndNameMap()
{
	m_resourceAcceptedValues.clear();

	SPELLsafePythonOperations ops("SPELLresourceMonitor::retrieveResourceValueRangeAndNameMap()");

	// Retrieved accepted values lists or resources
    PyObject* dict = SPELLpythonHelper::instance().callFunction("spell.utils.resources_utils","getResourceAcceptedValuesUtil",NULL);
	SPELLpyHandle itemList = PyDict_Keys(dict);
	unsigned int numItems = PyList_Size(itemList.get());
	for( unsigned int index = 0; index<numItems; index++)
	{
		PyObject* key = PyList_GetItem( itemList.get(), index );
		std::string resourceName = PYSSTR(key);
		PyObject* object = PyDict_GetItem( dict, key );

		if (object == NULL) continue;

		Py_XINCREF(object);

		std::vector<std::pair<std::string, std::string> > valueVector;
		for(Py_ssize_t i = 0; i < PyList_Size(object); i++) {
			PyObject *tuple = PyList_GetItem(object, i);
			Py_XINCREF(tuple);
			PyObject *value1 = PyTuple_GetItem(tuple, 0);
			PyObject *value2 = PyTuple_GetItem(tuple, 1);
			valueVector.push_back( std::make_pair( PYSSTR( value1 ), PYSSTR( value2 ) ) ); 
			Py_XDECREF(tuple);
		}
		Py_XDECREF(object);

		m_resourceAcceptedValues.insert( std::make_pair(resourceName, valueVector ));
	}

	m_resourceNameMapping.clear();

    dict = SPELLpythonHelper::instance().callFunction("spell.utils.resources_utils","getResourceNameMapUtil",NULL);
	itemList = PyDict_Keys(dict);
	numItems = PyList_Size(itemList.get());
	for( unsigned int index = 0; index<numItems; index++)
	{
		PyObject* key = PyList_GetItem( itemList.get(), index );
		std::string resourceSPELLName = PYSSTR(key);
		PyObject* value = PyDict_GetItem( dict, key );
		std::string resourceGCSName = PYSSTR(value);
		m_resourceNameMapping.insert( std::make_pair(resourceSPELLName, resourceGCSName) );
	}
}

//=============================================================================
// METHOD: SPELLresourceMonitor::initialize()
//=============================================================================
void SPELLresourceMonitor::initialize()
{
	if (!m_initialized && SPELLresourceMonitor::s_enabled)
	{
		DEBUG("[RD] Initializing resource monitor");

		SPELLsafePythonOperations ops("SPELLresourceMonitor::initialize()");
		retrieveResourceValueRangeAndNameMap();
		retrieveResources(m_resources);
		m_initialized = true;
	}
}

//=============================================================================
// METHOD: SPELLresourceMonitor::getResources()
//=============================================================================
void SPELLresourceMonitor::getResources()
{
	SPELLsafePythonOperations ops("SPELLresourceMonitor::getResources()");

	if (!m_initialized) 
	{
		initialize();
	}
	else
	{
		// get from GCS
		retrieveResources(m_resources);
	}
	
	std::vector<SPELLresourceInfo> allResources;

	SPELLresourceMonitor::ResourceMap::const_iterator it;
	for( it = m_resources.begin(); it != m_resources.end(); it++)
	{
		allResources.push_back(it->second);
	}

	m_listener->resourceList( allResources );
}

//=============================================================================
// METHOD: SPELLresourceMonitor::changeResource()
//=============================================================================
void SPELLresourceMonitor::changeResource( SPELLresourceInfo& resource )
{
	SPELLsafePythonOperations ops("SPELLresourceMonitor::changeResource()");

	DEBUG("[RD] Request changing resource " + resource.resourceName);

	if (!m_initialized) initialize();

	ResourceMap::iterator it = m_resources.find(resource.resourceName);

	for (long isLocal=0; isLocal<=1; ++isLocal)
	{
		std::string *resourceVal;
		if (isLocal)
		{
			resourceVal = &(resource.resourceLocalValue);
			if (it != m_resources.end() && *resourceVal == it->second.resourceLocalValue)
				continue;
		}
		else
		{
			resourceVal = &(resource.resourceGlobalValue);
			if (it != m_resources.end() && *resourceVal == it->second.resourceGlobalValue)
				continue;
		}

		// Evaluate the value for the resource
		PyObject* value = NULL;
		// If resourceValue is empty string, do not change value
		if (*resourceVal == "")
		{
			continue; //value = STRPY("");
		}
		else
		{
			// Build assignment expression. We need to check first, if there
			// are double quotes, convert them to single quotes
			SPELLutils::replace(*resourceVal, "\"", "'");
			DEBUG("[RD] Evaluating value expression: " + *resourceVal );
			// Check value correctness and evaluate it
			value = SSTRPY((*resourceVal)); //SPELLpythonHelper::instance().eval(*resourceVal, false);
		}

		*resourceVal = PYSSTR(value);

		DEBUG("[RD] Setting " + resource.resourceName + " to " + PYREPR(value) + " 'IsLocal' is " + BSTR(bool(isLocal)));

		// create args
		PyObject* pyIsLocal = PyBool_FromLong(isLocal);
		PyObject* pyName = SSTRPY(resource.resourceName);
		PyObject* retVal = SPELLpythonHelper::instance().callFunction("spell.utils.resources_utils", "setResourceUtil", pyName, value, pyIsLocal, NULL);
		/*PyObject* args = PyTuple_New(2);
		PyTuple_SetItem(args, 0, pyName);
		PyTuple_SetItem(args, 1, value);
		PyObject* kwargs = PyDict_New();
		PyDict_SetItemString(kwargs, "IsLocal", pyIsLocal);
		// Call set resource
		PyObject* retVal = SPELLpythonHelper::instance().callSpellFunction("SetResource", args, kwargs);
		// release
		Py_XDECREF(args);
		Py_XDECREF(kwargs);*/
		SPELLpythonHelper::instance().checkError();
		Py_XDECREF(pyIsLocal);	
		Py_XDECREF(pyName);
		Py_XDECREF(value);	

		if ((int) PyInt_AsLong(retVal) != 1)
		{
			DEBUG("[RD] Setting resource " + resource.resourceName + " to " + (*resourceVal) + " failed. 'IsLocal' is " + BSTR(bool(isLocal)));
			// setting resource failed, do nothing
			return;
		}

		DEBUG("[RD] Setting resource " + resource.resourceName + " to " + (*resourceVal) + " success. 'IsLocal' is " + BSTR(bool(isLocal)));
		//if (*resourceVal == "") *resourceVal = EMPTY_STRING;
	}

	// Update the resource in the internal model and notify to listeners
	if (it != m_resources.end() && (resource.resourceGlobalValue != "" || resource.resourceLocalValue != ""))
	{
		std::vector<SPELLresourceInfo> changed;
		if (resource.resourceGlobalValue != "")
		{
			it->second.resourceGlobalValue = resource.resourceGlobalValue;
		}
		if (resource.resourceLocalValue != "")
		{
			it->second.resourceLocalValue = resource.resourceLocalValue;
		}
		LOG_INFO("[RD] resource changed by user: " + resource.resourceName + 
					", current global value: " + it->second.resourceGlobalValue + 
					", current local value " + it->second.resourceLocalValue );
		changed.push_back(it->second);
		m_listener->resourceChanged( changed );
	}
}

//=============================================================================
// METHOD: SPELLresourceMonitor::getResource()
//=============================================================================
void SPELLresourceMonitor::getResource( SPELLresourceInfo& resource )
{
	if (!m_initialized) initialize();
	ResourceMap::iterator it = m_resources.find(resource.resourceName);
	if (it != m_resources.end())
	{
		resource.resourceGlobalValue = it->second.resourceGlobalValue;
		if (resource.resourceGlobalValue == "") resource.resourceGlobalValue = EMPTY_STRING;
		resource.resourceLocalValue = it->second.resourceLocalValue;
		if (resource.resourceLocalValue == "") resource.resourceLocalValue = EMPTY_STRING;
		resource.resourceType = it->second.resourceType;
	}
	else
	{
		resource.resourceGlobalValue = "???";
		resource.resourceLocalValue = "???";
		resource.resourceType = "???";
	}
}

//=============================================================================
// METHOD: SPELLresourceMonitor::shouldDiscard()
//=============================================================================
bool SPELLresourceMonitor::shouldDiscard( const std::string& resourceName )
{
	return false;
}

//=============================================================================
// METHOD: SPELLresourceMonitor::analyze()
//=============================================================================
void SPELLresourceMonitor::analyze()
{
	if (!SPELLresourceMonitor::s_enabled) return;

	if (!m_initialized) initialize();

	SPELLsafePythonOperations ops("SPELLresourceMonitor::analyze()");

	DEBUG("[RD] Analyze changes");

	std::vector<SPELLresourceInfo> changed;

	ResourceMap tempResourceMap;

	// Retrieve local resources
	retrieveResources(tempResourceMap, true);

	ResourceMap::iterator it, itKeep;
	for(it = tempResourceMap.begin(); it != tempResourceMap.end(); ++it)
	{
		std::string resourceName = it->first;
		//std::cout << resourceName << " -> " << shouldDiscard(resourceName,object) << std::endl;
		if (shouldDiscard(resourceName)) continue;

		itKeep = m_resources.find(resourceName);

		// The resource is new
		if (itKeep == m_resources.end())
		{
			LOG_INFO("[RD] Error: There should be no new global resource, but got '" + resourceName + "'");
		}
		// The resource exists, compare values
		else
		{
			bool hasChanged = false;
			if (itKeep->second.resourceLocalValue != it->second.resourceLocalValue)
			{
				LOG_INFO("[RD] Resource local value change: " + resourceName + ", current value: " + it->second.resourceLocalValue + ", previous: " + itKeep->second.resourceLocalValue );
				itKeep->second.resourceLocalValue = it->second.resourceLocalValue;
				hasChanged = true;
			}
			if (hasChanged)
			{
				changed.push_back(itKeep->second);
			}
		}
	}

	if ( changed.size()>0 )
	{
		m_listener->resourceChanged( changed );
	}
}

//=============================================================================
// METHOD: SPELLresourceChangeListener::resourceChanged()
//=============================================================================
void SPELLresourceChangeListener::resourceChanged( const std::vector<SPELLresourceInfo>& changed )
{
	if (SPELLresourceMonitor::s_enabled)
	{
		SPELLexecutor::instance().getCIF().notifyResourceChange( changed );
	}
}

//=============================================================================
// METHOD: SPELLresourceChangeListener::resourceList()
//=============================================================================
void SPELLresourceChangeListener::resourceList( const std::vector<SPELLresourceInfo>& resources )
{
	if (SPELLresourceMonitor::s_enabled)
	{
		SPELLexecutor::instance().getCIF().notifyResourceList( resources );
	}
}
