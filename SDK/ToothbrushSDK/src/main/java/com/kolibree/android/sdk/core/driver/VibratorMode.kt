package com.kolibree.android.sdk.core.driver

/**
 * Sets vibration mode
 *
 * See https://docs.google.com/spreadsheets/d/1XyTFLQrZ9D2PUrbRZ-eSqEkjjRnL588rzCylTWBTOl0/edit#gid=506526620&range=G4
 */
internal enum class VibratorMode {
    START,
    STOP,
    /**
     * Stop brushing + force recording stop.
     *
     * After vibration stop the FW will keep recording for 20 seconds. After 20 seconds, it stops the
     * recording and discards the last 20 seconds. This command tells the FW to stop recording immediately
     *
     * This behavior is supported even if the toothbrush isn't vibrating.
     *
     * <p>See https://jira.kolibree.com/browse/KLTB002-5725
     */
    STOP_AND_HALT_RECORDING
}
