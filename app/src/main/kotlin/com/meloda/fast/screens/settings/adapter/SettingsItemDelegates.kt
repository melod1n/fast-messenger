package com.meloda.fast.screens.settings.adapter

import android.view.LayoutInflater
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.hannesdorfmann.adapterdelegates4.dsl.adapterDelegateViewBinding
import com.meloda.fast.R
import com.meloda.fast.databinding.*
import com.meloda.fast.ext.bulkIsEnabled
import com.meloda.fast.ext.findIndex
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
            binding.root.bulkIsEnabled(item.isEnabled)

            binding.title.text = item.title

            item.onTitleChanged = binding.title::setText
            item.onEnabledStateChanged = binding.root::bulkIsEnabled
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
            binding.root.bulkIsEnabled(item.isEnabled)

            binding.title.text = item.title
            binding.title.toggleVisibilityIfHasContent()
            binding.summary.text = item.summary
            binding.summary.toggleVisibilityIfHasContent()

            item.onTitleChanged = binding.title::setText
            item.onSummaryChanged = binding.summary::setText
            item.onEnabledStateChanged = binding.root::bulkIsEnabled
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
                item.updateSummary()
            }

        fun showAlert() {
            val binding =
                ItemSettingsEditTextAlertBinding.inflate(LayoutInflater.from(context), null, false)

            binding.editText.setText(item.value)

            MaterialAlertDialogBuilder(context)
                .setView(binding.root)
                .setTitle(item.title)
                .setPositiveButton(R.string.ok) { _, _ ->
                    val newValue = binding.editText.text.toString()
                    item.value = newValue

                    onChangeAction.invoke(item.key, newValue)
                }
                .setNegativeButton(R.string.cancel, null)
                .show()
                .setOnShowListener {
                    binding.editText.showKeyboard()
                }
        }

        binding.root.setOnClickListener {
            onClickListener?.onClick(item.key)
            showAlert()
        }
        binding.root.setOnLongClickListener { onLongClickListener?.onLongClick(item.key) ?: false }

        bind {
            binding.root.bulkIsEnabled(item.isEnabled)

            binding.title.text = item.title
            binding.title.toggleVisibilityIfHasContent()
            binding.summary.text = item.summary
            binding.summary.toggleVisibilityIfHasContent()

            item.onTitleChanged = binding.title::setText
            item.onSummaryChanged = binding.summary::setText
            item.onEnabledStateChanged = binding.root::bulkIsEnabled
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
            binding.root.bulkIsEnabled(item.isEnabled)

            binding.title.text = item.title
            binding.title.toggleVisibilityIfHasContent()
            binding.summary.text = item.summary
            binding.summary.toggleVisibilityIfHasContent()

            binding.viewCheckBox.isChecked = item.requireValue()
            binding.viewCheckBox.setOnCheckedChangeListener { _, isChecked ->
                item.value = isChecked
                onChangeListener?.onChange(item.key, isChecked)
            }

            item.onTitleChanged = binding.title::setText
            item.onSummaryChanged = binding.summary::setText
            item.onEnabledStateChanged = binding.root::bulkIsEnabled
        }
    }

fun settingsSwitchItemDelegate(
    onClickListener: OnSettingsClickListener? = null,
    onLongClickListener: OnSettingsLongClickListener? = null,
    onChangeListener: OnSettingsChangeListener? = null,
) = adapterDelegateViewBinding<SettingsItem.Switch, AdapterDiffItem, ItemSettingsSwitchBinding>(
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
        binding.root.bulkIsEnabled(item.isEnabled)

        binding.title.text = item.title
        binding.title.toggleVisibilityIfHasContent()
        binding.summary.text = item.summary
        binding.summary.toggleVisibilityIfHasContent()

        binding.viewSwitch.isChecked = item.requireValue()
        binding.viewSwitch.setOnCheckedChangeListener { _, isChecked ->
            if (item.value != isChecked) {
                item.value = isChecked
                onChangeListener?.onChange(item.key, isChecked)
            }
        }

        item.onTitleChanged = binding.title::setText
        item.onSummaryChanged = binding.summary::setText
        item.onEnabledStateChanged = binding.root::bulkIsEnabled
    }
}

fun settingsListItemDelegate(
    onClickListener: OnSettingsClickListener? = null,
    onLongClickListener: OnSettingsLongClickListener? = null,
    onChangeListener: OnSettingsChangeListener? = null,
) = adapterDelegateViewBinding<SettingsItem.ListItem, AdapterDiffItem, ItemSettingsListBinding>(
    viewBinding = { layoutInflater, parent ->
        ItemSettingsListBinding.inflate(layoutInflater, parent, false)
    }
) {
    val onChangeAction: ((key: String, newValue: Any?) -> Unit) =
        { key: String, newValue: Any? ->
            onChangeListener?.onChange(key, newValue)
            item.updateSummary()
        }

    fun showAlert() {
        var selectedOption = item.value
        val items = item.valueTitles.toTypedArray()
        val checkedItem = item.values.findIndex { it == (selectedOption ?: 0) } ?: 0

        MaterialAlertDialogBuilder(context)
            .setTitle(item.title)
            .setSingleChoiceItems(items, checkedItem) { _, which ->
                selectedOption = item.values[which]
            }
            .setPositiveButton(R.string.ok) { dialog, _ ->
                if (item.value != selectedOption) {
                    item.value = selectedOption
                    onChangeAction.invoke(item.key, selectedOption)
                }
                dialog.dismiss()
            }
            .show()
    }

    binding.root.setOnClickListener {
        onClickListener?.onClick(item.key)
        if (!item.overrideOnClickAction) {
            showAlert()
        }
    }

    binding.root.setOnLongClickListener { onLongClickListener?.onLongClick(item.key) ?: false }

    bind {
        binding.root.bulkIsEnabled(item.isEnabled)

        binding.title.text = item.title
        binding.title.toggleVisibilityIfHasContent()
        binding.summary.text = item.summary
        binding.summary.toggleVisibilityIfHasContent()

        item.onTitleChanged = binding.title::setText
        item.onSummaryChanged = binding.summary::setText
        item.onEnabledStateChanged = binding.root::bulkIsEnabled
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
