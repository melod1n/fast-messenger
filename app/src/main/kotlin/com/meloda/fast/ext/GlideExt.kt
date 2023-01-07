package com.meloda.fast.ext

import android.graphics.Bitmap
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
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.load.resource.bitmap.CenterInside
import com.bumptech.glide.load.resource.bitmap.CircleCrop
import com.bumptech.glide.load.resource.bitmap.GranularRoundedCorners
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
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

    fun ImageView.loadWithGlide(block: GlideParams.() -> Unit) {
        val params = GlideParams()
        block.invoke(params)
        loadWithGlide(params)
    }

    fun ImageView.loadWithGlide(params: GlideParams) {
        val request = Glide.with(this)

        var builder = when {
            params.imageUrl != null -> request.load(params.imageUrl)
            params.imageUri != null -> request.load(params.imageUri)
            params.drawableRes != null -> request.load(params.drawableRes)
            drawable != null -> request.load(drawable)
            else -> request.load(null as Drawable?)
        }

        val transforms = params.transformations.toMutableList()
        if (params.asCircle) {
            transforms += TypeTransformations.CircleCrop
        }

        builder = builder
            .apply(TypeTransformations.createRequestOptions(transforms))
            .error(
                params.errorDrawable
                    ?: if (params.errorColor != null) {
                        ColorDrawable(requireNotNull(params.errorColor))
                    } else null
            )
            .placeholder(
                params.placeholderDrawable
                    ?: if (params.placeholderColor != null) {
                        ColorDrawable(requireNotNull(params.placeholderColor))
                    } else null
            )
            .addListener(ImageLoadRequestListener(params.onLoadedAction, params.onFailedAction))
            .addListener(ImageLoadDoneListener(params.onDoneAction))
            .diskCacheStrategy(params.cacheStrategy)
            .priority(params.loadPriority)

        if (params.crossFade || params.crossFadeDuration != null) {
            builder = builder.transition(withCrossFade(params.crossFadeDuration ?: 200))
        }

        builder.into(this)
    }
}

data class GlideParams(
    var imageUrl: String? = null,
    var imageUri: Uri? = null,
    var drawableRes: Int? = null,
    var imageDrawable: Drawable? = null,
    var placeholderDrawable: Drawable? = null,
    var placeholderColor: Int? = null,
    var errorDrawable: Drawable? = placeholderDrawable,
    var errorColor: Int? = null,
    var crossFade: Boolean = false,
    var crossFadeDuration: Int? = null,
    var asCircle: Boolean = false,
    var transformations: List<TypeTransformations> = emptyList(),
    var onLoadedAction: (() -> Unit)? = null,
    var onFailedAction: (() -> Unit)? = null,
    var onDoneAction: (() -> Unit)? = null,
    var loadPriority: Priority = Priority.NORMAL,
    var cacheStrategy: DiskCacheStrategy = DiskCacheStrategy.ALL,
)

class ImageLoadRequestListener(
    private val onLoadedAction: (() -> Unit)?,
    private val onFailedAction: (() -> Unit)?,
) : RequestListener<Drawable> {

    override fun onLoadFailed(
        e: GlideException?,
        model: Any?,
        target: Target<Drawable>?,
        isFirstResource: Boolean,
    ): Boolean {
        onFailedAction?.invoke()
        return false
    }

    override fun onResourceReady(
        resource: Drawable?,
        model: Any?,
        target: Target<Drawable>?,
        dataSource: DataSource?,
        isFirstResource: Boolean,
    ): Boolean {
        onLoadedAction?.invoke()
        return false
    }
}

class ImageLoadDoneListener(private val onDoneAction: (() -> Unit)?) : RequestListener<Drawable> {
    override fun onLoadFailed(
        e: GlideException?,
        model: Any?,
        target: Target<Drawable>?,
        isFirstResource: Boolean,
    ): Boolean {
        onDoneAction?.invoke()
        return false
    }

    override fun onResourceReady(
        resource: Drawable?,
        model: Any?,
        target: Target<Drawable>?,
        dataSource: DataSource?,
        isFirstResource: Boolean,
    ): Boolean {
        onDoneAction?.invoke()
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
        val bottomLeft: Float,
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
