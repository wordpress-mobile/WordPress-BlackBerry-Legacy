package com.wordpress.utils.csv;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.Hashtable;
import java.util.Vector;

import com.wordpress.utils.log.Log;

public class StatsParser {
	
	private Hashtable columnMap;
	private Vector statsData = new Vector();
	private int _internalPointer = 0;
		
	public boolean hasNext() {
		if(_internalPointer >= statsData.size())
			return false;
		else return true;
	}
	
	public String[] next() {
		String[] tmp = (String[])statsData.elementAt(_internalPointer);
		_internalPointer++;
		return tmp;
	}
	
	//reset the pointer
	public void reset() {
		_internalPointer = 0;
	}

	
	public int getColumnIndex(String columnName) {
		int idxColumn = -1;
		if (columnMap.get(columnName) != null) {
			Integer tmp = (Integer) columnMap.get(columnName);
			idxColumn = tmp.intValue();
		}
		return idxColumn;
	}
	
	
	public StatsParser (String data) {
		columnMap = new Hashtable();
		ByteArrayInputStream bais = new ByteArrayInputStream(data.getBytes());
		CSVReader reader = new CSVReader(bais, ',', '"', '"');
		String[] nextLine;
		try {
			// reading the header
			if ((nextLine = reader.readNext()) != null) {
				for (int i = 0; i < nextLine.length; i++) {
					String columnName = nextLine[i];
					columnMap.put(columnName, new Integer(i));
				}
			}
			// end reading header
			
			//reading the stats data
			while ((nextLine = reader.readNext()) != null) {
				statsData.addElement(nextLine);				
			} 
		}catch (IOException e) {
			Log.error(e, "Error while parsing stats data");
		}
	}
}