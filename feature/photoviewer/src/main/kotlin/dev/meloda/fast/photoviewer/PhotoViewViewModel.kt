package dev.meloda.fast.photoviewer

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.graphics.Bitmap
import android.widget.Toast
import androidx.core.content.FileProvider
import androidx.core.graphics.drawable.toBitmapOrNull
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import coil.imageLoader
import coil.request.ImageRequest
import dev.meloda.fast.common.extensions.setValue
import dev.meloda.fast.common.model.UiImage
import dev.meloda.fast.photoviewer.model.PhotoViewScreenState
import dev.meloda.fast.photoviewer.navigation.PhotoView
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.net.URLDecoder
import java.util.UUID

interface PhotoViewViewModel {
    val screenState: StateFlow<PhotoViewScreenState>

    fun onPageChanged(newPage: Int)

    fun onCopyLinkClicked()
    fun onCopyClicked()
}

class PhotoViewViewModelImpl(
    savedStateHandle: SavedStateHandle,
    private val applicationContext: Context
) : PhotoViewViewModel, ViewModel() {

    override val screenState = MutableStateFlow(PhotoViewScreenState.EMPTY)

    init {
        val arguments = PhotoView.from(savedStateHandle).arguments

        screenState.setValue { old ->
            old.copy(
                images = arguments.imageUrls
                    .map { URLDecoder.decode(it, "utf-8") }
                    .map(UiImage::Url),
                selectedPage = arguments.selectedIndex?.takeIf { it != -1 } ?: 0
            )
        }
    }

    override fun onPageChanged(newPage: Int) {
        screenState.setValue { old -> old.copy(selectedPage = newPage) }
    }

    override fun onCopyLinkClicked() {
        val url = screenState.value.images
            .getOrNull(screenState.value.selectedPage)
            ?.extractUrl() ?: return

        val clipboardManager =
            applicationContext.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager

        clipboardManager.setPrimaryClip(ClipData.newPlainText("URL", url))

        Toast.makeText(
            applicationContext,
            "URL copied to clipboard",
            Toast.LENGTH_SHORT
        ).show()
    }

    override fun onCopyClicked() {
        val clipboardManager =
            applicationContext.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager

        val url = screenState.value.images
            .getOrNull(screenState.value.selectedPage)
            ?.extractUrl() ?: return

        viewModelScope.launch(Dispatchers.IO) {
            val drawable = applicationContext.imageLoader.execute(
                ImageRequest.Builder(applicationContext)
                    .data(url)
                    .build()
            ).drawable ?: return@launch

            val imagesDir = File(applicationContext.cacheDir, "images")
            if (!imagesDir.exists()) imagesDir.mkdirs()
            val imageFile = File(imagesDir, "shared_image_id${UUID.randomUUID()}.png")
            FileOutputStream(imageFile).use {
                drawable.toBitmapOrNull()?.compress(Bitmap.CompressFormat.PNG, 100, it)
            }

            val uri = FileProvider.getUriForFile(
                applicationContext,
                "${applicationContext.packageName}.provider",
                imageFile
            )

            val clip = ClipData.newUri(applicationContext.contentResolver, "Image", uri)
            clipboardManager.setPrimaryClip(clip)

            withContext(Dispatchers.Main) {
                Toast.makeText(
                    applicationContext,
                    "Image copied to clipboard",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }
}
