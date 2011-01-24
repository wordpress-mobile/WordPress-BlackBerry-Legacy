package com.wordpress.view.dialog;


import com.wordpress.utils.log.Log;
import com.wordpress.xmlrpc.BlogConn;

import net.rim.device.api.ui.component.Dialog;
import net.rim.device.api.ui.component.DialogClosedListener;

public class ConnectionDialogClosedListener implements DialogClosedListener {
	public int choice;
	private final BlogConn conn;
	
	public ConnectionDialogClosedListener(BlogConn conn){
		super();
		this.conn = conn;
	}
	
	public void dialogClosed(Dialog dialog, int choice) {
		Log.trace("dialogClosed");
		this.choice = choice;
		if(choice == Dialog.CANCEL) {
			Log.trace("dialogClosed.CANCEL");
			this.conn.stopConnWork();
		}
	}
}