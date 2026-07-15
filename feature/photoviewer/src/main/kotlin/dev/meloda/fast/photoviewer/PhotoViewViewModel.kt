package dev.meloda.fast.photoviewer

import android.content.Context
import android.graphics.Bitmap
import androidx.core.graphics.drawable.toBitmapOrNull
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import coil.ImageLoader
import coil.request.ImageRequest
import coil.size.Size
import dev.meloda.fast.common.ImmutableList.Companion.toImmutableList
import dev.meloda.fast.common.extensions.setValue
import dev.meloda.fast.common.model.UiImage
import dev.meloda.fast.common.util.sha256
import dev.meloda.fast.logger.FastLogger
import dev.meloda.fast.photoviewer.model.PhotoViewArguments
import dev.meloda.fast.photoviewer.model.PhotoViewEffect
import dev.meloda.fast.photoviewer.model.PhotoViewIntent
import dev.meloda.fast.photoviewer.model.PhotoViewNavigationIntent
import dev.meloda.fast.photoviewer.model.PhotoViewScreenState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.net.URLDecoder
import kotlin.coroutines.cancellation.CancellationException

class PhotoViewViewModel(
    private val imageLoader: ImageLoader,
    private val applicationContext: Context,
    private val logger: FastLogger,
    private val platformManager: PlatformManager
) : ViewModel() {

    private val screenState = MutableStateFlow(PhotoViewScreenState.EMPTY)
    val screenStateFlow: StateFlow<PhotoViewScreenState> get() = screenState.asStateFlow()

    private val screenEffect = MutableSharedFlow<PhotoViewEffect>(extraBufferCapacity = 1)
    val screenEffectFlow: SharedFlow<PhotoViewEffect> get() = screenEffect.asSharedFlow()

    private var cacheDir: File? = null

    fun setArguments(arguments: PhotoViewArguments) {
        screenState.setValue {
            PhotoViewScreenState(
                images = arguments.imageUrls
                    .map { URLDecoder.decode(it, "utf-8") }
                    .map(UiImage::Url)
                    .toImmutableList(),
                selectedPage = arguments.selectedIndex?.takeIf { it != -1 } ?: 0,
                isLoading = false
            )
        }

        cacheDir = File(arguments.cacheDirPath)
    }

    fun handleIntent(intent: PhotoViewIntent) {
        when (intent) {
            PhotoViewIntent.Back -> screenEffect.tryEmit(
                PhotoViewEffect.Navigate(PhotoViewNavigationIntent.Back)
            )

            is PhotoViewIntent.PageChange -> onPageChanged(intent.newPage)

            PhotoViewIntent.CopyClick -> copyImage()
            PhotoViewIntent.CopyLinkClick -> copyImageLink()
            PhotoViewIntent.OpenInClick -> openImageIn()
            PhotoViewIntent.ShareClick -> shareImage()
        }
    }

    private fun onPageChanged(newPage: Int) {
        screenState.setValue { old -> old.copy(selectedPage = newPage) }
    }

    private fun shareImage() {
        if (screenState.value.isLoading) return

        val url = screenState.value.images
            .getOrNull(screenState.value.selectedPage)
            ?.extractUrl() ?: return

        viewModelScope.launch(Dispatchers.IO) {
            val imageFile = downloadAndStoreImageToCache(url) ?: return@launch
            platformManager.shareFile(imageFile.absolutePath)
        }
    }

    private fun openImageIn() {
        val url = screenState.value.images
            .getOrNull(screenState.value.selectedPage)
            ?.extractUrl() ?: return

        platformManager.openBrowser(url)
    }

    private fun copyImageLink() {
        val url = screenState.value.images
            .getOrNull(screenState.value.selectedPage)
            ?.extractUrl() ?: return

        platformManager.copyString(url, "URL")
    }

    private fun copyImage() {
        if (screenState.value.isLoading) return

        val url = screenState.value.images
            .getOrNull(screenState.value.selectedPage)
            ?.extractUrl() ?: return

        viewModelScope.launch(Dispatchers.IO) {
            val imageFile = downloadAndStoreImageToCache(url) ?: return@launch
            platformManager.copyFile(imageFile.absolutePath)
        }
    }

    private suspend fun downloadAndStoreImageToCache(url: String): File? {
        if (cacheDir == null) return null

        val imagesDir = File(cacheDir, "images")
        val imageFile = File(imagesDir, "${url.sha256()}.png")

        if (imageFile.isFile && imageFile.length() > 0L) {
            return imageFile
        }

        screenState.setValue { it.copy(isLoading = true) }

        return try {
            withContext(Dispatchers.IO) {
                check(imagesDir.exists() || imagesDir.mkdirs()) {
                    "Could not create cache directory: $imagesDir"
                }

                val temporaryFile = File(imagesDir, "${imageFile.name}.tmp")

                val result = imageLoader.execute(
                    ImageRequest.Builder(applicationContext)
                        .data(url)
                        .size(Size.ORIGINAL)
                        .build()
                )

                val bitmap = result.drawable?.toBitmapOrNull()
                    ?: return@withContext null

                val writtenSuccessfully = FileOutputStream(temporaryFile).use { output ->
                    bitmap.compress(
                        Bitmap.CompressFormat.PNG,
                        100,
                        output
                    )
                }

                if (!writtenSuccessfully) {
                    temporaryFile.delete()
                    return@withContext null
                }

                if (!temporaryFile.renameTo(imageFile)) {
                    temporaryFile.delete()
                    error("Could not rename temporary image to $imageFile")
                }

                imageFile
            }
        } catch (exception: CancellationException) {
            throw exception
        } catch (exception: Exception) {
            logger.error(this::class, "Failed to cache image: $url", exception)
            null
        } finally {
            screenState.setValue { it.copy(isLoading = false) }
        }
    }
}
