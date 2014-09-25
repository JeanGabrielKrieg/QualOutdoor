package com.qualoutdoor.recorder.telephony;

/**
 * This is an interface for accessing a signal strength information.
 * 
 * @see CustomSignalStrength
 * 
 * @author Gaborit Nicolas
 */
public interface ISignalStrength {
    
    /** Error value for dBm level */
    public static final int UNKNOWN_DBM = Integer.MAX_VALUE;
    /** Error value for asu level */
    public static final int UNKNOWN_ASU = 99;
    
    /** Get the RSSI value as dBm. Integer.MAX_VALUE if unknown */
    int getDbm();

    /**
     * Get the signal level as an asu value. LTE and CDMA values are between
     * 0..97. GSM and WCDMA values are between 0..31. 99 is unknown.
     */
    int getAsuLevel();

}
