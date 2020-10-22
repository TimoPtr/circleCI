/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.app.ui.brushhead.repo

import android.content.Context
import com.kolibree.android.clock.TrustedClock.systemZoneOffset
import com.kolibree.android.extensions.edit
import com.kolibree.android.persistence.BasePreferencesImpl
import com.kolibree.android.room.ZoneOffsetConverter
import io.reactivex.Completable
import io.reactivex.Maybe
import javax.inject.Inject
import org.threeten.bp.Instant
import org.threeten.bp.OffsetDateTime
import org.threeten.bp.ZoneOffset

/**
 * This Provider retrieves the last brush head replacement date sent to the API for each
 * toothbrushes
 */
internal interface BrushHeadDateSendApiProvider {
    /**
     * @return [Maybe]<[OffsetDateTime]> that will emit the last brush head replace date sent to the
     * backend for the specified [mac], or Complete if we never sent a date
     */
    fun getLastReplacedDateSentMaybe(mac: String): Maybe<OffsetDateTime>
    fun setLastReplacedDateSentCompletable(mac: String, replacedDate: OffsetDateTime): Completable
}

internal class BrushHeadDateSendApiProviderImpl @Inject constructor(context: Context) :
    BrushHeadDateSendApiProvider, BasePreferencesImpl(context) {

    private val preferences = prefs

    override fun getLastReplacedDateSentMaybe(mac: String): Maybe<OffsetDateTime> {
        return Maybe.fromCallable {
            val timestamp = preferences.getLong(mac + LAST_DATE_SENT_TIMESTAMP, DEFAULT_TIMESTAMP)
            val zoneOffset =
                getZoneOffsetFromString(preferences.getString(mac + LAST_DATE_SENT_OFFSET, null))

            if (timestamp != DEFAULT_TIMESTAMP && zoneOffset != null)
                OffsetDateTime.ofInstant(Instant.ofEpochSecond(timestamp), zoneOffset)
            else null
        }
    }

    override fun setLastReplacedDateSentCompletable(
        mac: String,
        replacedDate: OffsetDateTime
    ): Completable {
        return Completable.fromAction {
            preferences.edit {
                putLong(mac + LAST_DATE_SENT_TIMESTAMP, replacedDate.toEpochSecond())
                putString(mac + LAST_DATE_SENT_OFFSET, getZoneOffset())
            }
        }
    }

    private fun getZoneOffset(): String? {
        return ZoneOffsetConverter.fromZoneOffset(systemZoneOffset)
    }

    private fun getZoneOffsetFromString(zoneOffset: String?): ZoneOffset? {
        return ZoneOffsetConverter.toZoneOffset(zoneOffset)
    }

    companion object {
        const val DEFAULT_TIMESTAMP = -1L
        const val LAST_DATE_SENT_TIMESTAMP = "_last_date_sent_timestamp"
        const val LAST_DATE_SENT_OFFSET = "_last_date_sent_offset"
    }
}
