/*
 * Copyright (c) 2018 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 * Proprietary and confidential
 */

package com.kolibree.charts.models

import android.os.Parcelable
import androidx.annotation.Keep
import androidx.annotation.VisibleForTesting
import com.kolibree.android.processedbrushings.CheckupData
import com.kolibree.charts.persistence.models.StatInternal
import java.util.Objects
import kotlinx.android.parcel.Parcelize
import kotlinx.android.parcel.RawValue
import org.threeten.bp.Clock
import org.threeten.bp.Instant
import org.threeten.bp.OffsetDateTime

@Parcelize
@Keep
data class Stat(
    val id: Long,
    val duration: Long,
    val timestamp: Long,
    val clock: @RawValue Clock,
    private val averageBrushedSurface: Int = 0,
    val processedData: String = ""
) : Comparable<Stat>, Parcelable {

    val date: OffsetDateTime
        get() = OffsetDateTime.ofInstant(Instant.ofEpochMilli(timestamp), clock.zone)

    fun hasProcessedData(): Boolean = processedData.isNotEmpty()

    /**
     * Average brushed surface for the given Stat
     *
     * Returns 0 if it has no processedData associated
     */
    fun surface(): Int {
        if (!hasProcessedData()) return 0

        return averageBrushedSurface
    }

    override fun compareTo(other: Stat): Int {
        return when {
            timestamp > other.timestamp -> 1
            timestamp < other.timestamp -> -1
            else -> 0
        }
    }

    @VisibleForTesting
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || javaClass != other.javaClass) return false
        val that = other as Stat
        return duration == that.duration &&
            timestamp == that.timestamp &&
            processedData == that.processedData
    }

    @VisibleForTesting
    override fun hashCode(): Int {
        return Objects.hash(duration, timestamp, processedData)
    }

    companion object {
        internal fun fromStatInternal(
            stat: StatInternal,
            checkupData: CheckupData
        ) = Stat(
            id = stat.id,
            duration = stat.duration,
            timestamp = stat.timestamp,
            clock = stat.clock,
            averageBrushedSurface = checkupData.surfacePercentage,
            processedData = stat.processedData
        )
    }
}
