package com.meloda.fast.extensions

import android.view.View
import androidx.fragment.app.Fragment

object FragmentExtensions {

    fun <T : View> Fragment.findViewById(resId: Int): T {
        return requireView().findViewById(resId)
    }

    fun Fragment.runOnUiThread(runnable: Runnable) {
        activity?.runOnUiThread(runnable)
    }

}