/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.game.gameprogress.data.persistence

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.TypeConverters
import com.kolibree.android.commons.interfaces.Truncable
import com.kolibree.android.game.gameprogress.data.persistence.model.GameProgressEntity
import com.kolibree.android.synchronizator.data.database.UuidConverters
import io.reactivex.Completable
import java.util.UUID

@Dao
internal abstract class GameProgressDao : Truncable {

    @Query("SELECT * FROM game_progress WHERE profileId = :profileId AND gameId = :gameId")
    abstract fun getGameProgressEntityForProfileAndGame(profileId: Long, gameId: String): GameProgressEntity?

    @Query("SELECT * FROM game_progress WHERE profileId = :profileId")
    abstract fun getGameProgressEntitiesForProfile(profileId: Long): List<GameProgressEntity>

    @Query("SELECT * FROM game_progress WHERE uuid = :uuid")
    @TypeConverters(UuidConverters::class)
    abstract fun getEntitiesByUuid(uuid: UUID): List<GameProgressEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract fun insertEntity(entity: List<GameProgressEntity>)

    @Transaction
    open fun replaceEntities(profileId: Long, entities: List<GameProgressEntity>) {
        truncateForProfile(profileId)
        insertEntity(entities)
    }

    @Query("DELETE FROM game_progress WHERE profileId = :profileId AND gameId = :gameId")
    abstract fun truncateForGameAndGame(profileId: Long, gameId: String)

    @Query("DELETE FROM game_progress WHERE profileId = :profileId")
    abstract fun truncateForProfile(profileId: Long)

    @Query("DELETE FROM game_progress WHERE uuid = :uuid")
    @TypeConverters(UuidConverters::class)
    abstract fun truncateForUuid(uuid: UUID)

    @Query("DELETE FROM game_progress")
    abstract override fun truncate(): Completable
}
