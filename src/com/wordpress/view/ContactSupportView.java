package com.wordpress.view;

import net.rim.blackberry.api.invoke.Invoke;
import net.rim.blackberry.api.invoke.MessageArguments;
import net.rim.blackberry.api.mail.Address;
import net.rim.blackberry.api.mail.Message;
import net.rim.device.api.system.DeviceInfo;
import net.rim.device.api.system.RadioInfo;
import net.rim.device.api.ui.Field;
import net.rim.device.api.ui.FieldChangeListener;
import net.rim.device.api.ui.component.BasicEditField;
import net.rim.device.api.ui.component.ButtonField;
import net.rim.device.api.ui.component.LabelField;
import net.rim.device.api.ui.component.Menu;

import com.wordpress.bb.WordPressInfo;
import com.wordpress.bb.WordPressResource;
import com.wordpress.controller.BaseController;
import com.wordpress.controller.MainController;
import com.wordpress.utils.PropertyUtils;
import com.wordpress.utils.Tools;
import com.wordpress.utils.log.Log;
import com.wordpress.view.component.BaseButtonField;

public class ContactSupportView extends BaseView {
	private LabelField urlAddr;
	private BaseController mainController;

    public ContactSupportView(MainController controller, boolean isWPORGSite) {
    	
    	super(_resources.getString(WordPressResource.LABEL_CONTACT_SUPPORT) + " - " + (isWPORGSite == true ? "WordPress.org" : "WordPress.com"));
    	this.mainController = controller;
    	
    	if(isWPORGSite) {
    		addWPORGFields();
    	} else {
    		addWPCOMFields();
    	}
    	controller.bumpScreenViewStats("com/wordpress/view/ContactSupportView", "Contact Support Screen", "", null, "");
    }
    
    private void addWPORGFields(){
    	BasicEditField forumsField = new BasicEditField(BasicEditField.READONLY);
    	forumsField.setText(_resources.getString(WordPressResource.SUPPORT_WPORG_SITES));
    	forumsField.setMargin(10, 10, 0, 10);
    	add(forumsField);
    	urlAddr = GUIFactory.createURLLabelField("http://blackberry.forums.wordpress.org", "http://blackberry.forums.wordpress.org/", LabelField.FOCUSABLE);
    	urlAddr.setMargin(0, 10, 10, 10);
    	add(urlAddr);
    	
       	BasicEditField faqField = new BasicEditField(BasicEditField.READONLY);
    	faqField.setText(_resources.getString(WordPressResource.SUPPORT_WPORG_SITES_FAQ));
    	faqField.setMargin(10, 10, 0, 10);
    	add(faqField);
    	urlAddr = GUIFactory.createURLLabelField("http://blackberry.wordpress.org/faq/", "http://blackberry.wordpress.org/faq/", LabelField.FOCUSABLE);
    	urlAddr.setMargin(0, 10, 5, 10);
    	add(urlAddr); 	
    }
    
    private void addWPCOMFields() {
    	BasicEditField mainTextField = new BasicEditField(BasicEditField.READONLY);
    	mainTextField.setText(_resources.getString(WordPressResource.SUPPORT_DESC_1));
    	mainTextField.setMargin(10, 10, 10, 10);
    	add(mainTextField);
    	
    	BasicEditField faqField = new BasicEditField(BasicEditField.READONLY);
    	faqField.setText("- " + _resources.getString(WordPressResource.SUPPORT_FAQ));
    	faqField.setMargin(0, 10, 0, 10);
    	add(faqField);
    	urlAddr = GUIFactory.createURLLabelField("http://blackberry.wordpress.org/faq/", "http://blackberry.wordpress.org/faq/", LabelField.FOCUSABLE);
    	urlAddr.setMargin(0, 10, 5, 10);
    	add(urlAddr);
    	
    	BasicEditField forumsField = new BasicEditField(BasicEditField.READONLY);
    	forumsField.setText("- " + _resources.getString(WordPressResource.SUPPORT_FORUM));
    	forumsField.setMargin(0, 10, 0, 10);
    	add(forumsField);
    	urlAddr = GUIFactory.createURLLabelField("http://blackberry.forums.wordpress.org", "http://blackberry.forums.wordpress.org/", LabelField.FOCUSABLE);
    	urlAddr.setMargin(0, 10, 0, 10);
    	add(urlAddr);
    	
    	BasicEditField footerField = new BasicEditField(BasicEditField.READONLY);
    	footerField.setText(_resources.getString(WordPressResource.SUPPORT_DESC_2));
    	footerField.setMargin(10, 10, 10, 10);
    	add(footerField);
    	
    	BaseButtonField buttonContact = GUIFactory.createButton(_resources.getString(WordPressResource.LABEL_CONTACT_SUPPORT), ButtonField.CONSUME_CLICK);
    	buttonContact.setMargin(0, 10, 10, 10);
    	
    	FieldChangeListener contactSupportChangeListener = new FieldChangeListener() {

			public void fieldChanged(Field field, int context) {
    			if(context == 0) {
    				try{
    					Message m = new Message();
    					Address a = new Address(WordPressInfo.SUPPORT_EMAIL_ADDRESS, "WordPress Support Team");
    					Address[] addresses = {a};
    					m.addRecipients(net.rim.blackberry.api.mail.Message.RecipientType.TO, addresses);

    					String manufacturer = "Manufacturer: " + 
    					(DeviceInfo.getManufacturerName() == null ? " n.a." : DeviceInfo.getManufacturerName());
    					String deviceName =  "Device Name: " + (DeviceInfo.getDeviceName() == null ? " n.a." : DeviceInfo.getDeviceName()); 
    					String deviceSoftwareVersion = "Software Version (OS Version): " + (DeviceInfo.getSoftwareVersion() == null ? " n.a." : DeviceInfo.getSoftwareVersion());
    					String platformVersion = "Platform Version: " + (DeviceInfo.getPlatformVersion() == null ? " n.a." : DeviceInfo.getPlatformVersion()); 
    					String currentNetworkName = "Network Name: " + (RadioInfo.getCurrentNetworkName() == null ? " n.a." : RadioInfo.getCurrentNetworkName());

    					StringBuffer mailContent = new StringBuffer();
    					mailContent.append("App Version: "+PropertyUtils.getIstance().getAppVersion()+ "\n");

    					
    					mailContent.append(manufacturer + "\n");
    					mailContent.append(deviceName + "\n");
    					mailContent.append(platformVersion + "\n" );
    					mailContent.append(deviceSoftwareVersion + "\n");
    					mailContent.append(currentNetworkName + "\n");
    					mailContent.append("\n");
    					mailContent.append("Note: After you send the email, use the Escape Key to return to the application.");
    					mailContent.append("\n");
    					mailContent.append("*** Fill out the form below: ***");
    					mailContent.append("\n");
    					mailContent.append("Site URL: \n\n");
    					mailContent.append("I did: \n\n");
    					mailContent.append("I saw: \n\n");
    					mailContent.append("I expected: \n\n");
    					m.setContent(mailContent.toString());
    					m.setSubject("WordPress for BlackBerry Bug Report");
    					Invoke.invokeApplication(Invoke.APP_TYPE_MESSAGES, new MessageArguments(m));
    				} catch (Exception e) {
    					Log.error(e, "Problem invoking BlackBerry Mail App");
    					mainController.displayError("Problem invoking BlackBerry Mail App");
    				}
    			}

    		}
    	};
    	
    	buttonContact.setChangeListener(contactSupportChangeListener);
    	add(buttonContact);
    }
    
	public boolean onMenu(int instance) {
		boolean result;
		// Prevent the context menu from being shown if focus
		// is on the url field 
		if ( instance == Menu.INSTANCE_CONTEXT 
				&& (getLeafFieldWithFocus() instanceof LabelField)) {
			result = false;
		} else {
			result = super.onMenu(instance);
		}
		return result;
	}

	public BaseController getController() {
		return mainController;
	}
}