package com.astra.ses.spell.language.common;

import com.astra.ses.spell.language.ParseException;
import com.astra.ses.spell.language.Visitor;
import com.astra.ses.spell.language.model.SimpleNode;
import com.astra.ses.spell.language.model.ast.Attribute;
import com.astra.ses.spell.language.model.ast.Call;
import com.astra.ses.spell.language.model.ast.List;
import com.astra.ses.spell.language.model.ast.Name;
import com.astra.ses.spell.language.model.ast.Subscript;
import com.astra.ses.spell.language.model.ast.Tuple;
import com.astra.ses.spell.language.model.ast.expr_contextType;

public class CtxVisitor extends Visitor
{

	private int	ctx;

	public CtxVisitor()
	{
	}

	public void setParam(SimpleNode node) throws Exception
	{
		this.ctx = expr_contextType.Param;
		visit(node);
	}

	public void setKwOnlyParam(SimpleNode node) throws Exception
	{
		this.ctx = expr_contextType.KwOnlyParam;
		visit(node);
	}

	public void setStore(SimpleNode node) throws Exception
	{
		this.ctx = expr_contextType.Store;
		visit(node);
	}

	public void setStore(SimpleNode[] nodes) throws Exception
	{
		for (int i = 0; i < nodes.length; i++)
			setStore(nodes[i]);
	}

	public void setDelete(SimpleNode node) throws Exception
	{
		this.ctx = expr_contextType.Del;
		visit(node);
	}

	public void setDelete(SimpleNode[] nodes) throws Exception
	{
		for (int i = 0; i < nodes.length; i++)
			setDelete(nodes[i]);
	}

	public void setAugStore(SimpleNode node) throws Exception
	{
		this.ctx = expr_contextType.AugStore;
		visit(node);
	}

	public Object visitName(Name node) throws Exception
	{
		if (ctx == expr_contextType.Store)
		{
			if (node.reserved) { throw new ParseException(StringUtils.format(
			        "Cannot assign value to %s (because it's a keyword)",
			        node.id), node); }
		}
		node.ctx = ctx;
		return null;
	}

	public Object visitAttribute(Attribute node) throws Exception
	{
		node.ctx = ctx;
		return null;
	}

	public Object visitSubscript(Subscript node) throws Exception
	{
		node.ctx = ctx;
		return null;
	}

	public Object visitList(List node) throws Exception
	{
		if (ctx == expr_contextType.AugStore) { throw new ParseException(
		        "augmented assign to list not possible", node); }
		node.ctx = ctx;
		traverse(node);
		return null;
	}

	public Object visitTuple(Tuple node) throws Exception
	{
		if (ctx == expr_contextType.AugStore) { throw new ParseException(
		        "augmented assign to tuple not possible", node); }
		node.ctx = ctx;
		traverse(node);
		return null;
	}

	public Object visitCall(Call node) throws Exception
	{
		throw new ParseException("can't assign to function call", node);
	}

	public Object visitListComp(Call node) throws Exception
	{
		throw new ParseException("can't assign to list comprehension call",
		        node);
	}

	public Object unhandled_node(SimpleNode node) throws Exception
	{
		throw new ParseException("can't assign to operator:" + node, node);
	}
}
