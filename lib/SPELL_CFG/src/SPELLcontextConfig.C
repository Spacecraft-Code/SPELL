// ################################################################################
// FILE       : SPELLcontextConfig.C
// DATE       : Mar 17, 2011
// PROJECT    : SPELL
// DESCRIPTION: Implementation of the context configuration model
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
#include "SPELL_CFG/SPELLcontextConfig.H"
#include "SPELL_CFG/SPELLxmlConfigReaderFactory.H"
// Project includes --------------------------------------------------------
#include "SPELL_UTIL/SPELLlog.H"
#include "SPELL_WRP/SPELLconstants.H"
#include "SPELL_UTIL/SPELLpythonHelper.H"


// GLOBALS /////////////////////////////////////////////////////////////////
const std::string SPELLcontextConfig::ExecutorProgram = "ExecutorProgram";
const std::string SPELLcontextConfig::ExecutorStartTimeout = "ExecutorStartTimeout";
const std::string SPELLcontextConfig::ExecutorLoginTimeout = "ExecutorLoginTimeout";
const std::string SPELLcontextConfig::UseDriverTime = "UseDriverTime";



//=============================================================================
// CONSTRUCTOR: SPELLcontextConfig::SPELLcontextConfig()
//=============================================================================
SPELLcontextConfig::SPELLcontextConfig( const std::string& contextFile )
{
    m_reader = SPELLxmlConfigReaderFactory::createXMLConfigReader();
    m_reader->parseFile( contextFile );
    m_ctxFileName = contextFile;
    m_ctxDesc = "unknown";
    m_driver = "unknown";
    m_spacecraft = "unknown";
    m_gcs = "unknown";
    m_family = "unknown";
    m_procPath = "unknown";
    m_libPath = "unknown";
    m_outputDir = "";
    m_inputDir = "";

    loadBasics();

    loadExecutorParameters();

    loadDriverParameters();

    loadLocations();
}

//=============================================================================
// DESTRUCTOR:    SPELLcontextConfig::~SPELLcontextConfig
//=============================================================================
SPELLcontextConfig::~SPELLcontextConfig()
{
    m_executorConfig.clear();
    m_driverConfig.clear();
    m_locationPaths.clear();
    m_locationExts.clear();
    m_builtinDatabases.clear();
    if (m_reader != NULL)
    {
        delete m_reader;
        m_reader = NULL;
    }
}

//=============================================================================
// METHOD    : SPELLcontextConfig::getDriverParameter
//=============================================================================
std::string SPELLcontextConfig::getDriverParameter( const std::string& key ) const
{
    Properties::const_iterator it = m_driverConfig.find(key);
    if (it!= m_driverConfig.end())
    {
        return it->second;
    }
    return "";
}

//=============================================================================
// METHOD    : SPELLcontextConfig::getExecutorParameter
//=============================================================================
std::string SPELLcontextConfig::getExecutorParameter( const std::string& key ) const
{
    Properties::const_iterator it = m_executorConfig.find(key);
    if ( it != m_executorConfig.end())
    {
        return it->second;
    }
    return "";
}

//=============================================================================
// METHOD    : SPELLcontextConfig::loadBasics
//=============================================================================
void SPELLcontextConfig::loadBasics()
{
    SPELLxmlNode* root = m_reader->getRoot();
    m_ctxName = root->getAttributeValue("id");
    delete root;

    LOG_INFO("[CFG] Read context configuration for " + m_ctxName);

    SPELLxmlNodeList list = m_reader->findElementsByName( XMLTags::TAG_CTX_DESC );
    if (list.size()>0)
    {
        SPELLxmlNode* node = *(list.begin());
        m_ctxDesc = node->getValue();
    }
    else
    {
        m_ctxDesc = "";
    }
    dealloc_list(list);

    list = m_reader->findElementsByName( XMLTags::TAG_CTX_DRIVER );
    if (list.size()==0) THROW_EXCEPTION("Cannot create context configuration", "Unable to read context driver", SPELL_ERROR_CONFIG);
    SPELLxmlNode* node = *(list.begin());
    m_driver = node->getValue();
    dealloc_list(list);

    list = m_reader->findElementsByName( XMLTags::TAG_CTX_SC );
    if (list.size()==0) THROW_EXCEPTION("Cannot create context configuration", "Unable to read context S/C", SPELL_ERROR_CONFIG);
    node = *(list.begin());
    m_spacecraft = node->getValue();
    dealloc_list(list);

    list = m_reader->findElementsByName( XMLTags::TAG_CTX_GCS );
    if (list.size()==0) THROW_EXCEPTION("Cannot create context configuration", "Unable to read context GCS", SPELL_ERROR_CONFIG);
    node = *(list.begin());
    m_gcs = node->getValue();
    dealloc_list(list);

    list = m_reader->findElementsByName( XMLTags::TAG_CTX_FAM );
    if (list.size()==0) THROW_EXCEPTION("Cannot create context configuration", "Unable to read context family", SPELL_ERROR_CONFIG);
    node = *(list.begin());
    m_family = node->getValue();
    dealloc_list(list);

    list = m_reader->findElementsByName( XMLTags::TAG_CTX_PPATH );
    if (list.size()==0) THROW_EXCEPTION("Cannot create context configuration", "Unable to read context procedure path", SPELL_ERROR_CONFIG);
    node = *(list.begin());
    m_procPath = node->getValue();
    dealloc_list(list);

    list = m_reader->findElementsByName( XMLTags::TAG_CTX_LPATH );
    if (list.size()==1)
    {
        node = *(list.begin());
        m_libPath = node->getValue();
        dealloc_list(list);
    }
    else
    {
        m_libPath = "";
    }

    list = m_reader->findElementsByName( XMLTags::TAG_CTX_OPATH );
    if (list.size()==1)
    {
        node = *(list.begin());
        m_outputDir = node->getValue();
        dealloc_list(list);
    }
    else
    {
    	LOG_WARN("Output directory not defined in context configuration");
        m_outputDir = SPELLutils::getSPELL_DATA();
    }

    list = m_reader->findElementsByName( XMLTags::TAG_CTX_IPATH );
    if (list.size()==1)
    {
        node = *(list.begin());
        m_inputDir = node->getValue();
        dealloc_list(list);
    }
    else
    {
    	LOG_WARN("Input directory not defined in context configuration");
        m_inputDir = SPELLutils::getSPELL_DATA();
    }

    LOG_INFO("     Driver : " + m_driver);
    LOG_INFO("     S/C    : " + m_spacecraft);
    LOG_INFO("     GCS    : " + m_gcs);
    LOG_INFO("     Family : " + m_family);
    LOG_INFO("     P.Path : " + m_procPath);
    LOG_INFO("     L.Path : " + m_libPath);
    LOG_INFO("     O.Path : " + m_outputDir);
    LOG_INFO("     I.Path : " + m_inputDir);
}

//=============================================================================
// METHOD    : SPELLcontextConfig::loadExecutorParameters
//=============================================================================
void SPELLcontextConfig::loadExecutorParameters()
{
    SPELLxmlNodeList list = m_reader->findElementsByName( XMLTags::TAG_CTX_ECONFIG );
    if (list.size()>0)
    {
        LOG_INFO("[CFG] Executor config: ");
        SPELLxmlNode* executor = *(list.begin());
        SPELLxmlNodeList exProperties = executor->getChildren();
        for( SPELLxmlNodeList::iterator it = exProperties.begin(); it != exProperties.end(); it++)
        {
            std::string name = (*it)->getAttributeValue( XMLTags::TAG_ATTR_NAME );
            std::string value = (*it)->getValue();
            LOG_INFO("     " + name + "=" + value);
            m_executorConfig.insert( std::make_pair( name, value ));
        }
        dealloc_list(list);
        dealloc_list(exProperties);
    }
}

//=============================================================================
// METHOD    : SPELLcontextConfig::loadDriverParameters
//=============================================================================
void SPELLcontextConfig::loadDriverParameters()
{
    SPELLxmlNodeList list = m_reader->findElementsByName( XMLTags::TAG_CTX_DCONFIG );
    if (list.size()>0)
    {
        LOG_INFO("[CFG] Driver config: ");
        SPELLxmlNode* dconfig = *(list.begin());
        SPELLxmlNodeList dcProperties = dconfig->getChildren();
        for( SPELLxmlNodeList::iterator it = dcProperties.begin(); it != dcProperties.end(); it++)
        {
            std::string name = (*it)->getAttributeValue( XMLTags::TAG_ATTR_NAME );
            std::string value = (*it)->getValue();
            LOG_INFO("     " + name + "=" + value);
            m_driverConfig.insert( std::make_pair( name, value ));
        }
        dealloc_list(list);
        dealloc_list(dcProperties);
    }
}

//=============================================================================
// METHOD    : SPELLcontextConfig::loadLocations
//=============================================================================
void SPELLcontextConfig::loadLocations()
{
    SPELLxmlNodeList sectionNodes = m_reader->findElementsByName( XMLTags::TAG_DATABASES_SECTION );
    SPELLxmlNode* node = *(sectionNodes.begin());
    SPELLxmlNodeList children = node->getChildren();
    for( SPELLxmlNodeList::iterator nit = children.begin(); nit != children.end(); nit++)
    {
        if ((*nit)->getName() == XMLTags::TAG_LOCATION)
        {
            std::string lname = (*nit)->getAttributeValue(XMLTags::TAG_ATTR_NAME);
            std::string ltype = (*nit)->getAttributeValue(XMLTags::TAG_ATTR_TYPE);
            std::string lpath = (*nit)->getValue();
            std::string lext  = (*nit)->getAttributeValue( XMLTags::TAG_ATTR_EXT );
            m_locationPaths.insert( std::make_pair( lname, lpath ));
            m_locationExts.insert( std::make_pair( lname, lext ));
            m_locationTypes.insert( std::make_pair( lname, ltype ));
        }
        else if ((*nit)->getName() == XMLTags::TAG_DATABASE)
        {
            std::string dname = (*nit)->getAttributeValue(XMLTags::TAG_ATTR_NAME);
            std::string dloc  = (*nit)->getAttributeValue(XMLTags::TAG_ATTR_LOC);
            m_builtinDatabases.insert( std::make_pair( dname, dloc ));
        }
    }
    dealloc_list(sectionNodes);
    dealloc_list(children);
}

//=============================================================================
// METHOD    : SPELLcontextConfig::getLocationPath
//=============================================================================
std::string SPELLcontextConfig::getLocationPath( const std::string& locationName ) const
{
    Properties::const_iterator it = m_locationPaths.find(locationName);
    if (it == m_locationPaths.end())
    {
        THROW_EXCEPTION("Unable to get path for location name " + locationName, "No such name", SPELL_ERROR_CONFIG);
    }
    return (*it).second;
}

//=============================================================================
// METHOD    : SPELLcontextConfig::getLocationType
//=============================================================================
std::string SPELLcontextConfig::getLocationType( const std::string& locationName ) const
{
    Properties::const_iterator it = m_locationTypes.find(locationName);
    if (it == m_locationPaths.end())
    {
        THROW_EXCEPTION("Unable to get path for location name " + locationName, "No such name", SPELL_ERROR_CONFIG);
    }
    return (*it).second;
}

//=============================================================================
// METHOD    : SPELLcontextConfig::getLocations
//=============================================================================
std::list<std::string> SPELLcontextConfig::getLocations() const
{
	std::list<std::string> list;
    Properties::const_iterator it;
    for(it = m_locationPaths.begin(); it != m_locationPaths.end(); it++)
    {
        list.push_back(it->first);
    }
    return list;
}

//=============================================================================
// METHOD    : SPELLcontextConfig::getLocationExtension
//=============================================================================
std::string SPELLcontextConfig::getLocationExtension( const std::string& locationName ) const
{
    Properties::const_iterator it = m_locationExts.find(locationName);
    if (it == m_locationExts.end())
    {
        THROW_EXCEPTION("Unable to get extension for location name " + locationName, "No such name", SPELL_ERROR_CONFIG);
    }
    return (*it).second;
}

//=============================================================================
// METHOD    : SPELLcontextConfig::getBuiltinDatabases
//=============================================================================
std::list<std::string> SPELLcontextConfig::getBuiltinDatabases() const
{
    std::list<std::string> dbList;
    Properties::const_iterator it;
    for( it = m_builtinDatabases.begin(); it != m_builtinDatabases.end(); it++)
    {
        dbList.push_back( it->first );
    }
    return dbList;
}

//=============================================================================
// METHOD    : SPELLcontextConfig::getDatabaseLocation
//=============================================================================
std::string SPELLcontextConfig::getDatabaseLocation( const std::string& database ) const
{
    Properties::const_iterator it = m_builtinDatabases.find(database);
    if (it != m_builtinDatabases.end() )
    {
        return it->second;
    }
    return "";
}

