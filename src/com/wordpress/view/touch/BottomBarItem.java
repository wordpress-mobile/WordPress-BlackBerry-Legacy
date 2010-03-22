//#preprocess

//#ifdef IS_OS47_OR_ABOVE
package com.wordpress.view.touch;


public class BottomBarItem {
	private final String enabledBitmap;
	public String getEnabledBitmap() {
		return enabledBitmap;
	}

	public String getDisabledBitmap() {
		return disabledBitmap;
	}

	public String getTooltip() {
		return tooltip;
	}

	private final String disabledBitmap;
	private final String tooltip;

	public BottomBarItem(String enabledBitmap, String disabledBitmap, String tooltip) {
		this.enabledBitmap = enabledBitmap;
		this.disabledBitmap = disabledBitmap;
		this.tooltip = tooltip;
	}
}
//#endif