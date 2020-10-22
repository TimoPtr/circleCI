/*
 * Copyright (c) 2019 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.coachplus.controller

import androidx.annotation.Keep
import com.kolibree.android.sdk.connection.detectors.data.OverpressureState
import com.kolibree.android.sdk.connection.detectors.data.PlaqlessRawSensorState
import com.kolibree.android.sdk.connection.detectors.data.PlaqlessSensorState
import com.kolibree.android.sdk.connection.detectors.data.RawSensorState
import com.kolibree.kml.MouthZone16
import com.kolibree.sdkws.data.model.CreateBrushingData
import io.reactivex.Observable

/**
 * Coach+ brushing activity controller
 *
 * This class handles the logic of the Coach+ brushing activity and the processed data recording.
 */
@Keep
interface CoachPlusController {

    /**
     * To be called at every 'tick'. A tick is a unit of time that will be used for processed data
     * and for zone brushing times calculations.
     *
     * @return a CoachPlusControllerResult that contains the current state of the game.
     */
    fun onTick(): CoachPlusControllerResult

    /**
     * To be called when Coach plus enter in pause
     */
    fun onPause()

    /**
     * To be called on each SVM / RNN output. Make sure you keep the list order, only the first one
     * will be picked up.
     */
    fun onSvmData(possibleZones: List<MouthZone16>)

    /**
     * To be called on each RawData with the current status of coachPlus (pause or not)
     */
    fun onRawData(isPlaying: Boolean, sensorState: RawSensorState)

    /**
     * To be called on each PlaqlessRawData with the current status of coachPlus (pause or not)
     */
    fun onPlaqlessRawData(isPlaying: Boolean, sensorState: PlaqlessRawSensorState)

    /**
     * To be called on each PlaqlessData with the current status of coachPlus (pause or not)
     */
    fun onPlaqlessData(isPlaying: Boolean, sensorState: PlaqlessSensorState)

    /**
     * To be called when the device emits a new [OverpressureState]
     *
     * @param overpressureState [OverpressureState]
     */
    fun onOverpressureState(overpressureState: OverpressureState)

    /**
     * Create a CreateBrushingData object to be sent to the backend.
     *
     * @return CreateBrushingData object, with points, coins and quality (unused fields) set to 0.
     */
    fun createBrushingData(): CreateBrushingData

    /**
     * Get how many zones will require brushing passes.
     *
     * @return Int sequence zone count.
     */
    fun getSequenceLength(): Int

    /**
     * Get the MouthZone16 that the controller wants the user to brush.
     *
     * @return MouthZone16 current 'to be brushed' zone.
     */
    fun getCurrentZone(): MouthZone16

    /**
     * Computes the current brushing duration (without pauses) by summing the failed and well
     * brushed passes times for every zone in the sequence.
     *
     * @return Int brushing duration in seconds
     */
    fun computeBrushingDuration(): Int

    /**
     * Reset the controller to the state it was at its creation.
     */
    fun reset()

    /**
     * Get an Observable that will emit the current zone just after every zone change,
     *
     * @return Observable<SupervisionInfo> that will emit the new zone changes
     */
    val zoneChangeObservable: Observable<SupervisionInfo>

    /**
     * Compute the total time spent in each zone in milliseconds
     *
     * @return [IntArray] of total time spent in millis for each zone of the sequence
     */
    fun getAvroTransitionsTable(): IntArray

    /**
     * Notify that we reconnected to a Toothbrush after the connection was lost
     */
    fun notifyReconnection()
}

@Keep
data class SupervisionInfo(val zone: MouthZone16, val sequenceId: Byte)
