package com.meloda.fast.ext

import android.content.Context
import android.graphics.Rect
import android.graphics.drawable.Drawable
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import androidx.annotation.ColorInt
import androidx.annotation.Px
import androidx.appcompat.widget.Toolbar
import androidx.core.view.*
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

@Deprecated("use InsetManager")
fun View.showKeyboard(flags: Int = 0) {
    (AppGlobal.Instance.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager)
        .showSoftInput(this, flags)
}

@Deprecated("use InsetManager")
fun View.hideKeyboard(focusedView: View? = null, flags: Int = 0) {
    (AppGlobal.Instance.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager)
        .hideSoftInputFromWindow(focusedView?.windowToken ?: this.windowToken, flags)
}

fun TextInputLayout.clearError() {
    if (error != null) error = null
}

fun TextInputLayout.toggleError(errorText: String, isNeedToShow: Boolean) {
    if (isNeedToShow) {
        this.error = errorText
    } else {
        clearError()
    }
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
    (layoutParams as? ViewGroup.MarginLayoutParams)?.let { params ->
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

fun View.invisible() = run { isInvisible = true }
fun View.visible() = run { isVisible = true }
fun View.gone() = run { isGone = true }

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
    avatarMenuItem.setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM)
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

fun View.doOnApplyWindowInsets(
    block: (
        view: View,
        insets: WindowInsetsCompat,
        paddings: Rect,
        margins: Rect
    ) -> WindowInsetsCompat
) {
    val initialPaddings = recordInitialPaddingsForView(this)
    val initialMargins = recordInitialMarginsForView(this)

    ViewCompat.setOnApplyWindowInsetsListener(this) { view, insets ->
        block(view, insets, initialPaddings, initialMargins)
    }

    requestApplyInsetsWhenAttached()
}

private fun recordInitialPaddingsForView(view: View) =
    Rect(view.paddingLeft, view.paddingTop, view.paddingRight, view.paddingBottom)

private fun recordInitialMarginsForView(view: View) =
    Rect(view.marginStart, view.marginTop, view.marginEnd, view.marginBottom)

fun View.requestApplyInsetsWhenAttached() {
    if (isAttachedToWindow) {
        requestApplyInsets()
    } else {
        doOnAttach { requestApplyInsets() }
    }
}

fun EditText.updateTextIfDiffer(text: String?) {
    if (this.text?.toString() == text) return
    setText(text)
}

fun ViewGroup.bulkIsEnabled(isEnabled: Boolean) {
    this.isEnabled = isEnabled
    toggleChildrenIsEnabled(isEnabled)
}

fun ViewGroup.toggleChildrenIsEnabled(isEnabled: Boolean) {
    children.forEach { view -> view.toggleIsEnabled(isEnabled) }
}

fun View.toggleIsEnabled(isEnabled: Boolean) {
    this.isEnabled = isEnabled
}
