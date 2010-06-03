package com.wordpress.view.dialog;

import net.rim.device.api.i18n.ResourceBundle;
import net.rim.device.api.system.Bitmap;
import net.rim.device.api.system.Clipboard;
import net.rim.device.api.ui.Field;
import net.rim.device.api.ui.FieldChangeListener;
import net.rim.device.api.ui.component.ButtonField;
import net.rim.device.api.ui.component.Dialog;
import net.rim.device.api.ui.component.EditField;
import net.rim.device.api.ui.container.DialogFieldManager;
import net.rim.device.api.ui.container.HorizontalFieldManager;
import net.rim.device.api.ui.text.URLTextFilter;

import com.wordpress.bb.WordPressResource;


public final class AddLinkDialog extends Dialog {

    private EditField urlField;
    private EditField descriptionField;
    
	//create a variable to store the ResourceBundle for localization support
	protected static ResourceBundle _resources;

	static {
		//retrieve a reference to the ResourceBundle for localization support
		_resources = ResourceBundle.getBundle(WordPressResource.BUNDLE_ID, WordPressResource.BUNDLE_NAME);
	}
    

    public AddLinkDialog(){
        super(Dialog.D_YES_NO, _resources.getString(WordPressResource.LABEL_ADDLINK_TITLE), Dialog.NO, Bitmap.getPredefinedBitmap(Bitmap.INFORMATION), Dialog.GLOBAL_STATUS);
        urlField = new EditField(_resources.getString(WordPressResource.LABEL_URL)+ " ", "http://", 255, EditField.EDITABLE);
        urlField.setFilter(new URLTextFilter());
        descriptionField = new EditField(_resources.getString(WordPressResource.LABEL_DESC)+ " ", "", 200, EditField.EDITABLE);
        
        ButtonField buttonPaste= new ButtonField(_resources.getString(WordPressResource.LABEL_ADDLINK_PASTE), ButtonField.CONSUME_CLICK);
        ButtonField buttonClear= new ButtonField(_resources.getString(WordPressResource.LABEL_ADDLINK_CLEAR), ButtonField.CONSUME_CLICK);
        HorizontalFieldManager clearAndPasteButtonsManager = new HorizontalFieldManager(Field.FIELD_HCENTER);
        clearAndPasteButtonsManager.add(buttonPaste);
        clearAndPasteButtonsManager.add(buttonClear);
        buttonPaste.setChangeListener(listenerPasteButton);
        buttonClear.setChangeListener(listenerClearButton);
        
        net.rim.device.api.ui.Manager delegate = getDelegate();
        if( delegate instanceof DialogFieldManager){
            DialogFieldManager dfm = (DialogFieldManager)delegate;
            net.rim.device.api.ui.Manager manager = dfm.getCustomManager();
            if( manager != null ){
                manager.insert(urlField, 0);
                manager.insert(descriptionField, 1);
                urlField.setCursorPosition(7);
                manager.insert(clearAndPasteButtonsManager, 2);
            }
        }
    }    

	private FieldChangeListener listenerPasteButton = new FieldChangeListener() {
	    public void fieldChanged(Field field, int context) {
	    	Clipboard clipboard = Clipboard.getClipboard();
	    	String content = clipboard.toString();
	    	if( content.startsWith("http")) 
	    		urlField.setText(content);
	    	else 
	    		urlField.insert(content);
	   }
	};
	
	private FieldChangeListener listenerClearButton = new FieldChangeListener() {
	    public void fieldChanged(Field field, int context) {
	    	urlField.setText("http://");
	    	urlField.setCursorPosition(7);    	   
	   }
	};
    
    public String getUrlFromField(){
      return urlField.getText();
    }
    
    public String getDescriptionFromField(){
        return descriptionField.getText();
      }
}