// ################################################################################
// FILE       : SPELLdriverManager.C
// DATE       : Mar 17, 2011
// PROJECT    : SPELL
// DESCRIPTION: Implementation of the driver manager wrapper
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
#include "SPELL_WRP/SPELLdriverManager.H"
#include "SPELL_UTIL/SPELLpythonHelper.H"
#include "SPELL_UTIL/SPELLpythonMonitors.H"
#include "SPELL_UTIL/SPELLutils.H"
// Project includes --------------------------------------------------------
#include "SPELL_CFG/SPELLconfiguration.H"



SPELLdriverManager* SPELLdriverManager::s_instance = 0;

//=============================================================================
// CONSTRUCTOR : SPELLdriverManager::SPELLdriverManager()
//=============================================================================
SPELLdriverManager::SPELLdriverManager()
{
    m_dManager = 0;
}

//=============================================================================
// DESTRUCTOR : SPELLdriverManager::~SPELLdriverManager
//=============================================================================
SPELLdriverManager::~SPELLdriverManager()
{
}

//=============================================================================
// METHOD    : SPELLdriverManager::instance()
//=============================================================================
SPELLdriverManager& SPELLdriverManager::instance()
{
    if (s_instance == NULL)
    {
        s_instance = new SPELLdriverManager();
    }
    return *s_instance;
}

//=============================================================================
// METHOD    : SPELLdriverManager::getDriverManagerObject
//=============================================================================
PyObject* SPELLdriverManager::getDriverManagerObject()
{
    if (m_dManager == 0)
    {
    	// Borrowed
        PyObject* classObj = SPELLpythonHelper::instance().getObject("spell.lib.drivermgr", "DriverManager");
        m_dManager = SPELLpythonHelper::instance().callMethod( classObj, "instance", NULL );
        Py_XINCREF(m_dManager);
    }
    return m_dManager;
}

//=============================================================================
// METHOD    : SPELLdriverManager::setup
//=============================================================================
void SPELLdriverManager::setup( const std::string& ctxName, const std::string& interfaces )
{
	SPELLsafePythonOperations ops("SPELLdriverManager::setup()");
    PyObject* pmgr = getDriverManagerObject();
    if (interfaces.empty())
    {
        SPELLpythonHelper::instance().callMethod( pmgr, "setup", SSTRPY(ctxName), NULL);
    }
    else
    {
        SPELLpythonHelper::instance().callMethod( pmgr, "setup", SSTRPY(ctxName), SSTRPY(interfaces), NULL);
    }
    SPELLpythonHelper::instance().checkError();
}

//=============================================================================
// METHOD    : SPELLdriverManager::cleanup
//=============================================================================
void SPELLdriverManager::cleanup( bool shutdown )
{
	SPELLsafePythonOperations ops("SPELLdriverManager::cleanup()");
    PyObject* pmgr = getDriverManagerObject();
    if (shutdown)
    {
        SPELLpythonHelper::instance().callMethod( pmgr, "cleanup", Py_False, Py_True, NULL );
    }
    else
    {
        SPELLpythonHelper::instance().callMethod( pmgr, "cleanup", NULL);
    }
    SPELLpythonHelper::instance().checkError();
}

//=============================================================================
// METHOD    : SPELLdriverManager::onCommand
//=============================================================================
void SPELLdriverManager::onCommand( const std::string& commandId )
{
    PyObject* pmgr = getDriverManagerObject();
    PyObject* cmd = SSTRPY(commandId);
    Py_INCREF(cmd);
    try
    {
		SPELLpythonHelper::instance().callMethod( pmgr, "onCommand", cmd, NULL );
		SPELLpythonHelper::instance().checkError();
    }
    catch(SPELLcoreException& ex)
    {
    	LOG_ERROR("OnCommand failed: " + ex.what());
    }
}
