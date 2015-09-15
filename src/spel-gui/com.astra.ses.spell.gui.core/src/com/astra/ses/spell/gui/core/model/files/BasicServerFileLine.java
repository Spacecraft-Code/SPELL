///////////////////////////////////////////////////////////////////////////////
//
// PACKAGE   : com.astra.ses.spell.gui.core.model.files
// 
// FILE      : BasicServerFileLine.java
//
// DATE      : 2008-11-21 08:58
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
package com.astra.ses.spell.gui.core.model.files;

/******************************************************************************
 * Abstract class providing common operations and data for all server file
 * lines.
 *****************************************************************************/
public class BasicServerFileLine implements IServerFileLine
{
	private String	 m_source;
	private String[]	m_elements;

	/**************************************************************************
	 * Constructor
	 * 
	 * @param source
	 *************************************************************************/
	public BasicServerFileLine(String source)
	{
		m_source = source;
		m_elements = parseElements();
	}

	/**************************************************************************
	 * 
	 *************************************************************************/
	protected String[] parseElements()
	{
		return m_source.split("\t");
	}
	
	/**************************************************************************
	 * 
	 *************************************************************************/
	protected String getSource()
	{
		return m_source;
	}
	
	/**************************************************************************
	 * Convert the line to string
	 * 
	 * @return The line as a string
	 *************************************************************************/
	@Override
	public String toString()
	{
		return m_source;
	}

	/**************************************************************************
	 * Get the number of elements in the line
	 * 
	 * @return the number of elements
	 *************************************************************************/
	@Override
	public int getNumElements()
	{
		return m_elements.length;
	}

	/**************************************************************************
	 * Get element at position nth
	 * 
	 * @param index
	 *            the element index
	 * @return the element or empty string
	 *************************************************************************/
	@Override
	public String getElement(int index)
	{
		if (m_elements.length > index) { return m_elements[index]; }
		return "";
	}

	/**************************************************************************
	 * Set the element at index nth
	 * 
	 * @param index
	 *            the element index
	 * @param value
	 *            the element value
	 *************************************************************************/
	@Override
	public void setElement(int index, String value)
	{
		if (m_elements.length > index)
		{
			m_elements[index] = value;
		}
		else
		{
			throw new RuntimeException("Unable to set element " + index
			        + ", only " + m_elements.length + " elements available");
		}
	}

}
