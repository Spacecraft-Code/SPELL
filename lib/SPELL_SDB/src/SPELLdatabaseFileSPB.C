// ################################################################################
// FILE       : SPELLdatabaseFileSPB.C
// DATE       : Mar 17, 2011
// PROJECT    : SPELL
// DESCRIPTION: Implementation based on local SPB files
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
#include "SPELL_WRP/SPELLconstants.H"
// Local includes ----------------------------------------------------------
#include "SPELL_SDB/SPELLdatabaseFileSPB.H"
// System includes ---------------------------------------------------------
#include <algorithm>
#include <ctype.h>

// FORWARD REFERENCES //////////////////////////////////////////////////////
// TYPES ///////////////////////////////////////////////////////////////////
// DEFINES /////////////////////////////////////////////////////////////////
// GLOBALS /////////////////////////////////////////////////////////////////

//=============================================================================
// FUNCTION: check short date match. This is to be replaced by better code
//           using regular expressions, but we don't want to add another
//           dependency for now.
//=============================================================================
bool matchesShortDate( const std::string& str )
{
	if (str.size()!=8) return false;
	if (str[2] != ':' || str[5] != ':' ) return false;
	if (!isdigit(str[0])) return false;
	if (!isdigit(str[1])) return false;
	if (!isdigit(str[3])) return false;
	if (!isdigit(str[4])) return false;
	if (!isdigit(str[6])) return false;
	if (!isdigit(str[7])) return false;
	return true;
}

//=============================================================================
// CONSTRUCTOR: SPELLdatabaseFileSPB::SPELLdatabaseFileSPB()
//=============================================================================
SPELLdatabaseFileSPB::SPELLdatabaseFileSPB( const std::string& name, const std::string& filename, const std::string& defExt )
: SPELLdatabaseFile(name,filename,defExt)
{
}

//=============================================================================
// DESTRUCTOR: SPELLdatabaseFileSPB::~SPELLdatabaseFileSPB()
//=============================================================================
SPELLdatabaseFileSPB::~SPELLdatabaseFileSPB()
{
}

//=============================================================================
// METHOD: SPELLdatabaseFileSPB::load()
//=============================================================================
void SPELLdatabaseFileSPB::load()
{
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

    for( it = lines.begin(); it != lines.end(); it++)
    {
    	std::string lineToProcess = *it;

    	// Remove spaces
    	SPELLutils::trim(lineToProcess);

    	// Process only lines starting with $, and having :=
    	if ((lineToProcess.find("$")!=0)||(lineToProcess.find(":=")==std::string::npos))
    	{
    		continue;
    	}

    	// Replace tabs
    	SPELLutils::replace(lineToProcess,"\t"," ");
    	// Remove \r
    	SPELLutils::replace(lineToProcess,"\n","");
    	SPELLutils::replace(lineToProcess,"\r","");

    	// Now process line data
    	int idx = lineToProcess.find(":=");
    	std::string key = lineToProcess.substr(0,idx);
    	idx += 2;
    	std::string origValue = lineToProcess.substr(idx,lineToProcess.size()-idx);
    	SPELLutils::trim(key);
    	SPELLutils::trim(origValue);

    	// Post-process value
    	if (matchesShortDate(origValue))
    	{
    		// Convert SPB short dates to SPELL short dates
    		origValue = "+" + origValue;
    	}

    	PyObject* value = NULL;
    	//std::string vtype = "";
    	value = importValue(origValue);

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
    		SPELLdatabase::set( SSTRPY(key), value, format );
    		//TODO if types needed to commit: m_types.insert( std::make_pair(key,vtype));
    	}
    }
}
