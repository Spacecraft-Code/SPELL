// ################################################################################
// FILE       : SPELLregistry.C
// DATE       : Mar 17, 2011
// PROJECT    : SPELL
// DESCRIPTION: Implementation of the registry wrapper
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
#include "SPELL_UTIL/SPELLutils.H"
#include "SPELL_UTIL/SPELLpythonHelper.H"
#include "SPELL_UTIL/SPELLpythonMonitors.H"
#include "SPELL_WRP/SPELLregistry.H"
#include "SPELL_WRP/SPELLpyHandle.H"
// Project includes --------------------------------------------------------



SPELLregistry* SPELLregistry::s_instance = 0;

//=============================================================================
// CONSTRUCTOR : SPELLregistry::SPELLregistry()
//=============================================================================
SPELLregistry::SPELLregistry()
{
    m_registry = 0;
}

//=============================================================================
// DESTRUCTOR : SPELLregistry::~SPELLregistry
//=============================================================================
SPELLregistry::~SPELLregistry()
{
}

//=============================================================================
// METHOD    : SPELLregistry::instance()
//=============================================================================
SPELLregistry& SPELLregistry::instance()
{
    if (s_instance == NULL)
    {
        s_instance = new SPELLregistry();
    }
    return *s_instance;
}

//=============================================================================
// METHOD    : SPELLregistry::getRegistryObject
//=============================================================================
PyObject* SPELLregistry::getRegistryObject()
{
    if (m_registry == 0)
    {
    	m_registry = SPELLpythonHelper::instance().getObject("spell.lib.registry", "REGISTRY");
        Py_INCREF( m_registry );
    }
    return m_registry;
}

//=============================================================================
// METHOD    : SPELLregistry::get
//=============================================================================
PyObject* SPELLregistry::get( std::string key )
{
	SPELLsafePythonOperations ops("SPELLregistry::get(" + key + ")");
    PyObject* registry = getRegistryObject();
    SPELLpyHandle pyKey = SSTRPY(key);
    return PyObject_GetItem( registry, pyKey.get());
}

//=============================================================================
// METHOD    : SPELLregistry::set
//=============================================================================
void SPELLregistry::set( PyObject* obj, std::string key )
{
	SPELLsafePythonOperations ops("SPELLregistry::set(" + key + ")");
    PyObject* registry = getRegistryObject();
    PyObject_SetItem( registry, SSTRPY(key), obj );
}
