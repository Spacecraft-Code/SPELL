// ################################################################################
// FILE       : SPELLimportChecker.C
// DATE       : Mar 17, 2011
// PROJECT    : SPELL
// DESCRIPTION: Implementation of the import detector
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
#include "SPELL_EXC/SPELLimportChecker.H"
// Project includes --------------------------------------------------------
#include "SPELL_UTIL/SPELLutils.H"

// GLOBALS /////////////////////////////////////////////////////////////////

//=============================================================================
// CONSTRUCTOR:  SPELLimportChecker::SPELLimportChecker
//=============================================================================
SPELLimportChecker::SPELLimportChecker()
{
    m_importing = false;
    m_mainProc = "";
    m_currentProc = "";
}

//=============================================================================
// METHOD    : SPELLimportChecker::isImporting
//=============================================================================
const bool SPELLimportChecker::isImporting( const int& event,
        const std::string& file,
        const int& line,
        const std::string& name )
{
    if (event == PyTrace_LINE)
    {
        // Do not process imports, but take into account when the import process
        // finishes
        if (m_importing && (file == m_currentProc)) m_importing = false;
        if (!m_importing) m_currentProc = file;
    }
    else if (event == PyTrace_CALL)
    {
        if (m_mainProc == "")
        {
            m_mainProc = file;
            m_currentProc = file;
        }
        if ((name == "<module>") && (file != m_currentProc)) m_importing = true;
    }
    return m_importing;
}

//=============================================================================
// CONSTRUCTOR:  SPELLimportChecker::reset
//=============================================================================
void SPELLimportChecker::reset()
{
    m_importing = false;
    m_mainProc = "";
    m_currentProc = "";
}
