package com.meloda.fast.activity

import android.os.Bundle
import com.meloda.fast.R
import com.meloda.fast.base.BaseActivity
import com.meloda.fast.common.FragmentSwitcher
import com.meloda.fast.extensions.ContextExtensions.color
import com.meloda.fast.extensions.ContextExtensions.drawable
import com.meloda.fast.extensions.DrawableExtensions.tint
import com.meloda.fast.fragment.FragmentSettings
import com.meloda.fast.widget.Toolbar

class SettingsActivity : BaseActivity() {

    private lateinit var toolbar: Toolbar

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)
        initViews()

        setSupportActionBar(toolbar)

        toolbar.navigationIcon = drawable(R.drawable.ic_arrow_back).tint(color(R.color.accent))

        toolbar.setNavigationClickListener { onBackPressed() }

        supportFragmentManager.beginTransaction()
            .replace(R.id.fragmentContainer, FragmentSettings()).commitNow()
    }

    private fun initViews() {
        toolbar = findViewById(R.id.toolbar)
    }

    override fun onBackPressed() {
        val currentFragment = FragmentSwitcher.getCurrentFragment(supportFragmentManager) ?: return

        if (currentFragment.javaClass == FragmentSettings::class.java && (currentFragment as FragmentSettings).onBackPressed()) {
            super.onBackPressed()
        }
    }

}