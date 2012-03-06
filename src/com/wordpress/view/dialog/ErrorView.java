package com.wordpress.view.dialog;

import com.wordpress.bb.WordPressInfo;
import com.wordpress.bb.WordPressResource;
import com.wordpress.utils.PropertyUtils;
import com.wordpress.utils.log.Log;

import net.rim.blackberry.api.invoke.Invoke;
import net.rim.blackberry.api.invoke.MessageArguments;
import net.rim.blackberry.api.mail.Address;
import net.rim.blackberry.api.mail.Message;
import net.rim.device.api.i18n.ResourceBundle;
import net.rim.device.api.system.Bitmap;
import net.rim.device.api.system.DeviceInfo;
import net.rim.device.api.system.RadioInfo;
import net.rim.device.api.ui.Field;
import net.rim.device.api.ui.FieldChangeListener;
import net.rim.device.api.ui.component.ButtonField;
import net.rim.device.api.ui.component.Dialog;
import net.rim.device.api.ui.container.DialogFieldManager;

public class ErrorView extends Dialog {

	/**
	 * Use this dialog boxes to provide feedback about an Error. It include only an OK button. 
	 * 
	 * @param message
	 */
	public ErrorView(String message) {
		super(Dialog.D_OK, message, Dialog.OK, Bitmap.getPredefinedBitmap(Bitmap.EXCLAMATION), Dialog.GLOBAL_STATUS);
	}


	public ErrorView(final String message, final Exception e) {
		super(Dialog.D_OK, message, Dialog.OK, Bitmap.getPredefinedBitmap(Bitmap.EXCLAMATION), Dialog.GLOBAL_STATUS);
		ResourceBundle _resources =  ResourceBundle.getBundle(WordPressResource.BUNDLE_ID, WordPressResource.BUNDLE_NAME);
		net.rim.device.api.ui.Manager delegate = getDelegate();
		if( delegate instanceof DialogFieldManager){
			DialogFieldManager dfm = (DialogFieldManager)delegate;
			net.rim.device.api.ui.Manager manager = dfm.getCustomManager();
			if( manager != null && e != null ){
				ButtonField reportIssueBtnField = new ButtonField( _resources.getString( WordPressResource.MENUITEM_REPORT_ISSUE ));
				reportIssueBtnField.setChangeListener(new FieldChangeListener() {
					public void fieldChanged(Field field, int context) {
						
						String errorMessageToCopy = null;
						if( e != null ) {
							if (e.getMessage()!= null)
								errorMessageToCopy= message + " - " + e.getMessage();
						}
						String issueDetails = "["+ e.getClass().getName() + "] " + errorMessageToCopy;
						
						try{
	    					Message m = new Message();
	    					Address a = new Address(WordPressInfo.ISSUE_REPORT_EMAIL_ADDRESS, "WordPress Support Team");
	    					Address[] addresses = {a};
	    					m.addRecipients(net.rim.blackberry.api.mail.Message.RecipientType.TO, addresses);

	    					String manufacturer = "Manufacturer: " + 
	    					(DeviceInfo.getManufacturerName() == null ? " n.a." : DeviceInfo.getManufacturerName());
	    					String deviceName =  "Device Name: " + (DeviceInfo.getDeviceName() == null ? " n.a." : DeviceInfo.getDeviceName()); 
	    					String deviceSoftwareVersion = "Software Version (OS version): " + (DeviceInfo.getSoftwareVersion() == null ? " n.a." : DeviceInfo.getSoftwareVersion());
	    					String platformVersion = "Platform Version: " + (DeviceInfo.getPlatformVersion() == null ? " n.a." : DeviceInfo.getPlatformVersion()); 
	    					String currentNetworkName = "Network Name: " + (RadioInfo.getCurrentNetworkName() == null ? " n.a." : RadioInfo.getCurrentNetworkName());

	    					StringBuffer mailContent = new StringBuffer();
	    					mailContent.append("App Version: "+PropertyUtils.getIstance().getAppVersion()+ "\n");

	    					mailContent.append(manufacturer + "\n");
	    					mailContent.append(deviceName + "\n");
	    					mailContent.append(deviceSoftwareVersion + "\n");
	    					mailContent.append(platformVersion + "\n" );
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
	    					m.setContent( mailContent.toString()+ " \n\n ----- Stack Trace ----- \n\n" + issueDetails );
	    					m.setSubject("WordPress for BlackBerry Issue Report");
	    					Invoke.invokeApplication(Invoke.APP_TYPE_MESSAGES, new MessageArguments(m));
	    				} catch (Exception e) {
	    					Log.error(e, "Problem invoking BlackBerry Mail App");
	    					//Do not show any error here!
	    				}
						close();
					}
				});
				manager.insert(reportIssueBtnField, manager.getFieldCount());
			}
		}
	}
}
