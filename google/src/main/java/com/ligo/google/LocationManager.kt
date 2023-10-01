package com.ligo.google

import android.annotation.SuppressLint
import android.content.Context
import android.location.Address
import android.location.Geocoder
import android.location.Location
import android.os.Build
import android.os.Looper
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.Priority
import com.ligo.core.BuildConfig
import com.ligo.google.api.ILocationManager
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.core.Single
import io.reactivex.rxjava3.schedulers.Schedulers
import io.reactivex.rxjava3.subjects.PublishSubject
import java.util.Locale
import java.util.Optional
import com.ligo.data.model.Location as LocationModel

@SuppressLint("MissingPermission")
internal class LocationManager(
    private val context: Context,
    private val fusedLocationClient: FusedLocationProviderClient,
) : ILocationManager {

    companion object {
        private val LOCATION_UPDATES_INTERVAL = if (BuildConfig.DEBUG) 60_000L else 120_000L
    }

    private var location: Location? = null

    private val locationSubject = PublishSubject.create<Location>().toSerialized()
    private var locationCallback: LocationCallback = createLocationCallback { result ->
        result.lastLocation?.apply {
            locationSubject.onNext(this)
        }
    }

    override fun fetchLocationInfo(
        latitude: Double,
        longitude: Double,
    ): Single<Optional<LocationModel>> {
        return fetchResult(latitude, longitude)
            .map {
                val result = it.firstOrNull()
                if (result != null) {
                    val cityName = result.locality
                    val address = result.getAddressLine(0)
                    val name = if (cityName.isNullOrEmpty()) {
                        result.countryName
                    } else {
                        cityName + ", " + result.countryName
                    }
                    Optional.of(LocationModel(latitude, longitude, cityName, address, name))
                } else {
                    Optional.ofNullable(null)
                }
            }
    }

    private fun fetchResult(latitude: Double, longitude: Double): Single<List<Address>> {
        val geocoder = Geocoder(context, Locale.getDefault())
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            Single.create {
                geocoder.getFromLocation(latitude, longitude, 1) { result ->
                    it.onSuccess(result)
                }
            }
        } else {
            val result = geocoder.getFromLocation(latitude, longitude, 1)
            Single.just(result ?: listOf())
        }
            .subscribeOn(Schedulers.io())
    }

    override fun getLastKnownLocation(): Location? {
        return location
    }

    override fun fetchLastKnownLocation(listener: (Location) -> Unit) {
        fusedLocationClient.lastLocation.addOnSuccessListener {
            if (it != null) {
                location = it
                listener.invoke(it)
            }
        }
    }

    override fun requestLocationUpdates() {
        val locationRequest = LocationRequest.Builder(
            Priority.PRIORITY_HIGH_ACCURACY,
            LOCATION_UPDATES_INTERVAL
        ).setWaitForAccurateLocation(false)
            .setMinUpdateIntervalMillis(LOCATION_UPDATES_INTERVAL / 4)
            .setMaxUpdateDelayMillis(LOCATION_UPDATES_INTERVAL).build()

        fusedLocationClient.removeLocationUpdates(locationCallback)
        fusedLocationClient.requestLocationUpdates(
            locationRequest,
            locationCallback,
            Looper.getMainLooper()
        )
    }

    override fun cancelLocationUpdates() {
        fusedLocationClient.removeLocationUpdates(locationCallback)
    }

    private fun createLocationCallback(data: (LocationResult) -> Unit): LocationCallback {
        return object : LocationCallback() {
            override fun onLocationResult(result: LocationResult) {
                data.invoke(result)
            }
        }
    }

    override fun getLocationUpdatesObservable(): Observable<Location> {
        return locationSubject
    }
}