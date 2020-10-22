/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.game.synchronization.gameprogress.model

import androidx.annotation.Keep
import com.kolibree.android.clock.TrustedClock
import com.kolibree.android.game.gameprogress.domain.model.GameProgress
import com.kolibree.android.synchronizator.DataStoreId
import com.kolibree.android.synchronizator.models.SynchronizableItem
import java.util.UUID
import org.threeten.bp.ZonedDateTime

@Keep
internal data class ProfileGameProgressSynchronizableItem(
    override val kolibreeId: Long,
    val gameProgress: List<GameProgress>,
    @Deprecated(
        "You should not use this field it's not updated base on the backend",
        ReplaceWith("use the one from each gameProgress"),
        DeprecationLevel.ERROR
    ) override val updatedAt: ZonedDateTime = TrustedClock.getNowZonedDateTimeUTC(),
    @Deprecated(
        "This field is irrelevant here",
        ReplaceWith(""),
        DeprecationLevel.ERROR
    ) override val createdAt: ZonedDateTime = TrustedClock.getNowZonedDateTimeUTC(),
    override val uuid: UUID? = null
) : SynchronizableItem {

    /*
    * Kolibree ID on the backend side is an unique identifier of the resource.
    * In case of game progress, this ID is actually a profile ID
    */
    val profileId: DataStoreId = kolibreeId

    override fun withUpdatedAt(updatedAt: ZonedDateTime): SynchronizableItem =
        copy(updatedAt = updatedAt)

    override fun withUuid(uuid: UUID): SynchronizableItem = copy(uuid = uuid)

    override fun withKolibreeId(kolibreeId: DataStoreId): SynchronizableItem =
        copy(kolibreeId = kolibreeId)

    override fun updateFromLocalInstance(localItem: SynchronizableItem): SynchronizableItem = this
}
