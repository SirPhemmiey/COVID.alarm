package de.sladkin.corona.repository

import android.annotation.SuppressLint
import android.location.Criteria
import android.location.LocationManager
import de.sladkin.corona.model.LatLng

class LocationRepository(private val locationManager: LocationManager) {

    @SuppressLint("MissingPermission")
    fun geLocation(): LatLng? {
        val provider = locationManager.getBestProvider(Criteria(), false)
        return provider?.let {
            locationManager.getLastKnownLocation(provider)
                ?.let { loc -> LatLng(loc.latitude, loc.longitude, System.currentTimeMillis()) }
        }
    }

}