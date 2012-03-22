package com.wordpress.quickphoto.OS5;

import com.wordpress.utils.ImageEncodingPropertiesOS5;

import net.rim.device.api.ui.component.RadioButtonField;
import net.rim.device.api.ui.component.RadioButtonGroup;
import net.rim.device.api.ui.container.MainScreen;


/**
 * This MainScreen class allows a user to specify an encoding to be used
 * for taking a picture.
 */
public class EncodingPropertiesScreenOS5 extends MainScreen
{
    RadioButtonGroup _radioButtonGroup;
    CameraScreenOS5 _parentScreen;
    
    /**
     * Constructs a new EncodingPropertiesScreen object
     * @param encodingProperties The array of encoding properties available
     * @param parentScreen The parent screen of the application
     * @param currentSelectedIndex The index of the encoding that is currently selected
     */
    public EncodingPropertiesScreenOS5(ImageEncodingPropertiesOS5[] encodingProperties, CameraScreenOS5 parentScreen, int currentSelectedIndex) 
    {
        _parentScreen = parentScreen;
        _radioButtonGroup = new RadioButtonGroup();
        for(int i = 0; i < encodingProperties.length; i++)
        {
            RadioButtonField buttonField = new RadioButtonField(encodingProperties[i].toString());
            _radioButtonGroup.add(buttonField);
            this.add(buttonField);
        }
        _radioButtonGroup.setSelectedIndex(currentSelectedIndex);
    }
    
    
    /**
     * @see net.rim.device.api.ui.Screen#close()
     */
    public void close()
    {
        // Set the index of the selected encoding
        _parentScreen.setIndexOfEncoding(_radioButtonGroup.getSelectedIndex());
        super.close();
    }
    
    
    /**
     * @see net.rim.device.api.ui.container.MainScreen#onSavePrompt()
     */
    public boolean onSavePrompt()
    {
        // Prevent the save dialog from being displayed
        return true;
    }
}
