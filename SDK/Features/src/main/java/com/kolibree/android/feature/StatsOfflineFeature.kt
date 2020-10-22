/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.feature

import androidx.annotation.Keep

@Keep
object StatsOfflineFeature : Feature<Boolean> {

    override val initialValue = true

    override val displayable = false

    override val displayName = "Calculate aggregated stats"

    override val requiresAppRestart = false
}
