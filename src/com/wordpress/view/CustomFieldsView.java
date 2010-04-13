package com.wordpress.view;

import java.util.Hashtable;
import java.util.Vector;

import net.rim.device.api.system.Bitmap;
import net.rim.device.api.ui.Color;
import net.rim.device.api.ui.Field;
import net.rim.device.api.ui.FieldChangeListener;
import net.rim.device.api.ui.Font;
import net.rim.device.api.ui.Manager;
import net.rim.device.api.ui.component.BasicEditField;
import net.rim.device.api.ui.component.ButtonField;
import net.rim.device.api.ui.component.Dialog;
import net.rim.device.api.ui.component.LabelField;
import net.rim.device.api.ui.container.HorizontalFieldManager;

import com.wordpress.bb.WordPressResource;
import com.wordpress.controller.BaseController;
import com.wordpress.controller.BlogObjectController;
import com.wordpress.utils.log.Log;
import com.wordpress.view.component.BaseButtonField;
import com.wordpress.view.component.BitmapButtonField;
import com.wordpress.view.container.BorderedFieldManager;
import com.wordpress.view.dialog.DiscardChangeInquiryView;


public class CustomFieldsView extends StandardBaseView {
	
    private BlogObjectController controller;
    private final Vector oldCustomFields;
    private Vector changeListeners = new Vector();
    private Bitmap _deleteBitmap = Bitmap.getBitmapResource("stop.png");
    private Bitmap _addBitmap = Bitmap.getBitmapResource("edit_add.png");
	private BasicEditField _fieldValue;
	private BasicEditField _fieldName;
        
    public CustomFieldsView(BlogObjectController _controller, Vector customFields, String title) {
    	super(_resources.getString(WordPressResource.MENUITEM_CUSTOM_FIELDS)+" > "+ title, Manager.NO_VERTICAL_SCROLL | Manager.NO_VERTICAL_SCROLLBAR);
    	this.controller=_controller;
		this.oldCustomFields = customFields;
    	
		//this is the base row  
        BorderedFieldManager outerContainer = new BorderedFieldManager(
        		Manager.NO_HORIZONTAL_SCROLL
        		| Manager.NO_VERTICAL_SCROLL );
        
         //Add new custom field:
        LabelField lblNewCustomField = GUIFactory.getLabel(_resources.getString(WordPressResource.LABEL_ADD_CUSTOM_FIELD), Color.BLACK);
        outerContainer.add(lblNewCustomField);
        outerContainer.add(GUIFactory.createSepatorField());
        
        _fieldName = new BasicEditField(_resources.getString(WordPressResource.LABEL_NAME)+": ", "", 100, Field.EDITABLE);
        outerContainer.add(_fieldName);
        _fieldValue = new BasicEditField(_resources.getString(WordPressResource.LABEL_VALUE)+": ", " ", 100, Field.EDITABLE);
        outerContainer.add(_fieldValue);
        
        BitmapButtonField addButtonField = new BitmapButtonField(_addBitmap, ButtonField.CONSUME_CLICK);
        addButtonField.setChangeListener(addCustomField);
        outerContainer.add(addButtonField);
		
        add(outerContainer);		
		LabelField lblDesc = getLabel(_resources.getString(WordPressResource.LABEL_CUSTOM_FIELD_DESC)); 
		Font fnt = this.getFont().derive(Font.ITALIC);
		lblDesc.setFont(fnt);
		//add(lblDesc);
		
		//add the buttons
        BaseButtonField buttonOK = GUIFactory.createButton(_resources.getString(WordPressResource.BUTTON_OK), ButtonField.CONSUME_CLICK);
        BaseButtonField buttonBACK= GUIFactory.createButton(_resources.getString(WordPressResource.BUTTON_BACK), ButtonField.CONSUME_CLICK);
        buttonBACK.setChangeListener(listenerBackButton);
        buttonOK.setChangeListener(listenerOkButton);
        HorizontalFieldManager buttonsManager = new HorizontalFieldManager(Field.FIELD_HCENTER);
        buttonsManager.add(buttonOK);
		buttonsManager.add(buttonBACK);
		add(buttonsManager); 
		add(new LabelField("", Field.NON_FOCUSABLE)); //space after content

		//add the custom fields
		initUI(oldCustomFields);
		
		_fieldName.setFocus();
		setDirty(false);
    }
    
       
    private void initUI(Vector customFields) {
    	Log.debug("start UI init");
    	int size = customFields.size();
    	Log.debug("Found "+size +" custom fields");
    	
		for (int i = 0; i <size; i++) {
			Log.debug("Elaborating custom field # "+ i);
			try {
				Hashtable customField = (Hashtable)customFields.elementAt(i);
				
				//check the presence for key & value 
				if(customField.get("key") == null || customField.get("value") == null) {
					Log.debug("Found prev. deleted custom fields");
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
        		| Manager.NO_VERTICAL_SCROLL);
        
        BasicEditField fieldName = new BasicEditField(_resources.getString(WordPressResource.LABEL_NAME)+": ", insertedName, 100, Field.EDITABLE);
        outerContainer.add(fieldName);
                
        BasicEditField fieldValue = new BasicEditField(_resources.getString(WordPressResource.LABEL_VALUE)+": ", insertedValue, 100, Field.EDITABLE);
        outerContainer.add(fieldValue);
        
        BitmapButtonField addButtonField = new BitmapButtonField(_deleteBitmap, ButtonField.CONSUME_CLICK);
        MyFieldChangeListener myFieldChangeListener = new MyFieldChangeListener(fieldValue,fieldName);
        addButtonField.setChangeListener(myFieldChangeListener);
        outerContainer.add(addButtonField);
        changeListeners.addElement(myFieldChangeListener); //added change listener into array
        
        int fieldsCount = getFieldCount();
        insert(outerContainer, fieldsCount-2); //leave the buttons + spacedField at the bottom of the screen
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
	    		delete(fieldWithFocus);
	    		setDirty(true);
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
	
	private FieldChangeListener listenerOkButton = new FieldChangeListener() {
	    public void fieldChanged(Field field, int context) {
	    	saveChanges();
	    	controller.backCmd();
	    }
	};

	private FieldChangeListener listenerBackButton = new FieldChangeListener() {
	    public void fieldChanged(Field field, int context) {
	    	onClose();
	    }
	};
	
	private void saveChanges() {
		if(isDirty()) {
			upgradeCustomFields();
			controller.setObjectAsChanged(true);
		}
	}
	
	public boolean onClose()   {
		boolean isModified=false;
		if(isDirty()){
			isModified = true;
		}
		if(!isModified) {
			controller.backCmd();
			return true;
		}
		String quest=_resources.getString(WordPressResource.MESSAGE_INQUIRY_DIALOG_BOX);
    	DiscardChangeInquiryView infoView= new DiscardChangeInquiryView(quest);
    	int choice=infoView.doModal();    	 
    	if(Dialog.DISCARD == choice) {
    		Log.trace("user has selected discard");
			controller.backCmd();
    		return true;
    	}else if(Dialog.SAVE == choice) {
    		Log.trace("user has selected save");
    		saveChanges();
    		controller.backCmd();    		
    		return true;
    	} else {
    		Log.trace("user has selected cancel");
    		controller.backCmd();
    		return false;
    	}
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