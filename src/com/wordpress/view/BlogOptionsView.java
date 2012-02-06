package com.wordpress.view;

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
import com.wordpress.controller.AccountsController;
import com.wordpress.controller.BaseController;
import com.wordpress.controller.BlogOptionsController;
import com.wordpress.io.BlogDAO;
import com.wordpress.model.Blog;
import com.wordpress.utils.ImageUtils;
import com.wordpress.utils.log.Log;
import com.wordpress.view.component.BaseButtonField;
import com.wordpress.view.component.ClickableLabelField;
import com.wordpress.view.container.BorderedFieldManager;
import com.wordpress.view.dialog.DiscardChangeInquiryView;

public class BlogOptionsView extends StandardBaseView {
	
    private BlogOptionsController controller= null;
	private BasicEditField userNameField;
	private PasswordEditField passwordField;
	private BorderedFieldManager rowResizePhotos;
	private CheckboxField commentNotifications;
	private CheckboxField enableLocation;
	private CheckboxField enableSignature;
	private BasicEditField signatureField;
	private BorderedFieldManager rowVideoPressOptions;
	private CheckboxField resizeVideo;
	private BasicEditField videoResizeWidthField;
	private BasicEditField videoResizeHeightField;
	private CheckboxField enableAuth;
	private BasicEditField authUserNameField;
	private PasswordEditField authPasswordField;

	private CheckboxField resizePhoto;
	private ObjectChoiceField resizeOpt;
	private BasicEditField imageResizeWidthField;
	private BasicEditField imageResizeHeightField;
	private Integer imageResizeWidth;
	private Integer imageResizeHeight;
	
	public static final int[] recentsPostValues={10,20,30,40,50};
	public static final String[] recentsPostValuesLabel={"10","20","30","40","50"};
	
	HorizontalFieldManager buttonsManager;
		
	 public BlogOptionsView(BlogOptionsController blogsController, Blog blog) {
	    	super(blogsController.getBlogName(), Manager.NO_VERTICAL_SCROLL | Manager.NO_VERTICAL_SCROLLBAR);
	    	this.setSubTitleText(_resources.getString(WordPressResource.TITLE_BLOG_OPTION_VIEW));
	    	this.controller=blogsController;
	    	
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

	    	//loading input data	    
			String signature = blog.getSignature();
			if(signature == null) 
				signature = _resources.getString(WordPressResource.DEFAULT_SIGNATURE);
			
			String user =  blog.getUsername();
			String pass =  blog.getPassword();
			final String accountName = blog.getUsername();
			
	        String[] recentPost = recentsPostValuesLabel;
	        int recentPostSelect = indexRecPost;
			boolean isLocation = blog.isLocation();
			
			boolean isCommentNotifications =blog.isCommentNotifies();
			boolean isSignatureActive = blog.isSignatureEnabled();
			boolean isResVideo = blog.isResizeVideos();
			Integer videoResizeWidth = blog.getVideoResizeWidth() == null ? new Integer(0) : blog.getVideoResizeWidth();
			Integer videoResizeHeight = blog.getVideoResizeHeight() == null ? new Integer(0) : blog.getVideoResizeHeight();
	        //end loading
			
			
			//init GUI code
			//not a WP.COM blog - shows the credentials box
			if(blog.isWPCOMBlog() == false) {
	            BorderedFieldManager credentialOptionsRow = new BorderedFieldManager(
		        		Manager.NO_HORIZONTAL_SCROLL
		        		| Manager.NO_VERTICAL_SCROLL
		        		| BorderedFieldManager.BOTTOM_BORDER_NONE);
	            
	            credentialOptionsRow.add(
	   				 GUIFactory.getLabel(_resources.getString(WordPressResource.TITLE_CREDENTIALS), Color.BLACK)
	   				 );
	            credentialOptionsRow.add(GUIFactory.createSepatorField());
	            userNameField = new BasicEditField(_resources.getString(WordPressResource.LABEL_USERNAME)+": ", user, 60, Field.EDITABLE);
	            userNameField.setMargin(5, 0, 5, 0);
	            credentialOptionsRow.add(userNameField);
	            passwordField = new PasswordEditField(_resources.getString(WordPressResource.LABEL_PASSWD)+": ", pass, 64, Field.EDITABLE);
	            passwordField.setMargin(5, 0, 5, 0);
	            credentialOptionsRow.add(passwordField);
	            add(credentialOptionsRow);
			} 
	        //row max number of post/page
            BorderedFieldManager mainOptionsRow = new BorderedFieldManager(
	        		Manager.NO_HORIZONTAL_SCROLL
	        		| Manager.NO_VERTICAL_SCROLL
	        		| BorderedFieldManager.BOTTOM_BORDER_NONE);
            
            mainOptionsRow.add(
				 GUIFactory.getLabel(_resources.getString(WordPressResource.TITLE_MAIN_OPTIONS), Color.BLACK)
				 );
            mainOptionsRow.add(GUIFactory.createSepatorField());
       		enableLocation = new CheckboxField(_resources.getString(WordPressResource.LABEL_LOCATION_ENABLE), isLocation);
    		commentNotifications = new CheckboxField(_resources.getString(WordPressResource.LABEL_COMMENT_NOTIFICATIONS), isCommentNotifications);
    		mainOptionsRow.add(commentNotifications);
    		mainOptionsRow.add(enableLocation);
            add(mainOptionsRow);           

            //row resize photos
            boolean isResImg = blog.isResizePhotos();
            imageResizeWidth = blog.getImageResizeWidth();
            imageResizeHeight = blog.getImageResizeHeight();
            
            rowResizePhotos = new BorderedFieldManager(
            		Manager.NO_HORIZONTAL_SCROLL
            		| Manager.NO_VERTICAL_SCROLL
            		| BorderedFieldManager.BOTTOM_BORDER_NONE);

            LabelField titlePhotoOptions = GUIFactory.getLabel(_resources.getString(WordPressResource.TITLE_PHOTOS_OPTIONS),
            		Color.BLACK);
            rowResizePhotos.add(titlePhotoOptions);
            rowResizePhotos.add(GUIFactory.createSepatorField());
            BasicEditField lblDesc = getDescriptionTextField(_resources.getString(WordPressResource.DESCRIPTION_RESIZEPHOTOS));
            rowResizePhotos.add(lblDesc);

            resizePhoto=new CheckboxField(_resources.getString(WordPressResource.LABEL_RESIZEPHOTOS), isResImg);
            rowResizePhotos.add(resizePhoto);
                        
            String[] resizeOptLabelsFromBlog = blog.getBlogImageResizeLabels(); //read the default resize settings from the blog
            String[] resizeOptLabels = new String[resizeOptLabelsFromBlog.length+1];
            System.arraycopy(resizeOptLabelsFromBlog, 0, resizeOptLabels, 0, resizeOptLabelsFromBlog.length);
            resizeOptLabels[resizeOptLabelsFromBlog.length] = _resources.getString(WordPressResource.LABEL_ALWAYS_ASK);
            
            int selectedResizeOption = blog.getImageResizeSetting().intValue();
            resizeOpt = new ObjectChoiceField( _resources.getString(WordPressResource.LABEL_RESIZE_DIMENSION), resizeOptLabels, selectedResizeOption);
            rowResizePhotos.add(resizeOpt); 
            addImageResizeWidthField();
            addImageResizeHeightField();
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
            		_resources.getString(WordPressResource.LABEL_CUSTOM_RESIZE_IMAGE_WIDTH)+": ", 
            		(videoResizeWidth == null ? "" : videoResizeWidth.toString()), 
            		4, 
            		Field.EDITABLE | BasicEditField.FILTER_NUMERIC);
            rowVideoPressOptions.add(videoResizeWidthField);
            
            videoResizeHeightField = new BasicEditField(
            		_resources.getString(WordPressResource.LABEL_CUSTOM_RESIZE_IMAGE_HEIGHT)+": ", 
            		(videoResizeHeight == null ? "" : videoResizeHeight.toString()), 
            		4, 
            		Field.EDITABLE | BasicEditField.FILTER_NUMERIC);
            rowVideoPressOptions.add(videoResizeHeightField);
     		rowVideoPressOptions.add(getDescriptionTextField(_resources.getString(WordPressResource.DESCRIPTION_DEFAULT_VIDEO_VALUE)));
     		rowVideoPressOptions.add(GUIFactory.createSepatorField());
     		rowVideoPressOptions.add(
     				GUIFactory.createURLLabelField(_resources.getString(WordPressResource.LABEL_LEARN_MORE_VIDEOPRESS),
     						"http://videopress.com", LabelField.FOCUSABLE)
     				);
			add(rowVideoPressOptions);
 			
            //row Signature
            BorderedFieldManager signatureManager = new BorderedFieldManager(
	        		Manager.NO_HORIZONTAL_SCROLL
	        		| Manager.NO_VERTICAL_SCROLL
	        		| BorderedFieldManager.BOTTOM_BORDER_NONE);
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
			
			if(blog.isWPCOMBlog() == false) {
				//not a WP.COM blog - shows the HTTP auth box
				BorderedFieldManager authOptionsRow = new BorderedFieldManager(
						Manager.NO_HORIZONTAL_SCROLL
						| Manager.NO_VERTICAL_SCROLL);

				authOptionsRow.add(
						GUIFactory.getLabel(_resources.getString(WordPressResource.TITLE_HTTP_AUTH_OPTIONS), Color.BLACK)
				);
				authOptionsRow.add(GUIFactory.createSepatorField());
				enableAuth = new CheckboxField(_resources.getString(WordPressResource.LABEL_ENABLE_HTTP_AUTH), blog.isHTTPBasicAuthRequired());
				authOptionsRow.add(enableAuth);
				authUserNameField = new BasicEditField(_resources.getString(WordPressResource.LABEL_USERNAME)+": ", 
						( blog.getHTTPAuthUsername() == null ? "" : blog.getHTTPAuthUsername() ), 60, Field.EDITABLE);
				authUserNameField.setMargin(5, 0, 5, 0);
				authOptionsRow.add(authUserNameField);
				authPasswordField = new PasswordEditField(_resources.getString(WordPressResource.LABEL_PASSWD)+": ", 
						( blog.getHTTPAuthPassword() == null ? "" : blog.getHTTPAuthPassword() ), 64, Field.EDITABLE);
				authPasswordField.setMargin(5, 0, 5, 0);
				authOptionsRow.add(authPasswordField);
				add(authOptionsRow);
			} else {
				//WP.COM account detected - shows the Account box
				BorderedFieldManager credentialOptionsRow = new BorderedFieldManager(
						Manager.NO_HORIZONTAL_SCROLL
						| Manager.NO_VERTICAL_SCROLL);

				credentialOptionsRow.add(
						GUIFactory.getLabel(_resources.getString(WordPressResource.TITLE_VIEW_ACCOUNT), Color.BLACK)
				);
				credentialOptionsRow.add(GUIFactory.createSepatorField());

				ClickableLabelField lblMyAccounts = new ClickableLabelField(_resources.getString(WordPressResource.LABEL_USERNAME)+": " + accountName,
						LabelField.FOCUSABLE | LabelField.ELLIPSIS);
				lblMyAccounts.setTextColor(Color.BLUE);
				//lblMyAccounts.setMargin(2, 5, 5, 5);
				FieldChangeListener existingAccountListener = new FieldChangeListener() {
					public void fieldChanged(Field field, int context) {
						if(context == 0) {
							AccountsController ctrl = new AccountsController(accountName);
							ctrl.showView();
							commentNotifications.setFocus(); //XXX: trick to avoid issue with ourbuttons
						}
					}
				};
				lblMyAccounts.setChangeListener(existingAccountListener);
				credentialOptionsRow.add(lblMyAccounts);
				add(credentialOptionsRow);
			}
			
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
		/*boolean isModified=false;
		Blog blog = controller.getBlog();
		String pass = passwordField.getText();
		String user= userNameField.getText();
		int maxPostIndex = maxRecentPost.getSelectedIndex();
		int valueMaxPostCount = BlogOptionsController.recentsPostValues[maxPostIndex];
		boolean isResPhotos = resizePhoto.getChecked();
		Integer imageResizeWidth = getImageResizeWidth();
		Integer imageResizeHeight = getImageResizeHeight();
		boolean isCommentNotifications = commentNotifications.getChecked();
		boolean isLocation = enableLocation.getChecked();
		can we use isDirty on all view...?
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
		*/
		return this.isDirty();
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

			//self-hosted blog
			if(blog.isWPCOMBlog() == false) {
				String pass = passwordField.getText();
				String user= userNameField.getText();
				blog.setPassword(pass);
				blog.setUsername(user);
				//http auth opts
				blog.setHTTPBasicAuthRequired(enableAuth.getChecked());
				blog.setHTTPAuthPassword(authPasswordField.getText().trim());
				blog.setHTTPAuthUsername(authUserNameField.getText().trim());
			}
			
			boolean isResPhotos = resizePhoto.getChecked();
			blog.setResizePhotos(isResPhotos);
			blog.setImageResizeWidth(imageResizeWidth);
			blog.setImageResizeHeight(imageResizeHeight);
			blog.setCommentNotifies(commentNotifications.getChecked());
			blog.setLocation(enableLocation.getChecked());
			blog.setSignatureEnabled( enableSignature.getChecked() );
			blog.setSignature(signatureField.getText());
			blog.setResizeVideos(resizeVideo.getChecked());
			int resizeSetting = resizeOpt.getSelectedIndex();
			blog.setImageResizeSetting(new Integer(resizeSetting));
			
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
			controller.displayErrorAndWait("Error while saving blog options");
		}
	}

	public BaseController getController() {
		return controller;
	}
		
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
	
	private void addImageResizeWidthField() {
        imageResizeWidthField = new BasicEditField(
        		_resources.getString(WordPressResource.LABEL_CUSTOM_RESIZE_IMAGE_WIDTH)+": ", 
        		(imageResizeWidth == null ? "" : imageResizeWidth.toString()), 
        		4, 
        		Field.EDITABLE | BasicEditField.FILTER_NUMERIC);
        
        imageResizeWidthField.setFocusListener(listenerImageResizeWidthField);
       	rowResizePhotos.add(imageResizeWidthField);
	}

	private void addImageResizeHeightField() {
	    imageResizeHeightField = new BasicEditField(
	    		_resources.getString(WordPressResource.LABEL_CUSTOM_RESIZE_IMAGE_HEIGHT)+": ", 
	    		(imageResizeHeight == null ? "" : imageResizeHeight.toString()), 
	    		4, 
	    		Field.EDITABLE | BasicEditField.FILTER_NUMERIC);
	    
	    imageResizeHeightField.setFocusListener(listenerImageResizeHeightField);
    	rowResizePhotos.add(imageResizeHeightField);
	}

}