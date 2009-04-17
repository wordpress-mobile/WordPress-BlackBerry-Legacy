package com.wordpress.utils.observer;


/**
 * <p>
 * <b>This is a recode for J2ME of <i>public interface java.util.Observer</i>.</b>
 * </p>
 * <p>
 * A class can implement the Observer interface when it wants to be informed of
 * changes in observable objects.
 * </p>
 * 
 * @author dercoli
 * 
 */
public interface Observer {

	/**
	 * This method is called whenever the observed object is changed. An
	 * application calls an Observable object's notifyObservers method to have
	 * all the object's observers notified of the change.
	 * 
	 * @param observable-
	 *            the observable object.
	 * @param object-
	 *            an argument passed to the notifyObservers method.
	 */
	public void update(Observable observable, Object object);
}
