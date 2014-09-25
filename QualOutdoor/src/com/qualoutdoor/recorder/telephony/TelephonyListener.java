package com.qualoutdoor.recorder.telephony;

import java.util.List;

/**
 * A listener class for monitoring changes in specific telephony states.
 * Override the methods for the state that you want to receive updates for, and
 * pass your TelephonyListener object, along with bitwise-or of the LISTEN_xxx
 * flags to `ITelephony.listen()`
 * 
 * Using the LISTEN_xxx flags inform the ITelephony implementation which events
 * you wish to monitor.
 * 
 * @author Gaborit Nicolas
 */
public class TelephonyListener {

    /*
     * Theses constant are used as bitwise masks, hence the power of two.
     */
    /** Stop listening for updates. */
    public static final int LISTEN_NONE = 0;
    /** Listen for changes to the device call state. */
    public static final int LISTEN_CALL_STATE = 1;
    /** Listen for changes to the data connection state (cellular). */
    public static final int LISTEN_DATA_STATE = 2;
    /** Listen for changes to the network signal strengths (cellular). */
    public static final int LISTEN_SIGNAL_STRENGTHS = 4;
    /** Listen for changes to the Mobile Network Code */
    public static final int LISTEN_MNC = 8;
    /** Listen for changes to the Mobile Country Code */
    public static final int LISTEN_MCC = 16;
    /** Listen for changes to observed cell info. */
    public static final int LISTEN_CELL_INFO = 32;


    /** Constructor */
    public TelephonyListener() {}

    /** The events this TelephonyListener will monitor */
    public int events() {
        return LISTEN_NONE;
    }
    
    /** Callback invoked when device call state changes. */
    public void onCallStateChanged(int state, String incomingNumber) {}

    /**
     * Callback invoked when a observed cell info has changed, or new cells have
     * been added or removed.
     */
    public void onCellInfoChanged(List<ICellInfo> cellInfo) {}

    /** Callback invoked when network type or data connection state change. */
    public void onDataStateChanged(int state, int networkType) {}

    /** Callback invoked when network signal strengths changes. */
    public void onSignalStrengthsChanged(ISignalStrength signalStrength) {}

    /** Callback invoked when the MCC changed. */
    public void onMCCChanged(int mcc) {}

    /** Callback invoked when the MNC changed. */
    public void onMNCChanged(int mnc) {}
}