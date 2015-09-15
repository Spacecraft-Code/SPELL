package com.astra.ses.spell.gui.views.controls.master.executors;


public interface IExecutorsTable
{

	/***************************************************************************
	 * Apply the defined fonts to the control
	 **************************************************************************/
	public void applyFonts();

	/***************************************************************************
	 * Refresh the table item for the given procedure id 
	 **************************************************************************/
	public void refreshItem(String procId);

	/***************************************************************************
	 * Refresh all table items
	 **************************************************************************/
	public void refreshAll();

	/***************************************************************************
	 * Get the selected procedure
	 **************************************************************************/
	public String[] getSelectedProcedures();
}