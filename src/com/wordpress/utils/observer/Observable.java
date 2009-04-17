/**
 * 
 */
package com.wordpress.utils.observer;

import java.util.Vector;


/**
 * <p>
 * <b>This is a recode for J2ME of the <i>public class java.util.Observable</i>.</b>
 * </p>
 * <p>
 * This class represents an observable object, or "data" in the model-view
 * paradigm. It can be subclassed to represent an object that the application
 * wants to have observed.
 * </p>
 * <p>
 * An observable object can have one or more observers. An observer may be any
 * object that implements interface Observer. After an observable instance
 * changes, an application calling the Observable's notifyObservers method
 * causes all of its observers to be notified of the change by a call to their
 * update method.
 * </p>
 * <p>
 * The order in which notifications will be delivered is unspecified. The
 * default implementation provided in the Observable class will notify Observers
 * in the order in which they registered interest, but subclasses may change
 * this order, use no guaranteed order, deliver notifications on separate
 * threads, or may guarantee that their subclass follows this order, as they
 * choose.
 * </p>
 * <p>
 * Note that this notification mechanism is has nothing to do with threads and
 * is completely separate from the wait and notify mechanism of class Object.
 * </p>
 * <p>
 * When an observable object is newly created, its set of observers is empty.
 * Two observers are considered the same if and only if the equals method
 * returns true for them.
 * </p>
 * 
 * @author dercoli
 * 
 */
public class Observable {

	private boolean changed;
	private final Vector observers;

	/**
	 * Construct an Observable with zero Observers.
	 */
	public Observable() {
		observers = new Vector();
		changed = false;
	}

	/**
	 * Adds an observer to the set of observers for this object, provided that
	 * it is not the same as some observer already in the set. The order in
	 * which notifications will be delivered to multiple observers is not
	 * specified. See the class comment.
	 * 
	 * @param o -
	 *            an observer to be added. Throws: NullPointerException - if the
	 *            parameter o is null.
	 * 
	 * @throws NullPointerException -
	 *             if the parameter o is null.
	 */
	public synchronized void addObserver(Observer o) {
		if (o == null) {
			throw new NullPointerException();
		} else {
			synchronized (observers) {
				if (!observers.contains(o)) {
					observers.addElement(o);
				}
			}
		}

	}

	/**
	 * <p>
	 * Indicates that this object has no longer changed, or that it has already
	 * notified all of its observers of its most recent change, so that the
	 * <strong>hasChanged</strong> method will now return false. This method is
	 * called automatically by the <strong>notifyObservers</strong> methods.
	 * </p>
	 */
	protected synchronized void clearChanged() {
		changed = false;
	}

	/**
	 * Returns the number of observers of this Observable object.
	 * 
	 * @return the number of observers of this object.
	 */
	public synchronized int countObservers() {
		synchronized (observers) {
			return observers.size();
		}
	}

	/**
	 * Deletes an observer from the set of observers of this object. Passing
	 * null to this method will have no effect.
	 * 
	 * @param observer -
	 *            the observer to be deleted.
	 */
	public synchronized void deleteObserver(Observer observer) {
		if (observer != null) {
			synchronized (observers) {
				observers.removeElement(observer);
			}
		}
	}

	/**
	 * Clears the observer list so that this object no longer has any observers.
	 * 
	 */
	public synchronized void deleteObservers() {
		synchronized (observers) {
			observers.removeAllElements();
		}
	}

	/**
	 * Tests if this object has changed.
	 * 
	 * @return true if and only if the setChanged method has been called more
	 *         recently than the clearChanged method on this object; false
	 *         otherwise.
	 */
	public synchronized boolean hasChanged() {
		return changed;
	}

	/**
	 * <p>
	 * If this object has changed, as indicated by the hasChanged method, then
	 * notify all of its observers and then call the clearChanged method to
	 * indicate that this object has no longer changed. Each observer has its
	 * update method called with two arguments: this observable object and null.
	 * In other words, this method is equivalent to:
	 * </p>
	 * <p>
	 * <strong>notifyObservers(null)</strong>
	 * </p>
	 * 
	 */
	public void notifyObservers() {
		this.notifyObservers(null, false);
	}

	/**
	 * <p>
	 * If this object has changed, as indicated by the hasChanged method, then
	 * notify all of its observers and then call the clearChanged method to
	 * indicate that this object has no longer changed.
	 * </p>
	 * <p>
	 * Each observer has its update method called with two arguments: this
	 * observable object and the arg argument.
	 * </p>
	 * 
	 * 
	 * 
	 * @param object -
	 *            any object
	 */
	public void notifyObservers(Object object) {
		synchronized (observers) {
			for (int i = 0; i < observers.size(); i++) {
				((Observer) observers.elementAt(i)).update(this, object);
			}
			clearChanged();
		}
	}

	/**
	 * <p>
	 * Shortcut method for <code>setChanged()</code> and
	 * <code>notifyObservers()</code>.
	 * </p>
	 * <p>
	 * If setChanged is true, notify all of its observers and then call the
	 * clearChanged method to indicate that this object has no longer changed.
	 * </p>
	 * <p>
	 * Each observer has its update method called with two arguments: this
	 * observable object and the arg argument.
	 * </p>
	 * 
	 * 
	 * 
	 * @param object -
	 *            any object
	 * @param setChanged
	 *            call setChanged, if true
	 */
	public void notifyObservers(Object object, boolean setChanged) {
		if (setChanged) {
			setChanged();
		}
		this.notifyObservers(object);
	}

	/**
	 * Marks this Observable object as having been changed; the hasChanged
	 * method will now return true.
	 */
	protected synchronized void setChanged() {
		changed = true;
	}
}
