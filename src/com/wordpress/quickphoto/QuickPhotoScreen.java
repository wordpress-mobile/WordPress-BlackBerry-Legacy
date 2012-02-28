//#preprocess
package com.wordpress.quickphoto;

import java.io.IOException;
import java.util.Vector;

import net.rim.device.api.system.Application;
import net.rim.device.api.system.Bitmap;
import net.rim.device.api.system.Characters;
import net.rim.device.api.system.EncodedImage;
import net.rim.device.api.system.KeypadListener;
import net.rim.device.api.ui.Color;
import net.rim.device.api.ui.ContextMenu;
import net.rim.device.api.ui.DrawStyle;
import net.rim.device.api.ui.Field;
import net.rim.device.api.ui.FieldChangeListener;
import net.rim.device.api.ui.Font;
import net.rim.device.api.ui.Manager;
import net.rim.device.api.ui.MenuItem;
import net.rim.device.api.ui.Screen;
import net.rim.device.api.ui.TouchEvent;
import net.rim.device.api.ui.TransitionContext;
import net.rim.device.api.ui.Ui;
import net.rim.device.api.ui.UiApplication;
import net.rim.device.api.ui.UiEngineInstance;
import net.rim.device.api.ui.XYEdges;
import net.rim.device.api.ui.component.BasicEditField;
import net.rim.device.api.ui.component.BitmapField;
import net.rim.device.api.ui.component.ButtonField;
import net.rim.device.api.ui.component.Dialog;
import net.rim.device.api.ui.component.DialogClosedListener;
import net.rim.device.api.ui.component.LabelField;
import net.rim.device.api.ui.component.Menu;
import net.rim.device.api.ui.container.HorizontalFieldManager;
import net.rim.device.api.ui.container.MainScreen;
import net.rim.device.api.ui.container.VerticalFieldManager;
import net.rim.device.api.ui.decor.BackgroundFactory;
import net.rim.device.api.ui.decor.Border;
import net.rim.device.api.ui.decor.BorderFactory;
import net.rim.device.api.ui.extension.container.ZoomScreen;

import com.wordpress.bb.WordPressCore;
import com.wordpress.bb.WordPressResource;
import com.wordpress.controller.BaseController;
import com.wordpress.controller.MainController;
import com.wordpress.io.DraftDAO;
import com.wordpress.io.JSR75FileSystem;
import com.wordpress.model.PhotoEntry;
import com.wordpress.model.Post;
import com.wordpress.task.SendToBlogTask;
import com.wordpress.task.TaskProgressListener;
import com.wordpress.utils.ImageUtils;
import com.wordpress.utils.MultimediaUtils;
import com.wordpress.utils.StringUtils;
import com.wordpress.utils.log.Log;
import com.wordpress.view.GUIFactory;
import com.wordpress.view.StandardBaseView;
import com.wordpress.view.component.BaseButtonField;
import com.wordpress.view.component.HtmlTextField;
import com.wordpress.view.component.MarkupToolBar;
import com.wordpress.view.component.MarkupToolBarTextFieldMediator;
import com.wordpress.view.component.FileBrowser.RimFileBrowser;
import com.wordpress.view.component.FileBrowser.RimFileBrowserListener;
import com.wordpress.view.container.BorderedFieldManager;
import com.wordpress.view.container.JustifiedEvenlySpacedHorizontalFieldManager;
import com.wordpress.view.container.TableLayoutManager;
import com.wordpress.view.dialog.ConnectionInProgressView;
import com.wordpress.xmlrpc.BlogConn;
import com.wordpress.xmlrpc.post.NewPostConn;

public class QuickPhotoScreen extends StandardBaseView implements CameraScreenListener {
	 
	private static final int PHOTO_RESIZE_HEIGHT = 768;
	private static final int PHOTO_RESIZE_WIDTH = 1024;
	private static final int PREVIEW_THUMB_SIZE = 96;
	private Post post;
	private BasicEditField title;
	private HtmlTextField bodyTextBox;
	private BasicEditField tags;
	private LabelField wordCountField;
	private VerticalFieldManager rowImages;
	private TableLayoutManager mediaTableContainer;
	private ConnectionInProgressView connectionProgressView = null;
	private SendToBlogTask sendTask;
	
	private static final int PHOTO = 110;
	
	public QuickPhotoScreen(Post _post) {
	    	super(_resources.getString(WordPressResource.TITLE_POSTVIEW) , MainScreen.NO_VERTICAL_SCROLL | Manager.NO_HORIZONTAL_SCROLL);
			this.post = _post;
			
	        //row title
	    	BorderedFieldManager rowTitle = new BorderedFieldManager(Manager.NO_HORIZONTAL_SCROLL
	         		| Manager.NO_VERTICAL_SCROLL | BorderedFieldManager.BOTTOM_BORDER_NONE);
	    	LabelField lblTitle = GUIFactory.getLabel(_resources.getString(WordPressResource.LABEL_TITLE), Color.BLACK);
			title = new BasicEditField("", post.getTitle(), BasicEditField.DEFAULT_MAXCHARS, Field.EDITABLE);
	        rowTitle.add(lblTitle);
	        rowTitle.add(GUIFactory.createSepatorField());
	        rowTitle.add(title);
	        add(rowTitle);
	        
	        //The Box that shows tags
	        BorderedFieldManager rowTags = new BorderedFieldManager(Manager.NO_HORIZONTAL_SCROLL
	        		| Manager.NO_VERTICAL_SCROLL | BorderedFieldManager.BOTTOM_BORDER_NONE);
	        LabelField lblTags = GUIFactory.getLabel( _resources.getString(WordPressResource.LABEL_POST_TAGS), Color.BLACK);
			tags = new BasicEditField("", post.getTags(), BasicEditField.DEFAULT_MAXCHARS, Field.EDITABLE);
			rowTags.add(lblTags);
			rowTags.add(GUIFactory.createSepatorField());
			rowTags.add(tags);
			add(rowTags);
			
			
			//Media attached to the post
	        rowImages = new VerticalFieldManager( Manager.NO_HORIZONTAL_SCROLL
	        		| Manager.NO_VERTICAL_SCROLL | Manager.USE_ALL_WIDTH );
	        
	        rowImages.setBackground(BackgroundFactory.createSolidBackground(Color.WHITE));
	        rowImages.setMargin(5,5,5,5);
	        rowImages.setBorder(
	        		BorderFactory.createSimpleBorder( 
	        				new XYEdges(1, 1, 1, 1), 
	        				new XYEdges(Color.DARKGRAY, Color.DARKGRAY, Color.DARKGRAY, Color.DARKGRAY),
	        				Border.STYLE_SOLID
	        		) 
	        );	        
			mediaTableContainer = new TableLayoutManager(
					new int[] {
							TableLayoutManager.SPLIT_REMAINING_WIDTH,
							TableLayoutManager.SPLIT_REMAINING_WIDTH,
							TableLayoutManager.SPLIT_REMAINING_WIDTH,
					}, 
					new int[] { 2, 2, 2 }, //not used in this configuration
					10,
					Manager.USE_ALL_WIDTH
			);
			mediaTableContainer.setMargin(5, 5, 5, 5);
			rowImages.add(mediaTableContainer);
			JustifiedEvenlySpacedHorizontalFieldManager toolbarOne = new JustifiedEvenlySpacedHorizontalFieldManager();	
			toolbarOne.setMargin(5,0,5,0);
			BaseButtonField buttonZoomIn= GUIFactory.createButton(_resources.getString(WordPressResource.LABEL_PHOTO_TAKE_FROM_CAMERA), ButtonField.CONSUME_CLICK | ButtonField.USE_ALL_WIDTH | DrawStyle.ELLIPSIS);
			buttonZoomIn.setChangeListener(
					new FieldChangeListener() {
						public void fieldChanged(Field field, int context) {
							showCameraScreen();
						}
					}
			);
			buttonZoomIn.setMargin(0,5,0,5);
			
			BaseButtonField buttonZoomOut = GUIFactory.createButton(_resources.getString(WordPressResource.LABEL_PHOTO_ADD_FROM_LIB), ButtonField.CONSUME_CLICK | ButtonField.USE_ALL_WIDTH | DrawStyle.ELLIPSIS);
			buttonZoomOut.setChangeListener(
					new FieldChangeListener() {
						public void fieldChanged(Field field, int context) {
							String imageExtensions[] = MultimediaUtils.getSupportedWordPressImageFormat();
							final RimFileBrowser oldFileBrowser = new RimFileBrowser(imageExtensions, false);
							oldFileBrowser.setListener(new QuickPhotoFileBrowserListener(PHOTO));
							synchronized(Application.getEventLock()) {
								UiApplication.getUiApplication().pushScreen(oldFileBrowser);	
							}
						}
					}
			);
			buttonZoomOut.setMargin(0,5,0,5);
			toolbarOne.add(buttonZoomIn);
			toolbarOne.add(buttonZoomOut);
			rowImages.add(toolbarOne);
			add(rowImages);
			
			//row content 
	    	BorderedFieldManager rowContent = new BorderedFieldManager(Manager.NO_HORIZONTAL_SCROLL	| Manager.NO_VERTICAL_SCROLL);
	  		MarkupToolBarTextFieldMediator mediator = new MarkupToolBarTextFieldMediator();
	  		
	  		HorizontalFieldManager headerContent = new HorizontalFieldManager(Manager.NO_HORIZONTAL_SCROLL | Manager.USE_ALL_WIDTH);
	  		LabelField lblPostContent = GUIFactory.getLabel(_resources.getString(WordPressResource.LABEL_POST_CONTENT), Color.BLACK, DrawStyle.ELLIPSIS);
	  		int fntHeight = Font.getDefault().getHeight();
	  		Font fnt = Font.getDefault().derive(Font.PLAIN, fntHeight-4, Ui.UNITS_px);
	  		wordCountField = new LabelField("0", Field.USE_ALL_WIDTH | Field.FIELD_HCENTER | DrawStyle.RIGHT);
	  		wordCountField.setFont(fnt);
	  		mediator.setWcField(wordCountField);

	  		headerContent.add(lblPostContent);
			headerContent.add(wordCountField);
			rowContent.add(headerContent);
			rowContent.add(GUIFactory.createSepatorField());
			
			bodyTextBox = new HtmlTextField("", mediator);
			bodyTextBox.setMargin(5,0,5,0);//leave some spaces on the top & bottom
			mediator.setTextField(bodyTextBox);
			rowContent.add(bodyTextBox);
			rowContent.add(GUIFactory.createSepatorField());
			
			MarkupToolBar markupToolBar = new MarkupToolBar(mediator);
			mediator.setTb(markupToolBar);
			markupToolBar.attachTo(rowContent);
			add(rowContent);
	        add(new LabelField("", Field.NON_FOCUSABLE)); //space after content
	        
	        showCameraScreen();
	        MainController.getIstance().bumpScreenViewStats("com/wordpress/quickphoto/QuickPhotoScreen", "Quick Main Screen", "", null, "");
	 }

	private void showCameraScreen() {
        UiApplication.getUiApplication().invokeLater(new Runnable()
        {
        	public void run()
        	{  
        		CameraScreen screen = new CameraScreen();
        		screen.setListener(QuickPhotoScreen.this);
        		//define the screen transition
        		UiEngineInstance engine = Ui.getUiEngineInstance();
        		TransitionContext transitionContextIn;
        		transitionContextIn = new TransitionContext(TransitionContext.TRANSITION_SLIDE);
        		transitionContextIn.setIntAttribute(TransitionContext.ATTR_DURATION, 500);
        		transitionContextIn.setIntAttribute(TransitionContext.ATTR_DIRECTION, TransitionContext.DIRECTION_LEFT);   
        		engine.setTransition(null, screen, UiEngineInstance.TRIGGER_PUSH, transitionContextIn);
        		UiApplication.getUiApplication().pushScreen( screen );  
        	}
        });
	}
	
	private void removeBitmapField(PreviewBitmap thumb){
		mediaTableContainer.delete(thumb);
		Vector mediaObjects = post.getMediaObjects();
		mediaObjects.removeElement(thumb.getMediaObj());
		mediaTableContainer.invalidate();
		rowImages.invalidate();
	}
	
    /**
     * @see Screen#makeMenu(Menu, int)
     */
    protected void makeMenu(Menu menu, int instance)
    {
        super.makeMenu(menu, instance);
        if( this.isDirty() && instance != Menu.INSTANCE_CONTEXT ) {
        	menu.add( _submitPostItem );
        	menu.add( _saveDraftPostItem );
        }
        
        if ( instance == Menu.INSTANCE_CONTEXT ) {
        	if (post.getMediaObjects() != null &&  post.getMediaObjects().size() > 0 &&  this.getLeafFieldWithFocus() instanceof PreviewBitmap ) {
        		final PreviewBitmap focusedBitmap =  (PreviewBitmap) this.getLeafFieldWithFocus();
        		menu.add( new MenuItem( _resources, WordPressResource.MENUITEM_MEDIA_REMOVE, 130, 10) {
    	        	public void run() {
    	        		removeBitmapField(focusedBitmap);
    	        	}
    	        });
        	}
        }
    }
	
	public boolean isDirty() {
		if ( title.isDirty() || tags.isDirty() || bodyTextBox.isDirty() || mediaTableContainer.isDirty() || ( post.getMediaObjects() != null && post.getMediaObjects().size() > 0 ) )
			return true;
		else 
			return false;
	}
		
	public void save() {
		try {
			updateModel();
			DraftDAO.storePost(post, -1);
		} catch (Exception e) {
			MainController.getIstance().displayError(e,"Error while saving draft post!");
		}
	}
	
	private void updateModel() {
		post.setTitle( title.getText() );
		post.setTags( tags.getText() );
		
		String newContent= bodyTextBox.getText();
		String tagMore = null;
		if(newContent.indexOf("<!--more-->") > -1) {
			tagMore = "<!--more-->";
		} else if(newContent.indexOf("<!--More-->") > -1) {
			tagMore = "<!--More-->";
		}else if(newContent.indexOf("<!--MORE-->") > -1) {
			tagMore = "<!--MORE-->";
		}
		//check for the more tag
		if( tagMore != null ) {
			Log.trace("found Extended body");
			String[] split = StringUtils.split(newContent, tagMore);
			post.setBody(split[0]);
			String extended = "";
			//if there are > 1 tags more
			for (int i = 1; i < split.length; i++) {
				extended+=split[i];
			}
			post.setExtendedBody(extended);
		} 
		else //no tag more
			post.setBody(newContent);
	}
		
	public BaseController getController() {
		// TODO Auto-generated method stub
		return null;
	}

	public void mediaItemTaken(final String filePath) {
		this.setDirty(true);
		PhotoEntry mediaObj = new PhotoEntry();
		mediaObj.setFilePath(filePath);
		byte[] readFile = null;;
		try {
			readFile = JSR75FileSystem.readFile(filePath);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		if( readFile == null ) return;
		EncodedImage img = EncodedImage.createEncodedImage(readFile, 0, -1);
		//find the photo size
		int scale = ImageUtils.findBestImgScale(img.getWidth(), img.getHeight(), PREVIEW_THUMB_SIZE, PREVIEW_THUMB_SIZE);
		if(scale > 1)
			img.setScale(scale); //set the scale
		final Bitmap bitmapRescale = img.getBitmap();
		final BitmapField photoBitmapField = new PreviewBitmap(mediaObj, bitmapRescale, BitmapField.FOCUSABLE | BitmapField.FIELD_HCENTER | Manager.FIELD_VCENTER);
		photoBitmapField.setSpace(5, 5);
		Vector mediaObjects = post.getMediaObjects();
		UiApplication.getUiApplication().invokeLater(new Runnable() {
			public void run() {
				mediaTableContainer.add(photoBitmapField);
				mediaTableContainer.invalidate();
				rowImages.invalidate();
			}
		});
		mediaObjects.addElement(mediaObj);
		mediaObj.setResizeWidth(new Integer(PHOTO_RESIZE_WIDTH));
		mediaObj.setResizeHeight(new Integer(PHOTO_RESIZE_HEIGHT));	
	}
	
	
    //send post to blog
    private MenuItem _submitPostItem = new MenuItem(_resources, WordPressResource.MENUITEM_PUBLISH, 1000, 100) {

		public void run() {
    		try {
    			
    			updateModel();
    			post.setStatus("publish");
    			boolean publish= true;
    			//adding post connection
    			BlogConn connection = new NewPostConn (post.getBlog().getXmlRpcUrl(), 
    					post.getBlog().getUsername(),post.getBlog().getPassword(), post, publish);

    			int draftFolder = DraftDAO.storePost(post, -1);
    			connectionProgressView = new ConnectionInProgressView(_resources.getString(WordPressResource.CONNECTION_SENDING));
    			sendTask = new SendToBlogTask(post, draftFolder, connection);
    			sendTask.setProgressListener(new QuickPhotoPublishTaskListener());
    			//push into the Runner
    			WordPressCore.getInstance().getTasksRunner().enqueue(sendTask);
    			
    			connectionProgressView.setDialogClosedListener(
    					new DialogClosedListener() {
    						public void dialogClosed(Dialog dialog, int choice) {
    							if(choice == Dialog.CANCEL) {
    								Log.trace("Chiusura della conn dialog tramite cancel");
    								sendTask.stop();
    							}
    						}
    					}
    			);
 			 	connectionProgressView.show();
    		} catch (Exception e) {
    			MainController.getIstance().displayError(e, _resources.getString(WordPressResource.ERROR_WHILE_SAVING_POST));
    		}
    	}
    };
	
    private MenuItem _saveDraftPostItem = new MenuItem(_resources, WordPressResource.MENUITEM_SAVEDRAFT, 1100, 100) {
		public void run() {
    		try {
    			updateModel();
    			post.setStatus("publish");
    			DraftDAO.storePost(post, -1);
    			close();
    		} catch (Exception e) {
    			MainController.getIstance().displayError(e, _resources.getString(WordPressResource.ERROR_WHILE_SAVING_POST));
    		}
    	}
    };

    private class QuickPhotoFileBrowserListener implements RimFileBrowserListener {
    	//Type is not used at the moment. It will be used later when we add Videos
    	//int type = -1; 
    	public QuickPhotoFileBrowserListener(int multimediaFileType) {
    		//type = multimediaFileType;			
    	}
    	public void selectionDone(String theFile) {
    		Log.trace("[OldFileBrowserListener.selectionDone]");
    		Log.trace("filename: " + theFile);
    		if (theFile == null){

    		} else {
    			if(!theFile.startsWith("file://")) {
    				theFile = "file://"+ theFile;
    			} 
    			mediaItemTaken(theFile);	
    		}	
    	}
    }
    
    
	//listener on send post to blog
	private class QuickPhotoPublishTaskListener implements TaskProgressListener {

		public void taskComplete(Object obj) {			

			//task  stopped previous
			if (sendTask.isStopped()) 
				return;  
			
			if(connectionProgressView != null && connectionProgressView.isDisplayed()) {
				UiApplication.getUiApplication().invokeLater(new Runnable() {
					public void run() {
						UiApplication.getUiApplication().popScreen(connectionProgressView);
					}
				});
			}
			
			if (!sendTask.isError()){
				UiApplication.getUiApplication().invokeLater(new Runnable() {
					public void run() {
						close();
					}
				});
			}
			else {
				MainController.getIstance().displayError(sendTask.getErrorMsg());				
			}
		}
		
		//listener for the adding blogs task
		public void taskUpdate(Object obj) {
		
		}	
	}
    
	private class PreviewBitmap extends BitmapField {

		private final PhotoEntry mediaObj;

		public PhotoEntry getMediaObj() {
			return mediaObj;
		}

		public PreviewBitmap (PhotoEntry mediaObj, Bitmap bitmap, long style){
			super(bitmap, style);
			this.mediaObj = mediaObj;
		}

		private void openImage() {
			byte[] readFile = null;
			try {
				readFile = JSR75FileSystem.readFile(mediaObj.getFilePath());
			} catch (IOException e) {
				MainController.getIstance().displayError(e, "Sorry, we can't read the image data from disk.");
			}
			if( readFile == null ) return;
			final EncodedImage img = EncodedImage.createEncodedImage(readFile, 0, -1);
			UiApplication.getUiApplication().invokeLater(new Runnable() {
				public void run() {
			          ZoomScreen zoomableImg = new QuickPhotoZoomScreen(img);
			          UiApplication.getUiApplication().pushScreen(zoomableImg);
				}
			});
		}
		
	    protected void makeContextMenu(ContextMenu contextMenu) {
	       
	    	contextMenu.addItem( new MenuItem( _resources, WordPressResource.MENUITEM_MEDIA_REMOVE, 130, 10) {
	        	public void run() {
	        		removeBitmapField(PreviewBitmap.this);
	        	}
	        });
	    	
	    	super.makeContextMenu(contextMenu);
	    }
	    
		/**
		 * Overrides default implementation.  Performs default action if the 
		 * 4ways trackpad was clicked; otherwise, the default action occurs.
		 * 
		 * @see net.rim.device.api.ui.Screen#navigationClick(int,int)
		 */
		protected boolean navigationClick(int status, int time) {
			Log.trace(">>> navigationClick");

			if ((status & KeypadListener.STATUS_TRACKWHEEL) == KeypadListener.STATUS_TRACKWHEEL) {
				Log.trace("Input came from the trackwheel");
				// Input came from the trackwheel
				return super.navigationClick(status, time);

			} else if ((status & KeypadListener.STATUS_FOUR_WAY) == KeypadListener.STATUS_FOUR_WAY) {
				Log.trace("Input came from a four way navigation input device");
				openImage();
				return true;
			}
			return super.navigationClick(status, time);
		}

		//Allow the space bar to toggle the status of the selected row.
		protected boolean keyChar(char key, int status, int time)
		{
			//If the spacebar was pressed...
			if (key == Characters.SPACE || key == Characters.ENTER)
			{
				openImage();
				return true;
			}
			return false;
		}
		
		protected boolean touchEvent(TouchEvent message) {
			//Log.trace(">>> touchEvent");
			boolean isOutOfBounds = false;
			int x = message.getX(1);
			int y = message.getY(1);
			// Check to ensure point is within this field
			if(x < 0 || y < 0 || x > this.getExtent().width || y > this.getExtent().height) {
				isOutOfBounds = true;
			}
			if (isOutOfBounds) return false;

			//DOWN, UP, CLICK, UNCLICK, MOVE, and CANCEL. An additional event, GESTURE
			int eventCode = message.getEvent();
			if(eventCode == TouchEvent.CLICK) {
				//Log.trace("TouchEvent.CLICK");
				openImage();
				return true;
			}else if(eventCode == TouchEvent.DOWN) {
				//Log.trace("TouchEvent.CLICK");
			} else if(eventCode == TouchEvent.UP) {
				//Log.trace("TouchEvent.UP");
			} else if(eventCode == TouchEvent.UNCLICK) {
				//Log.trace("TouchEvent.UNCLICK");
				//return true; //consume the event: avoid context menu!!
			} else if(eventCode == TouchEvent.CANCEL) {
				//Log.trace("TouchEvent.CANCEL");
			}

			//return false; 
			return super.touchEvent(message);
		}			
	}

	//ZoomScreen that calls close() on itself when the zoom level is nearToFit and the user hit back.
	private class QuickPhotoZoomScreen extends ZoomScreen {
		public QuickPhotoZoomScreen(EncodedImage encodedImage){
			super(encodedImage);
			//#ifdef BlackBerrySDK7.0.0
			// Initialize the zoom screen to be zoomed all the way out
			setViewableArea(0, 0, 0);
			//#endif    
		}
		/**
		 * @see ZoomScreen#zoomedOutNearToFit()
		 */ 
		public void zoomedOutNearToFit()
		{            
			close();
		}
	}
}
