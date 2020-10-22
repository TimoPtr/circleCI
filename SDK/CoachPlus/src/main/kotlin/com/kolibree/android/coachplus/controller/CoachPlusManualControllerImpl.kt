/*
 * Copyright (c) 2019 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.coachplus.controller

import androidx.annotation.VisibleForTesting
import com.kolibree.android.clock.TrustedClock
import com.kolibree.android.coachplus.feedback.FeedBackMessage
import com.kolibree.android.commons.GameApiConstants
import com.kolibree.android.sdk.connection.detectors.data.OverpressureState
import com.kolibree.android.sdk.connection.detectors.data.PlaqlessRawSensorState
import com.kolibree.android.sdk.connection.detectors.data.PlaqlessSensorState
import com.kolibree.android.sdk.connection.detectors.data.RawSensorState
import com.kolibree.kml.MouthZone16
import com.kolibree.sdkws.data.model.CreateBrushingData
import java.util.concurrent.TimeUnit
import kotlin.math.roundToInt
import org.threeten.bp.Duration

/**
 * Coach Plus manual mode controller.
 *
 * Will display a sequenced list of zones according to the profile's goal brushing duration
 */
internal class CoachPlusManualControllerImpl(
    goalBrushingDuration: Duration,
    private val tickPeriod: Long
) :
    BaseCoachPlusControllerImpl(goalBrushingDuration) {

    override fun notifyReconnection() {
        // no-op
    }

    override fun onPause() {
        // no-op
    }

    override fun onPlaqlessData(isPlaying: Boolean, sensorState: PlaqlessSensorState) {
        // no-op. Manual can't have plaqless data
    }

    override fun onPlaqlessRawData(isPlaying: Boolean, sensorState: PlaqlessRawSensorState) {
        // no-op. Manual can't have plaqless data
    }

    override fun onOverpressureState(overpressureState: OverpressureState) {
        // no-op
    }

    @VisibleForTesting
    var currentZoneCompletionMillis: Long = 0L

    override fun onTick(): CoachPlusControllerResult {
        currentZoneCompletionMillis += tickPeriod
        if (isCurrentZoneCompleted()) {
            return if (hasMoreZones()) {
                val result = createResult(currentZoneIndex, true, false)
                currentZoneCompletionMillis = 0L
                brushNextZone()
                result
            } else {
                createResult(currentZoneIndex, true, true)
            }
        }

        return createResult(currentZoneIndex, true, false)
    }

    override fun onSvmData(possibleZones: List<MouthZone16>) {
        // No detector output in manual mode
    }

    override fun onRawData(isPlaying: Boolean, sensorState: RawSensorState) {
        // no-op
    }

    override fun createBrushingData(): CreateBrushingData {
        // For now (2018 October 15th) the backend uses TI for both Coach and Coach+
        return CreateBrushingData(
            GameApiConstants.GAME_COACH_MANUAL,
            computeBrushingDuration().toLong(),
            getGoalBrushingDuration().seconds.toInt(),
            TrustedClock.getNowOffsetDateTime(),
            0
        )
    }

    override fun computeBrushingDuration(): Int {
        val durationInMillis =
            getGoalBrushingTimePerZone() * currentZoneIndex + currentZoneCompletionMillis

        return TimeUnit.MILLISECONDS.toSeconds(durationInMillis).toInt()
    }

    override fun getAvroTransitionsTable(): IntArray {
        throw IllegalStateException("No AVRO data for manual Coach+ sessions")
    }

    override fun reset() {
        super.reset()
        currentZoneCompletionMillis = 0
    }

    @VisibleForTesting
    fun isCurrentZoneCompleted(): Boolean {
        return currentZoneCompletionMillis >= getGoalBrushingTimePerZone()
    }

    private fun createResult(
        currentIndex: Int,
        brushingGoodZone: Boolean,
        sequenceFinished: Boolean
    ): CoachPlusControllerResult {
        return CoachPlusControllerResult(
            SEQUENCE[currentIndex],
            (currentZoneCompletionMillis * 100f / getGoalBrushingTimePerZone()).roundToInt(),
            brushingGoodZone,
            sequenceFinished,
            FeedBackMessage.EmptyFeedback // Empty because eligible only with kml
        )
    }
}
