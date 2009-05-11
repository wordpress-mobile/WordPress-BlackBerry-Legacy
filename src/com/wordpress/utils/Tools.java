package com.wordpress.utils;

import java.util.Vector;

public class Tools {
	
	  /**
	   * Convert a vector to a string array.
	   * 
	   * @param v
	   *          vector to convert
	   * @return the string array
	   */
	  public static String[] toStringArray(final Vector v) {
	    final String[] res = new String[v.size()];
	    v.copyInto(res);
	    return res;
	  }
	  

	  public static void bubbleSort(final int array[]) {
		    final int elements = array.length;
		    int i;
		    int j;
		    int t = 0;
		    for (i = 0; i < elements; i++) {
		      for (j = 1; j < (elements - i); j++) {
		        if (array[j - 1] > array[j]) {
		          t = array[j - 1];
		          array[j - 1] = array[j];
		          array[j] = t;
		        }
		      }
		    }
		  }
	  
}
