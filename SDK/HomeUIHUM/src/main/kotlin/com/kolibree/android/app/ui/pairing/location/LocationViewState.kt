/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.app.ui.pairing.location

import com.kolibree.android.app.base.BaseViewState
import kotlinx.android.parcel.Parcelize

@Parcelize
internal data class LocationViewState(
    val screenType: LocationScreenType
) : BaseViewState {
    companion object {
        fun initial(screenType: LocationScreenType) = LocationViewState(screenType)
    }
}
