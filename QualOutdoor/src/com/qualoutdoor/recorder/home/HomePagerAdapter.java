package com.qualoutdoor.recorder.home;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import com.qualoutdoor.recorder.GenericFragment;
import com.qualoutdoor.recorder.QualOutdoorRecorderApp;
import com.qualoutdoor.recorder.R;

/**
 * This is the pager adapter for the HomeFragment.
 * 
 * We extend FragmentStatePagerAdapter, which will destroy and re-create
 * fragments as needed, saving and restoring their state in the process. This is
 * important to conserve memory and is a best practice when allowing navigation
 * between objects in a potentially large collection.
 * 
 * @author Gaborit Nicolas
 * 
 */
public class HomePagerAdapter extends FragmentPagerAdapter {

    /** Position of the location fragment */
    public static final int LOCATIONS = 0;
    /** Position of the device fragment */
    public static final int DEVICE = 1;
    /** Position of the network fragment */
    public static final int NETWORK = 2;
    /** Position of the neighbor cells fragment */
    public static final int NEIGHBORS = 3;

    /** The fragment titles list */
    private static CharSequence[] fragmentTitles = QualOutdoorRecorderApp
            .getAppResources().getStringArray(R.array.home_pager_titles);

    public HomePagerAdapter(FragmentManager fm) {
        super(fm);
    }

    @Override
    public Fragment getItem(int i) {
        // Initialize the resulting fragment
        Fragment result = null;

        // Switch on the fragment correct fragment
        switch (i) {
        case LOCATIONS:
            result = new LocationFragment();
            break;
        case DEVICE:
            result = new DeviceFragment();
            break;
        case NETWORK:
            result = new NetworkFragment();
            break;
        case NEIGHBORS:
            result = new NeighborsFragment();
            break;
        }

        return result;
    }

    @Override
    public int getCount() {
        return fragmentTitles.length;
    }

    @Override
    public CharSequence getPageTitle(int i) {
        return fragmentTitles[i];
    }

}
