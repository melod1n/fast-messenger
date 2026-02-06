package dev.meloda.fast.photoviewer

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.widget.Toast
import androidx.core.content.FileProvider
import androidx.core.graphics.drawable.toBitmapOrNull
import androidx.core.net.toUri
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import coil.imageLoader
import coil.request.ImageRequest
import dev.meloda.fast.common.extensions.setValue
import dev.meloda.fast.common.model.UiImage
import dev.meloda.fast.common.util.sha256
import dev.meloda.fast.photoviewer.model.PhotoViewArguments
import dev.meloda.fast.photoviewer.model.PhotoViewScreenState
import dev.meloda.fast.photoviewer.navigation.PhotoView
import dev.meloda.fast.ui.R
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.net.URLDecoder

interface PhotoViewViewModel {
    val screenState: StateFlow<PhotoViewScreenState>

    val shareRequest: StateFlow<Intent?>

    fun onPageChanged(newPage: Int)

    fun onShareClicked()
    fun onOpenInClicked()
    fun onCopyLinkClicked()
    fun onCopyClicked()

    fun onImageShared()
}

class PhotoViewViewModelImpl(
    arguments: PhotoViewArguments,
    private val applicationContext: Context
) : PhotoViewViewModel, ViewModel() {

    constructor(
        savedStateHandle: SavedStateHandle,
        applicationContext: Context
    ) : this(
        arguments = PhotoView.from(savedStateHandle).arguments,
        applicationContext = applicationContext
    )

    override val screenState = MutableStateFlow(PhotoViewScreenState.EMPTY)
    override val shareRequest = MutableStateFlow<Intent?>(null)

    init {
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

    override fun onShareClicked() {
        if (screenState.value.isLoading) return

        val url = screenState.value.images
            .getOrNull(screenState.value.selectedPage)
            ?.extractUrl() ?: return

        viewModelScope.launch(Dispatchers.IO) {
            val imageFile = downloadAndStoreImageToCache(url) ?: return@launch

            val uri = FileProvider.getUriForFile(
                applicationContext,
                "${applicationContext.packageName}.provider",
                imageFile
            )

            shareRequest.setValue {
                val intent = Intent(Intent.ACTION_SEND).apply {
                    type = "image/png"
                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                    putExtra(Intent.EXTRA_STREAM, uri)
                    clipData = ClipData.newRawUri(null, uri)
                }

                val chooserIntent = Intent.createChooser(intent, "Share image via...")
                chooserIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                chooserIntent
            }
        }
    }

    override fun onOpenInClicked() {
        val url = screenState.value.images
            .getOrNull(screenState.value.selectedPage)
            ?.extractUrl() ?: return

        val intent = Intent(Intent.ACTION_VIEW, url.toUri())
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)

        try {
            applicationContext.startActivity(intent)
        } catch (e: Exception) {
            e.printStackTrace()

            viewModelScope.launch(Dispatchers.Main) {
                Toast.makeText(
                    applicationContext,
                    R.string.error_occurred,
                    Toast.LENGTH_LONG
                ).show()
            }
        }
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
        if (screenState.value.isLoading) return

        val clipboardManager =
            applicationContext.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager

        val url = screenState.value.images
            .getOrNull(screenState.value.selectedPage)
            ?.extractUrl() ?: return

        viewModelScope.launch(Dispatchers.IO) {
            val imageFile = downloadAndStoreImageToCache(url) ?: return@launch

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

    override fun onImageShared() {
        shareRequest.setValue { null }
    }

    private suspend fun downloadAndStoreImageToCache(url: String): File? =
        runCatching {
            val imagesDir = File(applicationContext.cacheDir, "images")
            if (!imagesDir.exists()) {
                imagesDir.mkdirs()
            }
            val imageFile = File(imagesDir, "${url.sha256()}.png")
            if (imageFile.exists()) return imageFile

            withContext(Dispatchers.IO) {
                screenState.setValue { old -> old.copy(isLoading = true) }

                val drawable = applicationContext.imageLoader.execute(
                    ImageRequest.Builder(applicationContext)
                        .data(url)
                        .build()
                ).drawable ?: run {
                    screenState.setValue { old -> old.copy(isLoading = false) }
                    return@withContext null
                }

                FileOutputStream(imageFile).use {
                    drawable.toBitmapOrNull()?.compress(Bitmap.CompressFormat.PNG, 100, it)
                }

                imageFile
            }
        }.fold(
            onSuccess = { file ->
                screenState.setValue { old -> old.copy(isLoading = false) }
                file
            },
            onFailure = { e ->
                e.printStackTrace()
                screenState.setValue { old -> old.copy(isLoading = false) }
                null
            }
        )
}
