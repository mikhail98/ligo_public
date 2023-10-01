package com.ligo.navigator

import com.ligo.navigator.api.INavigator
import org.koin.dsl.module

val NavigatorModule = module {
    single<INavigator> { Navigator() }
}