package com.meloda.fast.base

import android.view.View
import androidx.appcompat.app.AppCompatActivity

abstract class BaseActivity : AppCompatActivity() {

    fun getRootView(): View {
        return findViewById(android.R.id.content)
    }

}