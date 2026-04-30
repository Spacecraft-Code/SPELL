// ################################################################################
// FILE       : SPELLcodeTreeLine.C
// DATE       : Mar 17, 2011
// PROJECT    : SPELL
// DESCRIPTION: Implementation of the execution line model
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
#include "SPELL_EXC/SPELLcodeTreeLine.H"
#include "SPELL_EXC/SPELLcodeTreeNodeIF.H"



//=============================================================================
// CONSTRUCTOR    : SPELLcodeTreeLine::SPELLcodeTreeLine
//=============================================================================
SPELLcodeTreeLine::SPELLcodeTreeLine( unsigned int lineNo, SPELLcodeTreeNodeIF* parentCode )
: SPELLcodeTreeLineIF()
{
	m_lineNo = lineNo;
	m_parentCode = parentCode;
	m_childCode = NULL;
    DEBUG("[LINE] SPELLcodeTreeLine created");
}

//=============================================================================
// DESTRUCTOR    : SPELLcodeTreeLine::~SPELLcodeTreeLine
//=============================================================================
SPELLcodeTreeLine::~SPELLcodeTreeLine()
{
    DEBUG("[LINE] SPELLcodeTreeLine destroyed");
}

//=============================================================================
// METHOD    : SPELLcodeTreeLine::reset
//=============================================================================
void SPELLcodeTreeLine::reset()
{
    DEBUG("[LINE] SPELLcodeTreeLine reset");
    if (m_childCode != NULL)
    {
    	m_childCode->reset();
    	delete m_childCode;
    	m_childCode = NULL;
    }
}

