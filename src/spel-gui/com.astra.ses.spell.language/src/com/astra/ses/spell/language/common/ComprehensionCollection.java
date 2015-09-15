package com.astra.ses.spell.language.common;

import java.util.ArrayList;
import java.util.Collections;

import com.astra.ses.spell.language.model.SimpleNode;
import com.astra.ses.spell.language.model.ast.Comprehension;
import com.astra.ses.spell.language.model.ast.comprehensionType;

public class ComprehensionCollection extends SimpleNode
{
	public ArrayList<Comprehension>	added	= new ArrayList<Comprehension>();

	public comprehensionType[] getGenerators()
	{
		ArrayList<Comprehension> f = added;
		added = null;
		Collections.reverse(f);
		return f.toArray(new comprehensionType[0]);
	}
}
