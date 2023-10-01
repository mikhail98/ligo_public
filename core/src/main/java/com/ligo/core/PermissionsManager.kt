package com.ligo.core

import android.app.Activity
import androidx.core.app.ActivityCompat

class PermissionsManager(private val activity: Activity) {

    companion object {
        const val PERMISSION_REQUEST_CODE_LOCATION = 1001
        const val PERMISSION_REQUEST_CODE_NOTIFICATION = 1002
    }

    fun requestNotificationsPermissions(): Boolean {
        val permissionsData = PermissionChecker.isNotificationPermissionEnabled(activity)
        if (!permissionsData.first) {
            requestPermissions(permissionsData.second, PERMISSION_REQUEST_CODE_NOTIFICATION)
        }
        return permissionsData.first
    }

    fun requestLocationPermissions(): Boolean {
        val permissionsData = PermissionChecker.isLocationPermissionEnabled(activity)
        if (!permissionsData.first) {
            requestPermissions(permissionsData.second, PERMISSION_REQUEST_CODE_LOCATION)
        }
        return permissionsData.first
    }

    private fun requestPermissions(permissions: Array<String>, requestCode: Int) {
        ActivityCompat.requestPermissions(activity, permissions, requestCode)
    }
}