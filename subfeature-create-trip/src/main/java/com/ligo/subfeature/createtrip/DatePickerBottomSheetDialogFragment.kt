package com.ligo.subfeature.createtrip

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.core.os.bundleOf
import com.ligo.common.BaseBottomSheetDialogFragment
import com.ligo.common.R
import com.ligo.common.ViewContainer
import com.ligo.common.setOnThrottleClickListener
import com.ligo.common.ui.timepicker.IntervalTimePicker
import com.ligo.common.ui.timepicker.IntervalTimePicker.TimeIntervalConstants.getInterval
import com.ligo.common.withBindingSafety
import com.ligo.data.model.ConfigStringKey
import com.ligo.data.model.ConfigStringKey.SELECTED_DATE_CANNOT_BE_EARLIER_THAN_NOW
import com.ligo.data.model.DetailedDate
import com.ligo.subfeature.createtrip.databinding.FragmentBottomSheetDatePickerBinding
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.subjects.PublishSubject
import io.reactivex.rxjava3.subjects.Subject
import org.koin.android.ext.android.inject
import org.koin.core.module.Module
import java.util.Calendar
import java.util.TimeZone
import com.ligo.core.R as CoreR

class DatePickerBottomSheetDialogFragment :
    BaseBottomSheetDialogFragment<DatePickerBottomSheetDialogFragmentViewModel>(), ViewContainer {

    companion object {
        const val TAG = "date_picker"

        private const val ARGS_TITLE = "DatePickerBottomSheetDialogFragment.title"
        private const val ARGS_DESCRIPTION = "DatePickerBottomSheetDialogFragment.message"

        fun newInstance(
            titleKey: String,
            descriptionKey: String,
        ): DatePickerBottomSheetDialogFragment {
            return DatePickerBottomSheetDialogFragment().apply {
                arguments = bundleOf(ARGS_TITLE to titleKey, ARGS_DESCRIPTION to descriptionKey)
            }
        }
    }

    private val detailedDateSubject: Subject<DetailedDate> =
        PublishSubject.create<DetailedDate>().toSerialized()

    override val koinModule: Module = DatePickerModule
    override val viewModel: DatePickerBottomSheetDialogFragmentViewModel by inject()

    private var selectedDay = Day.TODAY
    private val selectedCalendar = Calendar.getInstance()

    private var binding: FragmentBottomSheetDatePickerBinding? = null
    private val interval = IntervalTimePicker.TimeInterval.INTERVAL_5

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        binding = FragmentBottomSheetDatePickerBinding.inflate(inflater, container, false)
        withBindingSafety(binding) {
            btnBack.setLocalizedTextByKey(ConfigStringKey.BACK)
            btnSubmit.setLocalizedTextByKey(ConfigStringKey.SUBMIT)
        }
        return binding?.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        withBindingSafety(binding) {
            tvTitle.setLocalizedTextByKey(arguments?.getString(ARGS_TITLE).orEmpty())
            tvDescription.setLocalizedTextByKey(arguments?.getString(ARGS_DESCRIPTION).orEmpty())

            hoursToTp.setIs24HourView(true)
            hoursToTp.setTimePickerInterval(interval)

            tvToday.clipToOutline = true
            tvTomorrow.clipToOutline = true
            tvAfterTomorrow.clipToOutline = true

            val calendar = Calendar.getInstance()

            val today = localizationManager.getLocalized(ConfigStringKey.TODAY)
            val tomorrow = localizationManager.getLocalized(ConfigStringKey.TOMORROW)
            tvToday.text = "$today, ${calendar.getFormattedDate()}"
            calendar.add(Calendar.DAY_OF_MONTH, 1)
            tvTomorrow.text = "$tomorrow, ${calendar.getFormattedDate()}"
            calendar.add(Calendar.DAY_OF_MONTH, 1)
            tvAfterTomorrow.text = calendar.getFormattedDate()

            btnBack.clipToOutline = true
            btnSubmit.clipToOutline = true

            selectDay(Day.TODAY)
        }
    }

    private fun Calendar.getFormattedDate(): String =
        "${getFormatted(get(Calendar.DAY_OF_MONTH))}.${getFormatted(get(Calendar.MONTH) + 1)}"

    private fun getFormatted(field: Int): String {
        return String.format("%02d", field)
    }

    private fun selectDay(day: Day) {
        withBindingSafety(binding) {
            val context = context ?: return@withBindingSafety

            val whiteColor = ContextCompat.getColor(context, CoreR.color.white)
            val grayColor = ContextCompat.getColor(context, CoreR.color.gray_80)

            tvToday.setBackgroundResource(R.drawable.bg_date_picker_disabled)
            tvTomorrow.setBackgroundResource(R.drawable.bg_date_picker_disabled)
            tvAfterTomorrow.setBackgroundResource(R.drawable.bg_date_picker_disabled)

            tvToday.setTextColor(grayColor)
            tvTomorrow.setTextColor(grayColor)
            tvAfterTomorrow.setTextColor(grayColor)

            val viewToSelect = when (day) {
                Day.TODAY -> tvToday
                Day.TOMORROW -> tvTomorrow
                Day.DAY_AFTER_TOMORROW -> tvAfterTomorrow
            }
            viewToSelect.setBackgroundResource(R.drawable.bg_date_picker)
            viewToSelect.setTextColor(whiteColor)

            selectedCalendar.add(Calendar.DAY_OF_MONTH, -selectedDay.addedDays)
            selectedDay = day
            selectedCalendar.add(Calendar.DAY_OF_MONTH, selectedDay.addedDays)
        }
    }

    override fun onStart() {
        super.onStart()
        withBindingSafety(binding) {
            btnBack.setOnThrottleClickListener(startedCompositeDisposable) { dismiss() }
            tvToday.setOnThrottleClickListener(startedCompositeDisposable) { selectDay(Day.TODAY) }
            tvTomorrow.setOnThrottleClickListener(startedCompositeDisposable) { selectDay(Day.TOMORROW) }
            tvAfterTomorrow.setOnThrottleClickListener(startedCompositeDisposable) { selectDay(Day.DAY_AFTER_TOMORROW) }
            btnSubmit.setOnThrottleClickListener(startedCompositeDisposable) {
                selectedCalendar[Calendar.HOUR_OF_DAY] = hoursToTp.hour
                selectedCalendar[Calendar.MINUTE] = hoursToTp.minute * getInterval(interval)
                selectedCalendar[Calendar.SECOND] = 0

                if (selectedCalendar.timeInMillis < Calendar.getInstance().timeInMillis) {
                    Toast.makeText(
                        btnSubmit.context,
                        getLocalizedStringByKey(SELECTED_DATE_CANNOT_BE_EARLIER_THAN_NOW),
                        Toast.LENGTH_SHORT
                    ).show()
                } else {
                    selectedCalendar.timeZone = TimeZone.getTimeZone("Etc/GMT")
                    detailedDateSubject.onNext(
                        DetailedDate(
                            second = selectedCalendar[Calendar.SECOND],
                            minute = selectedCalendar[Calendar.MINUTE],
                            hour = selectedCalendar[Calendar.HOUR_OF_DAY],
                            day = selectedCalendar[Calendar.DAY_OF_MONTH],
                            month = selectedCalendar[Calendar.MONTH] + 1,
                        )
                    )
                    dismiss()
                }
            }
        }
    }

    fun getDetailedDateObservable(): Observable<DetailedDate> = detailedDateSubject

    enum class Day(val addedDays: Int) {
        TODAY(0), TOMORROW(1), DAY_AFTER_TOMORROW(2)
    }
}