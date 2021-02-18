package ru.melod1n.project.vkm.base

import android.view.View
import androidx.recyclerview.widget.RecyclerView

abstract class BaseHolder(v: View) : RecyclerView.ViewHolder(v) {
    abstract fun bind(position: Int)
}