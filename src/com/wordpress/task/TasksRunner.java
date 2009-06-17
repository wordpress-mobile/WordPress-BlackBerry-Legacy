package com.wordpress.task;

import com.wordpress.utils.Queue;
import com.wordpress.utils.log.Log;


public class TasksRunner {
  private final Queue executionQueue;

  private Object currentObject;

  private boolean stopping;
  private boolean started;
  private boolean isError;
  private StringBuffer errorMsg = new StringBuffer();

  private TaskWorker worker;

  public TasksRunner(final Queue tasksQueue) {
    executionQueue = tasksQueue;
    worker = new TaskWorker(this);
  }

  public boolean hasMoreTasks() {
    return !executionQueue.isEmpty();
  }

  public Object getNextTask() {
    currentObject = executionQueue.pop();
    return currentObject;
  }

  /**
   * Enqueue task for execution.
   */
  public void enqueue(final Task o) {
    if (!stopping && (!o.equals(currentObject)) && executionQueue.find(o) == null) {
      executionQueue.push(o);
      if (!started) {
        return;
      }
      synchronized (worker) {
        worker.notify();
      }
    }
  }

  public void quit() {
    stopping = true;
    synchronized (worker) {
      worker.quit();
    }
  }

  
  public void setWorker(final TaskWorker next) {
    worker = next;
  }

  public void taskCompleted() {
    currentObject = null;
  }

  public void startWorker() {
    started = true;
    synchronized (worker) {
      worker.start();
    }
  }
  
  public synchronized void resetError() {
	  isError = false;
	  errorMsg = new StringBuffer();
  }
  
  public boolean isError() {
	  return isError;
  }
  
  public synchronized void appendErrorMessage(String msg) {
	  errorMsg.append(msg+"\n");
  }
  
  public String getErrorMessage(String msg) {
	  return errorMsg.toString();
  }
}