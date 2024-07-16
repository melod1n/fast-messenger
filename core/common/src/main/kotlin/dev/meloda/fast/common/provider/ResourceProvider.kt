package dev.meloda.fast.common.provider

import android.content.res.Resources

interface ResourceProvider {

    val resources: Resources

    fun getString(resId: Int): String
}

class ResourceProviderImpl(override val resources: Resources) : ResourceProvider {

    override fun getString(resId: Int): String {
        return resources.getString(resId)
    }
}
