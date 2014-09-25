package com.qualoutdoor.recorder.persistent;

import java.util.concurrent.Semaphore;

/**
 * This class give a static access to the unique instance of the database
 * Semaphore
 * @author Gaborit Nicolas
 */
public final class DBSemaphore {

    /**
     * The semaphore limiting access to the database to one at a time, with
     * fairness (FIFO ordering)
     */
    public static final Semaphore ref = new Semaphore(1, true);

    /** Not meant to be instantiated */
    private DBSemaphore() {}

}
