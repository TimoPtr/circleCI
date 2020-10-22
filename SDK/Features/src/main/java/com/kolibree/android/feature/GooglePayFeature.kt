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
object GooglePayFeature : Feature<Boolean> {

    override val initialValue = false

    override val displayable = true

    override val displayName = "Show Checkout GPay"

    override val requiresAppRestart = false
}
