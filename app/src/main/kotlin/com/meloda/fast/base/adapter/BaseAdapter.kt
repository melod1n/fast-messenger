package com.meloda.fast.base.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter

@Suppress("MemberVisibilityCanBePrivate", "unused")
abstract class BaseAdapter<Item, VH : BaseHolder>(
    var context: Context,
    values: MutableList<Item>,
    diffUtil: DiffUtil.ItemCallback<Item>
) : ListAdapter<Item, VH>(diffUtil) {

    val cleanValues = mutableListOf<Item>()
    val values = mutableListOf<Item>()

    init {
        addAll(values)
    }

    protected var inflater: LayoutInflater = LayoutInflater.from(context)

    var itemClickListener: OnItemClickListener? = null
    var itemLongClickListener: OnItemLongClickListener? = null

    open fun destroy() {
        itemClickListener = null
        itemLongClickListener = null
    }

    override fun getItem(position: Int): Item {
        return values[position]
    }

    fun add(position: Int, item: Item) {
        values.add(position, item)
        cleanValues.add(position, item)
    }

    fun add(item: Item) {
        values += item
        cleanValues.add(item)
    }

    fun addAll(items: List<Item>) {
        values += items
        cleanValues.addAll(items)
    }

    fun addAll(position: Int, items: List<Item>) {
        values.addAll(position, items)
        cleanValues.addAll(position, items)
    }

    fun removeAll(items: List<Item>) {
        values.removeAll(items)
        cleanValues.removeAll(items)
    }

    fun removeAt(index: Int) {
        values.removeAt(index)
        cleanValues.removeAt(index)
    }

    fun remove(item: Item) {
        values.remove(item)
        cleanValues.remove(item)
    }

    fun clear() {
        values.clear()
        cleanValues.clear()
    }

    operator fun get(position: Int): Item {
        return values[position]
    }

    operator fun set(position: Int, item: Item) {
        values[position] = item
        cleanValues[position] = item
    }

    open fun notifyChanges(oldList: List<Item>, newList: List<Item>) {}

    fun isEmpty() = values.isEmpty()
    fun isNotEmpty() = values.isNotEmpty()

    fun view(resId: Int, viewGroup: ViewGroup, attachToRoot: Boolean = false): View {
        return inflater.inflate(resId, viewGroup, attachToRoot)
    }

    fun updateValues(list: MutableList<Item>) {
        values.clear()
        values += list
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        onBindItemViewHolder(holder, position)
    }

    protected fun initListeners(itemView: View, position: Int) {
        if (itemView is AdapterView<*>) return

        itemView.setOnClickListener {
            itemClickListener?.onItemClick(position)
        }

        itemView.setOnLongClickListener {
            itemLongClickListener?.onItemLongClick(position)
            return@setOnLongClickListener itemClickListener == null
        }
    }

    override fun getItemCount(): Int {
        return values.size
    }

    private fun onBindItemViewHolder(holder: VH, position: Int) {
        initListeners(holder.itemView, position)
        holder.bind(position)
    }

}
