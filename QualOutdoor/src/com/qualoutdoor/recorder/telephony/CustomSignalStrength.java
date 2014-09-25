package com.qualoutdoor.recorder.telephony;

import android.os.Bundle;
import android.telephony.CellSignalStrength;

/**
 * Implementation of ISignalStrength using a Bundle
 * 
 * @author Gaborit Nicolas
 * 
 */
public class CustomSignalStrength implements ISignalStrength {

    /**
     * We encapsulate the signal values in a bundle. They are parcelable and all
     * the fields are not required to be filled.
     */
    private Bundle valuesBundle;

    /************ The bundle keys ****************/
    /** Stores the signal strength dBm value. Holds an int. */
    public static final String DBM = "dBm";
    /** Stores the signal strength asu value. Holds an int. */
    public static final String ASU = "asu";

    /** Create an empty CustomSignalStrength. */
    public CustomSignalStrength() {
        // Initialize an empty bundle
        this.valuesBundle = new Bundle();
    }

    /**
     * Create a CustomSignalStrength from an existing bundle. If initValues is
     * null, create an empty CustomSignalStrength
     * 
     * @param initValues
     *            The bundle from which the values will be read.
     */
    public CustomSignalStrength(Bundle initValues) {
        // Initialize empty
        this();
        if (initValues != null)
            // Copy the values from the given bundle
            this.valuesBundle.putAll(initValues);
    }

    /**
     * We can create a CustomSignalStrength from an Android CellSignalStrength
     * instance.
     * 
     * @param cellSS
     *            The cell signal strength to initialize from.
     */
    public CustomSignalStrength(CellSignalStrength cellSS) {
        // Initialize an empty CustomSignalStrength
        this();
        // Add the dBm value
        this.valuesBundle.putInt(DBM, cellSS.getDbm());
        // Add the asu level
        this.valuesBundle.putInt(ASU, cellSS.getAsuLevel());
    }

    // TODO add a constructor from a SignalStrength object...

    /**
     * Return this signal strength as a Bundle.
     * 
     * @return The Bundle representing this signal strength
     */
    public Bundle getBundle() {
        return valuesBundle;
    }

    @Override
    public int getDbm() {
        if (valuesBundle.containsKey(DBM))
            // Return the stored dBm value
            return valuesBundle.getInt(DBM);
        else
            // Return error value
            return UNKNOWN_DBM;
    }

    @Override
    public int getAsuLevel() {
        if (valuesBundle.containsKey(ASU))
            // Return the stored ASU level
            return valuesBundle.getInt(ASU);
        else
            // Return error value
            return UNKNOWN_ASU;
    }

}
