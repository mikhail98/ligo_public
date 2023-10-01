package com.ligo.core

import android.app.ActivityManager
import android.content.Context
import android.content.res.Resources
import android.util.TypedValue
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import java.util.TimeZone

fun Float.dpToPx(): Float {
    return TypedValue.applyDimension(
        TypedValue.COMPLEX_UNIT_DIP,
        this,
        Resources.getSystem().displayMetrics
    )
}

fun Int.dpToPx(): Int {
    return this.toFloat().dpToPx().toInt()
}

fun printError(throwable: Throwable, isPrint: Boolean = BuildConfig.DEBUG) {
    if (isPrint) {
        println("LOGRE:: error ${throwable.localizedMessage ?: throwable.javaClass.name}")
        throwable.printStackTrace()
    }
}

fun checkServiceRunning(context: Context, serviceClass: Class<*>): Boolean {
    val manager = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager?
    for (service in manager?.getRunningServices(Int.MAX_VALUE) ?: listOf()) {
        if (serviceClass.name == service.service.className) {
            return true
        }
    }
    return false
}

fun getTimeInMillis(dateString: String): Long {
    val datePattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'"
    val dateTimeZone = "Etc/UTC"
    return try {
        return SimpleDateFormat(datePattern, Locale.getDefault()).apply {
            timeZone = TimeZone.getTimeZone(dateTimeZone)
        }.parse(dateString)?.time ?: System.currentTimeMillis()
    } catch (e: Exception) {
        System.currentTimeMillis()
    }
}

fun getCurrentLanguageCode(): String? {
    return Resources.getSystem().configuration?.locales?.get(0)?.language
}

fun getFormattedDate(milliSeconds: Long, dateFormat: String? = "dd MMMM yyyy, HH:mm"): String {
    val formatter = SimpleDateFormat(dateFormat, Locale.getDefault())
    val calendar = Calendar.getInstance()
    calendar.timeInMillis = milliSeconds
    return formatter.format(calendar.time)
}