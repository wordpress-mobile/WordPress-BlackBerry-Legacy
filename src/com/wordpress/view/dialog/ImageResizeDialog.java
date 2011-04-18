package com.wordpress.view.dialog;

import net.rim.device.api.i18n.ResourceBundle;
import net.rim.device.api.system.Bitmap;
import net.rim.device.api.ui.Field;
import net.rim.device.api.ui.FocusChangeListener;
import net.rim.device.api.ui.Manager;
import net.rim.device.api.ui.component.BasicEditField;
import net.rim.device.api.ui.component.Dialog;
import net.rim.device.api.ui.component.ObjectChoiceField;
import net.rim.device.api.ui.container.DialogFieldManager;
import net.rim.device.api.ui.container.VerticalFieldManager;

import com.wordpress.bb.WordPressResource;
import com.wordpress.model.Blog;
import com.wordpress.model.BlogInfo;
import com.wordpress.utils.ImageUtils;
import com.wordpress.utils.log.Log;


public class ImageResizeDialog extends Dialog {
	//create a variable to store the ResourceBundle for localization support
	protected static ResourceBundle _resources;

	static {
		//retrieve a reference to the ResourceBundle for localization support
		_resources = ResourceBundle.getBundle(WordPressResource.BUNDLE_ID, WordPressResource.BUNDLE_NAME);
	}
	
	private VerticalFieldManager  rowResizePhotos;
	private ObjectChoiceField resizeOpt;
	private BasicEditField imageResizeWidthField;
	private BasicEditField imageResizeHeightField;
	private Integer imageResizeWidth;
	private Integer imageResizeHeight;
	private final Blog blog;

	public ImageResizeDialog(Blog blog){
		super(Dialog.D_OK, _resources.getString(WordPressResource.LABEL_RESIZE_DIMENSION), Dialog.OK, 
				Bitmap.getPredefinedBitmap(Bitmap.QUESTION), Dialog.GLOBAL_STATUS);
		
		this.blog = blog;

	    imageResizeWidth = blog.getImageResizeWidth();
        imageResizeHeight = blog.getImageResizeHeight();
		
		rowResizePhotos = new VerticalFieldManager(
				Manager.NO_HORIZONTAL_SCROLL
				| Manager.NO_VERTICAL_SCROLL);

		String[] resizeOptLabelsFromBlog = blog.getBlogImageResizeLabels(); //read the default resize settings from the blog
		String[] resizeOptLabels = new String[resizeOptLabelsFromBlog.length+1];
		System.arraycopy(resizeOptLabelsFromBlog, 0, resizeOptLabels, 0, resizeOptLabelsFromBlog.length);
		resizeOptLabels[resizeOptLabelsFromBlog.length] = _resources.getString(WordPressResource.LABEL_ORIGINAL);

		resizeOpt = new ObjectChoiceField(" ", resizeOptLabels, resizeOptLabels.length-1); //select original as default
		rowResizePhotos.add(resizeOpt); 
		addImageResizeWidthField();
		addImageResizeHeightField();

		net.rim.device.api.ui.Manager delegate = getDelegate();
		if( delegate instanceof DialogFieldManager){
			DialogFieldManager dfm = (DialogFieldManager)delegate;
			net.rim.device.api.ui.Manager manager = dfm.getCustomManager();
			if( manager != null ){
				manager.insert(rowResizePhotos, 0);
			}
		}
	}

	
	public int[] getResizeDim(){

		int selectedResizeOption = resizeOpt.getSelectedIndex();

		switch (selectedResizeOption) {

		case BlogInfo.SMALL_IMAGE_RESIZE_SETTING:
		case BlogInfo.MEDIUM_IMAGE_RESIZE_SETTING:
		case BlogInfo.LARGE_IMAGE_RESIZE_SETTING:
			return blog.getDefaultImageResizeSettings(selectedResizeOption);

		case BlogInfo.CUSTOM_IMAGE_RESIZE_SETTING: //do not read the custom resize setting from the blog. we should read the UI values
			try {
				int imageResizeWidth = Integer.valueOf(imageResizeWidthField.getText()).intValue();
				int imageResizeHeight = Integer.valueOf(imageResizeHeightField.getText()).intValue();
				//Before saving we should do an additional check over img resize width and height.
				//it is necessary when user put a value into width/height field and then press backbutton;
				//the focus lost on those fields is never fired....
				int[] keepAspectRatio = ImageUtils.keepAspectRatio(imageResizeWidth, imageResizeHeight);
				imageResizeWidth = keepAspectRatio[0];
				imageResizeHeight = keepAspectRatio[1];
				return new int[]{imageResizeWidth, imageResizeHeight};
			} catch (Exception e) {
				return new int[]{0,0}; //original
			}
		default:
			return new int[]{0,0}; //original
		}		
	}
	
	private void addImageResizeWidthField() {
        imageResizeWidthField = new BasicEditField(
        		_resources.getString(WordPressResource.LABEL_CUSTOM_RESIZE_IMAGE_WIDTH)+": ", 
        		(imageResizeWidth == null ? "" : imageResizeWidth.toString()), 
        		4, 
        		Field.EDITABLE | BasicEditField.FILTER_NUMERIC);
        
        imageResizeWidthField.setFocusListener(listenerImageResizeWidthField);
       	rowResizePhotos.add(imageResizeWidthField);
	}

	private void addImageResizeHeightField() {
	    imageResizeHeightField = new BasicEditField(
	    		_resources.getString(WordPressResource.LABEL_CUSTOM_RESIZE_IMAGE_HEIGHT)+": ", 
	    		(imageResizeHeight == null ? "" : imageResizeHeight.toString()), 
	    		4, 
	    		Field.EDITABLE | BasicEditField.FILTER_NUMERIC);
	    
	    imageResizeHeightField.setFocusListener(listenerImageResizeHeightField);
    	rowResizePhotos.add(imageResizeHeightField);
	}
	
	
	// Recalculate the image resize height whenever the image resize width changes. Aspect ratio is fixed.
	private FocusChangeListener listenerImageResizeWidthField = new FocusChangeListener() {
	    public void focusChanged(Field field, int eventType) {
	    	if((eventType == FocusChangeListener.FOCUS_LOST) && (imageResizeWidthField.isDirty())) {
		    	try {
		    		int newWidth = Integer.parseInt(imageResizeWidthField.getText());
		    		if(newWidth == 0) {
		    			imageResizeWidthField.setText(String.valueOf(ImageUtils.DEFAULT_RESIZE_WIDTH));
		    			imageResizeHeightField.setText(String.valueOf(ImageUtils.DEFAULT_RESIZE_HEIGHT));
		    		}
		    		else {
		    			int newHeight = (int)(newWidth * 0.75);
		    			imageResizeHeightField.setText(Integer.toString(newHeight));
		    		}
		    	}
		    	catch(NumberFormatException e) {
		    		Log.error("Unexpected condition: ImageResizeWidthField was not numeric in BlogOptionsView.");
		    	}
	    	}
	   }
	};

	// Recalculate the image resize width whenever the image resize height changes. Aspect ratio is fixed.
	private FocusChangeListener listenerImageResizeHeightField = new FocusChangeListener() {
	    public void focusChanged(Field field, int eventType) {
	    	if((eventType == FocusChangeListener.FOCUS_LOST) && (imageResizeHeightField.isDirty())) {
		    	try {
		    		int newHeight = Integer.parseInt(imageResizeHeightField.getText());
		    		if(newHeight == 0) {
		    			imageResizeWidthField.setText(String.valueOf(ImageUtils.DEFAULT_RESIZE_WIDTH));
		    			imageResizeHeightField.setText(String.valueOf(ImageUtils.DEFAULT_RESIZE_HEIGHT));
		    		}
		    		else {
		    			int newWidth = (int)((newHeight * 1.3333) + 1);
		    			imageResizeWidthField.setText(Integer.toString(newWidth));
		    		}
		    	}
		    	catch(NumberFormatException e) {
		    		Log.error("Unexpected condition: ImageResizeHeightField was not numeric in BlogOptionsView.");
		    	}
	    	}
	   }
	};
	
}