/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.sdk.connection.detectors.data

import androidx.annotation.Keep

/** Overpressure Sensor state */
@Keep
data class OverpressureState(
    val detectorIsActive: Boolean,
    val uiNotificationIsActive: Boolean
)
