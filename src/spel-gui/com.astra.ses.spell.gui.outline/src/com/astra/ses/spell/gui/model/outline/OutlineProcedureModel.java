////////////////////////////////////////////////////////////////////////////////
//
// PACKAGE   : com.astra.ses.spell.gui.model.outline
// 
// FILE      : OutlineProcedureModel.java
//
// DATE      : Sep 22, 2010
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
////////////////////////////////////////////////////////////////////////////////
package com.astra.ses.spell.gui.model.outline;

import java.util.List;

import com.astra.ses.spell.gui.model.outline.nodes.OutlineCategoryNode;
import com.astra.ses.spell.gui.model.outline.nodes.OutlineDefNode;
import com.astra.ses.spell.gui.model.outline.nodes.OutlineErrorNode;
import com.astra.ses.spell.gui.model.outline.nodes.OutlineGotoNode;
import com.astra.ses.spell.gui.model.outline.nodes.OutlineNode;
import com.astra.ses.spell.gui.model.outline.nodes.OutlineNode.OutlineNodeType;
import com.astra.ses.spell.gui.model.outline.nodes.OutlineRootNode;
import com.astra.ses.spell.gui.model.outline.nodes.OutlineStepNode;
import com.astra.ses.spell.gui.procs.interfaces.exceptions.UninitProcedureException;
import com.astra.ses.spell.gui.procs.interfaces.model.ICodeLine;
import com.astra.ses.spell.gui.procs.interfaces.model.ICodeModel;
import com.astra.ses.spell.gui.procs.interfaces.model.IProcedure;
import com.astra.ses.spell.language.ParseException;
import com.astra.ses.spell.language.Parser;
import com.astra.ses.spell.language.Visitor;
import com.astra.ses.spell.language.model.ast.Call;
import com.astra.ses.spell.language.model.ast.FunctionDef;
import com.astra.ses.spell.language.model.ast.Name;
import com.astra.ses.spell.language.model.ast.NameTok;
import com.astra.ses.spell.language.model.ast.Str;

/******************************************************************************
 * Special tree node representing the main procedure. There is only one node of
 * this type on each callstack tree model.
 *****************************************************************************/
public class OutlineProcedureModel extends Visitor
{
	private static final String	   STEP_CALL	= "Step";
	private static final String	   GOTO_CALL	= "Goto";

	/** Holds the root (invisible parent node on the tree) */
	private OutlineRootNode	       m_root;
	/** Error node */
	private OutlineErrorNode	   m_errorNode;
	/** Category node for steps */
	private OutlineCategoryNode	   m_categorySteps;
	/** Category node for steps */
	private OutlineCategoryNode	   m_categoryGotos;
	/** Category node for steps */
	private OutlineCategoryNode	   m_categoryFunctionDefs;
	/** Parser instance */
	private Parser	               m_parser;
	/** Data provider reference */
	private IProcedure             m_model;
	/** Procedure identifier */
	private String	               m_procId;
	/** Flag for parsing token elements */
	private NextTokenType	       m_inToken;
	/** Used to compose the label of the next node */
	private String	               m_nextLabel;
	/** Used to hold the line number label of the next element */
	private int	                   m_nextLine;

	enum NextTokenType
	{
		FunctionCall, StepCall, StepTarget, GotoCall, None
	}

	/**************************************************************************
	 * Constructor.
	 * 
	 * @param name
	 *            The identifier of the procedure
	 * @param model
	 *            The associated procedure model, used to populate the tree
	 *            model.
	 *************************************************************************/
	public OutlineProcedureModel(IProcedure model)
	{
		// If we have a model available, populate this node with the data
		m_root = null;
		m_errorNode = null;
		m_parser = new Parser();
		m_procId = model.getProcId();
		m_model = model;
		initialize();
	}

	/***************************************************************************
	 * Get this model's root node
	 * 
	 * @return
	 **************************************************************************/
	public OutlineNode getRootNode()
	{
		return m_root;
	}

	/**************************************************************************
	 * Clear the model children.
	 *************************************************************************/
	public void clear()
	{
		// If we have a model available, populate this node with the data
		m_root.clearChildren();
	}

	/**************************************************************************
	 * Initialize the tree model with the associated procedure model.
	 *************************************************************************/
	void createContents()
	{
		initialize();
		m_inToken = NextTokenType.None;
		m_nextLabel = "";
		String codeId = null;
		try
		{
			codeId = m_model.getExecutionManager().getCurrentCode();
			ICodeModel codeModel = m_model.getExecutionManager().getCodeModel(codeId);
			List<ICodeLine> lines = codeModel.getLines();
			String code = "";
			for (ICodeLine line : lines)
			{
				if (!code.isEmpty()) code += "\n";
				code += line.getSource();
			}
			// Add a last line to ensure correct parsing at end
			code += "\npass\n";
			m_parser.parseCode(code, this);
		}
		catch (UninitProcedureException e)
		{
			//e.printStackTrace();
		}
		catch (ParseException e)
		{
			m_root.clearChildren();
			/*
			 * An error node instance is created
			 */
			m_errorNode = new OutlineErrorNode(e.getMessage(), codeId,
			        e.currentToken.beginLine);
			m_root.addChild(m_errorNode);
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}

	/**************************************************************************
	 * Initialize the tree model with the given procedure model.
	 * 
	 * @param model
	 *            Procedure model to use
	 *************************************************************************/
	private void initialize()
	{
		if (m_root == null)
		{
			m_root = new OutlineRootNode(m_procId);
			m_categorySteps = new OutlineCategoryNode("Steps",
			        OutlineNodeType.STEP);
			m_categoryGotos = new OutlineCategoryNode("Gotos",
			        OutlineNodeType.GOTO);
			m_categoryFunctionDefs = new OutlineCategoryNode("Declarations",
			        OutlineNodeType.DECLARATION);
			m_root.addChild(m_categorySteps);
			m_root.addChild(m_categoryGotos);
			m_root.addChild(m_categoryFunctionDefs);
		}
		else
		{
			m_categorySteps.clearChildren();
			m_categoryGotos.clearChildren();
			m_categoryFunctionDefs.clearChildren();
		}
	}

	@Override
	public Object visitFunctionDef(FunctionDef node) throws Exception
	{
		NameTok name = (NameTok) node.name;
		String codeId = m_model.getExecutionManager().getCurrentCode();
		m_categoryFunctionDefs.addChild(new OutlineDefNode(name.id + "()", codeId, node.beginLine));
		return super.visitFunctionDef(node);
	}

	@Override
	public Object visitCall(Call node) throws Exception
	{
		m_inToken = NextTokenType.FunctionCall;
		m_nextLine = node.beginLine;
		return super.visitCall(node);
	}

	@Override
	public Object visitName(Name node) throws Exception
	{
		if (m_inToken.equals(NextTokenType.FunctionCall))
		{
			if (node.id.equals(STEP_CALL))
			{
				m_nextLabel = "";
				m_inToken = NextTokenType.StepCall;
			}
			else if (node.id.equals(GOTO_CALL))
			{
				m_nextLabel = "Goto ";
				m_inToken = NextTokenType.GotoCall;
			}
		}
		return super.visitName(node);
	}

	@Override
	public Object visitStr(Str token) throws Exception
	{
		if (m_inToken.equals(NextTokenType.StepCall))
		{
			m_inToken = NextTokenType.StepTarget;
			m_nextLabel = token.s;
		}
		else if (m_inToken.equals(NextTokenType.StepTarget))
		{
			m_inToken = NextTokenType.None;
			m_nextLabel = token.s + " (" + m_nextLabel + ")";
			String codeId = m_model.getExecutionManager().getCurrentCode();
			m_categorySteps.addChild(new OutlineStepNode(m_nextLabel, codeId, m_nextLine));
			m_nextLabel = "";
		}
		else if (m_inToken.equals(NextTokenType.GotoCall))
		{
			m_inToken = NextTokenType.None;
			m_nextLabel += token.s;
			String codeId = m_model.getExecutionManager().getCurrentCode();
			m_categoryGotos.addChild(new OutlineGotoNode(m_nextLabel, codeId, m_nextLine));
			m_nextLabel = "";
		}
		return super.visitStr(token);
	}
}
