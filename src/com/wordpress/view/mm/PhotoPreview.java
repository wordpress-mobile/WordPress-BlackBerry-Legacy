package com.wordpress.view.mm;

 import net.rim.device.api.system.EncodedImage;
import net.rim.device.api.ui.MenuItem;
import net.rim.device.api.ui.component.LabelField;

import com.wordpress.bb.WordPressResource;
import com.wordpress.controller.BaseController;
import com.wordpress.controller.PostController;
import com.wordpress.view.BaseView;

 public class PhotoPreview extends BaseView {
 	
    private PostController controller; //controller associato alla view
	private final String key;
 		
     public PhotoPreview(PostController _controller, String key, EncodedImage Img) {
     	super();
     	this.controller=_controller;
		this.key = key;
         //add a screen title
         LabelField screenTitle = new LabelField(_resources.getString(WordPressResource.TITLE_PHOTOSVIEW),
                         LabelField.ELLIPSIS | LabelField.USE_ALL_WIDTH);
        setTitle(screenTitle);
     	ScrollableImageField scrollableImgField= new ScrollableImageField(Img);         
        add(scrollableImgField); 
        addMenuItem(_deletePhotoItem);
     }
     

     private MenuItem _deletePhotoItem = new MenuItem( _resources, WordPressResource.MENUITEM_PHOTOS_DELETE, 130, 10) {
         public void run() {
        	 controller.deletePhoto(key);
        	 controller.backCmd();
         }
     };     
     
 	public BaseController getController() {
 		return controller;
 	} 	
 }
