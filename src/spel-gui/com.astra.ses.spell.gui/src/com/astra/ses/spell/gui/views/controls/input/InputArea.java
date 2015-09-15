///////////////////////////////////////////////////////////////////////////////
//
// PACKAGE   : com.astra.ses.spell.gui.views.controls.input
// 
// FILE      : InputArea.java
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
package com.astra.ses.spell.gui.views.controls.input;

import java.io.File;
import java.util.ArrayList;
import java.util.Vector;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.fieldassist.IContentProposal;
import org.eclipse.jface.fieldassist.IContentProposalListener;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Text;

import com.astra.ses.spell.gui.core.interfaces.IContextProxy;
import com.astra.ses.spell.gui.core.interfaces.ServiceManager;
import com.astra.ses.spell.gui.core.model.notification.InputData;
import com.astra.ses.spell.gui.core.model.types.ClientMode;
import com.astra.ses.spell.gui.core.model.types.Level;
import com.astra.ses.spell.gui.core.model.types.PromptDisplayType;
import com.astra.ses.spell.gui.core.utils.Logger;
import com.astra.ses.spell.gui.interfaces.ISashListener;
import com.astra.ses.spell.gui.preferences.interfaces.IConfigurationManager;
import com.astra.ses.spell.gui.preferences.keys.FontKey;
import com.astra.ses.spell.gui.preferences.keys.PropertyKey;
import com.astra.ses.spell.gui.procs.interfaces.model.IProcedure;
import com.astra.ses.spell.gui.views.controls.ControlArea;

/*******************************************************************************
 * @brief Special control for showing user prompts in the tabular view
 * @date 20/10/07
 ******************************************************************************/
public class InputArea extends Composite implements SelectionListener,
		KeyListener, ISashListener, IContentProposalListener {

	private static final String KEY_SEPARATOR = ":";
	private static IConfigurationManager s_cfg = null;
	private static IContextProxy s_proxy = null;

	/** Text for commit button */
	public static final String BTN_COMMIT = "Confirm";
	/** Text for reset button */
	public static final String BTN_RESET = "Reset";
	/** Identifier for commit button */
	public static final String BTN_COMMIT_ID = "BTN_COMMIT";
	/** Identifier for reset button */
	public static final String BTN_RESET_ID = "BTN_RESET";
	/** Maximum font size */
	private static final int MAX_FONT_SIZE = 16;
	/** Minimum font size */
	private static final int MIN_FONT_SIZE = 7;
	/** Currently selected font size */
	private int m_fontSize;
	/** Created font for internal use */
	private Font m_myFont = null;

	/** Text field for text prompts */
	private Text m_promptText;
	/** Prompt field for text prompts */
	private PromptField m_textInput;
	/** Future storing the prompt answers */
	private InputData m_promptData;
	/** Holds the list of expected values */
	private Vector<String> m_expected;
	/** Flag for numeric text prompts */
	private boolean m_numericInput;
	/** Parent view of the input area */
	private ControlArea m_parent;
	/** Holds the radio buttons for options */
	private ArrayList<Button> m_optionsRadio;
	/** Holds the combo widget for options */
	private Combo m_optionsCombo;
	/** Holds the type of widget for options */
	private PromptDisplayType m_promptDisplayType;
	/** Provides the scrolling of option buttons */
	private ScrolledComposite m_optionScroll;
	/** Holds the set of radio buttons */
	private Composite m_optionContainer;
	/** Holds the selected option index if any */
	private int m_selectedOption;
	/** Commit button */
	private Button m_commitButton;
	/** Reset button */
	private Button m_resetButton;
	/** Holds the client mode */
	private ClientMode m_clientMode;
	/* Prompt blinking mechanism */
	private PromptBlinker m_blinker;
	/** Launcher of the blinker task */
	private PromptBlinkerLauncher m_blinkerLauncher;
	/** Blinker switch flag */
	private boolean m_blinkSwitch;
	/** Prompt sound file name */
	private PromptSounder m_sounder;
	/** Holds the procedure model */
	private IProcedure m_model;

	/***************************************************************************
	 * Constructor
	 * 
	 * @param view
	 *            The parent procedure view
	 * @param top
	 *            The dictionary composite
	 **************************************************************************/
	public InputArea(ControlArea area, IProcedure model) {
		super(area, SWT.NONE);
		m_parent = area;
		m_promptData = null;
		m_expected = null;
		m_clientMode = null;
		m_model = model;

		GridLayout layout = new GridLayout();
		layout.marginHeight = 0;
		layout.marginWidth = 0;
		layout.marginBottom = 5;
		layout.numColumns = 1;
		setLayout(layout);

		setLayoutData(new GridData(GridData.FILL_BOTH));

		m_promptText = new Text(this, SWT.BORDER | SWT.READ_ONLY | SWT.MULTI
				| SWT.WRAP | SWT.V_SCROLL);
		m_promptText.setText("Enter command");
		m_promptText.getVerticalBar().setVisible(false);
		GridData tpData = new GridData(GridData.FILL_HORIZONTAL);
		m_promptText.setLayoutData(tpData);
		m_promptText.setBackground(Display.getCurrent().getSystemColor(
				SWT.COLOR_WHITE));

		if (s_cfg == null) {
			s_cfg = (IConfigurationManager) ServiceManager
					.get(IConfigurationManager.class);
			s_proxy = (IContextProxy) ServiceManager.get(IContextProxy.class);
		}

		Composite inputGroup = new Composite(this, SWT.NONE);
		GridLayout ig_layout = new GridLayout(4, false);
		ig_layout.marginHeight = 0;
		ig_layout.marginTop = 0;
		ig_layout.marginBottom = 5;
		ig_layout.marginWidth = 0;
		ig_layout.marginLeft = 3;
		ig_layout.marginRight = 3;
		ig_layout.horizontalSpacing = 8;
		inputGroup.setLayout(ig_layout);
		inputGroup.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		m_textInput = new PromptField(inputGroup, "", this);
		GridData gd = new GridData(GridData.FILL_HORIZONTAL);
		m_textInput.getContents().setLayoutData(gd);

		m_commitButton = new Button(inputGroup, SWT.PUSH);
		m_commitButton.addSelectionListener(this);
		m_commitButton.setText(BTN_COMMIT);
		m_commitButton.setData("ID", BTN_COMMIT_ID);
		m_resetButton = new Button(inputGroup, SWT.PUSH);
		m_resetButton.addSelectionListener(this);
		m_resetButton.setText(BTN_RESET);
		m_resetButton.setData("ID", BTN_RESET_ID);

		m_commitButton.setEnabled(false);
		m_resetButton.setEnabled(false);

		m_optionsRadio = new ArrayList<Button>();
		m_optionsCombo = null;
		m_selectedOption = -1;
		m_optionScroll = null;

		m_blinker = null;
		m_blinkerLauncher = null;
		m_blinkSwitch = true;

		m_fontSize = s_cfg.getFont(FontKey.TEXT).getFontData()[0].getHeight();
		updateFontFromSize();

		// Set tab order
		Control[] tabOrder = { inputGroup };
		this.setTabList(tabOrder);
	}

	/***************************************************************************
	 * 
	 **************************************************************************/
	public void setEnabled(boolean enabled) {
		if (enabled && m_clientMode != ClientMode.CONTROL)
			return;
		m_promptText.setEnabled(enabled);
		super.setEnabled(enabled);
	}

	/***************************************************************************
	 * 
	 **************************************************************************/
	public void zoom(boolean increase) {
		boolean changed = true;
		if (increase) {
			m_fontSize++;
			if (m_fontSize > MAX_FONT_SIZE) {
				m_fontSize = MAX_FONT_SIZE;
				changed = false;
			}
		} else {
			m_fontSize--;
			if (m_fontSize < MIN_FONT_SIZE) {
				m_fontSize = MIN_FONT_SIZE;
				changed = false;
			}
		}
		if (changed) {
			updateFontFromSize();
		}
	}

	/***************************************************************************
	 * Set the table font
	 **************************************************************************/
	private void updateFontFromSize() {
		if (m_myFont != null) {
			m_myFont.dispose();
		}
		setRedraw(false);
		m_myFont = new Font(Display.getDefault(), "Courier New", m_fontSize,
				SWT.NORMAL);
		m_promptText.setFont(m_myFont);
		m_textInput.setFont(m_myFont);

		try {
			if (m_optionContainer != null && !m_optionContainer.isDisposed()) {
				for (Button opt : m_optionsRadio) {
					opt.setFont(m_myFont);
				}
				m_optionContainer.pack();
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}

		setRedraw(true);
		m_parent.layout();
	}

	/***************************************************************************
	 * Set the table font
	 **************************************************************************/
	public void setFont(Font font) {
		m_fontSize = font.getFontData()[0].getHeight();
		updateFontFromSize();
	}

	/***************************************************************************
	 * 
	 **************************************************************************/
	public boolean setFocus() {
		m_textInput.getContents().setFocus();
		return true;
	}

	/***************************************************************************
	 * 
	 **************************************************************************/
	public void setClientMode(ClientMode mode) {
		m_clientMode = mode;
		if (mode != ClientMode.CONTROL) {
			setEnabled(false);
		}
	}

	/***************************************************************************
	 * Issue a textual prompt
	 * 
	 * @param prompt
	 *            Prompt text
	 * @param value
	 *            Future for storing the answer
	 * @param isNumeric
	 *            Numeric prompt flag
	 **************************************************************************/
	public void prompt(InputData promptData) {
		Logger.debug("Start prompt", Level.PROC, this);
		reset();
		prepareBlinking();
		m_promptData = promptData;
		if (m_clientMode.equals(ClientMode.MONITOR)) {
			promptAsMonitoring();
		} else {
			promptAsControlling();
		}
	}

	/***************************************************************************
	 * Shows a prompt prepared to receive an answer (controlling GUI)
	 **************************************************************************/
	private void promptAsControlling() {

		boolean defaultOptionSet = false;

		if (m_promptData.isList()) {
			m_expected = m_promptData.getExpected();
			m_numericInput = false;
			// Set the prompt text
			m_promptText.setText(m_promptData.getText());

			// Adjust the height and visibility of scroll bar depending on
			// the control doing text wrapping or not
			int areaWidth = m_promptText.getClientArea().width;
			GC gc = new GC(m_promptText);
			int textWidth = gc.getFontMetrics().getAverageCharWidth()
					* m_promptText.getText().length();
			boolean wrapping = textWidth >= areaWidth;
			gc.dispose();
			m_promptText.getVerticalBar().setVisible(wrapping);
			if (wrapping) {
				GridData gd = (GridData) m_promptText.getLayoutData();
				gd.heightHint = 45;
				layout();
			}

			m_promptDisplayType = m_promptData.getPromptDisplayType();
			// Build the option list and show the option composite
			defaultOptionSet = updateOptions(m_promptData.getOptions(),
					m_promptData.getExpected(), m_promptData.getDefault());
			if (defaultOptionSet) {
				m_textInput.setValue(m_promptData.getDefault());
			}
			
			

		} 
		else 
		{
			//Not a List
			
			m_expected = null;
			m_promptText.setText(m_promptData.getText());
			m_numericInput = m_promptData.isNumeric();
			if (!m_promptData.getDefault().isEmpty()) {
				m_textInput.setValue(m_promptData.getDefault());
				defaultOptionSet = true;
			}
		}
		
		setHint();
		
		if (defaultOptionSet) {
			// If we have a default option preselected, enable the controls
			m_commitButton.setEnabled(true);
			m_resetButton.setEnabled(true);
		} else {
			// Reset the console input
			 
			m_textInput.reset();
			
		}
		// Set the focus, highlighting
		m_textInput.promptStart();
	}

	/***************************************************************************
	 * Shows a prompt prepared not expecting an answer (monitoring GUI)
	 **************************************************************************/
	private void promptAsMonitoring() {
		promptAsControlling();
		m_textInput.setEnabled(false);
		m_commitButton.setEnabled(false);
		m_resetButton.setEnabled(false);
		if (m_promptDisplayType == PromptDisplayType.RADIO) {
			for (Button opt : m_optionsRadio) {
				opt.setEnabled(false);
			}
		} else {
			m_optionsCombo.setEnabled(false);
		}
	}

	/***************************************************************************
	 * Used to set the hint of the text input
	 **************************************************************************/
	private void setHint() {
		// Set the text hint for the console input
		String hint = "";
		
		if(m_expected!=null) 
		{
		
			for (String opt : m_expected) {
				if (hint.length() > 0) {
					hint += ",";
				}
				hint += opt;
			} //for
		}
		m_textInput.setHint(hint);
	} //setHint

	/***************************************************************************
	 * Callback for radio buttons or combo (selection list)
	 * 
	 * @param e
	 *            Selection event
	 **************************************************************************/
	public void widgetDefaultSelected(SelectionEvent e) {
		widgetSelected(e);
	}

	/***************************************************************************
	 * Callback for radio buttons or combo (selection list)
	 * 
	 * @param e
	 *            Selection event
	 **************************************************************************/
	public void widgetSelected(SelectionEvent e) {
		if (e.widget instanceof Button || e.widget instanceof Combo) {
			if (e.widget == m_commitButton) {
				if (m_promptData != null) // Prompt mode
				{
					handlePromptAnswer();
				} else {
					handleCommand();
				}
			} else if (e.widget == m_resetButton) {
				
				m_textInput.reset();
				m_textInput.promptStart();
				
				if (m_promptData != null) {
					resetOptions();
				}
				updateButtons();
			} else {
				if (e.widget instanceof Combo) {
					m_selectedOption = m_optionsCombo.getSelectionIndex();
				} else {
					Button b = (Button) e.widget;
					m_selectedOption = (Integer) b.getData("ID");
				}
				// Update the text on the textual input
				m_textInput.setValue(m_promptData.getExpected().elementAt(
						m_selectedOption));
				updateButtons();
			}
		}
	}

	/***************************************************************************
	 * Called when the user types selections
	 **************************************************************************/
	private void onUserType(String typed) {
		if (m_promptData != null)
			if (m_promptData.isList() && m_promptData.getExpected() != null) {
				boolean found = false;
				if (!typed.isEmpty()) {
					for (String expected : m_promptData.getExpected()) {
						if (expected.equals(typed)) {
							int idx = m_promptData.getExpected().indexOf(
									expected);
							m_selectedOption = idx;
							if (m_optionsCombo != null) {
								m_optionsCombo.select(idx);
							} else {
								m_optionsRadio.get(idx).setSelection(true);
							}
							found = true;
							break;
						}
					}
				}
				if (!found) {
					m_selectedOption = -1;
					if (m_optionsCombo != null) {
						m_optionsCombo.clearSelection();
					} else {
						for (Button b : m_optionsRadio) {
							b.setSelection(false);
						}
					}
				}
			}
	}

	/***************************************************************************
	 * Reset the input area and show the no input page
	 * 
	 * @param resetAll
	 *            If true, reset automatically all the rest of input handlers
	 **************************************************************************/
	public void reset() {
		Logger.debug("Reset input area", Level.PROC, this);
		m_promptText.setText("Enter command:");
		GridData gd = (GridData) m_promptText.getLayoutData();
		gd.heightHint = SWT.DEFAULT;
		m_promptText.getVerticalBar().setVisible(false);
		m_textInput.delHint();
		m_textInput.reset();
		m_textInput.setEnabled(true);
		clearOptions();
		m_promptData = null;
		m_expected = null;
		stopBlinking();
	}

	/***************************************************************************
	 * Prepare the blinking
	 ***************************************************************************/
	private void prepareBlinking() {
		m_blinkSwitch = true;
		long msec = 10000;

		try {
			msec = m_model.getRuntimeInformation().getPromptWarningDelay() * 1000;
		} catch (NumberFormatException ex) {
			ex.printStackTrace();
		}

		m_blinkerLauncher = new PromptBlinkerLauncher(this, msec);
	}

	/***************************************************************************
	 * Start the blinking
	 ***************************************************************************/
	synchronized void startBLinking() {
		m_blinker = new PromptBlinker(this);
		String soundFile = s_cfg.getProperty(PropertyKey.PROMPT_SOUND_FILE);
		if ((soundFile != null) && (!soundFile.isEmpty())) {
			File f = new File(soundFile);
			if (f.canRead()) {
				m_sounder = new PromptSounder(soundFile);
				m_sounder.start();
			} else {
				m_sounder = null;
			}
		}
	}

	/***************************************************************************
	 * Start the blinking
	 ***************************************************************************/
	private synchronized void stopBlinking() {
		if (m_blinker != null) {
			m_blinker.stopBlinking();
			try {
				m_blinker.interrupt();
				m_blinker.join();
			} catch (Exception e) {
			}
			;
			m_blinker = null;
		}
		if (m_sounder != null) {
			m_sounder.cancel();
			m_sounder = null;
		}
		if (m_blinkerLauncher != null) {
			m_blinkerLauncher.cancel();
			m_blinkerLauncher = null;
		}
		// Ensure the text field has white background color
		m_promptText.setBackground(Display.getCurrent().getSystemColor(
				SWT.COLOR_WHITE));
		m_promptText.redraw();
	}

	/***************************************************************************
	 * Do a half-blink
	 ***************************************************************************/
	void blink() {
		if (!m_promptText.isDisposed()) {
			if (m_blinkSwitch) {
				m_blinkSwitch = false;
				m_promptText.setBackground(Display.getCurrent().getSystemColor(
						SWT.COLOR_YELLOW));
			} else {
				m_blinkSwitch = true;
				m_promptText.setBackground(Display.getCurrent().getSystemColor(
						SWT.COLOR_WHITE));
			}
			m_promptText.redraw();
		}
	}

	/***************************************************************************
	 * Callback for keyboard input on the console input (key press)
	 * 
	 * @param e
	 *            Key event
	 **************************************************************************/
	public void keyPressed(KeyEvent e) {
		if ((e.keyCode == 8) && (m_textInput.getValue().isEmpty())) {
			e.doit = false;
		}
	}

	/***************************************************************************
	 * Callback for keyboard input (key release)
	 * 
	 * @param e
	 *            Key event
	 **************************************************************************/
	public void keyReleased(KeyEvent e) {
		if ((e.keyCode == 13 || e.keyCode == 16777296) && m_promptData != null) {
			handlePromptAnswer();
		} else if ((e.keyCode == 13 || e.keyCode == 16777296)
				&& m_promptData == null) {
			handleCommand();
		} else {
			boolean enabled = !m_textInput.getValue().isEmpty();
			m_commitButton.setEnabled(enabled);
			m_resetButton.setEnabled(enabled);
			onUserType(m_textInput.getValue());
		}
	}

	/***************************************************************************
	 * Cancel ongoing prompt
	 **************************************************************************/
	public void cancelPrompt() {
		m_promptData.setCancel();
		if (m_clientMode.equals(ClientMode.CONTROL)) {
			s_proxy.answerPrompt(m_promptData);
		}
		m_textInput.promptEnd();
		reset();
	}

	/***************************************************************************
	 * Handle a text command
	 **************************************************************************/
	private void handlePromptAnswer() {
		Logger.debug("Handle prompt answer", Level.GUI, this);

		String answer = m_textInput.getValue();
		if (answer.length() == 0 && m_selectedOption < 0) {
			MessageDialog.openError(getShell(), "Prompt error",
					"Cannot commit, no value given");
			m_textInput.reset();
			m_textInput.promptStart();
			
			return;
		}
		if (m_numericInput) {
			try {
				// Is it an int?
				Integer.parseInt(answer);
				m_promptData.setReturnValue(answer);
			} catch (NumberFormatException ex) {
				try {
					// Is it a double?
					Double.parseDouble(answer);
					m_promptData.setReturnValue(answer);
				} catch (NumberFormatException ex2) {
					try {
						// Is it a hex?
						if (answer.startsWith("0x")) {
							int a = Integer.parseInt(answer.substring(2), 16);
							m_promptData.setReturnValue(a + "");
						} else {
							throw new NumberFormatException("Hex out of range");
						}
					} catch (NumberFormatException ex3) {
						MessageDialog.openError(getShell(), "Prompt error",
								"Cannot commit, expected a numeric value");
						
						m_textInput.reset();
						m_textInput.promptStart();
						
						return;
					}

				}
			}
		} else if (m_expected != null) {
			Logger.debug("Have expected values", Level.GUI, this);
			// If the input text field has an answer
			if (!answer.isEmpty()) {
				Logger.debug("Current text field answer: '" + answer + "'",
						Level.GUI, this);
				// Check if there is a selected option
				if (m_selectedOption >= 0) {
					// If there is a choice, it must be consistent with the text
					// input
					String optString = m_expected.get(m_selectedOption);

					Logger.debug("Option string: '" + optString + "'",
							Level.GUI, this);

					if (!optString.equals(answer)) {
						MessageDialog
								.openError(getShell(), "Prompt error",
										"Conflicting values found between text area and buttons");
						
						resetOptions();
						
						m_textInput.reset();
						m_textInput.promptStart();
						
						return;
					}
				}
				// If there is no selected option, check that the text input
				// matches any of the expected values
				else {
					boolean accept = false;
					for (String opt : m_expected) {
						if (opt.equals(answer)) {
							accept = true;
							break;
						}
					}
					if (!accept) {
						String values = "";
						for (String exp : m_expected)
							values += exp + "\n";
						MessageDialog.openError(getShell(), "Prompt error",
								"Must enter one of the expected values:\n"
										+ values);
						m_textInput.reset();
						m_textInput.promptStart();
						return;
					}
				}

				// If we reach this point it is ok. Set the prompt answer, but
				// getting the corresponding index of the option
				int idx = -1;
				int count = 0;
				for (String expected : m_expected) {
					if (expected.equals(answer)) {
						idx = count;
						break;
					}
					count++;
				}
				m_promptData.setReturnValue(Integer.toString(idx));
			}
			// If the text input does not have a text, at least there must be a
			// selected option in the list
			else if (m_selectedOption == -1) {
				String values = "";
				for (String exp : m_expected)
					values += exp + "\n";
				MessageDialog.openError(getShell(), "Prompt error",
						"Must enter one of the expected values:\n" + values);
				m_textInput.reset();
				m_textInput.promptStart();
				return;
			}
			// We have a selected option
			else {
				// Set the option index as return value
				m_promptData.setReturnValue(Integer.toString(m_selectedOption));
			}
		} else {
			m_promptData.setReturnValue(answer);
		}
		s_proxy.answerPrompt(m_promptData);
		m_parent.resetPrompt();
		m_textInput.promptEnd();
	}

	/***************************************************************************
	 * Handle a text command
	 **************************************************************************/
	private void handleCommand() {
		String promptValue = m_textInput.getValue();
		if (promptValue.isEmpty()) {
			return;
		}
		m_parent.issueCommand(promptValue);
		m_textInput.reset();
	}

	/***************************************************************************
	 * Clear the option buttons
	 **************************************************************************/
	private void clearOptions() {
		if (m_optionsRadio.size() > 0) {
			for (Button opt : m_optionsRadio) {
				opt.dispose();
			}
			m_optionsRadio.clear();
		}
		if (m_optionScroll != null) {
			m_optionContainer.dispose();
			m_optionScroll.dispose();
			m_optionScroll = null;
		}
		if (m_optionsCombo != null) {
			m_optionsCombo.dispose();
			m_optionsCombo = null;
		}
		m_selectedOption = -1;
		updateButtons();
	}

	/***************************************************************************
	 * Reset the selection in option buttons
	 **************************************************************************/
	private void resetOptions() {
		for (Button opt : m_optionsRadio) {
			opt.setSelection(false);
		}
		if (m_optionsCombo != null) {
			m_optionsCombo.deselectAll();
		}
		m_selectedOption = -1;
	}

	/***************************************************************************
	 * Update the available options
	 * 
	 * @param options
	 * @param expectedValues
	 **************************************************************************/
	private boolean updateOptions(Vector<String> options,
			Vector<String> expectedValues, String defaultOption) {
		if (options == null || options.size() == 0)
			return false;

		m_optionScroll = new ScrolledComposite(this, SWT.H_SCROLL
				| SWT.V_SCROLL);
		// DO NOT PUT LAYOUT DATA FOR THE SCROLLED: IT IS ADJUSTED WITH THE SASH
		// EVENTS!

		m_optionContainer = new Composite(m_optionScroll, SWT.NONE);
		m_optionContainer.setLayout(new GridLayout(1, true));

		boolean defaultOptionSet = false;

		if (m_promptDisplayType == PromptDisplayType.RADIO) {
			defaultOptionSet = setupRadioOptions(options, expectedValues,
					defaultOption);
		} else {
			defaultOptionSet = setupComboOptions(options, expectedValues,
					defaultOption);
		}
		m_optionContainer.pack();
		m_optionScroll.setContent(m_optionContainer);

		return defaultOptionSet;
	}

	/***************************************************************************
	 * Setup radio options
	 **************************************************************************/
	private boolean setupRadioOptions(Vector<String> options,
			Vector<String> expectedValues, String defaultOption) {
		int count = 0;
		boolean defaultOptionSet = false;
		for (String option : options) {
			try {
				String expected = "";
				if (expectedValues.size() > count) {
					expected = expectedValues.elementAt(count);
				}

				Button b = new Button(m_optionContainer, SWT.RADIO);
				b.setFont(m_myFont);
				String value = option.substring(
						option.indexOf(KEY_SEPARATOR) + 1, option.length());
				// Take into account the LIST|ALPHA case. When keys are the same
				// as values,
				// we do not want to display it twice.
				if (!expected.isEmpty() && !expected.equals(value)) {
					b.setText(expected + " : " + value);
				} else {
					b.setText(value);
				}

				if (!defaultOption.isEmpty()
						&& (expected.equals(defaultOption))) {
					b.setSelection(true);
					defaultOptionSet = true;
					m_selectedOption = count;
				} else {
					b.setSelection(false);
				}
				b.setData("ID", count);
				b.addKeyListener(this);
				b.addSelectionListener(this);
				m_optionsRadio.add(b);
			} catch (Exception ex) {
				ex.printStackTrace();
				Logger.error(
						"Error processing prompt radio options: "
								+ ex.getLocalizedMessage(), Level.GUI, this);
			}
			count++;
		}
		return defaultOptionSet;
	}

	/***************************************************************************
	 * Setup combo options
	 **************************************************************************/
	private boolean setupComboOptions(Vector<String> options,
			Vector<String> expectedValues, String defaultOption) {
		boolean defaultOptionSet = false;

		m_optionsCombo = new Combo(m_optionContainer, SWT.DROP_DOWN
				| SWT.READ_ONLY);
		m_optionsCombo.setData("IDs", expectedValues);
		m_optionsCombo.addKeyListener(this);
		m_optionsCombo.addSelectionListener(this);
		
		String s = ""; //expected value
		
		for (String option : options) {
			try {
				s = option.substring(
						option.indexOf(KEY_SEPARATOR) + 1, option.length());
				m_optionsCombo.add(s);
				
			} catch (Exception ex) {
				ex.printStackTrace();
				Logger.error(
						"Error processing combo options: "
								+ ex.getLocalizedMessage(), Level.GUI, this);
				m_optionsCombo.add("???");
			}
			
		} //for
		try {
			if (!defaultOption.isEmpty()) {
				int idx = expectedValues.indexOf(defaultOption);
				m_optionsCombo.select(idx);
				defaultOptionSet = true;
				m_selectedOption = idx;
			}
		} catch (Exception ex) {
			ex.printStackTrace();
			Logger.error(
					"Error processing default combo option: "
							+ ex.getLocalizedMessage(), Level.GUI, this);
		}
		return defaultOptionSet;
	}

	/***************************************************************************
	 * Update the input control buttons
	 **************************************************************************/
	private void updateButtons() {
		boolean hasInput = (m_selectedOption != -1);
		m_commitButton.setEnabled(hasInput);
		m_resetButton.setEnabled(hasInput);
	}

	@Override
	public void onSashMoved(int height) {
		if (m_optionScroll != null) {
			int substract = m_promptText.getBounds().height
					+ m_textInput.getContents().getBounds().height + 25;
			int finalHeight = height - substract;
			;
			if (finalHeight < 40)
				finalHeight = 40;
			m_optionScroll.setSize(getClientArea().width, finalHeight);
		}
	}

	@Override
	public void proposalAccepted(IContentProposal arg0) {

		//Remove current selection
		onUserType("");
		//Put current selection
		onUserType(m_textInput.getValue());
		
		//enable buttons
		m_commitButton.setEnabled(true);
		m_resetButton.setEnabled(true);
	}
}
