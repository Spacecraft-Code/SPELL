package com.astra.ses.spell.gui.shared.views.controls;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

public class SharedVariableDialog extends Dialog
{
	private Text m_key;
	private Text m_value;
	private String m_storedKey;
	private String m_storedValue;
	private boolean m_keyEditable;
	private String m_scopeName;
	private boolean m_scopeMode;
	
	public SharedVariableDialog(Shell parentShell, boolean keyEditable, String initialKey, String initialValue)
	{
		super(parentShell);
		m_keyEditable = keyEditable;
		m_storedKey = initialKey;
		m_storedValue = initialValue;
		m_scopeName = null;
		m_scopeMode = false;
		
	}

	public SharedVariableDialog(Shell parentShell)
	{
		super(parentShell);
		m_keyEditable = false;
		m_storedKey = null;
		m_storedValue = null;
		m_scopeName = null;
		m_scopeMode = true;
	}

	@Override
	protected Control createDialogArea(Composite parent)
	{
		Composite container = (Composite) super.createDialogArea(parent);
		GridLayout layout = new GridLayout(2, false);
		layout.marginRight = 5;
		layout.marginLeft = 10;
		container.setLayout(layout);

		if (m_scopeMode)
		{
			getShell().setText("New shared scope");
			Label lblKey = new Label(container, SWT.NONE);
			lblKey.setText("Scope name:");
	
			m_key = new Text(container,SWT.BORDER);
			m_key.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
			m_key.setText("Name");
		}
		else
		{
			getShell().setText("New shared variable");
			Label lblKey = new Label(container, SWT.NONE);
			lblKey.setText("Variable:");
	
			m_key = new Text(container, m_keyEditable ? SWT.BORDER : SWT.BORDER | SWT.READ_ONLY );
			m_key.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
			m_key.setText(m_storedKey);
	
			Label lblValue = new Label(container, SWT.NONE);
			GridData gd_lblNewLabel = new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1);
			gd_lblNewLabel.horizontalIndent = 1;
			lblValue.setLayoutData(gd_lblNewLabel);
			lblValue.setText("Value:");
	
			m_value = new Text(container, SWT.BORDER);
			m_value.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
			m_value.setText(m_storedValue);
		}
		
		return container;
	}

	// override method to use "Login" as label for the OK button
	@Override
	protected void createButtonsForButtonBar(Composite parent)
	{
		createButton(parent, IDialogConstants.OK_ID, IDialogConstants.OK_LABEL, true);
		createButton(parent, IDialogConstants.CANCEL_ID, IDialogConstants.CANCEL_LABEL, false);
	}

	@Override
	protected Point getInitialSize()
	{
		return new Point(350, 150);
	}

	@Override
	protected void okPressed()
	{
		if (m_scopeMode)
		{
			m_scopeName = m_key.getText();
		}
		else
		{
			m_storedKey = m_key.getText();
			m_storedValue = m_value.getText();
		}
		super.okPressed();
	}

	public String getKey()
	{
		return m_storedKey;
	}

	public String getValue()
	{
		return m_storedValue;
	}

	public String getScope()
	{
		return m_scopeName;
	}
}
