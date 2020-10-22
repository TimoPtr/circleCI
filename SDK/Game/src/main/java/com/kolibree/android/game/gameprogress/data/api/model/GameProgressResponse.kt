/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.game.gameprogress.data.api.model

import com.google.gson.annotations.SerializedName
import com.kolibree.android.clock.TrustedClock
import com.kolibree.android.game.gameprogress.domain.model.GameProgress
import org.threeten.bp.ZonedDateTime

internal data class GameProgressResponse(
    @SerializedName("game_id") val gameId: String,
    @SerializedName("progress") val progress: String,
    @SerializedName("updated_at") val updatedAt: ZonedDateTime?
) {
    fun toDomainGameProgress(): GameProgress =
        GameProgress(gameId, progress, updatedAt ?: TrustedClock.getNowZonedDateTimeUTC())
}
