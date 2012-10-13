package com.wordpress.view;

import java.io.IOException;
import java.util.Vector;

import javax.microedition.rms.RecordStoreException;

import net.rim.device.api.ui.Color;
import net.rim.device.api.ui.Field;
import net.rim.device.api.ui.FieldChangeListener;
import net.rim.device.api.ui.Manager;
import net.rim.device.api.ui.MenuItem;
import net.rim.device.api.ui.component.BasicEditField;
import net.rim.device.api.ui.component.ButtonField;
import net.rim.device.api.ui.component.CheckboxField;
import net.rim.device.api.ui.component.Dialog;
import net.rim.device.api.ui.component.EditField;
import net.rim.device.api.ui.component.LabelField;
import net.rim.device.api.ui.component.ObjectChoiceField;
import net.rim.device.api.ui.component.RadioButtonField;
import net.rim.device.api.ui.component.RadioButtonGroup;
import net.rim.device.api.ui.container.HorizontalFieldManager;
import net.rim.device.api.ui.text.URLTextFilter;

import com.wordpress.bb.WordPressCore;
import com.wordpress.bb.WordPressResource;
import com.wordpress.controller.BaseController;
import com.wordpress.controller.FrontController;
import com.wordpress.controller.MainController;
import com.wordpress.controller.PreferenceController;
import com.wordpress.io.AppDAO;
import com.wordpress.io.BaseDAO;
import com.wordpress.io.BlogDAO;
import com.wordpress.io.JSR75FileSystem;
import com.wordpress.model.Preferences;
import com.wordpress.utils.StringUtils;
import com.wordpress.utils.conn.ConnectionUtils;
import com.wordpress.utils.log.FileAppender;
import com.wordpress.utils.log.Log;
import com.wordpress.view.component.BaseButtonField;
import com.wordpress.view.container.BorderedFieldManager;
import com.wordpress.view.dialog.DiscardChangeInquiryView;

public class PreferencesView extends StandardBaseView {
	
    private PreferenceController controller= null;
    private Preferences mPrefs = Preferences.getIstance();
	private ObjectChoiceField audioGroup;
	private ObjectChoiceField photoGroup;
	private ObjectChoiceField videoGroup;
	private HorizontalFieldManager buttonsManager;
	private EditField _username;
	private EditField _password;
	private EditField _gateway;
    private EditField _gatewayPort;
    private EditField _apn;
    private EditField _sourceIP;
    private EditField _sourcePort;
	private CheckboxField userConnectionEnabledField;
	private CheckboxField userConnectionWapTypeField;
	private CheckboxField _userAllowBIS;
	private CheckboxField _userAllowWiFi;
	private CheckboxField _userAllowTCP;
	private CheckboxField _userAllowWAP2;
	private CheckboxField _userAllowBES;
	private CheckboxField _debugMode;
	private ObjectChoiceField storageOpt;
	private CheckboxField autoStartup;
	private CheckboxField backgroundOnClose;
	private RadioButtonField  _gpsAssisted;
	private RadioButtonField  _gpsAutonomous;
	private RadioButtonField  _gpsCellTower;
	private RadioButtonGroup rgrp;
	
	 public PreferencesView(PreferenceController _preferencesController) {
	    	super(_resources.getString(WordPressResource.TITLE_SETTINGS_VIEW), Manager.NO_VERTICAL_SCROLL | Manager.NO_VERTICAL_SCROLLBAR);
	    	this.controller=_preferencesController;
	    		    	
	    	//the multimedia capabilities are now not managed into app. 
	    	//the photo settings are managed into camera app.
	    	//addMultimediaOption(); 
            addConnectionOptionsFields();
            addGPSOptionsFields();
            addStorageOptionFields();
            addStartupOptionsFields();
            addAdvancedConnectionOptionsFields();
            addDebugModeOptionFields();
            
            BaseButtonField buttonOK= GUIFactory.createButton(_resources.getString(WordPressResource.BUTTON_OK), ButtonField.CONSUME_CLICK | ButtonField.NEVER_DIRTY);
            BaseButtonField buttonBACK= GUIFactory.createButton(_resources.getString(WordPressResource.BUTTON_BACK), ButtonField.CONSUME_CLICK | ButtonField.NEVER_DIRTY);
    		buttonBACK.setChangeListener(listenerBackButton);
            buttonOK.setChangeListener(listenerOkButton);
            buttonsManager = new HorizontalFieldManager(Field.FIELD_HCENTER);
            buttonsManager.add(buttonOK);
    		buttonsManager.add(buttonBACK);
    		
    		add(buttonsManager); 
           
            add(new LabelField("", Field.NON_FOCUSABLE)); //space after buttons

    		addMenuItem(_saveItem);
    		controller.bumpScreenViewStats("com/wordpress/view/PreferencesView", "Preferences Screen", "", null, "");
	 }
	 

	 private void addGPSOptionsFields() {
		 BorderedFieldManager gpsManager = new BorderedFieldManager(
				 Manager.NO_HORIZONTAL_SCROLL
				 | Manager.NO_VERTICAL_SCROLL);
		 rgrp = new RadioButtonGroup();

		 LabelField lblTitle = GUIFactory.getLabel(_resources.getString(WordPressResource.OPTIONSSCREEN_TITLE_GPS),
				 Color.BLACK);
		 gpsManager.add(lblTitle);
		 gpsManager.add(GUIFactory.createSepatorField());

		 int gpsMode = mPrefs.getGPSSettings();
		 
		 _gpsAssisted=new RadioButtonField (_resources.getString(WordPressResource.OPTIONSSCREEN_LABEL_GPS_ASSISTED), rgrp, gpsMode == Preferences.GPS_ASSISTED ? true : false);
		 gpsManager.add(_gpsAssisted);
		 _gpsAutonomous=new RadioButtonField (_resources.getString(WordPressResource.OPTIONSSCREEN_LABEL_GPS_AUTONOMOUS), rgrp,  gpsMode == Preferences.GPS_AUTONOMOUS ? true : false);
		 gpsManager.add(_gpsAutonomous);
		 _gpsCellTower=new RadioButtonField (_resources.getString(WordPressResource.OPTIONSSCREEN_LABEL_GPS_CELLTOWER), rgrp, gpsMode == Preferences.GPS_CELL_TOWER ? true : false);
		 gpsManager.add(_gpsCellTower);
		 add(gpsManager);
	 }

	 private void addStartupOptionsFields() {

		 BorderedFieldManager optManager = new BorderedFieldManager(
				 Manager.NO_HORIZONTAL_SCROLL
				 | Manager.NO_VERTICAL_SCROLL);
		 

		 LabelField lblStatus = GUIFactory.getLabel(_resources.getString(WordPressResource.OPTIONSSCREEN_TITLE_STARTUP),
				 Color.BLACK);
		 optManager.add(lblStatus);
		 optManager.add(GUIFactory.createSepatorField());
		 
		 //startup checkbox		 
		 autoStartup = new CheckboxField(_resources.getString(WordPressResource.OPTIONSSCREEN_STARTUP_LABEL), mPrefs.isAutoStartup());
		 optManager.add(autoStartup);
		 //row startup description
         BasicEditField lblStartup = GUIFactory.getDescriptionTextField(_resources.getString(WordPressResource.OPTIONSSCREEN_STARTUP_DESC)); 
		 optManager.add(lblStartup);
		 
		 optManager.add(new LabelField("", Field.NON_FOCUSABLE));
		 
		 //background on close checkbox
		 backgroundOnClose = new CheckboxField(_resources.getString(WordPressResource.OPTIONSSCREEN_BACKGROUND_LABEL), mPrefs.isBackgroundOnClose());
		 optManager.add(backgroundOnClose);
		 //background on close description
         BasicEditField lblDescReset = GUIFactory.getDescriptionTextField(_resources.getString(WordPressResource.OPTIONSSCREEN_BACKGROUND_DESC)); 
		 optManager.add(lblDescReset);	
		 
		 add(optManager);
	 }
	 	 	 
	 private void addStorageOptionFields(){
		 boolean isLoadingInProgress = MainController.getIstance().isLoadingBlogs();
		 
		 BorderedFieldManager storageManager = new BorderedFieldManager(
				 Manager.NO_HORIZONTAL_SCROLL
				 | Manager.NO_VERTICAL_SCROLL);

		 LabelField lblStatus = GUIFactory.getLabel(_resources.getString(WordPressResource.OPTIONSSCREEN_TITLE_STORAGE),
				 Color.BLACK);
		 storageManager.add(lblStatus);
		 storageManager.add(GUIFactory.createSepatorField());

		 if(JSR75FileSystem.supportMicroSD() && JSR75FileSystem.hasMicroSD()) {
			 //row storage opt
			 
			 String labelDeviceStorageLocation= _resources.getString(WordPressResource.OPTIONSSCREEN_STORAGE_DEVICE);
			 String labelSdCardStorageLocation= _resources.getString(WordPressResource.OPTIONSSCREEN_STORAGE_SDCARD);
			 String[] storageOptLabels = {labelDeviceStorageLocation, labelSdCardStorageLocation};
			 int selectedStorage = 0;
			 
			 try {
				 if(AppDAO.SD_STORE_PATH.equals(AppDAO.getBaseDirPath())) {
					 selectedStorage = 1;
				 }
			 } catch (RecordStoreException e) {
				 Log.error(e, "Storage Option Field error");
				 return;
			 } catch (IOException e) {
				 Log.error(e, "Storage Option Field error");
				 return;
			 } 
			 
			 storageOpt = new ObjectChoiceField(_resources.getString(WordPressResource.OPTIONSSCREEN_STORAGE_LOCATION_LABEL),storageOptLabels, selectedStorage);
			 storageManager.add(storageOpt); 

			 //do not let user changes the storage folder while loading blogs in backgorund
			if(isLoadingInProgress) {
				storageOpt.setEditable(false);
			}
			 
			 storageManager.add(new LabelField("", Field.NON_FOCUSABLE));
		 }

		 if(!isLoadingInProgress) {
			 BaseButtonField buttonReset = GUIFactory.createButton(_resources.getString(WordPressResource.BUTTON_REMOVE), ButtonField.CONSUME_CLICK);
			 buttonReset.setChangeListener(listenerResetButton);
			 storageManager.add(buttonReset);
			 BasicEditField lblDesc = getDescriptionTextField(_resources.getString(WordPressResource.DESCRIPTION_REMOVE_TEMPFILE)); 
			 storageManager.add(lblDesc);
		 }
		 add(storageManager);
	 }
	 
	 private void addDebugModeOptionFields(){

		 BorderedFieldManager debugManager = new BorderedFieldManager(
				 Manager.NO_HORIZONTAL_SCROLL
				 | Manager.NO_VERTICAL_SCROLL);
		 
		 LabelField lblTitle = GUIFactory.getLabel(_resources.getString(WordPressResource.OPTIONSSCREEN_TITLE_LOG_OPTIONS),
				 Color.BLACK);
		 debugManager.add(lblTitle);
		 debugManager.add(GUIFactory.createSepatorField());
		 
		 BasicEditField lblDesc = getDescriptionTextField(_resources.getString(WordPressResource.OPTIONSSCREEN_DEBUG_DESC));
		 lblDesc.setMargin(0,0,5,0);
		 debugManager.add(lblDesc);
		 	 
		 _debugMode=new CheckboxField(_resources.getString(WordPressResource.OPTIONSSCREEN_DEBUG_LABEL), mPrefs.isDebugMode());
		 debugManager.add(_debugMode);	
		 debugManager.add( getDescriptionTextField(_resources.getString(WordPressResource.OPTIONSSCREEN_DEBUG_WARNING)));

		 add(debugManager);
	 }
	 
	 private void addConnectionOptionsFields(){

		 BorderedFieldManager optManager = new BorderedFieldManager(
				 Manager.NO_HORIZONTAL_SCROLL
				 | Manager.NO_VERTICAL_SCROLL);
		 
		 
		 LabelField lblTitle = GUIFactory.getLabel(_resources.getString(WordPressResource.OPTIONSSCREEN_TITLE_CONNECTION_OPTIONS),
				 Color.BLACK);
		 optManager.add(lblTitle);
		 optManager.add(GUIFactory.createSepatorField());
		 
         //description text
         BasicEditField lblDesc = GUIFactory.getDescriptionTextField(_resources.getString(WordPressResource.OPTIONSSCREEN_ALLOW_DESC)); 
		 lblDesc.setMargin(0,0,5,0);
         optManager.add(lblDesc);
		 
		 if ( ConnectionUtils.isWifiAvailable() ) {
			 _userAllowWiFi=new CheckboxField(_resources.getString(WordPressResource.OPTIONSSCREEN_ALLOW_WIFI), mPrefs.isWiFiConnectionPermitted());
			 optManager.add(_userAllowWiFi);
		 }
		 _userAllowTCP=new CheckboxField(_resources.getString(WordPressResource.OPTIONSSCREEN_ALLOW_TCP), mPrefs.isTcpConnectionPermitted());
		 optManager.add(_userAllowTCP);
		 _userAllowWAP2=new CheckboxField(_resources.getString(WordPressResource.OPTIONSSCREEN_ALLOW_WAP2), mPrefs.isServiceBookConnectionPermitted());
		 optManager.add(_userAllowWAP2);
		 _userAllowBIS=new CheckboxField(_resources.getString(WordPressResource.OPTIONSSCREEN_ALLOW_BIS), mPrefs.isBlackBerryInternetServicePermitted());
		 optManager.add(_userAllowBIS);
		 _userAllowBES=new CheckboxField(_resources.getString(WordPressResource.OPTIONSSCREEN_ALLOW_BES), mPrefs.isBESConnectionPermitted());
		 optManager.add(_userAllowBES);
		 
		 add(optManager);
	 }

	 
	 
	 private void addAdvancedConnectionOptionsFields(){
		 
		 BorderedFieldManager optManager = new BorderedFieldManager(
				 Manager.NO_HORIZONTAL_SCROLL
				 | Manager.NO_VERTICAL_SCROLL);
		 
		 LabelField lblTitle = GUIFactory.getLabel(_resources.getString(WordPressResource.OPTIONSSCREEN_TITLE_ADVANCED_CONNECTION_OPTIONS),
				 Color.BLACK);
		 optManager.add(lblTitle);
		 optManager.add(GUIFactory.createSepatorField());
		 
	      //description
		 BasicEditField lblDesc = getDescriptionTextField(_resources.getString(WordPressResource.OPTIONSSCREEN_USERDEFINEDCONN_DESC));
		 lblDesc.setMargin(0,0,5,0);
		 optManager.add(lblDesc);
		 
		 userConnectionEnabledField=new CheckboxField(_resources.getString(WordPressResource.OPTIONSSCREEN_LABEL_ENABLE_ADVANCED_CONNECTION_SETTINGS), mPrefs.isUserConnectionOptionsEnabled());
		 optManager.add(userConnectionEnabledField);
		 BasicEditField lblWarning = getDescriptionTextField(_resources.getString(WordPressResource.OPTIONSSCREEN_USERDEFINEDCONN_WARNING));
		 optManager.add(lblWarning);
		 
         //row _apn
         _apn = new EditField(_resources.getString(WordPressResource.OPTIONSSCREEN_LABEL_APN)+": ", mPrefs.getApn());
         _apn.setMargin(5, 0, 0, 0);
         optManager.add(_apn);
         
         //row _username
         _username = new EditField(_resources.getString(WordPressResource.LABEL_USERNAME)+": ", mPrefs.getUsername());
         _username.setMargin(5, 0, 0, 0);
         optManager.add(_username);
         
         //row _password
         _password = new EditField(_resources.getString(WordPressResource.LABEL_PASSWD)+": ", mPrefs.getPassword());
         _password.setMargin(5, 0, 0, 0);
         optManager.add(_password);

         //row is wap connection?
         userConnectionWapTypeField=new CheckboxField(_resources.getString(WordPressResource.OPTIONSSCREEN_LABEL_ISWAP), mPrefs.isUserConnectionWap());
         userConnectionWapTypeField.setMargin(5, 0, 0, 0);
         optManager.add(userConnectionWapTypeField);
         
         //row _gateway IP
         _gateway = new EditField(_resources.getString(WordPressResource.OPTIONSSCREEN_LABEL_GWAY)+": ", mPrefs.getGateway(), 100, Field.EDITABLE);
         _gateway.setMargin(5, 0, 0, 0);
         _gateway.setFilter(new URLTextFilter());
         optManager.add(_gateway);
         
         //row _gatewayPort
         _gatewayPort = new EditField(_resources.getString(WordPressResource.OPTIONSSCREEN_LABEL_GWAYPORT)+": ", mPrefs.getGatewayPort(), EditField.DEFAULT_MAXCHARS, EditField.FILTER_INTEGER);
         _gatewayPort.setMargin(5, 0, 0, 0);
         optManager.add(_gatewayPort);
         
         //row _sourcePort
         _sourcePort = new EditField(_resources.getString(WordPressResource.OPTIONSSCREEN_LABEL_SRCPORT)+": ", mPrefs.getSourcePort(), EditField.DEFAULT_MAXCHARS, EditField.FILTER_INTEGER);
         _sourcePort.setMargin(5, 0, 0, 0);
         optManager.add(_sourcePort);
         
         //row _sourceIP
         _sourceIP = new EditField(_resources.getString(WordPressResource.OPTIONSSCREEN_LABEL_SRCIP)+": ", mPrefs.getSourceIP());
         _sourceIP.setMargin(5, 0, 0, 0);
         optManager.add(_sourceIP);

         add(optManager);
	 }
	 
	 /*
	 private void addMultimediaOption() {
		 
		 BorderedFieldManager multimediaOptManager = new BorderedFieldManager(
				 Manager.NO_HORIZONTAL_SCROLL
				 | Manager.NO_VERTICAL_SCROLL
				 | BorderedFieldManager.BOTTOM_BORDER_NONE);
		 
		 //audio config 
		 if( MultimediaUtils.isAudioRecordingSuported()){
			 String[] lines=MultimediaUtils.getSupportedAudioFormat();
			 int selectedIndex=0;
			 for (int i = 0; i < lines.length; i++) {
				 if(lines[i].equalsIgnoreCase(mPrefs.getAudioEncoding())){
					 selectedIndex=i;
				 }
			 }
			 
			 audioGroup = new ObjectChoiceField(_resources.getString(WordPressResource.LABEL_AUDIOENCODING),lines,selectedIndex);
			 //audioGroup.setChangeListener(controller.getAudioListener());
			 multimediaOptManager.add( audioGroup );
		 } else {
			 LabelField lbl = new LabelField(_resources.getString(WordPressResource.LABEL_AUDIORECORDING_NOTSUPPORTED));
			 Font fnt = this.getFont().derive(Font.ITALIC);
			 lbl.setFont(fnt);
			 multimediaOptManager.add(lbl);
		 }
		 
		 //photo config
		 if(MultimediaUtils.isPhotoCaptureSupported()){
			 String[] lines=MultimediaUtils.getSupportedPhotoFormat();
			 int selectedIndex=0;
			 for (int i = 0; i < lines.length; i++) {
				 if(lines[i].equalsIgnoreCase(mPrefs.getPhotoEncoding())){
					 selectedIndex=i;
				 }
			 }
			 
			 photoGroup = new ObjectChoiceField(_resources.getString(WordPressResource.LABEL_PHOTOENCODING),lines,selectedIndex);
			 //photoGroup.setChangeListener(controller.getPhotoListener());
			 multimediaOptManager.add( photoGroup );
		 } else {
			 LabelField lbl = new LabelField(_resources.getString(WordPressResource.LABEL_PHOTO_NOTSUPPORTED));
			 Font fnt = this.getFont().derive(Font.ITALIC);
			 lbl.setFont(fnt);
			 multimediaOptManager.add(lbl);
		 }
		 
		 //video config
		 if(MultimediaUtils.isVideoRecordingSupported()){
			 String[] lines=MultimediaUtils.getSupportedWordPressVideoFormat();
			 int selectedIndex=0;
			 for (int i = 0; i < lines.length; i++) {
				 if(lines[i].equalsIgnoreCase(mPrefs.getVideoEncoding())){
					 selectedIndex=i; 
				 }
			 }
			 
			 videoGroup = new ObjectChoiceField(_resources.getString(WordPressResource.LABEL_VIDEOENCODING),lines,selectedIndex);
			 //videoGroup.setChangeListener(controller.getVideoListener());
			 multimediaOptManager.add( videoGroup );
		 } else {
			 LabelField lbl = new LabelField(_resources.getString(WordPressResource.LABEL_VIDEORECORDING_NOTSUPPORTED));
			 Font fnt = this.getFont().derive(Font.ITALIC);
			 lbl.setFont(fnt);
			 multimediaOptManager.add(lbl);
		 }
		 add(multimediaOptManager);	
	 }
	 */

	 //create a menu item for users click to save
	    private MenuItem _saveItem = new MenuItem( _resources, WordPressResource.MENUITEM_SAVE, 1000, 10) {
	        public void run() {
	        	updatePreferencesModel();
	        	controller.savePrefAndBack();
	        }
	    };

	    
		private FieldChangeListener listenerOkButton = new FieldChangeListener() {
		    public void fieldChanged(Field field, int context) {
		    	updatePreferencesModel();
		    	controller.savePrefAndBack();
		    }
		};


		private FieldChangeListener listenerBackButton = new FieldChangeListener() {
		    public void fieldChanged(Field field, int context) {
		    	onClose();
		    }
		};
		
		private FieldChangeListener listenerResetButton = new FieldChangeListener() {
		    public void fieldChanged(Field field, int context) {

		    	int askQuestion = controller.askQuestion(_resources.getString(WordPressResource.MESSAGE_REMOVE_TEMPFILES));
		    	if (askQuestion == Dialog.YES) {
		    	
		    	} else {
		    		return;
		    	}
		    	try {
		    		FileAppender fileAppender = WordPressCore.getInstance().getFileAppender();
		    		Log.removeAppender(fileAppender);
		    		fileAppender.close();		  	
		    		AppDAO.resetAppData(); //reset the PersistenStore
					AppDAO.setUpFolderStructure(); //create the folder struction on FS
					fileAppender.open(); //reopen the log file
					Log.addAppender(fileAppender);
					Vector applicationBlogs = WordPressCore.getInstance().getApplicationBlogs();
		   		 	applicationBlogs.removeAllElements();
		   		 	MainController.getIstance().getApplicationAccounts().clear();
					updatePreferencesModel();
		    		FrontController.getIstance().backToMainView();
				} catch (RecordStoreException e) {
					Log.error(e.getMessage());
					controller.displayErrorAndWait(e, "Error while deleting temp files!");
				} catch (IOException e) {
					controller.displayErrorAndWait(e, "Error while deleting temp files!");
					Log.error(e.getMessage());
				} catch (Exception e) {
					controller.displayErrorAndWait(e, "Error while deleting temp files!");
					Log.error(e, e.getMessage());
				}
		    }
		};


		/**
		 * check changes on the UI preferences
		 */
		private boolean isUIChanged(){
			boolean stateChanged = false;
			if(isDirty()){
				 stateChanged = true;
			}
			return stateChanged;
		}
		
		//get the changes from the UI and update the model
		private void updatePreferencesModel(){
			if(audioGroup != null && audioGroup.isDirty()){
				
				int selected= ((ObjectChoiceField)audioGroup).getSelectedIndex();
	        	if(selected != -1) {
	        		String choice=(String)((ObjectChoiceField)audioGroup).getChoice(selected);
	        		if(!StringUtils.equalsIgnoreCase(choice, mPrefs.getAudioEncoding())){
	        		 mPrefs.setAudioEncoding(choice);
	        		}
	        	}
			}
			if(videoGroup != null && videoGroup.isDirty()){
				int selected= ((ObjectChoiceField)videoGroup).getSelectedIndex();
	        	if(selected != -1) {
	        		String choice=(String)((ObjectChoiceField)videoGroup).getChoice(selected);
	        		if(!StringUtils.equalsIgnoreCase(choice, mPrefs.getVideoEncoding())){
	        		 mPrefs.setVideoEncoding(choice);
	        		}
	        	}
				
			}
			if(photoGroup != null && photoGroup.isDirty() ){
				int selected= ((ObjectChoiceField)photoGroup).getSelectedIndex();
	        	if(selected != -1) {
	        		String choice=(String)((ObjectChoiceField)photoGroup).getChoice(selected);
	        		if(!StringUtils.equalsIgnoreCase(choice, mPrefs.getPhotoEncoding())){
	        		 mPrefs.setPhotoEncoding(choice);
	        		}
	        	}
			}
			
			if(userConnectionEnabledField.isDirty()) {
				mPrefs.setUserConnectionOptionsEnabled(userConnectionEnabledField.getChecked());
			}
			
			if(_username.isDirty()) {
				if( _username.getText().trim().equals("") )
					mPrefs.setUsername(null);
				else
					mPrefs.setUsername(_username.getText().trim());
			}
			
			if(_password.isDirty()) {
				if( _password.getText().trim().equals("") )
					mPrefs.setPassword(null);
				else
					mPrefs.setPassword(_password.getText().trim());
			}
			
			if(_gateway.isDirty()) {
				if( _gateway.getText().trim().equals("") )
					mPrefs.setGateway(null);
				else
				 mPrefs.setGateway(_gateway.getText().trim());
			}
			if( _gatewayPort.isDirty()) {
				if( _gatewayPort.getText().trim().equals("") )
					mPrefs.setGatewayPort(null);
				else
				 mPrefs.setGatewayPort(_gatewayPort.getText().trim());
			}
			if(_apn.isDirty()) {
				if( _apn.getText().trim().equals("") )
					mPrefs.setApn(null);
				else
				 mPrefs.setApn(_apn.getText().trim());
			}
			if(_sourceIP.isDirty()) {
				if( _sourceIP.getText().trim().equals("") )
					mPrefs.setSourceIP(null);
				else
				 mPrefs.setSourceIP(_sourceIP.getText().trim());
			}
			if(_sourcePort.isDirty()) {
				if( _sourcePort.getText().trim().equals("") )
					mPrefs.setSourcePort(null);
				else
				 mPrefs.setSourcePort(_sourcePort.getText().trim());
			}
			
			mPrefs.setUserConnectionWap(userConnectionWapTypeField.getChecked());
			mPrefs.setTcpConnectionPermitted(_userAllowTCP.getChecked());
			mPrefs.setBESConnectionPermitted(_userAllowBES.getChecked());
			mPrefs.setServiceBookConnectionPermitted(_userAllowWAP2.getChecked());
			if ( ConnectionUtils.isWifiAvailable() ) {
				mPrefs.setWiFiConnectionPermitted(_userAllowWiFi.getChecked());
			}
			
			mPrefs.setBlackBerryInternetServicePermitted(_userAllowBIS.getChecked());
			
			//enable/disable debug mode
			if(_debugMode.getChecked()) {
				WordPressCore.getInstance().getFileAppender().setLogLevel(Log.TRACE);
				Log.debug("File Appender Log level is now on TRACE");
				mPrefs.setDebugMode(true);
			} else {
				WordPressCore.getInstance().getFileAppender().setLogLevel(Log.ERROR);
				Log.debug("File Appender Log level is now on DEBUG");
				mPrefs.setDebugMode(false);
			}
			
			//startup features
			mPrefs.setAutoStartup(autoStartup.getChecked());
			mPrefs.setBackgroundOnClose(backgroundOnClose.getChecked());
			
			//GPS options
			mPrefs.setGPSSettings(rgrp.getSelectedIndex());			
			
			updateStorageMode();
			setDirty(false);
		}

		private void updateStorageMode(){
			if(JSR75FileSystem.supportMicroSD() && JSR75FileSystem.hasMicroSD() && storageOpt != null) {
				int selectedStorage = storageOpt.getSelectedIndex(); //get the selected storage
				Log.trace("storage mode selected: "+ selectedStorage);
				int prevStorage = 0;
				
				try {
					if(AppDAO.SD_STORE_PATH.equals(AppDAO.getBaseDirPath())) 
						prevStorage = 1;
					
					if(prevStorage == selectedStorage) {
						Log.trace("no storage folder was changed");
					} else {
						Log.trace("storage folder was changed");
						Log.trace("closing log file");
						
						FileAppender fileAppender = WordPressCore.getInstance().getFileAppender();
						Log.removeAppender(fileAppender);
						fileAppender.close();
						
						if(selectedStorage == 1)
							AppDAO.setBaseDirPath(AppDAO.SD_STORE_PATH);
						else
							AppDAO.setBaseDirPath(BaseDAO.DEVICE_STORE_PATH);
						
						AppDAO.setUpFolderStructure();
						//recreate the folders structure if missing
						try {
							Vector applicationBlogs = WordPressCore.getInstance().getApplicationBlogs();
							BlogDAO.setUpFolderStructureForBlogs(applicationBlogs);
						} catch (Exception e) {
							Log.error(e, "Error while creating tmp directories for blogs");
						}
						
						fileAppender.open(); //reopen the log file
						Log.addAppender(fileAppender);
					}	
				} catch (RecordStoreException e) {
					Log.error(e, "Error upgrading Storage location");
					controller.displayErrorAndWait(e, "Error while changing the temp file path.");
					return;
				} catch (IOException e) {
					Log.error(e, "Error upgrading Storage location");
					controller.displayErrorAndWait(e, "Error while changing the temp file path.");
					return;
				}
			}
		}
		
		public boolean onClose()   {
			
			if(!isUIChanged()) {
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
	    		//get the changes from the UI and update the model
	    		updatePreferencesModel();
	    		controller.savePrefAndBack();
	    		return true;
	    	} else {
	    		Log.trace("user has selected cancel");
	    		return false;
	    	}
	    }
		
		public BaseController getController() {
			return controller;
		}
}