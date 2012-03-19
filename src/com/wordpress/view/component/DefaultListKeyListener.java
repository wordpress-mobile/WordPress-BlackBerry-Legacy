package com.wordpress.view.component;

import net.rim.device.api.system.Application;
import net.rim.device.api.system.KeyListener;
import net.rim.device.api.ui.UiApplication;
import net.rim.device.api.ui.component.ListField;

/**
 * A KeyListener implementation
 */
public class DefaultListKeyListener implements KeyListener
{
	private ListLoadMoreListener view = null;
	private ListField listObj = null;
	
	
	public void setListObj(ListField commentsList) {
		this.listObj = commentsList;
	}

	public DefaultListKeyListener(ListLoadMoreListener _view, ListField listaPost) {
		super();
		this.view = _view;
		this.listObj = listaPost;
	}

	/**
	 * @see KeyListener#keyChar(char, int, int)
	 */
	public boolean keyChar(final char key, int status, int time)
	{ 
		if(key == 't' && listObj != null )
        {
            Runnable previousRunnable = new Runnable()
            {
                public void run()
                {
                	synchronized(Application.getEventLock()) 
                	{
                		listObj.setSelectedIndex(0);
                	}
                }
            };
            new Thread(previousRunnable).start();
            return true;
        }
		
		if(key == 'b' && listObj != null )
        {
            Runnable previousRunnable = new Runnable()
            {
                public void run()
                {
                	synchronized(Application.getEventLock()) 
                	{
                		view.loadMore(); //trigger the load more here
                		listObj.setSelectedIndex( listObj.getSize() - 1 );
                	}
                }
            };
            new Thread(previousRunnable).start();
            
            return true;
        }

		if(key == 'p' && listObj != null )
        {
            Runnable previousRunnable = new Runnable()
            {
                public void run()
                {
                	synchronized(Application.getEventLock()) 
                	{
                		int selectedIndex = listObj.getSelectedIndex();
                		if ( selectedIndex == 0 ) return;
                		listObj.setSelectedIndex( selectedIndex -1 );
                	}
                }
            };
            new Thread(previousRunnable).start();
            return true;
        }

		if(key == 'n' && listObj != null )
        {
            Runnable previousRunnable = new Runnable()
            {
                public void run()
                {
                	synchronized(Application.getEventLock()) 
                	{
                		int selectedIndex = listObj.getSelectedIndex();
                		if ( selectedIndex == listObj.getSize() - 1 ) return;

                		if ( selectedIndex + 1 == listObj.getSize() - 1 )
                			view.loadMore(); //trigger the load more here
                		listObj.setSelectedIndex( selectedIndex + 1 );
                	}
                }
            };
            new Thread(previousRunnable).start();
            return true;
        }
		return false;   
	}

	public boolean keyDown(int arg0, int arg1) {
		return false;
	}

	public boolean keyRepeat(int keycode, int time) {
		return false;
	}

	public boolean keyStatus(int keycode, int time) {
		return false;
	}

	public boolean keyUp(int keycode, int time) {
		return false;
	}
}