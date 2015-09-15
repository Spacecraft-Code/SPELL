///////////////////////////////////////////////////////////////////////////////
//
// PACKAGE   : com.astra.ses.spell.gui.print.commands
// 
// FILE      : PageBasePrinter.java
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
package com.astra.ses.spell.gui.print.printers;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.printing.Printer;

import com.astra.ses.spell.gui.print.PrintMode;
import com.astra.ses.spell.gui.print.commands.Geometry;
import com.astra.ses.spell.gui.print.commands.PrintingFonts;
import com.astra.ses.spell.gui.procs.interfaces.model.IProcedure;

public class PageBasePrinter
{
	private HeaderPrinter m_header;
	private FooterPrinter m_footer;
	private Printer m_printer;

	/************************************************************************************
	 * 
	 ***********************************************************************************/
	public PageBasePrinter( GC gc, PrintMode mode, Printer printer, IProcedure model, PrintingFonts fonts)
	{
		m_printer = printer;
	    m_header = new HeaderPrinter(gc,model,fonts,mode);
	    m_footer = new FooterPrinter(gc,fonts);
	}
	
	/************************************************************************************
	 * 
	 ***********************************************************************************/
	public void startPage( GC gc, int page, int totalPages, IProgressMonitor monitor )
	{
		if (m_printer.startPage())
		{
			monitor.setTaskName("Printing page " + page + " of " + totalPages);
			// Print frame
			gc.drawRectangle(Geometry.left,Geometry.top,Geometry.right-Geometry.left,Geometry.bottom-Geometry.top);
	    	if (monitor.isCanceled()) 
			{
	    		cancel();
	    		return;
			}
	    	m_header.print();
	    	if (monitor.isCanceled()) 
			{
	    		cancel();
	    		return;
			}
		}
	}
	
	/************************************************************************************
	 * 
	 ***********************************************************************************/
	public int getHeaderHeight()
	{
		return m_header.getTotalHeight();
	}

	/************************************************************************************
	 * 
	 ***********************************************************************************/
	public int getFooterHeight()
	{
		return m_footer.getTotalHeight();
	}
	
	/************************************************************************************
	 * 
	 ***********************************************************************************/
	public int getPrintSpace()
	{
		return (Geometry.bottom - Geometry.top) - m_header.getTotalHeight() - m_footer.getTotalHeight() - 30;
	}

	/************************************************************************************
	 * 
	 ***********************************************************************************/
	public void endPage( int page, int totalPages )
	{
		m_footer.print( page, totalPages );
		m_printer.endPage();
	}

	/************************************************************************************
	 * 
	 ***********************************************************************************/
	public void cancel()
	{
		m_printer.cancelJob();
	}
	
	/************************************************************************************
	 * 
	 ***********************************************************************************/
	public void dispose()
	{
    	m_header.dispose();
	}
}
