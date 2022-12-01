package com.meloda.fast.screens.settings

import android.content.Context
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.meloda.fast.R
import com.meloda.fast.base.adapter.BaseAdapter
import com.meloda.fast.base.adapter.BaseHolder
import com.meloda.fast.databinding.ItemSettingsCheckboxBinding
import com.meloda.fast.databinding.ItemSettingsEditTextAlertBinding
import com.meloda.fast.databinding.ItemSettingsEditTextBinding
import com.meloda.fast.databinding.ItemSettingsSwitchBinding
import com.meloda.fast.databinding.ItemSettingsTitleBinding
import com.meloda.fast.databinding.ItemSettingsTitleSummaryBinding
import com.meloda.fast.ext.showKeyboard
import com.meloda.fast.ext.toggleVisibilityIfHasContent
import com.meloda.fast.model.settings.SettingsItem
import java.util.Objects

class SettingsAdapter(
    context: Context,
    preAddedValues: List<SettingsItem<*>>
) : BaseAdapter<SettingsItem<*>, SettingsAdapter.Holder>(
    context, comparator, preAddedValues
) {

    var onClickAction: ((key: String) -> Unit)? = null
    var onChangeAction: ((key: String, newValue: Any?) -> Unit)? = null

    override fun getItemViewType(position: Int): Int {
        return when (getItem(position)) {
            is SettingsItem.Title -> SettingsItem.Title.ItemType
            is SettingsItem.TitleSummary -> SettingsItem.TitleSummary.ItemType
            is SettingsItem.EditText -> SettingsItem.EditText.ItemType
            is SettingsItem.CheckBox -> SettingsItem.CheckBox.ItemType
            is SettingsItem.Switch -> SettingsItem.Switch.ItemType
            else -> super.getItemViewType(position)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Holder {
        return when (viewType) {
            SettingsItem.Title.ItemType -> {
                TitleHolder(ItemSettingsTitleBinding.inflate(inflater, parent, false))
            }
            SettingsItem.TitleSummary.ItemType -> {
                TitleSummaryHolder(ItemSettingsTitleSummaryBinding.inflate(inflater, parent, false))
            }
            SettingsItem.EditText.ItemType -> {
                EditTextHolder(ItemSettingsEditTextBinding.inflate(inflater, parent, false))
            }
            SettingsItem.CheckBox.ItemType -> {
                CheckBoxHolder(ItemSettingsCheckboxBinding.inflate(inflater, parent, false))
            }
            SettingsItem.Switch.ItemType -> {
                SwitchHolder(ItemSettingsSwitchBinding.inflate(inflater, parent, false))
            }
            else -> Holder(View(context))
        }
    }

    override fun onBindViewHolder(holder: Holder, position: Int) {
        holder.bind(position)
    }

    @Suppress("UNCHECKED_CAST")
    open inner class Holder(v: View) : BaseHolder(v) {
        protected fun <T> getItemByType(position: Int): T {
            return getItem(position) as T
        }
    }

    inner class TitleHolder(private val binding: ItemSettingsTitleBinding) : Holder(binding.root) {

        override fun bind(position: Int) {
            val item: SettingsItem.Title = getItemByType(position)

            binding.title.text = item.title
        }
    }

    inner class TitleSummaryHolder(
        private val binding: ItemSettingsTitleSummaryBinding
    ) : Holder(binding.root) {

        override fun bind(position: Int) {
            val item: SettingsItem.TitleSummary = getItemByType(position)

            binding.root.setOnClickListener { onClickAction?.invoke(item.key) }

            binding.title.text = item.title
            binding.title.toggleVisibilityIfHasContent()
            binding.summary.text = item.summary
            binding.summary.toggleVisibilityIfHasContent()
        }
    }

    inner class EditTextHolder(
        private val binding: ItemSettingsEditTextBinding
    ) : Holder(binding.root) {

        override fun bind(position: Int) {
            val item: SettingsItem.EditText = getItemByType(position)

            binding.title.text = item.title
            binding.title.toggleVisibilityIfHasContent()
            binding.summary.text = item.summary
            binding.summary.toggleVisibilityIfHasContent()

            binding.root.setOnClickListener {
                showAlert(item)
            }
        }

        private fun showAlert(item: SettingsItem.EditText) {
            val binding = ItemSettingsEditTextAlertBinding.inflate(inflater, null, false)

            binding.editText.setText(item.value)

            MaterialAlertDialogBuilder(context)
                .setView(binding.root)
                .setTitle(item.title)
                .setPositiveButton(R.string.ok) { _, _ ->
                    val newValue = binding.editText.text.toString()
                    item.value = newValue

                    onChangeAction?.invoke(item.key, newValue)
                }
                .setNegativeButton(R.string.cancel, null)
                .show()
                .setOnShowListener {
                    binding.editText.showKeyboard()
                }
        }
    }

    inner class CheckBoxHolder(
        private val binding: ItemSettingsCheckboxBinding
    ) : Holder(binding.root) {

        init {
            binding.root.setOnClickListener { binding.viewCheckBox.toggle() }
        }

        override fun bind(position: Int) {
            val item: SettingsItem.CheckBox = getItemByType(position)

            binding.title.text = item.title
            binding.title.toggleVisibilityIfHasContent()
            binding.summary.text = item.summary
            binding.summary.toggleVisibilityIfHasContent()

            binding.viewCheckBox.isChecked = item.requireValue()
            binding.viewCheckBox.setOnCheckedChangeListener { _, isChecked ->
                item.value = isChecked
                onChangeAction?.invoke(item.key, isChecked)
            }
        }
    }

    inner class SwitchHolder(
        private val binding: ItemSettingsSwitchBinding
    ) : Holder(binding.root) {

        init {
            binding.root.setOnClickListener { binding.viewSwitch.toggle() }
        }

        override fun bind(position: Int) {
            val item: SettingsItem.Switch = getItemByType(position)

            binding.title.text = item.title
            binding.title.toggleVisibilityIfHasContent()
            binding.summary.text = item.summary
            binding.summary.toggleVisibilityIfHasContent()

            binding.viewSwitch.isChecked = item.requireValue()
            binding.viewSwitch.setOnCheckedChangeListener { _, isChecked ->
                item.value = isChecked
                onChangeAction?.invoke(item.key, isChecked)
            }
        }
    }

    fun searchIndex(key: String): Int? {
        for (i in indices) {
            val item = getItem(i)
            if (item.key == key) return i
        }

        return null
    }

    companion object {

        val comparator = object : DiffUtil.ItemCallback<SettingsItem<*>>() {
            override fun areItemsTheSame(
                oldItem: SettingsItem<*>,
                newItem: SettingsItem<*>
            ): Boolean {
                return oldItem.key == newItem.key
            }

            override fun areContentsTheSame(
                oldItem: SettingsItem<*>,
                newItem: SettingsItem<*>
            ): Boolean {
                return Objects.deepEquals(oldItem, newItem)
            }
        }
    }

}