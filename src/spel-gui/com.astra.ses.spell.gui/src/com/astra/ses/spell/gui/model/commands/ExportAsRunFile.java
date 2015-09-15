///////////////////////////////////////////////////////////////////////////////
//
// PACKAGE   : com.astra.ses.spell.gui.model.commands
// 
// FILE      : ExportAsRunFile.java
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
package com.astra.ses.spell.gui.model.commands;

import java.util.LinkedList;
import java.util.List;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.DirectoryDialog;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;

import com.astra.ses.spell.gui.core.interfaces.ServiceManager;
import com.astra.ses.spell.gui.core.model.files.AsRunFile;
import com.astra.ses.spell.gui.core.model.types.Level;
import com.astra.ses.spell.gui.core.utils.Logger;
import com.astra.ses.spell.gui.model.commands.helpers.CommandHelper;
import com.astra.ses.spell.gui.model.jobs.ExportAsRunFileJob;
import com.astra.ses.spell.gui.model.jobs.SaveAsRunFileJob;
import com.astra.ses.spell.gui.procs.interfaces.IProcedureManager;
import com.astra.ses.spell.gui.procs.interfaces.model.IProcedure;
import com.astra.ses.spell.gui.services.IRuntimeSettings;
import com.astra.ses.spell.gui.services.IRuntimeSettings.RuntimeProperty;

public class ExportAsRunFile extends AbstractHandler
{
	public static final String	ID	= "com.astra.ses.spell.gui.commands.ExportAsRunFile";

	/***************************************************************************
	 * The constructor.
	 **************************************************************************/
	public ExportAsRunFile()
	{
	}

	/***************************************************************************
	 * The command has been executed, so extract extract the needed information
	 * from the application context.
	 **************************************************************************/
	public CommandResult execute(ExecutionEvent event)
	        throws ExecutionException
	{
		IWorkbenchWindow window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
		IRuntimeSettings runtime = (IRuntimeSettings) ServiceManager.get(IRuntimeSettings.class);
		String instanceId = (String) runtime.getRuntimeProperty(RuntimeProperty.ID_PROCEDURE_SELECTION);

		try
		{
			IProcedureManager mgr = (IProcedureManager) ServiceManager.get(IProcedureManager.class);
			IProcedure proc = null;
			if (mgr.isLocallyLoaded(instanceId))
			{
				proc = mgr.getProcedure(instanceId);
			}
			else
			{
				proc = mgr.getRemoteProcedure(instanceId);
			}
			
			List<AsRunFile> toExport = new LinkedList<AsRunFile>();
			
			ExportAsRunFileJob job = new ExportAsRunFileJob( proc );
			CommandHelper.executeInProgress(job, true, true);

			if (job.result.equals(CommandResult.SUCCESS))
			{
				toExport.add(job.asrunFile);
				if (!job.asrunFile.getChildren().isEmpty())
				{
					boolean alsoChildren = MessageDialog.openQuestion(window.getShell(), "Export children ASRUN files", "This procedure has executed sub-procedures.\n\nDo you want to export these ASRUN files as well?");
					if (alsoChildren)
					{
						gatherChildAsRunFiles(job.asrunFile,toExport);
					}
				}
			}
			
			DirectoryDialog dialog = new DirectoryDialog(window.getShell(), SWT.SAVE );
			dialog.setMessage("Select directory to export ASRUN file(s) for '" + proc.getProcName() + "'");
			dialog.setText("Export ASRUN");
			String destination = dialog.open();
			if (destination != null && !destination.isEmpty())
			{
				SaveAsRunFileJob saveJob = new SaveAsRunFileJob(destination, toExport);
				CommandHelper.executeInProgress(saveJob, true, true);
				return saveJob.result;
			}
			else
			{
				return CommandResult.NO_EFFECT;
			}
		}
		catch(Exception ex)
		{
			ex.printStackTrace();
			return CommandResult.FAILED;
		}
	}
	
	/***************************************************************************
	 * Iterate over all ASRUN files for children
	 **************************************************************************/
	private void gatherChildAsRunFiles( AsRunFile parent, List<AsRunFile> list )
	{
		Logger.debug("Fetching child ASRUN files for " + parent.getProcId(), Level.PROC, this);
		for(String childFile : parent.getChildren())
		{
			Logger.debug("   - Get child ASRUN file: " + childFile, Level.PROC, this);
			ExportAsRunFileJob childJob = new ExportAsRunFileJob( childFile );
			CommandHelper.executeInProgress(childJob, true, true);
			if (childJob.result.equals(CommandResult.SUCCESS))
			{
				list.add(childJob.asrunFile);
				Logger.debug("   - ASRUN file has children: " + childJob.asrunFile.getChildren().size(), Level.PROC, this);
				if (!childJob.asrunFile.getChildren().isEmpty())
				{
					gatherChildAsRunFiles(childJob.asrunFile, list);
				}
			}
		}
	}
}
