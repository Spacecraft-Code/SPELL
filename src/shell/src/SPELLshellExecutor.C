// ################################################################################
// FILE       : SPELLshellExecutor.C
// DATE       : Mar 18, 2011
// PROJECT    : SPELL
// DESCRIPTION: Fake executor model for shell
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
#include "SPELLshellExecutor.H"
// Project includes --------------------------------------------------------
#include "SPELL_EXC/SPELLexecutorIF.H"
// System includes ---------------------------------------------------------


// FORWARD REFERENCES //////////////////////////////////////////////////////
// TYPES ///////////////////////////////////////////////////////////////////
// DEFINES /////////////////////////////////////////////////////////////////

//============================================================================
// CONSTRUCTOR:	SPELLshellExecutor()
//============================================================================
SPELLshellExecutor::SPELLshellExecutor()
: SPELLexecutorIF()
{
	m_cif = NULL;
	m_controller = NULL;
	m_scheduler = NULL;
	m_callstack = NULL;
	m_childMgr = NULL;
	m_frameManager = NULL;
	m_varManager = NULL;
}

//============================================================================
// DESTRUCTOR:	~SPELLshellExecutor()
//============================================================================
SPELLshellExecutor::~SPELLshellExecutor()
{

}

//============================================================================
// METHOD:	SPELLexecutor::initialize()
//============================================================================
void SPELLshellExecutor::initialize( SPELLcif* cif,
						 SPELLcontrollerIF* controller,
						 SPELLschedulerIF* scheduler,
						 SPELLcallstackIF* callstack,
						 SPELLframeManager* frameManager )
{
	m_cif = cif;
	m_controller = controller;
	m_scheduler = scheduler;
	m_callstack = callstack;
}
