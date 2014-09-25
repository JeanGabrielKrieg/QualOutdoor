package com.qualoutdoor.recorder.persistent;

/**
 * Exception thrown when a issue occurs while information collecting to fill database
 **/

public class CollectMeasureException extends Exception {
	
	private static final long serialVersionUID = 1L;

	public CollectMeasureException(String message) {
        super(message);
  }

}
