package com.qualoutdoor.recorder;

import android.app.Service;

/**
 * A component implementing this interface is able to provide a reference to a
 * Service of type *S*.
 * 
 * @author Gaborit Nicolas
 * 
 * @param <S>
 *            The type of the service provided
 */
public interface ServiceProvider<S extends Service> {

    /**
     * Get a reference to the service this component provides. Throws an
     * exception if the service is not available.
     */
    S getService() throws ServiceNotBoundException;

    /** Indicate whether the service is currently available */
    boolean isAvailable();

    /** Register to be informed when the service becomes available */
    void register(IServiceListener<S> listener);

    /** Unregister the given listener */
    void unregister(IServiceListener<S> listener);

    /**
     * This runtime exception is thrown when one tries to access a service while
     * it isn't available.
     */
    public class ServiceNotBoundException extends RuntimeException {
        /**
         * Generated serial version UID
         */
        private static final long serialVersionUID = 7232920276486060679L;

    }

}
