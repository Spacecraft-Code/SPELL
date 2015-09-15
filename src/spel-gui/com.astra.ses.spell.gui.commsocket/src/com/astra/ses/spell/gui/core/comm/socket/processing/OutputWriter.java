///////////////////////////////////////////////////////////////////////////////
//
// PACKAGE   : com.astra.ses.spell.gui.core.comm.socket.processing
// 
// FILE      : OutputWriter.java
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

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import com.astra.ses.spell.gui.core.comm.messages.MessageException;
import com.astra.ses.spell.gui.core.comm.messages.SPELLmessage;

public class OutputWriter
{
	/** Holds the output stream for sending messages */
	private DataOutputStream	m_out;
	/** Holds the client key */
	private String	         m_key;
	/** Lock for outputs */
	private Lock	         m_lock;

	/***********************************************************************
	 * Constructor
	 **********************************************************************/
	public OutputWriter(DataOutputStream out, String key)
	{
		m_out = out;
		m_key = key;
		m_lock = new ReentrantLock();
	}

	/***********************************************************************
	 * Send a given message to peer
	 **********************************************************************/
	public void send(SPELLmessage msg)
	{
		if (m_out == null) return;
		m_lock.lock();
		try
		{
			msg.setKey(m_key);
			
			ByteArrayOutputStream buf = new ByteArrayOutputStream();
			buf.write('\1');
			for( String key : msg.getKeys())
			{
				String value = msg.get(key);
				buf.write( encodeLength2(key.length()), 0, 2);
				buf.write(key.getBytes());

				int length = value.length();
				byte[] bytes = new byte[4];
				bytes[0] = (byte) ((length & 0xFF000000) >> 24);
				bytes[1] = (byte) ((length & 0x00FF0000) >> 16);
				bytes[2] = (byte) ((length & 0x0000FF00) >> 8);
				bytes[3] = (byte) (length  & 0x000000FF);
				buf.write( bytes, 0, 4 );
				
				buf.write(value.getBytes());
			}

			byte[] encodedLength = encodeLength4(buf.size());
			for (byte b : encodedLength) m_out.writeByte(b);
			
			for (byte b : buf.toByteArray() )
			{
				m_out.writeByte(b);
			}
			
			m_out.flush();
		}
		catch (MessageException ex)
		{
			ex.printStackTrace();
		}
		catch (IOException ex)
		{
			ex.printStackTrace();
		}
		finally
		{
			m_lock.unlock();
		}
	}

	/***********************************************************************
	 * Encode length
	 **********************************************************************/
	private byte[] encodeLength4(int length)
	{
		byte[] bytes = new byte[4];

		for (int i = 3; i >= 0; i--)
		{
			bytes[i] = (byte) (length & 0xff);
			length >>= 8;
		}
		return bytes;
	}

	/***********************************************************************
	 * Encode length
	 **********************************************************************/
	private byte[] encodeLength2(int length)
	{
		byte[] bytes = new byte[2];

		bytes[1] = (byte) (length & 0xff);
		length >>= 8;
		bytes[0] = (byte) (length & 0xff);

		return bytes;
	}
}
