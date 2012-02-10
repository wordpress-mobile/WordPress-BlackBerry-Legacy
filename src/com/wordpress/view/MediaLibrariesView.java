//#preprocess
package com.wordpress.view;

import java.util.Hashtable;

import net.rim.device.api.ui.DrawStyle;
import net.rim.device.api.ui.MenuItem;
//#ifdef VER_4.7.0 | BlackBerrySDK5.0.0 | BlackBerrySDK6.0.0 | BlackBerrySDK7.0.0
import net.rim.device.api.ui.Touchscreen;
import com.wordpress.view.touch.BottomBarItem;
//#endif
import com.wordpress.bb.WordPressResource;
import com.wordpress.controller.BaseController;
import com.wordpress.controller.MediaLibrariesController;
import com.wordpress.model.MediaLibrary;
import com.wordpress.utils.log.Log;
import com.wordpress.view.component.ListActionListener;
import com.wordpress.view.component.GenericListField;

public class MediaLibrariesView extends BaseView implements ListActionListener {
	
    private MediaLibrariesController controller= null;

	private GenericListField listaPost; 
	
	 public MediaLibrariesView(MediaLibrariesController  _controller) {
	    	super(_controller.getCurrentBlogName());
	     	this.setSubTitleText(_resources.getString(WordPressResource.TITLE_PHONE_MEDIA_VIEW));
	    	this.controller=_controller;	        
	        buildList();
	 }

	private void buildList() {
		removeAllMenuItems();	
		if(listaPost != null) 
			delete(listaPost);
		
		listaPost = new GenericListField(); 	        
		listaPost.setEmptyString(_resources.getString(WordPressResource.MESSAGE_NO_FILE_GROUP), DrawStyle.LEFT);
		listaPost.setDefautActionListener(this);
		
		MediaLibrary[] mediaLibrary = controller.getMediaLibraries();
		
		if( (mediaLibrary != null) && mediaLibrary.length > 0 ){
			Object[] mediaLibData = new Object[mediaLibrary.length];
			//create a small hashtable with necessary data
			for (int i = 0; i < mediaLibrary.length; i++) {
				Hashtable smallDataForTheList = new Hashtable();
				String title = mediaLibrary[i].getTitle();
				if (title == null || title.length() == 0) {
					title = _resources.getString(WordPressResource.LABEL_EMPTYTITLE);
				}
				smallDataForTheList.put("title", title);

				if (mediaLibrary[i].getMediaObjects() != null) {
					String subTitle = mediaLibrary[i].getMediaObjects().size() + " "+_resources.getString(WordPressResource.TITLE_MEDIA_VIEW);
					smallDataForTheList .put("images_number", subTitle);
				}

				mediaLibData[i]=(smallDataForTheList);
			}
			listaPost.set(mediaLibData);
			addMenuItem(_editMediaLibrary);
			addMenuItem(_deleteMediaLibrary);
		} 
		addMenuItem(_newMediaLibrary);
		
        //#ifdef VER_4.7.0 | BlackBerrySDK5.0.0 | BlackBerrySDK6.0.0 | BlackBerrySDK7.0.0
		if (Touchscreen.isSupported() == true) {
			int numberOfButtons = 1;
			if( (mediaLibrary != null) && mediaLibrary.length > 0 ){
				numberOfButtons = 2;
			}
			BottomBarItem items[] = new BottomBarItem[numberOfButtons];
			items[0] = new BottomBarItem("bottombar_add.png", "bottombar_add.png", _resources.getString(WordPressResource.MENUITEM_NEW));
			if(numberOfButtons == 2)
			items[1] = new BottomBarItem("bottombar_delete.png", "bottombar_delete.png", _resources.getString(WordPressResource.MENUITEM_DELETE));
			
			initializeBottomBar(items);
		}
    	//#endif
		add(listaPost);
	}
	
	//#ifdef VER_4.7.0 | BlackBerrySDK5.0.0 | BlackBerrySDK6.0.0 | BlackBerrySDK7.0.0	
	protected void bottomBarActionPerformed(int mnuItem) {
		switch (mnuItem) {
		case 0:
			controller.newMediaLibrary();
			break;
		case 1:
            int selectedPost = listaPost.getSelectedIndex();
            controller.deleteMediaLibrary(selectedPost);
            buildList();
			break;
		default:
			break;
		}
	}
//#endif
 
         
	public boolean onClose(){
		controller.backCmd();
		return true;
	}
	
    protected void onExposed() {
    	Log.trace("ON EXPOSED");
    	buildList();
        super.onExposed();
    }
    

    private MenuItem _deleteMediaLibrary = new MenuItem( _resources, WordPressResource.MENUITEM_DELETE, 220, 10) {
        public void run() {
            int selectedPost = listaPost.getSelectedIndex();
            controller.deleteMediaLibrary(selectedPost);
            buildList();
        }
    };
    
    private MenuItem _editMediaLibrary = new MenuItem( _resources, WordPressResource.MENUITEM_EDIT, 200, 10) {
        public void run() {
            int selectedPost = listaPost.getSelectedIndex();
            controller.editMediaLibrary(selectedPost);            
        }
    };
    
    private MenuItem _newMediaLibrary = new MenuItem( _resources, WordPressResource.MENUITEM_NEW, 210, 10) {
        public void run() {
            controller.newMediaLibrary();    
        }
    };
    
	public BaseController getController() {
		return controller;
	}


	public void actionPerformed() {
		 int selectedPost = listaPost.getSelectedIndex();
         Log.trace("selected library " + selectedPost);
         controller.editMediaLibrary(selectedPost); 
	}
	
}