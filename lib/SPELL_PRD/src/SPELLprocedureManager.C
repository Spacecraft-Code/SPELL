// ################################################################################
// FILE       : SPELLprocedureManager.C
// DATE       : Mar 17, 2011
// PROJECT    : SPELL
// DESCRIPTION: Implementation of procedure manager
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
#include "SPELL_PRD/SPELLprocedureManager.H"
// Project includes --------------------------------------------------------
#include "SPELL_CFG/SPELLconfiguration.H"
#include "SPELL_UTIL/SPELLlog.H"
#include "SPELL_UTIL/SPELLpythonHelper.H"
#include "SPELL_SYN/SPELLmonitor.H"



static SPELLprocedureManager* s_instance = 0;

//=============================================================================
// CONSTRUCTOR : SPELLprocedureManager::SPELLprocedureManager()
//=============================================================================
SPELLprocedureManager::SPELLprocedureManager()
{
    m_ctxName = "";
    m_procPath = "";
    m_libPath = "";
}

//=============================================================================
// DESTRUCTOR : SPELLprocedureManager::~SPELLprocedureManager
//=============================================================================
SPELLprocedureManager::~SPELLprocedureManager()
{
    m_idToFilename.clear();
    m_filenameToId.clear();
    m_idToName.clear();
    m_nameToId.clear();
}

//=============================================================================
// METHOD    : SPELLprocedureManager::instance()
//=============================================================================
SPELLprocedureManager& SPELLprocedureManager::instance()
{
    if (s_instance == NULL)
    {
        s_instance = new SPELLprocedureManager();
    }
    return *s_instance;
}

//=============================================================================
// METHOD    : SPELLprocedureManager::setup
//=============================================================================
void SPELLprocedureManager::setup( const std::string& ctxName )
{
	DEBUG("[PRCM] Setup for context " + ctxName);
    m_ctxName = ctxName;

    resolvePaths();

    /** \todo get from config the expected procedure properties. The minimum set is name. */
    refresh();
}

//=============================================================================
// METHOD    : SPELLprocedureManager::resolvePaths
//=============================================================================
void SPELLprocedureManager::resolvePaths()
{
	SPELLmonitor lock(m_lock);
    SPELLcontextConfig& ctx = SPELLconfiguration::instance().getContext(m_ctxName);
    m_procPath = ctx.getProcPath();
    m_libPath = ctx.getLibPath();
    // Resolve the path
    m_procPath = SPELLutils::resolvePath(m_procPath);
    m_libPath = SPELLutils::resolvePath(m_libPath);
    SPELLutils::trim(m_procPath, "\r\n");
    SPELLutils::trim(m_libPath, "\r\n");
    if (!SPELLutils::isDirectory(m_procPath))
	{
    	LOG_ERROR("Procedure path not found or not a directory: '" + m_procPath + "'");
    	m_procPath = "";
	}
    else
    {
    	LOG_INFO("[PRCM] Procedure path: " + m_procPath);
    }

    if (m_libPath != "" && m_libPath != "None")
    {
        if (!SPELLutils::isDirectory(m_libPath))
        {
        	LOG_ERROR("Library path not found or not a directory: '" + m_libPath + "'");
        	m_libPath = "";
        }
        else
        {
            LOG_INFO("[PRCM] Library path: " + m_libPath);
        }
    }
    else
    {
        LOG_INFO("[PRCM] No user libraries defined");
    }
}

//=============================================================================
// METHOD    : SPELLprocedureManager::refresh
//=============================================================================
void SPELLprocedureManager::refresh()
{
	SPELLmonitor lock(m_lock);
    DEBUG("[PRCM] Refreshing procedures");
    SPELLcontextConfig& ctx = SPELLconfiguration::instance().getContext(m_ctxName);
    std::string sc = ctx.getSC();
    m_idToFilename.clear();
    m_filenameToId.clear();
    m_idToName.clear();
    m_nameToId.clear();
    ProcModels::iterator it;
    for(it = m_procModels.begin(); it != m_procModels.end(); it++)
    {
        delete (*it).second;
    }
    m_procModels.clear();

    // Traverse directories and process found procedures and libraries
    if (m_procPath != "")
    {
    	LOG_INFO("Loading procedure files");
    	findProcedures( m_procPath, sc );
    }
    if (m_libPath != "" && m_libPath != "None")
    {
    	LOG_INFO("Loading library files");
        findLibraries( m_libPath );
    }
}

//=============================================================================
// METHOD    : SPELLprocedureManager::getLibPath
//=============================================================================
const std::string SPELLprocedureManager::getLibPath() const
{
    return m_libPath;
}

//=============================================================================
// METHOD    : SPELLprocedureManager::getProcPath
//=============================================================================
const std::string SPELLprocedureManager::getProcPath() const
{
    return m_procPath;
}

//=============================================================================
// METHOD    : SPELLprocedureManager::getProcFile
//=============================================================================
const std::string SPELLprocedureManager::getProcFile( const std::string& procId )
{
    DEBUG("[PRCM] Get procedure file for ID: " + procId );
    std::string theProcId = noInstanceId(procId);
    DEBUG("[PRCM] ID without instance: " + theProcId );
    ProcMap::iterator it = m_idToFilename.find(theProcId);
    if (it == m_idToFilename.end())
    {
        DEBUG("[PRCM] Did not find any file for: " + theProcId );
        THROW_EXCEPTION("Cannot get file for procedure/library '" + theProcId + "'", "No such identifier", SPELL_ERROR_PROCEDURES);
    }
    DEBUG("[PRCM] Found procedure file: " + it->second );
    return it->second;
}

//=============================================================================
// METHOD    : SPELLprocedureManager::getProcId
//=============================================================================
const std::string SPELLprocedureManager::getProcId( const std::string& filename )
{
    DEBUG("[PRCM] Get procedure ID for file: " + filename );
    ProcMap::iterator it = m_filenameToId.find(filename);
    if (it == m_filenameToId.end())
    {
        DEBUG("[PRCM] Did not find any ID");
        THROW_EXCEPTION("Cannot get identifier for file '" + filename + "'", "No such file", SPELL_ERROR_PROCEDURES);
    }
    DEBUG("[PRCM] Found proc ID: " + it->second );
    return it->second;
}

//=============================================================================
// METHOD    : SPELLprocedureManager::getProcList
//=============================================================================
const SPELLprocedureManager::ProcList SPELLprocedureManager::getProcList()
{
    ProcList list;
    ProcMap::iterator it;
    for( it = m_idToName.begin(); it != m_idToName.end(); it++)
    {
        list.push_back( (*it).first + "|" + (*it).second );
    }
    return list;
}

//=============================================================================
// METHOD    : SPELLprocedureManager::getProcName
//=============================================================================
const std::string SPELLprocedureManager::getProcName( const std::string& procId )
{
    DEBUG("[PRCM] Get procedure name for ID: " + procId );
    std::string theProcId = noInstanceId(procId);
    DEBUG("[PRCM] ID without instance: " + theProcId );
    ProcMap::iterator it = m_idToName.find(theProcId);
    if (it == m_idToName.end())
    {
        DEBUG("[PRCM] Did not find any name for: " + theProcId );
        THROW_EXCEPTION("Cannot get name for procedure '" + theProcId + "'", "No such identifier", SPELL_ERROR_PROCEDURES);
    }
    DEBUG("[PRCM] Found procedure name: " + it->second );
    return it->second;
}

//=============================================================================
// METHOD    : SPELLprocedureManager::findProcedures
//=============================================================================
void SPELLprocedureManager::findProcedures( const std::string& basePath, const std::string& sc )
{
    if (!SPELLutils::isDirectory(basePath))
	{
    	THROW_EXCEPTION("Cannot find procedures at " + basePath, "Path not found or not a directory", SPELL_ERROR_PROCEDURES);
	}

    std::list<std::string> files = SPELLutils::getFilesInDir( basePath );
    std::list<std::string>::iterator it;
    std::list<std::string>::iterator end = files.end();

    std::string procPath = getProcPath();

    for( it = files.begin(); it != end; it++ )
    {
        if ((*it) == "__init__.py") continue;


        std::size_t idx = (*it).find_last_of(".py");
        std::size_t idxb = (*it).find(".py~"); // Filter out backup files
        if ((idx != std::string::npos && idx == ((*it).length() - 1)  ) && ( idxb == std::string::npos ))
        {
            std::string procFile = basePath + PATH_SEPARATOR + (*it);
            SPELLprocedure* proc = NULL;
            try
            {
                proc = new SPELLprocedure( procPath, procFile );
                if (proc->forSpacecraft(sc))
                {
					DEBUG("[PRCM] Adding model for " + proc->getProcId() );
					m_procModels.insert( std::make_pair( proc->getProcId(), proc ));
					m_idToName.insert( std::make_pair( proc->getProcId(), proc->getName() ));
					m_nameToId.insert( std::make_pair( proc->getName(), proc->getProcId() ));
					m_filenameToId.insert( std::make_pair( procFile, proc->getProcId() ));
					m_idToFilename.insert( std::make_pair( proc->getProcId(), procFile ));
                }
                else
                {
                	LOG_WARN("Discarding procedure: " + proc->getProcId());
                }
            }
            catch(SPELLcoreException& ex)
            {
                if (proc != NULL) delete proc;
                LOG_ERROR("[PRC] Unable to parse procedure: " + procFile);
            }
        }
    }

    // Add the current path to python path
    if (SPELLpythonHelper::instance().isInitialized())
    {
        SPELLpythonHelper::instance().addToPath( basePath );
    }

    std::list<std::string> subdirs = SPELLutils::getSubdirs( basePath );
    end = subdirs.end();
    for( it = subdirs.begin(); it != end; it++ )
    {
        findProcedures( basePath + PATH_SEPARATOR + (*it), sc );
    }
}

//=============================================================================
// METHOD    : SPELLprocedureManager::findLibraries
//=============================================================================
void SPELLprocedureManager::findLibraries( const std::string& basePath )
{
    if (!SPELLutils::isDirectory(basePath))
	{
    	THROW_EXCEPTION("Cannot find libraries at " + basePath, "Path not found or not a directory", SPELL_ERROR_PROCEDURES);
	}

    std::list<std::string> files = SPELLutils::getFilesInDir( basePath );
    std::list<std::string>::iterator it;
    std::list<std::string>::iterator end = files.end();

    std::string libPath = getLibPath();

    for( it = files.begin(); it != end; it++ )
    {
        if ((*it) == "__init__.py") continue;
        std::size_t idx = (*it).find_last_of(".py");
        if (idx == (*it).length()-1 )
        {
            std::string libFile = basePath + PATH_SEPARATOR + (*it);
            std::string libId = (*it).substr(0,idx-2);
            DEBUG("[PRCM] Adding library mapping for " + libId );
            // Just append the library file-id mappings
            m_filenameToId.insert( std::make_pair( libFile, libId ));
            m_idToFilename.insert( std::make_pair( libId, libFile ));

            DEBUG("[PRCM] Creating model for " + libFile );
            SPELLlibrary* lib = new SPELLlibrary( libPath, libFile );
            m_libModels.insert( std::make_pair( lib->getLibId(), lib ));
        }
    }

    std::list<std::string> subdirs = SPELLutils::getSubdirs( basePath );
    end = subdirs.end();
    for( it = subdirs.begin(); it != end; it++ )
    {
        findLibraries( basePath + PATH_SEPARATOR + (*it) );
    }
}

//=============================================================================
// METHOD    : SPELLprocedureManager::getSourceCode
//=============================================================================
SPELLprocedureSourceCode SPELLprocedureManager::getSourceCode( const std::string& procId )
{
	SPELLmonitor lock(m_lock);
    ProcModels::iterator it;
    std::string theProcId = noInstanceId(procId);
    it = m_procModels.find(theProcId);
    if (it == m_procModels.end())
	{
    	// Search for the id in the filename map
    	LibModels::iterator libit = m_libModels.find(procId);
    	if (libit == m_libModels.end())
    	{
        	THROW_EXCEPTION("Cannot get source code for " + procId, "Procedure or library not found", SPELL_ERROR_PROCEDURES);
    	}
    	SPELLlibrary* lib = libit->second;
    	lib->refresh();
    	return lib->getSourceCode();
	}
    else
    {
    	SPELLprocedure* proc = it->second;
    	proc->refresh();
    	return proc->getSourceCode();
    }
}

//=============================================================================
// METHOD    : SPELLprocedureManager::getPropertyKeys
//=============================================================================
SPELLprocedure::PropertyKeys SPELLprocedureManager::getPropertyKeys( const std::string& procId )
{
	SPELLmonitor lock(m_lock);
    ProcModels::iterator it;
    std::string theProcId = noInstanceId(procId);
    it = m_procModels.find(theProcId);
    if (it == m_procModels.end())
	{
    	THROW_EXCEPTION("Cannot get properties for " + theProcId, "Procedure not found", SPELL_ERROR_PROCEDURES);
	}
    SPELLprocedure* proc = (*it).second;
    proc->refresh();
    return proc->getPropertyKeys();
}

//=============================================================================
// METHOD    : SPELLprocedureManager::getPropertyKeys
//=============================================================================
const std::string SPELLprocedureManager::getProperty( const std::string& procId, const std::string& key )
{
	SPELLmonitor lock(m_lock);
    ProcModels::iterator it;
    std::string theProcId = noInstanceId(procId);
    it = m_procModels.find(theProcId);
    if (it == m_procModels.end())
	{
    	THROW_EXCEPTION("Cannot get properties for " + theProcId, "Procedure not found", SPELL_ERROR_PROCEDURES);
	}
    SPELLprocedure* proc = (*it).second;
    return proc->getProperty(key);
}

//=============================================================================
// METHOD    : SPELLprocedureManager::noInstanceId
//=============================================================================
const std::string SPELLprocedureManager::noInstanceId( const std::string& procId )
{
    std::string noIId = procId;
    std::size_t pos = procId.find_first_of("#");
    if (pos != std::string::npos )
    {
        noIId = procId.substr(0, pos);
    }
    return noIId;
}

//=============================================================================
// METHOD    : SPELLprocedureManager::getProcedure
//=============================================================================
SPELLprocedure& SPELLprocedureManager::getProcedure( const std::string& procId )
{
	SPELLmonitor lock(m_lock);
	std::string noIId = noInstanceId(procId);
    ProcModels::iterator it;
    it = m_procModels.find(noIId);
	if (it != m_procModels.end())
	{
		return *it->second;
	}
	THROW_EXCEPTION("Cannot obtain procedure model", "No such id: " + procId, SPELL_ERROR_PROCEDURES);
}
