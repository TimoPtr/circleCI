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
import com.kolibree.android.sdk.connection.detectors.data.RawSensorState
import com.kolibree.android.sdk.math.Vector

@Keep
fun createRawSensorState() =
    RawSensorState(0f, Vector(0f, 0f, 0f), Vector(0f, 0f, 0f), Vector(0f, 0f, 0f))
