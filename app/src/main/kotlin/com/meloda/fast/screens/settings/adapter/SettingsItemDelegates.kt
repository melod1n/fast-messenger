package com.meloda.fast.screens.settings.adapter

import android.content.Context
import android.view.LayoutInflater
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.hannesdorfmann.adapterdelegates4.dsl.adapterDelegateViewBinding
import com.meloda.fast.R
import com.meloda.fast.databinding.ItemSettingsCheckboxBinding
import com.meloda.fast.databinding.ItemSettingsEditTextAlertBinding
import com.meloda.fast.databinding.ItemSettingsEditTextBinding
import com.meloda.fast.databinding.ItemSettingsListBinding
import com.meloda.fast.databinding.ItemSettingsSwitchBinding
import com.meloda.fast.databinding.ItemSettingsTitleBinding
import com.meloda.fast.databinding.ItemSettingsTitleSummaryBinding
import com.meloda.fast.ext.showKeyboard
import com.meloda.fast.ext.toggleVisibilityIfHasContent
import com.meloda.fast.model.base.AdapterDiffItem
import com.meloda.fast.model.settings.SettingsItem

fun settingsTitleItemDelegate() =
    adapterDelegateViewBinding<SettingsItem.Title, AdapterDiffItem, ItemSettingsTitleBinding>(
        viewBinding = { layoutInflater, parent ->
            ItemSettingsTitleBinding.inflate(layoutInflater, parent, false)
        }
    ) {
        bind {
            binding.title.text = item.title
        }
    }

fun settingsTitleSummaryItemDelegate(
    onClickListener: OnSettingsClickListener? = null,
    onLongClickListener: OnSettingsLongClickListener? = null,
) =
    adapterDelegateViewBinding<SettingsItem.TitleSummary, AdapterDiffItem, ItemSettingsTitleSummaryBinding>(
        viewBinding = { layoutInflater, parent ->
            ItemSettingsTitleSummaryBinding.inflate(layoutInflater, parent, false)
        }
    ) {
        binding.root.setOnClickListener { onClickListener?.onClick(item.key) }
        binding.root.setOnLongClickListener { onLongClickListener?.onLongClick(item.key) ?: false }

        bind {
            binding.title.text = item.title
            binding.title.toggleVisibilityIfHasContent()
            binding.summary.text = item.summary
            binding.summary.toggleVisibilityIfHasContent()
        }
    }

fun settingsEditTextItemDelegate(
    onClickListener: OnSettingsClickListener? = null,
    onLongClickListener: OnSettingsLongClickListener? = null,
    onChangeListener: OnSettingsChangeListener? = null,
) =
    adapterDelegateViewBinding<SettingsItem.EditText, AdapterDiffItem, ItemSettingsEditTextBinding>(
        viewBinding = { layoutInflater, parent ->
            ItemSettingsEditTextBinding.inflate(layoutInflater, parent, false)
        }
    ) {
        val onChangeAction: ((key: String, newValue: Any?) -> Unit) =
            { key: String, newValue: Any? ->
                onChangeListener?.onChange(key, newValue)

                binding.summary.text = item.summaryProvider?.provideSummary(item)
            }

        binding.root.setOnClickListener {
            onClickListener?.onClick(item.key)
            showAlert(context, item, onChangeAction)
        }
        binding.root.setOnLongClickListener { onLongClickListener?.onLongClick(item.key) ?: false }

        bind {
            binding.title.text = item.title
            binding.title.toggleVisibilityIfHasContent()
            binding.summary.text = item.summary
            binding.summary.toggleVisibilityIfHasContent()
        }
    }

private fun showAlert(
    context: Context,
    item: SettingsItem.EditText,
    onChangeListener: OnSettingsChangeListener? = null,
) {
    val binding =
        ItemSettingsEditTextAlertBinding.inflate(LayoutInflater.from(context), null, false)

    binding.editText.setText(item.value)

    MaterialAlertDialogBuilder(context)
        .setView(binding.root)
        .setTitle(item.title)
        .setPositiveButton(R.string.ok) { _, _ ->
            val newValue = binding.editText.text.toString()
            item.value = newValue

            onChangeListener?.onChange(item.key, newValue)
        }
        .setNegativeButton(R.string.cancel, null)
        .show()
        .setOnShowListener {
            binding.editText.showKeyboard()
        }
}

fun settingsCheckboxItemDelegate(
    onClickListener: OnSettingsClickListener? = null,
    onLongClickListener: OnSettingsLongClickListener? = null,
    onChangeListener: OnSettingsChangeListener? = null,
) =
    adapterDelegateViewBinding<SettingsItem.CheckBox, AdapterDiffItem, ItemSettingsCheckboxBinding>(
        viewBinding = { layoutInflater, parent ->
            ItemSettingsCheckboxBinding.inflate(layoutInflater, parent, false)
        }
    ) {
        binding.root.setOnClickListener {
            onClickListener?.onClick(item.key)
            binding.viewCheckBox.toggle()
        }
        binding.root.setOnLongClickListener { onLongClickListener?.onLongClick(item.key) ?: false }

        bind {
            binding.title.text = item.title
            binding.title.toggleVisibilityIfHasContent()
            binding.summary.text = item.summary
            binding.summary.toggleVisibilityIfHasContent()

            binding.viewCheckBox.isChecked = item.requireValue()
            binding.viewCheckBox.setOnCheckedChangeListener { _, isChecked ->
                item.value = isChecked
                onChangeListener?.onChange(item.key, isChecked)
            }
        }
    }

fun settingsSwitchItemDelegate(
    onClickListener: OnSettingsClickListener? = null,
    onLongClickListener: OnSettingsLongClickListener? = null,
    onChangeListener: OnSettingsChangeListener? = null,
) =
    adapterDelegateViewBinding<SettingsItem.Switch, AdapterDiffItem, ItemSettingsSwitchBinding>(
        viewBinding = { layoutInflater, parent ->
            ItemSettingsSwitchBinding.inflate(layoutInflater, parent, false)
        }
    ) {
        binding.root.setOnClickListener {
            onClickListener?.onClick(item.key)
            binding.viewSwitch.toggle()
        }
        binding.root.setOnLongClickListener { onLongClickListener?.onLongClick(item.key) ?: false }

        bind {
            binding.title.text = item.title
            binding.title.toggleVisibilityIfHasContent()
            binding.summary.text = item.summary
            binding.summary.toggleVisibilityIfHasContent()

            binding.viewSwitch.isChecked = item.requireValue()
            binding.viewSwitch.setOnCheckedChangeListener { _, isChecked ->
                item.value = isChecked
                onChangeListener?.onChange(item.key, isChecked)
            }
        }
    }

fun settingsListItemDelegate(
    onClickListener: OnSettingsClickListener? = null,
    onLongClickListener: OnSettingsLongClickListener? = null,
    onChangeListener: OnSettingsChangeListener? = null,
) =
    adapterDelegateViewBinding<SettingsItem.ListItem, AdapterDiffItem, ItemSettingsListBinding>(
        viewBinding = { layoutInflater, parent ->
            ItemSettingsListBinding.inflate(layoutInflater, parent, false)
        }
    ) {

        bind {

        }
    }

fun interface OnSettingsClickListener {
    fun onClick(key: String)
}

fun interface OnSettingsLongClickListener {
    fun onLongClick(key: String): Boolean
}

fun interface OnSettingsChangeListener {
    fun onChange(key: String, newValue: Any?)
}
