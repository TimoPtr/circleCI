/*
 * Copyright (c) 2019 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.sdk.connection.brushingmode

import android.content.Context
import androidx.annotation.Keep
import androidx.annotation.VisibleForTesting
import com.kolibree.android.accountinternal.profile.models.Profile
import com.kolibree.android.clock.TrustedClock
import com.kolibree.android.clock.TrustedClock.getNowOffsetDateTime
import com.kolibree.android.persistence.BasePreferencesImpl
import javax.inject.Inject
import org.threeten.bp.OffsetDateTime
import org.threeten.bp.format.DateTimeParseException
import timber.log.Timber

/**
 * Repository to read/write [BrushingMode] for [Profile]
 */
@Keep
interface BrushingModeRepository {
    fun setForProfile(profileId: Long, brushingMode: BrushingMode)

    /**
     * @return ProfileBrushingMode for [profileId]. Returns null if there's no brushing mode stored
     * for [profileId]
     */
    fun getForProfile(profileId: Long): ProfileBrushingMode?

    /**
     * return default brushing mode for Profile if there's no brushing mode stored
     */
    fun getDefaultModeIfNull(profileId: Long): ProfileBrushingMode
}

internal class BrushingModePerProfileRepository @Inject constructor(context: Context) :
    BasePreferencesImpl(context), BrushingModeRepository {
    override fun getForProfile(profileId: Long): ProfileBrushingMode? {
        val storedBrushingMode = prefs.getString(profileId.brushingModeKey(), null)
        val storedTimestamp = prefs.getString(profileId.timestampKey(), null)
        val parsedTimestamp = parseTimestampOrNull(storedTimestamp)

        if (storedBrushingMode == null || parsedTimestamp == null) return null

        return ProfileBrushingMode(
            profileId,
            BrushingMode.valueOf(storedBrushingMode),
            parsedTimestamp
        )
    }

    private fun parseTimestampOrNull(timestamp: String?) = timestamp
        ?.let {
            try {
                OffsetDateTime.parse(timestamp)
            } catch (e: DateTimeParseException) {
                Timber.w(e, "It might be because of the migration from LocalDateTime to OffsetDateTime")
                null
            }
        }

    override fun setForProfile(profileId: Long, brushingMode: BrushingMode) {
        prefsEditor.apply {
            putString(profileId.brushingModeKey(), brushingMode.toString())
            putString(
                profileId.timestampKey(),
                TrustedClock.getNowOffsetDateTime().toString()
            )

            commit()
        }
    }

    @VisibleForTesting
    override fun getDefaultModeIfNull(profileId: Long): ProfileBrushingMode {
        return getForProfile(profileId) ?: ProfileBrushingMode.NULL
    }

    private fun Long.brushingModeKey() = "$KEY_BRUSHING_MODE$this"
    private fun Long.timestampKey() = "$KEY_PROFILE_TIMESTAMP$this"
}

/*
Dynamic timestamps for brushing mode persistence. They'll be constructed at runtime from the
profile id
 */
private const val KEY_BRUSHING_MODE = "brushing_mode_for_"
private const val KEY_PROFILE_TIMESTAMP = "brushing_mode_timestamp_for_"

@Keep
data class ProfileBrushingMode(
    val profileId: Long,
    val brushingMode: BrushingMode,
    val dateTime: OffsetDateTime
) {

    companion object {

        val NULL = ProfileBrushingMode(0L, BrushingMode.defaultMode(), OffsetDateTime.MIN)
    }

    /**
     * Quick check ensuring that the [dateTime] associated with this object is not out of date.
     * A case like this can happen when the user arbitrarily changes the device clock.
     * Meaning this object [dateTime] would be in an unknown future.
     *
     * Callers have the responsibility to update [ProfileBrushingMode]
     * if [isValid] returns `false`
     *
     * @return `true` if this [ProfileBrushingMode] dateTime is before the user device datetime,
     *  `false` otherwise.
     */
    fun isValid(): Boolean = dateTime.isBefore(getNowOffsetDateTime())
}
