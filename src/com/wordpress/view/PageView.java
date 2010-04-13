package com.wordpress.view;

import net.rim.device.api.system.Characters;
import net.rim.device.api.ui.Color;
import net.rim.device.api.ui.Field;
import net.rim.device.api.ui.Manager;
import net.rim.device.api.ui.MenuItem;
import net.rim.device.api.ui.component.BasicEditField;
import net.rim.device.api.ui.component.LabelField;
import net.rim.device.api.ui.component.ObjectChoiceField;
import net.rim.device.api.ui.container.HorizontalFieldManager;
import net.rim.device.api.ui.container.MainScreen;
import net.rim.device.api.ui.text.TextFilter;

import com.wordpress.bb.WordPressResource;
import com.wordpress.controller.BaseController;
import com.wordpress.controller.PageController;
import com.wordpress.model.Page;
import com.wordpress.utils.StringUtils;
import com.wordpress.utils.log.Log;
import com.wordpress.view.component.HorizontalPaddedFieldManager;
import com.wordpress.view.component.HtmlTextField;
import com.wordpress.view.container.BorderedFieldManager;

public class PageView extends StandardBaseView {
	
    private PageController controller;
    private Page page;   
    //content of tabs summary
	private BasicEditField title;
	private HtmlTextField bodyTextBox;
	private ObjectChoiceField status;
	private LabelField lblPhotoNumber;
	private BasicEditField pageOrderField;
	private ObjectChoiceField parentPageField;
	private ObjectChoiceField pageTemplateField;
	
    public PageView(PageController _controller, Page _page) {
    	super(_resources.getString(WordPressResource.TITLE_POSTVIEW) , MainScreen.NO_VERTICAL_SCROLL | Manager.NO_HORIZONTAL_SCROLL);
    	this.controller=_controller;
		this.page = _page;
        
        //row photo #s
    	BorderedFieldManager outerManagerRowPhoto = new BorderedFieldManager(Manager.NO_HORIZONTAL_SCROLL
         		| Manager.NO_VERTICAL_SCROLL | BorderedFieldManager.BOTTOM_BORDER_NONE);    	 
    	lblPhotoNumber = GUIFactory.getLabel("", Color.BLACK);
        setNumberOfPhotosLabel(0);
        outerManagerRowPhoto.add(lblPhotoNumber);
        add(outerManagerRowPhoto);
        
        //row title
    	BorderedFieldManager outerManagerRowTitle = new BorderedFieldManager(Manager.NO_HORIZONTAL_SCROLL
         		| Manager.NO_VERTICAL_SCROLL | BorderedFieldManager.BOTTOM_BORDER_NONE);
        HorizontalFieldManager rowTitle = new HorizontalPaddedFieldManager();
		LabelField lblTitle = GUIFactory.getLabel(_resources.getString(WordPressResource.LABEL_TITLE)+":", Color.BLACK);
		title = new BasicEditField("", page.getTitle(), 100, Field.EDITABLE);
        rowTitle.add(lblTitle);
        rowTitle.add(title);
        outerManagerRowTitle.add(rowTitle);
        add(outerManagerRowTitle);

        //opts Manager
    	BorderedFieldManager outerManagerRowInfos = new BorderedFieldManager(Manager.NO_HORIZONTAL_SCROLL
         		| Manager.NO_VERTICAL_SCROLL | BorderedFieldManager.BOTTOM_BORDER_NONE);
    	add(outerManagerRowInfos); 

    	//row status
  		status = new ObjectChoiceField(_resources.getString(WordPressResource.LABEL_POST_STATUS)+":", 
  				controller.getStatusLabels(), 
  				controller.getPageStatusFieldIndex()
  				);
  		status.setMargin(0, 5, 5, 5);
  		outerManagerRowInfos.add(status);

  		//row parentPage
  		parentPageField = new ObjectChoiceField(_resources.getString(WordPressResource.LABEL_PARENT_PAGE)+":", 
  				controller.getParentPagesTitle(), 
  				controller.getParentPageFieldIndex()
  				);
  		parentPageField.setMargin(5, 5, 5, 5);
  		outerManagerRowInfos.add(parentPageField);
  	  		
  		//row pageTemplate
  		pageTemplateField = new ObjectChoiceField(_resources.getString(WordPressResource.LABEL_PAGE_TEMPLATE)+": ", 
  				controller.getPageTemplateLabels(), 
  				controller.getPageTemplateFieldIndex()
  				);
  		pageTemplateField.setMargin(5, 5, 5, 5);
  		outerManagerRowInfos.add(pageTemplateField);
  		
  		//row pageOrder
  		int pageOrder = page.getWpPageOrder();
  		if(pageOrder < 0) pageOrder=0;
  		pageOrderField = new BasicEditField(_resources.getString(WordPressResource.LABEL_PAGE_ORDER)+": ",
  				String.valueOf(pageOrder) , 
  				100, 
  				Field.EDITABLE);
  		pageOrderField.setFilter(TextFilter.get(TextFilter.NUMERIC));
  		pageOrderField.setMargin(5, 5, 5, 5);
  		outerManagerRowInfos.add(pageOrderField);
  		
  		//row content - decode the page body content
    	BorderedFieldManager outerManagerRowContent = new BorderedFieldManager(Manager.NO_HORIZONTAL_SCROLL
         		| Manager.NO_VERTICAL_SCROLL);
  		String buildBodyFieldContentFromHtml = controller.buildBodyFieldContentFromHtml(page.getDescription());
		
  		//decode the text more content
  		String extendedBody = page.getMtTextMore();
  		if(extendedBody != null && !extendedBody.trim().equals("")) {
  			String extendedBodyHTML = Characters.ENTER +"<!--more-->" + Characters.ENTER;
  			extendedBodyHTML += controller.buildBodyFieldContentFromHtml(extendedBody);
  			buildBodyFieldContentFromHtml += extendedBodyHTML;
  		}
		bodyTextBox= new HtmlTextField(buildBodyFieldContentFromHtml);
		
		
		LabelField lblPageContent = GUIFactory.getLabel(_resources.getString(WordPressResource.LABEL_CONTENT), Color.BLACK);
		outerManagerRowContent.add(lblPageContent);
		outerManagerRowContent.add(GUIFactory.createSepatorField());
		outerManagerRowContent.add(bodyTextBox);
		add(outerManagerRowContent);
		
		add(new LabelField("", Field.NON_FOCUSABLE)); //space after content
		
		addMenuItem(_saveDraftItem);
		addMenuItem(_submitItem);
		addMenuItem(_photosItem);
		addMenuItem(_previewItem);
		addMenuItem(_settingsItem);
		addMenuItem(_customFieldsMenuItem);
    }
    
    
    //set the photos number label text
    public void setNumberOfPhotosLabel(int count) {
    	lblPhotoNumber.setText(count + " "+_resources.getString(WordPressResource.TITLE_MEDIA_VIEW));
    }
        
    //save a local copy of post
    private MenuItem _saveDraftItem = new MenuItem( _resources, WordPressResource.MENUITEM_SAVEDRAFT, 100220, 10) {
        public void run() {
    		try {
    			updateModel();
	    		if (controller.isObjectChanged()) {
	    			controller.saveDraftPage();
	    			//clean the state of filed into this view
	    			cleanFieldState();
	    		}
    		} catch (Exception e) {
    			controller.displayError(e, _resources.getString(WordPressResource.ERROR_WHILE_SAVING_PAGE));
    		}
        }
    };
    
    //send post to blog
    private MenuItem _submitItem = new MenuItem( _resources, WordPressResource.MENUITEM_POST_SUBMIT, 100230, 10) {
        public void run() {
    		try {
    			updateModel();
   				controller.sendPageToBlog();
    				
    		} catch (Exception e) {
    			controller.displayError(e, _resources.getString(WordPressResource.ERROR_WHILE_SAVING_PAGE));
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
    
    private MenuItem _customFieldsMenuItem = new MenuItem(_resources, WordPressResource.MENUITEM_CUSTOM_FIELDS, 110, 10) {
        public void run() {
        	controller.showCustomFieldsView(title.getText());
        }
    };
    
    private MenuItem _photosItem = new MenuItem( _resources, WordPressResource.MENUITEM_MEDIA, 110, 10) {
        public void run() {
        	controller.showPhotosView();
        }
    };
    
    private MenuItem _previewItem = new MenuItem( _resources, WordPressResource.MENUITEM_PREVIEW, 100210, 10) {
        public void run() {
        	
        	if(title.isDirty() || bodyTextBox.isDirty() || 
        			status.isDirty() || lblPhotoNumber.isDirty()) {
        		//page is just changed
        		controller.startLocalPreview(title.getText(), bodyTextBox.getText(), "", "");
        	} else if (controller.isObjectChanged()) {
        		//page is changed, and the user has saved it as draft
    			controller.startLocalPreview(title.getText(), bodyTextBox.getText(), "", "");
    		} else {
    			//page not changed, check if is published 
    			if ("publish".equalsIgnoreCase(page.getPageStatus()) ) {
    				controller.startRemotePreview(page.getLink(), title.getText(), bodyTextBox.getText(), "", "");
            	} else {
            		controller.startLocalPreview(title.getText(), bodyTextBox.getText(), "", "");
            	}
    		}
        }
    };
    
    private MenuItem _settingsItem = new MenuItem( _resources, WordPressResource.MENUITEM_SETTINGS, 110, 10) {
        public void run() {
        	controller.showSettingsView();
        }
    };
    	
    
    /**
     * Change UI Fields "cleanliness" state to false.
     * A field's cleanliness state tracks when changes happen to a field.
     */
    private void cleanFieldState(){
    	title.setDirty(false);
    	bodyTextBox.setDirty(false);
    	status.setDirty(false);
    	pageOrderField.setDirty(false);
    	parentPageField.setDirty(false);
    	pageTemplateField.setDirty(false);
    }
    
       	
	/*
	 * Update Page data model and Track changes.
	 * 
	 * Photos changes are tracked into controller 
	 */
	private void updateModel() throws Exception{	
		
		if(title.isDirty()){
			page.setTitle(title.getText());
			controller.setObjectAsChanged(true);
			Log.trace("title dirty");
		}
		
		if(bodyTextBox.isDirty()) {
		/*	String newContent= bodyTextBox.getText();
			page.setDescription(newContent);
			controller.setObjectAsChanged(true);
			Log.trace("bodyTextBox dirty");*/
			
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
				Log.trace("found Extended page body");
				String[] split = StringUtils.split(newContent, tagMore);
				page.setDescription(split[0]);
				String extended = "";
				//if there are > 1 tags more
				for (int i = 1; i < split.length; i++) {
					extended+=split[i];
				}
				page.setMtTextMore(extended);
			} 
			else //no tag more
				page.setDescription(newContent);
			
			
			controller.setObjectAsChanged(true);
			Log.trace("bodyTextBox dirty");
		}
		
		if(status.isDirty()) {
			int selectedStatusID = status.getSelectedIndex();
			String newState= controller.getStatusKeys()[selectedStatusID];
			page.setPageStatus(newState);
			controller.setObjectAsChanged(true);
			Log.trace("status dirty");
		}
		
		if(pageOrderField.isDirty()){
			page.setWpPageOrder(Integer.parseInt(pageOrderField.getText()));
			pageOrderField.setDirty(false);
			controller.setObjectAsChanged(true);
			Log.trace("pageOrderField dirty");
		}
		
		if(parentPageField.isDirty()) {
			int selectedIndex = parentPageField.getSelectedIndex();
			controller.setParentPageID(selectedIndex);
			pageOrderField.setDirty(false);
			controller.setObjectAsChanged(true);
			Log.trace("parentPageField dirty");
		}
		
		//page template
		if(pageTemplateField.isDirty()) {
			Log.trace("pageTemplateField dirty");
			int selectedTemplateFieldID = pageTemplateField.getSelectedIndex();
			if(selectedTemplateFieldID > -1) {
				String pageTemplate= controller.getPageTemplateKeys()[selectedTemplateFieldID];
				if (pageTemplate != page.getWpPageTemplate()) {
					page.setWpPageTemplate(pageTemplate);
					controller.setObjectAsChanged(true);
				}
			}
		}
		
	}


	public boolean onClose()   {
		try {
			updateModel();
		} catch (Exception e) {
			controller.displayError(e, _resources.getString(WordPressResource.ERROR_WHILE_SAVING_PAGE));
		}
		return controller.dismissView();	
    }
	
	public BaseController getController() {
		return controller;
	}
}