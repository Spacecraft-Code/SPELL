///////////////////////////////////////////////////////////////////////////////
//
// PACKAGE   : com.astra.ses.spell.gui.views
// 
// FILE      : LogFileView.java
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
package com.astra.ses.spell.gui.views;

import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;

import com.astra.ses.spell.gui.core.model.files.LogFile;
import com.astra.ses.spell.gui.views.providers.LogFileLabelProvider;

/*******************************************************************************
 * LogFile view extends tabbed view setting background color
 * 
 ******************************************************************************/
public class LogFileView extends TabbedView
{
	/** Browser ID used by main plugin for preparing the perspective layout */
	public static final String	ID	= "com.astra.ses.spell.gui.views.LogFileView";

	/***************************************************************************
	 * Constructor
	 **************************************************************************/
	public LogFileView()
	{
		super();
	}

	/**************************************************************************
	 * Readjust comments width
	 *************************************************************************/
	protected void readjustWidth()
	{
		int lastColumnIndex = getTable().getColumnCount() - 1;
		int maxLen = 0;
		GC gc = new GC(Display.getCurrent());
		TableItem[] items = getTable().getItems();
		for (TableItem item : items)
		{
			String text = item.getText(lastColumnIndex);
			int len = gc.textExtent(text).x;
			if (len > maxLen) maxLen = len;
		}
		gc.dispose();
		gc.dispose();
		getTable().getColumn(lastColumnIndex).setWidth(maxLen + 5);
	}

	/**************************************************************************
	 * Initialize contents
	 *************************************************************************/
	@Override
	public void initContents()
	{
		super.initContents();
		getViewer().setLabelProvider(new LogFileLabelProvider());
		readjustWidth();
	}

	/**************************************************************************
	 * Refresh view
	 *************************************************************************/
	@Override
	public void refreshView()
	{
		super.refreshView();
		readjustWidth();
	}

	/***************************************************************************
	 * Dimension the columns
	 **************************************************************************/
	@Override
	public void controlResized(ControlEvent arg0)
	{
		Table table = getTable();
		int lastColumnIndex = table.getColumnCount() - 1;
		int tableWidth = table.getClientArea().width;
		int[] headerLabelsSize = ((LogFile) getTabbedFile()).getHeaderLabelsSize();
		int count = 0;
		for (TableColumn col : table.getColumns())
		{
			if (count == lastColumnIndex) break;
			int width = (int) (tableWidth * (((double) headerLabelsSize[count]) / 100.0));
			col.setWidth(width);
			count++;
		}
	}
}
