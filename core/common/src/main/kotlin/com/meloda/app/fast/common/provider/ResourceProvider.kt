package com.meloda.app.fast.common.provider

import android.content.res.Resources

interface ResourceProvider {

    fun getString(resId: Int): String
}

class ResourceProviderImpl(private val resources: Resources) : ResourceProvider {

    override fun getString(resId: Int): String {
        return resources.getString(resId)
    }
}
