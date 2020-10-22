/*
 * Copyright (c) 2019 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 * Proprietary and confidential
 *
 */

package com.kolibree.android.rewards.synchronization.profilesmileshistory

import androidx.annotation.VisibleForTesting
import com.kolibree.android.rewards.feedback.HistoryEventsConsumer
import com.kolibree.android.rewards.models.SmilesHistoryEventEntity
import com.kolibree.android.rewards.persistence.RewardsSynchronizedVersions
import com.kolibree.android.rewards.persistence.SmilesHistoryEventsDao
import com.kolibree.android.synchronizator.SynchronizableReadOnlyDataStore
import com.kolibree.android.synchronizator.models.SynchronizableReadOnly
import javax.inject.Inject
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import timber.log.Timber

internal class ProfileSmilesHistorySynchronizableReadOnlyDatastore
@VisibleForTesting constructor(
    private val smilesHistoryEventsDao: SmilesHistoryEventsDao,
    private val rewardsSynchronizedVersions: RewardsSynchronizedVersions,
    private val historyEventsConsumer: HistoryEventsConsumer,
    private val smileEventsMapper: SmilesHistoryEventMapper
) : SynchronizableReadOnlyDataStore {

    @Inject
    constructor(
        smilesHistoryEventsDao: SmilesHistoryEventsDao,
        rewardsSynchronizedVersions: RewardsSynchronizedVersions,
        historyEventsConsumer: HistoryEventsConsumer
    ) : this(
        smilesHistoryEventsDao,
        rewardsSynchronizedVersions,
        historyEventsConsumer,
        SmilesHistoryEventMapper()
    )

    override fun updateVersion(newVersion: Int) =
        rewardsSynchronizedVersions.setSmilesHistoryVersion(newVersion)

    override fun replace(synchronizable: SynchronizableReadOnly) {
        (synchronizable as? ProfileSmilesHistoryApiWithProfileId)?.let {
            val entities = smileEventsMapper.map(synchronizable)
            smilesHistoryEventsDao.replace(entities)

            GlobalScope.launch {
                consumeSmilesHistoryEventForProfile(synchronizable.profileId)
            }
        } ?: Timber.e("Expected ProfileTierEntity, was %s", synchronizable)
    }

    @VisibleForTesting
    fun consumeSmilesHistoryEventForProfile(profileId: Long) {
        /*
          some specific events (like "Personal Challenge completed") needs to point to
          history event to get information about smile rewards, that's why we need to know
          history event id. To do that we can simply read all event from database
         */
        val entitiesWithIds = smilesHistoryEventsDao.read()
            .filter {
                it.profileId == profileId
            }

        historyEventsConsumer.accept(entitiesWithIds)
    }
}

@VisibleForTesting
internal class SmilesHistoryEventMapper {
    fun map(smilesHistoryApiWithProfileId: ProfileSmilesHistoryApiWithProfileId): List<SmilesHistoryEventEntity> {
        return smilesHistoryApiWithProfileId.profileSmilesHistoryApi.smilesProfileHistory.map {
            SmilesHistoryEventEntity(
                profileId = smilesHistoryApiWithProfileId.profileId,
                message = it.message,
                eventType = it.eventType,
                smiles = it.smilesRewards,
                creationTime = it.creationTime,
                challengeId = it.challengeId,
                brushingId = it.brushingId,
                brushingType = it.brushingType,
                tierLevel = it.tierLevel,
                rewardsId = it.rewardsId,
                relatedProfileId = it.relatedProfileId
            )
        }
    }
}
