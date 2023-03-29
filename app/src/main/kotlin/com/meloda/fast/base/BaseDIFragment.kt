package com.meloda.fast.base

import androidx.annotation.LayoutRes
import org.kodein.di.DIAware

abstract class BaseDIFragment : BaseFragment, DIAware {

    constructor() : super()

    constructor(@LayoutRes resId: Int) : super(resId)
}
