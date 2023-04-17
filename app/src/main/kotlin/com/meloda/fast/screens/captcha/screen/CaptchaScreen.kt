package com.meloda.fast.screens.captcha.screen

import com.github.terrakok.cicerone.Router
import com.meloda.fast.base.screen.AppScreen
import com.meloda.fast.base.screen.createResultFlow
import com.meloda.fast.screens.captcha.CaptchaScreens

class CaptchaScreen : AppScreen<CaptchaArguments, CaptchaResult> {

    override val resultFlow = createResultFlow()

    override fun show(router: Router, args: CaptchaArguments) {
        router.navigateTo(
            CaptchaScreens.captchaScreen(
                captchaSid = args.captchaSid,
                captchaImage = args.captchaImage
            )
        )
    }
}


