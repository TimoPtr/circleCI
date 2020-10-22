/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.rewards.synchronization.personalchallenge.model

import androidx.annotation.Keep
import com.kolibree.android.clock.TrustedClock
import com.kolibree.android.rewards.personalchallenge.domain.model.V1PersonalChallenge
import com.kolibree.android.synchronizator.DataStoreId
import com.kolibree.android.synchronizator.models.SynchronizableItem
import java.util.UUID
import org.threeten.bp.ZonedDateTime

@Keep
internal data class ProfilePersonalChallengeSynchronizableItem(
    val backendId: Long?,
    override val kolibreeId: Long,
    val challenge: V1PersonalChallenge,
    override val updatedAt: ZonedDateTime = TrustedClock.getNowZonedDateTimeUTC(),
    override val uuid: UUID? = null
) : SynchronizableItem {

    /*
     * Kolibree ID on the backend side is an unique identifier of the resource.
     * In case of personal challenge, this ID is actually a profile ID (as user
     * can have only 1 challenge set at the same time and this is how the API
     * is structured.
     */
    val profileId: DataStoreId = kolibreeId

    override val createdAt: ZonedDateTime = challenge.creationDate

    override fun withUpdatedAt(updatedAt: ZonedDateTime): SynchronizableItem =
        copy(updatedAt = updatedAt)

    override fun withUuid(uuid: UUID): SynchronizableItem = copy(uuid = uuid)

    override fun withKolibreeId(kolibreeId: DataStoreId): SynchronizableItem =
        copy(kolibreeId = kolibreeId)

    /**
     * This class does not hold the entity PK, so we can't update the remote item with the local
     * primary key
     *
     * See parent documentation for context
     *
     * @return [SynchronizableItem] same instance on which the method was invoked
     */
    override fun updateFromLocalInstance(localItem: SynchronizableItem): SynchronizableItem = this
}
