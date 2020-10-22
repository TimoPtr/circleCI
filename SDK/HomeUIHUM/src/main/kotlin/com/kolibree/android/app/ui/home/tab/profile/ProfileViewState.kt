/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.app.ui.home.tab.profile

import com.kolibree.android.app.base.BaseViewState
import kotlinx.android.parcel.Parcelize

@Parcelize
internal data class ProfileViewState(
    val avatarUrl: String? = null,
    val firstName: String? = null
) : BaseViewState {

    companion object {
        fun initial() = ProfileViewState()
    }
}
