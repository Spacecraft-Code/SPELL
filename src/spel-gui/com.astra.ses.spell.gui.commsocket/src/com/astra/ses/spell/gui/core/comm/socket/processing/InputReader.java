///////////////////////////////////////////////////////////////////////////////
//
// PACKAGE   : com.astra.ses.spell.gui.core.comm.socket.processing
// 
// FILE      : InputReader.java
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
package com.astra.ses.spell.gui.core.comm.socket.processing;

import java.io.DataInputStream;
import java.io.EOFException;
import java.net.SocketException;
import java.util.ArrayList;

import com.astra.ses.spell.gui.core.comm.messages.SPELLmessage;
import com.astra.ses.spell.gui.core.comm.messages.SPELLmessageDisplay;
import com.astra.ses.spell.gui.core.comm.messages.SPELLmessageEOC;
import com.astra.ses.spell.gui.core.comm.messages.SPELLmessageError;
import com.astra.ses.spell.gui.core.comm.messages.SPELLmessageFactory;
import com.astra.ses.spell.gui.core.comm.messages.SPELLmessageNotify;
import com.astra.ses.spell.gui.core.comm.messages.SPELLmessageNotifyAsync;
import com.astra.ses.spell.gui.core.comm.messages.SPELLmessageOneway;
import com.astra.ses.spell.gui.core.comm.messages.SPELLmessagePrompt;
import com.astra.ses.spell.gui.core.comm.messages.SPELLmessageRequest;
import com.astra.ses.spell.gui.core.comm.messages.SPELLmessageResponse;
import com.astra.ses.spell.gui.core.comm.socket.ifc.CommInterfaceSocket;
import com.astra.ses.spell.gui.core.comm.socket.ifc.CommInterfaceSocketConstants;
import com.astra.ses.spell.gui.core.model.types.Level;
import com.astra.ses.spell.gui.core.utils.Logger;

/***************************************************************************
 * @brief Processing thread in charge of handling socket incoming data
 **************************************************************************/
public class InputReader extends Thread
{
	/** Buffer for building message data structures */
	private ArrayList<Byte> m_inputData;
	/** Holds the message length */
	private int m_length;
	/** Holds the input stream for receiving messages */
	private DataInputStream m_in;
	/** Reference to the interface */
	private CommInterfaceSocket m_interface;
	/** True if the message input loop shall keep working */
	private boolean m_working;

	/***********************************************************************
	 * Constructor
	 **********************************************************************/
	public InputReader(CommInterfaceSocket ifc, DataInputStream in)
	{
		super();
		m_interface = ifc;
		m_in = in;
		m_inputData = new ArrayList<Byte>();
		m_length = 0;
		m_working = true;
	}

	/***********************************************************************
	 * Incoming messages processing loop
	 **********************************************************************/
	public void run()
	{
		while (getWorking())
		{
			try
			{
				while (m_in != null)
				{
					byte b = m_in.readByte();
					m_inputData.add(b);
					while (m_inputData.size() >= CommInterfaceSocketConstants.PFX_LEN && getWorking())
					{
						if (m_length == 0)
							decodeLength();
						if (m_inputData.size() < m_length + CommInterfaceSocketConstants.PFX_LEN)
							break;

						byte[] packet = getPacket();
						dispatch(packet);
					}
				}
				if (m_in != null && !(m_in.available() > 0) && getWorking())
				{
					m_interface.commFailure("Lost connection with server", "");
				}
			}
			catch (EOFException eof)
			{
				if (getWorking())
				{
					Logger.warning("Connection terminated", Level.COMM, this);
					setWorking(false);
					m_interface.commFailure("Lost connection with listener", "Connection terminated by peer");
				}
				else
				{
					Logger.info("Connection terminated", Level.COMM, this);
				}
				return;
			}
			catch (SocketException se)
			{
				// Id is different in Linux/Windows
				if (!se.getLocalizedMessage().equals("Socket closed") && !se.getLocalizedMessage().equals("socket closed"))
				{
					System.err.println(se.getLocalizedMessage());
					m_interface.commFailure(se.getLocalizedMessage(), "");
				}
				else
				{
					Logger.info("Connection terminated, socket closed", Level.COMM, this);
				}
			}
			catch (Exception e)
			{
				e.printStackTrace();
				m_interface.commFailure("Unknown COMM error.", e.getLocalizedMessage());
			}
		}
	}

	/***************************************************************************
	 * Set the working status of the incoming loop
	 **************************************************************************/
	public synchronized void setWorking(boolean w)
	{
		m_working = w;
	}

	/***************************************************************************
	 * Check the status of the incoming loop
	 **************************************************************************/
	public synchronized boolean getWorking()
	{
		return m_working;
	}

	/***********************************************************************
	 * Get packet
	 **********************************************************************/
	private byte[] getPacket()
	{
		for (int c = 0; c < CommInterfaceSocketConstants.PFX_LEN; c++)
			m_inputData.remove(0);
		byte[] bytes = new byte[m_inputData.size()];
		int count = 0;
		for (Byte b : m_inputData)
		{
			bytes[count] = b.byteValue();
			count++;
		}
		m_inputData.clear();
		m_length = 0;
		return bytes;
	}

	/***********************************************************************
	 * Decode length
	 **********************************************************************/
	private void decodeLength()
	{
		byte[] bytes = new byte[4];
		int count = 0;
		for (Byte b : m_inputData)
		{
			bytes[count] = b.byteValue();
			count++;
			if (count == 4)
				break;
		}
		int length = 0;
		for (int c = 0; c < 4; c++)
		{
			length = (length << 8) + (bytes[c] < 0 ? bytes[c] + 256 : bytes[c]);
		}
		m_length = length;
	}

	/***********************************************************************
	 * Dispatch the incoming message as appropiate
	 **********************************************************************/
	private void dispatch(byte[] packet)
	{
		try
		{
			SPELLmessage smsg = SPELLmessageFactory.createMessage(packet);
			if (smsg != null)
			{
				String msgId = smsg.getSender() + "-" + smsg.getReceiver();
				if (smsg instanceof SPELLmessageResponse || smsg instanceof SPELLmessageError)
				{
					msgId += ":" + smsg.getSequence();
					m_interface.incomingResponse(msgId, smsg);
				}
				else if (smsg instanceof SPELLmessageNotify || smsg instanceof SPELLmessagePrompt)
				{
					m_interface.incomingMessage(msgId, smsg);
				}
				else if (smsg instanceof SPELLmessageRequest)
				{
					msgId += ":" + smsg.getSequence();
					m_interface.incomingRequest(msgId, smsg);
				}
				else if (smsg instanceof SPELLmessageNotifyAsync || smsg instanceof SPELLmessageDisplay || smsg instanceof SPELLmessageOneway)
				{
					m_interface.incomingMessage(msgId, smsg);
				}
				else if (smsg instanceof SPELLmessageEOC)
				{
					Logger.warning("Received EOC from server", Level.COMM, this);
					setWorking(false);
					m_interface.commFailure("Connection lost", "Server sent EOC");
				}
				else
				{
					String msg = "CANNOT PROCESS MSG TYPE: " + packet;
					System.err.println(msg);
					Logger.error(msg, Level.COMM, this);
				}
			}
		}
		catch (Exception ex)
		{
			System.err.println(packet);
			ex.printStackTrace();
			Logger.error(ex.getLocalizedMessage(), Level.COMM, this);
		}
	}
}
