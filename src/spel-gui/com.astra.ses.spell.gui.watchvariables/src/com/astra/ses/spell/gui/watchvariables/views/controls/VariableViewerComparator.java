package com.astra.ses.spell.gui.watchvariables.views.controls;

import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerComparator;
import org.eclipse.swt.SWT;

import com.astra.ses.spell.gui.watchvariables.notification.VariableData;
import com.astra.ses.spell.gui.watchvariables.model.WatchVariablesTableColumns;

public class VariableViewerComparator extends ViewerComparator {

    private enum Direction
	{
		ASCENDING,
		DESCENDING
	};

    private int m_propertyIndex;
    private Direction m_direction;
    
    public VariableViewerComparator(){
        m_propertyIndex = 0;
		m_direction = Direction.DESCENDING;
    } // End Constructor

	public int getDirection()
	{
		switch(m_direction)
		{
		case ASCENDING:
			return SWT.UP;
		case DESCENDING:
			default:
			return SWT.DOWN;
		}
	}

	public void setColumn( int column )
	{
		if (column == m_propertyIndex)
		{
			if (m_direction.equals(Direction.ASCENDING))
			{
				m_direction = Direction.DESCENDING;
			}
			else if (m_direction.equals(Direction.DESCENDING)) 
			{
				m_direction = Direction.ASCENDING;
			}
		}
		else
		{
			m_propertyIndex = column;
			m_direction = Direction.DESCENDING;
		}
	}

    @Override
	public int compare( Viewer viewer, Object e1, Object e2 )
	{
        VariableData v1 = (VariableData) e1;
        VariableData v2 = (VariableData) e2;
	    int rc = 0;

    	WatchVariablesTableColumns idx = WatchVariablesTableColumns.fromIndex(m_propertyIndex);
		switch(idx)
		{
    		case NAME_COLUMN:
    			rc = v1.getName().compareTo(v2.getName());
    			break;
    		case VALUE_COLUMN:
    			rc = v1.getValue().compareTo(v2.getValue());
    			break;
    		default:
    			rc = 0;
		}

        if (m_direction.equals(Direction.DESCENDING))
		{
			rc = -rc;
		}
		return rc;
    }


} //End Class
