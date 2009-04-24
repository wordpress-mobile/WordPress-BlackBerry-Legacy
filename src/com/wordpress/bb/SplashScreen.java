package com.wordpress.bb;

import java.io.IOException;
import java.io.InputStream;
import java.util.Timer;
import java.util.TimerTask;

import javax.microedition.io.Connector;
import javax.microedition.io.HttpConnection;
import javax.microedition.rms.RecordStoreException;

import net.rim.device.api.system.Bitmap;
import net.rim.device.api.system.KeyListener;
import net.rim.device.api.ui.Field;
import net.rim.device.api.ui.UiApplication;
import net.rim.device.api.ui.component.BitmapField;
import net.rim.device.api.ui.component.LabelField;
import net.rim.device.api.ui.container.MainScreen;

import com.wordpress.controller.MainController;
import com.wordpress.utils.Preferences;
import com.wordpress.view.dialog.ErrorView;


public class SplashScreen extends MainScreen {
   private MainController next;
   private UiApplication application;
   private Timer timer = new Timer();
   private static final Bitmap _bitmap = Bitmap.getBitmapResource("wplogo.png");
   private Preferences blogPrefs= Preferences.getIstance();
	   
   public SplashScreen(UiApplication ui, MainController next) {
		super(Field.USE_ALL_HEIGHT | Field.FIELD_LEFT);
		this.application = ui;
		this.next = next;
		this.add(new BitmapField(_bitmap, Field.FIELD_HCENTER| Field.FIELD_VCENTER));
		SplashScreenListener listener = new SplashScreenListener(this);
		this.addKeyListener(listener);
		application.pushScreen(this);
		
		try {
			boolean load = blogPrefs.load();
			if (load) {
				 // preferences loaded!
				//not first startup
			} else { 
				//first startup
				add(new LabelField("Installation in progress...",Field.FIELD_HCENTER| Field.FIELD_VCENTER));
				//we can control connection type here...
			}
			timer.schedule(new CountDown(), 3000);
		} catch (Exception e) {
			timer.cancel();
			UiApplication.getUiApplication().invokeLater(new Runnable() {
				public void run() {
					ErrorView errView = new ErrorView("Wordpress startup Error!");
					errView.doModal();
					dismiss();
				}
			});
		}
	}
   
   public void dismiss() {
      timer.cancel();
      application.popScreen(this);
      next.showView();
   }
   
   private class CountDown extends TimerTask {
      public void run() {
         DismissThread dThread = new DismissThread();
         application.invokeLater(dThread);
      }
   }
   
   private class DismissThread implements Runnable {
      public void run() {
         dismiss();
      }
   }
   
   /**
    * this method check networks type connection on startup. Not used. 
    * @author dercoli
    *
    */
   private class checkConnThread implements Runnable {
		public void run() {
			try {
				// try to make simple http conn
				blogPrefs.setDeviceSideConnection(false);
				final HttpConnection c = (HttpConnection) Connector.open("http://www.wordpress.com", Connector.READ_WRITE);
				final InputStream is = c.openInputStream();
				c.getLength();
				
			} catch (IOException ignore) {
				// try to make simple http conn with BB client side connection
				try {
					blogPrefs.setDeviceSideConnection(true);
					final HttpConnection c = (HttpConnection) Connector.open("http://www.wordpress.com" + ";deviceside=true",Connector.READ_WRITE);
					final InputStream is = c.openInputStream();
					c.getLength();
				} catch (Exception e) {
					//no internet activity, we should show a message
					e.printStackTrace();
					ErrorView errView = new ErrorView("Wordpress hasn't found network connection");
					errView.doModal();
					blogPrefs.setDeviceSideConnection(false);
				}
			}	
			try {
				blogPrefs.save(); //save connections setting on RMS
			} catch (RecordStoreException e) {} 
			catch (IOException e) {} 
			
			dismiss();
		}
	}
	   
   
   
   protected boolean navigationClick(int status, int time) {
     // dismiss();
      //return true;
	   return false;
   }
   
   protected boolean navigationUnclick(int status, int time) {
      return false;
   }
   
   protected boolean navigationMovement(int dx, int dy, int status, int time) {
      return false;
   }
   
   public static class SplashScreenListener implements KeyListener {
      private SplashScreen screen;
      public boolean keyChar(char key, int status, int time) {
         //intercept the ESC and MENU key - exit the splash screen
         boolean retval = false;
         /*switch (key) {
            case Characters.CONTROL_MENU:
            case Characters.ESCAPE:
            screen.dismiss();
            retval = true;
            break;
         }*/
         return retval;
      }
      public boolean keyDown(int keycode, int time) {
         return false;
      }
      public boolean keyRepeat(int keycode, int time) {
         return false;
      }
      public boolean keyStatus(int keycode, int time) {
         return false;
      }
      public boolean keyUp(int keycode, int time) {
         return false;
      }
      public SplashScreenListener(SplashScreen splash) {
         screen = splash;
      }
   }
} 