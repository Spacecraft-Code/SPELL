///////////////////////////////////////////////////////////////////////////////
//
// PACKAGE   : com.astra.ses.spell.gui.dialogs
// 
// FILE      : MasterShellDialog.java
//
// DATE      : Jun 26, 2014
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
package com.astra.ses.spell.gui.dialogs;

import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

import com.astra.ses.spell.gui.Activator;
import com.astra.ses.spell.gui.core.exceptions.CommandFailed;
import com.astra.ses.spell.gui.core.interfaces.IShellManager;
import com.astra.ses.spell.gui.core.interfaces.ServiceManager;
import com.astra.ses.spell.gui.core.interfaces.listeners.IShellListener;
import com.astra.ses.spell.gui.core.model.types.Severity;
import com.astra.ses.spell.gui.preferences.interfaces.IConfigurationManager;
import com.astra.ses.spell.gui.preferences.keys.FontKey;
import com.astra.ses.spell.gui.preferences.keys.GuiColorKey;
import com.astra.ses.spell.gui.views.controls.input.PromptField;

/*******************************************************************************
 * @brief Dialog containing the master shell
 ******************************************************************************/
public class MasterShellDialog extends TitleAreaDialog implements KeyListener,IShellListener
{
	/** Holds the dialog image icon */
	private Image	        m_image;
	private static IShellManager	s_smgr	= null;
	/** Console display */
	private Text	             m_display;
	/** The command input field */
	private PromptField	         m_prompt;

	/***************************************************************************
	 * Constructor
	 * 
	 * @param shell
	 *            The parent shell
	 **************************************************************************/
	public MasterShellDialog(Shell shell)
	{
		super(shell);
		if (s_smgr == null)
		{
			s_smgr = (IShellManager) ServiceManager.get(IShellManager.class);
		}
		// Try to load the shell plugin if available
		if (s_smgr.haveShell())
		{
			s_smgr.addShellListener(this);
		}
		// Obtain the image for the dialog icon
		ImageDescriptor descr = Activator.getImageDescriptor("icons/dlg_exec.png");
		m_image = descr.createImage();
	}

	/***************************************************************************
	 * Called when the dialog is about to close.
	 * 
	 * @return The superclass return value.
	 **************************************************************************/
	public boolean close()
	{
		if (s_smgr.haveShell())
		{
			s_smgr.removeShellListener(this);
		}
		m_image.dispose();
		return super.close();
	}
	
	/***************************************************************************
	 * Creates the dialog contents.
	 * 
	 * @param parent
	 *            The base composite of the dialog
	 * @return The resulting contents
	 **************************************************************************/
	protected Control createContents(Composite parent)
	{
		Control contents = super.createContents(parent);
		getShell().setSize(800, 600);
		setMessage("Type commands to perform actions\n\n(Type 'help' to get a list of available commands)");
		setTitle("Master command shell");
		setTitleImage(m_image);
		setShellStyle( getShellStyle() | SWT.RESIZE );
		return contents;
	}

	/***************************************************************************
	 * Create the dialog area contents.
	 * 
	 * @param parent
	 *            The base composite of the dialog
	 * @return The resulting contents
	 **************************************************************************/
	protected Control createDialogArea(Composite parent)
	{
		// Main composite of the dialog area -----------------------------------
		Composite top = new Composite(parent, SWT.NONE);
		top.setLayoutData(new GridData(GridData.FILL_BOTH));
		top.setLayout( new GridLayout(1,true) );

		IConfigurationManager cfg = (IConfigurationManager) ServiceManager.get(IConfigurationManager.class);

		Font codeFont = cfg.getFont(FontKey.MASTERC);
		Color bcolor = cfg.getGuiColor(GuiColorKey.CONSOLE_BG);
		Color wcolor = cfg.getGuiColor(GuiColorKey.CONSOLE_FG);

		// Create the console display
		m_display = new Text(top, SWT.MULTI | SWT.BORDER | SWT.V_SCROLL | SWT.H_SCROLL );
		m_display.setFont(codeFont);
		m_display.setBackground(bcolor);
		m_display.setForeground(wcolor);
		GridData ddata = new GridData( GridData.FILL_BOTH );
		ddata.minimumHeight = 200;
		m_display.setLayoutData( ddata );
		m_display.setText("");
		m_display.setEditable(false);

		// Create the input field
		System.out.println("DVI: Creates the Prompt field");
		m_prompt = new PromptField(top, "mShellDialog");
		//m_prompt.getContents().setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		m_prompt.getContents().addKeyListener(this);
		
		System.out.println("DVI: Set focus");
		m_prompt.getContents().setFocus();

		return parent;
	}

	/***************************************************************************
	 * Create the button bar buttons.
	 * 
	 * @param parent
	 *            The Button Bar.
	 **************************************************************************/
	@Override
	protected void createButtonsForButtonBar(Composite parent)
	{
	}

	/***************************************************************************
	 * Callback for key release event. Used for command processing.
	 * 
	 * @param e
	 *            Key release event
	 **************************************************************************/
	public void keyReleased(KeyEvent e)
	{
		// Check if the key code corresponds to one of the two enter keys.
		if (e.keyCode == 13 || e.keyCode == 16777296)
		{
			// Obtain the contents of the input field
			String cmdString = m_prompt.getValue();
			try
			{
				// Add the text to the display, reset the prompt, and
				// send the command string to the shell manager
				if (!s_smgr.haveShell())
				{
					addDisplayMessage("No shell plugin available.");
					return;
				}
				addDisplayMessage(m_prompt.getPrompt() + PromptField.PROMPT_SYMBOL + cmdString);
				s_smgr.shellInput(cmdString);
			}
			catch (CommandFailed ex)
			{
				// If the console manager raises an error show it on the
				// display
				addDisplayMessage("ERROR (" + m_prompt.getPrompt() + "): " + ex.getLocalizedMessage());
			}
			m_prompt.reset();
		}
	}

	/***************************************************************************
	 * Add a text message to the display
	 **************************************************************************/
	protected void addDisplayMessage(String message)
	{
		String text = m_display.getText();
		// Take into account wether the display is empty or not
		if (text.length() > 0)
		{
			text += m_display.getLineDelimiter() + message;
		}
		else
		{
			text = message;
		}
		m_display.setText(text);
		m_display.setSelection(text.length());
	}

	/***************************************************************************
	 * 
	 **************************************************************************/
	@Override
	public void shellOutput(String output, Severity severity)
	{
		addDisplayMessage(output);
	}

	@Override
    public void keyPressed(KeyEvent e)
    {
    }

}
