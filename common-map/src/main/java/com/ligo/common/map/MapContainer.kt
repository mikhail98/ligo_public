package com.ligo.common.map

import android.annotation.SuppressLint
import android.os.Bundle
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ProcessLifecycleOwner
import com.google.android.gms.maps.CameraUpdate
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.MapView
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.google.android.gms.maps.model.MapStyleOptions
import com.google.android.gms.maps.model.PolylineOptions
import com.ligo.data.model.Parcel
import com.ligo.data.model.Trip
import com.ligo.core.R as CoreR

@SuppressLint("MissingPermission")
interface MapContainer : LifecycleEventObserver {

    val isMyLocationEnabled: Boolean

    var googleMap: GoogleMap?
    var mapView: MapView?

    fun setupMap(
        savedInstanceState: Bundle?,
        mapView: MapView,
        callback: ((GoogleMap) -> Unit)? = null,
    ) {
        val context = mapView.context
        this.mapView = mapView
        mapView.onCreate(savedInstanceState)
        mapView.getMapAsync { map ->
            googleMap = map

            map.uiSettings.setAllGesturesEnabled(false)
            map.isMyLocationEnabled = isMyLocationEnabled
            map.uiSettings.isMyLocationButtonEnabled = false
            map.setMapStyle(MapStyleOptions.loadRawResourceStyle(context, R.raw.map_night_mode))
            map.setOnMarkerClickListener { true }
            callback?.invoke(map)
        }

        ProcessLifecycleOwner.get().lifecycle.addObserver(this)
    }

    fun handleRoute(route: List<LatLng>) {
        val line = PolylineOptions()
        line.width(8f)
        line.color(ContextCompat.getColor(mapView?.context ?: return, CoreR.color.accent))
        line.addAll(route)

        googleMap?.addPolyline(line)
        val boundsBuilder = LatLngBounds.builder()
        route.forEach(boundsBuilder::include)
        googleMap?.animateCamera(CameraUpdateFactory.newLatLngBounds(boundsBuilder.build(), 72))
    }

    override fun onStateChanged(source: LifecycleOwner, event: Lifecycle.Event) {
        when (event) {
            Lifecycle.Event.ON_START -> mapView?.onStart()
            Lifecycle.Event.ON_RESUME -> mapView?.onResume()
            Lifecycle.Event.ON_PAUSE -> mapView?.onPause()
            Lifecycle.Event.ON_STOP -> mapView?.onStop()
            Lifecycle.Event.ON_DESTROY -> mapView?.onDestroy()
            else -> Unit
        }
    }

    fun getCameraUpdateFactory(parcel: Parcel): CameraUpdate {
        val startPoint = parcel.startPoint
        val endPoint = parcel.endPoint
        val bounds = LatLngBounds.builder()
            .include(LatLng(startPoint.latitude, startPoint.longitude))
            .include(LatLng(endPoint.latitude, endPoint.longitude))
            .build()
        return CameraUpdateFactory.newLatLngBounds(bounds, 72)
    }

    fun getCameraUpdateFactory(trip: Trip): CameraUpdate {
        val startPoint = trip.startPoint
        val endPoint = trip.endPoint
        val bounds = LatLngBounds.builder()
            .include(LatLng(startPoint.latitude, startPoint.longitude))
            .include(LatLng(endPoint.latitude, endPoint.longitude))
            .build()
        return CameraUpdateFactory.newLatLngBounds(bounds, 72)
    }
}