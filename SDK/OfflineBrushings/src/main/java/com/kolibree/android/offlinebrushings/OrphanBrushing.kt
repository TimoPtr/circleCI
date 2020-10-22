/*
 * Copyright (c) 2017 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 * Proprietary and confidential
 */
package com.kolibree.android.offlinebrushings

import android.os.Parcelable
import androidx.annotation.Keep
import androidx.annotation.VisibleForTesting
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.kolibree.android.commons.GameApiConstants.GAME_OFFLINE
import com.kolibree.android.processedbrushings.CheckupCalculator
import com.kolibree.android.utils.KolibreeAppVersions
import com.kolibree.sdkws.data.model.Brushing
import com.kolibree.sdkws.data.model.CreateBrushingData
import java.util.Objects
import kotlinx.android.parcel.Parcelize
import org.threeten.bp.Duration
import org.threeten.bp.Instant
import org.threeten.bp.OffsetDateTime
import org.threeten.bp.ZoneOffset

/** Created by miguelaragues on 28/11/17.  */
@Entity(tableName = "orphan_brushing")
@Keep
@Parcelize
class OrphanBrushing(
    @ColumnInfo(name = "id")
    @PrimaryKey(autoGenerate = true)
    var id: Long = 0,

    @ColumnInfo(name = "duration")
    val duration: Long = 0,

    @ColumnInfo(name = "goal_duration")
    var goalDuration: Int = 0,

    @ColumnInfo(name = "timestamp")
    val timestamp: Long = 0,

    @ColumnInfo(name = "timestampZoneOffset")
    val timestampZoneOffset: ZoneOffset,

    @ColumnInfo(name = "toothbrush_mac")
    val toothbrushMac: String,

    @ColumnInfo(name = "toothbrush_serial")
    val toothbrushSerial: String,

    @ColumnInfo(name = "processed_data")
    val processedData: String? = null,

    // TODO this might need to be remove because it's never set
    @ColumnInfo(name = "kolibree_id")
    val kolibreeId: Long? = null,

    @ColumnInfo(name = "is_deleted_locally")
    var isDeletedLocally: Boolean = false,

    @ColumnInfo(name = "is_synchronized")
    val isSynchronized: Boolean = false,

    @ColumnInfo(name = "assigned_profile_id")
    var assignedProfileId: Long? = null
) : Parcelable {

    val durationObject: Duration
        get() = Duration.ofSeconds(duration)

    val isUploaded: Boolean
        get() = kolibreeId != null

    val dateTime: OffsetDateTime
        get() = OffsetDateTime.ofInstant(
            Instant.ofEpochSecond(timestamp),
            timestampZoneOffset
        )

    fun toBrushing(): Brushing = Brushing(
        duration,
        goalDuration,
        dateTime,
        0,
        0,
        processedData,
        0,
        null, // kolibreeId is never shared between different objects
        GAME_OFFLINE,
        toothbrushMac
    )

    fun toCreateBrushingData(
        appVersions: KolibreeAppVersions,
        checkupCalculator: CheckupCalculator
    ): CreateBrushingData {
        val data = innerCreateBrushingData()
        if (processedData?.isNotEmpty() == true) {
            val checkupData = checkupCalculator.calculateCheckup(
                processedData,
                data.date.toEpochSecond(),
                data.durationObject
            )
            data.coverage = checkupData.surfacePercentage
            data.setProcessedData(processedData)
        }
        data.addSupportData(
            toothbrushSerial,
            toothbrushMac,
            appVersions.appVersion,
            appVersions.buildVersion
        )
        return data
    }

    @VisibleForTesting
    fun innerCreateBrushingData(): CreateBrushingData = CreateBrushingData(
        GAME_OFFLINE, duration, goalDuration, dateTime, 0
    )

    override fun equals(o: Any?): Boolean {
        if (this === o) {
            return true
        }
        if (o == null || javaClass != o.javaClass) {
            return false
        }
        val that = o as OrphanBrushing
        val idEqualityCheckPasses: Boolean
        idEqualityCheckPasses = if (id == 0L || that.id == 0L) {
            true // one of the two isn't a DB record, don't take ID into account
        } else {
            id == that.id
        }
        return (idEqualityCheckPasses &&
            duration == that.duration &&
            goalDuration == that.goalDuration &&
            isDeletedLocally == that.isDeletedLocally &&
            isSynchronized == that.isSynchronized &&
            timestamp == that.timestamp &&
            toothbrushMac == that.toothbrushMac &&
            toothbrushSerial == that.toothbrushSerial &&
            processedData == that.processedData &&
            kolibreeId == that.kolibreeId &&
            assignedProfileId == that.assignedProfileId)
    }

    override fun hashCode(): Int =
        Objects.hash(
            duration,
            goalDuration,
            timestamp,
            timestampZoneOffset,
            toothbrushMac,
            toothbrushSerial,
            processedData,
            kolibreeId,
            isDeletedLocally,
            isSynchronized,
            assignedProfileId
        )

    companion object {

        @JvmStatic
        @JvmOverloads
        @Suppress("LongParameterList")
        fun create(
            durationInSeconds: Long,
            goalDuration: Int,
            processedData: String?,
            dateTime: OffsetDateTime,
            toothbrushSerial: String,
            toothbrushMac: String,
            id: Long = 0,
            assignedProfileId: Long? = null,
            isSynchronized: Boolean = false,
            kolibreeId: Long? = null
        ): OrphanBrushing = OrphanBrushing(
            duration = durationInSeconds,
            timestamp = dateTime.toEpochSecond(),
            timestampZoneOffset = dateTime.offset,
            toothbrushSerial = toothbrushSerial,
            toothbrushMac = toothbrushMac,
            goalDuration = goalDuration,
            processedData = processedData,
            id = id,
            kolibreeId = kolibreeId,
            assignedProfileId = assignedProfileId,
            isSynchronized = isSynchronized
        )
    }
}
