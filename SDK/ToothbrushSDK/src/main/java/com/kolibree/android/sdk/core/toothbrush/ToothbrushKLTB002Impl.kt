/*
 * Copyright (c) 2018 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 * Proprietary and confidential
 */

package com.kolibree.android.sdk.core.toothbrush

import androidx.annotation.VisibleForTesting
import com.google.common.base.Optional
import com.kolibree.android.commons.AvailableUpdate
import com.kolibree.android.commons.ToothbrushModel
import com.kolibree.android.sdk.connection.toothbrush.OtaUpdateEvent
import com.kolibree.android.sdk.connection.toothbrush.Toothbrush
import com.kolibree.android.sdk.connection.toothbrush.dsp.DspState
import com.kolibree.android.sdk.core.InternalKLTBConnection
import com.kolibree.android.sdk.core.driver.ble.BleDriver
import com.kolibree.android.sdk.core.driver.ble.ParameterSet
import com.kolibree.android.sdk.core.ota.kltb002.updater.KLTB002ToothbrushUpdater
import com.kolibree.android.sdk.error.CommandNotSupportedException
import com.kolibree.android.sdk.version.SoftwareVersion
import com.kolibree.kml.MouthZone16
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.Single

/** Created by miguelaragues on 27/4/18.  */
/**
 * [Toothbrush] implementation constructor
 *
 * @param mac non null toothbrush mac address
 * @param model non null device model
 * @param driver non null BleDriver
 * @param toothbrushName non null toothbrush bluetooth toothbrushName
 */
internal class ToothbrushKLTB002Impl
@VisibleForTesting
constructor(
    mac: String,
    model: ToothbrushModel,
    driver: BleDriver,
    toothbrushName: String,
    private val otaUpdater: KLTB002ToothbrushUpdater
) : ToothbrushBaseBleImpl(mac, model, driver, toothbrushName) {

    override fun update(update: AvailableUpdate): Observable<OtaUpdateEvent> =
        otaUpdater.update(update)

    override fun minFwSupportingPlayLed(): SoftwareVersion = MIN_FW_SUPPORTING_PLAY_LED

    override fun setSupervisedMouthZone(zone: MouthZone16, sequenceId: Byte): Single<Boolean> =
        Single.error(CommandNotSupportedException("Impossible to set supervisedZone on KLTB002"))

    override fun setAdvertisingIntervals(fastModeIntervalMs: Long, slowModeIntervalMs: Long) =
        Completable.fromAction {
            driver.setDeviceParameter(
                ParameterSet.setAdvertisingIntervalsPayload(fastModeIntervalMs, slowModeIntervalMs)
            )
        }

    override fun dspState(): Single<DspState> = Single.error(CommandNotSupportedException())

    companion object {

        @JvmStatic
        fun create(
            mac: Optional<String>,
            model: ToothbrushModel,
            driver: BleDriver,
            name: String,
            connection: InternalKLTBConnection
        ): ToothbrushKLTB002Impl {
            val kltb002Updater = KLTB002ToothbrushUpdater.create(connection, driver)

            return ToothbrushKLTB002Impl(mac.get(), model, driver, name, kltb002Updater)
        }

        /*
         * See
         * https://docs.google.com/spreadsheets/d/1XyTFLQrZ9D2PUrbRZ-eSqEkjjRnL588rzCylTWBTOl0/edit#gid=506526620&range=J8
         */
        private val MIN_FW_SUPPORTING_PLAY_LED = SoftwareVersion(0, 6, 0)
    }
}
