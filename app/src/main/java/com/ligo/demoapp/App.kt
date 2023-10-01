package com.ligo.demoapp

import android.app.Application
import com.ligo.core.Initializable
import com.ligo.core.SSLTruster
import com.ligo.tools.api.IInitializer
import org.koin.android.ext.android.inject
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin

class App : Application() {

    private val initializer by inject<IInitializer>()

    override fun onCreate() {
        super.onCreate()

        startKoin {
            androidContext(this@App)
            modules(KoinModules.getModules())
        }

        initializer.on(Initializable.On.CREATE)

        SSLTruster.disableSSLCertificateVerify()
    }
}