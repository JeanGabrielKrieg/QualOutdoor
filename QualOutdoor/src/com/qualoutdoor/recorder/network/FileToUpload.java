package com.qualoutdoor.recorder.network;

import java.io.InputStream;
/**
 * This class represents a file to be upload
 */

public class FileToUpload {
	
    /**the remote storage name of the file*/
	private String fileName;
	/**reader of file content*/
	private InputStream content;
	
	
	/**Constructor*/
	public FileToUpload(String fileName, InputStream content) {		
		this.fileName = fileName;
		this.content = content;
	}
	
	/**get file's name*/
	public String getFileName() {
		return fileName;
	}
	
	/**set file's name*/
	public void setFileName(String fileName) {
		this.fileName = fileName;
	}
	
	/**get file's content*/
	public InputStream getContent() {
		return content;
	}
	
	/**set file's content*/
	public void setContent(InputStream content) {
		this.content = content;
	}
	
	

}
