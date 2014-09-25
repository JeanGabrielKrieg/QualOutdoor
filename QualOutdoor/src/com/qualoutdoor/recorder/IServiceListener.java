package com.qualoutdoor.recorder;

import android.app.Service;

/**
 * A listener class to monitor when a service S becomes available. Implement the
 * method `onServiceAvailable()` to define your behavior when the service
 * becomes available.
 * 
 * @author Gaborit Nicolas
 */
public interface IServiceListener<S extends Service> {
    /** Callback made when the service becomes available */
    void onServiceAvailable(S service);
}
