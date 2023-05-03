package com.meloda.fast.screens.captcha

import com.github.terrakok.cicerone.androidx.FragmentScreen
import com.meloda.fast.screens.captcha.presentation.CaptchaFragment

object CaptchaScreens {

    fun captchaScreen() = FragmentScreen(key = "CaptchaScreen") {
        CaptchaFragment.newInstance()
    }
}
