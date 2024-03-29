package co.tcc.koga.android.service

import android.Manifest
import android.content.Context
import android.content.SharedPreferences
import android.content.pm.PackageManager
import androidx.core.app.ActivityCompat
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices

enum class ServiceState {
    STARTED,
    STOPPED,
}

enum class Actions {
    START,
    STOP
}

private fun setServiceState(name: String, key: String, context: Context, state: ServiceState) {
    val sharedPrefs = getPreferences(name, context)
    sharedPrefs.edit().let {
        it.putString(key, state.name)
        it.apply()
    }
}

fun setPTTSerivceState(context: Context, state: ServiceState) {
    setServiceState("PTT_SERVICE", "PTT_KEY", context, state)
}

fun setLocationServiceState(context: Context, state: ServiceState) {
    setServiceState("LOCATION_SERVICE", "LOCATION_KEY", context, state)
}

fun requestLocationPermission(context: Context): Boolean {
    if (ActivityCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
    ) {
        val mLocationRequest = LocationRequest.create()
        mLocationRequest.interval = 5000
        mLocationRequest.fastestInterval = 5000
        mLocationRequest.priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        val mLocationCallback: LocationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
            }
        }
        LocationServices.getFusedLocationProviderClient(context)
            .requestLocationUpdates(mLocationRequest, mLocationCallback, null)
        return true
    }

    return false
}

private fun getPreferences(name: String, context: Context): SharedPreferences {
    return context.getSharedPreferences(name, 0)
}