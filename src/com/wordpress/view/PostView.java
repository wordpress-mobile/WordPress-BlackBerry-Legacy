package com.wordpress.view;


import java.util.Hashtable;
import java.util.Vector;

import net.rim.device.api.system.Characters;
import net.rim.device.api.ui.Color;
import net.rim.device.api.ui.ContextMenu;
import net.rim.device.api.ui.DrawStyle;
import net.rim.device.api.ui.Field;
import net.rim.device.api.ui.FieldChangeListener;
import net.rim.device.api.ui.Font;
import net.rim.device.api.ui.Manager;
import net.rim.device.api.ui.MenuItem;
import net.rim.device.api.ui.Ui;

import net.rim.device.api.ui.component.BasicEditField;
import net.rim.device.api.ui.component.CheckboxField;
import net.rim.device.api.ui.component.Dialog;
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
import com.wordpress.view.component.ClickableLabelField;
import com.wordpress.view.component.ColoredLabelField;
import com.wordpress.view.component.HtmlTextField;
import com.wordpress.view.component.MarkupToolBar;
import com.wordpress.view.component.MarkupToolBarTextFieldMediator;
import com.wordpress.view.container.BorderedFieldManager;
import com.wordpress.view.dialog.InquiryView;

public class PostView extends StandardBaseView {
	
    private PostController controller;
    private Post post;    
    
	private BasicEditField title;
	private HtmlTextField bodyTextBox;
	private BasicEditField tags;
	private ObjectChoiceField status;
	private ClickableLabelField categories;
	private ClickableLabelField lblPhotoNumber;
	private CheckboxField enableLocation;
	private CheckboxField isLocationPublic;
	private LabelField wordCountField;
	
    public PostView(PostController _controller, Post _post) {
    	super(_resources.getString(WordPressResource.TITLE_POSTVIEW) , MainScreen.NO_VERTICAL_SCROLL | Manager.NO_HORIZONTAL_SCROLL);
    	this.controller=_controller;
		this.post = _post;
             
        //row title
    	BorderedFieldManager rowTitle = new BorderedFieldManager(Manager.NO_HORIZONTAL_SCROLL
         		| Manager.NO_VERTICAL_SCROLL | BorderedFieldManager.BOTTOM_BORDER_NONE);
    	LabelField lblTitle = GUIFactory.getLabel(_resources.getString(WordPressResource.LABEL_TITLE), Color.BLACK);
		title = new BasicEditField("", post.getTitle(), 100, Field.EDITABLE);
        rowTitle.add(lblTitle);
        rowTitle.add(GUIFactory.createSepatorField());
        rowTitle.add(title);
        add(rowTitle);

        //The Box that shows categories - tags - media - status
        BorderedFieldManager outerManagerRowInfos = new BorderedFieldManager(Manager.NO_HORIZONTAL_SCROLL
        		| Manager.NO_VERTICAL_SCROLL | BorderedFieldManager.BOTTOM_BORDER_NONE);

        //row media files attached
        LabelField lblMedia =  new ColoredLabelField(_resources.getString(WordPressResource.TITLE_MEDIA_VIEW)+": ", Color.BLACK);
        HorizontalFieldManager rowMedia = new HorizontalFieldManager(Manager.USE_ALL_WIDTH);
        lblPhotoNumber = new ClickableLabelField("0", LabelField.FOCUSABLE | LabelField.ELLIPSIS);
        FieldChangeListener listenerPhotoNumber = new FieldChangeListener() {
        	public void fieldChanged(Field field, int context) {
        		if(context == 0)
        			controller.showPhotosView(); 
        	}
        };
        lblPhotoNumber.setChangeListener(listenerPhotoNumber);
        setNumberOfPhotosLabel(0);
        rowMedia.add(lblMedia);
        rowMedia.add(lblPhotoNumber);
        rowMedia.setMargin(5, 5, 5, 5);
        outerManagerRowInfos.add(rowMedia);
        
        
        //row categories
        HorizontalFieldManager rowCategories = new HorizontalFieldManager(Manager.USE_ALL_WIDTH);
  		LabelField lblCategories =  new ColoredLabelField(_resources.getString(WordPressResource.LABEL_POST_CATEGORIES)+": ", Color.BLACK);
        String availableCategories = controller.getPostCategoriesLabel();
        categories = new ClickableLabelField(availableCategories, LabelField.ELLIPSIS | LabelField.USE_ALL_WIDTH | LabelField.FOCUSABLE) {
        	//override context menu      
	        protected void makeContextMenu(ContextMenu contextMenu) {
	            contextMenu.addItem(_categoryContextMenuItem);      
	        }
        };
        categories.setChangeListener(new FieldChangeListener() {
        	public void fieldChanged(Field field, int context) {
        		if(context == 0)
        			controller.showCategoriesView();	 
        	}
        });
  		
  		rowCategories.add(lblCategories);
  		rowCategories.add(categories);
  		rowCategories.setMargin(5, 5, 5, 5);
  		outerManagerRowInfos.add(rowCategories);
  		
  	   //row tags
		tags = new BasicEditField(_resources.getString(WordPressResource.LABEL_POST_TAGS)+": ", post.getTags(), 100, Field.EDITABLE);
		tags.setMargin(5, 5, 5, 5);
        outerManagerRowInfos.add(tags);
        
        //row status
  		status = new ObjectChoiceField(_resources.getString(WordPressResource.LABEL_POST_STATUS)+":", controller.getStatusLabels(), controller.getPostStatusFieldIndex(), FIELD_VCENTER);
  		status.setMargin(0, 5, 5, 5);
  		outerManagerRowInfos.add(status);
        
        add(outerManagerRowInfos);
        //row location
        BorderedFieldManager locationManager = new BorderedFieldManager(
        		Manager.NO_HORIZONTAL_SCROLL
        		| Manager.NO_VERTICAL_SCROLL
        		| BorderedFieldManager.BOTTOM_BORDER_NONE);
		enableLocation = new CheckboxField(_resources.getString(WordPressResource.LABEL_LOCATION_ADD), post.isLocation());
		locationManager.add(enableLocation);
		isLocationPublic = new CheckboxField(_resources.getString(WordPressResource.LABEL_LOCATION_PUBLIC), post.isLocationPublic());
		locationManager.add(isLocationPublic);
		add(locationManager);
  		
  		//row content - decode the post body content
    	BorderedFieldManager outerManagerRowContent = new BorderedFieldManager(Manager.NO_HORIZONTAL_SCROLL
         		| Manager.NO_VERTICAL_SCROLL);

  		String buildBodyFieldContentFromHtml = post.getBody();
  		//decode the post more content
  		String extendedBody = post.getExtendedBody();
  		if(extendedBody != null && !extendedBody.trim().equals("")) {
  			String extendedBodyHTML = Characters.ENTER +"<!--more-->" + Characters.ENTER;
  			extendedBodyHTML += extendedBody;
  			buildBodyFieldContentFromHtml += extendedBodyHTML;
  		}

  		MarkupToolBarTextFieldMediator mediator = new MarkupToolBarTextFieldMediator();
  		
  		HorizontalFieldManager headerContent = new HorizontalFieldManager(Manager.NO_HORIZONTAL_SCROLL | Manager.USE_ALL_WIDTH);
  		LabelField lblPostContent = GUIFactory.getLabel(_resources.getString(WordPressResource.LABEL_POST_CONTENT), Color.BLACK, DrawStyle.ELLIPSIS);
  		int fntHeight = Font.getDefault().getHeight();
  		Font fnt = Font.getDefault().derive(Font.PLAIN, fntHeight-4, Ui.UNITS_px);
  		wordCountField = new LabelField("0", Field.USE_ALL_WIDTH | Field.FIELD_HCENTER | DrawStyle.RIGHT);
  		wordCountField.setFont(fnt);
  		mediator.setWcField(wordCountField);

  		headerContent.add(lblPostContent);
		headerContent.add(wordCountField);
		outerManagerRowContent.add(headerContent);
		outerManagerRowContent.add(GUIFactory.createSepatorField());
		
		bodyTextBox = new HtmlTextField(buildBodyFieldContentFromHtml, mediator);
		bodyTextBox.setMargin(5,0,5,0);//leave some spaces on the top & bottom
		mediator.setTextField(bodyTextBox);
		outerManagerRowContent.add(bodyTextBox);
		outerManagerRowContent.add(GUIFactory.createSepatorField());
		
		MarkupToolBar markupToolBar = new MarkupToolBar(mediator);
		mediator.setTb(markupToolBar);
		markupToolBar.attachTo(outerManagerRowContent);
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
		
		//move the focus to the title Field
		title.setFocus();
    }
    
    //set the photos number label text
    public void setNumberOfPhotosLabel(int count) {
    	lblPhotoNumber.setText(String.valueOf(count));
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
    			//post is changed
	    		if (controller.isObjectChanged()) {
	    			saveOrSendPost(false);	    				
	    		} else 
	    		{
	    			controller.backCmd();
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
    			saveOrSendPost(true);
    		} catch (Exception e) {
    			controller.displayError(e, _resources.getString(WordPressResource.ERROR_WHILE_SAVING_POST));
    		}
    	}
    };

    private void saveOrSendPost(boolean sendPost)throws Exception {
    	
		if (post.isLocation()) {

			boolean isPresent = false;

			isPresent = findPreviousLocationCustomFields();
			//location tags are already present, asks to user what we should do with old location data
			if(isPresent == true) {
				InquiryView infoView= new InquiryView(_resources.getString(WordPressResource.MESSAGE_LOCATION_OVERWRITE));
				int choice=infoView.doModal();  
				if (choice == Dialog.YES) {
					//avvia il gps
					controller.startGeoTagging(sendPost);
				} else {
					if(sendPost)
						controller.sendPostToBlog();
					else
						controller.saveDraftPost();
				}
			} //location tags are not already present, so we should only start the gps 
			else {
				//start the gps controller
				controller.startGeoTagging(sendPost);
			}
		} else {
			removeLocationCustomFields(); //remove the location field if necessary
			if(sendPost)
				controller.sendPostToBlog();
			else {
				controller.saveDraftPost();
				
			}
		}
    }
    
    /**
     * find prev location customs fieds, and update location-public field according to GUI checkbox 
     */
    private boolean findPreviousLocationCustomFields(){
		Vector customFields = post.getCustomFields();
    	int size = customFields.size();
    	boolean isPresent = false;
    	Log.debug("Found "+size +" custom fields");
    	
		for (int i = 0; i <size; i++) {
			Log.debug("Elaborating custom field # "+ i);
			try {
				Hashtable customField = (Hashtable)customFields.elementAt(i);
				
				String ID = (String)customField.get("id");
				String key = (String)customField.get("key");
				String value = (String)customField.get("value");
				Log.debug("id - "+ID);
				Log.debug("key - "+key);
				Log.debug("value - "+value);	
				
				//find the lat/lon field
				if(key.equalsIgnoreCase("geo_longitude")) {
					isPresent = true;
				} 
				
				//update the geo_public custom field
				if( key.equalsIgnoreCase("geo_public")){
					Log.debug("Updated custom field : "+ key);
					if(post.isLocationPublic())
						customField.put("value", String.valueOf(1));
					else
						customField.put("value", String.valueOf(0));
				}
				
			} catch(Exception ex) {
				Log.error("Error while Elaborating custom field # "+ i);
			}
		}
		return isPresent;
    }
    
    private void removeLocationCustomFields() {
		Log.debug(">>> removeLocationCustomFields ");
		if(post.isLocation()) {
			Log.debug("location was not removed, bc post is already marked as geotagged");
			return;
		}
		
		Vector customFields = this.post.getCustomFields();
		int size = customFields.size();
    	Log.debug("Found "+size +" custom fields");
    	
		for (int i = 0; i <size; i++) {
			Log.debug("Elaborating custom field # "+ i);
			try {
				Hashtable customField = (Hashtable)customFields.elementAt(i);
				
				String ID = (String)customField.get("id");
				String key = (String)customField.get("key");
				String value = (String)customField.get("value");
				Log.debug("id - "+ID);
				Log.debug("key - "+key);
				Log.debug("value - "+value);	
				
				//find the lat/lon field
				if(key.equalsIgnoreCase("geo_address") || key.equalsIgnoreCase("geo_public")
						|| key.equalsIgnoreCase("geo_accuracy")
						|| key.equalsIgnoreCase("geo_longitude") || key.equalsIgnoreCase("geo_latitude")) {
				
					 customField.remove("key");
					 customField.remove("value");
					 Log.debug("Removed custom field : "+ key);
				} 
			} catch(Exception ex) {
				Log.error("Error while Elaborating custom field # "+ i);
			}
		}
		Log.debug("<<< removeLocationCustomFields ");
	}
    
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
		
		if(enableLocation.isDirty()) {
			post.setLocation(enableLocation.getChecked());
			controller.setObjectAsChanged(true);
		}
		if(isLocationPublic.isDirty()) {
			post.setLocationPublic(isLocationPublic.getChecked());
			controller.setObjectAsChanged(true);
		}
	}

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