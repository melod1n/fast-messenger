package com.meloda.fast.screens.captcha.screen

import com.github.terrakok.cicerone.Router
import com.github.terrakok.cicerone.androidx.FragmentScreen
import com.meloda.fast.base.screen.AppScreen
import com.meloda.fast.base.screen.createResultFlow
import com.meloda.fast.screens.captcha.CaptchaFragment
import com.meloda.fast.screens.captcha.di.captchaModule
import org.koin.core.context.loadKoinModules

class CaptchaScreen : AppScreen<CaptchaArguments, CaptchaResult> {

    init {
        loadKoinModules(captchaModule)
    }

    override val resultFlow = createResultFlow()

    override fun show(router: Router, args: CaptchaArguments) {
        router.navigateTo(FragmentScreen { CaptchaFragment.newInstance(args.captchaImageLink) })
    }
}


