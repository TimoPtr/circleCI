/*
 * Copyright (c) 2019 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.test.mocks

import androidx.annotation.Keep
import com.kolibree.android.sdk.connection.detectors.data.PlaqlessRawSensorState
import com.kolibree.android.sdk.connection.detectors.data.PlaqlessSensorState
import com.kolibree.android.sdk.plaqless.PlaqlessError
import com.kolibree.android.test.utils.randomSigned16
import com.kolibree.android.test.utils.randomUnsigned8
import com.kolibree.android.test.utils.randomUnsignedSigned16
import io.reactivex.Flowable

private const val RAW_DATA_WINDOW_SIZE = 25

@Keep
fun singleWindowSensorState(): Flowable<PlaqlessSensorState> = Flowable.fromIterable(
    (0 until RAW_DATA_WINDOW_SIZE).map { plaqlessSensorState() }
)

@Keep
fun singleWindowRawSensorState(): Flowable<PlaqlessRawSensorState> = Flowable.fromIterable(
    (0 until RAW_DATA_WINDOW_SIZE).map { plaqlessRawSensorState() }
)

private fun plaqlessSensorState(): PlaqlessSensorState =
    PlaqlessSensorState(
        453L,
        randomUnsignedSigned16(),
        randomUnsignedSigned16(),
        randomUnsignedSigned16(),
        randomUnsigned8(),
        randomUnsigned8(),
        randomUnsigned8(),
        randomUnsigned8(),
        randomUnsigned8(),
        randomUnsigned8(),
        randomUnsigned8(),
        PlaqlessError.NONE
    )

private fun plaqlessRawSensorState(): PlaqlessRawSensorState {
    val accelX: Short = randomSigned16()
    val accelY: Short = randomSigned16()
    val accelZ: Short = randomSigned16()
    val gyroX: Short = randomSigned16()
    val gyroY: Short = randomSigned16()
    val gyroZ: Short = randomSigned16()

    return PlaqlessRawSensorState(
        relativeTimestampMillis = 453L,
        accelX = accelX,
        accelY = accelY,
        accelZ = accelZ,
        gyroX = gyroX,
        gyroY = gyroY,
        gyroZ = gyroZ
    )
}
