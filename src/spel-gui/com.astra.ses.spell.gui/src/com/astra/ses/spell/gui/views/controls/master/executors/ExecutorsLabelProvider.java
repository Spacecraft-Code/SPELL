///////////////////////////////////////////////////////////////////////////////
//
// PACKAGE   : com.astra.ses.spell.gui.views.controls.master
// 
// FILE      : ExecutorsLabelProvider.java
//
// DATE      : 2008-11-21 08:55
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
package com.astra.ses.spell.gui.views.controls.master.executors;

import org.eclipse.jface.viewers.ITableColorProvider;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Display;

import com.astra.ses.spell.gui.core.interfaces.ServiceManager;
import com.astra.ses.spell.gui.preferences.interfaces.IConfigurationManager;
import com.astra.ses.spell.gui.procs.interfaces.model.IProcedure;

/**
 * @author Rafael Chinchilla
 *
 */
public class ExecutorsLabelProvider extends LabelProvider implements ITableLabelProvider, ITableColorProvider 
{
	private static IConfigurationManager s_cfg = null;

	/***************************************************************************
	 * 
	 **************************************************************************/
	private IConfigurationManager getConfig()
	{
		if (s_cfg == null)
		{
			s_cfg = (IConfigurationManager) ServiceManager.get(IConfigurationManager.class);
		}
		return s_cfg;
	}
	
	/***************************************************************************
	 * 
	 **************************************************************************/
	public ExecutorsLabelProvider()
	{
		super();
	}
	
	/***************************************************************************
	 * 
	 **************************************************************************/
    @Override
    public Image getColumnImage(Object element, int columnIndex)
    {
	    return null;
    }
    
	/***************************************************************************
	 * 
	 **************************************************************************/
    @Override
    public String getColumnText(Object element, int columnIndex)
    {
    	if (element instanceof IProcedure)
    	{
		    IProcedure proc = (IProcedure) element;
		    ExecutorsTableColumns item = ExecutorsTableColumns.index(columnIndex);
		    int index = proc.getProcId().indexOf("#");
		    switch(item)
		    {
		    case PROCEDURE:
		    	return proc.getProcName() + " (" + proc.getProcId().substring(index+1) + ")";
		    case STATUS:
		    	return proc.getRuntimeInformation().getStatus().description.toUpperCase();
		    case ORIGIN:
		    	return proc.getRuntimeInformation().getOriginId();
		    case STEP:
		    	String id = proc.getRuntimeInformation().getStageId();
		    	String title = proc.getRuntimeInformation().getStageTitle();
		    	if (id == null || title == null) return null;
		    	if ((id.equals(title) && id.equals("(none)")) ) return null; 
		    	return id + ": " + title;
		    case FLAGS:
		    	return null;
		    }
    	}
    	else if (columnIndex ==0)
    	{
    		return element.toString();
    	}
    	return null;
    }

	/***************************************************************************
	 * 
	 **************************************************************************/
    @Override
    public Color getForeground(Object element, int columnIndex)
    {
	    return Display.getDefault().getSystemColor(SWT.COLOR_BLACK);
    }

	/***************************************************************************
	 * 
	 **************************************************************************/
    @Override
    public Color getBackground(Object element, int columnIndex)
    {
    	if (element instanceof IProcedure)
    	{
		    IProcedure proc = (IProcedure) element;
		    return getConfig().getProcedureColor(proc.getRuntimeInformation().getStatus());
    	}
    	return null;
    }

}
