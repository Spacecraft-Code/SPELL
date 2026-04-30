// ################################################################################
// FILE       : SPELLlibrary.C
// DATE       : Mar 17, 2011
// PROJECT    : SPELL
// DESCRIPTION: Implementation of library model
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
#include "SPELL_PRD/SPELLlibrary.H"
// Project includes --------------------------------------------------------
#include "SPELL_UTIL/SPELLlog.H"
#include "SPELL_UTIL/SPELLerror.H"

//=============================================================================
// CONSTRUCTOR : SPELLlibrary::SPELLlibrary()
//=============================================================================
SPELLlibrary::SPELLlibrary( const std::string& libPath, const std::string& filename )
: m_source(filename)
{
    // Remove the .py extension first, if any
    std::size_t pos = filename.find_last_of(".py");

    if (pos != std::string::npos )
    {
        m_libId  = filename.substr(0,pos-2);
    }

    // The remove the lib base path, not the subfolders
    m_libId = m_libId.substr( libPath.size()+1, m_libId.size()-libPath.size()-1 );
}

//=============================================================================
// DESTRUCTOR : SPELLlibrary::~SPELLlibrary
//=============================================================================
SPELLlibrary::~SPELLlibrary()
{
}
