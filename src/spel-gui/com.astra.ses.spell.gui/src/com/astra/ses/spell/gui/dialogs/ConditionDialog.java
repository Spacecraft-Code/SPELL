///////////////////////////////////////////////////////////////////////////////
//
// PACKAGE   : com.astra.ses.spell.gui.dialogs
// 
// FILE      : ConditionDialog.java
//
// DATE      : 2008-11-21 08:55
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
package com.astra.ses.spell.gui.dialogs;

import java.util.ArrayList;
import java.util.Calendar;

import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StackLayout;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.DateTime;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Spinner;
import org.eclipse.swt.widgets.Text;

import com.astra.ses.spell.gui.Activator;
import com.astra.ses.spell.gui.core.model.types.Level;
import com.astra.ses.spell.gui.core.utils.Logger;

/*******************************************************************************
 * @brief Dialog for defining a scheduling condition
 * @date 18/09/07
 ******************************************************************************/
public class ConditionDialog extends TitleAreaDialog implements
        SelectionListener
{
	// =========================================================================
	// # STATIC DATA MEMBERS
	// =========================================================================

	// PRIVATE -----------------------------------------------------------------
	// PROTECTED ---------------------------------------------------------------
	// PUBLIC ------------------------------------------------------------------
	public static final String	          ID	= "com.astra.ses.spell.gui.dialogs.ConditionDialog";

	// =========================================================================
	// # INSTANCE DATA MEMBERS
	// =========================================================================

	// PRIVATE -----------------------------------------------------------------
	/** Holds the dialog image icon */
	private Image	                      m_image;
	/** Holds the condition definition */
	private String	                      m_condition;
	/** Holds the relative time condition radio */
	private Button	                      m_relTimeOption;
	/** Holds the absolute time condition radio */
	private Button	                      m_absTimeOption;
	/** Holds the telemetry condition radio */
	private Button	                      m_tmOption;
	// /** Holds the custom condition radio */
	// private Button m_customOption;
	/** Holds the stacked dictionary for the condition definition widgets */
	private Composite	                  m_stackContainer;
	/** Holds the stack layout */
	private StackLayout	                  m_stack;
	/** Holds the relative time condition panel */
	private Composite	                  m_relTimePanel;
	/** Holds the absolute time condition panel */
	private Composite	                  m_absTimePanel;
	/** Holds the tm condition panel */
	private Composite	                  m_tmPanel;
	// /** Holds the custom condition panel */
	// private Composite m_customPanel;
	/** Holds the calendar for time condition */
	private DateTime	                  m_calendar;
	/** Holds the time for absolute time condition */
	private DateTime	                  m_absTime;
	/** Holds the days for relative time condition */
	private NumericSpinner	              m_days;
	/** Holds the hours for relative time condition */
	private NumericSpinner	              m_hours;
	/** Holds the minutes for relative time condition */
	private NumericSpinner	              m_minutes;
	/** Holds the seconds for relative time condition */
	private NumericSpinner	              m_seconds;
	// /** Holds the custom condition text */
	// private Text m_customCondition;
	/** Holds the list composites for tm conditions */
	private ArrayList<ParameterCondition>	m_tmPars;
	/** Holds the delay for TM comparsions */
	private Text	                      m_delay;
	private Composite m_top;

	// =========================================================================
	// # INNER CLASSES
	// =========================================================================

	private class NumericSpinner
	{
		private Spinner	m_spinner;

		public void init(int max, int initial)
		{
			m_spinner.setMinimum(0);
			m_spinner.setMaximum(max);
			m_spinner.setSelection(initial);
			m_spinner.setIncrement(1);
			m_spinner.setPageIncrement(5);
			m_spinner.addModifyListener(new ModifyListener()
			{
				public void modifyText(ModifyEvent e)
				{
					int value = m_spinner.getSelection();
					if (value > m_spinner.getMaximum())
					{
						m_spinner.setSelection(m_spinner.getMaximum());
					}
					else if (value < m_spinner.getMinimum())
					{
						m_spinner.setSelection(m_spinner.getMinimum());
					}
				}
			});
		}

		public int getSelection()
		{
			return m_spinner.getSelection();
		}

		public NumericSpinner(Composite parent, int max, int initial)
		{
			m_spinner = new Spinner(parent, SWT.BORDER);
			init(max, initial);
		}

	}

	private class ParameterCondition extends Composite
	{
		Text	parameter;
		Combo	condition;
		Text	value;
		boolean	raw;
		Text	tolerance;

		public ParameterCondition(boolean firstElement)
		{
			super(m_tmPanel, SWT.NONE);
			GridLayout gl = new GridLayout();
			gl.makeColumnsEqualWidth = true;
			gl.numColumns = 5;
			gl.marginTop = 0;
			gl.marginBottom = 0;
			gl.marginHeight = 0;
			setLayout(gl);

			if (firstElement)
			{
				Label l1 = new Label(this, SWT.NONE);
				l1.setText("Parameter");
				Label l2 = new Label(this, SWT.NONE);
				l2.setText("Condition");
				Label l3 = new Label(this, SWT.NONE);
				l3.setText("Value(s)");
				Label l4 = new Label(this, SWT.NONE);
				l4.setText("Use Raw");
				Label l5 = new Label(this, SWT.NONE);
				l5.setText("Tolerance");
			}

			parameter = new Text(this, SWT.BORDER);
			parameter.addModifyListener(new ModifyListener()
			{
				public void modifyText(ModifyEvent e)
				{
					if (parameter.getText().length() == 0)
					{
						parameter.setBackground(Display.getCurrent()
						        .getSystemColor(SWT.COLOR_YELLOW));
					}
					else
					{
						parameter.setBackground(Display.getCurrent()
						        .getSystemColor(SWT.COLOR_WHITE));
					}
				}
			});

			condition = new Combo(this, SWT.NONE | SWT.READ_ONLY);
			condition.add("eq");
			condition.add("neq");
			condition.add("lt");
			condition.add("le");
			condition.add("gt");
			condition.add("ge");
			condition.add("bw");
			condition.add("nbw");
			condition.select(0);

			value = new Text(this, SWT.BORDER);
			value.addModifyListener(new ModifyListener()
			{
				public void modifyText(ModifyEvent e)
				{
					if (value.getText().length() == 0)
					{
						value.setBackground(Display.getCurrent()
						        .getSystemColor(SWT.COLOR_YELLOW));
					}
					else
					{
						value.setBackground(Display.getCurrent()
						        .getSystemColor(SWT.COLOR_WHITE));
					}
				}
			});

			Button rawCheck = new Button(this, SWT.CHECK);
			rawCheck.setText("");
			rawCheck.addSelectionListener(new SelectionAdapter()
			{
				public void widgetSelected(SelectionEvent e)
				{
					raw = ((Button) e.widget).getSelection();
				}
			});

			tolerance = new Text(this, SWT.BORDER);
			tolerance.setText("0.0");
			tolerance.addModifyListener(new ModifyListener()
			{
				public void modifyText(ModifyEvent e)
				{
					try
					{
						String value = ((Text) e.widget).getText();
						Double.parseDouble(value);
						tolerance.setBackground(Display.getCurrent()
						        .getSystemColor(SWT.COLOR_WHITE));
					}
					catch (Exception ex)
					{
						tolerance.setBackground(Display.getCurrent()
						        .getSystemColor(SWT.COLOR_YELLOW));
					}
				}
			});

			parameter.setBackground(Display.getCurrent().getSystemColor(
			        SWT.COLOR_YELLOW));
			value.setBackground(Display.getCurrent().getSystemColor(
			        SWT.COLOR_YELLOW));
		}

		public void dispose()
		{
			parameter.dispose();
			condition.dispose();
			value.dispose();
			tolerance.dispose();
			super.dispose();
		}
	};

	/***************************************************************************
	 * Exception used when the condition cannot be correctly parsed in the
	 * parseXXX methods
	 **************************************************************************/
	private class ParseException extends RuntimeException
	{
		private static final long	serialVersionUID	= 1L;

		public ParseException(String msg)
		{
			super(msg);
		}
	};

	// PROTECTED ---------------------------------------------------------------
	// PUBLIC ------------------------------------------------------------------

	// =========================================================================
	// # ACCESSIBLE METHODS
	// =========================================================================

	/***************************************************************************
	 * Constructor
	 * 
	 * @param shell
	 *            The parent shell
	 **************************************************************************/
	public ConditionDialog(Shell shell)
	{
		super(shell);
		// Obtain the image for the dialog icon
		ImageDescriptor descr = Activator
		        .getImageDescriptor("icons/dlg_exec.png");
		m_image = descr.createImage();
		m_condition = null;
	}

	/***************************************************************************
	 * Called when the dialog is about to close.
	 * 
	 * @return The superclass return value.
	 **************************************************************************/
	public boolean close()
	{
		m_image.dispose();
		return super.close();
	}

	/***************************************************************************
	 * Obtain the defined condition, if any
	 * 
	 * @return The condition or null
	 **************************************************************************/
	public String getCondition()
	{
		return m_condition;
	}

	// =========================================================================
	// # NON-ACCESSIBLE METHODS
	// =========================================================================

	/***************************************************************************
	 * Creates the dialog contents.
	 * 
	 * @param parent
	 *            The base composite of the dialog
	 * @return The resulting contents
	 **************************************************************************/
	protected Control createContents(Composite parent)
	{
		Control contents = super.createContents(parent);
		setMessage("Define the condition for the procedure schedule:");
		setTitle("Schedule condition");
		setTitleImage(m_image);
		
		createControls(m_top);

		return contents;
	}

	/***************************************************************************
	 * Create the dialog area contents.
	 * 
	 * @param parent
	 *            The base composite of the dialog
	 * @return The resulting contents
	 **************************************************************************/
	protected Control createDialogArea(Composite parent)
	{
		// Main composite of the dialog area -----------------------------------
		m_top = (Composite) super.createDialogArea(parent);

		GridLayout clayout = new GridLayout();
		clayout.marginHeight = 2;
		clayout.marginWidth = 2;
		clayout.marginTop = 10;
		clayout.marginBottom = 10;
		clayout.marginLeft = 2;
		clayout.marginRight = 2;
		clayout.numColumns = 1;
		m_top.setLayout(clayout);

		return m_top;
	}

	/***************************************************************************
	 * Create the executor information group
	 **************************************************************************/
	protected void createControls(Composite parent)
	{
		Group typeSelection = new Group(parent, SWT.BORDER);
		GridData cld = new GridData(GridData.FILL_HORIZONTAL);
		typeSelection.setLayoutData(cld);
		typeSelection.setText("Condition type");
		GridLayout clayout = new GridLayout();
		clayout.marginHeight = 2;
		clayout.marginWidth = 2;
		clayout.marginTop = 10;
		clayout.marginBottom = 10;
		clayout.marginLeft = 2;
		clayout.marginRight = 2;
		clayout.numColumns = 1;
		typeSelection.setLayout(clayout);

		m_relTimeOption = new Button(typeSelection, SWT.RADIO);
		m_relTimeOption.setText("Relative time");
		m_absTimeOption = new Button(typeSelection, SWT.RADIO);
		m_absTimeOption.setText("Absolute time");
		m_tmOption = new Button(typeSelection, SWT.RADIO);
		m_tmOption.setText("Telemetry");
		// m_customOption = new Button( typeSelection, SWT.RADIO );
		// m_customOption.setText("Custom");

		m_relTimeOption.setSelection(true);

		m_relTimeOption.addSelectionListener(this);
		m_absTimeOption.addSelectionListener(this);
		m_tmOption.addSelectionListener(this);
		// m_customOption.addSelectionListener(this);

		Label sep2 = new Label(parent, SWT.SEPARATOR | SWT.HORIZONTAL);
		sep2.setLayoutData( new GridData( GridData.FILL_HORIZONTAL ) );
		
		m_stackContainer = new Composite(parent, SWT.NONE);
		GridData std = new GridData(GridData.FILL_HORIZONTAL);
		m_stackContainer.setLayoutData(std);

		m_stack = new StackLayout();
		m_stackContainer.setLayout(m_stack);

		createRelativeTimePanel();

		createAbsoluteTimePanel();

		createTelemetryPanel();

		// createCustomPanel();

		m_stack.topControl = m_relTimePanel;
		m_stackContainer.layout();
	}

	/***************************************************************************
	 * Create the relative time condition panel
	 **************************************************************************/
	protected void createRelativeTimePanel()
	{
		m_relTimePanel = new Composite(m_stackContainer, SWT.NONE);
		m_relTimePanel.setLayoutData(new GridData(GridData.FILL_BOTH));
		GridLayout gl = new GridLayout();
		gl.numColumns = 1;
		m_relTimePanel.setLayout(gl);

		Label l = new Label(m_relTimePanel, SWT.BOLD);
		l.setText("Choose the time to wait before starting the procedure:");

		Composite g = new Composite(m_relTimePanel, SWT.NONE);
		g.setLayout(new RowLayout());

		Label l2 = new Label(g, SWT.NONE);
		l2.setText("Days: ");

		m_days = new NumericSpinner(g, 30, 0);

		Label l3 = new Label(g, SWT.NONE);
		l3.setText(" HH: ");
		m_hours = new NumericSpinner(g, 23, 0);

		Label l4 = new Label(g, SWT.NONE);
		l4.setText(" MM: ");
		m_minutes = new NumericSpinner(g, 59, 0);

		Label l5 = new Label(g, SWT.NONE);
		l5.setText(" SS: ");
		m_seconds = new NumericSpinner(g, 59, 30);
	}

	// /***************************************************************************
	// * Create the custom panel
	// **************************************************************************/
	// protected void createCustomPanel()
	// {
	// m_customPanel = new Composite( m_stackContainer, SWT.NONE );
	// m_customPanel.setLayoutData( new GridData( GridData.FILL_BOTH ));
	// GridLayout gl = new GridLayout();
	// gl.numColumns = 1;
	// m_customPanel.setLayout( gl );
	//
	// Label l = new Label(m_customPanel, SWT.BOLD);
	// l.setText("Specify the condition to start the procedure:");
	//
	// m_customCondition = new Text(m_customPanel, SWT.BORDER);
	// m_customCondition.setLayoutData( new GridData( GridData.FILL_HORIZONTAL
	// ));
	// }

	/***************************************************************************
	 * Create the absolute time condition panel
	 **************************************************************************/
	protected void createAbsoluteTimePanel()
	{
		m_absTimePanel = new Composite(m_stackContainer, SWT.NONE);
		m_absTimePanel.setLayoutData(new GridData(GridData.FILL_BOTH));
		GridLayout gl = new GridLayout();
		gl.numColumns = 1;
		m_absTimePanel.setLayout(gl);

		Label l = new Label(m_absTimePanel, SWT.BOLD);
		l.setText("Choose the date and time to start the procedure:");
		m_calendar = new DateTime(m_absTimePanel, SWT.CALENDAR | SWT.BORDER
		        | SWT.LONG);
		m_absTime = new DateTime(m_absTimePanel, SWT.TIME);
	}

	/***************************************************************************
	 * Create the telemetry condition panel
	 **************************************************************************/
	protected void createTelemetryPanel()
	{
		m_tmPanel = new Composite(m_stackContainer, SWT.NONE);
		m_tmPanel.setLayoutData(new GridData(GridData.FILL_BOTH));
		GridLayout gl = new GridLayout();
		gl.numColumns = 1;
		gl.marginHeight = 2;
		m_tmPanel.setLayout(gl);

		Label l = new Label(m_tmPanel, SWT.BOLD);
		l.setText("Set the TM parameter conditions to start the procedure:");

		// Control for adding/removing conditions ------------------------------
		Composite ctrl = new Composite(m_tmPanel, SWT.NONE);
		ctrl.setLayout(new RowLayout());

		Button b1 = new Button(ctrl, SWT.PUSH);
		b1.setText("More");
		b1.addSelectionListener(new SelectionAdapter()
		{
			public void widgetSelected(SelectionEvent e)
			{
				addParameterCondition();
			}
		});
		Button b2 = new Button(ctrl, SWT.PUSH);
		b2.setText("Less");
		b2.addSelectionListener(new SelectionAdapter()
		{
			public void widgetSelected(SelectionEvent e)
			{
				delParameterCondition();
			}
		});
		Button b3 = new Button(ctrl, SWT.PUSH);
		b3.setText("Clear");

		Label l1 = new Label(ctrl, SWT.NONE);
		l1.setText("    Delay: ");

		m_delay = new Text(ctrl, SWT.BORDER);
		m_delay.setText("30");
		m_delay.addModifyListener(new ModifyListener()
		{
			public void modifyText(ModifyEvent e)
			{
				try
				{
					Double.parseDouble(m_delay.getText());
					m_delay.setBackground(Display.getCurrent().getSystemColor(
					        SWT.COLOR_WHITE));
				}
				catch (Exception ex)
				{
					m_delay.setBackground(Display.getCurrent().getSystemColor(
					        SWT.COLOR_YELLOW));
				}
			}
		});

		m_tmPars = new ArrayList<ParameterCondition>();
		addParameterCondition();
	}

	/***************************************************************************
	 * Add a TM parameter condition
	 **************************************************************************/
	protected void addParameterCondition()
	{
		if (m_tmPars.size() == 4) return;
		ParameterCondition c = new ParameterCondition(m_tmPars.size() == 0);
		m_tmPars.add(c);
		m_tmPanel.layout();
	}

	/***************************************************************************
	 * Delete the last TM parameter condition
	 **************************************************************************/
	protected void delParameterCondition()
	{
		if (m_tmPars.size() == 1) return;
		ParameterCondition removed = m_tmPars.remove(m_tmPars.size() - 1);
		removed.dispose();
		m_tmPanel.layout();
	}

	/***************************************************************************
	 * Clear all the TM parameter conditions
	 **************************************************************************/
	protected void clearParameterConditions()
	{
		if (m_tmPars.size() == 1) return;
		for (ParameterCondition par : m_tmPars)
		{
			par.dispose();
		}
		m_tmPars.clear();
		addParameterCondition();
		m_tmPanel.layout();
	}

	/***************************************************************************
	 * Create the button bar buttons.
	 * 
	 * @param parent
	 *            The Button Bar.
	 **************************************************************************/
	protected void createButtonsForButtonBar(Composite parent)
	{
		createButton(parent, IDialogConstants.OK_ID, IDialogConstants.OK_LABEL,
		        false);
		createButton(parent, IDialogConstants.CANCEL_ID,
		        IDialogConstants.CANCEL_LABEL, true);
	}

	/***************************************************************************
	 * Called when one of the buttons of the button bar is pressed.
	 * 
	 * @param buttonId
	 *            The button identifier.
	 **************************************************************************/
	protected void buttonPressed(int buttonId)
	{
		m_condition = null;
		switch (buttonId)
		{
		case IDialogConstants.OK_ID:
			try
			{
				if (m_absTimeOption.getSelection())
				{
					parseAbsTimeCondition();
				}
				else if (m_relTimeOption.getSelection())
				{
					parseRelTimeCondition();
				}
				else if (m_tmOption.getSelection())
				{
					parseTmCondition();
				}
				// else if (m_customOption.getSelection())
				// {
				// parseCustomCondition();
				// }
				Logger.debug("Scheduling condition: " + m_condition,
				        Level.PROC, this);
				close();
			}
			catch (ParseException ex)
			{
				MessageDialog.openError(Display.getCurrent().getActiveShell(),
				        "Condition definition error", ex.getLocalizedMessage());
			}
			break;
		case IDialogConstants.CANCEL_ID:
			close();
		}
	}

	/***************************************************************************
	 * Called to parse the absolute time condition
	 **************************************************************************/
	protected void parseAbsTimeCondition() throws ParseException
	{
		String date = "{Until:\"" + m_calendar.getYear() + "/"
		        + (m_calendar.getMonth() + 1) + "/" + m_calendar.getDay() + " "
		        + m_absTime.getHours() + ":" + m_absTime.getMinutes() + ":"
		        + m_absTime.getSeconds() + "\"}";

		// This is the reference time (NOW+30 seconds)
		Calendar c = Calendar.getInstance();
		c.add(Calendar.SECOND, 30);

		// This is the defined time
		Calendar c2 = Calendar.getInstance();
		c2.set(Calendar.YEAR, m_calendar.getYear());
		c2.set(Calendar.MONTH, m_calendar.getMonth());
		c2.set(Calendar.DAY_OF_MONTH, m_calendar.getDay());
		c2.set(Calendar.HOUR_OF_DAY, m_absTime.getHours());
		c2.set(Calendar.MINUTE, m_absTime.getMinutes());
		c2.set(Calendar.SECOND, m_absTime.getSeconds());

		if (c2.before(c)) { throw new ParseException(
		        "Specified date is before NOW + 30 seconds"); }
		m_condition = date;
	}

	/***************************************************************************
	 * Called to parse the relative time condition
	 **************************************************************************/
	protected void parseRelTimeCondition() throws ParseException
	{
		int days = m_days.getSelection();
		int hours = m_hours.getSelection();
		int minutes = m_minutes.getSelection();
		int seconds = m_seconds.getSelection();

		String date = "{Delay:\"+" + days + " " + hours + ":" + minutes + ":"
		        + seconds + "\"}";

		int secs = days * 86400 + hours * 3600 + minutes * 60 + seconds;

		if (secs < 30) { throw new ParseException(
		        "Specified time shall be after NOW + 30 seconds"); }
		m_condition = date;
	}

	/***************************************************************************
	 * Called to parse the telemetry condition
	 **************************************************************************/
	protected void parseTmCondition() throws ParseException
	{
		if (m_delay.getText().length() == 0) { throw new ParseException(
		        "Undefined delay in TM condition"); }
		try
		{
			Double.parseDouble(m_delay.getText());
		}
		catch (Exception ex)
		{
			throw new ParseException("Bad delay definition in TM condition");
		}
		if (m_tmPars.size() > 0
		        && m_tmPars.get(0).parameter.getText().length() > 0)
		{
			String condition = "[";
			for (ParameterCondition par : m_tmPars)
			{
				if (par.parameter.getText().length() == 0) { throw new ParseException(
				        "Undefined parameter name in condition "
				                + m_tmPars.indexOf(par)); }
				if (par.condition.getText().length() == 0) { throw new ParseException(
				        "Undefined comparison in condition "
				                + m_tmPars.indexOf(par)); }
				if (par.value.getText().length() == 0) { throw new ParseException(
				        "Undefined value in condition " + m_tmPars.indexOf(par)); }
				String singleCondition = "[\"" + par.parameter.getText()
				        + "\"," + par.condition.getText() + ","
				        + par.value.getText();
				if (par.raw)
				{
					singleCondition += ",{ValueFormat:RAW";
					if (par.tolerance.getText().length() > 0)
					{
						singleCondition += ",Tolerance:"
						        + par.tolerance.getText();
					}
					singleCondition += "}]";
				}
				else if (par.tolerance.getText().length() > 0)
				{
					singleCondition += ",{Tolerance:" + par.tolerance.getText();
					singleCondition += "}]";
				}
				else
				{
					singleCondition += "]";
				}
				if (condition.length() != 1)
				{
					condition += ",";
				}
				condition += singleCondition;
			}
			condition += "]";
			m_condition = "{\"verify\":" + condition + ",Delay:"
			        + m_delay.getText() + "}";
		}
	}

	// /***************************************************************************
	// * Called to parse the custom condition
	// **************************************************************************/
	// protected void parseCustomCondition() throws ParseException
	// {
	// String c = m_customCondition.getText();
	// if (c != null && c.length()>0)
	// {
	// m_condition = c;
	// }
	// else
	// {
	// m_condition = null;
	// throw new ParseException("No custom condition defined");
	// }
	// }

	@Override
	public void widgetDefaultSelected(SelectionEvent e)
	{
		widgetSelected(e);
	}

	/***************************************************************************
	 * Change the visible panel when an option is selected
	 **************************************************************************/
	@Override
	public void widgetSelected(SelectionEvent e)
	{
		if (m_absTimeOption.getSelection())
		{
			m_stack.topControl = m_absTimePanel;
		}
		else if (m_relTimeOption.getSelection())
		{
			m_stack.topControl = m_relTimePanel;
		}
		// else if (m_customOption.getSelection())
		// {
		// m_stack.topControl = m_customPanel;
		// }
		else
		{
			m_stack.topControl = m_tmPanel;

		}
		m_stackContainer.layout();
		m_top.layout();
	}
}
