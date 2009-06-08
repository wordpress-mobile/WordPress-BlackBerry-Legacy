package com.wordpress.utils;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import javax.microedition.io.HttpConnection;

public class LocalHttpConn implements HttpConnection {
	private InputStream is = null;

	public LocalHttpConn(String html){
		is = new ByteArrayInputStream(html.getBytes());
	}
	
	public LocalHttpConn(byte[] bits){
		is = new ByteArrayInputStream(bits);
	}
	
	public long getDate() throws IOException {
		return 0;
	}

	public long getExpiration() throws IOException {
		return 0;
	}

	public String getFile() {
		return null;
	}

	public String getHeaderField(String name) throws IOException {
		return null;
	}

	public String getHeaderField(int n) throws IOException {
		return null;
	}

	public long getHeaderFieldDate(String name, long def)
			throws IOException {
		return 0;
	}

	public int getHeaderFieldInt(String name, int def) throws IOException {
		return 0;
	}

	public String getHeaderFieldKey(int n) throws IOException {
		return null;
	}

	public String getHost() {
		return null;
	}

	public long getLastModified() throws IOException {
		return 0;
	}

	public int getPort() {
		return 0;
	}

	public String getProtocol() {
		return null;
	}

	public String getQuery() {
		return null;
	}

	public String getRef() {
		return null;
	}

	public String getRequestMethod() {
		return null;
	}

	public String getRequestProperty(String key) {
		return null;
	}

	public int getResponseCode() throws IOException {
		return 200;
	}

	public String getResponseMessage() throws IOException {
		return "OK";
	}

	public String getURL() {
		return "";
	}

	public void setRequestMethod(String method) throws IOException {
		
	}

	public void setRequestProperty(String key, String value)
			throws IOException {
		
	}

	public String getEncoding() {
		return "UTF-8";
	}

	public long getLength() {
		try {
			if (is != null )
				return is.available();
			else return 0;
		} catch (IOException e) {
		}
		return 0L;
	}

	public String getType() {
		return "text/html";
	}

	public DataInputStream openDataInputStream() throws IOException {
		return new DataInputStream(is);
	}

	public InputStream openInputStream() throws IOException {
		return is;
	}

	public void close() throws IOException {
		is.close();
		
	}

	public DataOutputStream openDataOutputStream() throws IOException {
		return null;
	}

	public OutputStream openOutputStream() throws IOException {
		return null;
	}
	
}