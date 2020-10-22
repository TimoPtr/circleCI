/*
 * Copyright (c) 2019 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.coachplus.controller.kml

import androidx.annotation.VisibleForTesting
import com.kolibree.android.clock.TrustedClock
import com.kolibree.android.coachplus.controller.BaseCoachPlusControllerImpl
import com.kolibree.android.coachplus.controller.CoachPlusControllerResult
import com.kolibree.android.coachplus.controller.ZoneDurationAdjuster
import com.kolibree.android.coachplus.feedback.CoachPlusFeedbackMapper
import com.kolibree.android.coachplus.feedback.FeedBackMessage
import com.kolibree.android.commons.GameApiConstants
import com.kolibree.android.failearly.FailEarly
import com.kolibree.android.processedbrushings.CheckupCalculator
import com.kolibree.android.processedbrushings.CheckupData
import com.kolibree.android.sdk.connection.detectors.data.OverpressureState
import com.kolibree.android.sdk.connection.detectors.data.PlaqlessRawSensorState
import com.kolibree.android.sdk.connection.detectors.data.PlaqlessSensorState
import com.kolibree.android.sdk.connection.detectors.data.RawSensorState
import com.kolibree.android.sdk.plaqless.PlaqlessError
import com.kolibree.kml.BrushingSession
import com.kolibree.kml.CharVector
import com.kolibree.kml.MouthZone16
import com.kolibree.kml.PauseStatus
import com.kolibree.kml.ProcessedBrushing
import com.kolibree.kml.ProcessedBrushing16
import com.kolibree.kml.RawData
import com.kolibree.kml.SupervisedBrushingAppContext16
import com.kolibree.kml.SupervisedCallback16
import com.kolibree.kml.SupervisedResult16
import com.kolibree.sdkws.data.model.CreateBrushingData
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicReference
import java.util.concurrent.locks.ReentrantLock
import javax.inject.Provider
import kotlin.math.roundToInt
import org.threeten.bp.Duration
import timber.log.Timber

// Class is open for test purpose (hacky solution because of KML)
internal open class CoachPlusKmlControllerImpl(
    goalBrushingDuration: Duration,
    private val tickPeriod: Long,
    private val maxFailTime: Long = DEFAULT_MAX_FAIL_TIME_MS,
    private val checkupCalculator: CheckupCalculator,
    private val supervisedBrushingAppContextProvider: Provider<SupervisedBrushingAppContext16>,
    private val coachPlusFeedbackMapper: CoachPlusFeedbackMapper,
    private val durationAdjuster: ZoneDurationAdjuster?
) : BaseCoachPlusControllerImpl(goalBrushingDuration) {
    @VisibleForTesting
    val zonePasses = LongArray(SEQUENCE.size)

    @VisibleForTesting
    val failTimes = LongArray(SEQUENCE.size)

    @VisibleForTesting
    val lastSupervisedResult16 = AtomicReference<SupervisedResult16>()

    @VisibleForTesting
    val supervisedBrushingAppContext =
        AtomicReference(supervisedBrushingAppContextProvider.get())

    /*
    This boolean ensure that we don't add data after we called processFullBrushing
     */
    @VisibleForTesting
    val shouldContinueProcessingData = AtomicBoolean(true)

    /**
     * We add some safety here by avoiding putting data while we want to reset the appContext,
     * We use a fair lock to avoid starving issue since onRawData can be called very often
     */
    protected val lock = ReentrantLock(true)

    @VisibleForTesting
    val plaqlessError = AtomicReference(PlaqlessError.NONE)

    // field is open and lazy for test purpose (hacky solution because of KML)
    @VisibleForTesting
    open val callback: SupervisedCallback16 by lazy {
        object : SupervisedCallback16() {
            override fun onSupervisedResult(result: SupervisedResult16) {
                lastSupervisedResult16.set(result)
            }
        }
    }

    @VisibleForTesting
    internal val hasCriticalPlaqlessError: Boolean
        get() = plaqlessError.get() == PlaqlessError.OUT_OF_MOUTH

    init {
        supervisedBrushingAppContext.get().start(callback)
    }

    override fun onSvmData(possibleZones: List<MouthZone16>) {
        // No op
    }

    override fun onRawData(isPlaying: Boolean, sensorState: RawSensorState) {
        onRawData(isPlaying, sensorState.convertToKmlRawData())
    }

    override fun onPlaqlessRawData(isPlaying: Boolean, sensorState: PlaqlessRawSensorState) {
        onRawData(isPlaying, sensorState.convertToKmlRawData())
    }

    override fun onPlaqlessData(isPlaying: Boolean, sensorState: PlaqlessSensorState) {
        if (shouldContinueProcessingData.get()) {
            lock.lock()

            try {
                plaqlessError.set(sensorState.plaqlessError)

                val pauseStatus =
                    if (isPlaying && !hasCriticalPlaqlessError) PauseStatus.Running else PauseStatus.InPause
                supervisedBrushingAppContext.get().addPlaqlessData(
                    sensorState.convertToKmlPlaqlessData(),
                    pauseStatus
                )
            } finally {
                lock.unlock()
            }
        } else {
            Timber.w("Trying to add events after processFullBrushing has been called")
        }
    }

    override fun onOverpressureState(overpressureState: OverpressureState) =
        if (shouldContinueProcessingData.get()) {
            lock.lock()

            try {
                supervisedBrushingAppContext.get().addOverpressureData(
                    overpressureState.detectorIsActive,
                    overpressureState.uiNotificationIsActive
                )
            } finally {
                lock.unlock()
            }
        } else {
            Timber.w("Trying to add events after processFullBrushing has been called")
        }

    // TODO handle notify https://kolibree.atlassian.net/browse/KLTB002-7639
    override fun onTick() = onBrushedResultWithFeedback()

    override fun notifyReconnection() {
        supervisedBrushingAppContext.get().notifyReconnection()
    }

    override fun onPause() {
        coachPlusFeedbackMapper.reset()
    }

    override fun computeBrushingDuration(): Int {
        var duration = 0L
        for (i in SEQUENCE.indices) {
            duration += zonePasses[i]
            duration += failTimes[i]
        }
        return TimeUnit.MILLISECONDS.toSeconds(duration).toInt()
    }

    override fun getAvroTransitionsTable(): IntArray =
        SEQUENCE
            .indices
            .map { zonePasses[it] + failTimes[it] }
            .map { it.toInt() }
            .toIntArray()

    override fun reset() {
        lock.lock()
        super.reset()
        for (i in SEQUENCE.indices) {
            zonePasses[i] = 0L
            failTimes[i] = 0L
        }

        supervisedBrushingAppContext.set(supervisedBrushingAppContextProvider.get())
        coachPlusFeedbackMapper.reset()
        lastSupervisedResult16.set(null)

        supervisedBrushingAppContext.get().start(callback)

        lock.unlock()
    }

    @VisibleForTesting
    fun onRawData(isPlaying: Boolean, rawData: RawData) {
        if (shouldContinueProcessingData.get()) {
            lock.lock()
            val pauseStatus =
                if (isPlaying && !hasCriticalPlaqlessError) PauseStatus.Running else PauseStatus.InPause
            supervisedBrushingAppContext.get().addRawData(
                rawData,
                pauseStatus,
                SEQUENCE[currentZoneIndex]
            )
            lock.unlock()
        } else {
            Timber.w("Trying to add events after processFullBrushing has been called")
        }
    }

    override fun createBrushingData(): CreateBrushingData {
        shouldContinueProcessingData.set(false)
        var checkupData: CheckupData? = null
        var processedData: String? = null
        lock.lock()

        supervisedBrushingAppContext.get().apply {
            if (isFullBrushingProcessingPossible) {
                try {
                    val processFullBrushing =
                        convertProcessBrushing16ToProcessBrushing(processFullBrushing())
                    checkupData = checkupCalculator.calculateCheckup(processFullBrushing)
                    processedData = processFullBrushing.toJSON()
                } catch (e: RuntimeException) {
                    FailEarly.fail("Exception during generation of processedBrushing in KML", e)
                }
            } else {
                Timber.e("Impossible to process brushing data not enough data")
            }
        }
        val data = CreateBrushingData(
            GameApiConstants.GAME_COACH_PLUS,
            checkupData?.duration ?: Duration.ZERO,
            getGoalBrushingDuration().seconds.toInt(),
            checkupData?.dateTime ?: TrustedClock.getNowOffsetDateTime(),
            0
        )
        checkupData?.surfacePercentage?.let {
            data.coverage = it
        }
        data.setProcessedData(processedData)
        lock.unlock()
        return data
    }

    fun kmlAvroData(brushingSession: BrushingSession): CharVector =
        supervisedBrushingAppContext
            .get()
            .getAvro(brushingSession)

    @VisibleForTesting
    fun isBrushingGoodZone(currentZone: MouthZone16): Boolean =
        lastSupervisedResult16.get()?.let { lastSupervisedResult16 ->
            lastSupervisedResult16.isZoneCorrect &&
                lastSupervisedResult16.zone == currentZone
        } ?: false

    @VisibleForTesting
    fun isDurationPerZoneExceeded(): Boolean = zonePasses[currentZoneIndex] >= getDurationPerZone()

    @VisibleForTesting
    fun isCurrentZoneCompleted(): Boolean = isDurationPerZoneExceeded()

    @VisibleForTesting
    fun onGoodZoneBrushed(feedBackMessage: FeedBackMessage): CoachPlusControllerResult {
        increaseTimes(zonePasses)
        if (isCurrentZoneCompleted()) {
            return if (hasMoreZones()) {
                val result = createResult(
                    currentZoneIndex,
                    brushingGoodZone = true,
                    sequenceFinished = false,
                    feedBackMessage = feedBackMessage
                )
                brushNextZone()
                result
            } else {
                createResult(
                    currentZoneIndex,
                    brushingGoodZone = true,
                    sequenceFinished = true,
                    feedBackMessage = feedBackMessage
                )
            }
        }

        return createResult(
            currentZoneIndex,
            brushingGoodZone = true,
            sequenceFinished = false,
            feedBackMessage = feedBackMessage
        )
    }

    @VisibleForTesting
    fun onWrongZoneBrushed(feedBackMessage: FeedBackMessage): CoachPlusControllerResult {
        increaseTimes(failTimes)
        if (shouldPreventGumDamage(currentZoneIndex)) {
            return if (hasMoreZones()) {
                val result = createResult(
                    currentZoneIndex,
                    brushingGoodZone = false,
                    sequenceFinished = false,
                    feedBackMessage = feedBackMessage
                )
                brushNextZone()
                result
            } else {
                createResult(
                    currentZoneIndex,
                    brushingGoodZone = false,
                    sequenceFinished = true,
                    feedBackMessage = feedBackMessage
                )
            }
        }
        return createResult(
            currentZoneIndex,
            shouldPreventFrustration(currentZoneIndex),
            false,
            feedBackMessage
        )
    }

    @VisibleForTesting
    fun increaseTimes(array: LongArray) {
        if (plaqlessError.get() != PlaqlessError.OUT_OF_MOUTH) {
            array[currentZoneIndex] += tickPeriod
        }
    }

    private fun createResult(
        currentIndex: Int,
        brushingGoodZone: Boolean,
        sequenceFinished: Boolean,
        feedBackMessage: FeedBackMessage
    ): CoachPlusControllerResult {
        return CoachPlusControllerResult(
            SEQUENCE[currentIndex],
            getCompletionPercent(currentIndex),
            brushingGoodZone,
            sequenceFinished,
            feedBackMessage
        )
    }

    private fun getCompletionPercent(currentZoneIndex: Int) =
        (zonePasses[currentZoneIndex] * 100f / getDurationPerZone())
            .roundToInt()

    private fun onBrushedResultWithFeedback(): CoachPlusControllerResult =
        if (!isBrushingGoodZone(SEQUENCE[currentZoneIndex])) {
            val feedback = if (shouldPreventFrustration(currentZoneIndex)) {
                coachPlusFeedbackMapper.onGoodZoneBrushing(
                    lastSupervisedResult16.get(),
                    SEQUENCE[currentZoneIndex],
                    plaqlessError.get()
                )
            } else {
                coachPlusFeedbackMapper.onWrongZoneBrushing(
                    lastSupervisedResult16.get(),
                    SEQUENCE[currentZoneIndex],
                    plaqlessError.get()
                )
            }
            onWrongZoneBrushed(feedback)
        } else {
            onGoodZoneBrushed(
                coachPlusFeedbackMapper.onGoodZoneBrushing(
                    lastSupervisedResult16.get(),
                    SEQUENCE[currentZoneIndex],
                    plaqlessError.get()
                )
            )
        }

    internal fun getDurationPerZone(): Long =
        durationAdjuster?.getAdjustedDuration(getCurrentZone()) ?: getGoalBrushingTimePerZone()

    // To prevent user frustration we don't show warning before TOLERANCE_TIME_MS when changing zone
    @VisibleForTesting
    fun shouldPreventFrustration(currentZoneIndex: Int): Boolean =
        failTimes[currentZoneIndex] <= TOLERANCE_TIME_MS &&
            zonePasses[currentZoneIndex] == 0L

    // To prevent gum damages we give up brushing a zone if the time spent on it goes over the
    // goal brushing time per zone + 5 seconds
    @VisibleForTesting
    fun shouldPreventGumDamage(currentZoneIndex: Int): Boolean =
        failTimes[currentZoneIndex] + zonePasses[currentZoneIndex] >
            getDurationPerZone() + maxFailTime

    @VisibleForTesting
    fun convertProcessBrushing16ToProcessBrushing(processedBrushing16: ProcessedBrushing16) =
        ProcessedBrushing(processedBrushing16)
}

internal const val CLEANNESS_FULL = 100
