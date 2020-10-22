/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.statsoffline.integrityseal

import android.content.Context
import androidx.annotation.VisibleForTesting
import com.kolibree.android.commons.interfaces.Truncable
import com.kolibree.android.extensions.edit
import com.kolibree.android.persistence.BasePreferencesImpl
import io.reactivex.Completable
import javax.inject.Inject
import org.threeten.bp.DateTimeException
import org.threeten.bp.DayOfWeek
import timber.log.Timber

internal class IntegritySealDataStore
@Inject constructor(context: Context) : BasePreferencesImpl(context), Truncable {

    fun storedFirstDayOfWeek(): DayOfWeek? {
        val storedValue = prefs.getInt(PREVIOUS_FIRST_DAY_OF_WEEK, NO_DAY_OF_WEEK)

        if (storedValue == NO_DAY_OF_WEEK) return null

        return try {
            DayOfWeek.of(storedValue)
        } catch (dte: DateTimeException) {
            Timber.w(dte)

            null
        }
    }

    fun storeFirstDayOfWeek(dayOfWeek: DayOfWeek) {
        prefs.edit {
            putInt(PREVIOUS_FIRST_DAY_OF_WEEK, dayOfWeek.value)
        }
    }

    override fun getPreferencesName(): String = PREFS_NAME

    override fun truncate(): Completable = Completable.fromAction { clear() }
}

@VisibleForTesting
internal const val PREFS_NAME = "stats_offline_seal"

@VisibleForTesting
internal const val PREVIOUS_FIRST_DAY_OF_WEEK = "previous_first_day_of_week"

private const val NO_DAY_OF_WEEK = 0
