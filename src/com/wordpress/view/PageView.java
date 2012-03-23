package com.wordpress.view;

import java.util.Date;

import net.rim.device.api.system.Characters;
import net.rim.device.api.ui.Color;
import net.rim.device.api.ui.DrawStyle;
import net.rim.device.api.ui.Field;
import net.rim.device.api.ui.FieldChangeListener;
import net.rim.device.api.ui.Font;
import net.rim.device.api.ui.Manager;
import net.rim.device.api.ui.MenuItem;
import net.rim.device.api.ui.Ui;
import net.rim.device.api.ui.component.BasicEditField;
import net.rim.device.api.ui.component.ButtonField;
import net.rim.device.api.ui.component.LabelField;
import net.rim.device.api.ui.component.ObjectChoiceField;
import net.rim.device.api.ui.container.HorizontalFieldManager;
import net.rim.device.api.ui.container.MainScreen;
import net.rim.device.api.ui.text.TextFilter;

import com.wordpress.bb.WordPressResource;
import com.wordpress.controller.BaseController;
import com.wordpress.controller.PageController;
import com.wordpress.model.Page;
import com.wordpress.utils.CalendarUtils;
import com.wordpress.utils.StringUtils;
import com.wordpress.utils.log.Log;
import com.wordpress.view.component.BaseButtonField;
import com.wordpress.view.component.ClickableLabelField;
import com.wordpress.view.component.ColoredLabelField;
import com.wordpress.view.component.EmbossedButtonField;
import com.wordpress.view.component.HtmlTextField;
import com.wordpress.view.component.MarkupToolBar;
import com.wordpress.view.component.MarkupToolBarTextFieldMediator;
import com.wordpress.view.container.BorderedFieldManager;
import com.wordpress.view.container.JustifiedEvenlySpacedHorizontalFieldManager;

public class PageView extends StandardBaseView {
	
    private PageController controller;
    private Page page;   
    //content of tabs summary
	private BasicEditField title;
	private HtmlTextField bodyTextBox;
	private LabelField wordCountField;
	private ObjectChoiceField status;
	private ClickableLabelField lblPhotoNumber;
	private BasicEditField pageOrderField;
	private ObjectChoiceField parentPageField;
	private ObjectChoiceField pageTemplateField;
	private EmbossedButtonField sendPageBtn;
	
    public PageView(PageController _controller, Page _page) {
    	super(_resources.getString(WordPressResource.TITLE_POSTVIEW) , MainScreen.NO_VERTICAL_SCROLL | Manager.NO_HORIZONTAL_SCROLL);
    	this.controller=_controller;
		this.page = _page;
               
        //row title
    	BorderedFieldManager outerManagerRowTitle = new BorderedFieldManager(Manager.NO_HORIZONTAL_SCROLL
         		| Manager.NO_VERTICAL_SCROLL | BorderedFieldManager.BOTTOM_BORDER_NONE);
		LabelField lblTitle = GUIFactory.getLabel(_resources.getString(WordPressResource.LABEL_TITLE), Color.BLACK);
		title = new BasicEditField("", page.getTitle(), BasicEditField.DEFAULT_MAXCHARS, Field.EDITABLE);
		outerManagerRowTitle.add(lblTitle);
		outerManagerRowTitle.add(GUIFactory.createSepatorField());
		outerManagerRowTitle.add(title);
        add(outerManagerRowTitle);

        //opts Manager
    	BorderedFieldManager outerManagerRowInfos = new BorderedFieldManager(Manager.NO_HORIZONTAL_SCROLL
         		| Manager.NO_VERTICAL_SCROLL | BorderedFieldManager.BOTTOM_BORDER_NONE);
    	add(outerManagerRowInfos); 

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
    	  	
    	//row status
  		status = new ObjectChoiceField(_resources.getString(WordPressResource.LABEL_POST_STATUS)+":", 
  				controller.getStatusLabels(), 
  				controller.getPageStatusFieldIndex()
  				);
  		status.setChangeListener(new FieldChangeListener() {
			public void fieldChanged(Field field, int context) {
				updateSendMenuItemAndButtonLabel();				
			}
  		});
  		status.setMargin(5, 5, 5, 5);
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
  		String buildBodyFieldContentFromHtml = page.getDescription();
		
  		//decode the text more content
  		String extendedBody = page.getMtTextMore();
  		if(extendedBody != null && !extendedBody.trim().equals("")) {
  			String extendedBodyHTML = Characters.ENTER +"<!--more-->" + Characters.ENTER;
  			extendedBodyHTML += extendedBody;
  			buildBodyFieldContentFromHtml += extendedBodyHTML;
  		}
		bodyTextBox= new HtmlTextField(buildBodyFieldContentFromHtml);
		
		MarkupToolBarTextFieldMediator mediator = new MarkupToolBarTextFieldMediator();
  		
  		HorizontalFieldManager headerContent = new HorizontalFieldManager(Manager.NO_HORIZONTAL_SCROLL | Manager.USE_ALL_WIDTH);
  		LabelField lblPostContent = GUIFactory.getLabel(_resources.getString(WordPressResource.LABEL_CONTENT), Color.BLACK, DrawStyle.ELLIPSIS);
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
        
		JustifiedEvenlySpacedHorizontalFieldManager bottomToolbar = new JustifiedEvenlySpacedHorizontalFieldManager();	
		bottomToolbar.setMargin(5,0,5,0);
		sendPageBtn = (EmbossedButtonField) GUIFactory.createButton(_resources.getString(WordPressResource.MENUITEM_PUBLISH), ButtonField.CONSUME_CLICK | ButtonField.USE_ALL_WIDTH | DrawStyle.ELLIPSIS);
		sendPageBtn.setChangeListener(
				new FieldChangeListener() {
					public void fieldChanged(Field field, int context) {
						sendPageToBlog();
					}
				}
		);
		sendPageBtn.setMargin(0,5,0,5);

		BaseButtonField saveDraftPostBtn= GUIFactory.createButton(_resources.getString(WordPressResource.MENUITEM_SAVE_LOCALDRAFT), ButtonField.CONSUME_CLICK | ButtonField.USE_ALL_WIDTH | DrawStyle.ELLIPSIS);
		saveDraftPostBtn.setChangeListener(
				new FieldChangeListener() {
					public void fieldChanged(Field field, int context) {
						saveDraftPage();
					}
				}
		);
		saveDraftPostBtn.setMargin(0,5,0,5);
		bottomToolbar.add(sendPageBtn);
		bottomToolbar.add(saveDraftPostBtn);
		add(bottomToolbar); 
		
		addMenuItem(_previewItem);
		addMenuItem(_saveDraftItem);
		addMenuItem(_submitItem);
		addMenuItem(_photosItem);
		addMenuItem(_settingsItem);
		addMenuItem(_customFieldsMenuItem);
		
		updateSendMenuItemAndButtonLabel();
		
		controller.bumpScreenViewStats("com/wordpress/view/PageView", "PageView Screen", "", null, "");
    }
    
    
    //set the photos number label text
    public void setNumberOfPhotosLabel(int count) {
    	lblPhotoNumber.setText(String.valueOf(count));
    }
        
    private void saveDraftPage() {
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
    
    //save a local copy of post
    private MenuItem _saveDraftItem = new MenuItem( _resources, WordPressResource.MENUITEM_SAVE_LOCALDRAFT, 160000, 1000) {
        public void run() {
        	saveDraftPage();
        }
    };
    
    private void sendPageToBlog() {
    	try {
    		updateModel();
    		controller.sendPageToBlog();
    	} catch (Exception e) {
    		controller.displayError(e, _resources.getString(WordPressResource.ERROR_WHILE_SAVING_PAGE));
    	}
    }
    
    //send post to blog
    private MenuItem _submitItem = new MenuItem( _resources, WordPressResource.MENUITEM_PUBLISH, 160000, 1000) {
        public void run() {
        	sendPageToBlog();
        }
    };
    
    private MenuItem _customFieldsMenuItem = new MenuItem(_resources, WordPressResource.MENUITEM_CUSTOM_FIELDS, 80000, 1000) {
        public void run() {
        	controller.showCustomFieldsView(title.getText());
        }
    };
    
    private MenuItem _photosItem = new MenuItem( _resources, WordPressResource.MENUITEM_MEDIA, 80000, 1000) {
        public void run() {
        	controller.showPhotosView();
        }
    };
    
    private MenuItem _previewItem = new MenuItem( _resources, WordPressResource.MENUITEM_PREVIEW, 160000, 1000) {
    	public void run() {

    		if( controller.isDraftItem() || controller.isObjectChanged() || page.getID() == null) {
    			//1. draft page
    			//2. published page is changed 
    			controller.startLocalPreview(title.getText(), bodyTextBox.getText(), "", "");
    		} else if(title.isDirty() || bodyTextBox.isDirty() || 
    				status.isDirty() || lblPhotoNumber.isDirty()) {
    			//3. page main screen is just changed
    			controller.startLocalPreview(title.getText(), bodyTextBox.getText(), "", "");
    		} else {
    			//4. Page already synched with the server. We should ALWAYS use the blog login form...
   				controller.startRemotePreview(page.getPermaLink(), title.getText(), bodyTextBox.getText(), "", "");
    		}
    	}
    };

    private MenuItem _settingsItem = new MenuItem( _resources, WordPressResource.MENUITEM_SETTINGS, 80000, 1000) {
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
	
    public void updateSendMenuItemAndButtonLabel( ){
		int selectedStatusID = status.getSelectedIndex();
		String newState = controller.getStatusKeys()[selectedStatusID];

		if ( newState.equals( "publish" ) ) {
			//publish or schedule label
			
			//check if is published or scheduled
			Date righNowDate = new Date();//this date is NOT at GMT timezone 
			long righNow = CalendarUtils.adjustTimeFromDefaultTimezone(righNowDate.getTime());
			Date postDate = page.getDateCreatedGMT();//this date is GMT date

			if( postDate == null ) {
				_submitItem.setText( _resources.getString( WordPressResource.MENUITEM_PUBLISH ) );
				sendPageBtn.setText(_resources.getString( WordPressResource.MENUITEM_PUBLISH ) );
				return;
			}
			
			long postDateLong = postDate.getTime();
			if(righNow > postDateLong) {
				_submitItem.setText( _resources.getString( WordPressResource.MENUITEM_PUBLISH ) );
				sendPageBtn.setText(_resources.getString( WordPressResource.MENUITEM_PUBLISH ) );
			} else {
				_submitItem.setText( _resources.getString( WordPressResource.MENUITEM_SCHEDULE ) );
				sendPageBtn.setText(_resources.getString( WordPressResource.MENUITEM_SCHEDULE ) );
			}
		} else {
	  		//save
			_submitItem.setText( _resources.getString( WordPressResource.MENUITEM_SAVE ) );
			sendPageBtn.setText(_resources.getString( WordPressResource.MENUITEM_SAVE ) );
		}
		
    }
	
}