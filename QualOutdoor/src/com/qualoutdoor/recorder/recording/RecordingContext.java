package com.qualoutdoor.recorder.recording;

import com.qualoutdoor.recorder.ServiceProvider;

/**
 * Interface for context that can hand out a ServiceProvider for
 * RecordingService.
 * 
 * This interface along with other *ServiceContext* allow to share access to a
 * service, thus limiting the number of LocalConnection used.
 * 
 * @author Gaborit Nicolas
 */
public interface RecordingContext {

    /** Get a RecordingService Provider */
    ServiceProvider<RecordingService> getRecordingServiceProvider();
}
