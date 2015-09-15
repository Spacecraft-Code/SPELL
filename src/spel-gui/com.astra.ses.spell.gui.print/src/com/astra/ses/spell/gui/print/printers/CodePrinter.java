///////////////////////////////////////////////////////////////////////////////
//
// PACKAGE   : com.astra.ses.spell.gui.print.commands
// 
// FILE      : CodePrinter.java
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

import java.util.List;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.printing.PrinterData;
import org.eclipse.swt.widgets.Display;

import com.astra.ses.spell.gui.core.interfaces.ServiceManager;
import com.astra.ses.spell.gui.preferences.interfaces.IConfigurationManager;
import com.astra.ses.spell.gui.preferences.keys.PropertyKey;
import com.astra.ses.spell.gui.print.commands.Geometry;
import com.astra.ses.spell.gui.print.commands.PrintingFonts;
import com.astra.ses.spell.gui.procs.interfaces.model.ICodeLine;
import com.astra.ses.spell.gui.procs.interfaces.model.ICodeModel;
import com.astra.ses.spell.gui.procs.interfaces.model.IExecutionStatusManager;
import com.astra.ses.spell.gui.procs.interfaces.model.IProcedure;

public class CodePrinter
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
	/** Used to mark executed lines (needs to be disposed!) */
	private Color m_shadeColor;
	/** Stores the current configuration value for data display format */
	private String m_dataToDisplay;
	
	/************************************************************************************
	 * 
	 * @param gc
	 * @param model
	 * @param basePrinter
	 * @param fonts
	 * @param pdata
	 ***********************************************************************************/
	public CodePrinter( GC gc, IProcedure model, PageBasePrinter basePrinter, PrintingFonts fonts, PrinterData pdata )
	{
		m_gc = gc;
		m_model = model;
		m_fonts = fonts;
		m_printerData = pdata;
		m_basePrinter = basePrinter;
		m_shadeColor = new Color(gc.getDevice(), 240,240,240);
		m_dataToDisplay = ((IConfigurationManager)ServiceManager.get(IConfigurationManager.class)).getProperty(PropertyKey.DISPLAY_DATA);
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
		IExecutionStatusManager mgr = m_model.getExecutionManager();
		String codeId = mgr.getCurrentCode();
		ICodeModel model = mgr.getCodeModel(codeId);
		List<ICodeLine> lines = model.getLines();

		// Find out how many pages we need: the lines that we can fit are
		int maxLines = remainder / textHeight;
		// So the total pages are
		int totalPages = lines.size() / maxLines;

		// X coordinates of columns and elements
		int lineNoWidth = m_gc.textExtent("00000").x;
		int codeStart = Geometry.left + 26 + lineNoWidth;
		int lineNoCol = Geometry.left + 15 + lineNoWidth;
		// We give 20% space to data
		int codeEnd   = (Geometry.right - Geometry.left) - (Geometry.right - Geometry.left)/5;
		int statusDiv = codeEnd + (Geometry.right-codeEnd)/2;
		
		// Find out the maximum string length of code
		int textWidth = m_gc.textExtent("X").x;
		int maxChars = (codeEnd - codeStart)/textWidth;
		
		// Find out the maximum string length of data
		int dataMaxChars = (statusDiv - codeEnd)/textWidth;
		
		// Find out the maximum string length of status text
		int statusMaxChars = (Geometry.right - statusDiv)/textWidth;
		
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
			if (endIndex>=lines.size()) endIndex = lines.size()-1;

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
		    	ICodeLine line = lines.get(index);
		    	
		    	// Draw line number
		    	String ln = Integer.toString(line.getLineNo());
		    	int toAdd = 5-ln.length();
		    	if (toAdd>0) for(int count = 0; count<toAdd; count++) ln = "0" + ln;
		    	m_gc.drawText(ln, Geometry.left + 7, y);

		    	// Shading for executed lines
		    	if (line.getNumExecutions()>0)
		    	{
		    		m_gc.setBackground(m_shadeColor);
		    		m_gc.fillRectangle(lineNoCol, y, codeEnd-lineNoCol, textHeight);
		    	}
		    	
		    	// Draw source code
		    	String source = line.getSource();
		    	if (source.length()>maxChars)
		    	{
		    		source = source.substring(0,maxChars-3) + "...";
		    	}
		    	m_gc.drawText(source, codeStart, y);
	    		m_gc.setBackground(Display.getDefault().getSystemColor(SWT.COLOR_WHITE));
		    	
	    		// Draw data and status
	    		if (line.hasNotifications())
	    		{
	    			// We need to take into account the configuration: are we displaying only
	    			// NAME, only VALUE or both?
					String textToDraw = "";
					if (m_dataToDisplay.equals("NAME"))
					{
						textToDraw = line.getSummaryName();
					}
					else if (m_dataToDisplay.equals("VALUE"))
					{
						textToDraw = line.getSummaryValue();
					}
					else if (m_dataToDisplay.equals("BOTH"))
					{
						if (line.getSummaryName().trim().isEmpty())
						{
							textToDraw = line.getSummaryValue();
						}
						else if (line.getSummaryValue().trim().isEmpty())
						{
							textToDraw = line.getSummaryName();
						}
						else
						{
							textToDraw = line.getSummaryName() + " " + line.getSummaryValue();
						}
					}
					
					// Adjust the data text
					if (textToDraw.length()>dataMaxChars)
					{
						textToDraw = textToDraw.substring(0,dataMaxChars-3) + "...";
					}
					int fieldWidth = statusDiv - codeEnd;
					int dataWidth = m_gc.textExtent(textToDraw).x;
			    	m_gc.drawText(textToDraw, codeEnd + ( fieldWidth/2 - dataWidth/2), y);
			    	
					// Draw now the status, needs to be adjusted as well
			    	textToDraw = line.getSummaryStatus();
					if (textToDraw.length()>statusMaxChars)
					{
						textToDraw = textToDraw.substring(0,statusMaxChars-3) + "...";
					}
					fieldWidth = Geometry.right - statusDiv;
					int statusWidth = m_gc.textExtent(textToDraw).x;
			    	m_gc.drawText(textToDraw, statusDiv + ( fieldWidth/2 - statusWidth/2), y);
	    		}
		    	
		    	y += textHeight;
			}
			
			// Draw the vertical separators
			m_gc.drawLine( lineNoCol, Geometry.top + m_basePrinter.getHeaderHeight(), lineNoCol, Geometry.bottom - m_basePrinter.getFooterHeight());
			m_gc.drawLine( codeEnd, Geometry.top + m_basePrinter.getHeaderHeight(), codeEnd, Geometry.bottom - m_basePrinter.getFooterHeight());
			m_gc.drawLine( statusDiv, Geometry.top + m_basePrinter.getHeaderHeight(), statusDiv, Geometry.bottom - m_basePrinter.getFooterHeight());
			
			// Finish this page (creates the footer) 
	    	m_basePrinter.endPage(page+1,totalPages+1);
		}
	}

}
