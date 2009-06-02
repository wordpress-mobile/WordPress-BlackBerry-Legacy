package com.wordpress.task;

public class TaskWorker extends Thread {
	  private boolean stopping;
	  private final TasksRunner tasksRunner;
	  private Object currentObject;

	  public TaskWorker(final TasksRunner tasksRunner) {
	    this.tasksRunner = tasksRunner;
	  }

	  public void run() {
	    while (!stopping) {
	      synchronized (this) {
	        // is queue empty?
	        if (!tasksRunner.hasMoreTasks()) {
	          try {
	            wait();
	          } catch (final InterruptedException ignore) {
	          }
	        }
	        currentObject = tasksRunner.getNextTask();
	      }

	      if (stopping) {
	        break;
	      }

	      boolean done = false;
	      try {

	        ((Task) currentObject).execute();
	        done = true;
	      } catch (final Exception e) {
	    	  tasksRunner.appendErrorMessage(e.getMessage());
	    	  System.out.println("Error in task runner: " + e.getMessage());
	      } finally {
	        if (done) {
	          tasksRunner.taskCompleted();
	        } else {
	          stopping = true;
	          final TaskWorker next = new TaskWorker(tasksRunner);
	          tasksRunner.setWorker(next);
	          next.start();
	        }
	      }

	      currentObject = null;
	    }
	  }


	  public synchronized void quit() {
	    stopping = true;
	    notify();
	  }

	}