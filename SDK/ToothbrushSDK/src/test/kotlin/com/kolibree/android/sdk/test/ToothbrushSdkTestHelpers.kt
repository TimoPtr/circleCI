/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.sdk.test

import com.kolibree.android.sdk.core.binary.PayloadReader
import com.kolibree.android.sdk.core.driver.ble.BleDriver
import com.nhaarman.mockitokotlin2.whenever
import io.reactivex.subjects.SingleSubject

internal fun BleDriver.mockDvpCommand(payload: ByteArray): SingleSubject<PayloadReader> {
    return SingleSubject.create<PayloadReader>().apply {
        whenever(setAndGetDeviceParameterOnce(payload)).thenReturn(this)
    }
}
