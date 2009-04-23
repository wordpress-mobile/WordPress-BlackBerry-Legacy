package com.wordpress.view;


import net.rim.device.api.ui.Field;
import net.rim.device.api.ui.FocusChangeListener;
import net.rim.device.api.ui.MenuItem;
import net.rim.device.api.ui.component.BasicEditField;
import net.rim.device.api.ui.component.LabelField;
import net.rim.device.api.ui.component.SeparatorField;
import net.rim.device.api.ui.container.HorizontalFieldManager;
import net.rim.device.api.ui.container.VerticalFieldManager;

import com.wordpress.bb.WordPressResource;
import com.wordpress.controller.PostController;

public class PostView extends BaseView  implements FocusChangeListener{
	
    private PostController controller; //controller associato alla view
    
    //label of tabs
	private LabelField tabSummary;
	private LabelField tabBoby;
	private LabelField tabExtended;
	private LabelField tabExcerpt;

	//spacer between tabs
	private LabelField spacer1;
	private LabelField spacer2;
	private LabelField spacer3;

	//Tabs view manager
	private VerticalFieldManager tabSummaryManager;
	private VerticalFieldManager tabBodyManager;
	private VerticalFieldManager tabExtendedManager;
	private VerticalFieldManager tabExcerptManager;
		
	//current content of the view
	private VerticalFieldManager tabArea;

	//mixed content of tbas
	private LabelField tab1Heading;
	private BasicEditField tab1Field1;
	private BasicEditField tab1Field2;
	private HtmlTextField bodyTextBox;
	private HtmlTextField extendedTextBox;
	private HtmlTextField excerptTextBox;
	    
    
    public PostView(PostController _controller) {
    	super();
    	this.controller=_controller;
    	
    	//--
		HorizontalFieldManager hManager = new HorizontalFieldManager();
		tabSummary = new LabelField(_resources.getString(WordPressResource.LABEL_TAB_POSTSUMMARY), LabelField.FOCUSABLE | LabelField.HIGHLIGHT_SELECT);
		tabBoby = new LabelField("Page 2", LabelField.FOCUSABLE | LabelField.HIGHLIGHT_SELECT);
		tabExtended = new LabelField("Page 3", LabelField.FOCUSABLE | LabelField.HIGHLIGHT_SELECT);
		tabExcerpt = new LabelField("Page 4", LabelField.FOCUSABLE | LabelField.HIGHLIGHT_SELECT);
		spacer1 = new LabelField(" | ", LabelField.NON_FOCUSABLE);
		spacer2 = new LabelField(" | ", LabelField.NON_FOCUSABLE);
		spacer3 = new LabelField(" | ", LabelField.NON_FOCUSABLE);

		tabSummary.setFocusListener(this);
		tabBoby.setFocusListener(this);
		tabExtended.setFocusListener(this);
		tabExcerpt.setFocusListener(this);
		
		hManager.add(tabSummary);
		hManager.add(spacer1);
		hManager.add(tabBoby);
		hManager.add(spacer2);
		hManager.add(tabExtended);
		hManager.add(spacer3);
		hManager.add(tabExcerpt);
		add(hManager);
		add(new SeparatorField());
		
		tabSummaryManager = new VerticalFieldManager();
		tabBodyManager = new VerticalFieldManager();
		tabExtendedManager = new VerticalFieldManager();
		tabExcerptManager = new VerticalFieldManager();

		tabArea = displayTabSummary();
		add(tabArea);
    	    	
    	//--
        addMenuItem(_backItem);
    }


	public void focusChanged(Field field, int eventType) {
		if (tabArea != null) {
			if (eventType == FOCUS_GAINED) {
				if (field == tabSummary) {
					System.out.println("Switch to Tab 1");
					delete(tabArea);
					tabArea = displayTabSummary();
					add(tabArea);
				} else if (field == tabBoby) {
					System.out.println("Switch to Tab 2");
					System.out.println("Switch to Tab 1");
					delete(tabArea);
					tabArea = displayTabBody();
					add(tabArea);
				} else if (field == tabExtended) {
					System.out.println("Switch to Tab 3");
					System.out.println("Switch to Tab 1");
					delete(tabArea);
					tabArea = displayTabExtended();
					add(tabArea);
				}else if (field == tabExcerpt) {
					System.out.println("Switch to Tab Excerpt");
					delete(tabArea);
					tabArea = displayTabExcerpt();
					add(tabArea);
				}
			}
		}
	}

	public VerticalFieldManager displayTabSummary() {
		if (tab1Heading == null) {
			tab1Heading = new LabelField("Registration");
			tabSummaryManager.add(tab1Heading);
		}
		if (tab1Field1 == null) {
			tab1Field1 = new BasicEditField("Username: ", "");
			tabSummaryManager.add(tab1Field1);
		}
		if (tab1Field2 == null) {
			tab1Field2 = new BasicEditField("Password: ", "");
			tabSummaryManager.add(tab1Field2);
		}
		return tabSummaryManager;
	}

	public VerticalFieldManager displayTabBody() {
		if(bodyTextBox == null){
			bodyTextBox= new HtmlTextField("","contenuto del body",1000);
			tabBodyManager.add(bodyTextBox);
		}
		return tabBodyManager;
	}

	public VerticalFieldManager displayTabExtended() {
		if(extendedTextBox == null){
			extendedTextBox= new HtmlTextField("","contenuto del extedend",1000);
			tabExtendedManager.add(extendedTextBox);
		}
		
		return tabExtendedManager;
	}

	public VerticalFieldManager displayTabExcerpt() {
		if(excerptTextBox == null){
			excerptTextBox= new HtmlTextField("","contenuto del excerpt",1000);
			tabExcerptManager.add(excerptTextBox);
		}
		
		return tabExcerptManager;
	}

         
    
    //create a menu item for users to click
    protected MenuItem _backItem = new MenuItem( _resources, WordPressResource.MENUITEM_BACK, 110, 10) {
            public void run() {
            	controller.backCmd();
            }
    };  
}