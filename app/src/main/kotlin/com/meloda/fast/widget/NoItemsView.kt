package com.meloda.fast.widget

import android.content.Context
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import android.view.Gravity
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.annotation.ColorInt
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.view.isVisible
import com.meloda.fast.R
import com.meloda.fast.extensions.dpToPx

@Suppress("UNCHECKED_CAST")
class NoItemsView @JvmOverloads constructor(
    context: Context, private var attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : LinearLayout(context, attrs, defStyleAttr) {

    private lateinit var noItemsPicture: ImageView
    private lateinit var noItemsTextView: TextView

    private val textViewParams
        get() = LayoutParams(
            LayoutParams.MATCH_PARENT,
            LayoutParams.WRAP_CONTENT
        )

    private val imageViewParams
        get() = LayoutParams(
            LayoutParams.WRAP_CONTENT,
            LayoutParams.WRAP_CONTENT
        )

    init {
        create()
    }

    private fun create() {
        val a = context.obtainStyledAttributes(attrs, R.styleable.NoItemsView)

        minimumWidth = 256.dpToPx()
        minimumHeight = minimumWidth

        orientation = VERTICAL
        gravity = Gravity.CENTER

        noItemsPicture = ImageView(context)

        val imageViewSize = 64.dpToPx()

        val params = imageViewParams.apply {
            height = imageViewSize
            width = imageViewSize
        }

        noItemsPicture.layoutParams = params

        val noItemsDrawable = a.getDrawable(R.styleable.NoItemsView_noItemsImage)
        noItemsDrawable?.let {
            val noItemsDrawableTintColor = a.getColor(R.styleable.NoItemsView_noItemsImageTint, -1)
            if (noItemsDrawableTintColor != -1) {
                it.setTint(noItemsDrawableTintColor)
            }

            setNoItemsImage(it)
        }

        addView(noItemsPicture)

        noItemsTextView = TextView(context)

        val textParams = textViewParams
        textParams.width = 256.dpToPx()

        if (noItemsDrawable != null) {
            textParams.topMargin = 8.dpToPx()
        }

        noItemsTextView.layoutParams = textParams

        noItemsTextView.gravity = Gravity.CENTER
        noItemsTextView.setTextAppearance(R.style.TextAppearance_MaterialComponents_Body1)

        val noItemsTextColor = a.getColor(R.styleable.NoItemsView_noItemsTextColor, -1)
        if (noItemsTextColor != -1) {
            setNoItemsTextColor(noItemsTextColor)
        }

        val noItemsText = a.getString(R.styleable.NoItemsView_noItemsText)
        noItemsText?.let {
            setNoItemsText(it)
        }

        addView(noItemsTextView)

        val isVisibleByDefault = a.getBoolean(R.styleable.NoItemsView_isVisibleByDefault, true)
        isVisible = isVisibleByDefault

        a.recycle()
    }

    fun setNoItemsImage(@DrawableRes resId: Int) {
        setNoItemsImage(AppCompatResources.getDrawable(context, resId))
    }

    fun setNoItemsImage(drawable: Drawable?) {
        noItemsPicture.setImageDrawable(drawable)
    }

    fun setNoItemsImageTint(@ColorInt color: Int) {
        noItemsPicture.drawable?.setTint(color)
    }

    fun setNoItemsText(@StringRes resId: Int) {
        noItemsTextView.setText(resId)
    }

    fun setNoItemsText(text: String) {
        noItemsTextView.text = text
    }

    fun setNoItemsTextColor(@ColorInt color: Int) {
        noItemsTextView.setTextColor(color)
    }

    fun show() {
        isVisible = true
    }

    fun hide() {
        isVisible = false
    }

}