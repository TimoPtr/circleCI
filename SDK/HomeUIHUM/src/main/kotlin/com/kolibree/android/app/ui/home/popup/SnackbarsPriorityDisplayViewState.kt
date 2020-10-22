/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.app.ui.home.popup

import androidx.annotation.StringRes
import com.kolibree.android.annotation.VisibleForApp
import com.kolibree.android.app.Error
import com.kolibree.android.app.base.BaseViewState
import com.kolibree.android.app.ui.home.popup.DisplayedItem.Bluetooth
import com.kolibree.android.app.ui.home.popup.DisplayedItem.Location
import com.kolibree.android.app.ui.home.popup.DisplayedItem.LocationPermissionError
import com.kolibree.android.app.widget.snackbar.SnackbarConfiguration
import com.kolibree.android.homeui.hum.R
import kotlinx.android.parcel.IgnoredOnParcel
import kotlinx.android.parcel.Parcelize

@VisibleForApp
@Parcelize
data class SnackbarsPriorityDisplayViewState(
    val displayedItem: DisplayedItem? = null,
    val snackbarShown: Boolean = true
) : BaseViewState {

    fun withDisplayedItem(displayedItem: DisplayedItem? = null): SnackbarsPriorityDisplayViewState {
        return copy(displayedItem = displayedItem, snackbarShown = true)
    }

    /**
     * This SnackbarConfiguration rely on [DisplayedItem], the only attributes which can changes is
     * its visibility, updated in the two-way binding LiveData
     */
    @IgnoredOnParcel
    val snackbarConfiguration: SnackbarConfiguration = when (displayedItem) {
        Location -> getSnackbarConfiguration(R.string.home_location_unavailable_snackbar)
        Bluetooth -> getSnackbarConfiguration(R.string.home_bluetooth_unavailable_snackbar)
        LocationPermissionError -> getSnackbarConfiguration(
            messageId = R.string.pairing_grant_location_permission_error,
            actionId = R.string.ok
        )
        else -> SnackbarConfiguration()
    }

    private fun getSnackbarConfiguration(
        @StringRes messageId: Int,
        @StringRes actionId: Int = R.string.home_snackbar_enable_button
    ) = SnackbarConfiguration(
        isShown = snackbarShown,
        error = Error.from(
            messageId = messageId,
            buttonTextId = actionId
        )
    )

    fun withSnackbarShown(isShown: Boolean): SnackbarsPriorityDisplayViewState {
        return copy(snackbarShown = isShown)
    }

    @VisibleForApp
    companion object {
        fun initial(): SnackbarsPriorityDisplayViewState =
            SnackbarsPriorityDisplayViewState()
    }
}

@VisibleForApp
enum class DisplayedItem {
    Location, Bluetooth, LocationPermissionError
}
