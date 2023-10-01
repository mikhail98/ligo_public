package com.ligo.tools.api

interface ILocationTracker {

    fun startLocationTracking()

    fun stopLocationTracking()

    fun isLocationServiceRunning(): Boolean
}