/*
 * Copyright (c) 2019 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.sdkws.calendar.logic

import androidx.annotation.Keep
import com.kolibree.android.accountinternal.profile.models.Profile
import com.kolibree.android.calendar.logic.model.CalendarBrushingState
import io.reactivex.Completable
import io.reactivex.Flowable
import io.reactivex.Observable
import io.reactivex.Single
import org.threeten.bp.YearMonth

@Keep
interface CalendarBrushingsUseCase {

    /**
     * Returns a pair of [YearMonth], which represent the range
     * of the calendar to display for a given [profile].
     *
     * [getBrushingDateRange] subscribes on [Schedulers.io]
     *
     * @return an [Single] which emits a from-to month brushing range, needed for calendar initialization.
     */
    fun getBrushingDateRange(
        profile: Profile
    ): Single<Pair<YearMonth, YearMonth>>

    /**
     * Responsible for returning updates of brushing states for given [profile] (brushing data + streaks).
     *
     * [getBrushingState] subscribes on [Schedulers.io]
     *
     * @return an [Flowable] which returns [CalendarBrushingState] updates for given [profile].
     */
    fun getBrushingState(
        profile: Profile
    ): Flowable<CalendarBrushingState>

    /**
     * Requests for next page of older brushings of given [profile], if they are not fetched yet. Each page contains
     * brushings for two months (so if we request brushings before May, we will get brushings from
     * March and April).
     *
     * Note: this method doesn't return the requested data.
     * They will arrive in [Observable] returned by [getBrushingState].
     *
     * @return [Completable] when the request was successfully queued,
     * or requested data are already in the local storage.
     */
    fun maybeFetchBrushingsBeforeMonth(
        profile: Profile,
        month: YearMonth
    ): Completable
}
