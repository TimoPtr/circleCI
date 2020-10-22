/*
 * Copyright (c) 2019 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.bttester.main

import com.kolibree.android.app.base.BaseViewState
import kotlinx.android.parcel.Parcelize

@Parcelize
internal data class MainActivityViewState(private val enabled: Boolean = true) : BaseViewState {
    companion object {
        fun initial() = MainActivityViewState()
    }
}
