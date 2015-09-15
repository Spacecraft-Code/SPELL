///////////////////////////////////////////////////////////////////////////////
//
// PACKAGE   : com.astra.ses.spell.gui.presentation.text
// 
// FILE      : TextPresentation.java
//
// DATE      : 2008-11-21 13:54
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
package com.astra.ses.spell.gui.presentation.text;

import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;

import com.astra.ses.spell.gui.core.interfaces.ServiceManager;
import com.astra.ses.spell.gui.core.model.notification.DisplayData;
import com.astra.ses.spell.gui.core.model.notification.ErrorData;
import com.astra.ses.spell.gui.core.model.notification.StackNotification;
import com.astra.ses.spell.gui.core.model.notification.StatusNotification;
import com.astra.ses.spell.gui.core.model.types.ExecutionMode;
import com.astra.ses.spell.gui.core.model.types.Scope;
import com.astra.ses.spell.gui.core.model.types.Severity;
import com.astra.ses.spell.gui.interfaces.IPresentationNotifier;
import com.astra.ses.spell.gui.interfaces.ProcedurePresentationAdapter;
import com.astra.ses.spell.gui.interfaces.listeners.IGuiProcedureMessageListener;
import com.astra.ses.spell.gui.interfaces.listeners.IGuiProcedurePromptListener;
import com.astra.ses.spell.gui.interfaces.listeners.IGuiProcedureRuntimeListener;
import com.astra.ses.spell.gui.interfaces.listeners.IGuiProcedureStackListener;
import com.astra.ses.spell.gui.interfaces.listeners.IGuiProcedureStatusListener;
import com.astra.ses.spell.gui.preferences.interfaces.IConfigurationManager;
import com.astra.ses.spell.gui.preferences.keys.FontKey;
import com.astra.ses.spell.gui.preferences.keys.PreferenceCategory;
import com.astra.ses.spell.gui.preferences.keys.PropertyKey;
import com.astra.ses.spell.gui.presentation.text.controls.DisplayViewer;
import com.astra.ses.spell.gui.procs.interfaces.model.IExecutionInformationHandler;
import com.astra.ses.spell.gui.procs.interfaces.model.IProcedure;
import com.astra.ses.spell.gui.types.ExecutorStatus;

public class TextPresentation extends ProcedurePresentationAdapter implements IGuiProcedureMessageListener, IGuiProcedurePromptListener,
        IGuiProcedureStatusListener, IGuiProcedureRuntimeListener, IGuiProcedureStackListener, IPropertyChangeListener
{
	/** Holds the viewer control */
	private DisplayViewer m_displayViewer;
	/** Reference to the model */
	private IProcedure m_model;
	/** Presentation identifier for the extension */
	private static final String ID = "com.astra.ses.spell.gui.presentation.TextView";
	/** Presentation title */
	private static final String PRESENTATION_TITLE = "Text";
	/** Presentation description */
	private static final String PRESENTATION_DESC = "Procedure view in text mode";
	/** Presentation icon path */
	private static final String PRESENTATION_ICON = "icons/16x16/text.png";
	/** Reference to preferences */
	private static IConfigurationManager s_cfg = null;

	/************************************************************************************
	 * 
	 ***********************************************************************************/
	@Override
	public Composite createContents(IProcedure model, Composite stack)
	{
		if (s_cfg == null)
		{
			s_cfg = (IConfigurationManager) ServiceManager.get(IConfigurationManager.class);
		}
		
		Composite displayPage = new Composite(stack, SWT.NONE);
		GridLayout groupLayout = new GridLayout();
		groupLayout.marginHeight = 0;
		groupLayout.marginWidth = 0;
		groupLayout.marginBottom = 0;
		groupLayout.marginTop = 0;
		groupLayout.verticalSpacing = 0;
		groupLayout.numColumns = 1;
		displayPage.setLayout(groupLayout);
		GridData data = new GridData();
		data.grabExcessHorizontalSpace = true;
		data.grabExcessVerticalSpace = true;
		data.verticalAlignment = SWT.FILL;
		displayPage.setLayoutData(data);

		m_model = model;
		
		s_cfg.addPropertyChangeListener(this);

		// Create the viewer, main control.
		int capacity = model.getRuntimeInformation().getDisplayMessageCapacity();
		m_displayViewer = new DisplayViewer(displayPage, capacity);
		
		m_displayViewer.setCodeName(model.getProcName(),null,"");
		return displayPage;
	}

	/************************************************************************************
	 * 
	 ***********************************************************************************/
	@Override
	public void dispose()
	{
		s_cfg.removePropertyChangeListener(this);
	}
	
	/************************************************************************************
	 * 
	 ***********************************************************************************/
	@Override
	public void subscribeNotifications(IPresentationNotifier notifier)
	{
		notifier.addMessageListener(this);
		notifier.addPromptListener(this);
		notifier.addStatusListener(this);
		notifier.addRuntimeListener(this);
		notifier.addStackListener(this);
	}
	
	/************************************************************************************
	 * 
	 ***********************************************************************************/
	@Override
	public String getExtensionId()
	{
		return ID;
	}

	/************************************************************************************
	 * 
	 ***********************************************************************************/
	@Override
	public String getTitle()
	{
		return PRESENTATION_TITLE;
	}

	/************************************************************************************
	 * 
	 ***********************************************************************************/
	@Override
	public Image getIcon()
	{
		return Activator.getImageDescriptor(PRESENTATION_ICON).createImage();
	}

	/************************************************************************************
	 * 
	 ***********************************************************************************/
	@Override
	public String getDescription()
	{
		return PRESENTATION_DESC;
	}

	/************************************************************************************
	 * 
	 ***********************************************************************************/
	@Override
	public void zoom(boolean zoomIn)
	{
		m_displayViewer.zoom(zoomIn);
	}

	/************************************************************************************
	 * 
	 ***********************************************************************************/
	@Override
	public void showLine(int lineNo)
	{
	};

	/************************************************************************************
	 * 
	 ***********************************************************************************/
	@Override
	public void setAutoScroll(boolean enabled)
	{
		m_displayViewer.setAutoscroll(enabled);
	}

	/************************************************************************************
	 * 
	 ***********************************************************************************/
	@Override
	public void notifyDisplay(IProcedure model, DisplayData data)
	{
		if (!data.getExecutionMode().equals(ExecutionMode.PROCEDURE))
			return;
		m_displayViewer.addMessage(data.getMessage(), data.getSeverity(), data.getTime(), data.getScope(), data.getSequence());
	}

	/************************************************************************************
	 * 
	 ***********************************************************************************/
	@Override
	public void notifyPrompt(IProcedure model) {}

	/************************************************************************************
	 * 
	 ***********************************************************************************/
	@Override
	public void notifyFinishPrompt(IProcedure model) {}

	/************************************************************************************
	 * 
	 ***********************************************************************************/
	@Override
	public void notifyCancelPrompt(IProcedure model) {}

	/************************************************************************************
	 * 
	 ***********************************************************************************/
	@Override
	public void notifyError(IProcedure model, ErrorData data)
	{
		m_displayViewer.addMessage(data.getMessage() + "\n   " + data.getReason(), Severity.ERROR, data.getTime(), Scope.SYS, data.getSequence());
		m_displayViewer.notifyProcStatus(ExecutorStatus.ERROR);
	}

	/************************************************************************************
	 * 
	 ***********************************************************************************/
	@Override
	public void notifyModelLoaded(IProcedure model)
	{
		m_displayViewer.clear();
		// Try to recover any message to replay if we are monitoring
		DisplayData[] replayMessages = model.getRuntimeInformation().getDisplayMessages();
		// If there are messages, put them in the display after clearing
		if (replayMessages != null)
		{
			for (DisplayData data : replayMessages)
			{
				m_displayViewer.addMessage(data.getMessage(), data.getSeverity(), data.getTime(), data.getScope(), data.getSequence());
			}
		}
	}

	/************************************************************************************
	 * 
	 ***********************************************************************************/
	@Override
	public void notifyModelDisabled(IProcedure model)
	{
		m_displayViewer.setEnabled(false);
	}

	/************************************************************************************
	 * 
	 ***********************************************************************************/
	@Override
	public void notifyModelEnabled(IProcedure model)
	{
		m_displayViewer.setEnabled(true);
	}

	/************************************************************************************
	 * 
	 ***********************************************************************************/
	@Override
	public void notifyModelReset(IProcedure model)
	{
		m_displayViewer.clear();
	}

	/************************************************************************************
	 * 
	 ***********************************************************************************/
	@Override
	public void notifyModelUnloaded(IProcedure model)
	{
	}

	/************************************************************************************
	 * 
	 ***********************************************************************************/
	@Override
	public void notifyModelConfigured(IProcedure model)
	{
	}

	/************************************************************************************
	 * 
	 ***********************************************************************************/
	@Override
	public void notifyStatus(IProcedure model, StatusNotification data)
	{
		if (data.getStatus() != ExecutorStatus.UNKNOWN)
		{
			m_displayViewer.notifyProcStatus(data.getStatus());
		}
	}
	
	/************************************************************************************
	 * 
	 ***********************************************************************************/
	public String[] getDisplayTextLines()
	{
		return m_displayViewer.getTextLines();
	}

	/************************************************************************************
	 * 
	 ***********************************************************************************/
	@Override
    public void notifyStack(IProcedure model, StackNotification data)
    {
		int size = data.getStackPosition().size();
		String lineNo = data.getStackPosition().get(size-1);
		String cname = data.getCodeName();
		String name = data.getStackPosition().get(size-2);
		m_displayViewer.setCodeName("'" + name + "'", cname, lineNo);
    }

	/************************************************************************************
	 * 
	 ***********************************************************************************/
	@Override
	public void propertyChange(PropertyChangeEvent event)
	{
		String property = event.getProperty();
		if (property.startsWith(PreferenceCategory.PROC_COLOR.tag))
		{
			String statusStr = property.substring(PreferenceCategory.PROC_COLOR.tag.length() + 1);
			ExecutorStatus st = ExecutorStatus.valueOf(statusStr);
			m_displayViewer.setBackground(s_cfg.getProcedureColor(st));
		}
		else if (property.equals(PropertyKey.TEXT_TIMESTAMP.getPreferenceName()))
		{
			String newValue = (String) event.getNewValue();
			m_displayViewer.setShowTimestamp(newValue.equals("YES"));
		}
		else if (property.equals(PropertyKey.TEXT_HISTORY_ITEMS.getPreferenceName()))
		{
			String newValue = (String) event.getNewValue();
			int capacity = Integer.parseInt(newValue);
			m_displayViewer.setCapacity( capacity );
			((IExecutionInformationHandler) m_model.getRuntimeInformation()).setDisplayMessageCapacity(capacity);
		}
		else if (property.equals(FontKey.CODE.getPreferenceName()))
		{
			Font newFont = s_cfg.getFont(FontKey.CODE);
			m_displayViewer.setFont(newFont);
		}
	}

	@Override
    public String getListenerId()
    {
	    return "Text presentation for " + m_model.getProcId();
    }


}
