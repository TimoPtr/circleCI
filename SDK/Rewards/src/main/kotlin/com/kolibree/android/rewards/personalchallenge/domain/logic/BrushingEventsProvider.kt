/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.rewards.personalchallenge.domain.logic

import com.kolibree.android.accountinternal.CurrentProfileProvider
import com.kolibree.android.annotation.VisibleForApp
import com.kolibree.android.processedbrushings.CheckupCalculator
import com.kolibree.android.rewards.personalchallenge.domain.model.BrushingEvent
import com.kolibree.android.rewards.personalchallenge.domain.model.BrushingStats
import com.kolibree.sdkws.brushing.persistence.repo.BrushingsRepository
import io.reactivex.Flowable
import javax.inject.Inject

@VisibleForApp
interface BrushingEventsProvider {

    fun brushingEventsStream(profileId: Long): Flowable<List<BrushingEvent>>
    fun brushingEventsStreamCurrentProfile(): Flowable<List<BrushingEvent>>
}

internal class BrushingEventsProviderImpl @Inject constructor(
    private val currentProfileProvider: CurrentProfileProvider,
    private val brushingsRepository: BrushingsRepository,
    private val checkupCalculator: CheckupCalculator,
    private val mapper: BrushingStatsToPersonalChallengeInputMapper
) : BrushingEventsProvider {

    override fun brushingEventsStream(profileId: Long): Flowable<List<BrushingEvent>> =
        brushingStatsStream(profileId)
            .map(mapper::map)

    override fun brushingEventsStreamCurrentProfile(): Flowable<List<BrushingEvent>> =
        currentProfileProvider.currentProfileFlowable()
            .switchMap { profile -> brushingEventsStream(profile.id) }

    private fun brushingStatsStream(profileId: Long): Flowable<BrushingStats> =
        brushingsRepository.brushingsFlowable(profileId)
            .map { brushings ->
                val fistBrushingSession = brushingsRepository.getFirstBrushingSession(profileId)
                val allBrushings = brushings.size

                BrushingStats(
                    firstBrushingDate = fistBrushingSession?.dateTime?.toLocalDate(),
                    lastMonth = lastMonthOnly(brushings).toBrushingsStat(checkupCalculator),
                    allBrushing = allBrushings
                )
            }
}
