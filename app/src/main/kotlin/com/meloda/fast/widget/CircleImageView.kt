package com.meloda.fast.widget

import android.content.Context
import android.graphics.Canvas
import android.graphics.Path
import android.graphics.RectF
import android.util.AttributeSet
import androidx.appcompat.widget.AppCompatImageView
import androidx.core.view.doOnPreDraw

class CircleImageView : AppCompatImageView {

    companion object {
        val SCALE_TYPE = ScaleType.CENTER_CROP
    }

    private var path: Path? = null
    private var rect: RectF? = null

    constructor(context: Context) : this(context, null)

    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0)

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    ) {
        init()
    }


    override fun onDraw(canvas: Canvas) {
        rect?.let { rect ->
            if (rect.right == 0F || rect.bottom == 0F) {
                createRect(width, height)
            }
        }

        path?.run { canvas.clipPath(this) }
        super.onDraw(canvas)
    }

    private fun init() {
        scaleType = SCALE_TYPE

        doOnPreDraw { createRect(width, height) }
    }

    private fun createRect(width: Int, height: Int) {
        path = Path()
        rect = RectF(0f, 0f, width.toFloat(), height.toFloat()).apply {
            path?.addRoundRect(
                this,
                width.toFloat() / 2F,
                height.toFloat() / 2F,
                Path.Direction.CW
            )
        }
    }
}