// ################################################################################
// FILE       : SPELLgoto.C
// DATE       : Mar 17, 2011
// PROJECT    : SPELL
// DESCRIPTION: Implementation of the goto mechanism
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
// Local includes ----------------------------------------------------------
#include "SPELL_EXC/SPELLgoto.H"
// Project includes --------------------------------------------------------
#include "SPELL_UTIL/SPELLlog.H"
#include "SPELL_UTIL/SPELLutils.H"
// System includes ---------------------------------------------------------
#include "opcode.h"


// Macros for accessing opcodes
#define NEXTOP()        (*next_instr++)
#define NEXTARG()       (next_instr += 2, (next_instr[-1]<<8) + next_instr[-2])
#define INSTR_OFFSET()  ((int)(next_instr - first_instr))

static const std::string STEP_CALL("Step");
static const std::string GOTO_CALL("Goto");

typedef enum WhatNow_
{
    STEP_ID = 0,
    STEP_TITLE = 1,
    GOTO_ID = 2,
    SOMETHING_ELSE = 3
}
WhatNow;

//=============================================================================
// CONSTRUCTOR    : SPELLgoto::SPELLgoto
//=============================================================================
SPELLgoto::SPELLgoto( PyCodeObject* code )
    : m_code(code)
{
    DEBUG("[GOTO] Created for " + PYREPR( (PyObject*)code));
    analyze();
}

//=============================================================================
// DESTRUCTOR    : SPELLgoto::~SPELLgoto
//=============================================================================
SPELLgoto::~SPELLgoto()
{
    DEBUG("[GOTO] Destroyed for code " + PYREPR( (PyObject*) m_code));
    m_labels.clear();
    m_titles.clear();
    m_gotos.clear();
}

//=============================================================================
// METHOD     : SPELLgoto::analyze
//=============================================================================
void SPELLgoto::analyze()
{
    assert( m_code != NULL );

    DEBUG("[GOTO] Analyzing code " + PYREPR( (PyObject*) m_code));

    // Assume there is no INIT step
    m_initLine = -1;
    // Pointer to initial instruction (used by macros)
    unsigned char* first_instr = (unsigned char*) PyString_AS_STRING(m_code->co_code);
    // Size of the code to analyze
    const unsigned int code_size = PyString_Size(m_code->co_code);
    // Pointer to current instruction (used by macros)
    register unsigned char* next_instr = first_instr;
    // Holds the current line
    unsigned int lineno;
    // Holds the current instruction offset
    register unsigned int offset;
    // Holds the current opcode
    register unsigned int opcode;
    // Opcode argument
    register unsigned int oparg;

    // Holds the last loaded name
    std::string loaded_name,loaded_const = "";
    // Helps to parse the step statements
    WhatNow what_now = SOMETHING_ELSE;
	// Hold the goto information
	std::string step_id,step_title,goto_id = "";
    register unsigned short const1 = 1;

    offset = INSTR_OFFSET();
    while (offset < code_size)
    {
        // Obtain the opcode
        opcode = NEXTOP();

        // Get the opcode argument
        oparg  = 0;
        if (HAS_ARG(opcode)) oparg = NEXTARG();

        switch(opcode)
        {
        case LOAD_NAME:
        case LOAD_GLOBAL:
        {
            loaded_name = PYSTR(PyTuple_GetItem(m_code->co_names,oparg));
            if (loaded_name == STEP_CALL)
            {
				what_now = STEP_ID;
            }
            else if (loaded_name == GOTO_CALL)
            {
				what_now = GOTO_ID;
			}
			else
			{
				what_now = SOMETHING_ELSE;
			}
            break;
        }
        case LOAD_CONST:
        {
        	PyObject* pyConst = PyTuple_GetItem(m_code->co_consts,oparg);
        	if (pyConst != Py_None)
        	{
				loaded_const = PYSTR(pyConst);
				if (what_now == STEP_ID)
				{
					step_id = loaded_const;
					what_now = STEP_TITLE;
				}
				else if (what_now == STEP_TITLE )
				{
					step_title = loaded_const;
					what_now = SOMETHING_ELSE;
				}
				else if (what_now == GOTO_ID)
				{
					goto_id = loaded_const;
					what_now = SOMETHING_ELSE;
				}
        	}
            break;
        }
        case CALL_FUNCTION:
        {
            if (loaded_name == STEP_CALL)
            {
                // Get the corresponding script line
                lineno = PyCode_Addr2Line(m_code, offset);
                // If it is an INIT step, mark it
                if (step_id == "INIT")
                {
                    LOG_INFO("Found INIT step at " + ISTR(lineno));
                    m_initLine = lineno;
                }
                // Store the info
                LOG_INFO("[GOTO] Label '" + step_id + "' , '" + step_title + "' at " + ISTR(lineno));
              	m_labels.insert( std::make_pair( step_id, lineno ));
                m_labelLines.insert(lineno);
                m_titles.insert( std::make_pair( step_title, step_id ));
            }
            else if (loaded_name == GOTO_CALL)
            {
                lineno = PyCode_Addr2Line(m_code, offset);
                m_gotos.insert( std::make_pair( lineno, goto_id ));
            }
            const1 = 1;
            break;
        }
        }// End switch

//    	// Inform about errors, but ignore them at this analysis
//        PyObject* obj = PyErr_Occurred();
//   	    if (obj!= NULL)
//    	{
//   	        PyObject* ptype;
//   	        PyObject* pvalue;
//   	        PyObject* ptraceback;
//   	        PyErr_Fetch( &ptype, &pvalue, &ptraceback );
//   	        //LOG_ERROR("ERROR ON GOTO ANALYSIS: " + PYREPR(pvalue));
//    	}

        PyErr_Clear();

        // Get the instruction offset
        offset = INSTR_OFFSET();

    }
    DEBUG("[GOTO] Code analyzed, labels (" + ISTR(m_labels.size()) + "), gotos(" + ISTR(m_gotos.size()) + ") for code " + PYREPR( (PyObject*) m_code));
}

//=============================================================================
// METHOD     : SPELLgoto::validateGotos()
//=============================================================================
std::string SPELLgoto::validateGotos() const
{
	// Ensure that all registered goto's have one and only one corresponding label
	std::string parsingError = "";
	GotoMap::const_iterator it;
	for( it = m_gotos.begin(); it != m_gotos.end(); it++)
	{
		std::string gotoTarget = it->second;
		LabelMap::const_iterator lit = m_labels.find(gotoTarget);
		if (lit==m_labels.end())
		{
			if (parsingError != "") parsingError += ",";
			parsingError += gotoTarget;
		}
	}
	return parsingError;
}

//=============================================================================
// METHOD     : SPELLgoto::getTargetLine
//=============================================================================
const int SPELLgoto::getTargetLine( const unsigned int frameLine )
{
    GotoMap::const_iterator it;
    LabelMap::const_iterator lit;
    it = m_gotos.find( (unsigned int) frameLine );
    if (it != m_gotos.end() )
    {
        DEBUG("[GOTO] Found target line: " + ISTR(frameLine));
        lit = m_labels.find( (*it).second );
        return (*lit).second;
    }
    else
    {
        return -1;
    }
}

//=============================================================================
// METHOD     : SPELLgoto::isLabel
//=============================================================================
const bool SPELLgoto::isLabel( const unsigned int frameLine )
{
    return (m_labelLines.find(frameLine)!=m_labelLines.end());
}

//=============================================================================
// METHOD     : SPELLgoto::isInitStep
//=============================================================================
const bool SPELLgoto::isInitStep( const unsigned int frameLine )
{
    return ( (unsigned int) m_initLine == frameLine);
}
