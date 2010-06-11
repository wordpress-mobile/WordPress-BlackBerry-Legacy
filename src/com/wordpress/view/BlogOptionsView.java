package com.wordpress.view;

import java.util.Hashtable;

import net.rim.device.api.ui.Color;
import net.rim.device.api.ui.Field;
import net.rim.device.api.ui.FieldChangeListener;
import net.rim.device.api.ui.FocusChangeListener;
import net.rim.device.api.ui.Manager;
import net.rim.device.api.ui.component.BasicEditField;
import net.rim.device.api.ui.component.ButtonField;
import net.rim.device.api.ui.component.CheckboxField;
import net.rim.device.api.ui.component.Dialog;
import net.rim.device.api.ui.component.LabelField;
import net.rim.device.api.ui.component.ObjectChoiceField;
import net.rim.device.api.ui.component.PasswordEditField;
import net.rim.device.api.ui.container.HorizontalFieldManager;

import com.wordpress.bb.WordPressResource;
import com.wordpress.controller.AddBlogsController;
import com.wordpress.controller.BaseController;
import com.wordpress.controller.BlogOptionsController;
import com.wordpress.io.BlogDAO;
import com.wordpress.model.Blog;
import com.wordpress.utils.ImageUtils;
import com.wordpress.utils.log.Log;
import com.wordpress.view.component.BaseButtonField;
import com.wordpress.view.container.BorderedFieldManager;
import com.wordpress.view.dialog.DiscardChangeInquiryView;

public class BlogOptionsView extends StandardBaseView {
	
    private BlogOptionsController controller= null;
	private BasicEditField userNameField;
	private PasswordEditField passwordField;
	private ObjectChoiceField  maxRecentPost;
	private BorderedFieldManager rowResizePhotos;
	private CheckboxField resizePhoto;
	private BasicEditField imageResizeWidthField;
	private BasicEditField imageResizeHeightField;
	private CheckboxField commentNotifications;
	private CheckboxField enableLocation;
	private Integer imageResizeWidth;
	private Integer imageResizeHeight;
	private CheckboxField enableSignature;
	private BasicEditField signatureField;
	private BorderedFieldManager rowVideoPressOptions;
	private CheckboxField resizeVideo;
	private BasicEditField videoResizeWidthField;
	private BasicEditField videoResizeHeightField;
	
	HorizontalFieldManager buttonsManager;

	public Integer getImageResizeWidth() {
		if(imageResizeWidthField != null) {
			return Integer.valueOf(imageResizeWidthField.getText());
		}
		else {
			return new Integer(ImageUtils.DEFAULT_RESIZE_WIDTH);
		}
	}
	
	public Integer getImageResizeHeight() {
		if(imageResizeHeightField != null) {
			return Integer.valueOf(imageResizeHeightField.getText());
		}
		else {
			return new Integer(ImageUtils.DEFAULT_RESIZE_HEIGHT);
		}
	}
		
	 public BlogOptionsView(BlogOptionsController blogsController, Hashtable values) {
	    	super(_resources.getString(WordPressResource.TITLE_BLOG_OPTION_VIEW)+" > "+ blogsController.getBlogName(), Manager.NO_VERTICAL_SCROLL | Manager.NO_VERTICAL_SCROLLBAR);
	    	this.controller=blogsController;
	    	
	        //loading input data
	        String user = (String)values.get("user");
	        String pass= (String)values.get("pass");
	        String[] recentPost=(String[])values.get("recentpost");
	        int recentPostSelect = ((Integer)values.get("recentpostselected")).intValue();
			boolean isResImg = ((Boolean)values.get("isresphotos")).booleanValue();
			imageResizeWidth = (Integer)values.get("imageResizeWidth");
			imageResizeHeight = (Integer)values.get("imageResizeHeight");
			boolean isLocation = ((Boolean)values.get("islocation")).booleanValue();
			boolean isCommentNotifications = ((Boolean)values.get("iscommentnotifications")).booleanValue();
			boolean isSignatureActive = ((Boolean)values.get("isSignatureActive")).booleanValue();
			String signature = (String)values.get("signature");
			boolean isResVideo = ((Boolean)values.get("isresvideos")).booleanValue();
			Integer videoResizeWidth = (Integer)values.get("videoResizeWidth");
			Integer videoResizeHeight = (Integer)values.get("videoResizeHeight");
	        //end loading
			
	        //row username & password & max number of post/page
            BorderedFieldManager mainOptionsRow = new BorderedFieldManager(
	        		Manager.NO_HORIZONTAL_SCROLL
	        		| Manager.NO_VERTICAL_SCROLL
	        		| BorderedFieldManager.BOTTOM_BORDER_NONE);
            
            mainOptionsRow.add(
				 GUIFactory.getLabel(_resources.getString(WordPressResource.TITLE_MAIN_OPTIONS), Color.BLACK)
				 );
            mainOptionsRow.add(GUIFactory.createSepatorField());

            userNameField = new BasicEditField(_resources.getString(WordPressResource.LABEL_USERNAME)+": ", user, 60, Field.EDITABLE);
            userNameField.setMargin(5, 0, 5, 0);
            mainOptionsRow.add(userNameField);
            passwordField = new PasswordEditField(_resources.getString(WordPressResource.LABEL_PASSWD)+": ", pass, 64, Field.EDITABLE);
            passwordField.setMargin(5, 0, 5, 0);
            mainOptionsRow.add(passwordField);
            maxRecentPost = new ObjectChoiceField (_resources.getString(WordPressResource.LABEL_MAX_RECENT_BLOG_ITEMS), recentPost,recentPostSelect);
            maxRecentPost.setMargin(5, 0, 5, 0);
            mainOptionsRow.add(maxRecentPost);
       		enableLocation = new CheckboxField(_resources.getString(WordPressResource.LABEL_LOCATION_ENABLE), isLocation);
    		commentNotifications = new CheckboxField(_resources.getString(WordPressResource.LABEL_COMMENT_NOTIFICATIONS), isCommentNotifications);
    		mainOptionsRow.add(commentNotifications);
    		mainOptionsRow.add(enableLocation);
            add(mainOptionsRow);           

            //row resize photos
            rowResizePhotos = new BorderedFieldManager(
	        		Manager.NO_HORIZONTAL_SCROLL
	        		| Manager.NO_VERTICAL_SCROLL
	        		| BorderedFieldManager.BOTTOM_BORDER_NONE);
            
			 LabelField titlePhotoOptions = GUIFactory.getLabel(_resources.getString(WordPressResource.TITLE_PHOTOS_OPTIONS),
					 Color.BLACK);
			 rowResizePhotos.add(titlePhotoOptions);
			 rowResizePhotos.add(GUIFactory.createSepatorField());
			 
    		resizePhoto=new CheckboxField(_resources.getString(WordPressResource.LABEL_RESIZEPHOTOS), isResImg);
    		resizePhoto.setChangeListener(listenerResizePhotoCheckbox);
    		rowResizePhotos.add(resizePhoto);
     		BasicEditField lblDesc = getDescriptionTextField(_resources.getString(WordPressResource.DESCRIPTION_RESIZEPHOTOS));
			rowResizePhotos.add(lblDesc);
			
			if(isResImg) {
				addImageResizeWidthField();
				addImageResizeHeightField();
			}
            
			add(rowResizePhotos);
			
            //row resize Videos
			rowVideoPressOptions = new BorderedFieldManager(
	        		Manager.NO_HORIZONTAL_SCROLL
	        		| Manager.NO_VERTICAL_SCROLL
	        		| BorderedFieldManager.BOTTOM_BORDER_NONE);
			
			 LabelField lblTitleVideoPress = GUIFactory.getLabel(_resources.getString(WordPressResource.TITLE_VIDEOPRESS_OPTIONS),
					 Color.BLACK);
			 rowVideoPressOptions.add(lblTitleVideoPress);
			 rowVideoPressOptions.add(GUIFactory.createSepatorField());
			 
    		resizeVideo = new CheckboxField(_resources.getString(WordPressResource.LABEL_RESIZEVIDEOS), isResVideo);
    		rowVideoPressOptions.add(resizeVideo);
     		BasicEditField lblDescResizeVideo = getDescriptionTextField(_resources.getString(WordPressResource.DESCRIPTION_RESIZEVIDEOS));
     		rowVideoPressOptions.add(lblDescResizeVideo);
			
            videoResizeWidthField = new BasicEditField(
            		_resources.getString(WordPressResource.LABEL_RESIZE_IMAGE_WIDTH)+": ", 
            		(videoResizeWidth == null ? "" : videoResizeWidth.toString()), 
            		4, 
            		Field.EDITABLE | BasicEditField.FILTER_NUMERIC);
            rowVideoPressOptions.add(videoResizeWidthField);
            
            videoResizeHeightField = new BasicEditField(
            		_resources.getString(WordPressResource.LABEL_RESIZE_IMAGE_HEIGHT)+": ", 
            		(videoResizeHeight == null ? "" : videoResizeHeight.toString()), 
            		4, 
            		Field.EDITABLE | BasicEditField.FILTER_NUMERIC);
            rowVideoPressOptions.add(videoResizeHeightField);
			add(rowVideoPressOptions);
 			
            //row Signature
            BorderedFieldManager signatureManager = new BorderedFieldManager(
	        		Manager.NO_HORIZONTAL_SCROLL
	        		| Manager.NO_VERTICAL_SCROLL);
            signatureManager.add(
            		GUIFactory.getLabel(_resources.getString(WordPressResource.TITLE_SIGNATURE_OPTIONS),Color.BLACK)
            		);
            signatureManager.add(GUIFactory.createSepatorField());
    		enableSignature = new CheckboxField(_resources.getString(WordPressResource.DESCRIPTION_ADD_SIGNATURE), isSignatureActive);
    		signatureManager.add(enableSignature);
    		signatureField = new BasicEditField(_resources.getString(WordPressResource.LABEL_SIGNATURE)+": ", signature, 1000, Field.EDITABLE);
    		signatureField.setMargin(5, 0, 5, 0);
    		signatureManager.add(signatureField);
			add(signatureManager);
			
            BaseButtonField buttonOK = GUIFactory.createButton(_resources.getString(WordPressResource.BUTTON_OK), ButtonField.CONSUME_CLICK);
            BaseButtonField buttonBACK= GUIFactory.createButton(_resources.getString(WordPressResource.BUTTON_BACK), ButtonField.CONSUME_CLICK);
            buttonBACK.setChangeListener(listenerBackButton);
            buttonOK.setChangeListener(listenerOkButton);
            
            HorizontalFieldManager buttonsManager = new HorizontalFieldManager(Field.FIELD_HCENTER);
            buttonsManager.add(buttonOK);
    		buttonsManager.add(buttonBACK);
    		add(buttonsManager);
    		add(new LabelField("", Field.NON_FOCUSABLE)); //space after buttons
	}

	 private FieldChangeListener listenerBackButton = new FieldChangeListener() {
		 public void fieldChanged(Field field, int context) {
			 dismissView();
		 }
	 };
	 
	 private FieldChangeListener listenerOkButton = new FieldChangeListener() {
		    public void fieldChanged(Field field, int context) {
		    	boolean isModified = isModified();
				if(!isModified) {
					controller.backCmd();
					return;
				} else  {
					saveAndBack();
				}
		   }
		};

	 
	 public boolean onClose()   {
		 return dismissView();			
	 }
	 
	 private boolean dismissView() {
		 boolean isModified = isModified();

		 if(!isModified) {
			 controller.backCmd();
			 return true;
		 }

		 String quest=_resources.getString(WordPressResource.MESSAGE_INQUIRY_DIALOG_BOX);
		 DiscardChangeInquiryView infoView= new DiscardChangeInquiryView(quest);
		 int choice=infoView.doModal();    	 
		 if(Dialog.DISCARD == choice) {
			 Log.trace("user has selected discard");
			 controller.backCmd();
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

	private boolean isModified() {
		boolean isModified=false;
		Blog blog = controller.getBlog();
		String pass = passwordField.getText();
		String user= userNameField.getText();
		int maxPostIndex = maxRecentPost.getSelectedIndex();
		int valueMaxPostCount = AddBlogsController.recentsPostValues[maxPostIndex];
		boolean isResPhotos = resizePhoto.getChecked();
		Integer imageResizeWidth = getImageResizeWidth();
		Integer imageResizeHeight = getImageResizeHeight();
		boolean isCommentNotifications = commentNotifications.getChecked();
		boolean isLocation = enableLocation.getChecked();
		//can we use isDirty on all view...?
		if(!blog.getUsername().equals(user) || !blog.getPassword().equals(pass)
			|| blog.getMaxPostCount() != valueMaxPostCount 
			|| isResPhotos != blog.isResizePhotos()			
			|| !imageResizeWidth.equals(blog.getImageResizeWidth() )
			|| !imageResizeHeight.equals(blog.getImageResizeHeight()) 
			|| isCommentNotifications != blog.isCommentNotifies()  
			|| isLocation != blog.isLocation()
			|| enableSignature.isDirty()
			|| signatureField.isDirty()
			|| resizeVideo.isDirty()
			|| videoResizeHeightField.isDirty()
			|| videoResizeWidthField.isDirty()
		) {
			isModified=true;
		}
		
		return isModified;
	}
	
	
	//called when user click the OK button
	private void  saveAndBack(){
		try {

			Blog blog = controller.getBlog();

			//Before saving we should do an additional check over img resize width and height.
			//it is necessary when user put a value into width/height field and then press backbutton;
			//the focus lost on those fields is never fired....
			Integer imageResizeWidth = getImageResizeWidth();
			Integer imageResizeHeight = getImageResizeHeight();
			int[] keepAspectRatio = ImageUtils.keepAspectRatio(imageResizeWidth.intValue(), imageResizeHeight.intValue());
			imageResizeWidth = new Integer(keepAspectRatio[0]);
			imageResizeHeight = new Integer(keepAspectRatio[1]);

			String pass = passwordField.getText();
			String user= userNameField.getText();
			int maxPostIndex = maxRecentPost.getSelectedIndex();
			int valueMaxPostCount = AddBlogsController.recentsPostValues[maxPostIndex];
			boolean isResPhotos = resizePhoto.getChecked();
			blog.setPassword(pass);
			blog.setUsername(user);
			blog.setResizePhotos(isResPhotos);
			blog.setImageResizeWidth(imageResizeWidth);
			blog.setImageResizeHeight(imageResizeHeight);
			blog.setMaxPostCount(valueMaxPostCount);
			blog.setCommentNotifies(commentNotifications.getChecked());
			blog.setLocation(enableLocation.getChecked());
			blog.setSignatureEnabled( enableSignature.getChecked() );
			blog.setSignature(signatureField.getText());
			blog.setResizeVideos(resizeVideo.getChecked());
			
			try {
				blog.setVideoResizeWidth(
						Integer.valueOf(videoResizeWidthField.getText())
				);
			} catch (NumberFormatException e) {
				Log.error(e, "Error reading video resizing width");
				blog.setVideoResizeWidth(
						new Integer(0)
				);
			}
			
			try {
				blog.setVideoResizeHeight(
						Integer.valueOf(videoResizeHeightField.getText())
				);
			} catch (NumberFormatException e) {
				Log.error(e, "Error reading video resizing height");
				blog.setVideoResizeHeight(
						new Integer(0)
				);
			}
			
			BlogDAO.updateBlog(blog);
			controller.backCmd();
		} catch (Exception e) {
			Log.error(e, "Error while saving blog options");
			controller.displayErrorAndWait("Error while saving blog options");
		}
	}

	public BaseController getController() {
		return controller;
	}
	
	// Enable or disable image resize width/height fields when the "resize image" checkbox changes.
	private FieldChangeListener listenerResizePhotoCheckbox = new FieldChangeListener() {
	    public void fieldChanged(Field field, int context) {
	    	if(resizePhoto.getChecked() == true) {
	    		addImageResizeWidthField();
	    		addImageResizeHeightField();
	    	}
	    	else {
	    	       	rowResizePhotos.delete(imageResizeWidthField);
	    	       	imageResizeWidthField = null;
	    	       	rowResizePhotos.delete(imageResizeHeightField);
	    	       	imageResizeHeightField = null;
	    	}
	   }
	};
	
	// Recalculate the image resize height whenever the image resize width changes. Aspect ratio is fixed.
	private FocusChangeListener listenerImageResizeWidthField = new FocusChangeListener() {
	    public void focusChanged(Field field, int eventType) {
	    	if((eventType == FocusChangeListener.FOCUS_LOST) && (imageResizeWidthField.isDirty())) {
		    	try {
		    		int newWidth = Integer.parseInt(imageResizeWidthField.getText());
		    		if(newWidth == 0) {
		    			Dialog.alert(_resources.getString(WordPressResource.ERROR_RESIZE_WIDTH));
		    			imageResizeWidthField.setText(String.valueOf(ImageUtils.DEFAULT_RESIZE_WIDTH));
		    			imageResizeHeightField.setText(String.valueOf(ImageUtils.DEFAULT_RESIZE_HEIGHT));
		    		}
		    		else {
		    			int newHeight = (int)(newWidth * 0.75);
		    			imageResizeHeightField.setText(Integer.toString(newHeight));
		    		}
		    	}
		    	catch(NumberFormatException e) {
		    		Log.error("Unexpected condition: ImageResizeWidthField was not numeric in BlogOptionsView.");
		    	}
	    	}
	   }
	};

	// Recalculate the image resize width whenever the image resize height changes. Aspect ratio is fixed.
	private FocusChangeListener listenerImageResizeHeightField = new FocusChangeListener() {
	    public void focusChanged(Field field, int eventType) {
	    	if((eventType == FocusChangeListener.FOCUS_LOST) && (imageResizeHeightField.isDirty())) {
		    	try {
		    		int newHeight = Integer.parseInt(imageResizeHeightField.getText());
		    		if(newHeight == 0) {
		    			Dialog.alert(_resources.getString(WordPressResource.ERROR_RESIZE_HEIGHT));
		    			imageResizeWidthField.setText(String.valueOf(ImageUtils.DEFAULT_RESIZE_WIDTH));
		    			imageResizeHeightField.setText(String.valueOf(ImageUtils.DEFAULT_RESIZE_HEIGHT));
		    		}
		    		else {
		    			int newWidth = (int)((newHeight * 1.3333) + 1);
		    			imageResizeWidthField.setText(Integer.toString(newWidth));
		    		}
		    	}
		    	catch(NumberFormatException e) {
		    		Log.error("Unexpected condition: ImageResizeHeightField was not numeric in BlogOptionsView.");
		    	}
	    	}
	   }
	};
	
	private void addImageResizeWidthField() {
        imageResizeWidthField = new BasicEditField(
        		_resources.getString(WordPressResource.LABEL_RESIZE_IMAGE_WIDTH)+": ", 
        		(imageResizeWidth == null ? "" : imageResizeWidth.toString()), 
        		4, 
        		Field.EDITABLE | BasicEditField.FILTER_NUMERIC);
        
        imageResizeWidthField.setFocusListener(listenerImageResizeWidthField);
       	rowResizePhotos.add(imageResizeWidthField);
	}

	private void addImageResizeHeightField() {
	    imageResizeHeightField = new BasicEditField(
	    		_resources.getString(WordPressResource.LABEL_RESIZE_IMAGE_HEIGHT)+": ", 
	    		(imageResizeHeight == null ? "" : imageResizeHeight.toString()), 
	    		4, 
	    		Field.EDITABLE | BasicEditField.FILTER_NUMERIC);
	    
	    imageResizeHeightField.setFocusListener(listenerImageResizeHeightField);
    	rowResizePhotos.add(imageResizeHeightField);
	}

}