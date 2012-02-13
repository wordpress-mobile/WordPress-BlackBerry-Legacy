//#preprocessor

//#ifdef  BlackBerrySDK6.0.0 | BlackBerrySDK7.0.0
package com.wordpress.view.component;


import net.rim.device.api.ui.component.StandardTitleBar;

public class HeaderFieldNew extends StandardTitleBar implements WordPressTitleBar  {

	private String title;
	private String subTitle = null;

	public HeaderFieldNew(String title) {
		super();
		this.title = title;
		//this.addIcon("my_logo.png");
		this.addTitle(title);
		this.addNotifications();
		this.addSignalIndicator();
		this.setPropertyValue(StandardTitleBar.PROPERTY_BATTERY_VISIBILITY, StandardTitleBar.BATTERY_VISIBLE_LOW_OR_CHARGING);
	}

	/* (non-Javadoc)
	 * @see com.wordpress.view.component.WordPressTitleBar#setTitle(java.lang.String)
	 */
	public void setTitle(String title) {
		this.title = title;
		this.addTitle(title);
		invalidate();
	}

	/* (non-Javadoc)
	 * @see com.wordpress.view.component.WordPressTitleBar#setSubTitle(java.lang.String)
	 */
	public void setSubTitle(String subTitle) {
		this.subTitle = subTitle;
		this.addTitle(title + " - " + subTitle);
		invalidate();
	}

}

//#endif