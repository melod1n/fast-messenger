package com.meloda.fast.adapter

import android.content.Context
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import com.meloda.fast.R
import com.meloda.fast.base.BaseAdapter
import com.meloda.fast.base.BaseHolder
import com.meloda.fast.item.SimpleMenuItem
import java.util.*

class SimpleItemAdapter(context: Context, values: ArrayList<SimpleMenuItem>) :
    BaseAdapter<SimpleMenuItem, SimpleItemAdapter.ViewHolder>(context, values) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(view(R.layout.item_simple_menu, parent))
    }

    inner class ViewHolder(v: View) : BaseHolder(v) {

        private val title: TextView = v.findViewById(R.id.profileItemTitle)
        private val icon: ImageView = v.findViewById(R.id.profileItemIcon)

        override fun bind(position: Int) {
            val item = getItem(position)

            title.text = item.title

            icon.setImageDrawable(item.icon)
        }

    }
}