///////////////////////////////////////////////////////////////////////////////
//
// PACKAGE   : com.astra.ses.spell.gui.core.comm.messages
// 
// FILE      : SPELLmessagePrompt.java
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
package com.astra.ses.spell.gui.core.comm.messages;

import java.util.StringTokenizer;
import java.util.TreeMap;
import java.util.Vector;

import com.astra.ses.spell.gui.core.interfaces.IMessageField;
import com.astra.ses.spell.gui.core.interfaces.IMessageValue;
import com.astra.ses.spell.gui.core.model.notification.InputData;
import com.astra.ses.spell.gui.core.model.types.PromptDisplayType;
import com.astra.ses.spell.gui.core.model.types.Scope;

/*******************************************************************************
 * Represents a prompt message received from the server side. It contains the
 * code required for holding the future answer value. A prompt may be cancelled
 * or aborted due to an error. If this happens, the response message will have
 * the identifier CANCEL or UNKNOWN.
 * 
 ******************************************************************************/
public class SPELLmessagePrompt extends SPELLmessageRequest
{
	private static final String OPT_SEPARATOR = "\4";

	private InputData m_promptInput;

	/***************************************************************************
	 * Constructor.
	 * 
	 * @param data
	 *            XML data of the received SPELL prompt message
	 ***************************************************************************/
	public SPELLmessagePrompt(TreeMap<String, String> data)
	{
		super(data);
		try
		{
			String text = get(IMessageField.FIELD_TEXT);
			String procId = get(IMessageField.FIELD_PROC_ID);
			String dataType = get(IMessageField.FIELD_DATA_TYPE);
			long sequence = Long.parseLong(get(IMessageField.FIELD_MSG_SEQUENCE));
			boolean numeric = dataType.equals(IMessageValue.DATA_TYPE_NUM);
			Vector<String> options = getOptions();
			Vector<String> expected = getExpected();
			Scope scope = Scope.SYS;

			String defaultAnswer = "";
			if (hasKey(IMessageField.FIELD_DEFAULT))
			{
				defaultAnswer = get(IMessageField.FIELD_DEFAULT);
			}
			
			if (hasKey(IMessageField.FIELD_SCOPE))
			{
				scope = Scope.fromCode(get(IMessageField.FIELD_SCOPE));
			}
			if (options == null)
			{
				m_promptInput = new InputData(this, procId, text, scope, defaultAnswer, numeric, false);
			}
			else
			{
				PromptDisplayType promptDisplayType = PromptDisplayType.RADIO;
				int comboMask = new Integer(IMessageValue.DATA_TYPE_COMBO).intValue();
				int typeMask = new Integer(dataType).intValue();
				if ((typeMask & comboMask) > 0)
				{
					promptDisplayType = PromptDisplayType.COMBO;
				}
				m_promptInput = new InputData(this, procId, text, scope, options, expected, defaultAnswer, false, promptDisplayType);
			}
			m_promptInput.setSequence(sequence);
		}
		catch (MessageException e)
		{
			e.printStackTrace();
		}
	}

	/***************************************************************************
	 * Obtain the input data
	 * 
	 * @return The input data
	 **************************************************************************/
	public InputData getData()
	{
		return m_promptInput;
	}

	/***************************************************************************
	 * Obtain the list of available options, if any
	 * 
	 * @return The list of options, null if there are none
	 **************************************************************************/
	private Vector<String> getOptions()
	{
		Vector<String> options = null;
		if (!hasKey(IMessageField.FIELD_OPTIONS))
			return null;
		try
		{
			String optionsList = get(IMessageField.FIELD_OPTIONS);
			StringTokenizer tokenizer = new StringTokenizer(optionsList, OPT_SEPARATOR);
			options = new Vector<String>();
			while (tokenizer.hasMoreTokens())
			{
				String token = tokenizer.nextToken();
				options.addElement(token);
			}
		}
		catch (MessageException ex)
		{
			ex.printStackTrace();
		}
		return options;
	}

	/***************************************************************************
	 * Obtain the expected values
	 * 
	 * @return The list of expected values, if any
	 **************************************************************************/
	private Vector<String> getExpected()
	{
		Vector<String> expected = null;
		if (!hasKey(IMessageField.FIELD_EXPECTED))
			return null;
		try
		{
			String optionsList = get(IMessageField.FIELD_EXPECTED);
			StringTokenizer tokenizer = new StringTokenizer(optionsList, OPT_SEPARATOR);
			expected = new Vector<String>();
			while (tokenizer.hasMoreTokens())
			{
				String token = tokenizer.nextToken();
				expected.addElement(token);
			}
		}
		catch (MessageException ex)
		{
			ex.printStackTrace();
		}
		return expected;
	}

}
