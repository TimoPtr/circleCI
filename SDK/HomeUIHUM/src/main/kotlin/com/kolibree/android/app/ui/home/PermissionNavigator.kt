/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.app.ui.home

import com.kolibree.android.annotation.VisibleForApp

@VisibleForApp
interface PermissionNavigator {
    fun launchBluetoothPermission()

    fun launchLocationPermission()

    fun navigateToLocationSettings()
}
