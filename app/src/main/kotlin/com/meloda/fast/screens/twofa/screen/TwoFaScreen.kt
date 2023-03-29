package com.meloda.fast.screens.twofa.screen

import com.github.terrakok.cicerone.Router
import com.github.terrakok.cicerone.androidx.FragmentScreen
import com.meloda.fast.base.screen.AppScreen
import com.meloda.fast.base.screen.createResultFlow
import com.meloda.fast.screens.twofa.TwoFaFragment
import com.meloda.fast.screens.twofa.di.twoFaModule
import org.koin.core.context.loadKoinModules

class TwoFaScreen : AppScreen<Unit, TwoFaResult> {

    init {
        loadKoinModules(twoFaModule)
    }

    override val resultFlow = createResultFlow()

    override fun show(router: Router, args: Unit) {
        router.navigateTo(FragmentScreen { TwoFaFragment.newInstance() })
    }
}
