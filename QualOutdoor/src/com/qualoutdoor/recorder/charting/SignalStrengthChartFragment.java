package com.qualoutdoor.recorder.charting;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.qualoutdoor.recorder.IServiceListener;
import com.qualoutdoor.recorder.R;
import com.qualoutdoor.recorder.ServiceProvider;
import com.qualoutdoor.recorder.ServiceProvider.ServiceNotBoundException;
import com.qualoutdoor.recorder.telephony.ISignalStrength;
import com.qualoutdoor.recorder.telephony.TelephonyContext;
import com.qualoutdoor.recorder.telephony.TelephonyListener;
import com.qualoutdoor.recorder.telephony.TelephonyService;

/**
 * This fragment displays a LineChartView plotting the signal strength over the
 * time.
 * 
 * @author Gaborit Nicolas
 * 
 */
@SuppressLint("SetJavaScriptEnabled")
public class SignalStrengthChartFragment extends Fragment {

    /** Reference to the web view used in this fragment */
    private LineChartView chartView;

    /**
     * The Telephony Listener, which defines the behavior against telephony
     * state changes
     */
    private final TelephonyListener telListener = new TelephonyListener() {
        /** The events that are monitored */
        public int events() {
            return TelephonyListener.LISTEN_SIGNAL_STRENGTHS;
        }

        public void onSignalStrengthsChanged(ISignalStrength signalStrength) {
            // Get dBm value
            int value = signalStrength.getDbm();
            // If the signal strength value is known
            if (value != ISignalStrength.UNKNOWN_DBM) {
                // Get the current time
                long date = System.currentTimeMillis();

                // Add a the new signalStrength value to the chart
                chartView.addData(date, value);

            }
        };
    };

    /** The TelephonyService Provider given by the activity */
    private ServiceProvider<TelephonyService> telephonyService;
    /**
     * The service listener defines the behavior when the service becomes
     * available
     */
    private final IServiceListener<TelephonyService> telServiceListener = new IServiceListener<TelephonyService>() {
        @Override
        public void onServiceAvailable(TelephonyService service) {
            // Register the telephony listener
            service.listen(telListener, telListener.events());
        }
    };

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        // Inflate the WebView from the xml layout file
        chartView = (LineChartView) inflater.inflate(
                R.layout.fragment_chart_signal_strength, container, false);

        return chartView;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            // This cast makes sure that the container activity has implemented
            // TelephonyContext
            TelephonyContext telephonyContext = (TelephonyContext) getActivity();

            // Retrieve the service connection
            telephonyService = telephonyContext.getTelephonyServiceProvider();
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement " + TelephonyContext.class.toString());
        }
    }

    @Override
    public void onResume() {
        // Tell we want to be informed when services become available
        telephonyService.register(telServiceListener);
        super.onStart();
    }

    @Override
    public void onPause() {
        super.onPause();
        // If needed unregister our telephony listener
        try {
            telephonyService.getService().listen(telListener,
                    TelephonyListener.LISTEN_NONE);
        } catch (ServiceNotBoundException e) {}

        // Unregister the services listeners
        telephonyService.unregister(telServiceListener);
    }
}
