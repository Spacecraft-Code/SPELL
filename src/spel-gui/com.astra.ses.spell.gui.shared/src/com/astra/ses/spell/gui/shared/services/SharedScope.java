package com.astra.ses.spell.gui.shared.services;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.eclipse.core.runtime.IProgressMonitor;

import com.astra.ses.spell.gui.core.comm.messages.SPELLmessage;
import com.astra.ses.spell.gui.core.interfaces.IContextProxy;
import com.astra.ses.spell.gui.core.model.types.Level;
import com.astra.ses.spell.gui.core.utils.Logger;
import com.astra.ses.spell.gui.shared.messages.SPELLmessageClearSharedVariable;
import com.astra.ses.spell.gui.shared.messages.SPELLmessageGetSharedVariable;
import com.astra.ses.spell.gui.shared.messages.SPELLmessageGetSharedVariableKeys;
import com.astra.ses.spell.gui.shared.messages.SPELLmessageSetSharedVariable;
import com.astra.ses.spell.gui.shared.messages.SharedDataOperation;
import com.astra.ses.spell.gui.shared.views.controls.SharedVariable;

public class SharedScope implements ISharedScope
{
	private IContextProxy m_proxy = null;
	private String m_scope;
	private Map<String,SharedVariable> m_data;
	
	/***************************************************************************
	 * 
	 **************************************************************************/
	public SharedScope( String scope, IContextProxy proxy )
	{
		m_proxy = proxy;
		m_scope = scope;
		m_data = new TreeMap<String,SharedVariable>();
    	Logger.debug("Created scope model: " + scope, Level.PROC, this);
	}
	
	/***************************************************************************
	 * 
	 **************************************************************************/
	@Override
    public void update( IProgressMonitor monitor )
	{
    	Logger.debug("Update scope model: " + m_scope, Level.PROC, this);

		Map<String,SharedVariable> temp = new TreeMap<String,SharedVariable>();
		List<String> keys = doGetKeys();
		monitor.beginTask("Updating variables", keys.size());
		for(String key : keys)
		{
			String value = doGet(key);
			SharedVariable var = new SharedVariable(key,value);
			temp.put(key,var);
			monitor.worked(1);
			if (monitor.isCanceled()) return; 
		}
		m_data.clear();
		m_data.putAll(temp);
		monitor.done();
	}

	/***************************************************************************
	 * 
	 **************************************************************************/
	@Override
    public void update( SharedDataOperation operation, String[] varList, String[] valueList,
    		            List<SharedVariable> added, List<SharedVariable> updated, List<SharedVariable> deleted)
	{
		int count = 0;
		
		if (operation.equals(SharedDataOperation.DEL_SHARED_DATA))
		{
			for(String key : varList)
			{
				if (m_data.containsKey(key))
				{
					deleted.add( m_data.remove( key ) );
				}
				count++;
			}
		}
		else
		{
			for(String key : varList)
			{
				if (!m_data.containsKey(key))
				{
					m_data.put( key, new SharedVariable(key, valueList[count] ));
					added.add(m_data.get(key));
				}
				else
				{
					String oldValue = m_data.get( key ).value; 
					m_data.get( key ).value = valueList[count];
					if (!oldValue.equals(valueList[count]))
					{
						updated.add(m_data.get(key));
					}
				}
				count++;
			}
		}
	}

	/***************************************************************************
	 * 
	 **************************************************************************/
	@Override
    public boolean clear()
	{
		if (doClearScope())
		{
			m_data.clear();
			return true;
		}
		return false;
	}

	/***************************************************************************
	 * 
	 **************************************************************************/
	@Override
    public boolean clear( String variable )
	{
		if (doClear(variable))
		{
			m_data.remove(variable);
			return true;
		}
		return false;
	}

	/***************************************************************************
	 * 
	 **************************************************************************/
	@Override
    public boolean set( String variable, String value )
	{
		if (doSet(variable,value))
		{
			m_data.get(variable).value = value;
			return true;
		}
		return false;
	}

	/***************************************************************************
	 * 
	 **************************************************************************/
	@Override
    public SharedVariable get( String variable )
	{
		return m_data.get(variable);
	}

	/***************************************************************************
	 * 
	 **************************************************************************/
	@Override
    public SharedVariable[] getAll()
	{
		return m_data.values().toArray( new SharedVariable[0] );
	}

	/***************************************************************************
	 * 
	 **************************************************************************/
	@Override
    public String getScopeName()
	{
		return m_scope;
	}

	/***************************************************************************
	 * 
	 **************************************************************************/
	@Override
    public List<String> getKeys()
	{
		List<String> keys = new LinkedList<String>();
		keys.addAll(m_data.keySet());
		return keys;
	}

	/***************************************************************************
	 * 
	 **************************************************************************/
	private List<String> doGetKeys()
	{
		List<String> keys = null;
		SPELLmessage msg = new SPELLmessageGetSharedVariableKeys(m_scope);
		SPELLmessage response;
        try
        {
	        response = m_proxy.sendRequest(msg);
			if (response != null)
			{
				keys = SPELLmessageGetSharedVariableKeys.getKeys(response);
			}
        }
        catch (Exception e)
        {
	        e.printStackTrace();
        }
		return keys;
	}
	
	/***************************************************************************
	 * 
	 **************************************************************************/
	private String doGet( String key )
	{
		String value = null;
		SPELLmessage msg = new SPELLmessageGetSharedVariable(key, m_scope);
		SPELLmessage response;
        try
        {
	        response = m_proxy.sendRequest(msg);
			if (response != null)
			{
				value = SPELLmessageGetSharedVariable.getValue(response);
			}
        }
        catch (Exception e)
        {
	        e.printStackTrace();
        }
		return value;
	}
	
	/***************************************************************************
	 * 
	 **************************************************************************/
	private boolean doSet( String key, String value )
	{
		boolean success = false;
		SPELLmessage msg = new SPELLmessageSetSharedVariable( m_proxy.getClientKey(), key, value, m_scope);
		SPELLmessage response;
        try
        {
	        response = m_proxy.sendRequest(msg);
			if (response != null)
			{
				success = SPELLmessageSetSharedVariable.isSuccess(response);
			}
        }
        catch (Exception e)
        {
	        e.printStackTrace();
        }
		return success;
	}

	/***************************************************************************
	 * 
	 **************************************************************************/
	private boolean doClear( String key )
	{
		boolean success = false;
		SPELLmessage msg = new SPELLmessageClearSharedVariable(key, m_scope);
		SPELLmessage response;
        try
        {
	        response = m_proxy.sendRequest(msg);
			if (response != null)
			{
				success = SPELLmessageClearSharedVariable.isSuccess(response);
			}
        }
        catch (Exception e)
        {
	        e.printStackTrace();
        }
		return success;
	}
	
	/***************************************************************************
	 * 
	 **************************************************************************/
	private boolean doClearScope()
	{
		boolean success = false;
		SPELLmessage msg = new SPELLmessageClearSharedVariable( m_proxy.getClientKey(), m_scope);
		SPELLmessage response;
        try
        {
	        response = m_proxy.sendRequest(msg);
			if (response != null)
			{
				success = SPELLmessageClearSharedVariable.isSuccess(response);
			}
        }
        catch (Exception e)
        {
	        e.printStackTrace();
        }
		return success;
	}
}
