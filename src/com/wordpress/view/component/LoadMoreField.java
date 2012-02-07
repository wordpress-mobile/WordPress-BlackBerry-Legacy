package com.wordpress.view.component;

import net.rim.device.api.i18n.ResourceBundle;
import net.rim.device.api.system.EncodedImage;
import net.rim.device.api.system.GIFEncodedImage;
import net.rim.device.api.ui.Color;
import net.rim.device.api.ui.Field;
import net.rim.device.api.ui.Font;
import net.rim.device.api.ui.Graphics;
import net.rim.device.api.ui.Manager;
import net.rim.device.api.ui.component.LabelField;
import net.rim.device.api.ui.container.HorizontalFieldManager;

import com.wordpress.bb.WordPressResource;
import com.wordpress.utils.FieldDimensionUtilities;
import com.wordpress.utils.log.Log;

public class LoadMoreField extends HorizontalFieldManager {
	
	//create a variable to store the ResourceBundle for localization support
	protected static ResourceBundle _resources;
	
	static {
		//retrieve a reference to the ResourceBundle for localization support
		_resources = ResourceBundle.getBundle(WordPressResource.BUNDLE_ID, WordPressResource.BUNDLE_NAME);
	}
	
	public LoadMoreField() {
		this(Manager.USE_ALL_WIDTH | Manager.NON_FOCUSABLE);
	}

	public LoadMoreField(long style) {
		super(style);
		HorizontalFieldManager tmpContainer = new HorizontalFieldManager(Manager.NON_FOCUSABLE);
		
		LabelField labelField = new LabelField(_resources.getString(WordPressResource.CONN_LOADING_MORE),  Field.FIELD_HCENTER );
		Font fnt = Font.getDefault();
		int fntHeight = fnt.getHeight();
		fntHeight = (4*(fntHeight))/5;
		labelField.setFont(Font.getDefault().derive(Font.PLAIN, fntHeight));
		labelField.setMargin(7, 5, 7, 0);
		tmpContainer.add(labelField);
		
		GIFEncodedImage _theImage= (GIFEncodedImage)EncodedImage.getEncodedImageResource("loading-more-gif.bin");
		tmpContainer.add(new AnimatedGIFField(_theImage, Field.FIELD_VCENTER | Field.FIELD_HCENTER));

		this.add(tmpContainer);
	}

	protected void paintBackground(Graphics graphics) {
		int[] drawColors = { 0xFFFFFF, 0xD6D3D6, 0xD6D3D6, 0xFFFFFF };
		int height = getHeight();
		int width = getWidth();
		int[] X_PTS = { 0, 0, width, width };
		int[] Y_PTS = { 0, height, height, 0 };
		graphics.drawShadedFilledPath(X_PTS, Y_PTS, null, drawColors, null);
		graphics.setColor(Color.BLACK);
		graphics.drawLine(0, 0 , width, 0);
	}

	protected void sublayout( int width, int height ) 
	{
		int maxWidth   = 0;
		int numChildren = this.getFieldCount();

		if( numChildren > 1 ) {
			Log.error("The field can't contain more than one child");
			return;
		}
		
		if( isStyle( USE_ALL_WIDTH ) ) {
			// use all the width
			maxWidth = width;

		} else {

			for( int i = 0; i < numChildren; i++ ) {
				Field currentField = getField( i );
				int currentPreferredWidth = currentField.getPreferredWidth() + FieldDimensionUtilities.getBorderWidth( currentField );
				maxWidth  = Math.max( maxWidth, currentPreferredWidth );
			}
		}

		int prevTopMargin = 0;
		int usedHeight = 0;
		int x;
		for( int i = 0; i < numChildren; i++ ) {

			Field currentField = getField( i );
			int currentPreferredWidth = currentField.getPreferredWidth() + FieldDimensionUtilities.getBorderWidth( currentField );
			if( currentPreferredWidth < maxWidth ) {
				int newPadding = ( maxWidth - currentPreferredWidth ) / 2; 
				currentField.setPadding( currentField.getPaddingTop(), newPadding, currentField.getPaddingBottom(), newPadding );
			}
			layoutChild( currentField, maxWidth, height );

			usedHeight += Math.max( prevTopMargin, currentField.getMarginBottom() );
			x = ( maxWidth - currentField.getWidth() ) / 2;
			setPositionChild( currentField, x, usedHeight );
			usedHeight += currentField.getHeight();
			prevTopMargin = currentField.getMarginBottom();
		}
		setExtent( maxWidth, usedHeight );
	}
	
}
