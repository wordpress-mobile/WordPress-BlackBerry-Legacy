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
import com.wordpress.controller.PostController;
import com.wordpress.model.Category;
import com.wordpress.model.Post;
import com.wordpress.model.PostState;
import com.wordpress.utils.MultimediaUtils;
import com.wordpress.view.component.FileSelectorPopupScreen;
import com.wordpress.view.component.HtmlTagPopupScreen;
import com.wordpress.view.component.HtmlTextField;
import com.wordpress.view.component.MultimediaPopupScreen;

public class PostView extends BaseView  implements FocusChangeListener{
	
    private PostController controller; //controller associato alla view
    private Post post;    
    private PostState mState = null;
    
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

	//flag that reveals extended MM menu presence
	private boolean isMultimediaMenuPresent=false;
	
	//content of tabs summary
	private BasicEditField title;
	private ObjectChoiceField categoryChoice;
	private DateField authoredOn; 
	private CheckboxField isPublished;
	private CheckboxField isConvertLinebreaksEnabled;  
	private CheckboxField isCommentsEnabled;
	private CheckboxField isTrackbackEnabled; 
	
	//content of others tabs
	private HtmlTextField bodyTextBox;
	private HtmlTextField extendedTextBox;
	private HtmlTextField excerptTextBox;
	    
    
    public PostView(PostController _controller, Post _post) {
    	super();
    	this.controller=_controller;
		this.post = _post;
		this.mState= new PostState();
        //add a screen title
        LabelField title = new LabelField(_resources.getString(WordPressResource.TITLE_POSTVIEW),
                        LabelField.ELLIPSIS | LabelField.USE_ALL_WIDTH);
        setTitle(title);
    	//--
		HorizontalFieldManager hManager = new HorizontalFieldManager();
		tabSummary = new LabelField(_resources.getString(WordPressResource.LABEL_TAB_POSTSUMMARY), LabelField.FOCUSABLE | LabelField.HIGHLIGHT_SELECT);
		tabBoby = new LabelField(_resources.getString(WordPressResource.LABEL_TAB_POSTBODY), LabelField.FOCUSABLE | LabelField.HIGHLIGHT_SELECT);
		tabExtended = new LabelField(_resources.getString(WordPressResource.LABEL_TAB_POSTEXTENDED), LabelField.FOCUSABLE | LabelField.HIGHLIGHT_SELECT);
		tabExcerpt = new LabelField(_resources.getString(WordPressResource.LABEL_TAB_POSTEXCERPT), LabelField.FOCUSABLE | LabelField.HIGHLIGHT_SELECT);
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
		addMenuItem(_saveDraftPostItem);
		addMenuItem(_submitPostItem);
    }
    
    //save a local copy of post
    private MenuItem _saveDraftPostItem = new MenuItem( _resources, WordPressResource.MENUITEM_POST_SAVEDRAFT, 130, 10) {
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
    private MenuItem _submitPostItem = new MenuItem( _resources, WordPressResource.MENUITEM_POST_SUBMIT, 120, 10) {
        public void run() {
    		try {
    			savePost();
	    		controller.sendPostToBlog();
    		} catch (Exception e) {
    			controller.displayError(e, "Error while saving post data");
    		}
        }
    };
    
    private MenuItem _htmlItem = new MenuItem( _resources, WordPressResource.MENUITEM_POST_HTML, 100, 10) {
        public void run() {
        	UiApplication.getUiApplication().pushScreen(new HtmlTagPopupScreen());
        }
    };

    private MenuItem _mediaItem = new MenuItem( _resources, WordPressResource.MENUITEM_POST_MM, 110, 10) {
        public void run() {
        	controller.showMultimediaSelectionBox(bodyTextBox);
        }
    };
    
    
	public void focusChanged(Field field, int eventType) {
		if (tabArea != null) {
			if (eventType == FOCUS_GAINED) {
				if (field == tabSummary) {
					System.out.println("Switch to Tab Summary");
					delete(tabArea);
					tabArea = displayTabSummary();
					add(tabArea);
					removeTagAndMultimediaMenu();
				} else if (field == tabBoby) {
					System.out.println("Switch to Tab Body");
					delete(tabArea);
					tabArea = displayTabBody();
					add(tabArea);
					displayTagAndMultimediaMenu();
				} else if (field == tabExtended) {
					System.out.println("Switch to Tab Extended");
					delete(tabArea);
					tabArea = displayTabExtended();
					add(tabArea);
					displayTagAndMultimediaMenu();
				}else if (field == tabExcerpt) {
					System.out.println("Switch to Tab Excerpt");
					delete(tabArea);
					tabArea = displayTabExcerpt();
					add(tabArea);
					displayTagAndMultimediaMenu();
				}
			}
		}
	}
	
	
	/*
	 * update Post data model
	 */
	private void savePost() throws Exception{	

		//track changes on summary tab
		
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
		
		//track changes of Extended content
		if(extendedTextBox != null){
			String newContent= extendedTextBox.getText();
			if(!newContent.equals(post.getExtendedBody())){
				mState.setModified(true);
				post.setExtendedBody(newContent);
			}
		}
		
		//track changes of Excerpt content
		if(excerptTextBox != null){
			String newContent= excerptTextBox.getText();
			if(!newContent.equals(post.getExcerpt())){
				mState.setModified(true);
				post.setExcerpt(newContent);
			}
		}
	}

	public VerticalFieldManager displayTabSummary() {

		if (title == null) {
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
			
			tabSummaryManager.add(title);
			tabSummaryManager.add(categoryChoice);
			tabSummaryManager.add(authoredOn);
			tabSummaryManager.add(isPublished);
			tabSummaryManager.add(isConvertLinebreaksEnabled);
			tabSummaryManager.add(isCommentsEnabled);
			tabSummaryManager.add(isTrackbackEnabled);
		}

		return tabSummaryManager;
	}

	private void removeTagAndMultimediaMenu(){
		isMultimediaMenuPresent=false;
		removeMenuItem(_htmlItem);
		removeMenuItem(_mediaItem);
	}
	
	private void displayTagAndMultimediaMenu(){
		if(isMultimediaMenuPresent == false ){
			isMultimediaMenuPresent=true;
			addMenuItem(_htmlItem);
			addMenuItem(_mediaItem);
		} else {
			//menu already present
		}
	}
	
	public VerticalFieldManager displayTabBody() {
		if(bodyTextBox == null){
			bodyTextBox= new HtmlTextField(post.getBody());
			tabBodyManager.add(bodyTextBox);
		}
		return tabBodyManager;
	}

	public VerticalFieldManager displayTabExtended() {
		if(extendedTextBox == null){
			extendedTextBox= new HtmlTextField(post.getExtendedBody());
			tabExtendedManager.add(extendedTextBox);
		}
		
		return tabExtendedManager;
	}

	public VerticalFieldManager displayTabExcerpt() {
		if(excerptTextBox == null){
			excerptTextBox= new HtmlTextField(post.getExcerpt());
			tabExcerptManager.add(excerptTextBox);
		}
		
		return tabExcerptManager;
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
     
}