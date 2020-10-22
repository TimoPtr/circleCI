package com.kolibree.android.sdk.core.driver

import androidx.annotation.Keep
import com.kolibree.android.sdk.connection.detectors.data.RawSensorState
import com.kolibree.android.sdk.connection.detectors.data.WeightedMouthZone
import com.kolibree.android.sdk.error.FailureReason
import com.kolibree.kml.MouthZone16

/**
 * Created by aurelien on 17/11/16.
 *
 *
 * Common interface between drivers and connections
 */
@Keep
interface KLTBDriverListener {
    /** Called when the driver is notified about bluetooth connection loss  */
    fun onDisconnected()

    /**
     * Called when the toothbrush outputs raw sensors data
     *
     * @param rawData non null RawSensorState
     */
    fun onSensorRawData(rawData: RawSensorState)

    /**
     * Notify vibrator state changes
     *
     * @param on true if on, false if off
     */
    fun onVibratorStateChanged(on: Boolean)

    /**
     * Called when the SVM detector outputs
     *
     * @param detection non null MouthZone16 list
     */
    fun onSVMDetection(detection: List<MouthZone16>)

    /**
     * Called when a brushing session start or stop
     *
     * @param started true when the session start, false otherwise
     */
    fun onBrushingSessionStateChanged(started: Boolean)

    /**
     * Called when the RNN detector outputs
     *
     * @param detection non null WeightedMouthZone list
     */
    fun onRNNDetection(detection: List<WeightedMouthZone>)

    /** Called when a GATT connection has been established and the handshake is beginning  */
    fun onConnectionEstablishing()

    @Throws(FailureReason::class)
    fun onConnectionEstablished()
}
