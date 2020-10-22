/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.statsoffline.integrityseal

import com.kolibree.statsoffline.persistence.StatsOfflineDao
import java.util.Locale
import javax.inject.Inject
import org.threeten.bp.DayOfWeek
import org.threeten.bp.temporal.WeekFields

/**
 * Seal that ensures that the integrity of the Stats Offline records is valid since last run
 *
 * The integrity is user selected a new region where First day of week changed
 *
 * I decided to not wipe content on Timezone changed. I think the internals protect against that,
 * since we always use LocalDate/LocalDateTime. If this were not the case, then we can wipe content
 * if timezone changed
 *
 * If the seal is broken, this class wipes the database
 */
internal class IntegritySeal
@Inject constructor(
    private val statsOfflineDao: StatsOfflineDao,
    private val sealDataStore: IntegritySealDataStore
) {
    fun validateIntegrity() {
        if (isSealBroken()) {
            statsOfflineDao.truncate()

            sealDataStore.clear()
        }
    }

    /**
     * Checks if the integrity seal is broken
     */
    private fun isSealBroken(): Boolean = firstDayOfWeekChanged()

    /**
     * Checks if user's first day of week settings have changed since last time this method was
     * invoked
     *
     * On first invocation, it returns false and stores the current first day of week
     */
    private fun firstDayOfWeekChanged(): Boolean {
        val firstDayOfWeek = firstDayOfWeek()

        val storedFirstDayOfWeek = sealDataStore.storedFirstDayOfWeek()

        return if (storedFirstDayOfWeek == null) {
            sealDataStore.storeFirstDayOfWeek(firstDayOfWeek)

            false
        } else {
            firstDayOfWeek != storedFirstDayOfWeek
        }
    }

    private fun firstDayOfWeek(): DayOfWeek {
        return WeekFields.of(Locale.getDefault()).firstDayOfWeek
    }
}
