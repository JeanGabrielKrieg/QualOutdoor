package com.qualoutdoor.recorder.telephony;

import com.qualoutdoor.recorder.ServiceProvider;
import com.qualoutdoor.recorder.telephony.TelephonyService;

/**
 * Interface for context that can hand out a ServiceProvider for
 * TelephonyService.
 * 
 * This interface along with other *ServiceContext* allow to share access to a
 * service, thus limiting the number of LocalConnection used.
 * 
 * @author Gaborit Nicolas
 */
public interface TelephonyContext {

    /** Get a TelephonyService Provider */
    ServiceProvider<TelephonyService> getTelephonyServiceProvider();
}
