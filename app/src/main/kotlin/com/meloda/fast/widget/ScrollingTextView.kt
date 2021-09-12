package com.meloda.fast.widget

import android.content.Context
import android.graphics.Rect
import android.util.AttributeSet
import com.google.android.material.textview.MaterialTextView

class ScrollingTextView : MaterialTextView {
    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    )

    override fun onFocusChanged(focused: Boolean, direction: Int, previouslyFocusedRect: Rect?) {
        if (focused) super.onFocusChanged(focused, direction, previouslyFocusedRect)
    }

    override fun onWindowFocusChanged(focused: Boolean) {
        if (focused) super.onWindowFocusChanged(true)
    }

    override fun isFocused(): Boolean {
        return true
    }
}