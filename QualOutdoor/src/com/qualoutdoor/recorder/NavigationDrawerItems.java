package com.qualoutdoor.recorder;

import android.support.v4.app.Fragment;

import com.qualoutdoor.recorder.home.HomeFragment;
import com.qualoutdoor.recorder.map.DataMapFragment;
import com.qualoutdoor.recorder.scripts.ScriptListFragment;
import com.qualoutdoor.recorder.statistics.StatisticsFragment;

/**
 * This static class defines the order of the NavigationDrawer items and the
 * association with their corresponding fragments.
 * 
 * @author Gaborit Nicolas
 */
public class NavigationDrawerItems {
    /* Not meant to be instantiated */
    private NavigationDrawerItems() {}

    /** The items position of the Home section */
    public static final int HOME = 0;
    /** The items position of the Map section */
    public static final int MAP = 1;
    /** The items position of the Statistics section */
    public static final int STATISTICS = 2;
    /** The items position of the Scripts section */
    public static final int SCRIPTS = 3;

    /** The navigation titles */
    // Fetch the navigation titles from the application resources
    public static String[] navigationTitles = QualOutdoorRecorderApp
            .getAppResources().getStringArray(
                    R.array.top_level_navigation_titles);

    /** Get the fragment corresponding to the given item */
    public static Fragment getFragment(int itemPosition) {
        // The fragment that will be returned
        Fragment result = null;

        // Assign the correct fragment depending on the item position
        switch (itemPosition) {
        case HOME:
            result = new HomeFragment();
            break;
        case MAP:
            result = new DataMapFragment();
            break;
        case STATISTICS:
            result = new StatisticsFragment();
            break;
        case SCRIPTS:
            result = new ScriptListFragment();
            break;
        }

        return result;
    }

}
