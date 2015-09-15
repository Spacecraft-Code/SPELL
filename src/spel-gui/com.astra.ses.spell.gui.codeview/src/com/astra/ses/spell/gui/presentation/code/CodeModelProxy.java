///////////////////////////////////////////////////////////////////////////////
//
// PACKAGE   : com.astra.ses.spell.gui.presentation.code
// 
// FILE      : CodeModelProxy.java
//
// DATE      : Jun 4, 2013
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
package com.astra.ses.spell.gui.presentation.code;

import java.util.List;

import com.astra.ses.spell.gui.procs.interfaces.model.ICodeLine;
import com.astra.ses.spell.gui.procs.interfaces.model.ICodeModel;
import com.astra.ses.spell.gui.procs.interfaces.model.IExecutionStatusManager;

public class CodeModelProxy
{
	private IExecutionStatusManager m_model;
	private int m_displayOverride;

	/***************************************************************************
	 * 
	 **************************************************************************/
	public CodeModelProxy(IExecutionStatusManager mgr)
	{
		m_model = mgr;
		m_displayOverride = -1;
	}

	/***************************************************************************
	 * 
	 **************************************************************************/
	public List<String> getAvailableCodes()
	{
		return m_model.getStackCodeNames();
	}

	/***************************************************************************
	 * 
	 **************************************************************************/
	public List<String> getAvailableFunctions()
	{
		return m_model.getStackFunctions();
	}

	/***************************************************************************
	 * 
	 **************************************************************************/
	public String getCurrentCodeName()
	{
		if (m_displayOverride != -1)
		{
			return m_model.getStackCodeNames().get(m_displayOverride);
		}
		else
		{
			return m_model.getCurrentCode();
		}
	}

	/***************************************************************************
	 * 
	 **************************************************************************/
	public int getCurrentLineNo()
	{
		if (isOverriding())
		{
			if (m_model.getStackLines().size()>m_displayOverride)
			{
				return m_model.getStackLines().get(m_displayOverride);
			}
			else
			{
				return m_model.getCurrentLineNo();
			}
		}
		else
		{
			return m_model.getCurrentLineNo();
		}
	}

	/***************************************************************************
	 * 
	 **************************************************************************/
	public ICodeModel getCurrentCode()
	{
		if (m_displayOverride != -1)
		{
			return m_model.getCodeModel(getCurrentCodeName());
		}
		return m_model.getCodeModel(m_model.getCurrentCode());
	}

	/***************************************************************************
	 * 
	 **************************************************************************/
	public String getCurrentFunction()
	{
		if (m_displayOverride != -1)
		{
			return m_model.getStackFunctions().get(m_displayOverride);
		}
		return m_model.getCurrentFunction();
	}

	/***************************************************************************
	 * 
	 **************************************************************************/
	public ICodeLine getLine(int lineNo)
	{
		ICodeModel code = getCurrentCode();
		if (code != null)
			return getCurrentCode().getLine(lineNo);
		return null;
	}

	/***************************************************************************
	 * 
	 **************************************************************************/
	public boolean stackFirst()
	{
		if (m_model.getStackCodeNames().size() > 1)
		{
			if (setDisplayOverride(0))
			{
				m_model.stackTo(0);
				return true;
			} // if setDisplayOverride
		} // stack size > 1

		return false;
	}

	/***************************************************************************
	 * 
	 **************************************************************************/
	public boolean stackTo(int pos)
	{
		if (m_model.getStackCodeNames().size() > 1)
		{
			if (setDisplayOverride(pos))
			{
				m_model.stackTo(pos);
				return true;
			} // if setDisplayOverride
		} // stack size > 1

		return false;
	}

	/***************************************************************************
	 * 
	 **************************************************************************/
	public boolean stackTop()
	{
		if (isOverriding())
		{
			resetDisplayOverride();
			m_model.stackTop();
			return true;

		}
		else
		{
			return stackTo(m_model.getStackCodeNames().size() - 1);

		}
	}

	/***************************************************************************
	 * 
	 **************************************************************************/
	public boolean stackDown()
	{
		if (isOverriding())
		{
			int current = getDisplayOverride();
			current--;
			if (setDisplayOverride(current))
			{
				m_model.stackDown();
				return true;
			}
		}
		else
		{
			int current = getViewDepth();
			current--;
			if (setDisplayOverride(current))
			{
				m_model.stackDown();
				return true;
			}
		}
		return false;
	}

	/***************************************************************************
	 * 
	 **************************************************************************/
	public boolean stackUp()
	{
		if (isOverriding())
		{
			int current = getDisplayOverride();
			current++;
			if (setDisplayOverride(current))
			{
				m_model.stackUp();
				return true;
			}
		}
		else
		{
			int current = getViewDepth();
			current++;
			if (setDisplayOverride(current))
			{
				m_model.stackUp();
				return true;
			}
		}
		return false;
	}

	/***************************************************************************
	 * 
	 **************************************************************************/
	public int getStackDepth()
	{
		return m_model.getStackDepth();
	}

	/***************************************************************************
	 * 
	 **************************************************************************/
	public int getViewDepth()
	{
		return m_model.getViewDepth();
	}

	/***************************************************************************
	 * 
	 **************************************************************************/
	public boolean isOverriding()
	{
		return (m_displayOverride != -1);
	}

	/***************************************************************************
	 * 
	 **************************************************************************/
	public int getDisplayOverride()
	{
		return m_displayOverride;
	}

	/***************************************************************************
	 * 
	 **************************************************************************/
	public void resetDisplayOverride()
	{
		m_displayOverride = -1;
	}

	/***************************************************************************
	 * 
	 **************************************************************************/
	private boolean setDisplayOverride(int index)
	{
		if (0 <= index && index < m_model.getStackCodeNames().size())
		{
			m_displayOverride = index;
			return true;
		}
		return false;
	}
}
