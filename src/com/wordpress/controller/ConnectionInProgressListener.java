package com.wordpress.controller;

import net.rim.device.api.ui.component.Dialog;
import net.rim.device.api.ui.component.DialogClosedListener;

import com.wordpress.xmlrpc.BlogConn;

/**
 * listener on the connection in progress dialog. used by controller.
 * @author dercoli
 *
 */
class ConnectionInProgressListener implements DialogClosedListener {
	public int choice;
	private BlogConn conn;
	
	public ConnectionInProgressListener(BlogConn conn ){
		super();
		this.conn=conn;
		System.out.println("Created the listener for the conn dialog");
	}
	
	public void dialogClosed(Dialog dialog, int choice) {
		System.out.println("Chiusura della conn dialog");	
		this.choice = choice;
		if(choice==Dialog.CANCEL) {
			System.out.println("Chiusura della conn dialog tramite cancel");
			if(conn != null ) 
				conn.stopConnWork(); //stop the connection if the user click on cancel button
		}
	}
}