package com.meloda.fast.screens.photos

import android.widget.ImageView
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.meloda.fast.ext.ImageLoader.loadWithGlide
import kotlinx.coroutines.launch

class PhotoViewViewModel : ViewModel() {

    fun loadImageFromUrl(
        url: String,
        imageView: ImageView,
    ) = viewModelScope.launch {
        imageView.loadWithGlide { imageUrl = url }
    }
}
