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
object CoachPlusPlaqlessSupervisionFeature : Feature<Boolean> {

    override val initialValue: Boolean = true

    override val displayable: Boolean = true

    override val displayName: String = "Coach+ PQL Send supervision"

    override val requiresAppRestart: Boolean = false
}
