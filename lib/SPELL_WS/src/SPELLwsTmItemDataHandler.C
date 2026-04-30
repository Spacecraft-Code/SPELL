// ################################################################################
// FILE       : SPELLwsTmItemDataHandler.C
// DATE       : Mar 17, 2011
// PROJECT    : SPELL
// DESCRIPTION: Implementation of the data handler for tm items
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
#include "SPELL_WS/SPELLwsTmItemDataHandler.H"
#include "SPELL_WS/SPELLwsTimeDataHandler.H"
// Project includes --------------------------------------------------------
#include "SPELL_EXC/SPELLexecutor.H"
#include "SPELL_UTIL/SPELLlog.H"
#include "SPELL_UTIL/SPELLpythonHelper.H"
#include "SPELL_WRP/SPELLregistry.H"
// System includes ---------------------------------------------------------

//=============================================================================
// CONSTRUCTOR: SPELLwsTmItemDataHandler::SPELLwsTmItemDataHandler
//=============================================================================
SPELLwsTmItemDataHandler::SPELLwsTmItemDataHandler( PyObject* object )
: SPELLwsObjectDataHandler( object )
{
	/** \todo Review the need of storing additional information */
	setCode( SPELLwsData::DATA_TM_ITEM );
}

//=============================================================================
// DESTRUCTOR: SPELLwsTmItemDataHandler::~SPELLwsTmItemDataHandler
//=============================================================================
SPELLwsTmItemDataHandler::~SPELLwsTmItemDataHandler()
{
}

//=============================================================================
// METHOD    : SPELLwsTmItemDataHandler::write()
//=============================================================================
void SPELLwsTmItemDataHandler::write()
{
	DEBUG("[TIDH] Storing telemetry item");
	PyObject* itemName = SPELLpythonHelper::instance().callMethod(getObject(),"name",NULL);
	getStorage()->storeObject(itemName);

	PyObject* itemRaw = SPELLpythonHelper::instance().callMethod(getObject(),"raw",NULL);
	getStorage()->storeObject(itemRaw);

	PyObject* itemEng = SPELLpythonHelper::instance().callMethod(getObject(),"eng",NULL);
	getStorage()->storeObject(itemEng);

	PyObject* itemSt = SPELLpythonHelper::instance().callMethod(getObject(),"status",NULL);
	getStorage()->storeObject(itemSt);

	PyObject* itemTime = SPELLpythonHelper::instance().callMethod(getObject(),"time",NULL);
	SPELLwsTimeDataHandler timeHandler(itemTime);
	timeHandler.setStorage(getStorage());
	timeHandler.write();
	DEBUG("[TIDH] Storing telemetry item done");
}

//=============================================================================
// METHOD    : SPELLwsTmItemDataHandler::read()
//=============================================================================
void SPELLwsTmItemDataHandler::read()
{
	DEBUG("[TIDH] Loading telemetry item");
	PyObject* tmClass = SPELLregistry::instance().get("TM");

	PyObject* itemName = getStorage()->loadObject();

	PyObject* item = PyObject_GetItem( tmClass, itemName );

	PyObject* itemRaw = getStorage()->loadObject();

	PyObject* itemEng = getStorage()->loadObject();

	PyObject* itemSt = getStorage()->loadObject();

	SPELLwsTimeDataHandler timeHandler(NULL);
	timeHandler.setStorage(getStorage());
	timeHandler.read();
	PyObject* itemTime = timeHandler.getObject();

	SPELLpythonHelper::instance().callMethod(item,"_setRaw", itemRaw, NULL);
	SPELLpythonHelper::instance().callMethod(item,"_setEng", itemEng, NULL);
	SPELLpythonHelper::instance().callMethod(item,"_setTime", itemTime, NULL);
	SPELLpythonHelper::instance().callMethod(item,"_setStatus", itemSt, NULL);

	setObject(item);

	DEBUG("[TIDH] Loading telemetry item done");
}

