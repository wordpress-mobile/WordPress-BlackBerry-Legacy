package com.wordpress.location;

import javax.microedition.location.Criteria;
import javax.microedition.location.Location;
import javax.microedition.location.LocationException;
import javax.microedition.location.LocationProvider;

import net.rim.device.api.ui.Screen;
import net.rim.device.api.ui.UiApplication;
import net.rim.device.api.ui.component.Dialog;
import net.rim.device.api.ui.component.DialogClosedListener;

import com.wordpress.utils.log.Log;
import com.wordpress.utils.observer.Observable;
import com.wordpress.view.dialog.ConnectionInProgressView;

public class Gps extends Observable implements Runnable {
	
	protected Thread t = null;
	protected LocationProvider locationProvider;
	protected static int _interval = 1;   // Seconds - this is the period of position query.
	protected int threadPriority = Thread.NORM_PRIORITY;
	protected boolean isWorking;
	
	public void run() {
		
		Log.debug("INIZIO");
		final ConnectionInProgressView connectionProgressView= new ConnectionInProgressView("trovando la tua posizione");
		connectionProgressView.setDialogClosedListener(new GPSDialogClosedListener());
        
		UiApplication.getUiApplication().invokeAndWait(new Runnable() {
  			public void run() {
  			 	connectionProgressView.show();
  			}
  		});
        
		try {
			
			Criteria criteria = getAssistedCriteria();
			
			locationProvider = LocationProvider.getInstance(criteria);
			if ( locationProvider == null ) {
				criteria = getCellSiteCriteria();
				locationProvider = LocationProvider.getInstance(criteria);
			}
			if ( locationProvider == null ) {

				Runnable showGpsUnsupportedDialog = new Runnable() 
				{
					public void run() {
						Screen scr=UiApplication.getUiApplication().getActiveScreen();
		 			 	UiApplication.getUiApplication().popScreen(scr);
						Dialog.alert("GPS is not supported on this platform, exiting...");
						notifyObservers(null);
					}
				};
				
				UiApplication.getUiApplication().invokeLater( showGpsUnsupportedDialog );  // Ask event-dispatcher thread to display dialog ASAP.
			} else {
				final Location location = locationProvider.getLocation(-1);
				double longitude = location.getQualifiedCoordinates().getLongitude();
	            double latitude = location.getQualifiedCoordinates().getLatitude();
	            Log.debug("latitude " + latitude+ " longitude "+ longitude);
	            
	            UiApplication.getUiApplication().invokeLater(new Runnable() {
	            	public void run() {
	            		Screen scr=UiApplication.getUiApplication().getActiveScreen();
	            		UiApplication.getUiApplication().popScreen(scr);	
	            		notifyObservers(location);
	            	}
	            });
			
			}
		} catch (LocationException e) {
			Log.error(e, "Error while interrupting GPS");
			notifyObservers(e);
		} catch (InterruptedException e) {
			Log.error(e, "Error while interrupting GPS");
			notifyObservers(e);
		}
        Log.debug("FINE"); 
	}
	
	
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
    private Criteria getAssistedCriteria() {
    	Criteria criteria = new Criteria();
    	criteria.setHorizontalAccuracy(Criteria.NO_REQUIREMENT);
    	criteria.setVerticalAccuracy(Criteria.NO_REQUIREMENT);
    	criteria.setCostAllowed(true);
    	criteria.setPreferredPowerConsumption(Criteria.POWER_USAGE_HIGH);
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
    	criteria.setHorizontalAccuracy(Criteria.NO_REQUIREMENT);
    	criteria.setVerticalAccuracy(Criteria.NO_REQUIREMENT);
    	criteria.setCostAllowed(false);
    	criteria.setPreferredPowerConsumption(Criteria.POWER_USAGE_HIGH);
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