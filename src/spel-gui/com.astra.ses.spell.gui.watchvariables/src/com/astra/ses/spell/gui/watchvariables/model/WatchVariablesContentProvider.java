////////////////////////////////////////////////////////////////////////////////
//
// PACKAGE   : com.astra.ses.spell.gui.watchvariables.model
// 
// FILE      : WatchVariablesContentProvider.java
//
// DATE      : Sep 22, 2010 11:44:03 AM
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
////////////////////////////////////////////////////////////////////////////////
package com.astra.ses.spell.gui.watchvariables.model;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.Viewer;

import com.astra.ses.spell.gui.watchvariables.interfaces.IVariableManager;
import com.astra.ses.spell.gui.watchvariables.notification.VariableData;

/*******************************************************************************
 * 
 * {@link WatchVariablesContentProvider} provides the variables to show in the
 * variables view
 * 
 ******************************************************************************/
public class WatchVariablesContentProvider implements IStructuredContentProvider
{

	/** Procedure data provider reference */
	private IVariableManager	m_input;
	/** Show mode */
	private boolean m_showGlobals;
	private boolean m_showLocals;

	/**************************************************************************
	 * Constructor.
	 *************************************************************************/
	public WatchVariablesContentProvider()
	{
		m_showGlobals = true;
		m_showLocals = true;
	}
	
	/**************************************************************************
	 * 
	 *************************************************************************/
	@Override
	public void inputChanged(Viewer v, Object oldInput, Object newInput)
	{
		if (newInput != null)
		{
			m_input = (IVariableManager) newInput;
		}
	}
	
	/**************************************************************************
	 * 
	 *************************************************************************/
	public void showGlobals( boolean show )
	{
		m_showGlobals = show;
	}

	/**************************************************************************
	 * 
	 *************************************************************************/
	public void showLocals( boolean show )
	{
		m_showLocals = show;
	}

	/**************************************************************************
	 * 
	 *************************************************************************/
	@Override
	public Object[] getElements(Object parent)
	{
		if (m_input == null) return new VariableData[0];
		Map<String,VariableData> elements = m_input.getVariables();
		if (elements == null) return new VariableData[0];
		List<VariableData> toProvide = new LinkedList<VariableData>();

		for(VariableData var : elements.values())
		{
			if (var.isGlobal() && m_showGlobals) toProvide.add(var);
			if (!var.isGlobal() && m_showLocals) toProvide.add(var);
		}
		return toProvide.toArray();
	}

	/**************************************************************************
	 * 
	 *************************************************************************/
	@Override
    public void dispose()
    {
	    
    }
}
