package com.meloda.fast.base.adapter

import android.content.Context
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isInvisible
import androidx.recyclerview.widget.RecyclerView
import com.meloda.fast.extensions.dpToPx
import com.meloda.fast.util.AndroidUtils
import kotlin.math.roundToInt

class EmptyHeaderAdapter(
    var context: Context
) : RecyclerView.Adapter<EmptyHeaderAdapter.Holder>() {

    inner class Holder(itemView: View) : RecyclerView.ViewHolder(itemView)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = Holder(generateHeaderView())

    override fun onBindViewHolder(holder: Holder, position: Int) {
    }

    override fun getItemCount() = 1

    private fun generateHeaderView() = View(context).apply {
        layoutParams = ViewGroup.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            56.dpToPx()
        )
        isClickable = false
        isEnabled = false
        isFocusable = false
        isInvisible = true
    }

}