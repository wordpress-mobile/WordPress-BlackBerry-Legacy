package com.wordpress.view.container;

import net.rim.device.api.ui.Field;
import net.rim.device.api.ui.Manager;
import net.rim.device.api.ui.XYPoint;
import net.rim.device.api.util.Arrays;
/**
 * TableLayoutManager can be used to create multi column table
 * views. You can even embed table within another table to create
 * complex tabular views.
 */
public class TableLayoutManager extends Manager
{

    int _columnWidths[];
    int _suggestedColumnWidths[];
    int _rowHeights[];
    int _columnStyles[];

    /** Let the field use up ALL the space that it needs **/
    public static final int USE_PREFERRED_SIZE = 1;
    /** let the field use up as much space as it needs UP TO a maximum */
    public static final int USE_PREFERRED_WIDTH_WITH_MAXIMUM = 2;
    /** the fields should use up the remaining space evenly */
    public static final int SPLIT_REMAINING_WIDTH = 4;
    /** the column is fixed width **/
    public static final int FIXED_WIDTH = 8;

    private static int BITMASK_USE_PREFERRED =
            USE_PREFERRED_WIDTH_WITH_MAXIMUM
            | USE_PREFERRED_SIZE;

    public static int DEFAULT_PADDING = 5;
    int _rows;
    int _columns;
    private int _horizPadding;

    public TableLayoutManager(int columnStyles[], long style)
    {
        this(columnStyles, null, DEFAULT_PADDING, style);
    }

    /**
     * creates a table with the specified column styles
     *
     * @param columnStyles
     *           - array of styles for all the columns. The size
     *           of this array determines the number of columns.
     * @param columnWidths
     *           - array of widths. This is used for the FIXED_WIDTH,
     *           and USE_PREFERRED_WIDTH_WITH_MAXIMUM styles.
     *           the value is ignored for the other styles.
     * @param horizontalPadding
     *           - space between columns
     * @param style
     */

    public TableLayoutManager(int columnStyles[], int columnWidths[],
        int horizontalPadding, long style)
    {
        super(style);

        _horizPadding = horizontalPadding;

        _columnStyles = columnStyles;
        if (_columnStyles == null)
              throw new IllegalArgumentException("not column styles");

        if (columnWidths != null)
        {
            _suggestedColumnWidths = Arrays.copy(columnWidths,
                                     0,columnWidths.length);
            if (_suggestedColumnWidths.length < _columnStyles.length)
            {
                int oldLength = _suggestedColumnWidths.length;
                int increase = columnStyles.length - oldLength;
               _suggestedColumnWidths = Arrays.copy(columnWidths, 0,
                            columnStyles.length);
               Arrays.fill(_suggestedColumnWidths, 0,
                           oldLength, increase);
            }
        }
        else
            _suggestedColumnWidths = new int[_columnStyles.length];
    }

    private Field getField(int x, int y)
    {
        int i = x + (y * _columns);
        if (i >= getFieldCount()) return null;
        return getField(i);
    }

    private boolean isColumnStyle(int value, int flag)
    {
        return ((value) & (flag)) > 0;
    }

    /**
     * Implements the getPreferredWidth call to return the
     * expected width for this manager. The expected width is
     * the Max(Sum (Column Widths))
     */
    public int getPreferredWidth()
    {
        int numberFields = getFieldCount();
        if (numberFields == 0) return 0;
        int rows = numberFields / _columnStyles.length;
        int prefferedWidth = 0;

        int styles[] = _columnStyles;
        int[] columnWidths = new int[_columns];
        Arrays.fill(columnWidths, -1);

        for (int i = 0; i < _columns; i++)
        {
            // assign the fixed widths
            if (isColumnStyle(styles[i], FIXED_WIDTH))
            {
                columnWidths[i] = _suggestedColumnWidths[i];
            }
            else
            {
                if (isColumnStyle(styles[i], BITMASK_USE_PREFERRED))
                {
                    for (int j = 0; j < rows; j++)
                    {
                        Field field = getField(i, j);
                        if (field != null)
                        {

                            int actualWidth = getPreferredWidthOfChild(field)
                                     + field.getMarginLeft()
                                     + field.getMarginRight();
                            if (isColumnStyle(styles[i],
                                USE_PREFERRED_WIDTH_WITH_MAXIMUM))
                            {
                                actualWidth = Math.min(actualWidth,
                                  _suggestedColumnWidths[i]);
                            }

                            columnWidths[i] =
                               Math.max(actualWidth,columnWidths[i]);
                        }
                    }
                }
            }
        }
        // TODO - this loop can be optimized
        for (int n = 0; n < _columns; n++)
        {
            prefferedWidth += columnWidths[n];
        }
        
        return prefferedWidth;
    }

    /**
     * implements the preferred height for this layout
     */
    public int getPreferredHeight()
    {
        int numberFields = getFieldCount();
        if (numberFields == 0) return 0;

        int rows = numberFields / _columnStyles.length;
        int prefferedHeight = 0;

        int[] rowHeights = new int[rows];
        Arrays.fill(rowHeights, -1);
        
        _columns = _columnStyles.length; /* TODO: update the copy from RIM  */
        for (int i = 0; i < _columns; i++)
        {
            for (int j = 0; j < rows; j++)
            {
                Field field = getField(i, j);
                if (field != null)
                {
                int actualHeight = getPreferredHeightOfChild(field)
                            + field.getMarginBottom()
                            + field.getMarginTop();
                rowHeights[j] = Math.max(actualHeight, rowHeights[j]);
                }
            }
        }

        for (int n = 0; n < rows; n++)
        {
            prefferedHeight += rowHeights[n];
        }
        return prefferedHeight;
    }

    /**
     * Defines how Fields for this manager needs to be handled.
     */
    protected void sublayout(int layoutWidth, int layoutHeight)
    {
        int numberFields = getFieldCount();
        if (numberFields == 0) {   /* TODO: update the copy from RIM  */
        	if ( this._columnStyles != null ) { //At least one field was added and removed from this manager
        		setExtent( 0 ,  0 );
        	} 
        	return; 
        }
        layoutWidth -= getPaddingLeft() + getPaddingRight();
        layoutHeight -= getPaddingTop() + getPaddingBottom();
        _columns = _columnStyles.length;
        int styles[] = _columnStyles;
        if (isStyle(Field.USE_ALL_WIDTH))
        {
            boolean found = false;
            // if the field should take maximum space, at least
            // the last field
            // should be SPLIT_REMAINING_WIDTH
           for (int n = 0; n < _columns; n++)
           {
               if (styles[n] == SPLIT_REMAINING_WIDTH)
               {
                   found = true;
                   break;
               }
           }
           if (!found)
           {
               styles[_columns - 1] = SPLIT_REMAINING_WIDTH;
           }
        }
        _rows = numberFields / _columns;
        if ((numberFields % _columns) > 0) _rows++;
        _columnWidths = new int[_columns];  // arrays that keep track of
                                            // maximum widths
        _rowHeights = new int[_rows];

        // widths and heights are -1 if unassigned, we use this
        // fact to assign the column widths
        Arrays.fill(_columnWidths, -1);
        Arrays.fill(_rowHeights, -1);

        /*
         * there are three types of columns, fixed width, split
         * remaining width, and use preferred size step 1) we
         * need to look at the columns that are marked as "use
         * preferred size", find the widest element, then
         * record that maximum width step 2) as well, we can
         * assign the column widths for the columns that are
         * fixed width
         */

        for (int i = 0; i < _columns; i++)
        {
            // assign the fixed widths
            if (isColumnStyle(styles[i], FIXED_WIDTH))
            {
                _columnWidths[i] = _suggestedColumnWidths[i];
            }
            else
            {
                if (isColumnStyle(styles[i], BITMASK_USE_PREFERRED))
                {
                    for (int j = 0; j < _rows; j++)
                    {
                        Field field = getField(i, j);
                        if (field != null)
                        {
                            layoutChild(field, Math.max(0, layoutWidth
                               - (field.getMarginLeft()
                               + field.getMarginRight())),
                                 Math.max(0,layoutHeight
                               - (field.getMarginBottom()
                               + field.getMarginTop())));

                            int actualWidth = getPreferredWidthOfChild(field)
                                 + field.getMarginLeft()
                                 + field.getMarginRight();
                            int actualHeight = getPreferredHeightOfChild(field)
                                 + field.getMarginBottom()
                                 + field.getMarginTop();

                            if (isColumnStyle(styles[i],
                                  USE_PREFERRED_WIDTH_WITH_MAXIMUM))
                            {
                                actualWidth = Math.min(actualWidth,
                                   _suggestedColumnWidths[i]);
                            }

                            _columnWidths[i] = Math.max(actualWidth,
                                _columnWidths[i]);
                            _rowHeights[j] = Math.max(actualHeight,
                                _rowHeights[j]);
                        }
                    }
                }
            }
        }

        /*
         * step 3 - find out the total width used up by the
         * fields that have known widths
         */
        int usedColumnWidth = 0;
        int numUnassignedColumnWidths = 0;

        for (int i = 0; i < _columns; i++)
        {
            if (_columnWidths[i] >= 0)
            {
                usedColumnWidth += _columnWidths[i]
                        + ((i < (_columns - 1)) ? _horizPadding : 0);
            }
            else
            {
                numUnassignedColumnWidths++;
            }
        }

        /*
         * assign the remaining space evenly amongst the
         * unassigned columns
         */
        if (numUnassignedColumnWidths > 0)
        {
            int remainingWidthToAssign = layoutWidth - usedColumnWidth;
            if (remainingWidthToAssign < 0)
            {
                remainingWidthToAssign = 0;
            }

            int splitRemainingWidth = (remainingWidthToAssign -
                                      ((numUnassignedColumnWidths - 1)
                                      * _horizPadding))
                                         / numUnassignedColumnWidths;
            
            for (int i = 0; i < _columns; i++)
            {
                int assignedWidth = Math.min(remainingWidthToAssign,
                     splitRemainingWidth);
                if (_columnWidths[i] < 0)
                {
                    _columnWidths[i] = assignedWidth;
                    remainingWidthToAssign -= assignedWidth;
                }
            }
           
            /* TODO: Added by danilo. We should check the last Pixel!!! */
            if ( remainingWidthToAssign > 0 ) {
            	_columnWidths[_columns - 1 ] += remainingWidthToAssign; 
            	remainingWidthToAssign = 0;
            }
        }

        int currentRow = 0;
        int currentColumn = 0;
        int y = getPaddingTop();
        for (int n = 0; n < numberFields; n++)
        {            Field field = getField(n);

            if (!isColumnStyle(styles[currentColumn], USE_PREFERRED_SIZE))
            { // do
                // the others we missed from above
                layoutChild(field, Math.max(0, _columnWidths[currentColumn]
                        - (field.getMarginLeft()
                        + field.getMarginRight())),
                        Math.max(0, layoutHeight
                                - y
                                - (field.getMarginBottom()
                                + field.getMarginTop())));
            }

            _rowHeights[currentRow] = Math.max(_rowHeights[currentRow], field
                     .getExtent().height
                     + field.getMarginBottom()
                     + field.getMarginTop());

            currentColumn++;
            if ((n == (numberFields - 1)) || (currentColumn >= _columns))
            {
                // we are at the end of the row or list, so now
                // go and actually do the positioning for each row
                int x = getPaddingLeft();
                for (int i = 0; i < currentColumn; i++)
                {
                    Field field1 = getField(i, currentRow);
                    XYPoint offset = calcAlignmentOffset(field1, Math.max(0,
                             _columnWidths[i]
                                    - (field1.getMarginLeft() + field1
                                            .getMarginRight())), Math.max(0,
                            _rowHeights[currentRow]
                                  - (field1.getMarginBottom()
                                  + field1.getMarginTop())));
                     setPositionChild(field1, x + offset.x
                        + field1.getMarginLeft(), y + offset.y
                        + field1.getMarginTop());
                     x += _columnWidths[i] + _horizPadding;
                }

                y += _rowHeights[currentRow];
                currentColumn = 0;
                currentRow++;
            }
        }

        int totalWidth = 0;
        if (isStyle(Field.USE_ALL_WIDTH))
        {
            totalWidth = layoutWidth;
        }
        else
        {
            for (int i = 0; i < _columns; i++)
            {
                totalWidth += _columnWidths[i]
                      + ((i < (_columns - 1)) ? _horizPadding : 0);
            }
        }

        totalWidth += getPaddingLeft() + getPaddingRight();

        y += getPaddingBottom();
        setExtent(totalWidth, Math.min(y, layoutHeight));
    }

    /**
     * Navigation movement to allow for both cell to cell within
     * a columns and row movement
     */
    protected boolean navigationMovement(int dx, int dy, int status, int time)
    {
        int focusIndex = getFieldWithFocusIndex();
        int dirY = (dy > 0) ? 1 : -1;
        int absY = Math.abs(dy);

        for (int y = 0; y < absY; y++)
        {
            focusIndex += _columns * dirY;
            if (focusIndex < 0 || focusIndex >= getFieldCount())
            {
                //#ifdef BlackBerrySDK4.5.0
                this.invalidate(); //ref #217
                //#endif
                return false;
            }
            else
            {
                Field f = getField(focusIndex);
                if (f.isFocusable())
                {
                    f.setFocus();
                }
                else
                    y--; // do it over again
            }
        }

        int dirX = (dx > 0) ? 1 : -1;
        int absX = Math.abs(dx);
        for (int x = 0; x < absX; x++)
        {
            focusIndex += dirX;
            if (focusIndex < 0 || focusIndex >= getFieldCount())
            {
                //#ifdef BlackBerrySDK4.5.0
                this.invalidate(); //Ref #217
                //#endif
                return false;
            }
            else
            {
                Field f = getField(focusIndex);
                if (f.isFocusable())
                {
                    f.setFocus();
                }
                else
                    x--; // do it over again
            }
        }
        
        //#ifdef BlackBerrySDK4.5.0
        this.invalidate(); //ref #217
        //#endif
        
        return true;
    }

    /**
     * Calculate the styles and return appropriate XY offset
     * locations within the cell.
     * @param field
     * @param width
     * @param height
     * @return
     */
    private XYPoint calcAlignmentOffset(Field field, int width, int height)
    {
        XYPoint offset = new XYPoint(0, 0);
        long fieldStyle = field.getStyle();
        long field_x_style = fieldStyle & Field.FIELD_HALIGN_MASK;

        if (field_x_style == Field.FIELD_RIGHT)
        {
            offset.x = width - field.getExtent().width;
        }
        else if (field_x_style == Field.FIELD_HCENTER)
        {
            offset.x = (width - field.getExtent().width) / 2;
        }
        long field_y_style = fieldStyle & Field.FIELD_VALIGN_MASK;
        if (field_y_style == Field.FIELD_BOTTOM)
        {
            offset.y = height - field.getExtent().height;
        }
        else if (field_y_style == Field.FIELD_VCENTER)
        {
            offset.y = (height - field.getExtent().height) / 2;
        }
        return offset;
    }
}