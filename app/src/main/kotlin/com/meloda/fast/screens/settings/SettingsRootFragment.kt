package com.meloda.fast.screens.settings

import android.os.Bundle
import android.view.View
import androidx.fragment.app.commit
import androidx.fragment.app.setFragmentResultListener
import by.kirich1409.viewbindingdelegate.viewBinding
import com.meloda.fast.R
import com.meloda.fast.base.BaseFragment
import com.meloda.fast.common.Screens
import com.meloda.fast.databinding.FragmentSettingsRootBinding

class SettingsRootFragment : BaseFragment(R.layout.fragment_settings_root) {

    companion object {
        const val KeyCheckUpdates = "check_updates"
    }

    private val binding by viewBinding(FragmentSettingsRootBinding::bind)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.toolbar.setNavigationOnClickListener { requireActivity().onBackPressedDispatcher.onBackPressed() }

        setFragmentResultListener(KeyCheckUpdates) { _, _ ->
            openUpdatesScreen()
        }

        childFragmentManager.commit {
            replace(R.id.settings_fragment_container, SettingsPrefsFragment())
        }
    }

    fun openUpdatesScreen() {
        requireActivityRouter().navigateTo(Screens.Updates())
    }

}