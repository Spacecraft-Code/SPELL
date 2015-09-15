package com.astra.ses.spell.gui.preferences.values;

public enum DisplayDataPref
{
	NAME("Name"),
	VALUE("Value"),
	BOTH("Both name and value");
	
	private DisplayDataPref( String title )
	{
		this.title = title;
	}

	public static DisplayDataPref fromTitle( String title )
	{
		for(DisplayDataPref dd : values())
		{
			if (dd.title.equals(title)) return dd;
		}
		return null;
	}

	public String title;

}
