/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.coachplus.utils

import androidx.annotation.Keep
import androidx.annotation.StringRes
import com.kolibree.kml.MouthZone16

/**
 * Helper for matching a [MouthZone16] with its human readable description
 */
@Keep
interface ZoneHintProvider {

    @StringRes
    fun provideHintForWrongZone(): Int

    @StringRes
    fun provideHintForZone(zone: MouthZone16): Int
}
