// ################################################################################
// FILE       : SPELLexecutorDefaults.C
// DATE       : Feb 13, 2014
// PROJECT    : SPELL
// DESCRIPTION: Implementation of the context execution defaults.
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
#include "SPELL_CFG/SPELLexecutorDefaults.H"
// Project includes --------------------------------------------------------
#include "SPELL_UTIL/SPELLlog.H"

// GLOBALS /////////////////////////////////////////////////////////////////


//=============================================================================
// CONSTRUCTOR: SPELLexecutorDefaults::SPELLexecutorDefaults()
//=============================================================================
SPELLexecutorDefaults::SPELLexecutorDefaults( const SPELLexecutorDefaults& from )
{
	LOG_INFO("[CTX] Copied Executor Defaults for " + m_ctxName);

	m_execDelay = from.getExecDelay();
	m_promptWarningDelay = from.getpromptWarningDelay();
	m_runInto = from.isRunInto();
	m_byStep = from.isByStep();
	m_browsableLib = from.getBrowsableLib();
	m_maxVerbosity = from.getMaxVerbosity();
	m_forceTcConfirm = from.getForceTcConfirm();
	m_saveStateMode = from.getSaveStateMode();
	m_watchVariables = from.isWatchVariables();
	m_ctxName = from.getCtxName();
} //copy constructor

