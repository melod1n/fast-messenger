package ru.melod1n.project.vkm.widget

import android.app.Activity
import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Canvas
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import android.util.TypedValue
import android.view.Gravity
import android.view.View
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.annotation.ColorInt
import androidx.annotation.DrawableRes
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import com.facebook.drawee.view.SimpleDraweeView
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.theme.overlay.MaterialThemeOverlay
import ru.melod1n.project.vkm.R

class Toolbar : MaterialToolbar {

    constructor(context: Context) : this(context, null)

    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0)

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(
        MaterialThemeOverlay.wrap(
            context,
            attrs,
            defStyleAttr,
            com.google.android.material.R.style.Widget_MaterialComponents_Toolbar
        ),
        attrs,
        defStyleAttr
    )

    private fun init() {
        //...
    }

    override fun setTitle(resId: Int) {
        title = context.getString(resId)
    }

    override fun setTitle(title: CharSequence?) {
        findViewById<TextView>(R.id.toolbarTitle).text = title
    }

    override fun setTitleTextColor(color: Int) {
        findViewById<TextView>(R.id.toolbarTitle).setTextColor(color)
    }

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)
        init()
    }

    override fun setNavigationIcon(icon: Drawable?) {
        findViewById<ImageButton>(R.id.toolbarNavigationIcon).setImageDrawable(icon)
    }

    override fun setNavigationIcon(@DrawableRes resId: Int) {
        findViewById<ImageButton>(R.id.toolbarNavigationIcon).setImageResource(resId)
    }

//    fun setNavigationIconTintList(tintList: ColorStateList?) {
//        findViewById<ImageButton>(R.id.toolbarNavigationIcon).drawable?.setTintList(tintList)
//    }
//
//    fun setNavigationIconTint(@ColorInt tintColor: Int) {
//        findViewById<ImageButton>(R.id.toolbarNavigationIcon).drawable?.setTint(tintColor)
//    }

    fun setNavigationClickListener(listener: OnClickListener?) {
        findViewById<View>(R.id.toolbarNavigation).setOnClickListener(listener)
    }

    fun setNavigationOnBackClickListener(activity: Activity) {
        findViewById<View>(R.id.toolbarNavigation).setOnClickListener { activity.onBackPressed() }
    }

    fun setNavigationVisibility(visible: Boolean) {
        findViewById<View>(R.id.toolbarNavigation).visibility = if (visible) VISIBLE else GONE
    }

    fun setAvatarIcon(icon: Drawable?) {
        findViewById<ImageView>(R.id.toolbarAvatar).setImageDrawable(icon)
    }

    fun setAvatarIcon(@DrawableRes resId: Int) {
        findViewById<SimpleDraweeView>(R.id.toolbarAvatar).setActualImageResource(resId)
    }

    fun setAvatarClickListener(listener: OnClickListener?) {
        findViewById<View>(R.id.toolbarAvatar).setOnClickListener(listener)
    }

    fun setAvatarVisibility(visible: Boolean) {
        findViewById<View>(R.id.toolbarAvatar).visibility = if (visible) VISIBLE else GONE
    }

    fun getAvatar(): SimpleDraweeView {
        return findViewById(R.id.toolbarAvatar)
    }

    fun setTitleMode(titleMode: TitleMode) {
        val title = findViewById<TextView>(R.id.toolbarTitle)

        when (titleMode) {
            TitleMode.SIMPLE -> {
                title.gravity = Gravity.CENTER
                title.typeface = ResourcesCompat.getFont(context, R.font.google_sans_medium)
                title.setTextColor(ContextCompat.getColor(context, R.color.accent))
                title.setTextSize(TypedValue.COMPLEX_UNIT_SP, 22f)
            }
            TitleMode.HINT -> {
                title.gravity = Gravity.CENTER_VERTICAL and Gravity.START
                title.typeface = ResourcesCompat.getFont(context, R.font.google_sans_regular)
                title.setTextColor(ContextCompat.getColor(context, R.color.text_secondary_60_alpha))
                title.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16f)
            }
        }
    }

    enum class TitleMode {
        SIMPLE, HINT
    }
}