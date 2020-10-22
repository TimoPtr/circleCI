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
object ShowMindYourSpeedFeature : Feature<Boolean> {

    override val initialValue = false

    override val displayable = true

    override val displayName = "Mind Your Speed/Enable activity"

    override val requiresAppRestart = true
}
