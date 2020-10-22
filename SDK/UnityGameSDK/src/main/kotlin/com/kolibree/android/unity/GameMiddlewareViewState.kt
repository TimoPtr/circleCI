/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.unity

import androidx.annotation.Keep
import com.kolibree.android.app.ui.activity.mvi.UnityGameViewState
import kotlinx.android.parcel.Parcelize

@Parcelize
@Keep
data class GameMiddlewareViewState(override val progressVisible: Boolean) :
    UnityGameViewState {
    internal companion object {
        fun initial(): GameMiddlewareViewState =
            GameMiddlewareViewState(progressVisible = true)
    }
}
