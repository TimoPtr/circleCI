package com.kolibree.android.sdk.core.toothbrush

import com.google.common.base.Optional
import com.kolibree.android.commons.AvailableUpdate
import com.kolibree.android.commons.ToothbrushModel
import com.kolibree.android.game.ToothbrushMac
import com.kolibree.android.sdk.connection.toothbrush.OtaUpdateEvent
import com.kolibree.android.sdk.connection.toothbrush.Toothbrush
import com.kolibree.android.sdk.connection.toothbrush.dsp.DspState
import com.kolibree.android.sdk.core.driver.ble.BleDriver
import com.kolibree.android.sdk.core.driver.ble.ParameterSet
import com.kolibree.android.sdk.core.ota.kltb003.KLTB003ToothbrushUpdater
import com.kolibree.android.sdk.error.CommandNotSupportedException
import com.kolibree.android.sdk.error.FailureReason
import com.kolibree.android.sdk.plaqless.DspStateUseCase
import com.kolibree.android.sdk.version.SoftwareVersion
import com.kolibree.kml.MouthZone16
import io.reactivex.Observable
import io.reactivex.Single
import javax.inject.Inject

/**
 * Third generation [Toothbrush] implementation.
 *
 *
 * M1 uses DFU library to update the firmware. It starts a DfuService and listens to callbacks to
 * react to events.
 *
 *
 * Rebooting to bootloader changes the device Mac to Mac + 1, so we don't need to disconnect
 * before starting the service
 */
internal class ToothbrushKLTB003Impl @Inject constructor(
    driver: BleDriver,
    @ToothbrushMac mac: Optional<String>,
    model: ToothbrushModel,
    toothbrushName: String,
    private val updater: KLTB003ToothbrushUpdater,
    private val dspStateUseCase: DspStateUseCase
) : ToothbrushBaseBleImpl(mac.get(), model, driver, toothbrushName) {

    override fun update(update: AvailableUpdate): Observable<OtaUpdateEvent> =
        updater.update(update)

    override fun minFwSupportingPlayLed() = minFwSupportingPlayLed

    override fun setSupervisedMouthZone(zone: MouthZone16, sequenceId: Byte): Single<Boolean> =
        Single.create { emitter ->
            try {
                val result = driver.setAndGetDeviceParameter(
                    ParameterSet.setSupervisedZonePayload(
                        zone,
                        sequenceId
                    )
                )
                    .skip(1)
                    .readBoolean()
                emitter.onSuccess(result)
            } catch (failureReason: FailureReason) {
                emitter.tryOnError(failureReason)
            }
        }

    override fun dspState(): Single<DspState> =
        if (model.hasDsp) {
            dspStateUseCase.dspStateSingle()
        } else {
            Single.error(CommandNotSupportedException("No DSP on this device"))
        }

    override fun isConnectionAllowed(): Boolean {
        return !updater.isUpdateInProgress()
    }

    companion object {

        /*
         * See https://docs.google.com/spreadsheets/d/1XyTFLQrZ9D2PUrbRZ-eSqEkjjRnL588rzCylTWBTOl0/edit#gid=506526620&range=J8
         */
        private val minFwSupportingPlayLed = SoftwareVersion(1, 4, 0)
    }
}
