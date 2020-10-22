/*
 * Copyright (c) 2019 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 * Proprietary and confidential
 *
 */

package com.kolibree.android.location

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.Context.LOCATION_SERVICE
import android.content.pm.PackageManager
import android.location.LocationManager
import android.os.Build
import android.provider.Settings
import androidx.annotation.Keep
import androidx.annotation.VisibleForTesting
import androidx.core.content.ContextCompat
import javax.inject.Inject
import timber.log.Timber

/**
 * Helper to determine if the device's location state is ready for scanning
 *
 * It also allows to inspect the reason why the state is not ready
 */
@Keep
interface LocationStatus {
    fun getLocationAction(): LocationAction
    fun isReadyToScan(): Boolean
    fun shouldAskPermission(): Boolean
    fun shouldEnableLocation(): Boolean
}

internal class LocationStatusImpl @Inject constructor(context: Context) : LocationStatus {
    private val context = context.applicationContext

    /**
     * Returns a EnableLocationAction
     * - RequestPermission if Location permission isn't granted
     * - EnableLocation if Location is disabled
     * - NoLocationAction if no action is required to use Location
     *
     * This invocation does not keep track of the Location status
     */
    override fun getLocationAction(): LocationAction {
        if (shouldAskPermission()) return RequestPermission
        if (shouldEnableLocation()) return EnableLocation

        return NoAction
    }

    override fun isReadyToScan(): Boolean {
        return getLocationAction().isReadyToScan
    }

    override fun shouldAskPermission(): Boolean {
        val permission = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION)
        return permission != PackageManager.PERMISSION_GRANTED
    }

    /**
     * Returns true if we need location to be enabled, false otherwise
     *
     * Starting on Android M, Bluetooth scanning requires Location enabled.
     *
     * Always returns true if below Android M
     */
    @SuppressLint("NewApi")
    override fun shouldEnableLocation(): Boolean {
        if (currentAndroidVersion() >= Build.VERSION_CODES.P) {
            return !locationManager().isLocationEnabled
        } else if (currentAndroidVersion() >= Build.VERSION_CODES.M) {
            try {
                @Suppress("DEPRECATION")
                return getLocationMode() == Settings.Secure.LOCATION_MODE_OFF
            } catch (ignored: Settings.SettingNotFoundException) {
                Timber.e("Fatal : custom android build, may lead to a permission-asking loop")
            }
        }
        return false
    }

    private fun locationManager() = context.getSystemService(LOCATION_SERVICE) as LocationManager

    @VisibleForTesting
    internal fun getLocationMode(): Int {
        return Settings.Secure.getInt(
            context.contentResolver,
            Settings.Secure.LOCATION_MODE
        )
    }

    @VisibleForTesting
    internal fun currentAndroidVersion() = Build.VERSION.SDK_INT
}
