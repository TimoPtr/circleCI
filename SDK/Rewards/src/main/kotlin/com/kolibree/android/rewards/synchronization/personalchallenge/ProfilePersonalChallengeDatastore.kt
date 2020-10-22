/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.rewards.synchronization.personalchallenge

import com.kolibree.android.rewards.persistence.RewardsSynchronizedVersions
import com.kolibree.android.rewards.personalchallenge.data.persistence.PersonalChallengeDao
import com.kolibree.android.rewards.synchronization.personalchallenge.mapper.toPersistentEntity
import com.kolibree.android.rewards.synchronization.personalchallenge.mapper.toSynchronizableItem
import com.kolibree.android.rewards.synchronization.personalchallenge.model.ProfilePersonalChallengeSynchronizableItem
import com.kolibree.android.synchronizator.DataStoreId
import com.kolibree.android.synchronizator.SynchronizableItemDataStore
import com.kolibree.android.synchronizator.models.SynchronizableItem
import java.util.UUID
import javax.inject.Inject

internal class ProfilePersonalChallengeDatastore @Inject constructor(
    private val dao: PersonalChallengeDao,
    private val rewardsSynchronizedVersions: RewardsSynchronizedVersions
) : SynchronizableItemDataStore {

    override fun insert(synchronizable: SynchronizableItem): SynchronizableItem =
        when (synchronizable) {
            is ProfilePersonalChallengeSynchronizableItem -> insertInternal(synchronizable)
            else -> throw IllegalArgumentException("Cannot insert $synchronizable into PersonalChallengeDao")
        }

    private fun insertInternal(item: ProfilePersonalChallengeSynchronizableItem): SynchronizableItem {
        val insertedEntity = dao.replace(item.toPersistentEntity())
        return insertedEntity.toSynchronizableItem()
    }

    /**
     * Kolibree ID is profile ID in case of personal challenge
     * @see [ProfilePersonalChallengeSynchronizableItem.profileId]
     */
    override fun getByKolibreeId(kolibreeId: DataStoreId): SynchronizableItem? =
        dao.getChallengeForProfile(kolibreeId)?.toSynchronizableItem()

    override fun getByUuid(uuid: UUID): SynchronizableItem =
        dao.getByUuid(uuid).toSynchronizableItem()

    override fun delete(uuid: UUID) = dao.delete(uuid)

    override fun canHandle(synchronizable: SynchronizableItem): Boolean =
        synchronizable is ProfilePersonalChallengeSynchronizableItem

    override fun updateVersion(newVersion: Int) =
        rewardsSynchronizedVersions.setPersonalChallengeVersion(newVersion)
}
