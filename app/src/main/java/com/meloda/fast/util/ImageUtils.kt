package com.meloda.fast.util

import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.widget.ImageView
import com.facebook.drawee.view.SimpleDraweeView
import com.squareup.picasso.Picasso
import com.squareup.picasso.Target

object ImageUtils {

    fun loadImage(image: String, imageView: ImageView, placeholder: Drawable?) {
        if (image.isEmpty()) return

        if (imageView is SimpleDraweeView) {
            imageView.setImageURI(image)
            return
        }

        val picasso = Picasso.get()
            .load(image)
            .priority(Picasso.Priority.LOW)

        if (placeholder != null) picasso.placeholder(placeholder)

        picasso.into(imageView)
    }

    fun loadImage(image: String?, listener: OnLoadListener?) {
        if (image.isNullOrEmpty()) return

        val picasso = Picasso.get()
            .load(image)
            .priority(Picasso.Priority.LOW)

//        if (placeholder != null) picasso.placeholder(placeholder)


        val target = object : Target {
            override fun onPrepareLoad(placeHolderDrawable: Drawable?) {

            }

            override fun onBitmapFailed(e: Exception, errorDrawable: Drawable?) {
                listener?.onError(e)
            }

            override fun onBitmapLoaded(bitmap: Bitmap, from: Picasso.LoadedFrom) {
                listener?.onLoad(bitmap)
            }
        }

        picasso.into(target)
    }


    interface OnLoadListener {
        fun onLoad(bitmap: Bitmap)
        fun onError(e: Exception)
    }
}