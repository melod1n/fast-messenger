package com.meloda.fast.activity

import android.os.Bundle
import com.meloda.extensions.ContextExtensions.drawable
import com.meloda.fast.R
import com.meloda.fast.base.BaseActivity
import com.meloda.fast.common.FragmentSwitcher
import com.meloda.fast.fragment.SettingsFragment
import com.meloda.fast.util.ColorUtils
import com.meloda.fast.widget.Toolbar

class SettingsActivityDeprecated : BaseActivity() {

    private lateinit var toolbar: Toolbar

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)
        initViews()

        setSupportActionBar(toolbar)

        toolbar.setNavigationClickListener { onBackPressed() }

        toolbar.navigationIcon = drawable(R.drawable.ic_arrow_back)
        toolbar.tintNavigationIcon(ColorUtils.getColorAccent(this))

        supportFragmentManager.beginTransaction()
            .replace(R.id.fragmentContainer, SettingsFragment())
            .commit()
    }

    private fun initViews() {
        toolbar = findViewById(R.id.toolbar)
    }

    override fun onBackPressed() {
        val currentFragment = FragmentSwitcher.getCurrentFragment(supportFragmentManager) ?: return

        if (currentFragment.javaClass == SettingsFragment::class.java && (currentFragment as SettingsFragment).onBackPressed()) {
            super.onBackPressed()
        }
    }

}