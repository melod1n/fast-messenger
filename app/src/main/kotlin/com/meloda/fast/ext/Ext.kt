package com.meloda.fast.ext

import android.animation.ValueAnimator
import android.content.res.Resources
import android.graphics.Rect
import android.graphics.drawable.Drawable
import android.os.Parcelable
import android.util.DisplayMetrics
import android.util.SparseArray
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import androidx.annotation.ColorInt
import androidx.annotation.Px
import androidx.appcompat.widget.Toolbar
import androidx.core.view.*
import androidx.core.widget.doAfterTextChanged
import androidx.lifecycle.*
import com.google.common.net.MediaType
import com.meloda.fast.common.AppGlobal
import com.meloda.fast.databinding.ToolbarMenuItemAvatarBinding
import com.meloda.fast.ext.ImageLoader.loadWithGlide
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

fun Int.dpToPx(): Int {
    val metrics = Resources.getSystem().displayMetrics
    return (this * (metrics.densityDpi.toFloat() / DisplayMetrics.DENSITY_DEFAULT)).toInt()
}

fun Float.dpToPx(): Int {
    val metrics = Resources.getSystem().displayMetrics
    return (this * (metrics.densityDpi.toFloat() / DisplayMetrics.DENSITY_DEFAULT)).toInt()
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

fun ValueAnimator.startWithIntValues(from: Int, to: Int) {
    setIntValues(from, to)
    start()
}

fun ValueAnimator.startWithFloatValues(from: Float, to: Float) {
    setFloatValues(from, to)
    start()
}

fun View.setMarginsPx(
    @Px leftMargin: Int? = null,
    @Px topMargin: Int? = null,
    @Px rightMargin: Int? = null,
    @Px bottomMargin: Int? = null
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

inline fun <T, K> Pair<T?, K?>.runIfElementsNotNull(block: (T, K) -> Unit) {
    val firstCopy = first
    val secondCopy = second
    if (firstCopy != null && secondCopy != null) {
        block(firstCopy, secondCopy)
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

fun View.showKeyboard(flags: Int = 0) {
    AppGlobal.inputMethodManager.showSoftInput(this, flags)
}

fun View.hideKeyboard(focusedView: View? = null, flags: Int = 0) {
    AppGlobal.inputMethodManager.hideSoftInputFromWindow(
        focusedView?.windowToken ?: this.windowToken, flags
    )
}

fun Toolbar.tintMenuItemIcons(@ColorInt colorToTint: Int) {
    menu.forEach { item ->
        item.icon?.setTint(colorToTint)
    }
}

fun Toolbar.addAvatarMenuItem(urlToLoad: String? = null, drawable: Drawable? = null): MenuItem {
    val avatarMenuItemBinding = ToolbarMenuItemAvatarBinding.inflate(
        LayoutInflater.from(context), null, false
    )

    val avatarMenuItem = menu.add("Profile")
    avatarMenuItem.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS)
    avatarMenuItem.actionView = avatarMenuItemBinding.root

    val imageView = avatarMenuItemBinding.avatar

    when {
        urlToLoad != null -> {
            imageView.loadWithGlide(
                url = urlToLoad,
                transformations = ImageLoader.userAvatarTransformations
            )
        }
        drawable != null -> {
            imageView.loadWithGlide(
                drawable = drawable,
                transformations = ImageLoader.userAvatarTransformations
            )
        }
    }

    return avatarMenuItem
}

fun <T> MutableLiveData<T>.notifyObservers() {
    this.value = this.value
}

fun <T> MutableLiveData<T>.setIfNotEquals(item: T) {
    if (this.value != item) this.value = item
}

fun <T> MutableLiveData<T>.requireValue(): T {
    return this.value!!
}

val EditText.trimmedText: String get() = text.toString().trim()

val MediaType.mimeType: String get() = "${type()}/${subtype()}"

fun EditText.selectLast() {
    setSelection(text.length)
}

fun <T> T?.requireNotNull(): T {
    return requireNotNull(this)
}


fun String?.orDots(count: Int = 3): String {
    return this ?: ("." * count)
}

private operator fun String.times(count: Int): String {
    val builder = StringBuilder()
    for (i in 0 until count) {
        builder.append(this)
    }

    return builder.toString()
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

fun EditText.notifyObservers() {
    this.text = this.text
}

fun EditText.notifyAboutChanges(mutableLiveData: MutableLiveData<String>) {
    doAfterTextChanged { editable ->
        mutableLiveData.value = editable?.toString().orEmpty()
    }
}

fun CheckBox.notifyAboutChanges(mutableLiveData: MutableStateFlow<Boolean>) {
    setOnCheckedChangeListener { _, isChecked -> mutableLiveData.value = isChecked }
}

fun <T> MutableLiveData<T>.flowOnLifecycle(
    lifecycle: Lifecycle,
    onCollect: (item: T) -> Unit
) {
    asFlow()
        .flowWithLifecycle(lifecycle)
        .onEach { onCollect.invoke(it) }
        .launchIn(lifecycle.coroutineScope)
}