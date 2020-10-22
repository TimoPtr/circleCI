/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.app.ui.pairing.location

import android.os.Parcelable
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import com.kolibree.android.homeui.hum.R
import kotlinx.android.parcel.Parcelize

@Parcelize
internal enum class LocationScreenType(
    @StringRes val title: Int,
    @StringRes val description: Int,
    @StringRes val action: Int,
    @DrawableRes val icon: Int
) : Parcelable {
    GrantLocationPermission(
        title = R.string.pairing_grant_location_permission_title,
        description = R.string.pairing_grant_location_permission_description,
        action = R.string.pairing_grant_location_permission_action,
        icon = R.drawable.ic_location
    ),
    EnableLocation(
        title = R.string.pairing_enable_location_title,
        description = R.string.pairing_enable_location_description,
        action = R.string.pairing_enable_location_action,
        icon = R.drawable.ic_location_disabled
    )
}
