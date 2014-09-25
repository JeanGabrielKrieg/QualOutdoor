package com.qualoutdoor.recorder.statistics;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import com.qualoutdoor.recorder.GenericFragment;
import com.qualoutdoor.recorder.charting.NeighborsChartFragment;
import com.qualoutdoor.recorder.charting.SignalStrengthChartFragment;

/**
 * The pager adapter for the StatisticsFragment.
 * 
 * We extend FragmentStatePagerAdapter, which will destroy and re-create
 * fragments as needed, saving and restoring their state in the process. This is
 * important to conserve memory and is a best practice when allowing navigation
 * between objects in a potentially large collection.
 * 
 * @author Gaborit Nicolas
 * 
 */
public class StatisticsPagerAdapter extends FragmentPagerAdapter {

    /** Position of the signal strength chart fragment */
    private static final int WEB_VIEW = 0;
    /** Position of the neighbors chart fragment */
    private static final int NEIGHBORS = 1;

    /** The list of the fragment titles */
    /*
     * TODO This is temporary hard coded. Should be referenced in XML, see the
     * example in the HomePagerAdapter class
     */
    private CharSequence[] fragmentTitles = {
            "Signal Strength", "Neighbor Cells"
    };

    public StatisticsPagerAdapter(FragmentManager fm) {
        super(fm);
    }

    @Override
    public Fragment getItem(int i) {
        // Initialize the resulting fragment
        Fragment result = new GenericFragment();

        // Initialize the arguments bundle
        Bundle args = new Bundle();
        args.putCharSequence(GenericFragment.FRAGMENT_TEXT, fragmentTitles[i]);

        // Switch on the fragment name
        switch (i) {
        case WEB_VIEW:
            result = new SignalStrengthChartFragment();
            break;
        case NEIGHBORS:
            result = new NeighborsChartFragment();
            break;
        }

        // Attach the arguments
        result.setArguments(args);

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
