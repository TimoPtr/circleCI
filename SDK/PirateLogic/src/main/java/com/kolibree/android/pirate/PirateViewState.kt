/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.pirate

import com.kolibree.android.annotation.VisibleForApp
import com.kolibree.android.app.ui.activity.mvi.UnityGameViewState
import kotlinx.android.parcel.Parcelize

@Parcelize
@VisibleForApp
data class PirateViewState(override val progressVisible: Boolean) : UnityGameViewState {
    internal companion object {
        fun initial(): PirateViewState = PirateViewState(progressVisible = true)
    }
}
