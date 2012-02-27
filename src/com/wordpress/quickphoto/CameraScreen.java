//#preprocess
package com.wordpress.quickphoto;

import java.util.Vector;

import javax.microedition.media.Manager;
import javax.microedition.media.Player;
import javax.microedition.media.control.VideoControl;

import com.wordpress.bb.WordPressResource;
import com.wordpress.controller.MainController;
import com.wordpress.utils.ImageEncodingProperties;
import com.wordpress.utils.MultimediaUtils;
import com.wordpress.utils.log.Log;

import net.rim.device.api.system.Characters;
import net.rim.device.api.system.EncodedImage;
import net.rim.device.api.system.KeypadListener;
import net.rim.device.api.ui.Field;
import net.rim.device.api.ui.MenuItem;
import net.rim.device.api.ui.Screen;
import net.rim.device.api.ui.TouchEvent;
import net.rim.device.api.ui.TouchGesture;
import net.rim.device.api.ui.TransitionContext;
import net.rim.device.api.ui.Ui;
import net.rim.device.api.ui.UiApplication;
import net.rim.device.api.ui.UiEngineInstance;
import net.rim.device.api.ui.component.Menu;
import net.rim.device.api.ui.component.RichTextField;
import net.rim.device.api.ui.container.MainScreen;

import javax.microedition.amms.control.camera.FlashControl;
import javax.microedition.amms.control.camera.ZoomControl;
import net.rim.device.api.ui.input.InputSettings;
import net.rim.device.api.ui.input.NavigationDeviceSettings;
import net.rim.device.api.ui.menu.SubMenu;
import net.rim.device.api.util.StringProvider;
import net.rim.device.api.command.Command;
import net.rim.device.api.command.CommandHandler;
import net.rim.device.api.command.ReadOnlyCommandMetadata;
import net.rim.device.api.i18n.ResourceBundle;
import net.rim.device.api.amms.control.camera.EnhancedFocusControl;


//#ifdef BlackBerrySDK7.0.0
import net.rim.device.api.amms.control.camera.FeatureControl;
//#endif

/**
 * A UI screen to display the camera display and buttons
 */
final class CameraScreen extends MainScreen implements CameraScreenListener
{    

	private VideoControl _videoControl;    
    private Field _videoField;    
    private ImageEncodingProperties[] _encodings;            
    private ZoomControl _zoomControl;
    private Player _player;
    private MenuItem _encodingMenuItem; 
    private int _indexOfEncoding = 0;
    private String mediaFilePath = null;

    //#ifdef BlackBerrySDK7.0.0
    private EnhancedFocusControl _efc;
    //#endif

    private CameraScreenListener listener = null;
    
	//create a variable to store the ResourceBundle for localization support
	protected static ResourceBundle _resources;
	
	static {
		//retrieve a reference to the ResourceBundle for localization support
		_resources = ResourceBundle.getBundle(WordPressResource.BUNDLE_ID, WordPressResource.BUNDLE_NAME);
	}
    
    /**
     * Creates a new CameraScreen object
     */
    public CameraScreen()
    {
        // Set the title of the screen
        setTitle(_resources.getString(WordPressResource.MENUITEM_QUICKPHOTO));        

        // Initialize the camera object and video field
        initializeCamera();

        // Initialize the list of possible encodings
        try
        {
        	_encodings = MultimediaUtils.getSupportedPhotoFormat();
        }
        catch (Exception e)
        {
            // Something is wrong, indicate that there are no encoding options
            _encodings = null;
            //Do not show errors here. Default enconding will be used.
          //  MainController.getIstance().displayError(e, "Unable to initialize camera encodings");
        }    
        
        // If the field was constructed successfully, create the UI
        if( _videoField != null )
        {
            // Add the video field to the screen
            add(_videoField);
        
            // Initialize the camera features menus
           
            //#ifdef BlackBerrySDK7.0.0
            buildFocusModeMenuItems();          
            buildSceneModeMenuItems();
            //#endif 
            
            _encodingMenuItem = new MenuItem("Encoding Settings", 0x230010, 0){
            	public void run() {   
            		EncodingPropertiesScreen encodingPropertiesScreen = new EncodingPropertiesScreen(_encodings , CameraScreen.this, _indexOfEncoding);
            		UiApplication.getUiApplication().pushModalScreen(encodingPropertiesScreen);            
            	}               
            };          
            
            // Allow the screen to capture trackpad swipes
            InputSettings settings = NavigationDeviceSettings.createEmptySet();
            settings.set(NavigationDeviceSettings.DETECT_SWIPE, 1);
            addInputSettings(settings);
            
        }
        // If not, display an error message to the user
        else
        {
            add( new RichTextField( "Error connecting to camera." ) );
        }      
    }
    

    protected void onExposed(){
    	Log.trace("CameraScreen - onExposed");
    	super.onExposed();	
    	if ( this.mediaFilePath != null ) {
    		if ( listener != null ) {
    			listener.mediaItemTaken(mediaFilePath);
    		}
    		close();
    	}
    }
    
    public void setListener(CameraScreenListener listener) {
		this.listener = listener;
	}
    
    /**
     * @see Screen#makeMenu(Menu, int)
     */
    protected void makeMenu(Menu menu, int instance)
    {
           	
    	if ( instance == Menu.INSTANCE_CONTEXT ) return;
    	
    	super.makeMenu(menu, instance);
        if ( _videoField != null ) {
	        //#ifdef BlackBerrySDK7.0.0
	        if(_efc.isAutoFocusLocked())
	        {
	            menu.add(_turnOffAutoFocusMenuItem);
	        }
	        else
	        {
	            menu.add(_turnOnAutoFocusMenuItem);
	        }   
	        //#endif
	        
	        menu.add(_encodingMenuItem);
        }
    }
    
    /**
     * Takes a picture with the selected encoding settings
     */   
    private void takePicture()
    {
    	if ( _videoField == null ) return;
    	
        try
        {
            // A null encoding indicates that the camera should
            // use the default snapshot encoding.
            String encoding = null;            
            
            if( _encodings != null && _encodings.length > 0)
            {
                // Use the user-selected encoding
                encoding = _encodings[_indexOfEncoding].getFullEncoding();
            }
            
            // Retrieve the raw image from the VideoControl and
            // create a screen to display the image to the user.
            createImageScreen(_videoControl.getSnapshot(encoding));
        }
        catch(Exception e)
        {
        	MainController.getIstance().displayError(e, "Unable to take a picture");
        }  
    }
    
    /**
     * Initializes the Player, VideoControl and VideoField
     */
    private void initializeCamera()
    {
        try
        {
            // Create a player for the Blackberry's camera
            _player = Manager.createPlayer( "capture://video" );
            
            // Set the player to the REALIZED state (see Player javadoc)
            _player.realize();

            // Get the video control
            _videoControl = (VideoControl)_player.getControl( "VideoControl" );
            
            try {
				FlashControl flashControl = (FlashControl) _player.getControl("javax.microedition.amms.control.camera.FlashControl");
				if( flashControl != null )
					flashControl.setMode(FlashControl.AUTO);
			} catch (Exception e) {
				Log.error(e, "Can't set/get the Flash Control");
			}

            if (_videoControl != null)
            {
                // Create the video field as a GUI primitive (as opposed to a
                // direct video, which can only be used on platforms with
                // LCDUI support.)
                _videoField = (Field) _videoControl.initDisplayMode (VideoControl.USE_GUI_PRIMITIVE, "net.rim.device.api.ui.Field");
                _videoControl.setDisplayFullScreen(true);
                _videoControl.setVisible(true);
            }

            // Set the player to the STARTED state (see Player javadoc)
            _player.start();
                       
            //#ifdef BlackBerrySDK7.0.0
            // Enable auto-focus for the camera
            _efc = (EnhancedFocusControl)_player.getControl("net.rim.device.api.amms.control.camera.EnhancedFocusControl");
            //#endif
            
            // Enable zoom for the camera
            _zoomControl = (ZoomControl)_player.getControl( "javax.microedition.amms.control.camera.ZoomControl" );
        }
        catch(Exception e)
        {
        	MainController.getIstance().displayError("Unable to initialize camera");
        }
    }
    

    /**
     * Create a screen used to display a snapshot
     * @param raw A byte array representing an image
     */
    private void createImageScreen( byte[] raw )
    {   
        // Create image to be displayed
        EncodedImage encodedImage = EncodedImage.createEncodedImage(raw, 0, raw.length);
        
        // Initialize the screen        
        ImageScreen imageScreen = new ImageScreen(raw, encodedImage);
        imageScreen.setListener(this);

        // Push screen to display it to the user
		UiEngineInstance engine = Ui.getUiEngineInstance();
		TransitionContext transitionContextIn;
		transitionContextIn = new TransitionContext(TransitionContext.TRANSITION_SLIDE);
		transitionContextIn.setIntAttribute(TransitionContext.ATTR_DURATION, 500);
		transitionContextIn.setIntAttribute(TransitionContext.ATTR_DIRECTION, TransitionContext.DIRECTION_LEFT);   
		engine.setTransition(null, imageScreen, UiEngineInstance.TRIGGER_PUSH, transitionContextIn);
		
        UiApplication.getUiApplication().pushScreen(imageScreen);
    }
        
    /**
     * Sets the index of the encoding in the 'encodingList' Vector
     * @param index The index of the encoding in the 'encodingList' Vector
     */
    public void setIndexOfEncoding(int index)
    {
        _indexOfEncoding = index;
    }
    
    
    /**
     * @see net.rim.device.api.ui.container.MainScreen#onSavePrompt()
     */
    protected boolean onSavePrompt()
    {
        // Prevent the save dialog from being displayed
        return true;
    }
    
 
    protected boolean touchEvent(TouchEvent event)
    {
    	if(event.getEvent() == TouchEvent.GESTURE)
    	{
    		TouchGesture gesture = event.getGesture();
    		int gestureCode = gesture.getEvent();
    		if (gestureCode == TouchGesture.TAP) {
    			UiApplication.getApplication().invokeLater(new Runnable()
    			{
    				public void run()
    				{ 
    					takePicture();
    				}
    			});
    			return true;     
    		} else if(gesture.getEvent() == TouchGesture.NAVIGATION_SWIPE)  // Handle only trackpad swipe gestures
    		{
    			final int direction = gesture.getSwipeDirection();

    			UiApplication.getApplication().invokeLater(new Runnable()
    			{
    				public void run()
    				{
    					// Determine the direction of the swipe
    					if(direction == TouchGesture.SWIPE_NORTH)
    					{
    						_zoomControl.setDigitalZoom(ZoomControl.NEXT);
    					}
    					else if(direction == TouchGesture.SWIPE_SOUTH)
    					{
    						_zoomControl.setDigitalZoom(ZoomControl.PREVIOUS);
    					}
    				}
    			});

    			return true;
    		}
    	}

    	return false;
    }

    /**
     * @see net.rim.device.api.ui.Screen#close()
     */
    public void close()
    {
        if( _player!= null)
        {
            try
            {
                _player.stop();
                _player.close();
            }
            catch(Exception e)
            {
            	Log.error(e, "Error while closing the CameraScreen");
            }
        }    
        super.close();
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
			UiApplication.getApplication().invokeLater(new Runnable()
			{
				public void run()
				{ 
					takePicture();
				}
			});
			return true;  
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
			UiApplication.getApplication().invokeLater(new Runnable()
			{
				public void run()
				{ 
					takePicture();
				}
			});
			return true;  
		}
		return super.keyChar(c, status, time);
	}
    
    /**
     * @see net.rim.device.api.ui.Screen#invokeAction(int)
     */   
 /*
    protected boolean invokeAction(int action)
    {
        boolean handled = super.invokeAction(action); 
        
        if(!handled)
        {
            if(action == ACTION_INVOKE)
            {                         
                takePicture();
                return true;                
            }
        }  
              
        return handled;                
    }
    */
    
    //#ifdef BlackBerrySDK7.0.0
    
    MenuItem _turnOffAutoFocusMenuItem = new MenuItem("Turn Off Auto-Focus", 0x230020, 0){
    	public void run() {
    		try
    		{            
    			if(_efc != null)
    			{                            
    				_efc.stopAutoFocus();                                                    
    			}
    			else
    			{
    				MainController.getIstance().displayError("ERROR: Focus control not initialized.");
    			}
    		}
    		catch(Exception e)
    		{
    			MainController.getIstance().displayError("ERROR " + e.getClass() + ":  " + e.getMessage());
    		}
    	}               
    };


    MenuItem _turnOnAutoFocusMenuItem = new MenuItem("Turn on Auto-Focus", 0x230020, 0) {
    	public void run() {
    		try
    		{            
    			if(_efc != null)
    			{                           
    				_efc.startAutoFocus();                                               
    			}
    			else
    			{
    				MainController.getIstance().displayError("ERROR: Focus control not initialized.");
    			}
    		}
    		catch(Exception e)
    		{
    			MainController.getIstance().displayError("ERROR " + e.getClass() + ":  " + e.getMessage());
    		}
    	}               
    };
    
    
    /**
     * This method allows an array of menu items to be added to the submenu
     * which then gets added to the parent menu.
     * @param items The array of menu items that represents the submenu
     * @param menuTitle The text string of parent menu item that will contain the submenu items
     * @param ordering Ordering of the submenu relative to other items in the parent menu
     */
    private void addSubMenu(Vector items, String menuTitle, int ordering)
    {       
       int size = items.size();
     
       if(size > 0)
       {
            SubMenu subMenu = new SubMenu( null, menuTitle, ordering, Integer.MAX_VALUE);
            
            for(int i = size - 1; i >= 0; --i)
            {
                Object obj = items.elementAt(i);
                if(obj instanceof MenuItem) 
                {                                        
                    subMenu.add((MenuItem)obj);
                }
            }     
            
            addMenuItem(subMenu.getMenuItem());                     
        }
    }    
    


    /**
     * Builds the menu items for the various focus modes
     * supported on the device.
     */
    private void buildFocusModeMenuItems()
    {       
        if(_efc != null)
        {
            // Use a Vector to store each of the focus (sub)menu items
            Vector focusMenuItems = new Vector();
            
            // Check for fixed focus mode support
            if(_efc.isFocusModeSupported(EnhancedFocusControl.FOCUS_MODE_FIXED))
            {
                MenuItem enableFixedFocus = new MenuItem(new StringProvider("Enable Fixed Auto Focus"), 0x230010, 0);              
                enableFixedFocus.setCommand(new Command(new CommandHandler()
                {
                    /**
                    * @see CommandHandler#execute(ReadOnlyCommandMetadata, Object)
                    */
                    public void execute(ReadOnlyCommandMetadata metadata, Object context)
                    {
                        EnhancedFocusControl _efc =  (EnhancedFocusControl)_player.getControl("net.rim.device.api.amms.control.camera.EnhancedFocusControl");
                        _efc.setFocusMode(EnhancedFocusControl.FOCUS_MODE_FIXED );                        
                    };
                }));
                
                focusMenuItems.addElement(enableFixedFocus); 
            } 
            
            // Check for continuous focus mode support
            if(_efc.isFocusModeSupported(EnhancedFocusControl.FOCUS_MODE_CONTINUOUS ))
            {
                MenuItem enableContinuousAutoFocus = new MenuItem(new StringProvider("Enable Continuous Auto Focus"), 0x230020, 0); 
                enableContinuousAutoFocus.setCommand(new Command(new CommandHandler()
                {
                    /**
                    * @see CommandHandler#execute(ReadOnlyCommandMetadata, Object)
                    */
                    public void execute(ReadOnlyCommandMetadata metadata, Object context)
                    {
                        EnhancedFocusControl _efc =  (EnhancedFocusControl)_player.getControl("net.rim.device.api.amms.control.camera.EnhancedFocusControl");            
                        _efc.setFocusMode(EnhancedFocusControl.FOCUS_MODE_CONTINUOUS );
                    };
                }));                
                
                focusMenuItems.addElement(enableContinuousAutoFocus); 
            }
            
            // Check for single shot focus mode support
            if(_efc.isFocusModeSupported(EnhancedFocusControl.FOCUS_MODE_SINGLESHOT))
            {
                MenuItem enableSingleShotAutoFocus = new MenuItem(new StringProvider("Enable Single Shot Auto Focus"), 0x230030, 0);
                enableSingleShotAutoFocus.setCommand(new Command(new CommandHandler()
                {
                    /**
                    * @see CommandHandler#execute(ReadOnlyCommandMetadata, Object)
                    */
                    public void execute(ReadOnlyCommandMetadata metadata, Object context)
                    {
                        EnhancedFocusControl _efc =  (EnhancedFocusControl)_player.getControl("net.rim.device.api.amms.control.camera.EnhancedFocusControl");
                        _efc.setFocusMode(EnhancedFocusControl.FOCUS_MODE_SINGLESHOT );
                    };
                }));                 
                
                focusMenuItems.addElement(enableSingleShotAutoFocus); 
            } 
            
            addSubMenu(focusMenuItems, "Auto Focus Modes", 0x230030);     
        }
    }
    
    /**
     * Builds the menu items for the various scene modes supported on the device
     */
    private void buildSceneModeMenuItems()
    {

    	// Feature Control allows for accessing the various scene modes
    	final FeatureControl featureControl = (FeatureControl)_player.getControl("net.rim.device.api.amms.control.camera.FeatureControl");

    	if(featureControl != null)
    	{ 
    		// Use a Vector to store each of the scene mode (sub)menu items
    		Vector sceneModeMenuItems = new Vector();

    		// Check for auto scene mode support
    		if(featureControl.isSceneModeSupported(FeatureControl.SCENE_MODE_AUTO))
    		{
    			MenuItem enableSceneModeAuto = new MenuItem(new StringProvider("Enable Scene Mode: AUTO"), Integer.MAX_VALUE, 0);
    			enableSceneModeAuto.setCommand(new Command(new CommandHandler()
    			{
    				/**
    				 * @see CommandHandler#execute(ReadOnlyCommandMetadata, Object)
    				 */
    				public void execute(ReadOnlyCommandMetadata metadata, Object context)
    				{
    					featureControl.setSceneMode(FeatureControl.SCENE_MODE_AUTO);
    				};
    			}));                    

    			sceneModeMenuItems.addElement(enableSceneModeAuto); 
    		} 

    		// Check for beach scene mode support
    		if(featureControl.isSceneModeSupported(FeatureControl.SCENE_MODE_BEACH))
    		{
    			MenuItem enableSceneModeBeach = new MenuItem(new StringProvider("Enable Scene Mode: BEACH"), Integer.MAX_VALUE, 0);
    			enableSceneModeBeach.setCommand(new Command(new CommandHandler()
    			{
    				/**
    				 * @see CommandHandler#execute(ReadOnlyCommandMetadata, Object)
    				 */
    				public void execute(ReadOnlyCommandMetadata metadata, Object context)
    				{
    					featureControl.setSceneMode(FeatureControl.SCENE_MODE_BEACH);
    				};
    			}));                

    			sceneModeMenuItems.addElement(enableSceneModeBeach); 
    		}

    		// Check for face detection scene mode support
    		if(featureControl.isSceneModeSupported(FeatureControl.SCENE_MODE_FACEDETECTION))
    		{
    			MenuItem enableSceneModeFaceDetection = new MenuItem(new StringProvider("Enable Scene Mode: FACE DETECTION"), Integer.MAX_VALUE, 0);
    			enableSceneModeFaceDetection.setCommand(new Command(new CommandHandler()
    			{
    				/**
    				 * @see CommandHandler#execute(ReadOnlyCommandMetadata, Object)
    				 */
    				public void execute(ReadOnlyCommandMetadata metadata, Object context)
    				{
    					featureControl.setSceneMode(FeatureControl.SCENE_MODE_FACEDETECTION);
    				};
    			}));                

    			sceneModeMenuItems.addElement(enableSceneModeFaceDetection); 
    		}  

    		// Check for landscape scene mode support
    		if(featureControl.isSceneModeSupported(FeatureControl.SCENE_MODE_LANDSCAPE))
    		{
    			MenuItem enableSceneModeLandscape = new MenuItem(new StringProvider("Enable Scene Mode: LANDSCAPE"), Integer.MAX_VALUE, 0);
    			enableSceneModeLandscape.setCommand(new Command(new CommandHandler()
    			{
    				/**
    				 * @see CommandHandler#execute(ReadOnlyCommandMetadata, Object)
    				 */
    				public void execute(ReadOnlyCommandMetadata metadata, Object context)
    				{
    					featureControl.setSceneMode(FeatureControl.SCENE_MODE_LANDSCAPE);
    				};
    			}));                

    			sceneModeMenuItems.addElement(enableSceneModeLandscape); 
    		}  

    		// Check for macro scene mode support
    		if(featureControl.isSceneModeSupported(FeatureControl.SCENE_MODE_MACRO))
    		{
    			MenuItem enableSceneModeMacro = new MenuItem(new StringProvider("Enable Scene Mode: MACRO"), Integer.MAX_VALUE, 0);
    			enableSceneModeMacro.setCommand(new Command(new CommandHandler()
    			{
    				/**
    				 * @see CommandHandler#execute(ReadOnlyCommandMetadata, Object)
    				 */
    				public void execute(ReadOnlyCommandMetadata metadata, Object context)
    				{
    					featureControl.setSceneMode(FeatureControl.SCENE_MODE_MACRO);
    				};
    			}));                    

    			sceneModeMenuItems.addElement(enableSceneModeMacro); 
    		}  

    		// Check for night scene mode support
    		if(featureControl.isSceneModeSupported(FeatureControl.SCENE_MODE_NIGHT))
    		{
    			MenuItem enableSceneModeNight = new MenuItem(new StringProvider("Enable Scene Mode: NIGHT"), Integer.MAX_VALUE, 0);
    			enableSceneModeNight.setCommand(new Command(new CommandHandler()
    			{
    				/**
    				 * @see CommandHandler#execute(ReadOnlyCommandMetadata, Object)
    				 */
    				public void execute(ReadOnlyCommandMetadata metadata, Object context)
    				{
    					featureControl.setSceneMode(FeatureControl.SCENE_MODE_NIGHT);
    				};
    			}));               

    			sceneModeMenuItems.addElement(enableSceneModeNight); 
    		}  

    		// Check for party scene mode support
    		if(featureControl.isSceneModeSupported(FeatureControl.SCENE_MODE_PARTY))
    		{
    			MenuItem enableSceneModeParty = new MenuItem(new StringProvider("Enable Scene Mode: PARTY"), Integer.MAX_VALUE, 0);
    			enableSceneModeParty.setCommand(new Command(new CommandHandler()
    			{
    				/**
    				 * @see CommandHandler#execute(ReadOnlyCommandMetadata, Object)
    				 */
    				public void execute(ReadOnlyCommandMetadata metadata, Object context)
    				{
    					featureControl.setSceneMode(FeatureControl.SCENE_MODE_PARTY);
    				};
    			}));                

    			sceneModeMenuItems.addElement(enableSceneModeParty); 
    		}  

    		// Check for portrait scene mode support
    		if(featureControl.isSceneModeSupported(FeatureControl.SCENE_MODE_PORTRAIT))
    		{
    			MenuItem enableSceneModePortrait = new MenuItem(new StringProvider("Enable Scene Mode: PORTRAIT"), Integer.MAX_VALUE, 0);
    			enableSceneModePortrait.setCommand(new Command(new CommandHandler()
    			{
    				/**
    				 * @see CommandHandler#execute(ReadOnlyCommandMetadata, Object)
    				 */
    				public void execute(ReadOnlyCommandMetadata metadata, Object context)
    				{
    					featureControl.setSceneMode(FeatureControl.SCENE_MODE_PORTRAIT);
    				};
    			}));                

    			sceneModeMenuItems.addElement(enableSceneModePortrait); 
    		}  

    		// Check for snow scene mode support
    		if(featureControl.isSceneModeSupported(FeatureControl.SCENE_MODE_SNOW))
    		{
    			MenuItem enableSceneModeSnow = new MenuItem(new StringProvider("Enable Scene Mode: SNOW"), Integer.MAX_VALUE, 0);
    			enableSceneModeSnow.setCommand(new Command(new CommandHandler()
    			{
    				/**
    				 * @see CommandHandler#execute(ReadOnlyCommandMetadata, Object)
    				 */
    				public void execute(ReadOnlyCommandMetadata metadata, Object context)
    				{
    					featureControl.setSceneMode(FeatureControl.SCENE_MODE_SNOW);
    				};
    			}));                

    			sceneModeMenuItems.addElement(enableSceneModeSnow); 
    		}  

    		// Check for sport scene mode support
    		if(featureControl.isSceneModeSupported(FeatureControl.SCENE_MODE_SPORT))
    		{
    			MenuItem enableSceneModeSport = new MenuItem(new StringProvider("Enable Scene Mode: SPORT"), Integer.MAX_VALUE, 0);
    			enableSceneModeSport.setCommand(new Command(new CommandHandler()
    			{
    				/**
    				 * @see CommandHandler#execute(ReadOnlyCommandMetadata, Object)
    				 */
    				public void execute(ReadOnlyCommandMetadata metadata, Object context)
    				{
    					featureControl.setSceneMode(FeatureControl.SCENE_MODE_SPORT);
    				};
    			}));                

    			sceneModeMenuItems.addElement(enableSceneModeSport); 
    		} 

    		// Check for text scene mode support
    		if(featureControl.isSceneModeSupported(FeatureControl.SCENE_MODE_TEXT))
    		{
    			MenuItem enableSceneModeText = new MenuItem(new StringProvider("Enable Scene Mode: TEXT"), Integer.MAX_VALUE, 0);
    			enableSceneModeText.setCommand(new Command(new CommandHandler()
    			{
    				/**
    				 * @see CommandHandler#execute(ReadOnlyCommandMetadata, Object)
    				 */
    				public void execute(ReadOnlyCommandMetadata metadata, Object context)
    				{
    					featureControl.setSceneMode(FeatureControl.SCENE_MODE_TEXT);
    				};
    			}));                

    			sceneModeMenuItems.addElement(enableSceneModeText); 
    		}  

    		addSubMenu(sceneModeMenuItems, "Scene Modes", 0x230040);
    	}
    }
    
    //#endif
    
	public void mediaItemTaken(final String filePath) {
		this.mediaFilePath = filePath;
	}
	/*
    public void paint(Graphics g) {
    	super.paint(g);
    	int oldColour = g.getColor();
    	int oldAlpha = g.getGlobalAlpha();
    	try {
    		g.setGlobalAlpha(125);
    		g.setColor(Color.BLACK);
    		g.drawRect( (Display.getWidth() - 200) / 2, (Display.getHeight() - 200) / 2, 200, 200);
    	} finally {
    		g.setColor( oldColour );
    		g.setGlobalAlpha(oldAlpha);
    	}
    }*/
}
