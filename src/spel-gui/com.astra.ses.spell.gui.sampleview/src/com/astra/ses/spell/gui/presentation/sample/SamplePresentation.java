///////////////////////////////////////////////////////////////////////////////
//
// PACKAGE   : com.astra.ses.spell.gui.presentation.sample
// 
// FILE      : SamplePresentation.java
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
package com.astra.ses.spell.gui.presentation.sample;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;

import com.astra.ses.spell.gui.interfaces.IPresentationNotifier;
import com.astra.ses.spell.gui.interfaces.IProcedurePresentation;
import com.astra.ses.spell.gui.interfaces.ProcedurePresentationAdapter;
import com.astra.ses.spell.gui.procs.interfaces.model.IProcedure;

public class SamplePresentation extends ProcedurePresentationAdapter implements IProcedurePresentation
{
	/** Holds the presentation identifier */
	private static final String	ID	               = "com.astra.ses.spell.gui.presentation.Sample";
	/** Holds the presentation title */
	private static final String	PRESENTATION_TITLE	= "Sample";
	/** Holds the presentation description */
	private static final String	PRESENTATION_DESC	= "Sample procedure view";
	/** Holds the presentation icon */
	private static final String	PRESENTATION_ICON	= "icons/16x16/asterisk.png";
	/** Parent view */
	private IProcedure	        m_model;

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.astra.ses.spell.gui.interfaces.IProcedurePresentation#createContents
	 * (IProcedure,Composite)
	 */
	@Override
	public Composite createContents(IProcedure model, Composite stack)
	{
		m_model = model;

		Composite shellPage = new Composite(stack, SWT.NONE);
		GridLayout groupLayout = new GridLayout();
		groupLayout.marginHeight = 0;
		groupLayout.marginWidth = 0;
		groupLayout.marginBottom = 0;
		groupLayout.marginTop = 0;
		groupLayout.verticalSpacing = 0;
		groupLayout.numColumns = 1;
		shellPage.setLayout(groupLayout);
		GridData data = new GridData();
		data.grabExcessHorizontalSpace = true;
		data.grabExcessVerticalSpace = true;
		data.verticalAlignment = SWT.FILL;
		shellPage.setLayoutData(data);

		// TODO create the page contents on the shellPage composite

		return shellPage;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.astra.ses.spell.gui.interfaces.IProcedurePresentation#
	 * subscribeNotifications(IPresentationNotifier)
	 */
	@Override
	public void subscribeNotifications(IPresentationNotifier notifier)
	{
		// TODO subscribe to the interesting events in the notifier using
		// listener mechanism
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.astra.ses.spell.gui.interfaces.IProcedurePresentation#getExtensionId
	 * (boolean)
	 */
	@Override
	public String getExtensionId()
	{
		return ID;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.astra.ses.spell.gui.interfaces.IProcedurePresentation#getTitle()
	 */
	@Override
	public String getTitle()
	{
		return PRESENTATION_TITLE;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.astra.ses.spell.gui.interfaces.IProcedurePresentation#getIcon()
	 */
	@Override
	public Image getIcon()
	{
		return Activator.getImageDescriptor(PRESENTATION_ICON).createImage();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.astra.ses.spell.gui.interfaces.IProcedurePresentation#getDescription
	 * ()
	 */
	@Override
	public String getDescription()
	{
		return PRESENTATION_DESC;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.astra.ses.spell.gui.interfaces.IProcedurePresentation#zoom(boolean)
	 */
	@Override
	public void zoom(boolean zoomIn)
	{
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.astra.ses.spell.gui.interfaces.IProcedurePresentation#showLine(int)
	 */
	@Override
	public void showLine(int lineNo)
	{
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.astra.ses.spell.gui.interfaces.IProcedurePresentation#setAutoScroll
	 * (boolean)
	 */
	@Override
	public void setAutoScroll(boolean enabled)
	{
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.astra.ses.spell.gui.interfaces.IProcedurePresentation#setEnabled(
	 * boolean)
	 */
	@Override
	public void setEnabled(boolean enabled)
	{
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.astra.ses.spell.gui.interfaces.IProcedurePresentation#getAdapter(
	 * class)
	 */
	@Override
	public Object getAdapter(@SuppressWarnings("rawtypes") Class adapter)
	{
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.astra.ses.spell.gui.interfaces.IProcedurePresentation#setSelected
	 * (boolean)
	 */
	@Override
	public void setSelected(boolean selected)
	{
		// TODO Auto-generated method stub

	}
}
