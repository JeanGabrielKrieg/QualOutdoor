package com.qualoutdoor.recorder.location;

import java.util.Collections;
import java.util.LinkedList;
import java.util.NoSuchElementException;

import android.app.Service;
import android.content.Intent;
import android.location.Location;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesClient;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.location.LocationClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.qualoutdoor.recorder.Debug;
import com.qualoutdoor.recorder.LocalBinder;
import com.qualoutdoor.recorder.R;

/**
 * This service allows to access location data, it uses a Google Play Services
 * LocationClient in order to receive location update. An app component can bind
 * to it any time in order to monitor location.
 * 
 * #### Receiving location updates
 * 
 * There are two ways to access the current location :
 * 
 * ##### Make a direct query
 * 
 * Call directly the method `getLastKnownLocation()` to get the last known
 * location from the LocationClient used by the service.
 * 
 * ##### Request for updates the way you would do with a LocationClient
 * 
 * This is usually the best option. It is exactly the same process as described
 * in the [Android
 * Documentation](http://developer.android.com/training/location/
 * receive-location-updates.html), except you don't need to manage the
 * connection of the LocationClient.
 * 
 * 1. First create an instance of LocationRequest and LocationListener
 * 
 * 2. Override the callbacks in your LocationListener and suit the
 * LocationRequest to your needs so it describes best how you wish to be updated
 * (with `setInterval()` and other native methods).
 * 
 * 3. Call `requestLocationUpdates()` giving these instance as arguments
 * 
 * 4. Don't forget to call `removeLocationUpdate()` when you don't want to be
 * notified anymore.
 * 
 * 
 * @author Gaborit Nicolas
 */
public class LocationService extends Service implements
        GooglePlayServicesClient.ConnectionCallbacks,
        GooglePlayServicesClient.OnConnectionFailedListener {

    /** The interface binder for this service */
    private final IBinder mBinder = new LocalBinder<LocationService>(this);

    /**
     * A structure that stores a reference to a listener along with the update
     * request it has emitted
     */
    private class PendingRequest {
        LocationRequest request;
        LocationListener listener;

        PendingRequest(LocationRequest request, LocationListener listener) {
            this.request = request;
            this.listener = listener;
        }
    }

    /**
     * Store the locations listeners that asked for updates before the
     * LocationClient was connected
     */
    private LinkedList<PendingRequest> requestQueue = new LinkedList<PendingRequest>();

    /** Our location client reference */
    private LocationClient locationClient;
    /** Indicates if Google Play Services are available */
    private boolean servicesAvailable;
    /** Indicate if the client is connected */
    private boolean clientConnected = false;

    @Override
    public void onCreate() {
        // Create a new location client using this class to handle callbacks
        locationClient = new LocationClient(this, this, this);
        // Test if Google Play Services is available
        servicesAvailable = areServicesConnected();
        // If not available
        if (!servicesAvailable) {
            // Toast something useful
            Toast.makeText(this,
                    getString(R.string.error_location_services_unavailable),
                    Toast.LENGTH_SHORT).show();
            Log.e("LocationService", "servicesAvailable = false");
        } else {
            // Connect the client
            locationClient.connect();
        }
        super.onCreate();
    }

    @Override
    public void onDestroy() {
        Log.d("LocationService", "onDestroy");
        if (servicesAvailable && locationClient != null) {
            // Forget the current location client
            locationClient = null;
        }
        super.onDestroy();
    }

    @Override
    public IBinder onBind(Intent intent) {
        // Return our interface binder
        return mBinder;
    }

    /** Return the last known location */
    public Location getLastKnownLocation() {
        return locationClient.getLastLocation();
    }

    /**
     * Register the given LocationListener to receive location updates in
     * respect of the given LocationRequest parameters
     * 
     * @param locationRequest
     *            Parameters for the location updates
     * @param listener
     *            The listener that will receive the updates
     */
    public void requestLocationUpdates(LocationRequest locationRequest,
            LocationListener listener) {
        // If the client is connected already
        if (clientConnected && locationClient != null) {
            // Ask for location updates
            Log.d("LocationService",
                    "Request updates every " + locationRequest.getInterval()
                            + "ms for " + listener.toString());
            locationClient.requestLocationUpdates(locationRequest, listener);
        } else {
            // Add it to the queue
            requestQueue.offer(new PendingRequest(locationRequest, listener));
        }
    }

    /**
     * Unregister the given listener
     * 
     * @param listener
     *            The listener to remove
     */
    public void removeLocationUpdate(LocationListener listener) {
        // If the client is connected already
        Log.d("LocationService", "Remove updates for " + listener.toString());
        if (clientConnected && locationClient != null) {
            // Ask for location updates
            locationClient.removeLocationUpdates(listener);
        } else {
            // Remove all the occurrences from the pending request queue
            requestQueue.removeAll(Collections.singleton(listener));
        }
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        /*
         * Google Play services can resolve some errors it detects. If the error
         * has a resolution, it is possible to try sending an Intent to start a
         * Google Play services activity that can resolve error.
         */
        /*
         * But we are not an activity, so just inform the user x(
         */
        Toast.makeText(this,
                getString(R.string.error_location_on_connection_failed),
                Toast.LENGTH_SHORT).show();
        Log.e("LocationService", "onConnectionFailed");
    }

    @Override
    public void onConnected(Bundle bundle) {
        Log.d("LocationService", "onConnected services");
        PendingRequest pending;
        // Issue all the pending request
        for (;;) {
            try {
                // Retrieve and remove the head of the queue
                pending = requestQueue.remove();
            } catch (NoSuchElementException exc) {
                // No more pending request
                break;
            }
            // Issue the request
            Log.d("LocationService",
                    "Request updates every " + pending.request.getInterval()
                            + "ms for " + pending.listener.toString());
            locationClient.requestLocationUpdates(pending.request,
                    pending.listener);
        }
        // Indicate we are connected
        clientConnected = true;
    }

    @Override
    public void onDisconnected() {
        if (Debug.log)
            Log.d("LocationService", "onDisconnected");
        // Destroy the current location client (we will start anew)
        locationClient = null;
        // We are no longer connected
        clientConnected = false;
        if (Debug.log)
            Log.d("LocationService", "locationClient == null");

    }

    /** Check whether the Google Play Services are available */
    private boolean areServicesConnected() {
        // Check that Google Play services are available
        int resultCode = GooglePlayServicesUtil
                .isGooglePlayServicesAvailable(this);
        // Return true if success
        return (ConnectionResult.SUCCESS == resultCode);
    }
}
