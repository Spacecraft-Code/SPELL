// ################################################################################
// FILE       : SPELLprocedure.C
// DATE       : Mar 17, 2011
// PROJECT    : SPELL
// DESCRIPTION: Implementation of procedure model
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
// Local includes ----------------------------------------------------------
#include "SPELL_PRD/SPELLprocedure.H"
// Project includes --------------------------------------------------------
#include "SPELL_CFG/SPELLconfiguration.H"
#include "SPELL_UTIL/SPELLlog.H"
#include "SPELL_UTIL/SPELLerror.H"
// System includes ---------------------------------------------------------
#include <sys/stat.h>
#include <sys/mman.h>
#include <fcntl.h>
#include <stdio.h>

#define KEY_NAME "NAME"
#define KEY_PYTHON_VERSION "PYTHON VERSION"
#define KEY_FILE "FILE"
#define KEY_HISTORY "REVISION HISTORY"
#define KEY_SC1 "SPACECRAFT"
#define KEY_SC2 "SPACECRAFTS"

const std::string CHECK_SPACECRAFT = "CheckSpacecraft";
const std::string OVERRIDE_SC = "OverrideContextSpacecraftName";

//=============================================================================
// CONSTRUCTOR : SPELLprocedure::SPELLprocedure()
//=============================================================================
SPELLprocedure::SPELLprocedure( const std::string& procPath, const std::string& filename )
: m_source(filename)
{
    // Remove the .py extension first, if any
    std::size_t pos = filename.find_last_of(".py");

    if (pos != std::string::npos )
    {
        m_procId = filename.substr(0,pos-2);
    }

    // The remove the proc base path, not the subfolders
    m_procId = m_procId.substr( procPath.size()+1, m_procId.size()-procPath.size()-1 );

    // Will be parsed
    m_name = "";

    m_file = filename;
    m_header = "";

    // Parse the file to get properties and source code
    parseFile(filename);
}

//=============================================================================
// DESTRUCTOR : SPELLprocedure::~SPELLprocedure
//=============================================================================
SPELLprocedure::~SPELLprocedure()
{
    m_properties.clear();
}

//=============================================================================
// METHOD    : SPELLprocedure::getHeader
//=============================================================================
std::string SPELLprocedure::getHeader()
{
	if (!m_header.empty())
		return m_header;

	std::ifstream file;
	file.open( m_file.c_str() );
	if (!file.is_open())
	{
		THROW_EXCEPTION("Cannot parse file " + m_file, "Unable to open", SPELL_ERROR_FILESYSTEM);
	}
	// Will be true while in comment lines
	bool isComment = false;
	// Will be true while inside the header
	bool inHeader = false;
	// Will be true when finished parsing the header
	bool headerDone = false;
	// Will be true if parsing the history of changes
	bool inHistory  = false;
	m_header = "";
	// Stores the last key found
	std::string lastKey;
	while(!file.eof())
	{
		std::string line = "";
		std::getline(file,line);

		if ((line.size()>0)&&(!headerDone))
		{
			isComment = (line[0] == '#');
			if (isComment && (!inHeader))
			{
				// If it is a header delimiter line we are entering the header
				if (isLimitLine(line))
				{
					inHeader = true;
				}
			}
			else if (isComment && inHeader)
			{
				m_header += line + "\n";
				// If we are in header and another limit line comes, we have finished
				if (isLimitLine(line))
				{
					headerDone = true;
					break;
				}
			}
		}
	}
	file.close();
	return m_header;
}

//=============================================================================
// METHOD    : SPELLprocedure::parseFile
//=============================================================================
void SPELLprocedure::parseFile( const std::string& path )
{
    // Will be composed during parsing
    m_source.clear();

    DEBUG("[PROC] Parsing file " + path);

    std::ifstream file;
    file.open( path.c_str() );
    if (!file.is_open())
    {
        THROW_EXCEPTION("Cannot parse file " + path, "Unable to open", SPELL_ERROR_FILESYSTEM);
    }
    // Will be true while in comment lines
    bool isComment = false;
    // Will be true while inside the header
    bool inHeader = false;
    // Will be true when finished parsing the header
    bool headerDone = false;
    // Will be true if parsing the history of changes
    bool inHistory  = false;

    // Reset properties
    m_properties.clear();

    // Insert a blank history of changes
    m_properties[KEY_HISTORY] = "";

    // Stores the last key found
    std::string lastKey;
    while(!file.eof())
    {
        std::string line = "";
        std::getline(file,line);
        if ((line.size()>0)&&(!headerDone))
        {
            isComment = (line[0] == '#');
            if (isComment && (!inHeader))
            {
                // If it is a header delimiter line we are entering the header
                if (isLimitLine(line))
                {
                    inHeader = true;
                }
            }
            else if (isComment && inHeader)
            {
                // If we are in header and another limit line comes, we have finished
                if (isLimitLine(line))
                {
                    headerDone = true;
                }
                else if (inHistory)
                {
                    // This adds more lines to the history property
                    line = line.substr(1,line.size()-1);
                    SPELLutils::trim(line, "\r\n");
                    if (m_properties[KEY_HISTORY] == "")
                    {
                        m_properties[KEY_HISTORY] = line;
                    }
                    else
                    {
                    	m_properties[KEY_HISTORY] = (m_properties[KEY_HISTORY] + "\n" + line);
                    }
                }
                else if (isPropertyLine(line))
                {
                    // Remove the leading hash #
                    line = line.substr(1,line.size()-1);
                    // Split the line in key / value
                    std::vector<std::string> tokens = SPELLutils::tokenize(line, ":");
                    lastKey = tokens[0];
                    std::string value;
                    if (tokens.size()==2)
                    {
                        value = tokens[1];
                    }
                    else
                    {
                        value = "<?>";
                    }

                    // Remove unwanted characters
                    SPELLutils::trim(lastKey);
                    SPELLutils::trim(lastKey, "\r\n");

                    // Skip to process the history of changes
                    if (lastKey == KEY_HISTORY)
                    {
                    	inHistory = true;
                    }
                    else
                    {
                        SPELLutils::trim(value);
                        SPELLutils::trim(value, "\r\n");
                    	m_properties.insert( std::make_pair(lastKey, value));
                    }
                }
                else if (isPropertyContinued(line))
                {
                    // This adds more lines to the multiline properties
                    line = line.substr(1,line.size()-1);
                    SPELLutils::trim(line);
                    SPELLutils::trim(line, "\r\n");
                    if (m_properties[lastKey] == "<?>")
                    {
                        m_properties[lastKey] = line;
                    }
                    else
                    {
                    	m_properties[lastKey] = (m_properties[lastKey] + "\n" + line);
                    }
                }
            }
        }
    }
    file.close();
    // If the name was not found in the properties, take the proc id as the procedure name
    if (m_properties.find(KEY_NAME) == m_properties.end())
    {
        m_properties[KEY_NAME] = m_procId;
    }
    m_name = m_properties[KEY_NAME];
    m_properties[KEY_FILE] = m_file;
}

//=============================================================================
// METHOD    : SPELLprocedure::getProperty
//=============================================================================
const std::string SPELLprocedure::getProperty( const std::string& key )
{
    Properties::const_iterator it = m_properties.find(key);
    if ( it != m_properties.end())
    {
    	return m_properties[key];
    }
    else
    {
    	return "";
    }
}

//=============================================================================
// METHOD    : SPELLprocedure::getPropertyKeys
//=============================================================================
SPELLprocedure::PropertyKeys SPELLprocedure::getPropertyKeys()
{
	PropertyKeys keys;
	Properties::const_iterator it;
	for( it = m_properties.begin(); it != m_properties.end(); it++ )
	{
		keys.push_back(it->first);
	}
	return keys;
}

//=============================================================================
// METHOD    : SPELLprocedure::isPropertyLine
//=============================================================================
const bool SPELLprocedure::isPropertyLine( const std::string& line )
{
    std::size_t pos = line.find_first_of(":");
    return ( (line[0] == '#' ) && ( pos != std::string::npos) );
}

//=============================================================================
// METHOD    : SPELLprocedure::isLimitLine
//=============================================================================
const bool SPELLprocedure::isLimitLine( const std::string& line )
{
    std::size_t pos = line.find_first_not_of("#\n\r");
    return ((pos == std::string::npos) && (line.size()>5));
}

//=============================================================================
// METHOD    : SPELLprocedure::isPropertyContinued
//=============================================================================
const bool SPELLprocedure::isPropertyContinued( const std::string& line )
{
    std::size_t pos = line.find_first_not_of("# \n\r");
    return ( (line[0] == '#' ) && (pos != std::string::npos));
}

//=============================================================================
// METHOD    : SPELLprocedure::forSpacecraft
//=============================================================================
bool SPELLprocedure::forSpacecraft( const std::string& sc ) const
{
	if (SPELLconfiguration::instance().getContextParameter(CHECK_SPACECRAFT) == "false")
	{
		return true;
	}
    const std::string overrideSC = SPELLconfiguration::instance().getContextParameter(OVERRIDE_SC);
    std::string actualSC;
	if (overrideSC != "") {
		actualSC = overrideSC;
	} else {
        actualSC = sc;
    }

	std::string fam = SPELLconfiguration::instance().getFamilyFor(actualSC);
	// If the family is undefined, discard the proc
	if (fam == "") return false;

	Properties::const_iterator it;
	it = m_properties.find(KEY_SC1);
	if (it == m_properties.end())
	{
		it = m_properties.find(KEY_SC2);
	}

	if (it != m_properties.end())
	{
		std::string scList = it->second;
		std::vector<std::string> tokens = SPELLutils::tokenize(scList,",");
		for( std::vector<std::string>::iterator sit = tokens.begin(); sit != tokens.end(); sit++ )
		{
			std::string scFamInProc = *sit;
			SPELLutils::trim(scFamInProc);
			if (scFamInProc == fam) return true;
			if (scFamInProc == actualSC) return true;
		}
	}
	return false;
}

const std::vector<std::string> SPELLprocedureSourceCode::getSourceCodeLines() {
	if (!m_sourceLines.empty())
		return m_sourceLines;

	std::ifstream file;
	file.open( m_filename.c_str() );
	if (!file.is_open())
	{
		THROW_EXCEPTION("Cannot parse file " + m_filename, "Unable to open", SPELL_ERROR_FILESYSTEM);
	}
	// Stores the last key found
	std::string lastKey;
	std::string line = "";
	while(!file.eof())
	{
		std::getline(file,line);
		m_sourceLines.push_back(line);
	}

	return m_sourceLines;
};
