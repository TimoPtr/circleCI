/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.game.synchronization.shorttask.model

import com.kolibree.android.commons.ShortTask
import com.kolibree.android.synchronizator.DataStoreId
import com.kolibree.android.synchronizator.models.SynchronizableItem
import java.util.UUID
import org.threeten.bp.ZonedDateTime

internal data class ShortTaskSynchronizableItem(
    val shortTask: ShortTask,
    override val kolibreeId: DataStoreId,
    override val createdAt: ZonedDateTime,
    override val updatedAt: ZonedDateTime,
    override val uuid: UUID? = null
) : SynchronizableItem {

    val profileId: Long = kolibreeId

    override fun withUuid(uuid: UUID): SynchronizableItem =
        copy(uuid = uuid)

    override fun withKolibreeId(kolibreeId: DataStoreId): SynchronizableItem =
        copy(kolibreeId = kolibreeId)

    override fun withUpdatedAt(updatedAt: ZonedDateTime): SynchronizableItem =
        copy(updatedAt = updatedAt)

    override fun updateFromLocalInstance(localItem: SynchronizableItem): SynchronizableItem =
        this
}
