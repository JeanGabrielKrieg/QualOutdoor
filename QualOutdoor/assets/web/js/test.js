$(function () {
    $('#container').highcharts({
        chart: {
            type: 'bar'
        },

        plotOptions: {
            series: {
                dataLabels: {
                    enabled: true,
                    align: 'right',
                    color: '#FFFFFF',
                    x: -10,
                    style: {
                        fontSize: '1.3em'
                    }
                },
                pointPadding: 0.1,
                groupPadding: 0
            },

	    bar: {
		// The value defining the base of the bars
		threshold: -120
	    }
        },

        xAxis: {
	    // Display categories as x axis
            type: 'category'
            }
        },

        legend: {
            enabled: false
        },

        series: [{
            data: [{
                name: 'GSM',
                color: '#00FF00',
                y: 2,
                dataLabels: {
                    format: 'test',
                    style: {
                        fontWeight: 'bold'
                    }
                }
            }, {
                name: 'LTE',
                color: '#FF00FF',
                y: 5
            }, {
                name: 'GSM',
                color: '#00FF00',
                y: 2,
                dataLabels: {
                    format: 'test',
                    style: {
                        fontWeight: 'bold'
                    }
                }
            }, {
                name: 'LTE',
                color: '#FF00FF',
                y: 5
            }, {
                name: 'GSM',
                color: '#00FF00',
                y: 2,
                dataLabels: {
                    format: 'test',
                    style: {
                        fontWeight: 'bold'
                    }
                }
            }, {
                name: 'LTE',
                color: '#FF00FF',
                y: 5
            }, {
                name: 'GSM',
                color: '#00FF00',
                y: 2,
                dataLabels: {
                    format: 'test',
                    style: {
                        fontWeight: 'bold'
                    }
                }
            }, {
                name: 'LTE',
                color: '#FF00FF',
                y: 5
            }]
        }]
    });
});
