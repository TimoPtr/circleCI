/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.game.gameprogress.data.persistence.model

import androidx.annotation.Keep
import androidx.room.Entity
import androidx.room.TypeConverters
import com.kolibree.android.game.gameprogress.domain.model.GameProgress
import com.kolibree.android.room.DateConvertersString
import com.kolibree.android.synchronizator.data.database.UuidConverters
import java.util.UUID
import org.threeten.bp.ZonedDateTime

@Keep
@Entity(tableName = "game_progress", primaryKeys = ["profileId", "gameId"])
@TypeConverters(DateConvertersString::class, UuidConverters::class)
internal data class GameProgressEntity(
    val profileId: Long,
    val gameId: String,
    val progress: String,
    val updateDate: ZonedDateTime,
    val uuid: UUID? = null // should be the same for each profileID
) {
    fun toGameProgress(): GameProgress = GameProgress(gameId, progress, updateDate)
}
