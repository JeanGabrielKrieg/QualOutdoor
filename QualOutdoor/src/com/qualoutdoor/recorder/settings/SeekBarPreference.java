package com.qualoutdoor.recorder.settings;

import android.content.Context;
import android.content.res.TypedArray;
import android.preference.Preference;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;

import com.qualoutdoor.recorder.R;

/**
 * This class can be used to create a Preference that allows to select a value
 * with a handle or seek bar. Adapted from [this
 * source](http://robobunny.com/wp/2013/08/24/android-seekbar-preference-v2/).
 */
public class SeekBarPreference extends Preference implements
        OnSeekBarChangeListener {

    private final String TAG = getClass().getName();

    private static final int DEFAULT_VALUE = 50;

    private static final int MAX_DEFAULT = 100;
    private static final int MIN_DEFAULT = 0;
    private static final int INTERVAL_DEFAULT = 1;

    private int mMaxValue = MAX_DEFAULT;
    private int mMinValue = MIN_DEFAULT;
    private int mInterval = INTERVAL_DEFAULT;
    private int mCurrentValue;
    private String mUnitsLeft = "";
    private String mUnitsRight = "";
    private SeekBar mSeekBar;

    private TextView mStatusText;

    public SeekBarPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
        initPreference(context, attrs);
    }

    public SeekBarPreference(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        initPreference(context, attrs);
    }

    private void initPreference(Context context, AttributeSet attrs) {
        setValuesFromXml(context, attrs);
        mSeekBar = new SeekBar(context, attrs);
        mSeekBar.setMax(mMaxValue - mMinValue);
        mSeekBar.setOnSeekBarChangeListener(this);

        setWidgetLayoutResource(R.layout.seek_bar_preference);
    }

    private void setValuesFromXml(Context context, AttributeSet attrs) {
        // Grab the styled attribute defined in SeekBarPreference and in android
        TypedArray a = context.getTheme().obtainStyledAttributes(attrs,
                R.styleable.SeekBarPreference, 0, 0);

        try {

            mMaxValue = a.getInteger(R.styleable.SeekBarPreference_maxValue,
                    MAX_DEFAULT);
            mMinValue = a.getInteger(R.styleable.SeekBarPreference_minValue,
                    MIN_DEFAULT);

            mUnitsLeft = a.getString(R.styleable.SeekBarPreference_unitsLeft);
            mUnitsRight = a.getString(R.styleable.SeekBarPreference_unitsRight);
            if (mUnitsLeft == null)
                mUnitsLeft = "";
            if (mUnitsRight == null)
                mUnitsRight = "";

            mInterval = a.getInteger(R.styleable.SeekBarPreference_interval,
                    INTERVAL_DEFAULT);

        } finally {
            // The typed array is a shared ressource that must be freed
            a.recycle();
        }

    }

    @Override
    protected View onCreateView(ViewGroup parent) {
        View view = super.onCreateView(parent);

        // The basic preference layout puts the widget frame to the right of the
        // title and summary,
        // so we need to change it a bit - the seekbar should be under them.
        LinearLayout layout = (LinearLayout) view;
        layout.setOrientation(LinearLayout.VERTICAL);

        return view;
    }

    @Override
    public void onBindView(View view) {
        super.onBindView(view);

        try {
            // move our seekbar to the new view we've been given
            ViewParent oldContainer = mSeekBar.getParent();
            ViewGroup newContainer = (ViewGroup) view
                    .findViewById(R.id.seekBarPrefBarContainer);

            if (oldContainer != newContainer) {
                // remove the seekbar from the old view
                if (oldContainer != null) {
                    ((ViewGroup) oldContainer).removeView(mSeekBar);
                }
                // remove the existing seekbar (there may not be one) and add
                // ours
                newContainer.removeAllViews();
                newContainer.addView(mSeekBar,
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT);
            }
        } catch (Exception ex) {
            Log.e(TAG, "Error binding view: " + ex.toString());
        }

        // if dependency is false from the beginning, disable the seek bar
        if (view != null && !view.isEnabled()) {
            mSeekBar.setEnabled(false);
        }

        updateView(view);
    }

    /**
     * Update a SeekBarPreference view with our current state
     * 
     * @param view
     */
    protected void updateView(View view) {

        try {
            mStatusText = (TextView) view.findViewById(R.id.seekBarPrefValue);

            // Set the text for the value, surrounded by left and right text
            setStatusText(mCurrentValue);

            // Set the current progress of the seek bar
            mSeekBar.setProgress(mCurrentValue - mMinValue);

        } catch (Exception e) {
            Log.e(TAG, "Error updating seek bar preference", e);
        }

    }

    private void setStatusText(int value) {
        mStatusText.setText(mUnitsLeft + " " + value + " " + mUnitsRight);
    }

    @Override
    public void onProgressChanged(SeekBar seekBar, int progress,
            boolean fromUser) {
        int newValue = progress + mMinValue;

        if (newValue > mMaxValue)
            newValue = mMaxValue;
        else if (newValue < mMinValue)
            newValue = mMinValue;
        else if (mInterval != 1 && newValue % mInterval != 0)
            newValue = Math.round(((float) newValue) / mInterval) * mInterval;

        // change rejected, revert to the previous value
        if (!callChangeListener(newValue)) {
            seekBar.setProgress(mCurrentValue - mMinValue);
            return;
        }

        // change accepted, store it
        mCurrentValue = newValue;
        setStatusText(newValue);
        persistInt(newValue);

    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {}

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {
        notifyChanged();
    }

    @Override
    protected Object onGetDefaultValue(TypedArray ta, int index) {

        int defaultValue = ta.getInt(index, DEFAULT_VALUE);
        return defaultValue;

    }

    @Override
    protected void onSetInitialValue(boolean restoreValue, Object defaultValue) {

        if (restoreValue) {
            mCurrentValue = getPersistedInt(mCurrentValue);
        } else {
            int temp = 0;
            try {
                temp = (Integer) defaultValue;
            } catch (Exception ex) {
                Log.e(TAG, "Invalid default value: " + defaultValue.toString());
            }

            persistInt(temp);
            mCurrentValue = temp;
        }

    }

    /**
     * make sure that the seekbar is disabled if the preference is disabled
     */
    @Override
    public void setEnabled(boolean enabled) {
        super.setEnabled(enabled);
        mSeekBar.setEnabled(enabled);
    }

    @Override
    public void onDependencyChanged(Preference dependency,
            boolean disableDependent) {
        super.onDependencyChanged(dependency, disableDependent);

        // Disable movement of seek bar when dependency is false
        if (mSeekBar != null) {
            mSeekBar.setEnabled(!disableDependent);
        }
    }
}
