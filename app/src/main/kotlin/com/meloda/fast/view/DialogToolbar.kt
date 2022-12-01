package com.meloda.fast.view

import android.content.Context
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.core.content.withStyledAttributes
import androidx.core.view.updatePaddingRelative
import com.meloda.fast.R
import com.meloda.fast.databinding.ViewDialogToolbarBinding
import com.meloda.fast.ext.dpToPx
import com.meloda.fast.ext.toggleVisibility
import com.meloda.fast.ext.toggleVisibilityIfHasContent
import com.meloda.fast.util.ColorUtils
import kotlin.properties.Delegates

class DialogToolbar @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : ConstraintLayout(context, attrs, defStyleAttr) {

    private val binding =
        ViewDialogToolbarBinding.inflate(LayoutInflater.from(context), this)


    var title: String? by Delegates.observable(null) { _, _, _ ->
        applyTitle(title)
    }

    var subtitle: String? by Delegates.observable(null) { _, _, _ ->
        applySubtitle(subtitle)
    }

    var avatarDrawable: Drawable? by Delegates.observable(null) { _, _, _ ->
        applyAvatarDrawable(avatarDrawable)
    }

    var avatarClickAction: ((avatar: View) -> Unit)? by Delegates.observable(null) { _, _, _ ->
        applyAvatarClickAction(avatarClickAction)
    }

    var startIconDrawable: Drawable? by Delegates.observable(null) { _, _, _ ->
        applyStartIconDrawable(startIconDrawable)
    }

    var startButtonClickAction: (() -> Unit)? = null

    private val defaultBackgroundColor = ContextCompat.getColor(
        context,
        R.color.colorBackground
    )

    init {
        isSaveEnabled = false

        val padding = 4.dpToPx()
        updatePaddingRelative(top = padding, bottom = padding)

        context.withStyledAttributes(attrs, R.styleable.DialogToolbar) {
            title = getText(R.styleable.DialogToolbar_title)?.toString()
            subtitle = getText(R.styleable.DialogToolbar_subtitle)?.toString()
            avatarDrawable = getDrawable(R.styleable.DialogToolbar_avatar)
            startIconDrawable = getDrawable(R.styleable.DialogToolbar_startIcon)

            val attrBackgroundColor =
                getColor(R.styleable.DialogToolbar_backgroundColor, defaultBackgroundColor)

            val useTranslucentBackgroundColor =
                getBoolean(R.styleable.DialogToolbar_useTranslucentBackgroundColor, false)

            val backgroundColor =
                if (useTranslucentBackgroundColor) ColorUtils.alphaColor(attrBackgroundColor, 0.9F)
                else attrBackgroundColor

            setBackgroundColor(backgroundColor)
        }

        binding.startIconContainer.setOnClickListener { startButtonClickAction?.invoke() }
    }

    private fun syncView() {
        applyTitle(title)
        applySubtitle(subtitle)
        applyAvatarDrawable(avatarDrawable)
        applyStartIconDrawable(startIconDrawable)
    }

    private fun applyTitle(title: String?) {
        binding.title.text = title
        binding.title.toggleVisibilityIfHasContent()
    }

    private fun applySubtitle(subtitle: String?) {
        binding.subtitle.text = subtitle
        binding.subtitle.toggleVisibilityIfHasContent()
    }

    private fun applyAvatarDrawable(drawable: Drawable?) {
        binding.avatar.setImageDrawable(drawable)
        binding.avatar.toggleVisibilityIfHasContent()
    }

    private fun applyAvatarClickAction(action: ((avatar: View) -> Unit)?) {
        binding.avatar.setOnClickListener(action)
    }

    private fun applyStartIconDrawable(drawable: Drawable?) {
        binding.startIcon.setImageDrawable(drawable)

        binding.startIconContainer.toggleVisibility(drawable != null)
    }

    val avatarImageView get() = binding.avatar

}