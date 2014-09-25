package com.qualoutdoor.recorder.telephony;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.telephony.CellInfo;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.util.Log;

import com.qualoutdoor.recorder.LocalBinder;
import com.qualoutdoor.recorder.R;
import com.qualoutdoor.recorder.Utils;

/**
 * This service is an Android implementation of ITelephony, it uses a
 * TelephonyManager to access phone state informations. An app component can
 * bind to it any time in order to monitor the phone state.
 * 
 * This class is able to refresh data based on the implemented callbacks of the
 * Android API, but also to force the refresh itself at a regular pace.
 * 
 * @author Gaborit Nicolas
 */
public class TelephonyService extends Service implements ITelephony {

    /** This is the initial size for the cell info array list */
    private static final int ESTIMATED_MAX_CELLS = 10;
    /** The interface binder for this service */
    private final IBinder mTelephonyBinder = new LocalBinder<TelephonyService>(
            this);

    /** Indicates if the datas must be force refreshed regularly */
    private boolean forceRefresh;
    /** The minimum refresh rate in milliseconds */
    private int minimumRefreshRate;

    /** The current signal strength value */
    private ISignalStrength signalStrength;
    /** The current data connection state */
    private int dataState;
    /** The current network type */
    private int networkType;
    /** The current mobile country code */
    private int mcc;
    /** The current mobile network code */
    private int mnc;
    /** The current call state */
    private int callState;
    /** The incomingNumber */
    private String incomingNumber = "";
    /** The current visible cells */
    private ArrayList<ICellInfo> allCellInfos;

    /****** The listeners list ******/
    // Note : Might use CopyOnWriteArrayList to avoid
    // ConcurrentModificationExceptions if a
    // listener attempts to remove itself during event notification.
    /** Store the listeners listening to LISTEN_CALL_STATE */
    private ArrayList<TelephonyListener> listenersCallState = new ArrayList<TelephonyListener>();
    /** Store the listeners listening to LISTEN_CELL_INFO */
    private ArrayList<TelephonyListener> listenersCellInfo = new ArrayList<TelephonyListener>();
    /** Store the listeners listening to LISTEN_DATA_STATE */
    private ArrayList<TelephonyListener> listenersDataState = new ArrayList<TelephonyListener>();
    /** Store the listeners listening to LISTEN_SIGNAL_STRENGTHS */
    private ArrayList<TelephonyListener> listenersSignalStrength = new ArrayList<TelephonyListener>();
    /** Store the listeners listening to LISTEN_MCC */
    private ArrayList<TelephonyListener> listenersMCC = new ArrayList<TelephonyListener>();
    /** Store the listeners listening to LISTEN_MNC */
    private ArrayList<TelephonyListener> listenersMNC = new ArrayList<TelephonyListener>();

    /** This handler is used to post regular data refresh task */
    private final Handler handler = new Handler();

    /** The runnable associated with the Handler */
    private final Runnable refresher = new Runnable() {
        @Override
        public void run() {
            try {
                // Refresh all the telephony data
                refreshData();
            } catch (Exception exc) {
                // Log the error
                Log.e("Refresher", "", exc);
            } finally {
                // If forced refresh are active
                if (forceRefresh) {
                    // Call again later
                    handler.postDelayed(this, minimumRefreshRate);
                }
            }
        }
    };

    /** Reference to the TelephonyManager */
    private TelephonyManager telephonyManager;

    /** Reference to the WiFiManager */
    private WifiManager wifiManager;

    /** The events the phone state listener is monitoring */
    private static int nonForcedEvents = PhoneStateListener.LISTEN_CALL_STATE
            | PhoneStateListener.LISTEN_DATA_CONNECTION_STATE;
    /** The events that are disabled when forced refresh is enabled */
    private static int forcedEvents = PhoneStateListener.LISTEN_SIGNAL_STRENGTHS
            | PhoneStateListener.LISTEN_CELL_INFO;

    /** The Android phone state listener */
    private PhoneStateListener phoneStateListener = new PhoneStateListener() {

        @Override
        public void onDataConnectionStateChanged(int state, int networkType) {
            // Update data state
            updateDataState(state, networkType);

        };

        @Override
        public void onCallStateChanged(int state, String incomingNumber) {
            // Update the call state
            updateCallState(state, incomingNumber);
        };

        @Override
        public void onCellInfoChanged(List<CellInfo> newCellInfos) {
            Log.d("TelephonyService", "onCellInfoChanged");

            // TODO Keep a log for the phones for which newCellInfos == null
            List<CellInfo> cellInfos = newCellInfos;
            // Return if cellInfos is null
            if (cellInfos == null) {
                Log.e("TelephonyService", "newCellInfo = null");
                // Manually get all CellInfos
                cellInfos = getAllCellInfo();
            }

            // Update the cell infos and notify
            updateCellInfos(cellInfos);
        };

        @Override
        public void onSignalStrengthsChanged(
                android.telephony.SignalStrength signalStrength) {
            Log.d("TelephonyService", "onSignalStrengthsChanged");
            // TODO We are not currently able to parse a SignalStrength
            // Log.d("SignalStrength", "CdmaDbm : " +
            // signalStrength.getCdmaDbm());
            // Log.d("SignalStrength",
            // "CdmaEcio : " + signalStrength.getCdmaEcio());
            // Log.d("SignalStrength", "EvdoDbm : " +
            // signalStrength.getEvdoDbm());
            // Log.d("SignalStrength",
            // "EvdoEcio : " + signalStrength.getEvdoEcio());
            // Log.d("SignalStrength", "EvdoSnr : " +
            // signalStrength.getEvdoSnr());
            // Log.d("SignalStrength",
            // "GsmBitErrorRate : " + signalStrength.getGsmBitErrorRate());
            // Log.d("SignalStrength",
            // "GsmSignalStrength : "
            // + signalStrength.getGsmSignalStrength());
            // Log.d("SignalStrength", "isGsm : " + signalStrength.isGsm());
        };
    };

    /**
     * The behavior when preferences changed.
     */
    private final OnSharedPreferenceChangeListener prefListener = new OnSharedPreferenceChangeListener() {
        public void onSharedPreferenceChanged(SharedPreferences prefs,
                String key) {
            if (key.equals(getString(R.string.pref_key_display_refresh_rate))) {
                // Update the refresh rate preference
                minimumRefreshRate = prefs.getInt(key, getResources()
                        .getInteger(R.integer.default_display_refresh_rate));
            }
            if (key.equals(getString(R.string.pref_key_force_display_refresh))) {
                // Get the force refresh preference, default to false
                forceRefresh = prefs.getBoolean(
                        key,
                        getResources().getBoolean(
                                R.bool.pref_default_force_display_refresh));
            }
        };
    };

    @Override
    public void onCreate() {

        // Retrieve an instance of Telephony Manager
        telephonyManager = (TelephonyManager) getSystemService(TELEPHONY_SERVICE);
        // Retrieve the WiFi Manager
        wifiManager = (WifiManager) getSystemService(WIFI_SERVICE);

        // Initialize the current phone state values
        {
            // Initialize the signal strength
            signalStrength = new CustomSignalStrength();
            // Initialize the call state
            callState = telephonyManager.getCallState();
            // Initialize the network type
            networkType = telephonyManager.getNetworkType();
            // Initialize the mobile country code to unknown
            mcc = Integer.MAX_VALUE;
            // Initialize the mobile network code to unknown
            mnc = Integer.MAX_VALUE;
            // Initialize the data state
            dataState = telephonyManager.getDataState();
            // Initialize the cell list
            allCellInfos = new ArrayList<ICellInfo>(ESTIMATED_MAX_CELLS);
        }

        // Get the app preferences
        SharedPreferences prefs = PreferenceManager
                .getDefaultSharedPreferences(this);
        // Force the update of the force refresh preference
        prefListener.onSharedPreferenceChanged(prefs,
                getString(R.string.pref_key_force_display_refresh));

        // Force the update of the refresh rate preference
        prefListener.onSharedPreferenceChanged(prefs,
                getString(R.string.pref_key_display_refresh_rate));

        // Listen to changes to the preferences
        prefs.registerOnSharedPreferenceChangeListener(prefListener);

        // If the refresh is forced
        if (forceRefresh) {
            // Trigger the refreshing process
            handler.postDelayed(refresher, minimumRefreshRate);
            // Start listening to phone state
            telephonyManager.listen(phoneStateListener, nonForcedEvents);
        } else {
            // Start listening to phone state including cell infos and signal
            // strength
            telephonyManager.listen(phoneStateListener, nonForcedEvents
                    + forcedEvents);
        }
        super.onCreate();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        // Unregister our listener from the telephony manager system service
        telephonyManager.listen(phoneStateListener,
                PhoneStateListener.LISTEN_NONE);
        // The refresher should stop
        forceRefresh = false;
    }

    @Override
    public IBinder onBind(Intent intent) {
        // Return our interface binder
        return mTelephonyBinder;
    }

    @Override
    public List<ICellInfo> getAllICellInfo() {
        // Parse all the cellInfos and return them
        return parseCellInfos(getAllCellInfo());
    }

    /**
     * Wrap the call to `TelephonyManager.getAllCellInfo()` and return an empty
     * list instead of null when the method fails
     * 
     * @return The list of cell info, which can be empty
     */
    private List<CellInfo> getAllCellInfo() {
        // Get the current cell infos
        List<CellInfo> cellInfos = telephonyManager.getAllCellInfo();
        // If null, initialize empty
        if (cellInfos == null) {
            Log.e("TelephonyService", "getAllCellInfo == null");
            cellInfos = new ArrayList<CellInfo>(0);
        }
        return cellInfos;
    }

    @Override
    public int getCallState() {
        // We normally should convert the call state code given by
        // TelephonyManager to the given code in the ITelephony interface. But
        // they are the same so far.
        return telephonyManager.getCallState();
    }

    @Override
    public int getDataState() {
        // We normally should convert the data state code given by
        // TelephonyManager to the given code in the ITelephony interface. But
        // they are the same so far.
        return telephonyManager.getDataState();
    }

    @Override
    public int getNetworkType() {
        // We normally should convert the network type code given by
        // TelephonyManager to the given code in the ITelephony interface. But
        // they are the same so far.
        return telephonyManager.getNetworkType();
    }

    @Override
    public ISignalStrength getSignalStrength() {
        // Return the current signal strength
        return signalStrength;
    }

    @Override
    public String getDeviceId() {
        // Return the device ID
        return telephonyManager.getDeviceId();
    }

    @Override
    public String getMacAddress() {
        // Access the current WiFi connection state
        WifiInfo wInfo = wifiManager.getConnectionInfo();
        // Get the MAC address
        String macAddress = wInfo.getMacAddress();
        return macAddress;
    }

    @Override
    public int getIpAddress() {
        // Get the current IP address
        return wifiManager.getConnectionInfo().getIpAddress();
    }

    /**
     * Return the full device name, including model and manufacturer string
     * 
     * @return The full device name
     */
    public static String getDeviceName() {
        // Get the manufacturer string
        String manufacturer = Build.MANUFACTURER;
        // Get the model string
        String model = Build.MODEL;
        // Check that the model string does not already contain the manufacturer
        // string
        if (model.startsWith(manufacturer)) {
            return Utils.capitalize(model);
        } else {
            return Utils.capitalize(manufacturer) + " " + model;
        }
    }

    @Override
    public String getNetworkOperatorName() {
        return telephonyManager.getNetworkOperatorName();
    }

    @Override
    public String getSimOperatorName() {
        return telephonyManager.getSimOperatorName();
    }

    @Override
    public String getDeviceSoftwareVersion() {
        return telephonyManager.getDeviceSoftwareVersion();
    }

    @Override
    public boolean isNetworkRoaming() {
        return telephonyManager.isNetworkRoaming();
    }

    @Override
    public void listen(TelephonyListener listener, int events) {
        // Check if we are unregistering a listener
        if (events == TelephonyListener.LISTEN_NONE) {
            // Unregister the listener from all the lists
            listenersCallState.remove(listener);
            listenersCellInfo.remove(listener);
            listenersDataState.remove(listener);
            listenersSignalStrength.remove(listener);
            listenersMCC.remove(listener);
            listenersMNC.remove(listener);
        } else {
            // Add the listener to the corresponding list, according to the
            // events
            // it is subscribing to.
            // We are making bitwise comparison because 'events' is used as a
            // boolean mask.
            if ((events & TelephonyListener.LISTEN_CALL_STATE) == TelephonyListener.LISTEN_CALL_STATE) {
                // The listener wish to monitor the call state, add it to the
                // list
                listenersCallState.add(listener);
                // Notify it immediatly with the current data
                listener.onCallStateChanged(callState, incomingNumber);
            }
            if ((events & TelephonyListener.LISTEN_CELL_INFO) == TelephonyListener.LISTEN_CELL_INFO) {
                // The listener wish to monitor the cells infos, add it to the
                // list
                listenersCellInfo.add(listener);
                // Notify it immediatly with a read only copy of allCellInfos
                listener.onCellInfoChanged(Collections
                        .unmodifiableList(allCellInfos));
            }
            if ((events & TelephonyListener.LISTEN_DATA_STATE) == TelephonyListener.LISTEN_DATA_STATE) {
                // The listener wish to monitor the data state, add it to the
                // list
                listenersDataState.add(listener);
                // Notify it immediatly with the current data
                listener.onDataStateChanged(dataState, networkType);
            }
            if ((events & TelephonyListener.LISTEN_SIGNAL_STRENGTHS) == TelephonyListener.LISTEN_SIGNAL_STRENGTHS) {
                // The listener wish to monitor the signal strength, add it to
                // the list
                listenersSignalStrength.add(listener);
                // Notify it immediatly with the current data
                listener.onSignalStrengthsChanged(signalStrength);
            }
            if ((events & TelephonyListener.LISTEN_MCC) == TelephonyListener.LISTEN_MCC) {
                // Add the listener to the MCC list
                listenersMCC.add(listener);
                // Notify immediatly
                listener.onMCCChanged(mcc);
            }
            if ((events & TelephonyListener.LISTEN_MNC) == TelephonyListener.LISTEN_MNC) {
                // Add the listener to the MNC list
                listenersMNC.add(listener);
                // Notify immediatly
                listener.onMNCChanged(mnc);
            }
        }
    }

    @Override
    public void setMinimumRefreshRate(int milliseconds) {
        minimumRefreshRate = Math.min(minimumRefreshRate, milliseconds);
    }

    /**
     * Refresh the cell infos and signal strength data, and trigger the onChange
     * notifications
     */
    private void refreshData() {
        // Update the cell infos (this update the signal strength too)
        updateCellInfos(getAllCellInfo());
    }

    /**
     * Update the data state.
     * 
     * @param state
     *            The new data connection state code.
     * @param networkType
     *            The new network type code
     */
    private void updateDataState(int state, int networkType) {
        // Update the current data connection state
        this.dataState = state;
        // Update the current network type
        this.networkType = networkType;
        // Notify the data state listeners
        notifyDataStateListeners(state, networkType);
    }

    /**
     * Update the call state.
     * 
     * @param state
     *            The new call state code
     * @param incomingNumber
     *            The incoming number string
     */
    private void updateCallState(int state, String incomingNumber) {
        // Update the current call state
        this.callState = state;
        // Update the incomingNumber
        this.incomingNumber = incomingNumber;
        // Notify the call state listeners
        notifyCallStateListeners(state, incomingNumber);
    }

    /**
     * Parse a CellInfo list and update the allCellInfo, then notify the
     * listeners from the changes
     * 
     * @param cellInfos
     *            The new CellInfo list
     */
    private void updateCellInfos(List<CellInfo> cellInfos) {
        // Reset the cell info array list
        allCellInfos.clear();

        // Parse the cell infos list
        List<ICellInfo> iCellInfos = parseCellInfos(cellInfos);
        // Update the current ICellInfo list and retrieve the signal
        // strength at the same time.
        for (ICellInfo cell : iCellInfos) {
            // Add it to the list
            allCellInfos.add(cell);
            // Check if this is the primary cell
            if (cell.isRegistered()) {
                // This is the signal strength you are looking for
                signalStrength = cell.getSignalStrength();
                // Notify the signal strength listeners
                notifySignalStrengthListeners(signalStrength);
                // Update mcc
                if (mcc != cell.getMcc()) {
                    mcc = cell.getMcc();
                    // Notify mcc listeners
                    notifyMCCListeners(mcc);
                }
                // Update mnc
                if (mnc != cell.getMnc()) {
                    mnc = cell.getMnc();
                    // Notify mnc listeners
                    notifyMNCListeners(mnc);
                }
            }
        }
        // Create a non modifiable ICellInfo list
        List<ICellInfo> unmodifiableCellInfo = Collections
                .unmodifiableList(allCellInfos);

        // Notify the cell info listeners
        notifyCellInfoListeners(unmodifiableCellInfo);
    }

    /**
     * Parse a CellInfo list and return the corresponding ICellInfo list
     * 
     * @param cellInfos
     *            The CellInfo list to parse
     * @return A list of ICellInfo converted from the input list
     */
    private List<ICellInfo> parseCellInfos(List<CellInfo> cellInfos) {
        // Initialize the result list
        ArrayList<ICellInfo> result = new ArrayList<ICellInfo>(
                ESTIMATED_MAX_CELLS);
        // Declare the variable holding the converted cell
        CustomCellInfo customCell;
        // Parse the list
        for (CellInfo cell : cellInfos) {
            // Create a corresponding ICellInfo
            customCell = CustomCellInfo.buildFromCellInfo(cell);
            // Add it to the list
            result.add(customCell);
        }
        // We parsed the list
        return result;
    }

    /**
     * Notify each cell info listeners with the current ICellInfo list.
     * 
     * @param cellInfos
     *            The updated list of ICellInfo
     */
    private void notifyCellInfoListeners(List<ICellInfo> cellInfos) {
        for (TelephonyListener listener : listenersCellInfo) {
            // For each listener, notify
            listener.onCellInfoChanged(cellInfos);
        }
    }

    /**
     * Notify each signal strength listeners with the current ISignalStrength
     * value
     * 
     * @param signalStrength
     *            The new signal strength
     */
    private void notifySignalStrengthListeners(ISignalStrength signalStrength) {
        for (TelephonyListener listener : listenersSignalStrength) {
            // For each listener, notify
            listener.onSignalStrengthsChanged(signalStrength);
        }
    }

    /**
     * Notify each call state listeners with the current call state and network
     * type values
     */
    private void notifyCallStateListeners(int state, String incomingNumber) {
        for (TelephonyListener listener : listenersCallState) {
            // For each listener, notify
            listener.onCallStateChanged(state, incomingNumber);
        }
    }

    /** Notify each location listeners with the current ILocation value */
    private void notifyDataStateListeners(int state, int networkType) {
        for (TelephonyListener listener : listenersDataState) {
            // For each listener, notify
            listener.onDataStateChanged(state, networkType);
        }
    }

    /** Notify each MCC listeners with the current MCC value */
    private void notifyMCCListeners(int mcc) {
        for (TelephonyListener listener : listenersMCC) {
            // For each listener, notify
            listener.onMCCChanged(mcc);
        }
    }

    /** Notify each MNC listeners with the current MNC value */
    private void notifyMNCListeners(int mnc) {
        for (TelephonyListener listener : listenersMNC) {
            // For each listener, notify
            listener.onMNCChanged(mnc);
        }
    }

}
