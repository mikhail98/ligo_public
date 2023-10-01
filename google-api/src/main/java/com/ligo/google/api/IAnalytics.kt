package com.ligo.google.api

import android.os.Bundle

interface IAnalytics {

    fun logEvent(eventName: String, bundle: Bundle = Bundle())

    object Events {
        const val ACTION_SCREEN_OPENED = "ACTION_SCREEN_OPENED"
    }
}
