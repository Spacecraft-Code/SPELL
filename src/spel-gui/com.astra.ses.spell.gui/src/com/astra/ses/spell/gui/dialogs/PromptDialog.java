///////////////////////////////////////////////////////////////////////////////
//
// PACKAGE   : com.astra.ses.spell.gui.dialogs
// 
// FILE      : PromptDialog.java
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
 * @brief Dialog for prompting user for input
 * @date 18/09/07
 ******************************************************************************/
public class PromptDialog extends TitleAreaDialog
{
	// =========================================================================
	// # STATIC DATA MEMBERS
	// =========================================================================

	// PRIVATE -----------------------------------------------------------------
	// PROTECTED ---------------------------------------------------------------
	// PUBLIC ------------------------------------------------------------------
	public static final int	OPT_OK	      = 1;
	public static final int	OPT_CANCEL	  = 2;
	public static final int	OPT_YES	      = 4;
	public static final int	OPT_NO	      = 8;
	public static final int	OPT_NUM	      = 16;
	public static final int	OPT_ALPHA	  = 32;
	public static final int	OPT_ANY	      = 48;
	public static final int	OPT_YES_NO	  = 96;
	public static final int	OPT_OK_CANCEL	= 192;

	// =========================================================================
	// # INSTANCE DATA MEMBERS
	// =========================================================================

	// PRIVATE -----------------------------------------------------------------
	/** Holds the dialog image icon */
	private Image	        m_image;
	/** Holds the procedure id */
	private String	        m_procId;
	/** Holds the prompt message */
	private String	        m_msg;
	/** Holds the prompt type */
	private int	            m_type;
	/** Holds the text input */
	private Text	        m_input;
	/** Future */
	private Future	        m_future;

	// PROTECTED ---------------------------------------------------------------
	// PUBLIC ------------------------------------------------------------------

	// =========================================================================
	// # ACCESSIBLE METHODS
	// =========================================================================

	/***************************************************************************
	 * Constructor
	 * 
	 * @param shell
	 *            The parent shell
	 **************************************************************************/
	public PromptDialog(Shell shell, String procId, String msg, int opt,
	        String[] expected, Future future)
	{
		super(shell);
		// Obtain the image for the dialog icon
		ImageDescriptor descr = Activator
		        .getImageDescriptor("icons/dlg_question.png");
		m_image = descr.createImage();
		m_procId = procId;
		m_msg = msg;
		m_type = opt;
		m_future = future;
	}

	/***************************************************************************
	 * Called when the dialog is about to close.
	 * 
	 * @return The superclass return value.
	 **************************************************************************/
	public boolean close()
	{
		m_image.dispose();
		return super.close();
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
		setMessage("Procedure " + m_procId + " requires user input");
		setTitle(m_procId + " user input");
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
		layout.marginHeight = 10;
		layout.marginWidth = 10;
		layout.marginTop = 10;
		layout.marginBottom = 10;
		layout.marginLeft = 10;
		layout.marginRight = 10;
		layout.numColumns = 1;
		top.setLayout(layout);

		Label label = new Label(top, SWT.NONE);
		label.setText(m_msg);

		createInputControls(top);

		return parent;
	}

	/***************************************************************************
	 * Create the set of input controls depending on the dialog option
	 **************************************************************************/
	protected void createInputControls(Composite parent)
	{
		if (m_type <= OPT_NO || m_type == OPT_YES_NO)
		{
			return;
		}
		else
		{
			m_input = new Text(parent, SWT.BORDER);
			m_input.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		}
	}

	/***************************************************************************
	 * Create the button bar buttons.
	 * 
	 * @param parent
	 *            The Button Bar.
	 **************************************************************************/
	protected void createButtonsForButtonBar(Composite parent)
	{
		if (m_type == OPT_OK) createButton(parent, IDialogConstants.OK_ID,
		        IDialogConstants.OK_LABEL, true);
		else if (m_type == OPT_YES) createButton(parent,
		        IDialogConstants.YES_ID, IDialogConstants.YES_LABEL, true);
		else if (m_type == OPT_NO) createButton(parent, IDialogConstants.NO_ID,
		        IDialogConstants.NO_LABEL, false);
		else if (m_type == OPT_CANCEL) createButton(parent,
		        IDialogConstants.CANCEL_ID, IDialogConstants.CANCEL_LABEL,
		        false);
		else if (m_type == OPT_NUM || m_type == OPT_ALPHA || m_type == OPT_ANY)
		{
			createButton(parent, IDialogConstants.OK_ID,
			        IDialogConstants.OK_LABEL, true);
			createButton(parent, IDialogConstants.CANCEL_ID,
			        IDialogConstants.CANCEL_LABEL, false);
		}
		else if (m_type == OPT_YES_NO)
		{
			createButton(parent, IDialogConstants.YES_ID,
			        IDialogConstants.YES_LABEL, true);
			createButton(parent, IDialogConstants.NO_ID,
			        IDialogConstants.NO_LABEL, false);
		}
		else if (m_type == OPT_OK_CANCEL)
		{
			createButton(parent, IDialogConstants.OK_ID,
			        IDialogConstants.OK_LABEL, true);
			createButton(parent, IDialogConstants.CANCEL_ID,
			        IDialogConstants.CANCEL_LABEL, false);
		}
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
		case IDialogConstants.CANCEL_ID:
		case IDialogConstants.YES_ID:
		case IDialogConstants.NO_ID:
			if (m_input == null)
			{
				if (buttonId == IDialogConstants.OK_ID) m_future.set("TRUE");
				if (buttonId == IDialogConstants.YES_ID) m_future.set("TRUE");
				if (buttonId == IDialogConstants.CANCEL_ID) m_future
				        .set("FALSE");
				if (buttonId == IDialogConstants.NO_ID) m_future.set("FALSE");
			}
			else
			{
				String txt = m_input.getText();
				m_future.set(txt);
			}
		}
		close();
	}
}
