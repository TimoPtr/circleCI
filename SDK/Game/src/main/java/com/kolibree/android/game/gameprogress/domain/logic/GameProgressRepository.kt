/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.game.gameprogress.domain.logic

import androidx.annotation.VisibleForTesting
import com.kolibree.android.annotation.VisibleForApp
import com.kolibree.android.clock.TrustedClock
import com.kolibree.android.game.gameprogress.data.persistence.GameProgressDao
import com.kolibree.android.game.gameprogress.data.persistence.model.GameProgressEntity
import com.kolibree.android.game.gameprogress.domain.model.GameProgress
import com.kolibree.android.game.synchronization.gameprogress.model.ProfileGameProgressSynchronizableItem
import com.kolibree.android.synchronizator.Synchronizator
import com.kolibree.android.synchronizator.models.SynchronizableItem
import io.reactivex.Completable
import io.reactivex.Single
import java.util.UUID
import javax.inject.Inject

@VisibleForApp
interface GameProgressRepository {
    fun saveProgress(profileId: Long, gameId: String, progress: String): Completable
    fun getProgress(profileId: Long, gameId: String): GameProgress
}

internal class GameProgressRepositoryImpl @Inject constructor(
    private val dao: GameProgressDao,
    private val synchronizator: Synchronizator
) : GameProgressRepository {

    override fun saveProgress(profileId: Long, gameId: String, progress: String): Completable =
        Single.defer {
            val gameProgress = GameProgress(gameId, progress, TrustedClock.getNowZonedDateTimeUTC())

            updateGameProgress(profileId, gameProgress)
        }.flatMapCompletable { synchronizator.synchronizeCompletable() }

    override fun getProgress(profileId: Long, gameId: String): GameProgress =
        dao.getGameProgressEntityForProfileAndGame(profileId, gameId)?.toGameProgress() ?: GameProgress(
            gameId,
            "",
            TrustedClock.getNowZonedDateTimeUTC()
        )

    @VisibleForTesting
    fun updateGameProgress(
        profileId: Long,
        newProgress: GameProgress
    ): Single<SynchronizableItem> {
        val currentEntities = dao.getGameProgressEntitiesForProfile(profileId)

        val updatedGameProgress = getUpdatedDomainGameProgress(currentEntities, newProgress)

        val uuid = currentEntities.firstOrNull { it.uuid != null }?.uuid

        val newItem = pendingProfileGameProgress(
            profileId,
            updatedGameProgress,
            uuid
        )

        // if the uuid is null it mean that the profile have no progress at all
        // in that case we need to call create instead of update in order to create a uuid
        return if (uuid != null) {
            synchronizator.update(newItem)
        } else {
            synchronizator.create(newItem)
        }
    }

    @VisibleForTesting
    fun pendingProfileGameProgress(
        profileId: Long,
        gameProgress: List<GameProgress>,
        uuid: UUID?
    ): SynchronizableItem = ProfileGameProgressSynchronizableItem(
        kolibreeId = profileId,
        gameProgress = gameProgress,
        uuid = uuid
    )

    private fun getUpdatedDomainGameProgress(
        currentEntities: List<GameProgressEntity>,
        newProgress: GameProgress
    ): List<GameProgress> {
        val existingGameProgressForProfile = currentEntities.map { it.toGameProgress() }

        return existingGameProgressForProfile.filterNot { it.gameId == newProgress.gameId }.toMutableList()
            .apply { add(newProgress) }
    }
}
