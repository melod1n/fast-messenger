package com.meloda.fast.screens.twofa.screen

import com.github.terrakok.cicerone.Router
import kotlinx.coroutines.flow.MutableSharedFlow

interface TwoFaCoordinator {

    fun finishWithResult(result: TwoFaResult)
}

class TwoFaCoordinatorImpl(
    private val resultFlow: MutableSharedFlow<TwoFaResult>,
    private val router: Router
) : TwoFaCoordinator {

    override fun finishWithResult(result: TwoFaResult) {
        resultFlow.tryEmit(result)
        router.exit()
    }
}
