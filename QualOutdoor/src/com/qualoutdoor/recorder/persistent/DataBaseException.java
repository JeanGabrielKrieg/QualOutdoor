package com.qualoutdoor.recorder.persistent;

/** Exception thrown if a problem occurred while manipulating the database */
public class DataBaseException extends Exception {

    /**
     * Generated serial version UID
     */
    private static final long serialVersionUID = -1648154925544028898L;

    public DataBaseException(String message) {
        super(message);
    }

}
