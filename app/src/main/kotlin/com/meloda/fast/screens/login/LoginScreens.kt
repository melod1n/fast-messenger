package com.meloda.fast.screens.login

import com.github.terrakok.cicerone.androidx.FragmentScreen

object LoginScreens {

    fun login() = FragmentScreen {
        LoginFragment.newInstance()
    }
}
