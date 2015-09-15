///////////////////////////////////////////////////////////////////////////////
//
// PACKAGE   : com.astra.ses.spell.gui.presentation.code.controls
// 
// FILE      : CodeViewerColumn.java
//
// DATE      : 2008-11-24 08:34
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
package com.astra.ses.spell.gui.presentation.code.controls;

import org.eclipse.swt.SWT;

/******************************************************************************
 * 
 * CodeViewerColumns determines the way the columns will be rendered by default
 * 
 *****************************************************************************/
public enum CodeViewerColumn
{

	BREAKPOINT(" ", 0.02, 20, SWT.CENTER, false), 
	LINE_NO("#", 0.04, 30, SWT.RIGHT, true), 
	CODE("Code", 0.62, 500, SWT.LEFT, true), 
	DATA("Data", 0.1, 150, SWT.CENTER, true), 
	RESULT("Result", 0.1, 200, SWT.CENTER, true); 

	/** Column's Name */
	private String	m_name;
	/** Column's width ratio */
	private double	m_widthRatio;
	/** Column's alignment */
	private int	    m_alignment;
	/** Resizable flag */
	private boolean	m_resizable;
	/** Initial size */
	private int	    m_initialWidth;

	/***************************************************************************
	 * Private constructor
	 * 
	 * @param name
	 * @param widthRatio
	 **************************************************************************/
	private CodeViewerColumn(String name, double widthRatio, int initialWidth,
	        int alignment, boolean resizable)
	{
		m_name = name;
		m_widthRatio = widthRatio;
		m_alignment = alignment;
		m_resizable = resizable;
		m_initialWidth = initialWidth;
	}

	/***************************************************************************
	 * Get this column's name
	 * 
	 * @return
	 **************************************************************************/
	public String getName()
	{
		return m_name;
	}

	/***************************************************************************
	 * Get this column's width ratio
	 * 
	 * @return
	 **************************************************************************/
	public double getWidthRatio()
	{
		return m_widthRatio;
	}

	/***************************************************************************
	 * Get column content alignment
	 * 
	 * @return
	 **************************************************************************/
	public int getAlignment()
	{
		return m_alignment;
	}

	/***************************************************************************
	 * Get initial width
	 * 
	 * @return
	 **************************************************************************/
	public int getInitialWidth()
	{
		return m_initialWidth;
	}

	/**************************************************************************
	 * Check if the column may be resized
	 * 
	 * @return
	 *************************************************************************/
	public boolean isResizable()
	{
		return m_resizable;
	}
}
