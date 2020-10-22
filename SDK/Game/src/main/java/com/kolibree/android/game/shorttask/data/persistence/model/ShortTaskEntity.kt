/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.game.shorttask.data.persistence.model

import androidx.annotation.Keep
import androidx.annotation.VisibleForTesting
import androidx.room.Entity
import androidx.room.TypeConverters
import com.kolibree.android.clock.TrustedClock
import com.kolibree.android.commons.ShortTask
import com.kolibree.android.room.ZoneOffsetConverter
import com.kolibree.android.synchronizator.data.database.UuidConverters
import java.util.UUID
import org.threeten.bp.Instant
import org.threeten.bp.OffsetDateTime
import org.threeten.bp.ZoneOffset

@Keep
@Entity(tableName = "short_tasks", primaryKeys = ["profileId", "creationTimestamp"])
@TypeConverters(ZoneOffsetConverter::class, UuidConverters::class, ShortTaskConverters::class)
internal data class ShortTaskEntity @VisibleForTesting constructor(
    val profileId: Long,
    val shortTask: ShortTask,
    val creationTimestamp: Long, // in seconds
    val creationZoneOffset: ZoneOffset,
    val uuid: UUID?
) {
    constructor(
        profileId: Long,
        shortTask: ShortTask,
        creationDateTime: OffsetDateTime = TrustedClock.getNowOffsetDateTime(),
        uuid: UUID? = null
    ) : this(
        profileId,
        shortTask,
        creationDateTime.toEpochSecond(),
        creationDateTime.offset,
        uuid
    )

    /**
     * Create the OffsetDateTime object from the fields store in the DB (timestamp and ZoneOffset)
     */
    val creationDateTime: OffsetDateTime
        get() = OffsetDateTime.ofInstant(Instant.ofEpochSecond(creationTimestamp), creationZoneOffset)
}
