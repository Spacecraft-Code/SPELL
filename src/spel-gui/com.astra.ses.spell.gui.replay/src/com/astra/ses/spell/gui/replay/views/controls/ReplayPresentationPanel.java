///////////////////////////////////////////////////////////////////////////////
//
// PACKAGE   : com.astra.ses.spell.gui.replay.views.controls
// 
// FILE      : ReplayPresentationPanel.java
//
// DATE      : Jun 20, 2013
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
package com.astra.ses.spell.gui.replay.views.controls;

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
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

import com.astra.ses.spell.gui.core.interfaces.ServiceManager;
import com.astra.ses.spell.gui.core.model.notification.DisplayData;
import com.astra.ses.spell.gui.core.model.types.ClientMode;
import com.astra.ses.spell.gui.core.model.types.ItemStatus;
import com.astra.ses.spell.gui.core.model.types.Level;
import com.astra.ses.spell.gui.core.model.types.Severity;
import com.astra.ses.spell.gui.core.utils.Logger;
import com.astra.ses.spell.gui.interfaces.IPresentationPanel;
import com.astra.ses.spell.gui.interfaces.IProcedureView;
import com.astra.ses.spell.gui.preferences.interfaces.IConfigurationManager;
import com.astra.ses.spell.gui.preferences.keys.FontKey;
import com.astra.ses.spell.gui.preferences.keys.GuiColorKey;
import com.astra.ses.spell.gui.procs.interfaces.model.ICodeLine;
import com.astra.ses.spell.gui.procs.interfaces.model.ICodeModel;
import com.astra.ses.spell.gui.procs.interfaces.model.IProcedure;
import com.astra.ses.spell.gui.replay.Activator;
import com.astra.ses.spell.gui.types.ExecutorStatus;

/*******************************************************************************
 * @brief Composite which contains the set of controls used for changing pages
 * @date 09/10/07
 ******************************************************************************/
public class ReplayPresentationPanel extends Composite implements IPresentationPanel
{
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
	/** Procedure model reference, needed for dispose */
	private IProcedure m_model;

	/***************************************************************************
	 * Constructor.
	 **************************************************************************/
	public ReplayPresentationPanel(IProcedureView view, IProcedure model, Composite parent, int style, int numPresentations)
	{
		super(parent, style);

		m_view = view;
		m_model = model;

		m_currentStage = null;
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
	}
	
	/***************************************************************************
	 * 
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
		splayout.numColumns = 1;
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

		resetStage();
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
		Image image = Activator.getImageDescriptor("icons/more.png").createImage();
		m_btnIncrFont.setImage(image);
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
		image = Activator.getImageDescriptor("icons/less.png").createImage();
		m_btnDecrFont.setImage(image);
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
		m_modeDisplay.setBackground( Display.getDefault().getSystemColor(SWT.COLOR_MAGENTA) );
		m_modeDisplay.setText( "REPLAY" );
	}

	/***************************************************************************
	 * Add a presentation button
	 **************************************************************************/
	public void addPresentation(String title, String desc, Image icon, int pageIndex)
	{
		if (title.equals("Shell")) return;
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

	/***************************************************************************
	 * Display a message
	 **************************************************************************/
	public void displayMessage(String message, Severity sev)
	{
		String elements[] = message.split("\n");
		m_procDisplay.setText(elements[elements.length - 1]);
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

	/***************************************************************************
	 * Reset all controls
	 **************************************************************************/
	public void reset()
	{
		m_procDisplay.setText("");
		m_procDisplay.setBackground(m_okColor);
		resetStage();
	}

	/***************************************************************************
	 * Enable/disable
	 **************************************************************************/
	@Override
	public void setEnabled(boolean enabled)
	{
		// Nothing to do
	}

	/***************************************************************************
	 * Set current stage
	 **************************************************************************/
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

	/***************************************************************************
	 * Reset the current stage
	 **************************************************************************/
	public void resetStage()
	{
		m_currentStage = null;
		m_stageDisplay.setText("");
	}

	/***************************************************************************
	 * Select the given presentation
	 **************************************************************************/
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

	/***************************************************************************
	 * Callback procedure model configuration changes
	 **************************************************************************/
	@Override
	public void notifyModelConfigured(IProcedure model) {}

	/***************************************************************************
	 * 
	 **************************************************************************/
	@Override
	public void setClientMode(ClientMode mode) {}

	/***************************************************************************
	 * 
	 **************************************************************************/
	public void setProcedureStatus(ExecutorStatus status) {}

	/***************************************************************************
	 * 
	 **************************************************************************/
	@Override
    public void onItemsChanged(List<ICodeLine> lines) {}

	/***************************************************************************
	 * 
	 **************************************************************************/
	@Override
    public void onProcessingDelayChanged( final long delaySec ) {}

	/***************************************************************************
	 * 
	 **************************************************************************/
	@Override
    public void onCodeChanged(ICodeModel model) {}

	/***************************************************************************
	 * 
	 **************************************************************************/
	@Override
    public void onLineChanged(ICodeLine line) {}

	/***************************************************************************
	 * 
	 **************************************************************************/
	@Override
    public void onLinesChanged(List<ICodeLine> lines) {}
}
