// ################################################################################
// FILE       : SPELLcodeTreeNode.C
// DATE       : Mar 17, 2011
// PROJECT    : SPELL
// DESCRIPTION: Implementation of the execution node model
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
#include "SPELL_EXC/SPELLcodeTreeNode.H"
#include "SPELL_EXC/SPELLcodeTreeLine.H"



//=============================================================================
// CONSTRUCTOR    : SPELLcodeTreeNode::SPELLcodeTreeNode
//=============================================================================
SPELLcodeTreeNode::SPELLcodeTreeNode( unsigned int depth, const std::string& codeId, unsigned int firstLine, SPELLcodeTreeLineIF* parentLine )
: SPELLcodeTreeNodeIF()
{
	m_depth = depth;
	m_parentLine = parentLine;
	eventLine(firstLine);
    m_codeId = codeId;
    DEBUG("[NODE] SPELLcodeTreeNode created");
}

//=============================================================================
// DESTRUCTOR    : SPELLcodeTreeNode::~SPELLcodeTreeNode
//=============================================================================
SPELLcodeTreeNode::~SPELLcodeTreeNode()
{
    DEBUG("[NODE] SPELLcodeTreeNode destroyed");
}

//=============================================================================
// METHOD    : SPELLcodeTreeNode::reset
//=============================================================================
void SPELLcodeTreeNode::reset()
{
    DEBUG("[NODE] SPELLcodeTreeNode reset");
    m_currentLine = 0;
    SPELLcodeTreeLineIF::Map::iterator it;
    for( it = m_lines.begin(); it != m_lines.end(); it++)
    {
    	(it->second)->reset();
    	delete (it->second);
    }
    m_lines.clear();
}

//=============================================================================
// METHOD    : SPELLcodeTreeNode::getLine()
//=============================================================================
SPELLcodeTreeLineIF* SPELLcodeTreeNode::getLine( unsigned int lineNo ) const
{
	SPELLcodeTreeLineIF::Map::const_iterator it = m_lines.find(lineNo);
	if (it == m_lines.end()) return NULL;
	return it->second;
}

//=============================================================================
// METHOD    : SPELLcodeTreeNode::eventCall()
//=============================================================================
void SPELLcodeTreeNode::eventCall( const std::string& codeId, unsigned int lineNo )
{
	SPELLcodeTreeLineIF* currentLine = getCurrentLine();
	SPELLcodeTreeNodeIF* node = new SPELLcodeTreeNode(m_depth+1, codeId, lineNo, currentLine);
	currentLine->addChildCode(node);
}

//=============================================================================
// METHOD    : SPELLcodeTreeNode::eventLine()
//=============================================================================
void SPELLcodeTreeNode::eventLine( unsigned int lineNo )
{
	m_currentLine = lineNo;
	SPELLcodeTreeLineIF::Map::iterator it = m_lines.find(lineNo);
	if ( it == m_lines.end() )
	{
	    DEBUG("[NODE] Creating line " + ISTR(lineNo) + " on code " + m_codeId);
	    SPELLcodeTreeLineIF* line = new SPELLcodeTreeLine(lineNo, this);
		m_lines.insert( std::make_pair( lineNo, line ));
	}
}

//=============================================================================
// METHOD    : SPELLcodeTreeNode::getDepth()
//=============================================================================
unsigned int SPELLcodeTreeNode::getDepth()
{
	return m_depth;
}
