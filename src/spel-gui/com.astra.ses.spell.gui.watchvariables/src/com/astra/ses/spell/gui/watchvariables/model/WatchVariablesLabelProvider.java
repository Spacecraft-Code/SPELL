////////////////////////////////////////////////////////////////////////////////
//
// PACKAGE   : com.astra.ses.spell.gui.watchvariables.model
// 
// FILE      : WatchVariablesLabelProvider.java
//
// DATE      : Sep 22, 2010
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

import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.jface.viewers.ITableColorProvider;
import org.eclipse.jface.viewers.ITableFontProvider;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Display;

import com.astra.ses.spell.gui.watchvariables.notification.VariableData;

/******************************************************************************
 * Provides the labels and icons for the callstack tree.
 * 
 *****************************************************************************/
public class WatchVariablesLabelProvider implements ITableLabelProvider, ITableFontProvider, ITableColorProvider
{
	/** Holds the bold font for globals */
	private Font m_boldFont;
	/** Holds the normal font for locals */
	private Font m_normalFont;
	/** Holds the color for notified items */
	//private Color m_notifiedBgColor;
	/** notified element foreground color */
	//private Color m_notifiedFgColor;
	/** Holds the scope changed color */
	private Color m_scopeChangedColor;

	/**************************************************************************
	 * 
	 *************************************************************************/
	public WatchVariablesLabelProvider()
	{
		m_boldFont = new Font(Display.getDefault(), "Monospace", 9, SWT.BOLD);
		m_normalFont = new Font(Display.getDefault(), "Monospace", 9, SWT.NONE);
		//m_notifiedBgColor = new Color(Display.getDefault(), 21, 80, 122);
		//m_notifiedFgColor = Display.getDefault().getSystemColor(SWT.COLOR_WHITE);
		m_scopeChangedColor = new Color(Display.getDefault(), 218, 228, 235);
	}

	/**************************************************************************
	 * 
	 *************************************************************************/
	@Override
	public void addListener(ILabelProviderListener listener)
	{
	}

	/**************************************************************************
	 * 
	 *************************************************************************/
	@Override
	public void removeListener(ILabelProviderListener listener)
	{
	}

	/**************************************************************************
	 * 
	 *************************************************************************/
	@Override
	public void dispose()
	{
		m_boldFont.dispose();
		m_normalFont.dispose();
		//m_notifiedFgColor.dispose();
		//m_notifiedBgColor.dispose();
		m_scopeChangedColor.dispose();
	}

	/**************************************************************************
	 * 
	 *************************************************************************/
	@Override
	public boolean isLabelProperty(Object element, String property)
	{
		return false;
	}

	/**************************************************************************
	 * 
	 *************************************************************************/
	@Override
	public Image getColumnImage(Object element, int columnIndex)
	{
		return null;
	}

	/**************************************************************************
	 * 
	 *************************************************************************/
	@Override
	public String getColumnText(Object element, int columnIndex)
	{
		String text = null;
		VariableData data = (VariableData) element;
		switch (WatchVariablesTableColumns.fromIndex(columnIndex))
		{
		case NAME_COLUMN:
			text = data.getName();
			break;
		case VALUE_COLUMN:
			boolean complex = (data.getType().indexOf("dict") != -1) || (data.getType().indexOf("list") != -1)
			        || (data.getType().indexOf("adapter.databases") != -1) || (data.getType().indexOf("instance") != -1);
			if (complex)
			{
				text = "[...]";
			}
			else
			{
				text = data.getValue();
			}
			break;
		}
		return text;
	}

	/**************************************************************************
	 * 
	 *************************************************************************/
	@Override
	public Font getFont(Object element, int columnIndex)
	{
		VariableData data = (VariableData) element;
		if (data.isGlobal())
		{
			return m_boldFont;
		}
		return m_normalFont;
	}

	/**************************************************************************
	 * 
	 *************************************************************************/
	@Override
	public Color getForeground(Object element, int columnIndex)
	{
		return null;
	}

	/**************************************************************************
	 * 
	 *************************************************************************/
	@Override
	public Color getBackground(Object element, int columnIndex)
	{
		return null;
	}
}
