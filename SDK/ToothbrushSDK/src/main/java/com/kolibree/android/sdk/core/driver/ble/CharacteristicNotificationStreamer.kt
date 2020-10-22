/*
 * Copyright (c) 2019 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.sdk.core.driver.ble

import com.jakewharton.rxrelay2.PublishRelay
import com.kolibree.android.sdk.core.driver.ble.gatt.GattCharacteristic
import io.reactivex.BackpressureStrategy
import io.reactivex.Flowable

/**
 * Class to cast data from GattCharacteristics
 */
internal class CharacteristicNotificationStreamer {
    private val bleNotificationDataRelay = PublishRelay.create<BleNotificationData>()

    fun onNewData(bleNotificationData: BleNotificationData) {
        bleNotificationDataRelay.accept(bleNotificationData)
    }

    /**
     * Creates a [Flowable]<[ByteArray]> of data emitted by [gattCharacteristic]
     *
     * Multiple invocations will receive different instances
     *
     * @return [Flowable]<[ByteArray]> emitting [ByteArray] emitted by [gattCharacteristic] in
     * [notificationChangedStream]
     */
    @JvmOverloads
    inline fun characteristicStream(
        gattCharacteristic: GattCharacteristic,
        crossinline onSubscribeBlock: () -> Unit = {},
        crossinline onCancelBlock: () -> Unit = {}
    ): Flowable<ByteArray> {
        return bleNotificationDataRelay
            .toFlowable(BackpressureStrategy.BUFFER)
            .filter { it.characteristicUUID == gattCharacteristic.UUID }
            .map { it.response }
            .doOnSubscribe { onSubscribeBlock.invoke() }
            .doOnCancel { onCancelBlock.invoke() }
    }
}
