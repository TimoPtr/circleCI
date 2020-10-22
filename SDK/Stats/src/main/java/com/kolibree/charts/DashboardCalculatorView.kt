/*
 * Copyright (c) 2018 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 * Proprietary and confidential
 */

package com.kolibree.charts

import androidx.annotation.Keep
import com.kolibree.charts.models.WeeklyStat
import io.reactivex.Flowable
import org.threeten.bp.ZonedDateTime

@Keep
interface DashboardCalculatorView {

    /**
     * Get the past weekly stat for a given profile for a given profileId
     *
     * @param profileId the id of the profile to get the stat to
     * @return a Flowable with the past weekly stats
     */
    fun getWeeklyStatForProfile(profileId: Long?): Flowable<WeeklyStat>

    /**
     * Calculates the week startDate from an endDate
     */
    fun adjustedStartDate(endDate: ZonedDateTime): ZonedDateTime
}
