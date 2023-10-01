package com.ligo.data.preferences.app

import android.content.SharedPreferences
import com.ligo.data.model.LocalizedConfig
import com.ligo.data.model.Location
import com.ligo.data.model.User
import com.ligo.data.model.UserRequest
import com.ligo.data.preferences.BasePreferences

internal class AppPreferences(sharedPreferences: SharedPreferences) :
    BasePreferences(sharedPreferences),
    IAppPreferences {

    companion object {
        private const val KEY_PREFIX = "AppPreferences."
        private const val KEY_OPEN_TRIP = "${KEY_PREFIX}OpenTrip"
        private const val KEY_USER = "${KEY_PREFIX}User"
        private const val KEY_LOGIN_USER = "${KEY_PREFIX}LoginUser"
        private const val KEY_RECENT_LOCATION_SEARCH = "${KEY_PREFIX}RecentLocationSearch"
        private const val KEY_LOCALIZATIONS = "${KEY_PREFIX}Localizations"

        private const val MAX_RECENT_SIZE = 8
    }

    override fun saveUser(user: User?) {
        save(KEY_USER, user)
    }

    override fun getUser(): User? {
        return get(KEY_USER, User::class.java)
    }

    override fun saveRegisterUser(user: UserRequest?) {
        save(KEY_LOGIN_USER, user)
    }

    override fun getRegisterUser(): UserRequest? {
        return get(KEY_LOGIN_USER, UserRequest::class.java)
    }

    override fun saveLocalizations(localizedConfigs: List<LocalizedConfig>) {
        save(KEY_LOCALIZATIONS, LocalizedConfigsWrapper(localizedConfigs))
    }

    override fun getLocalizations(): List<LocalizedConfig> {
        return get(
            KEY_LOCALIZATIONS,
            LocalizedConfigsWrapper(emptyList()),
            LocalizedConfigsWrapper::class.java
        ).configs
    }

    override fun setOnboardingShown(onboardingType: String) {
        save("${KEY_PREFIX}$onboardingType", true)
    }

    override fun isOnboardingShown(onboardingType: String): Boolean {
        return get(
            "${KEY_PREFIX}$onboardingType",
            false,
            Boolean::class.java
        )
    }

    override fun saveOpenTripState(openTrip: Boolean) {
        save(KEY_OPEN_TRIP, openTrip)
    }

    override fun getOpenTripState(): Boolean {
        return get(KEY_OPEN_TRIP, true, Boolean::class.java)
    }

    override fun getRecentLocationSearch(): List<Location> {
        return get(
            KEY_RECENT_LOCATION_SEARCH,
            RecentSearchWrapper(),
            RecentSearchWrapper::class.java
        ).locations
    }

    override fun addRecentLocationSearch(location: Location?) {
        location ?: return
        val newList = ArrayList(getRecentLocationSearch())
        val indexOf = newList.indexOf(location)
        if (indexOf != -1) {
            newList.removeAt(indexOf)
        }
        newList.add(0, location)
        if (newList.size == MAX_RECENT_SIZE && newList.size != 1) newList.removeLast()

        save(KEY_RECENT_LOCATION_SEARCH, RecentSearchWrapper(newList))
    }

    override fun logout() {
        clearPrefs()
    }

    private class LocalizedConfigsWrapper(
        val configs: List<LocalizedConfig>,
    )

    private class RecentSearchWrapper(
        val locations: List<Location> = listOf(),
    )
}