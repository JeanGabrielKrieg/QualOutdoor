package com.qualoutdoor.recorder.home;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import android.app.Activity;
import android.location.Location;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.qualoutdoor.recorder.IServiceListener;
import com.qualoutdoor.recorder.R;
import com.qualoutdoor.recorder.ServiceProvider;
import com.qualoutdoor.recorder.ServiceProvider.ServiceNotBoundException;
import com.qualoutdoor.recorder.location.LocationContext;
import com.qualoutdoor.recorder.location.LocationService;

/**
 * This fragment displays all the available information relative to the current
 * location. Its parent activity must implements the LocationContext interface.
 * 
 * @author Gaborit Nicolas
 */
public class LocationFragment extends Fragment {

    /**
     * The Location Listener, which update location views when the location
     * changes
     */
    private final LocationListener locListener = new LocationListener() {
        @Override
        public void onLocationChanged(Location newLocation) {
            // Update the location
            location = newLocation;
            // Update the UI
            updateLocationView();
        };
    };

    /** Our location update request parameters */
    private static final LocationRequest locationRequest = new LocationRequest()
            .setInterval(0) // Asking for the fastest update interval, YOLO
            .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY); // With high
                                                                  // accuracy

    /** The LocationService Provider given by the activity */
    private ServiceProvider<LocationService> locationService;
    /**
     * The service listener defines the behavior when the service becomes
     * available
     */
    private final IServiceListener<LocationService> locServiceListener = new IServiceListener<LocationService>() {
        @Override
        public void onServiceAvailable(LocationService service) {
            // Register the location listener
            service.requestLocationUpdates(locationRequest, locListener);
        }
    };

    /** The current location */
    private Location location;

    /** The accuracy value text view */
    private TextView viewAccuracy;
    /** The latitude text view */
    private TextView viewLatitude;
    /** The longitude text view */
    private TextView viewLongitude;
    /** The altitude text view */
    private TextView viewAltitude;
    /** The speed text view */
    private TextView viewSpeed;
    /** The time text view */
    private TextView viewTime;
    /** The provider text view */
    private TextView viewProvider;

    /** Indicate that the views have been initialized */
    private boolean viewsInitialized = false;

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            // This cast makes sure that the container activity has implemented
            // LocationContext
            LocationContext locationContext = (LocationContext) getActivity();

            // Retrieve the service connection
            locationService = locationContext.getLocationServiceProvider();
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement " + LocationContext.class.toString());
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_location, container,
                false);
        // Initialize the views references
        viewAccuracy = (TextView) view
                .findViewById(R.id.fragment_location_accuracy);
        viewAltitude = (TextView) view
                .findViewById(R.id.fragment_location_altitude);
        viewLatitude = (TextView) view
                .findViewById(R.id.fragment_location_latitude);
        viewLongitude = (TextView) view
                .findViewById(R.id.fragment_location_longitude);
        viewProvider = (TextView) view
                .findViewById(R.id.fragment_location_provider);
        viewSpeed = (TextView) view.findViewById(R.id.fragment_location_speed);
        viewTime = (TextView) view.findViewById(R.id.fragment_location_time);

        // Indicate that the views are available
        viewsInitialized = true;

        return view;
    }

    @Override
    public void onResume() {
        // Tell we want to be informed when services become available
        locationService.register(locServiceListener);
        super.onStart();
    }

    @Override
    public void onPause() {
        // If needed unregister our telephony listener
        try {
            locationService.getService().removeLocationUpdate(locListener);
        } catch (ServiceNotBoundException e) {}

        // Unregister the services listeners
        locationService.unregister(locServiceListener);
        super.onPause();
    }

    /** Update the view related to the location */
    private void updateLocationView() {
        // Check that the views have been initialized and we are not in a
        // detached state
        if (viewsInitialized && !isDetached()) {
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {

                    // Create a formater for float and double values
                    DecimalFormat format = new DecimalFormat("0.###");
                    // Display the accuracy
                    viewAccuracy.setText(format.format(location.getAccuracy()));
                    // Display the altitude
                    viewAltitude.setText(format.format(location.getAltitude()));
                    // Display the latitude
                    viewLatitude.setText(format.format(location.getLatitude()));
                    // Display the longitude
                    viewLongitude.setText(format.format(location.getLongitude()));
                    // Display the provider name
                    viewProvider.setText(location.getProvider());
                    // Display the speed
                    viewSpeed.setText(format.format(location.getSpeed()));

                    // Create a date formater with the pattern hh:mm:ss
                    SimpleDateFormat dateFormat = new SimpleDateFormat(
                            "hh:mm:ss");
                    // Display the date of the fix
                    viewTime.setText(dateFormat.format(new Date(location
                            .getTime())));
                }

            });
        }
    }
}