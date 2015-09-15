///////////////////////////////////////////////////////////////////////////////
//
// PACKAGE   : com.astra.ses.spell.gui.views.presentations
// 
// FILE      : PresentationStack.java
//
// DATE      : 2008-11-24 08:34
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
package com.astra.ses.spell.gui.views.presentations;

import java.util.Vector;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StackLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;

import com.astra.ses.spell.gui.interfaces.IProcedurePresentation;
import com.astra.ses.spell.gui.procs.interfaces.model.IProcedure;

/*******************************************************************************
 * @brief Presentations stack composite wrapper
 * @date 09/10/07
 ******************************************************************************/
public class PresentationStack
{
	/** Holds the view identifier */
	public static final String	ID	= "com.astra.ses.spell.gui.views.models.PresentationStack";

	/** Stacked composite for holding the pages */
	private Composite	       m_stack;
	/** Layout for the stack */
	private StackLayout	       m_slayout;
	/** Holds the presentation pages */
	private Vector<Composite>	m_presentationPages;
	/** Reference to the procedure model */
	private IProcedure	       m_model;

	// PROTECTED ---------------------------------------------------------------
	// PUBLIC ------------------------------------------------------------------

	// =========================================================================
	// ACCESSIBLE METHODS
	// =========================================================================

	/***************************************************************************
	 * Constructor.
	 **************************************************************************/
	public PresentationStack(IProcedure model, Composite parent)
	{
		m_model = model;
		// Create the stack control for presentations
		m_stack = new Composite(parent, SWT.NONE);

		m_stack.setLayoutData(new GridData(GridData.FILL_BOTH));
		m_slayout = new StackLayout();
		m_slayout.marginHeight = 0;
		m_slayout.marginWidth = 0;
		m_stack.setLayout(m_slayout);
		m_presentationPages = new Vector<Composite>();
	}

	/***************************************************************************
	 * Add a presentation to the stack. Allows the presentation to create the
	 * graphical ui.
	 **************************************************************************/
	public void addPresentation(IProcedurePresentation presentation)
	{
		Composite c = presentation.createContents(m_model, m_stack);
		m_presentationPages.add(c);
	}

	/***************************************************************************
	 * Show the desired page: code, log or display.
	 **************************************************************************/
	public void showPresentation(int index)
	{
		m_slayout.topControl = m_presentationPages.get(index);
		// Refresh the stack
		m_stack.layout();
	}
}
