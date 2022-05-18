package com.meloda.fast.view

import android.graphics.Rect
import android.view.View
import androidx.recyclerview.widget.RecyclerView

class SpaceItemDecoration(
    private val topMargin: Int? = null,
    private val endMargin: Int? = null,
    private val bottomMargin: Int? = null,
    private val startMargin: Int? = null
) : RecyclerView.ItemDecoration() {

    override fun getItemOffsets(
        outRect: Rect,
        view: View,
        parent: RecyclerView,
        state: RecyclerView.State
    ) {
        topMargin?.run { outRect.top = this }
        endMargin?.run { outRect.right = this }
        bottomMargin?.run { outRect.bottom = this }
        startMargin?.run { outRect.left = this }
    }

}