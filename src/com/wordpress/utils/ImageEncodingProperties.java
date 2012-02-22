package com.wordpress.utils;

/**
 * A wrapper for the various encoding properties available
 * for use with the VideoControl.getSnapshot() method.
 */
public final class ImageEncodingProperties
{   
    /** The file format of the picture */
    private String _format;

    /** The width of the picture */
    private String _width;

    /** The height of the picture */
    private String _height;
    
    
    /** Booleans that indicate whether the values have been set */
    private boolean _formatSet;
    private boolean _widthSet;
    private boolean _heightSet;    

    
    /**
     * Set the file format to be used in snapshots
     * @param format The file format to be used in snapshots
     */
    public void setFormat(String format)
    {
        _format = format;
        _formatSet = true;
    }
    

    /**
     * Set the width to be used in snapshots
     * @param width The width to be used in snapshots
     */
    public void setWidth(String width)
    {
        _width = width;
        _widthSet = true;
    }
    

    /**
     * Set the height to be used in snapshots
     * @param height The height to be used in snapshots
     */
    public void setHeight(String height)
    {
        _height = height;
        _heightSet = true;
    }    
    

    /**
     * @see Object#toString()
     */
    public String toString()
    {
        // Return the encoding as a coherent String to be used in menus
        StringBuffer display = new StringBuffer();

        display.append(_width);
        display.append(" x ");
        display.append(_height);
        display.append(" ");
        display.append(_format);                

        return display.toString();
    }
    

    /**
     * Return the encoding as a properly formatted string to
     * be used by the VideoControl.getSnapshot() method.
     * @return The encoding expressed as a formatted string.
     */
    public String getFullEncoding()
    {
        StringBuffer fullEncoding = new StringBuffer();

        fullEncoding.append("encoding=");
        fullEncoding.append(_format);

        fullEncoding.append("&width=");
        fullEncoding.append(_width);

        fullEncoding.append("&height=");
        fullEncoding.append(_height);        

        return fullEncoding.toString();
    }
    
    
    /**
     * Checks whether all the fields been set
     * @return true if all fields have been set.
     */
    public boolean isComplete()
    {
        return _formatSet && _widthSet && _heightSet;
    }
}
