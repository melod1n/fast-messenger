package com.meloda.fast.extensions

import android.graphics.drawable.Drawable
import android.util.Log
import android.widget.ImageView
import com.meloda.fast.BuildConfig
import com.squareup.picasso.Callback
import com.squareup.picasso.Picasso

object ImageViewExtensions {

    fun ImageView.loadImage(
        sourceUrl: String,
        placeholder: Drawable? = null,
        callback: Callback? = null
    ) {
        if (sourceUrl.trim().isEmpty()) {
            if (BuildConfig.DEBUG) {
                Log.d("ImageView", "sourceUrl is empty")
            }
            return
        }

        val builder = Picasso.get().load(sourceUrl)

        placeholder?.let { builder.placeholder(it) }

        try {
            builder.into(this, object : Callback {
                override fun onSuccess() {
                    if (BuildConfig.DEBUG) {
                        Log.d("ImageView", "loaded photo from $sourceUrl")
                    }

                    callback?.onSuccess()
                }

                override fun onError(e: Exception?) {
                    if (BuildConfig.DEBUG) {
                        Log.d("ImageView", "error loading photo from $sourceUrl")
                    }

                    callback?.onError(e)
                }
            })
        } catch (e: Exception) {
            e.printStackTrace()

            if (BuildConfig.DEBUG) {
                Log.d("ImageView", "Error loading photo from $sourceUrl")
            }
        }
    }

}