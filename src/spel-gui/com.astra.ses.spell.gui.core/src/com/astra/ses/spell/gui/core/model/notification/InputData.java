///////////////////////////////////////////////////////////////////////////////
//
// PACKAGE   : com.astra.ses.spell.gui.core.model.notification
// 
// FILE      : InputData.java
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
package com.astra.ses.spell.gui.core.model.notification;

import java.util.Vector;

import com.astra.ses.spell.gui.core.comm.messages.SPELLmessage;
import com.astra.ses.spell.gui.core.model.types.PromptDisplayType;
import com.astra.ses.spell.gui.core.model.types.Scope;

public class InputData extends NotificationData
{
	private String m_text;
	private String m_returnValue;
	private boolean m_numericPrompt;
	private boolean m_ready;
	private boolean m_cancelled;
	private boolean m_error;
	private Vector<String> m_options;
	private Vector<String> m_expected;
	private String m_defaultAnswer;
	private PromptDisplayType m_promptDisplayType;
	private boolean m_justNotification;
	private Scope m_scope;
	private SPELLmessage m_promptMessage;

	public InputData(SPELLmessage msg, String procId, String text, Scope scope, String defaultAnswer, boolean numeric, boolean justNotification)
	{
		super(procId, "");
		m_text = text;
		m_numericPrompt = numeric;
		m_options = null;
		m_expected = null;
		m_ready = false;
		m_cancelled = false;
		m_error = false;
		m_returnValue = null;
		m_promptDisplayType = null;
		m_justNotification = justNotification;
		m_scope = scope;
		m_promptMessage = msg;
		m_defaultAnswer = defaultAnswer;
	}

	public InputData(SPELLmessage msg, String procId, String text, Scope scope, Vector<String> options, Vector<String> expected,
			String defaultAnswer, boolean justNotification, PromptDisplayType type)
	{
		super(procId, "");
		m_text = text;
		m_numericPrompt = false;
		m_options = options;
		m_expected = expected;
		m_ready = false;
		m_cancelled = false;
		m_error = false;
		m_returnValue = null;
		m_promptDisplayType = type;
		m_justNotification = justNotification;
		m_scope = scope;
		m_promptMessage = msg;
		m_defaultAnswer = defaultAnswer;
	}

	/***************************************************************************
	 * Obtain the prompt text
	 * 
	 * @return The prompt text
	 **************************************************************************/
	public String getText()
	{
		return m_text;
	}

	/***************************************************************************
	 * Obtain the prompt message
	 * 
	 * @return The prompt message
	 **************************************************************************/
	public SPELLmessage getMessage()
	{
		return m_promptMessage;
	}

	/***************************************************************************
	 * Obtain the prompt scope
	 * 
	 * @return The prompt scope
	 **************************************************************************/
	public Scope getScope()
	{
		return m_scope;
	}

	/***************************************************************************
	 * Obtain the default answer if any
	 * 
	 * @return The prompt scope
	 **************************************************************************/
	public String getDefault()
	{
		return m_defaultAnswer;
	}

	/***************************************************************************
	 * Obtain the prompt option list
	 * 
	 * @return The prompt option list
	 **************************************************************************/
	public PromptDisplayType getPromptDisplayType()
	{
		return m_promptDisplayType;
	}

	/***************************************************************************
	 * Obtain the prompt option list
	 * 
	 * @return The prompt option list
	 **************************************************************************/
	public Vector<String> getOptions()
	{
		return m_options;
	}

	/***************************************************************************
	 * Obtain the prompt expected values list
	 * 
	 * @return The prompt expected values list
	 **************************************************************************/
	public Vector<String> getExpected()
	{
		return m_expected;
	}

	/***************************************************************************
	 * Check if the prompt is a selection list
	 * 
	 * @return True if it is a selection list
	 **************************************************************************/
	public boolean isList()
	{
		return (m_options != null);
	}

	/***************************************************************************
	 * Check is this is just a descriptive prompt for a monitoring GUI, not
	 * expecting any answer
	 **************************************************************************/
	public boolean isNotification()
	{
		return (m_justNotification);
	}

	/***************************************************************************
	 * Check if the prompt is numeric
	 * 
	 * @return True if it is a numeric text prompt
	 **************************************************************************/
	public boolean isNumeric()
	{
		return (m_numericPrompt);
	}

	/***************************************************************************
	 * Mark this prompt as erroneous
	 **************************************************************************/
	public void setError()
	{
		setReturnValue("None");
		m_error = true;
		setReady();
	}

	/***************************************************************************
	 * Cancel this prompt
	 **************************************************************************/
	public void setCancel()
	{
		setReturnValue("<CANCEL>");
		m_cancelled = true;
		setReady();
	}

	/***************************************************************************
	 * Set the prompt return value
	 * 
	 * @param value
	 *            The answer from the user
	 **************************************************************************/
	public void setReturnValue(String value)
	{
		m_returnValue = value;
		setReady();
	}

	/***************************************************************************
	 * Obtain the prompt return value
	 * 
	 * @return The answer from the user
	 **************************************************************************/
	public String getReturnValue()
	{
		return m_returnValue;
	}

	/***************************************************************************
	 * Check if the prompt is cancelled
	 * 
	 * @return
	 **************************************************************************/
	public boolean isCancelled()
	{
		return m_cancelled;
	}

	/***************************************************************************
	 * Check if the prompt is in error status
	 * 
	 * @return
	 **************************************************************************/
	public boolean isError()
	{
		return m_error;
	}

	/***************************************************************************
	 * Set the prompt ready for processing after storing the return value or
	 * setting it to error/cancelled status
	 **************************************************************************/
	public synchronized void setReady()
	{
		m_ready = true;
	}

	/***************************************************************************
	 * Check if the prompt is ready for processing
	 * 
	 * @return True if there is an answer or the prompt is in error/cancelled
	 *         state
	 **************************************************************************/
	public synchronized boolean isReady()
	{
		return m_ready;
	}

}
