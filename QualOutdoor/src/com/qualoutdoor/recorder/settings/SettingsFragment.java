package com.qualoutdoor.recorder.settings;

import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;

import com.qualoutdoor.recorder.R;

/**
 * This fragment gives access to the different settings of the application. The
 * essential of the logic is declared in the XML. Only some view updates are
 * done here.
 * 
 * @author Gaborit Nicolas
 */
public class SettingsFragment extends PreferenceFragment {

    /**
     * The behavior when preferences changed. Used to update the texts and
     * summary on change.
     */
    private final OnSharedPreferenceChangeListener listener = new OnSharedPreferenceChangeListener() {

        @Override
        public void onSharedPreferenceChanged(
                SharedPreferences sharedPreferences, String key) {

            if (key.equals(getString(R.string.pref_key_minimum_upload_size))) {
                // Update the summary with the saved value
                updateMinimumUploadSizeSummary(sharedPreferences);
            }
            if (key.equals(getString(R.string.pref_key_network_policy))) {
                // Update the summary
                updateNetworkPolicySummary(sharedPreferences);
            }
            if (key.equals(getString(R.string.pref_key_protocol))) {
                // Update the summary
                updateProtocolSummary(sharedPreferences);
            }

        }

    };

    /** Update the minimum upload size summary text. */
    private void updateMinimumUploadSizeSummary(
            SharedPreferences sharedPreferences) {
        // Get the minimum upload size preference key
        String key = getString(R.string.pref_key_minimum_upload_size);
        // Get the corresponding preference
        Preference uploadSizePref = findPreference(key);
        // Get the default value
        String defaultValue = getResources().getInteger(
                R.integer.default_minimum_upload_size)
                + "";
        // Get the value string
        String value = sharedPreferences.getString(key, defaultValue);
        // Set summary to be the description for the user selected value
        uploadSizePref
                .setSummary(getString(R.string.pref_left_text_minimum_upload_size)
                        + " "
                        + value
                        + getString(R.string.pref_right_text_minimum_upload_size));
    }

    /** Update the network policy summary text. */
    private void updateNetworkPolicySummary(SharedPreferences sharedPreferences) {
        // Get the network policy preference key
        String key = getString(R.string.pref_key_network_policy);
        // Get the corresponding preference
        Preference pref = findPreference(key);
        // Get the default value
        String defaultValue = getResources().getString(
                R.string.pref_default_network_policy);
        // Get the saved value
        String value = sharedPreferences.getString(key, defaultValue);
        // Get the list of values
        String values[] = getResources().getStringArray(
                R.array.pref_list_values_network_policy);
        // Get the list of localized string
        String entries[] = getResources().getStringArray(
                R.array.pref_list_entries_network_policy);
        // Find the corresponding entry
        int index = 0;
        for (; index < values.length; index++) {
            if (values[index].equals(value))
                break; // We have found the saved value index
        }
        // Get the corresponding localized string
        String valueString = entries[index];

        // Set summary to be the description for the selected value
        pref.setSummary(valueString);
    }

    /** Update the protocol preference summary text */
    private void updateProtocolSummary(SharedPreferences sharedPreferences) {
        // Get the protocol preference key
        String key = getString(R.string.pref_key_protocol);
        // Get the corresponding preference
        Preference pref = findPreference(key);
        // Get the default value
        String defaultValue = getResources().getString(
                R.string.pref_default_protocol);
        // Get the saved value
        String value = sharedPreferences.getString(key, defaultValue);
        // Get the list of values
        String values[] = getResources().getStringArray(
                R.array.pref_list_values_protocol);
        // Get the list of localized string
        String entries[] = getResources().getStringArray(
                R.array.pref_list_entries_protocol);
        // Find the corresponding entry
        int index = 0;
        for (; index < values.length; index++) {
            if (values[index].equals(value))
                break; // We have found the saved value index
        }
        // Get the corresponding localized string
        String valueString = entries[index];
        // Set summary to be the description for the selected value
        pref.setSummary(valueString);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Load the preferences from the xml ressource
        addPreferencesFromResource(R.xml.preferences);
    }

    @Override
    public void onResume() {
        super.onResume();
        // Update the views summaries
        {
            // Get the shared preferences
            SharedPreferences prefs = PreferenceManager
                    .getDefaultSharedPreferences(getActivity());
            // Update the summary with the saved value
            updateMinimumUploadSizeSummary(prefs);
            updateNetworkPolicySummary(prefs);
            updateProtocolSummary(prefs);
        }
        // Register the preference listener
        getPreferenceScreen().getSharedPreferences()
                .registerOnSharedPreferenceChangeListener(listener);
    }

    @Override
    public void onPause() {
        super.onPause();
        // Unregister the preference listener
        getPreferenceScreen().getSharedPreferences()
                .unregisterOnSharedPreferenceChangeListener(listener);
    }
}
