///////////////////////////////////////////////////////////////////////////////
//
// PACKAGE   : com.astra.ses.spell.gui.core.model.files
// 
// FILE      : AsRunFile.java
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

import java.util.LinkedList;
import java.util.List;

import com.astra.ses.spell.gui.core.model.types.AsRunType;
import com.astra.ses.spell.gui.core.model.types.Level;
import com.astra.ses.spell.gui.core.utils.Logger;

/*******************************************************************************
 * Representation of an AsRun file
 * 
 ******************************************************************************/
public class AsRunFile extends BasicServerFile
{
	private String m_procId;
	private List<String> m_children;
	
	/***************************************************************************
	 * Constructor
	 * 
	 * @param source
	 **************************************************************************/
	public AsRunFile(String procId, String path, List<String> lines)
	{
		super(path,lines);
		m_procId = procId;
	}

	/***************************************************************************
	 * Parse the given AsRun source data
	 * 
	 * @param source
	 **************************************************************************/
	public void parse(List<String> lines)
	{
		m_children = new LinkedList<String>();
		int count = 1;
		for (String line : lines)
		{
			try
			{
				if (line.startsWith("#") || line.trim().isEmpty()) continue;
				AsRunFileLine arLine = new AsRunFileLine(getProcId(), line);
				if (arLine.getAsRunType().equals(AsRunType.CHILD))
				{
					m_children.add(arLine.getDataA());
				}
				addLine(arLine);
			}
			catch (Exception ex)
			{
				System.err.println("Unable to process asrun line: '" + line
				        + "' (" + count + ")");
				ex.printStackTrace();
				Logger.error("Unable to process asrun line: '" + line + "' ("
				        + count + "): " + ex, Level.PROC, this);
			}
			count++;
		}
	}

	/**************************************************************************
	 * Get the asrun header labels
	 *************************************************************************/
	public String[] getHeaderLabels()
	{
		String[] labels = new String[AsRunColumns.values().length];
		for (AsRunColumns ar : AsRunColumns.values())
		{
			labels[ar.ordinal()] = ar.name;
		}
		return labels;
	}

	/**************************************************************************
	 * Get the asrun header label relative widths (percentage)
	 *************************************************************************/
	public int[] getHeaderLabelsSize()
	{
		int[] sizes = new int[AsRunColumns.values().length];
		for (AsRunColumns ar : AsRunColumns.values())
		{
			sizes[ar.ordinal()] = ar.width;
		}
		return sizes;
	}
	
	/**************************************************************************
	 * Get the procedure identifier
	 *************************************************************************/
	public String getProcId()
	{
		return m_procId;
	}

	/**************************************************************************
	 * Get the children asruns if any
	 *************************************************************************/
	public List<String> getChildren()
	{
		return m_children;
	}
}
