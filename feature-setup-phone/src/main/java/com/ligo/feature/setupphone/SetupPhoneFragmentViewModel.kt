package com.ligo.feature.setupphone

import com.google.i18n.phonenumbers.NumberParseException
import com.google.i18n.phonenumbers.PhoneNumberUtil
import com.ligo.common.BaseViewModel
import com.ligo.data.preferences.app.IAppPreferences
import com.ligo.google.api.IAnalytics
import com.ligo.navigator.api.INavigator
import com.ligo.navigator.api.Target
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.subjects.PublishSubject
import io.reactivex.rxjava3.subjects.Subject
import java.util.Locale

class SetupPhoneFragmentViewModel(
    navigator: INavigator,
    analytics: IAnalytics,
    private val appPreferences: IAppPreferences,
) : BaseViewModel(navigator, analytics) {

    private val phoneValidationSubject: Subject<Boolean> =
        PublishSubject.create<Boolean>().toSerialized()

    fun proceedToSelectRole(phone: String) {
        if (!isPhoneValid(phone)) return
        val registerUser = appPreferences.getRegisterUser()
        appPreferences.saveRegisterUser(registerUser?.copy(phone = phone))
        navigator.open(Target.SelectRole)
    }

    private fun isPhoneValid(phone: String): Boolean {
        val phoneNumberUtil = PhoneNumberUtil.getInstance()
        val isValid = try {
            val phoneNumber = phoneNumberUtil.parse(phone, Locale.getDefault().country)
            phoneNumberUtil.isValidNumber(phoneNumber)
        } catch (ex: NumberParseException) {
            false
        }
        return isValid
    }

    fun validatePhone(phone: String) {
        phoneValidationSubject.onNext(isPhoneValid(phone))
    }

    fun getPhoneValidationObservable(): Observable<Boolean> =
        phoneValidationSubject
}