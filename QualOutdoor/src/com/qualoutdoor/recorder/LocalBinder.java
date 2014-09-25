package com.qualoutdoor.recorder;

import android.os.Binder;

/**
 * A local binder for a local service S. Allows to directly get a reference to
 * the bound service. This makes it easy then for the component to access the
 * service methods. Caution, this works only if the service and the component
 * binding to it are in the same process.
 * 
 * @author Gaborit Nicolas
 * 
 * @param <S>
 *            The class of the Service for which this Binder is used
 */
public class LocalBinder<S> extends Binder {

    /** The service to give when `getService()` is called */
    private S myService;

    /**
     * A constructor for the binder to know its service
     * 
     * @param service
     *            The service that will be given when `getService()` is called
     */

    public LocalBinder(S service) {
        super();
        myService = service;
    }

    /**
     * Get the service that provided this binder
     * 
     * @return The service that created emitted this LocalBinder
     */
    public S getService() {
        return myService;
    }
}
