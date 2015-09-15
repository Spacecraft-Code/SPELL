///////////////////////////////////////////////////////////////////////////////
//
// PACKAGE   : com.astra.ses.spell.gui.core.model.server
// 
// FILE      : ExecutorDefaults.java
//
// DATE      : 2014-02-24 10:58
//
// Copyright (C) 2008, 2015 SES ENGINEERING, Luxembourg S.A.R.L.
//
// By using this software in any way, you are agreeing to be bound by
// the terms of this license.
//
// All rights reserved. This program and the accompanying materials
// are made available under the terms of the Eclipse Public License v1.0
// which accompanies this distribution, and is available at
// http://www.eclipse.org/legal/epl-v10.html
//
// NO WARRANTY
// EXCEPT AS EXPRESSLY SET FORTH IN THIS AGREEMENT, THE PROGRAM IS PROVIDED
// ON AN "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, EITHER
// EXPRESS OR IMPLIED INCLUDING, WITHOUT LIMITATION, ANY WARRANTIES OR
// CONDITIONS OF TITLE, NON-INFRINGEMENT, MERCHANTABILITY OR FITNESS FOR A
// PARTICULAR PURPOSE. Each Recipient is solely responsible for determining
// the appropriateness of using and distributing the Program and assumes all
// risks associated with its exercise of rights under this Agreement ,
// including but not limited to the risks and costs of program errors,
// compliance with applicable laws, damage to or loss of data, programs or
// equipment, and unavailability or interruption of operations.
//
// DISCLAIMER OF LIABILITY
// EXCEPT AS EXPRESSLY SET FORTH IN THIS AGREEMENT, NEITHER RECIPIENT NOR ANY
// CONTRIBUTORS SHALL HAVE ANY LIABILITY FOR ANY DIRECT, INDIRECT, INCIDENTAL,
// SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING WITHOUT LIMITATION
// LOST PROFITS), HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
// CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
// ARISING IN ANY WAY OUT OF THE USE OR DISTRIBUTION OF THE PROGRAM OR THE
// EXERCISE OF ANY RIGHTS GRANTED HEREUNDER, EVEN IF ADVISED OF THE
// POSSIBILITY OF SUCH DAMAGES.
//
// Contributors:
//    SES ENGINEERING - initial API and implementation and/or initial documentation
//
// PROJECT   : SPELL
//
// SUBPROJECT: SPELL GUI Client
//
///////////////////////////////////////////////////////////////////////////////
package com.astra.ses.spell.gui.core.model.server;

import java.util.Map;
import java.util.TreeMap;

import com.astra.ses.spell.gui.core.model.types.BrowsableLibMode;
import com.astra.ses.spell.gui.core.model.types.SaveStateMode;


public class ExecutorDefaults
{
	private String m_ctxName;
	
	/** Holds the RunInto state */
	private boolean	m_runInto;
	/** Holds the execution delay */
	private int	    m_execDelay;
	/** PromptWaringDelay */
	private int m_promptWarningDelay;
	/** Holds the step-by-step state */
	private boolean	m_byStep;
	/** Holds the show lib mode */
	private BrowsableLibMode m_browsableLib;
	/** Tc confirmation flag */
	private boolean m_forceTcConfirm;
	private int m_maxVerbosity;
	private boolean m_watchVariables;
	private SaveStateMode m_saveStateMode;
	
	

	/***************************************************************************
	 * Constructor
	 **************************************************************************/
	public ExecutorDefaults()
	{
		m_ctxName = "";
		m_runInto = false;
		m_byStep = false;
		m_execDelay = 0;
		m_browsableLib = BrowsableLibMode.OFF;
		m_forceTcConfirm = false;
		m_promptWarningDelay = 0;
		m_maxVerbosity = 0;
		m_watchVariables = false;
		m_saveStateMode = SaveStateMode.MODE_UNINIT;
	} //ExecutorDefaults()

	/***************************************************************************
	 * Copy data from given info
	 **************************************************************************/
	public void copyFrom(ExecutorDefaults config)
	{
		m_ctxName = config.m_ctxName;
		m_runInto = config.m_runInto;
		m_byStep = config.m_byStep;
		m_execDelay = config.m_execDelay;
		m_browsableLib = config.m_browsableLib;
		m_forceTcConfirm = config.m_forceTcConfirm;
		m_promptWarningDelay = config.m_promptWarningDelay;
		m_maxVerbosity = config.m_maxVerbosity;
		m_watchVariables = config.m_watchVariables;
		m_saveStateMode = config.m_saveStateMode;
	} //copyFrom

	
	/***************************************************************************
	 * Getters and Setters
	 **************************************************************************/

	public void setCtxName(String name)
	{
		m_ctxName = name;
	}

	public String getCtxName()
	{
		return m_ctxName;
	}	
	

	public void setRunInto(boolean status)
	{
		m_runInto = status;
	}

	public boolean getRunInto()
	{
		return m_runInto;
	}


	public boolean getByStep()
	{
		return m_byStep;
	}

	public void setByStep(boolean status)
	{
		m_byStep = status;
	}


	public boolean getForceTcConfirm()
	{
		return m_forceTcConfirm;
	}
	
	public void setForceTcConfirm(boolean status)
	{
		m_forceTcConfirm = status;
	}


	public BrowsableLibMode getBrowsableLib()
	{
		return m_browsableLib;
	}
	
	public void setBrowsableLib( BrowsableLibMode mode )
	{
		m_browsableLib = mode;
	}


	public int getExecDelay()
	{
		return m_execDelay;
	}

	public void setExecDelay(int delay)
	{
		m_execDelay = delay;
	}


	public int getPromptWarningDelay() {
		return m_promptWarningDelay;
	} //getPromptWarningDelay

	public void setPromptWarningDelay(int m_promptWarningDelay) {
		this.m_promptWarningDelay = m_promptWarningDelay;
	} //setPromptWarningDelay
	
	
	public int getMaxVerbosity()
	{
		return m_maxVerbosity;
	} //getMaxVerbosity
	
	public void setMaxVerbosity(int filter)
	{
		m_maxVerbosity = filter;
	} //setMaxVerbosity
	
	
	public boolean getWatchVariables()
	{
		return m_watchVariables;
	} //getWatchVariables
	
	public void setWatchVariables(boolean status)
	{
		m_watchVariables = status;
	} //setWatchVariables

	
	public SaveStateMode getSaveStateMode()
	{
		return m_saveStateMode;
	} //getSaveStateMode
	
	public void setSaveStateMode(SaveStateMode ssm)
	{
		m_saveStateMode = ssm;
	} //setSaveStateMode
	
	
	// Choose the SaveStateMode according to the String
	public void setSaveStateModeStr(String str)
	{
		SaveStateMode ssm = SaveStateMode.MODE_UNINIT;
		
		for( SaveStateMode t_ssm : SaveStateMode.values() )
			if( t_ssm.getKey().equals(str) ) 
			{
				ssm = t_ssm;
				break;
			}
		
		m_saveStateMode = ssm;
	} //setSaveSateModeStr
	
	public String getSaveStateModeStr()
	{
		return m_saveStateMode.getKey();
	} //getSaveStateModeStr

	
	/***************************************************************************
	 * Obtain the client mode
	 **************************************************************************/
	public Map<String, String> getConfigMap()
	{
		Map<String, String> defaults = new TreeMap<String, String>();
		
		defaults.put(ExecutorDefaultParams.RUN_INTO.getParam(), m_runInto ? "True" : "False");
		defaults.put(ExecutorDefaultParams.BY_STEP.getParam(), m_byStep ? "True" : "False");
		switch(m_browsableLib)
		{
		case DISABLED:
			defaults.put(ExecutorDefaultParams.BROWSABLE_LIB.getParam(), "Disabled");
			break;
		case OFF:
			defaults.put(ExecutorDefaultParams.BROWSABLE_LIB.getParam(), "False");
			break;
		case ON:
			defaults.put(ExecutorDefaultParams.BROWSABLE_LIB.getParam(), "True");
			break;
		}
		defaults.put(ExecutorDefaultParams.FORCE_TC_CONFIRM.getParam(), m_forceTcConfirm ? "True" : "False");
		defaults.put(ExecutorDefaultParams.EXEC_DELAY.getParam(), Integer.toString(m_execDelay));
		defaults.put(ExecutorDefaultParams.PROMPT_DELAY.getParam(), Integer.toString(m_promptWarningDelay));
		defaults.put(ExecutorDefaultParams.MAX_VERBOSITY.getParam(), Integer.toString(m_maxVerbosity));
		defaults.put(ExecutorDefaultParams.WATCH_VARIABLES.getParam(), m_watchVariables ? "True" : "False");
		defaults.put(ExecutorDefaultParams.SAVE_STATE_MODE.getParam(), m_saveStateMode.getKey() );
		
		return defaults;
	} //getConfigMap
	
} //ExecutorDefaults class
