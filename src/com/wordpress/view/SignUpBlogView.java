//#preprocess
package com.wordpress.view;

import java.util.Hashtable;
import java.util.Vector;

import net.rim.device.api.system.Display;
import net.rim.device.api.system.EncodedImage;
import net.rim.device.api.ui.Color;
import net.rim.device.api.ui.Field;
import net.rim.device.api.ui.FieldChangeListener;
import net.rim.device.api.ui.Font;
import net.rim.device.api.ui.Manager;
import net.rim.device.api.ui.MenuItem;
import net.rim.device.api.ui.UiApplication;
//#ifdef IS_OS47_OR_ABOVE
import net.rim.device.api.ui.VirtualKeyboard;
//#endif
import net.rim.device.api.ui.component.BasicEditField;
import net.rim.device.api.ui.component.BitmapField;
import net.rim.device.api.ui.component.ButtonField;
import net.rim.device.api.ui.component.CheckboxField;
import net.rim.device.api.ui.component.LabelField;
import net.rim.device.api.ui.component.PasswordEditField;
import net.rim.device.api.ui.container.HorizontalFieldManager;

import com.wordpress.bb.WordPressCore;
import com.wordpress.bb.WordPressResource;
import com.wordpress.controller.AccountsController;
import com.wordpress.controller.BaseController;
import com.wordpress.controller.FrontController;
import com.wordpress.controller.SignUpBlogController;
import com.wordpress.io.BlogDAO;
import com.wordpress.model.Blog;
import com.wordpress.model.BlogInfo;
import com.wordpress.utils.ImageUtils;
import com.wordpress.utils.log.Log;
import com.wordpress.utils.observer.Observable;
import com.wordpress.utils.observer.Observer;
import com.wordpress.view.component.BaseButtonField;
import com.wordpress.view.component.ColoredLabelField;
import com.wordpress.view.container.BorderedFieldManager;
import com.wordpress.view.dialog.ConnectionDialogClosedListener;
import com.wordpress.view.dialog.ConnectionInProgressView;
import com.wordpress.xmlrpc.BlogConnResponse;
import com.wordpress.xmlrpc.BlogSignUpConn;


public class SignUpBlogView extends StandardBaseView {
	
    private SignUpBlogController controller = null;
	private ConnectionInProgressView connectionProgressView=null;
	private BlogSignUpConn connection;
	
    private BorderedFieldManager rowBlogURL;
    private BasicEditField blogNameField;
    private LabelField fullblogNameField;
    private ColoredLabelField blogNameErrorField;
    
    private BorderedFieldManager rowUserName;
    private BasicEditField userNameField;
    private ColoredLabelField userNameErrorField;
    
    private BorderedFieldManager rowEmail;
    private BasicEditField emailField;
    private ColoredLabelField emailErrorField;
	
    private BorderedFieldManager rowPassword;
    private PasswordEditField passwordField;
    private ColoredLabelField passwordErrorField;
    
    private CheckboxField userAgreeTOS;
			
	public SignUpBlogView(SignUpBlogController addBlogsController) {
	    	super( Manager.NO_VERTICAL_SCROLL | Manager.NO_VERTICAL_SCROLLBAR);
	    	this.controller=addBlogsController;
	    	
			//Set the preferred width to the image size or screen width if the image is larger than the screen width.
	    	EncodedImage classicHeaderImg = EncodedImage.getEncodedImageResource("logo-wpcom-login.png");
			int _preferredWidth = -1;
	        if (classicHeaderImg.getWidth() > Display.getWidth()) {
	            _preferredWidth = Display.getWidth();
	        }
	        if( _preferredWidth != -1) {        	
	        	EncodedImage resImg = ImageUtils.resizeEncodedImage(classicHeaderImg, _preferredWidth, classicHeaderImg.getHeight());
	        	classicHeaderImg = resImg;
	        }
	        BitmapField wpClassicHeaderBitmapField =  new BitmapField(classicHeaderImg.getBitmap(), Field.FIELD_HCENTER | Field.FIELD_VCENTER);
	        add(wpClassicHeaderBitmapField);
	        
	        //intro text
	        BasicEditField introTextField = new BasicEditField(BasicEditField.READONLY);
	        introTextField.setText(_resources.getString(WordPressResource.MESSAGE_SIGNUP_BLOG));
	        introTextField.setMargin(10, 10, 2, 10);
	    	add(introTextField);
	        
	    	//blog URL
            rowBlogURL = new BorderedFieldManager(
	        		Manager.NO_HORIZONTAL_SCROLL
	        		| Manager.NO_VERTICAL_SCROLL 
	        		| BorderedFieldManager.BOTTOM_BORDER_NONE);
            
            rowBlogURL.add(GUIFactory.getLabel(_resources.getString(WordPressResource.LABEL_BLOG_ADDRESS), Color.BLACK));
            rowBlogURL.add(GUIFactory.createSepatorField());
            blogNameField = new BasicEditField("", "insert your blog name", 60, Field.EDITABLE);
            rowBlogURL.add(blogNameField);
            fullblogNameField = GUIFactory.getLabel(".wordpress.com", Color.BLACK);
            Font fnt = Font.getDefault().derive(Font.ITALIC);
            fullblogNameField.setFont(fnt);
            rowBlogURL.add(fullblogNameField);
            
    		blogNameErrorField = new ColoredLabelField("", Color.RED);
    		blogNameErrorField.setMargin(2, 5, 5, 5);
    		blogNameField.setChangeListener(blogURLChangeListener);
            add(rowBlogURL);
            
            //username
            rowUserName = new BorderedFieldManager(
	        		Manager.NO_HORIZONTAL_SCROLL
	        		| Manager.NO_VERTICAL_SCROLL 
	        		| BorderedFieldManager.BOTTOM_BORDER_NONE);
            rowUserName.add(
      				 GUIFactory.getLabel(_resources.getString(WordPressResource.LABEL_USERNAME), Color.BLACK)
      				 );
            rowUserName.add(GUIFactory.createSepatorField());
            userNameField = new BasicEditField("", "", 60, Field.EDITABLE);
            rowUserName.add(userNameField);
      		userNameErrorField = new ColoredLabelField("", Color.RED);
    		userNameErrorField.setMargin(2, 5, 5, 5);
            add(rowUserName);
            
            rowEmail = new BorderedFieldManager(
	        		Manager.NO_HORIZONTAL_SCROLL
	        		| Manager.NO_VERTICAL_SCROLL 
	        		| BorderedFieldManager.BOTTOM_BORDER_NONE);
            rowEmail.add(
      				 GUIFactory.getLabel(_resources.getString(WordPressResource.LABEL_EMAIL), Color.BLACK)
      				 );
            rowEmail.add(GUIFactory.createSepatorField());
            emailField = new BasicEditField("", "", 100, BasicEditField.FILTER_EMAIL);
            rowEmail.add(emailField);
    		emailErrorField = new ColoredLabelField("", Color.RED);
    		emailErrorField.setMargin(2, 5, 5, 5);
            add(rowEmail);
            
            rowPassword = new BorderedFieldManager(
            		Manager.NO_HORIZONTAL_SCROLL
            		| Manager.NO_VERTICAL_SCROLL 
            );
            rowPassword.add(
            		GUIFactory.getLabel(_resources.getString(WordPressResource.LABEL_PASSWD), Color.BLACK)
            );
            rowPassword.add(GUIFactory.createSepatorField());
            passwordField = new PasswordEditField("", "", 64, Field.EDITABLE);
            rowPassword.add(passwordField);
    		passwordErrorField = new ColoredLabelField("", Color.RED);
    		passwordErrorField.setMargin(2, 5, 5, 5);
    		add(rowPassword);
                       
    		//TOS disclaimer
    		userAgreeTOS = new CheckboxField("You agree to the fascinating terms of service at http://wordpress.com/tos", false);
    		userAgreeTOS.setMargin(10, 10, 1, 10);
        	add(userAgreeTOS);
        	LabelField urlAddr = GUIFactory.createURLLabelField("More info", "http://wordpress.com/tos", LabelField.FOCUSABLE);
        	urlAddr.setMargin(0, 10, 10, 10);
        	add(urlAddr);

            BaseButtonField buttonOK = GUIFactory.createButton(_resources.getString(WordPressResource.BUTTON_SIGN_UP), ButtonField.CONSUME_CLICK);
            buttonOK.setChangeListener(listenerOkButton);
            HorizontalFieldManager buttonsManager = new HorizontalFieldManager(Field.FIELD_HCENTER);
            buttonsManager.add(buttonOK);
    		add(buttonsManager); 
        	
    		add(new LabelField("", Field.NON_FOCUSABLE));

    		addMenuItem(_addBlogItem);
    		
    		passwordErrorField = new ColoredLabelField("", Color.RED);
    		passwordErrorField.setMargin(2, 5, 5, 5);
	}
	 

	private void checkUserValues() {
		if(userAgreeTOS.getChecked() == false) {
			controller.displayMessage("You must agree to the terms of service at http://wordpress.com/tos before submitting this form.");
			return;
		}
		
		if (userNameField.getText().trim().length() == 0
				||  passwordField.getText().trim().length() == 0
				||  blogNameField.getText().trim().length() == 0
				||  emailField.getText().trim().length() == 0
		) {
			String waitString = null;
			
			if( blogNameField.getText().trim().length() < 4)
				waitString = "Site name must be at least 4 characters.";
			else if (userNameField.getText().trim().length() == 0)
				waitString = "User name must be at least 4 characters. Lowercase letters and numbers only.";
			else if (passwordField.getText().trim().length() == 0)
					waitString = "Use a mix of upper and lowercase characters to create a strong password. " +
							"If your password isn’t strong enough, you won’t be able to continue with signup.";
			else if (emailField.getText().trim().length() == 0)
				waitString = "The value you provided for your email is not valid.";
			
			controller.displayMessage(waitString);
			
			resetErrorFields();
			return;
			
		} else {
			resetErrorFields();
		}
		
		//#ifdef IS_OS47_OR_ABOVE
		VirtualKeyboard virtKbd = getVirtualKeyboard();
		if(virtKbd != null)
			virtKbd.setVisibility(VirtualKeyboard.HIDE);
		//#endif
		signup(blogNameField.getText(),userNameField.getText(), emailField.getText(), passwordField.getText()); 
	}


	private void resetErrorFields() {
		//reset the error fields
		if(rowBlogURL.getFieldCount() == 5)
			rowBlogURL.delete(blogNameErrorField);
		if(rowUserName.getFieldCount() == 4)
			rowUserName.delete(userNameErrorField);			
		if(rowPassword.getFieldCount() == 4)
			rowPassword.delete(passwordErrorField);
		if(rowEmail.getFieldCount() == 4)
			rowEmail.delete(emailErrorField);
		
		blogNameErrorField.setText("");
		userNameErrorField.setText("");
		emailErrorField.setText("");
		passwordErrorField.setText("");
	}
	
	 private FieldChangeListener blogURLChangeListener = new FieldChangeListener() {
	    	public void fieldChanged(Field field, int context) {
	  /*  		if(context == 1){
	    			return; //not a user changes
	    		}
	    */
	    		UiApplication.getUiApplication().invokeLater(new Runnable() {
					public void run() {
						fullblogNameField.setText(blogNameField.getText()+".wordpress.com");	
					} //end run
				});
	  
	    	}
	 };
	 
	private MenuItem _addBlogItem = new MenuItem( _resources, WordPressResource.BUTTON_SIGN_UP, 140, 10) {
		public void run() {
			checkUserValues();
		}
	};
	
	private FieldChangeListener listenerOkButton = new FieldChangeListener() {
	    public void fieldChanged(Field field, int context) {
	    	checkUserValues();
	    }
	};
	
	// Utility routine to by-pass the standard dialog box when the screen is closed  
	public boolean onClose(){
		controller.backCmd();
		return true;
	}
	
	public BaseController getController() {
		return controller;
	}
	
	private void signup(String blogName, String user, String email, String passwd){
		user = user.trim();
		passwd = passwd.trim();
		blogName = blogName.trim();
		email = email.trim();

		if (user != null && user != null && user.length() > 0) {
        	connection = new BlogSignUpConn ("https://wordpress.com/xmlrpc.php", user, passwd, blogName, email);
        	connectionProgressView= new ConnectionInProgressView(
        			_resources.getString(WordPressResource.CONNECTION_INPROGRESS));
        	connectionProgressView.setDialogClosedListener(new ConnectionDialogClosedListener(connection));
        	connection.addObserver(new BlogSignUpCallBack(connectionProgressView)); 
            connectionProgressView.show();
            connection.startConnWork();
        }
	}
		
	//callback for signup to the blog
	private class BlogSignUpCallBack implements Observer {
		ConnectionInProgressView dialog;
		
		public BlogSignUpCallBack(ConnectionInProgressView dialog) {
			super();
			this.dialog = dialog;
		}

		public void update(Observable observable, final Object object) {
			try{
				Log.trace("BlogSignUpCallBack->update");
				controller.dismissDialog(dialog);
				BlogConnResponse resp = (BlogConnResponse)object;

				if(resp.isStopped()){
					return;
				}
				
				if(resp.isError()) {
					final String respMessage=resp.getResponse();
					controller.displayError(respMessage);
					return;
				}
				
				try {
					Log.debug(resp.getResponseObject(), "it is a success");			
					final Hashtable tempData = (Hashtable)resp.getResponseObject();
							
					Log.trace(String.valueOf(tempData.get("success")));

					if( ((Boolean)tempData.get("success")).booleanValue() ) {
					
						Blog currentBlog = new Blog("-1",
								fullblogNameField.getText().trim(),
								fullblogNameField.getText().trim(), 
								"https://"+fullblogNameField.getText().trim()+"/xmlrpc.php", 
								userNameField.getText().trim(), 
								passwordField.getText().trim());

						currentBlog.setImageResizeWidth(new Integer(ImageUtils.DEFAULT_RESIZE_WIDTH));
						currentBlog.setImageResizeHeight(new Integer(ImageUtils.DEFAULT_RESIZE_HEIGHT));
						currentBlog.setLoadingState(BlogInfo.STATE_PENDING_ACTIVATION);
						currentBlog.setWPCOMBlog(true);
						
						try { 							
							Blog[] blogs = {currentBlog};
							AccountsController.storeWPCOMAccount(blogs);
							Vector tmpBlog = new Vector(1);
							tmpBlog.addElement(currentBlog);
							BlogDAO.newBlogs(tmpBlog);
							if(tmpBlog.size() == 0) 
								throw new Exception ("Error while adding blog");
						} catch (Exception e) {
							if(e != null && e.getMessage()!= null ) {
								controller.displayMessage(e.getMessage());
							} else {
								controller.displayMessage("Error while adding blog");			
							}
							return;
						}

						Vector applicationBlogs = WordPressCore.getInstance().getApplicationBlogs();
						BlogInfo blogI = new BlogInfo(currentBlog);
						applicationBlogs.addElement(blogI);
						controller.displayMessage("An e-mail has been sent to "+ emailField.getText().trim() +" to activate your account. " +
								"Check your inbox and click the link in the message. It should arrive within 30 minutes.");
						FrontController.getIstance().backAndRefreshView(true);
						return;

					} else {
						
						UiApplication.getUiApplication().invokeLater(new Runnable() {
							public void run() {
								if(tempData.get("blogname") != null) {
									Log.trace(String.valueOf(tempData.get("blogname")));
									blogNameErrorField.setText(String.valueOf(tempData.get("blogname")));
									rowBlogURL.add(blogNameErrorField);
								}
								if(tempData.get("user_name") != null) {
									Log.trace(String.valueOf(tempData.get("user_name")));
									userNameErrorField.setText(String.valueOf(tempData.get("user_name")));
									rowUserName.add(userNameErrorField);			
								}
								if(tempData.get("user_email") != null) {
									Log.trace(String.valueOf(tempData.get("user_email")));
									emailErrorField.setText(String.valueOf(tempData.get("user_email")));
									rowEmail.add(emailErrorField);
								}
								if(tempData.get("pass1") != null) {
									Log.trace(String.valueOf(tempData.get("pass1")));
									passwordErrorField.setText(String.valueOf(tempData.get("pass1")));
									rowPassword.add(passwordErrorField);
								}
							} //end run
						});
					}//end elses
				} catch (Exception e) {
					Log.error(e, "Error while adding blogs");
				}

			} catch (final Exception e) {
				controller.displayError(e,"Error while during signup, please try later.");	
			} 
		}
	}
}