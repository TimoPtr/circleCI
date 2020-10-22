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
object MindYourSpeedHideDotFeature : Feature<Boolean> {

    override val initialValue: Boolean = true

    override val displayable: Boolean = true

    override val displayName: String = "Mind Your Speed/Hide dot in wrong zone"

    override val requiresAppRestart: Boolean = false
}
