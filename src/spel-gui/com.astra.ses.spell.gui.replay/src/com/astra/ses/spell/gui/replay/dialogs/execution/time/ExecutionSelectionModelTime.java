///////////////////////////////////////////////////////////////////////////////
//
// PACKAGE   : com.astra.ses.spell.gui.replay.dialogs.execution
// 
// FILE      : ExecutionSelectionModelName.java
//
// DATE      : Jun 21, 2013
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
package com.astra.ses.spell.gui.replay.dialogs.execution.time;

import java.text.ParseException;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Map;

import com.astra.ses.spell.gui.core.model.types.Level;
import com.astra.ses.spell.gui.core.utils.Logger;
import com.astra.ses.spell.gui.replay.dialogs.execution.ExecutionSelectionLeafNode;
import com.astra.ses.spell.gui.replay.dialogs.execution.ExecutionSelectionModelBase;
import com.astra.ses.spell.gui.replay.dialogs.execution.ExecutionSelectionNode;
import com.astra.ses.spell.gui.replay.dialogs.execution.NodeType;

public class ExecutionSelectionModelTime extends ExecutionSelectionModelBase
{
	/***************************************************************************
	 * 
	 **************************************************************************/
	public ExecutionSelectionModelTime( String asrunPath )
	{
		super(asrunPath);
	}
	
	/***************************************************************************
	 * 
	 **************************************************************************/
	@Override
	public void load( List<String> items, Map<String,String> procNames )
	{
		if (m_root != null) return;
		m_root = new ExecutionSelectionNode("Executions",null, NodeType.ROOT);
		
		if (items != null && !items.isEmpty())
		{
			
			for(String item : items)
			{
				// Extract the date
				int idx = item.indexOf("_Executor");
				String dateStr = item.substring(0,idx);
				Date date;
                try
                {
	                date = s_df.parse(dateStr);
                }
                catch (ParseException e)
                {
	                e.printStackTrace();
	                continue;
                }
                
				// Find the year
				idx = item.indexOf("-");
				String yearStr = item.substring(0,idx);
				int year = Integer.parseInt(yearStr);
				// Find the month
				int idx2 = item.indexOf("-",idx+1);
				String monthStr = item.substring(idx+1,idx2);
				int month = Integer.parseInt(monthStr)-1;
				// Find the day
				int idx3 = item.indexOf("_");
				String dayStr = item.substring(idx3-2,idx3);
				int day = Integer.parseInt(dayStr);

				ExecutionSelectionNode yearNode = null; 
				for(ExecutionSelectionNode node : m_root.getChildren())
				{
					if (node.getYear()==year)
					{
						yearNode = node;
						break;
					}
				}
				if (yearNode == null)
				{
					yearNode = new ExecutionSelectionNode( yearStr, createYear(year).getTime(), NodeType.YEAR_GROUP );
					m_root.addChild(yearNode);
				}
				
				ExecutionSelectionNode monthNode = null; 
				for(ExecutionSelectionNode node : yearNode.getChildren())
				{
					if (node.getMonth()==month)
					{
						monthNode = node;
						break;
					}
				}
				if (monthNode == null)
				{
					Calendar c = createMonth(year,month);
					String mstr = s_df2.format(c.getTime());
					monthNode = new ExecutionSelectionNode( mstr, c.getTime(), NodeType.MONTH_GROUP );
					yearNode.addChild(monthNode);
				}

				ExecutionSelectionNode dayNode = null; 
				for(ExecutionSelectionNode node : monthNode.getChildren())
				{
					if (node.getDay()==day)
					{
						dayNode = node;
						break;
					}
				}
				if (dayNode == null)
				{
					Calendar c = createDay(year,month,day);
					String dstr = s_df3.format(c.getTime());
					dayNode = new ExecutionSelectionNode( dstr, c.getTime(), NodeType.DAY_GROUP );
					monthNode.addChild(dayNode);
				}

				idx = item.indexOf("_Executor_") + "_Executor_".length();
				String instanceId = item.substring(idx, item.length()-6);
				instanceId = instanceId.replace("__", "/");
				String asrun = m_asrunPath + "/" + item;
				idx = instanceId.indexOf("#");
				if (idx == -1)
				{
					Logger.error("ASRUN file '" + item + "' cannot be processed: no instance number", Level.PROC, this);
				}
				else
				{
					String procId = instanceId.substring(0,idx);
					String instanceNum = instanceId.substring(idx);
					String procName = procId;
					if (procNames.containsKey(procId))
					{
						procName = procNames.get(procId);
					}
					else
					{
						procName = s_pmgr.getProcedureName(procId);
						procNames.put(procId,procName);
					}
					ExecutionSelectionLeafNode node = new ExecutionSelectionLeafNode(procName, procId, instanceNum, date, asrun);
					dayNode.addChild(node);
				}
			}
		}
	}
}
