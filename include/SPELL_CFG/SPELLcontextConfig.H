// ################################################################################
// FILE       : SPELLcontextConfig.H
// DATE       : Mar 17, 2011
// PROJECT    : SPELL
// DESCRIPTION: Wrapper for context configuration
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

#ifndef __SPELL_CTX_CONFIGURATION_H__
#define __SPELL_CTX_CONFIGURATION_H__

// FILES TO INCLUDE ////////////////////////////////////////////////////////
// System includes ---------------------------------------------------------
// Local includes ----------------------------------------------------------
#include "SPELL_CFG/SPELLxmlConfigReader.H"
// Project includes --------------------------------------------------------

/** \addtogroup SPELL_CFG */
/*@{*/


// FORWARD REFERENCES //////////////////////////////////////////////////////
// TYPES ///////////////////////////////////////////////////////////////////
// DEFINES /////////////////////////////////////////////////////////////////

//////////////////////////////////////////////////////////////////////////////
/**
 * \brief Context configuration object.
 *
 * \par Description and usage:
 *
 * 		The SPELLconfiguration object provides an instance of this class for
 * 		each known SPELL context in the system. This object provides configuration
 * 		values specific to the associated SPELL context (also satellite-specific).
 *
 */
//////////////////////////////////////////////////////////////////////////////
class SPELLcontextConfig
{
public: //--------------------------------------------------------------------

    // EXCEPTIONS ////////////////////////////////////////////////////////////
    // ENUMS /////////////////////////////////////////////////////////////////
    // TYPES /////////////////////////////////////////////////////////////////
    // LIFECYCLE /////////////////////////////////////////////////////////////

		//////////////////////////////////////////////////////////////////////
		/** Constructor
		 *
		 * \param contextFile IN: path to context configuration file.
		**////////////////////////////////////////////////////////////////////
		SPELLcontextConfig( const std::string& contextFile );

		//////////////////////////////////////////////////////////////////////
		/** Destructor
		**////////////////////////////////////////////////////////////////////
		~SPELLcontextConfig();

    // STATIC ////////////////////////////////////////////////////////////////
		static const std::string ExecutorProgram;
		static const std::string ExecutorStartTimeout;
		static const std::string ExecutorLoginTimeout;
		static const std::string UseDriverTime;
    // METHODS ///////////////////////////////////////////////////////////////

		//////////////////////////////////////////////////////////////////////
		/** Obtain the context name.
		 *
		 * \returns The context name.
		**////////////////////////////////////////////////////////////////////
		std::string getName() const { return m_ctxName; };

		//////////////////////////////////////////////////////////////////////
		/** Obtain the context name.
		 *
		 * \returns The context name.
		**////////////////////////////////////////////////////////////////////
		std::string getFileName() const { return m_ctxFileName; };

		//////////////////////////////////////////////////////////////////////
		/** Obtain the context description.
		 *
		 * \returns The context description.
		**////////////////////////////////////////////////////////////////////
		std::string getDescription() const { return m_ctxDesc; };

		//////////////////////////////////////////////////////////////////////
		/** Obtain the spacecraft ID corresponding to this context.
		 *
		 * \returns The spacecraft ID.
		**////////////////////////////////////////////////////////////////////
		std::string getSC() const { return m_spacecraft; };

		//////////////////////////////////////////////////////////////////////
		/** Obtain the GCS family corresponding to this context.
		 *
		 * \returns The GCS family.
		**////////////////////////////////////////////////////////////////////
		std::string getFamily() const { return m_family; }

		//////////////////////////////////////////////////////////////////////
		/** Obtain the GCS name corresponding to this context.
		 *
		 * \returns The GCS name.
		**////////////////////////////////////////////////////////////////////
		std::string getGCS() const { return m_gcs; };

		//////////////////////////////////////////////////////////////////////
		/** Obtain the GCS name corresponding to this context.
		 *
		 * \returns The GCS name.
		**////////////////////////////////////////////////////////////////////
		std::string getDriverName() const { return m_driver; };

		//////////////////////////////////////////////////////////////////////
		/** Obtain the current procedure path.
		 *
		 * \returns The procedure search path.
		**////////////////////////////////////////////////////////////////////
		std::string getProcPath() const { return m_procPath; };

		//////////////////////////////////////////////////////////////////////
		/** Obtain the current library path.
		 *
		 * \returns The library search path.
		**////////////////////////////////////////////////////////////////////
		std::string getLibPath() const { return m_libPath; };

		//////////////////////////////////////////////////////////////////////
		/** Obtain the current output directory path
		 *
		 * \returns The path.
		**////////////////////////////////////////////////////////////////////
		std::string getOutputDirectory() const { return m_outputDir; };

		//////////////////////////////////////////////////////////////////////
		/** Obtain the current input directory path
		 *
		 * \returns The path.
		**////////////////////////////////////////////////////////////////////
		std::string getInputDirectory() const { return m_inputDir; };

		//////////////////////////////////////////////////////////////////////
		/** Obtain a driver configuration parameter.
		 *
		 * \param key IN: the name of the configuration parameter.
		 *
		 * \returns The value of the configuration parameter.
		**////////////////////////////////////////////////////////////////////
		std::string getDriverParameter( const std::string& key ) const;

		//////////////////////////////////////////////////////////////////////
		/** Obtain an executor configuration parameter.
		 *
		 * \param key IN: the name of the configuration parameter.
		 *
		 * \returns The value of the configuration parameter.
		**////////////////////////////////////////////////////////////////////
		std::string getExecutorParameter( const std::string& key ) const;

		//////////////////////////////////////////////////////////////////////
		/** Obtain the path for a given database location
		 *
		 * \param locationName IN: name of the database location
		 *
		 * \throws SPELLcoreException if the location does not exist
		 *
		 * \return Path associated to the given location
		**////////////////////////////////////////////////////////////////////
		std::string getLocationPath( const std::string& locationName ) const;

		//////////////////////////////////////////////////////////////////////
		/** Obtain the extension for a given database location
		 *
		 * \param locationName IN: name of the database location
		 *
		 * \throws SPELLcoreException if the location does not exist
		 *
		 * \return Extension associated to the given location
		**////////////////////////////////////////////////////////////////////
		std::string getLocationExtension( const std::string& locationName ) const;

		//////////////////////////////////////////////////////////////////////
		/** Obtain the type for a given database location
		 *
		 * \param locationName IN: name of the database location
		 *
		 * \throws SPELLcoreException if the location does not exist
		 *
		 * \return Extension associated to the given location
		**////////////////////////////////////////////////////////////////////
		std::string getLocationType( const std::string& locationName ) const;

		//////////////////////////////////////////////////////////////////////
		/** Obtain the list of builtin databases.
		 *
		 * \return The list of builtin databases.
		**////////////////////////////////////////////////////////////////////
		std::list<std::string> getBuiltinDatabases() const;

		//////////////////////////////////////////////////////////////////////
		/** Obtain the list of defined locations
		 *
		 * \return The list of defined locations
		**////////////////////////////////////////////////////////////////////
		std::list<std::string> getLocations() const;

		//////////////////////////////////////////////////////////////////////
		/** Obtain the location for a given builtin database.
		 *
		 * \return The database location for the given builtin database.
		**////////////////////////////////////////////////////////////////////
		std::string getDatabaseLocation( const std::string& database ) const;

    // DATA MEMBERS //////////////////////////////////////////////////////////

protected: //-----------------------------------------------------------------

    // EXCEPTIONS ////////////////////////////////////////////////////////////
    // ENUMS /////////////////////////////////////////////////////////////////
    // TYPES /////////////////////////////////////////////////////////////////
    // LIFECYCLE /////////////////////////////////////////////////////////////
    // METHODS ///////////////////////////////////////////////////////////////
    // DATA MEMBERS //////////////////////////////////////////////////////////

private: //-------------------------------------------------------------------

    // EXCEPTIONS ////////////////////////////////////////////////////////////
    // ENUMS /////////////////////////////////////////////////////////////////
    // TYPES /////////////////////////////////////////////////////////////////
		/** Internally used for storing property names and values */
		typedef std::map<std::string,std::string> Properties;
    // LIFECYCLE /////////////////////////////////////////////////////////////
    // METHODS ///////////////////////////////////////////////////////////////

		//////////////////////////////////////////////////////////////////////
		/** Load all basic parameters
		 *
		 * \throws SPELLcoreException in case of load failure
		**////////////////////////////////////////////////////////////////////
		void loadBasics();

		//////////////////////////////////////////////////////////////////////
		/** Load executor parameters
		**////////////////////////////////////////////////////////////////////
		void loadExecutorParameters();

		//////////////////////////////////////////////////////////////////////
		/** Load driver parameters
		**////////////////////////////////////////////////////////////////////
		void loadDriverParameters();

		//////////////////////////////////////////////////////////////////////
		/** Load all defined file locations.
		**////////////////////////////////////////////////////////////////////
		void loadLocations();

    // DATA MEMBERS //////////////////////////////////////////////////////////
		/** Holds the context name */
		std::string m_ctxName;
		/** Holds the context filename */
		std::string m_ctxFileName;
		/** Holds the context description */
		std::string m_ctxDesc;
		/** Holds the S/C name */
		std::string m_spacecraft;
		/** Holds the driver name */
		std::string m_driver;
		/** Holds the server family */
		std::string m_family;
		/** Holds the GCS*/
		std::string m_gcs;
		/** Holds the procedure path */
		std::string m_procPath;
		/** Holds the library path */
		std::string m_libPath;
		/** Holds the output data directory */
		std::string m_outputDir;
		/** Holds the input data directory */
		std::string m_inputDir;
		/** Holds the executor config  */
		Properties m_executorConfig;
		/** Holds the driver config  */
		Properties m_driverConfig;
		/** Holds the XML reader */
		SPELLxmlConfigReader* m_reader;
		/** Holds database location paths */
		Properties m_locationPaths;
		/** Holds database location extensions */
		Properties m_locationExts;
		/** Holds database location types */
		Properties m_locationTypes;
		/** Holds the predefined databases */
		Properties m_builtinDatabases;
};

/*@}*/
#endif
