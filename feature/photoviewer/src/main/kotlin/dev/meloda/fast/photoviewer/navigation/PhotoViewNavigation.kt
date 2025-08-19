package dev.meloda.fast.photoviewer.navigation

import androidx.lifecycle.SavedStateHandle
import androidx.navigation.toRoute
import dev.meloda.fast.photoviewer.model.PhotoViewArguments
import dev.meloda.fast.ui.extensions.customNavType
import kotlinx.serialization.Serializable
import kotlin.reflect.typeOf

@Serializable
data class PhotoView(val arguments: PhotoViewArguments) {
    companion object {
        val typeMap = mapOf(typeOf<PhotoViewArguments>() to customNavType<PhotoViewArguments>())

        fun from(savedStateHandle: SavedStateHandle) =
            savedStateHandle.toRoute<PhotoView>(typeMap)
    }
}
