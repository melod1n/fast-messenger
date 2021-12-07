package com.meloda.fast.screens.start

import android.os.Bundle
import android.view.View
import androidx.navigation.Navigation
import com.meloda.fast.R
import com.meloda.fast.api.UserConfig
import com.meloda.fast.base.BaseFragment

class StartFragment : BaseFragment(R.layout.fragment_start) {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val navController =
            Navigation.findNavController(requireActivity(), R.id.rootFragmentContainer)
        val mainGraph = navController.navInflater.inflate(R.navigation.main)

        val loginDestination = R.id.loginGraph
        val mainDestination = R.id.mainFragment

        mainGraph.startDestination =
            if (!UserConfig.isLoggedIn()) loginDestination
            else mainDestination

        navController.graph = mainGraph
    }

}