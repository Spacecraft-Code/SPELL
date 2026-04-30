// ################################################################################
// FILE       : SPELLdriverConfig.C
// DATE       : Mar 17, 2011
// PROJECT    : SPELL
// DESCRIPTION: Implementation of the driver configuration model
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
#include "SPELL_CFG/SPELLdriverConfig.H"
#include "SPELL_CFG/SPELLxmlConfigReaderFactory.H"
// Project includes --------------------------------------------------------
#include "SPELL_UTIL/SPELLlog.H"
#include "SPELL_WRP/SPELLconstants.H"
#include "SPELL_UTIL/SPELLpythonHelper.H"
#include "SPELL_UTIL/SPELLutils.H"


// GLOBALS /////////////////////////////////////////////////////////////////



//=============================================================================
// CONSTRUCTOR: SPELLdriverConfig::SPELLdriverConfig()
//=============================================================================
SPELLdriverConfig::SPELLdriverConfig( const std::string& driverFile )
{
    m_reader = SPELLxmlConfigReaderFactory::createXMLConfigReader();
    m_reader->parseFile( driverFile );
    loadBasics();
    loadProperties();
}

//=============================================================================
// DESTRUCTOR:    SPELLdriverConfig::~SPELLdriverConfig
//=============================================================================
SPELLdriverConfig::~SPELLdriverConfig()
{
    m_properties.clear();
    if (m_reader != NULL)
    {
        delete m_reader;
        m_reader = NULL;
    }
}

//=============================================================================
// METHOD    : SPELLdriverConfig::getDriverParameter
//=============================================================================
std::string SPELLdriverConfig::getProperty( const std::string& key ) const
{
    Properties::const_iterator it = m_properties.find(key);
    if (it!= m_properties.end())
    {
        return it->second;
    }
    return "";
}

//=============================================================================
// METHOD    : SPELLdriverConfig::loadBasics
//=============================================================================
void SPELLdriverConfig::loadBasics()
{
    SPELLxmlNode* root = m_reader->getRoot();
    m_drvIdentifier = root->getAttributeValue("id");
    delete root;

    LOG_INFO("[CFG] Load driver configuration for " + m_drvIdentifier);

    SPELLxmlNodeList list = m_reader->findElementsByName( XMLTags::TAG_DRV_NAME );
    if (list.size()>0)
    {
        SPELLxmlNode* node = *(list.begin());
        m_drvName = node->getValue();
    }
    else
    {
        m_drvName = "";
    }
    dealloc_list(list);

    list = m_reader->findElementsByName( XMLTags::TAG_DRV_INTERFACES );
    if (list.size()>0)
    {
		SPELLxmlNode* node = *(list.begin());
		std::string ifcs = node->getValue();
		dealloc_list(list);
		m_interfaces = SPELLutils::tokenize( ifcs, "," );
    }

    list = m_reader->findElementsByName( XMLTags::TAG_DRV_LIB );
    if (list.size()>0)
    {
    	SPELLxmlNode* node = *(list.begin());
		std::string libs = node->getValue();
		dealloc_list(list);
		m_libraries = SPELLutils::tokenize( libs, "," );
    }

    list = m_reader->findElementsByName( XMLTags::TAG_DRV_PATH );
    if (list.size()==0) THROW_EXCEPTION("Cannot create driver configuration", "Unable to read driver installation path", SPELL_ERROR_CONFIG);
    SPELLxmlNode* node = *(list.begin());
    m_path = node->getValue();
    dealloc_list(list);

    list = m_reader->findElementsByName( XMLTags::TAG_DRV_MAXPROC );
    if (list.size()==0)
	{
    	m_maxProcs = 0;
	}
    else
    {
        SPELLxmlNode* node = *(list.begin());
        m_maxProcs = (unsigned int) STRI(node->getValue());
    }
    dealloc_list(list);

    LOG_INFO("     Name      : " + m_drvName);
    LOG_INFO("     Interfaces: " + ISTR(m_interfaces.size()));
    LOG_INFO("     Libraries : " + ISTR(m_libraries.size()));
    LOG_INFO("     Path      : " + m_path);
    LOG_INFO("     Max procs : " + ISTR(m_maxProcs));
}

//=============================================================================
// METHOD    : SPELLdriverConfig::loadProperties
//=============================================================================
void SPELLdriverConfig::loadProperties()
{
    SPELLxmlNodeList list = m_reader->findElementsByName( XMLTags::TAG_DRV_PROPERTIES );
    DEBUG("[CFG] Elements for properties: " + ISTR(list.size()));
    if (list.size()>0)
    {
        LOG_INFO("[CFG] Driver properties: ");
        SPELLxmlNode* propertySection = *(list.begin());
        SPELLxmlNodeList properties = propertySection->getChildren();
        for( SPELLxmlNodeList::iterator it = properties.begin(); it != properties.end(); it++)
        {
            std::string name = (*it)->getAttributeValue( XMLTags::TAG_ATTR_NAME );
            std::string value = (*it)->getValue();
            LOG_INFO("     " + name + "=" + value);
            m_properties.insert( std::make_pair( name, value ));
        }
        dealloc_list(list);
        dealloc_list(properties);
        DEBUG("[CFG] Driver properties read");
    }
}
