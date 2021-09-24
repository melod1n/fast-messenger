package com.meloda.fast.widget

import android.content.Context
import android.graphics.Canvas
import android.graphics.Path
import android.graphics.RectF
import android.graphics.Region
import android.util.AttributeSet
import android.widget.FrameLayout
import com.meloda.fast.R

class RoundedFrameLayout : FrameLayout {
    /**
     * The corners than can be changed
     */
    private var topLeftCornerRadius = 0f
    private var topRightCornerRadius = 0f
    private var bottomLeftCornerRadius = 0f
    private var bottomRightCornerRadius = 0f

    constructor(context: Context) : super(context) {
        init(context, null, 0)
    }

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {
        init(context, attrs, 0)
    }

    constructor(
        context: Context,
        attrs: AttributeSet?,
        defStyleAttr: Int
    ) : super(context, attrs, defStyleAttr) {
        init(context, attrs, defStyleAttr)
    }

    private fun init(context: Context, attrs: AttributeSet?, defStyle: Int) {
        val typedArray = context.obtainStyledAttributes(
            attrs,
            R.styleable.RoundedFrameLayout, 0, 0
        )

        topLeftCornerRadius =
            typedArray.getDimension(R.styleable.RoundedFrameLayout_topLeftCornerRadius, 0f)
        topRightCornerRadius =
            typedArray.getDimension(R.styleable.RoundedFrameLayout_topRightCornerRadius, 0f)
        bottomLeftCornerRadius =
            typedArray.getDimension(R.styleable.RoundedFrameLayout_bottomLeftCornerRadius, 0f)
        bottomRightCornerRadius =
            typedArray.getDimension(R.styleable.RoundedFrameLayout_bottomRightCornerRadius, 0f)

        typedArray.recycle()
        setLayerType(LAYER_TYPE_HARDWARE, null)
    }

    override fun dispatchDraw(canvas: Canvas) {
        val count: Int = canvas.save()
        val path = Path()
        val cornerDimensions = floatArrayOf(
            topLeftCornerRadius, topLeftCornerRadius,
            topRightCornerRadius, topRightCornerRadius,
            bottomRightCornerRadius, bottomRightCornerRadius,
            bottomLeftCornerRadius, bottomLeftCornerRadius
        )
        path.addRoundRect(
            RectF(0f, 0f, canvas.width.toFloat(), canvas.height.toFloat()),
            cornerDimensions,
            Path.Direction.CW
        )
        canvas.clipPath(path, Region.Op.INTERSECT)
        canvas.clipPath(path)
        super.dispatchDraw(canvas)
        canvas.restoreToCount(count)
    }

    fun setTopLeftCornerRadius(topLeftCornerRadius: Float) {
        this.topLeftCornerRadius = topLeftCornerRadius
        invalidate()
    }

    fun setTopRightCornerRadius(topRightCornerRadius: Float) {
        this.topRightCornerRadius = topRightCornerRadius
        invalidate()
    }

    fun setBottomLeftCornerRadius(bottomLeftCornerRadius: Float) {
        this.bottomLeftCornerRadius = bottomLeftCornerRadius
        invalidate()
    }

    fun setBottomRightCornerRadius(bottomRightCornerRadius: Float) {
        this.bottomRightCornerRadius = bottomRightCornerRadius
        invalidate()
    }
}