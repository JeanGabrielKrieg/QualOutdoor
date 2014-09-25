package com.qualoutdoor.recorder.statistics;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.qualoutdoor.recorder.R;

/**
 * This fragment displays a Strip Pager used to navigate between different
 * screen of statistics.
 * 
 * @author Gaborit Nicolas
 */
public class StatisticsFragment extends Fragment {

    /**
     * The limit of offscreen fragments cached. That's at most (LIMIT + 1 +
     * LIMIT) fragments kept alive at once
     */
    /*
     * We set it beyond our number of fragments for smooth transitions between
     * the fragments. Be careful though, we shouldn't do this when our fragments
     * will become resources demanding
     */
    private static final int OFFSCREEN_PAGE_LIMIT = 3;

    /**
     * The PagerAdapter that will provide fragments representing each an object
     * in the collection.
     */
    private StatisticsPagerAdapter statisticsPagerAdapter;

    /**
     * The ViewPager that will display the object collection.
     */
    private ViewPager mViewPager;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        super.onCreate(savedInstanceState);

        // Create an adapter that when requested, will return a fragment
        // representing an object in
        // the collection.
        //
        // ViewPager and its adapters use support library fragments, so we must
        // use
        // getSupportFragmentManager.
        statisticsPagerAdapter = new StatisticsPagerAdapter(
                getChildFragmentManager());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_statistics, container,
                false);

        // Set up the ViewPager
        mViewPager = (ViewPager) view.findViewById(R.id.pager);
        // Setting the number of offscreen pages to keep alive
        mViewPager.setOffscreenPageLimit(OFFSCREEN_PAGE_LIMIT);
        // Attaching the adapter
        mViewPager.setAdapter(statisticsPagerAdapter);

        return view;
    }

}
