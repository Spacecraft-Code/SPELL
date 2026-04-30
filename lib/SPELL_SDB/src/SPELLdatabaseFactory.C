// ################################################################################
// FILE       : SPELLdatabaseFactory.C
// DATE       : Mar 17, 2011
// PROJECT    : SPELL
// DESCRIPTION: Implementation of SPELL database factory
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
#include "SPELL_UTIL/SPELLlog.H"
// Local includes ----------------------------------------------------------
#include "SPELL_SDB/SPELLdatabaseFactory.H"
#include "SPELL_SDB/SPELLdatabaseFile.H"
#include "SPELL_SDB/SPELLdatabaseFileSPB.H"
// System includes ---------------------------------------------------------

// FORWARD REFERENCES //////////////////////////////////////////////////////
// TYPES ///////////////////////////////////////////////////////////////////
// DEFINES /////////////////////////////////////////////////////////////////
// GLOBALS /////////////////////////////////////////////////////////////////

// Holds the singleton instance
SPELLdatabaseFactory* SPELLdatabaseFactory::s_instance = 0;

//=============================================================================
// STATIC: SPELLdatabaseFactory::instance()
//=============================================================================
SPELLdatabaseFactory& SPELLdatabaseFactory::instance()
{
    if (s_instance == 0)
    {
        s_instance = new SPELLdatabaseFactory();
    }
    return *s_instance;
}

//=============================================================================
// METHOD: SPELLdatabaseFactory::SPELLdatabaseFactory()
//=============================================================================
SPELLdatabase* SPELLdatabaseFactory::createDatabase( const std::string& type,
		 	 	 	 	 	 	 	 	 	 	 	 const std::string& name,
		 	 	 	 	 	 	 	 	 	 	 	 const std::string& filename,
		 	 	 	 	 	 	 	 	 	 	 	 const std::string& defExt )
{
	DEBUG("[SDBFAC] Create database: type=" + type + ", name=" + name + ", file=" + filename + ", ext=" + defExt);
	// Temp fix for subversion
	if (type == "spell.adapter.databases.DatabaseFile" || type == "file" || type == "svn" )
	{
		DEBUG("[SDBFAC] Using file type");
		return new SPELLdatabaseFile(name,filename,defExt);
	}
	else if (type == "spell.adapter.databases.DatabaseFileSPB" || type == "spb" )
	{
		DEBUG("[SDBFAC] Using SDB type");
		return new SPELLdatabaseFileSPB(name,filename,defExt);
	}
	else if (type == "spell.adapter.databases.Database")
	{
		DEBUG("[SDBFAC] Using default type");
		return new SPELLdatabase(name,filename,defExt);
	}
	else
	{
		LOG_ERROR("Could not create database for type '" + type + "'");
	}
	return NULL;
}
