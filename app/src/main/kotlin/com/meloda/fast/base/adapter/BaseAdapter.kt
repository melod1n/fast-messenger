package com.meloda.fast.base.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.widget.AdapterView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import com.meloda.fast.model.DataItem
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@Suppress("MemberVisibilityCanBePrivate", "unused", "UNCHECKED_CAST")
@SuppressLint("NotifyDataSetChanged")
abstract class BaseAdapter<T : DataItem<*>, VH : BaseHolder> constructor(
    var context: Context,
    diffUtil: DiffUtil.ItemCallback<T>,
    preAddedValues: List<T> = emptyList(),
) : ListAdapter<T, VH>(diffUtil) {

    protected val adapterScope = CoroutineScope(Dispatchers.Default)
    private val cleanList = mutableListOf<T>()

    protected var inflater: LayoutInflater = LayoutInflater.from(context)

    var itemClickListener: ((position: Int) -> Unit)? = null
    var itemLongClickListener: ((position: Int) -> Boolean)? = null

    init {
        cleanList.addAll(preAddedValues)
        addAll(preAddedValues)
    }

    fun cloneCurrentList(): MutableList<T> {
        return ArrayList(currentList)
    }

    open fun destroy() {}

    fun getOrNull(position: Int): T? {
        return if (position >= 0 && position <= currentList.lastIndex) get(position) else null
    }

    fun getOrElse(position: Int, defaultValue: (Int) -> T): T {
        return if (position >= 0 && position <= currentList.lastIndex) get(position)
        else defaultValue(position)
    }

    fun add(
        item: T,
        position: Int? = null,
        beforeFooter: Boolean = false,
        commitCallback: (() -> Unit)? = null
    ) = addAll(listOf(item), position, beforeFooter, commitCallback)

    fun addAll(
        items: List<T>,
        position: Int? = null,
        beforeFooter: Boolean = false,
        commitCallback: (() -> Unit)? = null
    ) {
        adapterScope.launch {
            val newList = cloneCurrentList()
            if (position == null) {
                val mutableItems = items.toMutableList()
                if (beforeFooter && newList.lastOrNull() is DataItem.Footer) {
                    newList.removeLastOrNull()
                }

                if (beforeFooter) {
                    mutableItems += DataItem.Footer as T
                }

                newList.addAll(mutableItems)
                cleanList.addAll(mutableItems)
            } else {
                newList.addAll(position, items)
                cleanList.addAll(position, items)
            }

            withContext(Dispatchers.Main) {
                submitList(newList, commitCallback)
            }
        }
    }

    fun remove(item: T, commitCallback: (() -> Unit)? = null) =
        removeAll(listOf(item), commitCallback)

    fun removeAll(items: List<T>, commitCallback: (() -> Unit)? = null) {
        val newList = cloneCurrentList()
        newList.removeAll(items)
        submitList(newList, commitCallback)

        cleanList.removeAll(items)
    }

    fun removeAt(index: Int, commitCallback: (() -> Unit)? = null) {
        val newList = cloneCurrentList()
        newList.removeAt(index)
        submitList(newList, commitCallback)

        cleanList.removeAt(index)
    }

    fun clear(commitCallback: (() -> Unit)? = null) = removeAll(currentList, commitCallback)

    fun setItem(
        item: T,
        withHeader: Boolean = false,
        withFooter: Boolean = false,
        commitCallback: (() -> Unit)? = null
    ) = setItems(listOf(item), withHeader, withFooter, commitCallback)

    @Suppress("UNCHECKED_CAST")
    fun setItems(
        list: List<T>?,
        withHeader: Boolean = false,
        withFooter: Boolean = false,
        commitCallback: (() -> Unit)? = null
    ) {
        adapterScope.launch {
            val items = mutableListOf<T>()
            if (withHeader) items.add(DataItem.Header as T)
            if (!list.isNullOrEmpty()) items.addAll(list)
            if (withFooter) items.add(DataItem.Footer as T)

            withContext(Dispatchers.Main) {
                if (items == currentList) {
                    refreshList()
                } else {
                    submitList(items, commitCallback)
                }
            }
        }
    }

    fun indexOf(item: T): Int {
        return currentList.indexOf(item)
    }

    val indices get() = currentList.indices

    operator fun get(position: Int): T {
        return currentList[position]
    }

    operator fun set(position: Int, item: T) = setItem(position, item)

    fun setItem(position: Int, item: T, commitCallback: (() -> Unit)? = null) {
        val newList = cloneCurrentList()
        newList[position] = item
        submitList(newList, commitCallback)

        cleanList[position] = item
    }

    fun isEmpty() = currentList.isEmpty()
    fun isNotEmpty() = currentList.isNotEmpty()

    @SuppressLint("NotifyDataSetChanged")
    fun refreshList() {
        notifyDataSetChanged()
    }

    fun updateCleanList(list: List<T>?) {
        cleanList.clear()
        list?.run { cleanList.addAll(this) }
    }

    override fun submitList(list: List<T>?) {
        super.submitList(list)
        updateCleanList(list)
    }

    override fun submitList(list: List<T>?, commitCallback: Runnable?) {
        super.submitList(list, commitCallback)
        updateCleanList(list)
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        initListeners(holder.itemView, position)
        holder.bind(position)
    }

    protected open fun initListeners(itemView: View, position: Int) {
        if (itemView is AdapterView<*>) return

        itemView.setOnClickListener { itemClickListener?.invoke(position) }
        itemView.setOnLongClickListener {
            itemLongClickListener?.invoke(position)
            return@setOnLongClickListener itemClickListener != null
        }
    }

    override fun getItemCount(): Int {
        return currentList.size
    }

    val lastPosition get() = currentList.lastIndex
}