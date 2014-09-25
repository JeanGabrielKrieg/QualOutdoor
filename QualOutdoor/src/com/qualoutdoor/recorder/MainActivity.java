package com.qualoutdoor.recorder;

import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.GravityCompat;
import android.support.v4.view.MenuItemCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarActivity;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.qualoutdoor.recorder.ServiceProvider.ServiceNotBoundException;
import com.qualoutdoor.recorder.location.LocationContext;
import com.qualoutdoor.recorder.location.LocationService;
import com.qualoutdoor.recorder.recording.IRecordingListener;
import com.qualoutdoor.recorder.recording.RecordingContext;
import com.qualoutdoor.recorder.recording.RecordingService;
import com.qualoutdoor.recorder.settings.SettingsActivity;
import com.qualoutdoor.recorder.telephony.ITelephony;
import com.qualoutdoor.recorder.telephony.TelephonyContext;
import com.qualoutdoor.recorder.telephony.TelephonyListener;
import com.qualoutdoor.recorder.telephony.TelephonyService;

/**
 * The main activity of the application. This is the only entry point to the
 * application.
 * 
 * Uses a NavigationDrawer to switch between different section of the
 * application.
 * 
 * Defines an action bar from which one can start/stop a recording, make an
 * upload, access the settings.
 * 
 * Acts as a RecordingContext, TelephonyContext and LocationContext to enable
 * sub-fragments to access these services.
 * 
 * @author Gaborit Nicolas
 * 
 */
public class MainActivity extends ActionBarActivity implements
        RecordingContext, TelephonyContext, LocationContext {

    /*
     * ########################################################################
     * State variable and services
     * ########################################################################
     */

    /** The current network type code */
    private int mNetworkType = ITelephony.NETWORK_TYPE_UNKNOWN;
    /** The data connection state */
    private int mDataState = ITelephony.DATA_DISCONNECTED;
    /** If the app is recording */
    private boolean mIsRecording = false;

    /** The telephony listener used to update the UI components in MainActivity */
    private final TelephonyListener telephonyListener = new TelephonyListener() {
        @Override
        public void onDataStateChanged(int state, int networkType) {
            // Update the network var
            mNetworkType = networkType;
            // Update the UI
            updateNetworkView();
            // Update the data state var
            mDataState = state;
            // Update the UI
            updateDataStateView();
        };
    };
    /** The events the telephony listener will monitor */
    private final static int telephonyEvents = TelephonyListener.LISTEN_DATA_STATE;

    /** The LocalServiceConnection used to access the TelephonyService */
    private final LocalServiceConnection<TelephonyService> telServiceConnection = new LocalServiceConnection<TelephonyService>(
            TelephonyService.class);
    /** The LocalServiceConnection used to access the RecordingService */
    private final LocalServiceConnection<RecordingService> recServiceConnection = new LocalServiceConnection<RecordingService>(
            RecordingService.class);
    /** The LocalServiceConnection used to access the LocationService */
    private final LocalServiceConnection<LocationService> locServiceConnection = new LocalServiceConnection<LocationService>(
            LocationService.class);

    /**
     * This component define the behavior when the TelephonyService becomes
     * available
     */
    private final IServiceListener<TelephonyService> telephonyServiceListener = new IServiceListener<TelephonyService>() {
        @Override
        public void onServiceAvailable(TelephonyService service) {
            // Register the telephony listener
            service.listen(telephonyListener, telephonyEvents);
        }
    };
    /** The recording listener used to update the UI components in MainActivity */
    private final IRecordingListener recordingListener = new IRecordingListener() {
        public void onRecordingChanged(boolean isRecording) {
            // Update the recording state
            mIsRecording = isRecording;
            // Update the UI
            updateRecordingButton();
        };
    };
    /**
     * This component define the behavior when the RecordingService becomes
     * available
     */
    private final IServiceListener<RecordingService> recordingServiceListener = new IServiceListener<RecordingService>() {
        @Override
        public void onServiceAvailable(RecordingService service) {
            // Register the recording listener
            service.register(recordingListener);
        }
    };

    /*
     * ########################################################################
     * Navigation Drawer members
     * ########################################################################
     */

    /** The current fragment title */
    private CharSequence fragmentTitle;
    /** The drawer title */
    private CharSequence drawerTitle;
    /** The active section in the navigation drawer */
    private int activeSection = -1;

    /** The previous actionbar title key in the savedInstanceState */
    private static final String PREVIOUS_TITLE = "previous_title";
    /** The active section key in the savedInstanceState */
    private static final String ACTIVE_SECTION = "active_section";

    /** A reference to the Navigation Drawer layout */
    private DrawerLayout drawerLayout;
    /** The ListView associated to the Navigation Drawer */
    private ListView drawerList;
    /**
     * An ActionBarDrawerToggle used to tie together the NavigationDrawer and
     * the ActionBar
     */
    private ActionBarDrawerToggle drawerToggle;

    /** The navigation drawer items click listener */
    private class DrawerItemClickListener implements
            ListView.OnItemClickListener {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position,
                long id) {
            // The behavior has been exported to the selectItem method.
            selectItem(position);
        }
    }

    /*
     * ########################################################################
     * Action bar views
     * ########################################################################
     */

    /**
     * The view located in the action bar which displays the current network
     * type
     */
    private TextView networkView;
    /** The recording action from the action bar menu */
    private MenuItem recordMenuItem;

    /*
     * ########################################################################
     * Methods
     * ########################################################################
     */

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Register the service listeners
        telServiceConnection.register(telephonyServiceListener);
        recServiceConnection.register(recordingServiceListener);

        // Initialize the drawer title
        drawerTitle = getResources().getString(R.string.title_activity_main);

        // Initialize the Navigation Drawer's content
        {
            // Get the DrawerLayout instance
            drawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
            // Set a custom shadow that overlays the main content when the
            // drawer opens
            drawerLayout.setDrawerShadow(R.drawable.drawer_shadow,
                    GravityCompat.START);
            // Get the ListView reference
            drawerList = (ListView) findViewById(R.id.left_drawer);
            // Items cannot be focused (unlike an EditText field for example)
            drawerList.setItemsCanFocus(false);

            // Set the adapter for the list view, it provides the titles to
            // display
            drawerList.setAdapter(new ArrayAdapter<String>(this,
                    R.layout.view_drawer_list_item,
                    NavigationDrawerItems.navigationTitles));
            // Set the list's click listener
            drawerList.setOnItemClickListener(new DrawerItemClickListener());

            // Instantiate an ActionBarDrawerToggle which implements
            // DrawerListener
            drawerToggle = new ActionBarDrawerToggle(this, /* host Activity */
            drawerLayout, /* DrawerLayout object */
            R.drawable.ic_drawer, /*
                                   * navigation drawer icon to replace the 'Up'
                                   * caret
                                   */
            R.string.drawer_open, /* "open drawer" description */
            R.string.drawer_close /* "close drawer" description */
            ) {
                /*
                 * Called when a drawer has settled in a completely closed
                 * state.
                 */
                public void onDrawerClosed(View view) {
                    super.onDrawerClosed(view);
                    // Set the ActionBar title to the fragment title
                    getSupportActionBar().setTitle(fragmentTitle);
                }

                /* Called when a drawer has settled in a completely open state. */
                public void onDrawerOpened(View drawerView) {
                    super.onDrawerOpened(drawerView);
                    // Set the ActionBar title to the drawer title
                    getSupportActionBar().setTitle(drawerTitle);
                }
            };

            // Set the drawer toggle as the DrawerListener
            drawerLayout.setDrawerListener(drawerToggle);

            // Enable the home button in the ActionBar
            getSupportActionBar().setHomeButtonEnabled(true);
            // Set the Home button to display an drawer indicator
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        }

        // Check we are not being restored
        if (savedInstanceState == null) {
            // Set the first fragment as the selected view
            selectItem(0);
        } else {
            // Retrieve the active section
            activeSection = savedInstanceState.getInt(ACTIVE_SECTION);
            // Get the corresponding fragment title
            fragmentTitle = NavigationDrawerItems.navigationTitles[activeSection];
            // Restore the previous ActionBar title (this depends on the state
            // of the drawer)
            getSupportActionBar().setTitle(
                    savedInstanceState.getCharSequence(PREVIOUS_TITLE));
        }

    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        // Sync the toggle state after onRestoreInstanceState has occurred.
        drawerToggle.syncState();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        // Forward the onConfigurationChanged call to the drawerToggle
        drawerToggle.onConfigurationChanged(newConfig);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        // Save the active section
        outState.putInt(ACTIVE_SECTION, activeSection);
        // Save the current action bar title
        outState.putCharSequence(PREVIOUS_TITLE, getSupportActionBar()
                .getTitle());
    }

    @Override
    protected void onStart() {
        super.onStart();
        Log.d("MainActivity", "onStart");
        // Bind to the TelephonyService
        telServiceConnection.bindToService(this);
        // Bind to the RecordingService
        recServiceConnection.bindToService(this);
        // Bind to the LocationService
        locServiceConnection.bindToService(this);
    }

    @Override
    protected void onStop() {
        super.onStop();

        Log.d("MainActivity", "onStop");
        // Unregister the TelephonyListener
        try {
            telServiceConnection.getService().listen(telephonyListener,
                    TelephonyListener.LISTEN_NONE);
        } catch (ServiceNotBoundException e) {}

        // Unregister the RecordingListener
        try {
            recServiceConnection.getService().unregister(recordingListener);
        } catch (ServiceNotBoundException e) {}

        // Unbind from the TelephonyService if needed
        telServiceConnection.unbindService();
        // Unbind from the RecordingService if needed
        recServiceConnection.unbindService();
        // Unbind from the LocationService if needed
        locServiceConnection.unbindService();
    }

    /*
     * ########################################################################
     * Update views methods
     * ########################################################################
     */

    /** Swaps fragments in the main content view on item selection */
    private void selectItem(int position) {
        // Check that we selected an different item than the active one
        if (position != activeSection) {
            // Create the corresponding fragment
            Fragment fragment = NavigationDrawerItems.getFragment(position);

            // Insert the fragment by replacing any existing fragment
            FragmentManager fragmentManager = getSupportFragmentManager();
            fragmentManager.beginTransaction()
                    .replace(R.id.container, fragment).commit();

            // Highlight the selected item
            drawerList.setItemChecked(position, true);

            // Update the fragment title
            fragmentTitle = NavigationDrawerItems.navigationTitles[position];

            // Set the ActionBar title to the fragment title
            getSupportActionBar().setTitle(fragmentTitle);

            // Set the active section
            activeSection = position;
        }
        // Close the drawer
        drawerLayout.closeDrawer(drawerList);
    }

    /** Update the data state indicator in the UI */
    private void updateDataStateView() {
        // Check that the view has been initialized
        if (networkView != null) {
            // Access the UI element from the UI thread
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    // Set the network view style according to the data state
                    if (mDataState == TelephonyManager.DATA_CONNECTED) {
                        // Data is on
                        // Change color to highlight the state
                        networkView.setTextColor(getResources().getColor(
                                R.color.network_info_highlight));
                        networkView.setTypeface(null, Typeface.BOLD_ITALIC);
                    } else {
                        // Data is off
                        networkView.setTextColor(getResources().getColor(
                                R.color.normal_text));
                        networkView.setTypeface(null, Typeface.NORMAL);
                    }
                    networkView.invalidate();
                }
            });
        }
    }

    /** Update the current network type indicator in the UI */
    private void updateNetworkView() {
        // Check that the view has been initialized
        if (networkView != null) {
            // Access the UI element from the UI thread
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    // Find the network names array
                    String[] networkNames = getResources().getStringArray(
                            R.array.network_type_name);
                    // Initialize the text view with the current network type
                    // string
                    networkView.setText(networkNames[mNetworkType]);
                    networkView.invalidate();
                }
            });
        }
    }

    /** Update the recording button view */
    private void updateRecordingButton() {
        // Check that the view has been initialized
        if (recordMenuItem != null) {
            // Access the UI elements from the UI thread
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    // Check if we are recording
                    if (mIsRecording) {
                        // The icon should represent the stop record action
                        recordMenuItem.setIcon(R.drawable.recording);
                    } else {
                        // The icon should represent the start record action
                        recordMenuItem.setIcon(R.drawable.not_recording);
                    }
                    // Tell the option menu its view has been updated
                    supportInvalidateOptionsMenu();
                }
            });
        }
    }

    /*
     * ########################################################################
     * ActionBar Options Menu
     * ########################################################################
     */

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.action_bar_menu, menu);

        // Initialize the record action reference
        recordMenuItem = menu.findItem(R.id.action_record);

        // Get the network item reference
        MenuItem networkMenuItem = menu.findItem(R.id.network_info);
        // Initialize the network info view reference
        networkView = (TextView) MenuItemCompat.getActionView(networkMenuItem);

        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        // Update the recording button view
        updateRecordingButton();
        // Update the network view
        updateNetworkView();
        // Update the data state view
        updateDataStateView();

        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Pass the event to ActionBarDrawerToggle, if it returns
        // true, then it has handled the app icon touch event
        if (drawerToggle.onOptionsItemSelected(item)) {
            return true;
        }
        // Handle the other action bar items...
        switch (item.getItemId()) {
        case R.id.action_record:
            RecordingService recService;
            try {
                recService = recServiceConnection.getService();
                if (recService.isRecording()) {
                    // The service is recording, so stop the recording
                    recService.stopRecording();
                } else {
                    // The service is not recording, so start the service
                    Intent recordingServiceIntent = new Intent(this,
                            RecordingService.class);
                    startService(recordingServiceIntent);
                }
            } catch (ServiceNotBoundException e) {}
            // Check that we are bound to the recording service
            return true;
        case R.id.action_upload:
            try {
                // Upload the data
                recServiceConnection.getService().uploadDatabase();
            } catch (ServiceNotBoundException e) {}
            return true;
        case R.id.action_settings:
            openSettings();
            return true;
        case R.id.action_help:
            openHelp();
            return true;
        default:
            // We don't handle the event, pass it to the super class
            return super.onOptionsItemSelected(item);
        }
    }

    /** Action associated to the settings option menu item */
    private void openSettings() {
        // Create an intent toward the DisplaySettingsActivity
        Intent intent = new Intent(this, SettingsActivity.class);
        // Start the activity
        startActivity(intent);
    }

    /** Action associated to the help option menu item */
    private void openHelp() {
        // Create an intent toward the DisplayHelpActivity
        Intent intent = new Intent(this, DisplayHelpActivity.class);
        // Start the activity
        startActivity(intent);
    }

    @Override
    public void onBackPressed() {
        // We desire to go back to the main section when back button is pressed,
        // and
        // to leave the app if we were in the main section already
        if (activeSection == 0) {
            // Defer to the system default behavior
            super.onBackPressed();
        } else {
            selectItem(0);
        }

    }

    /*
     * ########################################################################
     * ServiceProvider Interface
     * ########################################################################
     */

    @Override
    public ServiceProvider<TelephonyService> getTelephonyServiceProvider() {
        return telServiceConnection;
    }

    @Override
    public ServiceProvider<RecordingService> getRecordingServiceProvider() {
        return recServiceConnection;
    }

    @Override
    public ServiceProvider<LocationService> getLocationServiceProvider() {
        return locServiceConnection;
    }

}
