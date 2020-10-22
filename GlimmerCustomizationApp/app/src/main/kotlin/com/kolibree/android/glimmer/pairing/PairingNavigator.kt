/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.glimmer.pairing

import android.Manifest
import android.content.Context
import android.content.Intent
import android.provider.Settings
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContract
import androidx.activity.result.contract.ActivityResultContracts
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.kolibree.android.app.base.BaseNavigator
import com.kolibree.android.glimmer.tweaker.startTweakerActivity
import javax.inject.Inject

interface PairingNavigator {

    fun askForBluetoothPermission()

    fun askForLocationPermission()

    fun navigateToLocationSettings()

    fun navigateToTweakerActivity()
}

internal class PairingNavigatorImpl @Inject constructor() : BaseNavigator<PairingActivity>(),
    PairingNavigator {

    private lateinit var permissionContract: ActivityResultLauncher<Array<String>>

    private lateinit var locationSettingsContract: ActivityResultLauncher<Unit>

    override fun onCreate(owner: LifecycleOwner) {
        super.onCreate(owner)

        withOwner {
            setupPermissionsScreen()
            askForBluetoothPermission()
        }
    }

    override fun navigateToTweakerActivity() = withOwner {
        startTweakerActivity(this)
        finish()
    }

    override fun askForBluetoothPermission() {
        permissionContract.launch(arrayOf(Manifest.permission.BLUETOOTH_ADMIN))
    }

    override fun askForLocationPermission() {
        permissionContract.launch(arrayOf(Manifest.permission.ACCESS_FINE_LOCATION))
    }

    override fun navigateToLocationSettings() {
        locationSettingsContract.launch(Unit)
    }

    private fun PairingActivity.setupPermissionsScreen() {
        permissionContract = registerForActivityResult(
            ActivityResultContracts.RequestMultiplePermissions()
        ) { permissions ->
            permissions.forEach { (permission, isGranted) ->
                if (permission == Manifest.permission.BLUETOOTH_ADMIN) {
                    withOwner { onBluetoothPermissionState(isGranted) }
                } else if (permission == Manifest.permission.ACCESS_FINE_LOCATION) {
                    withOwner { onLocationPermissionState(isGranted) }
                }
            }
        }

        locationSettingsContract = registerForActivityResult(
            ActivityContract(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
        ) { withOwner { onLocationSettingsClosed() } }
    }

    class Factory @Inject constructor() : ViewModelProvider.Factory {

        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T =
            PairingNavigatorImpl() as T
    }
}

internal interface PairingNavigatorOwner {

    fun onBluetoothPermissionState(isGranted: Boolean)

    fun onLocationPermissionState(isGranted: Boolean)

    fun onLocationSettingsClosed()
}

private class ActivityContract(val action: String) : ActivityResultContract<Unit, Unit>() {
    override fun createIntent(context: Context, param: Unit?): Intent {
        return Intent(action)
    }

    override fun parseResult(resultCode: Int, intent: Intent?) {
        // no-op
    }
}
