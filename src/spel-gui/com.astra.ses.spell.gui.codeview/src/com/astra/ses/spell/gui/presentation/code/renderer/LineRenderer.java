///////////////////////////////////////////////////////////////////////////////
//
// PACKAGE   : com.astra.ses.spell.gui.presentation.code.controls
// 
// FILE      : LineRenderer.java
//
// DATE      : Nov 21, 2012
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
package com.astra.ses.spell.gui.presentation.code.renderer;

import org.eclipse.nebula.widgets.grid.GridItem;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.widgets.Display;

import com.astra.ses.spell.gui.presentation.code.CodeModelProxy;
import com.astra.ses.spell.gui.procs.interfaces.model.ICodeLine;
import com.astra.ses.spell.gui.procs.interfaces.model.IProcedure;


public class LineRenderer extends SourceRenderer
{
	public LineRenderer( IProcedure model, CodeModelProxy proxy )
	{
		super(model, proxy);
	}
	
	@Override
	public void paint(GC gc, Object value)
	{
		GridItem item = (GridItem) value;
		ICodeLine line = getLineModel(item);
		if (line == null) return;

		gc.setFont(getFont());

		boolean drawAsSelected = isSelected();

		boolean drawBackground = true;

		if (isCellSelected())
		{
			drawAsSelected = true;
		}

		if (drawAsSelected)
		{
			gc.setBackground(getDisplay().getSystemColor(SWT.COLOR_LIST_SELECTION));
			gc.setForeground(getDisplay().getSystemColor(SWT.COLOR_LIST_SELECTION_TEXT));
		}
		else
		{
			if (item.getParent().isEnabled())
			{
				Color back = getBackground(line);

				if (back != null)
				{
					gc.setBackground(back);
				}
				else
				{
					drawBackground = false;
				}
			}
			else
			{
				gc.setBackground(getDisplay().getSystemColor(SWT.COLOR_WIDGET_BACKGROUND));
			}
			gc.setForeground(item.getForeground());
		}

		if (drawBackground)
		{
			gc.fillRectangle(getBounds().x, getBounds().y-1, getBounds().width, getBounds().height+2);
		}
		
		int x = m_leftMargin;
		int y = getBounds().y + m_textTopMargin + m_topMargin;
		int width = getBounds().width - x - m_rightMargin;

		if (drawAsSelected)
		{
			gc.setForeground(getDisplay().getSystemColor(SWT.COLOR_LIST_SELECTION_TEXT));
		}
		else
		{
			gc.setForeground( Display.getCurrent().getSystemColor(SWT.COLOR_BLACK));
		}

		createTextLayout(gc,item);

		String text = item.getText(getColumn());
		text = text.replace("\t", "    ");
		text = getShortString(gc, text, width,true);

		x += setupAlignment( gc, text, width );
		

		gc.drawString(text, getBounds().x + x, y );

		gc.drawLine(getBounds().x-1, getBounds().y-1, getBounds().x-1, getBounds().y + getBounds().height+1);
		
		if (isLastLine(item))
		{
			gc.drawLine(getBounds().x-1, getBounds().y + getBounds().height, getBounds().x + getBounds().width, getBounds().y + getBounds().height);
		}
	}

}
