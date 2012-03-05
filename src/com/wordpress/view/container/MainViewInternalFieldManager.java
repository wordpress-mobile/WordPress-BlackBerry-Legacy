package com.wordpress.view.container;

import net.rim.device.api.ui.*;

public class MainViewInternalFieldManager extends Manager
{
    public Field _topField;
    public Field _bottomField;
    
    private int heightAvailableForTheGrid = 0;
    
    public MainViewInternalFieldManager( Field topField, Field bottomField )
    {
        super( Field.USE_ALL_HEIGHT );
        _topField = topField;
        _bottomField = bottomField;
        
        add( _topField );
        add( _bottomField );
    }

    public void paint(Graphics graphics)
	 {
		 graphics.setBackgroundColor(0xefebef);
		 graphics.clear();
		 super.paint(graphics);
	 }
	 
    protected void sublayout( int width, int height )
    {
        Field firstField;
        Field secondField;
        firstField = _topField;
        secondField = _bottomField;

        int maxWidth = 0;
        
        int availableHeight = height;
        availableHeight -= _topField.getMarginTop();
        availableHeight -= Math.max( _topField.getMarginBottom(), _bottomField.getMarginTop() );
        availableHeight -= _bottomField.getMarginBottom();

        layoutChild( firstField, width - firstField.getMarginLeft() - firstField.getMarginRight(), availableHeight );
        maxWidth = Math.max( maxWidth, firstField.getMarginLeft() + firstField.getWidth() + firstField.getMarginRight() );
        availableHeight -= firstField.getHeight();
        
        heightAvailableForTheGrid = availableHeight;
        
        layoutChild( secondField, width - secondField.getMarginLeft() - secondField.getMarginRight(), availableHeight );
        maxWidth = Math.max( maxWidth, secondField.getMarginLeft() + secondField.getWidth() + secondField.getMarginRight() );
        availableHeight -= secondField.getHeight();
        
        if( !isStyle( Field.USE_ALL_HEIGHT ) ) {
            height -= availableHeight;
        }
        if( !isStyle( Field.USE_ALL_WIDTH ) ) {
            width = maxWidth;
        }
        
        setPositionChild( _topField, getFieldX( _topField, width ), _topField.getMarginTop() );
        setPositionChild( _bottomField, getFieldX( _bottomField, width ),  _topField.getMarginTop() + _topField.getHeight() + Math.max( _topField.getMarginBottom(), _bottomField.getMarginTop() ) );
      //  setPositionChild( _bottomField, getFieldX( _bottomField, width ), height - _bottomField.getHeight() - _bottomField.getMarginBottom() );

        setExtent( width, height );
    }


    private int getFieldX( Field field, int width )
    {
        if( field.isStyle( Field.FIELD_RIGHT ) ) {
            return width - field.getWidth() - field.getMarginRight();
        } else if( field.isStyle( Field.FIELD_RIGHT ) ) {
            return field.getMarginLeft() + ( width - field.getMarginLeft() - field.getWidth() - field.getMarginRight() ) / 2;
        } else {
            return field.getMarginLeft();
        }
    }
    
    public Field getTopField()
    {
        return _topField;
    }
    
    public Field getBottomField()
    {
        return _bottomField;
    }
    
    public void replace( Field oldField, Field newField )
    {
        if( oldField == newField ) {
            // Nothing to do
            return;
        }
        
        if( oldField == _topField ) {
            _topField = newField;
        } else if( oldField == _bottomField ) {
            _bottomField = newField;
        }
        add( newField );
        delete( oldField );
    }
    
    public int getHeightAvailableForTheGrid() {
		return heightAvailableForTheGrid;
	}

}    
