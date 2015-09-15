///////////////////////////////////////////////////////////////////////////////
//
// PACKAGE   : com.astra.ses.spell.gui.model.htree
// 
// FILE      : HistoryTreeLabelProvider.java
//
// DATE      : Jun 12, 2013
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
package com.astra.ses.spell.gui.model.htree;

import org.eclipse.jface.viewers.CellLabelProvider;
import org.eclipse.jface.viewers.IColorProvider;
import org.eclipse.jface.viewers.IFontProvider;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.ViewerCell;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Display;

import com.astra.ses.spell.gui.core.interfaces.ServiceManager;
import com.astra.ses.spell.gui.htree.Activator;
import com.astra.ses.spell.gui.preferences.interfaces.IConfigurationManager;
import com.astra.ses.spell.gui.preferences.keys.FontKey;


/******************************************************************************
 * Provides the labels and icons for the callstack tree.
 *****************************************************************************/
public class HistoryTreeLabelProvider extends CellLabelProvider implements
        IColorProvider, IFontProvider, ILabelProvider
{
	
	private Font m_activeFont;
	private Font m_neverExecutedFont;
	private Image m_procImage;

	/**************************************************************************
	 * Constructor.
	 *************************************************************************/
	public HistoryTreeLabelProvider()
	{
		IConfigurationManager config = (IConfigurationManager) ServiceManager.get(IConfigurationManager.class);
		
		m_activeFont = config.getFont(FontKey.GUI_BOLD, 10);
		
		Font aux = config.getFont(FontKey.GUI_NOM,10);
		FontData[] fdata = aux.getFontData();
		for(FontData fd : fdata)
		{
			fd.setStyle(SWT.ITALIC);
		}
		m_neverExecutedFont = new Font(Display.getDefault(),fdata);
		
		m_procImage = Activator.getImageDescriptor("icons/16x16/cog.png").createImage();
	}

	/**************************************************************************
	 * Obtain the text corresponding to the given element.
	 *************************************************************************/
	@Override
	public String getText(Object obj)
	{
		if (obj instanceof HistoryTreeRootNode)
		{
			HistoryTreeRootNode node = (HistoryTreeRootNode) obj;
			
			int idx = node.getProcId().indexOf("#");
			String instanceNum = node.getProcId().substring(idx);

			return node.getName() + " (" + instanceNum + ")";
		}
		else if (obj instanceof HistoryTreeChildProcedureNode)
		{
			HistoryTreeChildProcedureNode node = (HistoryTreeChildProcedureNode) obj;
			return node.getName() + " at line " + node.getLine(); 
		}
		else
		{
			return obj.toString();
		}
	}

	/**************************************************************************
	 * 
	 *************************************************************************/
	@Override
	public Image getImage(Object obj)
	{
		return m_procImage;
	}

	/**************************************************************************
	 * 
	 *************************************************************************/
	@Override
	public Color getForeground(Object element)
	{
		Color result = null;
		
		if (element instanceof HistoryTreeRootNode)
		{
			HistoryTreeRootNode node = (HistoryTreeRootNode) element;
			if (node.isActive())
			{
				result = Display.getCurrent().getSystemColor(SWT.COLOR_BLUE);
			}
			else
			{
				result = Display.getCurrent().getSystemColor(SWT.COLOR_BLACK);
			}
		}
		else if (element instanceof HistoryTreeChildProcedureNode)
		{
			HistoryTreeChildProcedureNode node = (HistoryTreeChildProcedureNode) element;
			if (node.isActive())
			{
				result = Display.getCurrent().getSystemColor(SWT.COLOR_BLUE);
			}
			else if (node.isExecuted())
			{
				result = Display.getCurrent().getSystemColor(SWT.COLOR_BLACK);
			}
			else
			{
				result = Display.getCurrent().getSystemColor(SWT.COLOR_GRAY);
			}
		}
		
		return result;
	}

	/**************************************************************************
	 * 
	 *************************************************************************/
	@Override
    public Font getFont(Object element)
    {
		Font result = null;
		if (element instanceof HistoryTreeRootNode)
		{
			result = m_activeFont;
		}
		else if (element instanceof HistoryTreeChildProcedureNode)
		{
			HistoryTreeChildProcedureNode node = (HistoryTreeChildProcedureNode) element;
			if (node.isActive())
			{
				result = m_activeFont;
			}
			else if (node.isExecuted())
			{
				result = m_activeFont;
			}
			else
			{
				result = m_neverExecutedFont;
			}
		}
		return result;
    }

	/**************************************************************************
	 * 
	 *************************************************************************/
	@Override
	public Color getBackground(Object element)
	{
		Color result = null;
		if (element instanceof HistoryTreeChildProcedureNode)
		{
			HistoryTreeChildProcedureNode node = (HistoryTreeChildProcedureNode) element;
			if (node.isActive())
			{
				result = null;
			}
			else if (node.isExecuted())
			{
				result = null; 
			}
			else
			{
				result = null;
			}
		}
		return result;
	}

	/**************************************************************************
	 * Disposal.
	 *************************************************************************/
	@Override
	public void dispose()
	{
		// Do not dispose the active font!
		m_neverExecutedFont.dispose();
		m_procImage.dispose();
	}

	/**************************************************************************
	 * 
	 *************************************************************************/
	public String getToolTipText( Object element )
	{
		HistoryTreeNode node = (HistoryTreeNode) element;
		if (node.isExecuted())
		{
			return node.getInstanceId();
		}
		else
		{
			return node.getProcId();
		}
	}

	/**************************************************************************
	 * 
	 *************************************************************************/
	@Override
    public void update(ViewerCell cell)
    {
		Object element = cell.getElement();
	    cell.setText(getText(element));
	    cell.setBackground(getBackground(element));
	    cell.setForeground(getForeground(element));
	    cell.setImage(getImage(element));
	    cell.setFont(getFont(element));
    }

}
