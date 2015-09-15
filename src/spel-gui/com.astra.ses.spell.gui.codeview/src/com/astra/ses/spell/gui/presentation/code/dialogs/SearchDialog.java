///////////////////////////////////////////////////////////////////////////////
//
// PACKAGE   : com.astra.ses.spell.gui.presentation.code.dialogs
// 
// FILE      : SearchDialog.java
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
package com.astra.ses.spell.gui.presentation.code.dialogs;

import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

import com.astra.ses.spell.gui.presentation.code.Activator;
import com.astra.ses.spell.gui.presentation.code.controls.CodeViewer;

/*******************************************************************************
 * @brief Dialog for searching in code
 * @date 18/09/07
 ******************************************************************************/
public class SearchDialog extends TitleAreaDialog
{
	// =========================================================================
	// # STATIC DATA MEMBERS
	// =========================================================================
	// PRIVATE -----------------------------------------------------------------
	private static final String	BTN_FIND_TITLE	      = "Find";
	private static final String	BTN_FIND_NEXT_TITLE	  = "Find next";
	private static final String	BTN_FIND_PREV_TITLE	  = "Find previous";
	private static final String	BTN_CLEAR_CLOSE_TITLE	= "Clear search & Close";
	private static final int	BTN_FIND_CODE	      = 122;
	private static final int	BTN_FIND_NEXT_CODE	  = 123;
	private static final int	BTN_FIND_PREV_CODE	  = 124;
	private static final int	BTN_CLEAR_CLOSE_CODE	= 125;
	// PROTECTED ---------------------------------------------------------------
	// PUBLIC ------------------------------------------------------------------

	// =========================================================================
	// # INSTANCE DATA MEMBERS
	// =========================================================================

	// PRIVATE -----------------------------------------------------------------
	/** Holds the dialog image icon */
	private Image	            m_image;
	/** Holds the search text control */
	private Text	            m_text;
	/** Holds the code viewer reference */
	private CodeViewer	        m_viewer;
	/** Holds the latest number of occurrences */
	private static int	        m_occurrences	      = 0;
	/** Holds the latest search text */
	private static String	    m_lastSearch	      = null;

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
	public SearchDialog(Shell shell, CodeViewer viewer)
	{
		super(shell);
		setShellStyle(SWT.CLOSE);
		// Obtain the image for the dialog icon
		ImageDescriptor descr = Activator
		        .getImageDescriptor("icons/dlg_detail.png");
		m_image = descr.createImage();
		m_viewer = viewer;
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
		setTitle("Search text in procedure");

		if (!m_viewer.hasMatches())
		{
			m_occurrences = 0;
			m_lastSearch = null;
		}

		if (m_occurrences == 0)
		{
			setMessage("Type in the text to search");
		}
		else
		{
			setMessage(m_occurrences + " occurrences found.");
		}
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
		GridData areaData = new GridData(GridData.FILL_BOTH);
		areaData.widthHint = 300;
		top.setLayoutData(areaData);
		GridLayout layout = new GridLayout();
		layout.marginHeight = 5;
		layout.marginWidth = 5;
		layout.marginTop = 5;
		layout.marginBottom = 5;
		layout.marginLeft = 5;
		layout.marginRight = 5;
		layout.numColumns = 1;
		top.setLayout(layout);

		m_text = new Text(top, SWT.SINGLE | SWT.BORDER);
		m_text.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		if (m_lastSearch != null)
		{
			m_text.setText(m_lastSearch);
		}

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
		createButton(parent, BTN_FIND_CODE, BTN_FIND_TITLE, true);
		createButton(parent, BTN_FIND_PREV_CODE, BTN_FIND_PREV_TITLE, false);
		createButton(parent, BTN_FIND_NEXT_CODE, BTN_FIND_NEXT_TITLE, false);
		createButton(parent, BTN_CLEAR_CLOSE_CODE, BTN_CLEAR_CLOSE_TITLE, false);
		createButton(parent, IDialogConstants.CLOSE_ID, IDialogConstants.CLOSE_LABEL, false);
		getButton(BTN_FIND_NEXT_CODE).setEnabled(false);
		getButton(BTN_FIND_PREV_CODE).setEnabled(false);
		getButton(BTN_CLEAR_CLOSE_CODE).setEnabled(false);
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
		case BTN_FIND_CODE:
			String toSearch = m_text.getText();
			if (toSearch.isEmpty())
			{
				MessageDialog.openError(getShell(), "Cannot search",
				        "Musty provide a search string");
			}
			else
			{
				m_occurrences = m_viewer.searchString(toSearch);
				setMessage(m_occurrences + " occurrences found.");
				getButton(BTN_FIND_NEXT_CODE).setEnabled( (m_occurrences>0) );
				getButton(BTN_FIND_PREV_CODE).setEnabled( (m_occurrences>0) );
				getButton(BTN_CLEAR_CLOSE_CODE).setEnabled( (m_occurrences>0) );
				m_lastSearch = toSearch;
			}
			break;
		case BTN_FIND_NEXT_CODE:
			if (!m_viewer.searchNext())
			{
				setMessage("No more occurrences");
			}
			else
			{
				setMessage(m_occurrences + " occurrences found.");
			}
			break;
		case BTN_FIND_PREV_CODE:
			if (!m_viewer.searchPrevious())
			{
				setMessage("At first occurrence");
			}
			else
			{
				setMessage(m_occurrences + " occurrences found.");
			}
			break;
		case BTN_CLEAR_CLOSE_CODE:
			m_viewer.clearMatches();
			// Fall thru
		case IDialogConstants.CLOSE_ID:
			close();
			break;
		}
	}
}
