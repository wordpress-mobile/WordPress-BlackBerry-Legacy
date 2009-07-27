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
import net.rim.device.api.ui.text.TextFilter;

import com.wordpress.bb.WordPressResource;
import com.wordpress.controller.BaseController;
import com.wordpress.controller.PageController;
import com.wordpress.model.Page;
import com.wordpress.utils.log.Log;
import com.wordpress.view.component.HorizontalPaddedFieldManager;
import com.wordpress.view.component.HtmlTextField;

public class PageView extends BaseView {
	
    private PageController controller; //controller associato alla view
    private Page page;    

    //content of tabs summary
    private VerticalFieldManager manager;
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
        
   	  //A HorizontalFieldManager to hold the photos number label
        HorizontalFieldManager photoNumberManager = new HorizontalPaddedFieldManager(HorizontalFieldManager.NO_HORIZONTAL_SCROLL 
            | HorizontalFieldManager.NO_VERTICAL_SCROLL | HorizontalFieldManager.USE_ALL_WIDTH | HorizontalFieldManager.FIELD_HCENTER);
        lblPhotoNumber = getLabel("");
        setNumberOfPhotosLabel(0);
        photoNumberManager.add(lblPhotoNumber);
        
    	manager= new VerticalFieldManager( Field.FOCUSABLE | VerticalFieldManager.VERTICAL_SCROLL | VerticalFieldManager.VERTICAL_SCROLLBAR);          
		
        //row title
        HorizontalFieldManager rowTitle = new HorizontalPaddedFieldManager();
		LabelField lblTitle = getLabel(_resources.getString(WordPressResource.LABEL_POST_TITLE)+":");
		title = new BasicEditField("", page.getTitle(), 100, Field.EDITABLE);
        rowTitle.add(lblTitle);
        rowTitle.add(title);
        manager.add(rowTitle);
        manager.add(new SeparatorField());
             		
  		//row status
        HorizontalFieldManager rowStatus = new HorizontalPaddedFieldManager();
  		LabelField lblStatus =getLabel(_resources.getString(WordPressResource.LABEL_POST_STATUS)+":");
  		status = new ObjectChoiceField("", controller.getStatusLabels(),controller.getPageStatusFieldIndex());
  		rowStatus.add(lblStatus);
  		rowStatus.add(status);
  		manager.add(rowStatus);
  		manager.add(new SeparatorField()); 

  		//row parentPage
        HorizontalFieldManager rowParentPage = new HorizontalPaddedFieldManager();
  		LabelField lblParentPage =getLabel(_resources.getString(WordPressResource.LABEL_PARENT_PAGE)+":");
  		parentPageField = new ObjectChoiceField("", controller.getParentPagesTitle(), controller.getParentPageFieldIndex());
  		rowParentPage.add(lblParentPage);
  		rowParentPage.add(parentPageField);
  		manager.add(rowParentPage);
  		manager.add(new SeparatorField());
  		
  		//row pageTemplate
        HorizontalFieldManager rowPageTemplate = new HorizontalPaddedFieldManager();
  		LabelField lblPageTemplate =getLabel(_resources.getString(WordPressResource.LABEL_PAGE_TEMPLATE)+":");
  		pageTemplateField = new ObjectChoiceField("", controller.getPageTemplateLabels(), controller.getPageTemplateFieldIndex());
  		rowPageTemplate.add(lblPageTemplate);
  		rowPageTemplate.add(pageTemplateField);
  		manager.add(rowPageTemplate);
  		manager.add(new SeparatorField());
  		
  		//row pageOrder
        HorizontalFieldManager rowPageOrder = new HorizontalPaddedFieldManager();
  		LabelField lblPageOrder = getLabel(_resources.getString(WordPressResource.LABEL_PAGE_ORDER)+":");
  		int pageOrder = page.getWpPageOrder();
  		if(pageOrder < 0) pageOrder=0;
  		pageOrderField = new BasicEditField("",String.valueOf(pageOrder) , 100, Field.EDITABLE);
  		pageOrderField.setFilter(TextFilter.get(TextFilter.NUMERIC));
  		rowPageOrder.add(lblPageOrder);
  		rowPageOrder.add(pageOrderField);
  		manager.add(rowPageOrder);
  		manager.add(new SeparatorField());
  		
  		String buildBodyFieldContentFromHtml = controller.buildBodyFieldContentFromHtml(page.getDescription());
		bodyTextBox= new HtmlTextField(buildBodyFieldContentFromHtml);
		manager.add(bodyTextBox);
		addMenuItem(_saveDraftItem);
		addMenuItem(_submitItem);
		addMenuItem(_photosItem);
		addMenuItem(_previewItem);
		addMenuItem(_settingsItem);
		
		add(photoNumberManager);
		add(new SeparatorField());
		add(manager);
    }
    
    
    //set the photos number label text
    public void setNumberOfPhotosLabel(int count) {
    	lblPhotoNumber.setText(count + " "+_resources.getString(WordPressResource.TITLE_PHOTOSVIEW));
    }
    

    
    //save a local copy of post
    private MenuItem _saveDraftItem = new MenuItem( _resources, WordPressResource.MENUITEM_SAVEDRAFT, 10230, 10) {
        public void run() {
    		try {
    			updateModel();
	    		if (controller.isPageChanged()) {
	    			controller.saveDraftPage();
	    		}
    		} catch (Exception e) {
    			controller.displayError(e, "Error while saving data");
    		}
        }
    };
    
    //send post to blog
    private MenuItem _submitItem = new MenuItem( _resources, WordPressResource.MENUITEM_POST_SUBMIT, 10220, 10) {
        public void run() {
    		try {
    			updateModel();
   				controller.sendPageToBlog();
    				
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
        			status.isDirty() || lblPhotoNumber.isDirty()) {
        		//page is just changed
        		controller.startLocalPreview(title.getText(), bodyTextBox.getText(), "");
        	} else if (controller.isPageChanged()) {
        		//page is changed, and the user has saved it as draft
    			controller.startLocalPreview(title.getText(), bodyTextBox.getText(), "");
    		} else {
    			//page not changed, check if is published 
    			if ("publish".equalsIgnoreCase(page.getPageStatus()) ) {
    				controller.startRemotePreview(page.getLink(), title.getText(), bodyTextBox.getText(), "");
            	} else {
            		controller.startLocalPreview(title.getText(), bodyTextBox.getText(), "");
            	}
    		}
        }
    };
    
    private MenuItem _settingsItem = new MenuItem( _resources, WordPressResource.MENUITEM_SETTINGS, 110, 10) {
        public void run() {
        	controller.showSettingsView();
        }
    };
    	
       	
	/*
	 * Update Page data model and Track changes.
	 * 
	 * Photos changes are tracked into controller 
	 */
	private void updateModel() throws Exception{	
		
		if(title.isDirty()){
			page.setTitle(title.getText());
			controller.setPageAsChanged(true);
			Log.trace("title dirty");
		}
		
		if(bodyTextBox.isDirty()) {
			String newContent= bodyTextBox.getText();
			page.setDescription(newContent);
			controller.setPageAsChanged(true);
			Log.trace("bodyTextBox dirty");
		}
		
		if(status.isDirty()) {
			int selectedStatusID = status.getSelectedIndex();
			String newState= controller.getStatusKeys()[selectedStatusID];
			page.setPageStatus(newState);
			controller.setPageAsChanged(true);
			Log.trace("status dirty");
		}
		
		if(pageOrderField.isDirty()){
			page.setWpPageOrder(Integer.parseInt(pageOrderField.getText()));
			pageOrderField.setDirty(false);
			controller.setPageAsChanged(true);
			Log.trace("pageOrderField dirty");
		}
		
		if(parentPageField.isDirty()) {
			int selectedIndex = parentPageField.getSelectedIndex();
			controller.setParentPageID(selectedIndex);
			pageOrderField.setDirty(false);
			controller.setPageAsChanged(true);
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
					controller.setPageAsChanged(true);
				}
			}
		}
		
		/*
		//title
		String oldTitle=page.getTitle();
		if(oldTitle == null ) { //no previous title, setting the new title  
			if(!title.getText().trim().equals("")){
				page.setTitle(title.getText());
				controller.setPageAsChanged(true);
			}
		} else {
			if( !oldTitle.equals(title.getText()) ) { //title has changed
				page.setTitle(title.getText());
				controller.setPageAsChanged(true);
			}
		}
		
		//track changes of body content
		if(bodyTextBox != null) {
			String newContent= bodyTextBox.getText();
			if(!newContent.equals(page.getDescription())){
				page.setDescription(newContent);
				controller.setPageAsChanged(true);
			}
		}

		//page status
		int selectedStatusID = status.getSelectedIndex();
		String newState= controller.getStatusKeys()[selectedStatusID];
		if (newState != page.getPageStatus()) {
			page.setPageStatus(newState);
			controller.setPageAsChanged(true);
		}

		//page order field: : we have used the BB isDirty method instead of manual field change check
		if(pageOrderField.isDirty()){
			page.setWpPageOrder(Integer.parseInt(pageOrderField.getText()));
			pageOrderField.setDirty(false);
			controller.setPageAsChanged(true);
		}
		
		//parent page field: we have used the BB isDirty method instead of manual field change check
		if(parentPageField.isDirty()) {
			int selectedIndex = parentPageField.getSelectedIndex();
			controller.setParentPageID(selectedIndex);
			pageOrderField.setDirty(false);
			controller.setPageAsChanged(true);
		}
		
		//page template. doesn't work on new page
		int selectedTemplateFieldID = pageTemplateField.getSelectedIndex();
		if(selectedTemplateFieldID > -1) {
			String pageTemplate= controller.getPageTemplateKeys()[selectedTemplateFieldID];
			if (pageTemplate != page.getWpPageTemplate()) {
				page.setWpPageTemplate(pageTemplate);
				controller.setPageAsChanged(true);
			}
		}
		*/
	}


    //override onClose() to display a dialog box when the application is closed    
	public boolean onClose()   {
		try {
			updateModel();
		} catch (Exception e) {
			controller.displayError(e, "Error while saving post data");
		}
		return controller.dismissView();	
    }
	
	public BaseController getController() {
		return controller;
	}

}

