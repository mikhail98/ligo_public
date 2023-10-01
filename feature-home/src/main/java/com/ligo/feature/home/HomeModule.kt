package com.ligo.feature.home

import com.ligo.home.navigator.HomeNavigator
import com.ligo.navigator.homeapi.IHomeNavigator
import org.koin.dsl.module

val HomeModule = module {
    factory {
        HomeFragmentViewModel(
            get(),
            get(),
            get(),
            get(),
            get(),
            get(),
            get()
        )
    }

    single<IHomeNavigator> { HomeNavigator() }
}