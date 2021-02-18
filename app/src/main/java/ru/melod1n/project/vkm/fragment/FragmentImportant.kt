package ru.melod1n.project.vkm.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import ru.melod1n.project.vkm.R
import ru.melod1n.project.vkm.base.BaseFragment


class FragmentImportant : BaseFragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_important, container, false)
    }

}