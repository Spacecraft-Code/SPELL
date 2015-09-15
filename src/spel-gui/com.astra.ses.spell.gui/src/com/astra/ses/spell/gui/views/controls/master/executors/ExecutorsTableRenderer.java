///////////////////////////////////////////////////////////////////////////////
//
// PACKAGE   : com.astra.ses.spell.gui.views.controls.master
// 
// FILE      : ExecutorsTableRenderer.java
//
// DATE      : Jun 30, 2014
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
package com.astra.ses.spell.gui.views.controls.master.executors;

import org.eclipse.nebula.widgets.grid.Grid;
import org.eclipse.nebula.widgets.grid.GridItem;
import org.eclipse.nebula.widgets.grid.internal.DefaultCellRenderer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Display;

import com.astra.ses.spell.gui.core.interfaces.IProcedureClient;
import com.astra.ses.spell.gui.procs.interfaces.model.IProcedure;

@SuppressWarnings("restriction")
public class ExecutorsTableRenderer extends DefaultCellRenderer
{
	private Grid m_grid;
	private ExecutorsModel m_model;
	private ExecutorsTableColumns m_column;
	private final int MARGIN = 2;

	public ExecutorsTableRenderer(Grid grid, ExecutorsTableColumns column, ExecutorsModel model)
	{
		m_grid = grid;
		m_column = column;
		m_model = model;
	}

	@Override
	public Rectangle getTextBounds(GridItem item, boolean preferred)
	{
		// Deactivate tooltip. There is an annoying bug in the Grid
		// control
		// that makes it flicker and other erratic behaviour.
		return null;
	}

	@Override
	public void paint(GC gc, Object value)
	{
		if (m_column.equals(ExecutorsTableColumns.FLAGS))
		{
			GridItem item = (GridItem) value;
			int index = m_grid.indexOf(item);
			paintFlags(gc, m_model.elementAt(index));
		}
		else
		{
			super.paint(gc, value);
		}
	}

	/***************************************************************************
	 * Paint the images for the procedure flags (controlled, monitored,
	 * background
	 **************************************************************************/
	private void paintFlags(GC gc, IProcedure model)
	{
		Rectangle bounds = getBounds();

		final int qw = (bounds.width - 4 * MARGIN) / 3;
		final int qh = bounds.height - 2 * MARGIN;
		int x = bounds.x + MARGIN;

		// Draw the squares for the flags
		gc.setForeground(Display.getDefault().getSystemColor(SWT.COLOR_BLACK));
		gc.drawRectangle(x, bounds.y + MARGIN, qw, qh);
		x += qw + MARGIN;
		gc.drawRectangle(x, bounds.y + MARGIN, qw, qh);
		x += qw + MARGIN;
		gc.drawRectangle(x, bounds.y + MARGIN, qw, qh);

		// Draw the bottom border of the cell
		gc.setForeground(Display.getDefault().getSystemColor(SWT.COLOR_WIDGET_LIGHT_SHADOW));
		gc.drawRectangle(bounds.x - 1, bounds.y, bounds.width, bounds.height);

		// Font for the flags (disposed at the end)
		Font f = new Font(gc.getDevice(), "Arial", 12, SWT.BOLD);
		gc.setFont(f);

		// CONTROL FLAG
		IProcedureClient clt = model.getRuntimeInformation().getControllingClient();
		x = bounds.x + MARGIN;
		if (clt != null)
		{
			gc.setForeground(Display.getDefault().getSystemColor(SWT.COLOR_BLACK));
			gc.setBackground(Display.getDefault().getSystemColor(SWT.COLOR_GREEN));
			gc.fillRectangle(x + 1, bounds.y + MARGIN + 1, qw - 1, qh - 1);
		}
		else
		{
			gc.setForeground(Display.getDefault().getSystemColor(SWT.COLOR_WIDGET_LIGHT_SHADOW));
			gc.setBackground(Display.getDefault().getSystemColor(SWT.COLOR_WHITE));
		}
		Point e = gc.textExtent("C");
		gc.drawText("C", x + qw / 2 - e.x / 2, bounds.y + MARGIN + qh / 2 - e.y / 2);

		// MONITOR FLAG
		IProcedureClient[] mclt = model.getRuntimeInformation().getMonitoringClients();
		x = bounds.x + qw + 2 * MARGIN;
		if (mclt != null && mclt.length > 0)
		{
			gc.setForeground(Display.getDefault().getSystemColor(SWT.COLOR_BLACK));
			gc.setBackground(Display.getDefault().getSystemColor(SWT.COLOR_CYAN));
			gc.fillRectangle(x + 1, bounds.y + MARGIN + 1, qw - 1, qh - 1);
		}
		else
		{
			gc.setForeground(Display.getDefault().getSystemColor(SWT.COLOR_WIDGET_LIGHT_SHADOW));
			gc.setBackground(Display.getDefault().getSystemColor(SWT.COLOR_WHITE));
		}
		e = gc.textExtent("M");
		gc.drawText("M", x + qw / 2 - e.x / 2, bounds.y + MARGIN + qh / 2 - e.y / 2);

		// BACKGROUND FLAG
		x = bounds.x + 2 * qw + 4 * MARGIN;
		if (model.getRuntimeInformation().isBackground())
		{
			gc.setForeground(Display.getDefault().getSystemColor(SWT.COLOR_BLACK));
			gc.setBackground(Display.getDefault().getSystemColor(SWT.COLOR_MAGENTA));
			gc.fillRectangle(x-1, bounds.y + MARGIN + 1, qw - 1, qh - 1);
		}
		else
		{
			gc.setForeground(Display.getDefault().getSystemColor(SWT.COLOR_WIDGET_LIGHT_SHADOW));
			gc.setBackground(Display.getDefault().getSystemColor(SWT.COLOR_WHITE));
		}
		e = gc.textExtent("B");
		gc.drawText("B", x + qw / 2 - e.x / 2, bounds.y + MARGIN + qh / 2 - e.y / 2);

		f.dispose();
	}

}
