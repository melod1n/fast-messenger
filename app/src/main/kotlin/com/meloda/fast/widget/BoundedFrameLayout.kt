package com.meloda.fast.widget

import android.annotation.SuppressLint
import android.content.Context
import android.util.AttributeSet
import android.widget.FrameLayout
import com.meloda.fast.R

class BoundedFrameLayout : FrameLayout {
    private var mBoundedWidth: Int
    private var mBoundedHeight: Int

    constructor(context: Context) : super(context) {
        mBoundedWidth = 0
        mBoundedHeight = 0
    }

    @SuppressLint("CustomViewStyleable")
    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {
        val a = context.obtainStyledAttributes(attrs, R.styleable.BoundedView)
        mBoundedWidth = a.getDimensionPixelSize(R.styleable.BoundedView_bounded_width, 0)
        mBoundedHeight = a.getDimensionPixelSize(R.styleable.BoundedView_bounded_height, 0)
        a.recycle()
    }

    var maxWidth: Int
        get() = mBoundedWidth
        set(width) {
            if (mBoundedWidth != width) {
                mBoundedWidth = width
                requestLayout()
            }
        }

    var maxHeight: Int
        get() = mBoundedHeight
        set(height) {
            if (mBoundedHeight != height) {
                mBoundedHeight = height
                requestLayout()
            }
        }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        // Adjust width as necessary
        var widthMeasureSpec = widthMeasureSpec
        var heightMeasureSpec = heightMeasureSpec
        val measuredWidth = MeasureSpec.getSize(widthMeasureSpec)

        if (mBoundedWidth in 1 until measuredWidth) {
            val measureMode = MeasureSpec.getMode(widthMeasureSpec)
            widthMeasureSpec = MeasureSpec.makeMeasureSpec(mBoundedWidth, measureMode)
        }

        // Adjust height as necessary
        val measuredHeight = MeasureSpec.getSize(heightMeasureSpec)
        if (mBoundedHeight in 1 until measuredHeight) {
            val measureMode = MeasureSpec.getMode(heightMeasureSpec)
            heightMeasureSpec = MeasureSpec.makeMeasureSpec(mBoundedHeight, measureMode)
        }
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
    }

}