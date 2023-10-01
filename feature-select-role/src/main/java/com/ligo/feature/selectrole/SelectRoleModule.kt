package com.ligo.feature.selectrole

import org.koin.dsl.module

val SelectRoleModule = module {
    factory {
        SelectRoleFragmentViewModel(
            get(),
            get(),
            get(),
            get(),
            get(),
            get()
        )
    }
}