package com.ligo.data.api

import com.google.gson.GsonBuilder
import com.ligo.data.BuildConfig
import com.ligo.data.Constants
import com.ligo.data.api.typeadapters.EnumTypeAdapterFactory
import hu.akarnokd.rxjava3.retrofit.RxJava3CallAdapterFactory
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Response
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

internal class RetrofitBuilder {

    companion object {
        private const val CONNECTION_TIMEOUT = 90L
        private const val WRITE_TIMEOUT = 90L
        private const val READ_TIMEOUT = 90L

        const val AUTH_TOKEN_PARAMETER = "x-access-token"

        private const val CONTENT_TYPE_PARAMETER = "content-type"
        private const val CONTENT_TYPE_VALUE = "application/json"
    }

    private fun contentTypeHeaderParam() = Pair(CONTENT_TYPE_PARAMETER, CONTENT_TYPE_VALUE)

    private fun jsonHeader() = listOf(contentTypeHeaderParam())

    private val gsonBuilder by lazy {
        GsonBuilder()
            .registerTypeAdapterFactory(EnumTypeAdapterFactory())
            .create()
    }

    private fun <T> buildRetrofit(client: OkHttpClient, clazz: Class<T>) = Retrofit.Builder()
        .baseUrl(Constants.getBaseUrl())
        .addConverterFactory(GsonConverterFactory.create(gsonBuilder))
        .addCallAdapterFactory(RxJava3CallAdapterFactory.create())
        .client(client)
        .build()
        .create(clazz)

    private fun buildClient(headers: List<Pair<String, String>>) = with(OkHttpClient.Builder()) {
        connectTimeout(CONNECTION_TIMEOUT, TimeUnit.SECONDS)
        writeTimeout(WRITE_TIMEOUT, TimeUnit.SECONDS)
        readTimeout(READ_TIMEOUT, TimeUnit.SECONDS)
        addInterceptor { chain -> makeHeaders(chain, headers) }
        addNetworkInterceptor(makeRetrofitLoggingInterceptor())
        makeLoggingInterceptor()?.run { addInterceptor(this) }
        build()
    }

    private fun makeHeaders(
        chain: Interceptor.Chain,
        headers: List<Pair<String, String>>,
    ): Response {
        val requestBuilder = chain.request().newBuilder()
        headers.forEach { header ->
            requestBuilder.addHeader(header.first, header.second)
        }
        return chain.proceed(requestBuilder.build())
    }

    private fun makeLoggingInterceptor(): Interceptor? {
        return if (BuildConfig.DEBUG) LoggingInterceptor() else null
    }

    private fun makeRetrofitLoggingInterceptor(): HttpLoggingInterceptor {
        val logging = HttpLoggingInterceptor()
        logging.level = if (BuildConfig.DEBUG) {
            HttpLoggingInterceptor.Level.BODY
        } else {
            HttpLoggingInterceptor.Level.NONE
        }
        return logging
    }

    fun getAuthApi(): AuthApi = getApi(AuthApi::class.java, jsonHeader())

    fun getTripApi(): TripApi = getApi(TripApi::class.java, jsonHeader())

    fun getUserApi(): UserApi = getApi(UserApi::class.java, jsonHeader())

    fun getChatsApi(): ChatsApi = getApi(ChatsApi::class.java, jsonHeader())

    fun getParcelApi(): ParcelApi = getApi(ParcelApi::class.java, jsonHeader())

    fun getGoogleMapsApi(): GoogleApi = getApi(GoogleApi::class.java, jsonHeader())

    private fun <T> getApi(clazz: Class<T>, headers: List<Pair<String, String>>): T {
        return buildRetrofit(buildClient(headers), clazz)
    }
}