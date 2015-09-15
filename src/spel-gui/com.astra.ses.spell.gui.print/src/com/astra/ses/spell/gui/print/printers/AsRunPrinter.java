///////////////////////////////////////////////////////////////////////////////
//
// PACKAGE   : com.astra.ses.spell.gui.print.commands
// 
// FILE      : AsRunPrinter.java
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

import com.astra.ses.spell.gui.core.model.files.AsRunFile;
import com.astra.ses.spell.gui.core.model.files.AsRunFileLine;
import com.astra.ses.spell.gui.core.model.files.IServerFileLine;
import com.astra.ses.spell.gui.core.model.types.AsRunType;
import com.astra.ses.spell.gui.print.commands.Geometry;
import com.astra.ses.spell.gui.print.commands.PrintingFonts;

public class AsRunPrinter
{
	/** Needed to change the current font */ 
	private PrintingFonts m_fonts;
	/** Used to draw in the printer */
	private GC m_gc;
	/** Contains the asrun file */
	private AsRunFile m_asrun;
	/** Needed to know if we shall print only a page range */
	private PrinterData m_printerData;
	/** Prints the header, footer and frame and takes care of pagination */
	private PageBasePrinter m_basePrinter;
	/** Used to mark warning and error lines (needs to be disposed!) */
	private Color m_shadeColor;
	
	/************************************************************************************
	 * 
	 * @param gc
	 * @param model
	 * @param basePrinter
	 * @param fonts
	 * @param pdata
	 ***********************************************************************************/
	public AsRunPrinter( GC gc, AsRunFile asrun, PageBasePrinter basePrinter, PrintingFonts fonts, PrinterData pdata )
	{
		m_gc = gc;
		m_asrun = asrun;
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
    	
    	// Get the ASRUN file
		List<IServerFileLine> lines = m_asrun.getLines();

		// Find out how many pages we need: the lines that we can fit are
		int maxLines = remainder / textHeight;
		// So the total pages are
		int totalPages = lines.size() / maxLines;

		// X coordinates of columns and elements
		int timestampWidth = m_gc.textExtent("0000-00-00 00:00:00").x + 20;
		int timestampStart = Geometry.left + 7;
		int timestampCol = timestampStart + timestampWidth;
		
		int typeStart = timestampCol + 20;
		int typeWidth = m_gc.textExtent("PROMPT_EXPECTED").x + 20;
		int typeCol = typeStart + typeWidth;
		
		int subtypeStart = typeCol + 20;
		int subtypeWidth = m_gc.textExtent("PROMPT_EXPECTED").x + 20;
		int subtypeCol = subtypeStart + subtypeWidth;
		
		int stackStart = subtypeStart + subtypeWidth + 20;
		// We give 25% space to stack
		int stackWidth = (Geometry.right - Geometry.left)/4;
		int stackCol = stackStart + stackWidth;
		int dataStart = stackCol + 20;
		
		// Maximum characters for the stack
		int textWidth = m_gc.textExtent("X").x;
		int maxStack = (dataStart - stackStart)/textWidth; 
		int maxData = (Geometry.right - dataStart)/textWidth; 
		
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
		    	
		    	AsRunFileLine line = (AsRunFileLine) lines.get(index);

	    		m_gc.setBackground(Display.getDefault().getSystemColor(SWT.COLOR_WHITE));

		    	// Draw timestamp
		    	m_gc.drawText(line.getTimestamp(), timestampStart, y);

		    	if (line.getAsRunType().equals(AsRunType.STATUS))
		    	{
		    		m_gc.setBackground(m_shadeColor);
		    		m_gc.fillRectangle(timestampCol+1, y, Geometry.right-timestampCol-2, textHeight);
		    	}

		    	// Draw type
		    	String text = line.getType();
		    	m_gc.drawText(text, typeStart, y);
		    	
		    	// Draw subtype
		    	text = line.getSubType();
		    	m_gc.drawText(text, subtypeStart, y);
		    	
		    	// Draw stack
		    	text = line.getStackPosition();
		    	if (text.length()>maxStack)
		    	{
		    		text = "..." + text.substring(text.length()-maxStack-3);
		    	}
		    	m_gc.drawText(text, stackStart, y);
		    	
		    	// Draw other data
		    	text = line.getDataA() + " " + line.getDataB() + " " + line.getDataC() + " " + line.getDataD() + " " + line.getComment();
		    	text = text.trim();
		    	if (text.length()>maxData)
		    	{
		    		text = text.substring(0,text.length()-maxData-3) + "...";
		    	}
		    	m_gc.drawText(text, dataStart, y);
		    	
		    	y += textHeight;
			}
			
			// Draw the vertical separators
			m_gc.drawLine( timestampCol, Geometry.top + m_basePrinter.getHeaderHeight(), timestampCol, Geometry.bottom - m_basePrinter.getFooterHeight());
			m_gc.drawLine( typeCol, Geometry.top + m_basePrinter.getHeaderHeight(), typeCol, Geometry.bottom - m_basePrinter.getFooterHeight());
			m_gc.drawLine( subtypeCol, Geometry.top + m_basePrinter.getHeaderHeight(), subtypeCol, Geometry.bottom - m_basePrinter.getFooterHeight());
			m_gc.drawLine( stackCol, Geometry.top + m_basePrinter.getHeaderHeight(), stackCol, Geometry.bottom - m_basePrinter.getFooterHeight());
			
			// Finish this page (creates the footer) 
	    	m_basePrinter.endPage(page+1,totalPages+1);
		}
	}

}
