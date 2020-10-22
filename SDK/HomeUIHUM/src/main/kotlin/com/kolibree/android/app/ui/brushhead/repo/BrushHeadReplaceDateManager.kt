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
import com.kolibree.android.app.ui.brushhead.repo.model.BrushHeadInformation
import com.kolibree.android.app.ui.brushhead.repo.model.BrushHeadInformation.Companion.NEW_TOOTHBRUSH_PERCENTAGE
import com.kolibree.android.clock.TrustedClock
import com.kolibree.android.commons.interfaces.Truncable
import com.kolibree.android.extensions.edit
import com.kolibree.android.extensions.toEpochMilli
import com.kolibree.android.persistence.BasePreferencesImpl
import io.reactivex.Completable
import io.reactivex.Maybe
import io.reactivex.Single
import javax.inject.Inject
import org.threeten.bp.Instant
import org.threeten.bp.OffsetDateTime

internal interface BrushHeadInformationReader {
    fun read(mac: String): Maybe<BrushHeadInformation>
}

internal interface BrushHeadReplacedDateWriter {
    fun writeReplacedDateNow(mac: String): Single<BrushHeadInformation>
    fun writeBrushHeadInformation(brushHeadInformation: BrushHeadInformation): Completable
}

internal class BrushHeadReplaceDateManager @Inject constructor(
    context: Context
) : BasePreferencesImpl(context),
    BrushHeadInformationReader,
    BrushHeadReplacedDateWriter,
    Truncable {

    companion object {
        private const val DEFAULT_PERCENTAGE = NEW_TOOTHBRUSH_PERCENTAGE

        private const val PREFS_NAME = "brush_head_replace_date_prefs"
        private const val DATE_REPLACE_KEY = "brush_head_replace_date_for_"
        private const val PERCENTAGE_KEY = "brush_head_percentage_for_"
        fun createDateReplaceKey(mac: String) = "$DATE_REPLACE_KEY$mac"
        fun createPercentageKey(mac: String) = "$PERCENTAGE_KEY$mac"
    }

    override fun getPreferencesName() = PREFS_NAME

    override fun read(mac: String): Maybe<BrushHeadInformation> =
        Maybe.defer {
            readResetDate(mac)?.let { resetDate ->
                Maybe.just(
                    BrushHeadInformation(
                        macAddress = mac,
                        resetDate = resetDate,
                        percentageLeft = readPercentage(mac)
                    )
                )
            } ?: Maybe.empty()
        }

    private fun readResetDate(mac: String): OffsetDateTime? {
        val key = createDateReplaceKey(mac)
        val timestamp = prefs.getLong(key, -1L)
        return when {
            timestamp > 0 -> toOffsetDateTimeWithSystemZone(timestamp)
            else -> null
        }
    }

    private fun readPercentage(mac: String): Int {
        return prefs.getInt(createPercentageKey(mac), DEFAULT_PERCENTAGE)
    }

    override fun writeReplacedDateNow(mac: String): Single<BrushHeadInformation> =
        Single.fromCallable {
            val newBrushHeadInformation = BrushHeadInformation.newBrushHead(mac = mac)

            internalWriteBrushHeadInformation(newBrushHeadInformation)

            newBrushHeadInformation
        }

    override fun writeBrushHeadInformation(brushHeadInformation: BrushHeadInformation): Completable =
        Completable.fromAction {
            internalWriteBrushHeadInformation(brushHeadInformation)
        }

    private fun internalWriteBrushHeadInformation(brushHeadInformation: BrushHeadInformation) {
        val mac = brushHeadInformation.macAddress

        prefs.edit {
            putLong(createDateReplaceKey(mac), brushHeadInformation.resetDate.toEpochMilli())
            putInt(createPercentageKey(mac), brushHeadInformation.percentageLeft)
        }
    }

    private fun toOffsetDateTimeWithSystemZone(timestamp: Long): OffsetDateTime =
        OffsetDateTime.ofInstant(
            Instant.ofEpochMilli(timestamp),
            TrustedClock.systemZone
        )

    override fun truncate(): Completable = Completable.fromAction { clear() }
}
