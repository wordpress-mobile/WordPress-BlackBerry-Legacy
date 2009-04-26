package com.wordpress.view;

import net.rim.device.api.system.Characters;
import net.rim.device.api.system.KeyListener;
import net.rim.device.api.system.TrackwheelListener;
import net.rim.device.api.ui.Field;
import net.rim.device.api.ui.UiApplication;
import net.rim.device.api.ui.component.LabelField;
import net.rim.device.api.ui.component.SeparatorField;
import net.rim.device.api.ui.container.PopupScreen;
import net.rim.device.api.ui.container.VerticalFieldManager;

public class HtmlTagPopupScreen extends PopupScreen implements KeyListener, TrackwheelListener {
    
    private String _response;
        
    
    public HtmlTagPopupScreen()
    {
        super(new VerticalFieldManager(),Field.FOCUSABLE);
              
        LabelField question = new LabelField("Not yet implemented!!");
                    
        add(question);
        add(new SeparatorField());
       // add(answer);
    }
    
    // This function gets called if the password gets called
    // it pops the password screen and pushes the apps main screen
    public void accept() {
        UiApplication.getUiApplication().popScreen(this);
    }
    
    public void close() {
        UiApplication.getUiApplication().popScreen(this);
    }
    
    public String getResponse() {
        return _response;
    }
    
    
    ////////////////////////////////////////////
    /// implementation of TrackwheelListener
    ////////////////////////////////////////////
    
    public boolean trackwheelClick(int status, int time) {
    	
    	UiApplication.getUiApplication().popScreen(this);
        return true;
    }
        
    /** Invoked when the trackwheel is released */
    public boolean trackwheelUnclick(int status, int time) {
        return false;
    }

    /** Invoked when the trackwheel is rolled. */
    public boolean trackwheelRoll(int amount, int status, int time) {
        return true;
    }
    
    /////////////////////////////////////   
    /// implementation of Keylistener
    /////////////////////////////////////     
         
    public boolean keyChar(char key, int status, int time) {
        //intercept the ESC key - exit the app on its receipt
        boolean retval = false;
        switch (key) {
            case Characters.ENTER:
            	UiApplication.getUiApplication().popScreen(this);
                retval = true;
                break;
            case Characters.ESCAPE:
                close();
                break; 
            default:
                retval = super.keyChar(key,status,time);
        }
        return retval;
    }
    
    /** Implementation of KeyListener.keyDown */
    public boolean keyDown(int keycode, int time) {
        return false;
    }

    /** Implementation of KeyListener.keyRepeat */
    public boolean keyRepeat(int keycode, int time) {
        return false;
    }

    /** Implementation of KeyListener.keyStatus */
    public boolean keyStatus(int keycode, int time) {
        return false;
    }

    /** Implementation of KeyListener.keyUp */
    public boolean keyUp(int keycode, int time) {
        return false;
    }
}
