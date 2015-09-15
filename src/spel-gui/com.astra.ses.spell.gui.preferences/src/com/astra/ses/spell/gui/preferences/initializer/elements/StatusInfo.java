package com.astra.ses.spell.gui.preferences.initializer.elements;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

public class StatusInfo 
{
	private HashMap<String,String> m_components;
	private String m_text;
	private final static String LABEL_SEPARATOR = ";";
	public final static String STATUS_ID = "STATUS";
	
	/**
	 * Constructor
	 * 
	 * @param components
	 */
	public StatusInfo(HashMap<String,String> components) 
	{
		m_components = components;
		// Create label
		m_text = -1 + LABEL_SEPARATOR + STATUS_ID;
		int index = 0;
		for ( String component : m_components.keySet() )
		{
			if ( component.equals(STATUS_ID) )
			{
				m_text = Integer.toString(index) + LABEL_SEPARATOR + m_components.get(STATUS_ID);
				break;
			}
			index++;
		}
	}
	
	/**
	 * Constructor
	 * 
	 * @param label
	 */
	public StatusInfo(String text) 
	{
		m_text = text;
	}
	
	/**
	 * Get status text
	 * 
	 * @return
	 */
	public String getText()
	{
		return m_text;
	}
	
	/**
	 * Get status location
	 * 
	 * @return
	 */
	public int getLocation()
	{
		int pos = -1;
		List<String> status = Arrays.asList(m_text.split(LABEL_SEPARATOR));
		if (status.size() == 2)
		{
			try
			{
				pos = Integer.parseInt(status.get(0));
			}
			catch ( NumberFormatException ex)
			{
				// Nothing to be done
			}
		}
		return pos;
	}
	
	/**
	 * Get status description
	 * 
	 * @return
	 */
	public String getLabel()
	{
		String descr = "";
		List<String> status = Arrays.asList(m_text.split(LABEL_SEPARATOR));
		if (status.size() == 2)
		{
			descr = status.get(1);
		}
		return descr;
	}
}
