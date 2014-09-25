"use strict";

/** This Javascript file is designed to allow the use of an HighChart chart
 * inside an Android WebView */

/** This variable reference the chart object */
var chart;

/** Colorblind friendly palette, with colors close from ALSETT color scheme */
var COLOR_SCHEME = ['#00557c', '#d56500', '#30005f', '#d5c400', '#ac0024'];

/* Set some global options */
Highcharts.setOptions({
    // We want to display date according to our local timezone
    global: {useUTC:false},
    colors: COLOR_SCHEME
});

/** This is the default configuration object used for the line chart */
var DEFAULT_CONFIG = {
    // Render the chart in the container div
    chart: { renderTo: 'container' },

    // Disable tooltips when selecting a data
    tooltip: { enabled: false },

    // Disable the lower right 'Highcharto.com' credit
    credits: { enabled: false },

    plotOptions: { 	
	series: { 
	// Disable initial animation	    
	    animation: false,  
	    // Disable markers
            marker: {
                enabled: false
            }
	}
    }
};

/** This function can be called by Android in order to initialize the
 * chart with the given config (in addition to the static
 * configuration defined here).
 * @param {Object} config An option object as defined in the
 * HighCharts documentation */
function initConfig(config) {
    // Merge the given config into the default one
    var finalConfig = merge_into(DEFAULT_CONFIG, config);
    // Initialize the chart with this config
    chart = new Highcharts.Chart(finalConfig);
}

/**
 * Recursively merge properties of two objects, overwriting obj1
 * properties with obj2 properties into a new object.
 * @param {Object} obj1 The destination object, used as base for the merging.
 * @param {Object} obj2 The object whose properties are to be merged into the
 * first object.
 * @return {Object} The object resulting from the merging
 */
function merge_into(obj1, obj2) {
    // Initialize the merged object as empty
    var result = {};

    // Deep copy the destination object
    for(var prop in obj1) {
	// Set the property
	result[prop] = obj1[prop];
    }
    
    // Now import each property from the second object
    for (var prop in obj2) {
	try {
	    // Property in destination object set; update its value.
	    if (result[prop].constructor == Object) {
		// The property is a nested Object : deep merge
		result[prop] = merge_into(result[prop], obj2[prop]);
	    } else {
		// The value is overwritten
		result[prop] = obj2[prop];
	    }
	} catch(e) {
	    // Property in destination object not set; create it and
	    // set its value.
	    result[prop] = obj2[prop];
	}
    }
    return result;
};

/** Function that load a script from a file.
 * @param {String} url The javascript file to load
 * @param {Function} callback The callback to execute when loaded */
function loadScript(url, callback)
{
    console.log("loading script " + url);
    // Add a script element to head
    var head = document.getElementsByTagName('head')[0];
    var script = document.createElement('script');
    script.type = 'text/javascript';
    script.src = url;

    // then bind the event to the callback function 
    // there are several events for cross browser compatibility
    //script.onreadystatechange = callback;
    script.onload = callback;

    // fire the loading
    head.appendChild(script);
}
