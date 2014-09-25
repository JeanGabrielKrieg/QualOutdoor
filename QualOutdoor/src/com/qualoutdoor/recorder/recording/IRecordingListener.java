package com.qualoutdoor.recorder.recording;

/**
 * A listener interface for monitoring the state/progress of the
 * RecordingService. Override the callback to implements your specific behavior.
 * 
 * @author Gaborit Nicolas
 */
public interface IRecordingListener {
    /** Callback invoked when the recording starts or stops. */
    void onRecordingChanged(boolean isRecording);
}