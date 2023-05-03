package com.meloda.fast.screens.login.screen

import com.github.terrakok.cicerone.Router
import com.meloda.fast.base.screen.AppScreen
import com.meloda.fast.base.screen.createResultFlow
import com.meloda.fast.screens.login.model.LoginResult

class LoginScreen : AppScreen<Unit, LoginResult> {

    override val resultFlow = createResultFlow()
    override var args: Unit = Unit

    override fun show(router: Router, args: Unit) {
        this.args = args
    }
}
