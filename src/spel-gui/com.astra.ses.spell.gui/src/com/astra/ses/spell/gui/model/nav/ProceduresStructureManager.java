///////////////////////////////////////////////////////////////////////////////
//
// PACKAGE   : com.astra.ses.spell.gui.model.nav
// 
// FILE      : ProceduresStructureManager.java
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
package com.astra.ses.spell.gui.model.nav;

import java.io.File;
import java.util.Arrays;
import java.util.Collection;
import java.util.Map;

import com.astra.ses.spell.gui.core.interfaces.ServiceManager;
import com.astra.ses.spell.gui.core.interfaces.listeners.ICoreContextOperationListener;
import com.astra.ses.spell.gui.core.model.notification.ErrorData;
import com.astra.ses.spell.gui.core.model.server.ContextInfo;
import com.astra.ses.spell.gui.core.model.types.Level;
import com.astra.ses.spell.gui.core.utils.Logger;
import com.astra.ses.spell.gui.extensions.GuiNotifications;
import com.astra.ses.spell.gui.model.commands.helpers.CommandHelper;
import com.astra.ses.spell.gui.model.jobs.BuildProcListJob;
import com.astra.ses.spell.gui.model.nav.content.BaseProcedureSystemElement;
import com.astra.ses.spell.gui.model.nav.content.CategoryNode;
import com.astra.ses.spell.gui.model.nav.content.ProcedureNode;
import com.astra.ses.spell.gui.procs.interfaces.IProcedureManager;
import com.astra.ses.spell.gui.views.NavigationView;

/******************************************************************************
 * Procedures manager deals with categories and procedures
 *****************************************************************************/
public class ProceduresStructureManager implements ICoreContextOperationListener
{

	public static final String	ID	= "com.astra.ses.spell.gui.models.ProcedureList";

	/** Root Element */
	private CategoryNode	    m_root;
	/** Procedures IDs */
	private Map<String, String>	m_procedureIDs;
	/** ViewPart */
	private NavigationView	    m_view;
	/** Reference to procedure manager */
	private IProcedureManager   m_pMgr;

	/**************************************************************************
	 * Constructor
	 *************************************************************************/
	public ProceduresStructureManager()
	{
		m_root = new CategoryNode("Root");
		GuiNotifications.get().addListener(this, ICoreContextOperationListener.class);
		m_pMgr = (IProcedureManager) ServiceManager.get(IProcedureManager.class);
	}

	/***************************************************************************
	 * Assign the view
	 **************************************************************************/
	public void setView(NavigationView view)
	{
		m_view = view;
	}

	/**************************************************************************
	 * Get root element
	 * 
	 * @return
	 *************************************************************************/
	public Collection<BaseProcedureSystemElement> getRootElements()
	{
		return m_root.getChildren();
	}

	/**************************************************************************
	 * Create the tree structure according to the String collection
	 *************************************************************************/
	private void init()
	{
		m_root.clear();
		if (m_procedureIDs == null) { return; }
		// Insert each proc into the structure
		for (String procID : m_procedureIDs.keySet())
		{
			insertProcedure(procID);
		}
	}

	/**************************************************************************
	 * Insert a procedure into the tree structure
	 *************************************************************************/
	public void insertProcedure(String procID)
	{
		String[] splittedPath = new String[] { procID };
		if (procID.indexOf(File.separator) != -1)
		{
			splittedPath = procID.split("\\" + File.separator);
		}
		else if (procID.indexOf("/") != -1)
		{
			splittedPath = procID.split("/");
		}

		/* Categories */
		String[] categories = null;

		try
		{
			categories = Arrays.copyOfRange(splittedPath, 0,
			        splittedPath.length - 1);
		}
		catch (Exception e)
		{
			categories = new String[] {};
		}

		CategoryNode current = m_root;
		for (String element : categories)
		{
			// Search for a children with the given name
			BaseProcedureSystemElement child = current.getChild(element);
			// if it doesn't exist, create
			if (child == null)
			{
				child = new CategoryNode(element);
				current.addChildren(child);
				current = (CategoryNode) child;
			}
			else
			{
				// child shall be a category
				// If doesn't, procedures is not inserted
				if (child.isLeaf())
				{
					System.err.println(child.getName()
					        + " can't be explored (It is a Procedure)");
					return;
				}
				current = (CategoryNode) child;
			}
		}
		// Create the procedure
		ProcedureNode proc = new ProcedureNode(m_procedureIDs.get(procID),
		        procID);
		current.addChildren(proc);
	}

	/***************************************************************************
	 * Update the model with the given procedure ids. Obtain the procedure
	 * properties from the manager.
	 * 
	 * @param connectionLost
	 *            True if the connection has failed
	 **************************************************************************/
	public void update(boolean isConnected, boolean connectionLost, boolean refresh)
	{
		if (connectionLost || !isConnected)
		{
			// Clear the list of procedures
			if (m_procedureIDs != null)
			{
				m_procedureIDs.clear();
			}
		}
		else if (isConnected)
		{
			if (refresh)
			{
				Logger.debug("Updating procedure list", Level.PROC, this);
				BuildProcListJob job = new BuildProcListJob();
				CommandHelper.executeInProgress(job, true, false);
				m_procedureIDs = job.getLoadedProcedures();
			}
			else
			{
				m_procedureIDs = m_pMgr.getAvailableProcedures(false);
			}
		}
		init();
		if (m_view != null)
		{
			Logger.debug("Refreshing view: " + m_root.getChildren().size(), Level.GUI, this);
			m_view.refresh();
		}
	}

	// /////////////////////////////////////////////////////////////////////////
	// Context listener methods
	// /////////////////////////////////////////////////////////////////////////
	@Override
	public void notifyContextAttached(ContextInfo ctx)
	{
		Logger.debug("Updating model after context attached", Level.GUI, this);
		update(true,false,false);
	}

	@Override
	public void notifyContextDetached()
	{
		Logger.debug("Updating model after context detached", Level.GUI, this);
		update(false,false,false);
	}

	@Override
	public void notifyContextError(ErrorData error)
	{
		Logger.debug("Updating model after context error", Level.GUI, this);
		update(false,true,false);
	}

	@Override
	public String getListenerId()
	{
		return ID;
	}
}
