package com.wordpress.xmlrpc;


import java.io.IOException;
import java.util.Hashtable;
import java.util.TimeZone;
import java.util.Vector;

import javax.microedition.io.ConnectionNotFoundException;

import org.kxmlrpc.XmlRpcClient;
import org.kxmlrpc.XmlRpcException;

import com.wordpress.model.Blog;
import com.wordpress.model.Category;
import com.wordpress.utils.observer.Observable;

public abstract class BlogConn extends Observable implements Runnable {
	
	protected final Boolean TRUE = new Boolean(true);
    protected final Boolean FALSE = new Boolean(false);
	protected String urlConnessione;
    protected String mUsername;
    protected String mPassword;
    protected XmlRpcClient mConnection;	
	protected BlogConnResponse connResponse = new BlogConnResponse();
	protected boolean isWorking = false; //indica se la connessione sta lavorando
	protected Thread t = null;
	
	public BlogConn(String aURIHint, String aUserHint, String aPasswordHint, TimeZone aTz) {

	    mUsername = aUserHint;
	    mPassword = aPasswordHint;

	    //#ifdef BB_VERSION
		urlConnessione=aURIHint+";deviceside=true";
		//#else
		urlConnessione=aURIHint;
		//#endif 
				
  	   System.out.println("creata la connessione");
	}
	
		
	public void startConnWork(){
   	   //#debug
  	   System.out.println("Inizio richiesta XML-RPC");
		isWorking=true;
		t = new Thread(this);
		t.start();
	}
	
	/**
	 * blocca il funzionamento della connessione immediatamente.
	 */
	public void stopConnWork(){
		if(!isWorking) return;
   	   //#debug
  	   System.out.println("Richiesta chiusura della connessione XML-RPC");
		isWorking=false;
		t=null;
		mConnection=null;
		setStopMessage("chiusura della connessione XML-RPC");
		notifyObservers(connResponse);
	}
	
	public abstract  void run();

	protected Object execute(String aCommand, Vector aArgs){
		Object response = null;
		if(mConnection == null)
			mConnection = new XmlRpcClient(urlConnessione);
		
		try {
			response = mConnection.execute(aCommand, aArgs);
		} catch (ConnectionNotFoundException cnfe) {
			setErrorMessage(cnfe, "The server was not found");
		} catch (IOException ioe) {
			setErrorMessage(ioe, "A server communications error occured:");
		} catch (XmlRpcException xre) {
			setErrorMessage(xre, "An error occured talking to the blog:");
		} catch (Exception t) {
			setErrorMessage(t, "An error occured :");
		} 
   	 
  	   System.out.println("termine richiesta XML-RPC");
		isWorking=false;
		return response;
	}
	
	
	protected synchronized void readBlogCategories(Blog blog) throws Exception {
   	   //#debug
  	   System.out.println("Reperisco le categorie del blog : "+ blog.getBlogName());
		Vector args;
		Object response;
		Vector categoryStructs;
		Hashtable categoryStruct;
		Category[] categories;

		args = new Vector(3);
		args.addElement(blog.getBlogId());
		args.addElement(mUsername);
		args.addElement(mPassword);

		response = execute("mt.getCategoryList", args);
		if(connResponse.isError()) {
			 throw new Exception("Cannot read blog categories");	
		}
		
		args = null;

		try {
		    categoryStructs = (Vector) response;
		    categoryStruct = null;
		    if (categoryStructs.size() > 0) {
		        categories = new Category[categoryStructs.size()];
		        for (int i = 0; i < categories.length; i++) {
		            categoryStruct = (Hashtable) categoryStructs.elementAt(i);
		            categories[i] = new Category
		                ((String) categoryStruct.get("categoryId"),
		                 (String) categoryStruct.get("categoryName"));
		        }
		        blog.setCategories(categories);
		    } else {
		        blog.setCategories(null);
		    }
	   	   //#debug
	  	   System.out.println("Terminato reperimento delle categorie del blog : "+ blog.getBlogName());
		} catch (ClassCastException cce) {
		    throw new Exception("Invalid server response");
		}
	}
	
	
	protected void setStopMessage(String stopMsg){
		connResponse=new BlogConnResponse();
		connResponse.setError(false);
		connResponse.setStopped(true);
		connResponse.setResponse(stopMsg);
    	
		System.out.println(stopMsg);
	}
	
	protected void setErrorMessage(Exception e, String err){
		connResponse=new BlogConnResponse();
		connResponse.setError(true);
		
		if(e != null) {
			connResponse.setResponse(err+" - "+e.getMessage());
			System.out.println(err+" - "+e.getMessage());
		} else {
			connResponse.setResponse(err);
			System.out.println(err);			
		}
	}
	
	protected void setErrorMessage(String err){
		connResponse=new BlogConnResponse();
		connResponse.setError(true);
		connResponse.setResponse(err);
    
		System.out.println(err);
	}
}
