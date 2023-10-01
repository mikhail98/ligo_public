package com.ligo.tools

import com.ligo.core.getCurrentLanguageCode
import com.ligo.data.model.LocalizedConfig
import com.ligo.data.preferences.app.IAppPreferences
import com.ligo.data.repo.googleapis.IGoogleApisRepo
import com.ligo.tools.api.ILocalizationManager
import io.reactivex.rxjava3.core.Completable

internal class LocalizationManager(
    private val googleRepo: IGoogleApisRepo,
    private val appPreferences: IAppPreferences,
) : ILocalizationManager {

    companion object {
        private const val DEFAULT_LNG = "EN"
    }

    override fun updateLocalization(): Completable {
        return googleRepo.fetchLocalization()
            .flatMapCompletable { response ->
                saveLocalizedItems(response)
                Completable.complete()
            }
    }

    override fun getLocalized(key: String): String {
        return appPreferences.getLocalizations()
            .firstOrNull { it.locale == getLanguageCode() }
            ?.keys
            ?.firstOrNull { it.key.equals(key, true) }
            ?.value ?: key
    }

    private fun getLanguageCode(): String = getCurrentLanguageCode()?.uppercase() ?: DEFAULT_LNG

    private fun saveLocalizedItems(response: List<LocalizedConfig>) {
        appPreferences.saveLocalizations(response)
    }
}