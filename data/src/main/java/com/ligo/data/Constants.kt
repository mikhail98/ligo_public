package com.ligo.data

object Constants {

    private const val PROD = "https://ligo.herokuapp.com"
    private const val SANDBOX = "https://ligo-sandbox-72985dd69029.herokuapp.com"
    private const val LOCAL_MACHINE = "http://10.0.2.2"
    private const val LOCAL_NETWORK = "http://192.168.1.144"

    private val environment = if (BuildConfig.SANDBOX) ENV.SANDBOX else ENV.PROD

    fun getBaseUrl(): String {
        return when (environment) {
            ENV.SANDBOX -> SANDBOX
            ENV.PROD -> PROD
        }
    }

    fun getBaseSocketUrl(): String {
        return getBaseUrl()
    }

    private enum class ENV {
        SANDBOX, PROD
    }
}