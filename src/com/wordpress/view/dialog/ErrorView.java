package com.wordpress.view.dialog;

import java.io.IOException;

import com.wordpress.bb.SSLPostingException;
import com.wordpress.bb.WordPressInfo;
import com.wordpress.bb.WordPressResource;
import com.wordpress.utils.PropertyUtils;
import com.wordpress.utils.Tools;
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


	//The String message already contains e.getMessage()
	public ErrorView(final String message, final Exception e) {
		super(Dialog.D_OK, message, Dialog.OK, Bitmap.getPredefinedBitmap(Bitmap.EXCLAMATION), Dialog.GLOBAL_STATUS);
		ResourceBundle _resources =  ResourceBundle.getBundle(WordPressResource.BUNDLE_ID, WordPressResource.BUNDLE_NAME);
		net.rim.device.api.ui.Manager delegate = getDelegate();

		if( ! (delegate instanceof DialogFieldManager) ) return; //Just to make sure everything is ok with the UI. Don't think this will never happen.
		DialogFieldManager dfm = (DialogFieldManager)delegate;
		net.rim.device.api.ui.Manager manager = dfm.getCustomManager();
		if( manager == null || e == null ) return;

		final String solutionURL = this.getFAQLink( e );
		//Check if we have an FAQ entry for this Exception on the .org site.
		if ( solutionURL != null ){
			ButtonField reportIssueBtnField = new ButtonField( _resources.getString( WordPressResource.BUTTON_READ_SOLUTION ));
			reportIssueBtnField.setChangeListener(new FieldChangeListener() {
				public void fieldChanged(Field field, int context) {
					Tools.openURL( solutionURL );
					close();
				}
			});
			manager.insert(reportIssueBtnField, manager.getFieldCount());
		} else {
			ButtonField reportIssueBtnField = new ButtonField( _resources.getString( WordPressResource.BUTTON_NEED_HELP ));
			reportIssueBtnField.setChangeListener(new FieldChangeListener() {
				public void fieldChanged(Field field, int context) {
					Tools.openURL( WordPressInfo.SUPPORT_FAQ_URL );
					close();
				}
			});
			manager.insert(reportIssueBtnField, manager.getFieldCount());
			/*There isn't a FAQ. Build the send to support email dialog
			ButtonField reportIssueBtnField = new ButtonField( _resources.getString( WordPressResource.MENUITEM_REPORT_ISSUE ));
			reportIssueBtnField.setChangeListener(new FieldChangeListener() {
				public void fieldChanged(Field field, int context) {

					String issueDetails = message;
					if( e != null ) {
						issueDetails = "["+ e.getClass().getName() + "] " + message;
					}

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
			manager.insert(reportIssueBtnField, manager.getFieldCount()); */
		}
	}
	
	private String getFAQLink( final Exception e ) {
		if ( e == null || e.getMessage() == null ) return null;

		String errorMessage = e.getMessage().toLowerCase(); 
		
		if (e instanceof net.rim.device.api.io.file.FileIOException) {
			Log.error("The error code of the IOException is: " + ( (net.rim.device.api.io.file.FileIOException) e ).getErrorCode() );
			Log.error("See RIM documentation to decode it :) ");
			if ( errorMessage.indexOf("not enough free memory on the file system to complete this") != -1 ) {
				return "http://blackberry.wordpress.org/faq/#faq_no_space_for_op";
			} else if ( errorMessage.indexOf( "(1003)" ) != -1 ) { //NO_SUCH_ROOT - is the device attached to the pc?
				return "http://blackberry.wordpress.org/faq/#faq_no_space_for_op";
			}
		} else if ( e instanceof IOException ) {
			if ( errorMessage.indexOf( "file system out of resources" ) != -1 || errorMessage.indexOf("not enough free memory on the file system to complete this") != -1 )
				return "http://blackberry.wordpress.org/faq/#faq_no_space_for_op";
			else if ( errorMessage.indexOf( "file system full error" ) != -1 )
				return "http://blackberry.wordpress.org/faq/#faq_8";
			else if ( errorMessage.indexOf( "APN is not specified" ) != -1 || errorMessage.indexOf( "BIS conn: null" ) != -1 || errorMessage.indexOf( "TCP conn" ) != -1 ){
				return "http://blackberry.wordpress.org/faq/#faq_12";
			}
		} else if (e instanceof SSLPostingException) {
			return "http://blackberry.wordpress.org/faq/#faq_2";
		} 
		return null;
	}
}