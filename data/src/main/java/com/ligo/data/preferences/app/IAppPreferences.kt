package com.ligo.data.preferences.app

import com.ligo.data.model.LocalizedConfig
import com.ligo.data.model.Location
import com.ligo.data.model.User
import com.ligo.data.model.UserRequest

interface IAppPreferences {

    fun saveUser(user: User?)

    fun getUser(): User?

    fun saveRegisterUser(user: UserRequest?)

    fun getRegisterUser(): UserRequest?

    fun saveLocalizations(localizedConfigs: List<LocalizedConfig>)

    fun getLocalizations(): List<LocalizedConfig>

    fun setOnboardingShown(onboardingType: String)

    fun isOnboardingShown(onboardingType: String): Boolean

    fun addRecentLocationSearch(location: Location?)

    fun getRecentLocationSearch(): List<Location>

    fun getOpenTripState(): Boolean

    fun saveOpenTripState(openTrip: Boolean)

    fun logout()
}