///////////////////////////////////////////////////////////////////////////////
//
// PACKAGE   : com.astra.ses.spell.gui.procs.model
// 
// FILE      : ProcedureBase.java
//
// DATE      : Jun 11, 2013
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
///////////////////////////////////////////////////////////////////////////////
package com.astra.ses.spell.gui.procs.model;

import java.util.Map;

import com.astra.ses.spell.gui.core.interfaces.IExecutorInfo;
import com.astra.ses.spell.gui.core.interfaces.ServiceManager;
import com.astra.ses.spell.gui.core.model.server.ExecutorConfig;
import com.astra.ses.spell.gui.core.model.server.ExecutorInfo;
import com.astra.ses.spell.gui.core.model.types.Level;
import com.astra.ses.spell.gui.core.model.types.ProcProperties;
import com.astra.ses.spell.gui.core.utils.Logger;
import com.astra.ses.spell.gui.procs.interfaces.IProcedureManager;
import com.astra.ses.spell.gui.procs.interfaces.model.IProcedure;

/*******************************************************************************
 * 
 * Base model for procedures
 * 
 ******************************************************************************/
public abstract class ProcedureBase implements IProcedure
{
	/** Holds the procedure identifier */
	private String m_instanceId;
	/** Procedure properties */
	private Map<ProcProperties, String> m_properties;

	
	/***************************************************************************
	 * 
	 **************************************************************************/
	public ProcedureBase( String instanceId, Map<ProcProperties, String> properties )
	{
		m_instanceId = instanceId;
		m_properties = properties;
		if (getProcName() == null || getProcName().trim().isEmpty())
		{
			IProcedureManager mgr = (IProcedureManager) ServiceManager.get(IProcedureManager.class);
			int index = instanceId.indexOf("#");
			String name = mgr.getProcedureName(instanceId.substring(0, index));
			if (name != null)
			{
				m_properties.put(ProcProperties.PROC_NAME, name);
			}
			else
			{
				Logger.error("Cannot find procedure name for " + instanceId, Level.PROC, this);
			}
		}
	}
	
	/***************************************************************************
	 * 
	 **************************************************************************/
	@Override
	public String getProcId()
	{
		return m_instanceId;
	}

	/***************************************************************************
	 * 
	 **************************************************************************/
	@Override
	public String getProperty(ProcProperties property)
	{
		return m_properties.get(property);
	}

	/***************************************************************************
	 * 
	 **************************************************************************/
	protected void setProperty(ProcProperties property, String value)
	{
		m_properties.put(property,value);
	}

	/***************************************************************************
	 * 
	 **************************************************************************/
	@Override
	public String getProcName()
	{
		return getProperty(ProcProperties.PROC_NAME);
	}

	/***************************************************************************
	 * 
	 **************************************************************************/
	@Override
	public String getParent()
	{
		return getRuntimeInformation().getParent();
	}

	/***************************************************************************
	 * 
	 **************************************************************************/
	@Override
	public boolean isMain()
	{
		return (getParent() == null) || (getParent().trim().isEmpty());
	}

	/***************************************************************************
	 * 
	 **************************************************************************/
	@SuppressWarnings("rawtypes")
	@Override
	public Object getAdapter(Class adapter)
	{
		Object result = null;
		if (adapter.equals(ExecutorInfo.class))
		{
			IExecutorInfo info = new ExecutorInfo(getProcId());
			// TODO info.setParent(getParent().getProcId());
			getRuntimeInformation().visit(info);
			result = info;
		}
		else if (adapter.equals(ExecutorConfig.class))
		{
			ExecutorConfig cfg = new ExecutorConfig(getProcId());
			getRuntimeInformation().visit(cfg);
			result = cfg;
		}
		return result;
	}
}
