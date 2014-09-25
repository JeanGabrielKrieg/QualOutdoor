package com.qualoutdoor.recorder.charting;

import android.content.Context;
import android.util.AttributeSet;

public class BarChartView extends HighChartView {

    /** The HTML file for bar charts */
    private static final String HTML_FILE = "file:///android_asset/web/bar-chart.html";

    /** The adapter that provide all the data for the bar chart */
    private BarChartAdapter adapter;

    public BarChartView(Context context, AttributeSet attrs) {
        super(context, attrs);
        // Load bar chart HTML file
        loadUrl(HTML_FILE);
    }

    /** Attach an adapter to this view */
    public void setBarChartAdapter(BarChartAdapter adapter) {
        this.adapter = adapter;
        // Add the interface to the WebView
        addJavascriptInterface(adapter, adapter.NAME);
    }

    /** Call this method when the data of the adapter changed */
    public void updateData() {
        // Ask javascript to update his data
        execJS("updateData()");
    }
}
