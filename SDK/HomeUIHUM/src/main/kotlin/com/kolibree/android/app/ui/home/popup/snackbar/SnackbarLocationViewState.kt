/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.app.ui.home.popup.snackbar

import com.kolibree.android.annotation.VisibleForApp
import com.kolibree.android.app.base.BaseViewState
import com.kolibree.android.app.ui.home.popup.snackbar.LocationState.Disabled
import com.kolibree.android.app.ui.home.popup.snackbar.LocationState.Enabled
import com.kolibree.android.app.ui.home.popup.snackbar.LocationState.PermissionDenied
import com.kolibree.android.app.ui.home.popup.snackbar.LocationState.Unknown
import kotlinx.android.parcel.Parcelize

@VisibleForApp
@Parcelize
data class LocationPopupViewState(
    val locationState: LocationState = Unknown
) : BaseViewState {

    fun withPermissionDenied(): LocationPopupViewState =
        copy(locationState = PermissionDenied)

    fun withLocationDisabled(): LocationPopupViewState =
        copy(locationState = Disabled)

    fun withLocationEnabled(): LocationPopupViewState =
        copy(locationState = Enabled)

    fun withCurrentStateUnknown(): LocationPopupViewState =
        copy(locationState = Unknown)

    @VisibleForApp
    companion object {
        fun initial() = LocationPopupViewState()
    }
}

@VisibleForApp
enum class LocationState {
    Unknown, Disabled, Enabled, PermissionDenied
}
