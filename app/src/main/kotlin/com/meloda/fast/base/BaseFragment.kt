package com.meloda.fast.base

import android.os.Bundle
import androidx.annotation.LayoutRes
import androidx.fragment.app.Fragment

abstract class BaseFragment : Fragment {

    constructor() : super()

    constructor(@LayoutRes resId: Int) : super(resId)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (arguments == null) arguments = Bundle()
    }

}