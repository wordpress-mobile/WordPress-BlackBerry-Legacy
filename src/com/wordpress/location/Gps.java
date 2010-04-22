package com.wordpress.location;

import javax.microedition.location.Criteria;
import javax.microedition.location.Location;
import javax.microedition.location.LocationException;
import javax.microedition.location.LocationProvider;

import net.rim.device.api.i18n.ResourceBundle;
import net.rim.device.api.ui.UiApplication;
import net.rim.device.api.ui.component.Dialog;
import net.rim.device.api.ui.component.DialogClosedListener;

import com.wordpress.bb.WordPress;
import com.wordpress.bb.WordPressCore;
import com.wordpress.model.Preferences;
import com.wordpress.utils.log.Log;
import com.wordpress.utils.observer.Observable;
import com.wordpress.view.dialog.ConnectionInProgressView;
import com.wordpress.view.dialog.ErrorView;

public class Gps extends Observable implements Runnable {
	
	protected Thread t = null;
	protected LocationProvider locationProvider;
	protected int threadPriority = Thread.NORM_PRIORITY;
	protected boolean isWorking;
	protected ResourceBundle _resources = WordPressCore.getInstance().getResourceBundle();
	protected ConnectionInProgressView connectionProgressView;
	
	public void run() { 
		
		showGPSDialog();
        
		try {

			/*
			 * when using a getXXXCriteria... remeber that:
			 * 
			 * null is returned to indicate that no concrete LocationProvider could be created that 
			 * match the defined criteria, but there are other location providers for a more relaxed criteria
			 *  
			 */
			Criteria criteria;
			
			Preferences mPrefs = Preferences.getIstance();
			int providerIndex = mPrefs.getGPSSettings();

			switch ( providerIndex ) {
			case Preferences.GPS_ASSISTED:
				Log.debug("Trying to get the Assisted GPS...");
				criteria = getAssistedCriteria(Criteria.POWER_USAGE_MEDIUM);
				break;
			case Preferences.GPS_AUTONOMOUS:
				Log.debug("Trying to get the autonomous gps");
				criteria = getAutonomousPosCriteria();
				break;
			case Preferences.GPS_CELL_TOWER:
				Log.debug("Trying to get cell towers signals");
				criteria = getCellSiteCriteria();
				break;
			default:
				Log.debug("Trying to get GPS with default criteria");
				criteria = null;
				break;
			}

			Log.debug("acquiring location provider...");
			locationProvider = LocationProvider.getInstance(criteria);
			
			if ( locationProvider == null ) {
				Log.error("No GPS  LocationProviders that meet our criteria are currently available");
				showError(_resources.getString(WordPress.MESSAGE_GPS_NOT_AVAILABLE), null);
				return;
			}

			Log.debug("location provider acquired");

		} catch (LocationException e) {
			//this Ex occurs when all LocationProviders are currently permanently unavailable
			Log.error(e, "All LocationProviders are currently permanently unavailable");
			showError(_resources.getString(WordPress.MESSAGE_GPS_DISABLED_NOT_SUPPORTED), e); 
			return;
		} catch (Exception e) {
			Log.error(e, "Error while retriving LocationProviders");
			showError(_resources.getString(WordPress.MESSAGE_GPS_DISABLED_NOT_SUPPORTED), e);
			return;
		}
		
	/*	 try { Thread.sleep(5000); }
         catch (Exception ex) { }
		*/
		
		//retrive the device position
		try {
			
			final Location location = locationProvider.getLocation(120);
			double longitude = location.getQualifiedCoordinates().getLongitude();
			double latitude = location.getQualifiedCoordinates().getLatitude();
			Log.debug("latitude " + latitude+ " longitude "+ longitude);

			hideGPSDialog();
			notifyObservers(location);

		} catch (LocationException e) {
			Log.error(e, "LocationException while getLocation");
			showError("GPS Error ", e);
			return;
		} catch (InterruptedException e) {
			if(isWorking == true) {
				Log.error(e, "InterruptedException while getLocation");
				showError("GPS Error ", e); 
				}
			return;
		} catch (Exception e) {
			showError("GPS Error ", e);
			return;
		}
		
        Log.debug("location thread has finished"); 
	}


	private void showGPSDialog() {
		Log.debug("finding your position...");
		connectionProgressView = new ConnectionInProgressView(_resources.getString(WordPress.MESSAGE_FINDING_YOUR_LOCATION));
		connectionProgressView.setDialogClosedListener(new GPSDialogClosedListener());
        
		UiApplication.getUiApplication().invokeAndWait(new Runnable() {
  			public void run() {
  			 	connectionProgressView.show();
  			}
  		});
	}

	private synchronized void hideGPSDialog() {
		if(connectionProgressView != null && connectionProgressView.isDisplayed()) {
			UiApplication.getUiApplication().invokeLater(new Runnable() {
				public void run() {
					UiApplication.getUiApplication().popScreen(connectionProgressView);
				}
			});
		}
	}
	
	
	private synchronized void showError(final String msg, Exception e) {
	
		hideGPSDialog();
		final String errMessage;
		if(e != null && e.getMessage()!= null ) {
			errMessage = msg+"\n"+e.getMessage();
		} else
			errMessage = msg;
	
		UiApplication.getUiApplication().invokeLater(new Runnable() {
			public void run() {			
				ErrorView errView = new ErrorView(errMessage);
				errView.doModal();
			}
		});
		notifyObservers(null);
	}
		
	
	/**
	 * blocca il funzionamento della connessione immediatamente.
	 */
	public void stopLocation() {
		Log.debug("User requested stop the GPS connection");
		isWorking = false;
		
		notifyObservers(null);
		deleteObservers();
		
		try {
			t.interrupt();
		} catch (Exception e) {
			Log.error(e, "Error while interrupting GPS Thread");
		} finally {
			t = null;
			Log.trace("GPS Thread was set to null");
		}
		
		if ( locationProvider == null ) {
			locationProvider.reset();
		}
	}
	
	public void findMyPosition() {
		isWorking=true;
		t = new Thread(this);
		t.setPriority(threadPriority); //thread by default is set to priority normal
		t.start();
	}
	
    /**
     * assisted: Use this mode to get location information from satellites using a PDE. This mode allows a BlackBerry device
     * application to retrieve location information faster than the autonomous mode and more accurately than the cell site mode.
     * To use this mode requires wireless network coverage, and the BlackBerry device and the wireless service provider must
     * support this mode.
     */
    private Criteria getAssistedCriteria(int powerConsumption) {
    	Criteria criteria = new Criteria();
    	criteria.setHorizontalAccuracy(100);
    	criteria.setVerticalAccuracy(100);
    	criteria.setCostAllowed(true);
    	criteria.setPreferredPowerConsumption(powerConsumption);
    	return criteria;
    }
    
    
    /**
     * autonomous: Use this mode to get location information from the GPS receiver on the BlackBerry device without assistance
     * from the wireless network. This mode allows a BlackBerry device application to retrieve location information that has highaccuracy,
     * and does not require assistance from the wireless network. However, the speed at which this mode retrieves
     * location information is slower than the other modes.
     */
    private Criteria getAutonomousPosCriteria() {
    	Criteria criteria = new Criteria();
    	criteria.setCostAllowed(false);
    	return criteria;
    }
    
    /**
     * cell site: Use this mode to get location information from cell site towers. This mode allows a BlackBerry device application
     * retrieve location information faster than the assisted and autonomous modes. However, the accuracy of the location
     * information is low-level and does not provide tracking information such as speed or route information. Using this mode
     * requires wireless network coverage and that both the BlackBerry device and the wireless service provider support this mode.
     */
    private Criteria getCellSiteCriteria() {
    	Criteria criteria = new Criteria();
    	criteria.setHorizontalAccuracy(Criteria.NO_REQUIREMENT);
    	criteria.setVerticalAccuracy(Criteria.NO_REQUIREMENT);
    	criteria.setCostAllowed(true);
    	criteria.setPreferredPowerConsumption(Criteria.POWER_USAGE_LOW);
    	return criteria;
    }
    
	  
    /**
     * listener on the connection in progress dialog. used by controller.
     * @author dercoli
     *
     */
    private class GPSDialogClosedListener implements DialogClosedListener {
    	public int choice;
    	
    	public GPSDialogClosedListener(){
    		super();
    		Log.trace("Created the listener for the conn dialog");
    	}
    	
    	public void dialogClosed(Dialog dialog, int choice) {
    		Log.trace("Chiusura della conn dialog");	
    		this.choice = choice;
    		if(choice == Dialog.CANCEL) {
    			Log.trace("Chiusura della conn dialog tramite cancel");
    			stopLocation();
    		}
    	}
    }
}