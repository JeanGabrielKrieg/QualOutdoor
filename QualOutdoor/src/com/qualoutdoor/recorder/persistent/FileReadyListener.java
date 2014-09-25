package com.qualoutdoor.recorder.persistent;

import java.io.ByteArrayOutputStream;
/**Callback classed called when the reading of database content into a text file is over
 */

public interface FileReadyListener {

	/**database content is stored into file object*/
	void onFileReady(ByteArrayOutputStream file);
}
