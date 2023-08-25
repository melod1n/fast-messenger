package com.meloda.fast.screens.settings.model

fun interface OnSettingsClickListener {
    fun onClick(key: String)
}

fun interface OnSettingsLongClickListener {
    fun onLongClick(key: String)
}

fun interface OnSettingsChangeListener {
    fun onChange(key: String, newValue: Any?)
}
