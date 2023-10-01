package com.ligo.data.preferences

import android.content.SharedPreferences
import com.google.gson.Gson
import com.ligo.core.printError

internal abstract class BasePreferences(private val sharedPreferences: SharedPreferences) {

    fun <T> get(propertyName: String, defaultValue: T, clazz: Class<T>): T {
        val string = sharedPreferences.getString(propertyName, "")
        return if (string.isNullOrEmpty()) {
            defaultValue
        } else {
            try {
                Gson().fromJson(string, clazz)
            } catch (e: Exception) {
                defaultValue
            }
        }
    }

    fun <T> get(propertyName: String, clazz: Class<T>): T? {
        val string = sharedPreferences.getString(propertyName, "")
        return if (string.isNullOrEmpty()) {
            null
        } else {
            try {
                Gson().fromJson(string, clazz)
            } catch (e: Exception) {
                null
            }
        }
    }

    protected fun clearPrefs() {
        try {
            sharedPreferences.edit().clear().apply()
        } catch (e: Exception) {
            printError(e)
        }
    }

    fun <T> save(propertyName: String, obj: T) {
        sharedPreferences.edit().putString(propertyName, Gson().toJson(obj)).apply()
    }

    fun remove(propertyName: String) {
        sharedPreferences.edit().remove(propertyName).apply()
    }
}