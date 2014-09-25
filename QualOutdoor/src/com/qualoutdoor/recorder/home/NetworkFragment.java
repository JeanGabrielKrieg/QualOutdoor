package com.qualoutdoor.recorder.home;

import java.util.List;

import org.json.JSONException;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ScrollView;
import android.widget.TextView;

import com.qualoutdoor.recorder.IServiceListener;
import com.qualoutdoor.recorder.QualOutdoorRecorderApp;
import com.qualoutdoor.recorder.R;
import com.qualoutdoor.recorder.ServiceProvider;
import com.qualoutdoor.recorder.ServiceProvider.ServiceNotBoundException;
import com.qualoutdoor.recorder.telephony.ICellInfo;
import com.qualoutdoor.recorder.telephony.TelephonyContext;
import com.qualoutdoor.recorder.telephony.TelephonyListener;
import com.qualoutdoor.recorder.telephony.TelephonyService;
import com.qualoutdoor.recorder.telephony.ViewCellInfo;

/**
 * This fragment displays the main informations of the phone network state on a
 * single screen. Its parent activity must implements the TelephonyContext
 * interface.
 * 
 * @author Gaborit Nicolas
 */
public class NetworkFragment extends Fragment {

    /**
     * The Telephony Listener, which defines the behavior against telephony
     * state changes
     */
    private final TelephonyListener telListener = new TelephonyListener() {
        /** The events monitored by the Telephony Listener */
        @Override
        public int events() {
            return TelephonyListener.LISTEN_CELL_INFO
                    | TelephonyListener.LISTEN_DATA_STATE;
        }

        @Override
        public void onCellInfoChanged(List<ICellInfo> cellInfos) {
            // Find the number of neighbors cells
            neighborsCount = cellInfos.size();
            // Find the first registered cell
            for (ICellInfo cell : cellInfos) {
                if (cell.isRegistered()) {
                    // This is the primary cell
                    cellInfo = cell;
                    // Stop searching
                    try {
                        Log.d("NetworkFragment", "Primary Cell :\n"
                                + cell.toJSON().toString(4));
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    break;
                }
            }
            // Update the UI elements
            updateCellInfo();
        }

        public void onDataStateChanged(int state, int networkType) {
            // Update the network type
            network = networkType;
            // Update the UI
            updateNetworkTypeView();
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

            // Fill the text views
            {
                // Get the network operator name
                viewOperator.setText(service.getNetworkOperatorName());
                // Get the roaming state
                int roamingStringId = service.isNetworkRoaming() ? R.string.roaming
                        : R.string.not_roaming;
                viewRoaming.setText(getString(roamingStringId));
            }
        }
    };

    /** The network type code */
    private int network;
    /** The primary cell */
    private ICellInfo cellInfo;
    /** The number of detected neighboring cells */
    private int neighborsCount = 0;

    /** The primary cell view */
    private ViewCellInfo viewCellInfo;
    /** The view indicating the number of neighboring cells */
    private TextView viewNeighborsCount;
    /** The network type code text view */
    private TextView viewNetworkType;
    /** The view that displays the network operator name */
    private TextView viewOperator;
    /** The view that displays the roaming state */
    private TextView viewRoaming;

    /** The network type code strings */
    private static final String[] networkNames = QualOutdoorRecorderApp
            .getAppResources().getStringArray(R.array.network_type_name);

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
        ScrollView view = (ScrollView) inflater.inflate(
                R.layout.fragment_network, container, false);
        // Initialize the views references
        viewNetworkType = (TextView) view
                .findViewById(R.id.network_type_code_value);
        viewNeighborsCount = (TextView) view
                .findViewById(R.id.neighbors_count_value);
        viewCellInfo = (ViewCellInfo) view.findViewById(R.id.cell_info);
        viewOperator = (TextView) view.findViewById(R.id.operator_value);
        viewRoaming = (TextView) view.findViewById(R.id.is_roaming_value);

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

    /** Update the text field with the current value of network type */
    private void updateNetworkTypeView() {
        // Check that the view has been initialized and we are attached to the
        // activity
        if (viewNetworkType != null && !isDetached()) {
            // Access the UI from the main thread
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    viewNetworkType.setText(networkNames[network]);
                    // Invalidate the view that changed
                    viewNetworkType.invalidate();
                }
            });
        }
    }

    /** Update the cell views */
    private void updateCellInfo() {
        // Check that the view has been initialized and we are attached to the
        // activity
        if (viewCellInfo != null && !isDetached()) {
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (cellInfo != null)
                        // Update the ViewCellInfo
                        viewCellInfo.updateCellInfo(cellInfo);
                    // Update the number of neighbors
                    viewNeighborsCount.setText("" + neighborsCount);
                }
            });
        }
    }

}