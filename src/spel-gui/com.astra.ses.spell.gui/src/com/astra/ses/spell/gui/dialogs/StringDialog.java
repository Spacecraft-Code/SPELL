///////////////////////////////////////////////////////////////////////////////
//
// PACKAGE   : com.astra.ses.spell.gui.dialogs
// 
// FILE      : StringDialog.java
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
package com.astra.ses.spell.gui.dialogs;

import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

import com.astra.ses.spell.gui.Activator;

/*******************************************************************************
 * @brief Utility dialog for prompting for strings
 * @date 28/04/08
 ******************************************************************************/
public class StringDialog extends TitleAreaDialog
{
	// =========================================================================
	// # INSTANCE DATA MEMBERS
	// =========================================================================

	// PRIVATE -----------------------------------------------------------------
	/** Holds the dialog message */
	private String	m_message;
	/** Holds the long dialog message */
	private String	m_longMessage;
	/** Holds the dialog title */
	private String	m_title;
	/** Hide type flag */
	private boolean	m_hideType;
	/** Text control */
	private Text	m_text;
	/** Holds the dialog image icon */
	private Image	m_image;
	/** Holds the user answer */
	private String	m_answer;

	// PROTECTED ---------------------------------------------------------------
	// PUBLIC ------------------------------------------------------------------

	// =========================================================================
	// # ACCESSIBLE METHODS
	// =========================================================================

	/***************************************************************************
	 * Constructor
	 **************************************************************************/
	public StringDialog(Shell shell, String title, String msg, String longMsg,
	        boolean hideType)
	{
		super(shell);
		// Obtain the image for the dialog icon
		ImageDescriptor descr = Activator
		        .getImageDescriptor("icons/dlg_question.png");
		m_image = descr.createImage();
		m_message = msg;
		m_longMessage = longMsg;
		m_title = title;
		m_answer = null;
		m_text = null;
		m_hideType = hideType;
	}

	/***************************************************************************
	 * Called when the dialog is about to close.
	 * 
	 * @return The superclass return value.
	 **************************************************************************/
	public boolean close()
	{
		m_image.dispose();
		m_text.dispose();
		return super.close();
	}

	public String getAnswer()
	{
		return m_answer;
	}

	// =========================================================================
	// # NON-ACCESSIBLE METHODS
	// =========================================================================

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
		setMessage(m_message);
		setTitle(m_title);
		setTitleImage(m_image);
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
		GridLayout layout = new GridLayout();
		top.setLayoutData(new GridData(GridData.FILL_BOTH));
		layout.marginHeight = 30;
		layout.marginWidth = 10;
		layout.marginTop = 5;
		layout.marginBottom = 10;
		layout.marginLeft = 10;
		layout.marginRight = 10;
		layout.numColumns = 1;
		top.setLayout(layout);
		GridData tgd = new GridData(GridData.HORIZONTAL_ALIGN_FILL,
		        GridData.VERTICAL_ALIGN_BEGINNING, true, false);
		top.setLayoutData(tgd);

		Label label = new Label(top, SWT.NONE);
		label.setText(m_longMessage);

		if (m_hideType)
		{
			m_text = new Text(top, SWT.BORDER | SWT.PASSWORD);
		}
		else
		{
			m_text = new Text(top, SWT.BORDER);
		}
		GridData gd = new GridData(GridData.HORIZONTAL_ALIGN_FILL,
		        GridData.VERTICAL_ALIGN_BEGINNING, true, false);
		gd.verticalIndent = 10;
		gd.widthHint = 120;
		m_text.setLayoutData(gd);
		return parent;
	}

	/***************************************************************************
	 * Create the button bar buttons.
	 * 
	 * @param parent
	 *            The Button Bar.
	 **************************************************************************/
	protected void createButtonsForButtonBar(Composite parent)
	{
		createButton(parent, IDialogConstants.OK_ID, IDialogConstants.OK_LABEL,
		        true);
		createButton(parent, IDialogConstants.CANCEL_ID,
		        IDialogConstants.CANCEL_LABEL, false);
	}

	/***************************************************************************
	 * Called when one of the buttons of the button bar is pressed.
	 * 
	 * @param buttonId
	 *            The button identifier.
	 **************************************************************************/
	protected void buttonPressed(int buttonId)
	{
		switch (buttonId)
		{
		case IDialogConstants.OK_ID:
			m_answer = m_text.getText();
			if (m_answer.length() == 0) { return; }
			setReturnCode(TitleAreaDialog.OK);
			break;
		case IDialogConstants.CANCEL_ID:
			m_answer = null;
			setReturnCode(TitleAreaDialog.CANCEL);
			break;
		}
		close();
	}
}
