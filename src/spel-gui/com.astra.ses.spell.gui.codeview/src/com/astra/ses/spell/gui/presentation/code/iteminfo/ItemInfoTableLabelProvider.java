///////////////////////////////////////////////////////////////////////////////
//
// PACKAGE   : com.astra.ses.spell.gui.presentation.code.controls
// 
// FILE      : ItemInfoTableLabelProvider.java
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
package com.astra.ses.spell.gui.presentation.code.iteminfo;

import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.jface.viewers.ITableColorProvider;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Image;

import com.astra.ses.spell.gui.core.interfaces.ServiceManager;
import com.astra.ses.spell.gui.preferences.interfaces.IConfigurationManager;

/***************************************************************************
 * 
 * {@link ItemInfoTableLabelProvider} helps rendering the different item
 * notifications
 * 
 **************************************************************************/
class ItemInfoTableLabelProvider implements ITableLabelProvider, ITableColorProvider
{
	private static IConfigurationManager s_cfg = null;

	/***********************************************************************
	 * Constructor
	 **********************************************************************/
	public ItemInfoTableLabelProvider()
	{
		if (s_cfg == null)
		{
			s_cfg = (IConfigurationManager) ServiceManager.get(IConfigurationManager.class);
		}
	}

	@Override
	public String getColumnText(Object element, int columnIndex)
	{
		ItemInfoElement info = (ItemInfoElement) element;
		ItemInfoTableColumn column = ItemInfoTableColumn.values()[columnIndex];
		switch (column)
		{
		case EXECUTION:
			return info.execution;
		case NAME:
			String name = info.name;
			if (name.indexOf("@") != -1)
			{
				name = name.split("@")[1];
			}
			return name;
		case VALUE:
			return info.value;
		case STATUS:
			return info.status.getName();
		case TIME:
			return info.time;
		case COMMENTS:
			return info.comments;
		default:
			return "";
		}
	}

	@Override
	public Color getBackground(Object element, int columnIndex)
	{
		ItemInfoElement info = (ItemInfoElement) element;
		if (columnIndex == ItemInfoTableColumn.STATUS.ordinal())
		{
			return s_cfg.getStatusColor(info.status);
		}
		return null;
	}

	@Override
	public Color getForeground(Object element, int columnIndex)
	{
		return null;
	}

	@Override
	public void addListener(ILabelProviderListener listener)
	{
	}

	@Override
	public boolean isLabelProperty(Object element, String property)
	{
		return false;
	}

	@Override
	public Image getColumnImage(Object element, int columnIndex)
	{
		return null;
	}

	@Override
	public void removeListener(ILabelProviderListener listener)
	{
	}

	@Override
    public void dispose()
    {
    }
}
