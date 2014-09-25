package com.qualoutdoor.recorder.persistent;

import android.util.SparseArray;

/**
 * A class that encapsulate the data required for inserting a sample in the
 * database.
 * 
 * An instance of this class contains :
 * 
 * - A latitude and longitude
 * 
 * - A measure context (Group, User, MCC, MNC, NTC)
 * 
 * - A sparse array of measured data : (CID, RSSI, Call, Upload, Download)
 * 
 * @author Gaborit Nicolas
 */
public class Sample {
    /** The measure context of the sample */
    public MeasureContext measureContext;
    /** The metrics that have been sampled and their values */
    public SparseArray<String> data;
    /** Location of the sample */
    public double latitude;
    public double longitude;

    /**
     * Create a Sample with the given content.
     * 
     * @param measureContext
     *            The context of the sample
     * @param data
     *            The metrics that were sampled
     * @param latitude
     *            The latitude were the sample was made
     * @param longitude
     *            The longitude were the sample was made
     */
    public Sample(MeasureContext measureContext, SparseArray<String> data,
            double latitude, double longitude) {
        this.measureContext = measureContext;
        this.data = data;
        this.latitude = latitude;
        this.longitude = longitude;
    }
}
