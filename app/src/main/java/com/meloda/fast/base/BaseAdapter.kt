package com.meloda.fast.base

import android.content.Context
import android.os.Bundle
import android.os.Parcelable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import androidx.recyclerview.widget.RecyclerView
import com.meloda.arrayutils.ArrayUtils.asArrayList
import com.meloda.fast.common.AppGlobal
import com.meloda.fast.listener.ItemClickListener
import com.meloda.fast.listener.ItemLongClickListener
import java.io.Serializable
import java.util.*


@Suppress("UNCHECKED_CAST")
abstract class BaseAdapter<Item, VH : BaseHolder>(
    var context: Context,
    var values: ArrayList<Item> = arrayListOf()
) : RecyclerView.Adapter<VH>() {

    companion object {
        private const val P_ITEMS = "BaseAdapter.values"
    }

    private var cleanValues: ArrayList<Item>? = null

    private var inflater: LayoutInflater = LayoutInflater.from(context)

    var itemClickListener: ItemClickListener? = null
    var itemLongClickListener: ItemLongClickListener? = null

    open fun destroy() {}

    open fun getItem(position: Int): Item {
        return values[position]
    }

    fun add(position: Int, item: Item) {
        values.add(position, item)
        cleanValues?.add(position, item)
    }

    fun add(item: Item) {
        values.add(item)
        cleanValues?.add(item)
    }

    fun addAll(items: List<Item>) {
        values.addAll(items)
        cleanValues?.addAll(items)
    }

    fun addAll(position: Int, items: List<Item>) {
        values.addAll(position, items)
        cleanValues?.addAll(position, items)
    }

    operator fun set(position: Int, item: Item) {
        values[position] = item
        cleanValues?.set(position, item)
    }

    fun indexOf(item: Item): Int {
        return values.indexOf(item)
    }

    fun removeAt(index: Int) {
        values.removeAt(index)
        cleanValues?.removeAt(index)
    }

    fun remove(item: Item) {
        values.remove(item)
        cleanValues?.remove(item)
    }

    open fun notifyChanges(oldList: List<Item>, newList: List<Item> = values) {}

    fun isEmpty() = values.isNullOrEmpty()

    fun isNotEmpty() = !isEmpty()

    fun view(resId: Int, viewGroup: ViewGroup): View {
        return inflater.inflate(resId, viewGroup, false)
    }

    fun updateValues(arrayList: ArrayList<Item>) {
        values.clear()
        values.addAll(arrayList)
    }

    fun updateValues(list: List<Item>) = updateValues(list.asArrayList())

    override fun onBindViewHolder(holder: VH, position: Int) {
        onBindItemViewHolder(holder, position)
    }

    protected fun initListeners(itemView: View, position: Int) {
        if (itemView is AdapterView<*>) return

        itemView.setOnClickListener {
            if (itemClickListener != null) itemClickListener!!.onItemClick(
                position
            )
        }
        itemView.setOnLongClickListener {
            if (itemLongClickListener != null) itemLongClickListener!!.onItemLongClick(position)
            itemClickListener == null
        }
    }

    override fun getItemCount(): Int {
        return values.size
    }

    private fun onBindItemViewHolder(holder: VH, position: Int) {
        initListeners(holder.itemView, position)
        holder.bind(position)
    }

    fun onSaveInstanceState(): Parcelable {
        val bundle = Bundle()
        if (values.size > 0 && (values[0] is Parcelable || values[0] is Serializable)) {
            bundle.putSerializable(P_ITEMS, values)
        }
        return bundle
    }

    fun post(runnable: Runnable) {
        AppGlobal.handler.post(runnable)
    }

    fun onRestoreInstanceState(state: Parcelable?) {
        if (state is Bundle) {
            if (state.containsKey(P_ITEMS)) {
                values = state.getSerializable(P_ITEMS) as ArrayList<Item>
            }
        }
    }

    fun clear() {
        values.clear()
    }

    open fun filter(query: String) {
        if (cleanValues == null) {
            cleanValues = ArrayList(values)
        }

        values.clear()

        if (query.isEmpty()) {
            values.addAll(cleanValues!!)
        } else {
            for (item in cleanValues!!) {
                if (onQueryItem(item, query)) {
                    values.add(item)
                }
            }
        }

        notifyDataSetChanged()
    }

    open fun onQueryItem(item: Item, query: String): Boolean {
        return false
    }

    operator fun get(index: Int): Item {
        return values[index]
    }

}