package com.meloda.fast.screens.captcha.screen

import com.github.terrakok.cicerone.Router
import com.meloda.fast.base.screen.AppScreen
import com.meloda.fast.base.screen.createResultFlow
import com.meloda.fast.screens.captcha.CaptchaScreens
import kotlin.properties.Delegates

class CaptchaScreen : AppScreen<CaptchaArguments, CaptchaResult> {

    override val resultFlow = createResultFlow()

    override var args: CaptchaArguments by Delegates.notNull()

    override fun show(router: Router, args: CaptchaArguments) {
        this.args = args
        router.navigateTo(CaptchaScreens.captchaScreen())
    }
}


