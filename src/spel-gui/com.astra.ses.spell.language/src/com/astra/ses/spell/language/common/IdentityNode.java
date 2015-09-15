package com.astra.ses.spell.language.common;

import com.astra.ses.spell.language.model.SimpleNode;

public class IdentityNode extends SimpleNode
{
	public int	  id;
	public Object	image;

	IdentityNode(int id)
	{
		this.id = id;
	}

	public int getId()
	{
		return id;
	}

	public void setImage(Object image)
	{
		this.image = image;
	}

	public Object getImage()
	{
		return image;
	}

	public String toString()
	{
		return "IdNode[" + id + ", " + image + "]";
	}
}
