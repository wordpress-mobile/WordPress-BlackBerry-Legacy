package com.wordpress.view;

import com.wordpress.bb.WordPressInfo;
import com.wordpress.bb.WordPressResource;
import com.wordpress.controller.BaseController;
import com.wordpress.view.component.BaseButtonField;
import com.wordpress.view.component.WebBitmapField;
import com.wordpress.view.container.JustifiedEvenlySpacedHorizontalFieldManager;

import net.rim.device.api.system.Bitmap;
import net.rim.device.api.system.Display;
import net.rim.device.api.ui.DrawStyle;
import net.rim.device.api.ui.Field;
import net.rim.device.api.ui.FieldChangeListener;
import net.rim.device.api.ui.Manager;
import net.rim.device.api.ui.MenuItem;
import net.rim.device.api.ui.component.ButtonField;
import net.rim.device.api.ui.component.Menu;
import net.rim.device.api.ui.container.VerticalFieldManager;

public class GoogleMapView extends StandardBaseView {
	
	private BaseController controller;
	private final String lat;
	private final String lon;
	
	private VerticalFieldManager mapContainer;
	private JustifiedEvenlySpacedHorizontalFieldManager toolbarOne;
	private int currentZoomLevel = 16;
	private int minZoom = 0;
	private int maxZoom = 21;
		
	public GoogleMapView (BaseController controller, String title, String lat, String lon) {		
		super(title, Manager.NO_VERTICAL_SCROLL | Manager.NO_VERTICAL_SCROLLBAR);
		
		this.controller = controller;
		this.lat = lat;
		this.lon = lon;
		
		//buttons box	
		toolbarOne = new JustifiedEvenlySpacedHorizontalFieldManager();	
		toolbarOne.setMargin(5,0,5,0);
		
		BaseButtonField buttonZoomIn= GUIFactory.createButton(_resources.getString(WordPressResource.BUTTON_ZOOM_IN), ButtonField.CONSUME_CLICK | ButtonField.USE_ALL_WIDTH | DrawStyle.ELLIPSIS);
		buttonZoomIn.setChangeListener(
				new FieldChangeListener() {
					public void fieldChanged(Field field, int context) {
						if(currentZoomLevel < maxZoom) {
							currentZoomLevel++;
							changeMap();
						}
					}
				}
		);
		buttonZoomIn.setMargin(0,5,0,5);
		
		BaseButtonField buttonZoomOut= GUIFactory.createButton(_resources.getString(WordPressResource.BUTTON_ZOOM_OUT), ButtonField.CONSUME_CLICK | ButtonField.USE_ALL_WIDTH | DrawStyle.ELLIPSIS);
		buttonZoomOut.setChangeListener(
			new FieldChangeListener() {
				public void fieldChanged(Field field, int context) {
					if(currentZoomLevel > minZoom) {
						currentZoomLevel--;
						changeMap();
					}
				}
			}
		);
		buttonZoomOut.setMargin(0,5,0,5);
		toolbarOne.add(buttonZoomIn);
		toolbarOne.add(buttonZoomOut);
		add(toolbarOne);
		
		mapContainer = new VerticalFieldManager(NO_VERTICAL_SCROLL | USE_ALL_WIDTH);
		changeMap();
		add(mapContainer);
		buttonZoomIn.setFocus();
	}
	
	
	//aggiorna la mappa
	private void changeMap() {
		mapContainer.deleteAll();
		Bitmap bitmap = new Bitmap(getPredefinedMapWidth(), getPredefinedMapHeigth());
		WebBitmapField img = new WebBitmapField(buildMapURL(), bitmap, 
				Field.FIELD_HCENTER | Field.FIELD_VCENTER | Field.FOCUSABLE);
		mapContainer.add(img);
	}
	
	private String buildMapURL() {
		String mapUrl = WordPressInfo.GOOGLE_STATIC_MAP_URL+"?center="+lat+","+lon+"&zoom="+currentZoomLevel
		+"&size="+getPredefinedMapWidth()+"x"+getPredefinedMapHeigth()+"&maptype=roadmap&format=PNG"+
		"&markers=color:blue|label:You|"+lat+","+lon+"&sensor=false";
		return mapUrl;
	}
	
	private int getPredefinedMapWidth(){
		return Display.getWidth() - 10;
	}
	
	private int getPredefinedMapHeigth(){
		return (Display.getHeight() - (toolbarOne.getHeight()+18));
	}
	
    //Override the makeMenu method so we can add a custom menu item
    protected void makeMenu(Menu menu, int instance)
    {	
    	if(currentZoomLevel < maxZoom)
    		menu.add(_zoomInItem);

    	if(currentZoomLevel > minZoom)
    		menu.add(_zoomOutItem);
    	
		//Create the default menu.
        super.makeMenu(menu, instance);
    }
	
	
	private MenuItem _zoomInItem = new MenuItem( _resources, WordPressResource.BUTTON_ZOOM_IN, 10000, 100) {
		public void run() {
			currentZoomLevel++;
			changeMap();
		}
	};
	
	private MenuItem _zoomOutItem = new MenuItem( _resources, WordPressResource.BUTTON_ZOOM_OUT, 10000, 100) {
		public void run() {
			currentZoomLevel--;
			changeMap();
		}
	};
	
    //override onClose() to display a dialog box when the application 
    //menu close is selected or return btn is hitted    
	public boolean onClose() {
		controller.backCmd();
		return true;
    }

	public BaseController getController() {
		return controller;
	}
}