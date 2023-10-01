package com.ligo.data.api

import okhttp3.Interceptor
import okhttp3.Response

internal class LoggingInterceptor : Interceptor {

    companion object {
        private const val HTTPS = "HTTPS"
    }

    override fun intercept(chain: Interceptor.Chain): Response {
        val response = chain.proceed(chain.request())
        val request = response.request
        val http = if (request.isHttps) {
            HTTPS.plus(" ")
        } else {
            ""
        }
        val message = if (response.message.isNotEmpty()) {
            response.message.plus(" ")
        } else {
            ""
        }
        println("LOGRE:: $http${request.method} $message${response.code} ${request.url}")
        return response
    }
}