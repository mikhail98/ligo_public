package com.ligo.core

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.content.ContextCompat

object PermissionChecker {

    fun isLocationPermissionEnabled(context: Context): Pair<Boolean, Array<String>> {
        val permissions = mutableListOf(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
        )
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            permissions.add(Manifest.permission.FOREGROUND_SERVICE_LOCATION)
        }
        return checkPermissions(context, permissions)
    }

    fun isNotificationPermissionEnabled(context: Context): Pair<Boolean, Array<String>> {
        val permissions = mutableListOf<String>()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            permissions.add(Manifest.permission.POST_NOTIFICATIONS)
        }
        return checkPermissions(context, permissions)
    }

    fun isCameraPermissionEnabled(context: Context): Pair<Boolean, Array<String>> {
        val permissions = mutableListOf(Manifest.permission.CAMERA)
        return checkPermissions(context, permissions)
    }

    fun isMediaPermissionEnabled(context: Context): Pair<Boolean, Array<String>> {
        val permissions = mutableListOf<String>()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            permissions.add(Manifest.permission.READ_MEDIA_IMAGES)
        } else {
            permissions.add(Manifest.permission.READ_EXTERNAL_STORAGE)
        }
        return checkPermissions(context, permissions)
    }

    private fun checkPermissions(
        context: Context,
        permissions: List<String>,
    ): Pair<Boolean, Array<String>> {
        return if (permissions.isEmpty()) {
            true to arrayOf()
        } else {
            val isAllGranted = permissions.all {
                ContextCompat.checkSelfPermission(context, it) == PackageManager.PERMISSION_GRANTED
            }
            isAllGranted to permissions.toTypedArray()
        }
    }
}