package com.meloda.fast.screens.settings

fun interface OnSettingsClickListener {
    fun onClick(key: String)
}

fun interface OnSettingsLongClickListener {
    fun onLongClick(key: String): Boolean
}

fun interface OnSettingsChangeListener {
    fun onChange(key: String, newValue: Any?)
}
