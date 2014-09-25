package com.qualoutdoor.recorder.network;

import java.io.File;


/**
 * Callback class, called by DataSendingManager objects when sending is over
 */

public interface SendCompleteListener {
	/**
	 * Method called by DataSendingManager when it finishes sending file
	 * 
	 * @param protocole : protocol used for sending
	 * @param filesSended : file sent during sending
	 * @param result : whether sending succeeded or not
	 */
	void onTaskCompleted(String protocole,File fileSent, boolean result);

}
