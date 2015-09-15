///////////////////////////////////////////////////////////////////////////////
//
// PACKAGE   : com.astra.ses.spell.gui.presentation.text.controls
// 
// FILE      : DisplayViewer.java
//
// DATE      : 2008-11-21 13:54
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
package com.astra.ses.spell.gui.presentation.text.controls;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.TimeZone;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;

import com.astra.ses.spell.gui.core.interfaces.ServiceManager;
import com.astra.ses.spell.gui.core.model.types.Scope;
import com.astra.ses.spell.gui.core.model.types.Severity;
import com.astra.ses.spell.gui.preferences.interfaces.IConfigurationManager;
import com.astra.ses.spell.gui.preferences.keys.FontKey;
import com.astra.ses.spell.gui.preferences.keys.PropertyKey;
import com.astra.ses.spell.gui.presentation.text.model.ParagraphType;
import com.astra.ses.spell.gui.presentation.text.model.TextParagraph;
import com.astra.ses.spell.gui.types.ExecutorStatus;

/*******************************************************************************
 * @brief Text-based view of the procedure execution
 * @date 09/10/07
 ******************************************************************************/
public class DisplayViewer  
{
	private static IConfigurationManager s_cfg = null;
	private static DateFormat s_format ;
	
	// Time formatter
	static
	{
		s_format = new SimpleDateFormat("HH:mm:ss");
		s_format.setTimeZone(TimeZone.getTimeZone("GMT"));
	}
	
	/** Text contents */
	private CustomStyledText m_text;
	/** Show timestamp control */
	private Button m_chkTimestamp;
	/** Code name control */
	private Label m_codeTitle;
	private Label m_codeName;
	private Label m_functionTitle;
	private Label m_functionName;
	private Label m_lineTitle;
	private Label m_lineName;
	/** Previous status */
	private ExecutorStatus m_previousStatus = null;

	/***************************************************************************
	 * Constructor.
	 * 
	 * @param view
	 *            Parent procedure view
	 * @param top
	 *            Container composite
	 **************************************************************************/
	public DisplayViewer(Composite top, int capacity)
	{
		if (s_cfg == null)
		{
			s_cfg = (IConfigurationManager) ServiceManager.get(IConfigurationManager.class);
		}

		Composite base = new Composite(top, SWT.NONE);
		base.setLayoutData( new GridData( GridData.FILL_BOTH ));
		base.setLayout( new GridLayout(1,true) );
		
		m_text = new CustomStyledText(base, capacity);
		m_text.setLayoutData(new GridData(GridData.FILL_BOTH | GridData.GRAB_HORIZONTAL | GridData.GRAB_VERTICAL));
		m_text.setBackground(s_cfg.getProcedureColor(ExecutorStatus.LOADED));

		Composite tools = new Composite(base, SWT.BORDER);
		tools.setLayoutData( new GridData( GridData.FILL_HORIZONTAL ));
		tools.setLayout( new GridLayout( 10, false ) );
		
		m_chkTimestamp = new Button(tools, SWT.CHECK );
		m_chkTimestamp.setText("Show message timestamp");
		m_chkTimestamp.setLayoutData( new GridData( GridData.HORIZONTAL_ALIGN_BEGINNING ) );
		m_chkTimestamp.addSelectionListener( new SelectionAdapter()
		{
			public void widgetSelected( SelectionEvent ev )
			{
				toggleTimestamp();
			}
		}
		);
		
		m_chkTimestamp.setSelection(s_cfg.getProperty(PropertyKey.TEXT_TIMESTAMP).equals("YES"));
		
		Label sep = new Label( tools, SWT.SEPARATOR | SWT.VERTICAL );
		GridData gd = new GridData();
		gd.heightHint = 20;
		gd.widthHint = 30;
		sep.setLayoutData(gd);
		
		m_codeTitle = new Label(tools, SWT.NONE);
		m_codeTitle.setText("Current code: ");
		m_codeTitle.setLayoutData( new GridData( GridData.HORIZONTAL_ALIGN_BEGINNING ) );
		m_codeTitle.setFont(s_cfg.getFont(FontKey.GUI_BOLD));
		
		m_codeName = new Label( tools, SWT.NONE);
		m_codeName.setText( "" );
		m_codeName.setLayoutData( new GridData( GridData.GRAB_HORIZONTAL ) );

		Label sep2 = new Label( tools, SWT.SEPARATOR | SWT.VERTICAL );
		GridData gd2 = new GridData();
		gd2.heightHint = 20;
		gd2.widthHint = 30;
		sep2.setLayoutData(gd2);

		m_lineTitle = new Label(tools, SWT.NONE);
		m_lineTitle.setText("Line: ");
		m_lineTitle.setLayoutData( new GridData( GridData.HORIZONTAL_ALIGN_BEGINNING ) );
		m_lineTitle.setFont(s_cfg.getFont(FontKey.GUI_BOLD));
		
		m_lineName = new Label( tools, SWT.NONE);
		m_lineName.setText( "" );
		m_lineName.setLayoutData( new GridData( GridData.GRAB_HORIZONTAL ) );

		Label sep3 = new Label( tools, SWT.SEPARATOR | SWT.VERTICAL );
		GridData gd3 = new GridData();
		gd3.heightHint = 20;
		gd3.widthHint = 30;
		sep3.setLayoutData(gd3);
		
		m_functionTitle = new Label(tools, SWT.NONE);
		m_functionTitle.setText("Function: ");
		m_functionTitle.setLayoutData( new GridData( GridData.HORIZONTAL_ALIGN_BEGINNING ) );
		m_functionTitle.setFont(s_cfg.getFont(FontKey.GUI_BOLD));
		
		m_functionName = new Label( tools, SWT.NONE);
		m_functionName.setText( "" );
		m_functionName.setLayoutData( new GridData( GridData.GRAB_HORIZONTAL ) );
	}

	/***************************************************************************
	 * Set code information
	 **************************************************************************/
	public void setCodeName( String name, String function, String line )
	{
		if (m_codeName.isDisposed()) return;
		m_codeName.setText(name);
		m_lineName.setText(line);
		if (function == null || function.equals("<module>"))
		{
			m_functionTitle.setVisible(false);
			m_functionName.setVisible(false);
		}
		else
		{
			m_functionTitle.setVisible(true);
			m_functionName.setVisible(true);
			m_functionName.setText(function);
		}
		m_codeName.getParent().layout();
	}

	/***************************************************************************
	 * Toggle timestamp display
	 **************************************************************************/
	private void toggleTimestamp()
	{
		m_text.setShowTimestamp( m_chkTimestamp.getSelection() );
	}
	
	/***************************************************************************
	 * Enable or disable the viewer
	 **************************************************************************/
	public void setEnabled(boolean enable)
	{
		m_text.setEnabled(enable);
		m_chkTimestamp.setEnabled(enable);
	}

	/***************************************************************************
	 * Refresh the text view with the appropiate contents
	 **************************************************************************/
	public void refresh()
	{
		// Nothing to do
	}

	/***************************************************************************
	 * Receive the focus
	 **************************************************************************/
	public void setFocus()
	{
		m_text.setFocus();
	}

	/***************************************************************************
	 * Increase or decrease the font size
	 **************************************************************************/
	public void zoom(boolean increase)
	{
		m_text.zoom(increase);
	}

	/***************************************************************************
	 * Change autoscroll mode
	 * 
	 * @param enabled
	 **************************************************************************/
	public void setAutoscroll(boolean enabled)
	{
		m_text.setAutoScroll(enabled);
	}

	/***************************************************************************
	 * Callback for status notifications
	 **************************************************************************/
	public void notifyProcStatus(ExecutorStatus status)
	{
		// Do not consider WAITING as a relevant status to be show as message
		if ((status != ExecutorStatus.PROMPT) && (status != ExecutorStatus.WAITING) && (status != m_previousStatus))
		{
			m_previousStatus = status;
		}
		m_text.setRedraw(false);
		m_text.setBackground(s_cfg.getProcedureColor(status));
		m_text.setRedraw(true);
	}

	/***************************************************************************
	 * Add a normal message to the model
	 **************************************************************************/
	public synchronized void addMessage(String text, Severity severity, String timestamp, Scope scope, long sequence)
	{
		String msgTimestamp = "";
		try
		{
			// Time is coming from the server in the form of USECS
			Calendar c = Calendar.getInstance();
			c.setTimeInMillis( Long.parseLong(timestamp) / 1000 );
			msgTimestamp = s_format.format(c.getTime());
		}
		catch(Exception ex)
		{
			// Time is coming from the server in the form of USECS
			Calendar c = Calendar.getInstance();
			msgTimestamp = s_format.format(c.getTime());
		}
		
		TextParagraph p = null;
		if (severity == Severity.ERROR)
		{
			p = getTextParagraph(ParagraphType.ERROR, scope, text, msgTimestamp, sequence);
		}
		else if (severity == Severity.WARN)
		{
			p = getTextParagraph(ParagraphType.WARNING, scope, text, msgTimestamp, sequence);
		}
		// When replaying prompts
		else if (severity == Severity.PROMPT)
		{
			p = getTextParagraph(ParagraphType.PROMPT, scope, text, msgTimestamp, sequence);
		}
		else
		{
			p = getTextParagraph(ParagraphType.NORMAL, scope, text, msgTimestamp, sequence);
		}
		appendParagraph(p);
	}

	/***************************************************************************
	 * Clear the text view model
	 **************************************************************************/
	public void clear()
	{
		m_text.clear();
	}

	/***************************************************************************
	 * Obtain the context as lines of text
	 **************************************************************************/
	public String[] getTextLines()
	{
		return m_text.getTextLines();
	}

	/***************************************************************************
	 * Append a paragraph and show last line
	 **************************************************************************/
	private void appendParagraph(TextParagraph p)
	{
		m_text.append(p);
	}

	/***************************************************************************
	 * Create a text parahraph with the given type.
	 **************************************************************************/
	private TextParagraph getTextParagraph(ParagraphType t, Scope scope, String text, String timestamp, long sequence)
	{
		switch (t)
		{
		case ERROR:
			return new TextParagraph(ParagraphType.ERROR, scope, text, timestamp, sequence);
		case WARNING:
			return new TextParagraph(ParagraphType.WARNING, scope, text, timestamp, sequence);
		case NOTIF_WARN:
			return new TextParagraph(ParagraphType.WARNING, scope, text, timestamp, sequence);
		case NOTIF_ERR:
			return new TextParagraph(ParagraphType.ERROR, scope, text, timestamp, sequence);
		case PROMPT:
			return new TextParagraph(ParagraphType.NORMAL, scope, text, timestamp, sequence);
		case SPELL:
			return new TextParagraph(ParagraphType.NORMAL, scope, text, timestamp, sequence);
		default: /* NORMAL */
			return new TextParagraph(ParagraphType.NORMAL, scope, text, timestamp, sequence);
		}
	}
	
	/***************************************************************************
	 * 
	 **************************************************************************/
	public void setBackground( Color color )
	{
		m_text.setBackground(color);
	}

	/***************************************************************************
	 * 
	 **************************************************************************/
	public void setShowTimestamp( boolean show )
	{
		m_text.setShowTimestamp( show );
	}

	/***************************************************************************
	 * 
	 **************************************************************************/
	public void setCapacity( int capacity )
	{
		m_text.setCapacity( capacity );
	}

	/***************************************************************************
	 * 
	 **************************************************************************/
	public void setFont( Font newFont )
	{
		m_text.setFont(newFont);
	}
}
