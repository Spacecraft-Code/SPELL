///////////////////////////////////////////////////////////////////////////////
//
// PACKAGE   : com.astra.ses.spell.gui.presentation.code
// 
// FILE      : CodePresentation.java
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
package com.astra.ses.spell.gui.presentation.code;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;

import com.astra.ses.spell.gui.core.model.notification.ErrorData;
import com.astra.ses.spell.gui.core.model.notification.ItemNotification;
import com.astra.ses.spell.gui.core.model.notification.StatusNotification;
import com.astra.ses.spell.gui.core.model.types.ExecutionMode;
import com.astra.ses.spell.gui.interfaces.IPresentationNotifier;
import com.astra.ses.spell.gui.interfaces.ProcedurePresentationAdapter;
import com.astra.ses.spell.gui.interfaces.listeners.IGuiProcedureItemsListener;
import com.astra.ses.spell.gui.interfaces.listeners.IGuiProcedurePromptListener;
import com.astra.ses.spell.gui.interfaces.listeners.IGuiProcedureStatusListener;
import com.astra.ses.spell.gui.presentation.code.controls.CodeViewer;
import com.astra.ses.spell.gui.presentation.code.controls.CodeViewerContentProvider;
import com.astra.ses.spell.gui.presentation.code.controls.CodeViewerLabelProvider2;
import com.astra.ses.spell.gui.procs.interfaces.model.IProcedure;
import com.astra.ses.spell.gui.types.ExecutorStatus;

public class CodePresentation extends ProcedurePresentationAdapter implements IGuiProcedurePromptListener, IGuiProcedureStatusListener,
        IGuiProcedureItemsListener
{
	private static final String ID = "com.astra.ses.spell.gui.presentation.CodeView";
	private static final String PRESENTATION_TITLE = "Tabular";
	private static final String PRESENTATION_DESC = "Procedure view in source code";
	private static final String PRESENTATION_ICON = "icons/16x16/code.png";

	/** Code Viewer */
	private CodeViewer m_codeViewer;
	/** Parent view */
	private IProcedure m_model;
	/** Label provider for the viewer */
	private CodeViewerLabelProvider2 m_labelProvider;

	/**************************************************************************
	 * 
	 *************************************************************************/
	@Override
	public Composite createContents(IProcedure model, final Composite stack)
	{
		m_model = model;

		final Composite codePage = new Composite(stack, SWT.NONE);
		GridLayout codeLayout = new GridLayout(1,true);
		codeLayout.marginTop = 0;
		codeLayout.marginBottom = 0;
		codeLayout.marginLeft = 0;
		codeLayout.marginRight = 0;
        codePage.setLayout( codeLayout );
        
        codePage.setLayoutData( new GridData( GridData.FILL_BOTH ));

		// Create the viewer, main control.
		m_codeViewer = new CodeViewer(codePage, m_model);
		m_codeViewer.setContentProvider(new CodeViewerContentProvider());
		m_labelProvider = new CodeViewerLabelProvider2();
		m_codeViewer.setLabelProvider(m_labelProvider);
		m_codeViewer.setModel();
		m_codeViewer.getGrid().setLayoutData(new GridData(GridData.FILL_BOTH));

		return codePage;
	}

	/**************************************************************************
	 * 
	 *************************************************************************/
	@Override
	public void subscribeNotifications(IPresentationNotifier notifier)
	{
		notifier.addPromptListener(this);
		notifier.addStatusListener(this);
		notifier.addItemListener(this);
	}

	/**************************************************************************
	 * 
	 *************************************************************************/
	@Override
	public String getExtensionId()
	{
		return ID;
	}

	/**************************************************************************
	 * 
	 *************************************************************************/
	@Override
	public String getTitle()
	{
		return PRESENTATION_TITLE;
	}

	/**************************************************************************
	 * 
	 *************************************************************************/
	@Override
	public Image getIcon()
	{
		return Activator.getImageDescriptor(PRESENTATION_ICON).createImage();
	}

	/**************************************************************************
	 * 
	 *************************************************************************/
	@Override
	public String getDescription()
	{
		return PRESENTATION_DESC;
	}

	/**************************************************************************
	 * 
	 *************************************************************************/
	@Override
	public void zoom(boolean zoomIn)
	{
		m_codeViewer.zoom(zoomIn);
	}

	/**************************************************************************
	 * 
	 *************************************************************************/
	@Override
	public void showLine(int lineNo)
	{
		m_codeViewer.showLine(lineNo, true);
	}

	/**************************************************************************
	 * 
	 *************************************************************************/
	@Override
	public void notifyPrompt(IProcedure model)
	{
		try
		{
			m_codeViewer.showCurrentLine();
		}
		catch(Exception ex)
		{
			ex.printStackTrace();
		}
	}

	/**************************************************************************
	 * 
	 *************************************************************************/
	@Override
	public void notifyFinishPrompt(IProcedure model)
	{
	}

	/**************************************************************************
	 * 
	 *************************************************************************/
	@Override
	public void notifyCancelPrompt(IProcedure model)
	{
	}

	/**************************************************************************
	 * 
	 *************************************************************************/
	@Override
	public void notifyStatus(IProcedure model, StatusNotification data)
	{
		try
		{
			m_codeViewer.setExecutorStatus(data.getStatus());
		}
		catch(Exception ex)
		{
			ex.printStackTrace();
		}
		m_codeViewer.refresh();
	}

	/**************************************************************************
	 * 
	 *************************************************************************/
	@Override
	public void notifyError(IProcedure model, ErrorData data)
	{
		m_codeViewer.setExecutorStatus(ExecutorStatus.ERROR);
	}

	/**************************************************************************
	 * 
	 *************************************************************************/
	@Override
	public void notifyItem(IProcedure model, ItemNotification data)
	{
		if (!data.getExecutionMode().equals(ExecutionMode.PROCEDURE))
			return;
	}

	/**************************************************************************
	 * 
	 *************************************************************************/
	@Override
	public void setAutoScroll(boolean enabled)
	{
		m_codeViewer.setAutoScroll(enabled);
		if (enabled)
		{
			m_codeViewer.showCurrentLine();
		}
	}

	/**************************************************************************
	 * Mark the presentation as the current one
	 *************************************************************************/
	@Override
	public void setSelected(boolean selected)
	{
		if (selected) m_codeViewer.showCurrentLine();
	};


	/**************************************************************************
	 * 
	 *************************************************************************/
	@Override
	public void showCurrentLine()
	{
		try
		{
			if (m_codeViewer.isAutoScroll())
			{
				m_codeViewer.showCurrentLine();
			}
		}
		catch(Exception ex)
		{
			ex.printStackTrace();
		}
	}

	/**************************************************************************
	 * 
	 *************************************************************************/
	@Override
	public void setEnabled(boolean enabled)
	{
		m_codeViewer.getGrid().setEnabled(enabled);
	}

	/**************************************************************************
	 * 
	 *************************************************************************/
	@Override
    public String getListenerId()
    {
	    return "Code view for " + m_model.getProcId();
    }
}
