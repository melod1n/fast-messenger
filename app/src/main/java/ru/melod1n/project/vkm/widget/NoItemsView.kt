package ru.melod1n.project.vkm.widget

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
import androidx.core.view.isVisible
import ru.melod1n.project.vkm.R
import ru.melod1n.project.vkm.base.BaseAdapter
import ru.melod1n.project.vkm.extensions.ContextExtensions.drawable
import ru.melod1n.project.vkm.extensions.DrawableExtensions.tint
import ru.melod1n.project.vkm.extensions.FloatExtensions.int
import ru.melod1n.project.vkm.util.AndroidUtils

@Suppress("UNCHECKED_CAST")
class NoItemsView @JvmOverloads constructor(
    context: Context, private var attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : LinearLayout(context, attrs, defStyleAttr) {

    private lateinit var noItemsPicture: ImageView
    private lateinit var noItemsTextView: TextView

    private var recyclerView: RecyclerView? = null

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

        minimumWidth = AndroidUtils.px(256).int()
        minimumHeight = minimumWidth

        orientation = VERTICAL
        gravity = Gravity.CENTER

        noItemsPicture = ImageView(context)

        val params = imageViewParams
        params.height = AndroidUtils.px(64).int()
        params.width = AndroidUtils.px(64).int()

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
        textParams.width = AndroidUtils.px(256).int()

        if (noItemsDrawable != null) {
            textParams.topMargin = AndroidUtils.px(8).int()
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
        setNoItemsImage(context.drawable(resId))
    }

    fun setNoItemsImage(drawable: Drawable?) {
        noItemsPicture.setImageDrawable(drawable)
    }

    fun setNoItemsImageTint(@ColorInt color: Int) {
        noItemsPicture.drawable.tint(color)
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

    fun syncWith(recyclerView: RecyclerView) {
        this.recyclerView = recyclerView

        recyclerView.noItemsView = this
    }

    fun <ITEM, VH, ADAPTER : BaseAdapter<ITEM, VH>> updateState(adapter: ADAPTER) {
        if (adapter.isEmpty()) show() else hide()
    }

    fun show() {
        isVisible = true
    }

    fun hide() {
        isVisible = false
    }

}