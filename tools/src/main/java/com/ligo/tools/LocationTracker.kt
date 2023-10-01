package com.ligo.tools

import android.content.Context
import com.ligo.core.checkServiceRunning
import com.ligo.core.printError
import com.ligo.data.model.Location
import com.ligo.data.preferences.app.IAppPreferences
import com.ligo.data.repo.user.IUserRepo
import com.ligo.google.api.ILocationManager
import com.ligo.tools.api.ILocationTracker
import io.reactivex.rxjava3.disposables.CompositeDisposable
import io.reactivex.rxjava3.schedulers.Schedulers

internal class LocationTracker(
    private val context: Context,
    private val userRepo: IUserRepo,
    private val appPreferences: IAppPreferences,
    private val locationManager: ILocationManager,
) : ILocationTracker {

    private val locationDisposable = CompositeDisposable()

    override fun startLocationTracking() {
        val userId = appPreferences.getUser()?._id ?: return
        locationManager.getLocationUpdatesObservable()
            .flatMapCompletable {
                userRepo.updateLocation(userId, Location(it.latitude, it.longitude))
                    .subscribeOn(Schedulers.io())
                    .onErrorComplete()
            }
            .subscribe({}, ::printError)
            .also(locationDisposable::add)

        locationManager.requestLocationUpdates()

        context.startForegroundService(LocationTrackerService.getIntent(context))
    }

    override fun stopLocationTracking() {
        locationDisposable.clear()
        locationManager.cancelLocationUpdates()
        if (isLocationServiceRunning()) {
            context.stopService(LocationTrackerService.getIntent(context))
        }
    }

    override fun isLocationServiceRunning(): Boolean {
        return checkServiceRunning(context, LocationTrackerService::class.java)
    }
}