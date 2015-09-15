///////////////////////////////////////////////////////////////////////////////
//
// PACKAGE   : com.astra.ses.spell.gui.dialogs
// 
// FILE      : ControlInfoDialog.java
//
// DATE      : Jan 11, 2012
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

import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;

import com.astra.ses.spell.gui.Activator;
import com.astra.ses.spell.gui.core.interfaces.IProcedureClient;
import com.astra.ses.spell.gui.procs.interfaces.model.IProcedure;

/*******************************************************************************
 * @brief Dialog for procedure control information
 * @date 18/09/07
 ******************************************************************************/
public class ControlInfoDialog extends TitleAreaDialog
{
	// =========================================================================
	// # STATIC DATA MEMBERS
	// =========================================================================

	// PRIVATE -----------------------------------------------------------------

	// PROTECTED ---------------------------------------------------------------
	// PUBLIC ------------------------------------------------------------------
	public static final String	        ID	= "com.astra.ses.spell.gui.dialogs.ControlInfoDialog";

	// =========================================================================
	// # INSTANCE DATA MEMBERS
	// =========================================================================

	// PRIVATE -----------------------------------------------------------------
	/** Holds the dialog image icon */
	private Image	                    m_image;
	/** Holds the bold font */
	private Font 					    m_font;
	/** Holds the procedure model */
	private IProcedure					m_proc;

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
	public ControlInfoDialog(Shell shell, IProcedure proc)
	{
		super(shell);
		// Obtain the image for the dialog icon
		ImageDescriptor descr = Activator.getImageDescriptor("icons/dlg_exec.png");
		m_image = descr.createImage();
		m_font = JFaceResources.getBannerFont();
		m_proc = proc;
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
		setMessage("Control status of procedure '" + m_proc.getProcName() + "'");
		setTitle("Procedure control information");
		setTitleImage(m_image);
		getShell().setText("Control Information");
		return contents;
	}

	/***************************************************************************
	 * Create the dialog area contents.
	 * 
	 * @param parent
	 *            The base composite of the dialog
	 * @return The resulting contents
	 **************************************************************************/
	@Override
	protected Control createDialogArea(Composite parent)
	{
		// Main composite of the dialog area -----------------------------------
		Composite top = new Composite(parent, SWT.NONE);
		GridData areaData = new GridData(GridData.FILL_BOTH);
		areaData.widthHint = 400;
		top.setLayoutData(areaData);
		GridLayout layout = new GridLayout();
		layout.numColumns = 1;
		layout.verticalSpacing = 10;
		top.setLayout(layout);

		Label ctrlLabel = new Label( top, SWT.NONE );
		ctrlLabel.setFont(m_font);
		ctrlLabel.setText("Controlling client");

		Label sep1 = new Label( top, SWT.SEPARATOR | SWT.HORIZONTAL );
		sep1.setLayoutData( new GridData( GridData.FILL_HORIZONTAL ));

		Label ctrlClient = new Label( top, SWT.NONE );
		GridData cdata = new GridData();
		ctrlClient.setLayoutData(cdata);
		IProcedureClient cClient = m_proc.getRuntimeInformation().getControllingClient();
		if (cClient == null)
		{
			ctrlClient.setText( "(not controlled)" );
		}
		else
		{
			ctrlClient.setText( "Client '" + cClient.getKey() + "' at " + cClient.getHost());
		}
		

		Label monLabel = new Label( top, SWT.NONE );
		monLabel.setFont(m_font);
		monLabel.setText("Monitoring clients");

		Label sep2 = new Label( top, SWT.SEPARATOR | SWT.HORIZONTAL );
		sep2.setLayoutData( new GridData( GridData.FILL_HORIZONTAL ));

		IProcedureClient[] mclients = m_proc.getRuntimeInformation().getMonitoringClients();
		if ((mclients != null)&&(mclients.length>0))
		{
			for(IProcedureClient client : mclients)
			{
				Label label = new Label(top, SWT.NONE);
				label.setText( "Client '" + client.getKey() + "' at " + client.getHost());
			}
		}
		else
		{
			Label label = new Label(top, SWT.NONE);
			label.setText( "(not monitored)" );
		}
		
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
		createButton(parent, IDialogConstants.OK_ID, IDialogConstants.OK_LABEL,
		        true);
	}

	/***************************************************************************
	 * Called when one of the buttons of the button bar is pressed.
	 * 
	 * @param buttonId
	 *            The button identifier.
	 **************************************************************************/
	@Override
	protected void buttonPressed(int buttonId)
	{
		close();
	}
}
