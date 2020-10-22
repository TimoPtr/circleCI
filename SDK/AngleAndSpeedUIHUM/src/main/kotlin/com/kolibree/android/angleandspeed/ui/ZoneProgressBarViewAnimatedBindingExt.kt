/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.angleandspeed.ui

import android.animation.ValueAnimator
import android.view.animation.LinearInterpolator
import androidx.databinding.BindingAdapter
import com.kolibree.android.app.ui.widget.ZoneProgressData
import com.kolibree.android.app.widget.zone.ZoneProgressBarView
import kotlin.math.min
import org.threeten.bp.Duration

private const val SPEED_UP_FACTOR = 2f

/**
 * Smooths the [ZoneProgressBarView] progression by adding additional interpolated progress values.
 *
 * @param newData new chunk of progress data
 * @param animStepDuration estimated duration of each anim step, related to time window between data updates
 * @param totalDuration amount of accumulated time needed to achieve 100% for each zone
 */
@BindingAdapter("animatedZoneData", "stageStepDuration", "stageTargetDuration")
internal fun ZoneProgressBarView.bindTimeDrivenAnimatedProgress(
    newData: ZoneProgressData?,
    animStepDuration: Duration,
    totalDuration: Duration
) {
    if (newData == null || newData == data) return

    if (progressNotEligibleForAnimation(newData)) {
        (tag as? ValueAnimator)?.cancel()
        setZoneProgressData(newData)
        return
    }

    val zoneIndex = data.zones.indexOfFirst { it.progress < 1f }
    (tag as? ValueAnimator)?.cancel()
    animateProgress(zoneIndex, data, newData, animStepDuration, totalDuration)
}

private fun ZoneProgressBarView.progressNotEligibleForAnimation(newData: ZoneProgressData): Boolean {
    val zoneIndex = data.zones.indexOfFirst { it.progress < 1f }
    val newDataZoneIndex = newData.zones.indexOfFirst { it.progress < 1f }
    val progressWasRestarted = zoneIndex != -1 &&
        (newDataZoneIndex < zoneIndex || newData.zones[zoneIndex].progress < data.zones[zoneIndex].progress)
    return zoneIndex == -1 || data.zones.isEmpty() || progressWasRestarted
}

private fun ZoneProgressBarView.animateProgress(
    zoneIndex: Int,
    startData: ZoneProgressData,
    newData: ZoneProgressData,
    animStepDuration: Duration,
    totalDuration: Duration
) {
    val initialProgress = startData.zones[zoneIndex].progress
    val maxValue = min(
        newData.zones[zoneIndex].progress,
        initialProgress + calculateIncreaseFactor(animStepDuration, totalDuration)
    )
    val animator = createAnimator(initialProgress, maxValue, animStepDuration) { animator ->
        val progress = animator.animatedValue as Float
        setZoneProgressData(startData.updateProgressOnZone(zoneIndex, progress))
    }
    animator.start()
    tag = animator
}

private fun createAnimator(
    from: Float,
    to: Float,
    duration: Duration,
    updateFunction: (ValueAnimator) -> Unit
): ValueAnimator {
    val animator = ValueAnimator.ofFloat(from, to)
    animator.interpolator = LinearInterpolator()
    animator.duration = duration.toMillis()
    animator.addUpdateListener { animation -> updateFunction(animation) }
    return animator
}

private fun calculateIncreaseFactor(
    animStepDuration: Duration,
    totalDuration: Duration
) = animStepDuration.toMillis().toFloat() / totalDuration.toMillis() * SPEED_UP_FACTOR
