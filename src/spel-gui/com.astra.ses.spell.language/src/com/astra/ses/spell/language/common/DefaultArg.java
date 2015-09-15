package com.astra.ses.spell.language.common;

import com.astra.ses.spell.language.model.SimpleNode;
import com.astra.ses.spell.language.model.ast.exprType;

public class DefaultArg extends SimpleNode
{
	final public exprType	parameter;
	final public exprType	value;
	final public exprType	typeDef;
	final public int	  id;

	/**
	 * @param id
	 *            The id of the node that created this argument. E.g.:
	 *            JJTDEFAULTARG JJTONLYKEYWORDARG etc.
	 */
	public DefaultArg(exprType parameter, exprType value, exprType typeDef,
	        int id)
	{
		this.parameter = parameter;
		this.value = value;
		this.typeDef = typeDef;
		this.id = id;
	}

	public DefaultArg(exprType parameter, exprType value, int id)
	{
		this.parameter = parameter;
		this.value = value;
		this.typeDef = null;
		this.id = id;
	}

}
