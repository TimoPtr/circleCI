/*
 * Copyright (c) 2019 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */
package com.kolibree.android.sdk.core.driver.ble

import android.content.Context
import androidx.annotation.VisibleForTesting
import com.kolibree.android.commons.ToothbrushModel
import com.kolibree.android.sdk.connection.detectors.data.OverpressureState
import com.kolibree.android.sdk.core.driver.KLTBDriverListener
import com.kolibree.android.sdk.core.driver.ble.nordic.KLNordicBleManager
import com.kolibree.android.sdk.error.CommandNotSupportedException
import com.kolibree.android.sdk.version.SoftwareVersion
import io.reactivex.Flowable
import io.reactivex.Scheduler

// open for testing :-( See https://discuss.kotlinlang.org/t/how-to-test-protected-function/6133/17
internal open class CB1Driver : KolibreeBleDriver {
    constructor(
        context: Context,
        mac: String,
        listener: KLTBDriverListener
    ) : super(context, mac, listener)

    @VisibleForTesting
    internal constructor(
        bleManager: KLNordicBleManager,
        listener: KLTBDriverListener,
        bluetoothScheduler: Scheduler,
        mac: String,
        notificationCaster: CharacteristicNotificationStreamer,
        notifyListenerScheduler: Scheduler
    ) : super(
        bleManager,
        listener,
        bluetoothScheduler,
        mac,
        notificationCaster,
        notifyListenerScheduler
    )

    override fun calibrationDataSize(): Int = AraDriver.CALIBRATION_DATA_SIZE

    public override fun toothbrushModel(): ToothbrushModel = ToothbrushModel.CONNECT_B1

    override fun supportsReadingBootloader(): Boolean =
        super.supportsReadingBootloader() &&
            getFirmwareVersion().isNewerOrSame(READ_BOOTLOADER_SUPPORTED_FW)

    override fun supportsGRUData(): Boolean =
        !getFirmwareVersion().isNewerOrSame(RNN_NOT_SUPPORTED_FW)

    override fun supportsBrushingEventsPolling(): Boolean =
        getFirmwareVersion().isNewerOrSame(POLL_BRUSHING_EVENTS_FW)

    override fun overpressureStateFlowable(): Flowable<OverpressureState> =
        Flowable.error(CommandNotSupportedException())
}

private val READ_BOOTLOADER_SUPPORTED_FW = SoftwareVersion(1, 4, 9)
private val RNN_NOT_SUPPORTED_FW = SoftwareVersion(2, 0, 0)
private val POLL_BRUSHING_EVENTS_FW = SoftwareVersion(1, 6, 3)
