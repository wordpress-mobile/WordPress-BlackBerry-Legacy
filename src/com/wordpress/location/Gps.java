package com.wordpress.location;

import javax.microedition.location.Criteria;
import javax.microedition.location.Location;
import javax.microedition.location.LocationException;
import javax.microedition.location.LocationListener;
import javax.microedition.location.LocationProvider;

import net.rim.device.api.ui.component.Dialog;

public class Gps {
	
    private LocationProvider _locationProvider;
    private static int _interval = 1;   // Seconds - this is the period of position query.

    /**
     * assisted: Use this mode to get location information from satellites using a PDE. This mode allows a BlackBerry device
     * application to retrieve location information faster than the autonomous mode and more accurately than the cell site mode.
     * To use this mode requires wireless network coverage, and the BlackBerry device and the wireless service provider must
     * support this mode.
     */
    private void findPositionUsingPDE() {
    	Criteria criteria = new Criteria();
    	criteria.setHorizontalAccuracy(Criteria.NO_REQUIREMENT);
    	criteria.setVerticalAccuracy(Criteria.NO_REQUIREMENT);
    	criteria.setCostAllowed(true);
    	criteria.setPreferredPowerConsumption(Criteria.POWER_USAGE_HIGH);
    }
    
    /**
     * autonomous: Use this mode to get location information from the GPS receiver on the BlackBerry device without assistance
     * from the wireless network. This mode allows a BlackBerry device application to retrieve location information that has highaccuracy,
     * and does not require assistance from the wireless network. However, the speed at which this mode retrieves
     * location information is slower than the other modes.
     */
    private void findPositionAutonomous() {
    	Criteria criteria = new Criteria();
    	criteria.setHorizontalAccuracy(Criteria.NO_REQUIREMENT);
    	criteria.setVerticalAccuracy(Criteria.NO_REQUIREMENT);
    	criteria.setCostAllowed(false);
    	criteria.setPreferredPowerConsumption(Criteria.POWER_USAGE_HIGH);
    }
    
    /**
     * cell site: Use this mode to get location information from cell site towers. This mode allows a BlackBerry device application
     * retrieve location information faster than the assisted and autonomous modes. However, the accuracy of the location
     * information is low-level and does not provide tracking information such as speed or route information. Using this mode
     * requires wireless network coverage and that both the BlackBerry device and the wireless service provider support this mode.
     */
    private void findPositionCellSite() {
    	Criteria criteria = new Criteria();
    	criteria.setHorizontalAccuracy(Criteria.NO_REQUIREMENT);
    	criteria.setVerticalAccuracy(Criteria.NO_REQUIREMENT);
    	criteria.setCostAllowed(true);
    	criteria.setPreferredPowerConsumption(Criteria.POWER_USAGE_LOW);
    }
    
    public void findMyPosition() {
    	Criteria criteria = new Criteria();
    	criteria.setCostAllowed(true);
    	criteria.setPreferredPowerConsumption(Criteria.POWER_USAGE_HIGH);
    	//In a non-event thread, invoke
    	try {
			LocationProvider provider = LocationProvider.getInstance(criteria);
			
			  if ( provider == null )
	            {
	                // We would like to display a dialog box indicating that GPS isn't supported, 
	                // but because the event-dispatcher thread hasn't been started yet, modal 
	                // screens cannot be pushed onto the display stack.  So delay this operation
	                // until the event-dispatcher thread is running by asking it to invoke the 
	                // following Runnable object as soon as it can.
	                Runnable showGpsUnsupportedDialog = new Runnable() 
	                {
	                    public void run() {
	                        Dialog.alert("GPS is not supported on this platform, exiting...");
	                    }
	                };
	                
	               // invokeLater( showGpsUnsupportedDialog );  // Ask event-dispatcher thread to display dialog ASAP.
	            }
			
		} catch (LocationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    	
    	
    }
    
    
	/**
     * Invokes the Location API with the default criteria.
     * 
     * @return True if the Location Provider was successfully started; false otherwise.
     */
    private boolean startLocationUpdate()
    {
        boolean retval = false;
        
        try
        {
            _locationProvider = LocationProvider.getInstance(null);
            
            if ( _locationProvider == null )
            {
                // We would like to display a dialog box indicating that GPS isn't supported, 
                // but because the event-dispatcher thread hasn't been started yet, modal 
                // screens cannot be pushed onto the display stack.  So delay this operation
                // until the event-dispatcher thread is running by asking it to invoke the 
                // following Runnable object as soon as it can.
                Runnable showGpsUnsupportedDialog = new Runnable() 
                {
                    public void run() {
                        Dialog.alert("GPS is not supported on this platform, exiting...");
                    }
                };
                
               // invokeLater( showGpsUnsupportedDialog );  // Ask event-dispatcher thread to display dialog ASAP.
            }
            else
            {
                // Only a single listener can be associated with a provider, and unsetting it 
                // involves the same call but with null, therefore, no need to cache the listener
                // instance request an update every second.
                _locationProvider.setLocationListener(new LocationListenerImpl(), _interval, 1, 1);
                retval = true;
            }
        }
        catch (LocationException le)
        {
            System.err.println("Failed to instantiate the LocationProvider object, exiting...");
            System.err.println(le); 
        }        
        return retval;
    }
	
	
	
	  /**
     * Implementation of the LocationListener interface.
     */
    private class LocationListenerImpl implements LocationListener
    {
        /**
         * @see javax.microedition.location.LocationListener#locationUpdated(LocationProvider,Location)
         */
        public void locationUpdated(LocationProvider provider, Location location)
        {
            if(location.isValid())
            {
                float heading = location.getCourse();
                double longitude = location.getQualifiedCoordinates().getLongitude();
                double latitude = location.getQualifiedCoordinates().getLatitude();
                float altitude = location.getQualifiedCoordinates().getAltitude();
                float speed = location.getSpeed();
                //aggiornare le infomazioni qui
            }
        }
  
        public void providerStateChanged(LocationProvider provider, int newState)
        {
            // Not implemented.
        }        
    }
}