package com.meloda.fast.activity

import android.os.Bundle
import android.viewbinding.library.activity.viewBinding
import com.meloda.fast.R
import com.meloda.fast.base.BaseActivity
import com.meloda.fast.databinding.ActivityMainBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : BaseActivity(R.layout.activity_main) {

    private val binding: ActivityMainBinding by viewBinding()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

}