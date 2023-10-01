package com.ligo.toggler

import com.ligo.toggler.api.IToggler
import org.koin.dsl.module

val TogglerModule = module {
    single<IToggler> { Toggler(get()) }
}