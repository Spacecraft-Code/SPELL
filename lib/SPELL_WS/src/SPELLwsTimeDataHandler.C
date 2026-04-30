// ################################################################################
// FILE       : SPELLwsTimeDataHandler.C
// DATE       : Mar 17, 2011
// PROJECT    : SPELL
// DESCRIPTION: Implementation of the data handler for SPELL TIME
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
#include "SPELL_WS/SPELLwsTimeDataHandler.H"
// Project includes --------------------------------------------------------
#include "SPELL_UTIL/SPELLlog.H"
#include "SPELL_UTIL/SPELLpythonHelper.H"
// System includes ---------------------------------------------------------


//=============================================================================
// CONSTRUCTOR: SPELLwsTimeDataHandler::SPELLwsTimeDataHandler
//=============================================================================
SPELLwsTimeDataHandler::SPELLwsTimeDataHandler( PyObject* object )
: SPELLwsObjectDataHandler( object )
{
	setCode( SPELLwsData::DATA_SPELL_TIME );
}

//=============================================================================
// DESTRUCTOR: SPELLwsTimeDataHandler::~SPELLwsTimeDataHandler
//=============================================================================
SPELLwsTimeDataHandler::~SPELLwsTimeDataHandler()
{
}

//=============================================================================
// METHOD    : SPELLwsTimeDataHandler::write()
//=============================================================================
void SPELLwsTimeDataHandler::write()
{
	if (getObject() == NULL)
	{
		getStorage()->storeLong( -1 );
		return;
	}

	SPELLtime theTime = SPELLpythonHelper::instance().evalTime( getObject() );

	DEBUG("[TIMEDH] Storing time value: " + theTime.toTIMEString() );

	DEBUG("[TIMEDH] Storing seconds from epoch");
	// Store the seconds
	getStorage()->storeLong( theTime.getSeconds() );
	// Store the milliseconds
	getStorage()->storeLong( theTime.getMilliseconds() );
	// Store delta flag
	getStorage()->storeLong( theTime.isDelta() ? 1 : 0 );

	DEBUG("[TIMEDH] Storing time done");
}

//=============================================================================
// METHOD    : SPELLwsTimeDataHandler::read()
//=============================================================================
void SPELLwsTimeDataHandler::read()
{
	// Load the number of seconds
	int seconds = getStorage()->loadLong();
	// Load number of milliseconds
	int msec = getStorage()->loadLong();
	// Load delta flag
	int delta = getStorage()->loadLong();

	SPELLtime theTime;
	theTime.set( seconds, msec, (delta == 1) );

	DEBUG("[TIMEDH] Loaded time: " + theTime.toTIMEString() );

	// CREATE TIME
	PyObject* timeObject = SPELLpythonHelper::instance().pythonTime(theTime);

	if (timeObject == NULL)
	{
		THROW_EXCEPTION("Cannot load TIME", "Unable to create instance", SPELL_ERROR_WSTART);
	}

	setObject(timeObject);
	DEBUG("[TIMEDH] Load database items");
}
