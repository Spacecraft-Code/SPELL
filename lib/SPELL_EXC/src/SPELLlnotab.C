// ################################################################################
// FILE       : SPELLlnotab.C
// DATE       : Mar 17, 2011
// PROJECT    : SPELL
// DESCRIPTION: Implementation of the LNotab analyzer
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
#include "SPELL_EXC/SPELLlnotab.H"
// Project includes --------------------------------------------------------
#include "SPELL_UTIL/SPELLlog.H"
#include "SPELL_UTIL/SPELLutils.H"



//=============================================================================
// CONSTRUCTOR    : SPELLlnotab::SPELLlnotab
//=============================================================================
SPELLlnotab::SPELLlnotab( PyCodeObject* code )
{
    DEBUG("[LNOTAB] Created");
    analyze( code );
}

//=============================================================================
// DESTRUCTOR    : SPELLlnotab::~SPELLlnotab
//=============================================================================
SPELLlnotab::~SPELLlnotab()
{
    DEBUG("[LNOTAB] Destroyed");
    m_lines.clear();
    m_addrs.clear();
}

//=============================================================================
// METHOD     : SPELLlnotab::analyze
//=============================================================================
void SPELLlnotab::analyze( PyCodeObject* code )
{
	DEBUG("[LNOTAB] Analyze: " + PYSSTR(code->co_filename));
    char*          lnotab     = NULL;
    Py_ssize_t      lnotab_len = 0;
    register int offset     = 0;
    register int line         = code->co_firstlineno;
    register int addr       = 0;

    PyString_AsStringAndSize(code->co_lnotab, &lnotab, &lnotab_len);

    DEBUG("[LNOTAB] For code " + PYSTR(code->co_name));
    for (offset = 0; offset < lnotab_len; offset += 2)
    {
        m_lines.push_back(line);
        m_addrs.push_back(addr);
        addr += ((unsigned char*) lnotab)[offset];
        line += ((unsigned char*) lnotab)[offset+1];
    }
    m_lines.push_back(line);
    m_addrs.push_back(addr);
    DEBUG("[LNOTAB] Total offsets " + ISTR(m_lines.size()));
}

//=============================================================================
// METHOD     : SPELLlnotab::lineAfter
//=============================================================================
const int SPELLlnotab::lineAfter( const int& line )
{
    std::list<int>::iterator it;
    std::list<int>::iterator end = m_lines.end();
    DEBUG("[LNOTAB] Searching line after " + ISTR(line) + ", total lines " + ISTR(m_lines.size()));
    for(it = m_lines.begin(); it != end; it++)
    {
        if (*it == line)
        {
            it++;
            if (it == end)
            {
                DEBUG("[LNOTAB] Last available line on the block: " + ISTR(line));
                return -1;
            }
            DEBUG("[LNOTAB] Next line available is " + ISTR(*it));
            return *it;
        }
    }
    // If there is no next line, return -1
    DEBUG("[LNOTAB] No next line found");
    return -1;
}

//=============================================================================
// METHOD     : SPELLlnotab::lineBefore
//=============================================================================
const int SPELLlnotab::lineBefore( const int& line )
{
    std::list<int>::iterator it;
    std::list<int>::iterator end = m_lines.end();
    for(it = m_lines.begin(); it != end; it++)
    {
        if (*it == line)
        {
            // If the line is the first one, return the same line
            if (it == m_lines.begin()) return line;
            it--;
            return *it;
        }
    }
    LOG_WARN("[LNOTAB] No line before " + ISTR(line) + " found [" + PSTR(this) + "]");
    return -1;
}

//=============================================================================
// METHOD     : SPELLlnotab::offset
//=============================================================================
const int SPELLlnotab::offset( const int& line )
{
    std::list<int>::iterator it;
    std::list<int>::iterator end = m_lines.end();
    std::list<int>::iterator ait = m_addrs.begin();
    for(it = m_lines.begin(); it != end; it++)
    {
        if (*it == line)
        {
            return *ait;
        }
        ait++;
    }
    LOG_WARN("[LNOTAB] No offset for line " + ISTR(line) + " found (" + ISTR(m_lines.size()) + ") [" + PSTR(this) + "]");
    return -1;
}

