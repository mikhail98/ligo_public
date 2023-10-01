package com.ligo.core

import android.annotation.SuppressLint
import java.security.KeyStore
import java.security.SecureRandom
import java.security.cert.X509Certificate
import java.util.Arrays
import javax.net.ssl.HostnameVerifier
import javax.net.ssl.HttpsURLConnection
import javax.net.ssl.SSLContext
import javax.net.ssl.SSLSession
import javax.net.ssl.TrustManagerFactory
import javax.net.ssl.X509TrustManager

object SSLTruster {

    private const val SSL = "SSL"

    private const val HOST_101 = "https://ligo.herokuapp.com"
    private const val HOST_102 = "ligo-sandbox-72985dd69029.herokuapp.com"

    private const val HOST_201 = "maps.gstatic.com"
    private const val HOST_202 = "reports.crashlytics.com"

    private val hostList = listOf(
        HOST_101,
        HOST_102,
        HOST_201,
        HOST_202
    )

    fun disableSSLCertificateVerify() {
        val trustManager = if (BuildConfig.DEBUG) {
            getDebugTrustManager()
        } else {
            getReleaseTrustManager()
        }
        try {
            val sc = SSLContext.getInstance(SSL)
            sc.init(null, arrayOf(trustManager), SecureRandom())
            HttpsURLConnection.setDefaultSSLSocketFactory(sc.socketFactory)
            HttpsURLConnection.setDefaultHostnameVerifier(object : HostnameVerifier {
                override fun verify(hostname: String, session: SSLSession): Boolean {
                    hostList.forEach { host ->
                        if (hostname.equals(host, ignoreCase = true)) {
                            return true
                        }
                        if (hostname.contains("google", ignoreCase = true)) {
                            return true
                        }
                    }
                    return false
                }
            })
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun getReleaseTrustManager(): X509TrustManager {
        val trustManagerFactory =
            TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm())
        trustManagerFactory.init(null as KeyStore?)
        val trustManagers = trustManagerFactory.trustManagers

        check(!(trustManagers.isEmpty() || trustManagers[0] !is X509TrustManager)) {
            "Unexpected default trust managers: " + Arrays.toString(trustManagers)
        }
        return trustManagers[0] as X509TrustManager
    }

    @SuppressLint("TrustAllX509TrustManager")
    private fun getDebugTrustManager() = object : X509TrustManager {

        override fun checkClientTrusted(chain: Array<out X509Certificate>?, authType: String?) {
        }

        override fun checkServerTrusted(chain: Array<out X509Certificate>?, authType: String?) {
        }

        override fun getAcceptedIssuers() = arrayOf<X509Certificate>()
    }
}