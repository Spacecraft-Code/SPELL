package com.astra.ses.spell.language.common;

import com.astra.ses.spell.language.model.SimpleNode;
import com.astra.ses.spell.language.model.ast.decoratorsType;

public class Decorators extends SimpleNode
{
	public final decoratorsType[]	exp;

	public Decorators(decoratorsType[] exp, int id)
	{
		this.exp = exp;
		this.setId(id);
	}
}
