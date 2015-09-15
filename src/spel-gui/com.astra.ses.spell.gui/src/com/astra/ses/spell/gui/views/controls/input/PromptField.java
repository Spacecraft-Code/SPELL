///////////////////////////////////////////////////////////////////////////////
//
// PACKAGE   : com.astra.ses.spell.gui.views.controls.input
// 
// FILE      : PromptField.java
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
package com.astra.ses.spell.gui.views.controls.input;

import java.util.ArrayList;
import java.util.Vector;

import org.eclipse.jface.bindings.keys.KeyStroke;
import org.eclipse.jface.fieldassist.ContentProposalAdapter;
import org.eclipse.jface.fieldassist.ControlDecoration;
import org.eclipse.jface.fieldassist.FieldDecorationRegistry;
import org.eclipse.jface.fieldassist.IContentProposal;
import org.eclipse.jface.fieldassist.IContentProposalListener;
import org.eclipse.jface.fieldassist.IContentProposalProvider;
import org.eclipse.jface.fieldassist.SimpleContentProposalProvider;
import org.eclipse.jface.fieldassist.TextContentAdapter;
import org.eclipse.osgi.framework.internal.core.Tokenizer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.Label;
import org.w3c.dom.events.MouseEvent;

import com.astra.ses.spell.gui.core.interfaces.ServiceManager;
import com.astra.ses.spell.gui.core.model.types.ItemStatus;
import com.astra.ses.spell.gui.preferences.interfaces.IConfigurationManager;
import com.astra.ses.spell.gui.preferences.keys.FontKey;
import com.astra.ses.spell.gui.preferences.keys.GuiColorKey;
import com.astra.ses.spell.gui.types.GuiExecutorCommand;



/*******************************************************************************
 * Custom implementation of a prompt field. This control parses the input to
 * remove the prompt text, keeps the prompt text visible during user input,
 * shows input hints if required, and mantains an input history which can be
 * used with the cursor up/down keys.
 * 
 */
public class PromptField implements KeyListener
{
	private static IConfigurationManager s_cfg = null;
	
	/** Defined symbol for the prompt text end */
	public static final String	PROMPT_SYMBOL	= ">";

	/** Static block to initialize the colors */
	static
	{
		s_cfg = (IConfigurationManager) ServiceManager.get(IConfigurationManager.class);
	}

	/** Holds the real text input. Cannot be subclassed */
	private Text	           m_contents;
	/** Prompt string */
	private String	           m_promptStr;
	/** Prompt label */
	private Label			   m_promptLabel;
	/** Part of the prompt string to be removed */
	private String             m_promptRest;
	/** Default prompt string */
	private String	           m_defaultPromptStr;
	/** Hint string, if any */
	private String	           m_hintStr;
	/** History of previous inputs */
	private Vector<String>	   m_previousValues;
	/** Current position at the history */
	private int	               m_historyIndex;

	/** list of commands */
	private SimpleContentProposalProvider provider;
	/** decorator */
	private ControlDecoration deco;
	
	/** Content proposal */
	ContentProposalAdapter adp; 
	
	
	boolean bPrompt;
	
	
	/***************************************************************************
	 * Constructor
	 * 
	 * @param parent
	 *            Top composite
	 * @param prompt
	 *            Initial prompt text
	 **************************************************************************/
	public PromptField(Composite parent, String prompt)
	{
		// Create an empty history
		m_previousValues = new Vector<String>();
		m_historyIndex = 0;
		
		// Create the input text label
		m_promptLabel = new Label(parent, SWT.SINGLE);
		GridData gd = new GridData();
		m_promptLabel.setLayoutData(gd);

		m_promptLabel.setText(PROMPT_SYMBOL);
		
		// input text field
		m_contents = new Text(parent, SWT.BORDER | SWT.SINGLE);
		m_contents.setBackground(s_cfg.getGuiColor(GuiColorKey.CONSOLE_BG));
		m_contents.setForeground(s_cfg.getGuiColor(GuiColorKey.CONSOLE_FG));

		gd = new GridData();
		gd.horizontalAlignment = SWT.FILL;
		gd.horizontalIndent = 50;
		gd.widthHint = 50;
		m_contents.setLayoutData(gd);
		
		// Assign the prompt and hint strings
		
		m_promptStr = "";
		m_defaultPromptStr = "";
		m_hintStr = "";

		// Create field decoration
		createDecoration();

		IConfigurationManager cfg = (IConfigurationManager) ServiceManager.get(IConfigurationManager.class);
		m_contents.setFont(cfg.getFont(FontKey.MASTERC));

		// We need to listen ourselves for controlling the input
		m_contents.addKeyListener(this);
		GridData mcGrid = new GridData( GridData.FILL_HORIZONTAL );
		m_contents.setLayoutData(mcGrid);

		// Reset the prompt to the initial state
		reset();
		parent.layout();
	}

	/***************************************************************************
	 * Create decoration for the input field
	 **************************************************************************/
	private void createDecoration()
	{
		// Field decoration
		deco = new ControlDecoration(m_contents, SWT.RIGHT);
		deco.setDescriptionText("Use CTRL+Space to see the possible commands");
		deco.setImage(FieldDecorationRegistry.getDefault().getFieldDecoration(FieldDecorationRegistry.DEC_INFORMATION)
		        .getImage());
		deco.setShowOnlyOnFocus(false);
		deco.setShowHover(true);

		KeyStroke keyStroke;
		ArrayList<String> possibleCommandsList = new ArrayList<String>();
		for (GuiExecutorCommand cmd : GuiExecutorCommand.values())
		{
			possibleCommandsList.add(cmd.label.toLowerCase());
		}
		String[] possibleCommands = possibleCommandsList.toArray(new String[0]);
		char[] autoActivationCharacters = new char[0];
		
		provider = new SimpleContentProposalProvider(possibleCommands);
		
		try
		{
			keyStroke = KeyStroke.getInstance("Ctrl+Space");
			adp = new ContentProposalAdapter(m_contents, new TextContentAdapter(), provider, keyStroke,
			        autoActivationCharacters);
			
			adp.setProposalAcceptanceStyle(ContentProposalAdapter.PROPOSAL_REPLACE);
			adp.addContentProposalListener((IContentProposalListener) m_contents.getParent().getParent());
		}
		catch (Exception ex)
		{
			ex.printStackTrace();
		}
	}

	
	/***************************************************************************
	 * Constructor
	 * 
	 * @param parent
	 *            Top composite
	 * @param prompt
	 *            Initial prompt text
	 **************************************************************************/
	public PromptField(Composite parent, String prompt, KeyListener listener)
	{
		this(parent, prompt);
		m_contents.addKeyListener(listener);
	}

	/***************************************************************************
	 * Set control font size
	 **************************************************************************/
	public void setFont( Font font )
	{
		m_contents.setFont(font);
	}

	/***************************************************************************
	 * Obtain the wrapped control
	 * 
	 * @return Text the wrapped control
	 **************************************************************************/
	public Text getContents()
	{
		return m_contents;
	}

	/***************************************************************************
	 * Enable or disable the control
	 **************************************************************************/
	public void setEnabled(boolean enable)
	{
		m_contents.setEnabled(enable);
	}

	/***************************************************************************
	 * Reset the prompt field to the initial state
	 **************************************************************************/
	public void reset()
	{
		String prefix = "Type ";
		String suffix = PROMPT_SYMBOL;
		
		promptEnd();
		
		if(bPrompt) 
		{
			if (!m_hintStr.isEmpty())
			{
				// If the hint is too long, don't use it but put it as tooltip
				if (m_hintStr.length()>15)
				{
					m_contents.setToolTipText("Possible inputs: " + m_hintStr);
					m_hintStr = "";
					m_promptLabel.setText(suffix);
				}
				else
				{
					suffix = " (" + m_hintStr + ")" + suffix;
					m_promptLabel.setText(prefix + suffix);
				}
			}
			else
			{
				//Reset options
				setHint("");
				
			}
		} 
		else
		{
			m_contents.setToolTipText("Enter command or prompt answer in this control");
		}
		
		m_promptStr = m_defaultPromptStr;
		m_promptRest = m_promptStr;
		m_contents.setText(m_promptRest);
		
		m_contents.setFocus();
		m_contents.getParent().layout();
		
		
	}

	/***************************************************************************
	 * 
	 **************************************************************************/
	public void promptStart()
	{
		m_contents.setBackground(s_cfg.getStatusColor(ItemStatus.WARNING));
		m_contents.setForeground(s_cfg.getGuiColor(GuiColorKey.CONSOLE_BG));
		m_contents.setFocus();
		bPrompt = true;
	}

	/***************************************************************************
	 * Prompt has finished, so presentation must be set to default
	 **************************************************************************/
	public void promptEnd()
	{
		m_contents.setBackground(s_cfg.getGuiColor(GuiColorKey.CONSOLE_BG));
		m_contents.setForeground(s_cfg.getGuiColor(GuiColorKey.CONSOLE_FG));
		m_contents.update();
		bPrompt = false;
	}

	/***************************************************************************
	 * Set the field value
	 * 
	 * @param value
	 *            The field value
	 **************************************************************************/
	public void setValue(String value)
	{
		m_contents.setText( m_promptRest + value);
		m_contents.setSelection(m_contents.getText().length());
	}

	/***************************************************************************
	 * Obtain the field value (without the prompt text)
	 * 
	 * @return The field value
	 **************************************************************************/
	public String getValue()
	{
		String answer = m_contents.getText();
		answer = answer.replace( m_promptRest, "");
		return answer;
	}


	/***************************************************************************
	 * Set the hint text.
	 * 
	 * @param hint
	 *            The hint text
	 **************************************************************************/
	public void setHint(String hint)
	{
		String prefix = "Type ";
		String suffix = PROMPT_SYMBOL;
		m_hintStr = hint;
		
		
		if (!m_hintStr.isEmpty())
		{
			// If the hint is too long, don't use it but put it as tooltip
			if (m_hintStr.length()>15)
			{
				m_contents.setToolTipText("Possible inputs: " + m_hintStr);
				//m_hintStr = "";
				m_promptLabel.setText(suffix);
			}
			else
			{
				m_contents.setToolTipText("Enter possible inputs in this control");
				suffix = " (" + m_hintStr + ")" + suffix;
				m_promptLabel.setText(prefix + suffix);
			}
			
			String[] commands  = hint.split(",");
			
			if (commands.length != 0)
			{
				provider = new SimpleContentProposalProvider(commands);
				adp.setContentProposalProvider(provider);
				deco.show();
				
			}
		}
		else
		{
			String[] empty = {};
			provider = new SimpleContentProposalProvider(empty);
			adp.setContentProposalProvider(provider);
			deco.hide();
		}
		m_contents.getParent().layout();
	}

	/***************************************************************************
	 * Remove the hint text
	 **************************************************************************/
	public void delHint()
	{
		if (!m_hintStr.equals(""))
		{
			m_hintStr = "";
			reset();
		}
		m_promptLabel.setText(PROMPT_SYMBOL);
		
		//Restore command options
		ArrayList<String> possibleCommandsList = new ArrayList<String>();
		for (GuiExecutorCommand cmd : GuiExecutorCommand.values())
		{
			possibleCommandsList.add(cmd.label.toLowerCase());
		}
		

		//Setup commands again
		String[] possibleCommands = possibleCommandsList.toArray(new String[0]);
		provider = new SimpleContentProposalProvider(possibleCommands);
		adp.setContentProposalProvider(provider);
		deco.show();
	}

	/***************************************************************************
	 * Obtain the prompt text
	 * 
	 * @return The prompt text
	 **************************************************************************/
	public String getPrompt()
	{
		return m_promptStr;
	}

	/***************************************************************************
	 * Callback for key pressed event
	 * 
	 * @param e
	 *            Key event
	 **************************************************************************/
	public void keyPressed(KeyEvent e)
	{
		// If the user presses Cursor Up, move one step back in the history
		if (e.keyCode == 16777217)
		{
			if (m_previousValues.size() > 0)
			{
				setValue(m_previousValues.elementAt(m_historyIndex));
				m_historyIndex--;
				if (m_historyIndex < 0) m_historyIndex = 0;
			}
		}
		// If the user presses Cursor Down, move one step further in the hist.
		if (e.keyCode == 16777218)
		{
			if (m_previousValues.size() > 0)
			{
				// If we are at the very beginning of the history, clear
				// the field to make easy to enter a new value
				if (m_historyIndex >= m_previousValues.size() - 1)
				{
					m_historyIndex = m_previousValues.size() - 1;
					setValue("");
				}
				else
				{
					m_historyIndex++;
					setValue(m_previousValues.elementAt(m_historyIndex));
				}
			}
		}
	}

	/***************************************************************************
	 * Callback for key release events
	 * 
	 * @param e
	 *            The key event
	 **************************************************************************/
	public void keyReleased(KeyEvent e)
	{
		
		// If the enter keys are used, store the field value in the history
		if (e.keyCode == 13 || e.keyCode == 16777296)
		{
			m_previousValues.addElement(getValue());
			m_historyIndex = m_previousValues.size() - 1;
		}
	}

}
