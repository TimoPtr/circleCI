/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.pirate.controller.kml

import androidx.annotation.VisibleForTesting
import com.kolibree.android.pirate.controller.World3Constant.KLPirateLevel3PrescribedZone
import com.kolibree.android.pirate.controller.WorldController
import com.kolibree.android.processedbrushings.CheckupCalculator
import com.kolibree.android.sdk.connection.detectors.data.RawSensorState
import com.kolibree.kml.MouthZone16
import com.kolibree.kml.PauseStatus
import com.kolibree.kml.PirateHelper
import com.kolibree.kml.ProcessedBrushing
import com.kolibree.kml.ProcessedBrushing16
import com.kolibree.kml.SupervisedBrushingAppContext16
import com.kolibree.kml.SupervisedCallback16
import com.kolibree.kml.SupervisedResult16
import com.kolibree.sdkws.data.model.CreateBrushingData
import java.util.concurrent.atomic.AtomicReference
import org.threeten.bp.Duration
import timber.log.Timber

internal class World3KmlController(
    isRightHand: Boolean,
    checkupCalculator: CheckupCalculator,
    private val appContext: SupervisedBrushingAppContext16,
    pirateHelper: PirateHelper
) : WorldController(isRightHand, checkupCalculator) {

    @VisibleForTesting
    val currentZone = AtomicReference<MouthZone16>()

    private val callback: SupervisedCallback16 by lazy {
        object : SupervisedCallback16() {
            override fun onSupervisedResult(result: SupervisedResult16) {
                maybeChangeLane(pirateHelper.getLane(result).toString(), result.zone.name, result.isZoneCorrect)
            }
        }
    }

    // Hack to be able to unit test this class (because of SWIG)
    @VisibleForTesting
    fun getInternalCallback(): SupervisedCallback16 = callback

    @VisibleForTesting
    fun convertProcessBrushing16ToProcessBrushing(processedBrushing16: ProcessedBrushing16) =
        ProcessedBrushing(processedBrushing16)

    override fun init(targetBrushingTime: Int) {
        appContext.start(getInternalCallback())
    }

    override fun setPrescribedZoneId(prescribedZoneId: Int) {
        val zone = KLPirateLevel3PrescribedZone.values()[prescribedZoneId].toMouthZone()
        zone?.let {
            currentZone.set(it)
            waitingForChangeLaneMessage.set(true)
            Timber.i("change zone to ${currentZone.get().name}")
        }
    }

    override fun onRawData(sensorState: RawSensorState) {
        if (shouldContinueProcessingData.get()) {
            val pauseStatus = if (isPlaying.get()) PauseStatus.Running else PauseStatus.InPause
            if (currentZone.get() != null) {
                appContext.addRawData(sensorState.convertToKmlRawData(), pauseStatus, currentZone.get())
            }
        }
    }

    override fun getBrushingData(targetBrushingTime: Int): CreateBrushingData {
        // WARNING : You should not call super for this method because base
        // class contains legacy code that doesn't work with KML

        return createBrushingData(targetBrushingTime) {
            if (appContext.isFullBrushingProcessingPossible) {
                val processFullBrushing = appContext.processFullBrushing()
                Pair(
                    Duration.ofMillis(processFullBrushing.durationInMilliseconds),
                    convertProcessBrushing16ToProcessBrushing(processFullBrushing)
                )
            } else {
                null
            }
        }
    }
}
