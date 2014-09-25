package com.qualoutdoor.recorder.map;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Paint.Align;
import android.graphics.RectF;
import android.graphics.Shader.TileMode;
import android.util.AttributeSet;
import android.view.View;

import com.qualoutdoor.recorder.R;

/**
 * @class Custom view that shows a colored scale with a label. Be careful, the
 *        magic behind onMeasure cannot be mastered by ordinary programmers, I
 *        haven't fully understood this art myself... Please excuse the poor
 *        design of this class...
 * @author Gaborit Nicolas
 */
public class ColoredScale extends View {

    /** The text of the label */
    private String mText;
    /** The text of the minimum value */
    private String mMin;
    /** The text of the maximum value */
    private String mMax;
    /** The label color */
    private int mTextColor;
    /** The minimum value of the scale */
    private float mStartValue;
    /** The maximum value of the scale */
    private float mEndValue;
    /** The precision of graduation */
    private float mGraduationStep;
    /** The starting color of the scale */
    private int mStartColor;
    /** The ending color of the scale */
    private int mEndColor;
    /** The starting color of the graduation lines */
    private int mGraduationStartColor;
    /** The ending color of the graduation lines */
    private int mGraduationEndColor;
    /** Paint that define the scale style */
    private Paint mScalePaint;
    /** Paint that define the graduation style */
    private Paint mGraduationPaint;
    /** Paint that define the label style */
    private Paint mTextPaint;
    /** Whether a label should be displayed */
    private boolean mShowText = false;

    // Values and objects which are set ahead of time
    /** The X coordinate of the text label */
    private float mTextX = 0.0f;
    /** The Y coordinate of the text label */
    private float mTextY = 0.0f;
    /** The width of the text label, in pixels */
    private float mTextWidth = 0.0f;
    /** The height of the text label, in pixels */
    private float mTextHeight = 0.0f;
    /** The length of graduation lines */
    private float mGraduationLength;
    /** The boundaries of the scale itself */
    private RectF mScaleRect = new RectF();

    /**
     * Class constructor taking only a context. Use this constructor to create
     * {@link ColoredScale} objects from your own code.
     * 
     * @param context
     */
    public ColoredScale(Context context) {
        super(context);
        init();
    }

    /**
     * Class constructor taking a context and an attribute set. This constructor
     * is used by the layout engine to construct a {@link ColoredScale} from a
     * set of XML attributes.
     * 
     * @param context
     * @param attrs
     *            An attribute set which can contain attributes from
     *            {@link R.styleable.ColoredScale} as well as attributes
     *            inherited from {@link android.view.View}.
     */
    public ColoredScale(Context context, AttributeSet attrs) {
        super(context, attrs);

        // attrs contains the raw values for the XML attributes
        // that were specified in the layout, which don't include
        // attributes set by styles or themes, and which may have
        // unresolved references. Call obtainStyledAttributes()
        // to get the final values for each attribute.
        //
        // This call uses R.styleable.ColoredScale, which is an array of
        // the custom attributes that were declared in attrs.xml.
        TypedArray a = context.getTheme().obtainStyledAttributes(attrs,
                R.styleable.ColoredScale, 0, 0);

        try {
            // Retrieve the values from the TypedArray and store into
            // fields of this class.
            //
            // The R.styleable.ColoredScale_* constants represent the index for
            // each custom attribute in the R.styleable.ColoredScale array.
            mShowText = a.getBoolean(R.styleable.ColoredScale_showText, false);
            mText = a.getString(R.styleable.ColoredScale_label);
            mMin = a.getString(R.styleable.ColoredScale_minLabel);
            mMax = a.getString(R.styleable.ColoredScale_maxLabel);
            mTextHeight = a.getDimension(R.styleable.ColoredScale_labelHeight,
                    0.0f);
            mTextColor = a.getColor(R.styleable.ColoredScale_labelColor,
                    getResources().getColor(R.color.android_vlightgray));
            mStartValue = a.getFloat(R.styleable.ColoredScale_startValue, 0f);
            mEndValue = a.getFloat(R.styleable.ColoredScale_endValue, 1f);
            mGraduationStep = a.getFloat(
                    R.styleable.ColoredScale_graduationStep, 0.1f);
            mGraduationStartColor = a.getColor(
                    R.styleable.ColoredScale_graduationStartColor,
                    getResources().getColor(R.color.android_darkgray));
            mGraduationEndColor = a.getColor(
                    R.styleable.ColoredScale_graduationEndColor, getResources()
                            .getColor(R.color.android_lightgray));
            mStartColor = a.getColor(R.styleable.ColoredScale_startColor,
                    0xff000000);
            mEndColor = a.getColor(R.styleable.ColoredScale_endColor,
                    0xffffffff);
        } finally {
            // release the TypedArray so that it can be reused.
            a.recycle();
        }

        init();
    }

    /**
     * Initialize some size independant attributes. This code is in a separate
     * method so that it can be called from both constructors.
     */
    private void init() {

        // Set up the paint for the label text
        mTextPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        // Set text to be centered
        mTextPaint.setTextAlign(Paint.Align.CENTER);
        mTextPaint.setColor(mTextColor);
        if (mTextHeight == 0) {
            mTextHeight = mTextPaint.getTextSize();
        } else {
            mTextPaint.setTextSize(mTextHeight);
        }

        // Set up the paint for the scale's colored bar
        mScalePaint = new Paint();
        mScalePaint.setStyle(Paint.Style.FILL);

        // Set up the paint for the scale's graduations
        mGraduationPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mGraduationPaint.setStrokeCap(Paint.Cap.BUTT);
    }

    /**
     * Returns true if the text label should be visible.
     * 
     * @return True if the text label should be visible, false otherwise.
     */
    public boolean getShowText() {
        return mShowText;
    }

    /**
     * Controls whether the text label is visible or not. Setting this property
     * to false allows the colored scale graphic to take up the entire visible
     * area of the control.
     * 
     * @param showText
     *            true if the text label should be visible, false otherwise
     */
    public void setShowText(boolean showText) {
        mShowText = showText;
        // Invalidate the view so it can be redrawn
        invalidate();
    }

    /**
     * Returns the Y position of the label text, in pixels.
     * 
     * @return The Y position of the label text, in pixels.
     */
    public float getTextY() {
        return mTextY;
    }

    /**
     * Set the Y position of the label text, in pixels.
     * 
     * @param textY
     *            the Y position of the label text, in pixels.
     */
    public void setTextY(float textY) {
        mTextY = textY;
        // Invalidate the view so it can be redrawn
        invalidate();
    }

    /**
     * Returns the width reserved for label text, in pixels.
     * 
     * @return The width reserved for label text, in pixels.
     */
    public float getTextWidth() {
        return mTextWidth;
    }

    /**
     * Set the width of the area reserved for label text. This width is
     * constant; it does not change based on the actual width of the label as
     * the label text changes.
     * 
     * @param textWidth
     *            The width reserved for label text, in pixels.
     */
    public void setTextWidth(float textWidth) {
        mTextWidth = textWidth;
        // Invalidate the view so it can be redrawn
        invalidate();
    }

    /**
     * Returns the height of the label font, in pixels.
     * 
     * @return The height of the label font, in pixels.
     */
    public float getTextHeight() {
        return mTextHeight;
    }

    /**
     * Set the height of the label font, in pixels.
     * 
     * @param textHeight
     *            The height of the label font, in pixels.
     */
    public void setTextHeight(float textHeight) {
        mTextHeight = textHeight;
        // Invalidate the view so it can be redrawn
        invalidate();
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);

        //
        // Set dimensions for text, scale, etc

        // This is the real height of the text :
        float textRealHeight = mTextHeight * 1.5f;

        // Calculate the available space for the colored scale
        float realWidth = (float) w - (getPaddingLeft() + getPaddingRight());
        float realHeight = (float) h
                - (getPaddingTop() + getPaddingBottom() + textRealHeight);

        // We make the scale occupy all the available space
        mScaleRect = new RectF(0.0f, 0.0f, realWidth, realHeight);
        // Offset the RectF anchor according to paddings and label size
        mScaleRect.offsetTo(getPaddingLeft(), getPaddingTop() + textRealHeight);

        // Center the text position
        mTextX = w / 2;
        // Place the text after the top padding
        mTextY = getPaddingTop() + mTextHeight;

        // Determine top left corner of the scale
        float top = getPaddingTop() + textRealHeight;
        float left = getPaddingLeft();
        // Create the bounding Rect of the scale
        mScaleRect = new RectF(left, top, left + realWidth, top + realHeight);
        // Recalculate the gradient shader (going from the top-left corner to
        // the bottom right corner of the scale Rect
        LinearGradient scaleShader = new LinearGradient(mScaleRect.left,
                mScaleRect.top, mScaleRect.right, mScaleRect.top, mStartColor,
                mEndColor, TileMode.CLAMP);
        /*
         * TODO instead of using two colors only, define an attribute
         * "color array" and use it to create a gradient that covers all those
         * colors...
         */

        // Attach this shader to the scale paint
        mScalePaint.setShader(scaleShader);

        // Set the graduation length
        mGraduationLength = mScaleRect.height() / 2;
        // Calculate the stroke width for the graduation
        mGraduationPaint.setStrokeWidth(Math.max(2f, mGraduationLength / 15));
        // Calculate the gradient shader for the graduation
        LinearGradient graduationShader = new LinearGradient(0f,
                mScaleRect.bottom, 0f, mScaleRect.bottom - mGraduationLength,
                mGraduationStartColor, mGraduationEndColor, TileMode.MIRROR);
        // Attach this shader to the graduation paint
        mGraduationPaint.setShader(graduationShader);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        // Draw the scale
        canvas.drawRect(mScaleRect, mScalePaint);

        // Draw the label text
        if (getShowText()) {
            // The label
            mTextPaint.setTextAlign(Align.CENTER);
            canvas.drawText(mText, mTextX, mTextY, mTextPaint);
            // The min value
            mTextPaint.setTextAlign(Align.LEFT);
            canvas.drawText(mMin, mScaleRect.left, mTextY, mTextPaint);
            // The max value
            mTextPaint.setTextAlign(Align.RIGHT);
            canvas.drawText(mMax, mScaleRect.right, mTextY, mTextPaint);
        }

        // Draw graduations
        // The graduations x
        float xgrad = mScaleRect.left + mGraduationPaint.getStrokeWidth();
        // The x increment
        float xincr = (mScaleRect.width()) * mGraduationStep
                / (mEndValue - mStartValue);
        float stopY = mScaleRect.bottom - mGraduationLength;
        while (xgrad < mScaleRect.right) {
            canvas.drawLine(xgrad, mScaleRect.bottom, xgrad, stopY,
                    mGraduationPaint);
            xgrad += xincr;
        }
    }

    //
    // Measurement functions. This class uses a simple heuristic: it assumes
    // that the scale should be at least as thick as its label.
    //

    @Override
    protected int getSuggestedMinimumHeight() {
        return (int) (mTextHeight * 3);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        // Try for a height based on our minimum
        int minh = getPaddingLeft() + getPaddingRight()
                + getSuggestedMinimumHeight();
        int h = compatibleResolveSizeAndState(minh, heightMeasureSpec, 1);

        setMeasuredDimension(widthMeasureSpec, h);

        // // Get the measured width at this stage of the layouting
        // int width = getMeasuredWidth();
        // // Get the measured height at this stage of the layouting
        // int height = getMeasuredHeight();
        // // The height and width without padding
        // int widthWithoutPadding = width - getPaddingLeft() -
        // getPaddingRight();
        // int heigthWithoutPadding = height - getPaddingTop()
        // - getPaddingBottom();
        //
        // int maxWidth = (int) (heigthWithoutPadding);
        // int maxHeight = (int) (widthWithoutPadding);
        //
        // if (widthWithoutPadding > maxWidth) {
        // width = maxWidth + getPaddingLeft() + getPaddingRight();
        // } else {
        // height = maxHeight + getPaddingTop() + getPaddingBottom();
        // }
        //
        // setMeasuredDimension(width, height);
        //
        // // Try for a width based on our minimum and take the paddings into
        // // account
        // int minw = getPaddingLeft() + getPaddingRight()
        // + getSuggestedMinimumWidth();
        //
        // int w = Math.max(minw, MeasureSpec.getSize(widthMeasureSpec));
        //
        // // Whatever the width ends up being, ask for a height that would let
        // // the pie get as big as it can
        // int minh = (w - (int) mTextWidth) + getPaddingBottom()
        // + getPaddingTop();
        // int h = Math.min(MeasureSpec.getSize(heightMeasureSpec), minh);
        //
        // setMeasuredDimension(w, h);

    }

    /**
     * Utility to reconcile a desired size and state, with constraints imposed
     * by a MeasureSpec. The original resolveSizeAndState requires API lvl 11.
     * This version is meant to replace it for older version.
     * 
     * @param size
     *            How big the view wants to be
     * @param measureSpec
     *            Constraints imposed by the parent
     * @param childMeasuredState
     * @return Size information bit mask as defined by MEASURED_SIZE_MASK and
     *         MEASURED_STATE_TOO_SMALL.
     */
    private int compatibleResolveSizeAndState(int size, int measureSpec,
            int childMeasuredState) {
        int result = size;
        int specMode = MeasureSpec.getMode(measureSpec);
        int specSize = MeasureSpec.getSize(measureSpec);
        switch (specMode) {
        case MeasureSpec.UNSPECIFIED:
            result = size;
            break;
        case MeasureSpec.AT_MOST:
            if (specSize < size) {
                result = specSize | MEASURED_STATE_TOO_SMALL;
            } else {
                result = size;
            }
            break;
        case MeasureSpec.EXACTLY:
            result = specSize;
            break;
        }
        return result | (childMeasuredState & MEASURED_STATE_MASK);
    }
}
