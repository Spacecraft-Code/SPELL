///////////////////////////////////////////////////////////////////////////////
//
// PACKAGE   : com.astra.ses.spell.gui.print.commands
// 
// FILE      : TextPrinter.java
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

import java.text.SimpleDateFormat;
import java.util.Calendar;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.printing.PrinterData;
import org.eclipse.swt.widgets.Display;

import com.astra.ses.spell.gui.core.model.notification.DisplayData;
import com.astra.ses.spell.gui.core.model.types.Severity;
import com.astra.ses.spell.gui.print.commands.Geometry;
import com.astra.ses.spell.gui.print.commands.PrintingFonts;
import com.astra.ses.spell.gui.procs.interfaces.model.IProcedure;

public class TextPrinter
{
	/** Needed to change the current font */ 
	private PrintingFonts m_fonts;
	/** Used to draw in the printer */
	private GC m_gc;
	/** Needed to access the code data */
	private IProcedure m_model;
	/** Needed to know if we shall print only a page range */
	private PrinterData m_printerData;
	/** Prints the header, footer and frame and takes care of pagination */
	private PageBasePrinter m_basePrinter;
	/** Used to mark warning and error lines (needs to be disposed!) */
	private Color m_shadeColor;
	
	private static SimpleDateFormat s_df = new SimpleDateFormat("HH:mm:ss");
	
	/************************************************************************************
	 * 
	 * @param gc
	 * @param model
	 * @param basePrinter
	 * @param fonts
	 * @param pdata
	 ***********************************************************************************/
	public TextPrinter( GC gc, IProcedure model, PageBasePrinter basePrinter, PrintingFonts fonts, PrinterData pdata )
	{
		m_gc = gc;
		m_model = model;
		m_fonts = fonts;
		m_printerData = pdata;
		m_basePrinter = basePrinter;
		m_shadeColor = new Color(gc.getDevice(), 240,240,240);
	}
	
	/************************************************************************************
	 * 
	 ***********************************************************************************/
	public void dispose()
	{
		m_shadeColor.dispose();
	}
	
	/************************************************************************************
	 * 
	 * @param monitor
	 ***********************************************************************************/
	public void print( IProgressMonitor monitor )
	{
    	// Calculate the amount of pages
    	
    	// Space left after header and footer 
    	int remainder = m_basePrinter.getPrintSpace();
    	m_fonts.code();
    	// Height of the source code text font
    	int textHeight = m_gc.textExtent("T").y + 3;
    	
    	// Get the lines from the model
		DisplayData[] data = m_model.getRuntimeInformation().getDisplayMessages();

		// Find out how many pages we need: the lines that we can fit are
		int maxLines = remainder / textHeight;
		// So the total pages are
		int totalPages = data.length / maxLines;

		// X coordinates of columns and elements
		int lineNoWidth = m_gc.textExtent("00000").x;
		int timestampStart = Geometry.left + 26 + lineNoWidth;
		int lineNoCol = Geometry.left + 15 + lineNoWidth;
		int timestampCol = timestampStart + m_gc.textExtent("00:00:00").x + 15;
		int messageStart = timestampCol + 20;
		
		// Find out the maximum string length of code
		int textWidth = m_gc.textExtent("X").x;
		int maxChars = (Geometry.right - messageStart)/textWidth;
		
		int lineCount = 0;
		
		for( int page = 0; page <= totalPages; page++)
		{
			// Skip those pages out of range if needed
			if (m_printerData.scope == PrinterData.PAGE_RANGE)
			{
				if (page+1<m_printerData.startPage || page+1>m_printerData.endPage) continue;
			}

			// Position the y to start
			int y = Geometry.top + m_basePrinter.getHeaderHeight() + 15;
			
			// Calculate the indexes to be taken for the current page
			int startIndex = maxLines*page;
			int endIndex = maxLines*page + maxLines;
			if (endIndex>=data.length) endIndex = data.length-1;

			// Start the page, will create the header and frame
			m_basePrinter.startPage(m_gc,page, totalPages, monitor);
			
			// Ensure the code font is used now (the header modifies the font)
	    	m_fonts.code();
	    	
	    	// For each line in this page
			for(int index = startIndex; index < endIndex; index++)
			{
		    	if (monitor.isCanceled()) 
				{
		    		m_basePrinter.cancel();
		    		return;
				}
		    	
		    	// Get the code line
		    	DisplayData message = data[index];
		    	
		    	// Draw line number
		    	String ln = Integer.toString(lineCount);
		    	int toAdd = 5-ln.length();
		    	if (toAdd>0) for(int count = 0; count<toAdd; count++) ln = "0" + ln;
		    	m_gc.drawText(ln, Geometry.left + 7, y);

		    	// Draw timestamp
		    	String tm = message.getTime();
				try
				{
					// Time is coming from the server in the form of USECS
					Calendar c = Calendar.getInstance();
					c.setTimeInMillis( Long.parseLong(tm) / 1000 );
					tm = s_df.format(c.getTime());
			    	m_gc.drawText(tm, timestampStart, y);
				}
				catch(Exception ex) {};
				{
				}

				System.err.println(message.getSeverity());
		    	// Shading for executed lines
		    	if (message.getSeverity().equals(Severity.ERROR) || message.getSeverity().equals(Severity.WARN))
		    	{
		    		m_gc.setBackground(m_shadeColor);
		    		m_gc.fillRectangle(timestampCol+1, y, Geometry.right - timestampCol-1, textHeight);
		    	}
		    	
		    	// Draw message
		    	String msg = message.getMessage();
		    	if (msg.length()>maxChars)
		    	{
		    		msg = msg.substring(0,maxChars-3) + "...";
		    	}
		    	m_gc.drawText(msg, messageStart, y);
	    		m_gc.setBackground(Display.getDefault().getSystemColor(SWT.COLOR_WHITE));
		    	
		    	y += textHeight;
		    	lineCount++;
			}
			
			// Draw the vertical separators
			m_gc.drawLine( lineNoCol, Geometry.top + m_basePrinter.getHeaderHeight(), lineNoCol, Geometry.bottom - m_basePrinter.getFooterHeight());
			m_gc.drawLine( timestampCol, Geometry.top + m_basePrinter.getHeaderHeight(), timestampCol, Geometry.bottom - m_basePrinter.getFooterHeight());
			
			// Finish this page (creates the footer) 
	    	m_basePrinter.endPage(page+1,totalPages+1);
		}
	}

}
