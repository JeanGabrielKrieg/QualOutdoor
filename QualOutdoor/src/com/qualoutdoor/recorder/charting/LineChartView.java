package com.qualoutdoor.recorder.charting;

import android.content.Context;
import android.util.AttributeSet;

public class LineChartView extends HighChartView {

    /** The HTML file for line charts */
    private static final String HTML_FILE = "file:///android_asset/web/line-chart.html";

    public LineChartView(Context context, AttributeSet attrs) {
        super(context, attrs);
        // Load line chart HTML file
        loadUrl(HTML_FILE);
    }

    /**
     * Add a new data to the line chart
     * @param date Date of the data in milliseconds since UTC 1970...
     * @param value The new value
     */
    public void addData(long date, int value) {
        execJS("addData([" + date + "," + value + "])");
    }

}
