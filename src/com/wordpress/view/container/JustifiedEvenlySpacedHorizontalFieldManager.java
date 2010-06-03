package com.wordpress.view.container;

import net.rim.device.api.ui.*;



public class JustifiedEvenlySpacedHorizontalFieldManager extends Manager 
{
	private static final int SYSTEM_STYLE_SHIFT = 32;
	
    public JustifiedEvenlySpacedHorizontalFieldManager() 
    {
        this( 0 );
    }
    
    public JustifiedEvenlySpacedHorizontalFieldManager( long style ) 
    {
        super( USE_ALL_WIDTH | style );
    }
    
    protected void sublayout( int width, int height )
    {
        int availableWidth = width;

        int numFields = getFieldCount();
        int maxPreferredWidth = 0;
        int maxHeight = 0;


        // There may be a few remaining pixels after dividing up the space
        // we must split up the space between the first and last buttons
        int fieldWidth = width / numFields;
        int firstFieldExtra = 0;
        int lastFieldExtra = 0;
        
        int unUsedWidth = width - fieldWidth * numFields;
        if( unUsedWidth > 0 ) {
            firstFieldExtra = unUsedWidth / 2;
            lastFieldExtra = unUsedWidth - firstFieldExtra;
        }
        
        int prevRightMargin = 0;
        
        // Layout the child fields, and calculate the max height
        for( int i = 0; i < numFields; i++ ) {
            
            int nextLeftMargin = 0;
            if( i < numFields - 1 ) {
                Field nextField = getField( i );
                nextLeftMargin = nextField.getMarginLeft();
            }
            
            Field currentField = getField( i );
            int leftMargin = i == 0 ? currentField.getMarginLeft() : Math.max( prevRightMargin, currentField.getMarginLeft() ) / 2;
            int rightMargin = i < numFields - 1 ? Math.max( nextLeftMargin, currentField.getMarginRight() ) / 2 : currentField.getMarginRight();
            int currentVerticalMargins = currentField.getMarginTop() + currentField.getMarginBottom();
            int currentHorizontalMargins = leftMargin + rightMargin;
            
            int widthForButton = fieldWidth;
            if( i == 0 ) {
                widthForButton = fieldWidth + firstFieldExtra;
            } else if( i == numFields -1 ) {
                widthForButton = fieldWidth + lastFieldExtra;
            }
            layoutChild( currentField, widthForButton - currentHorizontalMargins, height - currentVerticalMargins );
            maxHeight = Math.max( maxHeight, currentField.getHeight() + currentVerticalMargins );
            
            prevRightMargin = rightMargin;
            nextLeftMargin = 0;
        }

        // Now position the fields, respecting the Vertical style bits
        int usedWidth = 0;
        int y;
        prevRightMargin = 0;
        for( int i = 0; i < numFields; i++ ) {
            
            Field currentField = getField( i );
            int marginTop = currentField.getMarginTop();
            int marginBottom = currentField.getMarginBottom();
            int marginLeft = Math.max( currentField.getMarginLeft(), prevRightMargin );
            int marginRight = currentField.getMarginRight();
            
            switch( (int)( ( currentField.getStyle() & FIELD_VALIGN_MASK ) >> SYSTEM_STYLE_SHIFT ) ) {
                case (int)( FIELD_BOTTOM >> SYSTEM_STYLE_SHIFT ):
                    y = maxHeight - currentField.getHeight() - currentField.getMarginBottom();
                    break;
                case (int)( FIELD_VCENTER >> SYSTEM_STYLE_SHIFT ):
                    y = marginTop + ( maxHeight - marginTop - currentField.getHeight() - marginBottom ) >> 1;
                    break;
                default:
                    y = marginTop;
            }
            setPositionChild( currentField, usedWidth + marginLeft, y );
            usedWidth += currentField.getWidth() + marginLeft;
            prevRightMargin = marginRight;
        }
        setExtent( width, maxHeight );
    }
    
}

