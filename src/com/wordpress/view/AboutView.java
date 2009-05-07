package com.wordpress.view;


import net.rim.device.api.system.Bitmap;
import net.rim.device.api.ui.Color;
import net.rim.device.api.ui.Field;
import net.rim.device.api.ui.Font;
import net.rim.device.api.ui.Graphics;
import net.rim.device.api.ui.Manager;
import net.rim.device.api.ui.component.BitmapField;
import net.rim.device.api.ui.component.LabelField;
import net.rim.device.api.ui.component.SeparatorField;
import net.rim.device.api.ui.container.HorizontalFieldManager;
import net.rim.device.api.ui.container.VerticalFieldManager;

import com.wordpress.bb.WordPressResource;
import com.wordpress.controller.AboutController;
import com.wordpress.controller.BaseController;
import com.wordpress.utils.PropertyUtils;

public class AboutView extends BaseView {
	private AboutController controller;

	//FIXME: scrolling error
    public AboutView(AboutController _aboutController) {
    	super(_resources.getString(WordPressResource.TITLE_ABOUT_VIEW));
		controller = _aboutController;
    	
		//Bitmap _bitmap = Bitmap.getBitmapResource("application-icon.png");
        Bitmap _bitmap = PropertyUtils.getAppIcon();
    	
        
    	String name = PropertyUtils.getAppName();
        String version = PropertyUtils.getAppVersion();
    	HorizontalFieldManager row= new HorizontalFieldManager(Manager.VERTICAL_SCROLL);
    	VerticalFieldManager col1= new VerticalFieldManager();
    	VerticalFieldManager col2= new VerticalFieldManager(Manager.VERTICAL_SCROLL);
    	col2.setMargin(10, 10, 5, 5);
    	col1.setMargin(10, 10, 5, 5);
    	
    	
    	col1.add(new BitmapField(_bitmap, Field.FIELD_HCENTER | Field.FIELD_VCENTER));
    	
    	LabelField titleField = new LabelField("WordPress for BlackBerry");
    	Font fnt = this.getFont().derive(Font.BOLD);
    	titleField.setFont(fnt);
		col2.add(titleField);
    	
    	col2.add(new LabelField("version 1.0"));
    	col2.add(new LabelField("",Field.FOCUSABLE));
    	col2.add(new LabelField("An Open Source BlackBerry app for Wordpress blogs."));
    	col2.add(new LabelField());
    	col2.add(new LabelField("Designed by Automattic in XXX. Developed by Danais and the WordPress Community"));
    	col2.add(new LabelField());
    	col2.add(new LabelField("For more information or to contribute to the project, visit out web site at"));
    	LabelField urlAddr = new LabelField("blackberry.wordpress.com")
		{
		    public void paint(Graphics graphics)
		    {
		        graphics.setColor(Color.BLUE);
		        super.paint(graphics);
		    }
		};
		col2.add(urlAddr);
    	//col2.add(new LabelField());
    	Bitmap img = Bitmap.getBitmapResource("aboutscreenfooter.png");
    	BitmapField bf = new BitmapField(img);
    	
    	col2.add(new LabelField("",Field.FOCUSABLE));
    	col2.add(bf);
    	
    	row.add(col1);
    	row.add(col2);
    	this.add(row);
    }
    
	public BaseController getController() {
		return controller;
	}
}