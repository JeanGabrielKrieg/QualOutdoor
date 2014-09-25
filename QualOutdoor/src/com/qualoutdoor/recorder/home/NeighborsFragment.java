package com.qualoutdoor.recorder.home;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ExpandableListAdapter;
import android.widget.ExpandableListView;
import android.widget.TextView;

import com.qualoutdoor.recorder.IServiceListener;
import com.qualoutdoor.recorder.R;
import com.qualoutdoor.recorder.ServiceProvider;
import com.qualoutdoor.recorder.ServiceProvider.ServiceNotBoundException;
import com.qualoutdoor.recorder.telephony.ICellInfo;
import com.qualoutdoor.recorder.telephony.TelephonyContext;
import com.qualoutdoor.recorder.telephony.TelephonyListener;
import com.qualoutdoor.recorder.telephony.TelephonyService;

/**
 * This fragment displays the list of the visible cells. Its parent activity
 * must implements the TelephonyContext interface.
 * 
 * @author Gaborit Nicolas
 */
public class NeighborsFragment extends Fragment {

    /** The events monitored by the Telephony Listener */
    private static final int events = TelephonyListener.LISTEN_CELL_INFO;

    /**
     * The Telephony Listener, which defines the behavior against telephony
     * state changes
     */
    private TelephonyListener telListener = new TelephonyListener() {

        @Override
        public void onCellInfoChanged(List<ICellInfo> cellInfos) {
            // Find the number of neighbors cells
            neighborsCount = cellInfos.size();
            // Update the view
            updateNeighborsCount();
            // Update the cell info list adapter
            listAdapter.updateDataSet(cellInfos);
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
            telephonyService.getService().listen(telListener, events);
        }
    };

    /** The cell info list adapter */
    private CellInfoExpandableListAdapter listAdapter;

    /** The view indicating the number of neighboring cells */
    private TextView viewNeighborsCount;
    /** The number of detected neighboring cells */
    private int neighborsCount = 0;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Initialize the cell info list adapter with an empty cell info list
        listAdapter = new CellInfoExpandableListAdapter(getActivity(),
                new ArrayList<ICellInfo>(0));
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
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        // Inflate the fragment view
        View view = inflater.inflate(R.layout.fragment_neighbors, container,
                false);

        // Get the expandable list view
        ExpandableListView listView = (ExpandableListView) view
                .findViewById(R.id.neighbors_cell_list);

        // Set the adapter for the list
        listView.setAdapter((ExpandableListAdapter) listAdapter);

        // Get the neighbors count view
        viewNeighborsCount = (TextView) view
                .findViewById(R.id.neighbors_count_value);

        return view;
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

    /** Update the number of cells view */
    private void updateNeighborsCount() {
        // Check that the view has been initialized and we are attached to the
        // activity
        if (viewNeighborsCount != null && !isDetached()) {
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    // Update the number of neighbors
                    viewNeighborsCount.setText("" + neighborsCount);
                }
            });
        }
    }
}