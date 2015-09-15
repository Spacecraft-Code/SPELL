///////////////////////////////////////////////////////////////////////////////
//
// PACKAGE   : com.astra.ses.spell.gui.print.commands
// 
// FILE      : HeaderPrinter.java
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

import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;

import com.astra.ses.spell.gui.print.Activator;
import com.astra.ses.spell.gui.print.PrintMode;
import com.astra.ses.spell.gui.print.commands.Geometry;
import com.astra.ses.spell.gui.print.commands.PrintingFonts;
import com.astra.ses.spell.gui.procs.interfaces.model.IProcedure;

public class HeaderPrinter
{
	private static final int PADDING = 15;

	private GC m_gc;
	private PrintingFonts m_fonts;
	private Image m_logo;
	private IProcedure m_model;
	private int x;
	private int y;
	private int theight;
	private int tbheight;
	private int fheight;
	private String m_mode;
	
	/************************************************************************************
	 * 
	 ***********************************************************************************/
	public HeaderPrinter( GC gc, IProcedure model, PrintingFonts fonts, PrintMode mode )
	{
		m_fonts = fonts;
		m_model = model;
		m_gc = gc;
		m_fonts.normal();
		theight = gc.textExtent("T").y;
		m_fonts.bold();
		tbheight = gc.textExtent("T").y;
		fheight = tbheight + 6;
		x = Geometry.left;
		y = Geometry.top;
		m_logo = Activator.getImageDescriptor("images/SPELL_LOGO.png").createImage();
		switch(mode)
		{
		case ASRUN:
			m_mode = "ASRUN file";
			break;
		case CODE:
			m_mode = "source code";
			break;
		case LOG:
			m_mode = "log file";
			break;
		case TEXT:
			m_mode = "text messages";
			break;
		}
	}
	
	/************************************************************************************
	 * 
	 ***********************************************************************************/
	public void dispose()
	{
		m_logo.dispose();
	}
	
	/************************************************************************************
	 * 
	 ***********************************************************************************/
	public int getTotalHeight()
	{
		return fheight*2;
	}
	
	/************************************************************************************
	 * 
	 ***********************************************************************************/
	private void horizontalLine()
	{
		// Leave room for logo at the right
		m_gc.drawLine(Geometry.left,y,Geometry.right-fheight*2,y);
	}
	
	/************************************************************************************
	 * 
	 ***********************************************************************************/
	private void drawLogo()
	{
		// Draw logo
		m_gc.drawRectangle(Geometry.right-getTotalHeight(), Geometry.top, getTotalHeight(), getTotalHeight());
		m_gc.drawImage(m_logo, 0, 0, 122, 123, Geometry.right-fheight*2+1, Geometry.top+1, getTotalHeight()-2, getTotalHeight()-2);
	}
	
	/************************************************************************************
	 * 
	 ***********************************************************************************/
	private void drawField( String title, String text )
	{
		// Draw title
		m_fonts.bold();
		int m = y + fheight/2 - tbheight/2;
		m_gc.drawText(title, x, m);

		// Prepare for text item
		x += m_gc.textExtent(title).x + PADDING;

		// Draw field
		m_fonts.normal();
		m = y + fheight/2 - theight/2;
		m_gc.drawText(text,x,m);
		
		// Prepare for text item
		x += m_gc.textExtent(text).x + PADDING;
		
		// Draw field separator
		m_gc.drawLine(x,y,x,y+fheight);
		
		// Prepare for next item
		x += PADDING;
	}
	
	/************************************************************************************
	 * 
	 ***********************************************************************************/
	public void print()
	{
		drawLogo();
		
		// Draw horizontal lines
		y = Geometry.top + fheight;
		horizontalLine();
		y += fheight;
		horizontalLine();
		
		// Initialize position to start drawing fields
		x = Geometry.left + PADDING;
		y = Geometry.top;

		drawField("Procedure:", m_model.getProcName() );
		drawField("Instance:", m_model.getProcId());
		String timeId = m_model.getRuntimeInformation().getTimeId();
		String text = timeId.substring(0,10) + " " + timeId.substring(11,13) + ":" + timeId.substring(13,15) + ":" + timeId.substring(15,17);
		drawField("Executed:", text);
		
		// Initialize position to start next line
		x = Geometry.left + PADDING;
		y = Geometry.top + fheight;
		
		drawField("Print mode:", m_mode );
		drawField("Status:", m_model.getRuntimeInformation().getStatus().name());
		drawField("Stack:", m_model.getExecutionManager().getCurrentCode() + ":" + m_model.getExecutionManager().getCurrentLineNo());
	}
}
