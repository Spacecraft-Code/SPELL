package com.astra.ses.spell.gui.shared.services;

import java.util.List;

import org.eclipse.core.runtime.IProgressMonitor;

import com.astra.ses.spell.gui.shared.messages.SharedDataOperation;
import com.astra.ses.spell.gui.shared.views.controls.SharedVariable;

public interface ISharedScope
{

	/***************************************************************************
	 * 
	 **************************************************************************/
	public void update(IProgressMonitor monitor);

	/***************************************************************************
	 * 
	 **************************************************************************/
	public void update( SharedDataOperation operation, String[] varList, String[] varValue, 
			List<SharedVariable> added, List<SharedVariable> updated, List<SharedVariable> deleted );

	/***************************************************************************
	 * 
	 **************************************************************************/
	public boolean clear();

	/***************************************************************************
	 * 
	 **************************************************************************/
	public boolean clear(String variable);

	/***************************************************************************
	 * 
	 **************************************************************************/
	public boolean set(String variable, String value);

	/***************************************************************************
	 * 
	 **************************************************************************/
	public SharedVariable get(String variable);

	/***************************************************************************
	 * 
	 **************************************************************************/
	public SharedVariable[] getAll();

	/***************************************************************************
	 * 
	 **************************************************************************/
	public String getScopeName();

	/***************************************************************************
	 * 
	 **************************************************************************/
	public List<String> getKeys();

}