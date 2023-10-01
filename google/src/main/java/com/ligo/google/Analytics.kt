package com.ligo.google

import android.os.Bundle
import com.google.firebase.analytics.FirebaseAnalytics
import com.ligo.google.api.IAnalytics

internal class Analytics(
    private val firebaseAnalytics: FirebaseAnalytics,
) : IAnalytics {

    companion object {
        private const val PARAM_VERSION = "version"
        private const val PARAM_PLATFORM = "platform"
        private const val PLATFORM = "Android"
    }

    override fun logEvent(eventName: String, bundle: Bundle) {
        if (!BuildConfig.DEBUG) {
            bundle.apply {
                putString(PARAM_VERSION, BuildConfig.VERSION_NAME)
                putString(PARAM_PLATFORM, PLATFORM)
            }
            firebaseAnalytics.logEvent(eventName, bundle)
        }
    }
}