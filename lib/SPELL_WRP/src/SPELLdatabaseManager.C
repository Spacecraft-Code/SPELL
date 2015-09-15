// ################################################################################
// FILE       : SPELLdatabaseManager.C
// DATE       : Mar 17, 2011
// PROJECT    : SPELL
// DESCRIPTION: Implementation of the database manager wrapper
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
#include "SPELL_WRP/SPELLdatabaseManager.H"
#include "SPELL_UTIL/SPELLpythonHelper.H"
#include "SPELL_UTIL/SPELLpythonMonitors.H"
#include "SPELL_UTIL/SPELLutils.H"
// Project includes --------------------------------------------------------
#include "SPELL_CFG/SPELLconfiguration.H"



SPELLdatabaseManager* SPELLdatabaseManager::s_instance = 0;

//=============================================================================
// CONSTRUCTOR : SPELLdatabaseManager::SPELLdatabaseManager()
//=============================================================================
SPELLdatabaseManager::SPELLdatabaseManager()
{
    m_dbManager = 0;
}

//=============================================================================
// DESTRUCTOR : SPELLdatabaseManager::~SPELLdatabaseManager
//=============================================================================
SPELLdatabaseManager::~SPELLdatabaseManager()
{
}

//=============================================================================
// METHOD    : SPELLdatabaseManager::instance()
//=============================================================================
SPELLdatabaseManager& SPELLdatabaseManager::instance()
{
    if (s_instance == NULL)
    {
        s_instance = new SPELLdatabaseManager();
    }
    return *s_instance;
}

//=============================================================================
// METHOD    : SPELLdatabaseManager::getDatabaseManagerObject
//=============================================================================
PyObject* SPELLdatabaseManager::getDatabaseManagerObject()
{
    if (m_dbManager == 0)
    {
    	m_dbManager = SPELLpythonHelper::instance().getObject("spell.lib.adapter.dbmgr", "DBMGR");
    	Py_XINCREF(m_dbManager);
    }
    return m_dbManager;
}

//=============================================================================
// METHOD    : SPELLdatabaseManager::loadBuiltinDatabases
//=============================================================================
void SPELLdatabaseManager::loadBuiltinDatabases()
{
	SPELLsafePythonOperations ops("SPELLdatabaseManager::loadBuiltinDatabases()");
	PyObject* mgr = getDatabaseManagerObject();
	// New reference
	PyObject* scdb = SPELLpythonHelper::instance().callMethod( mgr, "loadDatabase", STRPY("SCDB"), NULL);
	SPELLpythonHelper::instance().checkError();
	// New reference
	PyObject* gdb = SPELLpythonHelper::instance().callMethod( mgr, "loadDatabase", STRPY("GDB"), NULL );
	SPELLpythonHelper::instance().checkError();
	SPELLpythonHelper::instance().install( scdb, "SCDB" );
	SPELLpythonHelper::instance().install( gdb,  "GDB" );
	SPELLpythonHelper::instance().checkError();
}
