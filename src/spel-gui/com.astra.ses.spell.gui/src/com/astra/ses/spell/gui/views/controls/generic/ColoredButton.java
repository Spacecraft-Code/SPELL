///////////////////////////////////////////////////////////////////////////////
//
// PACKAGE   : com.astra.ses.spell.gui.views.controls
// 
// FILE      : ColoredButton.java
//
// DATE      : 2010-07-01
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
// SUBPROJECT: SPELL GUI Client
//
///////////////////////////////////////////////////////////////////////////////
package com.astra.ses.spell.gui.views.controls.generic;

import java.util.Collection;
import java.util.Vector;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseListener;
import org.eclipse.swt.events.MouseMoveListener;
import org.eclipse.swt.events.MouseTrackListener;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;

/******************************************************************************
 * 
 * FakeColouredButton is a composite containing a canvas which is rendered like
 * a button. The additional feature will be that this button can have a
 * background color
 * 
 *****************************************************************************/
public class ColoredButton extends Canvas
{

	/** Button inner margin */
	private static final int	          INNER_MARGIN_X	= 3;
	private static final int	          INNER_MARGIN_Y	= 6;

	/** Button text */
	private String	                      m_text;
	/** Mouseo ver status */
	private boolean	                      m_mouseOver;
	/** Mouse pressed status */
	private boolean	                      m_mousePressed;
	/** Selection listeners */
	private Collection<SelectionListener>	m_selectionListeners;
	/** Background color */
	private Color	                      m_backgroundColor;

	/**************************************************************************
	 * Default constructor
	 * 
	 * @param parent
	 * @param style
	 *************************************************************************/
	public ColoredButton(Composite parent, int style)
	{
		super(parent, style | SWT.TRANSPARENT);
		m_selectionListeners = new Vector<SelectionListener>();

		addPaintListener(new PaintListener()
		{
			@Override
			public void paintControl(PaintEvent e)
			{
				paintWidget(e);
			}
		});

		/*
		 * Mouse button pressure control
		 */
		addMouseListener(new MouseListener()
		{
			@Override
			public void mouseUp(MouseEvent e)
			{
				m_mousePressed = false;
				redraw();
				/*
				 * Perform selection notification
				 */
				// Check if widget is enabled
				if (!isEnabled()) return;
				// Only left clicks are processed
				if (e.button != 1) return;
				// Check if the mouse ws released while the mouse pointer was
				// over it
				if (e.x < 0 || e.y < 0) return;

				notifySelection();
			}

			@Override
			public void mouseDown(MouseEvent e)
			{
				m_mousePressed = true;
				redraw();
			}

			@Override
			public void mouseDoubleClick(MouseEvent e)
			{
			}
		});

		/*
		 * Mouse movement
		 */
		addMouseMoveListener(new MouseMoveListener()
		{
			@Override
			public void mouseMove(MouseEvent e)
			{
				if (e.x < 0 || e.y < 0)
				{
					m_mouseOver = false;
					redraw();
				}
			}
		});

		/*
		 * Mouse position relative to the widget
		 */
		addMouseTrackListener(new MouseTrackListener()
		{
			@Override
			public void mouseHover(MouseEvent e)
			{
				// TODO Auto-generated method stub

			}

			@Override
			public void mouseExit(MouseEvent e)
			{
				m_mouseOver = false;
				redraw();
			}

			@Override
			public void mouseEnter(MouseEvent e)
			{
				m_mouseOver = true;
				redraw();
			}
		});

		/*
		 * Default text initialization
		 */
		m_text = "";
	}

	@Override
	public void setBackground(Color bg)
	{
		m_backgroundColor = bg;
	}

	/***************************************************************************
	 * Set the text to show inside the button
	 * 
	 * @param text
	 **************************************************************************/
	public void setText(String text)
	{
		m_text = text;
		redraw();
	}

	@Override
	public void setEnabled(boolean enabled)
	{
		super.setEnabled(enabled);
		redraw();
	}

	@Override
	public Point computeSize(int wHint, int hHint, boolean changed)
	{
		Point result = new Point(wHint, hHint);
		/*
		 * Compute canvas size according to the text to show It is considered
		 * the string size plus some inner margin
		 */
		if (!m_text.isEmpty())
		{
			int borderWidth = getBorderWidth();
			GC gc = new GC(this);
			result = gc.textExtent(m_text, SWT.DRAW_TRANSPARENT);
			result.y += 2 * (INNER_MARGIN_Y + borderWidth);
			result.x += 2 * (INNER_MARGIN_X + borderWidth);
		}
		return result;
	}

	/***************************************************************************
	 * Add a selection listener
	 * 
	 * @param listener
	 **************************************************************************/
	public void addSelectionListener(SelectionListener listener)
	{
		m_selectionListeners.add(listener);
	}

	/***************************************************************************
	 * Remove a selection listener
	 * 
	 * @param listener
	 **************************************************************************/
	public void removeSelectionListener(SelectionListener listener)
	{
		m_selectionListeners.remove(listener);
	}

	/***************************************************************************
	 * Notify the listeners this widget's selection
	 **************************************************************************/
	public void notifySelection()
	{
		for (SelectionListener listener : m_selectionListeners)
		{
			Event event = new Event();
			event.widget = this;
			listener.widgetSelected(new SelectionEvent(event));
		}
	}

	/***************************************************************************
	 * Paint this widget
	 * 
	 * @param e
	 **************************************************************************/
	private void paintWidget(PaintEvent e)
	{
		Rectangle bounds = getBounds();
		int borderWidth = getBorderWidth();

		e.gc.setAdvanced(true);
		/*
		 * Set button background color
		 */
		Color bg = m_backgroundColor;
		if (m_mouseOver && m_mousePressed)
		{
			if (bg != null)
			{
				int darkRed = Math.max(0, bg.getRed() - 64);
				int darkGreen = Math.max(0, bg.getGreen() - 64);
				int darkBlue = Math.max(0, bg.getBlue() - 64);
				bg = new Color(Display.getDefault(), darkRed, darkGreen,
				        darkBlue);
			}
		}
		// This line is to avoid setting background color to null
		bg = bg == null ? getBackground() : bg;
		/*
		 * Fill the rectangle area
		 */
		e.gc.setBackground(bg);
		e.gc.fillRoundRectangle(0, 0, bounds.width - 2 * borderWidth - 2,
		        bounds.height - 2 * borderWidth - 2, 6, 6);
		/*
		 * Draw the text
		 */
		configureEnabled(e.gc);
		String toDraw = m_text;
		Point tsize = e.gc.textExtent(m_text);
		int max_x = bounds.width-2*INNER_MARGIN_X;
		while(tsize.x > max_x)
		{
			toDraw = toDraw.substring(0,toDraw.length()-1);
			tsize = e.gc.textExtent(toDraw);
		}
		int x = INNER_MARGIN_X + bounds.width/2 - tsize.x/2;
		e.gc.drawText(toDraw, x, INNER_MARGIN_Y, true);
		/*
		 * Draw the border
		 */
		e.gc.drawRoundRectangle(0, 0, bounds.width - 2 * borderWidth - 2,
		        bounds.height - 2 * borderWidth - 2, 6, // ArcWidth
		        6); // ArcHeight
	}

	/***************************************************************************
	 * Render this button on its enabled state
	 **************************************************************************/
	private void configureEnabled(GC graphics)
	{
		if (isEnabled())
		{
			graphics.setForeground(Display.getDefault().getSystemColor(
			        SWT.COLOR_WIDGET_FOREGROUND));
		}
		else
		{
			graphics.setForeground(Display.getDefault().getSystemColor(
			        SWT.COLOR_WIDGET_NORMAL_SHADOW));
		}
	}
}
