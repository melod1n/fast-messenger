package ru.melod1n.project.vkm.activity

import android.os.Bundle
import ru.melod1n.project.vkm.R
import ru.melod1n.project.vkm.base.BaseActivity
import ru.melod1n.project.vkm.common.FragmentSwitcher
import ru.melod1n.project.vkm.extensions.ContextExtensions.color
import ru.melod1n.project.vkm.extensions.ContextExtensions.drawable
import ru.melod1n.project.vkm.extensions.DrawableExtensions.tint
import ru.melod1n.project.vkm.fragment.FragmentSettings
import ru.melod1n.project.vkm.widget.Toolbar

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