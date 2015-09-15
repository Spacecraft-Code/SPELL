///////////////////////////////////////////////////////////////////////////////
//
// PACKAGE   : com.astra.ses.spell.gui.views.controls
// 
// FILE      : SplitPanel.java
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
package com.astra.ses.spell.gui.views.controls.generic;

import java.util.HashSet;
import java.util.Set;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.events.ControlListener;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Sash;

import com.astra.ses.spell.gui.interfaces.IPresentationPanel;
import com.astra.ses.spell.gui.interfaces.ISashListener;

public class SplitPanel extends Composite implements Listener, ControlListener
{
	public static enum Section
	{
		PRESENTATION, CONTROL_AREA, BOTH, NONE
	};
	
	/** First section composite */
	private Composite m_presentationSection;
	/** Second section composite */
	private Composite m_controlSection;
	/** Divider */
	private Sash m_sash;
	/** Layout data for the divider */
	private FormData m_sashData;
	/** Minimum size for sections */
	private int m_sizeLimit;
	/** For initial division calculation */
	private boolean m_initialSize;
	private IPresentationPanel m_presentationPanel;
	/** Sash listeners */
	private Set<ISashListener> m_listeners;

	/***************************************************************************
	 * Constructor
	 * 
	 * @param parent
	 *            Container composite
	 * @param horizontalSplit
	 *            True if the split bar is to be horizontal
	 * @param limit
	 *            Size limit for sections
	 * @param initial
	 *            Initial percentage of the split bar position
	 **************************************************************************/
	public SplitPanel(Composite parent, int limit, IPresentationPanel ppanel )
	{
		super(parent, SWT.NONE);

		m_presentationPanel = ppanel;
		m_sizeLimit = limit;
		m_initialSize = true;
		m_listeners = new HashSet<ISashListener>();

		FormLayout layout = new FormLayout();
		layout.marginWidth = 0;
		layout.marginHeight = 0;
		setLayout(layout);
		createSections();
		defineSectionsLayout();

		m_presentationSection.setSize(m_presentationSection.computeSize(SWT.DEFAULT, SWT.DEFAULT));
		m_controlSection.setSize(m_controlSection.computeSize(SWT.DEFAULT, SWT.DEFAULT));
		
		addControlListener(this);
	}

	/***************************************************************************
	 * Create the main parts of the control
	 **************************************************************************/
	private void createSections()
	{
		m_presentationSection = new Composite(this, SWT.NONE);
		m_presentationSection.setLayoutData(new GridData(GridData.FILL_BOTH));

		m_sash = new Sash(this, SWT.BORDER | SWT.HORIZONTAL );

		m_controlSection = new Composite(this, SWT.BORDER);
		m_controlSection.setLayoutData(new GridData(GridData.FILL_BOTH));

	}

	/***************************************************************************
	 * Define the split layout
	 **************************************************************************/
	private void defineSectionsLayout()
	{
		FormData section1_data = new FormData();
		section1_data.left = new FormAttachment(0, 0);
		section1_data.right = new FormAttachment(100, 0);
		section1_data.top = new FormAttachment(0, 0);
		section1_data.bottom = new FormAttachment(m_sash, 0);
		m_presentationSection.setLayoutData(section1_data);

		FormData section2_data = new FormData();
		section2_data.left = new FormAttachment(0, 0);
		section2_data.right = new FormAttachment(100, 0);
		section2_data.top = new FormAttachment(m_sash, 0);
		section2_data.bottom = new FormAttachment(100, 0);
		m_controlSection.setLayoutData(section2_data);

		m_sashData = new FormData();
		m_sashData.top = new FormAttachment(0, 500);
		m_sashData.left = new FormAttachment(0, 0);
		m_sashData.right = new FormAttachment(100, 0);
		m_sash.setLayoutData(m_sashData);

		m_sash.addListener(SWT.Selection, this);
	}

	/***************************************************************************
	 * Add listener to sash move events
	 **************************************************************************/
	public void addSashListener( ISashListener listener )
	{
		if (!m_listeners.contains(listener))
		{
			m_listeners.add(listener);
		}
	}
	
	/***************************************************************************
	 * Remove listener to sash move events
	 **************************************************************************/
	public void removeSashListener( ISashListener listener )
	{
		if (m_listeners.contains(listener))
		{
			m_listeners.remove(listener);
		}
	}
	
	/***************************************************************************
	 * Handle sash events, takes place when user drags the sash
	 **************************************************************************/
	public void handleEvent(Event e)
	{
		Rectangle sashRect = m_sash.getBounds();
		Rectangle shellRect = getClientArea();
		int height = shellRect.height - sashRect.height - m_sizeLimit;
		e.y = Math.max(Math.min(e.y, height), m_sizeLimit);
		if (e.y != sashRect.y)
		{
			m_sashData.top = new FormAttachment(0, e.y);
			layout();
		}
		for(ISashListener listener : m_listeners)
		{
			listener.onSashMoved( shellRect.height - e.y );
		}
	}

	/***************************************************************************
	 * Handle resize events of the dictionary
	 **************************************************************************/
	@Override
	public void controlResized(ControlEvent e)
	{
		computeSize();
	}

	/***************************************************************************
	 * Compute the position of the sash upon dictionary changes
	 **************************************************************************/
	public void computeSize()
	{
		if (m_initialSize)
		{
			Point ssSize = getSection(SplitPanel.Section.CONTROL_AREA).computeSize(SWT.DEFAULT, SWT.DEFAULT);
			int topHeight = getParent().getClientArea().height;
			int offset = topHeight - m_presentationPanel.getControl().getClientArea().height - ssSize.y;
			if (offset < 200) offset = 200;
			setDivision(offset);
			m_initialSize = false;
		}
		else
		{
			Rectangle sashRect = m_sash.getBounds();
			Rectangle panelRect = getClientArea();
			m_controlSection.computeSize(SWT.DEFAULT, SWT.DEFAULT);
			int offset = panelRect.height - sashRect.height - m_controlSection.getBounds().height;
			setDivision(offset);
		}
	}
	
	/***************************************************************************
	 * Handle move events
	 **************************************************************************/
	@Override
	public void controlMoved(ControlEvent e)
	{
	}

	/***************************************************************************
	 * Obtain the first section handle
	 * 
	 * @return The first section base composite
	 **************************************************************************/
	public Composite getSection(Section s)
	{
		if (s == Section.PRESENTATION)
		{
			return m_presentationSection;
		}
		else if (s == Section.CONTROL_AREA)
		{
			return m_controlSection;
		}
		else
		{
			return null;
		}
	}

	/***************************************************************************
	 * Manually set the division position, in percentage
	 **************************************************************************/
	public void setDivision(int offset)
	{
		m_sashData.top = new FormAttachment(0, offset);
		layout();
		Rectangle shellRect = getClientArea();
		for(ISashListener listener : m_listeners)
		{
			listener.onSashMoved( shellRect.height - offset );
		}
	}

	/***************************************************************************
	 * Dispose the control
	 **************************************************************************/
	public void dispose()
	{
		for (Control c : m_presentationSection.getChildren())
			c.dispose();
		for (Control c : m_controlSection.getChildren())
			c.dispose();
		m_presentationSection.dispose();
		m_controlSection.dispose();
		m_sash.dispose();
		super.dispose();
	}
}
