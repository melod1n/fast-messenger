package ru.melod1n.project.vkm.extensions

import android.view.View
import androidx.fragment.app.Fragment

object FragmentExtensions {

    fun <T : View> Fragment.findViewById(resId: Int): T {
        return requireView().findViewById(resId)
    }

    fun Fragment.runOnUiThread(runnable: Runnable) {
        requireActivity().runOnUiThread(runnable)
    }

}