package com.meloda.fast.base

import androidx.annotation.LayoutRes
import androidx.appcompat.app.AppCompatActivity

abstract class BaseActivity : AppCompatActivity {

    constructor() : super()

    constructor(@LayoutRes resId: Int) : super(resId)
}
