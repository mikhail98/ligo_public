package com.ligo.common.ui.timepicker

import android.annotation.SuppressLint
import android.content.Context
import android.content.res.Resources
import android.util.AttributeSet
import android.widget.NumberPicker
import android.widget.TimePicker
import com.ligo.common.ui.timepicker.IntervalTimePicker.TimeIntervalConstants.MINUTES_AMOUNT
import com.ligo.common.ui.timepicker.IntervalTimePicker.TimeIntervalConstants.getInterval
import com.ligo.core.printError

@SuppressLint("DiscouragedApi")
class IntervalTimePicker(context: Context, attributeSet: AttributeSet) :
    TimePicker(context, attributeSet) {

    private val minutePicker by lazy {
        findViewById<NumberPicker>(
            Resources.getSystem().getIdentifier("minute", "id", "android")
        )
    }

    fun setTimePickerInterval(interval: TimeInterval) {
        try {
            val timePickerInterval = getInterval(interval)

            val displayedValues = ArrayList<String>()
            var i = 0
            while (i < 60) {
                displayedValues.add(String.format("%02d", i))
                i += timePickerInterval
            }

            minutePicker.minValue = 0
            minutePicker.maxValue = (MINUTES_AMOUNT / timePickerInterval) - 1

            minutePicker.displayedValues = displayedValues.toTypedArray()
        } catch (e: Exception) {
            printError(e)
        }
    }

    override fun setMinute(minute: Int) {
        val index = minutePicker.displayedValues.indexOf(String.format("%02d", minute))
        minutePicker.value = index
    }

    enum class TimeInterval {
        INTERVAL_1, INTERVAL_5, INTERVAL_10, INTERVAL_15, INTERVAL_20, INTERVAL_30
    }

    object TimeIntervalConstants {
        const val MINUTES_AMOUNT = 60

        fun getInterval(interval: TimeInterval) = when (interval) {
            TimeInterval.INTERVAL_1 -> 1
            TimeInterval.INTERVAL_5 -> 5
            TimeInterval.INTERVAL_10 -> 10
            TimeInterval.INTERVAL_15 -> 15
            TimeInterval.INTERVAL_20 -> 20
            TimeInterval.INTERVAL_30 -> 30
        }
    }
}