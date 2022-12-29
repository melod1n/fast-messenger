package com.meloda.fast.model.base

interface AdapterDiffItem {

    val id: Int

    fun areItemsTheSame(newItem: AdapterDiffItem): Boolean {
        return id == newItem.id
    }

    fun areContentsTheSame(newItem: AdapterDiffItem): Boolean {
        return this == newItem
    }
}
