/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.app.ui.settings.secret.fakebrushings

import com.kolibree.android.app.base.BaseViewState
import com.kolibree.android.clock.TrustedClock
import kotlinx.android.parcel.Parcelize
import org.threeten.bp.LocalDateTime
import org.threeten.bp.format.DateTimeFormatter

@Parcelize
internal data class CreateFakeBrushingViewState(
    val selectedGame: GameToServer,
    val createBrushingAt: LocalDateTime,
    val selectedToothbrushName: String? = null
) : BaseViewState {
    val createAtFormatted: String = formatTime(createBrushingAt)

    fun withDateTime(localDateTime: LocalDateTime): CreateFakeBrushingViewState {
        return copy(createBrushingAt = localDateTime)
    }

    companion object {
        private val formatter = DateTimeFormatter.ISO_DATE_TIME

        fun initial() = CreateFakeBrushingViewState(
            selectedGame = GameToServer.COACH_PLUS,
            createBrushingAt = TrustedClock.getNowLocalDateTime())

        private fun formatTime(localDateTime: LocalDateTime): String {
            return formatter.format(localDateTime)
        }
    }
}
