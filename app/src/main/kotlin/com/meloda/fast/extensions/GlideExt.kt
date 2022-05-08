package com.meloda.fast.extensions

import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import android.net.Uri
import android.widget.ImageView
import com.bumptech.glide.Glide
import com.bumptech.glide.Priority
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.Transformation
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.load.resource.bitmap.*
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions.withCrossFade
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.RequestOptions
import com.bumptech.glide.request.target.Target

object ImageLoader {

    val userAvatarTransformations = listOf(
        TypeTransformations.CircleCrop
    )

    fun ImageView.clear() {
        this.setImageDrawable(null)
    }

    fun ImageView.loadWithGlide(
        url: String? = null,
        uri: Uri? = null,
        drawableRes: Int? = null,
        drawable: Drawable? = null,
        placeholderDrawable: Drawable = ColorDrawable(Color.TRANSPARENT),
        errorDrawable: Drawable = placeholderDrawable,
        crossFade: Boolean = false,
        crossFadeDuration: Int? = null,
        asCircle: Boolean = false,
        transformations: List<TypeTransformations> = emptyList(),
        onLoadedAction: (() -> Unit)? = null,
        onFailedAction: (() -> Unit)? = null,
        priority: Priority = Priority.NORMAL,
        cacheStrategy: DiskCacheStrategy = DiskCacheStrategy.ALL
    ) {
        val request = Glide.with(this)

        var builder = when {
            url != null -> request.load(url)
            uri != null -> request.load(uri)
            drawableRes != null -> request.load(drawableRes)
            drawable != null -> request.load(drawable)
            else -> request.load(null as Drawable?)
        }

        builder = builder
            .apply(TypeTransformations.createRequestOptions(transformations))
            .error(errorDrawable)
            .placeholder(placeholderDrawable)
            .addListener(ImageLoadRequestListener(onLoadedAction, onFailedAction))
            .diskCacheStrategy(cacheStrategy)
            .priority(priority)

        if (crossFade || crossFadeDuration != null) {
            builder = builder.transition(withCrossFade(crossFadeDuration ?: 200))
        }

        if (asCircle) {
            builder = builder.apply(
                TypeTransformations.createRequestOptions(
                    listOf(TypeTransformations.CircleCrop)
                )
            )
        }

        builder.into(this)
    }
}

class ImageLoadRequestListener(
    private val onLoadedAction: (() -> Unit)?,
    private val onFailedAction: (() -> Unit)?
) : RequestListener<Drawable> {

    override fun onLoadFailed(
        e: GlideException?,
        model: Any?,
        target: Target<Drawable>?,
        isFirstResource: Boolean
    ): Boolean {
        onFailedAction?.invoke()
        return false
    }

    override fun onResourceReady(
        resource: Drawable?,
        model: Any?,
        target: Target<Drawable>?,
        dataSource: DataSource?,
        isFirstResource: Boolean
    ): Boolean {
        onLoadedAction?.invoke()
        return false
    }
}

sealed class TypeTransformations {

    object CenterCrop : TypeTransformations()

    object CenterInside : TypeTransformations()

    object CircleCrop : TypeTransformations()

    class RoundedCornerCrop(val radius: Int) : TypeTransformations()

    class GranularRoundedCornerCrop(
        val topLeft: Float,
        val topRight: Float,
        val bottomRight: Float,
        val bottomLeft: Float
    ) : TypeTransformations()

    fun toGlideTransform(): Transformation<Bitmap> = when (this) {
        CenterCrop -> CenterCrop()
        CenterInside -> CenterInside()
        is RoundedCornerCrop -> RoundedCorners(radius)
        is GranularRoundedCornerCrop -> GranularRoundedCorners(
            topLeft,
            topRight,
            bottomRight,
            bottomLeft
        )
        CircleCrop -> CircleCrop()
    }

    companion object {

        fun createRequestOptions(transformations: List<TypeTransformations>): RequestOptions {
            val mappedTransformations = transformations
                .map(TypeTransformations::toGlideTransform)
                .toTypedArray()

            return RequestOptions().transform(* mappedTransformations)
        }
    }
}