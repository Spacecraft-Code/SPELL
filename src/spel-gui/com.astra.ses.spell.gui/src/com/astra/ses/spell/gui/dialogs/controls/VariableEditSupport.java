///////////////////////////////////////////////////////////////////////////////
//
// PACKAGE   : com.astra.ses.spell.gui.dialogs.controls
// 
// FILE      : VariableEditSupport.java
//
// DATE      : Feb 6, 2012
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
package com.astra.ses.spell.gui.dialogs.controls;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.ComboBoxCellEditor;
import org.eclipse.jface.viewers.EditingSupport;
import org.eclipse.jface.viewers.TextCellEditor;
import org.eclipse.swt.SWT;

import com.astra.ses.spell.gui.core.model.types.DataVariable;

public class VariableEditSupport extends EditingSupport
{
	private DictVariablesTable m_viewer;
	private DictVariablesTableItems m_forItem;
	
	public VariableEditSupport( DictVariablesTable viewer, DictVariablesTableItems forItem)
    {
	    super(viewer);
	    m_viewer = viewer;
	    m_forItem = forItem;
    }

	@Override
    protected CellEditor getCellEditor(Object element)
    {
		DataVariable var = (DataVariable) element;
		switch(m_forItem)
		{
		case VALUE:
		    return new TextCellEditor(m_viewer.getTable());
		case CONFIRM:
			if (var.isTyped())
			{
				String[] confirm = new String[2];
				confirm[0] = "True";
				confirm[1] = "False";
				ComboBoxCellEditor editor = new ComboBoxCellEditor(m_viewer.getTable(), confirm);
				editor.setStyle( SWT.READ_ONLY );
				if (var.getConfirmGet().equals("True"))
				{
					editor.setValue(0);
				}
				else
				{
					editor.setValue(1);
				}
				return editor;
			}
		}
		return null;
    }

	@Override
    protected boolean canEdit(Object element)
    {
		DataVariable var = (DataVariable) element;
	    switch(m_forItem)
	    {
	    case VALUE:
	    	return true;
	    case CONFIRM:
	    	return var.isTyped();
    	default:
    		return false;
	    }
    }

	@Override
    protected Object getValue(Object element)
    {
		DataVariable var = (DataVariable) element;
		switch(m_forItem)
		{
		case VALUE:
		    return var.formatValue(var.getValue());
		case CONFIRM:
			if (var.isTyped())
			{
			    String cf = var.getConfirmGet();
			    if (cf.equals("True"))
			    {
			    	return 0;
			    }
			    else
			    {
			    	return 1;
			    }
			}
			else
			{
				return 1;
			}
		}
		return null;
    }

	@Override
    protected void setValue(Object element, Object value)
    {
		DataVariable var = (DataVariable) element;
		switch(m_forItem)
		{
		case VALUE:
			try
			{
				var.setValue( (String) value );
			}
			catch(Exception ex)
			{
				MessageDialog.openError(m_viewer.getTable().getShell(), "Set value", "Cannot set variable value: \n" + ex.getLocalizedMessage());
			}
		    m_viewer.valueEdited();
		    break;
		case CONFIRM:
			if (var.isTyped())
			{
				try
				{
					int idx = (Integer) value;
					if (idx == 0)
					{
						var.setConfirmGet(true);
					}
					else
					{
						var.setConfirmGet(false);
					}
				}
				catch(Exception ex)
				{
					MessageDialog.openError(m_viewer.getTable().getShell(), "Set confirm", "Cannot set confirm flag: \n" + ex.getLocalizedMessage());
				}
			    m_viewer.valueEdited();
			}
		    break;
		}
    }

}
