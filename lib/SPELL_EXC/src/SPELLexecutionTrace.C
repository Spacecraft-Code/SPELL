// ################################################################################
// FILE       : SPELLexecutionTrace.C
// DATE       : Mar 17, 2011
// PROJECT    : SPELL
// DESCRIPTION: Implementation of the execution trace model
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
// Project includes --------------------------------------------------------
#include "SPELL_UTIL/SPELLlog.H"
#include "SPELL_UTIL/SPELLerror.H"
// Local includes ----------------------------------------------------------
#include "SPELL_EXC/SPELLexecutionTrace.H"



//=============================================================================
// CONSTRUCTOR    : SPELLexecutionTrace::SPELLexecutionTrace
//=============================================================================
SPELLexecutionTrace::SPELLexecutionTrace()
{
    reset();
}

//=============================================================================
// DESTRUCTOR    : SPELLexecutionTrace::~SPELLexecutionTrace
//=============================================================================
SPELLexecutionTrace::~SPELLexecutionTrace()
{
}

//=============================================================================
// METHOD    : SPELLexecutionTrace::reset()
//=============================================================================
void SPELLexecutionTrace::reset()
{
    m_currentLine = 0;
    m_visits.clear();
    m_executions.clear();
}

//=============================================================================
// METHOD    : SPELLexecutionTrace::setCurrentLine()
//=============================================================================
void SPELLexecutionTrace::setCurrentLine( unsigned int lineNo )
{
	m_currentLine = lineNo;
	// Do not increment the number of executions, but the number of visits
	LineMap::iterator it = m_visits.find(lineNo);
	if (it == m_visits.end())
	{
		m_visits.insert( std::make_pair( lineNo, 1) );
	}
	else
	{
		it->second++;
	}
	// Nevertheless, create the entry in the executions counter if needed.
	it = m_executions.find(lineNo);
	if (it == m_executions.end())
	{
		m_executions.insert( std::make_pair( lineNo, 0) );
	}
}

//=============================================================================
// METHOD    : SPELLexecutionTrace::markExecuted()
//=============================================================================
void SPELLexecutionTrace::markExecuted()
{
	LineMap::iterator it = m_executions.find(m_currentLine);
	it->second++;
}

//=============================================================================
// METHOD    : SPELLexecutionTrace::getNumVisits()
//=============================================================================
unsigned int SPELLexecutionTrace::getNumVisits( unsigned int lineNo ) const
{
	LineMap::const_iterator it = m_visits.find(lineNo);
	if (it == m_visits.end()) return 0;
	return it->second;
}

//=============================================================================
// METHOD    : SPELLexecutionTrace::getNumExecutions()
//=============================================================================
unsigned int SPELLexecutionTrace::getNumExecutions( unsigned int lineNo ) const
{
	LineMap::const_iterator it = m_executions.find(lineNo);
	if (it == m_executions.end()) return 0;
	return it->second;
}

