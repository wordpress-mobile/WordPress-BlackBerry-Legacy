package com.wordpress.controller;

import java.util.Hashtable;

import net.rim.device.api.system.GlobalEventListener;
import net.rim.device.api.ui.UiApplication;

import com.wordpress.bb.WordPressResource;
import com.wordpress.model.Blog;
import com.wordpress.view.BlogOptionsView;


public class BlogOptionsController extends BaseController {
	
	private BlogOptionsView view = null;
	private final Blog blog;
	private Hashtable guiValues= new Hashtable();
	public static final int[] recentsPostValues={10,20,30,40,50};
	public static final String[] recentsPostValuesLabel={"10","20","30","40","50"};
	
	public BlogOptionsController(Blog blog) {
		super();
		this.blog = blog;
		//read the int value for maxPost showed
		int recentPostSelected= blog.getMaxPostCount();
		//find the index of the value in the predefined array
		int indexRecPost=0;
	 	for (int i = 0; i < recentsPostValues.length; i++) {
	 		if (recentsPostValues[i] == recentPostSelected ) {
	 			indexRecPost=i;
	 			break;
	 		}
	    }

	 	if(blog.isWPCOMBlog() == false) {
			guiValues.put("user", blog.getUsername());
			guiValues.put("pass", blog.getPassword());
		}
		guiValues.put("recentpost", recentsPostValuesLabel);
		guiValues.put("recentpostselected", new Integer(indexRecPost));
		guiValues.put("isresphotos", new Boolean(blog.isResizePhotos()));
		guiValues.put("imageResizeWidth", blog.getImageResizeWidth()); //this is never null bc during init we put the default size
		guiValues.put("imageResizeHeight", blog.getImageResizeHeight()); //this is never null bc during init we put the default size
		guiValues.put("islocation", new Boolean(blog.isLocation()));
		guiValues.put("iscommentnotifications", new Boolean(blog.isCommentNotifies()));
		guiValues.put("isSignatureActive", new Boolean(blog.isSignatureEnabled()));
		guiValues.put("isresvideos",  new Boolean(blog.isResizeVideos()));
		guiValues.put("videoResizeWidth", blog.getVideoResizeWidth() == null ? new Integer(0) : blog.getVideoResizeWidth());
		guiValues.put("videoResizeHeight", blog.getVideoResizeHeight() == null ? new Integer(0) : blog.getVideoResizeHeight());
		
		String signature = blog.getSignature();
		if(signature == null) 
			signature = _resources.getString(WordPressResource.DEFAULT_SIGNATURE);
		guiValues.put("signature", signature);
		this.view= new BlogOptionsView(this,guiValues);
	}
	
	public void showView(){
		UiApplication.getUiApplication().pushScreen(view);
	}
	
	public Blog getBlog() {
		return blog;
	}
	
	public String getBlogName() {
		return blog.getName();
	}

	public void refreshView() {
		
	}
}