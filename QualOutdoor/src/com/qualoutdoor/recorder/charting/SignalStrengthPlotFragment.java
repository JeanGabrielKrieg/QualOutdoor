package com.qualoutdoor.recorder.charting;

import java.text.DecimalFormat;
import java.util.Observable;
import java.util.Observer;

import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.androidplot.Plot;
import com.androidplot.xy.BoundaryMode;
import com.androidplot.xy.LineAndPointFormatter;
import com.androidplot.xy.SimpleXYSeries;
import com.androidplot.xy.XYPlot;
import com.androidplot.xy.XYStepMode;
import com.qualoutdoor.recorder.R;

public class SignalStrengthPlotFragment extends Fragment {

	private static final int SAMPLE_RATE = 1000;
	private static final int HISTORY_SIZE = 60;
	private static final int MIN_SS = 0; //< Asu level
	private static final int MAX_SS = 31; //< Asu level

	// redraws a plot whenever an update is received:
	private class MyPlotUpdater implements Observer {
		Plot plot;
		SimpleXYSeries series;

		public MyPlotUpdater(Plot plot, SimpleXYSeries series) {
			this.plot = plot;
			this.series = series;
		}

		@Override
		public void update(Observable o, Object arg) {
			// get rid the oldest sample in history:
			if (series.size() > HISTORY_SIZE) {
				series.removeFirst();
			}
			// Get the signal strength as Asu level
			int ssAsu = ssSampler.getSignalStrength()
					.getGsmSignalStrength();
			// add the latest history sample:
			series.addLast(null, ssAsu);
			plot.redraw();
		}
	}
	
	private TelephonyManager telephonyManager;
	private SignalStrengthSampler ssSampler;
	private Thread thread;
	private XYPlot dynamicPlot;
	private MyPlotUpdater plotUpdater;
	private SimpleXYSeries ssLvlSeries;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View rootView = inflater.inflate(R.layout.fragment_dynamic_plot,
				container, false);
		return rootView;
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		// get handles to our View defined in layout.xml:
		dynamicPlot = (XYPlot) getView().findViewById(R.id.dynamicPlot);

		dynamicPlot.setRangeBoundaries(MIN_SS, MAX_SS, BoundaryMode.FIXED);
		dynamicPlot.setDomainBoundaries(0, HISTORY_SIZE, BoundaryMode.FIXED);

		ssLvlSeries = new SimpleXYSeries("Signal Strength");
		ssLvlSeries.useImplicitXVals();

		LineAndPointFormatter formatter = new LineAndPointFormatter(Color.rgb(
				100, 100, 200), null, null, null);
		formatter.getLinePaint().setStrokeWidth(3);
		dynamicPlot.addSeries(ssLvlSeries, formatter);

		dynamicPlot.setDomainStepMode(XYStepMode.INCREMENT_BY_VAL);
		dynamicPlot.setDomainStepValue(HISTORY_SIZE / 6);
		dynamicPlot.setRangeStepValue((MAX_SS - MIN_SS)/10);

		// Set the label text of range and domain
		dynamicPlot.setDomainLabel("Sample Index");
		dynamicPlot.setRangeLabel("DBm");
		// Sets the dimensions of the widget to exactly contain the text
		// contents
		dynamicPlot.getDomainLabelWidget().pack();
		dynamicPlot.getRangeLabelWidget().pack();

		// Set values display format
		dynamicPlot.setRangeValueFormat(new DecimalFormat("#"));
		dynamicPlot.setDomainValueFormat(new DecimalFormat("#"));

		// only display whole numbers in domain labels
		dynamicPlot.getGraphWidget().setDomainValueFormat(
				new DecimalFormat("0"));

		// Create a new SignalStrengthSampler
		ssSampler = new SignalStrengthSampler(SAMPLE_RATE);

		// Obtain a TelephonyManager instance
		telephonyManager = (TelephonyManager) getActivity().getSystemService(
				Context.TELEPHONY_SERVICE);
		// Register the SignalStrengthSampler listener to our TelephonyManager
		telephonyManager.listen(ssSampler.getPhoneStateListener(),
				PhoneStateListener.LISTEN_SIGNAL_STRENGTHS);

		// Create a PlotUpdater
		plotUpdater = new MyPlotUpdater(dynamicPlot, ssLvlSeries);

		// Register it as an observer of SignalStrengthSampler
		ssSampler.addObserver(plotUpdater);
		
		// Start the ssSampler
		thread = new Thread(ssSampler);
		thread.start();

	}

	@Override
	public void onDestroy() {
		// Unregister our listener
		telephonyManager.listen(ssSampler.getPhoneStateListener(),
				PhoneStateListener.LISTEN_NONE);
		// Stop the sampler
		thread.interrupt();
		super.onDestroy();
	}
}
