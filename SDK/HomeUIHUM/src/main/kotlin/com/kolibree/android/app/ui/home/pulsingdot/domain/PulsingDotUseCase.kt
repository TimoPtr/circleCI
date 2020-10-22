/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.app.ui.home.pulsingdot.domain

import com.kolibree.android.annotation.VisibleForApp
import com.kolibree.android.app.ui.home.pulsingdot.data.PulsingDotPersistence
import com.kolibree.android.app.ui.home.pulsingdot.data.PulsingDotProvider
import com.kolibree.android.feature.FeatureToggleSet
import com.kolibree.android.feature.PulsingDotFeature
import com.kolibree.android.feature.toggleForFeature
import io.reactivex.BackpressureStrategy
import io.reactivex.Flowable
import io.reactivex.Observable
import javax.inject.Inject

/**
 * This UseCase offer a flexible way to deal with the PulsingDot across the Application
 */
@VisibleForApp
interface PulsingDotUseCase {
    fun shouldShowPulsingDot(pulsingDot: PulsingDot): Flowable<Boolean>
    fun shouldShowExplanation(): Flowable<Boolean>
    fun onPulsingDotClicked(pulsingDot: PulsingDot)
    fun onExplanationShown()
}

internal class PulsingDotUseCaseImpl @Inject constructor(
    private val pulsingDotProvider: PulsingDotProvider,
    private val featureToggles: FeatureToggleSet
) : PulsingDotUseCase {

    override fun shouldShowPulsingDot(pulsingDot: PulsingDot): Flowable<Boolean> {
        if (isPulsingDotAlwaysDisplayed()) {
            return Flowable.just(true)
        }

        return hasBeenClicked(pulsingDot)
            .map { hasBeenClicked ->
                !hasBeenClicked && canShowPulsingDot(pulsingDot)
            }
            .doOnNext { shouldShow ->
                if (shouldShow) {
                    incrementPulsingDotShown(pulsingDot)
                }
            }
            .toFlowable(BackpressureStrategy.LATEST)
    }

    override fun onPulsingDotClicked(pulsingDot: PulsingDot) {
        pulsingDotProvider.setIsClicked(PulsingDotMapper.map(pulsingDot))
    }

    override fun shouldShowExplanation(): Flowable<Boolean> {
        return pulsingDotProvider.isExplanationShown().toFlowable(BackpressureStrategy.LATEST)
            .map { isShown -> !isShown }
    }

    override fun onExplanationShown() {
        pulsingDotProvider.setExplanationShown()
    }

    private fun incrementPulsingDotShown(pulsingDot: PulsingDot) {
        pulsingDotProvider.incTimesShown(PulsingDotMapper.map(pulsingDot))
    }

    private fun hasBeenClicked(pulsingDot: PulsingDot): Observable<Boolean> =
        pulsingDotProvider.isClicked(PulsingDotMapper.map(pulsingDot))

    private fun canShowPulsingDot(pulsingDot: PulsingDot) =
        pulsingDotProvider.getTimesShown(PulsingDotMapper.map(pulsingDot)) < MAX_TIME_SHOWN

    private fun isPulsingDotAlwaysDisplayed() =
        featureToggles.toggleForFeature(PulsingDotFeature).value

    companion object {
        const val MAX_TIME_SHOWN = 5
    }
}

internal class DisabledPulsingDotUseCase @Inject constructor() : PulsingDotUseCase {
    override fun shouldShowPulsingDot(pulsingDot: PulsingDot) = Flowable.just(false)
    override fun shouldShowExplanation(): Flowable<Boolean> = Flowable.just(false)
    override fun onPulsingDotClicked(pulsingDot: PulsingDot) = Unit
    override fun onExplanationShown() = Unit
}

@VisibleForApp
enum class PulsingDot {
    SMILE,
    LAST_BRUSHING_SESSION,
    BRUSH_BETTER,
    FREQUENCY_CHART,
}

/**
 * Mapper used to map a PulsingDot from data to domain following the clean architecture principle
 */
internal object PulsingDotMapper {
    fun map(pulsingDot: PulsingDot): PulsingDotPersistence = when (pulsingDot) {
        PulsingDot.SMILE -> PulsingDotPersistence.SMILE
        PulsingDot.LAST_BRUSHING_SESSION -> PulsingDotPersistence.LAST_BRUSHING_SESSION
        PulsingDot.BRUSH_BETTER -> PulsingDotPersistence.BRUSH_BETTER
        PulsingDot.FREQUENCY_CHART -> PulsingDotPersistence.FREQUENCY_CHART
    }
}
