// ################################################################################
// FILE       : SPELLbreakpoint.C
// DATE       : Mar 17, 2011
// PROJECT    : SPELL
// DESCRIPTION: Implementation of the breakpoint manager
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
#include "SPELL_EXC/SPELLbreakpoint.H"
#include "SPELL_EXC/SPELLbreakpointType.H"
// Project includes --------------------------------------------------------
#include "SPELL_UTIL/SPELLerror.H"
#include "SPELL_UTIL/SPELLlog.H"

// GLOBALS ////////////////////////////////////////////////////////////////////

//=============================================================================
// CONSTRUCTOR : SPELLbreakpoint::SPELLbreakpoint
//=============================================================================
SPELLbreakpoint::SPELLbreakpoint()
{
}

//=============================================================================
// DESTRUCTOR : SPELLbreakpoint::~SPELLbreakpoint
//=============================================================================
SPELLbreakpoint::~SPELLbreakpoint()
{
}

//=============================================================================
// METHOD    : SPELLbreakpoint::setBreakpoint()
//=============================================================================
bool SPELLbreakpoint::setBreakpoint( const std::string& file,
		                             unsigned int line,
		                             const SPELLbreakpointType type )
{
	if (type == UNKNOWN)
	{
		removeBreakpoint(file,line);
		return false;
	}
	else
	{
		addBreakpoint(file,line,type);
		return true;
	}
}

//=============================================================================
// METHOD    : SPELLbreakpoint::checkBreakpoint()
//=============================================================================
bool SPELLbreakpoint::checkBreakpoint( const std::string& file, unsigned int line )
{
	/*
     * PERMANENT BREAKPOINTS LOOKUP
     */
	BreakpointMap::iterator it = m_breakpoints.find(file);
	if ( it != m_breakpoints.end())
	{
		// Find the line number and remove it
		BreakpointList::const_iterator lit;
		for( lit = it->second.begin(); lit != it->second.end(); lit++)
		{
			if (*lit == line)
			{
				DEBUG("[BP] Found permanent breakpoint at " + file + ":" + ISTR(line));
				return true;
			}
		}
	}

    /*
     * TEMPORARY BREAKPOINTS LOOKUP
     */
	it = m_tempBreakpoints.find(file);
	if ( it != m_tempBreakpoints.end())
	{
		// Find the line number and remove it
		BreakpointList::const_iterator lit;
		for( lit = it->second.begin(); lit != it->second.end(); lit++)
		{
			if (*lit == line)
			{
				DEBUG("[BP] Found temporary breakpoint at " + file + ":" + ISTR(line));
				// Delete the temporary breakpoint
				m_tempBreakpoints.erase(it);
				return true;
			}
		}
	}

	return false;
}

//=============================================================================
// METHOD    : SPELLbreakpoint::addBreakpoint()
//=============================================================================
void SPELLbreakpoint::addBreakpoint( const std::string& file,
                                     unsigned int line,
                                     const SPELLbreakpointType type )
{
	// Even if the file entry does not exist yet, it will create it and add the line
	if (type == TEMPORARY)
 	{
		DEBUG("[BP] Set temporary breakpoint at " + file + ":" + ISTR(line) );
		m_tempBreakpoints[file].push_back(line);
    }
    else if (type == PERMANENT)
	{
		DEBUG("[BP] Set permanent breakpoint at " + file + ":" + ISTR(line) );
		m_breakpoints[file].push_back(line);
	}
}

//=============================================================================
// METHOD    : SPELLbreakpoint::removeBreakpoint()
//=============================================================================
void SPELLbreakpoint::removeBreakpoint( const std::string& file, unsigned int line )
{
	/*
     * REMOVE FROM PERMANENT BREAKPOINTS
     */
	BreakpointMap::iterator it = m_breakpoints.find(file);
	if ( it != m_breakpoints.end())
	{
		// Find the line number and remove it
		BreakpointList::iterator lit;
		for( lit = it->second.begin(); lit != it->second.end(); lit++)
		{
			if (*lit == line)
			{
				DEBUG("[BP] Remove breakpoint at " + file + ":" + ISTR(line) );
				it->second.erase(lit);
				return;
			}
		}
	}
	/*
     * TEMPORARY BREAKPOINTS ARE NOT REMOVED, AS THEY WILL BE REMOVED ONCE
     * THEY ARE REACHED
     */
}

//=============================================================================
// METHOD    : SPELLbreakpoint::clearBreakpoints()
//=============================================================================
void SPELLbreakpoint::clearBreakpoints()
{
	m_breakpoints.clear();
        /** TEMPORARY BREAKPOINTS ARE NOT CLEARED */
}
