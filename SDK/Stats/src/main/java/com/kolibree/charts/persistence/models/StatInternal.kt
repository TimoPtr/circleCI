/*
 * Copyright (c) 2018 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 * Proprietary and confidential
 */

package com.kolibree.charts.persistence.models

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.PrimaryKey
import com.kolibree.sdkws.data.model.Brushing
import java.util.concurrent.TimeUnit
import org.threeten.bp.Clock
import org.threeten.bp.Duration

/**
 *   Created by guillaume agis on 22/5/18.
 *  * Optimized  stat data, saved in Room
 */
@Entity(tableName = "stat")
internal data class StatInternal(
    @ColumnInfo(name = "id")
    @PrimaryKey(autoGenerate = true) var id: Long = 0,
    @ColumnInfo(name = "profile_id") var profileId: Long,
    @ColumnInfo(name = "duration") var duration: Long,
    @ColumnInfo(name = "timestamp") var timestamp: Long, // TODO replace this with timestamp in seconds
    @ColumnInfo(name = "clock") var clock: Clock,
    @ColumnInfo(name = "processedData") var processedData: String = ""
) {
    @Ignore
    constructor(
        profileId: Long,
        duration: Long,
        timestamp: Long,
        clock: Clock,
        processedData: String
    ) :
        this(0, profileId, duration, timestamp, clock, processedData)

    @Ignore
    constructor(profileId: Long, duration: Long, clock: Clock, timestamp: Long) :
        this(0, profileId, duration, timestamp, clock)

    companion object {
        fun fromBrushing(
            brushing: Brushing,
            clock: Clock
        ) = StatInternal(
            profileId = brushing.profileId,
            clock = clock,
            duration = brushing.duration,
            timestamp = TimeUnit.SECONDS.toMillis(brushing.dateTime.toEpochSecond()),
            processedData = brushing.processedData ?: ""
        )
    }

    @Ignore
    val durationObject: Duration = Duration.ofSeconds(duration)
}
