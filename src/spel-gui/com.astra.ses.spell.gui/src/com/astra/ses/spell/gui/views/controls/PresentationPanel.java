///////////////////////////////////////////////////////////////////////////////
//
// PACKAGE   : com.astra.ses.spell.gui.views.controls
// 
// FILE      : PresentationPanel.java
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
package com.astra.ses.spell.gui.views.controls;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

import com.astra.ses.spell.gui.Activator;
import com.astra.ses.spell.gui.core.interfaces.ServiceManager;
import com.astra.ses.spell.gui.core.model.notification.DisplayData;
import com.astra.ses.spell.gui.core.model.types.ClientMode;
import com.astra.ses.spell.gui.core.model.types.ItemStatus;
import com.astra.ses.spell.gui.core.model.types.Level;
import com.astra.ses.spell.gui.core.model.types.Severity;
import com.astra.ses.spell.gui.core.utils.Logger;
import com.astra.ses.spell.gui.interfaces.IPresentationPanel;
import com.astra.ses.spell.gui.interfaces.IProcedureView;
import com.astra.ses.spell.gui.model.commands.ToggleByStep;
import com.astra.ses.spell.gui.model.commands.ToggleRunInto;
import com.astra.ses.spell.gui.model.commands.helpers.CommandHelper;
import com.astra.ses.spell.gui.preferences.interfaces.IConfigurationManager;
import com.astra.ses.spell.gui.preferences.keys.FontKey;
import com.astra.ses.spell.gui.preferences.keys.GuiColorKey;
import com.astra.ses.spell.gui.procs.interfaces.model.ICodeLine;
import com.astra.ses.spell.gui.procs.interfaces.model.ICodeModel;
import com.astra.ses.spell.gui.procs.interfaces.model.IExecutionInformation;
import com.astra.ses.spell.gui.procs.interfaces.model.IProcedure;
import com.astra.ses.spell.gui.types.ExecutorStatus;

/*******************************************************************************
 * @brief Composite which contains the set of controls used for changing pages
 * @date 09/10/07
 ******************************************************************************/
public class PresentationPanel extends Composite implements IPresentationPanel
{
	private static final String CMD_ID = "com.astra.ses.spell.gui.views.controls.CommandId";
	private static final String PRESENTATION_INDEX = "com.astra.ses.spell.gui.views.controls.PresentationIndex";
	private static final int NUM_ELEMENTS = 6;
	
	private static IConfigurationManager s_cfg = (IConfigurationManager) ServiceManager.get(IConfigurationManager.class);

	private IProcedureView m_view;
	private Label m_stageDisplay;
	private String m_currentStage;
	private Text m_procDisplay;
	private Text m_modeDisplay;
	private Color m_okColor;
	private Color m_warningColor;
	private Color m_errorColor;
	private ArrayList<Button> m_presentationButton;
	private Composite m_presentationsPanel;
	private Button m_btnIncrFont;
	private Button m_btnDecrFont;
	private Text m_satName;
	/** Holds the autoscroll checkbox */
	private Button m_autoScroll;
	/** Holds the run over checkbox */
	private Button m_runInto;
	/** Holds the by step checkbox */
	private Button m_byStep;
	/** Holds the TC confirmation checkbox */
	private Text m_tcConfirm;
	/** Client mode */
	private ClientMode m_clientMode;
	/** Holds the current proc status */
	private ExecutorStatus m_currentStatus;
	/** Holds the last notified processing delay */
	private long m_processingDelay;
	/** Procedure model reference, needed for dispose */
	private IProcedure m_model;

	/***************************************************************************
	 * Constructor.
	 **************************************************************************/
	public PresentationPanel(IProcedureView view, IProcedure model, Composite parent, int style, int numPresentations)
	{
		super(parent, style);

		m_view = view;
		m_model = model;

		m_currentStage = null;
		m_processingDelay = -1;
		setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		GridLayout layout = new GridLayout(2,false);
		setLayout(layout);

		Composite leftBase = new Composite(this, SWT.NONE);
		GridLayout lbl = new GridLayout(1,false);
		lbl.marginHeight = 0;
		lbl.marginWidth = 0;
		leftBase.setLayout(lbl);
		leftBase.setLayoutData( new GridData( GridData.FILL_BOTH ));
		
		createStagePanel( leftBase );
		
		Composite presentationsBase = new Composite(leftBase, SWT.NONE);
		GridLayout pbl = new GridLayout(1,false);
		pbl.marginHeight = 0;
		pbl.marginWidth = 0;
		pbl.numColumns = 5;
		presentationsBase.setLayout(pbl);
		presentationsBase.setLayoutData( new GridData( GridData.FILL_HORIZONTAL ));

		createPresentationsPanel( presentationsBase, numPresentations );
		
		m_satName = new Text(this, SWT.BORDER | SWT.SINGLE | SWT.READ_ONLY | SWT.CENTER);
		m_satName.setFont(s_cfg.getFont(FontKey.BANNER));
		m_satName.setText(m_view.getDomain());
		m_satName.setBackground(m_okColor);
		m_satName.setToolTipText("Satellite name");
		m_satName.setEditable(false);
		GridData gd = new GridData();
		gd.heightHint = 47;
		m_satName.setLayoutData(gd);

		// Update the buttons to loaded state
		m_clientMode = null;
		setProcedureStatus(ExecutorStatus.UNINIT);
		
		m_model.getExecutionManager().addListener(this);
		
		//Set tab order
		Control[] tabOrder = { leftBase };
		this.setTabList(tabOrder);
	}

	/***************************************************************************
	 * Get the panel control
	 **************************************************************************/
	@Override
	public Composite getControl()
	{
		return this;
	}

	/***************************************************************************
	 * Create the stage panel
	 **************************************************************************/
	private void createStagePanel( Composite parent )
	{
		Composite stagePanel = new Composite(parent, SWT.NONE);
		GridLayout splayout = new GridLayout();
		splayout.marginHeight = 0;
		splayout.marginWidth = 0;
		splayout.marginLeft = 10;
		splayout.numColumns = 5;
		stagePanel.setLayout(splayout);
		GridData spData = new GridData(GridData.FILL_HORIZONTAL);
		spData.horizontalSpan = NUM_ELEMENTS;
		stagePanel.setLayoutData(spData);

		m_stageDisplay = new Label(stagePanel, SWT.NONE);
		GridData sdd = new GridData(GridData.FILL_HORIZONTAL);
		sdd.grabExcessHorizontalSpace = true;
		m_stageDisplay.setLayoutData(sdd);
		Font header = s_cfg.getFont(FontKey.HEADER);
		m_stageDisplay.setFont(header);

		m_autoScroll = new Button(stagePanel, SWT.CHECK);
		m_autoScroll.setText("Autoscroll");
		m_autoScroll.setSelection(true);
		m_autoScroll.setToolTipText("Enable or disable automatic scroll on procedure views");
		m_autoScroll.addSelectionListener(new SelectionAdapter()
		{
			public void widgetSelected(SelectionEvent e)
			{
				m_view.setAutoScroll(m_autoScroll.getSelection());
			}
		});

		m_runInto = new Button(stagePanel, SWT.CHECK);
		m_runInto.setText("Run Into");
		m_runInto.setSelection(false);
		m_runInto.setToolTipText("Enable or disable run into functions mode");
		m_runInto.addSelectionListener(new SelectionAdapter()
		{
			public void widgetSelected(SelectionEvent e)
			{
				CommandHelper.execute(ToggleRunInto.ID);
			}
		});

		m_byStep = new Button(stagePanel, SWT.CHECK);
		m_byStep.setText("By Step");
		m_byStep.setSelection(false);
		m_byStep.setToolTipText("Enable or disable step by step run mode");
		m_byStep.addSelectionListener(new SelectionAdapter()
		{
			public void widgetSelected(SelectionEvent e)
			{
				CommandHelper.execute(ToggleByStep.ID);
			}
		});

		m_tcConfirm = new Text(stagePanel, SWT.BORDER | SWT.READ_ONLY | SWT.CENTER);
		m_tcConfirm.setText("Normal TC");
		m_tcConfirm.setBackground(Display.getDefault().getSystemColor(SWT.COLOR_GRAY));
		m_tcConfirm.setToolTipText("Force TC confirmation flag");
		GridData tg = new GridData();
		tg.widthHint = 70;
		m_tcConfirm.setLayoutData(tg);

		resetStage();
		
		//Set tab order
		Control[] tabOrder = new Control[] {m_autoScroll, m_runInto, m_byStep };
		stagePanel.setTabList(tabOrder);
		 
	}

	/***************************************************************************
	 * Create the presentation controls panel
	 **************************************************************************/
	private void createPresentationsPanel( Composite parent, int numPresentations )
	{
		m_presentationButton = new ArrayList<Button>();
		m_presentationsPanel = new Composite(parent, SWT.NONE);
		GridLayout playout = new GridLayout();
		playout.marginHeight = 0;
		playout.marginWidth = 0;
		playout.numColumns = numPresentations;
		m_presentationsPanel.setLayout(playout);

		m_btnIncrFont = new Button(parent, SWT.PUSH);
		m_btnIncrFont.setText("");
		Image image = Activator.getImageDescriptor("icons/16x16/more.png").createImage();
		m_btnIncrFont.setImage(image);
		m_btnIncrFont.setData(CMD_ID, "+");
		m_btnIncrFont.addSelectionListener(new SelectionAdapter()
		{
			public void widgetSelected(SelectionEvent event)
			{
				m_view.zoom(true);
			}
		});
		m_btnIncrFont.setToolTipText("Increase font size");

		m_btnDecrFont = new Button(parent, SWT.PUSH);
		m_btnDecrFont.setText("");
		image = Activator.getImageDescriptor("icons/16x16/less.png").createImage();
		m_btnDecrFont.setImage(image);
		m_btnDecrFont.setData(CMD_ID, "-");
		m_btnDecrFont.addSelectionListener(new SelectionAdapter()
		{
			public void widgetSelected(SelectionEvent event)
			{
				m_view.zoom(false);
			}
		});
		m_btnDecrFont.setToolTipText("Decrease font size");

		Font bigFont = s_cfg.getFont(FontKey.HEADER);

		m_okColor = s_cfg.getGuiColor(GuiColorKey.TABLE_BG);
		m_warningColor = s_cfg.getStatusColor(ItemStatus.WARNING);
		m_errorColor = s_cfg.getStatusColor(ItemStatus.ERROR);

		m_procDisplay = new Text(parent, SWT.BORDER | SWT.SINGLE | SWT.READ_ONLY);
		m_procDisplay.setFont(bigFont);
		m_procDisplay.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		// Initialize with the latest message, if any
		DisplayData[] msgs = m_model.getRuntimeInformation().getDisplayMessages();
		if (msgs.length > 0)
		{
			DisplayData last = msgs[msgs.length - 1];
			displayMessage(last.getMessage(), last.getSeverity());
		}
		else
		{
			m_procDisplay.setText("");
			m_procDisplay.setBackground(m_okColor);
		}
		m_procDisplay.setEditable(false);
		m_procDisplay.setToolTipText("Procedure display");

		m_modeDisplay = new Text(parent, SWT.BORDER | SWT.READ_ONLY | SWT.SINGLE | SWT.CENTER );
		m_modeDisplay.setFont(bigFont);
		GridData mg = new GridData();
		mg.widthHint = 70;
		m_modeDisplay.setLayoutData(mg);
		if (m_model.getRuntimeInformation().getClientMode().equals(ClientMode.CONTROL))
		{
			m_modeDisplay.setBackground( Display.getDefault().getSystemColor(SWT.COLOR_GREEN) );
			m_modeDisplay.setText( "CTRL" );
		}
		else
		{
			m_modeDisplay.setBackground( Display.getDefault().getSystemColor(SWT.COLOR_CYAN) );
			m_modeDisplay.setText( "MON" );
		}
		
		//Set tab order
		Control[] tabOrder = new Control[] {m_presentationsPanel, m_btnIncrFont, m_btnDecrFont };
		parent.setTabList(tabOrder);
	}

	/* (non-Javadoc)
     * @see com.astra.ses.spell.gui.views.controls.IPresentationPanel#dispose()
     */
	@Override
    public void dispose()
	{
		m_model.getExecutionManager().removeListener(this);
		super.dispose();
	}
	
	/* (non-Javadoc)
     * @see com.astra.ses.spell.gui.views.controls.IPresentationPanel#addPresentation(java.lang.String, java.lang.String, org.eclipse.swt.graphics.Image, int)
     */
	@Override
    public void addPresentation(String title, String desc, Image icon, int pageIndex)
	{
		Logger.debug("Added presentation '" + title + "' with index " + pageIndex, Level.GUI, this);
		Button btn = new Button(m_presentationsPanel, SWT.TOGGLE);
		btn.setText(title);
		btn.setImage(icon);
		btn.setData(PRESENTATION_INDEX, pageIndex);
		btn.setSelection(false);
		btn.addSelectionListener(new SelectionAdapter()
		{
			public void widgetSelected(SelectionEvent event)
			{
				int index = (Integer) event.widget.getData(PRESENTATION_INDEX);
				selectPresentation(index);
			}
		});
		btn.setToolTipText(desc);
		m_presentationButton.add(btn);
	}

	/* (non-Javadoc)
     * @see com.astra.ses.spell.gui.views.controls.IPresentationPanel#displayMessage(java.lang.String, com.astra.ses.spell.gui.core.model.types.Severity)
     */
	@Override
    public void displayMessage(String message, Severity sev)
	{
		if (message.contains("\n"))
		{
			String elements[] = message.split("\n");
			m_procDisplay.setText(elements[0] + "...");
		}
		else
		{
			m_procDisplay.setText(message);
		}
		if (sev == Severity.INFO)
		{
			m_procDisplay.setBackground(m_okColor);
		}
		else if (sev == Severity.WARN)
		{
			m_procDisplay.setBackground(m_warningColor);
		}
		else if (sev == Severity.ERROR)
		{
			m_procDisplay.setBackground(m_errorColor);
		}
	}

	/* (non-Javadoc)
     * @see com.astra.ses.spell.gui.views.controls.IPresentationPanel#reset()
     */
	@Override
    public void reset()
	{
		m_procDisplay.setText("");
		m_procDisplay.setBackground(m_okColor);
		resetStage();
	}

	/* (non-Javadoc)
     * @see com.astra.ses.spell.gui.views.controls.IPresentationPanel#setEnabled(boolean)
     */
	@Override
    public void setEnabled(boolean enabled)
	{
		m_byStep.setEnabled(enabled);
		m_runInto.setEnabled(enabled);
		m_autoScroll.setEnabled(enabled);
		m_tcConfirm.setEnabled(enabled);
		if (enabled && m_clientMode == ClientMode.CONTROL)
		{
			setProcedureStatus(m_currentStatus);
		}
	}

	/* (non-Javadoc)
     * @see com.astra.ses.spell.gui.views.controls.IPresentationPanel#setStage(java.lang.String, java.lang.String)
     */
	@Override
    public void setStage(String id, String title)
	{
		if (id != null && !id.isEmpty())
		{
			if (m_currentStage == null || !m_currentStage.equals(id))
			{
				if (!title.trim().isEmpty())
				{
					title = " - " + title;
				}
				m_stageDisplay.setText("Step: " + id + title);
				m_currentStage = id;
			}
		}
	}

	/* (non-Javadoc)
     * @see com.astra.ses.spell.gui.views.controls.IPresentationPanel#resetStage()
     */
	@Override
    public void resetStage()
	{
		m_currentStage = null;
		m_stageDisplay.setText("");
	}

	/* (non-Javadoc)
     * @see com.astra.ses.spell.gui.views.controls.IPresentationPanel#selectPresentation(int)
     */
	@Override
    public void selectPresentation(int index)
	{
		// Ensure the button is enabled (it wont be in case of programmatic call
		// to this method)
		m_presentationButton.get(index).setSelection(true);
		// Disable the rest of buttons
		for (int count = 0; count < m_presentationButton.size(); count++)
		{
			if (count == index)
				continue;
			m_presentationButton.get(count).setSelection(false);
		}
		m_view.showPresentation(index);
	}

	/* (non-Javadoc)
     * @see com.astra.ses.spell.gui.views.controls.IPresentationPanel#notifyModelConfigured(com.astra.ses.spell.gui.procs.interfaces.model.IProcedure)
     */
	@Override
    public void notifyModelConfigured(IProcedure model)
	{
		IExecutionInformation info = model.getRuntimeInformation();
		boolean isRunInto = model.getExecutionManager().isRunInto();
		m_runInto.setSelection(isRunInto);
		m_byStep.setSelection(info.isStepByStep());
		if (info.isForceTcConfirmation())
		{
			m_tcConfirm.setText("TC Confirm");
			m_tcConfirm.setBackground(Display.getDefault().getSystemColor(SWT.COLOR_YELLOW));
		}
		else
		{
			m_tcConfirm.setText("Normal TC");
			m_tcConfirm.setBackground(Display.getDefault().getSystemColor(SWT.COLOR_GRAY));
		}
	}
		

	/* (non-Javadoc)
     * @see com.astra.ses.spell.gui.views.controls.IPresentationPanel#setClientMode(com.astra.ses.spell.gui.core.model.types.ClientMode)
     */
	@Override
    public void setClientMode(ClientMode mode)
	{
		m_clientMode = mode;
	}

	/* (non-Javadoc)
     * @see com.astra.ses.spell.gui.views.controls.IPresentationPanel#setProcedureStatus(com.astra.ses.spell.gui.core.model.types.ExecutorStatus)
     */
	@Override
    public void setProcedureStatus(ExecutorStatus status)
	{
		// Procedure control buttons enablement
		m_currentStatus = status;
		if ((m_clientMode != null) && (!isDisposed()))
		{
			boolean controlling = false;
			controlling = m_clientMode.equals(ClientMode.CONTROL);
			// Code execution tracking buttons enablement
			boolean trackingEnabled = false;
			switch (m_currentStatus)
			{
			case RUNNING:
			case WAITING:
			case PROMPT:
			case INTERRUPTED:
			case PAUSED:
				trackingEnabled = true;
			default:
				break;
			}
			m_autoScroll.setEnabled(trackingEnabled);
			m_runInto.setEnabled(trackingEnabled && controlling);
			m_byStep.setEnabled(trackingEnabled && controlling);
			m_tcConfirm.setEnabled(trackingEnabled && controlling);
		}
	}

	@Override
    public void onItemsChanged(List<ICodeLine> lines) {}

	@Override
    public void onProcessingDelayChanged( final long delaySec )
    {
		if (delaySec != m_processingDelay)
		{
			m_processingDelay = delaySec;
			Display.getDefault().syncExec( new Runnable()
			{
				public void run()
				{
					if (delaySec > 2 && delaySec < 10)
					{
						//m_modeDisplay.setBackground(Display.getDefault().getSystemColor(SWT.COLOR_YELLOW));
						m_modeDisplay.setToolTipText("Processing delay is " + delaySec + " seconds");
						m_modeDisplay.setMessage("Processing delay is " + delaySec + " seconds");
					}
					else if (delaySec>10)
					{
						//m_modeDisplay.setBackground(Display.getDefault().getSystemColor(SWT.COLOR_RED));
						m_modeDisplay.setToolTipText("Processing delay is " + delaySec + " seconds");
						m_modeDisplay.setMessage("Processing delay is " + delaySec + " seconds");
					}
					else
					{
						m_modeDisplay.setToolTipText("No relevant processing delay");
						m_modeDisplay.setMessage("No relevant processing delay");
						
//						if (m_model.getRuntimeInformation().getClientMode().equals(ClientMode.CONTROL))
//						{
//							m_modeDisplay.setBackground( Display.getDefault().getSystemColor(SWT.COLOR_GREEN) );
//						}
//						else
//						{
//							m_modeDisplay.setBackground( Display.getDefault().getSystemColor(SWT.COLOR_CYAN) );
//						}
					}
					m_modeDisplay.redraw();
				}
			});
		}
    }

	@Override
    public void onCodeChanged(ICodeModel model) {} 

	@Override
    public void onLineChanged(ICodeLine line) {}

	@Override
    public void onLinesChanged(List<ICodeLine> lines) {}
}
