package com.wordpress.view.dialog;

import java.util.Timer;
import java.util.TimerTask;

import net.rim.device.api.system.Application;
import net.rim.device.api.system.Backlight;
import net.rim.device.api.system.Bitmap;
import net.rim.device.api.system.EncodedImage;
import net.rim.device.api.system.GIFEncodedImage;
import net.rim.device.api.system.SystemListener2;
import net.rim.device.api.ui.component.Dialog;
import net.rim.device.api.ui.container.DialogFieldManager;

import com.wordpress.utils.log.Log;
import com.wordpress.view.component.AnimatedGIFField;

public class ConnectionInProgressView extends Dialog {

	/**
	 * Use this dialog boxes to provide feedback about an connection in progress. It include only an Cancel button and 
	 * have a syslistener to prevent backlight off. 
	 * 
	 * @param message
	 */
	public ConnectionInProgressView(String message) {

		super(message,  new String [] { "Cancel" },   new int [] { Dialog.CANCEL }, 
				Dialog.CANCEL,   Bitmap.getPredefinedBitmap(Bitmap.HOURGLASS));
		
		GIFEncodedImage _theImage= (GIFEncodedImage)EncodedImage.getEncodedImageResource("loading-gif.bin");
		DialogFieldManager dfm = (DialogFieldManager) getDelegate();
		dfm.setIcon(new AnimatedGIFField(_theImage));
		systemListener2 = new MySystemListener2();
		timer = new Timer();
	}
	
    private boolean listenersActive;
    private SystemListener2 systemListener2;
    private Timer timer;
	private TimerTask timerTask;
    
    private void checkAddListeners() {
		if(!listenersActive) {
            Application.getApplication().addSystemListener(systemListener2);
            timerTask = new BackLightTimerTask();
    		timer.schedule(timerTask, 200, 25000);
            listenersActive = true;
        }
	}
    
	private void checkRemoveListeners() {
		if(listenersActive) {
            Application.getApplication().removeSystemListener(systemListener2);
            timerTask.cancel();
            listenersActive = false;
        }
	}
	
    protected void onDisplay() {
        checkAddListeners();
        super.onExposed();
    }

    protected void onExposed() {
        checkAddListeners();
        super.onExposed();
    }
    
    protected void onObscured() {
        checkRemoveListeners();
        super.onObscured();
    }
    
    protected void onUndisplay() {
        checkRemoveListeners();
        super.onUndisplay();
    }
	
    
    /**
     * Internal timer task class to avoid Backlight turning off.
     */
    private class BackLightTimerTask extends TimerTask {
    	public void run() {
    		Log.trace("running the BackLightTimerTask");
    		Backlight.enable(true, 30);
    	}
    }
    
    
	private class MySystemListener2 implements SystemListener2 {

		//on - True if the backlight is on, false otherwise.
		public void backlightStateChange(boolean arg0) {
			if(arg0 == false) { 
				Log.trace("Backlight goes off!!");
				Backlight.enable(true, 30);
			} else {
				Log.trace("Backlight goes on!!");
			}
		}

		public void cradleMismatch(boolean mismatch) {
		}

		public void fastReset() {
		}

		public void powerOffRequested(int reason) {
		}

		public void usbConnectionStateChange(int state) {
		}

		public void batteryGood() {
		}

		public void batteryLow() {
		}

		public void batteryStatusChange(int status) {
		}

		public void powerOff() {
		}

		public void powerUp() {
		}

     };
}