package com.qualoutdoor.recorder.charting;

import android.webkit.JavascriptInterface;

/**
 * This interface is given to a Javascript bar chart, providing the data to
 * display.
 * 
 * @author Gaborit Nicolas
 */
public interface BarChartAdapter {

    /** This is the name of the component made available in Javascript */
    final static String NAME = "BarData";

    /**
     * Returns the value of the data with index i
     * 
     * @param i
     *            The index of the data
     * @return The value associated to data i
     */
    @JavascriptInterface
    float getValue(int i);

    /**
     * Returns the name (or category) to display on the axis for the data i
     * @param i
     *            Index of the data
     * @return The name of the category
     */
    @JavascriptInterface
    String getName(int i);

    /**
     * Returns the label to display over the bar of the data i
     * 
     * @param i
     *            Index of the data
     * @return
     */
    @JavascriptInterface
    String getLabel(int i);

    /**
     * Returns the tooltip of the corresponding data
     * @param i The data index
     * @return The tooltip text
     */
    @JavascriptInterface
    String getTooltip(int i);

    
    /**
     * Returns the group id of the data. The groups are used to defines which
     * color will be used to display the associated bar.
     * 
     * @param i
     *            Index of the data
     * @return The group to which the data belongs to
     */
    @JavascriptInterface
    int getGroup(int i);

    /**
     * Returns the size of the data to display
     * 
     * @return The size of the underlying data
     */
    @JavascriptInterface
    int size();

}
