package com.astra.ses.spell.language.common;

import com.astra.ses.spell.language.model.SimpleNode;
import com.astra.ses.spell.language.model.ast.NameTok;
import com.astra.ses.spell.language.model.ast.exprType;

public class ExtraArg extends SimpleNode
{
	public final NameTok	tok;
	public final exprType	typeDef;

	public ExtraArg(NameTok tok, int id)
	{
		this(tok, id, null);
	}

	public ExtraArg(NameTok tok, int id, exprType typeDef)
	{
		this.setId(id);
		this.tok = tok;
		this.typeDef = typeDef;
	}

}
