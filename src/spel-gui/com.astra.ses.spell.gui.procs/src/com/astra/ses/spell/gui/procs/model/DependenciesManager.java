///////////////////////////////////////////////////////////////////////////////
//
// PACKAGE   : com.astra.ses.spell.gui.procs.model
// 
// FILE      : DependenciesManager.java
//
// DATE      : Jun 11, 2013
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

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import org.eclipse.core.runtime.NullProgressMonitor;

import com.astra.ses.spell.gui.core.interfaces.ServiceManager;
import com.astra.ses.spell.gui.core.model.types.Level;
import com.astra.ses.spell.gui.core.utils.Logger;
import com.astra.ses.spell.gui.procs.interfaces.IProcedureManager;
import com.astra.ses.spell.gui.procs.interfaces.listeners.IDependenciesListener;
import com.astra.ses.spell.gui.procs.interfaces.model.Dependency;
import com.astra.ses.spell.gui.procs.interfaces.model.IDependenciesManager;
import com.astra.ses.spell.gui.procs.interfaces.model.IProcedure;
import com.astra.ses.spell.language.ParseException;
import com.astra.ses.spell.language.Parser;
import com.astra.ses.spell.language.Visitor;
import com.astra.ses.spell.language.model.ast.Call;
import com.astra.ses.spell.language.model.ast.Name;
import com.astra.ses.spell.language.model.ast.Str;
import com.astra.ses.spell.language.model.ast.exprType;
import com.astra.ses.spell.language.model.ast.keywordType;

public class DependenciesManager extends Visitor implements IDependenciesManager
{
	private List<Dependency> m_dependencies;
	private List<IDependenciesListener> m_listeners;
	private IProcedure m_model;
	private IProcedureManager m_pmgr;
	
	/**************************************************************************
	 * 
	 *************************************************************************/
	public DependenciesManager( IProcedure model )
	{
		m_model = model;
		m_dependencies = null;
		m_listeners = new LinkedList<IDependenciesListener>();
		m_pmgr = (IProcedureManager) ServiceManager.get(IProcedureManager.class);
		updateDependencies();
	}
	
	/**************************************************************************
	 * 
	 *************************************************************************/
	public Object visitCall(Call node) throws Exception
	{
		if (node.func instanceof Name)
		{
			Name func = (Name) node.func;
			if (func.id.equals("StartProc"))
			{
				exprType arg0 = node.args[0];
				if (arg0 instanceof Str)
				{
					Str str = (Str) arg0;
					String procId = completeId(str.s);
					String procName = UNKNOWN_NAME;
					if (procId != null)
					{
						procName = m_pmgr.getProcedureName(procId);
					}
					else
					{
						procId = UNKNOWN_ID;
					}
					int lineNo = node.beginLine;
					for(exprType expr : node.args)
					{
						if (expr.beginLine>lineNo) lineNo = expr.beginLine;
					}
					for(keywordType k : node.keywords)
					{
						if (k.beginLine>lineNo) lineNo = k.beginLine;
					}
					m_dependencies.add( new Dependency(procId, procName, lineNo ));
				}
				
			}
				
		}
		return super.visitCall(node);
	}

	/**************************************************************************
	 * 
	 *************************************************************************/
	@Override
	public void updateDependencies()
	{
		m_dependencies = new LinkedList<Dependency>();
		Parser parser = new Parser();
		
		String[] lines = m_model.getSourceCodeProvider().getSource(m_model.getProcId(), new NullProgressMonitor());
		String code = "";
		for(String line : lines)
		{
			if (!code.isEmpty()) code += "\n";
			code += line;
		}
		try
        {
	        parser.parseCode( code, this );
        }
		catch (ParseException ex)
		{
			Logger.error("Parse exception on procedure " + m_model.getProcId(), Level.PROC, this);
			Logger.error("Message: " + ex.getLocalizedMessage(), Level.PROC, this);
			Logger.error("Line: " + ex.currentToken.beginLine + ", column: " + ex.currentToken.beginColumn, Level.PROC, this);
			Logger.error("Current token: '" + ex.currentToken + "'", Level.PROC, this);
			Logger.error("Expected token sequences: " + Arrays.toString(ex.expectedTokenSequences), Level.PROC, this);
			Logger.error("Token image: " + Arrays.toString(ex.tokenImage), Level.PROC, this);
		}
        catch (Exception e)
        {
	        e.printStackTrace();
        }
	}
	
	/**************************************************************************
	 * 
	 *************************************************************************/
	@Override
	public int getDependenciesCount()
	{
		return m_dependencies.size();
	}

	/**************************************************************************
	 * 
	 *************************************************************************/
	@Override
	public Dependency getDependency( int index )
	{
		return m_dependencies.get(index);
	}

	/**************************************************************************
	 * 
	 *************************************************************************/
	@Override
	public void onChildOpen( String childInstanceId, int lineNo )
	{
		int idx = childInstanceId.indexOf("#");
		String procId = childInstanceId.substring(0,idx);
		boolean found = false;
		for(Dependency dep : m_dependencies)
		{
			if (dep.getProcedureId().equals(procId) && dep.getLineNo() == lineNo)
			{
				dep.setChildStarted(childInstanceId);
				found = true;
				break;
			}
		}
		if (found)
		{
			for(IDependenciesListener listener : m_listeners)
			{
				listener.onChildOpen(m_model.getProcId(), childInstanceId, lineNo);
			}
		}
		else
		{
			Logger.error("Cannot find dependency: " + procId, Level.PROC, this);
			Logger.error(Arrays.toString(m_dependencies.toArray()), Level.PROC, this);
		}
	}

	/**************************************************************************
	 * 
	 *************************************************************************/
	private String completeId( String procId )
	{
		String completeId = procId;
		if (!m_pmgr.isProcedureIdAvailable(procId))
		{
			completeId = m_pmgr.getCompleteProcedureId(procId);
		}
		return completeId;
	}

	/**************************************************************************
	 * 
	 *************************************************************************/
	@Override
	public void onChildClosed( String childInstanceId )
	{
		boolean found = false;
		try
		{
			for(Dependency dep : m_dependencies)
			{
				if (dep.getInstanceId().equals(childInstanceId))
				{
					dep.setChildFinished(childInstanceId);
					found = true;
					break;
				}
			}
			if (found)
			{
				for(IDependenciesListener listener : m_listeners)
				{
					listener.onChildClosed(m_model.getProcId(), childInstanceId);
				}
			}
			else
			{
				Logger.error("Cannot find dependency: " + childInstanceId, Level.PROC, this);
			}
		}
		catch(Exception ex)
		{
			ex.printStackTrace();
			Logger.error("Cannot find dependency of child: " + childInstanceId, Level.PROC, this);
		}
	}
	
	/**************************************************************************
	 * 
	 *************************************************************************/
	@Override
	public void addListener( IDependenciesListener listener )
	{
		if (!m_listeners.contains(listener))
		{
			m_listeners.add(listener);
		}
	}
	
	/**************************************************************************
	 * 
	 *************************************************************************/
	@Override
	public void removeListener( IDependenciesListener listener )
	{
		if (m_listeners.contains(listener))
		{
			m_listeners.remove(listener);
		}
	}

}
