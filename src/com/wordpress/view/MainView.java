//#preprocess
package com.wordpress.view;

import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;

import net.rim.blackberry.api.browser.URLEncodedPostData;
import net.rim.device.api.system.ApplicationDescriptor;
import net.rim.device.api.system.Bitmap;
import net.rim.device.api.system.Characters;
import net.rim.device.api.system.CodeModuleGroup;
import net.rim.device.api.system.CodeModuleGroupManager;
import net.rim.device.api.system.Display;
import net.rim.device.api.system.EncodedImage;
import net.rim.device.api.system.GIFEncodedImage;
import net.rim.device.api.system.KeypadListener;
import net.rim.device.api.ui.Color;
import net.rim.device.api.ui.DrawStyle;
import net.rim.device.api.ui.Field;
import net.rim.device.api.ui.FieldChangeListener;
import net.rim.device.api.ui.Font;
import net.rim.device.api.ui.Graphics;
import net.rim.device.api.ui.Manager;
import net.rim.device.api.ui.MenuItem;
import net.rim.device.api.ui.UiApplication;
import net.rim.device.api.ui.XYRect;
import net.rim.device.api.ui.component.BitmapField;
import net.rim.device.api.ui.component.Dialog;
import net.rim.device.api.ui.component.LabelField;
import net.rim.device.api.ui.component.Menu;
import net.rim.device.api.ui.container.MainScreen;

import com.wordpress.bb.WordPressCore;
import com.wordpress.bb.WordPressInfo;
import com.wordpress.bb.WordPressResource;
import com.wordpress.controller.AccountsController;
import com.wordpress.controller.BaseController;
import com.wordpress.controller.FrontController;
import com.wordpress.controller.MainController;
import com.wordpress.controller.MediaLibrariesController;
import com.wordpress.controller.PagesController;
import com.wordpress.controller.PostsController;
import com.wordpress.controller.RecentCommentsController;
import com.wordpress.controller.StatsController;
import com.wordpress.io.BlogDAO;
import com.wordpress.io.CommentsDAO;
import com.wordpress.model.Blog;
import com.wordpress.model.BlogInfo;
import com.wordpress.model.Preferences;
import com.wordpress.task.StopConnTask;
import com.wordpress.utils.DataCollector;
import com.wordpress.utils.StringUtils;
import com.wordpress.utils.Tools;
import com.wordpress.utils.log.Log;
import com.wordpress.utils.observer.Observable;
import com.wordpress.utils.observer.Observer;
import com.wordpress.view.component.AnimatedGIFField;
import com.wordpress.view.component.BlogSelectorField;
import com.wordpress.view.component.SelectorPopupScreen;
import com.wordpress.view.component.WelcomeField;
import com.wordpress.view.container.TableLayoutManager;
import com.wordpress.view.dialog.ConnectionInProgressView;

//#ifdef VER_4.7.0 | BlackBerrySDK5.0.0 | BlackBerrySDK6.0.0 | BlackBerrySDK7.0.0
import net.rim.device.api.ui.TouchGesture;
import net.rim.device.api.ui.TouchEvent;
//#endif

//#ifdef BlackBerrySDK7.0.0
import com.wordpress.view.reader.WPCOMReaderListView;
//#endif

import com.wordpress.xmlrpc.BlogConnResponse;
import com.wordpress.xmlrpc.BlogUpdateConn;

public class MainView extends StandardBaseView {
	
    private MainController mainController = null;
    private BlogInfo currentBlog = null;
    private WelcomeField wft;
    
    private TableLayoutManager	blogSelectorRow;
    private BitmapField blogIconField;
    private BlogSelectorField blogSelectorField;
    
    private TableLayoutManager actionsTable;
    final int actionsTableMargin = 10; //margin outside the table
    final int actionsTableItemHorizontalPadding = 10; // Horizontal padding between items
    final int actionsTableItemVerticalPadding = 10; // Vertical padding between items
    
	//Deafult icons used to show the status of the blog
	private Bitmap imgImportant = Bitmap.getBitmapResource("important.png");
	private Bitmap imgQueue = Bitmap.getBitmapResource("enqueued.png");
	private Bitmap wp_blue = Bitmap.getBitmapResource("wp_blue-m.png");
	private Bitmap wp_grey = Bitmap.getBitmapResource("wp_grey-m.png");
	private Bitmap pendingActivation = Bitmap.getBitmapResource("pending_activation.png"); //not yet used

	private final int mnuPosts = 100;
	private final int mnuPages = 110;
	private final int mnuComments = 120;
	private final int mnuMedia = 130;
	private final int mnuStats = 140;
	private final int mnuOptions = 150;
	private final int mnuRefresh = 160;
	private final int mnuDashboard = 170;
	private final int mnuReader = 180;

	public MainView(MainController mainController) {
		super( "WordPress", MainScreen.NO_VERTICAL_SCROLL | Manager.NO_HORIZONTAL_SCROLL | USE_ALL_HEIGHT);
		this.mainController = mainController;
     
		BlogInfo[] blogCaricati = mainController.getApplicationBlogs();
		if( blogCaricati.length > 0 ) {
			createTableAndSelector ( blogCaricati );
		} else {
			currentBlog = null;
			wft = new WelcomeField();
			wft.setToolbarHeight(titleField.getPreferredHeight());
			add(wft);
		}
        addMenuItem(_feedbackItem);
        addMenuItem(_bugReportItem);
        addMenuItem(_aboutItem);
		addMenuItem(_addBlogItem);
		addMenuItem(_setupItem);
		addMenuItem(_accountItem);
		addMenuItem(_updateItem);
		
		mainController.bumpScreenViewStats("com/wordpress/view/MainView", "MainView Screen", "", null, "");
	}
	
	private void createTableAndSelector( BlogInfo[] blogCaricati ) {
		String choices[] = new String[ blogCaricati.length ];
		for (int i=0; i< blogCaricati.length; i++) {
			BlogInfo currentRow = (BlogInfo) blogCaricati[i];
			String blogName = currentRow.getName();
			choices[i]= blogName;
		}
		int iSetTo = 0;
		currentBlog = blogCaricati[ iSetTo ];
		
		blogSelectorRow = new TableLayoutManager(
				new int[] {
						TableLayoutManager.USE_PREFERRED_SIZE,
						TableLayoutManager.SPLIT_REMAINING_WIDTH
				}, 
				new int[] { 2, 2 },
				10,
				Manager.USE_ALL_WIDTH | Manager.FIELD_HCENTER
		);
		
		blogIconField = createBlogIconField( blogCaricati[iSetTo] );
		blogSelectorRow.add( blogIconField );
		blogSelectorField = new BlogSelectorField( choices, iSetTo, FOCUSABLE | USE_ALL_WIDTH );
		blogSelectorField.setFieldMaxHeight( getBlogIconSize() ); //set the field with the same height of the icon
		blogSelectorField.setChangeListener(new BlogSelectorChangeListener());
		blogSelectorRow.add( blogSelectorField );
		blogSelectorRow.setMargin(5, 5, 0, 5);
		
		//XYEdges edgesFour = new XYEdges(4, 4, 4, 4);
		//blogSelectorRow.setBorder( BorderFactory.createRoundedBorder(edgesFour) );
		
		add( blogSelectorRow );
		
		actionsTable = new TableLayoutManager(
				new int[] {
						TableLayoutManager.SPLIT_REMAINING_WIDTH,
						TableLayoutManager.SPLIT_REMAINING_WIDTH,
						TableLayoutManager.SPLIT_REMAINING_WIDTH,
				}, 
				new int[] { 2, 2, 2 }, //not used in this configuration
				actionsTableItemHorizontalPadding,
				Manager.USE_ALL_WIDTH
		);

		actionsTable.add( new ActionTableItem( mnuPosts, getItemLabel(mnuPosts), mnuPosts ) );
		actionsTable.add( new ActionTableItem( mnuPages, getItemLabel(mnuPages), mnuPages ) );
		actionsTable.add( new ActionTableItem( mnuComments, getItemLabel(mnuComments), mnuComments ) );
		
		actionsTable.add( new ActionTableItemNullField() );
		actionsTable.add( new ActionTableItemNullField() );
		actionsTable.add( new ActionTableItemNullField() );
		
		actionsTable.add( new ActionTableItem( mnuStats,  getItemLabel(mnuStats), mnuStats ) );
		actionsTable.add( new ActionTableItem( mnuOptions, getItemLabel(mnuOptions), mnuOptions ) );
		actionsTable.add( new ActionTableItem( mnuMedia,  getItemLabel(mnuMedia), mnuMedia ) );
		
		actionsTable.add( new ActionTableItemNullField() );
		actionsTable.add( new ActionTableItemNullField() );
		actionsTable.add( new ActionTableItemNullField() );
		
		actionsTable.add( new ActionTableItem( mnuRefresh, getItemLabel(mnuRefresh), mnuRefresh ) );
		actionsTable.add( new ActionTableItem( mnuDashboard, getItemLabel(mnuDashboard), mnuDashboard ) );
		
		//#ifdef BlackBerrySDK7.0.0
		if ( currentBlog != null && currentBlog.isWPCOMBlog() )
			actionsTable.add( new ActionTableItem( mnuReader, getItemLabel(mnuReader), mnuReader ) );
		//#endif
		
		add( actionsTable );
	}
	
	/**
	 * 
	 * This method is called when the app asks the blavatar to the Gravatar service
	 * 
	 * Pearl 8220 - 240 x 320 pixels
	 * Curve 8300 Series, 8800 Series, 8700 Series - 320 x 240 pixels
	 * Curve 8350i - 320 x 240 pixels
	 * Curve 8900 - 480 x 360 pixels
	 * Bold  9000 Series - 480 x 320 pixels
	 * Bold  9900 Series - 640 x 480
	 * Tour  9600 Series - 480 x 360 pixels
	 * Storm 9500 Series - portrait view: 360 x 480 pixels,  landscape view: 480 x 360 pixels
	 * Torch 9800 360 x 480 (portrait) when held vertically.
	 * Torch 9810 - 640 x 480
	 * Torch2 9850/9860  - 800 x 480
	 * 
	 */
	public static int getBlogIconSize() {
		 int width = Display.getWidth(); 
		 int height = Display.getHeight();
		 
		 if( ( width == 240 && height == 320 ) || ( width == 320 && height == 240 ) ) {
			 return 48;
		 } else if( ( width == 480 && height == 320 ) ||  ( width == 320 && height == 480 ) ) { 
			 return 64;
		 } else if( ( width == 480 && height == 360 ) || ( width == 360 && height == 480) ) {
			 return 64;
		 } else if( ( width == 480 && height == 640 ) || ( width == 640 && height == 480 ) ) {
			 return 72;		 
		 } else if( ( width == 800 && height == 480 ) || ( width == 800 && height == 480 ) ){
			 return 72;
		 } else {
			 return 48;
		 }
	}
	
	private String getItemLabel(int type) {
		switch (type) {
		case (mnuPosts):
			return _resources.getString(WordPressResource.BUTTON_POSTS);
		case (mnuPages):
			return _resources.getString(WordPressResource.BUTTON_PAGES);
		case (mnuComments):
			return _resources.getString(WordPressResource.BUTTON_COMMENTS);
		case (mnuMedia):
			return _resources.getString(WordPressResource.BUTTON_MEDIA);
		case (mnuStats):
			return _resources.getString(WordPressResource.BUTTON_STATS);
		case (mnuOptions):
			return _resources.getString(WordPressResource.BUTTON_OPTIONS);
		case (mnuRefresh):
			return _resources.getString(WordPressResource.BUTTON_REFRESH_BLOG);
		case (mnuDashboard):
			return _resources.getString(WordPressResource.MENUITEM_DASHBOARD);
		case (mnuReader):
			return _resources.getString(WordPressResource.MENUITEM_READER);
		default:
			return null;
		}
	}
	
	 private boolean tableOrMenuItemSelected(int selection) {
		 if ( currentBlog == null ) return true;
		 
		if (currentBlog.getState() == BlogInfo.STATE_LOADING || currentBlog.getState() == BlogInfo.STATE_ADDED_TO_QUEUE) {
			mainController.displayMessage(_resources.getString(WordPressResource.MESSAGE_LOADING_BLOGS));
			return true;
		} 
		 
		switch (selection) {
		case mnuPosts:
			try {
				PostsController ctrl = new PostsController(BlogDAO.getBlog(currentBlog));
				ctrl.showView();
			} catch (Exception e) {
				mainController.displayError(e, "Cannot load the blog data");
			}
			break;
		 case mnuPages:
			 try {
				 PagesController ctrl= new PagesController(BlogDAO.getBlog(currentBlog));
				 ctrl.showView();
			 } catch (Exception e) {
				 mainController.displayError(e, "Cannot load the blog data");
			 }
			 break;
		 case mnuComments:
			 try {
				 RecentCommentsController ctrl=new RecentCommentsController(BlogDAO.getBlog(currentBlog));
				 ctrl.showView();
			 } catch (Exception e) {
				 mainController.displayError(e, "Cannot load the blog data");
			 }
			 break;
		 case mnuStats:
			 try {
					StatsController ctrl = new StatsController(BlogDAO.getBlog(currentBlog));
					ctrl.showView();
			 } catch (Exception e) {
				 mainController.displayError(e, "Cannot load the blog data");
			 }
			 break;
		 case mnuOptions:
			 try {
				 FrontController.getIstance().showBlogOptions(BlogDAO.getBlog(currentBlog));
			 } catch (Exception e) {
				 mainController.displayError(e, "Cannot load the blog data");
			 }
			 break;
		 case mnuRefresh:
			 try {
				 final BlogUpdateConn connection = new BlogUpdateConn (BlogDAO.getBlog(currentBlog));       
				 ConnectionInProgressView connectionProgressView = new ConnectionInProgressView(
						 _resources.getString(WordPressResource.CONNECTION_INPROGRESS));
				 connection.addObserver(new RefreshBlogCallBack(connectionProgressView)); 
				 connection.startConnWork(); //starts connection
				 int choice = connectionProgressView.doModal();
				 if(choice == Dialog.CANCEL) {
					 WordPressCore.getInstance().getTasksRunner().enqueue(new StopConnTask(connection));
				 }
			 } catch (Exception e) {
				 mainController.displayError(e, "Cannot load the blog data");
			 }
			 break;
		 case mnuDashboard:
				String user = currentBlog.getUsername();
		        String pass = currentBlog.getPassword();
		        
		        String cleanURL = currentBlog.getXmlRpcUrl().endsWith("/") ? currentBlog.getXmlRpcUrl() :  currentBlog.getXmlRpcUrl()+"/";
		        String loginURL = StringUtils.replaceLast(cleanURL,"/xmlrpc.php/", "/wp-login.php");
		        String dashboardURL = StringUtils.replaceLast(cleanURL,"/xmlrpc.php/", "/wp-admin/");
		        
				//create the link
				URLEncodedPostData urlEncoder = new URLEncodedPostData("UTF-8", false);

				urlEncoder.append("redirect_to", dashboardURL);
				urlEncoder.append("log", user);
				urlEncoder.append("pwd", pass);
				
				Tools.openNativeBrowser(loginURL, "WordPress for BlackBerry App", null, urlEncoder);
			 break;
		 case mnuMedia:
			 try {
				 MediaLibrariesController ctrl = new MediaLibrariesController(BlogDAO.getBlog(currentBlog));
				 ctrl.showView();
			 } catch (Exception e) {
				 mainController.displayError(e, "Cannot load the blog data");
			 }
			 break;
			 
		//#ifdef BlackBerrySDK7.0.0
		 case mnuReader:
	        	//load the first WP.COM available within the app
	        	Hashtable applicationAccounts = MainController.getIstance().getApplicationAccounts();
	        	Hashtable currentAccount = null;
	        	Enumeration k = applicationAccounts.keys();
	        	 if (k.hasMoreElements()) {
	    			 String key = (String) k.nextElement();
	    			 currentAccount = (Hashtable)applicationAccounts.get(key);
	        	 }
	        	String user2 = (String)currentAccount.get("username");
		        String pass2 = (String)currentAccount.get("passwd");
				WPCOMReaderListView _browserScreen = new WPCOMReaderListView(user2, pass2);
				UiApplication.getUiApplication().pushScreen(_browserScreen); 
			 break;
		//#endif
			 
		 default:
			 break;
		 }
		 return true;
	 }
	
	 private class BlogSelectorChangeListener implements FieldChangeListener {

		 public void fieldChanged(Field field, int context) {
			 if ( context == 0 ) {
				 final SelectorPopupScreen selScr = new SelectorPopupScreen( _resources.getString(WordPressResource.TITLE_BLOG_SELECTOR_POPUP), blogSelectorField.getChoices());
				 UiApplication.getUiApplication().invokeAndWait(new Runnable() {
					 public void run() {
						 selScr.pickItem();
					 }
				 });
				 int selectedIndex = selScr.getSelectedItem();
				 if ( selectedIndex != -1 ) {
					 currentBlog = mainController.getApplicationBlogs()[selectedIndex];
					 updateBlogIconField(currentBlog);
					 blogSelectorField.setSelectedIndex(selectedIndex);
					 actionsTable.invalidate();
				 }
			 }
		 }
	 }
	
	public void refreshMainView() {
		synchronized (this) {
			BlogInfo[] blogCaricati = mainController.getApplicationBlogs();
			if( blogCaricati.length > 0 ) {
				if( wft != null ) {
					delete(wft);
					createTableAndSelector(blogCaricati);
					wft = null;
				} else {
					if ( currentBlog == null )
						currentBlog = blogCaricati[0];
					final String choices[] = new String[ blogCaricati.length ];
					int iSetTo = 0;
					for (int i=0; i< blogCaricati.length; i++) {
						BlogInfo currentRow = (BlogInfo) blogCaricati[i];
						String blogName = currentRow.getName();
						choices[i]= blogName;
						if ( currentBlog != null && currentBlog.equals(currentRow) )
							iSetTo = i;
					}
					
					final int sel = iSetTo;
					UiApplication.getUiApplication().invokeLater(new Runnable() {
						public void run() {
							blogSelectorField.setChoices(choices);
							blogSelectorField.setSelectedIndex(sel);
							updateBlogIconField(currentBlog);
							actionsTable.invalidate();
						}
					});
				}
			} else {
				currentBlog = null;
				if( wft != null ) {
					//welcome screen already on the stack
				} else {
					wft = new WelcomeField();
					wft.setToolbarHeight(titleField.getPreferredHeight());
					if ( UiApplication.getUiApplication().isEventThread() ) {
						if ( blogSelectorRow.getManager() != null ) {
							delete( blogSelectorRow );
							delete( actionsTable );
						}
						add(wft);
					} else {
						UiApplication.getUiApplication().invokeLater(new Runnable() {
							public void run() {
								if ( blogSelectorRow.getManager() != null ) {
									delete( blogSelectorRow );
									delete( actionsTable );
								}
								add(wft);
							}
						});
					}
				}
			}
		}
	}
	
	private void updateBlogIconField(BlogInfo blogToUpdate){
		BitmapField blogIconField2 = createBlogIconField( blogToUpdate );
		blogSelectorRow.replace(blogIconField, blogIconField2);
		blogIconField = blogIconField2;
		blogSelectorRow.invalidate();
	}
	
	 private BitmapField createBlogIconField( BlogInfo currentRow ){
		 int stato = currentRow.getState();
		 Bitmap icon = null;
		 if(stato == BlogInfo.STATE_PENDING_ACTIVATION) {
			 icon = pendingActivation;
		 } else if (stato == BlogInfo.STATE_LOADING) { 
			GIFEncodedImage _theImage= (GIFEncodedImage)EncodedImage.getEncodedImageResource("loading-gif.bin");
			return  new AnimatedGIFField(_theImage, Field.NON_FOCUSABLE | FIELD_HCENTER | FIELD_VCENTER );
		 } else if (stato == BlogInfo.STATE_ADDED_TO_QUEUE) {
			 icon = imgQueue; 
		 } else if (stato == BlogInfo.STATE_LOADED_WITH_ERROR ||  stato == BlogInfo.STATE_ERROR) {
			 icon = imgImportant;
		 } else if( stato == BlogInfo.STATE_LOADED ) {
			 if(currentRow.getBlogIcon() != null) {
				 try {
					 icon =  Bitmap.createBitmapFromPNG(currentRow.getBlogIcon(), 0, -1);
					// BitmapField  test = new BitmapField( icon, Field.NON_FOCUSABLE | FIELD_HCENTER | FIELD_VCENTER );
					// test.setBorder(BorderFactory.createRoundedBorder(new XYEdges(6,6,6,6)));
					// return test;
				 } catch (Exception e) {
					 Log.error("no valid shortcut ico found in the blog obj");
				 }
			 }
			 //still null there was an error during img generation process
			 if( icon == null) {
				 if(currentRow.isWPCOMBlog()) {
					 icon = wp_blue;
				 } else {
					 icon = wp_grey;
				 }
			 }
		 } 
		 return new BitmapField( icon, Field.NON_FOCUSABLE | FIELD_HCENTER | FIELD_VCENTER );
	 }
	 	 
	 protected void sublayout(int width, int height) {
		 if( actionsTable != null ) {
			 int availableHeight = Display.getHeight();
			 availableHeight -= titleField.getPreferredHeight();
			 availableHeight -= blogSelectorRow.getPreferredHeight();
			 int fieldsHeight = actionsTable.getPreferredHeight();

			 if (fieldsHeight < availableHeight) {
				 int topMargin = ( availableHeight - fieldsHeight ) / 2;
				 actionsTable.setMargin(topMargin, actionsTableMargin, actionsTableMargin , actionsTableMargin);
			 } else {
				 actionsTable.setMargin(actionsTableMargin, actionsTableMargin, actionsTableMargin , actionsTableMargin);
			 }

			 super.sublayout(width, height);
		 }
	 }
	 
	 //update the view of blog list entry
	 public synchronized void setBlogItemViewState(BlogInfo blogInfo) {
		 //if ( blogIconField == null) return;
		 if ( currentBlog != null && currentBlog.equals(blogInfo)) {
			 currentBlog = blogInfo;
			 final BitmapField blogIconField2 = createBlogIconField( blogInfo );
			 UiApplication.getUiApplication().invokeLater(new Runnable() {
				 public void run() {
					 blogSelectorRow.replace(blogIconField, blogIconField2);
					 blogIconField = blogIconField2;
					 blogSelectorRow.invalidate();
				 }
			 });
		 }
	 }

	 private MenuItem _showBlogPosts = new MenuItem( _resources, WordPressResource.BUTTON_POSTS, 1300, 900) {
		 public void run() {
			 if ( currentBlog == null ) return;
			 tableOrMenuItemSelected(mnuPosts);
		 }
	 };
	 private MenuItem _showBlogPages = new MenuItem( _resources, WordPressResource.BUTTON_PAGES, 1310, 900) {
		 public void run() {
			 if ( currentBlog == null ) return;
			 tableOrMenuItemSelected(mnuPages);
		 }
	 };
	 private MenuItem _showBlogComments = new MenuItem( _resources, WordPressResource.BUTTON_COMMENTS, 1320, 900) {
		 public void run() {
			 if ( currentBlog == null ) return;
			 tableOrMenuItemSelected(mnuStats);
		 }
	 };
	 private MenuItem _showBlogStats = new MenuItem( _resources, WordPressResource.BUTTON_STATS, 1330, 900) {
		 public void run() {
			 if ( currentBlog == null ) return; 
			 tableOrMenuItemSelected(mnuStats);
		 }
	 };
	 private MenuItem _showBlogOptions = new MenuItem( _resources, WordPressResource.BUTTON_OPTIONS, 1340, 900) {
		 public void run() {
			 if ( currentBlog == null ) return;
			 tableOrMenuItemSelected(mnuOptions);
		 }
	 };
	 private MenuItem _refreshBlog = new MenuItem( _resources, WordPressResource.BUTTON_REFRESH_BLOG, 1350, 900) {
		 public void run() {
			 if ( currentBlog == null ) return; 
			 tableOrMenuItemSelected(mnuRefresh);
		 }
	 };
	 private MenuItem _showBlogDashBoard = new MenuItem( _resources, WordPressResource.MENUITEM_DASHBOARD, 1360, 900) {
		 public void run() {
			 if ( currentBlog == null ) return; 
			 tableOrMenuItemSelected(mnuDashboard);
		 }
	 };
	 
	 //#ifdef BlackBerrySDK7.0.0
	 private MenuItem _mobileReaderMenuItem = new MenuItem( _resources, WordPressResource.MENUITEM_READER, 1370, 900) {
		 public void run() {
			 tableOrMenuItemSelected(mnuReader);
		 }
	 }; 
	 //#endif
	 
   
    //add blog menu item 
    private MenuItem _addBlogItem = new MenuItem( _resources, WordPressResource.MENUITEM_ADDBLOG, 100000, 1000) {
    	public void run() {
    		if(mainController.isLoadingBlogs()) {
    			mainController.displayMessage(_resources.getString(WordPressResource.MESSAGE_LOADING_BLOGS));
				return;
    		}
    		mainController.showWelcomeView();
    	}
    };
        
    private MenuItem _deleteBlogItem = new MenuItem( _resources, WordPressResource.MENUITEM_DELETE_BLOG, 100100, 1000) {
    	public void run() {
    		if ( currentBlog == null ) return;
    		if (currentBlog.getState() == BlogInfo.STATE_LOADING || currentBlog.getState() == BlogInfo.STATE_ADDED_TO_QUEUE) {
    			mainController.displayMessage(_resources.getString(WordPressResource.MESSAGE_LOADING_BLOGS));
    		} else {
    			mainController.deleteBlog(currentBlog);	       
    			currentBlog = null;
    			refreshMainView();
    		}
    	}
    };

    private MenuItem _notificationItem = new MenuItem( _resources, WordPressResource.MENUITEM_NOTIFICATIONS, 100200, 1000) {
        public void run() {
        	BlogInfo[] blogs = mainController.getApplicationBlogs();
        	FrontController.getIstance().showNotificationView(blogs);
        }
    };
   
    
    private MenuItem _setupItem = new MenuItem( _resources, WordPressResource.MENUITEM_SETUP, 100300, 1000) {
        public void run() {
        	FrontController.getIstance().showSetupView();
        }
    };
    
    private MenuItem _accountItem = new MenuItem( _resources, WordPressResource.MENUITEM_ACCOUNTS, 100400, 1000) {
        public void run() {
        	AccountsController ctrl = new AccountsController();
    		ctrl.showView();	
        }
    };

    private MenuItem _updateItem = new MenuItem( _resources, WordPressResource.MENUITEM_CHECKUPDATE, 100500, 1000) {
    	public void run() {

    		try {
    			DataCollector dtc = new DataCollector();
    			int numBlogs = 0;
    			numBlogs = mainController.getApplicationBlogs().length;
    			dtc.checkForUpdate(numBlogs); //start data gathering here
    		} catch (Exception e) {
    			mainController.displayError(e, "Error while checking for new versions.");
    		}
    	}
    };
    
    private MenuItem _feedbackItem = new MenuItem( _resources, WordPressResource.MENUITEM_FEEDBACK, 200200, 1000) {
        public void run() {
        	try {
        		 // Pull out the App World data from the CodeModuleGroup
                String myAppName = ApplicationDescriptor.currentApplicationDescriptor().getName();
                CodeModuleGroup group = CodeModuleGroupManager.load( myAppName );
                final String myContentId = group == null ? "" : group.getProperty( "RIM_APP_WORLD_ID" );

                //if App World data is null or empty string, put the id manually
                if(myContentId != null && !myContentId.trim().equalsIgnoreCase("")){
                	Tools.openAppWorld(myContentId);
                } else {
                	Tools.openAppWorld("5802"); //id of the App given by RIM
                }
                
			} catch (Exception e) {
				Log.error(e, "Problem invoking BlackBerry App World");
				mainController.displayError("Problem invoking BlackBerry App World");
			}
        }
    };
    
    private MenuItem _bugReportItem = new MenuItem( _resources, WordPressResource.MENUITEM_BUG_REPORT, 200300, 1000) {
    	public void run() {
    		int selection = -1;
    		String[] messages = _resources.getStringArray(WordPressResource.MESSAGES_ADD_BLOG);
    		String[] blogNames = new String[]{messages[1], messages[2]};
    		String title = _resources.getString(WordPressResource.MESSAGE_WORDPRESS_VERSION);
    		SelectorPopupScreen selScr = new SelectorPopupScreen(title, blogNames);
    		selScr.pickItem();
    		selection = selScr.getSelectedItem();
    		if(selection == 0) {
    			ContactSupportView view = new ContactSupportView(mainController, false);
    			UiApplication.getUiApplication().pushScreen(view);
    		} else if(selection == 1) {
    			ContactSupportView view = new ContactSupportView(mainController, true);
    			UiApplication.getUiApplication().pushScreen(view);
    		}
    	}
    };

    private MenuItem _aboutItem = new MenuItem( _resources, WordPressResource.MENUITEM_ABOUT, 200400, 1000) {
        public void run() {
        	FrontController.getIstance().showAboutView();
        }
    };
        
    /*
     * used when background on close is activated
     */
    private MenuItem _exitItem = new MenuItem( _resources, WordPressResource.MENUITEM_EXIT, 300000, 2000) {
        public void run() {
        	WordPressCore.getInstance().exitWordPress();
        }
    };
             
    //Override the makeMenu method so we can add a custom menu item
    protected void makeMenu(Menu menu, int instance)
    {
    	
    	if ( instance == Menu.INSTANCE_CONTEXT ) return; // Do not show the popup menu on this screen

    	if( currentBlog != null ) {
    		menu.add(_showBlogPosts);
    		menu.add(_showBlogPages);
    		menu.add(_showBlogComments);
    		menu.add(_showBlogStats);
    		menu.add(_refreshBlog);
    		menu.add(_showBlogOptions);
    		menu.add(_showBlogDashBoard);
    		menu.add(_notificationItem);
    		menu.add(_deleteBlogItem);
    		
        	//#ifdef BlackBerrySDK7.0.0
        	
        	//show the reader menu item when there are WP.com accounts
        	Hashtable applicationAccounts = MainController.getIstance().getApplicationAccounts();
        	if(applicationAccounts != null && applicationAccounts.size() > 0 && currentBlog.isWPCOMBlog() )
        		menu.add(_mobileReaderMenuItem);
        	
        	//#endif
    	}
    	
    	if(Preferences.getIstance().isBackgroundOnClose())
    		menu.add(_exitItem);
    	
    	//add the check for activation menu item if the blog is on pending state
    	/*if(blogListController != null) {
    		BlogInfo blogSelected = blogListController.getBlogSelected();
    		if(blogSelected.getState() == BlogInfo.STATE_PENDING_ACTIVATION)
    			menu.add(_activateBlogItem);
    	}*/
    	
        //Create the default menu.
        super.makeMenu(menu, instance);
    }
    
    //override onClose() to display a dialog box when the application 
    //menu close is selected or return btn is hitted    
	public boolean onClose() {
		Log.trace ("public boolean onClose()...");
    	return mainController.exitApp();
    }
 
	public BaseController getController() {
		return mainController;
	}
	
	
	private class RefreshBlogCallBack implements Observer {

		private ConnectionInProgressView connectionProgressView;

		public RefreshBlogCallBack(ConnectionInProgressView connectionProgressView) {
			super();
			this.connectionProgressView = connectionProgressView;
		}

		public void update(Observable observable, final Object object) {

			Log.trace(">>>Refreshing Blog Response");
			
			UiApplication.getUiApplication().invokeLater(new Runnable() {
				public void run() {
					if ( connectionProgressView.isDisplayed())
						UiApplication.getUiApplication().popScreen(connectionProgressView);
					connectionProgressView = null;
				} //end run
			});

			BlogConnResponse resp= (BlogConnResponse) object;

			if(resp.isStopped()){
				return;
			}
			
			Blog responseBlog = null;

			if( ! resp.isError() ) {
				try{
					responseBlog = (Blog) resp.getResponseObject(); 	
					responseBlog.setLoadingState(BlogInfo.STATE_LOADED);
					BlogDAO.updateBlog(responseBlog);							
					CommentsDAO.cleanGravatarCache(responseBlog);
				} catch (final Exception e) {
					mainController.displayErrorAndWait(e,"Error while storing the blog data");
				}
			} else {
				//something went wrong load the full blog info
				try {
					responseBlog = BlogDAO.getBlog(currentBlog);
				} catch (Exception e1) {
					mainController.displayErrorAndWait(e1,"Error while storing the blog data");
					return;
				}
				responseBlog.setLoadingState(BlogInfo.STATE_ERROR);
				final String respMessage = resp.getResponse();
				mainController.displayError(respMessage);
				try {
					BlogDAO.updateBlog(responseBlog);
				} catch (Exception e) {
					mainController.displayErrorAndWait(e,"Error while storing the blog data");	
				}
			}

			//update app blog
			WordPressCore wpCore = WordPressCore.getInstance();
			Vector applicationBlogs = wpCore.getApplicationBlogs();
			
			//update application blogs
			final BlogInfo currentBlogI = new BlogInfo(responseBlog);
			for(int count = 0; count < applicationBlogs.size(); ++count)
			{
				BlogInfo applicationBlogTmp = (BlogInfo)applicationBlogs.elementAt(count);
				if (applicationBlogTmp.equals(currentBlogI) )		
				{
					applicationBlogs.setElementAt(currentBlogI, count);
					break;
				}
			}
			
			currentBlog =  currentBlogI;
			//update the icon here and text here
			UiApplication.getUiApplication().invokeLater(new Runnable() {
				public void run() {
					refreshMainView();
				}
			});
		}//end update
	}
	
	private class ActionTableItem extends BitmapField {

		private final int bitmapType;
		private final String label;
		private final int menuIndex;
		private final int PADDING = 2;
		private int actionTableIconSize = -1;

		private Bitmap bmp;
		Font myFont = null;

		private int maxItemWidth = 0;
		private int fieldHeight, fieldWidth = 0;
		private int bitmapWidth , bitmapHeight;
		private int labelAdvice;

		private boolean focusableFlag = true;
		
		public ActionTableItem(int bitmapType, String text, int menuIndex)
		{
			super(Bitmap.getBitmapResource("folder_yellow_open"), Field.FOCUSABLE | FIELD_HCENTER | FIELD_VCENTER | USE_ALL_WIDTH );
			this.bitmapType = bitmapType;
			this.label = text;
			this.menuIndex = menuIndex;
					
			myFont = Font.getDefault().derive(Font.PLAIN);
			labelAdvice = myFont.getAdvance(label);
			
			//FIXME: This code should be remove asap. It is called from the manager bf the layout occurs.
			/* Margin + Icon1 padding Icon2 padding Icon3 + Margin */
			int rr = Display.getWidth() - ( 2 * 10 ) - ( 2 * 10 );
			rr = rr / 3; //Max image width!
			rr = findBitmapSizeThatFits(rr);
			fieldHeight = ( 2 * PADDING ) + this.getBitmapz(rr).getHeight() + PADDING + myFont.getHeight() + ( 2* PADDING );
		}
		
		public int getPreferredWidth() {
			return maxItemWidth > 0 ? maxItemWidth : fieldWidth;
		}
		
		public int getPreferredHeight() {
			return fieldHeight;
		}
		
		public boolean isFocusable() {
			return focusableFlag;
		}
		
		protected void drawFocus( Graphics graphics, boolean on ) 
		{
			if (on) {
				int prevColor = graphics.getColor();
				try {
					XYRect rect = new XYRect();
					getFocusRect(rect);
					graphics.setColor(GUIFactory.BTN_COLOUR_BACKGROUND_FOCUS);
					graphics.fillRoundRect(rect.x, rect.y, rect.width, rect.height, 20, 20); 
				} finally {
					graphics.setColor(prevColor);
				}
			} else {
				super.drawFocus(graphics, on);
			}
			paint(graphics);
		}

		protected void paint(Graphics graphics) {
			if( bitmapType == mnuReader &&  currentBlog != null && ! currentBlog.isWPCOMBlog() ) {
				focusableFlag = false;
				return;
			}
			
			String currentLbl = label;
			if( bitmapType == mnuComments && currentBlog.getAwaitingModeration() > 0 ) {
				currentLbl = "(" + currentBlog.getAwaitingModeration() + ") " + currentLbl; 
				labelAdvice = myFont.getAdvance(currentLbl);
			}
			
			focusableFlag = true;
			int availableWidthForChieldFields = maxItemWidth - ( 4 * PADDING ); //Do not use all the width available. see findBitmapSizeThatFits.
			int xOffset = ( availableWidthForChieldFields -  bitmapWidth ) / 2 ;
			xOffset += 2 * PADDING;
			graphics.drawBitmap(xOffset, ( 2 * PADDING ), bitmapWidth, bitmapHeight, bmp, 0, 0);

			int prevColor = graphics.getColor();
			try {
				if ( isFocus() || graphics.isDrawingStyleSet( Graphics.DRAWSTYLE_FOCUS ) )
					graphics.setColor( Color.WHITE );
				else
					graphics.setColor(Color.BLACK);
				graphics.setFont(myFont);
				xOffset = ( availableWidthForChieldFields - labelAdvice ) / 2 ;		
				if ( xOffset < 0 ) xOffset = 0;
				xOffset += 2 * PADDING;
				int yOffset =  ( 2 * PADDING ) + bitmapHeight + PADDING;
				graphics.drawText( currentLbl, xOffset, yOffset, DrawStyle.ELLIPSIS, availableWidthForChieldFields );
			} finally {
				graphics.setColor(prevColor);
			}

		}
		
		protected void layout(int width, int height) {
			maxItemWidth = width;
			
			int newIconSize = actionTableIconSize;
			newIconSize = findBitmapSizeThatFits(maxItemWidth);

			if( newIconSize != actionTableIconSize ) {
				actionTableIconSize = newIconSize;
				this.bmp = this.getBitmapz(actionTableIconSize);
				this.setBitmap(bmp); //just to make sure...
				bitmapWidth = bmp.getWidth();
				bitmapHeight = bmp.getHeight();
				fieldWidth = Math.max( labelAdvice, bitmapWidth ) + 20 ; //not sure that it is used
				fieldHeight = ( 2 * PADDING ) + bitmapHeight + PADDING + myFont.getHeight() + ( 2* PADDING );
			}
			
			super.layout(width, fieldHeight);
		}
		
		private int findBitmapSizeThatFits( int cellSize ) {
			int horizontalPadding = ( 4 * PADDING ); //the padding around the content of the cell 
			if( cellSize >= 96 + horizontalPadding  ) { 
				return 96;
			} else if( cellSize >= 72 + horizontalPadding  ) {
				return 72;
 			} else if( cellSize >= 64 + horizontalPadding  ) {
				return 64;
 			} else
 				return 48;
		}
		
		private Bitmap getBitmapz(int sizePrefix) {
			String size = sizePrefix > 0 ? "_"+sizePrefix+".png" : ".png";
			switch ( bitmapType ) {
			case (mnuPosts):
				return Bitmap.getBitmapResource("dashboard_icon_posts"+size);
			case (mnuPages):
				return Bitmap.getBitmapResource("dashboard_icon_pages"+size);
			case (mnuComments):
				return Bitmap.getBitmapResource("dashboard_icon_comments"+size);
			case (mnuMedia):
				return Bitmap.getBitmapResource("dashboard_icon_posts"+size);
			case (mnuStats):
				return Bitmap.getBitmapResource("dashboard_icon_stats"+size);
			case (mnuOptions):
				return Bitmap.getBitmapResource("dashboard_icon_settings"+size);
			case (mnuRefresh):
				return Bitmap.getBitmapResource("dashboard_icon_refresh"+size);
			case (mnuDashboard):
				return Bitmap.getBitmapResource("dashboard_icon_dashboard"+size);
			case (mnuReader):
				return Bitmap.getBitmapResource("dashboard_icon_subs"+size);
			default:
				return null;
			}
		}

		/**
	     * Overrides default implementation.  Performs the show blog action if the 
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
				return tableOrMenuItemSelected( menuIndex );
			}
			return super.navigationClick(status, time);
		}
		
	    /**
	     * Overrides default.  Enter key will take show blog action on selected blog.
	     *  
	     * @see net.rim.device.api.ui.Screen#keyChar(char,int,int)
	     * 
	     */
		protected boolean keyChar(char c, int status, int time) {
			Log.trace(">>> keyChar");
			// Close this screen if escape is selected.
			if (c == Characters.ENTER) {
				return tableOrMenuItemSelected( menuIndex );
			}
			
			return super.keyChar(c, status, time);
		}
		
		//#ifdef VER_4.7.0 | BlackBerrySDK5.0.0 | BlackBerrySDK6.0.0 | BlackBerrySDK7.0.0
		protected boolean touchEvent(TouchEvent message) {
				
		/*	if(!this.getContentRect().contains(message.getGlobalX(1), message.getGlobalY(1)))
			{       			
				return false; 
			}*/
			
	  		// Get the screen coordinates of the touch event
	        int x = message.getX(1);
	        int y = message.getY(1);
	        // Check to ensure point is within this field
	        if(x < 0 || y < 0 || x > getExtent().width || y > getExtent().height) {
	            return false;
	        }
			
			int eventCode = message.getEvent();

			if(WordPressInfo.isForcelessTouchClickSupported) {
				if (eventCode == TouchEvent.GESTURE) {
					TouchGesture gesture = message.getGesture();
					int gestureCode = gesture.getEvent();
					if (gestureCode == TouchGesture.TAP) {
						tableOrMenuItemSelected( menuIndex );
						return true;
					}
				} 
				return false;
			} else {
				if(eventCode == TouchEvent.CLICK) {
					tableOrMenuItemSelected( menuIndex );
					return true;
				}else if(eventCode == TouchEvent.DOWN) {
				} else if(eventCode == TouchEvent.UP) {
				} else if(eventCode == TouchEvent.UNCLICK) {
					return true; //consume the event: avoid context menu!!
				} else if(eventCode == TouchEvent.CANCEL) {
				}
				return false; 
				//return super.touchEvent(message);
			}
		}
		//#endif
	}
	
	private class ActionTableItemNullField extends LabelField {
		
		public ActionTableItemNullField() {
			this(Field.NON_FOCUSABLE);
		}

		public ActionTableItemNullField(long style) {
			super("A", style);
		}

		protected void paint(Graphics graphics) {
			
		}
		
		protected void layout(int width, int height) {
			super.layout(actionsTableItemVerticalPadding, actionsTableItemVerticalPadding);
		}
		
		public int getPreferredWidth() {
			return actionsTableItemVerticalPadding;
		}
		
		public int getPreferredHeight() {
			return actionsTableItemVerticalPadding;
		}
	}
	
}