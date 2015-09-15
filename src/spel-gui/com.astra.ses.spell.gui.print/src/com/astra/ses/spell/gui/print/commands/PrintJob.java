///////////////////////////////////////////////////////////////////////////////
//
// PACKAGE   : com.astra.ses.spell.gui.print.commands
// 
// FILE      : PrintJob.java
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

import java.lang.reflect.InvocationTargetException;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.printing.Printer;
import org.eclipse.swt.printing.PrinterData;

import com.astra.ses.spell.gui.core.model.files.AsRunFile;
import com.astra.ses.spell.gui.core.model.files.LogFile;
import com.astra.ses.spell.gui.print.PrintMode;
import com.astra.ses.spell.gui.print.printers.AsRunPrinter;
import com.astra.ses.spell.gui.print.printers.CodePrinter;
import com.astra.ses.spell.gui.print.printers.LogPrinter;
import com.astra.ses.spell.gui.print.printers.PageBasePrinter;
import com.astra.ses.spell.gui.print.printers.TextPrinter;
import com.astra.ses.spell.gui.procs.interfaces.model.IProcedure;

public class PrintJob implements IRunnableWithProgress
{
	private Printer m_printer;
	private PrinterData m_printerData;
	private IProcedure m_model;
	private AsRunFile m_asrun;
	private LogFile m_log;
	private PrintMode m_mode;
	private PrintingFonts m_fonts;
	private PageBasePrinter m_basePrinter;

	/************************************************************************************
	 * 
	 ***********************************************************************************/
	public PrintJob( PrinterData printerData, IProcedure model, PrintMode mode )
	{
		m_printerData = printerData;
		m_mode = mode;
		m_printer = new Printer(printerData);
		m_model = model;
		m_asrun = null;
		m_log = null;
	}

	/************************************************************************************
	 * 
	 ***********************************************************************************/
	public PrintJob( PrinterData printerData, IProcedure model, AsRunFile asrun, PrintMode mode )
	{
		m_printerData = printerData;
		m_mode = mode;
		m_printer = new Printer(printerData);
		m_model = model;
		m_asrun = asrun;
		m_log = null;
	}

	/************************************************************************************
	 * 
	 ***********************************************************************************/
	public PrintJob( PrinterData printerData, IProcedure model, LogFile log, PrintMode mode )
	{
		m_printerData = printerData;
		m_mode = mode;
		m_printer = new Printer(printerData);
		m_model = model;
		m_asrun = null;
		m_log = log;
	}

	/************************************************************************************
	 * 
	 ***********************************************************************************/
	@Override
    public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException
    {
    	if (m_printer.startJob("Procedure " + m_model.getProcName()))
    	{
		    GC gc = new GC(m_printer);
		    m_fonts = new PrintingFonts(gc);
		    m_basePrinter = new PageBasePrinter(gc,m_mode,m_printer,m_model,m_fonts);
		    Geometry.initialize(m_printer);

		    switch(m_mode)
	    	{
	    	case ASRUN:
	    		printAsRun(gc,monitor);
	    		break;
	    	case CODE:
	    		printCode(gc,monitor);
	    		break;
	    	case LOG:
	    		printLog(gc,monitor);
	    		break;
	    	case TEXT:
	    		printText(gc,monitor);
	    		break;
	    	}
	    	m_printer.endJob();

	    	gc.dispose();
	    	m_fonts.dispose();
	    	m_basePrinter.dispose();
	    	m_printer.dispose();
    	}
    }
	
	/************************************************************************************
	 * 
	 ***********************************************************************************/
	private void printAsRun( GC gc, IProgressMonitor monitor )
	{
		AsRunPrinter asrunPrinter = new AsRunPrinter(gc,m_asrun,m_basePrinter,m_fonts,m_printerData);
		asrunPrinter.print(monitor);
		asrunPrinter.dispose();
	}

	/************************************************************************************
	 * 
	 ***********************************************************************************/
	private void printLog( GC gc, IProgressMonitor monitor )
	{
		LogPrinter logPrinter = new LogPrinter(gc,m_log,m_basePrinter,m_fonts,m_printerData);
		logPrinter.print(monitor);
		logPrinter.dispose();
	}

	/************************************************************************************
	 * 
	 ***********************************************************************************/
	private void printCode( GC gc, IProgressMonitor monitor )
	{
		CodePrinter codePrinter = new CodePrinter(gc,m_model,m_basePrinter,m_fonts,m_printerData);
		codePrinter.print(monitor);
		codePrinter.dispose();
	}

	/************************************************************************************
	 * 
	 ***********************************************************************************/
	private void printText( GC gc, IProgressMonitor monitor )
	{
		TextPrinter textPrinter = new TextPrinter(gc,m_model,m_basePrinter,m_fonts,m_printerData);
		textPrinter.print(monitor);
		textPrinter.dispose();
	}
}
