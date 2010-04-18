package com.wordpress.location;

import javax.microedition.location.Criteria;
import javax.microedition.location.Location;
import javax.microedition.location.LocationException;
import javax.microedition.location.LocationProvider;

import net.rim.device.api.i18n.ResourceBundle;
import net.rim.device.api.ui.Screen;
import net.rim.device.api.ui.UiApplication;
import net.rim.device.api.ui.component.Dialog;
import net.rim.device.api.ui.component.DialogClosedListener;

import com.wordpress.bb.WordPress;
import com.wordpress.bb.WordPressCore;
import com.wordpress.utils.log.Log;
import com.wordpress.utils.observer.Observable;
import com.wordpress.view.dialog.ConnectionInProgressView;

public class Gps extends Observable implements Runnable {
	
	protected Thread t = null;
	protected LocationProvider locationProvider;
	protected int threadPriority = Thread.NORM_PRIORITY;
	protected boolean isWorking;
	protected ResourceBundle _resources = WordPressCore.getInstance().getResourceBundle();
	
	public void run() { 
		
		Log.debug("finding your position...");
		final ConnectionInProgressView connectionProgressView= new ConnectionInProgressView(_resources.getString(WordPress.MESSAGE_FINDING_YOUR_LOCATION));
		connectionProgressView.setDialogClosedListener(new GPSDialogClosedListener());
        
		UiApplication.getUiApplication().invokeAndWait(new Runnable() {
  			public void run() {
  			 	connectionProgressView.show();
  			}
  		});
        
		try {

			/*
			 * when using a getXXXCriteria... remeber that:
			 * 
			 * If no concrete LocationProvider could be created that typically 
			 * can match the defined criteria but there are other location providers not meeting 
			 * the criteria that could be returned for a more relaxed criteria, null is returned to indicate this
			 *  
			 */

			Log.debug("Trying to get the Assisted GPS...");
			Criteria criteria = getAssistedCriteria(Criteria.POWER_USAGE_MEDIUM);
			locationProvider = LocationProvider.getInstance(criteria);

			if ( locationProvider == null ) {
				Log.debug("assisted gps one is not available, trying to get the assisted gps with less power consumption");
				criteria = getAssistedCriteria(Criteria.NO_REQUIREMENT);
				locationProvider = LocationProvider.getInstance(criteria);
			}

			if ( locationProvider == null ) {
				Log.debug("assisted gps is not available, trying to get the autonomous gps with high consumption");
				criteria = getAutonomousPosCriteria(Criteria.POWER_USAGE_HIGH);
				locationProvider = LocationProvider.getInstance(criteria);
			}

			if ( locationProvider == null ) {
				Log.debug("autonomous gps with high consumption is not available, trying to get the autonomous gps with medium consumption");
				criteria = getAutonomousPosCriteria(Criteria.POWER_USAGE_MEDIUM);
				locationProvider = LocationProvider.getInstance(criteria);
			}

			if ( locationProvider == null ) {
				Log.debug("autonomous gps with medium consumption is not available, trying to get the autonomous gps with no requirement on consumption");
				criteria = getAutonomousPosCriteria(Criteria.NO_REQUIREMENT);
				locationProvider = LocationProvider.getInstance(criteria);
			}

			if ( locationProvider == null ) {
				Log.debug("autonomous gps is not available, trying to get cell towers signals");
				criteria = getCellSiteCriteria();
				locationProvider = LocationProvider.getInstance(criteria);
			}
						
			if ( locationProvider == null ) {
				Log.debug("cell towers signals are not available, trying the default GPS");
				locationProvider = LocationProvider.getInstance(null);
			}
			
			if ( locationProvider == null ) {
				Log.error("No GPS  LocationProviders that meet our criteria are currently available");
				UiApplication.getUiApplication().invokeLater( showGpsUnsupportedDialog );  // Ask event-dispatcher thread to display dialog ASAP.
				return;
			}

		} catch (LocationException e) {
			//this Ex occurs when all LocationProviders are currently permanently unavailable
			Log.error(e, "All LocationProviders are currently permanently unavailable");
			UiApplication.getUiApplication().invokeLater( showGpsUnsupportedDialog );  // Ask event-dispatcher thread to display dialog ASAP.
			return;
		}
		
		//retrive the device position
		try {	
			
			final Location location = locationProvider.getLocation(-1);
			double longitude = location.getQualifiedCoordinates().getLongitude();
			double latitude = location.getQualifiedCoordinates().getLatitude();
			Log.debug("latitude " + latitude+ " longitude "+ longitude);

			UiApplication.getUiApplication().invokeLater(new Runnable() {
				public void run() {
					Screen scr=UiApplication.getUiApplication().getActiveScreen();
					UiApplication.getUiApplication().popScreen(scr);	
				}
			});
			notifyObservers(location);

		} catch (LocationException e) {
			Log.error(e, "LocationException while getLocation()");
			UiApplication.getUiApplication().invokeLater(new Runnable() {
				public void run() {
					Screen scr=UiApplication.getUiApplication().getActiveScreen();
					UiApplication.getUiApplication().popScreen(scr);	
				}
			});
			notifyObservers(e);
		} catch (InterruptedException e) {
			Log.error(e, "InterruptedException while getLocation()");
			notifyObservers(e);
		} catch (Exception e) {
			UiApplication.getUiApplication().invokeLater(new Runnable() {
				public void run() {
					Screen scr=UiApplication.getUiApplication().getActiveScreen();
					UiApplication.getUiApplication().popScreen(scr);	
				}
			});
			notifyObservers(e);
		}
        Log.debug("location thread has finished"); 
	}
	
	
	
	Runnable showGpsUnsupportedDialog = new Runnable() 
	{
		public void run() {
			Screen scr=UiApplication.getUiApplication().getActiveScreen();
			 	UiApplication.getUiApplication().popScreen(scr);
			Dialog.alert(_resources.getString(WordPress.MESSAGE_GPS_DISABLED_NOT_SUPPORTED));
			notifyObservers(null);
		}
	};
	
	
	/**
	 * blocca il funzionamento della connessione immediatamente.
	 */
	public void stopLocation() {
		Log.debug("User requested stop the GPS connection");
		isWorking = false;
		try {
			t.interrupt();
		} catch (Exception e) {
			Log.error(e, "Error while interrupting GPS Thread");
		} finally {
			t = null;
			Log.trace("GPS Thread was set to null");
		}

		notifyObservers(null);
		
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
    	criteria.setHorizontalAccuracy(Criteria.NO_REQUIREMENT);
    	criteria.setVerticalAccuracy(Criteria.NO_REQUIREMENT);
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
    private Criteria getAutonomousPosCriteria(int powerConsumption) {
    	Criteria criteria = new Criteria();
    	criteria.setHorizontalAccuracy(Criteria.NO_REQUIREMENT);
    	criteria.setVerticalAccuracy(Criteria.NO_REQUIREMENT);
    	criteria.setCostAllowed(false);
    	criteria.setPreferredPowerConsumption(powerConsumption);
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