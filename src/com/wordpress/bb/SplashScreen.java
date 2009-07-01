//#preprocess
package com.wordpress.bb;

import java.util.Timer;
import java.util.TimerTask;

import net.rim.device.api.system.Display;
import net.rim.device.api.system.EncodedImage;
import net.rim.device.api.system.KeyListener;
import net.rim.device.api.ui.Field;
import net.rim.device.api.ui.UiApplication;
import net.rim.device.api.ui.component.BitmapField;
import net.rim.device.api.ui.component.LabelField;
import net.rim.device.api.ui.container.MainScreen;

import com.wordpress.controller.MainController;
import com.wordpress.io.AppDAO;
import com.wordpress.io.BaseDAO;
import com.wordpress.model.Preferences;
import com.wordpress.utils.MultimediaUtils;
import com.wordpress.utils.log.Appender;
import com.wordpress.utils.log.FileAppender;
import com.wordpress.utils.log.Log;
import com.wordpress.view.dialog.ErrorView;


public class SplashScreen extends MainScreen {
   private MainController next;
   private UiApplication application;
   private Timer timer = new Timer();
   private Preferences blogPrefs= Preferences.getIstance();
	   
   public SplashScreen(UiApplication ui, MainController next) {
		super(Field.USE_ALL_HEIGHT | Field.FIELD_LEFT);
		this.application = ui;
		this.next = next;
		EncodedImage _theImage= EncodedImage.getEncodedImageResource("wplogo.png");
		int _preferredWidth = -1;
		//Set the preferred width to the image size or screen width if the image is larger than the screen width.
        if (_theImage.getWidth() > Display.getWidth()) {
            _preferredWidth = Display.getWidth();
        }
        if( _preferredWidth != -1) {        	
        	EncodedImage resImg = MultimediaUtils.bestFit2(_theImage, _preferredWidth, _theImage.getHeight());
        	_theImage = resImg;
        }
        
        this.add(new BitmapField(_theImage.getBitmap(), Field.FIELD_HCENTER| Field.FIELD_VCENTER));
		SplashScreenListener listener = new SplashScreenListener(this);
		this.addKeyListener(listener);
		application.pushScreen(this);
		
		//check application permission as first step
		WordPressApplicationPermissions.getIstance().checkPermissions();
						
		try {
			String baseDirPath = AppDAO.getBaseDirPath();
			if ( baseDirPath != null ) {
				//not first startup 	
				AppDAO.readApplicationPreferecens(blogPrefs); //load pref on startup
				timer.schedule(new CountDown(), 3000); //3sec splash
			} else { 
				add(new LabelField("Installation in progress...",Field.FIELD_HCENTER| Field.FIELD_VCENTER));
				//first startup
				AppDAO.setUpFolderStructure();
				timer.schedule(new CountDown(), 3000); //3sec splash
			}
			
			//#ifdef DEBUG
			Appender fileAppender = new FileAppender(AppDAO.getBaseDirPath(), BaseDAO.LOG_FILE_PREFIX);
			fileAppender.setLogLevel(Log.DEBUG); //if we set level to TRACE the file log size grows too fast
			fileAppender.open();
			Log.addAppender(fileAppender);
			//#endif

			
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

   /*
   private class SelectDirectoryThread implements Runnable {
	      public void run() {
	    	  String theDir = null;
				
				while ( theDir == null ) {
					DirectorySelectorPopUpScreen fps = new DirectorySelectorPopUpScreen();
					fps.pickFile();
			        theDir = fps.getFile();
			        System.out.println("sto provando con il file: "+theDir);			        				
					//first startup
					//AppDAO.setBaseDirPath("file:///store/home/user/wordpress/");
			        try {
			        	if(!JSR75FileSystem.isFileExist(theDir)){
							JSR75FileSystem.createDir(theDir);
							AppDAO.setBaseDirPath(theDir);
			        	}
			        } catch (Exception e) {
						theDir = null;
						ErrorView errView = new ErrorView("Directory not good");
						errView.doModal();						
					}
				}
			
			timer.schedule(new CountDown(), 2000); //show splash
	    	  
	      }
	   }
	   
   */
   
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