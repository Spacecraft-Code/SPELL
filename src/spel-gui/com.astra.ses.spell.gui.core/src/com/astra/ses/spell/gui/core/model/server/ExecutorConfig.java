///////////////////////////////////////////////////////////////////////////////
//
// PACKAGE   : com.astra.ses.spell.gui.core.model.server
// 
// FILE      : ExecutorConfig.java
//
// DATE      : 2008-11-21 08:58
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


public class ExecutorConfig
{
	/** Holds the procedure instance identifier **/
	private String m_procId;
	/** Holds the RunInto state */
	private boolean	m_runInto;
	/** Holds the execution delay */
	private int	    m_execDelay;
	/** Holds the step-by-step state */
	private boolean	m_byStep;
	/** Holds the show lib state */
	private boolean	m_showLib;
	/** Tc confirmation flag */
	private boolean m_tcConfirmation;
	/** PromptWaringDelay */
	private int m_promptWarningDelay;
	

	/***************************************************************************
	 * Constructor
	 **************************************************************************/
	public ExecutorConfig( String procId )
	{
		m_procId = procId;
		m_runInto = false;
		m_byStep = false;
		m_execDelay = 0;
		m_showLib = false;
		m_tcConfirmation = false;
		m_promptWarningDelay = 0;
	}

	/***************************************************************************
	 * Copy data from given info
	 **************************************************************************/
	public void copyFrom(ExecutorConfig config)
	{
		m_procId = config.m_procId;
		m_runInto = config.m_runInto;
		m_byStep = config.m_byStep;
		m_execDelay = config.m_execDelay;
		m_showLib = config.m_showLib;
		m_tcConfirmation = config.m_tcConfirmation;
		m_promptWarningDelay = config.m_promptWarningDelay;
	}

	/***************************************************************************
	 * Get the procedure identifier
	 **************************************************************************/
	public String getProcId()
	{
		return m_procId;
	}

	/***************************************************************************
	 * Assign the runinto status
	 **************************************************************************/
	public void setRunInto(boolean enabled)
	{
		m_runInto = enabled;
	}

	/***************************************************************************
	 * Obtain the runinto status
	 **************************************************************************/
	public boolean getRunInto()
	{
		return m_runInto;
	}

	/***************************************************************************
	 * Assign the bystep status
	 **************************************************************************/
	public void setStepByStep(boolean enabled)
	{
		m_byStep = enabled;
	}

	/***************************************************************************
	 * Assign the TC confirmation flag
	 **************************************************************************/
	public void setTcConfirmation(boolean force)
	{
		m_tcConfirmation = force;
	}

	/***************************************************************************
	 * Obtain the bystep status
	 **************************************************************************/
	public boolean getStepByStep()
	{
		return m_byStep;
	}

	/***************************************************************************
	 * Assign the browsable lib status
	 **************************************************************************/
	public void setBrowsableLib(boolean enabled)
	{
		m_showLib = enabled;
	}

	/***************************************************************************
	 * Obtain the browsable lib status
	 **************************************************************************/
	public boolean getBrowsableLib()
	{
		return m_showLib;
	}

	/***************************************************************************
	 * Assign the execution delay
	 **************************************************************************/
	public void setExecDelay(int delay)
	{
		m_execDelay = delay;
	}

	/***************************************************************************
	 * Obtain the execution delay
	 **************************************************************************/
	public int getExecDelay()
	{
		return m_execDelay;
	}

	/***************************************************************************
	 * Obtain the TC confirmation flag
	 **************************************************************************/
	public boolean getTcConfirmation()
	{
		return m_tcConfirmation;
	}

	/***************************************************************************
	 * Obtain the client mode
	 **************************************************************************/
	public Map<String, String> getConfigMap()
	{
		Map<String, String> config = new TreeMap<String, String>();
		
		config.put(ExecutorConfigKeys.RUN_INTO.getKey(), m_runInto ? "True" : "False");
		config.put(ExecutorConfigKeys.BY_STEP.getKey(), m_byStep ? "True" : "False");
		config.put(ExecutorConfigKeys.BROWSABLE_LIB.getKey(), m_showLib ? "True" : "False");
		config.put(ExecutorConfigKeys.FORCE_TC_CONFIRM.getKey(), m_tcConfirmation ? "True" : "False");
		config.put(ExecutorConfigKeys.EXEC_DELAY.getKey(), Integer.toString(m_execDelay));
		config.put(ExecutorConfigKeys.PROMPT_DELAY.getKey(), Integer.toString(m_promptWarningDelay));
		return config;
	}

	/***************************************************************************
	 * Obtain the Prompt Warning Delay
	 **************************************************************************/
	public int getPromptWarningDelay() {
		return m_promptWarningDelay;
	} //getPromptWarningDelay

	/***************************************************************************
	 * Set the promptWarningDelay
	 **************************************************************************/
	public void setPromptWarningDelay(int m_promptWarningDelay) {
		this.m_promptWarningDelay = m_promptWarningDelay;
	} //setPromptWarningDelay
	
}
