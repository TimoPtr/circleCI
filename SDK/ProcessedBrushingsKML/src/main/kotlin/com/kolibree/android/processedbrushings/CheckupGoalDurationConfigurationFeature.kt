/*
 * Copyright (c) 2019 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.processedbrushings

import androidx.annotation.Keep
import com.kolibree.android.feature.Feature
import com.kolibree.kml.KMLModule

private val kmlDefaultValue: Long by lazy { KMLModule.getDefaultGoalDurationPerZone16Milliseconds() }

@Keep
object CheckupGoalDurationConfigurationFeature : Feature<Long> {

    override val initialValue: Long = kmlDefaultValue

    override val displayable: Boolean = true

    override val displayName: String = "Goal Duration (ms)"

    override fun validate(newValue: Long): Boolean {
        return newValue > 0
    }

    override val requiresAppRestart: Boolean = true
}
