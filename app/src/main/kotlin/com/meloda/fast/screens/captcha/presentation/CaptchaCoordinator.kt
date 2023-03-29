package com.meloda.fast.screens.captcha.presentation

import com.github.terrakok.cicerone.Router
import com.meloda.fast.screens.captcha.screen.CaptchaResult
import kotlinx.coroutines.flow.MutableSharedFlow

interface CaptchaCoordinator {

    fun finishWithResult(result: CaptchaResult)
}

class CaptchaCoordinatorImpl constructor(
    val resultFlow: MutableSharedFlow<CaptchaResult>,
    val router: Router
) : CaptchaCoordinator {

    override fun finishWithResult(result: CaptchaResult) {
        resultFlow.tryEmit(result)
        router.exit()
    }
}
