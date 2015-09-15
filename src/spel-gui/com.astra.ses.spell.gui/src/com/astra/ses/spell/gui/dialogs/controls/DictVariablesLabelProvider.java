///////////////////////////////////////////////////////////////////////////////
//
// PACKAGE   : com.astra.ses.spell.gui.dialogs.controls
// 
// FILE      : DictVariablesLabelProvider.java
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
package com.astra.ses.spell.gui.dialogs.controls;

import org.eclipse.jface.viewers.ITableColorProvider;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Display;

import com.astra.ses.spell.gui.core.model.types.DataVariable;
import com.astra.ses.spell.gui.core.model.types.ValueType;

/**
 * @author Rafael Chinchilla
 *
 */
public class DictVariablesLabelProvider extends LabelProvider implements ITableLabelProvider, ITableColorProvider 
{
	public DictVariablesLabelProvider()
	{
		super();
	}
	
    @Override
    public void dispose()
    {
    	super.dispose();
    }

    @Override
    public Image getColumnImage(Object element, int columnIndex)
    {
	    return null;
    }
    
    @Override
    public String getColumnText(Object element, int columnIndex)
    {
    	if (element instanceof DataVariable)
    	{
		    DataVariable var = (DataVariable) element;
		    DictVariablesTableItems item = DictVariablesTableItems.index(columnIndex);
		    switch(item)
		    {
		    case NAME:
		    	return var.getName();
		    case VALUE:
		    	return var.formatValue(var.getValue());
		    case CONFIRM:
		    	return var.getConfirmGet();
		    case FORMAT:
		    	return var.getFormat();
		    case RANGE_EXPECTED:
		    	if (var.getExpected() != null)
		    	{
		    		String[] formatted = new String[var.getExpected().length];
		    		int index= 0;
		    		for(String exp : var.getExpected())
		    		{
		    			formatted[index] = var.formatValue(exp);
    					index++;
		    		}
		    		String str = "";
		    		for(String f : formatted)
		    		{
		    			if (!str.isEmpty()) str += ", ";
		    			str += f;
		    		}
		    		return str;
		    	}
		    	else if (var.getRange() != null)
		    	{
		    		String[] formatted = new String[2];
		    		int index= 0;
		    		for(String rng : var.getRange())
		    		{
		    			formatted[index] = var.formatValue(rng);
    					index++;
		    		}
		    		String str = "";
		    		for(String f : formatted)
		    		{
		    			if (!str.isEmpty()) str += ", ";
		    			str += f;
		    		}
		    		return str;
		    	}
		    	else
		    	{
		    		return "";
		    	}
		    case TYPE:
		    	if (var.getType().equals(ValueType.DATETIME.name()))
		    	{
	    			return "DATE/TIME";
		    	}
		    	else if (var.getType().equals(ValueType.RELTIME.name()))
		    	{
	    			return "REL. TIME";
		    	}
		    	return var.getType();
		    }
    	}
    	else if (columnIndex==0)
    	{
    		return element.toString();
    	}
    	return null;
    }

    @Override
    public Color getForeground(Object element, int columnIndex)
    {
	    return Display.getDefault().getSystemColor(SWT.COLOR_BLACK);
    }

    @Override
    public Color getBackground(Object element, int columnIndex)
    {
	    DataVariable var = (DataVariable) element;
	    if (var.isModified())
	    {
	    	return Display.getDefault().getSystemColor(SWT.COLOR_YELLOW);
	    }
    	return null;
    }

}
