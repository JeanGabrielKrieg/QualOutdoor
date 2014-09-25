package com.qualoutdoor.recorder.location;

import com.qualoutdoor.recorder.ServiceProvider;

/**
 * Interface for contexts that can hand out a ServiceProvider for
 * LocationService.
 * 
 * This interface along with other *ServiceContext* allow to share access to a
 * service, thus limiting the number of LocalConnection used.
 * 
 * @author Gaborit Nicolas
 */
public interface LocationContext {

    /** Get a LocationService Provider */
    ServiceProvider<LocationService> getLocationServiceProvider();
}
