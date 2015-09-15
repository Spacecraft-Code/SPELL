///////////////////////////////////////////////////////////////////////////////
//
// PACKAGE   : com.astra.ses.spell.gui.views.controls.master
// 
// FILE      : RecoveryFilesContentProvider.java
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
package com.astra.ses.spell.gui.views.controls.master.recovery;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.Viewer;

import com.astra.ses.spell.gui.core.interfaces.IContextProxy;
import com.astra.ses.spell.gui.core.interfaces.IFileManager;
import com.astra.ses.spell.gui.core.interfaces.ServiceManager;
import com.astra.ses.spell.gui.core.model.server.ProcedureRecoveryInfo;
import com.astra.ses.spell.gui.core.model.types.Level;
import com.astra.ses.spell.gui.core.utils.Logger;
import com.astra.ses.spell.gui.procs.interfaces.IProcedureManager;

/**
 * @author Rafael Chinchilla
 *
 */
public class RecoveryFilesContentProvider implements IStructuredContentProvider
{
	private static IProcedureManager s_mgr = null;
	
	static
	{
		s_mgr = (IProcedureManager) ServiceManager.get(IProcedureManager.class);
	}
	
	private List<ProcedureRecoveryInfo> m_procs;
	
	/* (non-Javadoc)
     * @see org.eclipse.jface.viewers.IContentProvider#dispose()
     */
    @Override
    public void dispose()
    {
    }

	/* (non-Javadoc)
     * @see org.eclipse.jface.viewers.IContentProvider#inputChanged(org.eclipse.jface.viewers.Viewer, java.lang.Object, java.lang.Object)
     */
    @Override
    public void inputChanged(Viewer viewer, Object oldInput, Object newInput)
    {
    	if (newInput != null)
    	{
	    	IContextProxy proxy = (IContextProxy) newInput;
	    	refreshContents(proxy);
    	}
    }
    

	/* (non-Javadoc)
     * @see org.eclipse.jface.viewers.IStructuredContentProvider#getElements(java.lang.Object)
     */
    @Override
    public Object[] getElements(Object inputElement)
    {
    	List<ProcedureRecoveryInfo> procs = new ArrayList<ProcedureRecoveryInfo>();
    	for(ProcedureRecoveryInfo proc : m_procs)
    	{
    		procs.add(proc);
    	}
	    return procs.toArray();
    }

    private void refreshContents( IContextProxy proxy )
    {
		if (m_procs == null)
		{
			m_procs = new ArrayList<ProcedureRecoveryInfo>();
		}
		else
		{
			m_procs.clear();
		}
    	if (proxy.isConnected())
    	{
    		IFileManager fileMgr = (IFileManager) ServiceManager.get(IFileManager.class);
		    List<String> files = fileMgr.getRecoveryFileList();
		    Logger.debug("Processing list of available recovery files", Level.PROC, this);
		    for(String file : files)
		    {
			    Logger.debug("   - '" + file + "'", Level.PROC, this);
		    	if (file == null || file.trim().isEmpty())
		    	{
		    		continue;
		    	}
		    	else if (file.length()>17)
		    	{
			    	ProcedureRecoveryInfo proc = new ProcedureRecoveryInfo( file );
			    	// Obtain the corresponding name
			    	proc.setName(s_mgr.getProcedureName(proc.getProcId()));
			    	m_procs.add(proc);
		    	}
		    	else
		    	{
		    		Logger.error("Cannot process recovery file: '" + file + "'", Level.PROC, this);
		    	}
		    }
    	}
    }
}
