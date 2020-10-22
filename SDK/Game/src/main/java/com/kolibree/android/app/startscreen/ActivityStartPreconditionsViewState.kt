/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.app.startscreen

import com.kolibree.android.annotation.VisibleForApp
import com.kolibree.android.app.base.BaseViewState
import kotlinx.android.parcel.Parcelize

@Parcelize
@VisibleForApp
data class ActivityStartPreconditionsViewState internal constructor(
    val canStart: Boolean = false
) : BaseViewState {

    internal companion object {

        fun initial() = ActivityStartPreconditionsViewState()
    }
}
