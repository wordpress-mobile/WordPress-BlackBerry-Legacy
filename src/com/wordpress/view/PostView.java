package com.wordpress.view;


import java.util.Date;

import net.rim.device.api.ui.Field;
import net.rim.device.api.ui.FocusChangeListener;
import net.rim.device.api.ui.MenuItem;
import net.rim.device.api.ui.UiApplication;
import net.rim.device.api.ui.component.BasicEditField;
import net.rim.device.api.ui.component.CheckboxField;
import net.rim.device.api.ui.component.DateField;
import net.rim.device.api.ui.component.Dialog;
import net.rim.device.api.ui.component.LabelField;
import net.rim.device.api.ui.component.ObjectChoiceField;
import net.rim.device.api.ui.component.SeparatorField;
import net.rim.device.api.ui.container.HorizontalFieldManager;
import net.rim.device.api.ui.container.VerticalFieldManager;

import com.wordpress.bb.WordPressResource;
import com.wordpress.controller.BaseController;
import com.wordpress.controller.PostController;
import com.wordpress.model.Category;
import com.wordpress.model.Post;
import com.wordpress.model.PostState;
import com.wordpress.utils.MultimediaUtils;
import com.wordpress.view.component.FileSelectorPopupScreen;
import com.wordpress.view.component.HtmlTagPopupScreen;
import com.wordpress.view.component.HtmlTextField;
import com.wordpress.view.component.MultimediaPopupScreen;
import com.wordpress.view.component.NotYetImpPopupScreen;

public class PostView extends BaseView {
	
    private PostController controller; //controller associato alla view
    private Post post;    
    private PostState mState = null;

    //content of tabs summary
	private BasicEditField title;
	private ObjectChoiceField categoryChoice;
	private DateField authoredOn; 
	private CheckboxField isPublished;
	private CheckboxField isConvertLinebreaksEnabled;  
	private CheckboxField isCommentsEnabled;
	private CheckboxField isTrackbackEnabled; 	
	private HtmlTextField bodyTextBox;

	
    public PostView(PostController _controller, Post _post) {
    	super();
    	this.controller=_controller;
		this.post = _post;
		this.mState= new PostState();
        //add a screen title
        LabelField screenTitle = new LabelField(_resources.getString(WordPressResource.TITLE_POSTVIEW),
                        LabelField.ELLIPSIS | LabelField.USE_ALL_WIDTH);
        setTitle(screenTitle);
        
        title = new BasicEditField(_resources.getString(WordPressResource.LABEL_POST_TITLE), post.getTitle(), 100, Field.EDITABLE);
		String[] categories= controller.getAvailableCategories();
		int selectedCat= controller.getPostCategoryIndex();
		if(selectedCat != -1) {
			categoryChoice=new ObjectChoiceField(_resources.getString(WordPressResource.LABEL_POST_CATEGORIES), categories, selectedCat);
		} else {
			categoryChoice=new ObjectChoiceField(_resources.getString(WordPressResource.LABEL_POST_CATEGORIES), categories);
		}
        
		Date postAuth = post.getAuthoredOn();
        long datetime= (postAuth == null) ? new Date().getTime() : postAuth.getTime();
        
        authoredOn= new DateField(_resources.getString(WordPressResource.LABEL_POST_PUBLISHEDON),datetime, DateField.DATE_TIME );
		isPublished= new CheckboxField(_resources.getString(WordPressResource.LABEL_POST_ISPUBLISHED), mState.isPublished());
		isConvertLinebreaksEnabled = new CheckboxField(_resources.getString(WordPressResource.LABEL_POST_CONVERTLINEBREAK), post.isConvertLinebreaksEnabled());
		isCommentsEnabled = new CheckboxField(_resources.getString(WordPressResource.LABEL_POST_ALLOWCOMMENTS), post.isCommentsEnabled());
		isTrackbackEnabled = new CheckboxField(_resources.getString(WordPressResource.LABEL_POST_ALLOWTRACKBACKS), post.isTrackbackEnabled());
		
		add(title);
		add(categoryChoice);
		add(authoredOn);
		add(isPublished);
		add(isConvertLinebreaksEnabled);
		add(isCommentsEnabled);
		add(isTrackbackEnabled);
		bodyTextBox= new HtmlTextField(post.getBody());
        add(bodyTextBox);
        
		addMenuItem(_saveDraftPostItem);
		addMenuItem(_submitPostItem);
		addMenuItem(_photosItem);
		addMenuItem(_previewItem);
		addMenuItem(_settingsItem);
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
    			controller.displayError(e, "Error while saving post data");
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
        	UiApplication.getUiApplication().pushScreen(new NotYetImpPopupScreen());
        }
    };
    
    private MenuItem _settingsItem = new MenuItem( _resources, WordPressResource.MENUITEM_POST_SETTINGS, 110, 10) {
        public void run() {
        	UiApplication.getUiApplication().pushScreen(new NotYetImpPopupScreen());
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
		
		//categories
		Category[] availableCategories = post.getBlog().getCategories();
		if (availableCategories != null) {
			Category selectedCategory=availableCategories[categoryChoice.getSelectedIndex()];
			
			int currentPostCat= -1;
			if(post.getPrimaryCategory() != null){
				currentPostCat= Integer.parseInt(post.getPrimaryCategory().getId());
			}
			
		  if(currentPostCat != Integer.parseInt(selectedCategory.getId())){
			post.setPrimaryCategory(selectedCategory);
			//mState.setModified(true); //TODO change with multiple categories
		   }
		} else {
			post.setPrimaryCategory(null);
		}
		
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
		
		//track changes of body content
		if(bodyTextBox != null) {
			String newContent= bodyTextBox.getText();
			if(!newContent.equals(post.getBody())){
				mState.setModified(true);
				post.setBody(newContent);
			}
		}
				
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
	
	public BaseController getController() {
		return controller;
	}
}