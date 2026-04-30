// ################################################################################
// FILE       : SPELLexecutorDefaults.H
// DATE       : Feb 13, 2014
// PROJECT    : SPELL
// DESCRIPTION: Context default values for Executors
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

#ifndef __SPELL_EXECUTOR_DEFAULTS_H__
#define __SPELL_EXECUTOR_DEFAULTS_H__

// FILES TO INCLUDE //////////////////////////////////////////////////////////
// Local includes ------------------------------------------------------------
// Project includes ----------------------------------------------------------
#include "SPELL_WRP/SPELLconstants.H"
#include "SPELL_CFG/SPELLbrowsableLibMode.H"
#include "SPELL_UTIL/SPELLutils.H"

/** \addtogroup SPELL_CFG */
/*@{*/


// FORWARD REFERENCES ////////////////////////////////////////////////////////
// TYPES /////////////////////////////////////////////////////////////////////
// DEFINES ///////////////////////////////////////////////////////////////////

//////////////////////////////////////////////////////////////////////////////
/**
 ** \brief
 **
**////////////////////////////////////////////////////////////////////////////
class SPELLexecutorDefaults
{
public: //--------------------------------------------------------------------

	// EXCEPTIONS ////////////////////////////////////////////////////////////
	// ENUMS /////////////////////////////////////////////////////////////////
	// TYPES /////////////////////////////////////////////////////////////////
	// LIFECYCLE /////////////////////////////////////////////////////////////

		//////////////////////////////////////////////////////////////////////
		/** Constructor.
		**////////////////////////////////////////////////////////////////////
		SPELLexecutorDefaults()
		{
			m_browsableLib = OFF;
			m_byStep = false;
			m_execDelay = 0;
			m_watchVariables = false;
			m_resourcesDisplay = false;
			m_localResourceSupport = false;
			m_forcePromptConfirm = false;
			m_forceTcConfirm = false;
			m_maxVerbosity = 0;
			m_runInto = false;
			m_promptWarningDelay = 0;
		};

		//////////////////////////////////////////////////////////////////////
		/** Destructor.
		**////////////////////////////////////////////////////////////////////
		~SPELLexecutorDefaults() {};

		//////////////////////////////////////////////////////////////////////
		/** Copy constructor.
		**////////////////////////////////////////////////////////////////////
		SPELLexecutorDefaults( const SPELLexecutorDefaults& from );

	// METHODS ///////////////////////////////////////////////////////////////

		// Member variables getters
		int getExecDelay() const
		{
			return m_execDelay;
		}

		int getPromptWarningDelay() const
		{
			return m_promptWarningDelay;
		}

		bool isRunInto() const
		{
			return m_runInto;
		}

		bool isByStep() const
		{
			return m_byStep;
		}

		SPELLbrowsableLibMode getBrowsableLib() const
		{
			return m_browsableLib;
		}

		std::string getBrowsableLibStr() const
		{
			return browsableLibModeToString(m_browsableLib);
		}

		int getMaxVerbosity() const
		{
			return m_maxVerbosity;
		}

		bool isForceTcConfirm() const
		{
			return m_forceTcConfirm;
		}
		bool isForcePromptConfirm() const
		{
			return m_forcePromptConfirm;
		}
		std::string getSaveStateMode() const
		{
			return m_saveStateMode;
		} //getSaveStateMode


		bool isWatchVariables() const
		{
			return m_watchVariables;
		}

		bool isResourcesDisplay() const
		{
			return m_resourcesDisplay;
		}

		bool isLocalResourcesSupported() const
		{
			return m_localResourceSupport;
		}

		std::string getCtxName() const
		{
			return m_ctxName;
		}


		// Member variables setters

		void setExecDelay(int execDelay)
		{
			m_execDelay = execDelay;
		}

		void setPromptWarningDelay(int promptWarningDelay)
		{
			m_promptWarningDelay = promptWarningDelay;
		}

		void setRunInto(bool runInto)
		{
			m_runInto = runInto;
		}

		void setByStep(bool	byStep)
		{
			m_byStep = byStep;
		}

		void setBrowsableLib(SPELLbrowsableLibMode browsableLib)
		{
			m_browsableLib = browsableLib;
		}

		void setBrowsableLibStr(std::string browsableLibStr)
		{
			m_browsableLib = stringToBrowsableLibMode(browsableLibStr);
		}


		void setMaxVerbosity(int maxVerbosity)
		{
			m_maxVerbosity = maxVerbosity;
		}

		void setForceTcConfirm(bool	forceTcConfirm)
		{
			m_forceTcConfirm = forceTcConfirm;
		}
		void setForcePromptConfirm(bool	forcePromptConfirm)
		{
			m_forcePromptConfirm = forcePromptConfirm;
		}
		void setSaveStateMode(std::string saveModeStr)
		{
			m_saveStateMode = saveModeStr;
		}

		void setWatchVariables(bool	watchVariables)
		{
			m_watchVariables = watchVariables;
		}

		void setResourcesDisplay(bool resourcesDisplay)
		{
			m_resourcesDisplay = resourcesDisplay;
		}

		void setLocalResourcesSupported(bool localResourceSupport)
		{
			m_localResourceSupport = localResourceSupport;
		}

		void setCtxName( std::string ctxName)
		{
			m_ctxName = ctxName;
		}

		std::string toString() {
			std::string str = "";
			str.append("Context: ").append(m_ctxName).append("\n");
			str.append("ExecDelay: ").append(ISTR(m_execDelay)).append("\n");
			str.append("PromptWarningDelay: ").append(ISTR(m_promptWarningDelay)).append("\n");
			str.append("RunInto: ").append(BSTR(m_runInto)).append("\n");
			str.append("ByStep: ").append(BSTR(m_byStep)).append("\n");
			str.append("BrowsableLib: ").append( browsableLibModeToString(m_browsableLib) ).append("\n");
			str.append("MaxVerbosity: ").append(ISTR(m_maxVerbosity)).append("\n");
			str.append("ForceTcConfirm: ").append(BSTR(m_forceTcConfirm)).append("\n");
			str.append("ForcePromptConfirm: ").append(BSTR(m_forcePromptConfirm)).append("\n");
			str.append("WatchVariables: ").append(BSTR(m_watchVariables)).append("\n");
			str.append("ResourcesDisplay: ").append(BSTR(m_resourcesDisplay)).append("\n");
			str.append("LocalResourcesSupported: ").append(BSTR(m_localResourceSupport)).append("\n");
			str.append("SaveStateMode: ").append(m_saveStateMode).append("\n");

			return str;
		}



private: //-------------------------------------------------------------------

	// EXCEPTIONS ////////////////////////////////////////////////////////////
	// ENUMS /////////////////////////////////////////////////////////////////
	// TYPES /////////////////////////////////////////////////////////////////
	// STATIC ////////////////////////////////////////////////////////////////
	// LIFECYCLE /////////////////////////////////////////////////////////////
	// METHODS ///////////////////////////////////////////////////////////////
	// DATA MEMBERS //////////////////////////////////////////////////////////

		int					m_execDelay;
		int		      		m_promptWarningDelay;
		bool				m_runInto;
		bool				m_byStep;
		//bool				m_browsableLib;
		SPELLbrowsableLibMode m_browsableLib;
		int					m_maxVerbosity;
		bool				m_forceTcConfirm;
		bool				m_forcePromptConfirm;
		std::string			m_saveStateMode;
		bool				m_watchVariables;
		bool				m_resourcesDisplay;
		bool				m_localResourceSupport;
		std::string 		m_ctxName;
};

/*@}*/
#endif
