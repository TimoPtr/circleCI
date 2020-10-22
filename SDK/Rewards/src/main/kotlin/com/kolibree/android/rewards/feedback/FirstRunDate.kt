/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.rewards.feedback

import android.content.SharedPreferences
import androidx.annotation.Keep
import androidx.annotation.VisibleForTesting
import com.kolibree.android.clock.TrustedClock
import javax.inject.Inject
import org.threeten.bp.ZonedDateTime

@Keep
interface FirstLoginDateUpdater {
    fun update()
}

@Keep
interface FirstLoginDateProvider {
    fun firstRunDate(): ZonedDateTime
}

@VisibleForTesting
internal class FirstLoginDateImpl @Inject constructor(
    private val preferences: SharedPreferences
) : FirstLoginDateUpdater,
    FirstLoginDateProvider {

    override fun firstRunDate(): ZonedDateTime {
        if (containsDate()) {
            return readDate()
        }

        update()

        return readDate()
    }

    @VisibleForTesting
    fun readDate(): ZonedDateTime {
        val textualDate = preferences.getString(FIRST_RAN_DATE_KEY, null)!!
        return ZonedDateTime.parse(textualDate)
    }

    override fun update() {
        if (!containsDate()) {
            val nowDateTime = TrustedClock.getNowZonedDateTime()

            preferencesEditor().putString(FIRST_RAN_DATE_KEY, nowDateTime.toString()).apply()
        }
    }

    private fun preferencesEditor(): SharedPreferences.Editor = preferences.edit()

    @VisibleForTesting
    fun containsDate() = preferences.contains(FIRST_RAN_DATE_KEY)
}

@VisibleForTesting
internal const val FIRST_RAN_DATE_KEY = "first_ran_datetime"
