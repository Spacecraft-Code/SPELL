///////////////////////////////////////////////////////////////////////////////
//
// PACKAGE   : com.astra.ses.spell.gui.core.model.files
// 
// FILE      : LogFileLine.java
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

/*******************************************************************************
 * Representation of an Log file line
 * 
 ******************************************************************************/
public class LogFileLine extends BasicServerFileLine
{
	private String	m_severity;
	private String	m_timestamp;
	private String	m_message;
	private String	m_origin;

	/***********************************************************************
	 * Constructor
	 **********************************************************************/
	public LogFileLine(String source)
	{
		super(source);
		fillValues();
	}

	/***********************************************************************
	 * Remove brackets on the given element
	 **********************************************************************/
	protected String removeBrackets(int index)
	{
		if (getNumElements() > index)
		{
			String element = getElement(index).replaceAll("[\\[\\] ]", "");
			setElement(index, element);
			return getElement(index);
		}
		return "";
	}

	/***********************************************************************
	 * Fill the line fields
	 **********************************************************************/
	protected void fillValues()
	{
		m_origin = removeBrackets(LogColumns.ORIGIN.ordinal());
		m_severity = removeBrackets(LogColumns.SEVERITY.ordinal());
		m_timestamp = removeBrackets(LogColumns.TIMESTAMP.ordinal());
		m_message = getElement(LogColumns.MESSAGE.ordinal());
	}

	/**************************************************************************
	 * Get the log origin
	 *************************************************************************/
	public String getOrigin()
	{
		return m_origin;
	}

	/**************************************************************************
	 * Get the log severity
	 *************************************************************************/
	public String getSeverity()
	{
		return m_severity;
	}

	/**************************************************************************
	 * Get the log timestamp
	 *************************************************************************/
	public String getTimestamp()
	{
		return m_timestamp;
	}

	/**************************************************************************
	 * Get the log message
	 *************************************************************************/
	public String getMessage()
	{
		return m_message;
	}
}
