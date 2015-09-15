///////////////////////////////////////////////////////////////////////////////
//
// PACKAGE   : com.astra.ses.spell.gui.print.commands
// 
// FILE      : FooterPrinter.java
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

import org.eclipse.swt.graphics.GC;

import com.astra.ses.spell.gui.core.interfaces.IContextProxy;
import com.astra.ses.spell.gui.core.interfaces.ServiceManager;
import com.astra.ses.spell.gui.print.commands.Geometry;
import com.astra.ses.spell.gui.print.commands.PrintingFonts;

public class FooterPrinter
{
	private static final int PADDING = 15;

	private GC m_gc;
	private PrintingFonts m_fonts;
	private int x;
	private int y;
	public int theight;
	public int tbheight;
	public int fheight;
	
	public FooterPrinter( GC gc, PrintingFonts fonts )
	{
		m_fonts = fonts;
		m_gc = gc;
		m_fonts.normal();
		theight = gc.textExtent("T").y;
		m_fonts.bold();
		tbheight = gc.textExtent("T").y;
		fheight = tbheight + 6;
		x = Geometry.left;
		y = Geometry.top;
	}
	
	public int getTotalHeight()
	{
		return fheight;
	}

	private void horizontalLine()
	{
		// Leave room for logo at the right
		m_gc.drawLine(Geometry.left,y,Geometry.right,y);
	}
	
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
	
	public void print( int page, int totalPages)
	{
		// Draw horizontal line
		y = Geometry.bottom - fheight;
		horizontalLine();
		
		// Initialize position to start drawing fields
		x = Geometry.left + PADDING;

		String text = "???";
		try
		{
			text = java.net.InetAddress.getLocalHost().getHostName();
		}
		catch(Exception ex){;};
		drawField("Workstation:", text );
		drawField("Client:", ((IContextProxy) ServiceManager.get(IContextProxy.class)).getClientKey());
		SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		drawField("Printed on:",df.format(Calendar.getInstance().getTime()));
		drawField("Page:", "" + page + "/" + totalPages);
	}
}
