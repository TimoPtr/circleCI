/*
 * Copyright (c) 2018 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 * Proprietary and confidential
 */

package com.kolibree.sdkws.brushing.persistence.models

import androidx.annotation.VisibleForTesting
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import com.kolibree.android.defensive.Preconditions
import com.kolibree.android.processedbrushings.CheckupCalculator
import com.kolibree.android.room.ZoneOffsetConverter
import com.kolibree.android.synchronizator.data.database.UuidConverters
import com.kolibree.sdkws.brushing.wrapper.IBrushing
import com.kolibree.sdkws.brushing.wrapper.IBrushing.Companion.MAXIMUM_ACCEPTABLE_BRUSHING_GOAL_TIME_SECONDS
import com.kolibree.sdkws.brushing.wrapper.IBrushing.Companion.MINIMUM_ACCEPTABLE_BRUSHING_GOAL_TIME_SECONDS
import com.kolibree.sdkws.brushing.wrapper.IBrushing.Companion.MINIMUM_BRUSHING_GOAL_TIME_SECONDS
import com.kolibree.sdkws.data.model.Brushing
import com.kolibree.sdkws.data.model.CreateBrushingData
import java.util.UUID
import org.threeten.bp.Duration
import org.threeten.bp.Instant
import org.threeten.bp.OffsetDateTime
import org.threeten.bp.ZoneOffset

@Entity(tableName = TABLE_NAME)
@TypeConverters(ZoneOffsetConverter::class, UuidConverters::class)
internal data class BrushingInternal @VisibleForTesting constructor(
    @ColumnInfo(name = "id")
    @PrimaryKey(autoGenerate = true) var id: Long? = null,
    @ColumnInfo(name = "game") val game: String,
    @ColumnInfo(name = "duration") val duration: Long, // Seconds
    @ColumnInfo(name = "timestamp") val timestamp: Long, // in seconds
    @ColumnInfo(name = "timestampZoneOffset") val timestampZoneOffset: ZoneOffset,
    @ColumnInfo(name = "profileid") val profileId: Long,
    @ColumnInfo(name = "coins") val coins: Int = 0, // not used, we keep it for retrocompatibility
    @ColumnInfo(name = "issync") var isSynchronized: Boolean,
    // TODO change this back to val
    // when PATCH_INCORRECT_BRUSHING_GOAL_VALUES flag will be removed
    @ColumnInfo(name = "goal_duration") var goalDuration: Int,
    @ColumnInfo(name = "processed_data") val processedData: String? = null,
    @ColumnInfo(name = "points") @Deprecated(message = "Always 0")
    val points: Int = 0, // not used, we keep it for retrocompatibility
    @ColumnInfo(name = "kolibree_id") val kolibreeId: Long = 0, // not used, we keep it for retrocompatibility
    @ColumnInfo(name = "is_deleted_locally") var isDeletedLocally: Boolean = false,
    @ColumnInfo(name = "serial") val toothbrushSerial: String? = "",
    @ColumnInfo(name = "mac") val toothbrushMac: String? = "",
    @ColumnInfo(name = "app_version") val appVersion: String? = "",
    @ColumnInfo(name = "app_build") val appBuild: String? = "",
    @ColumnInfo(
        name = "idempotency_key",
        defaultValue = ""
    ) val idempotencyKey: UUID,
    @ColumnInfo(name = "is_fake_brushing") val isFakeBrushing: Boolean = false
) {

    init {
        Preconditions.checkArgumentNonNegative(duration)
        Preconditions.checkArgument(game.isNotEmpty())
        Preconditions.checkArgumentNonNegative(profileId)
        Preconditions.checkArgumentNonNegative(coins)
        Preconditions.checkArgumentNonNegative(points)
        try {
            Preconditions.checkArgumentInRange(
                goalDuration,
                MINIMUM_ACCEPTABLE_BRUSHING_GOAL_TIME_SECONDS,
                MAXIMUM_ACCEPTABLE_BRUSHING_GOAL_TIME_SECONDS,
                "goal duration"
            )
        } catch (e: IllegalArgumentException) {
            // TODO this is a temporary solution and should be removed in the future
            @Suppress("ConstantConditionIf")
            if (IBrushing.PATCH_INCORRECT_BRUSHING_GOAL_VALUES) {
                goalDuration = MINIMUM_BRUSHING_GOAL_TIME_SECONDS
            } else {
                throw e
            }
        }
        Preconditions.checkArgumentNonNegative(kolibreeId)
    }

    constructor(
        id: Long? = null,
        game: String,
        duration: Long, // Seconds
        datetime: OffsetDateTime,
        profileId: Long,
        coins: Int = 0, // not used, we keep it for retrocompatibility

        isSynchronized: Boolean,
        goalDuration: Int,
        processedData: String? = null,
        points: Int = 0, // not used, we keep it for retrocompatibility
        kolibreeId: Long = 0, // not used, we keep it for retrocompatibility
        isDeletedLocally: Boolean = false,
        toothbrushSerial: String? = "",
        toothbrushMac: String? = "",
        appVersion: String? = "",
        appBuild: String? = "",
        idempotencyKey: UUID,
        isFakeBrushing: Boolean = false
    ) : this(
        id = id,
        game = game,
        duration = duration,
        timestamp = datetime.toEpochSecond(),
        timestampZoneOffset = datetime.offset,
        profileId = profileId,
        coins = coins,
        isSynchronized = isSynchronized,
        goalDuration = goalDuration,
        processedData = processedData,
        points = points,
        kolibreeId = kolibreeId,
        isDeletedLocally = isDeletedLocally,
        toothbrushSerial = toothbrushSerial,
        toothbrushMac = toothbrushMac,
        appVersion = appVersion,
        appBuild = appBuild,
        idempotencyKey = idempotencyKey,
        isFakeBrushing = isFakeBrushing
    )

    @Ignore
    val durationObject: Duration = Duration.ofSeconds(duration)

    /**
     * Create the OffsetDateTime object from the fields store in the DB (timestamp and ZoneOffset)
     */
    val dateTime: OffsetDateTime
        get() = OffsetDateTime.ofInstant(Instant.ofEpochSecond(timestamp), timestampZoneOffset)

    fun extractCreateBrushingData(checkupCalculator: CheckupCalculator): CreateBrushingData {
        val data = CreateBrushingData(
            game = game,
            duration = duration,
            goalDuration = goalDuration,
            date = dateTime,
            coins = coins,
            idempotencyKey = idempotencyKey,
            isFakeBrushing = isFakeBrushing
        )

        maybeAddProcessedData(checkupCalculator, data)

        data.addSupportData(toothbrushSerial, toothbrushMac, appVersion, appBuild)
        return data
    }

    private fun maybeAddProcessedData(
        checkupCalculator: CheckupCalculator,
        data: CreateBrushingData
    ) {
        if (processedData != null) {
            val checkupData =
                checkupCalculator.calculateCheckup(
                    processedData,
                    timestamp,
                    durationObject
                )
            data.coverage = checkupData.surfacePercentage
            data.setProcessedData(processedData)
        }
    }

    fun extractBrushing(): Brushing =
        Brushing(
            duration,
            goalDuration,
            dateTime,
            coins,
            points,
            processedData,
            profileId,
            kolibreeId,
            game,
            toothbrushMac
        )

    fun updateFromResponse(brushingInternal: BrushingInternal) = this.copy(
        kolibreeId = brushingInternal.kolibreeId,
        points = brushingInternal.points,
        profileId = brushingInternal.profileId,
        isSynchronized = true
    )

    companion object {

        /**
         * Create a [BrushingInternal] instance from a [CreateBrushingData]
         *
         * Only call this method with data that HAS NOT been synchronized yet.
         *
         * @param data [CreateBrushingData]
         */
        fun fromBrushingData(data: CreateBrushingData, profileId: Long) = BrushingInternal(
            game = data.game,
            duration = data.duration,
            timestamp = data.date.toEpochSecond(),
            timestampZoneOffset = data.date.offset,
            profileId = profileId,
            isSynchronized = false,
            coins = data.coins,
            goalDuration = data.goalDuration,
            processedData = data.getProcessedData(),
            points = 0,
            toothbrushSerial = data.serial,
            toothbrushMac = data.mac,
            appVersion = data.appVersion,
            appBuild = data.buildVersion,
            kolibreeId = 0,
            idempotencyKey = data.idempotencyKey,
            isFakeBrushing = data.isFakeBrushing
        )
    }
}

internal const val TABLE_NAME = "brushing"
