/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.app.ui.pairing.location

import android.content.Intent
import android.provider.Settings
import com.kolibree.android.app.base.BaseNavigator

internal class LocationNavigator : BaseNavigator<LocationFragment>() {

    fun navigateToLocationSettings() = withOwner {
        val locationSettingsIntent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
        activity?.startActivityForResult(locationSettingsIntent, LOCATION_REQUEST_CODE)
    }

    fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        when (requestCode) {
            LOCATION_REQUEST_CODE -> {
                withOwner { onLocationSettingsClose() }
            }
        }
    }
}

private const val LOCATION_REQUEST_CODE = 1020
