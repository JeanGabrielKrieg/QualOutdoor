package com.qualoutdoor.recorder.charting;

import java.util.List;

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
import com.qualoutdoor.recorder.telephony.ICellInfo;
import com.qualoutdoor.recorder.telephony.TelephonyContext;
import com.qualoutdoor.recorder.telephony.TelephonyListener;
import com.qualoutdoor.recorder.telephony.TelephonyService;

/**
 * This fragment displays the list of the visible cells inside a CellsChartView.
 * Its parent activity must implements the TelephonyContext interface.
 * 
 * @author Gaborit Nicolas
 */
public class NeighborsChartFragment extends Fragment {

    /** Reference to the web view used in this fragment */
    private BarChartView chartView;

    /** The BarChartAdapter */
    CellsChartAdapter cellChartAdapter = new CellsChartAdapter();

    /**
     * The Telephony Listener, which defines the behavior against telephony
     * state changes
     */
    private TelephonyListener telListener = new TelephonyListener() {
        /** The events monitored by the Telephony Listener */
        @Override
        public int events() {
            return TelephonyListener.LISTEN_CELL_INFO;
        }

        @Override
        public void onCellInfoChanged(List<ICellInfo> cellInfos) {
            // Update the cells list
            cellChartAdapter.updateDataSet(cellInfos);
            // Inform the webview that data have changed
            chartView.updateData();
        }
    };

    /** The TelephonyService Provider given by the activity */
    private ServiceProvider<TelephonyService> telephonyService;
    /**
     * The service listener defines the behavior when the service becomes
     * available
     */
    private IServiceListener<TelephonyService> telServiceListener = new IServiceListener<TelephonyService>() {
        @Override
        public void onServiceAvailable(TelephonyService service) {
            // Register the telephony listener
            telephonyService.getService().listen(telListener,
                    telListener.events());
        }
    };

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
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        // Inflate the fragment view
        chartView = (BarChartView) inflater.inflate(
                R.layout.fragment_chart_neighbors, container, false);
        
        // Set the BarChartAdapter
        chartView.setBarChartAdapter(cellChartAdapter);
        
        return chartView;
    }

    @Override
    public void onResume() {
        // Tell we want to be informed when services become available
        telephonyService.register(telServiceListener);
        super.onStart();
    }

    @Override
    public void onPause() {
        // If needed unregister our telephony listener
        try {
            telephonyService.getService().listen(telListener,
                    TelephonyListener.LISTEN_NONE);
        } catch (ServiceNotBoundException e) {}
        // Unregister the services listeners
        telephonyService.unregister(telServiceListener);
        super.onPause();
    }
}