package com.astra.ses.spell.language.common;

import com.astra.ses.spell.language.model.SimpleNode;
import com.astra.ses.spell.language.model.ast.Name;
import com.astra.ses.spell.language.model.ast.exprType;

public class JfpDef extends SimpleNode
{
	public final Name	  nameNode;
	public final exprType	typeDef;

	public JfpDef(Name node, exprType typeDef)
	{
		this.nameNode = node;
		this.typeDef = typeDef;
	}
}
