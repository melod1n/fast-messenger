package com.meloda.fast.screens.twofa.screen

import com.github.terrakok.cicerone.Router
import com.meloda.fast.base.screen.AppScreen
import com.meloda.fast.base.screen.createResultFlow
import com.meloda.fast.screens.twofa.TwoFaScreens
import com.meloda.fast.screens.twofa.model.TwoFaArguments
import com.meloda.fast.screens.twofa.model.TwoFaResult
import kotlin.properties.Delegates

class TwoFaScreen : AppScreen<TwoFaArguments, TwoFaResult> {

    override val resultFlow = createResultFlow()

    override var args: TwoFaArguments by Delegates.notNull()

    override fun show(router: Router, args: TwoFaArguments) {
        this.args = args
        router.navigateTo(TwoFaScreens.twoFaScreen())
    }
}
