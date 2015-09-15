package com.astra.ses.spell.language.common;

import com.astra.ses.spell.language.model.SimpleNode;

public interface IJJTGrammarState
{

	void pushNodePos(int beginLine, int beginColumn);

	SimpleNode peekNode();

	SimpleNode setNodePos();

	SimpleNode getLastOpened();

}
