package com.qualoutdoor.recorder.network;

import java.io.InputStream;

/**
 * Abstract class parent of every class implementing file sending.
 * */
public interface Sender {
	/**
	 * method used for uploading a file
	 */
	public  boolean sendFile(String URL,String fileName,InputStream content );
	

}
