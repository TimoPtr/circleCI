/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.app.ui.home

import com.kolibree.android.accountinternal.profile.models.Profile
import com.kolibree.android.annotation.VisibleForApp
import com.kolibree.android.app.base.BaseViewState
import com.kolibree.android.app.widget.snackbar.SnackbarConfiguration
import com.kolibree.android.homeui.hum.R
import kotlinx.android.parcel.Parcelize

@Parcelize
@VisibleForApp
data class HomeScreenViewState(
    val currentProfile: Profile,
    val selectedTabId: Int = R.id.bottom_navigation_home,
    val snackbarConfiguration: SnackbarConfiguration = SnackbarConfiguration()
) : BaseViewState {

    @VisibleForApp
    companion object {
        fun initial(profile: Profile) = HomeScreenViewState(profile)
    }
}
