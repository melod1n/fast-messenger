package com.meloda.fast.fragment

import android.os.Bundle
import android.view.View
import android.viewbinding.library.fragment.viewBinding
import com.meloda.fast.R
import com.meloda.fast.base.BaseFragment
import com.meloda.fast.databinding.FragmentLoginBinding

class LoginFragment : BaseFragment(R.layout.fragment_login) {

    private val binding: FragmentLoginBinding by viewBinding()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


    }



}