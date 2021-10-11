package com.meloda.fast.screens.photos

import android.widget.ImageView
import androidx.lifecycle.viewModelScope
import coil.load
import com.meloda.fast.base.viewmodel.BaseViewModel
import kotlinx.coroutines.launch

class PhotoViewViewModel : BaseViewModel() {

    fun loadImageFromUrl(
        url: String,
        imageView: ImageView
    ) = viewModelScope.launch {
        imageView.load(url)
    }

    fun saveImageToLocalStorage(url: String) = viewModelScope.launch {
        TODO("Not implemented")
    }

}