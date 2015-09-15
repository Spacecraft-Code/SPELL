package com.astra.ses.spell.language.common;

import com.astra.ses.spell.language.model.SimpleNode;
import com.astra.ses.spell.language.model.ast.exprType;

public class ExtraArgValue extends SimpleNode
{
	final public exprType	value;
	final public int	  id;

	public ExtraArgValue(exprType value, int id)
	{
		this.value = value;
		this.id = id;
	}

	public int getId()
	{
		return id;
	}
}
