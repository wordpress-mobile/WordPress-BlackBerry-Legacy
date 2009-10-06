package com.wordpress.view;

import java.util.Hashtable;
import java.util.Vector;

import net.rim.device.api.system.Bitmap;
import net.rim.device.api.system.Display;
import net.rim.device.api.ui.Color;
import net.rim.device.api.ui.Field;
import net.rim.device.api.ui.FieldChangeListener;
import net.rim.device.api.ui.Font;
import net.rim.device.api.ui.Graphics;
import net.rim.device.api.ui.Manager;
import net.rim.device.api.ui.component.BasicEditField;
import net.rim.device.api.ui.component.ButtonField;
import net.rim.device.api.ui.component.LabelField;
import net.rim.device.api.ui.container.HorizontalFieldManager;
import net.rim.device.api.ui.container.VerticalFieldManager;

import com.wordpress.bb.WordPressResource;
import com.wordpress.controller.BaseController;
import com.wordpress.controller.BlogObjectController;
import com.wordpress.utils.log.Log;
import com.wordpress.view.component.BitmapButtonField;
import com.wordpress.view.component.BorderedFieldManager;
import com.wordpress.view.component.HorizontalPaddedFieldManager;


public class CustomFieldsView extends BaseView {
	
    private BlogObjectController controller;
    private VerticalFieldManager _container;
    private final Vector oldCustomFields;
    private Vector changeListeners = new Vector();
    private Bitmap _deleteBitmap = Bitmap.getBitmapResource("stop.png");
    private Bitmap _addBitmap = Bitmap.getBitmapResource("edit_add.png");
	private BasicEditField _fieldValue;
	private BasicEditField _fieldName;
        
    public CustomFieldsView(BlogObjectController _controller, Vector customFields, String title) {
    	super(_resources.getString(WordPressResource.MENUITEM_CUSTOM_FIELDS)+" > "+ title);
    	this.controller=_controller;
		this.oldCustomFields = customFields;
    	
     	VerticalFieldManager internalManager = new VerticalFieldManager( Manager.NO_VERTICAL_SCROLL | Manager.NO_VERTICAL_SCROLLBAR ) {
    		public void paintBackground( Graphics g ) {
    			g.clear();
    			int color = g.getColor();
    			g.setColor( Color.LIGHTGREY );
    			g.drawBitmap(0, 0, Display.getWidth(), Display.getHeight(), _backgroundBitmap, 0, 0);
    			//g.fillRect( 0, 0, Display.getWidth(), Display.getHeight() );
    			g.setColor( color );
    		}
    		
    		protected void sublayout( int maxWidth, int maxHeight ) {
    			
    			int titleFieldHeight = 0;
    			if ( titleField != null ) {
    				titleFieldHeight = titleField.getHeight();
    			}
    			
    			int displayWidth = Display.getWidth(); // I would probably make these global
    			int displayHeight = Display.getHeight();
    			
    			super.sublayout( displayWidth, displayHeight - titleFieldHeight );
    			setExtent( displayWidth, displayHeight - titleFieldHeight );
    		}
    		
    	};
    	
    	_container = new VerticalFieldManager( Manager.VERTICAL_SCROLL | Manager.VERTICAL_SCROLLBAR );
    	internalManager.add( _container );
    	super.add( internalManager );
    	    	
		//this is the base row  
        BorderedFieldManager outerContainer = new BorderedFieldManager(
        		Manager.NO_HORIZONTAL_SCROLL
        		| Manager.NO_VERTICAL_SCROLL
        		| BorderedFieldManager.BOTTOM_BORDER_NONE);
         //Add new custom field:
        LabelField lblNewCustomField = getLabel(_resources.getString(WordPressResource.LABEL_ADD_CUSTOM_FIELD));
        outerContainer.add(lblNewCustomField);
        
        HorizontalFieldManager rowName = new HorizontalPaddedFieldManager();
        LabelField lblName = getLabel(_resources.getString(WordPressResource.LABEL_NAME)+":");
        rowName.add(lblName);
        _fieldName = new BasicEditField("", "", 100, Field.EDITABLE);
        rowName.add(_fieldName);
        outerContainer.add(rowName);
                
        HorizontalFieldManager rowValue = new HorizontalPaddedFieldManager();
        LabelField lblValue = getLabel(_resources.getString(WordPressResource.LABEL_VALUE)+":");
        rowValue.add(lblValue);
        _fieldValue = new BasicEditField("", " ", 100, Field.EDITABLE);
        rowValue.add(_fieldValue);
        outerContainer.add(rowValue);
        
        BitmapButtonField addButtonField = new BitmapButtonField(_addBitmap, ButtonField.CONSUME_CLICK);
        addButtonField.setChangeListener(addCustomField);
        outerContainer.add(addButtonField);
		
        add(outerContainer);		
		LabelField lblDesc = getLabel(_resources.getString(WordPressResource.LABEL_CUSTOM_FIELD_DESC)); 
		Font fnt = this.getFont().derive(Font.ITALIC);
		lblDesc.setFont(fnt);
		//add(lblDesc);
		
		//add the custom fields
		initUI(oldCustomFields);
		
		_fieldName.setFocus();
		_container.setDirty(false);
    }
    
    private void initUI(Vector customFields) {
    	Log.debug("start UI init");
    	int size = customFields.size();
    	Log.debug("Founded "+size +" custom fields");
    	
		for (int i = 0; i <size; i++) {
			Log.debug("Elaborating custom field # "+ i);
			try {
				Hashtable customField = (Hashtable)customFields.elementAt(i);
				
				//check the presence for key & value 
				if(customField.get("key") == null || customField.get("value") == null) {
					Log.debug("Founded prev. deleted custom fields");
					continue;
				}
				
				String ID = (String)customField.get("id");
				String key = (String)customField.get("key");
				String value = (String)customField.get("value");
				Log.debug("id - "+ID);
				Log.debug("key - "+key);
				Log.debug("value - "+value);	
				
				if(!key.startsWith("_")) {
					Log.debug("Custom Field added to UI");
					addCustomField(key, value ); 
				} else {
					Log.debug("Custom Field discarded from UI");
				}
			} catch(Exception ex) {
				Log.error("Error while Elaborating custom field # "+ i);
			}
		}
		
		Log.debug("End UI init");
    }
    
    private void addCustomField(String insertedName, String insertedValue) {
    	//this is the base row  
        BorderedFieldManager outerContainer = new BorderedFieldManager(
        		Manager.NO_HORIZONTAL_SCROLL
        		| Manager.NO_VERTICAL_SCROLL
        		| BorderedFieldManager.BOTTOM_BORDER_NONE);
        
        HorizontalFieldManager rowName = new HorizontalPaddedFieldManager();
        LabelField lblName = getLabel(_resources.getString(WordPressResource.LABEL_NAME)+":");
        rowName.add(lblName);
        BasicEditField fieldName = new BasicEditField("", insertedName, 100, Field.EDITABLE);
        rowName.add(fieldName);
        outerContainer.add(rowName);
                
        HorizontalFieldManager rowValue = new HorizontalPaddedFieldManager();
        LabelField lblValue = getLabel(_resources.getString(WordPressResource.LABEL_VALUE)+":");
        rowValue.add(lblValue);
        BasicEditField fieldValue = new BasicEditField("", insertedValue, 100, Field.EDITABLE);
        rowValue.add(fieldValue);
        outerContainer.add(rowValue);
        
        BitmapButtonField addButtonField = new BitmapButtonField(_deleteBitmap, ButtonField.CONSUME_CLICK);
        MyFieldChangeListener myFieldChangeListener = new MyFieldChangeListener(fieldValue,fieldName);
        addButtonField.setChangeListener(myFieldChangeListener);
        outerContainer.add(addButtonField);
        changeListeners.addElement(myFieldChangeListener); //added change listener into array
        
        add(outerContainer);
    }
    
	private FieldChangeListener addCustomField = new FieldChangeListener() {
	    public void fieldChanged(Field field, int context) {
	    	//get the value from the main UI row
	    	String insertedName = _fieldName.getText();
	    	String insertedValue = _fieldValue.getText();
	    	//reset main UI value
	    	_fieldName.setText("");
	    	_fieldValue.setText("");
	    	_fieldName.setFocus();
	    	addCustomField(insertedName, insertedValue);
	   }
	};
	

	private class MyFieldChangeListener implements FieldChangeListener {
		BasicEditField fieldValue;
		BasicEditField fieldName;
		
		public MyFieldChangeListener(BasicEditField fieldValue, BasicEditField fieldName) {
			super();
			this.fieldValue = fieldValue;
			this.fieldName = fieldName;
		}

		public String getName() {
			return fieldName.getText();
		}
		
		public String getValue() {
			return fieldValue.getText();
		}
		
		public void fieldChanged(Field field, int context) {
			Field fieldWithFocus = _container.getFieldWithFocus();
	    	
	    	if(fieldWithFocus instanceof BorderedFieldManager) {
	    		_container.delete(fieldWithFocus);
	    		_container.setDirty(true);
	    		//find this change listener and remove from the list
	    		int count = changeListeners.size();
	    		for (int i = 0; i < count; i++) {
	    			MyFieldChangeListener list = (MyFieldChangeListener)changeListeners.elementAt(i);
	    			if(list.equals(this)){
	    				changeListeners.removeElementAt(i);
	    				Log.trace("listener rimosso");
	    				break;
	    			}
				}
	    	}			
		}
	}

	public void add( Field field ) {
		_container.add( field );
	}
    
    //override onClose() to display a dialog box when the application is closed    
	public boolean onClose()   {
		if(_container.isDirty()) {
			upgradeCustomFields();
			controller.setObjectAsChanged(true);
		}
		controller.backCmd();
		return true;
    }
	
	/*
	 * Each custom field entry will have three fields: id, key and value. 
	 * The key and value fields correspond to the name and value you entered for the post. 
	 * The id field is a unique identifier for just that field. 
	 * When passing that data back to WP via metaWeblog.editPost if you provide and id, key and value 
	 * fields then an update is done. 
	 * If you provide just an id with no key or value fields then WP deletes that custom field.
	 */
	private void upgradeCustomFields() {
		Log.debug("start custom fields upgrading");
		
		int oldCustomFieldsSize = oldCustomFields.size();		
		
		//upgrade prev. custom fields
		for (int i = 0; i < oldCustomFieldsSize; i++) {
			Log.debug("Upgrading custom field # "+ i);
			try {
				Hashtable customField = (Hashtable)oldCustomFields.elementAt(i);				
				String ID = (String)customField.get("id");
				String key = (String)customField.get("key");
				String value = (String)customField.get("value");

				int listenerSize = changeListeners.size();
				boolean presence = false;
				if(!key.startsWith("_")) {
					Log.debug("key - "+key);
					Log.debug("id - "+ID);
					Log.debug("value - "+value);	

					for (int j = 0; j < listenerSize; j++) {
						MyFieldChangeListener list = (MyFieldChangeListener)changeListeners.elementAt(j);
						String tmpName= list.getName().trim();
						String tmpValue = list.getValue().trim();
						
						if(key.equalsIgnoreCase(tmpName)) {
							customField.put("value", tmpValue); //upgrade the custom field value
							changeListeners.removeElementAt(j);
							Log.debug("Upgrated custom field # "+ i);
							presence = true;
							break;
						}
					}
					//remove the current field
				 if(!presence) {
					 Log.debug("custom field # "+ i+ " marked for deletion");
					 customField.remove("key");
					 customField.remove("value");
				 }
					
				}
			} catch(Exception ex) {
				Log.error("Error while Elaborating custom field # "+ i);
			}
		}

		//add new custom fields
		int listenerSize = changeListeners.size();
		for (int i = 0; i <listenerSize; i++) {
			MyFieldChangeListener list = (MyFieldChangeListener)changeListeners.elementAt(i);
			//Hashtable customField = (Hashtable)oldCustomFields.elementAt(i);
			Hashtable customField = new Hashtable();
			customField.put("key", list.getName().trim()); //add the custom field value
			customField.put("value", list.getValue().trim()); //add the custom field value
			oldCustomFields.addElement(customField);
		}
		
		Log.debug("end custom fields upgrade task");
	}
	
	public BaseController getController() {
		return controller;
	}
}