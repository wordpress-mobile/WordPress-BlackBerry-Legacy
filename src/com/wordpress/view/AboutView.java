package com.wordpress.view;


import net.rim.device.api.system.Bitmap;
import net.rim.device.api.ui.Field;
import net.rim.device.api.ui.MenuItem;
import net.rim.device.api.ui.component.BitmapField;
import net.rim.device.api.ui.component.LabelField;
import net.rim.device.api.ui.component.SeparatorField;

import com.wordpress.bb.WordPressResource;
import com.wordpress.controller.AboutController;
import com.wordpress.utils.FileUtils;
import com.wordpress.utils.PropertyUtils;

public class AboutView extends BaseView {
	//FIXME: scrolling error
	
    private final static String LICENSE_INFO = " comes with ABSOLUTELY NO WARRANTY. This is free software, and you are welcome to redistribute it under certain conditions.\n\n Contains code licensed under the General Public License 2.0.";
    private AboutController aboutController; //controller associato alla view
    
    public AboutView(AboutController _aboutController) {
    	super();
    	aboutController=_aboutController;
    	//add a screen title
        LabelField title = new LabelField(_resources.getString(WordPressResource.TITLE_APPLICATION),
                        LabelField.ELLIPSIS | LabelField.USE_ALL_WIDTH);
        setTitle(title);
        
    	//Bitmap _bitmap = Bitmap.getBitmapResource("application-icon.png");
        Bitmap _bitmap = PropertyUtils.getAppIcon();
    	
        /*String name = mainCtrl.getAppProperty("MIDlet-Name");
        String version = mainCtrl.getAppProperty("MIDlet-Version");
        */
    	String name = PropertyUtils.getAppName();
        String version = PropertyUtils.getAppVersion();
    	        
        add(new BitmapField(_bitmap, Field.FIELD_HCENTER | Field.FIELD_VCENTER));
        add(new LabelField(name + " " + version,Field.FOCUSABLE));
        add(new LabelField( "Copyright (C) 2009 XXX s.r.l.",Field.FOCUSABLE));
        add(new SeparatorField());
        add(new LabelField( FileUtils.readTxtFile("License.txt"),Field.FOCUSABLE));
        add(new LabelField( "See the " + name + " website for more information.",Field.FOCUSABLE));
        addMenuItem(_backItem);
    }
    
    //create a menu item for users to click
    protected MenuItem _backItem = new MenuItem( _resources, WordPressResource.MENUITEM_BACK, 110, 10) {
            public void run() {
            	aboutController.backCmd();
            }
    };  
}