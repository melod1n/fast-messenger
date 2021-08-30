package com.meloda.fast.widget

import android.content.Context
import android.graphics.Canvas
import android.graphics.Path
import android.graphics.RectF
import android.util.AttributeSet
import android.view.ViewTreeObserver
import androidx.appcompat.widget.AppCompatImageView

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


    override fun onDraw(canvas: Canvas?) {
        rect ?: return
        canvas ?: return

        if (rect!!.right == 0f || rect!!.bottom == 0f) {
            createRect(width, height)
        }

        canvas.clipPath(path!!)
        super.onDraw(canvas)
    }

    private fun init() {
        scaleType = SCALE_TYPE

        viewTreeObserver.addOnPreDrawListener(object : ViewTreeObserver.OnPreDrawListener {
            override fun onPreDraw(): Boolean {
                createRect(width, height)
                viewTreeObserver.removeOnPreDrawListener(this)
                return false
            }
        })
    }

    private fun createRect(width: Int, height: Int) {
        rect = RectF(0f, 0f, width.toFloat(), height.toFloat())
        path = Path()
        path!!.addRoundRect(
            rect!!,
            (width / 2).toFloat(),
            (height / 2).toFloat(),
            Path.Direction.CW
        )
    }
}