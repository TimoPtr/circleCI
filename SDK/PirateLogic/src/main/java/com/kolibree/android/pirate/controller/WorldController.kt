/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.pirate.controller

import androidx.annotation.VisibleForTesting
import com.kolibree.android.app.utils.gamecontroler.GameController
import com.kolibree.android.clock.TrustedClock
import com.kolibree.android.commons.GameApiConstants
import com.kolibree.android.failearly.FailEarly
import com.kolibree.android.processedbrushings.CheckupCalculator
import com.kolibree.android.sdk.connection.detectors.data.RawSensorState
import com.kolibree.kml.MouthZone16
import com.kolibree.kml.ProcessedBrushing
import com.kolibree.sdkws.data.model.CreateBrushingData
import com.unity3d.player.UnityPlayer
import java.util.concurrent.atomic.AtomicBoolean
import org.threeten.bp.Duration
import timber.log.Timber

internal abstract class WorldController(
    isRightHand: Boolean,
    checkupCalculator: CheckupCalculator
) : GameController(isRightHand, checkupCalculator) {

    @VisibleForTesting(otherwise = VisibleForTesting.PROTECTED)
    val isPlaying = AtomicBoolean(false)

    @VisibleForTesting(otherwise = VisibleForTesting.PROTECTED)
    val waitingForChangeLaneMessage = AtomicBoolean(false)

    @VisibleForTesting(otherwise = VisibleForTesting.PROTECTED)
    val shouldContinueProcessingData = AtomicBoolean(true)

    open fun onRawData(sensorState: RawSensorState) {
        // no-op
    }

    // Could be called after when the game start and after a pause
    override fun run() {
        isPlaying.set(true)
    }

    override fun pause() {
        isPlaying.set(false)
    }

    override fun stop() {
        isPlaying.set(false)
    }

    override fun addGoldEarned(gold: Int) {
        this.gold += gold
        Timber.i("add gold $gold new total ${this.gold}")
    }

    override fun shouldChangeLane() {
        waitingForChangeLaneMessage.set(false)
    }

    override fun getQuality(targetBrushingTime: Int): Int {
        // no use in KML
        return 0
    }

    override fun setCompleteTime(time: Int) {
        // no use in KML
    }

    override fun setCurrentPossibleMouthZones(mouthZone: MutableList<MouthZone16>?) {
        // no op in KML
    }

    // Public only for testing purpose VisibleForTesting does not work well here
    internal fun maybeChangeLane(nextLane: String, currentZoneName: String, isZoneCorrect: Boolean) {
        if (waitingForChangeLaneMessage.get().not()) {
            Timber.v("nextLane is $nextLane zone $currentZoneName is correct $isZoneCorrect")
            changeLane(nextLane)
        } else {
            Timber.d("wait for change lane message")
        }
    }

    // Public only for testing purpose VisibleForTesting does not work well here
    internal fun changeLane(lane: String) {
        UnityPlayer.UnitySendMessage("BrushController", "GoToLane", lane)
    }

    @VisibleForTesting(otherwise = VisibleForTesting.PROTECTED)
    fun createBrushingData(
        targetBrushingTime: Int,
        processedBrushingGetter: () -> Pair<Duration, ProcessedBrushing>?
    ): CreateBrushingData {
        var coverage = 0
        var processedData = "{}"
        var duration = Duration.ofMillis(0)

        shouldContinueProcessingData.set(false)

        try {
            processedBrushingGetter()?.let { (brushingDuration, processedBrushing) ->
                val checkupData = checkupCalculator.calculateCheckup(processedBrushing)
                coverage = checkupData.surfacePercentage
                processedData = processedBrushing.toJSON()
                duration = brushingDuration
            }
        } catch (e: RuntimeException) {
            FailEarly.fail("Exception during generation of processedBrushing in KML", e)
            null
        } ?: Timber.e("Impossible to process brushing data not enough data")

        val data = CreateBrushingData(
            GameApiConstants.GAME_GO_PIRATE,
            duration,
            targetBrushingTime,
            TrustedClock.getNowOffsetDateTime(),
            gold
        )

        data.coverage = coverage
        data.setProcessedData(processedData)

        return data
    }
}
