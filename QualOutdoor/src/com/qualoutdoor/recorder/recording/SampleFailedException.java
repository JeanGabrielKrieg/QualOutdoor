package com.qualoutdoor.recorder.recording;

/**
 * This exception is thrown when a sample request has failed. Possible reasons are :
 * 
 * - The available data were too old
 * 
 * - The data could not be fetched
 * 
 * @author Gaborit Nicolas
 */
public class SampleFailedException extends Exception {

    /**
     * Generated serial version UID
     */
    private static final long serialVersionUID = 1119624279807950612L;

    public SampleFailedException(String message) {
        super(message);
    }

}
