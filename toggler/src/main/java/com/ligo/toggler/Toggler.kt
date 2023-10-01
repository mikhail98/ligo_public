package com.ligo.toggler

import com.ligo.core.BuildConfig
import com.ligo.google.api.IRemoteConfig
import com.ligo.toggler.api.Feature
import com.ligo.toggler.api.IToggler
import io.reactivex.rxjava3.core.Observable

internal class Toggler(private val remoteConfig: IRemoteConfig) : IToggler {

    override fun getAvailableFeatureListObservable(): Observable<List<Feature>> {
        return remoteConfig.getFetchAndActivateCompleteObservable()
            .map {
                if (BuildConfig.SANDBOX) buildSandboxToggles() else buildProdToggles()
            }
    }

    private fun buildSandboxToggles(): List<Feature> {
        val featureList = mutableListOf<Feature>()
        featureList.add(Feature.CHAT)
        return featureList
    }

    private fun buildProdToggles(): List<Feature> {
        val featureList = mutableListOf<Feature>()
        if (remoteConfig.getBoolean(IRemoteConfig.CHAT_ENABLED)) {
            featureList.add(Feature.CHAT)
        }
        return featureList
    }
}