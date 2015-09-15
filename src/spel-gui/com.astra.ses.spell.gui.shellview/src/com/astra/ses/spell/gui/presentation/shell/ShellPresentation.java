///////////////////////////////////////////////////////////////////////////////
//
// PACKAGE   : com.astra.ses.spell.gui.presentation.shell
// 
// FILE      : ShellPresentation.java
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
package com.astra.ses.spell.gui.presentation.shell;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;

import com.astra.ses.spell.gui.core.model.notification.DisplayData;
import com.astra.ses.spell.gui.core.model.notification.ItemNotification;
import com.astra.ses.spell.gui.core.model.types.ExecutionMode;
import com.astra.ses.spell.gui.interfaces.IPresentationNotifier;
import com.astra.ses.spell.gui.interfaces.ProcedurePresentationAdapter;
import com.astra.ses.spell.gui.interfaces.listeners.IGuiProcedureItemsListener;
import com.astra.ses.spell.gui.interfaces.listeners.IGuiProcedureMessageListener;
import com.astra.ses.spell.gui.presentation.shell.controls.ShellTerminal;
import com.astra.ses.spell.gui.procs.interfaces.model.IProcedure;

public class ShellPresentation extends ProcedurePresentationAdapter implements
        IGuiProcedureMessageListener, IGuiProcedureItemsListener
{
	private static final String	ID	               = "com.astra.ses.spell.gui.presentation.Shell";
	private static final String	PRESENTATION_TITLE	= "Shell";
	private static final String	PRESENTATION_DESC	= "Shell in procedure environment";
	private static final String	PRESENTATION_ICON	= "icons/16x16/terminal.png";

	/** Holds the terminal control */
	private ShellTerminal	    m_terminal;

	@Override
	public Composite createContents(IProcedure model, Composite stack)
	{
		Composite shellPage = new Composite(stack, SWT.NONE);
		GridLayout groupLayout = new GridLayout();
		groupLayout.marginHeight = 0;
		groupLayout.marginWidth = 0;
		groupLayout.marginBottom = 0;
		groupLayout.marginTop = 0;
		groupLayout.verticalSpacing = 0;
		groupLayout.numColumns = 1;
		shellPage.setLayout(groupLayout);
		GridData data = new GridData();
		data.grabExcessHorizontalSpace = true;
		data.grabExcessVerticalSpace = true;
		data.verticalAlignment = SWT.FILL;
		shellPage.setLayoutData(data);

		m_terminal = new ShellTerminal(shellPage, SWT.BORDER, model);
		m_terminal.setLayoutData(new GridData(GridData.FILL_BOTH));

		return shellPage;
	}

	@Override
	public void subscribeNotifications(IPresentationNotifier notifier)
	{
		notifier.addMessageListener(this);
		notifier.addItemListener(this);
	}

	@Override
	public String getExtensionId()
	{
		return ID;
	}

	@Override
	public String getTitle()
	{
		return PRESENTATION_TITLE;
	}

	@Override
	public Image getIcon()
	{
		return Activator.getImageDescriptor(PRESENTATION_ICON).createImage();
	}

	@Override
	public String getDescription()
	{
		return PRESENTATION_DESC;
	}

	@Override
	public void zoom(boolean zoomIn)
	{
		m_terminal.zoom(zoomIn);
	}

	@Override
	public void setSelected(boolean selected)
	{
		m_terminal.setFocus();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.astra.ses.spell.gui.interfaces.IProcedureMessageListener#notifyDisplay
	 * (com.astra.ses.spell.gui.procs.interfaces.model.IProcedure,
	 * com.astra.ses.spell.gui.core.model.notification.DisplayData)
	 */
	@Override
	public void notifyDisplay(IProcedure model, DisplayData data)
	{
		if (!data.getExecutionMode().equals(ExecutionMode.MANUAL)) return;
		switch (data.getSeverity())
		{
		case ERROR:
			m_terminal.addOutput("ERROR: " + data.getMessage());
			break;
		case WARN:
			m_terminal.addOutput("WARNING: " + data.getMessage());
			break;
		default:
			m_terminal.addOutput(data.getMessage());
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.astra.ses.spell.gui.interfaces.IProcedurePresentation#showLine(int)
	 */
	@Override
	public void showLine(int lineNo)
	{
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.astra.ses.spell.gui.interfaces.IProcedureItemsListener#notifyItem
	 * (com.astra.ses.spell.gui.procs.interfaces.model.IProcedure,
	 * com.astra.ses.spell.gui.core.model.notification.ItemNotification)
	 */
	@Override
	public void notifyItem(IProcedure model, ItemNotification data)
	{
		if (!data.getExecutionMode().equals(ExecutionMode.MANUAL)) return;
		// TODO
	}

	@Override
    public String getListenerId()
    {
	    return "Shell presentation";
    }
}
