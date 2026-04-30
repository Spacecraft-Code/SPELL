// ################################################################################
// FILE       : SPELLexecutionModel.C
// DATE       : Mar 17, 2011
// PROJECT    : SPELL
// DESCRIPTION: Implementation of the execution model
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
#include "SPELL_EXC/SPELLexecutionModel.H"
#include "SPELL_EXC/SPELLvarInfo.H"
#include "SPELL_EXC/SPELLexecutor.H"
// Project includes --------------------------------------------------------
#include "SPELL_UTIL/SPELLerror.H"
#include "SPELL_UTIL/SPELLlog.H"

// GLOBALS ////////////////////////////////////////////////////////////////////

//=============================================================================
// CONSTRUCTOR : SPELLexecutionModel::SPELLexecutionModel
//=============================================================================
SPELLexecutionModel::SPELLexecutionModel( const std::string& modelId,
		                                  const std::string& filename,
		                                  PyFrameObject* frame,
										  std::set<std::string>& initialVariables )
: SPELLgoto(frame->f_code),
  SPELLbytecode(frame->f_code),
  SPELLlnotab(frame->f_code),
  SPELLvariableChangeListener(),
  m_frame(frame),
  m_varMonitor(this,frame,initialVariables)
{
	m_modelId = modelId;
}

//=============================================================================
// DESTRUCTOR : SPELLexecutionModel::~SPELLexecutionModel
//=============================================================================
SPELLexecutionModel::~SPELLexecutionModel()
{
}

//=============================================================================
// METHOD: SPELLexecutionModel::update()
//=============================================================================
void SPELLexecutionModel::update()
{
	m_varMonitor.analyze();
}

//=============================================================================
// METHOD: SPELLexecutionModel::variableChanged()
//=============================================================================
void SPELLexecutionModel::variableChanged( const std::vector<SPELLvarInfo>& added,
										   const std::vector<SPELLvarInfo>& changed,
		                                   const std::vector<SPELLvarInfo>& deleted )
{
	if (SPELLvariableMonitor::s_enabled)
	{
		SPELLexecutor::instance().getCIF().notifyVariableChange( added, changed, deleted );
	}
}

//=============================================================================
// METHOD: SPELLexecutionModel::scopeChanged()
//=============================================================================
void SPELLexecutionModel::scopeChanged()
{
	if (SPELLvariableMonitor::s_enabled)
	{
		SPELLvariableMonitor::VarMap globals = m_varMonitor.getGlobalVariables();
		SPELLvariableMonitor::VarMap locals = m_varMonitor.getLocalVariables();
		std::vector<SPELLvarInfo> globalsV;
		std::vector<SPELLvarInfo> localsV;
		for(SPELLvariableMonitor::VarMap::const_iterator it = globals.begin(); it != globals.end(); it++)
		{
			globalsV.push_back( SPELLvarInfo( it->second.varName,
					                          it->second.varType,
					                          it->second.varValue, true) );
		}
		for(SPELLvariableMonitor::VarMap::const_iterator it = locals.begin(); it != locals.end(); it++)
		{
			localsV.push_back( SPELLvarInfo( it->second.varName,
					                          it->second.varType,
					                          it->second.varValue, false) );
		}
		SPELLexecutor::instance().getCIF().notifyVariableScopeChange( m_varMonitor.getScopeName(), globalsV, localsV );
	}
}
