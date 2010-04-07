package com.wordpress.utils.csv;

import java.io.ByteArrayInputStream;
import java.io.EOFException;
import java.io.IOException;
import java.util.Vector;

import net.rim.device.api.io.LineReader;
import net.rim.device.api.util.ToIntHashtable;

public class StatsParser {
	
	private final byte[] data;
	private Vector parsedStatsData = new Vector();
	private ToIntHashtable columnMap;
	private int _internalPointer = 0;
		
	public boolean hasNext() {
		if(_internalPointer >= parsedStatsData.size())
			return false;
		else return true;
	}
	
	public String[] next() {
		String[] tmp = (String[])parsedStatsData.elementAt(_internalPointer);
		_internalPointer++;
		return tmp;
	}
	
	//reset the pointer
	public void reset() {
		_internalPointer = 0;
	}

	
	public int getColumnIndex(String columnName) {
		int idxColumn = -1;
		if (columnMap.get(columnName) != -1) {
			idxColumn =  columnMap.get(columnName);
		}
		return idxColumn;
	}
	
	public void parseAll() throws IOException {
		String tmpString = new String (data);
		if (tmpString.startsWith("Error: ")) {
			LineReader br = new LineReader(new ByteArrayInputStream(data));
			String line = "";
			try {
				line = new String(br.readLine());
			} catch (EOFException eof) {
				throw new IOException("Unexpected error while parsing stats data");
			} catch (IOException ioe) {
				throw new IOException("Unexpected error while parsing stats data");
			}
			
			throw new IOException(line);
		}
		tmpString = null;
		
		columnMap = new ToIntHashtable();
		ByteArrayInputStream bais = new ByteArrayInputStream(data);
		CSVReader reader = new CSVReader(bais, ',', '"', '"');
		String[] nextLine;

		// reading the header
		if ((nextLine = reader.readNext()) != null) {
			for (int i = 0; i < nextLine.length; i++) {
				String columnName = nextLine[i];
				columnMap.put(columnName, i);
			}
		}
		// end reading header

		//reading the stats data
		while ((nextLine = reader.readNext()) != null) {
			parsedStatsData.addElement(nextLine);				
		} 
	}
	
	public StatsParser (byte[] data) {
		this.data = data;
	}
}