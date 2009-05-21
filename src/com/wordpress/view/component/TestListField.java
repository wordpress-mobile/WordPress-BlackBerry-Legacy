package com.wordpress.view.component;

//public class TestListField {

import java.util.Vector;

import net.rim.device.api.system.Bitmap;
import net.rim.device.api.system.KeypadListener;
import net.rim.device.api.ui.ContextMenu;
import net.rim.device.api.ui.DrawStyle;
import net.rim.device.api.ui.Field;
import net.rim.device.api.ui.Font;
import net.rim.device.api.ui.Graphics;
import net.rim.device.api.ui.Keypad;
import net.rim.device.api.ui.Manager;
import net.rim.device.api.ui.MenuItem;
import net.rim.device.api.ui.UiApplication;
import net.rim.device.api.ui.component.BitmapField;
import net.rim.device.api.ui.component.LabelField;
import net.rim.device.api.ui.component.ListField;
import net.rim.device.api.ui.component.ListFieldCallback;

public class TestListField extends ListField implements ListFieldCallback
{

	private Vector tasks;
	private Vector rows;
	private Bitmap p1;

	public TestListField(Vector tasks)
	{
		super(0, ListField.MULTI_SELECT);
		setRowHeight(40);
		setEmptyString("Nothing here!", DrawStyle.HCENTER);
		setCallback(this);
		setSearchable(false);

		p1 = Bitmap.getBitmapResource("p1.png");


		this.tasks = tasks;
		rows = new Vector();

		for (int x = 0; x < tasks.size(); ++x)
		{
			TableRowManager row = new TableRowManager();

			// SET THE TASK NAME LABELFIELD
			LabelField taskname = new LabelField("test", DrawStyle.ELLIPSIS);
			taskname.setFont(Font.getDefault().derive(Font.BOLD));
			row.add(taskname);
			row.add(new BitmapField(p1));


			// SET THE DUE DATE/TIME
			row.add(new LabelField("CAsso", DrawStyle.ELLIPSIS | LabelField.USE_ALL_WIDTH
					| DrawStyle.RIGHT));

			rows.addElement(row);
		}

		setSize(rows.size());
	}

	private class TableRowManager extends Manager
	{
		public TableRowManager()
		{
			super(0);
		}

		// Causes the fields within this row manager to be layed out then
		// painted.
		public void drawRow(Graphics g, int x, int y, int width, int height)
		{
			// Arrange the cell fields within this row manager.
			layout(width, height);

			// Place this row manager within its enclosing list.
			setPosition(x, y);

			// Apply a translating/clipping transformation to the graphics
			// context so that this row paints in the right area.
			g.pushRegion(getExtent());

			// Paint this manager's controlled fields.
			subpaint(g);

			g.setColor(0x00CACACA);
			g.drawLine(0, 0, getPreferredWidth(), 0);
			g.drawLine(10, 0, 10, getPreferredHeight());

			// Restore the graphics context.
			g.popContext();
		}

		// Arrages this manager's controlled fields from left to right within
		// the enclosing table's columns.
		protected void sublayout(int width, int height)
		{
			// set the size and position of each field.
			int fontHeight = Font.getDefault().getHeight();
			int preferredWidth = getPreferredWidth();

			// start with the Bitmap Field of the priority icon
			Field field = getField(0);
			layoutChild(field, 10, 40);
			setPositionChild(field, 0, 0);

			// set the task name label field
			field = getField(1);
			layoutChild(field, preferredWidth - 16, fontHeight + 1);
			setPositionChild(field, 16, 3);

			// set the list name label field
			field = getField(2);
			layoutChild(field, 150, fontHeight + 1);
			setPositionChild(field, 16, fontHeight + 6);

			// set the due time name label field
			field = getField(3);
			layoutChild(field, 150, fontHeight + 1);
			setPositionChild(field, preferredWidth - 152, fontHeight + 6);

			setExtent(preferredWidth, getPreferredHeight());
		}

		// The preferred width of a row is defined by the list renderer.
		public int getPreferredWidth()
		{
			return Graphics.getScreenWidth();
		}

		// The preferred height of a row is the "row height" as defined in the
		// enclosing list.
		public int getPreferredHeight()
		{
			return getRowHeight();
		}
	}

	// ListFieldCallback Implementation
	public void drawListRow(ListField listField, Graphics g, int index, int y, int width)
	{
		TestListField list = (TestListField) listField;
		TableRowManager rowManager = (TableRowManager) list.rows.elementAt(index);
		rowManager.drawRow(g, 0, y, width, list.getRowHeight());
	}

	public Object get(ListField list, int index)
	{
		return tasks.elementAt(index);
	}

	public int indexOfList(ListField list, String prefix, int start)
	{
	/*	for(int x = start; x < tasks.size(); ++x)
		{
			Task task = (Task)tasks.elementAt(x);
			if(task.getName().startsWith(prefix))
			{
				return x;
			}
		}*/
		return -1;
	}

	public int getPreferredWidth(ListField list)
	{
		return Graphics.getScreenWidth();
	}


	public void delete(int index)
	{		
		super.delete(index);
		tasks.removeElementAt(index);
	}

}
