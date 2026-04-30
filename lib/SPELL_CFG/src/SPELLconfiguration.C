// ################################################################################
// FILE       : SPELLconfiguration.C
// DATE       : Mar 17, 2011
// PROJECT    : SPELL
// DESCRIPTION: Implementation of the configuration reader
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
#include "SPELL_CFG/SPELLconfiguration.H"
#include "SPELL_CFG/SPELLxmlConfigReaderFactory.H"
// Project includes --------------------------------------------------------
#include "SPELL_UTIL/SPELLlog.H"
#include "SPELL_WRP/SPELLconstants.H"
#include "SPELL_UTIL/SPELLpythonHelper.H"
#include "SPELL_WRP/SPELLpyHandle.H"


// GLOBALS /////////////////////////////////////////////////////////////////
// Holds the singleton instance
static SPELLconfiguration* s_instance = 0;

//=============================================================================
// CONSTRUCTOR : SPELLconfiguration::SPELLconfiguration()
//=============================================================================
SPELLconfiguration::SPELLconfiguration()
: m_contexts(),
  m_drivers()
{
    m_reader = SPELLxmlConfigReaderFactory::createXMLConfigReader();
    m_fileName = "";
}

//=============================================================================
// DESTRUCTOR : SPELLconfiguration::~SPELLconfiguration
//=============================================================================
SPELLconfiguration::~SPELLconfiguration()
{
    if (m_contexts.size()>0)
    {
        ContextMap::iterator it;
        ContextMap::iterator end = m_contexts.end();
        for( it = m_contexts.begin(); it != end; it++)
        {
            delete (*it).second;
        }
        m_contexts.clear();
    }
    if (m_drivers.size()>0)
    {
        DriverMap::iterator it;
        DriverMap::iterator end = m_drivers.end();
        for( it = m_drivers.begin(); it != end; it++)
        {
            delete (*it).second;
        }
        m_drivers.clear();
    }
    if (m_reader != NULL)
    {
        delete m_reader;
        m_reader = NULL;
    }
    m_contextConfig.clear();
    m_listenerConfig.clear();
    m_executorsConfig.clear();
}

//=============================================================================
// METHOD    : SPELLconfiguration::instance()
//=============================================================================
SPELLconfiguration& SPELLconfiguration::instance()
{
    if (s_instance == NULL)
    {
        s_instance = new SPELLconfiguration();
    }
    return *s_instance;
}

//=============================================================================
// METHOD: SPELLconfiguration::commonOrDefault
//=============================================================================
unsigned int SPELLconfiguration::commonOrDefault( const std::string& key, unsigned int defaultValue )
{
	std::string value = getCommonParameter(key);
	if (value == "")
	{
		LOG_WARN("Missing configuration parameter '" + key + "', using default " + ISTR(defaultValue));
		return defaultValue;
	}
	return STRI(value);
}


//=============================================================================
// METHOD    : SPELLconfiguration::loadConfig
//=============================================================================
void SPELLconfiguration::loadConfig( std::string fileName )
{
	// Do not initialize twice with the same file
	if ((m_fileName == "") || (m_fileName != fileName))
	{
		LOG_INFO("[CFG] Loading configuration from " + fileName);
		m_reader->parseFile( fileName );
		m_fileName = fileName;
		m_contextConfig.clear();
		m_listenerConfig.clear();
		m_executorsConfig.clear();

		std::string baseName = SPELLutils::basePath( SPELLutils::basePath( fileName ) );

		loadContexts( baseName );

		loadDrivers( baseName );

		// Get server specific configuration for all systems
		loadSpecificConfiguration( XMLTags::TAG_COMMON_SECTION, m_commonConfig );

		// Get server specific configuration for contexts
		loadSpecificConfiguration( XMLTags::TAG_CONTEXT_SECTION, m_contextConfig );

		// Get server specific configuration for executors
		loadSpecificConfiguration( XMLTags::TAG_EXECUTOR_SECTION, m_executorsConfig );

		// Get server specific configuration for listener
		loadSpecificConfiguration( XMLTags::TAG_LISTENER_SECTION, m_listenerConfig );

		// Load family definitions
		loadFamilies();
	}
}

//=============================================================================
// METHOD    : SPELLconfiguration::loadContexts
//=============================================================================
void SPELLconfiguration::loadContexts( const std::string& basePath )
{
    SPELLxmlNodeList nodes = m_reader->findElementsByName( XMLTags::TAG_CONTEXTS_SECTION );
    if (nodes.size()>0)
    {
		std::list<std::string> contextFiles;
		for( SPELLxmlNodeList::iterator it = nodes.begin(); it!=nodes.end(); it++)
		{
			if ((*it)->hasChildren())
			{
				SPELLxmlNodeList children = (*it)->getChildren();
				for( SPELLxmlNodeList::iterator cit = children.begin(); cit!=children.end(); cit++)
				{
					if ( (*cit)->getName() == XMLTags::TAG_CONTEXT )
					{
						std::string contextFile = (*cit)->getValue();
						contextFiles.push_back(contextFile);
						LOG_INFO("[CFG] Found context configuration: " + contextFile);
					}
				}
			}
		}
		for( std::list<std::string>::iterator it = contextFiles.begin(); it != contextFiles.end(); it++)
		{
			loadContextConfiguration( basePath + + PATH_SEPARATOR + Locations::CONTEXT_DIR + PATH_SEPARATOR + (*it) );
		}
		dealloc_list(nodes);
    }
}

//=============================================================================
// METHOD    : SPELLconfiguration::loadDrivers
//=============================================================================
void SPELLconfiguration::loadDrivers( const std::string& basePath )
{
    SPELLxmlNodeList nodes = m_reader->findElementsByName( XMLTags::TAG_DRIVERS_SECTION );
    if (nodes.size()>0)
    {
		std::list<std::string> driverFiles;
		for( SPELLxmlNodeList::iterator it = nodes.begin(); it!=nodes.end(); it++)
		{
			if ((*it)->hasChildren())
			{
				SPELLxmlNodeList children = (*it)->getChildren();
				for( SPELLxmlNodeList::iterator cit = children.begin(); cit!=children.end(); cit++)
				{
					if ( (*cit)->getName() == XMLTags::TAG_DRIVER )
					{
						std::string driverFile = (*cit)->getValue();
						driverFiles.push_back(driverFile);
						LOG_INFO("[CFG] Found driver configuration: " + driverFile);
					}
				}
			}
		}
		for( std::list<std::string>::iterator it = driverFiles.begin(); it != driverFiles.end(); it++)
		{
			loadDriverConfiguration( basePath + + PATH_SEPARATOR + Locations::SPELL_DIR + PATH_SEPARATOR + (*it) );
		}
		dealloc_list(nodes);
    }
}

//=============================================================================
// METHOD    : SPELLconfiguration::loadFamilies
//=============================================================================
void SPELLconfiguration::loadFamilies()
{
    SPELLxmlNodeList nodes = m_reader->findElementsByName( XMLTags::TAG_FAMILIES_SECTION );
    if (nodes.size()>0)
    {
		for( SPELLxmlNodeList::iterator it = nodes.begin(); it!=nodes.end(); it++)
		{
			if ((*it)->hasChildren())
			{
				SPELLxmlNodeList children = (*it)->getChildren();
				for( SPELLxmlNodeList::iterator cit = children.begin(); cit!=children.end(); cit++)
				{
					if ( (*cit)->getName() == XMLTags::TAG_CTX_FAM )
					{
						std::string pname = (*cit)->getAttributeValue(XMLTags::TAG_ATTR_NAME);
						std::string pvalue = (*cit)->getValue();
						std::vector<std::string> famComps = SPELLutils::tokenize(pvalue,",");
						for(std::vector<std::string>::iterator it = famComps.begin(); it != famComps.end(); it++ )
						{
							SPELLutils::trim(*it);
						}
						m_families.insert( std::make_pair(pname,famComps) );
						LOG_INFO("Registered SC family: " + pname + "(" + pvalue + ")");
					}
				}
			}
		}
		dealloc_list(nodes);
    }
}

//=============================================================================
// METHOD    : SPELLconfiguration::getFamilyFor
//=============================================================================
std::string SPELLconfiguration::getFamilyFor( const std::string& sc )
{
	FamilyMap::iterator it;
	std::vector<std::string>::iterator fit;
	for( it = m_families.begin(); it != m_families.end(); it++ )
	{
		if (sc == it->first) return it->first;
		std::vector<std::string> comps = it->second;
		for(fit = comps.begin(); fit != comps.end(); fit++ )
		{
			std::string v1 = sc;
			std::string v2 = *fit;
			if (v1 == v2) return it->first;
		}
	}
	return "";
}

//=============================================================================
// METHOD    : SPELLconfiguration::loadSpecificConfiguration
//=============================================================================
void SPELLconfiguration::loadSpecificConfiguration( std::string section, Properties& properties )
{
    SPELLxmlNodeList sectionNodes = m_reader->findElementsByName( section );
    if (sectionNodes.size()>0)
    {
		SPELLxmlNode* node = *(sectionNodes.begin());
		SPELLxmlNodeList children = node->getChildren();
		for( SPELLxmlNodeList::iterator nit = children.begin(); nit != children.end(); nit++)
		{
			if ((*nit)->getName() == XMLTags::TAG_PROPERTY)
			{
				std::string pname = (*nit)->getAttributeValue(XMLTags::TAG_ATTR_NAME);
				std::string pvalue = (*nit)->getValue();
				properties.insert( std::make_pair( pname, pvalue ));
			}
		}

		dealloc_list(sectionNodes);
		dealloc_list(children);
    }
}

//=============================================================================
// METHOD    : SPELLconfiguration::loadContextConfiguration
//=============================================================================
void SPELLconfiguration::loadContextConfiguration( std::string contextFile )
{
    try
    {
        SPELLcontextConfig* context = new SPELLcontextConfig( contextFile );
        m_contexts[ context->getName() ] = context;
    }
    catch(SPELLcoreException& ex)
    {
        LOG_ERROR("[CFG] Unable to read context configuration from " + contextFile);
        LOG_ERROR("[CFG] Read error: " + ex.what());
    }
}

//=============================================================================
// METHOD    : SPELLconfiguration::loadDriverConfiguration
//=============================================================================
void SPELLconfiguration::loadDriverConfiguration( std::string driverFile )
{
    try
    {
        SPELLdriverConfig* driver = new SPELLdriverConfig( driverFile );
        m_drivers[ driver->getIdentifier() ] = driver;
    }
    catch(SPELLcoreException& ex)
    {
        LOG_ERROR("[CFG] Unable to read driver configuration from " + driverFile);
        LOG_ERROR("[CFG] Read error: " + ex.what());
    }
}

//=============================================================================
// METHOD    : SPELLconfiguration::getContext
//=============================================================================
SPELLcontextConfig& SPELLconfiguration::getContext( std::string ctxName )
{
    if (m_contexts.find(ctxName) == m_contexts.end())
    {
        THROW_EXCEPTION("Cannot find context " + ctxName, "No such context", SPELL_ERROR_CONFIG);
    }
    return *m_contexts[ctxName];
}
//
//=============================================================================
// METHOD    : SPELLconfiguration::getAvailableContexts
//=============================================================================
std::vector<std::string> SPELLconfiguration::getAvailableContexts( )
{
    std::vector<std::string> contexts;
    ContextMap::iterator it;

    for( it = this->m_contexts.begin(); it != this->m_contexts.end() ; it++)
    {
        contexts.push_back((*it).first);
    }

    return contexts;
}


//=============================================================================
// METHOD    : SPELLconfiguration::getDriver
//=============================================================================
SPELLdriverConfig& SPELLconfiguration::getDriver( std::string driverName )
{
    if (m_drivers.find(driverName) == m_drivers.end())
    {
        THROW_EXCEPTION("Cannot find driver " + driverName, "No such driver", SPELL_ERROR_CONFIG);
    }
    return *m_drivers[driverName];
}

//=============================================================================
// METHOD    : SPELLconfiguration::getCommonParameter
//=============================================================================
std::string SPELLconfiguration::getCommonParameter( std::string parameter )
{
    Properties::iterator it = m_commonConfig.find(parameter);
    if ( it != m_commonConfig.end())
    {
        return (*it).second;
    }
    else
    {
        return "";
    }
}

//=============================================================================
// METHOD    : SPELLconfiguration::getListenerParameter
//=============================================================================
std::string SPELLconfiguration::getListenerParameter( std::string parameter )
{
    Properties::iterator it = m_listenerConfig.find(parameter);
    if ( it != m_listenerConfig.end())
    {
        return (*it).second;
    }
    else
    {
        return "";
    }
}

//=============================================================================
// METHOD    : SPELLconfiguration::getContextParameter
//=============================================================================
std::string SPELLconfiguration::getContextParameter( std::string parameter )
{
    Properties::iterator it = m_contextConfig.find(parameter);
    if ( it != m_contextConfig.end())
    {
        return (*it).second;
    }
    else
    {
        return "";
    }
}

//=============================================================================
// METHOD    : SPELLconfiguration::getExecutorParameter
//=============================================================================
std::string SPELLconfiguration::getExecutorParameter( std::string parameter )
{
    Properties::iterator it = m_executorsConfig.find(parameter);
    if ( it != m_executorsConfig.end())
    {
        return (*it).second;
    }
    else
    {
        return "";
    }
}

//=============================================================================
// METHOD    : SPELLconfiguration::loadPythonConfig
//=============================================================================
void SPELLconfiguration::loadPythonConfig( std::string fileName )
{
    DEBUG("[CFG] Importing python config module");
    // Borrowed reference returned
    PyObject* config = SPELLpythonHelper::instance().getObject( "spell.config.reader", "Config" );
    DEBUG("[CFG]    - Getting instance");
    // New reference returned
    SPELLpyHandle instance = SPELLpythonHelper::instance().callMethod( config, "instance", NULL );
    DEBUG("[CFG]    - Loading configuration on python side");
    // New reference returned
    PyObject* filename = SSTRPY(fileName);
    SPELLpythonHelper::instance().callMethod( instance.get(), "load", filename, NULL);
    DEBUG("[CFG]    -Done");
}
