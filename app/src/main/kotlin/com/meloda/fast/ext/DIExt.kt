package com.meloda.fast.ext

import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.meloda.fast.base.BaseDIFragment
import org.kodein.di.*

inline fun <reified VM : ViewModel> BaseDIFragment.activityViewModel(): Lazy<VM> {
    return viewModels(ownerProducer = { parentFragment ?: requireActivity() },
        factoryProducer = { getFactoryInstance() })
}

inline fun <reified VM : ViewModel> BaseDIFragment.fragmentViewModel(): Lazy<VM> {
    return viewModels(factoryProducer = { getFactoryInstance() })
}

inline fun <reified VM : ViewModel> DI.MainBuilder.bindViewModel(overrides: Boolean? = null): DI.Builder.TypeBinder<VM> {
    return bind<VM>(VM::class.java.simpleName, overrides)
}

fun BaseDIFragment.getFactoryInstance(): ViewModelProvider.Factory {
    val viewModeFactory: ViewModelFactory by instance()
    return viewModeFactory
}

class ViewModelFactory(private val injector: DI) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return injector.direct.instance<ViewModel>(tag = modelClass.simpleName) as T
    }
}
