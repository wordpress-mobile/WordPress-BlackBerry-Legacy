package com.wordpress.view;


import net.rim.device.api.ui.Field;
import net.rim.device.api.ui.Manager;
import net.rim.device.api.ui.MenuItem;
import net.rim.device.api.ui.component.BasicEditField;
import net.rim.device.api.ui.component.LabelField;
import net.rim.device.api.ui.component.ObjectChoiceField;
import net.rim.device.api.ui.component.SeparatorField;
import net.rim.device.api.ui.container.HorizontalFieldManager;
import net.rim.device.api.ui.container.MainScreen;
import net.rim.device.api.ui.container.VerticalFieldManager;

import com.wordpress.bb.WordPressResource;
import com.wordpress.controller.BaseController;
import com.wordpress.controller.PostController;
import com.wordpress.model.Post;
import com.wordpress.view.component.HtmlTextField;
import com.wordpress.view.component.HorizontalPaddedFieldManager;

public class PostView extends BaseView {
	
    private PostController controller; //controller associato alla view
    private Post post;    

    //content of tabs summary
	private BasicEditField title;
	private HtmlTextField bodyTextBox;
	private BasicEditField tags;
	private ObjectChoiceField status;
	private LabelField categories;
	private LabelField lblPhotoNumber;
	private VerticalFieldManager manager;
	
    public PostView(PostController _controller, Post _post) {
    	super(_resources.getString(WordPressResource.TITLE_POSTVIEW) , MainScreen.NO_VERTICAL_SCROLL | Manager.NO_HORIZONTAL_SCROLL);
    	this.controller=_controller;
		this.post = _post;
        
   	  //A HorizontalFieldManager to hold the photos number label
		HorizontalPaddedFieldManager photoNumberManager = new HorizontalPaddedFieldManager(HorizontalFieldManager.NO_HORIZONTAL_SCROLL 
            | HorizontalFieldManager.NO_VERTICAL_SCROLL | HorizontalFieldManager.USE_ALL_WIDTH | HorizontalFieldManager.FIELD_HCENTER);
        lblPhotoNumber = getLabel("");
        setNumberOfPhotosLabel(0);
        photoNumberManager.add(lblPhotoNumber);
        
    	manager= new VerticalFieldManager( Field.FOCUSABLE | VerticalFieldManager.VERTICAL_SCROLL | VerticalFieldManager.VERTICAL_SCROLLBAR);          
		
        //row title
    	HorizontalPaddedFieldManager rowTitle = new HorizontalPaddedFieldManager();
		LabelField lblTitle = getLabel(_resources.getString(WordPressResource.LABEL_POST_TITLE)+":");
		title = new BasicEditField("", post.getTitle(), 100, Field.EDITABLE);
        //title.setMargin(margins);
        rowTitle.add(lblTitle);
        rowTitle.add(title);
        manager.add(rowTitle);
        manager.add(new SeparatorField());
        
        //row tags
        HorizontalPaddedFieldManager rowTags = new HorizontalPaddedFieldManager();
		LabelField lblTags = getLabel(_resources.getString(WordPressResource.LABEL_POST_TAGS)+":");
		tags = new BasicEditField("", post.getTags(), 100, Field.EDITABLE);
        rowTags.add(lblTags);
        rowTags.add(tags);
        manager.add(rowTags);
        manager.add(new SeparatorField());
        
        //row categories
        HorizontalFieldManager rowCategories = new HorizontalPaddedFieldManager(Manager.USE_ALL_WIDTH);
  		LabelField lblCategories = getLabel(_resources.getString(WordPressResource.LABEL_POST_CATEGORIES)+":");
        String availableCategories = controller.getPostCategoriesLabel();
  		categories = new LabelField(availableCategories, LabelField.ELLIPSIS | LabelField.USE_ALL_WIDTH);
  		  		
  	/*	Bitmap imgOpen = Bitmap.getBitmapResource("disclosure-indicator.png"); 
  		BitmapField bfOpenCat = new BitmapField(imgOpen, BitmapField.FOCUSABLE)
  		{			
  		    //override context menu      
	        protected void makeContextMenu(ContextMenu contextMenu) {
	            contextMenu.addItem(_categoryContextMenuItem);      
	        }
  		};
  		bfOpenCat.setMargin(margins);
  		bfOpenCat.setSpace(5, 5);
  		*/
  		rowCategories.add(lblCategories);
  		rowCategories.add(categories);
  		//rowCategories.add(bfOpenCat);
  		manager.add(rowCategories);
  		manager.add(new SeparatorField());
  		
  		//row status
        HorizontalFieldManager rowStatus = new HorizontalPaddedFieldManager();
  		LabelField lblStatus =getLabel(_resources.getString(WordPressResource.LABEL_POST_STATUS)+":");
  		status = new ObjectChoiceField("", controller.getStatusLabels(),controller.getPostStatusFieldIndex());
  		rowStatus.add(lblStatus);
  		rowStatus.add(status);
  		 
  		manager.add(rowStatus);
  		manager.add(new SeparatorField()); 
  		
  		//decode the post html
  		String buildBodyFieldContentFromHtml = controller.buildBodyFieldContentFromHtml(post.getBody());
		bodyTextBox= new HtmlTextField(buildBodyFieldContentFromHtml);
		manager.add(bodyTextBox);
		addMenuItem(_saveDraftPostItem);
		addMenuItem(_submitPostItem);
		addMenuItem(_photosItem);
		addMenuItem(_previewItem);
		addMenuItem(_settingsItem);
		addMenuItem(_categoryContextMenuItem);
		
		add(photoNumberManager);
		add(new SeparatorField());
		add(manager);
    }
    
    
    //set the photos number label text
    public void setNumberOfPhotosLabel(int count) {
    	lblPhotoNumber.setText(count + " "+_resources.getString(WordPressResource.TITLE_PHOTOSVIEW));
    }
    
    //update the cat label field
    public void updateCategoriesField(){
    	String availableCategories = controller.getPostCategoriesLabel();
   		categories.setText(availableCategories);
   		this.invalidate();
    }
    
    //save a local copy of post
    private MenuItem _saveDraftPostItem = new MenuItem( _resources, WordPressResource.MENUITEM_SAVEDRAFT, 10230, 10) {
        public void run() {
    		try {
    			savePost();
	    		if (controller.isPostChanged()) {
	    			controller.saveDraftPost();
	    		}
    		} catch (Exception e) {
    			controller.displayError(e, "Error while saving post data");
    		}
        }
    };
    
    //send post to blog
    private MenuItem _submitPostItem = new MenuItem( _resources, WordPressResource.MENUITEM_POST_SUBMIT, 10220, 10) {
        public void run() {
    		try {
    			savePost();
   				controller.sendPostToBlog();
    				
    		} catch (Exception e) {
    			controller.displayError(e, "Error Sending saving post data");
    		}
        }
    };
    
    /*
    private MenuItem _htmlItem = new MenuItem( _resources, WordPressResource.MENUITEM_POST_HTML, 100, 10) {
        public void run() {
        	UiApplication.getUiApplication().pushScreen(new HtmlTagPopupScreen());
        }
    };
*/
    
    private MenuItem _photosItem = new MenuItem( _resources, WordPressResource.MENUITEM_POST_PHOTOS, 110, 10) {
        public void run() {
        	controller.showPhotosView();
        }
    };
    
    private MenuItem _previewItem = new MenuItem( _resources, WordPressResource.MENUITEM_PREVIEW, 110, 10) {
        public void run() {
        	if(title.isDirty() || bodyTextBox.isDirty() || 
        			tags.isDirty() || status.isDirty() || categories.isDirty() || lblPhotoNumber.isDirty())
        		controller.startLocalPreview(title.getText(), bodyTextBox.getText(), tags.getText());
        	else       	
    		if (controller.isPostChanged()) {
    			controller.startLocalPreview(title.getText(), bodyTextBox.getText(), tags.getText());
    		} else {
    			controller.startRemotePreview(post.getLink(), title.getText(), bodyTextBox.getText(), tags.getText());
    		}
        }
    };
    
    private MenuItem _settingsItem = new MenuItem( _resources, WordPressResource.MENUITEM_SETTINGS, 110, 10) {
        public void run() {
        	controller.showSettingsView();
        }
    };
    	
    private MenuItem _categoryContextMenuItem = new MenuItem(_resources, WordPressResource.MENUITEM_POST_CATEGORIES, 110, 10) {
        public void run() {
        	controller.showCategoriesView();
        }
    };
    	
    	
	/*
	 * update Post data model
	 */
	private void savePost() throws Exception{	
		//track changes 
		
		//title
		String oldTitle=post.getTitle();
		if(oldTitle == null ) { //no previous title, setting the new title  
			if(!title.getText().trim().equals("")){
				post.setTitle(title.getText());
				controller.setPostAsChanged(true);
			}
		} else {
			if( !oldTitle.equals(title.getText()) ) { //title has changed
				post.setTitle(title.getText());
				controller.setPostAsChanged(true);
			}
		}
		
		//track changes of body content
		if(bodyTextBox != null) {
			String newContent= bodyTextBox.getText();
			if(!newContent.equals(post.getBody())){
				post.setBody(newContent);
				controller.setPostAsChanged(true);
			}
		}
		
		if(tags != null) {
			String newContent= tags.getText();
			if(!newContent.equals(post.getTags())){
				post.setTags(newContent);
				controller.setPostAsChanged(true);
			}
		}
		
		int selectedStatusID = status.getSelectedIndex();
		String newState= controller.getStatusKeys()[selectedStatusID];
		if (newState != post.getStatus()) {
			post.setStatus(newState);
			controller.setPostAsChanged(true);
		}
	}


    //override onClose() to display a dialog box when the application is closed    
	public boolean onClose()   {
		try {
//			savePost();

		} catch (Exception e) {
			controller.displayError(e, "Error while saving post data");
		}
		return controller.dismissView();	
    }
	
	public BaseController getController() {
		return controller;
	}
}