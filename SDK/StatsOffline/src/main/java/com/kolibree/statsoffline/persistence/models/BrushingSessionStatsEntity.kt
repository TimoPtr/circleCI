/*
 * Copyright (c) 2019 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.statsoffline.persistence.models

import android.os.Parcelable
import androidx.annotation.Keep
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Ignore
import androidx.room.Index
import com.kolibree.android.extensions.toKolibreeDay
import com.kolibree.kml.MouthZone16
import com.kolibree.statsoffline.models.AverageCheckup
import com.kolibree.statsoffline.models.BrushingMotionStats
import com.kolibree.statsoffline.models.emptyAverageCheckup
import com.kolibree.statsoffline.models.validate
import java.util.Collections
import kotlinx.android.parcel.IgnoredOnParcel
import kotlinx.android.parcel.Parcelize
import org.threeten.bp.LocalDate
import org.threeten.bp.LocalDateTime

/**
 * Stats data for a brushing session
 *
 * Kolibree days start at 4AM, so [assignedDate] can have a different [LocalDate] than [creationTime]. It's either the
 * same day as [assignedDate], or the daybefore.
 *
 * The reasoning behind this is that some users that go to bed late (1AM) wanted those brushings to belong to the day
 * they have lived on, not the actual date.
 *
 * Examples
 *
 * - [creationTime] 1/10/2019 23:00 -> [assignedDate] is 01/10/2019
 * - [creationTime] 1/10/2019 02:40 -> [assignedDate] is 30/09/2019
 */
@Keep
interface StatsSession : BrushingMotionStats, Parcelable {
    val profileId: Long
    val creationTime: LocalDateTime
    val duration: Int
    val averageSurface: Int
    val averageCheckup: AverageCheckup
    val assignedDate: LocalDate
}

@Entity(
    tableName = "brushing_session_stat",
    primaryKeys = ["profileId", "creationTime"],
    foreignKeys = [ForeignKey(
        entity = DayAggregatedStatsEntity::class,
        parentColumns = arrayOf("profileId", "day"),
        childColumns = arrayOf("profileId", "assignedDate"),
        onDelete = ForeignKey.NO_ACTION,
        deferred = true
    )],
    indices = [Index(value = ["profileId", "assignedDate"])]
)
@Suppress("ConstructorParameterNaming")
@Parcelize
internal data class BrushingSessionStatsEntity(
    override val profileId: Long,
    /*
    User's date time at which the brushing was created. Deliberately ignore timezone.
     */
    override val creationTime: LocalDateTime,
    override val duration: Int,
    override val averageSurface: Int,
    val _averageCheckupMap: AverageCheckup = emptyAverageCheckup(),
    /**
     * [creationTime] transformed to [Kolibree Day][com.kolibree.android.extensions.toKolibreeDay]
     *
     * Possible values are the same [LocalDate] as [creationTime] or one day before [creationTime]
     */
    override val assignedDate: LocalDate = creationTime.toKolibreeDay(),
    val cleanPercent: Int? = null,
    val missedPercent: Int? = null,
    val plaqueLeftPercent: Int? = null,
    val plaqueAggregate: Map<MouthZone16, StatsPlaqueAggregate>? = null,
    override val correctMovementAverage: Double = 0.0,
    override val underSpeedAverage: Double = 0.0,
    override val correctSpeedAverage: Double = 0.0,
    override val overSpeedAverage: Double = 0.0,
    override val correctOrientationAverage: Double = 0.0,
    override val overPressureAverage: Double = 0.0
) : StatsSession {
    init {
        _averageCheckupMap.validate()
    }

    @Ignore
    @IgnoredOnParcel
    override val averageCheckup: AverageCheckup = Collections.unmodifiableMap(_averageCheckupMap)
}
