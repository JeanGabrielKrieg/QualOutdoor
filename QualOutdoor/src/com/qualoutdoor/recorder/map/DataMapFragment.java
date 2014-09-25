package com.qualoutdoor.recorder.map;

import android.app.Activity;
import android.graphics.Color;
import android.location.Location;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.qualoutdoor.recorder.IServiceListener;
import com.qualoutdoor.recorder.R;
import com.qualoutdoor.recorder.ServiceProvider;
import com.qualoutdoor.recorder.ServiceProvider.ServiceNotBoundException;
import com.qualoutdoor.recorder.location.LocationContext;
import com.qualoutdoor.recorder.location.LocationService;
import com.qualoutdoor.recorder.telephony.ISignalStrength;
import com.qualoutdoor.recorder.telephony.TelephonyContext;
import com.qualoutdoor.recorder.telephony.TelephonyListener;
import com.qualoutdoor.recorder.telephony.TelephonyService;

/**
 * A demo map fragment that displays signal strengths on a map
 * 
 * @author Gaborit Nicolas
 */
public class DataMapFragment extends Fragment implements LocationListener {

    private static final float SCALE_START_HUE = 60;
    private static final float SCALE_END_HUE = 0;

    private ISignalStrength signalStrength;

    /** Reference to the Map object */
    private GoogleMap map;
    /** Reference to the MapFragment */
    private SupportMapFragment mapFragment;
    /** Tag used to identify the MapFragment */
    private static String MAP_FRAGMENT_TAG = "mapFragment";

    private ServiceProvider<TelephonyService> telephonyService;

    private ServiceProvider<LocationService> locationService;

    private IServiceListener<TelephonyService> telServiceListener = new IServiceListener<TelephonyService>() {
        @Override
        public void onServiceAvailable(TelephonyService service) {
            // Register the telephony listener
            service.listen(telListener, events);
        };
    };

    private static final int events = TelephonyListener.LISTEN_SIGNAL_STRENGTHS;
    private TelephonyListener telListener = new TelephonyListener() {
        @Override
        public void onSignalStrengthsChanged(ISignalStrength signalStrength) {
            // Update signal strength
            DataMapFragment.this.signalStrength = signalStrength;
        };
    };

    private IServiceListener<LocationService> locServiceListener = new IServiceListener<LocationService>() {
        @Override
        public void onServiceAvailable(LocationService service) {
            // Register as a listener
            LocationRequest locationRequest = new LocationRequest()
                    .setInterval(2000);
            service.requestLocationUpdates(locationRequest,
                    DataMapFragment.this);
        }
    };
    private Location location;

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            // This cast makes sure that the container activity has implemented
            // TelephonyContext
            TelephonyContext telephonyContext = (TelephonyContext) activity;

            // Retrieve the service connection
            telephonyService = telephonyContext.getTelephonyServiceProvider();
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement " + TelephonyContext.class.toString());
        }
        try {
            // This cast makes sure that the container activity has implemented
            // LocationContext
            LocationContext locationContext = (LocationContext) activity;

            // Retrieve the service connection
            locationService = locationContext.getLocationServiceProvider();
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement " + LocationContext.class.toString());
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // It isn't possible to set a fragment's id programmatically so we set a
        // tag instead and search for it using that tag.
        mapFragment = (SupportMapFragment) getFragmentManager()
                .findFragmentByTag(MAP_FRAGMENT_TAG);

        /*
         * Don't know why this check causes the map to not be displayed after
         * switching activities...
         */
        // We only create a fragment if it doesn't already exist.
        // if (mapFragment == null) {

        // To programmatically add the map, we first create a
        // SupportMapFragment and override the onActivityCreated callback
        // for map initialization
        mapFragment = new SupportMapFragment() {
            @Override
            public void onActivityCreated(Bundle savedInstanceState) {
                super.onActivityCreated(savedInstanceState);
                // The map is available from now on
                setUpMapIfNeeded();
                Log.d("DataMapFragment", "Map creation callback");
            };
        };

        // Then we add it using a FragmentTransaction.
        FragmentTransaction fragmentTransaction = getFragmentManager()
                .beginTransaction();
        fragmentTransaction.add(R.id.map_container, mapFragment,
                MAP_FRAGMENT_TAG);
        fragmentTransaction.commit();
        // }

        // Set up the map if already available
        setUpMapIfNeeded();
    }

    // Called when the fragment has to instantiate its own view
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        // Inflate the view from the xml layout file
        View rootView = inflater.inflate(R.layout.fragment_map, container,
                false);
        return rootView;
    }

    @Override
    public void onResume() {
        super.onResume();
        // Check that the map is available
        if (setUpMapIfNeeded()) {
            Log.d("Map", "Map available");
        } else {
            Log.d("Map", "NO MAP AVAILABLE");
        }

        // Tell we want to be informed when services become available
        telephonyService.register(telServiceListener);
        locationService.register(locServiceListener);
    }

    @Override
    public void onPause() {
        super.onPause();
        // If needed unregister our telephony listener
        try {
            telephonyService.getService().listen(telListener,
                    TelephonyListener.LISTEN_NONE);
        } catch (ServiceNotBoundException e) {}
        // Unregister location listener
        try {
            locationService.getService().removeLocationUpdate(this);
        } catch (ServiceNotBoundException e) {}

        // Remove map
        map = null;

        // Unregister the services listeners
        telephonyService.unregister(telServiceListener);
        locationService.unregister(locServiceListener);
    }

    /**
     * Instantiate and initialize the Map object from the MapFragment if needed.
     * 
     * @return true if the map is available.
     */
    private boolean setUpMapIfNeeded() {
        // Do a null check to confirm that we have not already instantiated the
        // map.
        if (map == null) {
            // Obtain the Map object from the MapFragment
            map = mapFragment.getMap();
            // Check if we were successful in obtaining the map.
            if (map != null) {
                // Map is available : initialize
                setUpMap();
            } else {
                // The map is not available
                return false;
            }
        }
        // The map is initialized
        return true;
    }

    /** Initialize the Map object */
    private void setUpMap() {
        // Disable the compass
        map.getUiSettings().setCompassEnabled(false);
        // Disable tilt gestures
        map.getUiSettings().setTiltGesturesEnabled(false);
        // Set the map to satellite view
        map.setMapType(GoogleMap.MAP_TYPE_SATELLITE);
        // Activate the 'center on my location button'
        map.setMyLocationEnabled(true);
    }

    @Override
    public void onLocationChanged(Location location) {
        // Update location
        this.location = location;
        if (map != null) {

            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    LatLng latlng = new LatLng(DataMapFragment.this.location
                            .getLatitude(), DataMapFragment.this.location
                            .getLongitude());
                    // Center the camera on the new location
                    CameraUpdate update = CameraUpdateFactory.newLatLngZoom(
                            latlng, 20);

                    // Move the camera
                    map.animateCamera(update);

                    // Add a marker
                    createMarker(latlng);

                }
            });
        }
    }

    /* TODO: Rewrite this hard coded mess... */
    private void createMarker(LatLng latlng) {
        // The color of the marker
        float[] hsv = new float[3];
        hsv[1] = 255f;
        hsv[2] = 255f;
        // Compute the hue
        float relativeAsu = ((float) signalStrength.getAsuLevel()) / 31; // MAX_ASU
        hsv[0] = SCALE_START_HUE + relativeAsu
                * (SCALE_END_HUE - SCALE_START_HUE);
        // Instantiates a new CircleOptions object and defines the radius in
        // meters
        CircleOptions circleOptions = new CircleOptions().radius(1) // Radius in
                                                                    // meter
                .strokeColor(0x00000000) // transparent
                .center(latlng) // on location
                .fillColor(Color.HSVToColor(hsv)); // with good color
        map.addCircle(circleOptions);
    }

}
