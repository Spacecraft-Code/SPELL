///////////////////////////////////////////////////////////////////////////////
//
// PACKAGE   : com.astra.ses.spell.gui.core.comm.socket.utils
// 
// FILE      : Mailbox.java
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
package com.astra.ses.spell.gui.core.comm.socket.utils;

import java.util.HashMap;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import com.astra.ses.spell.gui.core.comm.messages.SPELLmessage;
import com.astra.ses.spell.gui.core.model.types.Level;
import com.astra.ses.spell.gui.core.utils.Logger;

public class Mailbox
{
	private Lock	m_lock	= new ReentrantLock();

	private class Queue
	{
		private ArrayBlockingQueue<SPELLmessage>	m_queue;

		public Queue()
		{
			m_queue = new ArrayBlockingQueue<SPELLmessage>(1);
		}

		public SPELLmessage get(long timeout)
		{
			SPELLmessage msg = null;
			long startTime = System.currentTimeMillis();
			while (msg == null)
			{
				msg = m_queue.poll();
				if (msg == null)
				{
					Thread.yield();
					if (System.currentTimeMillis() > (startTime + timeout))
					{
						break;
					}
				}
			}
			return msg;
		}

		public void put(SPELLmessage msg)
		{
			try
			{
				m_queue.put(msg);
			}
			catch (InterruptedException e)
			{
			
			}
		}
	}

	private HashMap<String, Queue>	m_responseQueue;

	public Mailbox()
	{
		m_responseQueue = new HashMap<String, Queue>();
	}

	public void prepare(String id)
	{
		m_lock.lock();
		try
		{
			if (!m_responseQueue.containsKey(id))
			{
				m_responseQueue.put(id, new Queue());
			}
		}
		finally
		{
			m_lock.unlock();
		}
	}

	public void place(String id, SPELLmessage response)
	{
		Queue q = getQueue(id);
		if (q != null)
		{
			q.put(response);
		}
		else
		{
			Logger.error("Discarded outdated response: " + id, Level.COMM, this);
		}
	}

	public SPELLmessage retrieve(String id, long timeout)
	{
		Queue q = getQueue(id);
		SPELLmessage msg = q.get(timeout);
		if (msg != null)
		{
			clean(id);
		}
		else
		{
			Logger.error("Timeout on request " + id, Level.COMM, this);
		}
		return msg;
	}

	public boolean isWaitingFor(String id)
	{
		m_lock.lock();
		boolean waiting = m_responseQueue.containsKey(id);
		m_lock.unlock();
		return waiting;
	}

	private void clean(String id)
	{
		m_lock.lock();
		try
		{
			if (m_responseQueue.containsKey(id))
			{
				m_responseQueue.remove(id);
			}
		}
		finally
		{
			m_lock.unlock();
		}
	}

	private Queue getQueue(String id)
	{
		m_lock.lock();
		Queue q = null;
		try
		{
			if (m_responseQueue.containsKey(id))
			{
				q = m_responseQueue.get(id);
			}
		}
		finally
		{
			m_lock.unlock();
		}
		return q;
	}
}
