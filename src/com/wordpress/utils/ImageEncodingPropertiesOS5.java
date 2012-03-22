package com.wordpress.utils;

/**
 * 
 * For OS5 only. See com.wordpress.utils.ImageEncodingProperties for OS6 or highe
 * 
 * A wrapper for the various encoding properties available for use with the
 * VideoControl.getSnapshot() method.
 */
public final class ImageEncodingPropertiesOS5 {
    /** The file format of the picture */
    private String _format;

    /** The width of the picture */
    private String _width;

    /** The height of the picture */
    private String _height;

    /** The quality of the picture */
    private String _quality;

    /** Booleans that indicate whether the values have been set */
    private boolean _formatSet;
    private boolean _widthSet;
    private boolean _heightSet;
    private boolean _qualitySet;

    /**
     * Set the file format to be used in snapshots
     * 
     * @param format
     *            The file format to be used in snapshots
     */
    public void setFormat(final String format) {
        _format = format;
        _formatSet = true;
    }

    /**
     * Set the width to be used in snapshots
     * 
     * @param width
     *            The width to be used in snapshots
     */
    public void setWidth(final String width) {
        _width = width;
        _widthSet = true;
    }

    /**
     * Set the height to be used in snapshots
     * 
     * @param height
     *            The height to be used in snapshots
     */
    public void setHeight(final String height) {
        _height = height;
        _heightSet = true;
    }

    /**
     * Set the quality to be used in snapshots
     * 
     * @param quality
     *            The quality to be used in snapshots
     */
    public void setQuality(final String quality) {
        _quality = quality;
        _qualitySet = true;
    }

    /**
     * Return the encoding as a coherent String to be used in menus
     * 
     * @see Object#toString()
     */
    public String toString() {
        final StringBuffer display = new StringBuffer();

        display.append(_width);
        display.append(" x ");
        display.append(_height);
        display.append(" ");
        display.append(_format);
        display.append(" (");
        display.append(_quality);
        display.append(")");

        return display.toString();
    }

    /**
     * Return the encoding as a properly formatted string to be used by the
     * VideoControl.getSnapshot() method.
     * 
     * @return The encoding expressed as a formatted string.
     */
    public String getFullEncoding() {
        final StringBuffer fullEncoding = new StringBuffer();

        fullEncoding.append("encoding=");
        fullEncoding.append(_format);

        fullEncoding.append("&width=");
        fullEncoding.append(_width);

        fullEncoding.append("&height=");
        fullEncoding.append(_height);

        fullEncoding.append("&quality=");
        fullEncoding.append(_quality);

        return fullEncoding.toString();
    }

    /**
     * Checks whether all the fields been set
     * 
     * @return true if all fields have been set.
     */
    public boolean isComplete() {
        return _formatSet && _widthSet && _heightSet && _qualitySet;
    }
}