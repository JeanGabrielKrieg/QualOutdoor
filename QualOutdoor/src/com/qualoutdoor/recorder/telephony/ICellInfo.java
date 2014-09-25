package com.qualoutdoor.recorder.telephony;

import org.json.JSONObject;

/*
 * TODO : Maybe it would be best if the calls in this
 * interface were throwing an UnknownException when the
 * CellInfo doesn't know the requested data, rather than
 * responding with Integer.MAX_INT  
 */

/**
 * This is an interface for accessing a cell information.
 * 
 * @see CustomCellInfo
 * 
 * @author Gaborit Nicolas
 */
public interface ICellInfo {

    /** This cell represent an unknown type of cell */
    static final int CELL_UNKNOWN = 0;
    /** This cell represent a CDMA cell */
    static final int CELL_CDMA = 4;
    /** This cell represent a GSM cell */
    static final int CELL_GSM = 1;
    /** This cell represent a LTE cell */
    static final int CELL_LTE = 3;
    /** This cell represent a WCDMA cell */
    static final int CELL_WCDMA = 2;

    /**
     * Return the JSON object corresponding to this ICellInfo
     * 
     * @return This ICellInfo as a JSON object
     */
    JSONObject toJSON();

    /**
     * Get the cell type code. The code returned indicates the technology
     * associated with the cell.
     * 
     * @return The Cell Type code : CELL_UNKNOWN, CELL_CDMA, CELL_GSM, CELL_LTE,
     *         CELL_WCDMA
     */
    int getCellType();

    /**
     * Approximate time of this cell information in nanoseconds since boot.
     * 
     * @return The time since boot in ns. Long.MAX_VALUE if unknown.
     */
    long getTimeStamp();

    /**
     * Indicate if this cell is connected.
     * 
     * @return true if connected to the device
     */
    boolean isRegistered();

    /** Return an object that encapsulate the signal strength from this cell */
    ISignalStrength getSignalStrength();

    /**
     * Get the Cell Identity.
     * 
     * @return The CID. Integer.MAX_VALUE if unknown or not GSM, WCDMA, LTE.
     */
    int getCid();

    /**
     * Get the Location Area Code.
     * 
     * @return The LAC. Integer.MAX_VALUE if unknown or not GSM, WCDMA.
     */
    int getLac();

    /**
     * Get the Mobile Country Code.
     * 
     * @return The MCC. Integer.MAX_VALUE if unknown or not GSM, WCDMA, LTE.
     */
    int getMcc();

    /**
     * Get the Mobile Network Code.
     * 
     * @return The MNC. Integer.MAX_VALUE if unknown or not GSM, WCDMA, LTE.
     */
    int getMnc();

    /**
     * Get the primary scrambling code.
     * 
     * @return The CID. Integer.MAX_VALUE if unknown or not WCDMA.
     */
    int getPsc();

    /**
     * Get the Physical Cell ID.
     * 
     * @return The PCI. Integer.MAX_VALUE if unknown or not LTE.
     */
    int getPci();

    /**
     * Get the Tracking Area Code.
     * 
     * @return The TAC. Integer.MAX_VALUE if unknown or not LTE.
     */
    int getTac();

    /**
     * Get the Timing Advance value.
     * 
     * @return The Timing Advance. Integer.MAX_VALUE if unknown or not LTE.
     */
    int getTimingAdvance();
}
