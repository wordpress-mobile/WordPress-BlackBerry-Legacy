package com.wordpress.controller;

import java.util.Hashtable;

import net.rim.device.api.ui.Field;
import net.rim.device.api.ui.FieldChangeListener;
import net.rim.device.api.ui.UiApplication;
import net.rim.device.api.ui.component.Dialog;

import com.wordpress.bb.WordPressResource;
import com.wordpress.io.BlogDAO;
import com.wordpress.model.Blog;
import com.wordpress.utils.ImageUtils;
import com.wordpress.utils.log.Log;
import com.wordpress.view.BlogOptionsView;
import com.wordpress.view.dialog.DiscardChangeInquiryView;


public class BlogOptionsController extends BaseController {
	
	private BlogOptionsView view = null;
	
	private Hashtable guiValues= new Hashtable();
	private final Blog blog;
	
	public BlogOptionsController(Blog blog) {
		super();
		this.blog = blog;
		//read the int value for maxPost showed
		int recentPostSelected= blog.getMaxPostCount();
		//find the index of the value in the predefined array
		int indexRecPost=0;
	 	for (int i = 0; i < AddBlogsController.recentsPostValues.length; i++) {
	 		if (AddBlogsController.recentsPostValues[i] == recentPostSelected ) {
	 			indexRecPost=i;
	 			break;
	 		}
	    }
		guiValues.put("user", blog.getUsername());
		guiValues.put("pass", blog.getPassword());
		guiValues.put("recentpost", AddBlogsController.recentsPostValuesLabel);
		guiValues.put("recentpostselected", new Integer(indexRecPost));
		guiValues.put("isresphotos", new Boolean(blog.isResizePhotos()));
		guiValues.put("imageResizeWidth", blog.getImageResizeWidth());
		guiValues.put("imageResizeHeight", blog.getImageResizeHeight());
		guiValues.put("islocation", new Boolean(blog.isLocation()));
		guiValues.put("iscommentnotifications", new Boolean(blog.isCommentNotifies()));
		
		this.view= new BlogOptionsView(this,guiValues);
	}
	
	public void showView(){
		UiApplication.getUiApplication().pushScreen(view);
	}


	private FieldChangeListener listenerOkButton = new FieldChangeListener() {
	    public void fieldChanged(Field field, int context) {
	    	
	    	boolean isModified = isModified();
			
			if(!isModified) {
				backCmd();
				return;
			} else  {
				saveAndBack();
			}
	   }
	};


	private FieldChangeListener listenerBackButton = new FieldChangeListener() {
	    public void fieldChanged(Field field, int context) {
	        backCmd();
	   }
	};

	public FieldChangeListener getOkButtonListener() {
		return listenerOkButton;
	}
	   
	public FieldChangeListener getBackButtonListener() {
		return listenerBackButton;
	}
	
	public String getBlogName() {
		return blog.getName();
	}
	
	private boolean isModified() {
		boolean isModified=false;
		
		String pass= view.getBlogPass();
		String user= view.getBlogUser();
		int maxPostIndex=view.getMaxRecentPostIndex();
		int valueMaxPostCount=AddBlogsController.recentsPostValues[maxPostIndex];
		boolean isResPhotos = view.isResizePhoto();
		Integer imageResizeWidth = view.getImageResizeWidth();
		Integer imageResizeHeight = view.getImageResizeHeight();
		boolean isCommentNotifications = view.isCommentNotifications();
		boolean isLocation = view.isLocation();
		//we can use isDirty on all view...
		if(!blog.getUsername().equals(user) || !blog.getPassword().equals(pass)
			|| blog.getMaxPostCount() != valueMaxPostCount 
			|| isResPhotos != blog.isResizePhotos()			
			|| !imageResizeWidth.equals(blog.getImageResizeWidth() )
			|| !imageResizeHeight.equals(blog.getImageResizeHeight()) 
			|| isCommentNotifications != blog.isCommentNotifies()  
			|| isLocation != blog.isLocation()
		) {
			isModified=true;
		}
		
		return isModified;
	}
	
	//called when user click the OK button
	private void  saveAndBack(){
		//Before saving we should do an additional check over img resize width and height.
		//it is necessary when user put a value into width/height field and then press backbutton;
		//the focus lost on those fields is never fired....
		Integer imageResizeWidth = view.getImageResizeWidth();
		Integer imageResizeHeight = view.getImageResizeHeight();
		int[] keepAspectRatio = ImageUtils.keepAspectRatio(imageResizeWidth.intValue(), imageResizeHeight.intValue());
		imageResizeWidth = new Integer(keepAspectRatio[0]);
		imageResizeHeight = new Integer(keepAspectRatio[1]);
		
		String pass= view.getBlogPass();
		String user= view.getBlogUser();
		int maxPostIndex=view.getMaxRecentPostIndex();
		int valueMaxPostCount=AddBlogsController.recentsPostValues[maxPostIndex];
		boolean isResPhotos = view.isResizePhoto();
		blog.setPassword(pass);
		blog.setUsername(user);
		blog.setResizePhotos(isResPhotos);
		blog.setImageResizeWidth(imageResizeWidth);
		blog.setImageResizeHeight(imageResizeHeight);
		blog.setMaxPostCount(valueMaxPostCount);
		blog.setCommentNotifies(view.isCommentNotifications());
		blog.setLocation(view.isLocation());
		
		try {
			BlogDAO.updateBlog(blog);
		} catch (Exception e) {
			displayError(e, "Error while saving blog options");
		}
		
		backCmd();
	}
	
	public boolean dismissView() {
		boolean isModified=this.isModified();
			
		if(!isModified) {
			backCmd();
			return true;
		}
		
		String quest=_resources.getString(WordPressResource.MESSAGE_INQUIRY_DIALOG_BOX);
    	DiscardChangeInquiryView infoView= new DiscardChangeInquiryView(quest);
    	int choice=infoView.doModal();    	 
    	if(Dialog.DISCARD == choice) {
    		Log.trace("user has selected discard");
    		backCmd();
    		return true;
    	}else if(Dialog.SAVE == choice) {
    		Log.trace("user has selected save");
    		saveAndBack();
    		return true;
    	} else {
    		Log.trace("user has selected cancel");
    		return false;
    	}
	}

	public void refreshView() {
		
	}
}