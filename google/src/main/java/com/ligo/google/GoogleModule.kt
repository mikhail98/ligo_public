package com.ligo.google

import android.annotation.SuppressLint
import android.content.Context
import com.google.android.gms.location.LocationServices
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.messaging.FirebaseMessaging
import com.google.firebase.storage.FirebaseStorage
import com.ligo.google.api.IAnalytics
import com.ligo.google.api.IAuthManager
import com.ligo.google.api.IFcmTokenManager
import com.ligo.google.api.ILocationManager
import com.ligo.google.api.IRemoteConfig
import com.ligo.google.api.IStorageManager
import org.koin.dsl.module

@SuppressLint("MissingPermission")
val GoogleModule = module {

    single { FirebaseAnalytics.getInstance(get()) }

    single { FirebaseMessaging.getInstance() }

    single { FirebaseStorage.getInstance() }

    single<IRemoteConfig> { RemoteConfig() }

    single<IAnalytics> { Analytics(get()) }

    single<IStorageManager> { StorageManager(get(), get()) }

    single<ILocationManager> { LocationManager(get(), get()) }

    single<IFcmTokenManager> { FcmTokenManager(get()) }

    single<IAuthManager> { AuthManager() }

    single { LocationServices.getFusedLocationProviderClient(get<Context>()) }
}