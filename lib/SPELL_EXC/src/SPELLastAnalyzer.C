// ################################################################################
// FILE       : SPELLastAnalyzer.C
// DATE       : Mar 17, 2011
// PROJECT    : SPELL
// DESCRIPTION: Implementation of the AST analyzer
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
// Project includes --------------------------------------------------------
#include "SPELL_EXC/SPELLastAnalyzer.H"
#include "SPELL_UTIL/SPELLlog.H"
#include "SPELL_UTIL/SPELLpythonHelper.H"
// System includes ---------------------------------------------------------
#undef STR
#include "node.h"
#include "token.h"

/** \addtogroup SPELL_EXC */
/*@{*/

//============================================================================
// CONSTRUCTOR: SPELLastAnalyzer::SPELLastAnalyzer()
//============================================================================
SPELLastAnalyzer::SPELLastAnalyzer()
{
	m_lastLineType = NONE;
	m_openLineNo = 0;
	DEBUG("[ASTN] AST analyzer created");
};

//============================================================================
// DESTRUCTOR: SPELLastAnalyzer::~SPELLastAnalyzer()
//============================================================================
SPELLastAnalyzer::~SPELLastAnalyzer()
{
	DEBUG("[ASTN] AST analyzer destroyed");
};

//============================================================================
// METHOD: SPELLastAnalyzer::process()
//============================================================================
void SPELLastAnalyzer::process( const std::string& filename )
{
	DEBUG("[ASTN] Analyzing file " + filename);

	// Do not reparse the code if already done
	NodeMap::iterator it = m_nodes.find(filename);
	if (it == m_nodes.end())
	{
		NodeInfo info;

		// Read the source code
		std::string source = SPELLpythonHelper::instance().readProcedureFile(filename);

		if (source == "")
		{
			THROW_EXCEPTION("Unable to parse script", "Cannot read source code", SPELL_ERROR_PYTHON_API);
		}

		// Compile the script to obtain the AST code
		struct _node* node = PyParser_SimpleParseStringFlags( source.c_str(), Py_file_input, 0 );
		if (node == NULL) // Could not get ast
		{
			THROW_EXCEPTION("Unable to parse script", "Could not get node tree", SPELL_ERROR_PYTHON_API);
		}

		// The top node contains the main node statements
		unsigned int parDepth = 0;
		m_openLineNo = 0;
		m_lastLineType = NONE;
		for(unsigned int index = 0; index < (unsigned int) node->n_nchildren; index++)
		{
			findNodeLines( &node->n_child[index], 0, parDepth, info );
		}

		PyNode_Free(node);

		m_nodes.insert( std::make_pair(std::string(filename), info) );
		m_currentNode = info;
	}
	else
	{
		m_currentNode = it->second;
	}
}

//============================================================================
// METHOD: SPELLastAnalyzer::findNodeLines()
//============================================================================
void SPELLastAnalyzer::findNodeLines( struct _node* node, unsigned int depth, unsigned int& parDepth, NodeInfo& info )
{
	unsigned int lineno = node->n_lineno;
	LineTypes::iterator it = info.lineTypes.find(lineno);
	// If the line is new, put it in the map with no type
	if (it == info.lineTypes.end())
	{
		// The line inherits the state of the previous one
		info.lineTypes.insert( std::make_pair( lineno, m_lastLineType ));
		info.maxLineNo = lineno;
	}
	// If the line is not new and we have an open par, mark it as multiple
	else if (node->n_type == LPAR || node->n_type == LSQB || node->n_type == LBRACE )
	{
		// If it is the first open par, mark the starting line number
		if (parDepth == 0)
		{
			m_openLineNo = lineno;
		}
		// If we already are inside a list/tuple
		if (parDepth > 0)
		{
			// If the current line number matches the open line number, we
			// shall mark it as START_MULTIPLE also. The easier way is to check
			// if we are already in START_MULTIPLE type, dont change the type
			// in that case.
			if (it->second != START_MULTIPLE)
			{
				it->second = MULTIPLE;
			}
		}
		else
		{
			it->second = START_MULTIPLE;
		}
		m_lastLineType = MULTIPLE;
		parDepth++;
	}
	// If we have a close par, decrease the depth. Also, if the line of
	// the close par is the same as the open par, AND the depth is 1, the line is simple.
	// If this is the case (the line exists and its value is MULTIPLE already)
	// mark it back to NONE
	else if (node->n_type == RPAR || node->n_type == RSQB || node->n_type == RBRACE )
	{
		if (lineno == m_openLineNo)
		{
			// Mark it as simple only if the depth is 1 (and will become 0 later)
			if (parDepth == 1)
			{
				it->second = NONE;
			}
		}
		else
		{
			it->second = MULTIPLE;
		}
		// For the next line processed
		if (parDepth == 1)
		{
			m_lastLineType = NONE;
		}
		else
		{
			m_lastLineType = MULTIPLE;
		}
	}

	for(unsigned int index = 0; index < (unsigned int) node->n_nchildren; index++)
	{
		findNodeLines( &node->n_child[index], depth+1, parDepth, info );
	}

	if (node->n_type == RPAR || node->n_type == RSQB || node->n_type == RBRACE )
	{
		parDepth--;
	}
}

//============================================================================
// METHOD: SPELLastAnalyzer::isSimpleLine()
//============================================================================
bool SPELLastAnalyzer::isSimpleLine( unsigned int lineno )
{
	LineTypes::iterator it = m_currentNode.lineTypes.find(lineno);
	if (it != m_currentNode.lineTypes.end())
	{
		return (it->second == NONE);
	}
	return true;
}

//============================================================================
// METHOD: SPELLastAnalyzer::isBlockStart()
//============================================================================
bool SPELLastAnalyzer::isBlockStart( unsigned int lineno )
{
	LineTypes::iterator it = m_currentNode.lineTypes.find(lineno);
	if (it != m_currentNode.lineTypes.end())
	{
		return (it->second == START_MULTIPLE);
	}
	return false;
}

//============================================================================
// METHOD: SPELLastAnalyzer::isInsideBlock()
//============================================================================
bool SPELLastAnalyzer::isInsideBlock( unsigned int lineno )
{
	LineTypes::iterator it = m_currentNode.lineTypes.find(lineno);
	if (it != m_currentNode.lineTypes.end())
	{
		return (it->second == MULTIPLE);
	}
	return false;
}

//============================================================================
// METHOD: SPELLastAnalyzer::getBlockEnd()
//============================================================================
unsigned int SPELLastAnalyzer::getBlockEnd( unsigned int startLineNo )
{
	unsigned int firstAfterBlock = startLineNo;
	for(unsigned int count = startLineNo+1; count < m_currentNode.maxLineNo ; count++)
	{
		LineTypes::iterator it = m_currentNode.lineTypes.find(count);
		if (it != m_currentNode.lineTypes.end())
		{
			if ((it->second == NONE)||(it->second == START_MULTIPLE))
			{
				firstAfterBlock = it->first;
				break;
			}
		}
	}
	return firstAfterBlock;
}
