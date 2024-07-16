package dev.meloda.fast.photoviewer.navigation

import androidx.lifecycle.SavedStateHandle
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import androidx.navigation.toRoute
import dev.meloda.fast.common.extensions.customNavType
import dev.meloda.fast.photoviewer.model.PhotoViewArguments
import dev.meloda.fast.photoviewer.presentation.PhotoViewRoute
import kotlinx.serialization.Serializable
import java.net.URLEncoder
import kotlin.reflect.typeOf

@Serializable
data class PhotoView(val arguments: PhotoViewArguments) {
    companion object {
        val typeMap = mapOf(typeOf<PhotoViewArguments>() to customNavType<PhotoViewArguments>())

        fun from(savedStateHandle: SavedStateHandle) =
            savedStateHandle.toRoute<PhotoView>(typeMap)
    }
}

fun NavGraphBuilder.photoViewScreen(
    onBack: () -> Unit
) {
    composable<PhotoView>(typeMap = PhotoView.typeMap) {
        PhotoViewRoute(onBack = onBack)
    }
}

fun NavController.navigateToPhotoView(images: List<String>) {
    this.navigate(
        PhotoView(
            arguments = PhotoViewArguments(
                images.map { URLEncoder.encode(it, "utf-8") }
            )
        )
    )
}
