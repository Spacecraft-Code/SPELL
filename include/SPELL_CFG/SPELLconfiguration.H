// ################################################################################
// FILE       : SPELLconfiguration.H
// DATE       : Mar 17, 2011
// PROJECT    : SPELL
// DESCRIPTION: Wrapper to configuration reader instance
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

#ifndef __SPELL_CONFIGURATION_H__
#define __SPELL_CONFIGURATION_H__

// FILES TO INCLUDE ////////////////////////////////////////////////////////
// System includes ---------------------------------------------------------
// Project includes --------------------------------------------------------
#include "SPELL_UTIL/SPELLbase.H"
// Local includes ----------------------------------------------------------
#include "SPELL_CFG/SPELLcontextConfig.H"
#include "SPELL_CFG/SPELLdriverConfig.H"

/** \defgroup SPELL_CFG Configuration (SPELL_CFG)
 *
 * This module provides facilities for loading and processing the main SPELL
 * configuration files for the server side processes. The \c SPELLconfiguration
 * singleton is the access point for getting all the configuration values.
 *
 * The current implementation is based on Xerces library (xerces.apache.org)
 *
 * */
/*@{*/


// FORWARD REFERENCES //////////////////////////////////////////////////////
// TYPES ///////////////////////////////////////////////////////////////////
// DEFINES /////////////////////////////////////////////////////////////////

//////////////////////////////////////////////////////////////////////////////
/**
 * Configuration reader class.
 *
 * \par Description and usage:
 *
 * 		This singleton provides all the configuration data needed for the SPELL
 * 		server processes. It loads the configuration parameters from an XML
 * 		configuration file by using the methods loadConfig() and loadPythonConfig().
 * 		Once the configuration is loaded, the configuration values are
 * 		accessed via the \c getXXXparameter() methods or via the context
 * 		configuration instances, which can be obtained with the \c getContext()
 * 		method.
 */
//////////////////////////////////////////////////////////////////////////////
class SPELLconfiguration
{
public: //--------------------------------------------------------------------

    // EXCEPTIONS ////////////////////////////////////////////////////////////
    // ENUMS /////////////////////////////////////////////////////////////////
    // TYPES /////////////////////////////////////////////////////////////////
    // LIFECYCLE /////////////////////////////////////////////////////////////

		//////////////////////////////////////////////////////////////////////
		/** Destructor.
		**////////////////////////////////////////////////////////////////////
		~SPELLconfiguration();

    // STATIC ////////////////////////////////////////////////////////////////

		//////////////////////////////////////////////////////////////////////
		/** Obtain the singleton instance.
		 *
		 * \return The singleton instance.
		**////////////////////////////////////////////////////////////////////
		static SPELLconfiguration& instance();

    // METHODS ///////////////////////////////////////////////////////////////

		//////////////////////////////////////////////////////////////////////
		/** Load configuration
		 *
		 * \param fileName IN: the main XML configuration file name (path)
		**////////////////////////////////////////////////////////////////////
		void loadConfig( std::string fileName );

		//////////////////////////////////////////////////////////////////////
		/** Load python bindings.
		 *
		 * \param fileName IN: path to the SPELL language configuration file.
		**////////////////////////////////////////////////////////////////////
		void loadPythonConfig( std::string fileName );

		//////////////////////////////////////////////////////////////////////
		/** Obtain common configuration parameters (server-specific).
		 *
		 * \param parameter IN: configuration parameter name
		 *
		 * \return Configuration parameter value
		**////////////////////////////////////////////////////////////////////
		std::string getCommonParameter( std::string parameter );

		//////////////////////////////////////////////////////////////////////
		/** Obtain listener configuration parameters (server-specific).
		 *
		 * \param parameter IN: configuration parameter name
		 *
		 * \return Configuration parameter value
		**////////////////////////////////////////////////////////////////////
		std::string getListenerParameter( std::string parameter );

		//////////////////////////////////////////////////////////////////////
		/** Obtain context configuration parameters (server-specific)
		 *
		 * \param parameter IN: configuration parameter name
		 *
		 * \return Configuration parameter value
		**////////////////////////////////////////////////////////////////////
		std::string getContextParameter( std::string parameter );

		//////////////////////////////////////////////////////////////////////
		/** Obtain executor configuration parameters (server-specific)
		 *
		 * \param parameter IN: configuration parameter name
		 *
		 * \return Configuration parameter value
		**////////////////////////////////////////////////////////////////////
		std::string getExecutorParameter( std::string parameter );

		//////////////////////////////////////////////////////////////////////
		/** Obtain available contexts
		 *
		 * \return Contexts identifiers
		**////////////////////////////////////////////////////////////////////
		std::vector<std::string> getAvailableContexts( );

		//////////////////////////////////////////////////////////////////////
		/** Obtain context configuration
		 *
		 * \param ctxName IN: context name
		 *
		 * \throws SPELLcoreException if the given context does not exist
		 *
		 * \return Context configuration object
		**////////////////////////////////////////////////////////////////////
		SPELLcontextConfig& getContext( std::string ctxName );

		//////////////////////////////////////////////////////////////////////
		/** Obtain driver configuration
		 *
		 * \param drvName IN: driver identifier
		 *
		 * \throws SPELLcoreException if the given driver does not exist
		 *
		 * \return Driver configuration object
		**////////////////////////////////////////////////////////////////////
		SPELLdriverConfig& getDriver( std::string drvName );

		//////////////////////////////////////////////////////////////////////
		/** Obtain the main configuration file name.
		 *
		 * \return The main configuration file name
		**////////////////////////////////////////////////////////////////////
		std::string getFile() const {
			return m_fileName;
		};

		//////////////////////////////////////////////////////////////////////
		/** Get the family name corresponding to the given SC name
		 *
		**////////////////////////////////////////////////////////////////////
        std::string getFamilyFor( const std::string& sc );

		//////////////////////////////////////////////////////////////////////
		/** Get the family name corresponding to the given SC name
		 *
		**////////////////////////////////////////////////////////////////////
        unsigned int commonOrDefault( const std::string& key, unsigned int defaultValue );

    // DATA MEMBERS //////////////////////////////////////////////////////////

protected: //-----------------------------------------------------------------

    // EXCEPTIONS ////////////////////////////////////////////////////////////
    // ENUMS /////////////////////////////////////////////////////////////////
    // TYPES /////////////////////////////////////////////////////////////////

		/** Used for server configuration parameters */
		typedef std::map<std::string,std::string> Properties;

		/** Context list */
		typedef std::map<std::string, SPELLcontextConfig*> ContextMap;

		/** Driver list */
		typedef std::map<std::string, SPELLdriverConfig*> DriverMap;

    // LIFECYCLE /////////////////////////////////////////////////////////////

		//////////////////////////////////////////////////////////////////////
		/** Constructor
		**////////////////////////////////////////////////////////////////////
		SPELLconfiguration();

    // METHODS ///////////////////////////////////////////////////////////////

		//////////////////////////////////////////////////////////////////////
		/** Load all context configurations.
		**////////////////////////////////////////////////////////////////////
		void loadContexts( const std::string& basePath );

		//////////////////////////////////////////////////////////////////////
		/** Load all driver configurations.
		**////////////////////////////////////////////////////////////////////
		void loadDrivers( const std::string& basePath );

		//////////////////////////////////////////////////////////////////////
		/** Load the context configurations provided by the given context
		 * configuration XML file.
		 *
		 * \param contextFile IN: path to the XML file.
		**////////////////////////////////////////////////////////////////////
		void loadContextConfiguration( std::string contextFile );

		//////////////////////////////////////////////////////////////////////
		/** Load the context configurations provided by the given context
		 * configuration XML file.
		 *
		 * \param contextFile IN: path to the XML file.
		**////////////////////////////////////////////////////////////////////
		void loadDriverConfiguration( std::string driverFile );

		//////////////////////////////////////////////////////////////////////
		/** Load the family definitions
		 *
		**////////////////////////////////////////////////////////////////////
        void loadFamilies( );

		//////////////////////////////////////////////////////////////////////
		/** Load the configuration corresponding to the given section, and
		 * store the values in the given property collection.
		 *
		 * \param section IN: name of the section
		 * \param properties INOUT: collections to store the values in.
		**////////////////////////////////////////////////////////////////////
		void loadSpecificConfiguration( std::string section, Properties& properties );

    // DATA MEMBERS //////////////////////////////////////////////////////////

private: //-------------------------------------------------------------------

    // EXCEPTIONS ////////////////////////////////////////////////////////////
    // ENUMS /////////////////////////////////////////////////////////////////
    // TYPES /////////////////////////////////////////////////////////////////

		typedef std::map< std::string,std::vector<std::string> > FamilyMap;

    // LIFECYCLE /////////////////////////////////////////////////////////////
    // METHODS ///////////////////////////////////////////////////////////////
    // DATA MEMBERS //////////////////////////////////////////////////////////
		/** Holds the map of available contexts (loaded on demand)*/
		ContextMap m_contexts;
		/** Holds the map of available drivers (loaded on demand)*/
		DriverMap m_drivers;
		/** Holds the XML reader */
		SPELLxmlConfigReader* m_reader;
		/** Holds common configuration for all system */
		Properties m_commonConfig;
		/** Holds server-specific configuration for all executors (may be overriden by per-context config ) */
		Properties m_executorsConfig;
		/** Holds server-specific configuration for all contexts (may be overriden by per-context config ) */
		Properties m_contextConfig;
		/** Holds server-specific configuration for listener */
		Properties m_listenerConfig;
		/** Holds the main configuration file name */
		std::string m_fileName;
		/** Holds the family definitions */
		FamilyMap m_families;
};

/*@}*/
#endif
