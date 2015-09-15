///////////////////////////////////////////////////////////////////////////////
//
// PACKAGE   : com.astra.ses.spell.gui.dialogs.controls
// 
// FILE      : DictVariablesColumnComparator.java
//
// DATE      : 2008-11-21 08:55
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
package com.astra.ses.spell.gui.dialogs.controls;

import java.util.Arrays;

import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerComparator;
import org.eclipse.swt.SWT;

import com.astra.ses.spell.gui.core.model.types.DataVariable;

/*******************************************************************************
 * @brief
 * @date 
 ******************************************************************************/
public class DictVariablesColumnComparator extends ViewerComparator 
{
	private enum Direction
	{
		ASCENDING,
		DESCENDING
	};
	
	private Direction m_direction;
	private int m_propertyIndex;
	private boolean m_simple;
	
	public DictVariablesColumnComparator( boolean simple )
	{
		m_propertyIndex = 0;
		m_direction = Direction.DESCENDING;
		m_simple = simple;
	}
	
	public int getDirection()
	{
		switch(m_direction)
		{
		case ASCENDING:
			return SWT.UP;
		case DESCENDING:
			default:
			return SWT.DOWN;
		}
	}
	
	public void setColumn( int column )
	{
		if (column == m_propertyIndex)
		{
			if (m_direction.equals(Direction.ASCENDING))
			{
				m_direction = Direction.DESCENDING;
			}
			else if (m_direction.equals(Direction.DESCENDING)) 
			{
				m_direction = Direction.ASCENDING;
			}
		}
		else
		{
			m_propertyIndex = column;
			m_direction = Direction.DESCENDING;
		}
	}
	
	@Override
	public int compare( Viewer viewer, Object e1, Object e2 )
	{
		DataVariable v1 = (DataVariable) e1;
		DataVariable v2 = (DataVariable) e2;
		int rc = 0;
		
		if (m_simple)
		{
			DictVariablesSimpleTableItems idx = DictVariablesSimpleTableItems.index(m_propertyIndex);
			switch(idx)
			{
			case NAME:
				rc = v1.getName().compareTo(v2.getName());
				break;
			case VALUE:
				rc = v1.getValue().compareTo(v2.getValue());
				break;
			default:
				rc = 0;
			}
		}
		else
		{
			DictVariablesTableItems idx = DictVariablesTableItems.index(m_propertyIndex);
			switch(idx)
			{
			case CONFIRM:
				if (v1.getConfirmGet()==null) 
				{
					rc = -1;
				}
				else if (v2.getConfirmGet() == null)
				{
					rc = 1;
				}
				else
				{
					rc = v1.getConfirmGet().compareTo(v2.getConfirmGet());
				}
				break;
			case FORMAT:
				if (v1.getFormat()==null) 
				{
					rc = -1;
				}
				else if (v2.getFormat() == null)
				{
					rc = 1;
				}
				else
				{
					rc = v1.getFormat().compareTo(v2.getFormat());
				}
				break;
			case NAME:
				if (v1.getName()==null) 
				{
					rc = -1;
				}
				else if (v2.getName() == null)
				{
					rc = 1;
				}
				else
				{
					rc = v1.getName().compareTo(v2.getName());
				}
				break;
			case RANGE_EXPECTED:
				String[] a1 = null;
				String[] a2 = null;
				if (v1.getExpected() == null)
				{
					if (v1.getRange() != null )
					{
						a1 = v1.getRange();
					}
				}
				else
				{
					a1 = v1.getExpected();
				}
				if (v2.getExpected() == null)
				{
					if (v2.getRange() != null )
					{
						a2 = v2.getRange();
					}
				}
				else
				{
					a2 = v2.getExpected();
				}

				if ( (a1 != null) && (a2 != null))
				{
					if (Arrays.equals(a1, a2))
					{
						rc = 0;
					}
					else if (a1.length == a2.length)
					{
						int sindex = 0;
						rc = 0;
						for(String s : a1)
						{
							int cp = s.compareTo(a2[sindex]);
							if (cp != 0)
							{
								rc = cp;
								break;
							}
							sindex++;
						}
					}
					else if (a1.length < a2.length)
					{
						return -1;
					}
					else
					{
						return 1;
					}
				}
				break;
			case TYPE:
				if (v1.getType()==null) 
				{
					rc = -1;
				}
				else if (v2.getType() == null)
				{
					rc = 1;
				}
				else
				{
					rc = v1.getType().compareTo(v2.getType());
				}
				break;
			case VALUE:
				if (v1.getValue()==null) 
				{
					rc = -1;
				}
				else if (v2.getValue() == null)
				{
					rc = 1;
				}
				else
				{
					rc = v1.getValue().compareTo(v2.getValue());
				}
				break;
			default:
				rc = 0;
			};
		}
		if (m_direction.equals(Direction.DESCENDING))
		{
			rc = -rc;
		}
		return rc;
	}
}
