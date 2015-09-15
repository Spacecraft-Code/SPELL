// Autogenerated AST node
package com.astra.ses.spell.language.model.ast;

import com.astra.ses.spell.language.model.SimpleNode;

public class keywordType extends SimpleNode
{
	public NameTokType	arg;
	public exprType	   value;

	public keywordType(NameTokType arg, exprType value)
	{
		this.arg = arg;
		this.value = value;
	}

	public keywordType(NameTokType arg, exprType value, SimpleNode parent)
	{
		this(arg, value);
		this.beginLine = parent.beginLine;
		this.beginColumn = parent.beginColumn;
	}

	public String toString()
	{
		StringBuffer sb = new StringBuffer("keyword[");
		sb.append("arg=");
		sb.append(dumpThis(this.arg));
		sb.append(", ");
		sb.append("value=");
		sb.append(dumpThis(this.value));
		sb.append("]");
		return sb.toString();
	}

	public Object accept(VisitorIF visitor) throws Exception
	{
		traverse(visitor);
		return null;
	}

	public void traverse(VisitorIF visitor) throws Exception
	{
		if (arg != null) arg.accept(visitor);
		if (value != null) value.accept(visitor);
	}

}