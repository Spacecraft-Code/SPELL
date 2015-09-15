///////////////////////////////////////////////////////////////////////////////
//
// PACKAGE   : com.astra.ses.spell.gui.model.jobs
// 
// FILE      : ExportLogFileJob.java
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
package com.astra.ses.spell.gui.model.jobs;

import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.lang.reflect.InvocationTargetException;
import java.util.List;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.operation.IRunnableWithProgress;

import com.astra.ses.spell.gui.core.interfaces.IFileManager;
import com.astra.ses.spell.gui.core.interfaces.ServiceManager;
import com.astra.ses.spell.gui.core.model.files.IServerFileLine;
import com.astra.ses.spell.gui.core.model.files.LogFile;
import com.astra.ses.spell.gui.core.model.types.Level;
import com.astra.ses.spell.gui.core.model.types.ServerFileType;
import com.astra.ses.spell.gui.core.utils.Logger;
import com.astra.ses.spell.gui.model.commands.CommandResult;
import com.astra.ses.spell.gui.procs.interfaces.model.IProcedure;

public class ExportLogFileJob implements IRunnableWithProgress
{
	public CommandResult result;
	public LogFile	     logFile;
	private IProcedure   m_proc;
	private String       m_destinationFileName;

	public ExportLogFileJob( IProcedure proc, String destinationFile)
	{
		logFile = null;
		m_proc = proc;
		m_destinationFileName = destinationFile;
	}

	@Override
	public void run(IProgressMonitor monitor) throws InvocationTargetException,
	        InterruptedException
	{
		try
		{
			monitor.setTaskName("Retrieving LOG file for procedure " + m_proc.getProcName());
			
			IFileManager fileMgr = (IFileManager) ServiceManager.get(IFileManager.class);
			String path = fileMgr.getServerFilePath(m_proc.getProcId(), ServerFileType.EXECUTOR_LOG, monitor);
			Logger.debug("LOG file path: '" + path + "'", Level.PROC, this);

			logFile = (LogFile) fileMgr.getServerFile( path, ServerFileType.EXECUTOR_LOG, null, monitor);
			
			List<IServerFileLine> lines = logFile.getLines();
			
			monitor.beginTask("Exporting log data", lines.size());
			
			PrintWriter writer = new PrintWriter( new OutputStreamWriter( new FileOutputStream(m_destinationFileName)));
			for(IServerFileLine line : lines)
			{
				writer.println( line.toString() );
				monitor.worked(1);
				if (monitor.isCanceled()) break;
			}
			writer.close();
			monitor.done();
			result = CommandResult.SUCCESS;
		}
		catch (Exception e)
		{
			Logger.error("Could retrieve LOG:" + e.getLocalizedMessage(),Level.PROC, this);
		}
		monitor.done();
	}

}
