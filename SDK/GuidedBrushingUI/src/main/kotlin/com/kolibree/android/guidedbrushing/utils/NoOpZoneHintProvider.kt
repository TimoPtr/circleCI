/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.guidedbrushing.utils

import androidx.annotation.StringRes
import com.kolibree.android.coachplus.utils.ZoneHintProvider
import com.kolibree.android.guidedbrushing.R
import com.kolibree.kml.MouthZone16
import javax.inject.Inject

internal class NoOpZoneHintProvider @Inject constructor() : ZoneHintProvider {

    override fun provideHintForWrongZone(): Int = R.string.empty

    @StringRes
    override fun provideHintForZone(zone: MouthZone16): Int = R.string.empty
}
