///////////////////////////////////////////////////////////////////////////////
//
// PACKAGE   : com.astra.ses.spell.gui.presentation.code.controls
// 
// FILE      : StackControl.java
//
// DATE      : Mar 28, 2014
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
package com.astra.ses.spell.gui.presentation.code.controls;

import java.util.List;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;

import com.astra.ses.spell.gui.presentation.code.Activator;
import com.astra.ses.spell.gui.presentation.code.CodeModelProxy;
import com.astra.ses.spell.gui.procs.interfaces.model.IProcedure;
import com.astra.ses.spell.gui.types.ExecutorStatus;

public class StackControl extends Composite
{
	/** Hold the button images */
	private Image m_firstIcon;
	private Image m_leftIcon;
	private Image m_rightIcon;
	private Image m_lastIcon;
	
	/* Number of functions relative to total of #number of stack*/
	private String m_numberPattern = "#current of #stack levels";
	private Label m_numberLabel;
	
	/** Navigation */
	private Button m_firstButton;
	private Button m_leftButton;
	private Button m_rightButton;
	private Button m_lastButton;
	
	private Combo m_stackItemsCombo;
	
	//private Button m_navButton;

	/***************************************************************************
	 * 
	 **************************************************************************/
	public StackControl( Composite parent, int style, IProcedure model, final CodeViewer viewer )
	{
		super(parent,style);
		
		m_firstIcon = Activator.getImageDescriptor("icons/bullet_arrow_first.png").createImage();
		m_leftIcon = Activator.getImageDescriptor("icons/bullet_arrow_left.png").createImage();
		m_rightIcon = Activator.getImageDescriptor("icons/bullet_arrow_right.png").createImage();
		m_lastIcon = Activator.getImageDescriptor("icons/bullet_arrow_last.png").createImage();
		
		//m_ruleIcon = Activator.getImageDescriptor("icons/rule.png").createImage();

		//First button
		m_firstButton = new Button(this, SWT.PUSH);
		m_firstButton.setImage( m_firstIcon );
		m_firstButton.setEnabled(false);
		m_firstButton.setToolTipText("Move to the fist level in the call stack");
		m_firstButton.addSelectionListener( new SelectionAdapter()
		{
			public void widgetSelected( SelectionEvent ev )
			{
				viewer.displayCodeFirst();
			}
		});
		
		
		//Left button
		m_leftButton = new Button(this,SWT.PUSH);
		m_leftButton.setImage( m_leftIcon );
		m_leftButton.setEnabled(false);
		m_leftButton.setToolTipText("Move one level down in the call stack");
		m_leftButton.addSelectionListener( new SelectionAdapter()
		{
			public void widgetSelected( SelectionEvent ev )
			{
				viewer.displayCodeLeft();
			}
		});
		
		//Label
		m_numberLabel = new Label(this, SWT.NONE);
		String sNumber = m_numberPattern.replace("#current", String.valueOf(1)).replace("#stack", String.valueOf(1)) ;
		m_numberLabel.setText(sNumber);
		
		// Right button
		m_rightButton = new Button(this,SWT.PUSH);
		m_rightButton.setImage( m_rightIcon );
		m_rightButton.setEnabled(false);
		m_rightButton.setToolTipText("Move one level up in the call stack");
		m_rightButton.addSelectionListener( new SelectionAdapter()
		{
			public void widgetSelected( SelectionEvent ev )
			{
				viewer.displayCodeRight();
			}
		});

		// Last button
		m_lastButton = new Button(this, SWT.PUSH);
		m_lastButton.setImage( m_lastIcon );
		m_lastButton.setEnabled(false);
		m_lastButton.setToolTipText("Move to the last level in the call stack");
		m_lastButton.addSelectionListener( new SelectionAdapter()
		{
			public void widgetSelected( SelectionEvent ev )
			{
				viewer.displayCodeLast();
			}
		});		
		
		
		//Combo with the current procedure
		
		m_stackItemsCombo = new Combo(this, SWT.PUSH | SWT.H_SCROLL | SWT.V_SCROLL);
		
		String name = model.getProcId();
		int idx = name.indexOf("#");
		name = "1: " + name.substring(0,idx) + " (main)";

		m_stackItemsCombo.add(name);
		m_stackItemsCombo.select(0);
		m_stackItemsCombo.setLayoutData( new GridData(GridData.FILL_HORIZONTAL) );
		
		m_stackItemsCombo.setToolTipText("Select a level in the call stack");
		
		m_stackItemsCombo.addSelectionListener( new SelectionAdapter()
		{
			public void widgetSelected( SelectionEvent ev )
			{
				viewer.displayCode(m_stackItemsCombo.getSelectionIndex());
			}
		});
		
		//Set tab order
		Control[] tabOrder = new Control[] { m_firstButton, m_leftButton, m_rightButton, m_lastButton, m_stackItemsCombo};
		this.setTabList(tabOrder);
		
	}

	/***************************************************************************
	 * 
	 **************************************************************************/
	public void dispose()
	{
		m_firstIcon.dispose();
		m_leftIcon.dispose();
		m_rightIcon.dispose();
		m_lastIcon.dispose();
		super.dispose();
	}
	
	/***************************************************************************
	 * 
	 **************************************************************************/
	void updateCodeNavigation( ExecutorStatus st, CodeModelProxy proxy )
	{
		if (m_leftButton.isDisposed() || m_rightButton.isDisposed()) return;

		// Get values from proxy
		String code;
		String function;
		//String function = proxy.getCurrentFunction();
		int currentOV = proxy.getDisplayOverride();
		int currentSD = proxy.getViewDepth();
		List<String> codes = proxy.getAvailableCodes();
		List<String> functions = proxy.getAvailableFunctions();
		int size = codes.size();
		
		int currentStack = (currentOV==-1?currentSD:currentOV);

		//Update displayed stack

		// level number
		String sNumber = m_numberPattern.replace("#current", String.valueOf(currentStack+1)).replace("#stack", String.valueOf(size)) ;
		m_numberLabel.setText(sNumber);
		
		//write levels on combo
		m_stackItemsCombo.removeAll();
		
		int i = 0;
		for(String c: codes) {
			code = String.valueOf(i+1) + ": " + c;
			function = functions.get(i);
			//function = "functions size: " + functions.size() + "List: " + functions.toString();
			
			if (function.equals("<module>"))
			{
				code += " (main)";
			}
			else
			{
				code += "::" + function + "()";
			}
			m_stackItemsCombo.add( code );
			i++;
		} //for fill combo
		
		//select current element
		m_stackItemsCombo.select(currentStack);
		
				
		if (currentOV == currentSD)
		{
			proxy.resetDisplayOverride();
		}

		// Update enablement of buttons. First check the status: if the
		// status is not correct, not worth it to keep checking more conditions
		if (st != null)
		{
			switch(st)
			{
			case PAUSED:
			case FINISHED:
			case PROMPT:
			case INTERRUPTED:
			case ABORTED:
			case ERROR:
				break;
			default:
				m_firstButton.setEnabled(false);
				m_leftButton.setEnabled(false);
				m_rightButton.setEnabled(false);
				m_lastButton.setEnabled(false);
				return;
			}
		}
		
		if (proxy.isOverriding())
		{
			m_firstButton.setEnabled( currentStack > 0 );
			m_leftButton.setEnabled( size>1 && currentOV>0);
			m_rightButton.setEnabled(currentOV<size-1);
			m_lastButton.setEnabled(currentOV<size-1);
		}
		else
		{
			m_firstButton.setEnabled( currentStack > 0 );
			m_leftButton.setEnabled( size>1 && currentSD>0);
			m_rightButton.setEnabled(currentSD<size-1);
			m_lastButton.setEnabled(currentSD<size-1);
		}
		//m_navButton.setEnabled(m_leftButton.isEnabled() || m_rightButton.isEnabled());
	}

}
