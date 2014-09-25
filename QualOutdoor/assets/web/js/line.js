"use strict";
/*##################################################*/
/*  Requires chart.js. */
/*##################################################*/

/** This Javascript file is designed to allow the use of a line chart
 * inside an Android WebView. This line chart display only on series.
 * Depends upon chart.js. */

/** The maximum number of data */
var MAX_HISTORY = 500;
/** The timespan to display in milliseconds. TODO: make configurable */
var TIMESPAN = 30*1000;

/** This is the default configuration object used for the line chart */
var LINE_CONFIG = {

    // We have only one serie
    series: [ {data:[]} ],

    // Use a date format for the xAxis values
    xAxis: { type: 'datetime' },

    // No legend needed because we don't have multiple series
    legend: { enabled: false },

    
    plotOptions: {
        series: {
            states: {
                hover: {
		    // Disable hover on series
                    enabled: false
                }
            }
        }
    },
};

// Add our config to the chart.js default config
DEFAULT_CONFIG = merge_into(DEFAULT_CONFIG, LINE_CONFIG);

/** Add a new value to the serie data. Shift the data if MAX_HISTORY
 * is reached.
 * @param {Number|Array} value The value to add. Taken as y value if a
 * Number, or (x,y) value pair if Array */
function addData(value) {
    // Should we get rid of the oldest value in order to insert the
    // new one
    var shift = (chart.series[0].data.length >= MAX_HISTORY);
    // Add the value, without redrawing
    chart.series[0].addPoint(value, false, shift);
    // Recenter the view
    chart.xAxis[0].setExtremes(value[0] - TIMESPAN, value[0]);
};
