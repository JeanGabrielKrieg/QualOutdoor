"use strict";
/*##################################################*/
/*  Requires chart.js. */
/*##################################################*/

/** This Javascript file is designed to allow the use of a bar chart
 * inside an Android WebView. Depends upon chart.js. */

/** This JavascriptInterface allows access to the data to display */
var BarData;

/** This is the default configuration object used for the bar chart */
var CELLS_CONFIG = {
    
    chart: {
	// We use a bar chart
	type: 'bar'
    },

    tooltip: {
	// Enable tooltip
	enabled: true,
	// We format the tooltip with a callback to BarData
        formatter: function () {
	    // Get the tooltip of the x-th value
            return BarData.getTooltip(this.x);
        }
    },
    
    // We have only one serie
    series: [ {data:[]} ],

    xAxis: {
	// Display categories for x axis
        type: 'category',
        labels: {
            style: {
                fontSize: '13px',
                fontFamily: 'Verdana, sans-serif'
            }
        }
    },

    // No legend needed because we don't use series conventionally
    legend: { enabled: false },

    plotOptions: {
        series: {
	    // Display CID or PSC as a label over the bars
            dataLabels: {
                enabled: false, // TODO it does not work well...
                align: 'right',
                color: '#FFFFFF',
		x: -10,
                style: {
                    fontSize: '1.3em'
                }
            },
	    // Reduce the padding between bars
            pointPadding: 0.1,
            groupPadding: 0
	},
        states: {
            hover: {
		// Disable hover on series
                enabled: false
            }
        },
	bar: {
	    // The value defining the base of the bars
	    threshold: -140 // TODO: define as XML configuration
	}
    }
};

// Add our config to the chart.js default config
DEFAULT_CONFIG = merge_into(DEFAULT_CONFIG, CELLS_CONFIG);

/** Called by Android when new data are available. */
function updateData() {
    // Get data size
    var size = BarData.size();
    // Construct the new data
    var newData = [];
    for (var i = 0; i < size; i++) {
	// Construct the data
	var data = {
	    // Name to display on x axis
	    name: BarData.getName(i),
	    // The color of the group of data this data belongs to
	    color: COLOR_SCHEME[BarData.getGroup(i)%COLOR_SCHEME.length],
	    // The actual value of the bar
	    y: BarData.getValue(i),
	    // The label to display over the bar
	    dataLabels: {format: BarData.getLabel(i)}
	};

	// Add the data
	newData.push(data);
    }
    
    // Update the chart data
    chart.series[0].setData(newData);
}
