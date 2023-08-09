package com.meloda.fast.base.adapter

import androidx.recyclerview.widget.DiffUtil
import com.hannesdorfmann.adapterdelegates4.AdapterDelegate
import com.hannesdorfmann.adapterdelegates4.AsyncListDifferDelegationAdapter
import com.meloda.fast.model.base.AdapterDiffItem

class AsyncDiffItemAdapter(
    customDiffCallback: DiffUtil.ItemCallback<AdapterDiffItem>? = null,
    vararg delegates: AdapterDelegate<out List<AdapterDiffItem>>,
) : AsyncListDifferDelegationAdapter<AdapterDiffItem>(customDiffCallback ?: DIFF_CALLBACK) {

    constructor(
        vararg delegates: AdapterDelegate<out List<AdapterDiffItem>>,
    ) : this(customDiffCallback = null) {
        delegates.forEach(::addDelegate)
    }

    init {
        delegates.forEach(::addDelegate)
    }

    fun addDelegates(vararg delegates: AdapterDelegate<out List<AdapterDiffItem>>) {
        delegates.forEach(::addDelegate)
    }

    @Suppress("UNCHECKED_CAST")
    fun addDelegate(delegate: AdapterDelegate<out List<AdapterDiffItem>>) {
        (delegate as? AdapterDelegate<List<AdapterDiffItem>>)?.let(delegatesManager::addDelegate)
    }

    fun isEmpty() = itemCount == 0
    fun isNotEmpty() = itemCount > 0

    companion object {
        val DIFF_CALLBACK = object : DiffUtil.ItemCallback<AdapterDiffItem>() {
            override fun areItemsTheSame(
                oldItem: AdapterDiffItem,
                newItem: AdapterDiffItem,
            ): Boolean {
                return oldItem.areItemsTheSame(newItem)
            }

            override fun areContentsTheSame(
                oldItem: AdapterDiffItem,
                newItem: AdapterDiffItem,
            ): Boolean {
                return oldItem.areContentsTheSame(newItem)
            }
        }
    }
}
