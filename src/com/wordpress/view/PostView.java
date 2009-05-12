package com.wordpress.view;


import net.rim.device.api.system.Bitmap;
import net.rim.device.api.ui.Color;
import net.rim.device.api.ui.ContextMenu;
import net.rim.device.api.ui.Field;
import net.rim.device.api.ui.Font;
import net.rim.device.api.ui.Graphics;
import net.rim.device.api.ui.Manager;
import net.rim.device.api.ui.MenuItem;
import net.rim.device.api.ui.UiApplication;
import net.rim.device.api.ui.XYEdges;
import net.rim.device.api.ui.component.BasicEditField;
import net.rim.device.api.ui.component.BitmapField;
import net.rim.device.api.ui.component.LabelField;
import net.rim.device.api.ui.component.SeparatorField;
import net.rim.device.api.ui.container.HorizontalFieldManager;

import com.wordpress.bb.WordPressResource;
import com.wordpress.controller.BaseController;
import com.wordpress.controller.PostController;
import com.wordpress.model.Category;
import com.wordpress.model.Post;
import com.wordpress.model.PostState;
import com.wordpress.view.component.HtmlTextField;
import com.wordpress.view.component.NotYetImpPopupScreen;

public class PostView extends BaseView {
	
    private PostController controller; //controller associato alla view
    private Post post;    
    private PostState mState = null;

    //content of tabs summary
	private BasicEditField title;
	private HtmlTextField bodyTextBox;
	private BasicEditField tags;
	private LabelField categories;
	private LabelField status;

	
    public PostView(PostController _controller, Post _post) {
    	super(_resources.getString(WordPressResource.TITLE_POSTVIEW));
    	this.controller=_controller;
		this.post = _post;
		this.mState= new PostState();
        
        //row title
        HorizontalFieldManager rowTitle = new HorizontalFieldManager();
		LabelField lblTitle = getLabel(_resources.getString(WordPressResource.LABEL_POST_TITLE));
		title = new BasicEditField("", post.getTitle(), 100, Field.EDITABLE);
        title.setMargin(margins);
        rowTitle.add(lblTitle);
        rowTitle.add(title);
        this.add(rowTitle);
        this.add(new SeparatorField());
        
        //row tags
        HorizontalFieldManager rowTags = new HorizontalFieldManager();
		LabelField lblTags = getLabel(_resources.getString(WordPressResource.LABEL_POST_TAGS));
		tags = new BasicEditField("", post.getTags(), 100, Field.EDITABLE);
		tags.setMargin(margins);
        rowTags.add(lblTags);
        rowTags.add(tags);
        this.add(rowTags);
        this.add(new SeparatorField());
        
        //row categories
        HorizontalFieldManager rowCategories = new HorizontalFieldManager(Manager.USE_ALL_WIDTH);
  		LabelField lblCategories = getLabel(_resources.getString(WordPressResource.LABEL_POST_CATEGORIES));
        String availableCategories = controller.getPostCategoriesLabel();
  		categories = new LabelField(availableCategories);
  		categories.setMargin(margins);
  		  		
  		Bitmap imgOpen = Bitmap.getBitmapResource("disclosure-indicator.png"); 
  		BitmapField bfOpenCat = new BitmapField(imgOpen, BitmapField.FOCUSABLE)
  		{			
  		    //override context menu      
	        protected void makeContextMenu(ContextMenu contextMenu) {
	            contextMenu.addItem(_categoryContextMenuItem);      
	        }
  		};
  		bfOpenCat.setMargin(margins);
  		bfOpenCat.setSpace(5, 5);
  		
  		rowCategories.add(lblCategories);
  		rowCategories.add(categories);
  		rowCategories.add(bfOpenCat);
  		this.add(rowCategories);
  		this.add(new SeparatorField());
  		
  		//row status
        HorizontalFieldManager rowStatus = new HorizontalFieldManager();
  		LabelField lblStatus =getLabel(_resources.getString(WordPressResource.LABEL_POST_STATUS));
  		status = new LabelField("???");
  		status.setMargin(margins);
  		BitmapField bfOpenStatus = new BitmapField(imgOpen, BitmapField.FOCUSABLE)
  		{			
  		    //override context menu      
	        protected void makeContextMenu(ContextMenu contextMenu) {
	            contextMenu.addItem(_statusContextMenuItem);      
	        }
  		};
  		bfOpenStatus.setMargin(margins);
  		bfOpenStatus.setSpace(5, 5);
  		
  		rowStatus.add(lblStatus);
  		rowStatus.add(status);
  		rowStatus.add(bfOpenStatus); 
  		this.add(rowStatus);
  		this.add(new SeparatorField()); 
  		
        
  		//row photos

      
		/*
        isPublished= new CheckboxField(_resources.getString(WordPressResource.LABEL_POST_ISPUBLISHED), mState.isPublished());
		isConvertLinebreaksEnabled = new CheckboxField(_resources.getString(WordPressResource.LABEL_POST_CONVERTLINEBREAK), post.isConvertLinebreaksEnabled());
		isCommentsEnabled = new CheckboxField(_resources.getString(WordPressResource.LABEL_POST_ALLOWCOMMENTS), post.isCommentsEnabled());
		isTrackbackEnabled = new CheckboxField(_resources.getString(WordPressResource.LABEL_POST_ALLOWTRACKBACKS), post.isTrackbackEnabled());
		*/
		

	/*	add(categoryChoice);
		add(authoredOn);
		add(isPublished);
		add(isConvertLinebreaksEnabled);
		add(isCommentsEnabled);
		add(isTrackbackEnabled);*/
		bodyTextBox= new HtmlTextField(post.getBody());
        add(bodyTextBox);
		addMenuItem(_saveDraftPostItem);
		addMenuItem(_submitPostItem);
		addMenuItem(_photosItem);
		addMenuItem(_previewItem);
		addMenuItem(_settingsItem);
    }
    
    //update the cat label field
    public void updateCategoriesField(){
    	String availableCategories = controller.getPostCategoriesLabel();
   		categories.setText(availableCategories);
   		this.invalidate();
    }
    
    //save a local copy of post
    private MenuItem _saveDraftPostItem = new MenuItem( _resources, WordPressResource.MENUITEM_POST_SAVEDRAFT, 10230, 10) {
        public void run() {
    		try {
    			savePost();
	    		if (mState.isModified()) {
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
    
    private MenuItem _previewItem = new MenuItem( _resources, WordPressResource.MENUITEM_POST_PREVIEW, 110, 10) {
        public void run() {
        	controller.showPreview();
        }
    };
    
    private MenuItem _settingsItem = new MenuItem( _resources, WordPressResource.MENUITEM_POST_SETTINGS, 110, 10) {
        public void run() {
        	controller.showSettingsView();
        }
    };
    	
    private MenuItem _categoryContextMenuItem = new MenuItem(_resources, WordPressResource.MENUITEM_POST_CATEGORIES, 10, 2) {
        public void run() {
        	controller.showCategoriesView();
        }
    };
    	
    private MenuItem _statusContextMenuItem = new MenuItem(_resources, WordPressResource.MENUITEM_POST_STATUS, 10, 2) {
        public void run() {
        	
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
				mState.setModified(true);
			}
		} else {
			if( !oldTitle.equals(title.getText()) ) { //title has changed
				post.setTitle(title.getText());
				mState.setModified(true);
			}
		}
		
		//track changes of body content
		if(bodyTextBox != null) {
			String newContent= bodyTextBox.getText();
			if(!newContent.equals(post.getBody())){
				mState.setModified(true);
				post.setBody(newContent);
			}
		}
		
		if(tags != null) {
			String newContent= tags.getText();
			if(!newContent.equals(post.getTags())){
				mState.setModified(true);
				post.setTags(newContent);
			}
		}
		
		//categories
		Category[] availableCategories = post.getBlog().getCategories();
		if (availableCategories != null) {
			//FIXME: doing multiple cats
		/*	Category selectedCategory=availableCategories[categoryChoice.getSelectedIndex()];
			
			int currentPostCat= -1;
			if(post.getPrimaryCategory() != null){
				currentPostCat= Integer.parseInt(post.getPrimaryCategory().getId());
			}
			
		  if(currentPostCat != Integer.parseInt(selectedCategory.getId())){
			post.setPrimaryCategory(selectedCategory);
			//mState.setModified(true); //TODO change with multiple categories
		   }
		*/
		} else {
			post.setCategories(null);
		}
	/*	
		//published date
		Date postAuthoredOn = post.getAuthoredOn();
		if(postAuthoredOn != null) {
			if(authoredOn.getDate() != post.getAuthoredOn().getTime()){
				post.setAuthoredOn(authoredOn.getDate());
				mState.setModified(true);
			}
		} else {
			//TODO add a change listener on date 
			post.setAuthoredOn(authoredOn.getDate());
		}
		
		//linebreaks
		if(isConvertLinebreaksEnabled.getChecked() != post.isConvertLinebreaksEnabled()){
			post.setConvertLinebreaksEnabled(isConvertLinebreaksEnabled.getChecked()); 
			mState.setModified(true);
		}
		
		//comments
		if(isCommentsEnabled.getChecked() != post.isCommentsEnabled()){
			post.setCommentsEnabled(isCommentsEnabled.getChecked());
			mState.setModified(true);
		}

		//trackback
		if(isTrackbackEnabled.getChecked() != post.isTrackbackEnabled()){
			post.setTrackbackEnabled(isTrackbackEnabled.getChecked());
			mState.setModified(true);
		}		
	
		mState.setPublished(isPublished.getChecked());
		*/				
	}


    //override onClose() to display a dialog box when the application is closed    
	public boolean onClose()   {
		try {
			savePost();
		} catch (Exception e) {
			controller.displayError(e, "Error while saving post data");
		}
		return controller.dismissView();	
    }

	public PostState getPostState() {
		return mState;
	}
	
	
	public void setPostState(boolean value) {
		 mState.setModified(value);
	}
	
	public BaseController getController() {
		return controller;
	}
}