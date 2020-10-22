/*
 * Copyright (c) 2019 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.sdkws.calendar.logic

import android.annotation.SuppressLint
import androidx.annotation.VisibleForTesting
import com.google.common.base.Optional
import com.kolibree.android.calendar.logic.api.BrushingStreaksApi
import com.kolibree.android.calendar.logic.api.model.BrushingStreaksResponse
import com.kolibree.android.calendar.logic.model.BrushingStreak
import com.kolibree.android.calendar.logic.persistence.BrushingStreaksDao
import com.kolibree.android.calendar.logic.persistence.model.BrushingStreakEntity
import com.kolibree.android.commons.DATE_FORMATTER
import com.kolibree.android.defensive.Preconditions
import com.kolibree.android.failearly.FailEarly
import com.kolibree.android.network.toParsedResponseSingle
import com.kolibree.android.network.utils.NetworkChecker
import com.kolibree.sdkws.brushing.persistence.repo.BrushingsRepository
import com.kolibree.sdkws.calendar.di.CalendarApiModule.CALCULATE_STREAKS_ON_THE_FLY
import com.kolibree.sdkws.data.model.Brushing
import io.reactivex.Flowable
import io.reactivex.Observable
import io.reactivex.Single
import io.reactivex.schedulers.Schedulers
import javax.inject.Inject
import javax.inject.Named
import org.threeten.bp.LocalDate

internal class CalendarBrushingsRepository @Inject constructor(
    private val api: BrushingStreaksApi,
    private val dao: BrushingStreaksDao,
    private val networkChecker: NetworkChecker,
    private val brushingsRepository: BrushingsRepository,
    @Named(CALCULATE_STREAKS_ON_THE_FLY) private val calculateOnTheFly: Boolean
) {
    /**
     * Returns a set of brushings streaks (pair of from-to dates) for particular profile. Values from local persistence
     * are returned first. If there is an internet connect, streaks are refreshed from API and persistence if updated
     * based on received data. Afterwards, new value of streaks are returned.
     *
     * [getStreaksForProfile] subscribes on [Schedulers.io]
     *
     * @param profileId id of profile
     * @return an [Observable] which emits a set of [BrushingStreak] for profile.
     */
    fun getStreaksForProfile(profileId: Long): Flowable<Set<BrushingStreak>> {
        val methodToCall: Flowable<Set<BrushingStreak>> = when {
            calculateOnTheFly -> calculateStreaksForProfile(profileId)
            networkChecker.hasConnectivity() -> getOnlineAndOfflineStreaksForProfile(profileId)
            else -> getOnlyOfflineStreaksForProfile(profileId)
        }
        return methodToCall.subscribeOn(Schedulers.io())
    }

    @VisibleForTesting
    fun getOnlineAndOfflineStreaksForProfile(profileId: Long): Flowable<Set<BrushingStreak>> {
        return Flowable.fromCallable { getPersistedStreaks(profileId) }
            .concatWith(getStreaksFromApi(profileId))
    }

    @VisibleForTesting
    fun getOnlyOfflineStreaksForProfile(profileId: Long): Flowable<Set<BrushingStreak>> {
        return Flowable.fromCallable {
            return@fromCallable getPersistedStreaks(profileId)
        }
    }

    @VisibleForTesting
    fun getStreaksFromApi(profileId: Long): Single<Set<BrushingStreak>> {
        return api.getStreaksForProfile(profileId)
            .toParsedResponseSingle()
            .map { response -> getSanitizedStreaks(response) }
            .doOnSuccess { streaks -> persistStreaks(profileId, streaks) }
    }

    @SuppressLint("ExperimentalClassUse")
    @VisibleForTesting
    fun getSanitizedStreaks(response: BrushingStreaksResponse): Set<BrushingStreak> =
        response.body?.streaks?.map { streak ->
            try {
                Preconditions.checkArgument(streak != null)
                Preconditions.checkArgument(streak?.size == 2) // we need to have pairs
                Preconditions.checkNotNull(streak?.get(0)) // of non null
                Preconditions.checkNotNull(streak?.get(1))
                val streakStart = LocalDate.from(DATE_FORMATTER.parse(streak?.get(0))) // local dates
                val streakEnd = LocalDate.from(DATE_FORMATTER.parse(streak?.get(1)))
                Preconditions.checkArgument(streakStart.isBefore(streakEnd)) // in the ascending order
                Optional.of(BrushingStreak(streakStart, streakEnd))
            } catch (e: RuntimeException) {
                FailEarly.fail(exception = e)
                Optional.absent<BrushingStreak>()
            }
        }?.filter { it.isPresent }
            ?.map { optional -> optional.get() }
            ?.toSet()
            ?: emptySet()

    @VisibleForTesting
    fun getPersistedStreaks(profileId: Long): Set<BrushingStreak> {
        return dao.queryByProfile(profileId).map { entity -> entity.toStreak() }.toSet()
    }

    @VisibleForTesting
    fun persistStreaks(profileId: Long, streaks: Set<BrushingStreak>) {
        dao.replaceForProfile(
            profileId,
            streaks.toList().map { streak -> BrushingStreakEntity.from(profileId, streak) })
    }

    private fun calculateStreaksForProfile(profileId: Long): Flowable<Set<BrushingStreak>> {
        return brushingsRepository.getBrushings(profileId)
            .map { calculateStreaksFromBrushings(it) }
            .toFlowable()
    }

    @VisibleForTesting
    fun calculateStreaksFromBrushings(brushings: List<Brushing>): Set<BrushingStreak> {
        val daysWithMultiBrushings = brushings.groupingBy { brushing: Brushing -> brushing.dateTime.toLocalDate() }
            .fold(initialValue = 0) { accumulator, _ -> accumulator + 1 }
            .filter { it.value >= 2 }
            .map { it.key }
            .toSortedSet()

        val streaks = mutableMapOf<LocalDate, LocalDate>()

        daysWithMultiBrushings.forEach { day ->
            if (streaks.containsValue(day.minusDays(1))) {
                streaks.entries.first { it.value == day.minusDays(1) }.let {
                    streaks[it.key] = day
                }
            } else if (streaks.containsKey(day.minusDays(1))) {
                streaks[day.minusDays(1)] = day
            } else if (!streaks.containsKey(day)) {
                streaks[day] = day
            }
        }

        return streaks.filter { !it.key.isEqual(it.value) }.map { BrushingStreak(it.key, it.value) }.toSet()
    }
}
