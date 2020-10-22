/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.app.ui.checkup.day

import com.kolibree.android.annotation.VisibleForApp
import com.kolibree.android.app.base.BaseViewState
import com.kolibree.android.app.ui.checkup.base.BaseCheckupViewState
import com.kolibree.android.clock.TrustedClock
import com.kolibree.kml.MouthZone16
import kotlinx.android.parcel.Parcelize
import org.threeten.bp.OffsetDateTime

/** Checkup [BaseViewState] implementation */
@Parcelize
@VisibleForApp
data class DayCheckupViewState(
    override val coverage: Float?,
    override val durationPercentage: Float,
    override val durationSeconds: Long,
    override val date: OffsetDateTime,
    override val game: String?,
    val checkupData: List<Map<MouthZone16, Float>>
) : BaseCheckupViewState {

    internal companion object {

        fun initial() =
            DayCheckupViewState(
                coverage = null,
                durationPercentage = 0f,
                durationSeconds = 0L,
                date = TrustedClock.getNowOffsetDateTime(),
                game = null,
                checkupData = emptyList()
            )
    }
}
