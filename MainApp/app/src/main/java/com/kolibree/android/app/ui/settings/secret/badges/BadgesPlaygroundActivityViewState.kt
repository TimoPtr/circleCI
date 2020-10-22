/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.app.ui.settings.secret.badges

import com.kolibree.android.app.base.BaseViewState
import com.kolibree.android.app.ui.widget.ZoneProgressData
import kotlinx.android.parcel.Parcelize

@Suppress("MagicNumber")
@Parcelize
internal data class BadgesPlaygroundActivityViewState(
    val zones16: ZoneProgressData = ZoneProgressData.create(16),
    val zones8: ZoneProgressData = ZoneProgressData.create(8),
    val zones4: ZoneProgressData = ZoneProgressData.create(4)
) :
    BaseViewState {
    companion object {
        fun initial() = BadgesPlaygroundActivityViewState()
    }
}
