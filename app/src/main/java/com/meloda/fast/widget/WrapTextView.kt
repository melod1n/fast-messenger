package com.meloda.fast.widget

import android.content.Context
import android.text.Layout
import android.util.AttributeSet
import androidx.appcompat.widget.AppCompatTextView
import com.meloda.fast.R
import kotlin.math.ceil
import kotlin.math.max

class WrapTextView(context: Context, attrs: AttributeSet? = null) :
    AppCompatTextView(context, attrs) {

    private var fixWrapText = false

    constructor(context: Context) : this(context, null)

    init {
        init(context, attrs)
    }

    private fun init(context: Context, attrs: AttributeSet?) {
        val a = context.theme.obtainStyledAttributes(attrs, R.styleable.WrapTextView, 0, 0)

        try {
            fixWrapText = a.getBoolean(R.styleable.WrapTextView_fixWrap, false)
        } finally {
            a.recycle()
        }
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)

        if (fixWrapText && MeasureSpec.getMode(widthMeasureSpec) != MeasureSpec.EXACTLY) {
            val width = getMaxWidth(layout)
            if (width in 1 until measuredWidth) {
                super.onMeasure(
                    MeasureSpec.makeMeasureSpec(width, MeasureSpec.AT_MOST),
                    heightMeasureSpec
                )
            }
        }
    }

    private fun getMaxWidth(layout: Layout): Int {
        if (layout.lineCount < 2) return 0

        var maxWidth = 0.0f
        for (i in 0 until layout.lineCount) {
            maxWidth = max(maxWidth, layout.getLineWidth(i))
        }

        return ceil(maxWidth).toInt()
    }
}