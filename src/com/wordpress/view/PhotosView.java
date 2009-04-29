package com.wordpress.view;


import net.rim.device.api.ui.MenuItem;
import net.rim.device.api.ui.component.LabelField;

import com.wordpress.bb.WordPressResource;
import com.wordpress.controller.PostController;
import com.wordpress.model.Post;

public class PhotosView extends BaseView {
	
    private PostController controller; //controller associato alla view
    private Post post;    
	
    public PhotosView(PostController _controller, Post _post) {
    	super();
    	this.controller=_controller;
		this.post = _post;
        //add a screen title
        LabelField screenTitle = new LabelField(_resources.getString(WordPressResource.TITLE_PHOTOSVIEW),
                        LabelField.ELLIPSIS | LabelField.USE_ALL_WIDTH);
        setTitle(screenTitle);
        addMenuItem(_mediaItem);
    }
    

    private MenuItem _mediaItem = new MenuItem( _resources, WordPressResource.MENUITEM_PHOTOS_ADD, 110, 10) {
        public void run() {
        	controller.showMultimediaSelectionBox();
        }
    };
    	


    //override onClose() to display a dialog box when the application is closed    
	public boolean onClose()   {
		controller.backCmd();
		return true;
    }
	
}