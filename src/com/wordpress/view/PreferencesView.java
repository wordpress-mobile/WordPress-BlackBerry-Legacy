package com.wordpress.view;

import java.io.IOException;

import javax.microedition.rms.RecordStoreException;

import net.rim.device.api.system.ApplicationDescriptor;
import net.rim.device.api.system.ApplicationManager;
import net.rim.device.api.ui.Field;
import net.rim.device.api.ui.FieldChangeListener;
import net.rim.device.api.ui.Font;
import net.rim.device.api.ui.Manager;
import net.rim.device.api.ui.MenuItem;
import net.rim.device.api.ui.component.ButtonField;
import net.rim.device.api.ui.component.CheckboxField;
import net.rim.device.api.ui.component.Dialog;
import net.rim.device.api.ui.component.EditField;
import net.rim.device.api.ui.component.LabelField;
import net.rim.device.api.ui.component.ObjectChoiceField;
import net.rim.device.api.ui.component.SeparatorField;
import net.rim.device.api.ui.container.HorizontalFieldManager;
import net.rim.device.api.ui.text.URLTextFilter;

import com.wordpress.bb.WordPressResource;
import com.wordpress.controller.BaseController;
import com.wordpress.controller.PreferenceController;
import com.wordpress.io.AppDAO;
import com.wordpress.model.Preferences;
import com.wordpress.utils.MultimediaUtils;
import com.wordpress.utils.StringUtils;
import com.wordpress.utils.log.Log;
import com.wordpress.view.component.BorderedFieldManager;
import com.wordpress.view.dialog.DiscardChangeInquiryView;

public class PreferencesView extends BaseView {
	
    private PreferenceController controller= null;
    private Preferences mPrefs=Preferences.getIstance();
    
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
	private CheckboxField _userAllowWap;
	private CheckboxField _userAllowWiFi;
	private CheckboxField _userAllowTCP;
	private CheckboxField _userAllowWAP2;
	private CheckboxField _userAllowBES;

	
	 public PreferencesView(PreferenceController _preferencesController) {
	    	super(_resources.getString(WordPressResource.TITLE_PREFERENCES_VIEW));
	    	this.controller=_preferencesController;
	    	
            addMultimediaOption();
            addConnectionOptionsFields();
            addWapOptionsFields();           
            
            ButtonField buttonOK= new ButtonField(_resources.getString(WordPressResource.BUTTON_OK), ButtonField.CONSUME_CLICK);
            ButtonField buttonBACK= new ButtonField(_resources.getString(WordPressResource.BUTTON_BACK), ButtonField.CONSUME_CLICK);
    		buttonBACK.setChangeListener(listenerBackButton);
            buttonOK.setChangeListener(listenerOkButton);
            buttonsManager = new HorizontalFieldManager(Field.FIELD_HCENTER);
            buttonsManager.add(buttonOK);
    		buttonsManager.add(buttonBACK);
    		
    		this.add(new LabelField("", Field.NON_FOCUSABLE)); //space before buttons
    		add(buttonsManager); 
    		
            //reset app temp file option
            add(new SeparatorField());
			LabelField lblDescReset = getLabel(_resources.getString(WordPressResource.DESCRIPTION_REMOVE_TEMPFILE)); 
			Font fnt = this.getFont().derive(Font.ITALIC);
			lblDescReset.setFont(fnt);
			add(lblDescReset);
			ButtonField buttonReset= new ButtonField(_resources.getString(WordPressResource.BUTTON_REMOVE), ButtonField.CONSUME_CLICK);
			buttonReset.setChangeListener(listenerResetButton);
            add(buttonReset);

    		addMenuItem(_saveItem);
	 }

	 private void addConnectionOptionsFields(){

		 BorderedFieldManager optManager = new BorderedFieldManager(
				 Manager.NO_HORIZONTAL_SCROLL
				 | Manager.NO_VERTICAL_SCROLL
				 | BorderedFieldManager.BOTTOM_BORDER_NONE);
		 
         //row allow description
         LabelField lblDescReset = getLabel(_resources.getString(WordPressResource.OPTIONSSCREEN_ALLOW_DESC)); 
		 Font fnt = this.getFont().derive(Font.ITALIC);
		 lblDescReset.setFont(fnt);
		 optManager.add(lblDescReset);

		 _userAllowWiFi=new CheckboxField(_resources.getString(WordPressResource.OPTIONSSCREEN_ALLOW_WIFI), mPrefs.isWiFiConnectionPermitted());
		 optManager.add(_userAllowWiFi);
		 _userAllowTCP=new CheckboxField(_resources.getString(WordPressResource.OPTIONSSCREEN_ALLOW_TCP), mPrefs.isTcpConnectionPermitted());
		 optManager.add(_userAllowTCP);
		 _userAllowWap=new CheckboxField(_resources.getString(WordPressResource.OPTIONSSCREEN_ALLOW_WAP), mPrefs.isWapConnectionPermitted());
		 optManager.add(_userAllowWap);
		 _userAllowWAP2=new CheckboxField(_resources.getString(WordPressResource.OPTIONSSCREEN_ALLOW_WAP2), mPrefs.isServiceBookConnectionPermitted());
		 optManager.add(_userAllowWAP2);
		 _userAllowBES=new CheckboxField(_resources.getString(WordPressResource.OPTIONSSCREEN_ALLOW_BES), mPrefs.isBESConnectionPermitted());
		 optManager.add(_userAllowBES);
		 
		 add(optManager);
	 }

	 
	 
	 private void addWapOptionsFields(){
		 
		 BorderedFieldManager optManager = new BorderedFieldManager(
				 Manager.NO_HORIZONTAL_SCROLL
				 | Manager.NO_VERTICAL_SCROLL
				 | BorderedFieldManager.BOTTOM_BORDER_NONE);
		 
	      //row allow description
         LabelField lblDescReset = getLabel(_resources.getString(WordPressResource.OPTIONSSCREEN_USERDEFINEDCONN_DESC)); 
		 Font fnt = this.getFont().derive(Font.ITALIC);
		 lblDescReset.setFont(fnt);
		 optManager.add(lblDescReset);
		 
		 userConnectionEnabledField=new CheckboxField(_resources.getString(WordPressResource.OPTIONSSCREEN_LABEL_ENABLED), mPrefs.isUserConnectionOptionsEnabled());
		 optManager.add(userConnectionEnabledField);
		 
         //row _apn
         HorizontalFieldManager rowAPN = new HorizontalFieldManager();
         rowAPN.add( getLabel(_resources.getString(WordPressResource.OPTIONSSCREEN_LABEL_APN)) ); 
         _apn = new EditField("", mPrefs.getApn());
         rowAPN.add(_apn);
         optManager.add(rowAPN);
         
         //row _username
         HorizontalFieldManager rowUserName = new HorizontalFieldManager();
         rowUserName.add( getLabel(_resources.getString(WordPressResource.LABEL_USERNAME)) ); 
         _username = new EditField("", mPrefs.getUsername());
         rowUserName.add(_username);
         optManager.add(rowUserName);
         
         //row _password
         HorizontalFieldManager rowPass = new HorizontalFieldManager();
         rowPass.add( getLabel(_resources.getString(WordPressResource.LABEL_PASSWD)) ); 
         _password = new EditField("", mPrefs.getPassword());
         rowPass.add(_password);
         optManager.add(rowPass);

         //row is wap connection?
         userConnectionWapTypeField=new CheckboxField(_resources.getString(WordPressResource.OPTIONSSCREEN_LABEL_ISWAP), mPrefs.isUserConnectionWap());
         optManager.add(userConnectionWapTypeField);
         
         //row _gateway IP
         HorizontalFieldManager rowGTW = new HorizontalFieldManager();
         rowGTW.add( getLabel(_resources.getString(WordPressResource.OPTIONSSCREEN_LABEL_GWAY)) ); 
         _gateway = new EditField("", mPrefs.getGateway(), 100, Field.EDITABLE);
         _gateway.setFilter(new URLTextFilter());
         rowGTW.add(_gateway);
         optManager.add(rowGTW);
         
         //row _gatewayPort
         HorizontalFieldManager rowGTWport = new HorizontalFieldManager();
         rowGTWport.add( getLabel(_resources.getString(WordPressResource.OPTIONSSCREEN_LABEL_GWAYPORT)) ); 
         _gatewayPort = new EditField("", mPrefs.getGatewayPort(), EditField.DEFAULT_MAXCHARS, EditField.FILTER_INTEGER);
         rowGTWport.add(_gatewayPort);
         optManager.add(rowGTWport);
         
         //row _sourcePort
         HorizontalFieldManager rowSourcePort = new HorizontalFieldManager();
         rowSourcePort.add( getLabel(_resources.getString(WordPressResource.OPTIONSSCREEN_LABEL_SRCPORT)) ); 
         _sourcePort = new EditField("", mPrefs.getSourcePort(), EditField.DEFAULT_MAXCHARS, EditField.FILTER_INTEGER);
         rowSourcePort.add(_sourcePort);
         optManager.add(rowSourcePort);
         
         //row _sourceIP
         HorizontalFieldManager rowSourceIP = new HorizontalFieldManager();
         rowSourceIP.add( getLabel(_resources.getString(WordPressResource.OPTIONSSCREEN_LABEL_SRCIP)) ); 
         _sourceIP = new EditField("", mPrefs.getSourceIP());
         rowSourceIP.add(_sourceIP);
         optManager.add(rowSourceIP);
         
         add(optManager);
         
	 }
	 
	 
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
			 String[] lines=MultimediaUtils.getSupportedVideoFormat();
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
	 
	 
	 
	 
	 //create a menu item for users click to save
	    private MenuItem _saveItem = new MenuItem( _resources, WordPressResource.MENUITEM_SAVE, 1000, 10) {
	        public void run() {
	        	updateDataModel();
	        	controller.savePrefAndBack();
	        }
	    };

	    
		private FieldChangeListener listenerOkButton = new FieldChangeListener() {
		    public void fieldChanged(Field field, int context) {
		    	updateDataModel();
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
					AppDAO.setUpFolderStructure();
					controller.displayMessage(_resources.getString(WordPressResource.MESSAGE_APP_RESTART));
					Log.debug("Called application restart...");

			        //Get the current application descriptor.
			        ApplicationDescriptor current = ApplicationDescriptor.currentApplicationDescriptor();
			        Log.debug("Scheduling the restart immediately...");
			        controller.stopAllThread(); //stop bg thread
			        ApplicationManager.getApplicationManager().scheduleApplication(current, System.currentTimeMillis() + 3000, true);
			        Log.debug("Application is exiting...");
			        System.exit(0);
				} catch (RecordStoreException e) {
					Log.error(e.getMessage());
				} catch (IOException e) {
					Log.error(e.getMessage());
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

			
		/*	if(audioGroup != null){
				if(audioGroup.isDirty()) {
					stateChanged = true;
	        	}
			}
			if(videoGroup != null){
				if(videoGroup.isDirty()) {
					stateChanged = true;
	        	}
			}
			if(photoGroup != null){
				if(photoGroup.isDirty()) {
					stateChanged = true;
        		}
			}
			if(_username.isDirty() || _password.isDirty() || userConnectionEnabledField.isDirty()
					|| _gateway.isDirty() ||  _gatewayPort.isDirty() || _apn.isDirty()
					|| _sourceIP.isDirty() || _sourcePort.isDirty() 
					|| userConnectionWapTypeField.isDirty()
					|| _userAllowWap.isDirty() 
					|| _userAllowWiFi.isDirty()
					|| _userAllowTCP.isDirty()
					|| _userAllowWAP2.isDirty()
					|| _userAllowBES.isDirty()
			) {
				 stateChanged = true;
			}
			
			*/
			
			return stateChanged;
		}
		
		//get the changes from the UI and update the model
		private void updateDataModel(){
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
			mPrefs.setWiFiConnectionPermitted(_userAllowWiFi.getChecked());
			mPrefs.setWapConnectionPermitted(_userAllowWap.getChecked());
			
			
			setDirty(false);
		}
    
	    //override onClose() to display a dialog box when the application is closed    
		public boolean onClose()   {
			
			if(!isUIChanged()) {
				controller.backCmd();
				return true;
			} 	
			
			String quest="Changes Made!";
	    	DiscardChangeInquiryView infoView= new DiscardChangeInquiryView(quest);
	    	int choice=infoView.doModal();    	 
	    	
	    	if(Dialog.DISCARD == choice) {
	    		System.out.println("la scelta dell'utente è discard");
	    		controller.backCmd();
	    		return true;
	    	}else if(Dialog.SAVE == choice) {
	    		System.out.println("la scelta dell'utente è save");
	    		//get the changes from the UI and update the model
	    		updateDataModel();
	    		controller.savePrefAndBack();
	    		return true;
	    	} else {
	    		System.out.println("la scelta dell'utente è cancel");
	    		return false;
	    	}
	    }
		
		public BaseController getController() {
			return controller;
		}
}