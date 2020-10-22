/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.app.ui.pairing.wake_your_brush

import com.kolibree.android.app.ui.pairing.PairingNavigator
import com.kolibree.android.sdk.connection.CheckConnectionPrerequisitesUseCase
import com.kolibree.android.sdk.connection.ConnectionPrerequisitesState.BluetoothDisabled
import com.kolibree.android.sdk.connection.ConnectionPrerequisitesState.ConnectionAllowed
import com.kolibree.android.sdk.connection.ConnectionPrerequisitesState.LocationPermissionNotGranted
import com.kolibree.android.sdk.connection.ConnectionPrerequisitesState.LocationServiceDisabled
import javax.inject.Inject

internal class WakeYourBrushPrerequisitesUseCase
@Inject constructor(
    private val navigator: PairingNavigator,
    private val checkConnectionPrerequisitesUseCase: CheckConnectionPrerequisitesUseCase
) {
    /**
     * Validates that we meet the prerequisites to start the Pairing Flow. Namely
     * - Bluetooth is enabled
     * - Location permissions have been granted
     * - Location is enabled
     *
     * If prerequisites aren't met, the user will be placed on the appropriate screen to enable
     * Bluetooth or Location
     *
     * @return true if prerequisites are valid, false otherwise
     */
    fun validateOrNavigate(): Boolean {
        when (checkConnectionPrerequisitesUseCase.checkConnectionPrerequisites()) {
            BluetoothDisabled -> navigator.navigateFromWakeYourBrushToEnableBluetooth()
            LocationServiceDisabled -> navigator.navigateFromWakeYourBrushToEnableLocation()
            LocationPermissionNotGranted -> navigator.navigateFromWakeYourBrushToGrantLocationPermission()
            ConnectionAllowed -> {
                return true
            }
        }

        return false
    }
}
