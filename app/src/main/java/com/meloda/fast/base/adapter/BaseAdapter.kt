package com.meloda.fast.base.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import androidx.lifecycle.MutableLiveData
import androidx.recyclerview.widget.RecyclerView
import com.meloda.fast.base.BaseHolder
import com.meloda.fast.extensions.LiveDataExtensions.add
import com.meloda.fast.extensions.LiveDataExtensions.addAll
import com.meloda.fast.extensions.LiveDataExtensions.clear
import com.meloda.fast.extensions.LiveDataExtensions.get
import com.meloda.fast.extensions.LiveDataExtensions.isEmpty
import com.meloda.fast.extensions.LiveDataExtensions.isNotEmpty
import com.meloda.fast.extensions.LiveDataExtensions.plusAssign
import com.meloda.fast.extensions.LiveDataExtensions.remove
import com.meloda.fast.extensions.LiveDataExtensions.removeAll
import com.meloda.fast.extensions.LiveDataExtensions.removeAt
import com.meloda.fast.extensions.LiveDataExtensions.set
import com.meloda.fast.extensions.LiveDataExtensions.size

@Suppress("UNCHECKED_CAST", "unused", "MemberVisibilityCanBePrivate", "CanBeParameter")
abstract class BaseAdapter<Item, VH : BaseHolder>(
    var context: Context,
    values: ArrayList<Item>
) : RecyclerView.Adapter<VH>() {

    val cleanValues = MutableLiveData<MutableList<Item>>(arrayListOf())
    val values = MutableLiveData<MutableList<Item>>(arrayListOf())

    protected var inflater: LayoutInflater = LayoutInflater.from(context)

    init {
        this.values.value = values
    }

    var itemClickListener: OnItemClickListener? = null
    var itemLongClickListener: OnItemLongClickListener? = null

    open fun destroy() {
        itemClickListener = null
        itemLongClickListener = null
    }

    open fun getItem(position: Int): Item {
        return values[position]
    }

    fun add(position: Int, item: Item) {
        values.add(item, position)
        cleanValues.add(item, position)
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
        values.addAll(items, position)
        cleanValues.addAll(items, position)
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

    fun updateValues(arrayList: ArrayList<Item>) {
        values.clear()
        values += arrayList
    }

    fun updateValues(list: List<Item>) = updateValues(ArrayList(list))

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

    val size get() = itemCount

    private fun onBindItemViewHolder(holder: VH, position: Int) {
        initListeners(holder.itemView, position)
        holder.bind(position)
    }

}
