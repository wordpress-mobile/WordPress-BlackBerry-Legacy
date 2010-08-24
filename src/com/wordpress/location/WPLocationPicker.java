//#preprocess

//#ifdef IS_OS50_OR_ABOVE

package com.wordpress.location;

import java.util.Enumeration;
import java.util.Vector;

import net.rim.device.api.gps.GPSInfo;
import net.rim.device.api.lbs.picker.ContactsLocationPicker;
import net.rim.device.api.lbs.picker.EnterLocationPicker;
import net.rim.device.api.lbs.picker.GPSLocationPicker;
import net.rim.device.api.lbs.picker.LocationPicker;
import net.rim.device.api.lbs.picker.MapsLocationPicker;
import net.rim.device.api.lbs.picker.RecentLocationPicker;

public class WPLocationPicker {
	private LocationPicker _locationPicker;

	public WPLocationPicker() {
		{

			// Define an array containing individual picker types.
			Vector locationPickersVector = new Vector();
			
			//If GPSInfo.getGPSDataSource() returns a null object,
			// GPS is not supported on the current BlackBerry Smartphone 
			boolean gpsSupported = GPSInfo.getGPSDataSource() != null;
			if(gpsSupported)
			{
				locationPickersVector.addElement(GPSLocationPicker.getInstance());
			}  
			
			locationPickersVector.addElement(EnterLocationPicker.getInstance(true));
			locationPickersVector.addElement(RecentLocationPicker.getInstance());

			LocationPicker.Picker mapsLocationPicker = null;   
			try       
			{
				mapsLocationPicker = MapsLocationPicker.getInstance();       
				//locationPickersVector.addElement(mapsLocationPicker);
			}
			catch(IllegalStateException ise)
			{
			}

			locationPickersVector.addElement(ContactsLocationPicker.getInstance(true));

			LocationPicker.Picker[] locationPickersArray = new LocationPicker.Picker[locationPickersVector.size()];
			locationPickersVector.copyInto(locationPickersArray);

			// Get a LocationPicker instance containing individual picker types
			// and make this class a location picker listener.
			_locationPicker = LocationPicker.getInstance(locationPickersArray);

			Enumeration globalPickers = _locationPicker.getGlobalLocationPickers();

			while (globalPickers.hasMoreElements())
			{
				_locationPicker.addLocationPicker((LocationPicker.Picker)globalPickers.nextElement());
			}

		}          
	}
	
	public void show() {
	    // Display the location picker
        _locationPicker.show();     
	}
	
	public void setListener(LocationPicker.Listener listener) {
		 _locationPicker.setListener(listener);
	}

}

//#endif