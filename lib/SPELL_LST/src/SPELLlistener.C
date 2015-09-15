// ################################################################################
// FILE       : SPELLlistener.C
// DATE       : Jul 05, 2011
// PROJECT    : SPELL
// DESCRIPTION: 
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
// Local includes ----------------------------------------------------------
// Project includes --------------------------------------------------------
#include "SPELL_LST/SPELLlistener.H"
#include "SPELL_LST/SPELLlistenerGui.H"
#include "SPELL_UTIL/SPELLlog.H"
#include "SPELL_UTIL/SPELLerror.H"
#include "SPELL_CFG/SPELLconfiguration.H"
// System includes ---------------------------------------------------------

SPELLlistener::SPELLlistener()
{
	m_guiListener = NULL;
	m_contextListener = NULL;
}

SPELLlistener::~SPELLlistener()
{
	if (m_guiListener) delete m_guiListener;
	if (m_contextListener) delete m_contextListener;
}

void SPELLlistener::start( const std::string& configFile )
{
	LOG_INFO("Listener start");

    SPELLconfiguration::instance().loadConfig(configFile);

    // Configure the time format if defined in configuration
    std::string format = SPELLconfiguration::instance().getCommonParameter("TdsTimeFormat");
    if (format.length()!=0)
    {
    	if (format.compare ("1") == 0)
    	{
    		SPELLutils::setTimeFormat(TIME_FORMAT_SLASH);
    	}
    	else if (format.compare("0")==0)
    	{
    		SPELLutils::setTimeFormat(TIME_FORMAT_DOT);
    	}
    }

	m_guiListener = new SPELLlistenerGui( configFile );
	m_contextListener = new SPELLlistenerContext( configFile );

	m_guiListener->startup(m_contextListener, NULL);
	m_contextListener->startup(m_guiListener, NULL);
}

void SPELLlistener::stop()
{
    m_guiListener->shutdown();
    m_contextListener->shutdown();
}
