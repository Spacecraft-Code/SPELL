///////////////////////////////////////////////////////////////////////////////
//
// PACKAGE   : com.astra.ses.spell.gui.presentation.code.controls
// 
// FILE      : BpRenderer.java
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
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Display;

import com.astra.ses.spell.gui.core.model.types.BreakpointType;
import com.astra.ses.spell.gui.presentation.code.Activator;
import com.astra.ses.spell.gui.presentation.code.CodeModelProxy;
import com.astra.ses.spell.gui.procs.interfaces.model.ICodeLine;
import com.astra.ses.spell.gui.procs.interfaces.model.IProcedure;


public class BpRenderer extends SourceRenderer
{
	private Image[] breakpointImages;
	
	public BpRenderer( IProcedure model, CodeModelProxy proxy )
	{
		super(model,proxy);
		breakpointImages = new Image[2];
		breakpointImages[BreakpointType.PERMANENT.ordinal()] = Activator.getImageDescriptor("icons/breakpoint_permanent.png")
                 .createImage();
        breakpointImages[BreakpointType.TEMPORARY.ordinal()] = Activator.getImageDescriptor("icons/breakpoint_temporary.png")
                 .createImage();
        assert(breakpointImages[0] != null);
        assert(breakpointImages[1] != null);
	}
	
	public void dispose()
	{
		for(Image img : breakpointImages)
		{
			img.dispose();
		}
	}
	
	@Override
	public void paint(GC gc, Object value)
	{
		GridItem item = (GridItem) value;
		ICodeLine line = getLineModel(item);
		if (line == null) return;

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
			gc.setForeground(Display.getDefault().getSystemColor(SWT.COLOR_BLACK));
		}

		if (drawBackground)
		{
			gc.fillRectangle(getBounds().x, getBounds().y-1, getBounds().width, getBounds().height+2);
		}
	
		if (isLastLine(item))
		{
			gc.drawLine(getBounds().x-1, getBounds().y + getBounds().height, getBounds().x + getBounds().width, getBounds().y + getBounds().height);
		}

		try
		{
			BreakpointType btype = line.getBreakpoint();
			if (btype != null)
			{
				if (!btype.equals(BreakpointType.UNKNOWN))
                {
                    Image breakpointImage = breakpointImages[btype.ordinal()];
                    int width = breakpointImage.getBounds().width;
                    int height = breakpointImage.getBounds().height;
                    int x = getBounds().x + (getBounds().width - width) / 2;
                    int y = getBounds().y + (getBounds().height - height) / 2;
                    gc.drawImage(breakpointImage, 0, 0, width, height, x, y, width, height);
                }
			}
		}
		catch(Exception ignore) {};
	}
}
