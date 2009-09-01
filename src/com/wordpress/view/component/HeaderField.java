/*-
 * Copyright (c) 2007, Derek Konigsberg
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer. 
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in the
 *    documentation and/or other materials provided with the distribution. 
 * 3. Neither the name of the project nor the names of its
 *    contributors may be used to endorse or promote products derived
 *    from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS
 * FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE
 * COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT,
 * INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING,
 * BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
 * HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT,
 * STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED
 * OF THE POSSIBILITY OF SUCH DAMAGE.
 * 
 * 
 * Modified by Danilo Ercoli, ercoli@gmail.com, 
 * on 28 july 2009
 * 
 */

package com.wordpress.view.component;


import net.rim.device.api.system.Application;
import net.rim.device.api.system.DeviceInfo;
import net.rim.device.api.system.Display;
import net.rim.device.api.system.RadioInfo;
import net.rim.device.api.system.RadioStatusListener;
import net.rim.device.api.system.SystemListener;
import net.rim.device.api.ui.Color;
import net.rim.device.api.ui.DrawStyle;
import net.rim.device.api.ui.Field;
import net.rim.device.api.ui.Font;
import net.rim.device.api.ui.Graphics;

/**
 * General purpose header field for application screens.
 * Based on the sample code provided here:
 * http://www.northcubed.com/site/?p=15
 */
public class HeaderField extends Field {
    private Font headerFont;
    private String title;
    private boolean showSignal;
    private boolean showBattery;
    private boolean showTitle;
    private int fieldWidth;
    private int fieldHeight;
    private int fontColor;
    private int backgroundColor;
    private int batteryBackground;
    private int signalBarColor;
    private SystemListener systemListener;
    private RadioStatusListener radioStatusListener;
    private boolean listenersActive;
    private int signalLevel;
    private int batteryLevel;
    
    public HeaderField(String title) {
        super(Field.NON_FOCUSABLE);
        this.title =title;
        this.showSignal = true;
        this.showBattery = true;
        this.showTitle = true;
        this.fontColor = -1;
        this.headerFont = Font.getDefault().derive(Font.BOLD);
        this.backgroundColor = -1;
        this.batteryBackground = 0x999999;
        this.signalBarColor = Color.GRAY;
        this.fieldHeight = headerFont.getHeight();
        this.fieldWidth = Display.getWidth();
        signalLevel = RadioInfo.getSignalLevel();
        batteryLevel = DeviceInfo.getBatteryLevel();
        
        this.listenersActive = false;
        
        this.systemListener = new SystemListener() {
            public void powerOff() {
            }
            public void powerUp() {
            }
            public void batteryLow() {
                onBatteryStatusChanged();
            }
            public void batteryGood() {
                onBatteryStatusChanged();
            }
            public void batteryStatusChange(int status) {
                onBatteryStatusChanged();
            }
        };
        this.radioStatusListener = new RadioStatusListener() {
            public void signalLevel(int level) {
                onRadioStatusChanged();
            }
            public void networkStarted(int networkId, int service) {
                onRadioStatusChanged();
            }
            public void baseStationChange() {
                onRadioStatusChanged();
            }
            public void radioTurnedOff() {
                onRadioStatusChanged();
            }
            public void pdpStateChange(int apn, int state, int cause) {
                onRadioStatusChanged();
            }
            public void networkStateChange(int state) {
                onRadioStatusChanged();
            }
            public void networkScanComplete(boolean success) {
                onRadioStatusChanged();
            }
            public void mobilityManagementEvent(int eventCode, int cause) {
                onRadioStatusChanged();
            }
            public void networkServiceChange(int networkId, int service) {
                onRadioStatusChanged();
            }
        };
    }
    
    protected void onBatteryStatusChanged() {
        batteryLevel = DeviceInfo.getBatteryLevel();
        invalidate();
    }
    
    protected void onRadioStatusChanged() {
        signalLevel = RadioInfo.getSignalLevel();
        invalidate();
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

    private void checkAddListeners() {
		if(!listenersActive) {
            Application.getApplication().addSystemListener(systemListener);
            Application.getApplication().addRadioListener(radioStatusListener);
            onBatteryStatusChanged();
            onRadioStatusChanged();
            listenersActive = true;
        }
	}
    
	private void checkRemoveListeners() {
		if(listenersActive) {
            Application.getApplication().removeSystemListener(systemListener);
            Application.getApplication().removeRadioListener(radioStatusListener);
            listenersActive = false;
        }
	}
	
    /**
     * Remove any global event listeners.  Intended to be called on shutdown,
     * where the active screen may not get popped off the stack prior to
     * System.exit() being called.
     */
    public void removeListeners() {
        if(listenersActive) {
            Application.getApplication().removeSystemListener(systemListener);
            Application.getApplication().removeRadioListener(radioStatusListener);
            listenersActive = false;
        }
    }
    
    public void setTitle(String title) {
        this.title = title;
        invalidate();
    }
    
    public void setFontColor(int fontColor) {
        this.fontColor = fontColor;
        invalidate();
    }
    
    public void setBatteryBackground(int batteryBackground) {
        this.batteryBackground = batteryBackground;
        invalidate();
    }
    
    public void setBackgroundColor(int backgroundColor) {
        this.backgroundColor = backgroundColor;
        invalidate();
    }
    
    public void showSignal(boolean bool) {
        showSignal = bool;
        invalidate();
    }
    
    public void showBattery(boolean bool) {
        showBattery = bool;
        invalidate();
    }
    
    public void showTitle(boolean bool) {
        showTitle = bool;
        invalidate();
    }
    
    protected void layout(int width, int height) {
        setExtent(getPreferredWidth(), getPreferredHeight());
    }
    
    public int getPreferredWidth() {
        return fieldWidth;
    }
    
    public int getPreferredHeight() {
        return fieldHeight;
    }
    
    protected void paint(Graphics graphics) {
        if(fontColor == -1) {
            fontColor = graphics.getColor();
        }

        graphics.setFont(headerFont);
        int graphicsDiff = 0;
        int preferredWidth = this.getPreferredWidth();
        int preferredHeight = this.getPreferredHeight();
        int midPoint = preferredHeight / 2;
        
        if(backgroundColor != -1) {
            graphics.setColor(backgroundColor);
            graphics.fillRect(0, 0, preferredWidth, preferredHeight);
        }
        
        if(showSignal) {
        	graphics.pushRegion(preferredWidth - 37, midPoint - 7, 35, 14, 0, 0);
        	drawSignalIndicator(graphics);
        	graphics.popContext();
        	
            graphicsDiff += 37;
        }
        
        if(showBattery) {
        	graphics.pushRegion(preferredWidth - 48 - graphicsDiff, midPoint - 7, 44, 14, 0, 0);
        	drawBatteryIndicator(graphics);
        	graphics.popContext();
        	
        	graphicsDiff += 48;
        }
        
        graphics.setColor(fontColor);
        
        if(showTitle) {
            graphics.drawText(title, 1, 0, DrawStyle.ELLIPSIS, preferredWidth - (graphicsDiff+5));
        }
    }
    
    private void drawSignalIndicator(Graphics graphics) {
    	graphics.setColor(Color.DARKGRAY);
        graphics.fillRect(7, 12, 4, 2);
        graphics.fillRect(13, 9, 4, 5);
        graphics.fillRect(19, 6, 4, 8);
        graphics.fillRect(25, 3, 4, 11);
        graphics.fillRect(31, 0, 4, 14);
    	
    	graphics.setColor(signalBarColor);
    	graphics.drawLine(0, 0, 8, 0);
    	graphics.drawLine(0, 0, 4, 4);
    	graphics.drawLine(8, 0, 4, 4);
    	graphics.drawLine(4, 4, 4, 13);
    	
        if(signalLevel >= -120) {
            //1 band
            graphics.fillRect(7, 12, 4, 2);
        }
        if(signalLevel >= -101) {
            //2 bands
            graphics.fillRect(13, 9, 4, 5);
        }
        if(signalLevel >= -92) {
            //3 bands
            graphics.fillRect(19, 6, 4, 8);
        }
        if(signalLevel >= -86) {
            //4 bands
            graphics.fillRect(25, 3, 4, 11);
        }
        if(signalLevel >= -77) {
            //5 bands
            graphics.fillRect(31, 0, 4, 14);
        }
    }

    private void drawBatteryIndicator(Graphics graphics) {
    	int backgroundColor = graphics.getBackgroundColor();
    	
    	graphics.setColor(batteryBackground);
    	graphics.drawRect(1, 0, 40, 14);
    	graphics.drawRect(2, 1, 38, 12);
    	graphics.drawLine(0, 2, 0, 12);
    	graphics.fillRect(41, 3, 3, 8);

    	graphics.setColor(backgroundColor);
    	graphics.fillRect(3, 2, 36, 10);
    	
		// Pick the battery color
    	/*if(batteryLevel > 75) { graphics.setColor(0x28f300); }
		else if(batteryLevel > 50) { graphics.setColor(0x91dc00); }
		else if(batteryLevel > 25) { graphics.setColor(0xefec00); }
		else { graphics.setColor(0xff2200); }
		*/
    	graphics.setColor(Color.LIGHTGREY);
    	
    	// Paint the battery level indicator
    	graphics.fillRect(4, 3, 6, 8);
    	graphics.fillRect(11, 3, 6, 8);
    	graphics.fillRect(18, 3, 6, 8);
    	graphics.fillRect(25, 3, 6, 8);
    	graphics.fillRect(32, 3, 6, 8);
    	
    	graphics.setColor(backgroundColor);
        int power = (int)((34.00/100) * batteryLevel);
        power = Math.max(power, 0);
        power = Math.min(power, 34);
        graphics.fillRect(38 - (34 - power), 3, 34 - power, 8);
    }
}