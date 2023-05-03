package com.meloda.fast.screens.twofa

import com.github.terrakok.cicerone.androidx.FragmentScreen
import com.meloda.fast.screens.twofa.presentation.TwoFaFragment

object TwoFaScreens {

    fun twoFaScreen() = FragmentScreen(key = "TwoFaScreen") {
        TwoFaFragment.newInstance()
    }

}
