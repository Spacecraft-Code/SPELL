// ################################################################################
// FILE       : SPELLcallstack.C
// DATE       : Mar 17, 2011
// PROJECT    : SPELL
// DESCRIPTION: Implementation of the callstack model
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
// Project includes --------------------------------------------------------
#include "SPELL_UTIL/SPELLlog.H"
#include "SPELL_UTIL/SPELLerror.H"
#include "SPELL_WRP/SPELLconstants.H"
#include "SPELL_PRD/SPELLprocedureManager.H"
#include "SPELL_IPC/SPELLipc.H"
#include "SPELL_SYN/SPELLmonitor.H"
// Local includes ----------------------------------------------------------
#include "SPELL_EXC/SPELLexecutor.H"
#include "SPELL_EXC/SPELLcallstack.H"
#include "SPELL_EXC/SPELLcodeTreeNode.H"



//=============================================================================
// CONSTRUCTOR    : SPELLcallstack::SPELLcallstack
//=============================================================================
SPELLcallstack::SPELLcallstack()
: SPELLcallstackIF()
{
	m_rootNode = NULL;
	m_currentNode = NULL;
	m_viewNode = NULL;
    m_previousSoMode = SO_ALWAYS_INTO;
    m_soMode = SO_ALWAYS_INTO;
    m_notify = true;
    m_errorState = false;
    reset();
    DEBUG("[CSTACK] SPELLcallstack created");
}

//=============================================================================
// DESTRUCTOR    : SPELLcallstack::~SPELLcallstack
//=============================================================================
SPELLcallstack::~SPELLcallstack()
{
	clearStack();
    DEBUG("[CSTACK] SPELLcallstack destroyed");
}

//=============================================================================
// METHOD    : SPELLcallstack::reset
//=============================================================================
void SPELLcallstack::reset()
{
	m_stack = "";
	m_recoveryMode = false;
	m_fullStack.clear();
	m_fullStackString = "";
    m_stageId = "";
    m_stageTitle = "";
    m_codeName = "";
    m_markExecuted = false;
    m_notify = true;
    m_errorState = false;
    m_currentLine = 0;
    clearStack();
    DEBUG("[CSTACK] SPELLcallstack reset");
}

//=============================================================================
// METHOD    : SPELLcallstack::getStack()
//=============================================================================
const std::string& SPELLcallstack::getStack()
{
	SPELLmonitor mon(m_lock);

	return m_stack;
}

//=============================================================================
// METHOD    : SPELLcallstack::getFullStack()
//=============================================================================
const std::string SPELLcallstack::getFullStack()
{
	SPELLmonitor mon(m_lock);
	return m_fullStackString;
}

//=============================================================================
// METHOD    : SPELLcallstack::getCodeName()
//=============================================================================
const std::string& SPELLcallstack::getCodeName()
{
	SPELLmonitor mon(m_lock);

	return m_codeName;
}

//=============================================================================
// METHOD    : SPELLcallstack::setStage
//=============================================================================
void SPELLcallstack::setStage( const std::string& id, const std::string& title )
{
	SPELLmonitor mon(m_lock);

    m_stageId = id;
    m_stageTitle = title;
}

//=============================================================================
// METHOD    : SPELLcallstack::getStage
//=============================================================================
const std::string SPELLcallstack::getStage()
{
	SPELLmonitor mon(m_lock);

    return m_stageId + ":" + m_stageTitle;
}

//=============================================================================
// METHOD    : SPELLcallstack::isSteppingOver
//=============================================================================
const bool SPELLcallstack::isSteppingOver()
{
	SPELLmonitor mon(m_lock);

	if (m_viewNode == NULL || m_currentNode == NULL) return false;
	return (m_viewNode->getDepth() < m_currentNode->getDepth());
}

//=============================================================================
// METHOD    : SPELLcallstack::moveTolevel()
//=============================================================================
void SPELLcallstack::moveToLevel( unsigned int level )
{
	SPELLmonitor mon(m_lock);
	unsigned int theLevel = 0;
	SPELLcodeTreeNodeIF* theNode = m_rootNode;
	DEBUG("[CSTACK] Move to level " + ISTR(level) );
	while( theLevel != level )
	{
		theNode = theNode->getCurrentLine()->getChildCode();
		theLevel++;
	}
	m_viewNode = theNode;
}

//=============================================================================
// METHOD    : SPELLcallstack::callbackEventLine
//=============================================================================
void SPELLcallstack::callbackEventLine( PyFrameObject* frame, const std::string& file, const int line, const std::string& name )
{
    DEBUG("[CSTACK] Event line: " + file + ":" + ISTR(line) + ", mode=" + ISTR(m_soMode) + ", so=" + BSTR(isSteppingOver()));

    // Store current line
    m_currentLine = line;

    // If the mark executed flag is set, before changing the current line to the new onee we need to
    // mark the old one as executed. The flag will not be set in case of skip or gotos.
    if (m_markExecuted)
    {
    	// Mark the previous line as executed.
		DEBUG("[CSTACK] Previous line executed");
    	SPELLexecutor::instance().getFrameManager().getCurrentTraceModel().markExecuted();
    }
    else
    {
    	// The flag is always kept to value true, unless there is a skip or goto. In that
    	// case, the flag is disabled, so that the line is not notified as executed in this
    	// method, and then the flag is reset to true here so that the next line visited
    	// will be (by default) marked as executed once left.
    	m_markExecuted = true;
    	DEBUG("[CSTACK] Previous line NOT executed");
    }

    // Visit the line
	SPELLexecutor::instance().getFrameManager().getTraceModel(file).setCurrentLine(line);
    // Add a new line for this event
    m_currentNode->eventLine(line);
    // Update the stack string
    m_stack = std::string(file + ":" + ISTR(line));
    m_fullStack[m_fullStack.size()-1] = m_stack;
    rebuildFullStack();

    // Update the code name string
    m_codeName = name;

    if (m_notify)
	{
    	DEBUG("[CSTACK] Event line notify: " + getFullStack() + ", mode=" + ISTR(m_soMode) + ", so=" + BSTR(isSteppingOver()));
    	SPELLexecutor::instance().getCIF().notifyLine();
	}

    if (m_errorState)
    {
        DEBUG("[CSTACK] Reset error mode");
        m_errorState = false;
    }

}

//=============================================================================
// METHOD    : SPELLcallstack::callbackEventCall
//=============================================================================
void SPELLcallstack::callbackEventCall( PyFrameObject* frame, const std::string& file, const int line, const std::string& name )
{
	// Call to a function, increase the stack
	DEBUG("[CSTACK] Event call: " + file + ":" + ISTR(line) + ", mode=" + ISTR(m_soMode) + ", so=" + BSTR(isSteppingOver()));

    // Store current line
    m_currentLine = line;

	// Consider first the first call in the execution: create the roots
	if (m_rootNode == NULL)
	{
		DEBUG("[CSTACK] First call" );
		m_rootNode = new SPELLcodeTreeNode(0,file,line,NULL);
		m_currentNode = m_rootNode;
		m_viewNode = m_rootNode;

	    // Visit the line
		SPELLexecutor::instance().getFrameManager().getCurrentTraceModel().setCurrentLine(line);
	}
	// If we are in this case, there is a function being called
	else if (!m_recoveryMode)
	{
		assert(m_currentNode != NULL);

		DEBUG("[CSTACK] Function call" );
		// ...and ensure the next line will be marked by default
		m_markExecuted = true;
		// Will add the child code corresponding to the call

		DEBUG("[CSTACK] Doing call on node " + m_currentNode->getCodeIdentifier() + ", depth " + ISTR(m_currentNode->getDepth()) );
		m_currentNode->eventCall(file,line);
		// Now update which the current node is
		m_currentNode = m_currentNode->getCurrentLine()->getChildCode();
		// If we are stepping over, do not change the view node position
		// otherwise move the view node together with the leaf
		if ( m_soMode >= SO_ONCE_INTO )
		{
			DEBUG("[CSTACK] Increasing level: " + ISTR(m_soMode) );
			m_viewNode = m_currentNode;
		}
	}

	if (!m_recoveryMode)
	{
	    // Update the stack string
	    m_stack = std::string(file + ":" + ISTR(line));
		m_fullStack.push_back(m_stack);
		rebuildFullStack();

	    // Update the code name string
	    m_codeName = name;

	    if (m_notify)
	    {
	    	DEBUG("[CSTACK] Event call notify: " + getFullStack() + ", mode=" + ISTR(m_soMode) + ", so=" + BSTR(isSteppingOver()));
	    	SPELLexecutor::instance().getCIF().notifyCall();
	    }
	}
	else
	{
		// Reset the recovery mode after the call event
	    m_recoveryMode = false;
	    DEBUG("[CSTACK] Reset recovery mode");
	}
}

//=============================================================================
// METHOD    : SPELLcallstack::callbackEventError
//=============================================================================
void SPELLcallstack::callbackEventError( PyFrameObject* frame, const std::string& file, const int line, const std::string& name)
{
    m_errorState = true;
}

//=============================================================================
// METHOD    : SPELLcallstack::callbackEventReturn
//=============================================================================
void SPELLcallstack::callbackEventReturn( PyFrameObject* frame, const std::string& file, const int line, const std::string& name )
{
    // We are finishing execution, ignore it
    if (name == "<module>")
	{
        DEBUG("[CSTACK] Ignore return event at end of code");
    	return;
	}

    DEBUG("[CSTACK] Event return: " + file + ":" + ISTR(line) + ", mode=" + ISTR(m_soMode) + ", so=" + BSTR(isSteppingOver()));

    if (m_errorState)
    {
        DEBUG("[CSTACK] Ignore return event meanwhile in error mode");
        return;
    }

    // Move the view node up if it currently matches with the leaf, otherwise leave it.
    // When moving the view node, we shall notify the client.

    // Do not remove the code on top. Just move the current node pointer.
    m_currentNode = m_currentNode->getParent()->getParentCode();

    // Store current line
    m_currentLine = line;

    if (m_currentNode->getDepth() <= m_viewNode->getDepth())
    {
		DEBUG("[CSTACK] Move view node in return" );
    	m_viewNode = m_currentNode;
        // Reset the step over mode unless it is set to always
        switch(m_soMode)
        {
        case SO_ONCE_OVER:
        case SO_ONCE_INTO:
        	m_soMode = m_previousSoMode;
			DEBUG("[CSTACK] New mode: " + ISTR(m_soMode) );
        	break;
        default:
        	// Do not change it if we are in 'ALWAYS' mode
        	break;
        }
    }

    // If in error state, do not move up the call stack
    // FIXME: to be checked that we dont need to put the
    // error state filter above in this function to prevent further changes
    // to the callstack model values. We may need the information...
    if (m_notify && !m_errorState)
    {
    	DEBUG("[CSTACK] Event return notify: " + getFullStack());
    	SPELLexecutor::instance().getCIF().notifyReturn();
    }

    // Update the stack string
    if (m_fullStack.size()>=2)
    {
    	m_stack = m_fullStack[m_fullStack.size()-2];
    }
    else
    {
    	m_stack = std::string(file + ":" + ISTR(line));
    }
    m_fullStack.pop_back();
    rebuildFullStack();

    // Update the code name string
    m_codeName = name;

}

//=============================================================================
// METHOD    : SPELLcallstack::clearStack
//=============================================================================
void SPELLcallstack::clearStack()
{
	SPELLmonitor mon(m_lock);
	if (m_rootNode != NULL)
	{
		m_rootNode->reset();
		delete m_rootNode;
		m_stack = "";
		m_fullStack.clear();
		m_fullStackString = "";
	}
	m_rootNode = NULL;
	m_currentNode = NULL;
	m_viewNode = NULL;
}

//=============================================================================
// METHOD    : SPELLcallstack::rebuildFullStack
//=============================================================================
void SPELLcallstack::rebuildFullStack()
{
	SPELLmonitor mon(m_lock);
	m_fullStackString = "";
	std::vector<std::string>::iterator it;
	std::vector<std::string>::iterator end = m_fullStack.end();
	for(it = m_fullStack.begin(); it != end; it++)
	{
		if (m_fullStackString != "") m_fullStackString += ":";
		m_fullStackString += *it;
	}
}
