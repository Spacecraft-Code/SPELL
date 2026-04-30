// ################################################################################
// FILE       : SPELLdatabaseFile.C
// DATE       : Mar 17, 2011
// PROJECT    : SPELL
// DESCRIPTION: Implementation of database based on local files
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
// Project includes --------------------------------------------------------
#include "SPELL_UTIL/SPELLutils.H"
#include "SPELL_UTIL/SPELLpythonHelper.H"
#include "SPELL_UTIL/SPELLpythonMonitors.H"
#include "SPELL_WRP/SPELLconstants.H"
// Local includes ----------------------------------------------------------
#include "SPELL_SDB/SPELLdatabaseFile.H"
// System includes ---------------------------------------------------------
#include <algorithm>

// FORWARD REFERENCES //////////////////////////////////////////////////////
// TYPES ///////////////////////////////////////////////////////////////////
// DEFINES /////////////////////////////////////////////////////////////////
// GLOBALS /////////////////////////////////////////////////////////////////

//=============================================================================
// CONSTRUCTOR: SPELLdatabaseFile::SPELLdatabaseFile()
//=============================================================================
SPELLdatabaseFile::SPELLdatabaseFile( const std::string& name, const std::string& filename, const std::string& defExt )
: SPELLdatabase(name,filename,defExt)
{
}

//=============================================================================
// DESTRUCTOR: SPELLdatabaseFile::~SPELLdatabaseFile()
//=============================================================================
SPELLdatabaseFile::~SPELLdatabaseFile()
{
}

//=============================================================================
// METHOD: SPELLdatabaseFile::create()
//=============================================================================
void SPELLdatabaseFile::create()
{
	std::ofstream file;
	file.open( getFilename().c_str(), std::ios::out);
	if (!file.is_open())
	{
        THROW_EXCEPTION("Cannot load database", "Unable to create file '" + getFilename() + "'", SPELL_ERROR_FILESYSTEM);
	}
	file.close();
}

//=============================================================================
// METHOD: SPELLdatabaseFile::load()
//=============================================================================
void SPELLdatabaseFile::load()
{
	DEBUG("[SDBF] Load database from file " + getFilename());

    std::ifstream file;
    std::string filename = getFilename();
	file.open( filename.c_str(), std::ios::in );
    if (!file.is_open())
    {
    	filename = getFilename() + "." + SPELLutils::toLower(getExtension());
    	DEBUG("[SDBF] Second try: " + filename);
		file.open( filename.c_str(), std::ios::in );
		if (!file.is_open())
		{
	    	filename = getFilename() + "." + SPELLutils::toUpper(getExtension());
	    	DEBUG("[SDBF] Third try: " + filename);
			file.open( filename.c_str(), std::ios::in );
			if (!file.is_open())
			{
				THROW_EXCEPTION("Cannot load database", "Cannot open file '" + getFilename() + "'", SPELL_ERROR_FILESYSTEM);
			}
		}
    }

    std::vector<std::string> lines;
    while(!file.eof())
    {
        std::string line = "";
        std::getline(file,line);
        lines.push_back(line);
    }
    file.close();
    std::vector<std::string>::iterator it;
    std::string lineToProcess = "";
    unsigned int count = 0;
    for( it = lines.begin(); it != lines.end(); it++)
    {
    	count++;
    	// If there is something in the buffer, append the next line
    	if (lineToProcess != "")
    	{
    		// But remove backslash and spaces first
    		lineToProcess = lineToProcess.substr(0, lineToProcess.size()-1);
    		SPELLutils::trim(lineToProcess);
        	std::string line = *it;
        	SPELLutils::trim(line);
        	lineToProcess += line;
    	}
    	// Otherwise add the current line
    	else
    	{
        	std::string line = *it;
        	SPELLutils::trim(line);
    		lineToProcess = line;
    	}

    	// Remove \r
    	lineToProcess.erase(std::remove( lineToProcess.begin(), lineToProcess.end(), '\r'),lineToProcess.end());

    	// Remove spaces
    	SPELLutils::trim(lineToProcess);

    	// Ignore empty lines
    	if (lineToProcess == "") continue;

    	// Ignore comment lines
    	if (lineToProcess.find("#")==0)
    	{
    		lineToProcess = "";
    		continue;
    	}

    	// If the line ends with backslash we need to concatenate with next line
    	if (lineToProcess.at(lineToProcess.size()-1) == '\\')
    	{
    		continue;
    	}

    	// Replace tabs
    	SPELLutils::replace(lineToProcess,"\t"," ");

    	DEBUG("   '" + lineToProcess + "'");

    	// Now process line data
    	std::vector<std::string> tokens = SPELLutils::tokenized(lineToProcess," ");
    	std::string origValue = "";
    	for (unsigned int index=1; index<tokens.size(); index++)
    	{
    		if (origValue != "") origValue += " ";
    		origValue += tokens[index];
    	}
    	std::string key = tokens[0];

    	// Remove spaces
    	SPELLutils::trim(key);
    	SPELLutils::trim(origValue);

    	if (key == "")
    	{
        	std::cerr << "ERROR: bad key on dictionary " + getName() + ", line " << count << std::endl;
        	lineToProcess = "";
    		continue;
    	}

		PyObject* value = NULL;
		//std::string vtype = "";
		value = importValue(origValue);
		Py_XINCREF(value);

    	std::string format = "";
    	if (PyLong_Check(value) || PyInt_Check(value))
    	{
    		if (origValue.find("0x") == 0)
    		{
    			format = LanguageConstants::HEX;
    		}
    		else if (origValue.find("0b") == 0)
    		{
    			format = LanguageConstants::BIN;
    		}
    		else if (origValue.find("0") == 0 && origValue != "0")
    		{
    			format = LanguageConstants::OCT;
    		}
    	}

		if (value != NULL)
		{
	    	DEBUG("    - Key: " + key + ", Value: " + PYREPR(value));
			set( SSTRPY(key), value, format );
			//TODO if types needed to commit: m_types.insert( std::make_pair(key,vtype));
		}
		else
		{
			LOG_ERROR("ERROR: unable to resolve value on line " + ISTR(count) + " in " + getName() );
		}
    	lineToProcess = "";
    }
}

//=============================================================================
// METHOD: SPELLdatabaseFile::importValue()
//=============================================================================
PyObject* SPELLdatabaseFile::importValue( const std::string& origValue )
{
	PyObject* pyOrigValue = SSTRPY(origValue);
	Py_INCREF(pyOrigValue);
	PyObject* result = SPELLpythonHelper::instance().callFunction("spell.utils.vimport", "ImportValue", pyOrigValue, NULL );
	Py_XDECREF(pyOrigValue);
	if (result != NULL)
	{
		return PyList_GetItem(result,0);
	}
	else
	{
		return NULL;
	}
}

//=============================================================================
// METHOD: SPELLdatabaseFile::reload()
//=============================================================================
void SPELLdatabaseFile::reload()
{
	clearValues();
	load();
}

//=============================================================================
// METHOD: SPELLdatabaseFile::id()
//=============================================================================
std::string SPELLdatabaseFile::id()
{
	return getFilename();
}

//=============================================================================
// METHOD: SPELLdatabaseFile::commit()
//=============================================================================
void SPELLdatabaseFile::commit()
{

}
