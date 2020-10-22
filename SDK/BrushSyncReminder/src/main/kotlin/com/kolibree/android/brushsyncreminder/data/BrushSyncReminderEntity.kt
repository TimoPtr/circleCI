/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.brushsyncreminder.data

import androidx.annotation.Keep
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import com.kolibree.android.clock.TrustedClock
import com.kolibree.android.room.ZoneOffsetConverter
import com.kolibree.android.synchronizator.data.database.UuidConverters
import java.util.UUID
import org.threeten.bp.Instant
import org.threeten.bp.OffsetDateTime
import org.threeten.bp.ZoneOffset

@Keep
@Entity(tableName = "brush_sync_reminder")
@TypeConverters(ZoneOffsetConverter::class, UuidConverters::class)
internal data class BrushSyncReminderEntity(
    @PrimaryKey
    @ColumnInfo(name = "profile_id") val profileId: Long,
    @ColumnInfo(name = "uuid") val uuid: UUID?,
    @ColumnInfo(name = "is_enabled") val isEnabled: Boolean,
    @ColumnInfo(name = "created_at_timestamp") val createdAtTimestamp: Long,
    @ColumnInfo(name = "created_at_zone_offset") val createdAtZoneOffset: ZoneOffset,
    @ColumnInfo(name = "updated_at_timestamp") val updatedAtTimestamp: Long,
    @ColumnInfo(name = "updated_at_zone_offset") val updatedAtZoneOffset: ZoneOffset,
    @ColumnInfo(name = "reminder_date_timezone") val reminderDateTimestamp: Long,
    @ColumnInfo(name = "reminder_date_zone_offset") val reminderDateZoneOffset: ZoneOffset

) {
    val createdAt: OffsetDateTime
        get() = OffsetDateTime.ofInstant(
            Instant.ofEpochSecond(createdAtTimestamp),
            createdAtZoneOffset
        )

    val updatedAt: OffsetDateTime
        get() = OffsetDateTime.ofInstant(
            Instant.ofEpochSecond(updatedAtTimestamp),
            updatedAtZoneOffset
        )

    val reminderDate: OffsetDateTime
        get() = OffsetDateTime.ofInstant(
            Instant.ofEpochSecond(reminderDateTimestamp),
            reminderDateZoneOffset
        )

    fun updatedNow() = copy(
        updatedAtTimestamp = TrustedClock.getNowInstant().epochSecond,
        updatedAtZoneOffset = TrustedClock.systemZoneOffset
    )

    internal companion object {
        fun new(profileId: Long): BrushSyncReminderEntity {
            val epochSecond = TrustedClock.getNowInstant().epochSecond
            val zoneOffset = TrustedClock.systemZoneOffset
            return BrushSyncReminderEntity(
                profileId = profileId,
                uuid = null,
                isEnabled = false,
                createdAtTimestamp = epochSecond,
                createdAtZoneOffset = zoneOffset,
                updatedAtTimestamp = epochSecond,
                updatedAtZoneOffset = zoneOffset,
                reminderDateTimestamp = 0L,
                reminderDateZoneOffset = zoneOffset
            )
        }
    }
}
