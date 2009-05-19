package com.wordpress.view;

import net.rim.device.api.system.Bitmap;
import net.rim.device.api.system.Characters;
import net.rim.device.api.ui.Color;
import net.rim.device.api.ui.Field;
import net.rim.device.api.ui.FocusChangeListener;
import net.rim.device.api.ui.Font;
import net.rim.device.api.ui.Graphics;
import net.rim.device.api.ui.Manager;
import net.rim.device.api.ui.MenuItem;
import net.rim.device.api.ui.UiApplication;
import net.rim.device.api.ui.XYEdges;
import net.rim.device.api.ui.component.BitmapField;
import net.rim.device.api.ui.component.LabelField;
import net.rim.device.api.ui.component.NullField;
import net.rim.device.api.ui.component.SeparatorField;
import net.rim.device.api.ui.container.HorizontalFieldManager;

import com.wordpress.bb.WordPressResource;
import com.wordpress.controller.BaseController;
import com.wordpress.controller.BlogController;
import com.wordpress.controller.FrontController;
import com.wordpress.view.component.NotYetImpPopupScreen;

public class BlogView extends BaseView {
	
    private BlogController controller=null;

	private MyHorizontalFieldManager rowPosts;
	private MyHorizontalFieldManager rowPages;
	private MyHorizontalFieldManager rowOptions;
	private MyHorizontalFieldManager rowComments;
	private MyHorizontalFieldManager rowRefresh;
	  
    	
	private MyHorizontalFieldManager buildEntry(String label, Bitmap img){
		MyHorizontalFieldManager currentRow = new MyHorizontalFieldManager();
		currentRow.setFocusListener(listenerFocus);
		
		XYEdges margins = new XYEdges(5,5,5,5);

		LabelField lblPosts = new LabelField(label);
       	Font fnt = this.getFont().derive(Font.BOLD);
       	lblPosts.setFont(fnt);
		lblPosts.setMargin(margins);
        
		BitmapField bfPostsFolder = new BitmapField(img);
   
		
		currentRow.add(bfPostsFolder);
		currentRow.add(lblPosts);
		currentRow.add(new NullField(LabelField.FOCUSABLE));
		return currentRow;
	}
	
	public BlogView(BlogController _controller) {
		super(_controller.getBlogName(), Field.FIELD_HCENTER);
		this.controller=_controller;
		
        //commons Img
        Bitmap imgFolder = Bitmap.getBitmapResource("drafts-folder.png");
        Bitmap imgSettings = Bitmap.getBitmapResource("settings.png"); 
       
        //posts
        rowPosts = buildEntry(_resources.getString(WordPressResource.BUTTON_POSTS), imgFolder);
        this.add(rowPosts);
        this.add(new SeparatorField());
        
        //page        
        rowPages = buildEntry(_resources.getString(WordPressResource.BUTTON_PAGES), imgFolder);
        this.add(rowPages);
        this.add(new SeparatorField());
        
        //options
        rowComments =  buildEntry(_resources.getString(WordPressResource.BUTTON_COMMENTS), imgFolder);
        this.add(rowComments);
        this.add(new SeparatorField());
        
        //options
        rowOptions =  buildEntry(_resources.getString(WordPressResource.BUTTON_OPTIONS), imgSettings);
        this.add(rowOptions);
        this.add(new SeparatorField());
               
        //refresh
        rowRefresh = buildEntry(_resources.getString(WordPressResource.BUTTON_REFRESH_BLOG), imgFolder);
        this.add(rowRefresh);
        
        addMenuItem(_goItem);
	}
	
    
    private MenuItem _goItem = new MenuItem( _resources, WordPressResource.BUTTON_OK, 220, 10) {
        public void run() {
        	doSelection();
        }
    };
    
    private void doSelection(){
    	Field fieldWithFocus = this.getFieldWithFocus();
    	if(fieldWithFocus == rowPosts) {
    		controller.showPosts();
    	} else if (fieldWithFocus == rowPages) {
    		UiApplication.getUiApplication().pushScreen(new NotYetImpPopupScreen());
    	} else if (fieldWithFocus == rowComments) {
    		controller.showComments();
    	} else if (fieldWithFocus == rowOptions) {
    		controller.showBlogOptions();
    	} else if (fieldWithFocus == rowRefresh) {
    		controller.refreshBlog();
    	}	
    }
    
	private FocusChangeListener listenerFocus = new FocusChangeListener() {
		public void focusChanged(Field field, int eventType) {
			if (eventType == FOCUS_GAINED) {
				if (field instanceof MyHorizontalFieldManager) {
					((MyHorizontalFieldManager)field).setMyColor(Color.AQUA);
				}
			} else if (eventType == FOCUS_LOST) {
				if (field instanceof MyHorizontalFieldManager) {
					((MyHorizontalFieldManager)field).setMyColor(Color.WHITE);
				}
			}
		}
	};


	protected boolean keyChar(char c, int status, int time) {
		// Close this screen if escape is selected.
		if (c == Characters.ESCAPE) {
			return  this.onClose();
		} else if (c == Characters.ENTER) {
			doSelection();
			return true;
		}

		return super.keyChar(c, status, time);
	}
	

    /*
	 // Handle trackball clicks.
	protected boolean navigationClick(int status, int time) {
		Field fieldWithFocus = this.getFieldWithFocus();
		if(fieldWithFocus == mainButtonsManager) { //focus on the top buttons, do not open menu on whell click
			return true;
		}
		else 
		 return super.navigationClick(status,time);
	}

	*/
	
	//override onClose() to by-pass the standard dialog box when the screen is closed    
	public boolean onClose()   {
		//controller.backCmd();
		FrontController.getIstance().backAndRefreshView(true);	
		return true;
	}

	public BaseController getController() {
		return controller;
	}   
	
	private class MyHorizontalFieldManager extends HorizontalFieldManager	{
		private int myColor= -1;

		public void setMyColor(int myColor) {
			this.myColor = myColor;
			invalidate();
		}

		public void paint(Graphics graphics)
	    {
			if(myColor != -1) {
				graphics.setBackgroundColor(myColor);
				graphics.clear();
			}
	        super.paint(graphics);
	    }

		public MyHorizontalFieldManager() {
			super(Manager.FIELD_HCENTER | Manager.USE_ALL_WIDTH);
		}
	};
}