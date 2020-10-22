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
object GuidedBrushingTipsFeature : Feature<Boolean> {

    override val initialValue: Boolean = false

    override val displayable: Boolean = true

    override val displayName: String = "Activate Guided Brushing Tips"

    override val requiresAppRestart: Boolean = false
}
