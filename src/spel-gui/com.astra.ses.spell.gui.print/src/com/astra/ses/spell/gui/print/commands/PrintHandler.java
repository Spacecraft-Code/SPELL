///////////////////////////////////////////////////////////////////////////////
//
// PACKAGE   : com.astra.ses.spell.gui.print.commands
// 
// FILE      : PrintHandler.java
//
// DATE      : Oct 29, 2013
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
package com.astra.ses.spell.gui.print.commands;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.printing.PrintDialog;
import org.eclipse.swt.printing.PrinterData;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PlatformUI;

import com.astra.ses.spell.gui.core.interfaces.ServiceManager;
import com.astra.ses.spell.gui.model.commands.CommandResult;
import com.astra.ses.spell.gui.model.commands.helpers.CommandHelper;
import com.astra.ses.spell.gui.model.jobs.GetAsRunFileJob;
import com.astra.ses.spell.gui.model.jobs.GetLogFileJob;
import com.astra.ses.spell.gui.print.PrintMode;
import com.astra.ses.spell.gui.procs.interfaces.IProcedureManager;
import com.astra.ses.spell.gui.procs.interfaces.model.IProcedure;
import com.astra.ses.spell.gui.services.IRuntimeSettings;
import com.astra.ses.spell.gui.services.IRuntimeSettings.RuntimeProperty;

/*******************************************************************************
 * ActiveProcedurePrintable handler will provide an IPrintable object from the
 * active procedure. The kind of printable returned depends on the mode provided
 ******************************************************************************/
public class PrintHandler extends AbstractHandler
{
	private static final String ARG_MODE = "com.astra.ses.spell.gui.commands.print.mode";
	
	/************************************************************************************
	 * 
	 ***********************************************************************************/
	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException
	{
		Shell shell = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell();
		try
		{
			PrintMode mode = PrintMode.valueOf(event.getParameter(ARG_MODE));

			IProcedureManager mgr = (IProcedureManager) ServiceManager.get(IProcedureManager.class);
			
			IRuntimeSettings runtime = (IRuntimeSettings) ServiceManager.get(IRuntimeSettings.class);
			String instanceId = (String) runtime.getRuntimeProperty(RuntimeProperty.ID_PROCEDURE_SELECTION);
			if (instanceId != null)
			{
				IProcedure proc = mgr.getProcedure(instanceId);
				if (proc != null)
				{
					PrintDialog dialog = new PrintDialog(shell);
					dialog.setText("Print active procedure");
					PrinterData initialData = new PrinterData();
					initialData.orientation = PrinterData.LANDSCAPE;
					dialog.setPrinterData(initialData);
					
					PrinterData printerData = dialog.open();
					if (printerData != null)
					{
						if (mode.equals(PrintMode.ASRUN))
						{
							GetAsRunFileJob arjob = new GetAsRunFileJob(proc.getProcId());
							CommandHelper.executeInProgress(arjob, false, false);
							if (arjob.result.equals(CommandResult.SUCCESS))
							{
								PrintJob job = new PrintJob( printerData, proc, arjob.asRunFile, mode );
								CommandHelper.executeInProgress(job, false, false);
							}
							else
							{
								MessageDialog.openError(shell, "Print ASRUN", "Cannot print ASRUN file: failed to download");
							}
						}
						else if (mode.equals(PrintMode.LOG))
						{
							GetLogFileJob ljob = new GetLogFileJob(proc.getProcId());
							CommandHelper.executeInProgress(ljob, false, false);
							if (ljob.result.equals(CommandResult.SUCCESS))
							{
								PrintJob job = new PrintJob( printerData, proc, ljob.logFile, mode );
								CommandHelper.executeInProgress(job, false, false);
							}
							else
							{
								MessageDialog.openError(shell, "Print LOG", "Cannot print LOG file: failed to download");
							}
						}
						else
						{
							PrintJob job = new PrintJob( printerData, proc, mode );
							CommandHelper.executeInProgress(job, false, false);
						}
					}
				}
			}
		}
		catch(Exception ex)
		{
			ex.printStackTrace();
			MessageDialog.openError(shell, "Print procedure", "Failed to print procedure:\n\n" + ex.getLocalizedMessage());
		}
		
		return null;
	}
}
