package com.astra.ses.spell.language.common;

import com.astra.ses.spell.language.model.SimpleNode;

public interface ITreeBuilder
{

	public static final boolean	DEBUG_TREE_BUILDER	= false;

	public abstract SimpleNode closeNode(SimpleNode sn, int num)
	        throws Exception;

	public abstract SimpleNode openNode(int jjtfileInput);

	public abstract SimpleNode getLastOpened();

}
