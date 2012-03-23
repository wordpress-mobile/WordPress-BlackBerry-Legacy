package com.wordpress.view.component;

import net.rim.device.api.ui.Color;
import net.rim.device.api.ui.Graphics;
import net.rim.device.api.ui.component.CheckboxField;

public class DisabledCheckBoxField extends CheckboxField {
	
	public boolean isEnabled = true; // by default - enabled
	
	public DisabledCheckBoxField(String string, boolean sticky) {
		super(string, sticky);
	}
	
	protected void paint(Graphics graphics) {
		if (isEnabled) {
			// if enabled - drawing in BLACK
			graphics.setColor(Color.BLACK);
		} else {
			// if field is not enabled - drawing in GRAY
			graphics.setColor(Color.GRAY);
		}
		super.paint(graphics);
	}
	
	public void setEnablez(boolean isEnabled) {
		this.isEnabled = isEnabled;
		this.invalidate();
	}
	
	public void setChecked(boolean checked) {
		// if field is enabled - passing parameter value
		// if not enabled - passing false
		final boolean flag2set = isEnabled ? checked:false;
		super.setChecked(flag2set);
	}
};