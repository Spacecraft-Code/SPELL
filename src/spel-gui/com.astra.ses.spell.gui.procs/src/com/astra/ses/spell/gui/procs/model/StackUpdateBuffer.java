///////////////////////////////////////////////////////////////////////////////
//
// PACKAGE   : com.astra.ses.spell.gui.procs.model
// 
// FILE      : StackUpdateBuffer.java
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
package com.astra.ses.spell.gui.procs.model;

import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.ReentrantLock;

import com.astra.ses.spell.gui.procs.interfaces.model.ICodeLine;

public class StackUpdateBuffer extends Thread
{
	private Set<ICodeLine> m_toUpdate;
	private AtomicBoolean m_working;
	private ExecutionStatusManager m_manager;
	private ReentrantLock m_addLock;
	
	/***************************************************************************
	 * 
	 **************************************************************************/
	public StackUpdateBuffer( ExecutionStatusManager manager )
	{
		super("ExecutionStatusManager::StackUpdateBuffer");
		m_toUpdate = new LinkedHashSet<ICodeLine>();
		m_working = new AtomicBoolean(true);
		m_manager = manager;
		m_addLock = new ReentrantLock();
	}
	
	/***************************************************************************
	 * 
	 **************************************************************************/
	public void run()
	{
		while(m_working.get())
		{
			try
            {
				Thread.sleep(250);
				List<ICodeLine> list = getAll();
	            if (list != null && !list.isEmpty())
	            {
	            	m_manager.notifyLinesChanged(list);
	            }
            }
            catch (InterruptedException e){};
		}
	}
	
	/***************************************************************************
	 * 
	 **************************************************************************/
	private List<ICodeLine> getAll()
	{
		List<ICodeLine> list = new LinkedList<ICodeLine>();
		m_addLock.lock();
		try
        {
			list.addAll(m_toUpdate);
			m_toUpdate.clear();
        }
        catch (Exception e)
        {
	        e.printStackTrace();
        }
		finally
		{
			m_addLock.unlock();
		}
        return list;
	}
	
	/***************************************************************************
	 * 
	 **************************************************************************/
	public void scheduleUpdate( ICodeLine line )
	{
		m_addLock.lock();
		try
        {
			m_toUpdate.add(line);
        }
        catch (Exception e)
        {
	        e.printStackTrace();
        }
		finally
		{
			m_addLock.unlock();
		}
	}

	/***************************************************************************
	 * 
	 **************************************************************************/
	public void scheduleUpdate( List<ICodeLine> lines )
	{
		m_addLock.lock();
		try
        {
			m_toUpdate.addAll(lines);
        }
        catch (Exception e)
        {
	        e.printStackTrace();
        }
		finally
		{
			m_addLock.unlock();
		}
	}

	/***************************************************************************
	 * 
	 **************************************************************************/
	public void stopUpdate()
	{
		m_working.set(false);
		interrupt();
	}
}
