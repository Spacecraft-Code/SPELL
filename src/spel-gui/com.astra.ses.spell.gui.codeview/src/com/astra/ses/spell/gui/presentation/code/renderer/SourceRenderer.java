///////////////////////////////////////////////////////////////////////////////
//
// PACKAGE   : com.astra.ses.spell.gui.presentation.code.controls
// 
// FILE      : SourceRenderer.java
//
// DATE      : Nov 21, 2012
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
package com.astra.ses.spell.gui.presentation.code.renderer;

import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.nebula.widgets.grid.GridItem;
import org.eclipse.nebula.widgets.grid.internal.DefaultCellRenderer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyleRange;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.graphics.TextLayout;
import org.eclipse.swt.graphics.TextStyle;
import org.eclipse.swt.widgets.Display;

import com.astra.ses.spell.gui.core.interfaces.ServiceManager;
import com.astra.ses.spell.gui.preferences.interfaces.IConfigurationManager;
import com.astra.ses.spell.gui.preferences.keys.FontKey;
import com.astra.ses.spell.gui.preferences.keys.GuiColorKey;
import com.astra.ses.spell.gui.presentation.code.CodeModelProxy;
import com.astra.ses.spell.gui.presentation.code.search.CodeSearch.SearchMatch;
import com.astra.ses.spell.gui.presentation.code.syntax.ISyntaxFormatter;
import com.astra.ses.spell.gui.presentation.code.syntax.SyntaxFormatter;
import com.astra.ses.spell.gui.procs.interfaces.model.ICodeLine;
import com.astra.ses.spell.gui.procs.interfaces.model.IProcedure;
import com.astra.ses.spell.gui.types.ExecutorStatus;

public class SourceRenderer extends DefaultCellRenderer 
{
	protected static IConfigurationManager s_cfg = (IConfigurationManager) ServiceManager.get(IConfigurationManager.class);

	protected final int m_leftMargin = 4;
	protected final int m_rightMargin = 4;
	protected final int m_topMargin = 0;
	protected final int m_textTopMargin = 1;

	private TextLayout m_textLayout;
	private Font m_font;
	private ISyntaxFormatter m_formatter;

	/** Highlight color for searchs */
	private Color m_hrColor = null;

	/** Currently selected m_font size */
	private int m_fontSize;
	/** Min and max m_font size */
	private int m_minFontSize;
	private int m_maxFontSize;
	
	/** Holds the highlighting color */
	private Color	               m_highlightColor;
	/** Holds the foreground color */
	private Color	               m_fgColor;
	private ExecutorStatus		   m_status;

	private IProcedure m_model;
	private CodeModelProxy m_proxy;
	
	/***************************************************************************
	 * 
	 **************************************************************************/
	public SourceRenderer( IProcedure model, CodeModelProxy proxy )
	{
		m_font = s_cfg.getFont(FontKey.CODE);
		m_fontSize = m_font.getFontData()[0].getHeight();
		m_minFontSize = 5;
		m_maxFontSize = 18;

		m_hrColor = Display.getCurrent().getSystemColor(SWT.COLOR_CYAN);

		m_formatter = new SyntaxFormatter(m_font);
		
		m_model = model;
		m_proxy = proxy;
		
		m_highlightColor = s_cfg.getGuiColor(GuiColorKey.HIGHLIGHT);
		m_fgColor = s_cfg.getGuiColor(GuiColorKey.ITEMS);
	}

	/***************************************************************************
	 * 
	 **************************************************************************/
	public void dispose()
	{
		m_formatter.dispose();
	}
	
	/***************************************************************************
	 * 
	 **************************************************************************/
	public void setExecutorStatus( ExecutorStatus st )
	{
		m_status =  st;
	}
	
	/***************************************************************************
	 * 
	 **************************************************************************/
	public void onNewSource( String codeId, String[] source )
	{
		m_formatter.newSource(codeId, source);
	}
	
	/***************************************************************************
	 * 
	 **************************************************************************/
	public int getFontSize()
	{
		return m_fontSize;
	}
	
	/***************************************************************************
	 * Increase or decrease the m_font size
	 * 
	 * @param increase
	 *            If true, increase the m_font size
	 **************************************************************************/
	public boolean zoom(boolean increase)
	{
		boolean changed = true;
		if (increase)
		{
			m_fontSize++;
			if (m_fontSize > m_maxFontSize)
			{
				m_fontSize = m_maxFontSize;
				changed = false;
			}
		}
		else
		{
			m_fontSize--;
			if (m_fontSize < m_minFontSize)
			{
				m_fontSize = m_minFontSize;
				changed = false;
			}
		}
		if (changed)
		{
			m_font = s_cfg.getFont(FontKey.CODE, m_fontSize);
		}
		return changed;
	}

	/***************************************************************************
	 * 
	 **************************************************************************/
	@Override
	public Rectangle getTextBounds(GridItem item, boolean preferred)
	{
		// Deactivate tooltip. There is an annoying bug in the Grid control
		// that makes it flicker and other erratic behaviour.
		return null;
	}
	
	/***************************************************************************
	 * 
	 **************************************************************************/
	protected Color getHighlightColor()
	{
		return m_highlightColor;
	}
	
	/***************************************************************************
	 * 
	 **************************************************************************/
	protected ICodeLine getLineModel( GridItem item )
	{
		int idx = item.getParent().indexOf(item);
		return getCodeProxy().getLine(idx);
	}

	/***************************************************************************
	 * 
	 **************************************************************************/
	protected boolean isLastLine( GridItem item )
	{
		int idx = item.getParent().indexOf(item);
		return (getCodeProxy().getCurrentCode().getLines().size()-1 == idx);
	}

	/***************************************************************************
	 * 
	 **************************************************************************/
	@Override
	public void paint(GC gc, Object value)
	{
		GridItem item = (GridItem) value;
		ICodeLine line = getLineModel(item);
		if (line == null) return;

		gc.setFont(m_font);
		m_formatter.setFont(m_font);

		boolean drawAsSelected = isSelected();
		boolean drawBackground = true;
		boolean isHighlighted = isHighlighted(line);

		if (drawAsSelected)
		{
			gc.setBackground(getDisplay().getSystemColor(SWT.COLOR_LIST_SELECTION));
		}
		else
		{
			if (item.getParent().isEnabled())
			{
				Color back = getBackground(line);

				if (back != null)
				{
					gc.setBackground(back);
				}
				else
				{
					drawBackground = false;
				}
			}
			else
			{
				gc.setBackground(getDisplay().getSystemColor(SWT.COLOR_WIDGET_BACKGROUND));
			}
			gc.setForeground(item.getForeground());
		}

		if (drawBackground)
		{
			gc.fillRectangle(getBounds().x, getBounds().y-1, getBounds().width, getBounds().height+2);
		}
		
		int x = m_leftMargin;
		int y = getBounds().y + m_textTopMargin + m_topMargin;
		int width = getBounds().width - x - m_rightMargin;

		// Create the text layout object if needed
		createTextLayout(gc,item);

		// Place the text in the layout and apply alignment
		String text = item.getText(getColumn());
		text = text.replace("\t", "    ");
		text = getShortString(gc, text, width, false);
		x += setupAlignment( gc, text, width );
		m_textLayout.setText(text);

		m_formatter.applyScheme(m_textLayout, line.getLineNo(), drawAsSelected, isHighlighted);

		// Case when drawing search results
		drawSearch(item,gc);

		// Apply the text layout
		m_textLayout.draw(gc, getBounds().x + x, y );

		// Draw the row lines
		gc.setForeground( Display.getCurrent().getSystemColor(SWT.COLOR_BLACK));
		gc.drawLine(getBounds().x-1, getBounds().y-1, getBounds().x-1, getBounds().y + getBounds().height+1);
		if (isLastLine(item))
		{
			gc.drawLine(getBounds().x-1, getBounds().y + getBounds().height, getBounds().x + getBounds().width, getBounds().y + getBounds().height);
		}
	}
	
	/***************************************************************************
	 * 
	 **************************************************************************/
	private void drawSearch( GridItem item, GC gc )
	{
		SearchMatch match = (SearchMatch) item.getData("DATA_SEARCH_MATCH"); 
		if (match !=null)
		{
			// We will set colors later
			StyleRange range = new StyleRange(match.startOffset, match.length, null, m_hrColor);
            TextStyle original = m_textLayout.getStyle(range.start);
            range.font = original.font;
            range.borderStyle = SWT.BORDER_DOT;
            m_textLayout.setStyle(range, range.start, range.start + range.length);
		}
	}
	
	/***************************************************************************
	 * 
	 **************************************************************************/
	protected Font getFont()
	{
		return m_font;
	}

	/***************************************************************************
	 * 
	 **************************************************************************/
	protected Color getForegound()
	{
		return m_fgColor;
	}

	/***************************************************************************
	 * 
	 **************************************************************************/
	protected void createTextLayout( GC gc, GridItem item )
	{
		if (m_textLayout == null)
		{
			m_textLayout = new TextLayout(gc.getDevice());
			m_textLayout.setFont(m_font);
			item.getParent().addDisposeListener(new DisposeListener()
			{
				public void widgetDisposed(DisposeEvent e)
				{
					m_textLayout.dispose();
				}
			});
		}
	}
	
	/***************************************************************************
	 * 
	 **************************************************************************/
	protected int setupAlignment( GC gc, String text, int width )
	{
		int x = 0;
		if (getAlignment() == SWT.RIGHT)
		{
			int len = gc.stringExtent(text).x;
			if (len < width)
			{
				x = width - len;
			}
		}
		else if (getAlignment() == SWT.CENTER)
		{
			int len = gc.stringExtent(text).x;
			if (len < width)
			{
				x = (width - len) / 2;
			}
		}
		return x;
	}

	/**
	 * Copied from DefaultCellRenderer in Nebula Grid widget implementation
	 */
	protected static String getShortString(GC gc, String t, int width, boolean center)
	{

		if (t == null)
		{
			return null;
		}

		if (t.equals(""))
		{
			return "";
		}

		if (width >= gc.stringExtent(t).x)
		{
			return t;
		}

		int w = gc.stringExtent("...").x;
		
		if (!center)
		{
			String text = t + "...";
			int textW = gc.stringExtent(text).x;
			int toRemove = 0;
			while(textW > width)
			{
				toRemove++;
				text = t.substring(0,t.length()-toRemove) + "...";
				textW = gc.stringExtent(text).x;
			}
			return text;
		}

		String text = t;
		int l = text.length();
		int pivot = l / 2;
		int s = pivot;
		int e = pivot + 1;
		while (s >= 0 && e < l)
		{
			String s1 = text.substring(0, s);
			String s2 = text.substring(e, l);
			int l1 = gc.stringExtent(s1).x;
			int l2 = gc.stringExtent(s2).x;
			if (l1 + w + l2 < width)
			{
				text = s1 + "..." + s2;
				break;
			}
			s--;
			e++;
		}

		if (s == 0 || e == l)
		{
			text = text.substring(0, 1) + "..." + text.substring(l - 1, l);
		}

		return text;
	}
	
	/***************************************************************************
	 * 
	 **************************************************************************/
	protected IProcedure getModel()
	{
		return m_model;
	}

	/***************************************************************************
	 * 
	 **************************************************************************/
	protected CodeModelProxy getCodeProxy()
	{
		return m_proxy;
	}

	/***************************************************************************
	 * 
	 **************************************************************************/
	protected ExecutorStatus getStatus()
	{
		return m_status;
	}
	
	/***************************************************************************
	 * 
	 **************************************************************************/
	protected boolean isHighlighted( ICodeLine line )
	{
		int lineNo = getCodeProxy().getCurrentLineNo(); 
		return ( getCodeProxy().getCurrentCode().getLine(lineNo) == line);
	}

	/***************************************************************************
	 * 
	 **************************************************************************/
	protected Color getBackground( ICodeLine line )
	{
		// If the line is executed, darken the color a bit
		Color result = null;

		// If we are in the current line, use highlight color instead
		try
		{
			if (isHighlighted(line))
			{
				result = m_highlightColor;
			}
			else if (m_status != null)
			{
				if (line.getNumExecutions()>0)
				{
					result = s_cfg.getProcedureColorDark(m_status);
				}
				else
				{
					result = s_cfg.getProcedureColor(m_status);
				}
			}
		}
		catch(Exception ignore) {}

		if (result == null)
		{
			result = Display.getDefault().getSystemColor(SWT.COLOR_WHITE);
		}
		return result;
	}

	/***************************************************************************
	 * 
	 **************************************************************************/
    public void onPropertiesChanged(PropertyChangeEvent event)
    {
		String property = event.getProperty();
		if (property.equals(GuiColorKey.HIGHLIGHT.getPreferenceName()))
		{
			Color newColor = s_cfg.getGuiColor(GuiColorKey.HIGHLIGHT);
			if (newColor != null)
			{
				m_highlightColor = newColor;
			}
		}
		else if (property.equals(GuiColorKey.ITEMS.getPreferenceName()))
		{
			Color newColor = s_cfg.getGuiColor(GuiColorKey.ITEMS); 
			if (newColor != null)
			{
				m_fgColor = newColor;
			}
		}
		else if (property.equals(FontKey.CODE.getPreferenceName()))
		{
			Font newFont = s_cfg.getFont(FontKey.CODE);
			if (newFont != null)
			{
				m_font = newFont;
			}
		}
    }
}
