package com.meloda.fast.base.adapter

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.AdapterView
import android.widget.Filter
import android.widget.Filterable
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import kotlinx.coroutines.*
import kotlin.properties.Delegates

@Suppress("MemberVisibilityCanBePrivate", "unused", "UNCHECKED_CAST")
abstract class BaseAdapter<T : Any, VH : BaseHolder> constructor(
    var context: Context,
    diffUtil: DiffUtil.ItemCallback<T>,
    preAddedValues: List<T> = emptyList(),
) : ListAdapter<T, VH>(diffUtil), Filterable {

    private var valuesFilter: ValuesFilter? = null

    protected val adapterScope = CoroutineScope(Dispatchers.Default)
    private val cleanList = mutableListOf<T>()

    protected var inflater: LayoutInflater = LayoutInflater.from(context)

    var itemClickListener: ((position: Int) -> Unit)? = null
    var itemLongClickListener: ((position: Int) -> Boolean)? = null

    private val listForSave = mutableListOf<T>()

    var isSearching: Boolean by Delegates.observable(false) { _, _, _ ->
        updateSearchingState()
    }

    init {
        cleanList.addAll(preAddedValues)
        addAll(preAddedValues)
    }

    fun cloneCurrentList(): MutableList<T> {
        return currentList.toMutableList()
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
        commitCallback: (() -> Unit)? = null
    ) = addAll(listOf(item), position, commitCallback)

    fun addAll(
        items: List<T>,
        position: Int? = null,
        commitCallback: (() -> Unit)? = null
    ) {
        adapterScope.launch {
            val newList = cloneCurrentList()
            if (position == null) {
                val mutableItems = items.toMutableList()

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
        cleanList.removeAll(items)

        submitList(newList, commitCallback)
    }

    fun removeAt(index: Int, commitCallback: (() -> Unit)? = null) {
        val newList = cloneCurrentList()
        newList.removeAt(index)
        cleanList.removeAt(index)

        submitList(newList, commitCallback)
    }

    fun clear(commitCallback: (() -> Unit)? = null) = removeAll(currentList, commitCallback)

    fun setItem(
        item: T,
        commitCallback: (() -> Unit)? = null
    ) = setItems(listOf(item), commitCallback)

    @Suppress("UNCHECKED_CAST")
    fun setItems(
        list: List<T>?,
        commitCallback: (() -> Unit)? = null
    ) {
        adapterScope.launch {
            val items = mutableListOf<T>()
            if (!list.isNullOrEmpty()) items.addAll(list)

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

    fun searchIndexOf(item: T): Int? {
        val index = indexOf(item)
        return if (index == -1) null else index
    }

    val indices get() = currentList.indices

    operator fun get(position: Int): T {
        return currentList[position]
    }

    operator fun set(position: Int, item: T) = setItem(position, item)

    fun setItem(position: Int, item: T, commitCallback: (() -> Unit)? = null) {
        val newList = cloneCurrentList()
        newList[position] = item
        cleanList[position] = item

        submitList(newList, commitCallback)
    }

    fun isEmpty() = currentList.isEmpty()
    fun isNotEmpty() = currentList.isNotEmpty()

    fun refreshList() {
        notifyItemRangeChanged(0, itemCount)
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

    private fun updateSearchingState() {
        Log.d("BaseAdapter", "updateSearchingState: $isSearching")

        cleanList.clear()

        if (isSearching) {
            listForSave.clear()
            listForSave += cloneCurrentList()
        } else {
            setItems(listForSave, commitCallback = {
                listForSave.clear()
            })
        }
    }

    open fun filter(query: String) {
        if (cleanList.isEmpty()) {
            cleanList.addAll(listForSave)
        }

        val newList = mutableListOf<T>()

        setItems(emptyList(), commitCallback = {
            if (query.isEmpty()) {
                newList.addAll(cleanList)
            } else {
                for (item in cleanList) {
                    if (onQueryItem(item, query)) {
                        newList.add(item)
                    }
                }
            }

            setItems(newList)
        })
    }

    open fun onQueryItem(item: T, query: String): Boolean {
        return false
    }

    override fun getFilter(): Filter {
        if (valuesFilter == null) {
            valuesFilter = ValuesFilter()
        }

        return requireNotNull(valuesFilter)
    }

    private inner class ValuesFilter : Filter() {
        override fun performFiltering(constraint: CharSequence?): FilterResults {
            val results = FilterResults()

            if (isEmpty()) return results

            if (!constraint.isNullOrEmpty()) {
                val filteredList = mutableListOf<T>()
                for (item in listForSave) {
                    if (onQueryItem(item, constraint.toString())) {
                        filteredList.add(item)
                    }
                }
                results.count = filteredList.size
                results.values = filteredList
            } else {
                results.count = listForSave.size
                results.values = listForSave
            }

            return results
        }

        override fun publishResults(constraint: CharSequence?, results: FilterResults) {
            val items = results.values as? List<T>
            setItems(items)
        }
    }

    override fun onCurrentListChanged(previousList: MutableList<T>, currentList: MutableList<T>) {
        super.onCurrentListChanged(previousList, currentList)
    }
}