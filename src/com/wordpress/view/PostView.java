package com.wordpress.view;


import net.rim.device.api.system.Characters;
import net.rim.device.api.ui.ContextMenu;
import net.rim.device.api.ui.Field;
import net.rim.device.api.ui.Keypad;
import net.rim.device.api.ui.Manager;
import net.rim.device.api.ui.MenuItem;
import net.rim.device.api.ui.component.BasicEditField;
import net.rim.device.api.ui.component.LabelField;
import net.rim.device.api.ui.component.ObjectChoiceField;
import net.rim.device.api.ui.container.HorizontalFieldManager;
import net.rim.device.api.ui.container.MainScreen;

import com.wordpress.bb.WordPressResource;
import com.wordpress.controller.BaseController;
import com.wordpress.controller.PostController;
import com.wordpress.model.Post;
import com.wordpress.utils.StringUtils;
import com.wordpress.utils.log.Log;
import com.wordpress.view.component.BorderedFieldManager;
import com.wordpress.view.component.HorizontalPaddedFieldManager;
import com.wordpress.view.component.HtmlTextField;

public class PostView extends StandardBaseView {
	
    private PostController controller; //controller associato alla view
    private Post post;    
    //content of tabs summary
	private BasicEditField title;
	private HtmlTextField bodyTextBox;
	private BasicEditField tags;
	private ObjectChoiceField status;
	private LabelField categories;
	private LabelField lblPhotoNumber;
	
    public PostView(PostController _controller, Post _post) {
    	super(_resources.getString(WordPressResource.TITLE_POSTVIEW) , MainScreen.NO_VERTICAL_SCROLL | Manager.NO_HORIZONTAL_SCROLL);
    	this.controller=_controller;
		this.post = _post;
        	
        //row photo #s
    	BorderedFieldManager outerManagerRowPhoto = new BorderedFieldManager(Manager.NO_HORIZONTAL_SCROLL
         		| Manager.NO_VERTICAL_SCROLL | BorderedFieldManager.BOTTOM_BORDER_NONE);    	 
    	lblPhotoNumber = getLabel("");
        setNumberOfPhotosLabel(0);
        outerManagerRowPhoto.add(lblPhotoNumber);
        add(outerManagerRowPhoto);
        
        //row title
    	BorderedFieldManager outerManagerRowTitle = new BorderedFieldManager(Manager.NO_HORIZONTAL_SCROLL
         		| Manager.NO_VERTICAL_SCROLL | BorderedFieldManager.BOTTOM_BORDER_NONE);
    	HorizontalFieldManager rowTitle = new HorizontalPaddedFieldManager();
		LabelField lblTitle = getLabel(_resources.getString(WordPressResource.LABEL_POST_TITLE)+":");
		title = new BasicEditField("", post.getTitle(), 100, Field.EDITABLE);
        //title.setMargin(margins);
        rowTitle.add(lblTitle);
        rowTitle.add(title);
        outerManagerRowTitle.add(rowTitle);
        add(outerManagerRowTitle);
                
        //row tags
    	BorderedFieldManager outerManagerRowInfos = new BorderedFieldManager(Manager.NO_HORIZONTAL_SCROLL
         		| Manager.NO_VERTICAL_SCROLL | BorderedFieldManager.BOTTOM_BORDER_NONE);
        HorizontalFieldManager rowTags = new HorizontalPaddedFieldManager();
		LabelField lblTags = getLabel(_resources.getString(WordPressResource.LABEL_POST_TAGS)+":");
		tags = new BasicEditField("", post.getTags(), 100, Field.EDITABLE);
        rowTags.add(lblTags);
        rowTags.add(tags);
        outerManagerRowInfos.add(rowTags);
        
        //row categories
        HorizontalFieldManager rowCategories = new HorizontalPaddedFieldManager(Manager.USE_ALL_WIDTH);
  		LabelField lblCategories = getLabel(_resources.getString(WordPressResource.LABEL_POST_CATEGORIES)+":");
        String availableCategories = controller.getPostCategoriesLabel();
  		categories = new LabelField(availableCategories, LabelField.ELLIPSIS | LabelField.USE_ALL_WIDTH | LabelField.FOCUSABLE)
  		{
  			protected boolean keyDown(int keycode, int time) {
  				switch (Keypad.key(keycode)) {
  				case Keypad.KEY_SPACE:
  				case Keypad.KEY_ALT:
  				case Keypad.KEY_ENTER:
  					controller.showCategoriesView();
  					return true;
  				default:
  					return false;
  				}
  			}

  			//override context menu      
	        protected void makeContextMenu(ContextMenu contextMenu) {
	            contextMenu.addItem(_categoryContextMenuItem);      
	        }
  		};
  		  		
  		rowCategories.add(lblCategories);
  		rowCategories.add(categories);
  		outerManagerRowInfos.add(rowCategories);
  		
  		//row status
        HorizontalFieldManager rowStatus = new HorizontalPaddedFieldManager();
  		LabelField lblStatus =getLabel(_resources.getString(WordPressResource.LABEL_POST_STATUS)+":");
  		status = new ObjectChoiceField("", controller.getStatusLabels(),controller.getPostStatusFieldIndex());
  		rowStatus.add(lblStatus);
  		rowStatus.add(status); 
  		outerManagerRowInfos.add(rowStatus);
  		
  		
  		add(outerManagerRowInfos);
  		
  		//row content - decode the post body content
    	BorderedFieldManager outerManagerRowContent = new BorderedFieldManager(Manager.NO_HORIZONTAL_SCROLL
         		| Manager.NO_VERTICAL_SCROLL);

  		String buildBodyFieldContentFromHtml = controller.buildBodyFieldContentFromHtml(post.getBody());
  		//decode the post more content
  		String extendedBody = post.getExtendedBody();
  		if(extendedBody != null && !extendedBody.trim().equals("")) {
  			String extendedBodyHTML = Characters.ENTER +"<!--more-->" + Characters.ENTER;
  			extendedBodyHTML += controller.buildBodyFieldContentFromHtml(extendedBody);
  			buildBodyFieldContentFromHtml += extendedBodyHTML;
  		}
  		
		bodyTextBox= new HtmlTextField(buildBodyFieldContentFromHtml);
        LabelField lblPostContent = getLabel(_resources.getString(WordPressResource.LABEL_POST_CONTENT));
		outerManagerRowContent.add(lblPostContent);
        outerManagerRowContent.add(bodyTextBox);
		add(outerManagerRowContent);
        add(new LabelField("", Field.NON_FOCUSABLE)); //space after content
		
		addMenuItem(_saveDraftPostItem);
		addMenuItem(_submitPostItem);
		addMenuItem(_photosItem);
		addMenuItem(_previewItem);
		addMenuItem(_settingsItem);
		addMenuItem(_categoryContextMenuItem);
		addMenuItem(_customFieldsMenuItem);
		addMenuItem(_excerptMenuItem);
		addMenuItem(_commentsMenuItem);
		
    }
    	
    //set the photos number label text
    public void setNumberOfPhotosLabel(int count) {
    	lblPhotoNumber.setText(count + " "+_resources.getString(WordPressResource.TITLE_MEDIA_VIEW));
    }
    
    //update the cat label field
    public void updateCategoriesField(){
    	String availableCategories = controller.getPostCategoriesLabel();
   		categories.setText(availableCategories);
   		this.invalidate();
    }
    
    //save a local copy of post
    private MenuItem _saveDraftPostItem = new MenuItem( _resources, WordPressResource.MENUITEM_SAVEDRAFT, 100220, 10) {
        public void run() {
    		try {
    			updateModel();
	    		if (controller.isObjectChanged()) {
	    			controller.saveDraftPost();
	    			//clean the state of filed into this view
	    			cleanFieldState();
	    		}
    		} catch (Exception e) {
    			controller.displayError(e, _resources.getString(WordPressResource.ERROR_WHILE_SAVING_POST));
    		}
        }
    };
    
    //send post to blog
    private MenuItem _submitPostItem = new MenuItem( _resources, WordPressResource.MENUITEM_POST_SUBMIT, 100230, 10) {
        public void run() {
    		try {
    			updateModel();
   				controller.sendPostToBlog();
    				
    		} catch (Exception e) {
    			controller.displayError(e, _resources.getString(WordPressResource.ERROR_WHILE_SAVING_POST));
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
    
    private MenuItem _photosItem = new MenuItem( _resources, WordPressResource.MENUITEM_MEDIA, 110, 10) {
        public void run() {
        	controller.showPhotosView();
        }
    };
    
    private MenuItem _previewItem = new MenuItem( _resources, WordPressResource.MENUITEM_PREVIEW, 100210, 10) {
        public void run() {
        	String categoriesLabel = controller.getPostCategoriesLabel();
        	if(title.isDirty() || bodyTextBox.isDirty() || 
        			tags.isDirty() || status.isDirty() || categories.isDirty() || lblPhotoNumber.isDirty()) {
        		//post is just changed
        		controller.startLocalPreview(title.getText(), bodyTextBox.getText(), tags.getText(), categoriesLabel); 
        	} else if (controller.isObjectChanged()) {
    			//post is changed, and the user has saved it as draft
    			controller.startLocalPreview(title.getText(), bodyTextBox.getText(), tags.getText(), categoriesLabel);
    		} else {
    			//post not changed, check if is published 
    			if ("publish".equalsIgnoreCase(post.getStatus()) ) {
    				controller.startRemotePreview(post.getLink(), title.getText(), bodyTextBox.getText(), tags.getText(), categoriesLabel);
            	} else {
        			controller.startLocalPreview(title.getText(), bodyTextBox.getText(), tags.getText(), categoriesLabel);
            	}
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
    
    private MenuItem _customFieldsMenuItem = new MenuItem(_resources, WordPressResource.MENUITEM_CUSTOM_FIELDS, 110, 10) {
        public void run() {
        	controller.showCustomFieldsView(title.getText());
        }
    };
    
    private MenuItem _excerptMenuItem = new MenuItem(_resources, WordPressResource.MENUITEM_EXCERPT, 110, 10) {
        public void run() {
        	controller.showExcerptView(title.getText());
        }
    };
       
    private MenuItem _commentsMenuItem = new MenuItem(_resources, WordPressResource.MENUITEM_COMMENTS, 110, 10) {
        public void run() {
        	controller.showComments();
        }
    };
    
    
    	
    /**
     * Change UI Fields "cleanliness" state to false.
     * A field's cleanliness state tracks when changes happen to a field.
     */
    private void cleanFieldState(){
    	title.setDirty(false);
    	bodyTextBox.setDirty(false);
    	tags.setDirty(false);
    	status.setDirty(false);
    }
    
	/*
	 * Update Post data model and Track post changes.
	 * 
	 * Categories changes are tracked into controller
	 * Photos changes are tracked into controller 
	 */
	private void updateModel() throws Exception{	

		if(title.isDirty()) {
			post.setTitle(title.getText());
			controller.setObjectAsChanged(true);
			Log.trace("title dirty");
		}
		
		if(bodyTextBox.isDirty()) {
			String newContent= bodyTextBox.getText();
			
			String tagMore = null;
			if(newContent.indexOf("<!--more-->") > -1) {
				tagMore = "<!--more-->";
			} else if(newContent.indexOf("<!--More-->") > -1) {
				tagMore = "<!--More-->";
			}else if(newContent.indexOf("<!--MORE-->") > -1) {
				tagMore = "<!--MORE-->";
			}
			
			//check for the more tag
			if( tagMore != null ) {
				Log.trace("found Extended body");
				String[] split = StringUtils.split(newContent, tagMore);
				post.setBody(split[0]);
				String extended = "";
				//if there are > 1 tags more
				for (int i = 1; i < split.length; i++) {
					extended+=split[i];
				}
				post.setExtendedBody(extended);
			} 
			else //no tag more
				post.setBody(newContent);
			
			
			controller.setObjectAsChanged(true);
			Log.trace("bodyTextBox dirty");
		}
		
		if(tags.isDirty()) {
			String newContent= tags.getText();
			post.setTags(newContent);
			controller.setObjectAsChanged(true);
			Log.trace("tags dirty");
		}
		
		if(status.isDirty()) {
			int selectedStatusID = status.getSelectedIndex();
			String newState= controller.getStatusKeys()[selectedStatusID];
			post.setStatus(newState);
			controller.setObjectAsChanged(true);
			Log.trace("status dirty");
		}
	}


    //override onClose() to display a dialog box when the application is closed    
	public boolean onClose()   {
		try {
			updateModel();
		} catch (Exception e) {
			controller.displayError(e, _resources.getString(WordPressResource.ERROR_WHILE_SAVING_POST));
		}
		return controller.dismissView();	
    }
	
	public BaseController getController() {
		return controller;
	}
}