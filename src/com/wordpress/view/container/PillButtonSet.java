package com.wordpress.view.container;

/*
 * PillButtonSet.java
 *
 * Research In Motion Limited proprietary and confidential
 * Copyright Research In Motion Limited, 2009-2009
 */



import net.rim.device.api.ui.Field;

import com.wordpress.view.component.PillButtonField;

/**
 *
 */
public class PillButtonSet extends JustifiedEvenlySpacedHorizontalFieldManager
{
    private Field _selectedField;

    public PillButtonSet()
    {
    }

    protected void sublayout( int maxWidth, int maxHeight )
    {
        Field child;
        int numChildren = this.getFieldCount();
        int index = 0;
        if( numChildren == 1 ) {
            child = getField( index );
            if( child instanceof PillButtonField ) {
                ( (PillButtonField) child ).setDrawPosition( PillButtonField.DRAWPOSITION_SINGLE );
            }
        } else {
            child = getField( index );
            if( child instanceof PillButtonField ) {
                ( (PillButtonField) child ).setDrawPosition( PillButtonField.DRAWPOSITION_LEFT );
            }
            for( index = 1; index < numChildren - 1 ; index++ ) {
                child = getField( index );
                if( child instanceof PillButtonField ) {
                    ( (PillButtonField) child ).setDrawPosition( PillButtonField.DRAWPOSITION_MIDDLE );
                }
            }
            child = getField( index );
            if( child instanceof PillButtonField ) {
                ( (PillButtonField) child ).setDrawPosition( PillButtonField.DRAWPOSITION_RIGHT );
            }
        }
        super.sublayout( maxWidth, maxHeight );
    }

    public void setSelectedField( Field selectedField )
    {
        if( _selectedField == selectedField ) {
            return; // already selected
        }

        // Clear old one
        if( _selectedField instanceof PillButtonField ) {
            ( (PillButtonField) _selectedField ).setSelected( false );
        }

        _selectedField = selectedField;

        // Select New Field
        if( _selectedField instanceof PillButtonField ) {
            ( (PillButtonField) _selectedField ).setSelected( true );
        }
    }
}
