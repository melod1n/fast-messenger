package com.meloda.fast.model

sealed class DataItem<IdType> {
    abstract val dataItemId: IdType

    object Header : DataItem<Int>() {
        override val dataItemId = Int.MIN_VALUE
    }

    object Footer : DataItem<Int>() {
        override val dataItemId = Int.MIN_VALUE + 1
    }
}