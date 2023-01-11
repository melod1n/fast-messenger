package com.meloda.fast.ext

import android.graphics.Rect
import android.graphics.drawable.Drawable
import android.os.Parcelable
import android.util.SparseArray
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import androidx.annotation.ColorInt
import androidx.annotation.Px
import androidx.appcompat.widget.Toolbar
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.children
import androidx.core.view.doOnAttach
import androidx.core.view.forEach
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import com.meloda.fast.R
import com.meloda.fast.common.AppGlobal
import com.meloda.fast.databinding.ToolbarMenuItemAvatarBinding
import com.meloda.fast.ext.ImageLoader.loadWithGlide

val EditText.trimmedText: String get() = text.toString().trim()
fun EditText.selectLast() {
    setSelection(text.length)
}

inline fun EditText.onDone(crossinline callback: () -> Unit) {
    imeOptions = EditorInfo.IME_ACTION_DONE
    maxLines = 1
    setOnEditorActionListener { _, actionId, _ ->
        if (actionId == EditorInfo.IME_ACTION_DONE) {
            callback.invoke()
            return@setOnEditorActionListener true
        }
        false
    }
}

fun View.showKeyboard(flags: Int = 0) {
    AppGlobal.inputMethodManager.showSoftInput(this, flags)
}

fun View.hideKeyboard(focusedView: View? = null, flags: Int = 0) {
    AppGlobal.inputMethodManager.hideSoftInputFromWindow(
        focusedView?.windowToken ?: this.windowToken, flags
    )
}

fun TextInputLayout.clearError() {
    if (error != null) error = null
}

fun TextInputLayout.clearTextOnErrorIconClick(textField: TextInputEditText) {
    setErrorIconOnClickListener {
        textField.text = null
        textField.showKeyboard()
    }
}

@JvmOverloads
fun ImageView.toggleVisibilityIfHasContent(visibilityWhenFalse: Int = View.GONE) {
    visibility = if (drawable != null) View.VISIBLE else visibilityWhenFalse
}

@JvmOverloads
fun TextView.toggleVisibilityIfHasContent(visibilityWhenFalse: Int = View.GONE) {
    visibility = if (!text.isNullOrEmpty()) View.VISIBLE else visibilityWhenFalse
}

fun View.setMarginsPx(
    @Px leftMargin: Int? = null,
    @Px topMargin: Int? = null,
    @Px rightMargin: Int? = null,
    @Px bottomMargin: Int? = null,
) {
    if (layoutParams is ViewGroup.MarginLayoutParams) {
        val params = layoutParams as ViewGroup.MarginLayoutParams
        leftMargin?.run { params.leftMargin = this }
        topMargin?.run { params.topMargin = this }
        rightMargin?.run { params.rightMargin = this }
        bottomMargin?.run { params.bottomMargin = this }
        requestLayout()
    }
}

fun TextView.clear() {
    text = null
}

fun ViewGroup.saveChildViewStates(): SparseArray<Parcelable> {
    val childViewStates = SparseArray<Parcelable>()
    children.forEach { child -> child.saveHierarchyState(childViewStates) }
    return childViewStates
}

fun ViewGroup.restoreChildViewStates(childViewStates: SparseArray<Parcelable>) {
    children.forEach { child -> child.restoreHierarchyState(childViewStates) }
}

fun View.invisible() = run { visibility = View.INVISIBLE }

fun View.visible() = run { visibility = View.VISIBLE }
fun View.gone() = run { visibility = View.GONE }

@JvmOverloads
fun View.toggleVisibility(visible: Boolean?, visibilityWhenFalse: Int = View.GONE) =
    run { visibility = if (visible == true) View.VISIBLE else visibilityWhenFalse }

fun Toolbar.tintMenuItemIcons(@ColorInt colorToTint: Int) {
    menu.forEach { item ->
        item.icon?.setTint(colorToTint)
    }
}

fun Toolbar.addAvatarMenuItem(urlToLoad: String? = null, drawable: Drawable? = null): MenuItem {
    val avatarMenuItemBinding = ToolbarMenuItemAvatarBinding.inflate(
        LayoutInflater.from(context), null, false
    )

    val avatarMenuItem = menu.add(context.getString(R.string.navigation_profile))
    avatarMenuItem.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS)
    avatarMenuItem.actionView = avatarMenuItemBinding.root

    val imageView = avatarMenuItemBinding.avatar

    when {
        urlToLoad != null -> {
            imageView.loadWithGlide {
                imageUrl = urlToLoad
                transformations = ImageLoader.userAvatarTransformations
            }
        }
        drawable != null -> {
            imageView.loadWithGlide {
                imageDrawable = drawable
                transformations = ImageLoader.userAvatarTransformations
            }
        }
    }

    return avatarMenuItem
}

fun View.doOnApplyWindowInsets(block: (view: View, insets: WindowInsetsCompat, padding: Rect) -> WindowInsetsCompat) {
    val initialPadding = recordInitialPaddingForView(this)

    ViewCompat.setOnApplyWindowInsetsListener(this) { v, insets ->
        block(v, insets, initialPadding)
    }

    requestApplyInsetsWhenAttached()
}

private fun recordInitialPaddingForView(view: View) =
    Rect(view.paddingLeft, view.paddingTop, view.paddingRight, view.paddingBottom)

fun View.requestApplyInsetsWhenAttached() {
    if (isAttachedToWindow) {
        requestApplyInsets()
    } else {
        doOnAttach { requestApplyInsets() }
    }
}
