package com.astra.ses.spell.gui.preferences.initializer.elements;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;

public class CommandsInfo 
{
	private LinkedHashMap<String, String> m_commands;
	private String m_text;
	private final static String COMMAND_SEPARATOR = ";";
	private final static String LABEL_SEPARATOR = "@";

	/**
	 * Constructor
	 * 
	 * @param commands
	 */
	public CommandsInfo(LinkedHashMap<String,String> commands) 
	{
		m_commands = commands;
		m_text = "";
		// Creating label
		for ( String id : commands.keySet() )
		{
			if ( !id.equals(StatusInfo.STATUS_ID) )
			{
				m_text += id + LABEL_SEPARATOR + commands.get(id) + COMMAND_SEPARATOR; 
			}
		}
		// Deleting last separator (if any)
		if ( m_text.endsWith(COMMAND_SEPARATOR) )			
		{
			m_text = m_text.substring(0, m_text.length()-1);
		}
	}

	/**
	 * Constructor
	 * 
	 * @param text
	 */
	public CommandsInfo(String text) 
	{
		m_commands = new LinkedHashMap<String, String>();
		List<String> commands = Arrays.asList(text.split(COMMAND_SEPARATOR));
		for (String command : commands)
		{
			List<String> definition = Arrays.asList(command.split(LABEL_SEPARATOR));
			if ( definition.size() == 2 )
			{
				m_commands.put(definition.get(0),definition.get(1));
			}
		}
	}
	
	/**
	 * Get commands map
	 * 
	 * @return
	 */
	public LinkedHashMap<String,String> getMap()
	{
		return m_commands;
	}
	
	/**
	 * Get commands text
	 * 
	 * @return
	 */
	public String getText()
	{
		return m_text;
	}	
}
