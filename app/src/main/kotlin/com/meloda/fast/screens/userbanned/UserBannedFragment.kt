package com.meloda.fast.screens.userbanned

import android.os.Bundle
import android.view.View
import androidx.core.os.bundleOf
import androidx.fragment.app.viewModels
import by.kirich1409.viewbindingdelegate.viewBinding
import com.meloda.fast.R
import com.meloda.fast.base.viewmodel.BaseViewModelFragment
import com.meloda.fast.databinding.FragmentUserBannedBinding
import dev.chrisbanes.insetter.applyInsetter

class UserBannedFragment :
    BaseViewModelFragment<UserBannedViewModel>(R.layout.fragment_user_banned) {

    companion object {

        private const val ArgMemberName = "member_name"
        private const val ArgMessage = "message"
        private const val ArgRestoreUrl = "restore_url"
        private const val ArgAccessToken = "access_token"

        fun newInstance(
            memberName: String,
            message: String,
            restoreUrl: String,
            accessToken: String
        ): UserBannedFragment {
            val fragment = UserBannedFragment()
            fragment.arguments = bundleOf(
                ArgMemberName to memberName,
                ArgMessage to message,
                ArgRestoreUrl to restoreUrl,
                ArgAccessToken to accessToken
            )
            return fragment
        }
    }

    override val viewModel: UserBannedViewModel by viewModels()
    private val binding by viewBinding(FragmentUserBannedBinding::bind)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.root.applyInsetter {
            type(navigationBars = true) { padding() }
        }

        binding.toolbar.applyInsetter {
            type(statusBars = true) { padding() }
        }
        binding.toolbar.setNavigationOnClickListener { requireActivity().onBackPressedDispatcher.onBackPressed() }

        binding.name.text = requireArguments().getString(ArgMemberName)
        binding.reason.text = requireArguments().getString(ArgMessage)
    }

}