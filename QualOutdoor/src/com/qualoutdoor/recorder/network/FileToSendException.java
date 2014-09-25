package com.qualoutdoor.recorder.network;

/**
 * Exception thrown when the file to upload doesn't follow a correct format
 */
public class FileToSendException extends Exception {

	
	private static final long serialVersionUID = 1L;

	public FileToSendException(String message) {
        super(message);
  }
}
