package com.meloda.fast.base

import android.os.Build
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.meloda.fast.R
import com.meloda.fast.extensions.ContextExtensions.color
import com.meloda.fast.util.AndroidUtils
import com.meloda.fast.util.ColorUtils

abstract class BaseActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (Build.VERSION.SDK_INT == Build.VERSION_CODES.M) {
            val navigationBarColor =
                if (AndroidUtils.isDarkTheme()) {
                    color(R.color.dark_primaryDark)
                } else {
                    ColorUtils.darkenColor(color(R.color.primaryDark))
                }

            window.navigationBarColor = navigationBarColor
        }
    }

    fun getRootView(): View {
        return findViewById(android.R.id.content)
    }

}