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
import io.reactivex.Flowable
import io.reactivex.Scheduler

internal open class CE1Driver : AraDriver {

    constructor(context: Context, mac: String, listener: KLTBDriverListener) : super(
        context,
        mac,
        listener
    )

    @VisibleForTesting
    constructor(
        bleManager: KLNordicBleManager,
        listener: KLTBDriverListener,
        bluetoothScheduler: Scheduler,
        mac: String,
        streamer: CharacteristicNotificationStreamer,
        notifyListenerScheduler: Scheduler
    ) : super(bleManager, listener, bluetoothScheduler, mac, streamer, notifyListenerScheduler)

    override fun toothbrushModel(): ToothbrushModel = ToothbrushModel.CONNECT_E1

    override fun overpressureStateFlowable(): Flowable<OverpressureState> =
        Flowable.error(CommandNotSupportedException())
}
