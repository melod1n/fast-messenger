package com.meloda.fast.widget

import android.annotation.SuppressLint
import android.content.Context
import android.util.AttributeSet
import android.widget.LinearLayout
import androidx.core.content.withStyledAttributes
import com.meloda.fast.R

@SuppressLint("CustomViewStyleable")
class BoundedLinearLayout : LinearLayout {
    private var mBoundedWidth: Int = 0
    private var mBoundedHeight: Int = 0

    constructor(context: Context) : super(context) {
        mBoundedWidth = 0
        mBoundedHeight = 0
    }

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {
        context.withStyledAttributes(attrs, R.styleable.BoundedView) {
            mBoundedWidth = getDimensionPixelSize(R.styleable.BoundedView_bounded_width, 0)
            mBoundedHeight = getDimensionPixelSize(R.styleable.BoundedView_bounded_height, 0)
        }
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
        var newWidthMeasureSpec = widthMeasureSpec
        var newHeightMeasureSpec = heightMeasureSpec

        val measuredWidth = MeasureSpec.getSize(newWidthMeasureSpec)
        if (mBoundedWidth in 1 until measuredWidth) {
            val measureMode = MeasureSpec.getMode(newWidthMeasureSpec)
            newWidthMeasureSpec = MeasureSpec.makeMeasureSpec(mBoundedWidth, measureMode)
        }

        val measuredHeight = MeasureSpec.getSize(newHeightMeasureSpec)
        if (mBoundedHeight in 1 until measuredHeight) {
            val measureMode = MeasureSpec.getMode(newHeightMeasureSpec)
            newHeightMeasureSpec = MeasureSpec.makeMeasureSpec(mBoundedHeight, measureMode)
        }
        super.onMeasure(newWidthMeasureSpec, newHeightMeasureSpec)
    }
}