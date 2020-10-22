/*
 * Copyright (c) 2019 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.angleandspeed.common.logic

import androidx.annotation.VisibleForTesting
import com.kolibree.android.angleandspeed.common.logic.model.AngleAndSpeedFeedback
import com.kolibree.android.angleandspeed.common.logic.model.AngleFeedback
import com.kolibree.android.angleandspeed.common.logic.model.SpeedFeedback
import com.kolibree.android.sdk.connection.detectors.data.OverpressureState
import com.kolibree.android.sdk.connection.detectors.data.RawSensorState
import com.kolibree.kml.AnglesAndSpeedAppContext
import com.kolibree.kml.EulerAnglesDegrees
import com.kolibree.kml.MouthZone16
import com.kolibree.kml.MouthZone16Vector
import com.kolibree.kml.PauseStatus
import com.kolibree.kml.SpeedKPI
import com.kolibree.kml.get
import io.reactivex.Flowable
import io.reactivex.processors.PublishProcessor
import java.util.concurrent.atomic.AtomicReference
import java.util.concurrent.locks.ReentrantLock
import javax.inject.Provider

internal class AngleAndSpeedUseCaseImpl(
    anglesAndSpeedAppContextProvider: Provider<AnglesAndSpeedAppContext>
) : AngleAndSpeedUseCase,
    RawDataSensorListener {

    private val angleAndSpeedProcessor: PublishProcessor<AngleAndSpeedFeedback> =
        PublishProcessor.create()

    override val angleAndSpeedFlowable: Flowable<AngleAndSpeedFeedback> =
        angleAndSpeedProcessor.onBackpressureLatest()

    @VisibleForTesting
    var prescribedZones: MouthZone16Vector? = null

    private val anglesAndSpeedAppContext =
        AtomicReference(anglesAndSpeedAppContextProvider.get())

    private val lock = ReentrantLock(true)

    override fun onRawData(isPlaying: Boolean, sensorState: RawSensorState) {
        lock.lock()
        if (prescribedZones != null) {

            val pauseStatus = if (isPlaying) PauseStatus.Running else PauseStatus.InPause

            val isResultAvailable = anglesAndSpeedAppContext.get().addRawData(
                sensorState.convertToKmlRawData(),
                pauseStatus,
                prescribedZones
            )

            if (isResultAvailable) {
                val result = anglesAndSpeedAppContext.get().lastResult
                if (isPlaying) {
                    val feedback =
                        AngleAndSpeedFeedback(
                            angleDegrees = fromKml(result.orientationDegrees),
                            speedFeedback = fromKml(result.optionalKpi.get()?.speedCorrectness),
                            isZoneCorrect = result.optionalKpi.get()?.isOrientationCorrect
                                ?: throw IllegalStateException("result doesnt have KPI")
                        )

                    angleAndSpeedProcessor.onNext(feedback)
                }
            }
        }
        lock.unlock()
    }

    override fun setPrescribedZones(prescribedZones: Array<MouthZone16>) {
        lock.lock()
        this.prescribedZones = MouthZone16Vector(prescribedZones)
        lock.unlock()
    }

    override fun onOverpressureState(overpressureState: OverpressureState) {
        // no-op
    }

    @VisibleForTesting fun fromKml(eulerAnglesDegrees: EulerAnglesDegrees) =
        AngleFeedback(
            roll = eulerAnglesDegrees.roll,
            pitch = eulerAnglesDegrees.pitch,
            yaw = eulerAnglesDegrees.yaw
        )

    @VisibleForTesting fun fromKml(speedKpi: SpeedKPI?): SpeedFeedback = when (speedKpi) {
        SpeedKPI.Correct -> SpeedFeedback.CORRECT
        SpeedKPI.Overspeed -> SpeedFeedback.OVERSPEED
        SpeedKPI.Underspeed -> SpeedFeedback.UNDERSPEED
        else -> throw IllegalArgumentException("Unknown SpeedKPI value $speedKpi")
    }
}
