///////////////////////////////////////////////////////////////////////////////
//
// PACKAGE   : com.astra.ses.spell.gui.model.jobs
// 
// FILE      : ExportAsRunFileJob.java
//
// DATE      : Sep 4, 2013
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
package com.astra.ses.spell.gui.model.jobs;

import java.lang.reflect.InvocationTargetException;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.operation.IRunnableWithProgress;

import com.astra.ses.spell.gui.core.interfaces.IFileManager;
import com.astra.ses.spell.gui.core.interfaces.ServiceManager;
import com.astra.ses.spell.gui.core.model.files.AsRunFile;
import com.astra.ses.spell.gui.core.model.types.Level;
import com.astra.ses.spell.gui.core.model.types.ServerFileType;
import com.astra.ses.spell.gui.core.utils.Logger;
import com.astra.ses.spell.gui.model.commands.CommandResult;
import com.astra.ses.spell.gui.procs.interfaces.model.IProcedure;

public class ExportAsRunFileJob implements IRunnableWithProgress
{
	public CommandResult result;
	public AsRunFile     asrunFile;
	private IProcedure   m_proc;
	private String       m_asrunFileName;

	public ExportAsRunFileJob( IProcedure proc )
	{
		asrunFile = null;
		m_proc = proc;
		m_asrunFileName = null;
	}

	public ExportAsRunFileJob( String asRunFileName )
	{
		asrunFile = null;
		m_proc = null;
		m_asrunFileName = asRunFileName;
	}

	@Override
	public void run(IProgressMonitor monitor) throws InvocationTargetException,
	        InterruptedException
	{
		try
		{
			IFileManager fileMgr = (IFileManager) ServiceManager.get(IFileManager.class);
			
			if (m_proc == null)
			{
				monitor.setTaskName("Retrieving ASRUN file '" + m_asrunFileName + "'");
				asrunFile = (AsRunFile) fileMgr.getServerFile( m_asrunFileName, ServerFileType.ASRUN, null, monitor);
				result = CommandResult.SUCCESS;
			}
			else
			{
				monitor.setTaskName("Retrieving ASRUN file for procedure " + m_proc.getProcName());
				String path = fileMgr.getServerFilePath(m_proc.getProcId(), ServerFileType.ASRUN, monitor);
				Logger.debug("ASRUN file path: '" + path + "'", Level.PROC, this);
				asrunFile = (AsRunFile) fileMgr.getServerFile( path, ServerFileType.ASRUN, null, monitor);
				result = CommandResult.SUCCESS;
			}
		}
		catch (Exception e)
		{
			Logger.error("Could not retrieve ASRUN:" + e.getLocalizedMessage(),Level.PROC, this);
			result = CommandResult.FAILED;
		}
		monitor.done();
	}

}
