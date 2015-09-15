// Autogenerated AST node
package com.astra.ses.spell.language.model.ast;

import com.astra.ses.spell.language.model.SimpleNode;

public class GeneratorExp extends exprType
{
	public exprType	           elt;
	public comprehensionType[]	generators;

	public GeneratorExp(exprType elt, comprehensionType[] generators)
	{
		this.elt = elt;
		this.generators = generators;
	}

	public GeneratorExp(exprType elt, comprehensionType[] generators,
	        SimpleNode parent)
	{
		this(elt, generators);
		this.beginLine = parent.beginLine;
		this.beginColumn = parent.beginColumn;
	}

	public String toString()
	{
		StringBuffer sb = new StringBuffer("GeneratorExp[");
		sb.append("elt=");
		sb.append(dumpThis(this.elt));
		sb.append(", ");
		sb.append("generators=");
		sb.append(dumpThis(this.generators));
		sb.append("]");
		return sb.toString();
	}

	public Object accept(VisitorIF visitor) throws Exception
	{
		return visitor.visitGeneratorExp(this);
	}

	public void traverse(VisitorIF visitor) throws Exception
	{
		if (elt != null) elt.accept(visitor);
		if (generators != null)
		{
			for (int i = 0; i < generators.length; i++)
			{
				if (generators[i] != null) generators[i].accept(visitor);
			}
		}
	}

}
