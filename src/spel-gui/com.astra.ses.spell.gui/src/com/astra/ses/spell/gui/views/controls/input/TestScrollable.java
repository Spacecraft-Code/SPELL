package com.astra.ses.spell.gui.views.controls.input;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

public class TestScrollable
{
	public static void main(String[] args)
	{
		Display display = new Display();
		Shell shell = new Shell(display);
		shell.setLayout(new GridLayout(1,true));
		shell.setSize(600, 300);

		// this button is always 400 x 400. Scrollbars appear if the window is
		// resized to be
		// too small to show part of the button
		ScrolledComposite c1 = new ScrolledComposite(shell, SWT.BORDER | SWT.H_SCROLL | SWT.V_SCROLL);
		c1.setLayoutData( new GridData( GridData.FILL_BOTH ));
		
		Composite container = new Composite(c1, SWT.NONE);
		container.setLayout( new GridLayout(1,true) );
		container.setBackground(display.getSystemColor(SWT.COLOR_YELLOW));

		Text text = new Text(container, SWT.BORDER | SWT.WRAP );
		text.setText("Lorem ipsum dolor sit amet, consectetur adipisicing elit, sed do eiusmod tempor incididunt\n ut labore et dolore magna aliqua. Ut enim ad minim veniam, quis nostrud exercitation \n ullamco laboris nisi ut aliquip ex ea commodo consequat. \nDuis aute irure dolor in reprehenderit in voluptate velit esse cillum \ndolore eu fugiat nulla pariatur. Excepteur sint occaecat cupidatat non proident,\n sunt in culpa qui officia deserunt mollit anim id est laborum.");
		text.setLayoutData( new GridData( GridData.FILL_HORIZONTAL ));
		
		for(int i=0; i<20; i++)
		{
			Button b = new Button(container, SWT.RADIO);
			b.setText("This is the option with text " + i );
			b.setLayoutData( new GridData( GridData.FILL_HORIZONTAL) );
		}

		container.pack();

		c1.setContent(container);

		shell.open();
		while (!shell.isDisposed())
		{
			if (!display.readAndDispatch())
				display.sleep();
		}
		display.dispose();
	}
}
