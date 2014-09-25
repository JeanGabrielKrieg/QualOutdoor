package com.qualoutdoor.recorder.home;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.format.Formatter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ScrollView;
import android.widget.TextView;

import com.qualoutdoor.recorder.IServiceListener;
import com.qualoutdoor.recorder.R;
import com.qualoutdoor.recorder.ServiceProvider;
import com.qualoutdoor.recorder.telephony.TelephonyContext;
import com.qualoutdoor.recorder.telephony.TelephonyService;

/**
 * This fragment displays the main informations of the phone device on a single
 * screen. Its parent activity must implements the TelephonyContext interface.
 * 
 * @author Gaborit Nicolas
 */
public class DeviceFragment extends Fragment {

    /** The TelephonyService Provider given by the activity */
    private ServiceProvider<TelephonyService> telephonyService;
    /**
     * The service listener defines the behavior when the service becomes
     * available
     */
    private final IServiceListener<TelephonyService> telServiceListener = new IServiceListener<TelephonyService>() {
        @Override
        public void onServiceAvailable(TelephonyService service) {
            // Fill the text views

            // Get the device IMEI
            viewImei.setText(service.getDeviceId());
            // Get the device IMEI SV
            viewImeiSv.setText(service.getDeviceSoftwareVersion());
            // Get the sim operator name
            viewSimOperator.setText(service.getSimOperatorName());
            // Get the MAC address
            viewMac.setText(service.getMacAddress());
            // Get the IP address TODO deprecated (can't format ipv6
            // addresses...)
            viewIp.setText(Formatter.formatIpAddress(service.getIpAddress()));

        }
    };

    /** The view that displays the IMEI */
    private TextView viewImei;
    /** The view that displays the IMEI SV */
    private TextView viewImeiSv;
    /** The view that displays the MAC address */
    private TextView viewMac;
    /** The view that displays the sim operator */
    private TextView viewSimOperator;
    /** The view that displays the IP address */
    private TextView viewIp;

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
                R.layout.fragment_device, container, false);
        // Initialize the views references
        viewImei = (TextView) view.findViewById(R.id.imei_value);
        viewImeiSv = (TextView) view.findViewById(R.id.imeisv_value);
        viewSimOperator = (TextView) view.findViewById(R.id.sim_operator_value);
        viewMac = (TextView) view.findViewById(R.id.mac_value);
        viewIp = (TextView) view.findViewById(R.id.ip_value);

        // Initialize the Model value
        TextView viewModel = (TextView) view.findViewById(R.id.model_value);
        // Get the model and constructor names
        viewModel.setText(TelephonyService.getDeviceName());

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
        // Unregister the services listeners
        telephonyService.unregister(telServiceListener);
        super.onPause();
    }
}