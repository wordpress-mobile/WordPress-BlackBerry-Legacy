package com.wordpress.model;

import java.util.Hashtable;
import java.util.Vector;

import net.rim.device.api.i18n.ResourceBundle;
import net.rim.device.api.util.Arrays;
import net.rim.device.api.util.Comparator;

import com.wordpress.bb.WordPressCore;
import com.wordpress.bb.WordPressResource;
import com.wordpress.controller.AccountsController;
import com.wordpress.utils.log.Log;

public class Blog {

	private int loadingState = -1; //loading state of this blog. see BlogInfo constants
	
	private boolean isWPCOMBlog = false;
	
	private String id;
	private String name;
	private String url; //user inserted blogs url
	private String xmlRpcUrl; //real url for publishing on this blog
	private String username;
	private String password;
	
	private boolean isResizePhotos = true;
    private Integer imageResizeWidth = null; //used for custom size
    private Integer imageResizeHeight = null; //used for custom size 
    private Integer  imageResizeSetting = new Integer (BlogInfo.ALWAYS_ASK_IMAGE_RESIZE_SETTING);
    
    //VideoPress video Resize Options
	private boolean isResizeVideos = false;
    private Integer videoResizeWidth = null;
    private Integer videoResizeHeight = null; 
    
	private boolean isCommentNotifies=false; //true when comment notifies is active
	private boolean isLocation=false; //true when location is active
	
	private boolean isSignatureEnabled=false; //true add a signature at the end of each post
	private String signature=null;

	private int maxPostCount = 50 ;

	private Category[] categories = null;
	private Hashtable postStatusList=null; 	
	private Hashtable pageStatusList=null;
	private Hashtable pageTemplates=null;
	private Hashtable commentStatusList=null; 
	private Tag[] tags=null;
	private Hashtable blogOptions=null; 
	private Hashtable wpcomFeatures=null;
	private Hashtable postFormats=null; //introduces in WP3.1 

	private Vector recentPostTitles = null; //response of mt.getRecentPostTitles		
	private Vector pages = null;

	private String statsUsername = null; //this data could be different from http auth
	private String statsPassword = null; //this data could be different from http auth 
	
	private boolean isHTTPBasicAuthRequired = false;
	private String HTTPAuthUsername = null; //could be used only for self-hosted blog - this data could be different from stats auth
	private String HTTPAuthPassword = null; //could be used used for self-hosted blog - this data could be different from stats auth 
	
	public Blog(String blogId, String blogName, String blogUrl,String blogXmlRpcUrl, 
			String userName, String pass) {
		this.id = blogId;
		this.name = blogName;
		this.url = blogUrl;
		this.xmlRpcUrl = blogXmlRpcUrl;
		this.username=userName;
		this.password=pass;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}
	
	public Category[] getCategories() {
		if(categories == null) return null;
		Category[] sortedArray = new Category[categories.length]; 
		System.arraycopy(categories, 0, sortedArray, 0, categories.length);
		Arrays.sort(sortedArray, new CategoryNameComparator() );
		return sortedArray;
	}
	
	private class CategoryNameComparator implements  Comparator {
	    public int compare(Object emp1, Object emp2){    
	        //parameter are of type Object, so we have to downcast it to Employee objects
	        String emp1Name = ((Category)emp1).getLabel().toLowerCase();        
	        String emp2Name = ((Category)emp2).getLabel().toLowerCase();
	        //uses compareTo method of String class to compare names of the employee
	        return emp1Name.compareTo(emp2Name);
	    }
	}
	
	public void setCategories(Category[] aCategories) {
		categories = aCategories;
	}

	public String getName() {
		if(blogOptions != null && blogOptions.get("blog_title") != null) {
			try {
				Hashtable currentOption = (Hashtable) blogOptions.get("blog_title");
				String blogTitle = (String)currentOption.get("value");
				if(blogTitle == null || blogTitle.equalsIgnoreCase("")) {
					//not found the blog name within the options
					return name;
				} else {
					return blogTitle;
				}
			} catch (Exception e) {
				return name;
			}
		} else {
			//return the previously stored blog name (stored during blog init)			
			return name;
		}
	}

	public String getUrl() {
		return url;
	}

	public String getXmlRpcUrl() {
		return xmlRpcUrl;
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getPassword() {
		if(!isWPCOMBlog) {
			return password;
		} else {
			try {
				return AccountsController.getAccountPassword(username);
			} catch (Exception e) {
				Log.trace(e, "Error while reading the account password");
				return password;
			}
		}
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public boolean isResizePhotos() {
		return isResizePhotos;
	}

	public void setResizePhotos(boolean isResizePhotos) {
		this.isResizePhotos = isResizePhotos;
	}

	public void setImageResizeWidth(Integer imageResizeWidth) {
		this.imageResizeWidth = imageResizeWidth;
	}
	
	public Integer getImageResizeWidth() {
		return imageResizeWidth;
	}
	
	public void setImageResizeHeight(Integer imageResizeHeight) {
		this.imageResizeHeight = imageResizeHeight;
	}
	
	public Integer getImageResizeHeight() {
		return imageResizeHeight;
	}
	
	public Integer getImageResizeSetting() {
		return imageResizeSetting;
	}

	public void setImageResizeSetting(Integer imageResizeSetting) {
		this.imageResizeSetting = imageResizeSetting;
	}

	public boolean isResizeVideos() {
		return isResizeVideos;
	}

	public void setResizeVideos(boolean isResizeVideos) {
		this.isResizeVideos = isResizeVideos;
	}

	public Integer getVideoResizeWidth() {
		return videoResizeWidth;
	}

	public void setVideoResizeWidth(Integer videoResizeWidth) {
		this.videoResizeWidth = videoResizeWidth;
	}

	public Integer getVideoResizeHeight() {
		return videoResizeHeight;
	}

	public void setVideoResizeHeight(Integer videoResizeHeight) {
		this.videoResizeHeight = videoResizeHeight;
	}
	
	public int getMaxPostCount() {
		return maxPostCount;
	}

	public void setMaxPostCount(int maxPostCount) {
		this.maxPostCount = maxPostCount;
	}

	public Hashtable getPostStatusList() {
		return postStatusList;
	}

	public void setPostStatusList(Hashtable postStatusList) {
		this.postStatusList = postStatusList;
	}

	public Hashtable getPageStatusList() {
		return pageStatusList;
	}

	public void setPageStatusList(Hashtable pageStatusList) {
		this.pageStatusList = pageStatusList;
	}
	
	public Tag[] getTags() {
		return tags;
	}

	public void setTags(Tag[] tags) {
		this.tags = tags;
	}
	
	public Vector getRecentPostTitles() {
		return recentPostTitles;
	}

	public void setRecentPostTitles(Vector recentPostTitles) {
		this.recentPostTitles = recentPostTitles;
	}

	public Hashtable getCommentStatusList() {
		return commentStatusList;
	}

	public void setCommentStatusList(Hashtable commentStatusList) {
		this.commentStatusList = commentStatusList;
	}

	public int getLoadingState() {
		return loadingState;
	}

	public void setLoadingState(int loadingState) {
		this.loadingState = loadingState;
	}

	public Vector getPages() {
		return pages;
	}

	public void setPages(Vector pages) {
		this.pages = pages;
	}

	public Hashtable getPageTemplates() {
		return pageTemplates;
	}

	public void setPageTemplates(Hashtable pageTemplates) {
		this.pageTemplates = pageTemplates;
	}

	public boolean isCommentNotifies() {
		return isCommentNotifies;
	}

	public void setCommentNotifies(boolean isCommentNotifies) {
		this.isCommentNotifies = isCommentNotifies;
	}

	public boolean isLocation() {
		return isLocation;
	}

	public void setLocation(boolean isLocation) {
		this.isLocation = isLocation;
	}
	
	public String getStatsPassword() {
		return statsPassword;
	}
	
	public void setStatsPassword(String statsPassword) {
		this.statsPassword = statsPassword;
	}
	
	public String getStatsUsername() {
		return statsUsername;
	}
	
	public void setStatsUsername(String statsUsername) {
		this.statsUsername = statsUsername;
	}
	
	public String getHTTPAuthUsername() {
		return HTTPAuthUsername;
	}

	public void setHTTPAuthUsername(String httpAuthUsername) {
		this.HTTPAuthUsername = httpAuthUsername;
	}

	public String getHTTPAuthPassword() {
		return HTTPAuthPassword;
	}

	public void setHTTPAuthPassword(String httpAuthPassword) {
		this.HTTPAuthPassword = httpAuthPassword;
	}

	public boolean isSignatureEnabled() {
		return isSignatureEnabled;
	}

	public void setSignatureEnabled(boolean isSignatureActive) {
		this.isSignatureEnabled = isSignatureActive;
	}

	
	public String getSignature() {
		return signature;
	}

	public void setSignature(String signature) {
		this.signature = signature;
	}

	public boolean isWPCOMBlog() {
		return isWPCOMBlog;
	}

	public void setWPCOMBlog(boolean isWPCOMBlog) {
		this.isWPCOMBlog = isWPCOMBlog;
	}

	public boolean isHTTPBasicAuthRequired() {
		return isHTTPBasicAuthRequired;
	}

	public void setHTTPBasicAuthRequired(boolean isHTTPBasicAuthRequired) {
		this.isHTTPBasicAuthRequired = isHTTPBasicAuthRequired;
	}	
	
	public Hashtable getBlogOptions() {
		return blogOptions;
	}

	public void setBlogOptions(Hashtable blogOptions) {
		this.blogOptions = blogOptions;
	}

	public Hashtable getWpcomFeatures() {
		return wpcomFeatures;
	}

	public void setWpcomFeatures(Hashtable wpcomFeatures) {
		this.wpcomFeatures = wpcomFeatures;
	}
	
	public Hashtable getPostFormats() {
		return postFormats;
	}

	public void setPostFormats(Hashtable postFormats) {
		this.postFormats = postFormats;
	}
	
	
	public boolean isVideoPressUpgradeAvailable() {
		//for backward compatibility we return true when this info is missing in the blog object.
		//So, a second check is done on the SendToBlogTask media-callback when the server
		//returns XML-RPC exception with error code 500
		boolean predefinedResponse = true;
		if(wpcomFeatures != null) {
			if (wpcomFeatures.get("videopress_enabled") == null) return predefinedResponse; 
			boolean value = ((Boolean)wpcomFeatures.get("videopress_enabled")).booleanValue();
			return value;
		} else {
			return predefinedResponse; 
		}
	}
	
	
	public String[] getBlogImageResizeLabels() {
		//read the value from the blogOptions is available
		Hashtable blogOptions = this.getBlogOptions();
		ResourceBundle _resources = WordPressCore.getInstance().getResourceBundle();

		 //important, should be ordered like the indexes defined in BlogIndex!!
		String[] resizeOptLabels = {_resources.getString(WordPressResource.LABEL_SMALL),
				_resources.getString(WordPressResource.LABEL_MEDIUM), 
				_resources.getString(WordPressResource.LABEL_LARGE), 
				_resources.getString(WordPressResource.LABEL_CUSTOM), 
		};
		try{
			if(blogOptions != null) {
				Hashtable tmp = (Hashtable)blogOptions.get("thumbnail_size_w");
				Hashtable tmp2 = (Hashtable)blogOptions.get("thumbnail_size_h");
				resizeOptLabels[0] = _resources.getString(WordPressResource.LABEL_SMALL) +" ("+_resources.getString(WordPressResource.LABEL_MAX)+" "+ String.valueOf(tmp.get("value"))+"x"+String.valueOf(tmp2.get("value"))+")";

				tmp = (Hashtable)blogOptions.get("medium_size_w");
				tmp2 = (Hashtable)blogOptions.get("medium_size_h");
				resizeOptLabels[1] = _resources.getString(WordPressResource.LABEL_MEDIUM) +" ("+_resources.getString(WordPressResource.LABEL_MAX)+" "+  String.valueOf(tmp.get("value"))+"x"+String.valueOf(tmp2.get("value"))+")";

				tmp = (Hashtable)blogOptions.get("large_size_w");
				tmp2 = (Hashtable)blogOptions.get("large_size_h");
				resizeOptLabels[2] =_resources.getString(WordPressResource.LABEL_LARGE) +" ("+_resources.getString(WordPressResource.LABEL_MAX)+" "+  String.valueOf(tmp.get("value"))+"x"+String.valueOf(tmp2.get("value"))+")";
			}
		}catch (Exception e) {
			//use fixed dimension defined within this app
			resizeOptLabels[0] =_resources.getString(WordPressResource.LABEL_SMALL) +
			" ("+_resources.getString(WordPressResource.LABEL_MAX)+" "+  BlogInfo.DEFAULT_IMAGE_RESIZE_THUMB_SIZE[0]+"x"+BlogInfo.DEFAULT_IMAGE_RESIZE_THUMB_SIZE[1]+")";
			resizeOptLabels[1] =_resources.getString(WordPressResource.LABEL_MEDIUM) +
			" ("+_resources.getString(WordPressResource.LABEL_MAX)+" "+  BlogInfo.DEFAULT_IMAGE_RESIZE_MEDIUM_SIZE[0]+"x"+BlogInfo.DEFAULT_IMAGE_RESIZE_MEDIUM_SIZE[1]+")";
			resizeOptLabels[2] = _resources.getString(WordPressResource.LABEL_LARGE) +
			" ("+_resources.getString(WordPressResource.LABEL_MAX)+" "+  BlogInfo.DEFAULT_IMAGE_RESIZE_LARGE_SIZE[0]+"x"+BlogInfo.DEFAULT_IMAGE_RESIZE_LARGE_SIZE[1]+")";
		}
		return resizeOptLabels;
	}
	
	public int[] getDefaultImageResizeSettings(int resizeDim) {	
		int imageResizeWidth = 0;
		int imageResizeHeight = 0;
		
		String key_w = "";
		String key_h = "";
		
		switch (resizeDim) {
		case BlogInfo.SMALL_IMAGE_RESIZE_SETTING:
			key_w = "thumbnail_size_w";
			key_h = "thumbnail_size_h";
			imageResizeWidth =  BlogInfo.DEFAULT_IMAGE_RESIZE_THUMB_SIZE[0];
			imageResizeHeight =  BlogInfo.DEFAULT_IMAGE_RESIZE_THUMB_SIZE[1];
			break;
		case BlogInfo.MEDIUM_IMAGE_RESIZE_SETTING:
			key_w = "medium_size_w";
			key_h = "medium_size_h";
			imageResizeWidth =  BlogInfo.DEFAULT_IMAGE_RESIZE_MEDIUM_SIZE[0];
			imageResizeHeight =  BlogInfo.DEFAULT_IMAGE_RESIZE_MEDIUM_SIZE[1];
			break;
		case BlogInfo.LARGE_IMAGE_RESIZE_SETTING:
			key_w = "large_size_w";
			key_h = "large_size_h";
			imageResizeWidth =  BlogInfo.DEFAULT_IMAGE_RESIZE_LARGE_SIZE[0];
			imageResizeHeight =  BlogInfo.DEFAULT_IMAGE_RESIZE_LARGE_SIZE[1];
			break;
		case BlogInfo.CUSTOM_IMAGE_RESIZE_SETTING:
			imageResizeWidth = this.getImageResizeWidth().intValue();
			imageResizeHeight = this.getImageResizeHeight().intValue();
			return new int[]{imageResizeWidth, imageResizeHeight};
		default:
			 return new int[]{0,0}; 
		}

		try {
			Hashtable blogOptions = this.getBlogOptions();
			Hashtable tmp = (Hashtable)blogOptions.get(key_w);
			imageResizeWidth = Integer.parseInt( String.valueOf(tmp.get("value")) );
			tmp = (Hashtable)blogOptions.get(key_h);
			imageResizeHeight = Integer.parseInt( String.valueOf(tmp.get("value")) );
		} catch (Exception e) {
			//we have set default values b4
		}
		return new int[]{imageResizeWidth, imageResizeHeight};
	}
}