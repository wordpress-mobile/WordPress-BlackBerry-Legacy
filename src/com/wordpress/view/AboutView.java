package com.wordpress.view;

import net.rim.device.api.system.Bitmap;
import net.rim.device.api.ui.Field;
import net.rim.device.api.ui.Font;
import net.rim.device.api.ui.Manager;
import net.rim.device.api.ui.component.BasicEditField;
import net.rim.device.api.ui.component.BitmapField;
import net.rim.device.api.ui.component.LabelField;
import net.rim.device.api.ui.component.Menu;
import net.rim.device.api.ui.container.VerticalFieldManager;

import com.wordpress.bb.WordPressResource;
import com.wordpress.controller.AboutController;
import com.wordpress.controller.BaseController;
import com.wordpress.utils.PropertyUtils;
import com.wordpress.utils.Tools;
import com.wordpress.view.container.TableLayoutManager;

public class AboutView extends BaseView {
	private AboutController controller;
	private LabelField urlAddr;
	private LabelField sourceCodeAddr;
	private LabelField privacyAddr;

    public AboutView(AboutController _aboutController) {
    	super(_resources.getString(WordPressResource.TITLE_ABOUT_VIEW));
		controller = _aboutController;
    	
        Bitmap _bitmap = PropertyUtils.getAppIcon();
    	//String name = PropertyUtils.getAppName();
        
    	String version = PropertyUtils.getIstance().getAppVersion();
        
        //The Header Row
        TableLayoutManager	firstRow = new TableLayoutManager(new int[] {
        		TableLayoutManager.USE_PREFERRED_SIZE,
        		TableLayoutManager.SPLIT_REMAINING_WIDTH
        }, new int[] { 2, 2 }, 0,
        Manager.USE_ALL_WIDTH);  
           	
    	BitmapField wpLogo = new BitmapField(_bitmap, Field.FIELD_HCENTER | Field.FIELD_VCENTER);
    	wpLogo.setSpace(5, 5);
    	
    	VerticalFieldManager titleManager = new VerticalFieldManager(Manager.FIELD_VCENTER | Manager.USE_ALL_WIDTH );
    	LabelField titleField = new LabelField(_resources.getString(WordPressResource.TITLE_APPLICATION));
    	Font fnt = this.getFont().derive(Font.BOLD);
    	titleField.setFont(fnt);
    	titleManager.add(titleField);
    	titleManager.add(new LabelField(_resources.getString(WordPressResource.ABOUTVIEW_VERSION)+" "+version));

    	firstRow.add(wpLogo);
    	firstRow.add(titleManager);
    	add(firstRow);
    	//end header row
    	    	
    	String mainText = _resources.getString(WordPressResource.ABOUTVIEW_DESC) 
    	+ "\n\n" 
    	+ _resources.getString(WordPressResource.ABOUTVIEW_DEVELOPED_BY)
    	+ "\n\n"
    	+ _resources.getString(WordPressResource.ABOUTVIEW_MORE_INFO);
    		   	
    	BasicEditField mainTextField = new BasicEditField(BasicEditField.READONLY);
    	mainTextField.setText(mainText);
    	mainTextField.setMargin(10, 10, 2, 10);
    	add(mainTextField);
    	
    	urlAddr = GUIFactory.createURLLabelField("http://blackberry.wordpress.org", "http://blackberry.wordpress.org", LabelField.FOCUSABLE);
    	urlAddr.setMargin(0, 10, 2, 10);
    	add(urlAddr);
    	    	
    	//Privacy
    	BasicEditField privacyTextField = new BasicEditField(BasicEditField.READONLY);
    	privacyTextField.setText(_resources.getString(WordPressResource.ABOUT_VIEW_PRIVACY_POLICY));
    	privacyTextField.setMargin(10, 10, 2, 10);
    	add(privacyTextField);
    	privacyAddr = GUIFactory.createURLLabelField("details...", "http://automattic.com/privacy", LabelField.FOCUSABLE);
    	privacyAddr.setMargin(0, 10, 2, 10);
    	add(privacyAddr);
    	
    	//source code
    	BasicEditField tosTextField = new BasicEditField(BasicEditField.READONLY);
    	tosTextField.setText(_resources.getString(WordPressResource.ABOUT_VIEW_SOURCE_CODE));
    	tosTextField.setMargin(10, 10, 2, 10);
    	add(tosTextField);
    	sourceCodeAddr = GUIFactory.createURLLabelField("blackberry.trac.wordpress.org", "http://blackberry.trac.wordpress.org", LabelField.FOCUSABLE);
    	sourceCodeAddr.setMargin(0, 10, 2, 10);
    	add(sourceCodeAddr);
    	
    	Bitmap img = Bitmap.getBitmapResource("aboutscreenfooter.png");
    	BitmapField bf = new BitmapField(img);
    	bf.setMargin(5, 0, 2, 10);
    	add(bf);
    	add(new LabelField("",Field.FOCUSABLE));
    }
    
	public boolean onMenu(int instance) {
		boolean result;
		// Prevent the context menu from being shown if focus
		// is on the url field
		if (getLeafFieldWithFocus() == urlAddr && instance == Menu.INSTANCE_CONTEXT) {
			result = false;
		} else {
			result = super.onMenu(instance);
		}
		return result;
	}
	
	public BaseController getController() {
		return controller;
	}
}