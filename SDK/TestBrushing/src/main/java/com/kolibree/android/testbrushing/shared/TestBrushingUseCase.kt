/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.testbrushing.shared

import androidx.annotation.VisibleForTesting
import com.kolibree.android.annotation.VisibleForApp
import com.kolibree.android.commons.DEFAULT_BRUSHING_GOAL
import com.kolibree.android.commons.GameApiConstants
import com.kolibree.android.failearly.FailEarly
import com.kolibree.android.game.bi.Contract.ActivityName.FREE_BRUSHING
import com.kolibree.android.game.bi.KmlAvroCreator
import com.kolibree.android.game.sensors.GameSensorListener
import com.kolibree.android.processedbrushings.CheckupCalculator
import com.kolibree.android.processedbrushings.CheckupData
import com.kolibree.android.processedbrushings.exception.ProcessedBrushingNotAvailableException
import com.kolibree.android.sdk.connection.KLTBConnection
import com.kolibree.android.sdk.connection.detectors.data.OverpressureState
import com.kolibree.android.sdk.connection.detectors.data.PlaqlessRawSensorState
import com.kolibree.android.sdk.connection.detectors.data.PlaqlessSensorState
import com.kolibree.android.sdk.connection.detectors.data.RawSensorState
import com.kolibree.kml.FreeBrushingAppContext
import com.kolibree.kml.MouthZone16
import com.kolibree.kml.PauseStatus
import com.kolibree.sdkws.data.model.CreateBrushingData
import io.reactivex.Single
import io.reactivex.schedulers.Schedulers
import java.util.concurrent.atomic.AtomicBoolean
import javax.inject.Inject

@VisibleForApp
interface TestBrushingUseCase : GameSensorListener {

    fun createBrushingData(connection: KLTBConnection): Single<CreateBrushingData>

    override fun currentZone(): MouthZone16? = null

    override fun onSVMData(source: KLTBConnection, data: MutableList<MouthZone16>) {
        FailEarly.fail("onSVMData is not supported by HumTestBrushingUseCase")
    }

    fun notifyReconnection()
}

internal class TestBrushingUseCaseImpl @Inject constructor(
    private val checkupCalculator: CheckupCalculator,
    private val appContext: FreeBrushingAppContext,
    @VisibleForTesting internal val avroCreator: KmlAvroCreator
) : TestBrushingUseCase {

    @VisibleForTesting
    internal val started = AtomicBoolean(false)

    override fun onRawData(isPlaying: Boolean, sensorState: RawSensorState) {
        withPauseStatus(isPlaying) { pauseStatus ->
            appContext.addRawData(sensorState.convertToKmlRawData(), pauseStatus)
        }
    }

    override fun onPlaqlessData(isPlaying: Boolean, sensorState: PlaqlessSensorState) {
        withPauseStatus(isPlaying) { pauseStatus ->
            appContext.addPlaqlessData(sensorState.convertToKmlPlaqlessData(), pauseStatus)
        }
    }

    override fun onPlaqlessRawData(isPlaying: Boolean, sensorState: PlaqlessRawSensorState) {
        withPauseStatus(isPlaying) { pauseStatus ->
            appContext.addRawData(sensorState.convertToKmlRawData(), pauseStatus)
        }
    }

    override fun onOverpressureState(overpressureState: OverpressureState) {
        if (!started.getAndSet(true)) {
            appContext.start()
        }

        appContext.addOverpressureData(
            overpressureState.detectorIsActive,
            overpressureState.uiNotificationIsActive
        )
    }

    override fun notifyReconnection() {
        appContext.notifyReconnection()
    }

    private inline fun withPauseStatus(isPlaying: Boolean, execute: (PauseStatus) -> Unit) {
        if (!started.getAndSet(true)) {
            appContext.start()
        }

        val pauseStatus = if (isPlaying) PauseStatus.Running else PauseStatus.InPause
        execute(pauseStatus)
    }

    override fun createBrushingData(connection: KLTBConnection): Single<CreateBrushingData> =
        Single.fromCallable {
            if (!appContext.isFullBrushingProcessingPossible)
                throw ProcessedBrushingNotAvailableException()

            val processedBrushing16 = appContext.processFullBrushing()
            val processedBrushing = processedBrushing16.toProcessedBrushing()
            val processedData = processedBrushing.toJSON()
            val checkupData = checkupCalculator.calculateCheckup(processedBrushing)

            val brushingData = initBrushingData(checkupData)
            brushingData.coverage = checkupData.surfacePercentage
            brushingData.setProcessedData(processedData)
            brushingData
        }.subscribeOn(Schedulers.io())
            .flatMap { brushingData ->
                avroCreator.createBrushingSession(connection, FREE_BRUSHING)
                    .flatMapCompletable { avroCreator.submitAvroData(appContext.getAvro(it)) }
                    .andThen(Single.just(brushingData))
                    .onErrorReturn { brushingData }
            }

    @VisibleForTesting
    fun initBrushingData(checkupData: CheckupData) = CreateBrushingData(
        GameApiConstants.GAME_SBA,
        checkupData.duration,
        DEFAULT_BRUSHING_GOAL,
        checkupData.dateTime,
        checkupData.surfacePercentage
    )
}
